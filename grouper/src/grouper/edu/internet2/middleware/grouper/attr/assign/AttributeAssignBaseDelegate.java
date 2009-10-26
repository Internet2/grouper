/**
 * @author mchyzer
 * $Id: AttributeAssignBaseDelegate.java,v 1.6 2009-10-26 02:26:07 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.assign;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;


/**
 * delegate privilege calls from attribute defs
 */
public abstract class AttributeAssignBaseDelegate {

  /**
   */
  AttributeAssignBaseDelegate() {
    //empty
  }
  
  /**
   * @param action is the action on the attribute assignment (e.g. read, write, assign [default])
   * if null, should go to default
   * @param attributeDefName
   * @return attribute assign
   */
  abstract AttributeAssign newAttributeAssign(String action, AttributeDefName attributeDefName);
  
  /**
   * make sure the user can read the attribute (including looking at object if necessary)
   * @param attributeDefName
   */
  void assertCanReadAttributeDefName(AttributeDefName attributeDefName) {
    AttributeDef attributeDef = attributeDefName.getAttributeDef();
    assertCanReadAttributeDef(attributeDef);
  }

  /**
   * make sure the user can read the attribute (including looking at object if necessary)
   * @param attributeDef
   */
  abstract void assertCanReadAttributeDef(AttributeDef attributeDef);

  /**
   * make sure the user can update the attribute (including looking at object if necessary)
   * @param attributeDefName
   */
  abstract void assertCanUpdateAttributeDefName(AttributeDefName attributeDefName);

  /** delegatable result */
  private AttributeAssignDelegatable attributeAssignDelegatable = null;
  
  /**
   * make sure the user can delegate the attribute
   * @param action
   * @param attributeDefName
   */
  void assertCanDelegateAttributeDefName(String action, AttributeDefName attributeDefName) {
    if (this.attributeAssignDelegatable == null) {
      this.attributeAssignDelegatable = this.retrieveDelegatable(action, attributeDefName);
    }
    
    if (this.attributeAssignDelegatable.delegatable()) {
      return;
    }
    throw new RuntimeException("Cannot delegate: " + this + ", " + action + ", " + attributeDefName);
  }

  /**
   * retrieve if delegatable
   * @param action 
   * @param attributeDefName 
   * @return if delegatable or grant
   */
  private AttributeAssignDelegatable retrieveDelegatable(String action, AttributeDefName attributeDefName) {
    
    if (AttributeDefType.perm != attributeDefName.getAttributeDef().getAttributeDefType()) {
      throw new RuntimeException("Can only delegate a permission: " 
          + attributeDefName.getAttributeDef().getAttributeDefType());
    }
    
    Set<PermissionEntry> permissionEntries = GrouperDAOFactory.getFactory().getPermissionEntry()
      .findByMemberIdAndAttributeDefNameId(GrouperSession.staticGrouperSession().getMemberUuid(), attributeDefName.getId());
    
    boolean isGrant = false;
    boolean isDelegate = false;
    action = StringUtils.defaultString(action, AttributeDef.ACTION_DEFAULT);
    for (PermissionEntry permissionEntry : permissionEntries) {
      if (permissionEntry.isEnabled() && StringUtils.equals(action, permissionEntry.getAction())) {
        AttributeAssignDelegatable localDelegatable = permissionEntry.getAttributeAssignDelegatable();
        isGrant = isGrant || (localDelegatable == AttributeAssignDelegatable.GRANT);
        isDelegate = isDelegate || localDelegatable.delegatable();
      }
    }
    if (isGrant) {
      return AttributeAssignDelegatable.GRANT;
    }
    if (isDelegate) {
      return AttributeAssignDelegatable.TRUE;
    }
    return AttributeAssignDelegatable.FALSE;
  }
  
