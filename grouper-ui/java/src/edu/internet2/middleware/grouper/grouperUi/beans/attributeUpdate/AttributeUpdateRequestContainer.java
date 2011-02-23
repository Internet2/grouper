/*
 * @author mchyzer
 * $Id: SimpleMembershipUpdateContainer.java,v 1.4 2009-11-02 08:50:40 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.attributeUpdate;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;



/**
 * bean for simple attribute update.  holds all state for this module
 */
public class AttributeUpdateRequestContainer implements Serializable {

  /** attribute def we are editing */
  private AttributeDef attributeDefToEdit;
  
  
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
