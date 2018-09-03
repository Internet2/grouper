/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.List;

/**
 * @author vsachdeva
 *
 */
public class StemTemplateContainer {
  
  private String templateType;
  
  private String templateKey;
  
  private String templateFriendlyName;
  
  private String templateDescription;
  
  private List<ServiceAction> serviceActions;
  

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

  public List<ServiceAction> getServiceActions() {
    return serviceActions;
  }

  public void setServiceActions(List<ServiceAction> serviceActions) {
    this.serviceActions = serviceActions;
  }
  

}
