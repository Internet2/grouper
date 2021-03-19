package edu.internet2.middleware.grouper.attr.value;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class AttributeAssignValueSave {
  
  /**
   * attribute assign to add/assign values to
   */
  private AttributeAssign attributeAssign;
  
  /**
   * attribute assign to add/assign values to
   */
  private String attributeAssignId;
  
  /**
   * attribute assign value operation - replace is not valid
   */
  private AttributeAssignValueOperation attributeAssignValueOperation;
  
  /**
   * value to add/assign to the attribute assign 
   */
  private Object value;
  
  /**
   * set this to true to run as a root session
   */
  private boolean runAsRoot;
  
  
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
   * attribute assign to add/assign values to
   * @param attributeAssign
   * @return
   */
  public AttributeAssignValueSave assignAttributeAssign(AttributeAssign attributeAssign) {
    this.attributeAssign = attributeAssign;
    return this;
  }
  
  /**
   * attribute assign value operation - replace is not valid
   * @param attributeAssignValueOperation
   * @return
   */
  public AttributeAssignValueSave assignAttributeAssignValueOperation(AttributeAssignValueOperation attributeAssignValueOperation) {
    this.attributeAssignValueOperation = attributeAssignValueOperation;
    return this;
  }
  
  /**
   * attribute assign to add/assign values to
   * @param attributeAssign
   * @return
   */
  public AttributeAssignValueSave assignAttributeAssignId(String attributeAssignId) {
    this.attributeAssignId = attributeAssignId;
    return this;
  }
  
  /**
   * set this to true to run as a root session
   * @param runAsRoot
   * @return
   */
  public AttributeAssignValueSave assignRunAsRoot(boolean runAsRoot) {
    this.runAsRoot = runAsRoot;
    return this;
  }
  
  
  /**
   * value to add/assign to the attribute assign
   * @param attributeAssign
   * @return
   */
  public AttributeAssignValueSave assignValue(Object value) {
    this.value = value;
    return this;
  }
  
  /**
   * save attribute assign value
   * @return
   */
  public AttributeAssignValueResult save() {

    return (AttributeAssignValueResult) GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
      
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
            
            if (value == null) {
              GrouperUtil.assertion(value != null,  "value cannot be null");
            }
            
            if (attributeAssignValueOperation == null) {
              if (attributeAssign.getAttributeDef().isMultiValued()) { 
                attributeAssignValueOperation = AttributeAssignValueOperation.add_value;
              } else {
                attributeAssignValueOperation = AttributeAssignValueOperation.assign_value;
              }
            }
            
            if (attributeAssignValueOperation == AttributeAssignValueOperation.remove_value) {
               AttributeAssignValueResult attributeAssignValueResult = attributeAssign.getValueDelegate().deleteValueObject(value);
               saveResultType = attributeAssignValueResult.isDeleted() ? SaveResultType.DELETE : SaveResultType.NO_CHANGE;
               return attributeAssignValueResult;
            } else if (attributeAssignValueOperation == AttributeAssignValueOperation.add_value) {
              
              if (attributeAssign.getAttributeDef().isMultiValued()) {
                AttributeAssignValueResult attributeAssignValueResult = attributeAssign.getValueDelegate().addValueObject(value);
                saveResultType = attributeAssignValueResult.isChanged() ? SaveResultType.INSERT : SaveResultType.NO_CHANGE;
                return attributeAssignValueResult;
              } else {
                throw new RuntimeException("'add_value' attributeAssignValueOperation is valid only for attribute defs that are multivalued.");
              } 
             
            } else if (attributeAssignValueOperation == AttributeAssignValueOperation.assign_value) {
              
              if (attributeAssign.getAttributeDef().isMultiValued()) {
                throw new RuntimeException("'assign_value' attributeAssignValueOperation is valid only for attribute defs that are not multivalued.");
              } else {
                AttributeAssignValueResult attributeAssignValueResult = attributeAssign.getValueDelegate().assignValueObject(value);
                saveResultType = attributeAssignValueResult.isChanged() ? SaveResultType.INSERT : SaveResultType.NO_CHANGE;
                return attributeAssignValueResult;
              } 
             
            } else {
              throw new RuntimeException("attributeAssignValueOperation is not valid.");
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
