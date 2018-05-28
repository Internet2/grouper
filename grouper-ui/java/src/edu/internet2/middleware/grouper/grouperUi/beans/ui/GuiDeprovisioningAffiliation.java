package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.HashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningAffiliation;

public class GuiDeprovisioningAffiliation {
  
  private String label;
  
  private String translatedLabel;
  
  private String groupNameMeansInAffiliation;
  
  public GuiDeprovisioningAffiliation(String label, String translatedLabel, String groupNameMeansInAffiliation) {
    this.label = label;
    this.translatedLabel = translatedLabel;
    this.groupNameMeansInAffiliation = groupNameMeansInAffiliation;
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

  public String getGroupNameMeansInAffiliation() {
    return groupNameMeansInAffiliation;
  }
  
  public void setGroupNameMeansInAffiliation(String groupNameMeansInAffiliation) {
    this.groupNameMeansInAffiliation = groupNameMeansInAffiliation;
  }
  
  public static Set<GuiDeprovisioningAffiliation> convertFromGrouperDeprovisioningAffiliations(Set<GrouperDeprovisioningAffiliation> affiliations) {
   
    Set<GuiDeprovisioningAffiliation> guiAffiliations = new HashSet<GuiDeprovisioningAffiliation>();
    if (affiliations == null || affiliations.size() == 0) {
      return guiAffiliations;
    }
    
    for (GrouperDeprovisioningAffiliation affiliation: affiliations) {
      guiAffiliations.add(new GuiDeprovisioningAffiliation(affiliation.getLabel(), getAffiliationTranslatedLabel(affiliation.getLabel()),
          affiliation.getGroupNameMeansInAffiliation()));
    }
    
    return guiAffiliations;
    
  }
  
  private static String getAffiliationTranslatedLabel(String affiliationLabel) {
    if (TextContainer.retrieveFromRequest().getText().containsKey("deprovisioningAffiliationLabel_" + affiliationLabel)) {
      return TextContainer.retrieveFromRequest().getText().get("deprovisioningAffiliationLabel_" + affiliationLabel);
    } else {
      return affiliationLabel;
    }
  }

}
