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
   * if we are debugging
   * @return
   */
  public abstract boolean isDebug();

  /**
   * if we should capture debug info
   * @param inDiagnostics
   */
  public abstract void assignDebug(boolean isDebug);

  /**
   * debug log where lines are separated by newlines
   * @return
   */
  public abstract StringBuilder getDebugLog();
  
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
   * @param sizeLimit
   * @return the list of results, never null
   */
  public abstract List<LdapEntry> list(final String ldapServerId, final String searchDn, 
      final LdapSearchScope ldapSearchScope, final String filter, final String[] attributeNames, Long sizeLimit);
  
  /**
   * Get the following entries.
   * @param ldapServerId
   * @param searchDn
   * @param dnList
   * @param attributeNames
   * @return the list of results, never null
   */
  public abstract List<LdapEntry> read(final String ldapServerId, final String searchDn, final List<String> dnList, final String[] attributeNames);
  
  /**
   * Delete an entry if it exists.
   * @param ldapServerId
   * @param dn
   */
  public abstract void delete(final String ldapServerId, final String dn);
  
  /**
   * Create entry.  If entry exists, update attributes instead.
   * @param ldapServerId
   * @param ldapEntry
   * @return true if created, false if updated
   */
  public abstract boolean create(final String ldapServerId, final LdapEntry ldapEntry);
  
  /**
   * Move an object to a new dn.  Assuming this would only be called if it's expected to work.
   * i.e. If the ldap server doesn't allow this, the caller should avoid calling this and instead
   * do a delete/re-create as appropriate.
   * @param ldapServerId
   * @param oldDn
   * @param newDn
   * @return true if moved, false if newDn exists and oldDn doesn't exist so no update
   */
  public abstract boolean move(final String ldapServerId, String oldDn, String newDn);
  
  /**
   * test a connection
   * @param ldapServerId
   * @return true if success, false or exception if not successful (error is in exception)
   */
  public abstract boolean testConnection(final String ldapServerId);
  
  /**
   * modify attributes for an object.  this should be done in bulk, and if there is an error, throw it
   * @param ldapServerId
   * @param dn
   * @param ldapModificationItems
   * @throws Exception if problem
   */
  public abstract void internal_modifyHelper(final String ldapServerId, final String dn, final List<LdapModificationItem> ldapModificationItems);
  
  /**
   * Authenticate a user
   * @param ldapServerId 
   * @param userDn
   * @param password
   */
  public abstract void authenticate(final String ldapServerId, final String userDn, final String password);
  
  /**
   * Check if connections need to be refreshed due to config changes
   * @param ldapServerId
   */
  public abstract void refreshConnectionsIfNeeded(final String ldapServerId);
  
}
