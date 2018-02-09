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
package edu.internet2.middleware.grouper.ldap;

import java.util.List;
import java.util.Map;

/**
 * @author shilen
 */
public interface LdapSession {

  /**
   * run a filter, for one attribute, and return a list of that attribute typecast as a certain type
   * note, if it is a multi-valued attributes, and there are multiple object results, it will be flattened into one list
   * @param <R>
   * @param returnType note, only String.class is currently supported
   * @param ldapServerId
   * @param searchDn
   * @param ldapSearchScope 
   * @param filter
   * @param attributeName
   * @return the list of results, never null
   */
  public abstract <R> List<R> list(final Class<R> returnType, final String ldapServerId, 
      final String searchDn, final LdapSearchScope ldapSearchScope, final String filter, final String attributeName);


  /**
   * run a filter, for one attribute, and return a map of the DN key to the value of list of that attribute typecast as a certain type
   * @param <R>
   * @param returnType note, only String.class is currently supported
   * @param ldapServerId
   * @param searchDn
   * @param ldapSearchScope 
   * @param filter
   * @param attributeName
   * @return the list of results, never null
   */
  public abstract <R> Map<String, List<R>> listInObjects(final Class<R> returnType, final String ldapServerId, 
      final String searchDn, final LdapSearchScope ldapSearchScope, final String filter, final String attributeName);
  
  /**
   * Run a filter and return the results.
   * @param ldapServerId 
   * @param searchDn 
   * @param ldapSearchScope 
   * @param filter 
   * @param attributeNames 
   * @return the list of results, never null
   */
  public abstract List<LdapEntry> list(final String ldapServerId, final String searchDn, 
      final LdapSearchScope ldapSearchScope, final String filter, final String[] attributeNames);
}
