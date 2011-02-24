/*
 * @author mchyzer
 * $Id: SimpleMembershipUpdateContainer.java,v 1.4 2009-11-02 08:50:40 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.attributeUpdate;

import java.io.Serializable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;



/**
 * bean for simple attribute update.  holds all state for this module
 */
@SuppressWarnings("serial")
public class AttributeUpdateRequestContainer implements Serializable {

  /** attribute def we are editing */
  private AttributeDef attributeDefToEdit;
  
  /** list of actions for the attribute def */
  private List<String> actions;
  
  /**
   * list of actions for the attribute def
   * @return actions
   */
  public List<String> getActions() {
    return this.actions;
  }

  /**
   * list of actions for the attribute def
   * @param actions1
   */
  public void setActions(List<String> actions1) {
    this.actions = actions1;
  }

  /**
   * attribute def we are editing
   * @return the attribute def
   */
  public AttributeDef getAttributeDefToEdit() {
    return this.attributeDefToEdit;
  }

  /**
   * attribute def we are editing
   * @param attributeDefToEdit1
   */
  public void setAttributeDefToEdit(AttributeDef attributeDefToEdit1) {
    this.attributeDefToEdit = attributeDefToEdit1;
  }

  /** if this is a create as opposed to update */
  private boolean create;
  
  
  
  /**
   * if this is a create as opposed to update
   * @return if create
   */
  public boolean isCreate() {
    return this.create;
  }

  /**
   * if this is a create as opposed to update
   * @param create1
   */
  public void setCreate(boolean create1) {
    this.create = create1;
  }

  /**
   * store to session scope
   */
  public void storeToRequest() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    httpServletRequest.setAttribute("attributeUpdateRequestContainer", this);
  }

  /**
   * retrieveFromSession, cannot be null
   * @return the app state in request scope
   */
  public static AttributeUpdateRequestContainer retrieveFromRequestOrCreate() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    AttributeUpdateRequestContainer attributeUpdateRequestContainer = 
      (AttributeUpdateRequestContainer)httpServletRequest.getAttribute("attributeUpdateRequestContainer");
    if (attributeUpdateRequestContainer == null) {
      attributeUpdateRequestContainer = new AttributeUpdateRequestContainer();
      attributeUpdateRequestContainer.storeToRequest();
    }
    return attributeUpdateRequestContainer;
  }

  /** 
   * text bean
   * @return text bean
   */
  public AttributeUpdateText getText() {
    return AttributeUpdateText.retrieveSingleton();
  }
  
  
  
}
