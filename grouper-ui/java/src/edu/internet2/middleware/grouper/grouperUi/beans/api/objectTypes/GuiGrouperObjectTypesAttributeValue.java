package edu.internet2.middleware.grouper.grouperUi.beans.api.objectTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeValue;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;

public class GuiGrouperObjectTypesAttributeValue {
  
  private static final Map<String, String> objectTypesToDescriptions = new HashMap<String, String>();
  
  static {
    objectTypesToDescriptions.put("ref", "objectTypeRefFolderDescription");
    objectTypesToDescriptions.put("basis", "objectTypeBasisFolderDescription");
    objectTypesToDescriptions.put("policy", "objectTypePolicyFolderDescription");
    objectTypesToDescriptions.put("etc", "objectTypeEtcFolderDescription");
    objectTypesToDescriptions.put("bundle", "objectTypeBundleFolderDescription");
    objectTypesToDescriptions.put("org", "objectTypeOrgFolderDescription");
    objectTypesToDescriptions.put("test", "objectTypeTestFolderDescription");
    objectTypesToDescriptions.put("service", "objectTypeServiceFolderDescription");
    objectTypesToDescriptions.put("app", "objectTypeAppFolderDescription");
    objectTypesToDescriptions.put("readOnly", "objectTypeReadOnlyFolderDescription");
    objectTypesToDescriptions.put("grouperSecurity", "objectTypeGrouperSecurityFolderDescription");
  }
  
  private GuiGrouperObjectTypesAttributeValue(GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue) {
    this.grouperObjectTypesAttributeValue = grouperObjectTypesAttributeValue;
  }
  
  private GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue;

  public GrouperObjectTypesAttributeValue getGrouperObjectTypesAttributeValue() {
    return grouperObjectTypesAttributeValue;
  }
  
  /**
   * return the gui folder with settings
   * @return gui stem
   */
  public GuiStem getGuiFolderWithSettings() {
    if (this.grouperObjectTypesAttributeValue == null) {
      return null;
    }
    
    String stemId = this.grouperObjectTypesAttributeValue.getObjectTypeOwnerStemId();
    Stem stem = GrouperDAOFactory.getFactory().getStem().findByUuid(stemId, false);
    
    if (stem == null) {
      return null;
    }
    
    return new GuiStem(stem);
  }
  
  public static List<GuiGrouperObjectTypesAttributeValue> convertFromGrouperObjectTypesAttributeValues(List<GrouperObjectTypesAttributeValue> attributeValues) {
    
    List<GuiGrouperObjectTypesAttributeValue> guiGrouperObjectTypesAttributeValues = new ArrayList<GuiGrouperObjectTypesAttributeValue>();
    
    for (GrouperObjectTypesAttributeValue singleAttributeValue: attributeValues) {
      guiGrouperObjectTypesAttributeValues.add(new GuiGrouperObjectTypesAttributeValue(singleAttributeValue));
    }
    
    return guiGrouperObjectTypesAttributeValues;
    
  }
  
  public String getObjectTypeDescriptionKey() {
    return objectTypesToDescriptions.get(grouperObjectTypesAttributeValue.getObjectTypeName());
  }
  
  

}
