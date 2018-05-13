package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.HashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningRealm;

public class GuiDeprovisioningRealm {
  
  private String label;
  
  private String translatedLabel;
  
  private String groupNameMeansInRealm;
  
  public GuiDeprovisioningRealm(String label, String translatedLabel, String groupNameMeansInRealm) {
    this.label = label;
    this.translatedLabel = translatedLabel;
    this.groupNameMeansInRealm = groupNameMeansInRealm;
  }
  
  public String getLabel() {
    return label;
  }
  
  public void setLabel(String label) {
    this.label = label;
  }

  public String getTranslatedLabel() {
    return translatedLabel;
  }
  
  public void setTranslatedLabel(String translatedLabel) {
    this.translatedLabel = translatedLabel;
  }

  public String getGroupNameMeansInRealm() {
    return groupNameMeansInRealm;
  }
  
  public void setGroupNameMeansInRealm(String groupNameMeansInRealm) {
    this.groupNameMeansInRealm = groupNameMeansInRealm;
  }
  
  public static Set<GuiDeprovisioningRealm> convertFromGrouperDeprovisioningRealms(Set<GrouperDeprovisioningRealm> realms) {
   
    Set<GuiDeprovisioningRealm> guiRealms = new HashSet<GuiDeprovisioningRealm>();
    if (realms == null || realms.size() == 0) {
      return guiRealms;
    }
    
    for (GrouperDeprovisioningRealm realm: realms) {
      guiRealms.add(new GuiDeprovisioningRealm(realm.getLabel(), getRealmTranslatedLabel(realm.getLabel()),
          realm.getGroupNameMeansInRealm()));
    }
    
    return guiRealms;
    
  }
  
  private static String getRealmTranslatedLabel(String realmLabel) {
    if (TextContainer.retrieveFromRequest().getText().containsKey("deprovisioningRealmLabel_" + realmLabel)) {
      return TextContainer.retrieveFromRequest().getText().get("deprovisioningRealmLabel_" + realmLabel);
    } else {
      return realmLabel;
    }
  }

}
