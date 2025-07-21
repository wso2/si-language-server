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
package io.siddhi.langserver.core.completion;

import io.siddhi.langserver.core.completion.providers.CompletionProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Factory of completion providers.
 *
 * @since 1.0.0
 */
public class LSCompletionProviderFactory {

    private static final LSCompletionProviderFactory INSTANCE = new LSCompletionProviderFactory();
    private Map<String, CompletionProvider> providers = new HashMap<>();

    /**
     * Load the providers at the factory initialization time using {@link ServiceLoader}.
     */
    private LSCompletionProviderFactory() {
        ServiceLoader<CompletionProvider> providerServices =
                ServiceLoader.load(CompletionProvider.class);
        for (CompletionProvider provider : providerServices) {
            this.providers.put(provider.getProviderName(), provider);
        }
    }

    public static LSCompletionProviderFactory getInstance() {
        return INSTANCE;
    }

    public Map<String, CompletionProvider> getProviders() {
        return this.providers;
    }

    public CompletionProvider getProvider(String key) {
        return this.providers.get(key);
    }
}
