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
/**
 * 
 */
package edu.internet2.middleware.grouper.ws.status;

import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.grouper.audit.AuditType;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;


/**
 * see if the server can connect to the DB (cache results)
 * @author mchyzer
 *
 */
public class DiagnosticDbTest extends DiagnosticTask {

  
  /**
   * cache the results
   */
  private static GrouperCache<String, Boolean> dbCache = new GrouperCache<String, Boolean>("dbDiagnostic", 100, false, 120, 120, false);
  

  /**
   * @see edu.internet2.middleware.grouper.ws.status.DiagnosticTask#doTask()
   */
  @Override
  protected boolean doTask() {
    
    if (dbCache.containsKey("grouper")) {

      this.appendSuccessTextLine("Retrieved object from cache");

    } else {

      HibernateSession.byHqlStatic().createQuery(
        "from AuditType as theAuditType where theAuditType.auditCategory = :theAuditCategory and theAuditType.actionName = :theActionName")
        .setString("theAuditCategory", "stem")
        .setString("theActionName", "addStem")
        .uniqueResult(AuditType.class);
  
    
      this.appendSuccessTextLine("Retrieved object from database");
      dbCache.put("grouper", Boolean.TRUE);
      
    }
    return true;
    
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.status.DiagnosticTask#retrieveName()
   */
  @Override
  public String retrieveName() {
    
    return "dbTest_grouper";
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.status.DiagnosticTask#retrieveNameFriendly()
   */
  @Override
  public String retrieveNameFriendly() {
    return "Database test";
  }

  /**
   * 
   */
  @Override
  public boolean equals(Object obj) {
    return obj instanceof DiagnosticDbTest;
  }
  
  /**
   * 
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder().toHashCode();
  }

}
