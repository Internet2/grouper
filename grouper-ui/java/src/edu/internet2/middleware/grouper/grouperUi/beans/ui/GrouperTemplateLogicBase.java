package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.List;

public abstract class GrouperTemplateLogicBase {

  private String stemId;
  
  
  public String getStemId() {
    return stemId;
  }

  public void setStemId(String stemId) {
    this.stemId = stemId;
  }

  public boolean isPromptForKeyAndLabelAndDescription() {
    return true;
  }
  
  
  /**
   *
   * @param selectedServiceActions - list of service actions selected by user on the UI
   */
  public boolean validate(List<ServiceAction> selectedServiceActions) {

    for (ServiceAction serviceAction: selectedServiceActions) {
      
      ServiceAction temp = new ServiceAction();
      temp.setId(serviceAction.getId());
      temp.setParentServiceAction(serviceAction.getParentServiceAction());
      
      // for each selected service action, go through the hierarchy upwards and make sure everything is selected.
      while (temp.getParentServiceAction() != null) {
         if (!selectedServiceActions.contains(temp.getParentServiceAction())) {
           return false;
         }
         ServiceAction tempTemp = temp.getParentServiceAction();
         temp = new ServiceAction();
         temp.setId(tempTemp.getId());
         temp.setParentServiceAction(tempTemp.getParentServiceAction());
      }
      
    }
    
    return true;
  }
  
  
  public abstract List<ServiceAction> getServiceActions();
  
  public abstract String getSelectLabelKey();
  
  
}
