/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.ui.util.ProgressBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * data for group import
 * @author mchyzer
 *
 */
public class GroupImportContainer {

  private String groupId;
  
  private String subjectId;
  
  private String sourceId;
  
  
  public String getGroupId() {
    return groupId;
  }



  
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }



  
  public String getSubjectId() {
    return subjectId;
  }



  
  public void setSubjectId(String subjectId) {
    this.subjectId = subjectId;
  }



  
  public String getSourceId() {
    return sourceId;
  }



  
  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }

  /**
   * uniquely identifies this import as opposed to other imports in other tabs
   */
  private String uniqueImportId;
  
  
  
  
  public String getUniqueImportId() {
    return uniqueImportId;
  }


  
  public void setUniqueImportId(String uniqueImportId) {
    this.uniqueImportId = uniqueImportId;
  }

  private boolean importReplaceMembers;

  private boolean removeMembers;

  
  public boolean isImportReplaceMembers() {
    return importReplaceMembers;
  }

  
  public void setImportReplaceMembers(boolean importReplaceMembers) {
    this.importReplaceMembers = importReplaceMembers;
  }

  
  public boolean isRemoveMembers() {
    return removeMembers;
  }

  
  public void setRemoveMembers(boolean removeMembers) {
    this.removeMembers = removeMembers;
  }

  /**
   * generate a report (if needed) based on current progress
   * @return the report
   */
  public String getReport() {
    
    //    <%-- loop through all the groups and give each report --%>
    //    <c:forEach items="${grouperRequestContainer.groupImportContainer.guiGroups}" var="guiGroup" >
    //      <hr />
    //      <h4>${guiGroup.linkWithIcon}</h4>
    //      ${grouperRequestContainer.groupImportContainer.reportForGroupNameMap[guiGroup.group.name]}
    //    </c:forEach>

    
    
    StringBuilder report = new StringBuilder("");

    for (GuiGroup guiGroup : GrouperUtil.nonNull(this.getGuiGroups())) {

      report.append("<h4>").append(guiGroup.getLinkWithIcon()).append("</h4><ul>\n");

      GroupImportGroupSummary groupImportGroupSummary = this.groupImportGroupSummaryForGroupMap.get(guiGroup.getGroup());
      
      this.setGroupImportGroupSummary(groupImportGroupSummary);

      if (groupImportGroupSummary == null) {
        continue;
      }
      
      boolean didntImportDueToSubjects = groupImportGroupSummary.getGroupCountErrors() > 0;

      if (importReplaceMembers && didntImportDueToSubjects) {
        report.append(TextContainer.retrieveFromRequest().getText().get("groupImportReportNoReplaceError")).append("\n");
      }

      // dont add the error report line if there are no errors 
      if (groupImportGroupSummary.getGroupCountErrors() > 0) {
        report.append(TextContainer.retrieveFromRequest().getText().get("groupImportReportErrorSummary")).append("\n");
      }

      this.setGroupImportGroupSummary(groupImportGroupSummary);
      
      if (groupImportGroupSummary.isComplete()) {
        report.append(TextContainer.retrieveFromRequest().getText().get("groupImportReportSummary")).append("\n");
      }
      report.append(TextContainer.retrieveFromRequest().getText().get("groupImportReportSuccess")).append("\n");

      if (groupImportGroupSummary.getGroupImportErrors().size() > 0) {
        report.append("<h5>").append(TextContainer.retrieveFromRequest().getText().get("groupImportReportErrorsTitle")).append("</h5>\n");
        report.append("<ul>\n");
        for (GroupImportError groupImportError : GrouperUtil.nonNull(groupImportGroupSummary.getGroupImportErrors())) {

          this.setErrorText(groupImportError.getErrorEscaped());

          this.setErrorSubject(groupImportError.getSubjectLabel());
          this.setErrorRowNumber(-1);
          String errorLine = null;
          if (groupImportError.getRowNumber() != null) {
            this.setErrorRowNumber(groupImportError.getRowNumber());
            errorLine = TextContainer.retrieveFromRequest().getText().get("groupImportReportErrorLine");
          } else {
            errorLine = TextContainer.retrieveFromRequest().getText().get("groupImportReportErrorLineNoRow");
          }
          
          report.append(errorLine).append("\n");
          
        }
        
        report.append("</ul>\n");
      }
      
      report.append("</ul>\n");
    }
    
    
//    // = Errors
//    //groupImportReportErrorLine = <li><span class="label label-important">Error</span>&nbsp;on row ${grouperRequestContainer.groupImportContainer.errorRowNumber}. ${grouperRequestContainer.groupImportContainer.errorText}: "${grouperUtil.xmlEscape(grouperRequestContainer.groupImportContainer.errorSubject)}"</li>
//
//    report.append(TextContainer.retrieveFromRequest().getText().get("groupImportReportSummary")).append("\n");
//    report.append(TextContainer.retrieveFromRequest().getText().get("groupImportReportSuccess")).append("\n");
//    
//    // dont add the error report line if there are no errors 
//    if (errorsCount > 0) {
//      report.append(TextContainer.retrieveFromRequest().getText().get("groupImportReportErrorSummary")).append("\n");
//    }
//    report.append("</ul>\n");
//    
//    //only add the errors section if there are errors
//    if (errorsCount > 0) {
//      report.append("<h5>").append(TextContainer.retrieveFromRequest().getText().get("groupImportReportErrorsTitle")).append("</h5>\n");
//      report.append("<ul>\n");
//      report.append(errors.toString());
//      report.append("</ul>\n");
//    }
//    
//    reportByGroupName.put(group.getName(), report.toString());


    //<ul>
    //  <li>Before importing, the membership count was 10 and is now 12.</li>
    //  <li>You successfully added 2 members and deleted 0 members.</li>
    //  <li>2 members were not imported due to errors, as shown below.</li>
    //</ul>
    //<h5>Errors</h5>
    //<ul>
    //  <li><span class="label label-important">Error</span>&nbsp;on row 2. Subject not found: "foo-bar-user"</li>
    //</ul>

    return report.toString();
  }
  
  /**
   * have a progress bean
   */
  private ProgressBean progressBean = new ProgressBean();
  
  /**
   * have a progress bean
   * @return the progressBean
   */
  public ProgressBean getProgressBean() {
    return this.progressBean;
  }

  /**
   * row number there is an error
   */
  private int errorRowNumber;
  
  /**
   * row number there is an error
   * @return the errorRowNumber
   */
  public int getErrorRowNumber() {
    return this.errorRowNumber;
  }
  
  /**
   * row number there is an error
   * @param errorRowNumber1 the errorRowNumber to set
   */
  public void setErrorRowNumber(int errorRowNumber1) {
    this.errorRowNumber = errorRowNumber1;
  }

  /**
   * error text
   */
  private String errorText;
  
  /**
   * error text
   * @return the errorText
   */
  public String getErrorText() {
    return this.errorText;
  }
  
  /**
   * error text
   * @param errorText1 the errorText to set
   */
  public void setErrorText(String errorText1) {
    this.errorText = errorText1;
  }

  /**
   * error subject
   */
  private String errorSubject;
  
  
  /**
   * error subject
   * @return the errorSubject
   */
  public String getErrorSubject() {
    return this.errorSubject;
  }
  
  /**
   * error subject
   * @param errorSubject1 the errorSubject to set
   */
  public void setErrorSubject(String errorSubject1) {
    this.errorSubject = errorSubject1;
  }

  /**
   * current summary for externalized text
   */
  private GroupImportGroupSummary groupImportGroupSummary;
  
  /**
   * current summary for externalized text
   * @return
   */
  public GroupImportGroupSummary getGroupImportGroupSummary() {
    return groupImportGroupSummary;
  }

  /**
   * current summary for externalized text
   * @param groupImportGroupSummary
   */
  public void setGroupImportGroupSummary(GroupImportGroupSummary groupImportGroupSummary) {
    this.groupImportGroupSummary = groupImportGroupSummary;
  }

  /**
   * groups which we are importing to
   */
  private Set<GuiGroup> guiGroups;
  
  /**
   * key is group, value is the summary
   */
  private Map<Group, GroupImportGroupSummary> groupImportGroupSummaryForGroupMap = new LinkedHashMap<Group, GroupImportGroupSummary>();
  
  /**
   * groups which we are importing to
   * @return groups
   */
  public Set<GuiGroup> getGuiGroups() {
    return this.guiGroups;
  }

  /**
   * groups which we are importing to
   * @param groups1
   */
  public void setGuiGroups(Set<GuiGroup> groups1) {
    this.guiGroups = groups1;
  }

  /**
   * key is group name, value is the report for the group
   * @return map
   */
  public Map<Group, GroupImportGroupSummary> getGroupImportGroupSummaryForGroupMap() {
    return this.groupImportGroupSummaryForGroupMap;
  }

  /**
   * key is group name, value is the report for the group
   * @param groupImportGroupSummaryForGroupMap1
   */
  public void setGroupImportGroupSummaryForGroupMap(Map<Group, GroupImportGroupSummary> groupImportGroupSummaryForGroupMap1) {
    this.groupImportGroupSummaryForGroupMap = groupImportGroupSummaryForGroupMap1;
  }

  /**
   * comma separated list of entity ids not found
   */
  private String entityIdsNotFound;
  
  /**
   * comma separated list of entity ids not found
   * @return list
   */
  public String getEntityIdsNotFound() {
    return this.entityIdsNotFound;
  }

  /**
   * comma separated list of entity ids not found
   * @param entityIdsNotFound1
   */
  public void setEntityIdsNotFound(String entityIdsNotFound1) {
    this.entityIdsNotFound = entityIdsNotFound1;
  }

  /**
   * gui groups in addition to the one in the combobox
   */
  private Set<GuiGroup> groupImportExtraGuiGroups;

  /**
   * gui subjects in addition to the one in the combobox
   */
  private Set<GuiSubject> groupImportExtraGuiSubjects;
  
  /**
   * gui subjects in addition to the one in the combobox
   * @return gui subjects
   */
  public Set<GuiSubject> getGroupImportExtraGuiSubjects() {
    return this.groupImportExtraGuiSubjects;
  }

  /**
   * gui subjects in addition to the one in the combobox
   * @param groupImportExtraGuiSubjects1
   */
  public void setGroupImportExtraGuiSubjects(Set<GuiSubject> groupImportExtraGuiSubjects1) {
    this.groupImportExtraGuiSubjects = groupImportExtraGuiSubjects1;
  }

  /**
   * if import from group
   */
  private boolean importFromGroup;
  /**
   * if import from subject
   */
  private boolean importFromSubject;

  /**
   * if export all of just member subject ids
   */
  private boolean exportAll = false;

  /**
   * gui groups in addition to the one in the combobox
   * @return the set of groups
   */
  public Set<GuiGroup> getGroupImportExtraGuiGroups() {
    return this.groupImportExtraGuiGroups;
  }

  /**
   * if there is a subject in url and we want to populate that field, 
   * then this is the value
   * @return the value
   */
  public String getImportDefaultSubject() {
    
    SubjectContainer subjectContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getSubjectContainer();
    
    if(subjectContainer.getGuiSubject() != null) {
      return subjectContainer.getGuiSubject().getSubject().getSourceId() + "||" + subjectContainer.getGuiSubject().getSubject().getId();
    }
    return null;
  }

  /**
   * if import from group
   * @return if from group
   */
  public boolean isImportFromGroup() {
    return this.importFromGroup;
  }

  /**
   * if import from subject
   * @return if from subject
   */
  public boolean isImportFromSubject() {
    return this.importFromSubject;
  }

  /**
   * gui groups in addition to the one in the combobox
   * @param groupImportExtraGuiGroups1
   */
  public void setGroupImportExtraGuiGroups(Set<GuiGroup> groupImportExtraGuiGroups1) {
    this.groupImportExtraGuiGroups = groupImportExtraGuiGroups1;
  }

  /**
   * if import from group
   * @param importFromGroup1
   */
  public void setImportFromGroup(boolean importFromGroup1) {
    this.importFromGroup = importFromGroup1;
  }

  /**
   * if import from subject
   * @param importFromSubject1
   */
  public void setImportFromSubject(boolean importFromSubject1) {
    this.importFromSubject = importFromSubject1;
  }


  /**
   * if export all of just member subject ids
   * @return export all
   */
  public boolean isExportAll() {
    return this.exportAll;
  }


  /**
   * if export all of just member subject ids
   * @param exportAll1
   */
  public void setExportAll(boolean exportAll1) {
    this.exportAll = exportAll1;
  }


  /**
   * return the filename of the file being exported
   * @return the filename of the file being exported
   */
  public String getExportFileName() {
    GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();
    if (this.isExportAll()) {
      return guiGroup.getExportAllFileName();
    }
    return guiGroup.getExportSubjectIdsFileName();
  }

}
