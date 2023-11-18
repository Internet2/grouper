package edu.internet2.middleware.grouper.grouperUi.beans.api.objectTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.grouperTypes.StemOrGroupObjectType;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiObjectBase;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;

public class GuiStemObjectType {
  
  /**
   * stem and candidate object type
   */
  private StemOrGroupObjectType stemObjectType;
  
  public GuiStemObjectType(StemOrGroupObjectType stemObjectType) {
    this.stemObjectType = stemObjectType;
  }

  /**
   * get stem and candidate object type
   * @return
   */
  public StemOrGroupObjectType getStemObjectType() {
    return stemObjectType;
  }

  public GuiObjectBase getGuiObject() {
    if (stemObjectType.getGrouperObject() instanceof Group) {
      return new GuiGroup((Group)stemObjectType.getGrouperObject());
    }
    if (stemObjectType.getGrouperObject() instanceof Stem) {
      return new GuiStem((Stem)stemObjectType.getGrouperObject());
    }
    throw new RuntimeException(stemObjectType.getGrouperObject() + " is not of type group or stem");
  }
  
  public boolean isStem() {
    if (stemObjectType.getGrouperObject() instanceof Stem) {
      return true;
    }
    return false;
  }

  /**
   * convert from stem object type to gui stem object type
   * @param stemObjectTypes
   * @return
   */
  public static List<GuiStemObjectType> convertFromStemObjectType(List<StemOrGroupObjectType> stemObjectTypes) {
    
    List<GuiStemObjectType> guiStemObjectTypes = new ArrayList<GuiStemObjectType>();
    
    for (StemOrGroupObjectType stemObjectType: stemObjectTypes) {
      guiStemObjectTypes.add(new GuiStemObjectType(stemObjectType));
    }
    
    return guiStemObjectTypes;
  }
  
  public boolean isShowDataOwnerMemberDescription() {
    List<String> dataOwnerRequiringTypeNames = Arrays.asList("ref", "basis", "policy", "bundle", "org", "manual");
    return dataOwnerRequiringTypeNames.contains(this.getStemObjectType().getObjectType());
  }
  
  public boolean isShowServiceName() {
    return this.getStemObjectType().getObjectType().equals("app");
  }
  
}
