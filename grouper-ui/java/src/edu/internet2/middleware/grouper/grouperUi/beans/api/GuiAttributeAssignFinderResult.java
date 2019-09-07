package edu.internet2.middleware.grouper.grouperUi.beans.api;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinderResult;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;

public class GuiAttributeAssignFinderResult {
  
  private AttributeAssignFinderResult attributeAssignFinderResult;

  public GuiAttributeAssignFinderResult(AttributeAssignFinderResult attributeAssignFinderResult) {
    this.attributeAssignFinderResult = attributeAssignFinderResult;
  }

  public AttributeAssignFinderResult getAttributeAssignFinderResult() {
    return attributeAssignFinderResult;
  }

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
  
  public GuiGroup getOwnerGuiGroup() {
    
    Group ownerGroup = attributeAssignFinderResult.getOwnerGroup();
    
    if (ownerGroup != null) {
      return new GuiGroup(ownerGroup);
    }
    
    return null;
    
  }
  
  public GuiStem getOwnerGuiStem() {
    
    Stem ownerStem = attributeAssignFinderResult.getOwnerStem();
    
    if (ownerStem != null) {
      return new GuiStem(ownerStem);
    }
    
    return null;
    
  }
  
  public GuiAttributeDef getOwnerGuiAttributeDef() {
    
    AttributeDef ownerAttributeDef = attributeAssignFinderResult.getOwnerAttributeDef();
    
    if (ownerAttributeDef != null) {
      return new GuiAttributeDef(ownerAttributeDef);
    }
    
    return null;
  }
  
  public GuiMember getOwnerGuiMember() {
    
    Member ownerMember = attributeAssignFinderResult.getOwnerMember();
    
    if (ownerMember != null) {
      return new GuiMember(ownerMember);
    }
    
    return null;
  }
  

}
