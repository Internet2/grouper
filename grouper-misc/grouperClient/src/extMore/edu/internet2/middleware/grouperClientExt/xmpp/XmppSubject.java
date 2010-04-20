package edu.internet2.middleware.grouperClientExt.xmpp;


/**
 * subject bean for web services
 * 
 * @author mchyzer
 * 
 */
public class XmppSubject {

  /**
   * constructor
   */
  public XmppSubject() {
    // blank
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
