package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.List;

import edu.internet2.middleware.grouper.internal.util.GrouperUuid;

public abstract class GrouperTemplateLogicBase {

  /**
   * stem id user is working on
   */
  private String stemId;
  
  /**
   * stem id user is working on
   * @return
   */
  public String getStemId() {
    return stemId;
  }

  /**
   * stem id user is working on
   * @param stemId
   */
  public void setStemId(String stemId) {
    this.stemId = stemId;
  }

  /**
   * Should user be asked for key, friendly name and description?
   * @return
   */
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
  
  /**
   * create new service action
   * @param defaulChecked
   * @param indentLevel
   * @param externalizedKey
   * @param type
   * @param args
   * @param parentServiceAction
   * @return
   */
  protected final ServiceAction createNewServiceAction(boolean defaulChecked, int indentLevel, 
      String externalizedKey, ServiceActionType type, List<ServiceActionArgument> args,
      ServiceAction parentServiceAction) {
  
    ServiceAction serviceAction = new ServiceAction();
    serviceAction.setService(this);
    serviceAction.setDefaultChecked(defaulChecked);
    serviceAction.setIndentLevel(indentLevel);
    serviceAction.setExternalizedKey(externalizedKey);
    serviceAction.setServiceActionType(type);
    serviceAction.getArgs().addAll(args);
    serviceAction.setParentServiceAction(parentServiceAction);
    serviceAction.setId(GrouperUuid.getUuid());
    
    return serviceAction;
  }
  
  /**
   * list of service actions to show on the UI
   * @return
   */
  public abstract List<ServiceAction> getServiceActions();
  
  /**
   * label to show
   * @return
   */
  public abstract String getSelectLabelKey();
  
  
}
