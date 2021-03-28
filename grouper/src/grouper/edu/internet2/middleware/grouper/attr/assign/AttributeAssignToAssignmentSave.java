/**
 * 
 */
package edu.internet2.middleware.grouper.attr.assign;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * <p>Use this class to add/edit/delete attribute def names on attribute assigns.</p>
 * <p>Sample call
 * 
 * <blockquote>
 * <pre>
 * AttributeAssignToAssignmentSave attributeAssignToAssignmentSave = new AttributeAssignToAssignmentSave();
 * AttributeAssignResult attributeAssignResult = attributeAssignToAssignmentSave.assignAttributeAssign(attributeAssign).assignAttributeDefName(attributeDefName).save();
 * System.out.println(attributeAssignToAssignmentSave.getSaveResultType()); // DELETE, INSERT, NO_CHANGE, or UPDATE
 * </pre>
 * </blockquote>
 * 
 * </p>
 * 
 * <p> Sample call to remove attribute def name from an attribute assign
 * <blockquote>
 * <pre>
 * AttributeAssignToAssignmentSave attributeAssignToAssignmentSave = new AttributeAssignToAssignmentSave().assignAttributeAssign(attributeAssign)
 *     .assignAttributeDefName(attributeDefName)
 *     .assignAttributeAssignOperation(AttributeAssignOperation.remove_attr)
 *     .save();
 * 
 * </pre>
 * </blockquote>
 * </p>
 *
 */
public class AttributeAssignToAssignmentSave {
  
  /**
   * attributeDefName to assign/add to the attributeAssign
   */
  private AttributeDefName attributeDefName;
  
  /**
   * attributeDefName to assign/add to the attributeAssign
   */
  private String attributeDefNameId;
  
  /**
   * attributeDefName to assign/add to the attributeAssign
   */
  private String attributeDefNameName;
  
  /**
   * attribute assign to which attribute def name is to be added/assigned
   */
  private AttributeAssign attributeAssign;
  
  /**
   * attribute assign to which attribute def name is to be added/assigned
   */
  private String attributeAssignId;
  
  /**
   * set this to true to run as a root session
   */
  private boolean runAsRoot;
  
  /**
   * attribute assign operation
   */
  private AttributeAssignOperation attributeAssignOperation;
  
  
  /** save type after the save */
  private SaveResultType saveResultType;
  
  /**
   * save type after the save
   * @return
   */
  public SaveResultType getSaveResultType() {
    return saveResultType;
  }
  
  /**
   * attributeDefName to assign/add to the attributeAssign
   * @param attributeDefName
   * @return
   */
  public AttributeAssignToAssignmentSave assignAttributeDefName(AttributeDefName attributeDefName) {
    this.attributeDefName = attributeDefName;
    return this;
  }
  
  /**
   * attributeDefName to assign/add to the attributeAssign
   * @param attributeDefNameId
   * @return
   */
  public AttributeAssignToAssignmentSave assignAttributeDefNameId(String attributeDefNameId) {
    this.attributeDefNameId = attributeDefNameId;
    return this;
  }
  
  /**
   * attributeDefName to assign/add to the attributeAssign
   * @param attributeDefNameName
   * @return
   */
  public AttributeAssignToAssignmentSave assignAttributeDefNameName(String attributeDefNameName) {
    this.attributeDefNameName = attributeDefNameName;
    return this;
  }
  
  
  /**
   * attribute assign to which attribute def name is to be added/assigned
   * @param attributeAssign
   * @return
   */
  public AttributeAssignToAssignmentSave assignAttributeAssign(AttributeAssign attributeAssign) {
    this.attributeAssign = attributeAssign;
    return this;
  }
  
  /**
   * attribute assign to which attribute def name is to be added/assigned
   * @param attributeAssignId
   * @return
   */
  public AttributeAssignToAssignmentSave assignAttributeAssignId(String attributeAssignId) {
    this.attributeAssignId = attributeAssignId;
    return this;
  }
  
