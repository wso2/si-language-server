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
package io.siddhi.langserver.runner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.siddhi.langserver.runner.handler.EventSimulatorRpcHandler;
import io.siddhi.langserver.runner.handler.RuntimeRpcHandler;
import io.siddhi.langserver.runner.handler.Utils;

import java.util.Arrays;
import java.util.List;

/**
 * Handles JSON-RPC requests for Siddhi language server runner.
 *
 * @since 1.0.0
 */
public class SiddhiJsonRpcHandler {

    private final Gson gson = new Gson();
    private final EventSimulatorRpcHandler eventSimulatorRpcHandler = new EventSimulatorRpcHandler();
    private final RuntimeRpcHandler runtimeRpcHandler = new RuntimeRpcHandler();

    public String handleRequest(String jsonRequest) {
        try {
            JsonObject request = gson.fromJson(jsonRequest, JsonObject.class);
            JsonObject params = request.getAsJsonObject("params");
            int id = request.get("id").getAsInt();
            String method = request.get("method").getAsString();

            List<String> parts = Arrays.asList(method.split("/"));
            if (parts.size() != 2) {
                return Utils.createErrorResponse("Invalid method format", null);
            }

            String domain = parts.get(0);
            String action = parts.get(1);
            switch (domain) {
                case "runtime":
                    return runtimeRpcHandler.handleRequest(params, action, id);
                case "eventSimulator":
                    return eventSimulatorRpcHandler.handleRequest(params, action, id);
                default:
                    return Utils.createErrorResponse("Method not found", id);
            }
        } catch (JsonParseException e) {
            return Utils.createErrorResponse("Invalid JSON", null);
        }
    }
}
