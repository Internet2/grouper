package edu.internet2.middleware.grouper.grouperUi.beans.api;


public class GuiDeprovisioningMembershipSubjectContainer {
  
  private GuiMembershipSubjectContainer guiMembershipSubjectContainer;
  
  private boolean showCheckbox;
  
  private boolean checkCheckbox;

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
  
}
