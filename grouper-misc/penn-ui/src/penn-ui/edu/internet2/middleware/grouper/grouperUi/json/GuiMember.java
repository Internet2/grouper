/*
 * @author mchyzer
 * $Id: GuiMember.java,v 1.3 2009-08-05 06:38:26 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.json;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.grouperUi.util.GuiUtils;
import edu.internet2.middleware.subject.SubjectNotFoundException;



/**
 * member bean to be shipped to gui
 */
public class GuiMember {

  /**
   * default constructor
   */
  public GuiMember() {
    
  }
  
  /**
   * construct from member
   * @param member
   */
  public GuiMember(Member member) {
    GuiSubject guiSubject = null;
    try {
      guiSubject = new GuiSubject(member.getSubject());
    } catch (SubjectNotFoundException snfe) {
      guiSubject = new GuiSubject(new SubjectWrapper(member));
    }
    this.setSubject(guiSubject);
    this.setUuid(member.getUuid());
  }
  
  /** member uuid */
  private String uuid;
  
  
  /**
   * member uuid
   * @return the uuid
   */
  public String getUuid() {
    return this.uuid;
  }

  
  /**
   * member uuid
   * @param uuid1 the uuid to set
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  }

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
