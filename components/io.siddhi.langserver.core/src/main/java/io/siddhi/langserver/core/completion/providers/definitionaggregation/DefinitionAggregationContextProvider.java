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
import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides completions for DefinitionAggregationContext.
 * {@link io.siddhi.query.compiler.SiddhiQLParser.Definition_aggregationContext}.
 *
 * @since 1.0.0
 */
public class DefinitionAggregationContextProvider extends CompletionProvider {

    public DefinitionAggregationContextProvider() {
        this.providerName = SiddhiQLParser.Definition_aggregationContext.class.getName();
    }

    @Override
    public List<CompletionItem> getCompletions() {
        List<Map<String, Object>> suggestions = new ArrayList<>();
        List<CompletionItem> completions = new ArrayList<>();
        ParserRuleContext aggregationDefinitionContext = (ParserRuleContext) LSCompletionContext.INSTANCE
                .getParseTreeMap().get(SiddhiQLParser.Definition_aggregationContext.class.getName());
        if (aggregationDefinitionContext != null) {
            int childCount = aggregationDefinitionContext.getChildCount();
            if (childCount > 0) {
                if (aggregationDefinitionContext
                        .getChild(childCount - 1) instanceof SiddhiQLParser.Attribute_referenceContext) {
                    completions.addAll(LSCompletionContext.INSTANCE
                            .getProvider(SiddhiQLParser.Attribute_referenceContext.class.getName()).getCompletions());
                } else if (aggregationDefinitionContext
                        .getChild(childCount - 1) instanceof SiddhiQLParser.Group_byContext) {
                    suggestions.add(SnippetBlockUtil.SNIPPETS.get("KEYWORD_AGGREGATE_BY"));
                } else if (aggregationDefinitionContext
                        .getChild(childCount - 1) instanceof SiddhiQLParser.Group_by_query_selectionContext) {
                    suggestions.add(SnippetBlockUtil.SNIPPETS.get("KEYWORD_GROUP_BY"));
                    suggestions.add(SnippetBlockUtil.SNIPPETS.get("KEYWORD_AGGREGATE_BY"));
                }
            }

        }
        suggestions.add(SnippetBlockUtil.SNIPPETS.get("KEYWORD_EVERY"));
        suggestions.add(SnippetBlockUtil.SNIPPETS.get("KEYWORD_FROM"));
        completions.addAll(generateCompletionList(suggestions));
        return completions;
    }
}
