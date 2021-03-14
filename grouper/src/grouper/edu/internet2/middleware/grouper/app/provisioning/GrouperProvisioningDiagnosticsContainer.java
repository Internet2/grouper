package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Map;
import java.util.TreeMap;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.ui.util.ProgressBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;

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
    return this.report.toString();
  }


  
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }


  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }

  /**
   * report results
   */
  private StringBuilder report = new StringBuilder();

  /**
   * append configuration to diagnostics
   */
  public void appendConfiguration() {
    this.report.append("<h4>Configuration</h4>");
    
    Map<String, String> configuration = new TreeMap<String, String>();
    
    GrouperLoaderConfig grouperLoaderConfig = GrouperLoaderConfig.retrieveConfig();
    
    String configPrefix = "provisioner." + this.getGrouperProvisioner().getConfigId() + ".";
    
    for (String propertyName : grouperLoaderConfig.propertyNames()) {
      if (propertyName.startsWith(configPrefix)) {
        String suffix = GrouperUtil.prefixOrSuffix(propertyName, configPrefix, false);
        String lowerKey = suffix.toLowerCase();
        boolean secret = lowerKey.contains("pass") || lowerKey.contains("secret") || lowerKey.contains("private");
        configuration.put(propertyName, secret ? "****** (redacted)" : grouperLoaderConfig.propertyValueString(propertyName));
      }
    }

    this.report.append("<pre>");
    for (String propertyName : configuration.keySet()) {
      this.report.append(GrouperUtil.xmlEscape(propertyName + " = " + configuration.get(propertyName))).append("\n");
    }
    this.report.append("</pre>");
    
  }  
  /**
   * run diagnostics
   */
  public void runDiagnostics() {
    this.report = new StringBuilder();
    
    this.appendConfiguration();
    
  }
  

}
