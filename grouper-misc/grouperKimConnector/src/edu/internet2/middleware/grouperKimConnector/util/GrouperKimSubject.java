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
package edu.internet2.middleware.grouperKimConnector.util;

import java.util.Map;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

/**
 * result includes the subject and attribute names
 */
public class GrouperKimSubject {
  
  /**
   * 
   */
  public GrouperKimSubject() {
    
  }
  
  /**
   * construct with subjects (array of 1)
   * @param debugMap
   * @param wsSubjects
   * @param theSubjectAttributeNames
   */
  public GrouperKimSubject(Map<String, Object> debugMap, WsSubject[] wsSubjects, String[] theSubjectAttributeNames) {
    
    WsSubject theWsSubject = GrouperClientUtils.length(wsSubjects) == 1 ? wsSubjects[0] : null;

    if (theWsSubject == null || !GrouperClientUtils.equals("T", theWsSubject.getSuccess())) {
      if (theWsSubject != null) {
        debugMap.put("resultCode", theWsSubject.getResultCode());
      }
      debugMap.put("wsSubject", "null");
      debugMap.put("wsSubjects.length", GrouperClientUtils.length(wsSubjects));
    } else {
      this.setWsSubject(theWsSubject);
      this.setSubjectAttributeNames(theSubjectAttributeNames);
    }

  }
  
  /** subject and attribute values */
  private WsSubject wsSubject;
  
  /** subject attribute names */
  private String[] subjectAttributeNames;

  
  /**
   * @return the wsSubject
   */
  public WsSubject getWsSubject() {
    return this.wsSubject;
  }

  
  /**
   * @param wsSubject1 the wsSubject to set
   */
  public void setWsSubject(WsSubject wsSubject1) {
    this.wsSubject = wsSubject1;
  }

  
  /**
   * @return the subjectAttributeNames
   */
  public String[] getSubjectAttributeNames() {
    return this.subjectAttributeNames;
  }

  
  /**
   * @param subjectAttributeNames1 the subjectAttributeNames to set
   */
  public void setSubjectAttributeNames(String[] subjectAttributeNames1) {
    this.subjectAttributeNames = subjectAttributeNames1;
  }
  
}
