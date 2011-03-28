/*
 * @author mchyzer
 * $Id: SimpleMembershipUpdateContainer.java,v 1.4 2009-11-02 08:50:40 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.attributeNameUpdate;

import java.io.Serializable;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;



/**
 * bean for simple attribute update.  holds all state for this module
 */
@SuppressWarnings("serial")
public class AttributeNameUpdateRequestContainer implements Serializable {

  /** attribute definition to filter by */
  private AttributeDef attributeDefForFilter = null;
  
  /** if there is an attribute def on the new or edit screen */
  private AttributeDef attributeDef = null;
  
  /** attribute def names that imply this */
  private Set<AttributeDefName> attributeDefNamesThatImplyThis = null;
  
  /** attribute def names implied by this */
  private Set<AttributeDefName> attributeDefNamesImpliedByThis = null;
  
  /** attribute def names that imply this immediate */
  private Set<AttributeDefName> attributeDefNamesThatImplyThisImmediate = null;
  
  /** attribute def names implied by this immediate */
  private Set<AttributeDefName> attributeDefNamesImpliedByThisImmediate = null;
  
  /**
   * attribute def names that imply this immediate
   * @return attribute def names that imply this immediate
   */
  public Set<AttributeDefName> getAttributeDefNamesThatImplyThisImmediate() {
    return this.attributeDefNamesThatImplyThisImmediate;
  }

  /**
   * attribute def names that imply this immediate
   * @param attributeDefNamesThatImplyThisImmediate1
   */
  public void setAttributeDefNamesThatImplyThisImmediate(
      Set<AttributeDefName> attributeDefNamesThatImplyThisImmediate1) {
    this.attributeDefNamesThatImplyThisImmediate = attributeDefNamesThatImplyThisImmediate1;
  }

  /**
   * attribute def names implied by this immediate
   * @return attribute def names implied by this immediate
   */
  public Set<AttributeDefName> getAttributeDefNamesImpliedByThisImmediate() {
    return this.attributeDefNamesImpliedByThisImmediate;
  }

  /**
   * attribute def names implied by this immediate
   * @param attributeDefNamesImpliedByThisImmediate1
   */
  public void setAttributeDefNamesImpliedByThisImmediate(
      Set<AttributeDefName> attributeDefNamesImpliedByThisImmediate1) {
    this.attributeDefNamesImpliedByThisImmediate = attributeDefNamesImpliedByThisImmediate1;
  }

  /**
   * attribute def names implied by this
   * @return attribute def names implied by this
   */
  public Set<AttributeDefName> getAttributeDefNamesImpliedByThis() {
    return this.attributeDefNamesImpliedByThis;
  }

  /**
   * attribute def names implied by this
   * @param attributeDefNamesImpliedByThis1
   */
  public void setAttributeDefNamesImpliedByThis(
      Set<AttributeDefName> attributeDefNamesImpliedByThis1) {
    this.attributeDefNamesImpliedByThis = attributeDefNamesImpliedByThis1;
  }

  /**
   * attribute def names that imply this
   * @return attribute def names that imply this
   */
  public Set<AttributeDefName> getAttributeDefNamesThatImplyThis() {
    return this.attributeDefNamesThatImplyThis;
  }

  /**
   * attribute def names that imply this
   * @param attributeDefNamesThatImplyThis1
   */
  public void setAttributeDefNamesThatImplyThis(
      Set<AttributeDefName> attributeDefNamesThatImplyThis1) {
    this.attributeDefNamesThatImplyThis = attributeDefNamesThatImplyThis1;
  }

  /**
   * if there is an attribute def on the new or edit screen
   * @return attribute def
   */
  public AttributeDef getAttributeDef() {
    return this.attributeDef;
  }

  /**
   * if there is an attribute def on the new or edit screen
   * @param attributeDef1
   */
  public void setAttributeDef(AttributeDef attributeDef1) {
    this.attributeDef = attributeDef1;
  }

  /**
   * attribute definition to filter by
   * @return attribute defintion
   */
  public AttributeDef getAttributeDefForFilter() {
    return this.attributeDefForFilter;
  }

  /**
   * attribute definition to filter by
   * @param attributeDefForFilter1
   */
  public void setAttributeDefForFilter(AttributeDef attributeDefForFilter1) {
    this.attributeDefForFilter = attributeDefForFilter1;
  }

  /**
   * store to session scope
   */
  public void storeToRequest() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    httpServletRequest.setAttribute("attributeNameUpdateRequestContainer", this);
  }

  /**
   * retrieveFromSession, cannot be null
   * @return the app state in request scope
   */
  public static AttributeNameUpdateRequestContainer retrieveFromRequestOrCreate() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    AttributeNameUpdateRequestContainer attributeNameUpdateRequestContainer = 
      (AttributeNameUpdateRequestContainer)httpServletRequest.getAttribute("attributeNameUpdateRequestContainer");
    if (attributeNameUpdateRequestContainer == null) {
      attributeNameUpdateRequestContainer = new AttributeNameUpdateRequestContainer();
      attributeNameUpdateRequestContainer.storeToRequest();
    }
    return attributeNameUpdateRequestContainer;
  }

  /** 
   * text bean
   * @return text bean
   */
  public AttributeNameUpdateText getText() {
    return AttributeNameUpdateText.retrieveSingleton();
  }

  /** attribute def name to edit */
  private AttributeDefName attributeDefNameToEdit;
  
  /**
   * attribute def name to edit
   * @return the attribute def name
   */
  public AttributeDefName getAttributeDefNameToEdit() {
    return this.attributeDefNameToEdit;
  }

  /**
   * attribute def name to edit
   * @param attributeDefNameToEdit1
   */
  public void setAttributeDefNameToEdit(AttributeDefName attributeDefNameToEdit1) {
    this.attributeDefNameToEdit = attributeDefNameToEdit1;
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


}
