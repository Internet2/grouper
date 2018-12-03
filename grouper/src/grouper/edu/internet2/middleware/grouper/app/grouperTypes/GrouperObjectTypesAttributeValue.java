package edu.internet2.middleware.grouper.app.grouperTypes;


public class GrouperObjectTypesAttributeValue {
  
  /**
   * object type name
   */
  private String objectTypeName;
  
  /**
   * is direct assignment 
   */
  private boolean directAssignment;
  
  /**
   * data owner property
   */
  private String objectTypeDataOwner;
  
  /**
   * member description property
   */
  private String objectTypeMemberDescription;
  
  /**
   * service name property
   */
  private String objectTypeServiceName;
  
  /**
   * owner stem id where type config is inherited from. This should be populated only when it's not direct assignment
   */
  private String objectTypeOwnerStemId;

  /**
   * object type name
   * @return
   */
  public String getObjectTypeName() {
    return objectTypeName;
  }

  /**
   * object type name
   * @param objectTypeName
   */
  public void setObjectTypeName(String objectTypeName) {
    this.objectTypeName = objectTypeName;
  }

  /**
   * is direct assignment
   * @return
   */
  public boolean isDirectAssignment() {
    return directAssignment;
  }

  /**
   * is direct assignment
   * @param directAssignment
   */
  public void setDirectAssignment(boolean directAssignment) {
    this.directAssignment = directAssignment;
  }

  /**
   * data owner property
   * @return
   */
  public String getObjectTypeDataOwner() {
    return objectTypeDataOwner;
  }

  /**
   * data owner property
   * @param objectTypeDataOwner
   */
  public void setObjectTypeDataOwner(String objectTypeDataOwner) {
    this.objectTypeDataOwner = objectTypeDataOwner;
  }

  /**
   * member description property
   * @return
   */
  public String getObjectTypeMemberDescription() {
    return objectTypeMemberDescription;
  }

  /**
   * member description property
   * @param objectTypeMemberDescription
   */
  public void setObjectTypeMemberDescription(String objectTypeMemberDescription) {
    this.objectTypeMemberDescription = objectTypeMemberDescription;
  }

  /**
   * service name property
   * @return
   */
  public String getObjectTypeServiceName() {
    return objectTypeServiceName;
  }

  /**
   * service name property
   * @param objectTypeServiceName
   */
  public void setObjectTypeServiceName(String objectTypeServiceName) {
    this.objectTypeServiceName = objectTypeServiceName;
  }
  
  /**
   * owner stem id where type config is inherited from. This should be populated only when it's not direct assignment
   * @return
   */
  public String getObjectTypeOwnerStemId() {
    return objectTypeOwnerStemId;
  }

  /**
   * owner stem id where type config is inherited from. This should be populated only when it's not direct assignment
   * @param objectTypeOwnerStemId
   */
  public void setObjectTypeOwnerStemId(String objectTypeOwnerStemId) {
    this.objectTypeOwnerStemId = objectTypeOwnerStemId;
  }
  
  /**
   * copy a given attribute value object
   * @param from
   * @return
   */
  public static GrouperObjectTypesAttributeValue copy(GrouperObjectTypesAttributeValue from) {
    GrouperObjectTypesAttributeValue value = new GrouperObjectTypesAttributeValue();
    value.setDirectAssignment(from.isDirectAssignment());
    value.setObjectTypeDataOwner(from.getObjectTypeDataOwner());
    value.setObjectTypeMemberDescription(from.getObjectTypeMemberDescription());
    value.setObjectTypeName(from.getObjectTypeName());
    value.setObjectTypeOwnerStemId(from.getObjectTypeOwnerStemId());
    value.setObjectTypeServiceName(from.getObjectTypeServiceName());
    return value;
  }

}
