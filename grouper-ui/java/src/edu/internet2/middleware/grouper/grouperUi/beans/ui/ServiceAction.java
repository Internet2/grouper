/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * @author vsachdeva
 *
 */
public class ServiceAction {
  

  /**
   * id of this service class
   */
  private String id;
  
  /**
   * implementation class associated with this service action
   */
  private GrouperTemplateLogicBase service;
  
  /**
   * indent level
   */
  private int indentLevel;
  
  /**
   * service action type
   */
  private ServiceActionType serviceActionType;
  
  /**
   * list of arguments
   */
  private List<ServiceActionArgument> args = new ArrayList<ServiceActionArgument>();
  
  /**
   * externalized key
   */
  private String externalizedKey;
  
  /**
   * should show checked on the UI by default?
   */
  private boolean defaultChecked;
  
  /**
   * parent service action
   */
  private ServiceAction parentServiceAction;
  
  /**
   * list of children service actions
   */
  private List<ServiceAction> chidrenServiceActions = new ArrayList<ServiceAction>();
  
  /**
   * id of this service class
   * @return
   */
  public String getId() {
    return id;
  }

  /**
   * id of this service class
   * @param id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * implementation class associated with this service action
   * @return
   */
  public GrouperTemplateLogicBase getService() {
    return service;
  }

  /**
   * implementation class associated with this service action
   * @param service
   */
  public void setService(GrouperTemplateLogicBase service) {
    this.service = service;
  }
  

  /**
   * list of arguments
   * @return
   */
  public List<ServiceActionArgument> getArgs() {
    return args;
  }

  /**
   * list of arguments
   * @param args
   */
  public void setArgs(List<ServiceActionArgument> args) {
    this.args = args;
  }

  /**
   * indent level
   * @return
   */
  public int getIndentLevel() {
    return indentLevel;
  }

  /**
   * indent level
   * @param indentLevel
   */
  public void setIndentLevel(int indentLevel) {
    this.indentLevel = indentLevel;
  }

  /**
   * service action type
   * @return
   */
  public ServiceActionType getServiceActionType() {
    return serviceActionType;
  }

  /**
   * service action type
   * @param serviceActionType
   */
  public void setServiceActionType(ServiceActionType serviceActionType) {
    this.serviceActionType = serviceActionType;
  }

  /**
   * externalized key 
   * @return
   */
  public String getExternalizedKey() {
    return externalizedKey;
  }

  /**
   * externalized key
   * @param externalizedKey
   */
  public void setExternalizedKey(String externalizedKey) {
    this.externalizedKey = externalizedKey;
  }

  /**
   * should show checked on the UI by default?
   * @return
   */
  public boolean isDefaultChecked() {
    return defaultChecked;
  }

  /**
   * should show checked on the UI by default?
   * @param defaulChecked
   */
  public void setDefaultChecked(boolean defaulChecked) {
    this.defaultChecked = defaulChecked;
  }
  
  /**
   * parent service action
   * @return
   */
  public ServiceAction getParentServiceAction() {
    return parentServiceAction;
  }

  /**
   * parent service action
   * @param parentServiceAction
   */
  public void setParentServiceAction(ServiceAction parentServiceAction) {
    this.parentServiceAction = parentServiceAction;
  }
  
  /**
   * add to the list of child service actions
   * @param childServiceAction
   */
  public void addChildServiceAction(ServiceAction childServiceAction) {
    chidrenServiceActions.add(childServiceAction);
  }
  
  /**
   * list of children service actions
   * @return
   */
  public List<ServiceAction> getChidrenServiceActions() {
    return chidrenServiceActions;
  }

  public Map<String, String> getArgMap() {
    Map<String, String> argsMap = new HashMap<String, String>();
    
    for (ServiceActionArgument arg: args) {
      argsMap.put(arg.getName(), arg.getValue());
    }
    
    return argsMap;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ServiceAction)) {
      return false;
    }
    return StringUtils.equals(this.id, ((ServiceAction)obj).id);
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return StringUtils.defaultString(this.id).hashCode();
  }
  
  

}