  /**
   * make sure the user can grant delegation to the attribute
   * @param action
   * @param attributeDefName
   */
  void assertCanGrantAttributeDefName(String action, AttributeDefName attributeDefName) {
    if (this.attributeAssignDelegatable == null) {
      this.attributeAssignDelegatable = this.retrieveDelegatable(action, attributeDefName);
    }
    
    if (this.attributeAssignDelegatable == AttributeAssignDelegatable.GRANT) {
      return;
    }
    throw new RuntimeException("Cannot grant: " + this + ", " + action + ", " + attributeDefName);

  }


  
  /**
   * 
   * @param attributeDefName
   * @return the result including if added or already there
   */
  public AttributeAssignResult assignAttribute(AttributeDefName attributeDefName) {

    return assignAttribute(null, attributeDefName);
  }

  /**
   * 
   * @param attributeDefNameName
   * @return the result including if added or already there
   */
  public AttributeAssignResult assignAttributeByName(String attributeDefNameName) {
    return assignAttributeByName(null, attributeDefNameName);
  }

  /**
   * 
   * @param attributeDefNameId
   * @return the result including if added or already there
   */
  public AttributeAssignResult assignAttributeById(String attributeDefNameId) {
    return this.assignAttributeById(null, attributeDefNameId);
  }

  /**
   * 
   * @param attributeDefNameId
   * @return true if has attribute, false if not
   */
  public boolean hasAttributeById(String attributeDefNameId) {
    return hasAttributeById(null, attributeDefNameId);
  }
  
  /**
   * 
   * @param attributeDefName
   * @return true if has attribute, false if not
   */
  public boolean hasAttribute(AttributeDefName attributeDefName) {
    return hasAttribute(null, attributeDefName);
  }

  /**
   * 
   * @param action on the assignment
   * @param attributeDefName
   * @param checkSecurity 
   * @return true if has attribute, false if not
   */
  boolean hasAttributeHelper(String action, AttributeDefName attributeDefName, boolean checkSecurity) {
    if (checkSecurity) {
      this.assertCanReadAttributeDefName(attributeDefName);
    }
    Set<AttributeAssign> attributeAssigns = retrieveAttributeAssignsByOwnerAndAttributeDefNameId(attributeDefName.getId());
    
    action = StringUtils.defaultIfEmpty(action, AttributeDef.ACTION_DEFAULT);
    for (AttributeAssign attributeAssign : attributeAssigns) {
      String currentAttributeAction = attributeAssign.getAttributeAssignAction().getName();
      if (StringUtils.equals(action, currentAttributeAction)) {
        return true;
      }
    }

    return false;
  }

  /**
   * retrieve an assignment (should be single assign)
   * @param action
   * @param attributeDefName
   * @param checkSecurity
   * @param exceptionIfNull
   * @return the assignment
   */
  public AttributeAssign retrieveAssignment(String action, AttributeDefName attributeDefName, 
      boolean checkSecurity, boolean exceptionIfNull) {
    if (checkSecurity) {
      this.assertCanReadAttributeDefName(attributeDefName);
    }
    Set<AttributeAssign> attributeAssigns = retrieveAttributeAssignsByOwnerAndAttributeDefNameId(attributeDefName.getId());
    return retrieveAssignmentHelper(action, attributeDefName, exceptionIfNull,
        attributeAssigns);
  }

  /**
   * @param action
   * @param attributeDefName
   * @param exceptionIfNull
   * @param attributeAssigns
   * @return assignment
   */
  private AttributeAssign retrieveAssignmentHelper(String action,
      AttributeDefName attributeDefName, boolean exceptionIfNull,
      Set<AttributeAssign> attributeAssigns) {
    AttributeAssign attributeAssignResult = null;
    action = StringUtils.defaultIfEmpty(action, AttributeDef.ACTION_DEFAULT);
    for (AttributeAssign attributeAssign : attributeAssigns) {
      String currentAttributeAction = attributeAssign.getAttributeAssignAction().getName();
      if (StringUtils.equals(action, currentAttributeAction)) {
        if (attributeAssignResult != null) {
          throw new RuntimeException("Multiple assignments exist: " + attributeDefName + ", " + action + ", " + this);
        }
        attributeAssignResult = attributeAssign;
      }
    }
    if (exceptionIfNull && attributeAssignResult == null) {
      throw new RuntimeException("Cant find assignment: " + action + ", " + attributeDefName + ", " + this);
    }
    return attributeAssignResult;
  }

