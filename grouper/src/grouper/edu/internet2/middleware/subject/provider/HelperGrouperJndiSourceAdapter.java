/**
 * Copyright 2012 Internet2
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
/*--
$Id: HelperGrouperJndiSourceAdapter.java,v 1.2 2008-09-14 04:54:00 mchyzer Exp $
$Date: 2008-09-14 04:54:00 $

Copyright 2005 Internet2 and Stanford University.  All Rights Reserved.
See doc/license.txt in this distribution.
 */
/*
 * JNDISourceAdapter.java
 *
 * Created on March 6, 2006
 *
 * Author Ellen Sluss
 */
package edu.internet2.middleware.subject.provider;

/**
 * JNDI Source 
 *
 */
public class HelperGrouperJndiSourceAdapter
        extends LdapSourceAdapter {

  /**
   * 
   */
  public HelperGrouperJndiSourceAdapter() {
    super();
  }

  /**
   * @param arg0
   * @param arg1
   */
  public HelperGrouperJndiSourceAdapter(String arg0, String arg1) {
    super(arg0, arg1);
  }
    
}
