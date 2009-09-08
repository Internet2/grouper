/*
 * @author mchyzer
 * $Id: GuiMember.java,v 1.2 2009-09-08 18:53:31 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.io.Serializable;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.subject.SubjectNotFoundException;



/**
 * member bean wraps grouper class with useful methods for UIs
 */
public class GuiMember implements Serializable {

  /**
   * default constructor
   */
  public GuiMember() {
    
  }
  
  /** member */
  private Member member;
  
  /**
   * construct from member
   * @param member1
   */
  public GuiMember(Member member1) {
    try {
      this.guiSubject = new GuiSubject(member1.getSubject());
    } catch (SubjectNotFoundException snfe) {
      this.guiSubject = new GuiSubject(new SubjectWrapper(member1));
    }
    this.setGuiSubject(this.guiSubject);
    this.member = member1;
  }
  
  /**
   * return the member
   * @return the member
   */
  public Member getMember() {
    return this.member;
  }
  
  /** the subject for this member */
  private GuiSubject guiSubject;
  
  /** if this subject is deletable (has an immediate membership) */
  private boolean deletable;

  /**
   * 
   * @return the subject
   */
  public GuiSubject getGuiSubject() {
    return this.guiSubject;
  }

  /**
   * subject
   * @param subject1
   */
  public void setGuiSubject(GuiSubject subject1) {
    this.guiSubject = subject1;
  }

  /**
   * if there is an immediate membership which can be deleted
   * @return if deletable
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
