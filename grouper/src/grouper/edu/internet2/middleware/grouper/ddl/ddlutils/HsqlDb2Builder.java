/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouper.ddl.ddlutils;

import java.io.IOException;

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.hsqldb.HsqlDbBuilder;

/**
 * @author shilen
 */
public class HsqlDb2Builder extends HsqlDbBuilder {

  /**
   * @param platform
   */
  public HsqlDb2Builder(Platform platform) {
    super(platform);    
  }

  /**
   * @see org.apache.ddlutils.platform.SqlBuilder#writeExternalIndexDropStmt(org.apache.ddlutils.model.Table, org.apache.ddlutils.model.Index)
   */
  public void writeExternalIndexDropStmt(Table table, Index index) throws IOException {
    // does not use the ON <tablename> clause
    print("DROP INDEX ");
    printIdentifier(getIndexName(index)); 
    printEndOfStatement();
  }
}