  /**
   * see if the group
   * @param attributeDefNameName
   * @return true if has attribute, false if not
   */
  public boolean hasAttributeByName(String attributeDefNameName) {
    return hasAttributeByName(null, attributeDefNameName);
  }

  /**
   * @param attributeDefId
   * @return the assignments for a def name
   */
  public Set<AttributeAssign> retrieveAssignmentsByAttributeDefId(String attributeDefId) {
    AttributeDef attributeDef = GrouperDAOFactory.getFactory()
      .getAttributeDef().findByIdSecure(attributeDefId, true);
    return retrieveAssignments(attributeDef);
  }
  
  /**
   * @param attributeDefId
   * @return the assignments for a def
   */
  public Set<AttributeDefName> retrieveAttributesByAttributeDefId(String attributeDefId) {
    AttributeDef attributeDef = GrouperDAOFactory.getFactory()
      .getAttributeDef().findByIdSecure(attributeDefId, true);
    return retrieveAttributes(attributeDef);
  }
  
  /**
   * @param nameOfAttributeDef
   * @return the attributes for a def
   */
  public Set<AttributeDefName> retrieveAttributesByAttributeDef(String nameOfAttributeDef) {
    AttributeDef attributeDef = GrouperDAOFactory.getFactory()
      .getAttributeDef().findByNameSecure(nameOfAttributeDef, true);
    return retrieveAttributes(attributeDef);
  }
  
  /**
   * @param name is the name of the attribute def
   * @return the assignments for a def
   */
  public Set<AttributeAssign> retrieveAssignmentsByAttributeDef(String name) {
    
    AttributeDef attributeDef = AttributeDefFinder.findByName(name, true);
    
    return retrieveAssignments(attributeDef);
  }
  
  /**
   * find the assignments of any name associated with a def
   * @param attributeDef
   * @return the set of assignments or the empty set
   */
  public Set<AttributeAssign> retrieveAssignments(AttributeDef attributeDef) {
    this.assertCanReadAttributeDef(attributeDef);

    return retrieveAttributeAssignsByOwnerAndAttributeDefId(attributeDef.getId());

  }

  /**
   * @param attributeDefName
   * @return the assignments for a def name
   */
  public Set<AttributeAssign> retrieveAssignments(AttributeDefName attributeDefName) {
    this.assertCanReadAttributeDefName(attributeDefName);

    return retrieveAttributeAssignsByOwnerAndAttributeDefNameId(attributeDefName.getId());
  }

  /**
   * @param attributeDef
   * @return the attributes for a def
   */
  public Set<AttributeDefName> retrieveAttributes(AttributeDef attributeDef) {
    this.assertCanReadAttributeDef(attributeDef);
    return retrieveAttributeDefNamesByOwnerAndAttributeDefId(attributeDef.getId());
  }
  

  /**
   * 
   * @param attributeDefName
   * @return if removed or already not assigned
   */
  public boolean removeAttribute(AttributeDefName attributeDefName) {
    return removeAttribute(null, attributeDefName);
  }

  /**
   * get attribute assigns by owner and attribute def name id
   * @param attributeDefNameId
   * @return set of assigns or empty if none there
   */
  abstract Set<AttributeAssign> retrieveAttributeAssignsByOwnerAndAttributeDefNameId(String attributeDefNameId);
  
  
  /**
   * get attribute assigns by owner and attribute def id
   * @param attributeDefId
   * @return set of assigns or empty if none there
   */
  abstract Set<AttributeAssign> retrieveAttributeAssignsByOwnerAndAttributeDefId(String attributeDefId);

