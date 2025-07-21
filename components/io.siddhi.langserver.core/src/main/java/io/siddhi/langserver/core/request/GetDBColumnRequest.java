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
package io.siddhi.langserver.core.request;

/**
 * Request class for database column retrieval operations.
 *
 * @since 1.0.0
 */
public class GetDBColumnRequest {

    public String dataSourceLocation;
    public String driver;
    public String username;
    public String password;
    public String tableName;

    public GetDBColumnRequest(String dataSourceLocation, String driver, String username, String password,
                              String tableName) {
        this.dataSourceLocation = dataSourceLocation;
        this.driver = driver;
        this.username = username;
        this.password = password;
        this.tableName = tableName;
    }
}
