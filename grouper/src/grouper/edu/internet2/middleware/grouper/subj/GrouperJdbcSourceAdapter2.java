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
 * $Id: GrouperJdbcSourceAdapter2.java,v 1.2 2009-08-11 20:18:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.subj;

import edu.internet2.middleware.subject.provider.HelperGrouperJdbcSourceAdapter2;



/**
 * Grouper's jdbc source adapter.  Has c3p0 pooling (eventually), 
 * shares pool with grouper (evenutally), and decrypts passwords.
 */
public class GrouperJdbcSourceAdapter2 extends HelperGrouperJdbcSourceAdapter2 {

  /**
   * 
   */
  public GrouperJdbcSourceAdapter2() {
  }

  /**
   * @param id
   * @param name
   */
  public GrouperJdbcSourceAdapter2(String id, String name) {
    super(id, name);
  }

}
