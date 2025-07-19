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

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.langserver.core.LSEventSimulatorDataHolder;
import io.siddhi.langserver.core.LSEventStreamService;
import io.siddhi.langserver.core.LSOperationContext;
import io.siddhi.langserver.core.request.AddSimulationRequest;
import io.siddhi.langserver.core.request.DeleteFeedSimulationRequest;
import io.siddhi.langserver.core.request.DeleteFileRequest;
import io.siddhi.langserver.core.request.GetDBColumnRequest;
import io.siddhi.langserver.core.request.GetFeedSimulationRequest;
import io.siddhi.langserver.core.request.GetFeedSimulationStatusRequest;
import io.siddhi.langserver.core.request.GetSimulationsRequest;
import io.siddhi.langserver.core.request.GetStreamAttributeRequest;
import io.siddhi.langserver.core.request.GetStreamsRequest;
import io.siddhi.langserver.core.request.UpdateSimulationRequest;
import io.siddhi.langserver.core.response.EventSimulatorInitializeResponse;
import io.siddhi.langserver.core.response.SimulationConfigResponse;
import io.siddhi.langserver.core.response.SimulatorResponse;
import io.siddhi.langserver.core.response.StreamDefinitionResponse;
import io.siddhi.langserver.core.response.StreamResponse;
import io.siddhi.query.api.definition.StreamDefinition;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.json.JSONObject;
import org.wso2.carbon.event.simulator.core.api.NotFoundException;
import org.wso2.carbon.event.simulator.core.exception.FileOperationsException;
import org.wso2.carbon.event.simulator.core.exception.SimulationValidationException;
import org.wso2.carbon.event.simulator.core.factories.DatabaseApiServiceFactory;
import org.wso2.carbon.event.simulator.core.factories.FeedApiServiceFactory;
import org.wso2.carbon.event.simulator.core.factories.FilesApiServiceFactory;
import org.wso2.carbon.event.simulator.core.impl.DatabaseApiServiceImpl;
import org.wso2.carbon.event.simulator.core.impl.FeedApiServiceImpl;
import org.wso2.carbon.event.simulator.core.impl.FilesApiServiceImpl;
import org.wso2.carbon.event.simulator.core.internal.util.SimulationConfigUploader;
import org.wso2.carbon.event.simulator.core.model.DBConnectionModel;
import org.wso2.carbon.streaming.integrator.common.exception.ResponseMapper;
import org.wso2.msf4j.formparam.FileInfo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.ws.rs.core.Response;

/**
 * Provides services for event simulation and simulation configuration management in Siddhi.
 *
 * @since 1.0.0
 */
@JsonSegment("eventSimulator")
public class EventSimulatorService extends ExtensionService {

    private final FilesApiServiceImpl filesApiService = (FilesApiServiceImpl) FilesApiServiceFactory.getFilesApi();
    private final DatabaseApiServiceImpl databaseApiService =
            (DatabaseApiServiceImpl) DatabaseApiServiceFactory.getConnectToDatabaseApi();
    private final FeedApiServiceImpl feedApi = (FeedApiServiceImpl) FeedApiServiceFactory.getFeedApi();
    private boolean eventSimulatorInitialized = false;

    @JsonRequest
    public CompletableFuture<EventSimulatorInitializeResponse> initializeEventSimulator() {
        return CompletableFuture.supplyAsync(() -> {
            if (!eventSimulatorInitialized) {
                try {
                    LSEventSimulatorDataHolder.INSTANCE.initializeEventSimulatorDataHolder(new LSEventStreamService());
                    LSEventSimulatorDataHolder.INSTANCE.loadSimulationConfigurations();
                    eventSimulatorInitialized = true;
                } catch (IOException e) {
                    return new EventSimulatorInitializeResponse(false, e.getMessage());
                }
            }
            return new EventSimulatorInitializeResponse(true, "Event Simulator initialized");
        });
    }

    @JsonRequest
    public CompletableFuture<StreamResponse> getStreams(GetStreamsRequest getStreamsRequest) {
        return CompletableFuture.supplyAsync(() -> {
            SiddhiAppRuntime siddhiAppRuntime =
                    getSiddhiAppRuntime(getStreamsRequest.siddhiAppUri, getStreamsRequest.siddhiApp);
            Map<String, StreamDefinition> streamDefinitionMap = siddhiAppRuntime.getStreamDefinitionMap();
            StreamResponse streamResponse = new StreamResponse();
            streamDefinitionMap.forEach((key, value) -> {
                streamResponse.addStream(key);
            });
            return streamResponse;
        });
    }

