package edu.internet2.middleware.grouper.app.provisioning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectUtils;

/**
 * <p>Use this class to add/edit/delete provisioning attributes on groups</p>
 * <p>Sample call
 * 
 * <blockquote>
 * <pre>
 * ProvisionableGroupSave provisionableGroupSave = new ProvisionableGroupSave();
 * GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = provisionableGroupSave.assignTargetName("ldapProvTest").assignMetadataString("md_testInput", "testValue").assignGroup(group).save();
 * System.out.println(provisionableGroupSave.getSaveResultType()); // INSERT, DELETE, NO_CHANGE, or UPDATE
 * </pre>
 * </blockquote>
 * 
 * </p>
 * 
 * <p> Sample call to delete provisioning attributes from a group
 * <blockquote>
 * <pre>
 * ProvisionableGroupSave provisionableGroupSave = new ProvisionableGroupSave();
 * provisionableGroupSave.assignTargetName("ldapProvTest")
        .assignSaveMode(SaveMode.DELETE).assignGroupName(group.getName()).save();
 * </pre>
 * </blockquote>
 * </p>
 * <p> Sample call to update only single attribute
 * <blockquote>
 * <pre>
 * ProvisionableGroupSave provisionableGroupSave = new ProvisionableGroupSave();
 * provisionableGroupSave.assignTargetName("ldapProvTest")
        .assignProvision(true)
        .assignReplaceAllSettings(false)
        .assignGroup(group).save();
 * </pre>
 * </blockquote>
 * </p>
 */
public class ProvisionableGroupSave {
  
  private Group group;
  
  private String groupId;
  
  private String groupName;
  
  private boolean runAsRoot;
  
  private boolean replaceAllSettings = true;
  
  private Map<String, Object> metadataMap = new HashMap<String, Object>();
  
  private String targetName;
  
  private boolean provision = true;
  
  private boolean provisionAssigned;
  
  /** save mode */
  private SaveMode saveMode;
  
  private SaveResultType saveResultType;
  
  /**
   * assign provision
   * @param provision
   * @return
   */
  public ProvisionableGroupSave assignProvision(boolean provision) {
    this.provision = provision;
    this.provisionAssigned = true;
    return this;
  }
  
  /**
   * assign provisioning target name
   * @param targetName
   * @return
   */
  public ProvisionableGroupSave assignTargetName(String targetName) {
    this.targetName = targetName;
    return this;
  }
  
  /**
   * get save result type after the save call
   * @return
   */
  public SaveResultType getSaveResultType() {
    return saveResultType;
  }
  
  /**
   * set this to true to run as a root session
   * @param runAsRoot
   * @return
   */
  public ProvisionableGroupSave assignRunAsRoot(boolean runAsRoot) {
    this.runAsRoot = runAsRoot;
    return this;
  }
  
  /**
   * replace all existing settings. defaults to true.
   * @return this for chaining
   */
  public ProvisionableGroupSave assignReplaceAllSettings(boolean replaceAllSettings) {
    this.replaceAllSettings = replaceAllSettings;
    return this;
  }
  
  /**
   * assign save mode
   * @param saveMode
   * @return
   */
  public ProvisionableGroupSave assignSaveMode(SaveMode saveMode) {
    this.saveMode = saveMode;
    return this;
  }
  
  /**
   * assign group on which attributes need to be stored
   * @param stem
   * @return
   */
  public ProvisionableGroupSave assignGroup(Group group) {
    this.group = group;
    return this;
  }
  
  /**
   * assign group id on which attributes need to be stored
   * @param stem
   * @return
   */
  public ProvisionableGroupSave assignGroupId(String groupId) {
    this.groupId = groupId;
    return this;
  }
  
  /**
   * assign group name on which attributes need to be stored
   * @param stem
   * @return
   */
  public ProvisionableGroupSave assignGroupName(String groupName) {
    this.groupName = groupName;
    return this;
  }
  
  /**
   * assign string type metadata
   * @param name
   * @param value
   * @return
   */
  public ProvisionableGroupSave assignMetadataString(String name, String value) {
    metadataMap.put(name, value);
    return this;
  }
  
