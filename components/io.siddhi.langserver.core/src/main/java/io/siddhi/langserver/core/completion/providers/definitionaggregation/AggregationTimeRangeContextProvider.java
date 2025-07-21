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

import io.siddhi.langserver.core.completion.LSCompletionContext;
import io.siddhi.langserver.core.completion.providers.CompletionProvider;
import io.siddhi.langserver.core.utils.SnippetBlockUtil;
import io.siddhi.query.compiler.SiddhiQLParser;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides completions for AggregationTimeRangeContext
 * {@link io.siddhi.query.compiler.SiddhiQLParser.Aggregation_time_rangeContext}.
 *
 * @since 1.0.0
 */
public class AggregationTimeRangeContextProvider extends CompletionProvider {

    public AggregationTimeRangeContextProvider() {
        this.providerName = SiddhiQLParser.Aggregation_time_rangeContext.class.getName();
    }

    @Override
    public List<CompletionItem> getCompletions() {
        List<CompletionItem> completions;
        List<Map<String, Object>> suggestions = new ArrayList<>();
        suggestions.add(SnippetBlockUtil.SNIPPETS.get("TRIPLE_DOT"));
        completions = LSCompletionContext.INSTANCE
                .getProvider(SiddhiQLParser.Aggregation_time_durationContext.class.getName()).getCompletions();
        completions.addAll(generateCompletionList(suggestions));
        return completions;
    }
}
