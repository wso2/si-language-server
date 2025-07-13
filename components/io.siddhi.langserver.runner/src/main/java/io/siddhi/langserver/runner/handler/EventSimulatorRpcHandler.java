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
package io.siddhi.langserver.runner.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.siddhi.langserver.core.LSEventSimulatorDataHolder;
import io.siddhi.langserver.runner.request.FeedSimulationRequest;
import io.siddhi.langserver.runner.request.SingleEventRequest;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.carbon.event.simulator.core.api.NotFoundException;
import org.wso2.carbon.event.simulator.core.api.SingleApiService;
import org.wso2.carbon.event.simulator.core.factories.FeedApiServiceFactory;
import org.wso2.carbon.event.simulator.core.factories.SingleApiServiceFactory;
import org.wso2.carbon.event.simulator.core.impl.FeedApiServiceImpl;

import java.io.IOException;

/**
 * Handles event simulator-related JSON-RPC requests for Siddhi language server runner.
 *
 * @since 1.0.0
 */
public class EventSimulatorRpcHandler {

    private final Gson gson = new Gson();

    public String handleRequest(JsonObject params, String method, int id) {
        try {
            switch (method) {
                case "singleEvent":
                    return simulateSingleEvent(gson.fromJson(params, SingleEventRequest.class), id);
                case "feedSimulation":
                    return simulateEventFeed(gson.fromJson(params, FeedSimulationRequest.class), id);
                default:
                    return Utils.createErrorResponse("Method not found", id);
            }
        } catch (JsonParseException e) {
            return Utils.createErrorResponse("Invalid JSON", null);
        }
    }

    private String simulateSingleEvent(SingleEventRequest singleEventParams, int id) {
        SingleApiService singleApiService = SingleApiServiceFactory.getSingleApi();
        try {
            singleApiService.runSingleSimulation(singleEventParams.body);
            return Utils.createSuccessResponse("Simulation completed", id);
        } catch (NotFoundException e) {
            return Utils.createErrorResponse(e.getMessage(), id);
        }
    }

    private String simulateEventFeed(FeedSimulationRequest feedSimulationParams, int id) {
        FeedApiServiceImpl feedApiService = (FeedApiServiceImpl) FeedApiServiceFactory.getFeedApi();
        try {
            LSEventSimulatorDataHolder.INSTANCE.activateSimulationConfig(feedSimulationParams.simulationName);
            feedApiService.operateFeedSimulation(feedSimulationParams.action, feedSimulationParams.simulationName);
            return Utils.createSuccessResponse("Simulation completed", id);
        } catch (NotFoundException | CarbonDeploymentException | IOException e) {
            return Utils.createErrorResponse(e.getMessage(), id);
        }
    }
}