  /**
   * 
   * @param attributeDefNameId
   * @return if added or already there
   */
  public boolean removeAttributeById(String attributeDefNameId) {
    return removeAttributeById(null, attributeDefNameId);
  }

  /**
   * 
   * @param attributeDefNameName
   * @return if added or already there
   */
  public boolean removeAttributeByName(String attributeDefNameName) {
    return removeAttributeByName(null, attributeDefNameName);
  }

  /**
   * get attribute def names by owner and attribute def id
   * @param attributeDefId
   * @return set of def names or empty if none there
   */
  abstract Set<AttributeDefName> retrieveAttributeDefNamesByOwnerAndAttributeDefId(String attributeDefId);

  /**
   * @param action is the action on the assignment (null means default action)
   * @param attributeDefName
   * @return the result including if added or already there
   */
  public AttributeAssignResult assignAttribute(String action, AttributeDefName attributeDefName) {
    return this.assignAttributeHelper(action, attributeDefName, true);

  }

  /**
   * @param action is the action on the assignment (null means default action)
   * @param attributeDefName
   * @param checkSecurity
   * @return the result including if added or already there
   */
  AttributeAssignResult assignAttributeHelper(String action, AttributeDefName attributeDefName, boolean checkSecurity) {
    if (checkSecurity) {
      this.assertCanUpdateAttributeDefName(attributeDefName);
    }

    AttributeAssign attributeAssign = retrieveAssignment(action, attributeDefName, false, false);
    
    if (attributeAssign != null) {
      return new AttributeAssignResult(false, attributeAssign);
    }
    
    attributeAssign = newAttributeAssign(action, attributeDefName);
    
    if (StringUtils.isBlank(attributeAssign.getAttributeAssignActionId())) {
      attributeAssign.setAttributeAssignActionId(attributeDefName.getAttributeDef()
          .getAttributeDefActionDelegate().allowedAction(AttributeDef.ACTION_DEFAULT, true).getId());
    }
    
    attributeAssign.saveOrUpdate();

    return new AttributeAssignResult(true, attributeAssign);

  }

  /**
   * @param action is the action on the assignment (null means default action)
   * @param attributeDefNameId
   * @return the result including if added or already there
   */
  public AttributeAssignResult assignAttributeById(String action, String attributeDefNameId) {
    
    AttributeDefName attributeDefName = GrouperDAOFactory.getFactory()
      .getAttributeDefName().findByIdSecure(attributeDefNameId, true);
    return assignAttribute(action, attributeDefName);

  }

  /**
   * @param action is the action on the assignment (null means default action)
   * @param attributeDefNameName
   * @return the result including if added or already there
   */
  public AttributeAssignResult assignAttributeByName(String action, String attributeDefNameName) {
    AttributeDefName attributeDefName = GrouperDAOFactory.getFactory()
      .getAttributeDefName().findByNameSecure(attributeDefNameName, true);
    return assignAttribute(action, attributeDefName);

  }

  /**
   * @param action is the action on the assignment (null means default action)
   * @param attributeDefName
   * @return true if has attribute, false if not
   */
  public boolean hasAttribute(String action, AttributeDefName attributeDefName) {
    return hasAttributeHelper(action, attributeDefName, true);
  }

  /**
   * @param action is the action on the assignment (null means default action)
   * @param attributeDefNameId
   * @return true if has attribute, false if not
   */
  public boolean hasAttributeById(String action, String attributeDefNameId) {
    AttributeDefName attributeDefName = GrouperDAOFactory.getFactory()
      .getAttributeDefName().findByIdSecure(attributeDefNameId, true);

    return hasAttribute(action, attributeDefName);

  }

