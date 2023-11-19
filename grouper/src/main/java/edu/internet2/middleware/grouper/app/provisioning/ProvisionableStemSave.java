package edu.internet2.middleware.grouper.app.provisioning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.ObjectUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
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
 * <p>Use this class to add/edit/delete provisioning attributes on stems</p>
 * <p>Sample call
 * 
 * <blockquote>
 * <pre>
 * ProvisionableStemSave provisionableStemSave = new ProvisionableStemSave();
 * GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = provisionableStemSave.assignTargetName("ldapProvTest").assignMetadataString("md_testInput", "testValue").assignStem(stem).save();
 * System.out.println(provisionableStemSave.getSaveResultType()); // INSERT, DELETE, NO_CHANGE, or UPDATE
 * </pre>
 * </blockquote>
 * 
 * </p>
 * 
 * <p> Sample call to delete provisioning attributes from a stem
 * <blockquote>
 * <pre>
 * ProvisionableStemSave provisionableStemSave = new ProvisionableStemSave();
 * provisionableStemSave.assignTargetName("ldapProvTest")
        .assignSaveMode(SaveMode.DELETE).assignStem(stem).save();
 * </pre>
 * </blockquote>
 * </p>
 * <p> Sample call to update only single attribute
 * <blockquote>
 * <pre>
 * ProvisionableStemSave provisionableStemSave = new ProvisionableStemSave();
 * GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = provisionableStemSave.assignTargetName("ldapProvTest")
        .assignPolicyGroupOnly(true)
        .assignReplaceAllSettings(false)
        .assignStem(stem).save();
 * </pre>
 * </blockquote>
 * </p>
 */
public class ProvisionableStemSave {
  
  private Stem stem;
  
  private String stemId;
  
  private String stemName;
  
  private boolean runAsRoot;
  
  private boolean replaceAllSettings = true;
  
  private Map<String, Object> metadataMap = new HashMap<String, Object>();
  
  private String targetName;
  
  private Scope stemScope = Scope.SUB;
  
  private boolean stemScopeAssigned;
  
  private boolean provision = true;
  
  private boolean provisionAssigned;
  
  /** save mode */
  private SaveMode saveMode;
  
  private SaveResultType saveResultType;
  
  /**
   * assign provision or not
   * @param provision
   * @return
   */
  public ProvisionableStemSave assignProvision(boolean provision) {
    this.provision = provision;
    this.provisionAssigned = true;
    return this;
  }
  
  /**
   * name of the provisioning target
   * @param targetName
   * @return
   */
  public ProvisionableStemSave assignTargetName(String targetName) {
    this.targetName = targetName;
    return this;
  }
  
  /**
   * assign stem scope. SUB/ONE
   * @param stemScope
   * @return
   */
  public ProvisionableStemSave assignStemScope(Scope stemScope) {
    this.stemScope = stemScope;
    this.stemScopeAssigned = true;
    return this;
  }
  
  /**
   * assign stem scrope. SUB/ONE
   * @param stemScope
   * @return
   */
  public ProvisionableStemSave assignStemScopeString(String stemScope) {
    this.stemScope = Scope.valueOfIgnoreCase(stemScope, true);
    this.stemScopeAssigned = true;
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
  public ProvisionableStemSave assignRunAsRoot(boolean runAsRoot) {
    this.runAsRoot = runAsRoot;
    return this;
  }
  
  /**
   * replace all existing settings. defaults to true.
   * @return this for chaining
   */
  public ProvisionableStemSave assignReplaceAllSettings(boolean replaceAllSettings) {
    this.replaceAllSettings = replaceAllSettings;
    return this;
  }
  
  /**
   * assign save mode
   * @param saveMode
   * @return
   */
  public ProvisionableStemSave assignSaveMode(SaveMode saveMode) {
    this.saveMode = saveMode;
    return this;
  }
  
  /**
   * assign stem on which attributes need to be stored
   * @param stem
   * @return
   */
  public ProvisionableStemSave assignStem(Stem stem) {
    this.stem = stem;
    return this;
  }
  
  /**
   * assign stem id on which attributes need to be stored
   * @param stem
   * @return
   */
  public ProvisionableStemSave assignStemId(String stemId) {
    this.stemId = stemId;
    return this;
  }
  
  /**
   * assign stem name on which attributes need to be stored
   * @param stem
   * @return
   */
  public ProvisionableStemSave assignStemName(String stemName) {
    this.stemName = stemName;
    return this;
  }
  
  /**
   * assign string type metadata
   * @param name
   * @param value
   * @return
   */
  public ProvisionableStemSave assignMetadataString(String name, String value) {
    metadataMap.put(name, value);
    return this;
  }
  
  /**
   * assign boolean type metadata
   * @param name
   * @param value
   * @return
   */
  public ProvisionableStemSave assignMetadataBoolean(String name, Boolean value) {
    metadataMap.put(name, value);
    return this;
  }
  
  /**
   * assign integer type metadata
   * @param name
   * @param value
   * @return
   */
  public ProvisionableStemSave assignMetadataInteger(String name, Integer value) {
    metadataMap.put(name, value);
    return this;
  }
  
  /**
   * assign policy group only metadata
   * @param policyGroupOnly
   * @return
   */
  public ProvisionableStemSave assignPolicyGroupOnly(boolean policyGroupOnly) {
    metadataMap.put("md_grouper_allowPolicyGroupOverride", policyGroupOnly);
    return this;
  }
  
  /**
   * assign provisionable regex.
   * @param provisionableRegex
   * @return
   */
  public ProvisionableStemSave assignProvisionableRegex(String provisionableRegex) {
    metadataMap.put("md_grouper_allowProvisionableRegexOverride", provisionableRegex);
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
            
            if (stem == null && !StringUtils.isBlank(stemId)) {
              stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), stemId, false, new QueryOptions().secondLevelCache(false));
            }
            
