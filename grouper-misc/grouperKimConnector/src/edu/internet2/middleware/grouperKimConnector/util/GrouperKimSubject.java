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