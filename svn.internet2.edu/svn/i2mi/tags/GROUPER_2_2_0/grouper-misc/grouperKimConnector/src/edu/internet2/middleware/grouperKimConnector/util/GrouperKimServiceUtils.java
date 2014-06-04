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
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperKimConnector.util;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouperClient.api.GcGetSubjects;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;


/**
 *
 */
public class GrouperKimServiceUtils {

  /**
   * convert a principal id to a subject
   * @param principalId 
   * @return the GrouperKimSubject which is never null, though the subject inside might be...
   */
  public static GrouperKimSubject convertPrincipalIdToSubject(String principalId) {
    Map<String, Object> debugMap = new HashMap<String, Object>();
    return convertPrincipalIdToSubject(debugMap, principalId);
  }

  /**
   * convert a principal id to a subject
   * @param debugMap for logging
   * @param principalId 
   * @return the GrouperKimSubject which is never null, though the subject inside might be...
   */
  public static GrouperKimSubject convertPrincipalIdToSubject(Map<String, Object> debugMap, String principalId) {
    debugMap.put("principalId", principalId);
  
    principalId = GrouperKimUtils.translatePrincipalId(principalId);
    debugMap.put("translatedPrincipalId", principalId);
    
    String sourceId = GrouperKimUtils.separateSourceId(principalId);
    String subjectIdentifier = GrouperKimUtils.separateSourceIdSuffix(principalId);
    
    GcGetSubjects gcGetSubjects = new GcGetSubjects();
    
    gcGetSubjects.addWsSubjectLookup(new WsSubjectLookup(null,sourceId, subjectIdentifier));
    
    gcGetSubjects.assignIncludeSubjectDetail(true);
    
    WsGetSubjectsResults wsGetSubjectsResults = gcGetSubjects.execute();
    
    //we did one assignment, we have one result
    WsSubject[] wsSubjects = wsGetSubjectsResults.getWsSubjects();
    
    GrouperKimSubject grouperKimSubject = new GrouperKimSubject(debugMap, wsSubjects, wsGetSubjectsResults.getSubjectAttributeNames());
        
    return grouperKimSubject;
  
  }

  /**
   * convert a principal id to a subject
   * @param entityId 
   * @return the GrouperKimSubject which is never null, though the subject inside might be...
   */
  public static GrouperKimSubject convertEntityIdToSubject(String entityId) {
    Map<String, Object> debugMap = new HashMap<String, Object>();
    return convertEntityIdToSubject(debugMap, entityId);
  }

  /**
   * convert a principal id to a subject
   * @param debugMap for logging
   * @param entityId 
   * @return the GrouperKimSubject which is never null, though the subject inside might be...
   */
  public static GrouperKimSubject convertEntityIdToSubject(Map<String, Object> debugMap, String entityId) {
    
    if (debugMap != null) {
      debugMap.put("entityId", entityId);
    }
  
    entityId = GrouperKimUtils.translatePrincipalId(entityId);
  
    if (debugMap != null) {
      debugMap.put("translatedEntityId", entityId);
    }
    
    String sourceId = GrouperKimUtils.separateSourceId(entityId);
    String subjectId = GrouperKimUtils.separateSourceIdSuffix(entityId);
    
    GcGetSubjects gcGetSubjects = new GcGetSubjects();
    
    gcGetSubjects.addWsSubjectLookup(new WsSubjectLookup(subjectId, sourceId, null));
    
    gcGetSubjects.assignIncludeSubjectDetail(true);
    
    WsGetSubjectsResults wsGetSubjectsResults = gcGetSubjects.execute();
    
    //we did one assignment, we have one result
    WsSubject[] wsSubjects = wsGetSubjectsResults.getWsSubjects();
    
    GrouperKimSubject grouperKimSubject = new GrouperKimSubject(debugMap, wsSubjects, wsGetSubjectsResults.getSubjectAttributeNames());
    return grouperKimSubject;
  }

  /**
   * convert principal name to subject result
   * @param principalName 
   * @return the GrouperKimSubject which is never null, though the subject inside might be...
   */
  public static GrouperKimSubject convertPrincipalNameToSubject(String principalName) {
    Map<String, Object> debugMap = new HashMap<String, Object>();
    return convertPrincipalNameToSubject(debugMap, principalName);
  }

  /**
   * convert principal name to subject result
   * @param debugMap for logging
   * @param principalName 
   * @return the GrouperKimSubject which is never null, though the subject inside might be...
   */
  public static GrouperKimSubject convertPrincipalNameToSubject(Map<String, Object> debugMap, String principalName) {
    debugMap.put("principalName", principalName);
  
    principalName = GrouperKimUtils.translatePrincipalName(principalName);
    debugMap.put("translatedPrincipalName", principalName);
  
    String sourceId = GrouperKimUtils.separateSourceId(principalName);
    String subjectIdentifier = GrouperKimUtils.separateSourceIdSuffix(principalName);
  
    GcGetSubjects gcGetSubjects = new GcGetSubjects();
  
    gcGetSubjects.addWsSubjectLookup(new WsSubjectLookup(null, sourceId, subjectIdentifier));
  
    gcGetSubjects.assignIncludeSubjectDetail(true);
  
    WsGetSubjectsResults wsGetSubjectsResults = gcGetSubjects.execute();
  
    //we did one assignment, we have one result
    WsSubject[] wsSubjects = wsGetSubjectsResults.getWsSubjects();
  
    GrouperKimSubject grouperKimSubject = new GrouperKimSubject(debugMap, wsSubjects, wsGetSubjectsResults.getSubjectAttributeNames());
    
    return grouperKimSubject;
  
  }

}
