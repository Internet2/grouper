package edu.internet2.middleware.grouper.grouperUi.beans.api.objectTypes;

import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.APP;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.BASIS;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.ETC;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.GROUPER_SECURITY;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.ORG;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.POLICY;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.READ_ONLY;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.REF;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.SERVICE;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.TEST;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeValue;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;

public class GuiGrouperObjectTypesAttributeValue {
  
  private static final Map<String, String> objectTypesToDescriptions = new LinkedHashMap<String, String>();
  
  static {
    objectTypesToDescriptions.put(BASIS, "objectTypeBasisFolderDescription");
    objectTypesToDescriptions.put(REF, "objectTypeRefFolderDescription");
    objectTypesToDescriptions.put(POLICY, "objectTypePolicyFolderDescription");
    objectTypesToDescriptions.put(ETC, "objectTypeEtcFolderDescription");
    objectTypesToDescriptions.put(GROUPER_SECURITY, "objectTypeGrouperSecurityFolderDescription");
    objectTypesToDescriptions.put(ORG, "objectTypeOrgFolderDescription");
    objectTypesToDescriptions.put(APP, "objectTypeAppFolderDescription");
    objectTypesToDescriptions.put(SERVICE, "objectTypeServiceFolderDescription");
    objectTypesToDescriptions.put(READ_ONLY, "objectTypeReadOnlyFolderDescription");
    objectTypesToDescriptions.put(TEST, "objectTypeTestFolderDescription");

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
