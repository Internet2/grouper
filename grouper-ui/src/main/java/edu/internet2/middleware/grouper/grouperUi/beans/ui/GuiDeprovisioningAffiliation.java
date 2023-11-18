package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.builder.CompareToBuilder;

import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningAffiliation;

public class GuiDeprovisioningAffiliation implements Comparable<GuiDeprovisioningAffiliation> {
  
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
  
  public static Set<GuiDeprovisioningAffiliation> convertFromGrouperDeprovisioningAffiliations(Collection<GrouperDeprovisioningAffiliation> affiliations) {
   
    Set<GuiDeprovisioningAffiliation> guiAffiliations = new TreeSet<GuiDeprovisioningAffiliation>();
    if (affiliations == null || affiliations.size() == 0) {
      return guiAffiliations;
    }
    
    for (GrouperDeprovisioningAffiliation affiliation: affiliations) {
      guiAffiliations.add(new GuiDeprovisioningAffiliation(affiliation.getLabel(), getAffiliationTranslatedLabel(affiliation.getLabel()),
          affiliation.getGroupNameMeansInAffiliation()));
    }
    
    return guiAffiliations;
    
  }
  
  public static String getAffiliationTranslatedLabel(String affiliationLabel) {
    if (TextContainer.retrieveFromRequest().getText().containsKey("deprovisioningAffiliationLabel_" + affiliationLabel)) {
      return TextContainer.retrieveFromRequest().getText().get("deprovisioningAffiliationLabel_" + affiliationLabel);
    } else {
      return affiliationLabel;
    }
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(GuiDeprovisioningAffiliation other) {
    if (other == null) {
      return 1;
    }
    return new CompareToBuilder().append(this.label, other.label).toComparison();
  }

}