  /**
   * set this to true to run as a root session
   * @param runAsRoot
   * @return
   */
  public AttributeAssignToAssignmentSave assignRunAsRoot(boolean runAsRoot) {
    this.runAsRoot = runAsRoot;
    return this;
  }
  
  /**
   * attribute assign operation
   * @param attributeAssignOperation
   * @return
   */
  public AttributeAssignToAssignmentSave assignAttributeAssignOperation(AttributeAssignOperation attributeAssignOperation) {
    this.attributeAssignOperation = attributeAssignOperation;
    return this;
  }
  
  /**
   * save attribute def name to attribute assign
   * @return
   */
  public AttributeAssignResult save() {
    
    return (AttributeAssignResult) GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
      
      @Override
      public Object callback(GrouperTransaction grouperTransaction) throws GrouperDAOException {
        
        grouperTransaction.setCachingEnabled(false);
        
        GrouperSessionHandler grouperSessionHandler = new GrouperSessionHandler() {
          
          @Override
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
            
            if (attributeAssign == null && StringUtils.isNotBlank(attributeAssignId)) {              
              attributeAssign = AttributeAssignFinder.findById(attributeAssignId, false);
            }
            
            GrouperUtil.assertion(attributeAssign != null,  "AttributeAssign not found");
            
            if (attributeDefName == null && StringUtils.isNotBlank(attributeDefNameId)) {              
              attributeDefName = AttributeDefNameFinder.findById(attributeDefNameId, false);
            }
            
            if (attributeDefName == null && StringUtils.isNotBlank(attributeDefNameName)) {              
              attributeDefName = AttributeDefNameFinder.findByName(attributeDefNameName, false);
            }
            
            GrouperUtil.assertion(attributeDefName != null,  "AttributeDefName not found");
            
            if (attributeAssignOperation == null) {
              if (attributeDefName.getAttributeDef().isMultiAssignable()) { 
                attributeAssignOperation = AttributeAssignOperation.add_attr;
              } else {
                attributeAssignOperation = AttributeAssignOperation.assign_attr;
              }
            }
            
            if (attributeAssignOperation == AttributeAssignOperation.remove_attr) {
              AttributeAssignResult attributeAssignResult = attributeAssign.getAttributeDelegate().removeAttribute(attributeDefName);
               saveResultType = attributeAssignResult.isChanged()? SaveResultType.DELETE : SaveResultType.NO_CHANGE;
               return attributeAssignResult;
            } else if (attributeAssignOperation == AttributeAssignOperation.add_attr) {
              
              if (attributeAssign.getAttributeDef().isMultiAssignable()) {
                AttributeAssignResult attributeAssignResult = attributeAssign.getAttributeDelegate().addAttribute(attributeDefName);
                saveResultType = attributeAssignResult.isChanged() ? SaveResultType.INSERT : SaveResultType.NO_CHANGE;
                return attributeAssignResult;
              } else {
                throw new RuntimeException("'add_attr' attributeAssignOperation is valid only for attribute defs that are multi-assignable.");
              } 
             
            } else if (attributeAssignOperation == AttributeAssignOperation.assign_attr) {
              
              if (attributeAssign.getAttributeDef().isMultiAssignable()) {
                throw new RuntimeException("'assign_attr' attributeAssignOperation is valid only for attribute defs that are not multi-assignable.");
              } else {
                AttributeAssignResult attributeAssignResult = attributeAssign.getAttributeDelegate().assignAttribute(attributeDefName);
                saveResultType = attributeAssignResult.isChanged() ? SaveResultType.INSERT : SaveResultType.NO_CHANGE;
                return attributeAssignResult;
              }
             
            } else {
              throw new RuntimeException("attributeAssignOperation is not valid.");
            }
            
          }
        };
        
        if (runAsRoot) {
          return GrouperSession.internal_callbackRootGrouperSession(grouperSessionHandler);
        }
        
        return GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession(), grouperSessionHandler);
        
      }
    });
    
  }

}
