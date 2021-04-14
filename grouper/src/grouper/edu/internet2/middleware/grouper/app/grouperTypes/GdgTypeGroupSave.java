package edu.internet2.middleware.grouper.app.grouperTypes;

import java.util.List;

import org.apache.commons.lang.ObjectUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectUtils;

/**
 * <p>Use this class to add/edit/delete object types on groups</p>
 * <p>Sample call
 * 
 * <blockquote>
 * <pre>
 * GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave();
 * GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue = gdgTypeGroupSave
        .assignGroup(group)
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
 * System.out.println(gdgTypeGroupSave.getSaveResultType()); // INSERT, DELETE, NO_CHANGE, or UPDATE
 * </pre>
 * </blockquote>
 * 
 * </p>
 * 
 * <p> Sample call to delete an object type from a group
 * <blockquote>
 * <pre>
 * GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave();
 * gdgTypeGroupSave
        .assignGroup(group)
        .assignType("ref")
        .assignSaveMode(SaveMode.DELETE)
        .save();
 * </pre>
 * </blockquote>
 * </p>
 * <p> Sample call to update only single attribute
 * <blockquote>
 * <pre>
 * GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave();
 * gdgTypeGroupSave
        .assignGroup(group)
        .assignType("ref")
        .assignReplaceAllSettings(false)
        .assignDataOwner("do1")
        .save();
 * </pre>
 * </blockquote>
 * </p>
 */
public class GdgTypeGroupSave {
  
  /**
   * group on which object types attributes need to be saved
   */
  private Group group;
  
  /**
   * group id on which object types attributes need to be saved
   */
  private String groupId;
  
  /**
   * group name on which object types attributes need to be saved
   */
  private String groupName;
  
  /**
   * type e.g. ref, basis, app
   */
  private String type;
  
  /**
   * data owner to assign
   */
  private String dataOwner;
  
  private boolean dataOwnerAssigned;
  
  /**
   * member description to assign
   */
  private String memberDescription;
  
  private boolean memberDescriptionAssigned;
  
  /**
   * service name to assign
   */
  private String serviceName;
  
  private boolean serviceNameAssigned;
  
  /**
   * set this to true to run as a root session
   */
  private boolean runAsRoot;
  
  /**
   * replace all existing settings. defaults to true.
   */
  private boolean replaceAllSettings = true;
  
  /** save mode */
  private SaveMode saveMode;
  
  /**
   * group on which object types attributes need to be saved
   * @param group
   * @return
   */
  public GdgTypeGroupSave assignGroup(Group group) {
    this.group = group;
    return this;
  }
  
  /**
   * group id on which object types attributes need to be saved
   * @param groupId
   * @return
   */
  public GdgTypeGroupSave assignGroupId(String groupId) {
    this.groupId = groupId;
    return this;
  } 
  
  /**
   * group name on which object types attributes need to be saved
   * @param groupName
   * @return
   */
  public GdgTypeGroupSave assignGroupName(String groupName) {
    this.groupName = groupName;
    return this;
  }
  
  /**
   * type e.g. ref, basis, app
   * @param type
   * @return
   */
  public GdgTypeGroupSave assignType(String type) {
    this.type = type;
    return this;
  }
  
  /**
   * data owner to assign
   * @param dataOwner
   * @return
   */
  public GdgTypeGroupSave assignDataOwner(String dataOwner) {
    this.dataOwner = dataOwner;
    this.dataOwnerAssigned = true;
    return this;
  }
  
  /**
   * member description to assign
   * @param memberDescription
   * @return
   */
  public GdgTypeGroupSave assignMemberDescription(String memberDescription) {
    this.memberDescription = memberDescription;
    this.memberDescriptionAssigned = true;
    return this;
  }
  
  /**
   * service name to assign
   * @param serviceName
   * @return
   */
  public GdgTypeGroupSave assignServiceName(String serviceName) {
    this.serviceName = serviceName;
    this.serviceNameAssigned = true;
    return this;
  }
  
