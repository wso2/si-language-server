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
package io.siddhi.langserver.core.utils.metadata;

import java.util.Arrays;
import java.util.List;

/**
 * For storing Processor and ExpressionExecutor related meta data Used in JSON responses.
 *
 * @since 1.0.0
 */
public class ProcessorMetaData {

    private String name;
    private String namespace;
    private String description;
    private List<ParameterMetaData> parameters;
    private List<String[]> parameterOverloads;
    private List<AttributeMetaData> returnAttributes;
    private String[] examples;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String[] getExamples() {
        return (examples != null) ? Arrays.copyOf(examples, examples.length) : new String[0];
    }

    public void setExamples(String[] examples) {
        this.examples = (examples != null) ? Arrays.copyOf(examples, examples.length) : null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ParameterMetaData> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterMetaData> parameters) {
        this.parameters = parameters;
    }

    public List<AttributeMetaData> getReturnAttributes() {
        return returnAttributes;
    }

    public void setReturnAttributes(List<AttributeMetaData> returnAttributes) {
        this.returnAttributes = returnAttributes;
    }

    public List<String[]> getParameterOverloads() {
        return parameterOverloads;
    }

    public void setParameterOverloads(List<String[]> parameterOverloads) {
        this.parameterOverloads = parameterOverloads;
    }

}
