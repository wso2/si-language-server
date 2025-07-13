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
package io.siddhi.langserver.core.response;

import java.util.ArrayList;
import java.util.List;

/**
 * Response class for stream list queries.
 *
 * @since 1.0.0
 */
public class StreamResponse {

    public List<String> streamNames;

    public StreamResponse() {
        this.streamNames = new ArrayList<>();
    }

    public void addStream(String stream) {
        this.streamNames.add(stream);
    }
}