  /**
   * set this to true to run as a root session
   * @param runAsRoot
   * @return
   */
  public GdgTypeGroupSave assignRunAsRoot(boolean runAsRoot) {
    this.runAsRoot = runAsRoot;
    return this;
  }
  
  /**
   * replace all existing settings. defaults to true.
   * @param replaceAllSettings
   * @return
   */
  public GdgTypeGroupSave assignReplaceAllSettings(boolean replaceAllSettings) {
    this.replaceAllSettings = replaceAllSettings;
    return this;
  }
  
  /**
   * assign save mode
   * @param saveMode
   * @return
   */
  public GdgTypeGroupSave assignSaveMode(SaveMode saveMode) {
    this.saveMode = saveMode;
    return this;
  }
  
  
  private SaveResultType saveResultType;
  
  /**
   * get save result type after the save call
   * @return
   */
  public SaveResultType getSaveResultType() {
    return saveResultType;
  }
  
  /**
   * add/edit/delete object type attributes from a group
   * @return bean containing the current attribute values - can be null
   */
  public GrouperObjectTypesAttributeValue save() {

    //default to insert or update
    saveMode = (SaveMode)ObjectUtils.defaultIfNull(saveMode, SaveMode.INSERT_OR_UPDATE);
    
    GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue = (GrouperObjectTypesAttributeValue) GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
      
      public Object callback(GrouperTransaction grouperTransaction) throws GrouperDAOException {
        
        grouperTransaction.setCachingEnabled(false);
        
        final Subject SUBJECT_IN_SESSION = GrouperSession.staticGrouperSession().getSubject();
        
        return GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
          
          @Override
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
            
            if (StringUtils.isBlank(type) || !GrouperObjectTypesSettings.getObjectTypeNames().contains(type)) {
              throw new RuntimeException("type is required and must be one of the valid inputs ["+GrouperUtil.collectionToString(GrouperObjectTypesSettings.getObjectTypeNames()) + "]");
            }
            
            if (group == null && !StringUtils.isBlank(groupId)) {
              group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupId, false, new QueryOptions().secondLevelCache(false));
            }
            
            if (group == null && !StringUtils.isBlank(groupName)) {
              group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupName, false, new QueryOptions().secondLevelCache(false));
            }
            
            GrouperUtil.assertion(group != null,  "Group not found");
            
            if (!runAsRoot) {
              if (!group.canHavePrivilege(SUBJECT_IN_SESSION, AccessPrivilege.ADMIN.getName(), false)) {
                throw new RuntimeException("Subject '" + SubjectUtils.subjectToString(SUBJECT_IN_SESSION) 
                  + "' cannot ADMIN group '" + group.getName() + "'");
              }
            }
            
            List<String> dataOwnerMemberDescriptionRequiringObjectTypeNames = GrouperObjectTypesSettings.getDataOwnerMemberDescriptionRequiringObjectTypeNames();
            
            if (!dataOwnerMemberDescriptionRequiringObjectTypeNames.contains(type) && (dataOwner != null || memberDescription != null) ) {
              throw new RuntimeException("For type '"+type + "' dataOwner and memberDescription cannot be assigned");
            }
            
            List<String> serviceRequiringObjectTypeNames = GrouperObjectTypesSettings.getServiceRequiringObjectTypeNames();
            
            if (!serviceRequiringObjectTypeNames.contains(type) && serviceName != null) {
              throw new RuntimeException("For type '"+type + "' serviceName cannot be assigned");
            }
            
            if (saveMode == SaveMode.DELETE) {
              
              GrouperObjectTypesAttributeValue attributeValueBefore = GrouperObjectTypesConfiguration.getGrouperObjectTypesAttributeValue(group, type);
              
              if ( (attributeValueBefore == null) || (attributeValueBefore != null && !attributeValueBefore.isDirectAssignment()) ) {
                
                saveResultType = SaveResultType.NO_CHANGE;
                return null;
                
              } else {
                GrouperObjectTypesConfiguration.deleteTypeAttribute(group, type);
                saveResultType = SaveResultType.DELETE;
                return GrouperObjectTypesConfiguration.getGrouperObjectTypesAttributeValue(group, type);
              }
              
            }

