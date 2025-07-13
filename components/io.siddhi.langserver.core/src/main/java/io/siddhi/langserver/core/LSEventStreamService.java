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
import io.siddhi.core.event.Event;
import io.siddhi.core.stream.input.InputHandler;
import io.siddhi.query.api.definition.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.streaming.integrator.common.EventStreamService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service class for managing Siddhi event streams in the language server.
 *
 * @since 1.0.0
 */
public class LSEventStreamService implements EventStreamService {

    private static final Logger log = LoggerFactory.getLogger(LSEventStreamService.class);

    public List<String> getStreamNames(String siddhiAppName) {
        SiddhiAppRuntime siddhiAppRuntime = LSOperationContext.INSTANCE.getSiddhiAppRuntime(siddhiAppName);
        return new ArrayList<>(siddhiAppRuntime.getStreamDefinitionMap().keySet());
    }

    public List<Attribute> getStreamAttributes(String siddhiAppName, String streamName) {
        SiddhiAppRuntime siddhiAppRuntime = LSOperationContext.INSTANCE.getSiddhiAppRuntime(siddhiAppName);
        if (siddhiAppRuntime.getStreamDefinitionMap().containsKey(streamName)) {
            return (siddhiAppRuntime.getStreamDefinitionMap().get(streamName)).getAttributeList();
        } else {
            log.error("Siddhi App '{}' does not contain stream '{}'.", siddhiAppName, streamName);
            return Collections.emptyList();
        }
    }

    public void pushEvent(String siddhiAppName, String streamName, Event event) {
        SiddhiAppRuntime siddhiAppRuntime = LSOperationContext.INSTANCE.getSiddhiAppRuntime(siddhiAppName);
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler(streamName);

        try {
            inputHandler.send(event);
        } catch (InterruptedException e) {
            log.error("Error when pushing events to Siddhi engine ", e);
        }
    }
}
