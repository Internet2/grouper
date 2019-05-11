package edu.internet2.middleware.grouper.grouperUi.beans.api.objectTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesConfiguration;
import edu.internet2.middleware.grouper.app.grouperTypes.StemObjectType;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;

public class GuiStemObjectType {
  
  /**
   * stem and candidate object type
   */
  private StemObjectType stemObjectType;
  
  public GuiStemObjectType(StemObjectType stemObjectType) {
    this.stemObjectType = stemObjectType;
  }

  /**
   * get stem and candidate object type
   * @return
   */
  public StemObjectType getStemObjectType() {
    return stemObjectType;
  }

  /**
   * gui stem that is candidate
   * @return
   */
  public GuiStem getGuiStem() {
    return new GuiStem(stemObjectType.getStem());
  }

  /**
   * convert from stem object type to gui stem object type
   * @param stemObjectTypes
   * @return
   */
  public static List<GuiStemObjectType> convertFromStemObjectType(List<StemObjectType> stemObjectTypes) {
    
    List<GuiStemObjectType> guiStemObjectTypes = new ArrayList<GuiStemObjectType>();
    
    for (StemObjectType stemObjectType: stemObjectTypes) {
      guiStemObjectTypes.add(new GuiStemObjectType(stemObjectType));
    }
    
    return guiStemObjectTypes;
  }
  
  public boolean isShowDataOwnerMemberDescription() {
    List<String> dataOwnerRequiringTypeNames = Arrays.asList("ref", "basis", "policy", "bundle", "org");
    return dataOwnerRequiringTypeNames.contains(this.getStemObjectType().getObjectType());
  }
  
  public boolean isShowServiceName() {
    return this.getStemObjectType().getObjectType().equals("app");
  }
  
}
