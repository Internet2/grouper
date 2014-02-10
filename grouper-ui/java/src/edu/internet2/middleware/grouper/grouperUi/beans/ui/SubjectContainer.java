package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMembershipSubjectContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.provider.SourceManager;


public class SubjectContainer {

  /**
   * gui subject on the screen
   */
  private GuiSubject guiSubject;

  /**
   * groups that the current user is in
   */
  private Set<GuiMembershipSubjectContainer> guiMembershipSubjectContainers;
  
  /**
   * keep track of the paging on the stem screen
   */
  private GuiPaging guiPaging = null;
  
  /**
   * gui subject on the screen
   * @return the gui subject on the screen
   */
  public GuiSubject getGuiSubject() {
    return this.guiSubject;
  }

  /**
   * gui subject on the screen
   * @param guiSubject1
   */
  public void setGuiSubject(GuiSubject guiSubject1) {
    this.guiSubject = guiSubject1;
  }

  /**
   * get sources to pick which source
   * @return the sources
   */
  public Set<Source> getSources() {
    
    //we could cache this at some point
    Collection<Source> sources = SourceManager.getInstance().getSources();
    
    return new LinkedHashSet<Source>(sources);
  }

  /**
   * memberships in group
   * @return subjects and memberships
   */
  public Set<GuiMembershipSubjectContainer> getGuiMembershipSubjectContainers() {
    return this.guiMembershipSubjectContainers;
  }

  /**
   * keep track of the paging on the subjects screen
   * @return the paging object, init if not there...
   */
  public GuiPaging getGuiPaging() {
    if (this.guiPaging == null) {
      this.guiPaging = new GuiPaging();
    }
    return this.guiPaging;
  }

  /**
   * assign the membership containers
   * @param guiMembershipSubjectContainers
   */
  public void setGuiMembershipSubjectContainers(
      Set<GuiMembershipSubjectContainer> guiMembershipSubjectContainers) {
    this.guiMembershipSubjectContainers = guiMembershipSubjectContainers;
  }

  /**
   * paging for the memberships screen
   * @param guiPaging
   */
  public void setGuiPaging(GuiPaging guiPaging) {
    this.guiPaging = guiPaging;
  }
  
}
