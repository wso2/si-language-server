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

package io.siddhi.langserver.core.completion.providers.siddhiapp;

import io.siddhi.langserver.core.completion.LSCompletionContext;
import io.siddhi.langserver.core.completion.providers.CompletionProvider;
import io.siddhi.query.compiler.SiddhiQLParser;
import org.eclipse.lsp4j.CompletionItem;

import java.util.List;

/**
 * Provider for parse context.
 *
 * @since 1.0.0
 */
public class ParseContextProvider extends CompletionProvider {

    public ParseContextProvider() {
        this.providerName = SiddhiQLParser.ParseContext.class.getName();
    }

    @Override
    public List<CompletionItem> getCompletions() {
        /**
         *   parse context is taken as the current context when the source code above the cursor position is a valid
         *   siddhi app.
         */
        return LSCompletionContext.INSTANCE.getProvider(SiddhiQLParser.Siddhi_appContext.class.getName())
                .getCompletions();
    }
}
