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

package io.siddhi.langserver.core.completion.providers.definitionwindow;

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
 * Provides completions for DefinitionWindowContext.
 * {@link io.siddhi.query.compiler.SiddhiQLParser.Definition_windowContext}.
 *
 * @since 1.0.0
 */
public class DefinitionWindowContextProvider extends CompletionProvider {

    public DefinitionWindowContextProvider() {
        this.providerName = SiddhiQLParser.Definition_windowContext.class.getName();
    }

    @Override
    public List<CompletionItem> getCompletions() {
        List<Map<String, Object>> suggestions = new ArrayList<>();
        ParserRuleContext definitionWindowContext = (ParserRuleContext) LSCompletionContext.INSTANCE.getParseTreeMap()
                .get(SiddhiQLParser.Definition_windowContext.class.getName());
        int childCount = definitionWindowContext.getChildCount();
        if (childCount > 0) {
            if (definitionWindowContext.getChild(childCount - 1) instanceof SiddhiQLParser.SourceContext) {
                suggestions.add(SnippetBlockUtil.SNIPPETS.get("ATTRIBUTE_LIST_SNIPPET"));
            }
        }
        return generateCompletionList(suggestions);
    }
}
