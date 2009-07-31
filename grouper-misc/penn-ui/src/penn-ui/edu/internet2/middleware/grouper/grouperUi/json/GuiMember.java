/*
 * @author mchyzer
 * $Id: GuiMember.java,v 1.1 2009-07-31 14:27:27 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.json;


/**
 * member bean to be shipped to gui
 */
public class GuiMember {

  /** the subject for this member */
  private GuiSubject subject;
  
  /** if this subject is deletable (has an immediate membership) */
  private boolean deletable;

  /**
   * 
   * @return the subject
   */
  public GuiSubject getSubject() {
    return this.subject;
  }

  /**
   * subject
   * @param subject1
   */
  public void setSubject(GuiSubject subject1) {
    this.subject = subject1;
  }

  /**
   * if there is an immediate membership which can be deleted
   * @return
   */
  public boolean isDeletable() {
    return this.deletable;
  }

  /**
   * if this subject has an immediate membership
   * @param deletable1
   */
  public void setDeletable(boolean deletable1) {
    this.deletable = deletable1;
  }
  
}
