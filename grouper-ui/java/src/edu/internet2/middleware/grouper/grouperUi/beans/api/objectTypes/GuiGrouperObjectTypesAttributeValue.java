package edu.internet2.middleware.grouper.grouperUi.beans.api.objectTypes;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeValue;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;

public class GuiGrouperObjectTypesAttributeValue {
  
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
  
  

}
