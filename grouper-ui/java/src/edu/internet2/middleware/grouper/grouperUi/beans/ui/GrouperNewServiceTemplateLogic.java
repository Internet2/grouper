/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vsachdeva
 *
 */
public class GrouperNewServiceTemplateLogic extends GrouperTemplateLogicBase {
  
  
  @Override
  public List<ServiceAction> displayOnScreen() {
    
    List<ServiceAction> serviceActions = new ArrayList<ServiceAction>();
    
    ServiceAction serviceAction = new ServiceAction();
    serviceAction.setService(this);
    return serviceActions;
  }
  
  

}