  /**
   * assign boolean type metadata
   * @param name
   * @param value
   * @return
   */
  public ProvisionableGroupSave assignMetadataBoolean(String name, Boolean value) {
    metadataMap.put(name, value);
    return this;
  }
  
  /**
   * assign integer type metadata
   * @param name
   * @param value
   * @return
   */
  public ProvisionableGroupSave assignMetadataInteger(String name, Integer value) {
    metadataMap.put(name, value);
    return this;
  }
  
  /**
   * save attributes in the database
   * @return a bean containing the current attribute values
   */
  public GrouperProvisioningAttributeValue save() {
    
    //default to insert or update
    saveMode = (SaveMode)ObjectUtils.defaultIfNull(saveMode, SaveMode.INSERT_OR_UPDATE);
    
    GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = (GrouperProvisioningAttributeValue) GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
      
      @Override
      public Object callback(GrouperTransaction grouperTransaction) throws GrouperDAOException {
        
        grouperTransaction.setCachingEnabled(false);
        
        final Subject SUBJECT_IN_SESSION = GrouperSession.staticGrouperSession().getSubject();
        
        return GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
          
          @Override
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
            
            if (StringUtils.isBlank(targetName) || !GrouperProvisioningSettings.getTargets(true).containsKey(targetName)) {
              throw new RuntimeException("target is required and must be one of the valid targets ["+GrouperUtil.collectionToString(GrouperProvisioningSettings.getTargets(true).keySet()) + "]");
            }
            
            if (group == null && !StringUtils.isBlank(groupId)) {
              group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupId, false, new QueryOptions().secondLevelCache(false));
            }
            
            if (group == null && !StringUtils.isBlank(groupName)) {
              group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupName, false, new QueryOptions().secondLevelCache(false));
            }
            
            GrouperUtil.assertion(group != null,  "Group not found");
            
            GrouperProvisioningTarget provisioningTarget = GrouperProvisioningSettings.getTargets(true).get(targetName);
            
            if (!runAsRoot) {
              
              if (!PrivilegeHelper.isWheelOrRoot(SUBJECT_IN_SESSION)) {
                throw new RuntimeException("Subject '" + SubjectUtils.subjectToString(SUBJECT_IN_SESSION)+ "' is not wheel or root user.");
              }
              
              if (!GrouperProvisioningService.isTargetEditable(provisioningTarget, SUBJECT_IN_SESSION, group)) {
                throw new RuntimeException("Not allowed to edit target.");
              }
              
            }
            
            if (!GrouperProvisioningService.isTargetEditable(provisioningTarget, grouperSession.getSubject(), group)) {
              throw new RuntimeException("Not allowed to edit target.");
            }
            
            if (saveMode == SaveMode.DELETE) {
              
              
              GrouperProvisioningAttributeValue provisioningAttributeValueBefore = GrouperProvisioningService.getProvisioningAttributeValue(group, targetName);
              
              if (provisioningAttributeValueBefore != null) {            
                GrouperProvisioningService.deleteAttributeAssign(group, targetName);
                saveResultType = SaveResultType.DELETE;
                return GrouperProvisioningService.getProvisioningAttributeValue(group, targetName);
              } else {
                saveResultType = SaveResultType.NO_CHANGE;
                return null;
              }
              
            }
            
            GrouperProvisioningAttributeValue existingValues = GrouperProvisioningService.getProvisioningAttributeValue(group, targetName);
            
            boolean existingDirectAssignment = existingValues != null && existingValues.isDirectAssignment();
            
            if (saveMode == SaveMode.UPDATE && !existingDirectAssignment) {
              throw new RuntimeException("Updating provisioning settings on a group but they do not exist.");
            }
            
            if (saveMode == SaveMode.INSERT && existingDirectAssignment) {
              throw new RuntimeException("Inserting provisioning settings on a group but they already exist.");
            }
            
            String doProvision = provision ? targetName : null;
            if (existingValues == null) {
              
              final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
              attributeValue.setDirectAssignment(true);
              attributeValue.setDoProvision(doProvision);
              attributeValue.setTargetName(targetName);
              
              Map<String, Object> metadataNameValues = populateMetadata(metadataMap);
             
              attributeValue.setMetadataNameValues(metadataNameValues);
              
              GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, group);
              
