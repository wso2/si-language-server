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

package io.siddhi.langserver.core.completion.providers.common;

import io.siddhi.langserver.core.completion.LSCompletionContext;
import io.siddhi.langserver.core.completion.providers.CompletionProvider;
import io.siddhi.langserver.core.utils.SnippetBlockUtil;
import io.siddhi.query.compiler.SiddhiQLParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provider for group by query selection context.
 *
 * @since 1.0.0
 */
public class GroupByQuerySelectionContextProvider extends CompletionProvider {

    public GroupByQuerySelectionContextProvider() {
        this.providerName = SiddhiQLParser.Group_by_query_selectionContext.class.getName();
    }

    @Override
    public List<CompletionItem> getCompletions() {
        List<Map<String, Object>> suggestions = new ArrayList<>();
        ParserRuleContext currentContext = LSCompletionContext.INSTANCE.getCurrentContext();
        ParseTree firstChild = currentContext.getChild(0);
        if (firstChild instanceof TerminalNodeImpl) {
            if (firstChild.getText().equalsIgnoreCase("select")) {
                List<CompletionItem> completions;
                completions = LSCompletionContext.INSTANCE.getProvider(
                                SiddhiQLParser.Output_attributeContext.class.getName())
                        .getCompletions();
                suggestions = new ArrayList<>();
                suggestions.add(SnippetBlockUtil.SNIPPETS.get("KEYWORD_GROUP_BY"));
                completions.addAll(generateCompletionList(suggestions));
                return completions;
            } else {
                suggestions = new ArrayList<>();
                suggestions.add(SnippetBlockUtil.SNIPPETS.get("KEYWORD_SELECT"));
                return generateCompletionList(suggestions);
            }
        } else {
            suggestions.add(SnippetBlockUtil.SNIPPETS.get("KEYWORD_SELECT"));
        }
        return generateCompletionList(suggestions);
    }
}
