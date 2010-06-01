package edu.internet2.middleware.grouperClientExt.xmpp;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

/**
 * subject from grouper event
 * @author mchyzer
 */
public class GrouperClientXmppSubject {

  /**
   * 
   */
  public GrouperClientXmppSubject() {
    //empty
  }
  
  /**
   * @param esbEvent 
   * 
   */
  public GrouperClientXmppSubject(EsbEvent esbEvent) {
    
    this.subjectId = esbEvent.getSubjectId();
    this.sourceId = esbEvent.getSourceId();
    this.name = esbEvent.subjectAttribute("name");
    this.description = esbEvent.subjectAttribute("description");

    for (String[] row : GrouperClientUtils.nonNull(esbEvent.getSubjectAttributes(), String[].class)) {
      this.attribute.put(row[0], row[1]);
    }
    
  }
  
  /** subject id */
  private String subjectId;
  
  /** source id */
  private String sourceId;
  
  /** name */
  private String name;
  
  /** description */
  private String description;
  
  /** attributes */
  private Map<String, String> attribute = new HashMap<String, String>();

  /**
   * name
   * @return name
   */
  public String getName() {
    return this.name;
  }

  /**
   * name
   * @param name1
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * description
   * @return description
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * description
   * @param description1
   */
  public void setDescription(String description1) {
    this.description = description1;
  }

  /**
   * convert from WS subject to this subject
   * @param wsSubject
   * @param subjectAttributeNames
   */
  public GrouperClientXmppSubject(WsSubject wsSubject, String[] subjectAttributeNames) {
    this.subjectId = wsSubject.getId();
    this.sourceId = wsSubject.getSourceId();
    this.name = wsSubject.getName();
    for (int i=0;i<GrouperClientUtils.length(subjectAttributeNames); i++) {
      this.attribute.put(subjectAttributeNames[i], wsSubject.getAttributeValue(i));
    }
  }
  
  /**
   * subject id
   * @return subject id
   */
  public String getSubjectId() {
    return this.subjectId;
  }

  /**
   * subject id id
   * @param subjectId1
   */
  public void setSubjectId(String subjectId1) {
    this.subjectId = subjectId1;
  }

  /**
   * source id
   * @return source id
   */
  public String getSourceId() {
    return this.sourceId;
  }

  /**
   * source id
   * @param sourceId1
   */
  public void setSourceId(String sourceId1) {
    this.sourceId = sourceId1;
  }

  /**
   * attributes
   * @return attributes
   */
  public Map<String, String> getAttribute() {
    return this.attribute;
  }

  /**
   * attributes
   * @param attribute1
   */
  public void setAttribute(Map<String, String> attribute1) {
    this.attribute = attribute1;
  }
  
  /**
   * @see Object#equals(Object)
   */
  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof GrouperClientXmppSubject)) {
      return false;
    }
    GrouperClientXmppSubject otherSubject = (GrouperClientXmppSubject)other;

    return GrouperClientUtils.equals(this.sourceId, otherSubject.sourceId) && GrouperClientUtils.equals(this.subjectId, otherSubject.subjectId);
  }

  /**
   * @see Object#hashCode()
   */
  @Override
  public int hashCode() {
    return (this.subjectId + this.sourceId).hashCode();
  }
  
  
  
}
