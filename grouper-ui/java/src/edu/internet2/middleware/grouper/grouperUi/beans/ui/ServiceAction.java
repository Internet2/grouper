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
  
  private String id;
  
  private GrouperTemplateLogicBase service;
  
  private int indentLevel;
  
  private ServiceActionType serviceActionType;
  
  private List<ServiceActionArgument> args = new ArrayList<ServiceActionArgument>();
  
  private String externalizedKey;
  
  private boolean defaultChecked;
  
  private ServiceAction parentServiceAction;
  
  private List<ServiceAction> chidrenServiceActions = new ArrayList<ServiceAction>();
  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public GrouperTemplateLogicBase getService() {
    return service;
  }

  public void setService(GrouperTemplateLogicBase service) {
    this.service = service;
  }
  

  public List<ServiceActionArgument> getArgs() {
    return args;
  }

  public void setArgs(List<ServiceActionArgument> args) {
    this.args = args;
  }

  public int getIndentLevel() {
    return indentLevel;
  }

  public void setIndentLevel(int indentLevel) {
    this.indentLevel = indentLevel;
  }

  public ServiceActionType getServiceActionType() {
    return serviceActionType;
  }

  public void setServiceActionType(ServiceActionType serviceActionType) {
    this.serviceActionType = serviceActionType;
  }


  public String getExternalizedKey() {
    return externalizedKey;
  }

  public void setExternalizedKey(String externalizedKey) {
    this.externalizedKey = externalizedKey;
  }

  public boolean isDefaultChecked() {
    return defaultChecked;
  }

  public void setDefaultChecked(boolean defaulChecked) {
    this.defaultChecked = defaulChecked;
  }
  
  public ServiceAction getParentServiceAction() {
    return parentServiceAction;
  }

  public void setParentServiceAction(ServiceAction parentServiceAction) {
    this.parentServiceAction = parentServiceAction;
  }
  
  public void addChildServiceAction(ServiceAction childServiceAction) {
    chidrenServiceActions.add(childServiceAction);
  }
  
  
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
