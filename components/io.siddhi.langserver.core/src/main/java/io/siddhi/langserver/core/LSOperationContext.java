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

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.langserver.core.diagnostic.DiagnosticProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * A local context for Siddhi language server operations and runtime management.
 *
 * @since 1.0.0
 */
public class LSOperationContext {

    public static final LSOperationContext INSTANCE = new LSOperationContext();
    private final SiddhiManager siddhiManager;
    private final DiagnosticProvider diagnosticProvider;
    private final Map<String, SiddhiAppRuntime> siddhiAppRuntimeMap = new HashMap<>();
    private SiddhiLanguageServer siddhiLanguageServer = null;

    private LSOperationContext() {
        this.diagnosticProvider = DiagnosticProvider.getInstance();
        this.siddhiManager = new SiddhiManager();
    }

    public SiddhiLanguageServer getSiddhiLanguageServer() {
        return this.siddhiLanguageServer;
    }

    public void setSiddhiLanguageServer(SiddhiLanguageServer siddhiLanguageServer) {
        this.siddhiLanguageServer = siddhiLanguageServer;
    }

    public SiddhiManager getSiddhiManager() {
        return this.siddhiManager;
    }

    public SiddhiAppRuntime getSiddhiAppRuntime(String siddhiAppName) {
        return this.siddhiAppRuntimeMap.get(siddhiAppName);
    }

    public void addSiddhiAppRuntime(String siddhiAppName, SiddhiAppRuntime siddhiAppRuntime) {
        siddhiAppRuntimeMap.put(siddhiAppName, siddhiAppRuntime);
    }

    public boolean checkIfSiddhiAppRuntimeExists(String siddhiAppName) {
        return siddhiAppRuntimeMap.containsKey(siddhiAppName);
    }

    public DiagnosticProvider getDiagnosticProvider() {
        return diagnosticProvider;
    }
}
