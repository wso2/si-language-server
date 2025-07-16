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

package io.siddhi.langserver.core.extension;

import io.siddhi.langserver.core.request.GetDependencySharingExtensionsRequest;
import io.siddhi.langserver.core.request.GetDependencyStatusesRequest;
import io.siddhi.langserver.core.request.GetExtensionStatusRequest;
import io.siddhi.langserver.core.request.InstallDependenciesRequest;
import io.siddhi.langserver.core.request.UninstallDependenciesRequest;
import io.siddhi.langserver.core.response.ExtensionInstallerInitializeResponse;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.wso2.carbon.siddhi.extensions.installer.core.config.mapping.ConfigMapper;
import org.wso2.carbon.siddhi.extensions.installer.core.config.mapping.models.ExtensionConfig;
import org.wso2.carbon.siddhi.extensions.installer.core.constants.ExtensionsInstallerConstants;
import org.wso2.carbon.siddhi.extensions.installer.core.exceptions.ExtensionsInstallerException;
import org.wso2.carbon.siddhi.extensions.installer.core.execution.DependencyInstaller;
import org.wso2.carbon.siddhi.extensions.installer.core.execution.DependencyInstallerImpl;
import org.wso2.carbon.siddhi.extensions.installer.core.execution.DependencyRetriever;
import org.wso2.carbon.siddhi.extensions.installer.core.execution.DependencyRetrieverImpl;
import org.wso2.carbon.siddhi.extensions.installer.core.execution.SiddhiAppExtensionUsageDetector;
import org.wso2.carbon.siddhi.extensions.installer.core.execution.SiddhiAppExtensionUsageDetectorImpl;
import org.wso2.carbon.siddhi.extensions.installer.core.models.SiddhiAppStore;
import org.wso2.carbon.siddhi.extensions.installer.core.util.MissingExtensionsInstaller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Provides services for managing Siddhi extension installations and statuses.
 *
 * @since 1.0.0
 */
@JsonSegment("extensionInstaller")
public class SiddhiExtensionInstallerService extends ExtensionService {

    private Map<String, ExtensionConfig> extensionConfigs;
    private final SiddhiAppStore siddhiAppStore;
    private boolean initialized = false;

    public SiddhiExtensionInstallerService() {
        this.siddhiAppStore = new SiddhiAppStore();
    }

    @JsonRequest
    public CompletableFuture<Object> initializeExtensionInstaller() {
        return CompletableFuture.supplyAsync(() -> {
            if (!initialized) {
                try {
                    this.extensionConfigs =
                            ConfigMapper.loadAllExtensionConfigs(ExtensionsInstallerConstants.CONFIG_FILE_LOCATION);
                    initialized = true;
                } catch (ExtensionsInstallerException e) {
                    return new ExtensionInstallerInitializeResponse(false, e.getMessage());
                }
            }
            return new ExtensionInstallerInitializeResponse(true, "Extension Installer initialized successfully");
        });
    }

    @JsonRequest
    public CompletableFuture<Object> getAllExtensionStatuses() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                DependencyRetriever retriever = new DependencyRetrieverImpl(extensionConfigs);
                return retriever.getAllExtensionStatuses(false);
            } catch (ExtensionsInstallerException e) {
                return errorResponse(e);
            }
        });
    }

    @JsonRequest
    public CompletableFuture<Object> getExtensionStatus(GetExtensionStatusRequest extensionStatusRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                DependencyRetriever retriever = new DependencyRetrieverImpl(extensionConfigs);
                return retriever.getExtensionStatusFor(extensionStatusRequest.extensionName);
            } catch (ExtensionsInstallerException e) {
                return errorResponse(e);
            }
        });
    }

    @JsonRequest
    public CompletableFuture<Object> getDependencyStatuses(GetDependencyStatusesRequest dependencyStatusesRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                DependencyRetriever retriever = new DependencyRetrieverImpl(extensionConfigs);
                return retriever.getDependencyStatusesFor(dependencyStatusesRequest.extensionName);
            } catch (ExtensionsInstallerException e) {
                return errorResponse(e);
            }
        });
    }

    @JsonRequest
    public CompletableFuture<Object> getDependencySharingExtensions(
            GetDependencySharingExtensionsRequest dependencySharingExtensionsRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                DependencyRetriever retriever = new DependencyRetrieverImpl(extensionConfigs);
                return retriever.getDependencySharingExtensionsFor(dependencySharingExtensionsRequest.extensionName);
            } catch (ExtensionsInstallerException e) {
                return errorResponse(e);
            }
        });
    }

    @JsonRequest
    public CompletableFuture<Object> installDependencies(InstallDependenciesRequest installDependenciesRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                DependencyInstaller installer = new DependencyInstallerImpl(extensionConfigs);
                return installer.installDependenciesFor(installDependenciesRequest.extensionName);
            } catch (ExtensionsInstallerException e) {
                return errorResponse(e);
            }
        });
    }

    @JsonRequest
    public CompletableFuture<Object> uninstallDependencies(UninstallDependenciesRequest uninstallDependenciesRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                DependencyInstaller installer = new DependencyInstallerImpl(extensionConfigs);
                Map<String, Object> result =
                        installer.unInstallDependenciesFor(uninstallDependenciesRequest.extensionName);
                return result;
            } catch (ExtensionsInstallerException e) {
                return errorResponse(e);
            }
        });
    }

    @JsonRequest
    public CompletableFuture<Object> installMissingExtensions() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                SiddhiAppExtensionUsageDetector usageDetector =
                        new SiddhiAppExtensionUsageDetectorImpl(extensionConfigs);
                DependencyInstaller installer = new DependencyInstallerImpl(extensionConfigs);
                return MissingExtensionsInstaller.installMissingExtensions(siddhiAppStore, usageDetector, installer);
            } catch (ExtensionsInstallerException e) {
                return errorResponse(e);
            }
        });
    }

    private Map<String, Object> errorResponse(Exception e) {
        Map<String, Object> map = new HashMap<>();
        map.put("error", true);
        map.put("message", e.getMessage());
        return map;
    }

    private void copyFileIfNotExists(Path sourceFile, Path targetDirectory) throws IOException {
        if (!Files.exists(sourceFile)) {
            throw new NoSuchFileException("Source file does not exist: " + sourceFile);
        }

        if (!Files.isDirectory(targetDirectory)) {
            throw new NotDirectoryException("Target is not a directory: " + targetDirectory);
        }

        Path targetFile = targetDirectory.resolve(sourceFile.getFileName());

        if (!Files.exists(targetFile)) {
            Files.copy(sourceFile, targetFile);
        }
    }
}
