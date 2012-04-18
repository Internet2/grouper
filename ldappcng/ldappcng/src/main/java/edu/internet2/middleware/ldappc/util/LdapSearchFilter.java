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
 * Copyright 2006-2007 The University Of Chicago Copyright 2006-2007 University
 * Corporation for Advanced Internet Development, Inc. Copyright 2006-2007 EDUCAUSE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.ldappc.util;

import javax.naming.directory.SearchControls;

/**
 * This holds the elements for performing a Ldap search on a given base with a specified
 * scope and search filter.
 */
public class LdapSearchFilter {

  /**
   * DN of the search base.
   */
  private String base;

  /**
   * Scope of the search. Either
   * {@link javax.naming.directory.SearchControls#OBJECT_SCOPE},
   * {@link javax.naming.directory.SearchControls#ONELEVEL_SCOPE}, or
   * {@link javax.naming.directory.SearchControls#SUBTREE_SCOPE}.
   */
  private int scope;

  /**
   * Ldap search filter.
   */
  private String filter;

  /**
   * When a subject is not found, either fail (throw an LdappcException), warn (log), or
   * ignore (do nothing).
   */
  public enum OnNotFound {
    fail, warn, ignore
  };

  private OnNotFound onNotFound;

  /**
   * Allow multiple provisioned objects on a target for a single subject.
   */
  private boolean multipleResults;

  /**
   * Construct a LdapSearchFilter with the given parameters.
   * 
   * @param base
   *          DN of the search base
   * @param scope
   *          Scope of the search
   * @param filter
   *          Ldap search filter
   * @param onNotFound
   *          action to perform when a subject can not be found
   * @param multipleResults
   *          allow multiple provisioned objects for a subject
   */
  public LdapSearchFilter(String base, int scope, String filter, OnNotFound onNotFound,
      boolean multipleResults) {
    setBase(base);
    setScope(scope);
    setFilter(filter);
    setOnNotFound(onNotFound);
    setMultipleResults(multipleResults);
  }

  /**
   * Set the base DN.
   * 
   * @param base
   *          DN of the base entry
   * @throws IllegalArgumentException
   *           thrown if base is null.
   */
  public void setBase(String base) throws IllegalArgumentException {
    if (base == null) {
      throw new IllegalArgumentException("Base may not be null.");
    }
    this.base = base;
  }

  /**
   * Gets the base DN.
   * 
   * @return DN of the base
   */
  public String getBase() {
    return base;
  }

  /**
   * Set the search scope. Either
   * {@link javax.naming.directory.SearchControls#OBJECT_SCOPE},
   * {@link javax.naming.directory.SearchControls#ONELEVEL_SCOPE}, or
   * {@link javax.naming.directory.SearchControls#SUBTREE_SCOPE}.
   * 
   * @param scope
   *          Search scope
   * @throws IllegalArgumentException
   *           thrown if an invalid scope is provided
   */
  public void setScope(int scope) throws IllegalArgumentException {
    if (scope != SearchControls.OBJECT_SCOPE && scope != SearchControls.ONELEVEL_SCOPE
        && scope != SearchControls.SUBTREE_SCOPE) {
      throw new IllegalArgumentException("Scope value is invalid.");
    }
    this.scope = scope;
  }

  /**
   * Get the search scope.
   * 
   * @return Search scope
   */
  public int getScope() {
    return scope;
  }

  /**
   * Get the search filter.
   * 
   * @return Search filter
   */
  public String getFilter() {
    return filter;
  }

  /**
   * Set the search filter.
   * 
   * @param filter
   *          Search filter
   * @throws IllegalArgumentException
   *           thrown if filter is null.
   */
  public void setFilter(String filter) throws IllegalArgumentException {
    if (filter == null) {
      throw new IllegalArgumentException("Filter may not be null.");
    }
    this.filter = filter;
  }

  /**
   * Get desired behavior when a subject is not found.
   * 
   * @return {@link #onNotFound}
   */
  public OnNotFound getOnNotFound() {
    return onNotFound;
  }

  public void setOnNotFound(OnNotFound onNotFound) {
    this.onNotFound = onNotFound;
  }

  /**
   * Get whether or not multiple provisioned objects for a subject are allowed.
   * 
   * @return true if multiple results are allowed, defaults to false
   */
  public boolean getMultipleResults() {
    return multipleResults;
  }

  public void setMultipleResults(boolean multipleResults) {
    this.multipleResults = multipleResults;
  }

  /**
   * Returns a string representation of the object.
   * 
   * @return a string representation of the object.
   */
  public String toString() {
    return "[base=" + getBase() + "][scope=" + getScope() + "][filter=" + getFilter()
        + "]";
  }
}
