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

    @JsonRequest
    public CompletableFuture<DesignModelResponse> getDesignView(GetDesignViewRequest getDesignViewRequest) {
        return CompletableFuture.supplyAsync(() -> {
            DesignModelResponse designModelResponse = new DesignModelResponse();
            try {
                DesignGenerator designGenerator = new DesignGenerator();
                designGenerator.setSiddhiManager(LSOperationContext.INSTANCE.getSiddhiManager());
                String siddhiAppString =
                        new String(Base64.getDecoder().decode(getDesignViewRequest.value), StandardCharsets.UTF_8);
                EventFlow eventFlow = designGenerator.getEventFlow(siddhiAppString);
                Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
                String eventFlowJson = gson.toJson(eventFlow);
                byte[] encodedBytes = Base64.getEncoder().encode(eventFlowJson.getBytes(StandardCharsets.UTF_8));
                String encodedString = new String(encodedBytes, StandardCharsets.UTF_8);
                designModelResponse.setContent(encodedString);
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
                String eventFlowJson =
                        new String(Base64.getDecoder().decode(sourceCodeRequest.designJson), StandardCharsets.UTF_8);
                Gson gson = DeserializersRegisterer.getGsonBuilder().disableHtmlEscaping().create();
                EventFlow eventFlow = gson.fromJson(eventFlowJson, EventFlow.class);
                CodeGenerator codeGenerator = new CodeGenerator();
                String siddhiAppCode = codeGenerator.generateSiddhiAppCode(eventFlow);

                String encodedSiddhiAppString =
                        new String(Base64.getEncoder().encode(siddhiAppCode.getBytes(StandardCharsets.UTF_8)),
                                StandardCharsets.UTF_8);
                designModelResponse.setContent(encodedSiddhiAppString);
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
            String jsonString = new Gson().toJson(response);
            designModelResponse.setContent(jsonString);
            return designModelResponse;
        });
    }
}
