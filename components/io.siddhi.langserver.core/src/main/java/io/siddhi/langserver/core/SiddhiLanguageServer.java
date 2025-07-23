/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.siddhi.langserver.core;

import io.siddhi.langserver.core.extension.DesignModelGeneratorService;
import io.siddhi.langserver.core.extension.EventSimulatorService;
import io.siddhi.langserver.core.extension.ExportService;
import io.siddhi.langserver.core.extension.SiddhiExtensionInstallerService;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.json.JsonRpcMethod;
import org.eclipse.lsp4j.jsonrpc.json.JsonRpcMethodProvider;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.wso2.carbon.siddhi.editor.core.internal.EditorDataHolder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Siddhi Language Server implementation providing language analytic capabilities for Siddhi application development.
 *
 * @since 1.0.0
 */
public class SiddhiLanguageServer implements LanguageServer, Endpoint, JsonRpcMethodProvider {

    private final DesignModelGeneratorService designModelGeneratorService;
    private final EventSimulatorService eventSimulatorService;
    private final SiddhiExtensionInstallerService siddhiExtensionInstallerService;
    private final ExportService exportService;
    private final Map<String, Endpoint> extensionServices = new HashMap<>();
    private LanguageClient client;
    private SiddhiTextDocumentService textDocumentService;
    private SiddhiWorkspaceService workspaceService;
    private int shutDownStatus = 1;
    private Map<String, JsonRpcMethod> supportedMethods;

    public SiddhiLanguageServer() {
        LSOperationContext.INSTANCE.setSiddhiLanguageServer(this);
        EditorDataHolder.setSiddhiManager(LSOperationContext.INSTANCE.getSiddhiManager());
        this.textDocumentService = new SiddhiTextDocumentService();
        this.workspaceService = new SiddhiWorkspaceService();
        this.designModelGeneratorService = new DesignModelGeneratorService();
        this.eventSimulatorService = new EventSimulatorService();
        this.siddhiExtensionInstallerService = new SiddhiExtensionInstallerService();
        this.exportService = new ExportService();

    }

    /**
     * Set the client instance to which the diagnostics are pushed.
     *
     * @param languageClient
     */
    public void connect(LanguageClient languageClient) {
        this.client = languageClient;
        //todo: Initiate loggers once the logging framework is implemented.
    }

    /**
     * This method binds the language server with server options.
     *
     * @param initializeParams an object which comprises initialization options for the language server.
     * @return {@link InitializeResult} object which comprises the capabilities of the language server.
     */
    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams initializeParams) {
        final CompletionOptions completionOptions = new CompletionOptions();
        completionOptions.setTriggerCharacters(Arrays.asList("#", "@"));
        final InitializeResult initializedResult = new InitializeResult(new ServerCapabilities());
        initializedResult.getCapabilities().setTextDocumentSync(TextDocumentSyncKind.Full);
        initializedResult.getCapabilities().setCompletionProvider(completionOptions);
        return CompletableFuture.supplyAsync(() -> initializedResult);
    }

    /**
     * Shuts the language server down.
     *
     * @return private LSCompletionContext completionContext; {@link Object} a new Object instance.
     */
    @Override
    public CompletableFuture<Object> shutdown() {
        this.shutDownStatus = 0;
        return CompletableFuture.supplyAsync(Object::new);
    }

    @Override
    public void exit() {
        System.exit(shutDownStatus);
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return this.textDocumentService;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return this.workspaceService;
    }

    public LanguageClient getClient() {
        return this.client;
    }

    @Override
    public Map<String, JsonRpcMethod> supportedMethods() {
        if (this.supportedMethods != null) {
            return this.supportedMethods;
        }
        synchronized (this.extensionServices) {
            Map<String, JsonRpcMethod> supportedMethods =
                    new LinkedHashMap<>(ServiceEndpoints.getSupportedMethods(getClass()));
            Map<String, JsonRpcMethod> supportedExtensions = designModelGeneratorService.supportedMethods();
            Endpoint designModelEndpoint = ServiceEndpoints.toEndpoint(designModelGeneratorService);
            for (Map.Entry<String, JsonRpcMethod> entry : supportedExtensions.entrySet()) {
                this.extensionServices.put(entry.getKey(), designModelEndpoint);
                supportedMethods.put(entry.getKey(), entry.getValue());
            }
            supportedExtensions = eventSimulatorService.supportedMethods();
            Endpoint eventSimulatorEndpoint = ServiceEndpoints.toEndpoint(eventSimulatorService);
            for (Map.Entry<String, JsonRpcMethod> entry : supportedExtensions.entrySet()) {
                this.extensionServices.put(entry.getKey(), eventSimulatorEndpoint);
                supportedMethods.put(entry.getKey(), entry.getValue());
            }
            supportedExtensions = siddhiExtensionInstallerService.supportedMethods();
            Endpoint runtimeEndpoint = ServiceEndpoints.toEndpoint(siddhiExtensionInstallerService);
            for (Map.Entry<String, JsonRpcMethod> entry : supportedExtensions.entrySet()) {
                this.extensionServices.put(entry.getKey(), runtimeEndpoint);
                supportedMethods.put(entry.getKey(), entry.getValue());
            }
            supportedExtensions = exportService.supportedMethods();
            Endpoint exportEndpoint = ServiceEndpoints.toEndpoint(exportService);
            for (Map.Entry<String, JsonRpcMethod> entry : supportedExtensions.entrySet()) {
                this.extensionServices.put(entry.getKey(), exportEndpoint);
                supportedMethods.put(entry.getKey(), entry.getValue());
            }
            this.supportedMethods = supportedMethods;
            return supportedMethods;
        }
    }

    @Override
    public CompletableFuture<?> request(String method, Object parameter) {
        if (!extensionServices.containsKey(method)) {
            throw new UnsupportedOperationException("The json request '" + method + "' is unknown.");
        }
        return extensionServices.get(method).request(method, parameter);
    }

    @Override
    public void notify(String method, Object parameter) {
        if (extensionServices.containsKey(method)) {
            extensionServices.get(method).notify(method, parameter);
        }
    }

}