  /**
   * see if the group
   * @param action is the action on the assignment (null means default action)
   * @param attributeDefNameName
   * @return true if has attribute, false if not
   */
  public boolean hasAttributeByName(String action, String attributeDefNameName) {
    AttributeDefName attributeDefName = GrouperDAOFactory.getFactory()
      .getAttributeDefName().findByNameSecure(attributeDefNameName, true);
    
    Set<AttributeAssign> attributeAssigns = retrieveAssignments(attributeDefName);
    action = StringUtils.defaultIfEmpty(action, AttributeDef.ACTION_DEFAULT);
    for (AttributeAssign attributeAssign : attributeAssigns) {
      String currentAttributeAction = attributeAssign.getAttributeAssignAction().getId();
      if (StringUtils.equals(action, currentAttributeAction)) {
        return true;
      }
    }
    return false;

  }

  /**
   * 
   * @param action is the action on the assignment (null means default action)
   * @param attributeDefName
   * @return if removed or already not assigned
   */
  public boolean removeAttribute(String action, AttributeDefName attributeDefName) {
    return removeAttributeHelper(action, attributeDefName, true);
    

  }

  /**
   * 
   * @param action is the action on the assignment (null means default action)
   * @param attributeDefName
   * @param checkSecurity 
   * @return if removed or already not assigned
   */
  private boolean removeAttributeHelper(String action, AttributeDefName attributeDefName, boolean checkSecurity) {
    if (checkSecurity) {
      this.assertCanUpdateAttributeDefName(attributeDefName);
    }
    
    //see if it exists
    if (!this.hasAttributeHelper(action, attributeDefName, false)) {
      return false;
    }
    action = StringUtils.defaultIfEmpty(action, AttributeDef.ACTION_DEFAULT);
    Set<AttributeAssign> attributeAssigns = retrieveAttributeAssignsByOwnerAndAttributeDefNameId(attributeDefName.getId());
    for (AttributeAssign attributeAssign : attributeAssigns) {
      String currentAttributeAction = attributeAssign.getAttributeAssignAction().getId();
      if (StringUtils.equals(action, currentAttributeAction)) {
        attributeAssign.delete();
      }
    }
  
    return true;
  }

  /**
   * 
   * @param action is the action on the assignment (null means default action)
   * @param attributeDefNameId
   * @return if added or already there
   */
  public boolean removeAttributeById(String action, String attributeDefNameId) {
    AttributeDefName attributeDefName = GrouperDAOFactory.getFactory()
      .getAttributeDefName().findByIdSecure(attributeDefNameId, true);
    return removeAttribute(action, attributeDefName);
  }

  /**
   * 
   * @param action is the action on the assignment (null means default action)
   * @param attributeDefNameName
   * @return if added or already there
   */
  public boolean removeAttributeByName(String action, String attributeDefNameName) {
    AttributeDefName attributeDefName = GrouperDAOFactory.getFactory()
      .getAttributeDefName().findByNameSecure(attributeDefNameName, true);
    return removeAttribute(action, attributeDefName);
  
  }
  
  /**
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public abstract String toString();

  /**
   * 
   * @param attributeDefName
   * @param assign true to assign, false to unassign
   * @param attributeAssignDelegateOptions if there are more options, null if not
   * @return the result including if added or already there
   * 
   */
  public AttributeAssignResult delegateAttribute(AttributeDefName attributeDefName, boolean assign, 
      AttributeAssignDelegateOptions attributeAssignDelegateOptions) {
  
    return delegateAttribute(null, attributeDefName, assign, attributeAssignDelegateOptions);
  }

