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
$Id: JNDISourceAdapter.java,v 1.16 2009-10-23 04:04:22 mchyzer Exp $
$Date: 2009-10-23 04:04:22 $

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
 * JNDI Source.  This is the legacy classname which will use the new code in LdapSourceAdapter.
 * If you want to use the legacy code, configure for edu.internet2.middleware.subject.provider.JNDISourceAdapterLegacy
 *
 */
public class JNDISourceAdapter extends LdapSourceAdapter {

  /**
   * 
   */
  public JNDISourceAdapter() {
    super();
    
  }

  /**
   * @param id
   * @param name
   */
  public JNDISourceAdapter(String id, String name) {
    super(id, name);
    
  }


}
