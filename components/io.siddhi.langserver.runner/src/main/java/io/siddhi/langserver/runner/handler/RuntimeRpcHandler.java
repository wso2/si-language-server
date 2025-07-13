package io.siddhi.langserver.runner.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.langserver.core.LSEventSimulatorDataHolder;
import io.siddhi.langserver.runner.request.StartRequest;
import io.siddhi.langserver.runner.simulator.SimulatorEventStreamService;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Handles runtime-related JSON-RPC requests for Siddhi language server runner.
 *
 * @since 1.0.0
 */
public class RuntimeRpcHandler {

    private final SiddhiManager siddhiManager = new SiddhiManager();
    private final Gson gson = new Gson();
    private SiddhiAppRuntime siddhiAppRuntime = null;
    private String siddhiAppName = null;

    public String handleRequest(JsonObject params, String action, int id) {
        try {
            switch (action) {
                case "start":
                    return handleStart(gson.fromJson(params, StartRequest.class), id);
                case "stop":
                    return handleStop(id);
                default:
                    return Utils.createErrorResponse("Method not found", id);
            }
        } catch (JsonParseException e) {
            return Utils.createErrorResponse("Invalid JSON", null);
        }
    }

    private String handleStart(StartRequest startParams, int id) {
        if (startParams == null || startParams.path == null) {
            return Utils.createErrorResponse("Missing parameter 'path'", id);
        }

        try {
            Path path = Paths.get(startParams.path);
            String siddhiApp = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            this.siddhiAppName = path.getFileName().toString();
            int index = this.siddhiAppName.lastIndexOf(".siddhi");
            this.siddhiAppName = (index > 0) ? this.siddhiAppName.substring(0, index) : this.siddhiAppName;
            siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
            siddhiAppRuntime.start();
            initializeEventSimulator();
            return Utils.createSuccessResponse("Siddhi app started", id);
        } catch (IOException | CarbonDeploymentException e) {
            return Utils.createErrorResponse("Failed to read Siddhi file: " + e.getMessage(), id);
        }
    }

    private String handleStop(int id) {
        if (siddhiAppRuntime != null) {
            siddhiAppRuntime.shutdown();
            siddhiAppRuntime = null;
            return Utils.createSuccessResponse("Siddhi app stopped", id);
        }
        return Utils.createErrorResponse("No Siddhi app is currently running", id);
    }

    private void initializeEventSimulator() throws IOException, CarbonDeploymentException {
        LSEventSimulatorDataHolder lsEventSimulatorDataHolder = LSEventSimulatorDataHolder.INSTANCE;
        lsEventSimulatorDataHolder.initializeEventSimulatorDataHolder(
                new SimulatorEventStreamService(this.siddhiAppRuntime));
    }

}
