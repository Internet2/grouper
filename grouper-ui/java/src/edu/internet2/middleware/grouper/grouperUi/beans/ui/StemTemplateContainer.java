/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author vsachdeva
 *
 */
public class StemTemplateContainer {
  
  /**
   * template type eg: service
   */
  private String templateType;
  
  /**
   * user specified template key eg: wiki
   */
  private String templateKey;
  
  /**
   * friendly name of the template. optional
   */
  private String templateFriendlyName;
  
  /**
   * template description. optional
   */
  private String templateDescription;
  
  /**
   * list of service actions for selected template type
   */
  private List<ServiceAction> serviceActions = new ArrayList<ServiceAction>();
  
  /**
   * implementation class for selected template type
   */
  private GrouperTemplateLogicBase templateLogic;
  
  /**
   * current service action
   */
  private ServiceAction currentServiceAction;
  
  /**
   * all the template types to labels
   */
  private Map<String, String> templateOptions = new HashMap<String, String>();
  

  /**
   * template type eg: service
   * @return
   */
  public String getTemplateType() {
    return templateType;
  }

  /**
   * @param templateType: template type eg: service
   */
  public void setTemplateType(String templateType) {
    this.templateType = templateType;
  }

  /**
   * @return user specified template key eg: wiki
   */
  public String getTemplateKey() {
    return templateKey;
  }

  /**
   * @param templateKey: user specified template key eg: wiki
   */
  public void setTemplateKey(String templateKey) {
    this.templateKey = templateKey;
  }

  /**
   * @return friendly name of the template. optional
   */
  public String getTemplateFriendlyName() {
    return templateFriendlyName;
  }

  /**
   * @param templateFriendlyName: friendly name of the template. optional
   */
  public void setTemplateFriendlyName(String templateFriendlyName) {
    this.templateFriendlyName = templateFriendlyName;
  }

  /**
   * @return template description. optional
   */
  public String getTemplateDescription() {
    return templateDescription;
  }

  /**
   * @param templateDescription: template description. optional
   */
  public void setTemplateDescription(String templateDescription) {
    this.templateDescription = templateDescription;
  }


  /**
   * @return implementation class for selected template type
   */
  public GrouperTemplateLogicBase getTemplateLogic() {
    return templateLogic;
  }

  /**
   * @param templateLogic: implementation class for selected template type
   */
  public void setTemplateLogic(GrouperTemplateLogicBase templateLogic) {
    this.templateLogic = templateLogic;
  }

  /**
   * @return all the template types to labels
   */
  public Map<String, String> getTemplateOptions() {
    return templateOptions;
  }

  /**
   * @param templateOptions: all the template types to labels
   */
  public void setTemplateOptions(Map<String, String> templateOptions) {
    this.templateOptions = templateOptions;
  }

  /**
   * @return list of service actions for selected template type
   */
  public List<ServiceAction> getServiceActions() {
    return serviceActions;
  }

  /**
   * @param serviceActions: list of service actions for selected template type
   */
  public void setServiceActions(List<ServiceAction> serviceActions) {
    this.serviceActions = serviceActions;
  }

  /**
   * @return current service action
   */
  public ServiceAction getCurrentServiceAction() {
    return currentServiceAction;
  }

  /**
   * @param currentServiceAction Current service action
   */
  public void setCurrentServiceAction(ServiceAction currentServiceAction) {
    this.currentServiceAction = currentServiceAction;
  }
  

}