              saveResultType = SaveResultType.INSERT;
              
            } else {
              
              saveResultType = SaveResultType.NO_CHANGE;
              
              final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
              attributeValue.setDirectAssignment(true);
              
              if (replaceAllSettings || provisionAssigned) {
                
                if (!StringUtils.equals(existingValues.getDoProvision(), doProvision)) {
                  attributeValue.setDoProvision(doProvision);
                  saveResultType = SaveResultType.UPDATE;
                } else {
                  attributeValue.setDoProvision(existingValues.getDoProvision());
                }
                
              } else {
                attributeValue.setDoProvision(existingValues.getDoProvision());
              }
              
              attributeValue.setTargetName(targetName);
              
              Map<String, Object> mergedMetadataMap = new HashMap<String, Object>();
              if (replaceAllSettings) {
                mergedMetadataMap = metadataMap;
              } else {
                
                if (existingValues.getMetadataNameValues() != null) {
                  mergedMetadataMap.putAll(existingValues.getMetadataNameValues());
                }
                
                mergedMetadataMap.putAll(metadataMap);
              }
              
              Map<String, Object> metadataNameValues = populateMetadata(mergedMetadataMap);
             
              if (!GrouperUtil.mapEquals(metadataNameValues, existingValues.getMetadataNameValues())) {
                attributeValue.setMetadataNameValues(metadataNameValues);
                saveResultType = SaveResultType.UPDATE;
              }
              
              if (saveResultType == SaveResultType.UPDATE) {           
                GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, group);
              }
              
            }
            
            return GrouperProvisioningService.getProvisioningAttributeValue(group, targetName);
          }

        });
      }
    });
    
    return grouperProvisioningAttributeValue;
  }
  

  private Map<String, Object> populateMetadata(Map<String, Object> metadataMap) {
    
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(targetName).initialize(GrouperProvisioningType.fullProvisionFull);

    GrouperProvisioningObjectMetadata provisioningObjectMetadata = grouperProvisioner.retrieveGrouperProvisioningObjectMetadata();
    List<GrouperProvisioningObjectMetadataItem> metadataItems = provisioningObjectMetadata.getGrouperProvisioningObjectMetadataItems();
    List<GrouperProvisioningObjectMetadataItem> metadataItemsForFolder = metadataItems.stream()
        .filter(metadataItem -> metadataItem.isShowForFolder())
        .collect(Collectors.toList());
    
    Set<String> validMetadataNames = metadataItemsForFolder.stream().map(metadataItem -> metadataItem.getName()).collect(Collectors.toSet());
    
    Map<String, Object> metadataNameValues = new HashMap<String, Object>();
    
    for (GrouperProvisioningObjectMetadataItem metadataItem: metadataItemsForFolder) {
      
      if (metadataItem.isRequired() && (!metadataMap.containsKey(metadataItem.getName()) || metadataMap.get(metadataItem.getName()) == null) ) {
        throw new RuntimeException(metadataItem.getName() +" is a required field. Add it to the metadataMap.");
      }
      
      if (metadataMap.get(metadataItem.getName()) != null) {
        try {                 
          Object convertedValue = metadataItem.getValueType().convert(metadataMap.get(metadataItem.getName()));
          if (!GrouperUtil.equals(metadataMap.get(metadataItem.getName()), metadataItem.getDefaultValue())) {
            metadataNameValues.put(metadataItem.getName(), convertedValue);
          }
        } catch(Exception e) {
          throw new RuntimeException(metadataItem.getName() +" is not of correct type.");
        }
      }
      
    }
    
    Map<String, String> validateMetadataInputForFolder = provisioningObjectMetadata.validateMetadataInputForFolder(metadataNameValues);
    
    if (validateMetadataInputForFolder != null && validateMetadataInputForFolder.size() > 0) {
      throw new RuntimeException("There are errors in metadata input. "+GrouperUtil.mapToString(validateMetadataInputForFolder));
    }
    
    for (String metadataName: metadataMap.keySet()) {
      if (!validMetadataNames.contains(metadataName)) {
        throw new RuntimeException("'"+metadataName+"' is not a valid metadata field.");
      }
    }
    
    return metadataNameValues;
  }
  
}