            if (stem == null && !StringUtils.isBlank(stemName)) {
              stem = StemFinder.findByName(GrouperSession.staticGrouperSession(), stemName, false, new QueryOptions().secondLevelCache(false));
            }
            
            GrouperUtil.assertion(stem != null,  "Stem not found");
            
            GrouperProvisioningTarget provisioningTarget = GrouperProvisioningSettings.getTargets(true).get(targetName);
            
            if (!runAsRoot) {
              
              if (!PrivilegeHelper.isWheelOrRoot(SUBJECT_IN_SESSION)) {
                throw new RuntimeException("Subject '" + SubjectUtils.subjectToString(SUBJECT_IN_SESSION)+ "' is not wheel or root user.");
              }
              
              if (!GrouperProvisioningService.isTargetEditable(provisioningTarget, SUBJECT_IN_SESSION, stem)) {
                throw new RuntimeException("Not allowed to edit target.");
              }
              
            }
            
            if (!GrouperProvisioningService.isTargetEditable(provisioningTarget, grouperSession.getSubject(), stem)) {
              throw new RuntimeException("Not allowed to edit target.");
            }
            
            if (saveMode == SaveMode.DELETE) {
              
              GrouperProvisioningAttributeValue provisioningAttributeValueBefore = GrouperProvisioningService.getProvisioningAttributeValue(stem, targetName);
              
              if (provisioningAttributeValueBefore != null) {            
                GrouperProvisioningService.deleteAttributeAssign(stem, targetName);
                saveResultType = SaveResultType.DELETE;
                return GrouperProvisioningService.getProvisioningAttributeValue(stem, targetName);
              } else {
                saveResultType = SaveResultType.NO_CHANGE;
                return null;
              }
              
            }
            
            GrouperProvisioningAttributeValue existingValues = GrouperProvisioningService.getProvisioningAttributeValue(stem, targetName);
            
            boolean existingDirectAssignment = existingValues != null && existingValues.isDirectAssignment();
            
            if (saveMode == SaveMode.UPDATE && !existingDirectAssignment) {
              throw new RuntimeException("Updating provisioning settings on a stem but they do not exist.");
            }
            
            if (saveMode == SaveMode.INSERT && existingDirectAssignment) {
              throw new RuntimeException("Inserting provisioning settings on a stem but they already exist.");
            }
            
            if (stemScopeAssigned && stemScope == null) {
              throw new RuntimeException("stem scope cannot be set to null.");
            }
            
            String doProvision = provision ? targetName : null;
            
            if (existingValues == null) {
              
              final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
              attributeValue.setDirectAssignment(true);
              attributeValue.setDoProvision(doProvision);
              attributeValue.setTargetName(targetName);
              attributeValue.setStemScopeString(stemScope.name());
              
              Map<String, Object> metadataNameValues = populateMetadata(metadataMap);
             
              attributeValue.setMetadataNameValues(metadataNameValues);
              
              GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
              
              saveResultType = SaveResultType.INSERT;
              
            } else {
              
              final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
              attributeValue.setDirectAssignment(true);
              
              saveResultType = SaveResultType.NO_CHANGE;
              
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
              
              if (replaceAllSettings || stemScopeAssigned) {
                
                if (!StringUtils.equals(existingValues.getStemScopeString(), stemScope.name())) {
                  attributeValue.setStemScopeString(stemScope.name());
                  saveResultType = SaveResultType.UPDATE;
                } else {
                  attributeValue.setStemScopeString(existingValues.getStemScopeString());
                }
                
              } else {
                attributeValue.setStemScopeString(existingValues.getStemScopeString());
              }
              
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
                GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
              }
            }
            
            return GrouperProvisioningService.getProvisioningAttributeValue(stem, targetName);
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
