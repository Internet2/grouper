package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningOverallConfiguration;
import edu.internet2.middleware.grouper.misc.GrouperObject;

public class GuiDeprovisioningMembershipSubjectContainer {
  
  private GuiMembershipSubjectContainer guiMembershipSubjectContainer;
  
  private boolean showCheckbox;
  
  private boolean checkCheckbox;
  
  public GuiDeprovisioningMembershipSubjectContainer(GuiMembershipSubjectContainer guiMembershipSubjectContainer,
      boolean showCheckbox, boolean checkCheckbox) {
    
    this.guiMembershipSubjectContainer = guiMembershipSubjectContainer;
    this.showCheckbox = showCheckbox;
    this.checkCheckbox = checkCheckbox;
    
  }

  public GuiMembershipSubjectContainer getGuiMembershipSubjectContainer() {
    return guiMembershipSubjectContainer;
  }

  public void setGuiMembershipSubjectContainer(
      GuiMembershipSubjectContainer guiMembershipSubjectContainer) {
    this.guiMembershipSubjectContainer = guiMembershipSubjectContainer;
  }

  public boolean isShowCheckbox() {
    return showCheckbox;
  }
  
  public void setShowCheckbox(boolean showCheckbox) {
    this.showCheckbox = showCheckbox;
  }
  
  public boolean isCheckCheckbox() {
    return checkCheckbox;
  }

  public void setCheckCheckbox(boolean checkCheckbox) {
    this.checkCheckbox = checkCheckbox;
  }
  
  /**
   * convert gui membership subject containers to gui deprovisioning membership subject containers
   * @param guiMembershipSubjectContainers
   * @return the converted set
   */
  public static Set<GuiDeprovisioningMembershipSubjectContainer> convertFromGuiMembershipSubjectContainers(Set<GuiMembershipSubjectContainer> guiMembershipSubjectContainers) {
    
    ArrayList<GuiMembershipSubjectContainer> sortedMembershipSubjectContainers = new ArrayList<GuiMembershipSubjectContainer>(guiMembershipSubjectContainers);
    
    Collections.sort(sortedMembershipSubjectContainers, new java.util.Comparator<GuiMembershipSubjectContainer>() {

      @Override
      public int compare(GuiMembershipSubjectContainer o1, GuiMembershipSubjectContainer o2) {
        
        if (o1 == o2) {
          return 0;
        }
        
        if (o2 == null) {
          return 1;
        }
        
        if (o1 == null) {
          return -1;
        }
        
        return o1.getGuiObjectBase().getNameColonSpaceSeparated().compareTo(o2.getGuiObjectBase().getNameColonSpaceSeparated());
      }
    });
    
    Set<GuiDeprovisioningMembershipSubjectContainer> guiDeprovisioningContainers = new LinkedHashSet<GuiDeprovisioningMembershipSubjectContainer>();
    
    for (GuiMembershipSubjectContainer guiMembershipSubjectContainer: sortedMembershipSubjectContainers) {
      
      GrouperObject grouperObject = guiMembershipSubjectContainer.getMembershipSubjectContainer().getGroupOwner();
      
      if (grouperObject == null) {
        grouperObject = guiMembershipSubjectContainer.getMembershipSubjectContainer().getStemOwner();
      }
      
      if (grouperObject == null) {
        grouperObject = guiMembershipSubjectContainer.getMembershipSubjectContainer().getAttributeDefOwner();
      }
      
      if (grouperObject == null) {
        continue;
      }
      
      GrouperDeprovisioningOverallConfiguration config = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(grouperObject);
      
      guiDeprovisioningContainers.add(new GuiDeprovisioningMembershipSubjectContainer(guiMembershipSubjectContainer,
          config.isShowForRemoval(), config.isAutoselectForRemoval()));
    }
    
    return guiDeprovisioningContainers;
  }
  
}
