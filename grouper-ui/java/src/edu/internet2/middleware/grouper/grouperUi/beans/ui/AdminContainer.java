/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiDaemonJob;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiHib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiInstrumentationDataInstance;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.subject.Subject;


/**
 * admin container
 * @author mchyzer
 *
 */
public class AdminContainer {

  private List<GuiInstrumentationDataInstance> guiInstrumentationDataInstances;
  
  private Map<String, Map<String, Long>> guiInstrumentationGraphResults;
  
  private Set<String> guiInstrumentationDaysWithData;
  
  private String guiInstrumentationFilterDate;
  
  private boolean daemonJobsShowExtendedResults = false;
  
  private List<GuiHib3GrouperLoaderLog> guiHib3GrouperLoaderLogs;
  
  /**
   * paging for daemon jobs
   */
  private GuiPaging daemonJobsGuiPaging = null;

  
  /**
   * if import from group
   * @return if from group
   */
  public boolean isSubjectApiDiagnosticsShow() {
    
    if (!GrouperUiConfig.retrieveConfig().propertyValueBoolean("uiV2.admin.subjectApiDiagnostics.show", true)) {
      return false;
    }
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    
    String error = GrouperUiFilter.requireUiGroup("uiV2.admin.subjectApiDiagnostics.must.be.in.group", loggedInSubject, false);

    if (StringUtils.isBlank(error)) {
      return true;
    }
    
    return false;
  }
  
  /**
   * if show instrumentation
   * @return if show instrumentation
   */
  public boolean isInstrumentationShow() {
    
    if (!GrouperUiConfig.retrieveConfig().propertyValueBoolean("uiV2.admin.instrumentation.show", true)) {
      return false;
    }
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    
    String error = GrouperUiFilter.requireUiGroup("uiV2.admin.instrumentation.must.be.in.group", loggedInSubject, false);

    if (StringUtils.isBlank(error)) {
      return true;
    }
    
    return false;
  }
  
  /**
   * if show daemon jobs
   * @return if show daemon jobs
   */
  public boolean isDaemonJobsShow() {
    
    if (!GrouperUiConfig.retrieveConfig().propertyValueBoolean("uiV2.admin.daemonJobs.show", true)) {
      return false;
    }
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    
    String error = GrouperUiFilter.requireUiGroup("uiV2.admin.daemonJobs.must.be.in.group", loggedInSubject, false);

    if (StringUtils.isBlank(error)) {
      return true;
    }
    
    return false;
  }

  
  /**
   * @return the guiInstrumentationDataInstances
   */
  public List<GuiInstrumentationDataInstance> getGuiInstrumentationDataInstances() {
    return guiInstrumentationDataInstances;
  }

  
  /**
   * @param guiInstrumentationDataInstances the guiInstrumentationDataInstances to set
   */
  public void setGuiInstrumentationDataInstances(
      List<GuiInstrumentationDataInstance> guiInstrumentationDataInstances) {
    this.guiInstrumentationDataInstances = guiInstrumentationDataInstances;
  }

  
  /**
   * @return the guiInstrumentationGraphResults
   */
  public Map<String, Map<String, Long>> getGuiInstrumentationGraphResults() {
    return guiInstrumentationGraphResults;
  }

  
  /**
   * @param guiInstrumentationGraphResults the guiInstrumentationGraphResults to set
   */
  public void setGuiInstrumentationGraphResults(
      Map<String, Map<String, Long>> guiInstrumentationGraphResults) {
    this.guiInstrumentationGraphResults = guiInstrumentationGraphResults;
  }

  
  /**
   * @return the guiInstrumentationDaysWithData
   */
  public Set<String> getGuiInstrumentationDaysWithData() {
    return guiInstrumentationDaysWithData;
  }

  
  /**
   * @param guiInstrumentationDaysWithData the guiInstrumentationDaysWithData to set
   */
  public void setGuiInstrumentationDaysWithData(Set<String> guiInstrumentationDaysWithData) {
    this.guiInstrumentationDaysWithData = guiInstrumentationDaysWithData;
  }

  
  /**
   * @return the guiInstrumentationFilterDate
   */
  public String getGuiInstrumentationFilterDate() {
    return guiInstrumentationFilterDate;
  }

  
  /**
   * @param guiInstrumentationFilterDate the guiInstrumentationFilterDate to set
   */
  public void setGuiInstrumentationFilterDate(String guiInstrumentationFilterDate) {
    this.guiInstrumentationFilterDate = guiInstrumentationFilterDate;
  }
  
  /**
   * set of jobs to show on screen
   */
  private List<GuiDaemonJob> guiDaemonJobs;
  
  /**
   * set of jobs to show on screen
   * @return set of jobs
   */
  public List<GuiDaemonJob> getGuiDaemonJobs() {
    return this.guiDaemonJobs;
  }

  /**
   * set of jobs to show on screen
   * @param guiDaemonJobs1
   */
  public void setGuiDaemonJobs(List<GuiDaemonJob> guiDaemonJobs1) {
    this.guiDaemonJobs = guiDaemonJobs1;
  }

  
  /**
   * @return the daemonJobsGuiPaging
   */
  public GuiPaging getDaemonJobsGuiPaging() {
    if (daemonJobsGuiPaging == null) {
      daemonJobsGuiPaging = new GuiPaging();
    }
    return daemonJobsGuiPaging;
  }

  
  /**
   * @param daemonJobsGuiPaging the daemonJobsGuiPaging to set
   */
  public void setDaemonJobsGuiPaging(GuiPaging daemonJobsGuiPaging) {
    this.daemonJobsGuiPaging = daemonJobsGuiPaging;
  }
  
  /**
   * @return refresh interval in seconds
   */
  public int getDaemonJobsRefreshInterval() {
    return GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.admin.daemonJobs.refreshInterval", 30);
  }
  
  /**
   * @return refresh count
   */
  public int getDaemonJobsRefreshCount() {
    return GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.admin.daemonJobs.refreshCount", 30);
  }

  
  /**
   * @return the daemonJobsShowExtendedResults
   */
  public boolean isDaemonJobsShowExtendedResults() {
    return daemonJobsShowExtendedResults;
  }

  
  /**
   * @param daemonJobsShowExtendedResults the daemonJobsShowExtendedResults to set
   */
  public void setDaemonJobsShowExtendedResults(boolean daemonJobsShowExtendedResults) {
    this.daemonJobsShowExtendedResults = daemonJobsShowExtendedResults;
  }

  
  /**
   * @return the guiHib3GrouperLoaderLogs
   */
  public List<GuiHib3GrouperLoaderLog> getGuiHib3GrouperLoaderLogs() {
    return guiHib3GrouperLoaderLogs;
  }

  
  /**
   * @param guiHib3GrouperLoaderLogs the guiHib3GrouperLoaderLogs to set
   */
  public void setGuiHib3GrouperLoaderLogs(
      List<GuiHib3GrouperLoaderLog> guiHib3GrouperLoaderLogs) {
    this.guiHib3GrouperLoaderLogs = guiHib3GrouperLoaderLogs;
  }
  
  /**
   * 
   * @return number of rows
   */
  public int getDaemonJobsViewLogsNumberOfRows() {
    return GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.loader.logs.maxSize", 400);
  }
}
