/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiInstrumentationDataInstance;
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
}