            GrouperObjectTypesAttributeValue existingValues = GrouperObjectTypesConfiguration.getGrouperObjectTypesAttributeValue(group, type);
            
            boolean existingDirectAssignment = existingValues != null && existingValues.isDirectAssignment();
            
            if (saveMode == SaveMode.UPDATE && !existingDirectAssignment) {
              throw new RuntimeException("Updating GDG type group settings but they doesnt exist!");
            }
            
            if (saveMode == SaveMode.INSERT && existingDirectAssignment) {
              throw new RuntimeException("Inserting GDG type group settings but they already exist!");
            }
            
            if (existingValues == null) {
              
              GrouperObjectTypesAttributeValue attributeValue = new GrouperObjectTypesAttributeValue();
              attributeValue.setDirectAssignment(true);
              attributeValue.setObjectTypeDataOwner(dataOwner);
              attributeValue.setObjectTypeMemberDescription(memberDescription);
              attributeValue.setObjectTypeName(type);
              attributeValue.setObjectTypeServiceName(serviceName);
              GrouperObjectTypesConfiguration.saveOrUpdateTypeAttributes(attributeValue, group);
              saveResultType = SaveResultType.INSERT;
              
            } else {
              
              saveResultType = SaveResultType.NO_CHANGE;
              
              GrouperObjectTypesAttributeValue attributeValue = new GrouperObjectTypesAttributeValue();
              attributeValue.setDirectAssignment(true);
              attributeValue.setObjectTypeName(type);
              
              if (replaceAllSettings || dataOwnerAssigned) {
                
                if (!StringUtils.equals(dataOwner, existingValues.getObjectTypeDataOwner())) {
                  attributeValue.setObjectTypeDataOwner(dataOwner);
                  saveResultType = SaveResultType.UPDATE;
                } else {
                  attributeValue.setObjectTypeDataOwner(existingValues.getObjectTypeDataOwner());
                }
                
              } else {
                attributeValue.setObjectTypeDataOwner(existingValues.getObjectTypeDataOwner());
              }
              
              if (replaceAllSettings || memberDescriptionAssigned) {
                
                if (!StringUtils.equals(memberDescription, existingValues.getObjectTypeMemberDescription())) {
                  attributeValue.setObjectTypeMemberDescription(memberDescription);
                  saveResultType = SaveResultType.UPDATE;
                } else {
                  attributeValue.setObjectTypeMemberDescription(existingValues.getObjectTypeMemberDescription());
                }
                
              } else {
                attributeValue.setObjectTypeMemberDescription(existingValues.getObjectTypeMemberDescription());
              }
              
              if (replaceAllSettings || serviceNameAssigned) {
                
                if (!StringUtils.equals(serviceName, existingValues.getObjectTypeServiceName())) {
                  attributeValue.setObjectTypeServiceName(serviceName);
                  saveResultType = SaveResultType.UPDATE;
                } else {
                  attributeValue.setObjectTypeServiceName(existingValues.getObjectTypeServiceName());
                }
                attributeValue.setObjectTypeServiceName(serviceName);
              } else {
                attributeValue.setObjectTypeServiceName(existingValues.getObjectTypeServiceName());
              }
              
              if (saveResultType == SaveResultType.UPDATE) {           
                GrouperObjectTypesConfiguration.saveOrUpdateTypeAttributes(attributeValue, group);
              }
              
            }
            
            return GrouperObjectTypesConfiguration.getGrouperObjectTypesAttributeValue(group, type);
          }
        });
        
      };
      
    });
    
    return grouperObjectTypesAttributeValue;
    
  }
  
}
