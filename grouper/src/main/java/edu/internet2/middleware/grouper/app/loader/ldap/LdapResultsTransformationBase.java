/**
 * Copyright 2018 Internet2
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
package edu.internet2.middleware.grouper.app.loader.ldap;

/**
 * Extend this class to adjust the ldap results for ldap loaders
 * @author shilen
 */
public abstract class LdapResultsTransformationBase {

  /**
   * Transform the membershipResults, groupNameToDisplayName, and groupNameToDescription as needed.
   * @param ldapResultsTransformationInput 
   * @return output with the new results
   */
  public abstract LdapResultsTransformationOutput transformResults(LdapResultsTransformationInput ldapResultsTransformationInput);
}