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
package io.siddhi.langserver.core.completion.providers.executionelement;

import io.siddhi.langserver.core.beans.LSErrorNode;
import io.siddhi.langserver.core.completion.LSCompletionContext;
import io.siddhi.langserver.core.completion.providers.CompletionProvider;
import io.siddhi.query.compiler.SiddhiQLParser;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides completions for ExecutionElementContext.
 * {@link io.siddhi.query.compiler.SiddhiQLParser.Execution_elementContext}.
 *
 * @since 1.0.0
 */
public class ExecutionElementContextProvider extends CompletionProvider {

    public ExecutionElementContextProvider() {
        this.providerName = SiddhiQLParser.Execution_elementContext.class.getName();
    }

    @Override
    public List<CompletionItem> getCompletions() {
        List<Map<String, Object>> suggestions = new ArrayList<>();
        LSErrorNode errorNode = (LSErrorNode) LSCompletionContext.INSTANCE
                .getParseTreeMap().get(LSErrorNode.class.getName());
        if (errorNode.getPreviousSymbol().contains("@")) {
            return LSCompletionContext.INSTANCE
                    .getProvider(SiddhiQLParser.AnnotationContext.class.getName()).getCompletions();

        }
        return generateCompletionList(suggestions);
    }
}