  /**
   * @param action is the action on the assignment (null means default action)
   * @param attributeDefName
   * @param assign true to assign, false to unassign
   * @param attributeAssignDelegateOptions if there are more options, null if not
   * @return the result including if added or already there
   */
  public AttributeAssignResult delegateAttribute(String action, AttributeDefName attributeDefName, boolean assign, 
      final AttributeAssignDelegateOptions attributeAssignDelegateOptions) {
    this.assertCanDelegateAttributeDefName(action, attributeDefName);
    
    if (attributeAssignDelegateOptions != null && attributeAssignDelegateOptions.isAssignAttributeAssignDelegatable() ) {
      if (attributeAssignDelegateOptions.getAttributeAssignDelegatable() == AttributeAssignDelegatable.GRANT
          || attributeAssignDelegateOptions.getAttributeAssignDelegatable() == AttributeAssignDelegatable.TRUE) {
        this.assertCanGrantAttributeDefName(action, attributeDefName);
      }
    }
    
    if (assign) {
    
      //do the same thing that an assign would do
      final AttributeAssignResult attributeAssignResult = this.assignAttributeHelper(action, attributeDefName, false);
      
      if (attributeAssignDelegateOptions != null) {
        
        GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
          
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
            AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
            if (attributeAssignDelegateOptions.isAssignAttributeAssignDelegatable()) {
              attributeAssign.setAttributeAssignDelegatable(attributeAssignDelegateOptions.getAttributeAssignDelegatable());
            }
            if (attributeAssignDelegateOptions.isAssignDisabledDate()) {
              attributeAssign.setDisabledTime(attributeAssignDelegateOptions.getDisabledTime());
            }
            if (attributeAssignDelegateOptions.isAssignEnabledDate()) {
              attributeAssign.setDisabledTime(attributeAssignDelegateOptions.getEnabledTime());
            }
            attributeAssign.saveOrUpdate();
            return null;
          }
        });
        
      }
      
      return attributeAssignResult;
      
    }
    
    boolean changed = this.removeAttributeHelper(action, attributeDefName, false);
    return new AttributeAssignResult(changed, null);
  }

  /**
   * 
   * @param attributeDefNameId
   * @param assign true to assign, false to unassign
   * @param attributeAssignDelegateOptions if there are more options, null if not
   * @return the result including if added or already there
   */
  public AttributeAssignResult delegateAttributeById(String attributeDefNameId, boolean assign, 
      AttributeAssignDelegateOptions attributeAssignDelegateOptions) {
    return this.delegateAttributeById(null, attributeDefNameId, assign, attributeAssignDelegateOptions);
  }

  /**
   * @param action is the action on the assignment (null means default action)
   * @param attributeDefNameId
   * @param assign true to assign, false to unassign
   * @param attributeAssignDelegateOptions if there are more options, null if not
   * @return the result including if added or already there
   */
  public AttributeAssignResult delegateAttributeById(String action, String attributeDefNameId, boolean assign, 
      AttributeAssignDelegateOptions attributeAssignDelegateOptions) {
    
    AttributeDefName attributeDefName = GrouperDAOFactory.getFactory()
      .getAttributeDefName().findByIdSecure(attributeDefNameId, true);
    return delegateAttribute(action, attributeDefName, assign, attributeAssignDelegateOptions);
  
  }

  /**
   * 
   * @param attributeDefNameName
   * @param assign true to assign, false to unassign
   * @param attributeAssignDelegateOptions if there are more options, null if not
   * @return the result including if added or already there
   */
  public AttributeAssignResult delegateAttributeByName(String attributeDefNameName, boolean assign, 
      AttributeAssignDelegateOptions attributeAssignDelegateOptions) {
    return delegateAttributeByName(null, attributeDefNameName, assign, attributeAssignDelegateOptions);
  }

  /**
   * @param action is the action on the assignment (null means default action)
   * @param assign true to assign, false to unassign
   * @param attributeAssignDelegateOptions if there are more options, null if not
   * @param attributeDefNameName
   * @return the result including if added or already there
   */
  public AttributeAssignResult delegateAttributeByName(String action, String attributeDefNameName, boolean assign, 
      AttributeAssignDelegateOptions attributeAssignDelegateOptions) {
    AttributeDefName attributeDefName = GrouperDAOFactory.getFactory()
      .getAttributeDefName().findByNameSecure(attributeDefNameName, true);
    return delegateAttribute(action, attributeDefName, assign, attributeAssignDelegateOptions);
  
  }

  
}
