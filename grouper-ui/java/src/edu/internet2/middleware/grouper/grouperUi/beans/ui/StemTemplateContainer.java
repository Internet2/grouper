/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author vsachdeva
 *
 */
public class StemTemplateContainer {
  
  private String templateType;
  
  private String templateKey;
  
  private String templateFriendlyName;
  
  private String templateDescription;
  
  private List<ServiceAction> serviceActions = new ArrayList<ServiceAction>();
  
  private GrouperTemplateLogicBase templateLogic;
  
  private ServiceAction currentServiceAction;
  
  private Map<String, String> templateOptions = new HashMap<String, String>();
  

  public String getTemplateType() {
    return templateType;
  }

  public void setTemplateType(String templateType) {
    this.templateType = templateType;
  }

  public String getTemplateKey() {
    return templateKey;
  }

  public void setTemplateKey(String templateKey) {
    this.templateKey = templateKey;
  }

  public String getTemplateFriendlyName() {
    return templateFriendlyName;
  }

  public void setTemplateFriendlyName(String templateFriendlyName) {
    this.templateFriendlyName = templateFriendlyName;
  }

  public String getTemplateDescription() {
    return templateDescription;
  }

  public void setTemplateDescription(String templateDescription) {
    this.templateDescription = templateDescription;
  }


  public GrouperTemplateLogicBase getTemplateLogic() {
    return templateLogic;
  }

  public void setTemplateLogic(GrouperTemplateLogicBase templateLogic) {
    this.templateLogic = templateLogic;
  }

  public ServiceAction getCurrentServiceAction() {
    return currentServiceAction;
  }

  public void setCurrentServiceAction(ServiceAction currentServiceAction) {
    this.currentServiceAction = currentServiceAction;
  }

  public Map<String, String> getTemplateOptions() {
    return templateOptions;
  }

  public void setTemplateOptions(Map<String, String> templateOptions) {
    this.templateOptions = templateOptions;
  }

  public List<ServiceAction> getServiceActions() {
    return serviceActions;
  }

  public void setServiceActions(List<ServiceAction> serviceActions) {
    this.serviceActions = serviceActions;
  }
  

}
