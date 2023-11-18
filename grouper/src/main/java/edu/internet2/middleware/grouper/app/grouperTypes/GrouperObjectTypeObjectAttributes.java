package edu.internet2.middleware.grouper.app.grouperTypes;

public class GrouperObjectTypeObjectAttributes {
  
  private String markerAttributeAssignId;


  public GrouperObjectTypeObjectAttributes(String id, String name, String markerAttributeAssignId) {
    this.id = id;
    this.name = name;
    this.markerAttributeAssignId = markerAttributeAssignId;
  }
  
  
  private String id;
  private String name;
  
 
  private boolean isOwnedByGroup;
  private boolean isOwnedByStem;
  
  private String objectTypeDirectAssign;
  
  /**
   * object type name
   */
  private String objectTypeName;
  
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
  
  private String objectTypeOwnerStemId;

  
  public String getId() {
    return id;
  }

  
  public void setId(String id) {
    this.id = id;
  }

  
  public String getName() {
    return name;
  }

  
  public void setName(String name) {
    this.name = name;
  }

  
  public boolean isOwnedByGroup() {
    return isOwnedByGroup;
  }

  
  public void setOwnedByGroup(boolean isOwnedByGroup) {
    this.isOwnedByGroup = isOwnedByGroup;
  }

  
  public boolean isOwnedByStem() {
    return isOwnedByStem;
  }

  
  public void setOwnedByStem(boolean isOwnedByStem) {
    this.isOwnedByStem = isOwnedByStem;
  }

  
  public String getObjectTypeName() {
    return objectTypeName;
  }

  
  public void setObjectTypeName(String objectTypeName) {
    this.objectTypeName = objectTypeName;
  }

  
  public String getObjectTypeDataOwner() {
    return objectTypeDataOwner;
  }

  
  public void setObjectTypeDataOwner(String objectTypeDataOwner) {
    this.objectTypeDataOwner = objectTypeDataOwner;
  }

  
  public String getObjectTypeMemberDescription() {
    return objectTypeMemberDescription;
  }

  
  public void setObjectTypeMemberDescription(String objectTypeMemberDescription) {
    this.objectTypeMemberDescription = objectTypeMemberDescription;
  }

  
  public String getObjectTypeServiceName() {
    return objectTypeServiceName;
  }

  
  public void setObjectTypeServiceName(String objectTypeServiceName) {
    this.objectTypeServiceName = objectTypeServiceName;
  }


  
  public String getObjectTypeOwnerStemId() {
    return objectTypeOwnerStemId;
  }


  
  public void setObjectTypeOwnerStemId(String objectTypeOwnerStemId) {
    this.objectTypeOwnerStemId = objectTypeOwnerStemId;
  }


  
  public String getObjectTypeDirectAssign() {
    return objectTypeDirectAssign;
  }


  
  public void setObjectTypeDirectAssign(String objectTypeDirectAssign) {
    this.objectTypeDirectAssign = objectTypeDirectAssign;
  }


  
  public String getMarkerAttributeAssignId() {
    return markerAttributeAssignId;
  }
  
  
  
}