    @JsonRequest
    public CompletableFuture<StreamDefinitionResponse> getStreamAttributes(
            GetStreamAttributeRequest getStreamAttributeRequest) {
        return CompletableFuture.supplyAsync(() -> {
            SiddhiAppRuntime siddhiAppRuntime = getSiddhiAppRuntime(getStreamAttributeRequest.siddhiAppUri,
                    getStreamAttributeRequest.siddhiAppString);
            String streamName = getStreamAttributeRequest.streamName;
            StreamDefinition streamDefinition = siddhiAppRuntime.getStreamDefinitionMap().get(streamName);
            return new StreamDefinitionResponse(streamDefinition.getAttributeList(), streamName);
        });
    }

    @JsonRequest
    public CompletableFuture<SimulatorResponse> addFeedSimulation(AddSimulationRequest simulationAddRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String simulationConfig = simulationAddRequest.simulationConfig;
                Response response = feedApi.addFeedSimulation(simulationConfig);
                String simulationName =
                        SimulationConfigUploader.getConfigUploader().getSimulationName(simulationConfig);
                String siddhiAppName = LSEventSimulatorDataHolder.getSiddhiAppName(
                        (new JSONObject(simulationConfig)).getJSONArray("sources"));
                LSEventSimulatorDataHolder.INSTANCE.addConfigToSiddhiAppConfigs(siddhiAppName, simulationName);
                return buildSimulatorResponse(response);
            } catch (NotFoundException | SimulationValidationException e) {
                return new SimulatorResponse(false, e.getMessage());
            }
        });
    }

    @JsonRequest
    public CompletableFuture<SimulationConfigResponse> getFeedSimulation(
            GetFeedSimulationRequest feedSimulationRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Response feedSimulation = feedApi.getFeedSimulation(feedSimulationRequest.simulationName);
                ResponseMapper entity = (ResponseMapper) feedSimulation.getEntity();
                return new SimulationConfigResponse(entity.getMessage(), buildSimulatorResponse(feedSimulation));
            } catch (NotFoundException e) {
                return new SimulationConfigResponse(null, new SimulatorResponse(false, e.getMessage()));
            }
        });
    }

    @JsonRequest
    public CompletableFuture<SimulationConfigResponse> getFeedSimulations(GetSimulationsRequest simulationsGetRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<JSONObject> activeSimulations =
                        LSEventSimulatorDataHolder.INSTANCE.getSimulationConfigs(simulationsGetRequest.siddhiApps);
                JSONObject response = new JSONObject();
                response.put("activeSimulations", activeSimulations);
                response.put("inActiveSimulations", Collections.emptyList());
                return new SimulationConfigResponse(response.toString(), new SimulatorResponse(true, ""));
            } catch (IOException e) {
                return new SimulationConfigResponse(null, new SimulatorResponse(false, e.getMessage()));
            }
        });
    }

    @JsonRequest
    public CompletableFuture<SimulatorResponse> updateFeedSimulation(UpdateSimulationRequest simulationUpdateRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return buildSimulatorResponse(feedApi.updateFeedSimulation(simulationUpdateRequest.simulationName,
                        simulationUpdateRequest.simulationConfig));
            } catch (NotFoundException | FileOperationsException e) {
                return new SimulatorResponse(false, "");
            }
        });
    }

    @JsonRequest
    public CompletableFuture<SimulatorResponse> deleteFeedSimulation(
            DeleteFeedSimulationRequest deleteFeedSimulationRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return buildSimulatorResponse(
                        feedApi.deleteFeedSimulation(deleteFeedSimulationRequest.simulationName));
            } catch (NotFoundException e) {
                return new SimulatorResponse(false, "");
            }
        });
    }

    @JsonRequest
    public CompletableFuture<SimulationConfigResponse> getFeedSimulationStatus(
            GetFeedSimulationStatusRequest feedSimulationStatusRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Response response = feedApi.getFeedSimulationStatus(feedSimulationStatusRequest.simulationName);
                ResponseMapper entity = (ResponseMapper) response.getEntity();
                return new SimulationConfigResponse(entity.getMessage(), buildSimulatorResponse(response));
            } catch (NotFoundException e) {
                return new SimulationConfigResponse(null, new SimulatorResponse(false, e.getMessage()));
            }
        });
    }

    @JsonRequest
    public CompletableFuture<SimulatorResponse> uploadFile(FileInfo fileDetail) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path fileAbsolutePath = Paths.get(fileDetail.getFileName());
                fileDetail.setFileName(fileAbsolutePath.getFileName().toString());
                return buildSimulatorResponse(
                        filesApiService.uploadFile(Files.newInputStream(fileAbsolutePath), fileDetail));
            } catch (NotFoundException | IOException | FileOperationsException e) {
                return new SimulatorResponse(false, e.getMessage());
            }
        });
    }

    @JsonRequest
    public CompletableFuture<SimulationConfigResponse> getFileNames() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Response response = filesApiService.getFileNames();
                return new SimulationConfigResponse((response.getEntity()).toString(),
                        buildSimulatorResponse(response));
            } catch (NotFoundException e) {
                return new SimulationConfigResponse(null, new SimulatorResponse(false, e.getMessage()));
            }
        });
    }

    @JsonRequest
    public CompletableFuture<SimulatorResponse> updateFile(FileInfo fileDetail) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String fileName = fileDetail.getFileName();
                return buildSimulatorResponse(
                        filesApiService.updateFile(fileName, Files.newInputStream(Paths.get(fileName)), fileDetail));
            } catch (NotFoundException | FileOperationsException | IOException e) {
                return new SimulatorResponse(false, e.getMessage());
            }
        });
    }

    @JsonRequest
    public CompletableFuture<SimulatorResponse> deleteFile(DeleteFileRequest deleteFileRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return buildSimulatorResponse(filesApiService.deleteFile(deleteFileRequest.fileName));
            } catch (NotFoundException | FileOperationsException e) {
                return new SimulatorResponse(false, e.getMessage());
            }
        });
    }

    @JsonRequest
    public CompletableFuture<SimulatorResponse> testDBConnection(DBConnectionModel dbConnectionModel) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return buildSimulatorResponse(databaseApiService.testDBConnection(dbConnectionModel));
            } catch (NotFoundException e) {
                return new SimulatorResponse(false, e.getMessage());
            }
        });
    }

    @JsonRequest
    public CompletableFuture<SimulationConfigResponse> getDatabaseTableColumns(GetDBColumnRequest dbColumnGetRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                DBConnectionModel dbConnectionModel = new DBConnectionModel().password(dbColumnGetRequest.password).
                        username(dbColumnGetRequest.username).driver(dbColumnGetRequest.driver)
                        .dataSourceLocation(dbColumnGetRequest.dataSourceLocation);
                Response response =
                        databaseApiService.getDatabaseTableColumns(dbConnectionModel, dbColumnGetRequest.tableName);
                return new SimulationConfigResponse(response.getEntity().toString(), buildSimulatorResponse(response));
            } catch (NotFoundException e) {
                return new SimulationConfigResponse(null, new SimulatorResponse(false, e.getMessage()));
            }
        });
    }

    @JsonRequest
    public CompletableFuture<SimulationConfigResponse> getDatabaseTables(DBConnectionModel dbConnectionModel) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Response response = databaseApiService.getDatabaseTables(dbConnectionModel);
                return new SimulationConfigResponse(response.getEntity().toString(), buildSimulatorResponse(response));
            } catch (NotFoundException e) {
                return new SimulationConfigResponse(null, new SimulatorResponse(false, e.getMessage()));
            }
        });
    }

    private SimulatorResponse buildSimulatorResponse(Response response) {
        int statusCode = response.getStatus();
        boolean isSuccess = statusCode >= 200 && statusCode < 300;

        String message;
        Object entity = response.getEntity();

        if (entity instanceof ResponseMapper) {
            message = ((ResponseMapper) entity).getMessage();
        } else if (entity != null) {
            message = entity.toString();
        } else {
            message = isSuccess ? "Operation successful." : "Unknown error occurred.";
        }

        return new SimulatorResponse(isSuccess, message);
    }

    private SiddhiAppRuntime getSiddhiAppRuntime(String siddhiAppUri, String encodedSiddhiApp) {
        String siddhiAppName = Paths.get(siddhiAppUri).getFileName().toString();
        if (LSOperationContext.INSTANCE.checkIfSiddhiAppRuntimeExists(siddhiAppName)) {
            return LSOperationContext.INSTANCE.getSiddhiAppRuntime(siddhiAppName);
        }
        String siddhiAppString = new String(Base64.getDecoder().decode(encodedSiddhiApp), StandardCharsets.UTF_8);
        SiddhiAppRuntime siddhiAppRuntime =
                LSOperationContext.INSTANCE.getSiddhiManager().createSiddhiAppRuntime(siddhiAppString);
        LSOperationContext.INSTANCE.addSiddhiAppRuntime(siddhiAppName, siddhiAppRuntime);
        return siddhiAppRuntime;
    }
}
