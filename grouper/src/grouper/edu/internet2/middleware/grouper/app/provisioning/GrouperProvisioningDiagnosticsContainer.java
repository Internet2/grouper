package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouper.ui.util.ProgressBean;

public class GrouperProvisioningDiagnosticsContainer {
  
  /**
   * uniquely identifies this diagnostics request as opposed to other diagnostics in other tabs
   */
  private String uniqueDiagnosticsId;
  
  /**
   * have a progress bean
   */
  private ProgressBean progressBean = new ProgressBean();
  
  private GrouperProvisioner grouperProvisioner;
  
  /**
   * have a progress bean
   * @return the progressBean
   */
  public ProgressBean getProgressBean() {
    return this.progressBean;
  }

  
  public String getUniqueDiagnosticsId() {
    return uniqueDiagnosticsId;
  }

  
  public void setUniqueDiagnosticsId(String uniqueDiagnosticsId) {
    this.uniqueDiagnosticsId = uniqueDiagnosticsId;
  }
  
  public String getReport() {
    return "<pre>valid report</pre>";
  }


  
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }


  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }


  public void runDiagnostics() {
    
  }
  

}
