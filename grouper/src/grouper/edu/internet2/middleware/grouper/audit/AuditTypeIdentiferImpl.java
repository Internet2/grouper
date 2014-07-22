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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.audit;


/**
 *
 */
public class AuditTypeIdentiferImpl implements AuditTypeIdentifier {

  /** action name */
  private String actionName;

  /** audit category */
  private String auditCategory;
  
  /**
   * @param actionName1
   * @param auditCategory1
   */
  public AuditTypeIdentiferImpl(String actionName1, String auditCategory1) {
    super();
    this.actionName = actionName1;
    this.auditCategory = auditCategory1;
  }

  /**
   * @see edu.internet2.middleware.grouper.audit.AuditTypeIdentifier#getActionName()
   */
  public String getActionName() {
    return this.actionName;
  }

  /**
   * @see edu.internet2.middleware.grouper.audit.AuditTypeIdentifier#getAuditCategory()
   */
  public String getAuditCategory() {
    return this.auditCategory;
  }

}
