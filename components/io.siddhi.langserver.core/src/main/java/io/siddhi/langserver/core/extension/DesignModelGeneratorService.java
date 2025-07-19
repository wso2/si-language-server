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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.siddhi.core.exception.SiddhiAppCreationException;
import io.siddhi.langserver.core.LSOperationContext;
import io.siddhi.langserver.core.request.GetDesignViewRequest;
import io.siddhi.langserver.core.request.GetSourceCodeRequest;
import io.siddhi.langserver.core.response.DesignModelResponse;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.wso2.carbon.siddhi.editor.core.commons.metadata.MetaData;
import org.wso2.carbon.siddhi.editor.core.commons.response.MetaDataResponse;
import org.wso2.carbon.siddhi.editor.core.commons.response.Status;
import org.wso2.carbon.siddhi.editor.core.util.SourceEditorUtils;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.EventFlow;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.CodeGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.deserializers.DeserializersRegisterer;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.DesignGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.DesignGenerationException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Provides services for generating design models and source code for Siddhi applications.
 *
 * @since 1.0.0
 */
@JsonSegment("flowDesignService")
public class DesignModelGeneratorService extends ExtensionService {

    private final DesignGenerator designGenerator;
    private final CodeGenerator codeGenerator;
    private static final Gson DEFAULT_GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public DesignModelGeneratorService() {
        this.designGenerator = new DesignGenerator();
        this.designGenerator.setSiddhiManager(LSOperationContext.INSTANCE.getSiddhiManager());
        this.codeGenerator = new CodeGenerator();
    }

    @JsonRequest
    public CompletableFuture<DesignModelResponse> getDesignView(GetDesignViewRequest getDesignViewRequest) {
        return CompletableFuture.supplyAsync(() -> {
            DesignModelResponse designModelResponse = new DesignModelResponse();
            try {
                String siddhiAppString = decodeBase64(getDesignViewRequest.value);
                EventFlow eventFlow = this.designGenerator.getEventFlow(siddhiAppString);
                String eventFlowJson = DEFAULT_GSON.toJson(eventFlow);
                designModelResponse.setContent(encodeBase64(eventFlowJson));
                return designModelResponse;
            } catch (SiddhiAppCreationException | DesignGenerationException e) {
                designModelResponse.setError(e);
            }
            return designModelResponse;
        });
    }

    @JsonRequest
    public CompletableFuture<DesignModelResponse> getSourceCode(GetSourceCodeRequest sourceCodeRequest) {
        return CompletableFuture.supplyAsync(() -> {
            DesignModelResponse designModelResponse = new DesignModelResponse();
            try {
                String eventFlowJson = decodeBase64(sourceCodeRequest.designJson);
                Gson gson = DeserializersRegisterer.getGsonBuilder().disableHtmlEscaping().create();
                EventFlow eventFlow = gson.fromJson(eventFlowJson, EventFlow.class);
                String siddhiAppCode = codeGenerator.generateSiddhiAppCode(eventFlow);
                designModelResponse.setContent(encodeBase64(siddhiAppCode));
            } catch (CodeGenerationException e) {
                designModelResponse.setError(e);
            }
            return designModelResponse;
        });
    }

    @JsonRequest
    public CompletableFuture<DesignModelResponse> getMetaData() {
        return CompletableFuture.supplyAsync(() -> {
            DesignModelResponse designModelResponse = new DesignModelResponse();
            MetaDataResponse response = new MetaDataResponse(Status.SUCCESS);
            Map<String, MetaData> extensions = SourceEditorUtils.getExtensionProcessorMetaData();
            response.setInBuilt(extensions.remove(""));
            response.setExtensions(extensions);
            designModelResponse.setContent(new Gson().toJson(response));
            return designModelResponse;
        });
    }

    private String decodeBase64(String encoded) {
        byte[] decodedBytes = Base64.getDecoder().decode(encoded);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    private String encodeBase64(String content) {
        byte[] encodedBytes = Base64.getEncoder().encode(content.getBytes(StandardCharsets.UTF_8));
        return new String(encodedBytes, StandardCharsets.UTF_8);
    }
}
