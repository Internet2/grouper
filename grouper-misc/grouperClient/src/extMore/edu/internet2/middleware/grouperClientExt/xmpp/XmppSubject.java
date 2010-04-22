package edu.internet2.middleware.grouperClientExt.xmpp;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;


/**
 * subject bean for web services
 * 
 * @author mchyzer
 * 
 */
public class XmppSubject {

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof XmppSubject)) {
      return false;
    }
    XmppSubject other = (XmppSubject)obj;
    
    return GrouperClientUtils.equals(this.id, other.id) && GrouperClientUtils.equals(this.sourceId, other.sourceId);
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return (this.id == null ? 0 : this.id.hashCode()) + (this.sourceId == null ? 0 : this.sourceId.hashCode());
  }

  /**
   * constructor
   */
  public XmppSubject() {
    // blank
  }

  /**
   * assign attribute map
   * @param attributeNames
   */
  public void assignAttributeMap(String[] attributeNames) {
    if (GrouperClientUtils.length(attributeNames) > 0) {
      
      for (int i=0;i<GrouperClientUtils.length(attributeNames);i++) {
        this.getAttribute().put(attributeNames[i], 
            this.getAttributeValues()[i]);
      }
      
    }
  }
  
  /**
   * construct for full refresh
   * @param wsSubject
   * @param attributeNames
   */
  public XmppSubject(WsSubject wsSubject, String[] attributeNames) {
    this.setId(wsSubject.getId());
    this.setSourceId(wsSubject.getSourceId());
    for (int i=0;i<GrouperClientUtils.length(attributeNames); i++) {
      this.attribute.put(attributeNames[i], wsSubject.getAttributeValue(i));
    }
  }
  
  /** id of subject, note if no subject found, and identifier was passed in,
   * that will be placed here */
  private String id;

  /** source of subject */
  private String sourceId;

  /**
   * attribute data of subjects in group (in same order as attributeNames)
   */
  private String[] attributeValues;

  /**
   * attributes by name and value (not marshaled)
   */
  private Map<String, String> attribute = new HashMap<String, String>();
  
  
  /**
   * @return the attribute
   */
  public Map<String, String> getAttribute() {
    return this.attribute;
  }

  /**
   * subject id, note if no subject found, and identifier was passed in,
   * that will be placed here
   * @return the id
   */
  public String getId() {
    return this.id;
  }

  /**
   * subject id, note if no subject found, and identifier was passed in,
   * that will be placed here
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }

  /**
   * if attributes are being sent back per config in the grouper.properties,
   * this is attribute0 value, this is extended subject data
   * 
   * @return the attribute0
   */
  public String[] getAttributeValues() {
    return this.attributeValues;
  }
  
  /**
   * attribute data of subjects in group (in same order as attributeNames)
   * 
   * @param attributesa
   *            the attributes to set
   */
  public void setAttributeValues(String[] attributesa) {
    this.attributeValues = attributesa;
  }

  /**
   * @return the source
   */
  public String getSourceId() {
    return this.sourceId;
  }

  /**
   * @param source1 the source to set
   */
  public void setSourceId(String source1) {
    this.sourceId = source1;
  }

}
