package edu.internet2.middleware.grouper.grouperUi.beans.api;

import edu.internet2.middleware.grouper.app.usdu.SubjectResolutionAttributeValue;

public class GuiSubjectResolutionSubject {

  /**
   * gui subject
   */
  private GuiSubject guiSubject;
  
  /**
   * subject resolution attribute value for gui subject
   */
  private SubjectResolutionAttributeValue subjectResolutionAttributeValue;

  /**
   * 
   * @return gui subject
   */
  public GuiSubject getGuiSubject() {
    return guiSubject;
  }

  /**
   * gui subject
   * @param guiSubject
   */
  public void setGuiSubject(GuiSubject guiSubject) {
    this.guiSubject = guiSubject;
  }

  /**
   * 
   * @return subject resolution attribute value for gui subject
   */
  public SubjectResolutionAttributeValue getSubjectResolutionAttributeValue() {
    return subjectResolutionAttributeValue;
  }

  /**
   * subject resolution attribute value for gui subject
   * @param subjectResolutionAttributeValue
   */
  public void setSubjectResolutionAttributeValue(SubjectResolutionAttributeValue subjectResolutionAttributeValue) {
    this.subjectResolutionAttributeValue = subjectResolutionAttributeValue;
  }
  
  
  
}
