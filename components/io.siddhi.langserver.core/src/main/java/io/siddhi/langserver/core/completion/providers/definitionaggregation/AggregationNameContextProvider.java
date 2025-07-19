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

package io.siddhi.langserver.core.completion.providers.definitionaggregation;

import io.siddhi.langserver.core.completion.providers.CompletionProvider;
import io.siddhi.query.compiler.SiddhiQLParser;
import org.eclipse.lsp4j.CompletionItem;

import java.util.List;

/**
 * Provides Completions for AggregationNameContext.
 * {@link io.siddhi.query.compiler.SiddhiQLParser.Aggregation_nameContext}
 *
 * @since 1.0.0
 */
public class AggregationNameContextProvider extends CompletionProvider {

    public AggregationNameContextProvider() {
        this.providerName = SiddhiQLParser.Aggregation_nameContext.class.getName();
    }

    @Override
    public List<CompletionItem> getCompletions() {
        return generateCompletionList(null);
    }
}
