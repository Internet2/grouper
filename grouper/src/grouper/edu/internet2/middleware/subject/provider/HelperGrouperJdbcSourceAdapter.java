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
/*
 * @author mchyzer
 * $Id: HelperGrouperJdbcSourceAdapter.java,v 1.4 2008-09-14 04:54:00 mchyzer Exp $
 */
package edu.internet2.middleware.subject.provider;



/**
 * Some methods in subject api have package security, so this class is a temporary
 * measure to get access to them
 */
public class HelperGrouperJdbcSourceAdapter extends JDBCSourceAdapter {

  /**
   * 
   */
  public HelperGrouperJdbcSourceAdapter() {
    super();
  }

  /**
   * @param arg0
   * @param arg1
   */
  public HelperGrouperJdbcSourceAdapter(String arg0, String arg1) {
    super(arg0, arg1);
  }

}
