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

import io.siddhi.langserver.core.completion.providers.CompletionProvider;
import io.siddhi.langserver.core.utils.SnippetBlockUtil;
import io.siddhi.query.compiler.SiddhiQLParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provide completions for AttributeNameContext {@link io.siddhi.query.compiler.SiddhiQLParser.Attribute_nameContext}.
 *
 * @since 1.0.0
 */
public class AttributeNameContextProvider extends CompletionProvider {

    public AttributeNameContextProvider() {
        this.providerName = SiddhiQLParser.Attribute_nameContext.class.getName();
    }

    @Override
    public List<CompletionItem> getCompletions() {
        ParserRuleContext parent = getParent();
        List<Map<String, Object>> suggestions = new ArrayList<>();
        //AttributeName can be a reference
        if (parent instanceof SiddhiQLParser.Attribute_referenceContext) {
            return generateCompletionList(suggestions);
        } else if (parent instanceof SiddhiQLParser.Output_attributeContext) {
            //AttributeName can be an output attribute
            suggestions.add(SnippetBlockUtil.SNIPPETS.get("ALIAS_SNIPPET"));
            return generateCompletionList(suggestions);
        } else {
            suggestions.add(SnippetBlockUtil.SNIPPETS.get("ATTRIBUTE_NAME_AND_TYPE_SNIPPET"));
            return generateCompletionList(suggestions);
        }
    }
}
