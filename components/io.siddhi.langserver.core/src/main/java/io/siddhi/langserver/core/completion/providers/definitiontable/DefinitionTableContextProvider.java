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

package io.siddhi.langserver.core.completion.providers.definitiontable;

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
 * Provides completions for DefinitionTableContext.
 * {@link io.siddhi.query.compiler.SiddhiQLParser.Definition_tableContext}.
 *
 * @since 1.0.0
 */
public class DefinitionTableContextProvider extends CompletionProvider {

    public DefinitionTableContextProvider() {
        this.providerName = SiddhiQLParser.Definition_tableContext.class.getName();
    }

    @Override
    public List<CompletionItem> getCompletions() {
        List<Map<String, Object>> suggestions = new ArrayList<>();
        ParserRuleContext definitionTableContext = (ParserRuleContext) LSCompletionContext.INSTANCE.getParseTreeMap()
                .get(SiddhiQLParser.Definition_tableContext.class.getName());
        int childCount = definitionTableContext.getChildCount();
        if (childCount > 0) {
            if (definitionTableContext.getChild(childCount - 1) instanceof SiddhiQLParser.SourceContext) {
                suggestions.add(SnippetBlockUtil.SNIPPETS.get("ATTRIBUTE_LIST_SNIPPET"));
            }
        }
        return generateCompletionList(suggestions);
    }
}
