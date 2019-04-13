package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.reports.GrouperReportConfigurationBean;

public class GrouperReportConfigInstance {

  private GrouperReportConfigurationBean reportConfigBean;
  
  private List<GuiReportInstance> guiReportInstances = new ArrayList<GuiReportInstance>();

  public GrouperReportConfigurationBean getReportConfigBean() {
    return reportConfigBean;
  }

  public void setReportConfigBean(GrouperReportConfigurationBean reportConfigBean) {
    this.reportConfigBean = reportConfigBean;
  }

  public List<GuiReportInstance> getGuiReportInstances() {
    return guiReportInstances;
  }

  public void setGuiReportInstances(List<GuiReportInstance> guiReportInstances) {
    this.guiReportInstances = guiReportInstances;
  }

}
