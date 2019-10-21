package edu.internet2.middleware.grouper.grouperUi.beans.api;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinderResult;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;

/**
 * gui version of one attribute assign finder result
 */
public class GuiAttributeAssignFinderResult {
  
  /**
   * attribute assign finder result
   */
  private AttributeAssignFinderResult attributeAssignFinderResult;

  public GuiAttributeAssignFinderResult(AttributeAssignFinderResult attributeAssignFinderResult) {
    this.attributeAssignFinderResult = attributeAssignFinderResult;
  }

  /**
   * attribute assign finder result
   * @return
   */
  public AttributeAssignFinderResult getAttributeAssignFinderResult() {
    return attributeAssignFinderResult;
  }

  /**
   * get back gui version of attribute assign finder result 
   * @return
   */
  public GuiAttributeAssign getGuiAttributeAssign() {
    GuiAttributeAssign guiAttributeAssign =  new GuiAttributeAssign();
    AttributeAssign attributeAssign = attributeAssignFinderResult.getAttributeAssign();
    guiAttributeAssign.setAttributeAssign(attributeAssign);
    try {
      attributeAssign.retrieveAttributeAssignable().getAttributeDelegate().assertCanUpdateAttributeDefName(attributeAssign.getAttributeDefName());
      guiAttributeAssign.setCanUpdateAttributeDefName(true);
    } catch (InsufficientPrivilegeException e) {
      guiAttributeAssign.setCanUpdateAttributeDefName(false);
    }
    
    return guiAttributeAssign;
  }
  
  /**
   * get attribute assign owner group if not null
   * @return
   */
  public GuiGroup getOwnerGuiGroup() {
    
    Group ownerGroup = attributeAssignFinderResult.getOwnerGroup();
    
    if (ownerGroup != null) {
      return new GuiGroup(ownerGroup);
    }
    
    return null;
    
  }
  
  /**
   * get attribute assign owner stem if not null
   * @return
   */
  public GuiStem getOwnerGuiStem() {
    
    Stem ownerStem = attributeAssignFinderResult.getOwnerStem();
    
    if (ownerStem != null) {
      return new GuiStem(ownerStem);
    }
    
    return null;
    
  }
  
  /**
   * get attribute assign owner attribute def if not null
   * @return
   */
  public GuiAttributeDef getOwnerGuiAttributeDef() {
    
    AttributeDef ownerAttributeDef = attributeAssignFinderResult.getOwnerAttributeDef();
    
    if (ownerAttributeDef != null) {
      return new GuiAttributeDef(ownerAttributeDef);
    }
    
    return null;
  }
  
  /**
   * get attribute assign owner member if not null
   * @return
   */
  public GuiMember getOwnerGuiMember() {
    
    Member ownerMember = attributeAssignFinderResult.getOwnerMember();
    
    if (ownerMember != null) {
      return new GuiMember(ownerMember);
    }
    
    return null;
  }
  
  /**
   * get attribute assign owner membership if not null
   * @return
   */
  public GuiMembership getOwnerGuiMembership() {
    
    Membership ownerMembership = attributeAssignFinderResult.getOwnerMembership();
    
    if (ownerMembership != null) {
      return new GuiMembership(ownerMembership);
    }
    
    return null;
    
  }
  

}
