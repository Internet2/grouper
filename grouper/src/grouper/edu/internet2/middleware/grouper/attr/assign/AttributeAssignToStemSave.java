package edu.internet2.middleware.grouper.attr.assign;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * <p>Use this class to add/edit/delete attribute def names on folders.</p>
 * <p>Sample call
 * 
 * <blockquote>
 * <pre>
 * AttributeAssignToStemSave attributeAssignToStemSave = new AttributeAssignToStemSave().assignAttributeDefName(attributeDefName).assignStem(stem);
 * AttributeAssign attributeAssign = attributeAssignToStemSave.save();
 * System.out.println(attributeAssignToStemSave.getSaveResultType()); // DELETE, INSERT, NO_CHANGE, or UPDATE
 * </pre>
 * </blockquote>
 * 
 * </p>
 * 
 * <p> Sample call to remove attribute def name from a folder
 * <blockquote>
 * <pre>
 * new AttributeAssignToStemSave().assignAttributeDefName(attributeDefName).assignStem(stem).assignSaveMode(SaveMode.DELETE).save();
 * </pre>
 * </blockquote>
 * </p>
 *
 */
public class AttributeAssignToStemSave {
  
  /**
   * set this to true to run as a root session
   */
  private boolean runAsRoot;
  
  /**
   * set this to true to run as a root session
   * @param runAsRoot
   * @return
   */
  public AttributeAssignToStemSave assignRunAsRoot(boolean runAsRoot) {
    this.runAsRoot = runAsRoot;
    return this;
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    GrouperStartup.startup();
    GrouperSession grouperSession = GrouperSession.startRootSession();

    new StemSave().assignName("test").save();
    
    AttributeDef attributeDef = new AttributeDefSave().assignName("test:def").assignToStem(true).assignValueType(AttributeDefValueType.marker).save();
    AttributeDefName attributeDefName = new AttributeDefNameSave(attributeDef).assignName("test:defName").save();
    
//    attestationStemSave = new AttestationStemSave().assignStemName("test");
//    attestationStemSave.save();

//    AttributeAssignToStemSave attributeAssignToStemSave = new AttributeAssignToStemSave().assignStemName("test").assignNameOrAttributeDefName("test:defName");
//    attributeAssignToStemSave.save();

    AttributeAssignToStemSave attributeAssignToStemSave = new AttributeAssignToStemSave().assignStemName("test").assignNameOfAttributeDefName("test:defName").assignSaveMode(SaveMode.DELETE);
  attributeAssignToStemSave.save();
    
    System.out.println(attributeAssignToStemSave.getSaveResultType());
    
    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * attributeDefName
   */
  private AttributeDefName attributeDefName;

  /**
   * attribute def name to be added/updated/deleted from folder
   * @param theAttributeDefName
   * @return this for chaining
   */
  public AttributeAssignToStemSave assignAttributeDefName(AttributeDefName theAttributeDefName) {
    this.attributeDefName = theAttributeDefName;
    return this;
  }

  private String nameOfAttributeDefName;

  /**
   * attribute def name to be added/updated/deleted from folder
   * @param theNameOfAttributeDefName
   * @return
   */
  public AttributeAssignToStemSave assignNameOfAttributeDefName(String theNameOfAttributeDefName) {
    this.nameOfAttributeDefName = theNameOfAttributeDefName;
    return this;
  }
  
  /**
   * stem
   */
  private Stem stem;
  
  /**
   * stem id to add to, mutually exclusive with stem name
   */
  private String stemId;
  /**
   * stem name to add to, mutually exclusive with stem id
   */
  private String stemName;

  /** save mode */
  private SaveMode saveMode;
  
  /** save type after the save */
  private SaveResultType saveResultType = null;

  public AttributeAssignToStemSave() {

  }

  /**
   * assign a stem
   * @param theStem
   * @return this for chaining
   */
  public AttributeAssignToStemSave assignStem(Stem theStem) {
    this.stem = theStem;
    return this;
  }

  /**
   * stem id to add to, mutually exclusive with stem name and stem
   * @param theStemId
   * @return this for chaining
   */
  public AttributeAssignToStemSave assignStemId(String theStemId) {
    this.stemId = theStemId;
    return this;
  }

  /**
   * stem name to add to, mutually exclusive with stem id and stem
   * @param theStemName
   * @return this for chaining
   */
  public AttributeAssignToStemSave assignStemName(String theStemName) {
    this.stemName = theStemName;
    return this;
  }

  /**
   * asssign save mode
   * @param theSaveMode
   * @return this for chaining
   */
  public AttributeAssignToStemSave assignSaveMode(SaveMode theSaveMode) {
    this.saveMode = theSaveMode;
    return this;
  }

  /**
   * get the save type
   * @return save type
   */
  public SaveResultType getSaveResultType() {
    return this.saveResultType;
  }

  /**
   * <pre>
   * add or edit or delete an attribute def name from folder
   * </pre>
   * @return the attribute assign that was updated or created or deleted
   */
  public AttributeAssign save() throws InsufficientPrivilegeException, GroupNotFoundException {

    //default to insert or update
    saveMode = (SaveMode)ObjectUtils.defaultIfNull(saveMode, SaveMode.INSERT_OR_UPDATE);
    
    AttributeAssign attributeAssign = (AttributeAssign)GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
    
        public Object callback(GrouperTransaction grouperTransaction)
            throws GrouperDAOException {
          
          GrouperSessionHandler grouperSessionHandler = new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
             
              grouperTransaction.setCachingEnabled(false);
              
              if (stem == null && !StringUtils.isBlank(AttributeAssignToStemSave.this.stemId)) {
                stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), AttributeAssignToStemSave.this.stemId, false, new QueryOptions().secondLevelCache(false));
              } 
              if (stem == null && !StringUtils.isBlank(AttributeAssignToStemSave.this.stemName)) {
                stem = StemFinder.findByName(GrouperSession.staticGrouperSession(), AttributeAssignToStemSave.this.stemName, false, new QueryOptions().secondLevelCache(false));
              }
              GrouperUtil.assertion(stem!=null,  "Stem not found");

              
              if (attributeDefName == null && !StringUtils.isBlank(AttributeAssignToStemSave.this.nameOfAttributeDefName)) {
                attributeDefName = AttributeDefNameFinder.findByName(AttributeAssignToStemSave.this.nameOfAttributeDefName, false);
              } 
              GrouperUtil.assertion(attributeDefName!=null,  "AttributeDefName not found");

              
              // handle deletes
              if (saveMode == SaveMode.DELETE) {
                
                AttributeAssignResult attributeAssignResult = stem.getAttributeDelegate().removeAttribute(attributeDefName);
                boolean changed = attributeAssignResult.isChanged();
                
                AttributeAssignToStemSave.this.saveResultType = changed ? SaveResultType.DELETE : SaveResultType.NO_CHANGE;

                return attributeAssignResult.getAttributeAssign();
              }
              
              AttributeAssign attributeAssign = 
                  stem.getAttributeDelegate().retrieveAssignment(null, attributeDefName, true, false);
              
              AttributeAssignResult attributeAssignResult = null;
              if (attributeDefName.getAttributeDef().isMultiAssignable()) {            
                attributeAssignResult = stem.getAttributeDelegate().addAttribute(attributeDefName);
              } else {
                attributeAssignResult = stem.getAttributeDelegate().assignAttribute(attributeDefName);
              }
              
              boolean changed = attributeAssignResult.isChanged();
              
              if (saveMode == SaveMode.INSERT && !changed) {
                throw new RuntimeException("Inserting attribute to stem but it already exists!");
              }
              if (saveMode == SaveMode.UPDATE && attributeAssign == null) {
                throw new RuntimeException("Updating membership but it doesnt exist!");
              }
              
              if (attributeAssign == null) {
                AttributeAssignToStemSave.this.saveResultType = SaveResultType.INSERT;
              } else {
                AttributeAssignToStemSave.this.saveResultType = attributeAssignResult.isChanged() ? SaveResultType.UPDATE : SaveResultType.NO_CHANGE;
              }
              
              
              return attributeAssignResult.getAttributeAssign();
            }
          };
          
          if (runAsRoot) {
            return (AttributeAssign) GrouperSession.internal_callbackRootGrouperSession(grouperSessionHandler);
          }
          return (AttributeAssign) GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession(), grouperSessionHandler);
          
        }
    });
    
    
    return attributeAssign;
    
  }

  
}
