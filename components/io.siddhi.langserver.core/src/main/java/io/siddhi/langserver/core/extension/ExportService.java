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

import io.siddhi.langserver.core.response.ExportResponse;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.wso2.carbon.config.ConfigProviderFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.siddhi.editor.core.commons.request.ExportAppsRequest;
import org.wso2.carbon.siddhi.editor.core.exception.DockerGenerationException;
import org.wso2.carbon.siddhi.editor.core.exception.KubernetesGenerationException;
import org.wso2.carbon.siddhi.editor.core.internal.ExportUtils;
import org.wso2.carbon.siddhi.editor.core.vscode.PublicExportUtils;
import org.wso2.carbon.utils.Utils;

import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

/**
 * Provides services for exporting Siddhi applications to Docker and Kubernetes environments.
 *
 * @since 1.0.0
 */
@JsonSegment("export")
public class ExportService extends ExtensionService {

    private ConfigProvider configProvider = null;
    private static final String EXPORT_TYPE_DOCKER = "docker";

    /**
     * Exports the application to a Docker image.
     *
     * @param exportAppsRequest the request containing export configurations
     * @return a CompletableFuture containing the export response
     */
    @JsonRequest
    public CompletableFuture<ExportResponse> exportDocker(ExportAppsRequest exportAppsRequest) {
        return CompletableFuture.supplyAsync(() -> {
            String errorMessage = "";
            try {
                initializeConfigProvider();
                ExportUtils exportUtils =
                        PublicExportUtils.getExportUtils(this.configProvider,
                                exportAppsRequest, EXPORT_TYPE_DOCKER);
                exportUtils.createZipFile();
                return new ExportResponse(true, errorMessage);
            } catch (DockerGenerationException | ConfigurationException | KubernetesGenerationException e) {
                errorMessage = "Exception caught while generating Docker export artifacts. " + e.getMessage();
            }
            return new ExportResponse(false, errorMessage);
        });
    }

    private void initializeConfigProvider() throws ConfigurationException {
        if (configProvider == null) {
            configProvider = ConfigProviderFactory.getConfigProvider(
                    Paths.get(Utils.getCarbonConfigHome().toString(), "server", "deployment.yaml"));
        }
    }
}
