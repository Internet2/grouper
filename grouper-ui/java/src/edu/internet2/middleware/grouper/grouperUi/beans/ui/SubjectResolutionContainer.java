package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.app.usdu.SubjectResolutionAttributeValue;
import edu.internet2.middleware.grouper.app.usdu.SubjectResolutionStat;
import edu.internet2.middleware.grouper.app.usdu.UsduSettings;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubjectResolutionSubject;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiSorting;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;

/**
 * 
 */
public class SubjectResolutionContainer {
  
  /**
   * how many deleted
   */
  private int deleteCount;
  
  /**
   * how many deleted
   * @return the deleteCount
   */
  public int getDeleteCount() {
    return this.deleteCount;
  }
  
  /**
   * how many deleted
   * @param deleteCount1 the deleteCount to set
   */
  public void setDeleteCount(int deleteCount1) {
    this.deleteCount = deleteCount1;
  }

  /**
   * true to show deleted only, false to show unresolved only, null to show all
   */
  private Boolean showDeleted;
  
  /**
   * true to show deleted only, false to show unresolved only, null to show all
   * @return the showDeleted
   */
  public Boolean getShowDeleted() {
    return this.showDeleted;
  }
  
  /**
   * true to show deleted only, false to show unresolved only, null to show all
   * @return the showDeleted
   */
  public String getShowDeletedLabel() {
    if (this.showDeleted == null) {
      return "showAll";
    }
    if (this.showDeleted) {
      return "showDeleted";
    }
    return "doNotShowDeleted";
  }
  
  /**
   * true to show deleted only, false to show unresolved only, null to show all
   * @param showDeleted1 the showDeleted to set
   */
  public void setShowDeleted(Boolean showDeleted1) {
    this.showDeleted = showDeleted1;
  }

  /**
   * stats for resolvable/unresolvable subjects per source
   */
  private List<SubjectResolutionStat> subjectResolutionStats = new ArrayList<SubjectResolutionStat>();
  
  /**
   * unresolved subjects
   */
  private Set<SubjectResolutionAttributeValue> unresolvedSubjects = new HashSet<SubjectResolutionAttributeValue>();
  
  /**
   * keep track of the paging
   */
  private GuiPaging guiPaging = null;
  
  /**
   * make sure subject resolution is enabled and allowed
   */
  public void assertSubjectResolutionEnabledAndAllowed() {
    if (!UsduSettings.usduEnabled()) {
      throw new RuntimeException("Subject resolution/USDU is disabled");
    }
    
    if (!this.isAllowedToSubjectResolution()) {
      throw new RuntimeException("Not allowed to subject resolution");
    }

  }
  
  /**
   * keep track of the paging on
   * @return the paging object, init if not there...
   */
  public GuiPaging getGuiPaging() {
    if (this.guiPaging == null) {
      this.guiPaging = new GuiPaging();
    }
    return this.guiPaging;
  }

  /**
   * only grouper sysadmins are allowed to see the subject resolution page
   * @return
   */
  public boolean isAllowedToSubjectResolution() {
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    return  PrivilegeHelper.isWheelOrRoot(loggedInSubject);
  }
  
  /**
   * 
   * @return the list of subject sources with unresolved and resolved count
   */
  public List<SubjectResolutionStat> getSubjectResolutionStats() {
    return this.subjectResolutionStats;
  }
  
  
  /**
   * the list of subject sources with unresolved and resolved count
   * @param subjectResolutionStats
   */
  public void setSubjectResolutionStats(List<SubjectResolutionStat> subjectResolutionStats) {
    this.subjectResolutionStats = subjectResolutionStats;
  }

  /**
   * 
   * @return get unresolved subjects
   */
  public Set<SubjectResolutionAttributeValue> getUnresolvedSubjects() {
    return this.unresolvedSubjects;
  }
  
  
  /**
   * unresolved subjects
   * @param unresolvedSubjects
   */
  public void setUnresolvedSubjects(Set<SubjectResolutionAttributeValue> unresolvedSubjects) {
    this.unresolvedSubjects = unresolvedSubjects;
  }


  /**
   * sorting, e.g. for the audit screen
   */
  private GuiSorting guiSorting;
  
  /**
   * sorting, e.g. for the audit screen
   * @return the sorting
   */
  public GuiSorting getGuiSorting() {
    return this.guiSorting;
  }

  /**
   * sorting, e.g. for the audit screen
   * @param guiSorting1
   */
  public void setGuiSorting(GuiSorting guiSorting1) {
    this.guiSorting = guiSorting1;
  }

  /**
   * audit entries for group
   */
  private Set<GuiAuditEntry> guiAuditEntries;

  /**
   * audit entries for group
   * @return audit entries
   */
  public Set<GuiAuditEntry> getGuiAuditEntries() {
    return this.guiAuditEntries;
  }

  /**
   * audit entries for group
   * @param guiAuditEntries1
   */
  public void setGuiAuditEntries(Set<GuiAuditEntry> guiAuditEntries1) {
    this.guiAuditEntries = guiAuditEntries1;
  }
  
  /**
   * if extended results on audit display
   */
  private boolean auditExtendedResults = false;

  /**
   * if extended results on audit display
   * @return if extended results
   */
  public boolean isAuditExtendedResults() {
    return this.auditExtendedResults;
  }

  /**
   * if extended results on audit display
   * @param auditExtendedResults1
   */
  public void setAuditExtendedResults(boolean auditExtendedResults1) {
    this.auditExtendedResults = auditExtendedResults1;
  }
  
  /**
   * gui subject resolution for one subject
   */
  private GuiSubjectResolutionSubject guiSubjectResolutionSubject;

  /**
   * 
   * @return gui subject resolution for one subject
   */
  public GuiSubjectResolutionSubject getGuiSubjectResolutionSubject() {
    return guiSubjectResolutionSubject;
  }

  /**
   * gui subject resolution for one subject
   * @param guiSubjectResolutionSubject
   */
  public void setGuiSubjectResolutionSubject(GuiSubjectResolutionSubject guiSubjectResolutionSubject) {
    this.guiSubjectResolutionSubject = guiSubjectResolutionSubject;
  }
  
}
