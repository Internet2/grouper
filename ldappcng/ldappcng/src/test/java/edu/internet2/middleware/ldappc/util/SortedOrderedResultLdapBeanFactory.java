/*******************************************************************************
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
 ******************************************************************************/
/*
 * Copyright 2011 University Corporation for Advanced Internet Development, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.ldappc.util;

import java.util.LinkedHashMap;

import edu.vt.middleware.ldap.bean.AbstractLdapResult;
import edu.vt.middleware.ldap.bean.LdapEntry;
import edu.vt.middleware.ldap.bean.LdapResult;
import edu.vt.middleware.ldap.bean.SortedLdapBeanFactory;

/** Provides an ldap bean factory whose ldap results are sorted by insertion. */
public class SortedOrderedResultLdapBeanFactory extends SortedLdapBeanFactory {

  /** {@inheritDoc} */
  public LdapResult newLdapResult() {
    return new OrderedLdapResult();
  }

  /**
   * <code>OrderedLdapResult</code> represents a collection of ldap entries that are ordered by insertion.
   */
  protected class OrderedLdapResult extends AbstractLdapResult<LinkedHashMap<String, LdapEntry>> {

    /** Default constructor. */
    public OrderedLdapResult() {
      super(SortedOrderedResultLdapBeanFactory.this);
      this.entries = new LinkedHashMap<String, LdapEntry>();
    }
  }

}
