package edu.internet2.middleware.grouper.app.provisioning;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLogState;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.subject.provider.SourceManager;

/**
 * 
 * @author mchyzer
 *
 */
public abstract class GrouperProvisioningConfiguration {
  
  /**
   * thread pool size
   */
  private int threadPoolSize = 5;

  
  
// grouper-loader.base.properties 3090
//  # If the group requires members then if there are no members it is not valid and could be deleted
//  # {valueType: "boolean", subSection: "advanced", defaultValue: "false", order: 113000, showEl: "${showAdvanced}"}
//  # provisioner.genericProvisioner.groupsRequireMembers =

  /**
   * thread pool size
   * @return thread pool size
   */
  public int getThreadPoolSize() {
    return this.threadPoolSize;
  }

  private boolean customizeEntityCrud;
  
  public boolean isCustomizeEntityCrud() {
    return customizeEntityCrud;
  }

  public void setCustomizeEntityCrud(boolean customizeEntityCrud) {
    this.customizeEntityCrud = customizeEntityCrud;
  }

  /**
   * # If the full or incremental provisioner should have a ERROR if there is an error in a group / entity / membership
   * # {valueType: "boolean", order: 130010, defaultValue: "true", subSection: "errorHandling", showEl: "${errorHandlingShow}"}
   * # provisioner.genericProvisioner.errorHandlingProvisionerDaemonShouldFailOnObjectError =
   */
  private boolean errorHandlingProvisionerDaemonShouldFailOnObjectError = true;

  /**
   * # If the full or incremental provisioner should have a ERROR if there is an error in a group / entity / membership
   * # {valueType: "boolean", order: 130010, defaultValue: "true", subSection: "errorHandling", showEl: "${errorHandlingShow}"}
   * # provisioner.genericProvisioner.errorHandlingProvisionerDaemonShouldFailOnObjectError =
   * @return
   */
  public boolean isErrorHandlingProvisionerDaemonShouldFailOnObjectError() {
    return errorHandlingProvisionerDaemonShouldFailOnObjectError;
  }

  /**
   * # If the full or incremental provisioner should have a ERROR if there is an error in a group / entity / membership
   * # {valueType: "boolean", order: 130010, defaultValue: "true", subSection: "errorHandling", showEl: "${errorHandlingShow}"}
   * # provisioner.genericProvisioner.errorHandlingProvisionerDaemonShouldFailOnObjectError =
   * @param errorHandlingProvisionerDaemonShouldFailOnObjectError
   */
  public void setErrorHandlingProvisionerDaemonShouldFailOnObjectError(
      boolean errorHandlingProvisionerDaemonShouldFailOnObjectError) {
    this.errorHandlingProvisionerDaemonShouldFailOnObjectError = errorHandlingProvisionerDaemonShouldFailOnObjectError;
  }

  /**
   * Level 1 error handling percent.  This is the percent chance that the incremental will process errors.
   */
  private float errorHandlingPercentLevel1 = 1;
  
  /**
   * Level 1 error handling minutes.  This is how many minutes in the past that error will be retried (+20 seconds).
   */
  private float errorHandlingMinutesLevel1 = 180;
  
  /**
   * Level 2 error handling percent.  This is the percent chance that the incremental will process errors.  Note, this does not include the level 1, so it actually occurs level2% - level1% of the time.
   */
  private float errorHandlingPercentLevel2 = 5;

  /**
   * Level 2 error handling minutes.  This is how many minutes in the past that error will be retried (+20 seconds).
   */
  private float errorHandlingMinutesLevel2 = 120;

  /**
   * Level 3 error handling percent.  This is the percent chance that the incremental will process errors.  Note, this does not include the level 1+2, so it actually occurs level3% - level1+2% of the time.
   */
  private float errorHandlingPercentLevel3 = 10;

  /**
   * Level 3 error handling minutes.  This is how many minutes in the past that error will be retried (+20 seconds).
   */
  private float errorHandlingMinutesLevel3 = 12;

  /**
   * Level 4 error handling percent.  This is the percent chance that the incremental will process errors.  Note, this does not include the level 1+2+3, so it actually occurs level4% - level1+2+3% of the time.  If 100 then do this all the time.
   */
  private float errorHandlingPercentLevel4 = 100;

  /**
   * Level 4 error handling minutes.  This is how many minutes in the past that error will be retried (+20 seconds).
   */
  private float errorHandlingMinutesLevel4 = 3;

  
  public float getErrorHandlingPercentLevel1() {
    return errorHandlingPercentLevel1;
  }

  
  public float getErrorHandlingMinutesLevel1() {
    return errorHandlingMinutesLevel1;
  }

  
  public float getErrorHandlingPercentLevel2() {
    return errorHandlingPercentLevel2;
  }

  
  public float getErrorHandlingMinutesLevel2() {
    return errorHandlingMinutesLevel2;
  }

  
  public float getErrorHandlingPercentLevel3() {
    return errorHandlingPercentLevel3;
  }

  
  public float getErrorHandlingMinutesLevel3() {
    return errorHandlingMinutesLevel3;
  }

  
  public float getErrorHandlingPercentLevel4() {
    return errorHandlingPercentLevel4;
  }

  
  public float getErrorHandlingMinutesLevel4() {
    return errorHandlingMinutesLevel4;
  }

  /**
   * # Object errors will be logged, at least a handful of each type
   * # {valueType: "boolean", order: 130020, defaultValue: "true", subSection: "errorHandling", showEl: "${errorHandlingShow}"}
   * # provisioner.genericProvisioner.errorHandlingLogErrors =
   */
  private boolean errorHandlingLogErrors = true;

  /**
   * # Object errors will be logged, at least a handful of each type
   * # {valueType: "boolean", order: 130020, defaultValue: "true", subSection: "errorHandling", showEl: "${errorHandlingShow}"}
   * # provisioner.genericProvisioner.errorHandlingLogErrors =
   * @return
   */
  public boolean isErrorHandlingLogErrors() {
    return errorHandlingLogErrors;
  }

  /**
   * # Object errors will be logged, at least a handful of each type
   * # {valueType: "boolean", order: 130020, defaultValue: "true", subSection: "errorHandling", showEl: "${errorHandlingShow}"}
   * # provisioner.genericProvisioner.errorHandlingLogErrors =
   * @param errorHandlingLogErrors
   */
  public void setErrorHandlingLogErrors(boolean errorHandlingLogErrors) {
    this.errorHandlingLogErrors = errorHandlingLogErrors;
  }

  /**
   * # Object errors will be logged, at least a handful of each type
   * # {valueType: "integer", order: 130030, defaultValue: "5", subSection: "errorHandling", showEl: "${errorHandlingShow && errorHandlingLogErrors}"}
   * # provisioner.genericProvisioner.errorHandlingLogCountPerType = 
   */
  private int errorHandlingLogCountPerType = 5;

  /**
   * # Object errors will be logged, at least a handful of each type
   * # {valueType: "integer", order: 130030, defaultValue: "5", subSection: "errorHandling", showEl: "${errorHandlingShow && errorHandlingLogErrors}"}
   * # provisioner.genericProvisioner.errorHandlingLogCountPerType = 
   * @return
   */
  public int getErrorHandlingLogCountPerType() {
    return errorHandlingLogCountPerType;
  }

  /**
   * # Object errors will be logged, at least a handful of each type
   * # {valueType: "integer", order: 130030, defaultValue: "5", subSection: "errorHandling", showEl: "${errorHandlingShow && errorHandlingLogErrors}"}
   * # provisioner.genericProvisioner.errorHandlingLogCountPerType = 
   * @param errorHandlingLogCountPerType1
   */
  public void setErrorHandlingLogCountPerType(int errorHandlingLogCountPerType1) {
    this.errorHandlingLogCountPerType = errorHandlingLogCountPerType1;
  }

  /**
   * # If invalid data counts as an error.  Data is invalid if it is the wrong type or fails a validation
   * # {valueType: "boolean", order: 130040, defaultValue: "true", subSection: "errorHandling", showEl: "${errorHandlingShow}"}
   * # provisioner.genericProvisioner.errorHandlingInvalidDataIsAnError = 
   */
  private boolean errorHandlingInvalidDataIsAnError = true;

  /**
   * # If invalid data counts as an error.  Data is invalid if it is the wrong type or fails a validation
   * # {valueType: "boolean", order: 130040, defaultValue: "true", subSection: "errorHandling", showEl: "${errorHandlingShow}"}
   * # provisioner.genericProvisioner.errorHandlingInvalidDataIsAnError = 
   * @return
   */
  public boolean isErrorHandlingInvalidDataIsAnError() {
    return errorHandlingInvalidDataIsAnError;
  }

  /**
   * # If invalid data counts as an error.  Data is invalid if it is the wrong type or fails a validation
   * # {valueType: "boolean", order: 130040, defaultValue: "true", subSection: "errorHandling", showEl: "${errorHandlingShow}"}
   * # provisioner.genericProvisioner.errorHandlingInvalidDataIsAnError = 
   * @param errorHandlingInvalidDataIsAnError
   */
  public void setErrorHandlingInvalidDataIsAnError(boolean errorHandlingInvalidDataIsAnError) {
    this.errorHandlingInvalidDataIsAnError = errorHandlingInvalidDataIsAnError;
  }

  /**
   * # If attribute length validation counts as an error.  This happens when there is a max length on an attribute and the data is too long
   * # {valueType: "boolean", order: 130050, defaultValue: "true", subSection: "errorHandling", showEl: "${errorHandlingShow}"}
   * # provisioner.genericProvisioner.errorHandlingLengthValidationIsAnError = 
   */
  private boolean errorHandlingLengthValidationIsAnError = true;

  /**
   * # If attribute length validation counts as an error.  This happens when there is a max length on an attribute and the data is too long
   * # {valueType: "boolean", order: 130050, defaultValue: "true", subSection: "errorHandling", showEl: "${errorHandlingShow}"}
   * # provisioner.genericProvisioner.errorHandlingLengthValidationIsAnError = 
   * @return
   */
  public boolean isErrorHandlingLengthValidationIsAnError() {
    return errorHandlingLengthValidationIsAnError;
  }

  /**
   * # If attribute length validation counts as an error.  This happens when there is a max length on an attribute and the data is too long
   * # {valueType: "boolean", order: 130050, defaultValue: "true", subSection: "errorHandling", showEl: "${errorHandlingShow}"}
   * # provisioner.genericProvisioner.errorHandlingLengthValidationIsAnError = 
   * @param errorHandlingLengthValidationIsAnError
   */
  public void setErrorHandlingLengthValidationIsAnError(boolean errorHandlingLengthValidationIsAnError) {
    this.errorHandlingLengthValidationIsAnError = errorHandlingLengthValidationIsAnError;
  }

  /**
   * # If the grouper translated objects match to multiple target objects on the same attribute, then this problem happens
   * # {valueType: "boolean", order: 130065, defaultValue: "true", subSection: "errorHandling", showEl: "${errorHandlingShow}"}
   * # provisioner.genericProvisioner.errorHandlingMatchingValidationIsAnError = 
   */
  private boolean errorHandlingMatchingValidationIsAnError = true;

  /**
   * # If the grouper translated objects match to multiple target objects on the same attribute, then this problem happens
   * # {valueType: "boolean", order: 130065, defaultValue: "true", subSection: "errorHandling", showEl: "${errorHandlingShow}"}
   * # provisioner.genericProvisioner.errorHandlingMatchingValidationIsAnError = 
   * @return true if so
   */
  public boolean isErrorHandlingMatchingValidationIsAnError() {
    return this.errorHandlingMatchingValidationIsAnError;
  }

  /**
   * # If the grouper translated objects match to multiple target objects on the same attribute, then this problem happens
   * # {valueType: "boolean", order: 130065, defaultValue: "true", subSection: "errorHandling", showEl: "${errorHandlingShow}"}
   * # provisioner.genericProvisioner.errorHandlingMatchingValidationIsAnError = 
   * @param errorHandlingMatchingValidationIsAnError1
   */
  public void setErrorHandlingMatchingValidationIsAnError(boolean errorHandlingMatchingValidationIsAnError1) {
    this.errorHandlingMatchingValidationIsAnError = errorHandlingMatchingValidationIsAnError1;
  }

  /**
   * # If required but missing attributes count as an error.  Attribute can be marked as required, if they are blank then this problem happens
   * # {valueType: "boolean", order: 130060, defaultValue: "true", subSection: "errorHandling", showEl: "${errorHandlingShow}"}
   * # provisioner.genericProvisioner.errorHandlingRequiredValidationIsAnError = 
   */
  private boolean errorHandlingRequiredValidationIsAnError = true;

  /**
   * # If required but missing attributes count as an error.  Attribute can be marked as required, if they are blank then this problem happens
   * # {valueType: "boolean", order: 130060, defaultValue: "true", subSection: "errorHandling", showEl: "${errorHandlingShow}"}
   * # provisioner.genericProvisioner.errorHandlingRequiredValidationIsAnError = 
   * @return
   */
  public boolean isErrorHandlingRequiredValidationIsAnError() {
    return errorHandlingRequiredValidationIsAnError;
  }
  
  /**
   * # If required but missing attributes count as an error.  Attribute can be marked as required, if they are blank then this problem happens
   * # {valueType: "boolean", order: 130060, defaultValue: "true", subSection: "errorHandling", showEl: "${errorHandlingShow}"}
   * # provisioner.genericProvisioner.errorHandlingRequiredValidationIsAnError = 
   * @param errorHandlingRequiredValidationIsAnError
   */
  public void setErrorHandlingRequiredValidationIsAnError(boolean errorHandlingRequiredValidationIsAnError) {
    this.errorHandlingRequiredValidationIsAnError = errorHandlingRequiredValidationIsAnError;
  }

  /**
   * # If missing object in target counts as an error.  If the object is missing from the target and cannot be created this this problem happens
   * # {valueType: "boolean", order: 130070, defaultValue: "true", subSection: "errorHandling", showEl: "${errorHandlingShow}"}
   * # provisioner.genericProvisioner.errorHandlingTargetObjectDoesNotExistIsAnError = 
   */
  private boolean errorHandlingTargetObjectDoesNotExistIsAnError = true;

  /**
   * # If missing object in target counts as an error.  If the object is missing from the target and cannot be created this this problem happens
   * # {valueType: "boolean", order: 130070, defaultValue: "true", subSection: "errorHandling", showEl: "${errorHandlingShow}"}
   * # provisioner.genericProvisioner.errorHandlingTargetObjectDoesNotExistIsAnError = 
   * @return
   */
  public boolean isErrorHandlingTargetObjectDoesNotExistIsAnError() {
    return errorHandlingTargetObjectDoesNotExistIsAnError;
  }

  /**
   * # If missing object in target counts as an error.  If the object is missing from the target and cannot be created this this problem happens
   * # {valueType: "boolean", order: 130070, defaultValue: "true", subSection: "errorHandling", showEl: "${errorHandlingShow}"}
   * # provisioner.genericProvisioner.errorHandlingTargetObjectDoesNotExistIsAnError = 
   * @param errorHandlingTargetObjectDoesNotExistIsAnError
   */
  public void setErrorHandlingTargetObjectDoesNotExistIsAnError(boolean errorHandlingTargetObjectDoesNotExistIsAnError) {
    this.errorHandlingTargetObjectDoesNotExistIsAnError = errorHandlingTargetObjectDoesNotExistIsAnError;
  }

  
  
  private boolean makeChangesToEntities;
  
  

  public boolean isMakeChangesToEntities() {
    return makeChangesToEntities;
  }

  public void setMakeChangesToEntities(boolean makeChangesToEntities) {
    this.makeChangesToEntities = makeChangesToEntities;
  }

  private boolean customizeMembershipCrud;

  public boolean isCustomizeMembershipCrud() {
    return customizeMembershipCrud;
  }



  private String subjectIdentifierForMemberSyncTable;
  
  


  public String getSubjectIdentifierForMemberSyncTable() {
    return subjectIdentifierForMemberSyncTable;
  }

  public void setCustomizeMembershipCrud(boolean customizeMembershipCrud) {
    this.customizeMembershipCrud = customizeMembershipCrud;
  }
  
  private boolean onlyAddMembershipsIfUserExistsInTarget;
  
  

  
  public boolean isOnlyAddMembershipsIfUserExistsInTarget() {
    return onlyAddMembershipsIfUserExistsInTarget;
  }

  private boolean customizeGroupCrud;

  public boolean isCustomizeGroupCrud() {
    return customizeGroupCrud;
  }






  public void setCustomizeGroupCrud(boolean customizeGroupCrud) {
    this.customizeGroupCrud = customizeGroupCrud;
  }


  private boolean groupsRequireMembers;
  
  private boolean hasEntityAttributes;
  
  private boolean resolveAttributesWithSql;

  private boolean resolveAttributesWithLdap;

  private boolean useGlobalSqlResolver;
  private boolean useGlobalLdapResolver;

  private String globalSqlResolver;

  private String globalLdapResolver;
  
  private boolean selectAllSqlOnFull = true;
  private boolean filterAllLDAPOnFull = true;
  
  private boolean loadEntitiesToGrouperTable;


  private String entityAttributesSqlExternalSystem;
  private String entityAttributesTableViewName;
  private String entityAttributesColumnNames;
  private String entityAttributesSubjectSourceIdColumn;
  private String entityAttributesSubjectSearchMatchingColumn;
  private String entityAttributesSqlMappingType;
  private String entityAttributesSqlMappingEntityAttribute;
  private String entityAttributesSqlMappingExpression;
  private String entityAttributesLastUpdatedColumn;
  private String entityAttributesLastUpdatedType;
  
  private String entityAttributesLdapExternalSystem;
  private String entityAttributesLdapBaseDn;
  private String entityAttributesLdapSubjectSource;
  private String entityAttributesLdapSearchScope;
  private String entityAttributesLdapFilterPart;
  private String entityAttributesLdapAttributes;
  private String entityAttributesLdapMutliValuedAttributes;
  private String entityAttributesLdapMatchingSearchAttribute;
  private String entityAttributesLdapMappingType;
  private String entityAttributesLdapMappingEntityAttribute;
  private String entityAttributesLdapMatchingExpression;
  private String entityAttributesLdapLastUpdatedAttribute;
  private String entityAttributesLdapLastUpdatedAttributeFormat;
  
  private String searchAttributeNameToRetrieveEntities;
  
  
  public String getSearchAttributeNameToRetrieveEntities() {
    return searchAttributeNameToRetrieveEntities;
  }

  
  public void setSearchAttributeNameToRetrieveEntities(
      String searchAttributeNameToRetrieveEntities) {
    this.searchAttributeNameToRetrieveEntities = searchAttributeNameToRetrieveEntities;
  }

  public boolean isFilterAllLDAPOnFull() {
    return filterAllLDAPOnFull;
  }





  
  public String getEntityAttributesLdapExternalSystem() {
    return entityAttributesLdapExternalSystem;
  }





  
  public String getEntityAttributesLdapBaseDn() {
    return entityAttributesLdapBaseDn;
  }





  
  public String getEntityAttributesLdapSearchScope() {
    return entityAttributesLdapSearchScope;
  }





  
  public String getEntityAttributesLdapFilterPart() {
    return entityAttributesLdapFilterPart;
  }





  
  public String getEntityAttributesLdapAttributes() {
    return entityAttributesLdapAttributes;
  }





  
  public String getEntityAttributesLdapMutliValuedAttributes() {
    return entityAttributesLdapMutliValuedAttributes;
  }





  
  public String getEntityAttributesLdapMatchingSearchAttribute() {
    return entityAttributesLdapMatchingSearchAttribute;
  }





  
  public String getEntityAttributesLdapMappingType() {
    return entityAttributesLdapMappingType;
  }





  
  public String getEntityAttributesLdapMappingEntityAttribute() {
    return entityAttributesLdapMappingEntityAttribute;
  }





  
  public String getEntityAttributesLdapMatchingExpression() {
    return entityAttributesLdapMatchingExpression;
  }





  
  public String getEntityAttributesLdapLastUpdatedAttribute() {
    return entityAttributesLdapLastUpdatedAttribute;
  }





  
  public String getEntityAttributesLdapLastUpdatedAttributeFormat() {
    return entityAttributesLdapLastUpdatedAttributeFormat;
  }





  public boolean isUseGlobalLdapResolver() {
    return useGlobalLdapResolver;
  }




  
  public String getGlobalLdapResolver() {
    return globalLdapResolver;
  }




  public String getEntityAttributesSqlExternalSystem() {
    return entityAttributesSqlExternalSystem;
  }



  
  public String getEntityAttributesTableViewName() {
    return entityAttributesTableViewName;
  }


  
  
  public String getEntityAttributesColumnNames() {
    return entityAttributesColumnNames;
  }





  public String getEntityAttributesSubjectSourceIdColumn() {
    return entityAttributesSubjectSourceIdColumn;
  }



  
  public String getEntityAttributesSubjectSearchMatchingColumn() {
    return entityAttributesSubjectSearchMatchingColumn;
  }



  
  public String getEntityAttributesSqlMappingType() {
    return entityAttributesSqlMappingType;
  }



  
  public String getEntityAttributesSqlMappingEntityAttribute() {
    return entityAttributesSqlMappingEntityAttribute;
  }



  
  public String getEntityAttributesSqlMappingExpression() {
    return entityAttributesSqlMappingExpression;
  }



  
  public String getEntityAttributesLastUpdatedColumn() {
    return entityAttributesLastUpdatedColumn;
  }



  
  public String getEntityAttributesLastUpdatedType() {
    return entityAttributesLastUpdatedType;
  }



  public boolean isHasEntityAttributes() {
    return hasEntityAttributes;
  }


  
  public boolean isResolveAttributesWithSql() {
    return resolveAttributesWithSql;
  }

  
  public boolean isResolveAttributesWithLdap() {
    return resolveAttributesWithLdap;
  }

  
  public boolean isUseGlobalSqlResolver() {
    return useGlobalSqlResolver;
  }

  
  
  public String getGlobalSqlResolver() {
    return globalSqlResolver;
  }

  
  
  public boolean isSelectAllSqlOnFull() {
    return selectAllSqlOnFull;
  }
  
  
  public boolean isLoadEntitiesToGrouperTable() {
    return loadEntitiesToGrouperTable;
  }

  /**
   * group of users to exclude from provisioning
   */
  private String groupIdOfUsersNotToProvision;

  /**
   * group of users to exclude from provisioning
   * @return
   */
  public String getGroupIdOfUsersNotToProvision() {
    return groupIdOfUsersNotToProvision;
  }

  /**
   * group of users to exclude from provisioning
   * @param groupIdOfUsersNotToProvision
   */
  public void setGroupIdOfUsersNotToProvision(String groupIdOfUsersNotToProvision) {
    this.groupIdOfUsersNotToProvision = groupIdOfUsersNotToProvision;
  }

  /**
   * if set then only provision users who are in this group
   */
  private String groupIdOfUsersToProvision;
  
  /**
   * if set then only provision users who are in this group
   * @return group id
   */
  public String getGroupIdOfUsersToProvision() {
    return groupIdOfUsersToProvision;
  }

  /**
   * if create group in target during diagnostics
   */
  private Boolean createGroupDuringDiagnostics;

  /**
   * if create group in target during diagnostics
   * @return if create
   */
  public boolean isCreateGroupDuringDiagnostics() {
    return GrouperUtil.booleanValue(createGroupDuringDiagnostics, false);
  }
  
  /**
   * if delete group from target during diagnostics
   */
  private Boolean deleteGroupDuringDiagnostics;

  /**
   * if delete group from target during diagnostics
   * @return if delete
   */
  public boolean isDeleteGroupDuringDiagnostics() {
    return GrouperUtil.booleanValue(deleteGroupDuringDiagnostics, false);
  }
  
  /**
   * if create entity in target during diagnostics
   */
  private Boolean createEntityDuringDiagnostics;

  /**
   * if create entity in target during diagnostics
   * @return if create
   */
  public boolean isCreateEntityDuringDiagnostics() {
    return GrouperUtil.booleanValue(createEntityDuringDiagnostics, false);
  }
  
  /**
   * if delete entity from target during diagnostics
   */
  private Boolean deleteEntityDuringDiagnostics;

  /**
   * if delete entity from target during diagnostics
   * @return if delete
   */
  public boolean isDeleteEntityDuringDiagnostics() {
    return GrouperUtil.booleanValue(deleteEntityDuringDiagnostics, false);
  }

  /**
   * if select all groups during diagnostics (default false)
   */
  private Boolean diagnosticsGroupsAllSelect;

  /**
   * if select all groups during diagnostics
   * @return true if so
   */
  public boolean isDiagnosticsGroupsAllSelect() {
    if (this.diagnosticsGroupsAllSelect != null) {
      return this.diagnosticsGroupsAllSelect;
    }
    return false;
  }
  
  /**
   * if select all entities during diagnostics (default false)
   */
  private Boolean diagnosticsEntitiesAllSelect;

  /**
   * if select all entities during diagnostics
   * @return true if so
   */
  public boolean isDiagnosticsEntitiesAllSelect() {
    if (this.diagnosticsEntitiesAllSelect != null) {
      return this.diagnosticsEntitiesAllSelect;
    }
    return false;
  }
  
  /**
   * group name of group to use for diagnostics
   */
  private String diagnosticsGroupName;

  /**
   * group name of group to use for diagnostics
   * @return the group name
   */
  public String getDiagnosticsGroupName() {
    return diagnosticsGroupName;
  }
  
  /**
   * subject id or identifier of entity to use for diagnostics
   */
  private String diagnosticsSubjectIdOrIdentifier;

  /**
   * subject id or identifier of entity to use for diagnostics
   * @return the subject id or identifier
   */
  public String getDiagnosticsSubjectIdOrIdentifier() {
    return diagnosticsSubjectIdOrIdentifier;
  }
  
  /**
   * if select all memberships during diagnostics (default false)
   */
  private Boolean diagnosticsMembershipsAllSelect;

  /**
   * if select all memberships during diagnostics
   * @return true if so
   */
  public boolean isDiagnosticsMembershipsAllSelect() {
    if (this.diagnosticsMembershipsAllSelect != null) {
      return this.diagnosticsMembershipsAllSelect;
    }
    return false;
  }
  
  /**
   * Only provision policy groups
   */
  private Boolean onlyProvisionPolicyGroups;
  
  /**
   * Only provision policy groups, default false
   * @return 
   */
  public boolean isOnlyProvisionPolicyGroups() {
    if (this.onlyProvisionPolicyGroups != null) {
      return this.onlyProvisionPolicyGroups;
    }
    return false;
  }
  
  /**
   * If you want a metadata item on folders for specifying if provision only policy groups
   */
  private Boolean allowPolicyGroupOverride;
  
  /**
   * If you want a metadata item on folders for specifying if provision only policy groups
   * @return
   */
  public boolean isAllowPolicyGroupOverride() {
    if (this.allowPolicyGroupOverride != null) {
      return this.allowPolicyGroupOverride;
    }
    return true;
  }
  
  /**
   * If you want to filter for groups in a provisionable folder by a regex on its name, specify here.  If the regex matches then the group in the folder is provisionable.  e.g. folderExtension matches ^.*_someExtension   folderName matches ^.*_someExtension   groupExtension matches ^.*_someExtension   groupName matches ^.*_someExtension$
   */
  private String provisionableRegex;

  /**
   * If you want to filter for groups in a provisionable folder by a regex on its name, specify here.  If the regex matches then the group in the folder is provisionable.  e.g. folderExtension matches ^.*_someExtension   folderName matches ^.*_someExtension   groupExtension matches ^.*_someExtension   groupName matches ^.*_someExtension$
   * @return
   */
  public String getProvisionableRegex() {
    return this.provisionableRegex;
  }
  
  /**
   * If you want a metadata item on folders for specifying regex of names of objects to provision
   */
  private Boolean allowProvisionableRegexOverride;
  
  /**
   * If you want a metadata item on folders for specifying regex of names of objects to provision
   * @return
   */
  public boolean isAllowProvisionableRegexOverride() {
    if (this.allowProvisionableRegexOverride != null) {
      return this.allowProvisionableRegexOverride;
    }
    return true;
  }
  
  private boolean readOnly;
  
  
  public boolean isReadOnly() {
    return readOnly;
  }

  /** if the target should be checked before sending actions.  e.g. if an addMember is made to a provisionable group, then check the target to see if the entity is already a member first. */
  private boolean recalculateAllOperations;

  /**
   * attribute name to config
   */
  private Map<String, GrouperProvisioningConfigurationAttribute> targetGroupAttributeNameToConfig = new LinkedHashMap<String, GrouperProvisioningConfigurationAttribute>();
  
  /**
   * metadata name to metadata item
   */
  private Map<String, GrouperProvisioningObjectMetadataItem> metadataNameToMetadataItem = new TreeMap<String, GrouperProvisioningObjectMetadataItem>();
  
  /**
   * metadata name to metadata item
   * @return
   */
  public Map<String, GrouperProvisioningObjectMetadataItem> getMetadataNameToMetadataItem() {
    return metadataNameToMetadataItem;
  }

  /**
   * metadata name to metadata item
   * @param metadataNameToMetadataItem
   */
  public void setMetadataNameToMetadataItem(
      Map<String, GrouperProvisioningObjectMetadataItem> metadataNameToMetadataItem) {
    this.metadataNameToMetadataItem = metadataNameToMetadataItem;
  }

  /**
   * 
   * @return map
   */
  public Map<String, GrouperProvisioningConfigurationAttribute> getTargetGroupAttributeNameToConfig() {
    return targetGroupAttributeNameToConfig;
  }

  /**
   * expression to get the membership id from the target group
   */
  private String membershipMatchingIdExpression;

  public String getMembershipMatchingIdExpression() {
    return membershipMatchingIdExpression;
  }


  
  public void setMembershipMatchingIdExpression(String membershipMatchingIdExpression) {
    this.membershipMatchingIdExpression = membershipMatchingIdExpression;
  }
  
  
  private boolean unresolvableSubjectsInsert = false;

  public boolean isUnresolvableSubjectsInsert() {
    return unresolvableSubjectsInsert;
  }

  
  public void setUnresolvableSubjectsInsert(boolean unresolvableSubjectsInsert) {
    this.unresolvableSubjectsInsert = unresolvableSubjectsInsert;
  }

  private boolean unresolvableSubjectsRemove = false;

  public boolean isUnresolvableSubjectsRemove() {
    return unresolvableSubjectsRemove;
  }


  public void setUnresolvableSubjectsRemove(boolean unresolvableSubjectsRemove) {
    this.unresolvableSubjectsRemove = unresolvableSubjectsRemove;
  }
  
  
  private boolean logCommandsAlways = false;

  
  
  
  public boolean isLogCommandsAlways() {
    return logCommandsAlways;
  }

  
  public void setLogCommandsAlways(boolean logCommandsAlways) {
    this.logCommandsAlways = logCommandsAlways;
  }

  private int logMaxErrorsPerType;
  
  public int getLogMaxErrorsPerType() {
    return logMaxErrorsPerType;
  }

  public void setLogMaxErrorsPerType(int logMaxErrorsPerType) {
    this.logMaxErrorsPerType = logMaxErrorsPerType;
  }

  public boolean isLogCommandsOnError() {
    return logCommandsOnError;
  }

  
  public void setLogCommandsOnError(boolean logCommandsOnError) {
    this.logCommandsOnError = logCommandsOnError;
  }

  private boolean logCommandsOnError = false;
  
  private boolean logAllObjectsVerbose = false;
  
  private boolean logAllObjectsVerboseToLogFile = false;

  private boolean logAllObjectsVerboseToDaemonDbLog = false;
  
  
  
  public boolean isLogAllObjectsVerboseToLogFile() {
    return logAllObjectsVerboseToLogFile;
  }

  public void setLogAllObjectsVerboseToLogFile(boolean logAllObjectsVerboseToLogFile) {
    this.logAllObjectsVerboseToLogFile = logAllObjectsVerboseToLogFile;
  }

  public boolean isLogAllObjectsVerboseToDaemonDbLog() {
    return logAllObjectsVerboseToDaemonDbLog;
  }

  public void setLogAllObjectsVerboseToDaemonDbLog(boolean logAllObjectsVerboseToDaemonDbLog) {
    this.logAllObjectsVerboseToDaemonDbLog = logAllObjectsVerboseToDaemonDbLog;
  }

  public boolean isLogAllObjectsVerbose() {
    return logAllObjectsVerbose;
  }

  
  public void setLogAllObjectsVerbose(boolean logAllObjectsVerbose) {
    this.logAllObjectsVerbose = logAllObjectsVerbose;
  }

  private boolean debugLog = false;
  
  
  
  
  public boolean isDebugLog() {
    return debugLog;
  }



  
  public void setDebugLog(boolean debugLog) {
    this.debugLog = debugLog;
  }

  /**
   * reference back up to the provisioner
   */
  private GrouperProvisioner grouperProvisioner = null;

  /**
   * reference back up to the provisioner
   * @return the provisioner
   */
  public GrouperProvisioner getGrouperProvisioner() {
    return this.grouperProvisioner;
  }

  /**
   * reference back up to the provisioner
   * @param grouperProvisioner1
   */
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner1) {
    this.grouperProvisioner = grouperProvisioner1;
  }

  private String configId;

  public void setConfigId(String configId) {
    this.configId = configId;
  }
  public String getConfigId() {
    return configId;
  }
  
  /**
   * key is groupEntity or membership
   */
  private Map<String, List<String>> grouperProvisioningToTargetTranslation = new HashMap<String, List<String>>();
  
  
  public Map<String, List<String>> getGrouperProvisioningToTargetTranslation() {
    return grouperProvisioningToTargetTranslation;
  }
  
  /**
   * In incremental processing, each provisionable group/entity to sync memberships to sync counts as 10, each provisionable membership to sync counts as 1.  
   * If the total score is more than this number, it will convert the incrementals to a a full sync.  e.g. 10000 individual memberships 
   * to sync (and not more than 500 in a single group), or 1000 groups to sync, or a combination.  -1 means do not convert to full sync
   */
  private int scoreConvertToFullSyncThreshold;
  
  /**
   * In incremental processing, each provisionable group/entity to sync memberships to sync counts as 10, each provisionable membership to sync counts as 1.  
   * If the total score is more than this number, it will convert the incrementals to a a full sync.  e.g. 10000 individual memberships 
   * to sync (and not more than 500 in a single group), or 1000 groups to sync, or a combination.  -1 means do not convert to full sync
   * @return
   */
  public int getScoreConvertToFullSyncThreshold() {
    return scoreConvertToFullSyncThreshold;
  }

  /**
   * In incremental processing, each provisionable group/entity to sync memberships to sync counts as 10, each provisionable membership to sync counts as 1.  
   * If the total score is more than this number, it will convert the incrementals to a a full sync.  e.g. 10000 individual memberships 
   * to sync (and not more than 500 in a single group), or 1000 groups to sync, or a combination.  -1 means do not convert to full sync
   * @param scoreConvertToFullSyncThreshold1
   */
  public void setScoreConvertToFullSyncThreshold(int scoreConvertToFullSyncThreshold1) {
    this.scoreConvertToFullSyncThreshold = scoreConvertToFullSyncThreshold1;
  }

  /**
   * If there are this number of memberships or more for a single provisionable group, then perform a "group sync" instead of the individual operations instead, for efficiency
   * default to provisionerDefault.membershipsConvertToGroupSyncThreshold which is 500.
   * -1 to not use this feature
   */
  private int membershipsConvertToGroupSyncThreshold;
  
  /**
   * If there are this number of memberships or more for a single provisionable group, then perform a "group sync" instead of the individual operations instead, for efficiency
   * default to provisionerDefault.membershipsConvertToGroupSyncThreshold which is 500
   * -1 to not use this feature
   * @return threshold
   */
  public int getMembershipsConvertToGroupSyncThreshold() {
    return membershipsConvertToGroupSyncThreshold;
  }

  /**
   * If there are this number of memberships or more for a single provisionable group, then perform a "group sync" instead of the individual operations instead, for efficiency
   * default to provisionerDefault.membershipsConvertToGroupSyncThreshold which is 500
   * -1 to not use this feature
   * @param membershipsConvertToGroupSyncThreshold
   */
  public void setMembershipsConvertToGroupSyncThreshold(
      int membershipsConvertToGroupSyncThreshold) {
    this.membershipsConvertToGroupSyncThreshold = membershipsConvertToGroupSyncThreshold;
  }

  /**
   * get a config name for this or dependency
   * @param configName
   * @param required 
   * @return the config
   */
  public Boolean retrieveConfigBoolean(String configName, boolean required) {
    String configValueString = retrieveConfigString(configName, required);
    return GrouperClientUtils.booleanObjectValue(configValueString);
  }

  /**
   * get a config name for this or dependency
   * @param configName
   * @param required 
   * @return the config
   */
  public Double retrieveConfigDouble(String configName, boolean required) {
    String configValueString = retrieveConfigString(configName, required);
    return GrouperClientUtils.doubleObjectValue(configValueString);
  }

  /**
   * get a config name for this or dependency
   * @param configName
   * @param required 
   * @return the config
   */
  public Integer retrieveConfigInt(String configName, boolean required) {
    String configValueString = retrieveConfigString(configName, required);
    return GrouperClientUtils.intObjectValue(configValueString, true);
  }

  /**
   * get a config name for this or dependency
   * @param configSuffix
   * @param required 
   * @return the config
   */
  public String retrieveConfigString(String configSuffix, boolean required) {
    
    String value = GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner." + this.getConfigId() + "." + configSuffix);
    if (!StringUtils.isBlank(value)) {
      return value;
    }
    value = GrouperLoaderConfig.retrieveConfig().propertyValueString("provisionerDefault." + configSuffix);
    if (!StringUtils.isBlank(value)) {
      return value;
    }
    if (required) {
      throw new RuntimeException("Cant find config for provisioning: provisioner." + this.getConfigId() + "." + configSuffix);
    }
    return null;
  
  }

  private Map<String, Object> debugMap = null;

  public void preConfigure() {
    // must have key
    if (StringUtils.isBlank(this.grouperProvisioner.getConfigId())) {
      throw new RuntimeException("Why is config id blank?");
    }

    // must have provisioning type
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningType() == null) {
      throw new RuntimeException("Why is provisioning type blank?");
    }

    this.setConfigId(this.grouperProvisioner.getConfigId());

    if (this.grouperProvisioner.getGcGrouperSync() == null) {
      this.grouperProvisioner.setGcGrouperSync(GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, this.getConfigId()));
    }
    
    if (StringUtils.isBlank(this.grouperProvisioner.getGcGrouperSync().getSyncEngine())) {
      this.grouperProvisioner.getGcGrouperSync().setSyncEngine(GcGrouperSync.PROVISIONING);
      this.grouperProvisioner.getGcGrouperSync().getGcGrouperSyncDao().store();
    }
    
    if (!GrouperClientUtils.equals(GcGrouperSync.PROVISIONING, this.grouperProvisioner.getGcGrouperSync().getSyncEngine())) {
      throw new RuntimeException("Why is sync engine not 'provisioning'?  " + this.getConfigId() + ", " + this.grouperProvisioner.getGcGrouperSync().getSyncEngine());
    }

    GcGrouperSyncJob gcGrouperSyncJob = this.grouperProvisioner.getGcGrouperSyncJob();
    if (gcGrouperSyncJob == null) {
      gcGrouperSyncJob = this.grouperProvisioner.getGcGrouperSync().getGcGrouperSyncJobDao()
          .jobRetrieveOrCreateBySyncType(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().name());
      this.grouperProvisioner.setGcGrouperSyncJob(gcGrouperSyncJob);
    }
    if (GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("provisioner." + this.getConfigId() + ".debugConfig", false)) {
      
      debugMap = this.grouperProvisioner.getDebugMap();
    } else {
      debugMap = new LinkedHashMap<String, Object>();
    }
  }

  /**
   * If groups need to be resolved in the target before provisioning
   */
  private boolean hasTargetGroupLink = false;

  /**
   * If users need to be resolved in the target before provisioning
   */
  private boolean hasTargetEntityLink = false;

  /**
   * subject sources to provision  optional, defaults to all except g:gsa, grouperExternal, g:isa, localEntities. comma separated list. checkboxes. 
   */
  private Set<String> subjectSourcesToProvision = null;


  /**
   * attributes to use when searching
   */
  private List<GrouperProvisioningConfigurationAttribute> entitySearchAttributes = null;

  /**
   * attributes to use when searching
   */
  private List<GrouperProvisioningConfigurationAttribute> groupSearchAttributes = null;

  /** 
   * attributes to use when selecting from target
   */
  private Set<String> groupSelectAttributes = null;

  /**
   * attributes to use when selecting from target
   * @return
   */
  public Set<String> getGroupSelectAttributes() {
    return groupSelectAttributes;
  }

  /**
   * attributes to use when selecting from target
   * @param groupSelectAttributes
   */
  public void setGroupSelectAttributes(Set<String> groupSelectAttributes) {
    this.groupSelectAttributes = groupSelectAttributes;
  }

  /**
   * attributes to use when selecting from target
   * @return
   */
  public Set<String> getEntitySelectAttributes() {
    return entitySelectAttributes;
  }

 /**
  * attributes to use when selecting from target
  * @param entitySelectAttributes
  */
 public void setEntitySelectAttributes(Set<String> entitySelectAttributes) {
   this.entitySelectAttributes = entitySelectAttributes;
 }

 /**
  * attributes to use when selecting from target
  */
 private Set<String> entitySelectAttributes = null;

  /**
   * attributes to use when matching
   */
  private List<GrouperProvisioningConfigurationAttribute> entityMatchingAttributes = null;
  
  public List<GrouperProvisioningConfigurationAttribute> getEntityMatchingAttributes() {
    return entityMatchingAttributes;
  }

  /**
   * attributes to use when searching
   */
  private List<GrouperProvisioningConfigurationAttribute> groupMatchingAttributes = null;
  
  public List<GrouperProvisioningConfigurationAttribute> getGroupMatchingAttributes() {
    return groupMatchingAttributes;
  }

  /**
   * someAttr  everything is assumed to be single valued except objectclass and the provisionedAttributeName optional
   */
  private Set<String> entityAttributesMultivalued = null;
  
  /**
   * someAttr  everything is assumed to be single valued except objectclass and the provisionedAttributeName optional
   */
  private Set<String> groupAttributesMultivalued = null;

  /**
   * if there are fewer than this many subjects to process, just resolve them
   */
  private int refreshSubjectLinkIfLessThanAmount;
  
  /**
   * if there are fewer than this many groups to process, just resolve them
   */
  private int refreshGroupLinkIfLessThanAmount;
  
  /**
   * if there are fewer than this many entities to process, just resolve them
   */
  private int refreshEntityLinkIfLessThanAmount;
  
  /**
   * if there are fewer than this many groups to process, just resolve them
   * @return
   */
  public int getRefreshGroupLinkIfLessThanAmount() {
    return refreshGroupLinkIfLessThanAmount;
  }



  /**
   * if there are fewer than this many groups to process, just resolve them
   * @param refreshGroupLinkIfLessThanAmount
   */
  public void setRefreshGroupLinkIfLessThanAmount(int refreshGroupLinkIfLessThanAmount) {
    this.refreshGroupLinkIfLessThanAmount = refreshGroupLinkIfLessThanAmount;
  }



  /**
   * if there are fewer than this many entities to process, just resolve them
   * @return
   */
  public int getRefreshEntityLinkIfLessThanAmount() {
    return refreshEntityLinkIfLessThanAmount;
  }



  /**
   * if there are fewer than this many entities to process, just resolve them
   * @param refreshEntityLinkIfLessThanAmount
   */
  public void setRefreshEntityLinkIfLessThanAmount(int refreshEntityLinkIfLessThanAmount) {
    this.refreshEntityLinkIfLessThanAmount = refreshEntityLinkIfLessThanAmount;
  }



  /**
   * if there are fewer than this many subjects to process, just resolve them
   * @return
   */
  public int getRefreshSubjectLinkIfLessThanAmount() {
    return refreshSubjectLinkIfLessThanAmount;
  }

  /**
   * if there are fewer than this many subjects to process, just resolve them
   * @param refreshSubjectLinkIfLessThanAmount
   */
  public void setRefreshSubjectLinkIfLessThanAmount(
      int refreshSubjectLinkIfLessThanAmount) {
    this.refreshSubjectLinkIfLessThanAmount = refreshSubjectLinkIfLessThanAmount;
  }
  
  /**
   * if entities should be inserted in target 
   */
  private boolean insertEntities = false;

  /**
   * if memberships should be replaced in target
   */
  private boolean replaceMemberships = false;
  
  /**
   * if memberships should be replaced in target
   * @return
   */
  public boolean isReplaceMemberships() {
    return replaceMemberships;
  }
  
  /**
   * if memberships should be replaced in target
   * @return
   */
  public void setReplaceMemberships(boolean replaceMemberships) {
    this.replaceMemberships = replaceMemberships;
  }

  /**
   * if memberships should be inserted in target
   * @return
   */
  public boolean isInsertMemberships() {
    return insertMemberships;
  }

  /**
   * if memberships should be inserted in target
   * @param insertMemberships
   */
  public void setInsertMemberships(boolean insertMemberships) {
    this.insertMemberships = insertMemberships;
  }

  /**
   * if memberships should be deleted in target
   * @return
   */
  public boolean isDeleteMemberships() {
    return deleteMemberships;
  }

  /**
   * if memberships should be deleted in target
   * @param deleteMemberships
   */
  public void setDeleteMemberships(boolean deleteMemberships) {
    this.deleteMemberships = deleteMemberships;
  }

  /**
   * update groups
   */
  private boolean updateGroups = true;

  /**
   * update entities
   */
  private boolean updateEntities = false;

  /**
   * update groups
   * @return
   */
  public boolean isUpdateGroups() {
    return updateGroups;
  }

  /**
   * update groups
   * @param updateGroups
   */
  public void setUpdateGroups(boolean updateGroups) {
    this.updateGroups = updateGroups;
  }

  /**
   * update entities
   * @return
   */
  public boolean isUpdateEntities() {
    return updateEntities;
  }

  /**
   * update entities
   * @param updateEntities
   */
  public void setUpdateEntities(boolean updateEntities) {
    this.updateEntities = updateEntities;
  }

  /**
   * delete groups
   */
  private boolean deleteGroups = true;

  /**
   * delete groups
   * @return
   */
  public boolean isDeleteGroups() {
    return deleteGroups;
  }

  /**
   * delete groups
   * @param deleteGroups
   */
  public void setDeleteGroups(boolean deleteGroups) {
    this.deleteGroups = deleteGroups;
  }

  /**
   * delete entities if grouper deleted them
   */
  private boolean deleteEntitiesIfGrouperDeleted = false;

  /**
   * delete entities if not exist in grouper
   */
  private boolean deleteEntitiesIfNotExistInGrouper = false;
  
  /**
   * delete entities
   */
  private boolean deleteEntities = false;

  /**
   * select entities
   */
  private boolean selectEntities = true;
  
  /**
   * should the provisioner select all entities from the target
   */
  private boolean selectAllEntities = true;
  
  /**
   * should the provisioner select all groups from the target
   */
  private boolean selectAllGroups = true;
  
  
  public boolean isSelectAllGroups() {
    return selectAllGroups;
  }

  public void setSelectAllGroups(boolean selectAllGroups) {
    this.selectAllGroups = selectAllGroups;
  }

  /**
   * delete entities if grouper deleted them
   * @return
   */
  public boolean isDeleteEntitiesIfGrouperDeleted() {
    return deleteEntitiesIfGrouperDeleted;
  }

  /**
   * delete entities if grouper deleted them
   * @param deleteEntitiesIfGrouperDeleted
   */
  public void setDeleteEntitiesIfGrouperDeleted(boolean deleteEntitiesIfGrouperDeleted) {
    this.deleteEntitiesIfGrouperDeleted = deleteEntitiesIfGrouperDeleted;
  }

  /**
   * delete entities if not exist in grouper
   * @return
   */
  public boolean isDeleteEntitiesIfNotExistInGrouper() {
    return deleteEntitiesIfNotExistInGrouper;
  }

  /**
   * delete entities if not exist in grouper
   * @param deleteEntitiesIfNotExistInGrouper
   */
  public void setDeleteEntitiesIfNotExistInGrouper(
      boolean deleteEntitiesIfNotExistInGrouper) {
    this.deleteEntitiesIfNotExistInGrouper = deleteEntitiesIfNotExistInGrouper;
  }

  /**
   * delete memberships if grouper deleted them
   * @return
   */
  public boolean isDeleteMembershipsIfGrouperDeleted() {
    return deleteMembershipsIfGrouperDeleted;
  }

  /**
   * delete memberships if grouper deleted them
   * @param deleteMembershipsIfGrouperDeleted
   */
  public void setDeleteMembershipsIfGrouperDeleted(
      boolean deleteMembershipsIfGrouperDeleted) {
    this.deleteMembershipsIfGrouperDeleted = deleteMembershipsIfGrouperDeleted;
  }

  /**
   * delete memberships if not exist in grouper
   * @return
   */
  public boolean isDeleteMembershipsIfNotExistInGrouper() {
    return deleteMembershipsIfNotExistInGrouper;
  }

  /**
   * delete memberships if not exist in grouper
   * @param deleteMembershipsIfNotExistInGrouper
   */
  public void setDeleteMembershipsIfNotExistInGrouper(
      boolean deleteMembershipsIfNotExistInGrouper) {
    this.deleteMembershipsIfNotExistInGrouper = deleteMembershipsIfNotExistInGrouper;
  }
  
  public boolean isDeleteMembershipsOnlyInTrackedGroups() {
    return deleteMembershipsOnlyInTrackedGroups;
  }

  
  public void setDeleteMembershipsOnlyInTrackedGroups(
      boolean deleteMembershipsOnlyInTrackedGroups) {
    this.deleteMembershipsOnlyInTrackedGroups = deleteMembershipsOnlyInTrackedGroups;
  }

  /**
   * delete entities
   * @return
   */
  public boolean isDeleteEntities() {
    return deleteEntities;
  }

  /**
   * delete entities
   * @param deleteEntities
   */
  public void setDeleteEntities(boolean deleteEntities) {
    this.deleteEntities = deleteEntities;
  }

  /**
   * select entities
   * @return
   */
  public boolean isSelectEntities() {
    return selectEntities;
  }

  /**
   * select entities
   * @param selectEntities
   */
  public void setSelectEntities(boolean selectEntities) {
    this.selectEntities = selectEntities;
  }
  
  /**
   * should the provisioner select all entities from the target
   * @return
   */
  public boolean isSelectAllEntities() {
    return selectAllEntities;
  }

  /**
   * should the provisioner select all entities from the target
   * @param selectAllEntities
   */
  public void setSelectAllEntities(boolean selectAllEntities) {
    this.selectAllEntities = selectAllEntities;
  }

  /**
   * select memberships
   * @return
   */
  public boolean isSelectMemberships() {
    return selectMemberships;
  }

  /**
   * select memberships
   * @param selectMemberships
   */
  public void setSelectMemberships(boolean selectMemberships) {
    this.selectMemberships = selectMemberships;
  }

  /**
   * if memberships should be inserted in target
   */
  private boolean insertMemberships = true;

  
  /**
   * if memberships should be deleted in target
   */
  private boolean deleteMemberships = true;

  /**
   * 
   */
  private boolean deleteMembershipsIfGrouperCreated = true;
  
  /**
   * delete memberships if grouper deleted them
   */
  private boolean deleteMembershipsIfGrouperDeleted = false;

  /**
   * delete memberships if not exist in grouper
   */
  private boolean deleteMembershipsIfNotExistInGrouper = false;
  
  /**
   * delete memberships only in tracked groups
   */
  private boolean deleteMembershipsOnlyInTrackedGroups = true;

  /**
   * select memberships
   */
  private boolean selectMemberships = true;


  /**
   * if groups should be inserted in target
   */
  private boolean insertGroups = true;
  
  /**
   * if groups should be selected from target
   */
  private boolean selectGroups = true;
  
  /**
   * if groups should be selected from target
   * @return
   */
  public boolean isSelectGroups() {
    return selectGroups;
  }

  /**
   * if groups should be selected from target
   * @param selectGroups
   */
  public void setSelectGroups(boolean selectGroups) {
    this.selectGroups = selectGroups;
  }

  /**
   * search filter to look up entity if cannot just use the matchingId
   */
  private String entitySearchFilter = null;
  
  /**
   * search filter to look up entity if cannot just use the matchingId
   * @return
   */
  public String getEntitySearchFilter() {
    return entitySearchFilter;
  }

  /**
   * search filter to look up entity if cannot just use the matchingId
   * @param userSearchFilter
   */
  public void setEntitySearchFilter(String userSearchFilter) {
    this.entitySearchFilter = userSearchFilter;
  }

  private String entitySearchAllFilter;

  /**
   * search filter to look up all entities
   * @return
   */
  public String getEntitySearchAllFilter() {
    return entitySearchAllFilter;
  }

  /**
   * search filter to look up all entities
   * @param userSearchAllFilter
   */
  public void setEntitySearchAllFilter(String userSearchAllFilter) {
    this.entitySearchAllFilter = userSearchAllFilter;
  }
  
  protected Class<? extends GrouperProvisioningConfigurationAttribute> grouperProvisioningConfigurationAttributeClass() {
    return GrouperProvisioningConfigurationAttribute.class;
  }  
  
  
  @Override
  public String toString() {
    
    StringBuilder result = new StringBuilder();
    
    GrouperProvisioningConfiguration provisionerConfiguration = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration();
    Set<String> fieldNames = GrouperUtil.fieldNames(provisionerConfiguration.getClass(), 
        GrouperProvisioningConfiguration.class, null, true, false, false);
    
    // assume configurations cache stuff in fields.  We can make this more flexible / customizable at some point
    fieldNames.remove("configId");
    fieldNames.remove("debugLog");
    fieldNames.remove("debugMap");
    fieldNames.remove("grouperProvisioner");
    fieldNames.remove("targetEntityAttributeNameToConfig");
    fieldNames.remove("targetEntityFieldNameToConfig");
    fieldNames.remove("targetGroupAttributeNameToConfig");
    fieldNames.remove("targetGroupFieldNameToConfig");
    fieldNames.remove("targetMembershipAttributeNameToConfig");
    fieldNames.remove("targetMembershipFieldNameToConfig");
    fieldNames.remove("grouperProvisioningToTargetTranslation");
    fieldNames.remove("metadataNameToMetadataItem");
    fieldNames.remove("entityMatchingAttributes");
    fieldNames.remove("groupMatchingAttributes");
    fieldNames.remove("entitySearchAttributes");
    fieldNames.remove("groupSearchAttributes");
    
    fieldNames = new TreeSet<String>(fieldNames);
    boolean firstField = true;
    for (String fieldName : fieldNames) {
      
      Object value = GrouperUtil.propertyValue(provisionerConfiguration, fieldName);
      if (!GrouperUtil.isBlank(value)) {
        
        if ((value instanceof Collection) && ((Collection)value).size() == 0) {
          continue;
        }
        if ((value instanceof Map) && ((Map)value).size() == 0) {
          continue;
        }
        if ((value.getClass().isArray()) && Array.getLength(value) == 0) {
          continue;
        }
        if (!firstField) {
          result.append(", ");
        }
        firstField = false;
        result.append(fieldName).append(" = '").append(GrouperUtil.toStringForLog(value, false)).append("'");
      }
    }
    if (GrouperUtil.length(this.groupMatchingAttributes) > 0) {
      result.append(", groupMatchingAttributes: ");
      boolean first = true;
      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : this.groupMatchingAttributes) {
        if (!first) {
          result.append(", ");
        }
        first = false;
        result.append(grouperProvisioningConfigurationAttribute.getName());
      }
    }
    if (GrouperUtil.length(this.groupSearchAttributes) > 0) {
      result.append(", groupSearchAttributes: ");
      boolean first = true;
      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : this.groupSearchAttributes) {
        if (!first) {
          result.append(", ");
        }
        first = false;
        result.append(grouperProvisioningConfigurationAttribute.getName());
      }
    }
    if (GrouperUtil.length(this.entityMatchingAttributes) > 0) {
      result.append(", entityMatchingAttributes: ");
      boolean first = true;
      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : this.entityMatchingAttributes) {
        if (!first) {
          result.append(", ");
        }
        first = false;
        result.append(grouperProvisioningConfigurationAttribute.getName());
      }
    }
    if (GrouperUtil.length(this.entitySearchAttributes) > 0) {
      result.append(", entitySearchAttributes: ");
      boolean first = true;
      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : this.entitySearchAttributes) {
        if (!first) {
          result.append(", ");
        }
        first = false;
        result.append(grouperProvisioningConfigurationAttribute.getName());
      }
    }
    for (String key : new TreeSet<String>(this.metadataNameToMetadataItem.keySet())) {
      if (result.charAt(result.length()-1) != '\n') {
        result.append("\n");
      }
      GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem = this.metadataNameToMetadataItem.get(key);
      result.append(" - metadata item: " + key + ": " + grouperProvisioningObjectMetadataItem.toString());
    }
    for (String key : new TreeSet<String>(this.grouperProvisioningToTargetTranslation.keySet())) {
      List<String> translations = this.grouperProvisioningToTargetTranslation.get(key);
      for (int i=0;i<translations.size();i++) {
        if (result.charAt(result.length()-1) != '\n') {
          result.append("\n");
        }
        result.append(" - grouperProvisioningToTargetTranslation").append(key).append(".").append(i).append(".script = '").append(translations.get(i)).append("'");
      }
    }
    for (String key : new TreeSet<String>(this.targetGroupAttributeNameToConfig.keySet())) {
      if (result.charAt(result.length()-1) != '\n') {
        result.append("\n");
      }
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.targetGroupAttributeNameToConfig.get(key);
      result.append(" - target group attribute config: " + key + ": " + grouperProvisioningConfigurationAttribute.toString());
    }
    for (String key : new TreeSet<String>(this.targetEntityAttributeNameToConfig.keySet())) {
      if (result.charAt(result.length()-1) != '\n') {
        result.append("\n");
      }
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.targetEntityAttributeNameToConfig.get(key);
      result.append(" - target entity attribute config: " + key + ": " + grouperProvisioningConfigurationAttribute.toString());
    }
    for (String key : new TreeSet<String>(this.targetMembershipAttributeNameToConfig.keySet())) {
      if (result.charAt(result.length()-1) != '\n') {
        result.append("\n");
      }
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.targetMembershipAttributeNameToConfig.get(key);
      result.append(" - target membership attribute config: " + key + ": " + grouperProvisioningConfigurationAttribute.toString());
    }
    
    return result.toString();
  }

  /**
   * search filter to look up group if cannot just use the matchingId
   */
  private String groupSearchFilter = null;
  
  
  
  /**
   * search filter to look up group if cannot just use the matchingId
   * @return
   */
  public String getGroupSearchFilter() {
    return groupSearchFilter;
  }



  /**
   * search filter to look up group if cannot just use the matchingId
   * @param groupSearchFilter
   */
  public void setGroupSearchFilter(String groupSearchFilter) {
    this.groupSearchFilter = groupSearchFilter;
  }
  
  private String groupSearchAllFilter;

  /**
   * search filter to look up all groups
   * @return
   */
  public String getGroupSearchAllFilter() {
    return groupSearchAllFilter;
  }

  /**
   * search filter to look up all groups
   * @param groupSearchAllFilter
   */
  public void setGroupSearchAllFilter(String groupSearchAllFilter) {
    this.groupSearchAllFilter = groupSearchAllFilter;
  }
  
  /**
   * 
   */
  private boolean deleteEntitiesIfGrouperCreated = false;
  
  /**
   * 
   */
  private boolean deleteGroupsIfGrouperCreated = true;

  /**
   * 
   * @return
   */
  public boolean isDeleteEntitiesIfGrouperCreated() {
    return deleteEntitiesIfGrouperCreated;
  }

  /**
   * 
   * @param deleteEntitiesIfGrouperCreated
   */
  public void setDeleteEntitiesIfGrouperCreated(boolean deleteEntitiesIfGrouperCreated) {
    this.deleteEntitiesIfGrouperCreated = deleteEntitiesIfGrouperCreated;
  }

  /**
   * 
   * @return
   */
  public boolean isDeleteGroupsIfGrouperCreated() {
    return deleteGroupsIfGrouperCreated;
  }

  
  public void setDeleteGroupsIfGrouperCreated(boolean deleteGroupsIfGrouperCreated) {
    this.deleteGroupsIfGrouperCreated = deleteGroupsIfGrouperCreated;
  }

  
  public boolean isDeleteMembershipsIfGrouperCreated() {
    return deleteMembershipsIfGrouperCreated;
  }

  
  public void setDeleteMembershipsIfGrouperCreated(
      boolean deleteMembershipsIfGrouperCreated) {
    this.deleteMembershipsIfGrouperCreated = deleteMembershipsIfGrouperCreated;
  }

  /**
   * true or false if groups in full sync should be deleted if in group all filter and not in grouper
   * or for attributes delete other attribute not provisioned by grouper default to false
   */
  private boolean deleteGroupsIfNotExistInGrouper = false;

  /**
   * true or false if groups that were created in grouper were deleted should it be deleted in ldap?
   * or for attributes, delete attribute value if deleted in grouper default to true
   */
  private boolean deleteGroupsIfGrouperDeleted = false;

  /**
   * if provisioning normal memberships or privileges  default to "members" for normal memberships, otherwise which privileges
   */
  private GrouperProvisioningMembershipFieldType grouperProvisioningMembershipFieldType = null;

  /**
   * attribute name in a group/entity object that refers to memberships (if applicable)
   */
  private String attributeNameForMemberships;

  /**
   * attribute name in a group/entity object that refers to memberships (if applicable)
   * @return
   */
  public String getAttributeNameForMemberships() {
    return attributeNameForMemberships;
  }

  /**
   * attribute name in a group/entity object that refers to memberships (if applicable)
   * @param attributeNameForMemberships
   */
  public void setAttributeNameForMemberships(String attributeNameForMemberships) {
    this.attributeNameForMemberships = attributeNameForMemberships;
  }

  /**
   * 
   */
  public abstract void configureSpecificSettings();

  private GrouperProvisioningBehaviorMembershipType grouperProvisioningBehaviorMembershipType;

  /**
   * number of metadata
   */
  private int numberOfMetadata;

  private String groupMembershipAttributeName;

  public String getGroupMembershipAttributeName() {
    return groupMembershipAttributeName;
  }

  public String getGroupMembershipAttributeValue() {
    return groupMembershipAttributeValue;
  }

  private String groupMembershipAttributeValue;

  /**
   * attribute name in a user object that refers to memberships (if applicable)
   */
  private String entityMembershipAttributeName;

  public String getEntityMembershipAttributeName() {
    return entityMembershipAttributeName;
  }

  public String getEntityMembershipAttributeValue() {
    return entityMembershipAttributeValue;
  }

  private String entityMembershipAttributeValue;  
  
  
  /**
   * number of metadata
   * @return
   */
  public int getNumberOfMetadata() {
    return numberOfMetadata;
  }

  /**
   * number of metadata
   * @param numberOfMetadata
   */
  public void setNumberOfMetadata(int numberOfMetadata) {
    this.numberOfMetadata = numberOfMetadata;
  }

  public GrouperProvisioningBehaviorMembershipType getGrouperProvisioningBehaviorMembershipType() {
    return grouperProvisioningBehaviorMembershipType;
  }

  
  public void setGrouperProvisioningBehaviorMembershipType(
      GrouperProvisioningBehaviorMembershipType grouperProvisioningBehaviorMembershipType) {
    this.grouperProvisioningBehaviorMembershipType = grouperProvisioningBehaviorMembershipType;
  }
  
  public void configureProvisionableSettings() {
    if (this.getConfigId() == null) {
      this.setConfigId(this.grouperProvisioner.getConfigId());
    }
    
    this.onlyProvisionPolicyGroups = this.retrieveConfigBoolean("onlyProvisionPolicyGroups", false);

    this.allowPolicyGroupOverride = this.retrieveConfigBoolean("allowPolicyGroupOverride", false);

    this.allowProvisionableRegexOverride = this.retrieveConfigBoolean("allowProvisionableRegexOverride", false);

    this.provisionableRegex = this.retrieveConfigString("provisionableRegex", false);
  }

  public void configureGenericSettings() {
    configureProvisionableSettings();
    
    {
      Boolean operateOnGrouperMemberships = this.retrieveConfigBoolean("operateOnGrouperMemberships", false);
      if (operateOnGrouperMemberships == null) {
        operateOnGrouperMemberships = false;
      }
      
      if (operateOnGrouperMemberships) {
        String provisioningTypeString = this.retrieveConfigString("provisioningType", true);
        this.grouperProvisioningBehaviorMembershipType = GrouperProvisioningBehaviorMembershipType.valueOf(provisioningTypeString);
      }
      
    }

    this.numberOfMetadata = GrouperUtil.intValue(this.retrieveConfigInt("numberOfMetadata", false), 0);
    
    for (int i=0;i<this.numberOfMetadata;i++) {
      
      GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem = new GrouperProvisioningObjectMetadataItem();
      {
        String name = this.retrieveConfigString("metadata."+i+".name", false);
        if (!name.startsWith("md_")) {
          //TODO validate this
          this.debugMap.put("invalid_metadataName_" + name, true);
          continue;
        }
        grouperProvisioningObjectMetadataItem.setName(name);
        if (this.metadataNameToMetadataItem.containsKey(name)) {
          throw new RuntimeException("Conflicting metadata names! " + name);
        }
        this.metadataNameToMetadataItem.put(name, grouperProvisioningObjectMetadataItem);
      }
      
      grouperProvisioningObjectMetadataItem.setLabelKey(grouperProvisioningObjectMetadataItem.getName() + "_" + this.getGrouperProvisioner().getConfigId() + "_label");
      grouperProvisioningObjectMetadataItem.setDescriptionKey(grouperProvisioningObjectMetadataItem.getName() + "_" + this.getGrouperProvisioner().getConfigId() + "_description");
      
      {
        boolean showForFolder = GrouperUtil.booleanValue(this.retrieveConfigBoolean("metadata."+i+".showForFolder", false), false);
        grouperProvisioningObjectMetadataItem.setShowForFolder(showForFolder);
      }
      
      {
        boolean showForGroup = GrouperUtil.booleanValue(this.retrieveConfigBoolean("metadata."+i+".showForGroup", false), false);
        grouperProvisioningObjectMetadataItem.setShowForGroup(showForGroup);
      }
      
      {
        boolean showForMember = GrouperUtil.booleanValue(this.retrieveConfigBoolean("metadata."+i+".showForMember", false), false);
        grouperProvisioningObjectMetadataItem.setShowForMember(showForMember);
      }
      
      {
        boolean showForMembership = GrouperUtil.booleanValue(this.retrieveConfigBoolean("metadata."+i+".showForMembership", false), false);
        grouperProvisioningObjectMetadataItem.setShowForMembership(showForMembership);
      }
      
      {
        boolean canChange = GrouperUtil.booleanValue(this.retrieveConfigBoolean("metadata."+i+".canChange", false), true);
        grouperProvisioningObjectMetadataItem.setCanChange(canChange);
      }

      {
        boolean canUpdate = GrouperUtil.booleanValue(this.retrieveConfigBoolean("metadata."+i+".canUpdate", false), true);
        grouperProvisioningObjectMetadataItem.setCanUpdate(canUpdate);
      }
      
      {
        String valueType = this.retrieveConfigString("metadata."+i+".valueType", false);
        GrouperProvisioningObjectMetadataItemValueType grouperProvisioningObjectMetadataItemValueType = 
            StringUtils.isBlank(valueType) ? GrouperProvisioningObjectMetadataItemValueType.STRING 
                : GrouperProvisioningObjectMetadataItemValueType.valueOfIgnoreCase(valueType, true);
        grouperProvisioningObjectMetadataItem.setValueType(grouperProvisioningObjectMetadataItemValueType);
        
        if (grouperProvisioningObjectMetadataItemValueType == GrouperProvisioningObjectMetadataItemValueType.BOOLEAN) {
          grouperProvisioningObjectMetadataItem.setFormElementType(GrouperProvisioningObjectMetadataItemFormElementType.RADIOBUTTON);
         
          String trueLabel = GrouperTextContainer.textOrNull("config.defaultTrueLabel");
          String falseLabel = GrouperTextContainer.textOrNull("config.defaultFalseLabel");
          
          String defaultValue = this.retrieveConfigString("metadata."+i+".defaultValue", false);
          
          List<MultiKey> keysAndLabels = new ArrayList<MultiKey>();
          
          if (StringUtils.isNotBlank(defaultValue)) {
            
            Boolean booleanObjectValue = GrouperUtil.booleanObjectValue(defaultValue);
            if (booleanObjectValue != null) {
              String defaultValueStr = booleanObjectValue ? "("+trueLabel+")" : "("+falseLabel+")"; 
              keysAndLabels.add(new MultiKey("", GrouperTextContainer.textOrNull("config.defaultValueLabel")+" " + defaultValueStr ));
            }
          }
          
          keysAndLabels.add(new MultiKey("true", trueLabel));
          keysAndLabels.add(new MultiKey("false", falseLabel));
          grouperProvisioningObjectMetadataItem.setKeysAndLabelsForDropdown(keysAndLabels);
        } else {
          String formElementType = this.retrieveConfigString("metadata."+i+".formElementType", false);
          GrouperProvisioningObjectMetadataItemFormElementType grouperProvisioningObjectMetadataItemFormElementType =
              StringUtils.isBlank(formElementType) ? GrouperProvisioningObjectMetadataItemFormElementType.TEXT 
                  : GrouperProvisioningObjectMetadataItemFormElementType.valueOfIgnoreCase(formElementType, true);
          grouperProvisioningObjectMetadataItem.setFormElementType(grouperProvisioningObjectMetadataItemFormElementType);
        }
      }
      
      {
        String defaultValue = this.retrieveConfigString("metadata."+i+".defaultValue", false);
        grouperProvisioningObjectMetadataItem.setDefaultValue(defaultValue);
      }
      
      {
        String dropdownValues = this.retrieveConfigString("metadata."+i+".dropdownValues", false);
        if (!StringUtils.isBlank(dropdownValues)) {
          String[] dropdownValuesArray = GrouperUtil.splitTrim(dropdownValues, ",");
          List<MultiKey> keysAndLabels = new ArrayList<MultiKey>();
          keysAndLabels.add(new MultiKey("", ""));
          for (String dropdownValue : dropdownValuesArray) {
            dropdownValue = GrouperUtil.replace(dropdownValue, "&#x2c;", ",");
            MultiKey keyAndLabel = new MultiKey(dropdownValue, dropdownValue);
            keysAndLabels.add(keyAndLabel);
          }
          grouperProvisioningObjectMetadataItem.setKeysAndLabelsForDropdown(keysAndLabels);
        }
      }
      
      {
        boolean required = GrouperUtil.booleanValue(this.retrieveConfigBoolean("metadata."+i+".required", false), false);
        grouperProvisioningObjectMetadataItem.setRequired(required);
      }

      {
        String groupIdThatCanView = this.retrieveConfigString("metadata."+i+".groupIdThatCanView", false);
        grouperProvisioningObjectMetadataItem.setGroupIdThatCanView(groupIdThatCanView);
      }
      {
        String groupIdThatCanUpdate = this.retrieveConfigString("metadata."+i+".groupIdThatCanUpdate", false);
        grouperProvisioningObjectMetadataItem.setGroupIdThatCanUpdate(groupIdThatCanUpdate);
      }
      
    }
    
    this.groupMembershipAttributeName = this.retrieveConfigString("groupMembershipAttributeName", false);
    this.groupMembershipAttributeValue = this.retrieveConfigString("groupMembershipAttributeValue", false);
    
    this.entityMembershipAttributeName = this.retrieveConfigString("entityMembershipAttributeName", false);
    this.entityMembershipAttributeValue = this.retrieveConfigString("entityMembershipAttributeValue", false);
    

    for (String objectType: new String[] {"targetGroupAttribute", "targetEntityAttribute", "targetMembershipAttribute"}) {
      
      boolean foundMatchingId = false;
      String foundMatchingIdName = null;
      
      boolean foundMembershipAttribute = false;
      String foundMembershipAttributeName = null;
      
      for (int i=0; i< 20; i++) {
  
        
        GrouperProvisioningConfigurationAttribute attributeConfig = GrouperUtil.newInstance(this.grouperProvisioningConfigurationAttributeClass());
        
        attributeConfig.setGrouperProvisioner(grouperProvisioner);
        attributeConfig.setConfigIndex(i);
        
        if (StringUtils.equals("targetGroupAttribute", objectType)) {
          attributeConfig.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.group);
        } else if (StringUtils.equals("targetEntityAttribute", objectType)) {
          attributeConfig.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.entity);
        } else if (StringUtils.equals("targetMembershipAttribute", objectType)) {
          attributeConfig.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.membership);
        } else {
          throw new RuntimeException("Cant find object type: " + objectType);
        }

        String name = this.retrieveConfigString(objectType + "."+i+ ".name" , false);
        if (StringUtils.isBlank(name)) {
          break;
        }
        attributeConfig.setName(name);
        
        {
          boolean showAttributeValidation = GrouperUtil.booleanValue(this.retrieveConfigBoolean(objectType + "."+i+".showAttributeValidation" , false), false);
          if (showAttributeValidation) {
            {
              Integer maxlength = this.retrieveConfigInt(objectType + "."+i+".maxlength", false);
              attributeConfig.setMaxlength(maxlength);
            }
            
            {
              String validExpression = this.retrieveConfigString(objectType + "."+i+".validExpression", false);
              attributeConfig.setValidExpression(validExpression);
            }
            
            {
              boolean required = GrouperUtil.booleanValue(this.retrieveConfigBoolean(objectType + "."+i+".required" , false), false);
              attributeConfig.setRequired(required);
            }
            
          }
        }
  
        {
          String translateExpressionType = this.retrieveConfigString(objectType + "."+i+".translateExpressionType" , false);
          attributeConfig.setTranslateExpressionType(GrouperProvisioningConfigurationAttributeTranslationType.valueOfIgnoreCase(translateExpressionType, false));
        }
        
        {
          String translateExpressionType = this.retrieveConfigString(objectType + "."+i+".translateExpressionTypeCreateOnly" , false);
          attributeConfig.setTranslateExpressionTypeCreateOnly(GrouperProvisioningConfigurationAttributeTranslationType.valueOfIgnoreCase(translateExpressionType, false));
        }
        
        {
          String translateExpression = this.retrieveConfigString(objectType + "."+i+".translateExpression" , false);
          attributeConfig.setTranslateExpression(translateExpression);
        }
        
        {
          boolean nullChecksInScript = GrouperUtil.booleanValue(this.retrieveConfigBoolean(objectType + "."+i+".nullChecksInScript" , false), false);
          attributeConfig.setCheckForNullsInScript(nullChecksInScript);
        }
        
        {
          String translationContinueCondition = this.retrieveConfigString(objectType + "."+i+".translationContinueCondition" , false);
          attributeConfig.setTranslationContinueCondition(translationContinueCondition);
        }
        
        {
          String translateExpressionCreateOnly = this.retrieveConfigString(objectType+"."+i+".translateExpressionCreateOnly" , false);
          attributeConfig.setTranslateExpressionCreateOnly(translateExpressionCreateOnly);
        }
        
        {
          String translateFromStaticValues = this.retrieveConfigString(objectType + "."+i+".translateFromStaticValues" , false);
          attributeConfig.setTranslateFromStaticValues(translateFromStaticValues);
        }
        
        {
          String translateFromStaticValuesCreateOnly = this.retrieveConfigString(objectType+"."+i+".translateFromStaticValuesCreateOnly" , false);
          attributeConfig.setTranslateFromStaticValuesCreateOnly(translateFromStaticValuesCreateOnly);
        }
        
        {
          String translateFromGrouperProvisioningGroupField = this.retrieveConfigString(objectType+"."+i+".translateFromGrouperProvisioningGroupField" , false);
          attributeConfig.setTranslateFromGrouperProvisioningGroupField(translateFromGrouperProvisioningGroupField);
        }
        
        {
          String translateFromGrouperProvisioningEntityField = this.retrieveConfigString(objectType+"."+i+".translateFromGrouperProvisioningEntityField" , false);
          attributeConfig.setTranslateFromGrouperProvisioningEntityField(translateFromGrouperProvisioningEntityField);
        }
        
        {
          String translateFromGrouperTargetGroupField = this.retrieveConfigString(objectType+"."+i+".translateFromGrouperTargetGroupField" , false);
          attributeConfig.setTranslateFromGrouperTargetGroupField(translateFromGrouperTargetGroupField);
        }
        
        {
          String translateFromGrouperTargetEntityField = this.retrieveConfigString(objectType+"."+i+".translateFromGrouperTargetEntityField" , false);
          attributeConfig.setTranslateFromGrouperTargetEntityField(translateFromGrouperTargetEntityField);
        }
        
        {
          String translateFromGrouperProvisioningGroupFieldCreateOnly = this.retrieveConfigString(objectType+"."+i+".translateFromGrouperProvisioningGroupFieldCreateOnly" , false);
          attributeConfig.setTranslateFromGrouperProvisioningGroupFieldCreateOnly(translateFromGrouperProvisioningGroupFieldCreateOnly);
        }
        
        {
          String translateFromGrouperProvisioningEntityFieldCreateOnly = this.retrieveConfigString(objectType+"."+i+".translateFromGrouperProvisioningEntityFieldCreateOnly" , false);
          attributeConfig.setTranslateFromGrouperProvisioningEntityFieldCreateOnly(translateFromGrouperProvisioningEntityFieldCreateOnly);
        }
        
        {
          boolean showAttributeCrud = GrouperUtil.booleanValue(this.retrieveConfigBoolean(objectType + "."+i+".showAttributeCrud" , false), false);
          if (showAttributeCrud) {

            {
              boolean insert = GrouperUtil.booleanValue(this.retrieveConfigBoolean(objectType + "."+i+".insert" , false), true);
              attributeConfig.setInsert(insert);
            }
    
            {
              boolean update = GrouperUtil.booleanValue(this.retrieveConfigBoolean(objectType+"."+i+".update" , false), true);
              attributeConfig.setUpdate(update);
            }
            {
              boolean select = GrouperUtil.booleanValue(this.retrieveConfigBoolean(objectType + "."+i+".select" , false), true);
              attributeConfig.setSelect(select);
            }
          }
        }

        {
          boolean showAttributeValueSettings = GrouperUtil.booleanValue(this.retrieveConfigBoolean(objectType + "."+i+".showAttributeValueSettings" , false), false);
          Boolean multiValued = null;
          if (showAttributeValueSettings) {
            multiValued = this.retrieveConfigBoolean(objectType + "."+i+".multiValued" , false);
          }
          // default multivalued to true for membership attribute if nothing set already
          if (multiValued == null) {
            if (StringUtils.equals(objectType, "targetGroupAttribute") && !StringUtils.isBlank(this.groupMembershipAttributeName)
                && StringUtils.equals(this.groupMembershipAttributeName, name)) {
              multiValued = true;
            }
            if (StringUtils.equals(objectType, "targetEntityAttribute") && !StringUtils.isBlank(this.entityMembershipAttributeName)
                && StringUtils.equals(this.entityMembershipAttributeName, name)) {
              multiValued = true;
            }
          }
          attributeConfig.setMultiValued(multiValued == null ? false: multiValued);
          
          if (showAttributeValueSettings) {
            {
              String defaultValue = this.retrieveConfigString(objectType + "."+i+".defaultValue" , false);
              attributeConfig.setDefaultValue(defaultValue);
            }
            
    
            {
              GrouperProvisioningConfigurationAttributeValueType valueType = 
                  GrouperProvisioningConfigurationAttributeValueType.valueOfIgnoreCase(
                      this.retrieveConfigString(objectType+ "."+i+".valueType" , false), false);
              if (valueType == null) {
                valueType = GrouperProvisioningConfigurationAttributeValueType.STRING;
              }
              attributeConfig.setValueType(valueType);
            }
            
            {
              String ignoreIfMatchesValuesRaw = this.retrieveConfigString(objectType + "."+i+".ignoreIfMatchesValue" , false);
              if (!StringUtils.isBlank(ignoreIfMatchesValuesRaw)) {
                GrouperProvisioningConfigurationAttributeValueType valueType = GrouperUtil.defaultIfNull(attributeConfig.getValueType(), 
                    GrouperProvisioningConfigurationAttributeValueType.STRING);
                
                for (String ignoreIfMatchesValueRaw : GrouperUtil.splitTrim(ignoreIfMatchesValuesRaw, ",")) {
                  ignoreIfMatchesValueRaw = StringUtils.replace(ignoreIfMatchesValueRaw, "U+002C", ",");
                  Object ignoreIfMatchesValue = valueType.convert(ignoreIfMatchesValueRaw);
                  attributeConfig.getIgnoreIfMatchesValues().add(ignoreIfMatchesValue);
                }
              }
            }
            
            {
             
              Boolean caseSensitiveCompare = GrouperUtil.booleanValue(this.retrieveConfigBoolean(objectType + "."+i+".caseSensitiveCompare" , false), true);
              attributeConfig.setCaseSensitiveCompare(caseSensitiveCompare);
            }
            
          }
        }

        if ("targetGroupAttribute".equals(objectType)) {
          if (targetGroupAttributeNameToConfig.containsKey(name)) {
            throw new RuntimeException("Multiple configurations for " + objectType + " attribute: " + name);
          }
        
          targetGroupAttributeNameToConfig.put(name, attributeConfig);
          
        } else if ("targetEntityAttribute".equals(objectType)) {
          if (targetEntityAttributeNameToConfig.containsKey(name)) {
            throw new RuntimeException("Multiple configurations for " + objectType + " attribute: " + name);
          }
          targetEntityAttributeNameToConfig.put(name, attributeConfig);
          
        } else if ("targetMembershipAttribute".equals(objectType)) {
          if (targetMembershipAttributeNameToConfig.containsKey(name)) {
            throw new RuntimeException("Multiple configurations for " + objectType + " attribute: " + name);
          }
          targetMembershipAttributeNameToConfig.put(name, attributeConfig);
          
        } else {
          throw new RuntimeException("Invalid object type: '" + objectType + "'");
        }
      }
    }
    
    this.hasTargetGroupLink = GrouperUtil.defaultIfNull(this.retrieveConfigBoolean("hasTargetGroupLink", false), false);
    if (this.hasTargetGroupLink) {
      this.debugMap.put("hasTargetGroupLink", this.hasTargetGroupLink);
    }

    this.hasTargetEntityLink = GrouperUtil.defaultIfNull(this.retrieveConfigBoolean("hasTargetEntityLink", false), false);
    if (this.hasTargetEntityLink) {
      this.debugMap.put("hasTargetEntityLink", this.hasTargetEntityLink);
    }
    
    if (GrouperUtil.defaultIfNull(this.retrieveConfigBoolean("group2advanced", false), false)) {
      this.groupsRequireMembers = GrouperUtil.defaultIfNull(this.retrieveConfigBoolean("groupsRequireMembers", false), false);
    }
    
    this.groupSearchFilter = this.retrieveConfigString("groupSearchFilter", false);

    this.membershipMatchingIdExpression = this.retrieveConfigString("membershipMatchingIdExpression", false);

    this.unresolvableSubjectsInsert = GrouperUtil.defaultIfNull(this.retrieveConfigBoolean("unresolvableSubjectsInsert", false), false);

    this.unresolvableSubjectsRemove = GrouperUtil.defaultIfNull(this.retrieveConfigBoolean("unresolvableSubjectsRemove", false), false);

    this.logAllObjectsVerbose = GrouperUtil.defaultIfNull(this.retrieveConfigBoolean("logAllObjectsVerbose", false), false);

    this.logAllObjectsVerboseToDaemonDbLog = GrouperUtil.defaultIfNull(this.retrieveConfigBoolean("logAllObjectsVerboseToDaemonDbLog", false), true);

    this.logAllObjectsVerboseToLogFile = GrouperUtil.defaultIfNull(this.retrieveConfigBoolean("logAllObjectsVerboseToLogFile", false), true);

    this.logCommandsAlways = GrouperUtil.defaultIfNull(this.retrieveConfigBoolean("logCommandsAlways", false), false);
    
    this.logCommandsOnError = GrouperUtil.defaultIfNull(this.retrieveConfigBoolean("logCommandsOnError", false), false);
    
    this.logMaxErrorsPerType = GrouperUtil.intValue(this.retrieveConfigInt("logMaxErrorsPerType", false), 10);
    
    this.debugLog = GrouperUtil.defaultIfNull(this.retrieveConfigBoolean("debugLog", false), false);
    
    this.operateOnGrouperEntities = GrouperUtil.booleanValue(this.retrieveConfigBoolean("operateOnGrouperEntities", false), false);
    this.operateOnGrouperMemberships = GrouperUtil.booleanValue(this.retrieveConfigBoolean("operateOnGrouperMemberships", false), false);
    this.operateOnGrouperGroups = GrouperUtil.booleanValue(this.retrieveConfigBoolean("operateOnGrouperGroups", false), false);
    
    this.subjectSourcesToProvision = GrouperUtil.nonNull(GrouperUtil.splitTrimToSet(this.retrieveConfigString("subjectSourcesToProvision", false), ","));

    for (String sourceId : this.subjectSourcesToProvision) {
      if (null == SourceManager.getInstance().getSource(sourceId)) {
        throw new RuntimeException("Cant find source: '" + sourceId + "'");
      }
    }
    this.debugMap.put("subjectSourcesToProvision", GrouperUtil.join(this.subjectSourcesToProvision.iterator(), ','));

    this.groupAttributeValueCacheHas = GrouperUtil.booleanValue(this.retrieveConfigBoolean("groupAttributeValueCacheHas", false), false);
    if (this.groupAttributeValueCacheHas) {
      this.groupAttributeDbCaches = new GrouperProvisioningConfigurationAttributeDbCache[4];

      for (int i=0;i<4;i++) {
        boolean theGroupAttributeValueCacheHas = GrouperUtil.booleanValue(this.retrieveConfigBoolean("groupAttributeValueCache" + i + "has", false), false);
        if (!theGroupAttributeValueCacheHas) {
          continue;
        }
        this.groupAttributeDbCaches[i] = new GrouperProvisioningConfigurationAttributeDbCache(this.grouperProvisioner, i, "group");
        String theGroupAttributeValueCache0source = this.retrieveConfigString("groupAttributeValueCache" + i + "source", true);
        this.groupAttributeDbCaches[i].setSource(
            GrouperProvisioningConfigurationAttributeDbCacheSource.valueOfIgnoreCase(theGroupAttributeValueCache0source, true));

        String theGroupAttributeValueCache0type = this.retrieveConfigString("groupAttributeValueCache" + i + "type", true);
        this.groupAttributeDbCaches[i].setType(
            GrouperProvisioningConfigurationAttributeDbCacheType.valueOfIgnoreCase(theGroupAttributeValueCache0type, true));

        if (this.groupAttributeDbCaches[i].getType() == GrouperProvisioningConfigurationAttributeDbCacheType.attribute) {
          this.groupAttributeDbCaches[i].setAttributeName(this.retrieveConfigString("groupAttributeValueCache" + i + "groupAttribute", true));
        } else if (this.groupAttributeDbCaches[i].getType() == GrouperProvisioningConfigurationAttributeDbCacheType.translationScript) {
          this.groupAttributeDbCaches[i].setTranslationScript(this.retrieveConfigString("groupAttributeValueCache" + i + "translationScript", true));
        } else if (this.groupAttributeDbCaches[i].getType() == GrouperProvisioningConfigurationAttributeDbCacheType.object) {
          // nuthin
        } else {
          throw new RuntimeException("Invalid attribute cache type: " + "groupAttributeValueCache" + i + "type" + ", " 
              + this.groupAttributeDbCaches[i].getType());
        }
      }
    }
    
    this.entityAttributeValueCacheHas = GrouperUtil.booleanValue(this.retrieveConfigBoolean("entityAttributeValueCacheHas", false), false);
    if (this.entityAttributeValueCacheHas) {
      this.entityAttributeDbCaches = new GrouperProvisioningConfigurationAttributeDbCache[4];

      for (int i=0;i<4;i++) {
        boolean theEntityAttributeValueCacheHas = GrouperUtil.booleanValue(this.retrieveConfigBoolean("entityAttributeValueCache" + i + "has", false), false);
        if (!theEntityAttributeValueCacheHas) {
          continue;
        }
        this.entityAttributeDbCaches[i] = new GrouperProvisioningConfigurationAttributeDbCache(this.grouperProvisioner, i, "entity");
        String theEntityAttributeValueCache0source = this.retrieveConfigString("entityAttributeValueCache" + i + "source", true);
        this.entityAttributeDbCaches[i].setSource(
            GrouperProvisioningConfigurationAttributeDbCacheSource.valueOfIgnoreCase(theEntityAttributeValueCache0source, true));

        String theEntityAttributeValueCache0type = this.retrieveConfigString("entityAttributeValueCache" + i + "type", true);
        this.entityAttributeDbCaches[i].setType(
            GrouperProvisioningConfigurationAttributeDbCacheType.valueOfIgnoreCase(theEntityAttributeValueCache0type, true));

        if (this.entityAttributeDbCaches[i].getType() == GrouperProvisioningConfigurationAttributeDbCacheType.attribute) {
          this.entityAttributeDbCaches[i].setAttributeName(this.retrieveConfigString("entityAttributeValueCache" + i + "entityAttribute", true));
        } else if (this.entityAttributeDbCaches[i].getType() == GrouperProvisioningConfigurationAttributeDbCacheType.translationScript) {
          this.entityAttributeDbCaches[i].setTranslationScript(this.retrieveConfigString("entityAttributeValueCache" + i + "translationScript", true));
        } else if (this.entityAttributeDbCaches[i].getType() == GrouperProvisioningConfigurationAttributeDbCacheType.subjectTranslationScript) {
          this.entityAttributeDbCaches[i].setTranslationScript(this.retrieveConfigString("entityAttributeValueCache" + i + "translationScript", true));
        } else if (this.entityAttributeDbCaches[i].getType() == GrouperProvisioningConfigurationAttributeDbCacheType.object) {
          // nuthin
        } else {
          throw new RuntimeException("Invalid attribute cache type: " + "entityAttributeValueCache" + i + "type" + ", " 
              + this.entityAttributeDbCaches[i].getType());
        }
      }
    }
    
    this.refreshSubjectLinkIfLessThanAmount = GrouperUtil.intValue(this.retrieveConfigInt("refreshSubjectLinkIfLessThanAmount", false), 20);
    this.refreshGroupLinkIfLessThanAmount = GrouperUtil.intValue(this.retrieveConfigInt("refreshGroupLinkIfLessThanAmount", false), 20);
    this.refreshEntityLinkIfLessThanAmount = GrouperUtil.intValue(this.retrieveConfigInt("refreshEntityLinkIfLessThanAmount", false), 20);
    
    this.scoreConvertToFullSyncThreshold = GrouperUtil.intValue(this.retrieveConfigInt("scoreConvertToFullSyncThreshold", false), 10000);
    this.membershipsConvertToGroupSyncThreshold = GrouperUtil.intValue(this.retrieveConfigInt("membershipsConvertToGroupSyncThreshold", false), 500);
    
    this.entitySearchFilter = this.retrieveConfigString("userSearchFilter", false);
    this.entitySearchAllFilter = this.retrieveConfigString("userSearchAllFilter", false);
    this.groupSearchFilter = this.retrieveConfigString("groupSearchFilter", false);
    this.groupSearchAllFilter = this.retrieveConfigString("groupSearchAllFilter", false);
    
    this.customizeGroupCrud = GrouperUtil.booleanValue(this.retrieveConfigBoolean("customizeGroupCrud", false), false);
    
    if (!this.operateOnGrouperGroups) {
      this.insertGroups = false;
      
      this.deleteGroups = false;
  
      this.updateGroups = false;
  
      this.selectGroups = false;
  
      this.deleteGroupsIfNotExistInGrouper = false;
  
      this.deleteGroupsIfGrouperDeleted = false;
  
      this.deleteGroupsIfGrouperCreated = false;
    }
    
    if (this.customizeGroupCrud) {

      this.insertGroups = GrouperUtil.booleanValue(this.retrieveConfigBoolean("insertGroups", false), true);

      this.selectGroups = GrouperUtil.booleanValue(this.retrieveConfigBoolean("selectGroups", false), true);

      this.updateGroups = GrouperUtil.booleanValue(this.retrieveConfigBoolean("updateGroups", false), true);

      this.deleteGroups = GrouperUtil.booleanValue(this.retrieveConfigBoolean("deleteGroups", false), true);

      this.deleteGroupsIfNotExistInGrouper = GrouperUtil.booleanValue(this.retrieveConfigBoolean("deleteGroupsIfNotExistInGrouper", false), false);

      this.deleteGroupsIfGrouperDeleted = GrouperUtil.booleanValue(this.retrieveConfigBoolean("deleteGroupsIfGrouperDeleted", false), false);

      this.deleteGroupsIfGrouperCreated = GrouperUtil.booleanValue(this.retrieveConfigBoolean("deleteGroupsIfGrouperCreated", false), 
          (deleteGroups && !this.deleteGroupsIfNotExistInGrouper && !this.deleteGroupsIfGrouperDeleted));
    }

    
    if (!this.operateOnGrouperMemberships) {
      this.insertMemberships = false;
      
      this.deleteMemberships = false;
  
      this.selectMemberships = false;
  
      this.deleteMembershipsIfNotExistInGrouper = false;
  
      this.deleteMembershipsIfGrouperDeleted = false;
  
      this.deleteMembershipsIfGrouperCreated = false;
      
    }

    
    this.customizeMembershipCrud = GrouperUtil.booleanValue(this.retrieveConfigBoolean("customizeMembershipCrud", false), false);
    if (this.customizeMembershipCrud) {
      
      this.insertMemberships = GrouperUtil.booleanValue(this.retrieveConfigBoolean("insertMemberships", false), true);
      
      this.replaceMemberships = GrouperUtil.booleanValue(this.retrieveConfigBoolean("replaceMemberships", false), false);

      this.selectMemberships = GrouperUtil.booleanValue(this.retrieveConfigBoolean("selectMemberships", false), true);

      this.deleteMemberships = GrouperUtil.booleanValue(this.retrieveConfigBoolean("deleteMemberships", false), true);

      this.deleteMembershipsIfNotExistInGrouper = GrouperUtil.booleanValue(this.retrieveConfigBoolean("deleteMembershipsIfNotExistInGrouper", false), false);
      this.deleteMembershipsOnlyInTrackedGroups = GrouperUtil.booleanValue(this.retrieveConfigBoolean("deleteMembershipsOnlyInTrackedGroups", false), true);

      this.deleteMembershipsIfGrouperDeleted = GrouperUtil.booleanValue(this.retrieveConfigBoolean("deleteMembershipsIfGrouperDeleted", false), false);

      this.deleteMembershipsIfGrouperCreated = GrouperUtil.booleanValue(this.retrieveConfigBoolean("deleteMembershipsIfGrouperCreated", false), 
          (deleteMemberships && !this.deleteMembershipsIfNotExistInGrouper && !this.deleteMembershipsIfGrouperDeleted));
    }

    boolean errorHandlingShow = GrouperUtil.booleanValue(this.retrieveConfigBoolean("errorHandlingShow", false), false);

    if (errorHandlingShow) {
      
      this.errorHandlingLogErrors = GrouperUtil.booleanValue(this.retrieveConfigBoolean("errorHandlingLogErrors", false), true);
      this.errorHandlingLogCountPerType = GrouperUtil.intValue(this.retrieveConfigInt("errorHandlingLogCountPerType", false), 5);
      this.errorHandlingProvisionerDaemonShouldFailOnObjectError = GrouperUtil.booleanValue(this.retrieveConfigBoolean("errorHandlingProvisionerDaemonShouldFailOnObjectError", false), true);
      this.errorHandlingInvalidDataIsAnError = GrouperUtil.booleanValue(this.retrieveConfigBoolean("errorHandlingInvalidDataIsAnError", false), true);
      this.errorHandlingLengthValidationIsAnError = GrouperUtil.booleanValue(this.retrieveConfigBoolean("errorHandlingLengthValidationIsAnError", false), true);
      this.errorHandlingMatchingValidationIsAnError = GrouperUtil.booleanValue(this.retrieveConfigBoolean("errorHandlingMatchingValidationIsAnError", false), true);
      this.errorHandlingRequiredValidationIsAnError = GrouperUtil.booleanValue(this.retrieveConfigBoolean("errorHandlingRequiredValidationIsAnError", false), true);
      this.errorHandlingTargetObjectDoesNotExistIsAnError = GrouperUtil.booleanValue(this.retrieveConfigBoolean("errorHandlingTargetObjectDoesNotExistIsAnError", false), true);
      
      this.errorHandlingPercentLevel1 = GrouperUtil.floatValue(this.retrieveConfigDouble("errorHandlingPercentLevel1", false), 1);
      this.errorHandlingMinutesLevel1 = GrouperUtil.floatValue(this.retrieveConfigDouble("errorHandlingMinutesLevel1", false), 180);
      this.errorHandlingPercentLevel2 = GrouperUtil.floatValue(this.retrieveConfigDouble("errorHandlingPercentLevel2", false), 5);
      this.errorHandlingMinutesLevel2 = GrouperUtil.floatValue(this.retrieveConfigDouble("errorHandlingMinutesLevel2", false), 120);
      this.errorHandlingPercentLevel3 = GrouperUtil.floatValue(this.retrieveConfigDouble("errorHandlingPercentLevel3", false), 10);
      this.errorHandlingMinutesLevel3 = GrouperUtil.floatValue(this.retrieveConfigDouble("errorHandlingMinutesLevel3", false), 12);
      this.errorHandlingPercentLevel4 = GrouperUtil.floatValue(this.retrieveConfigDouble("errorHandlingPercentLevel4", false), 100);
      this.errorHandlingMinutesLevel4 = GrouperUtil.floatValue(this.retrieveConfigDouble("errorHandlingMinutesLevel4", false), 3);
      
    }
    
    this.makeChangesToEntities = GrouperUtil.booleanValue(this.retrieveConfigBoolean("makeChangesToEntities", false), false);

    // reset some defaults if making changes
    if (this.makeChangesToEntities) {
      this.insertEntities = true;
      this.updateEntities = true;
      this.deleteEntities = true;
      this.deleteEntitiesIfGrouperCreated = true;
    }

    this.customizeEntityCrud = GrouperUtil.booleanValue(this.retrieveConfigBoolean("customizeEntityCrud", false), false);
  
    if (!this.operateOnGrouperEntities) {
      this.insertEntities = false;
      
      this.deleteEntities = false;
  
      this.updateEntities = false;
  
      this.selectEntities = false;
  
      this.deleteEntitiesIfNotExistInGrouper = false;
  
      this.deleteEntitiesIfGrouperDeleted = false;
  
      this.deleteEntitiesIfGrouperCreated = false;
      
    }
    
    if (this.customizeEntityCrud) {
      this.insertEntities = GrouperUtil.booleanValue(this.retrieveConfigBoolean("insertEntities", false), this.insertEntities);
      
      this.deleteEntities = GrouperUtil.booleanValue(this.retrieveConfigBoolean("deleteEntities", false), this.deleteEntities);
  
      this.updateEntities = GrouperUtil.booleanValue(this.retrieveConfigBoolean("updateEntities", false), this.updateEntities);
  
      this.selectEntities = GrouperUtil.booleanValue(this.retrieveConfigBoolean("selectEntities", false), this.selectEntities);
  
      this.deleteEntitiesIfNotExistInGrouper = GrouperUtil.booleanValue(this.retrieveConfigBoolean("deleteEntitiesIfNotExistInGrouper", false), this.deleteEntitiesIfNotExistInGrouper);
  
      this.deleteEntitiesIfGrouperDeleted = GrouperUtil.booleanValue(this.retrieveConfigBoolean("deleteEntitiesIfGrouperDeleted", false), this.deleteEntitiesIfGrouperDeleted);
  
      this.deleteEntitiesIfGrouperCreated = GrouperUtil.booleanValue(this.retrieveConfigBoolean("deleteEntitiesIfGrouperCreated", false), 
          (this.makeChangesToEntities && deleteEntities && !this.deleteEntitiesIfNotExistInGrouper && !this.deleteEntitiesIfGrouperDeleted));
          
    }
    this.selectAllEntities = GrouperUtil.booleanValue(this.retrieveConfigBoolean("selectAllEntities", false), this.operateOnGrouperEntities);
    this.selectAllGroups = GrouperUtil.booleanValue(this.retrieveConfigBoolean("selectAllGroups", false), this.operateOnGrouperGroups);

    this.groupIdOfUsersToProvision = this.retrieveConfigString("groupIdOfUsersToProvision", false);
    this.groupIdOfUsersNotToProvision = this.retrieveConfigString("groupIdOfUsersNotToProvision", false);
    this.searchAttributeNameToRetrieveEntities = this.retrieveConfigString("searchAttributeNameToRetrieveEntities", false);
    
    this.loadEntitiesToGrouperTable = GrouperUtil.booleanValue(this.retrieveConfigBoolean("loadEntitiesToGrouperTable", false), false);
    
    this.hasEntityAttributes = GrouperUtil.booleanValue(this.retrieveConfigBoolean("entityResolver.entityAttributesNotInSubjectSource", false), false);
    this.resolveAttributesWithSql = GrouperUtil.booleanValue(this.retrieveConfigBoolean("entityResolver.resolveAttributesWithSQL", false), false);
    this.resolveAttributesWithLdap = GrouperUtil.booleanValue(this.retrieveConfigBoolean("entityResolver.resolveAttributesWithLDAP", false), false);
    this.useGlobalSqlResolver = GrouperUtil.booleanValue(this.retrieveConfigBoolean("entityResolver.useGlobalSQLResolver", false), false);

    this.useGlobalLdapResolver = GrouperUtil.booleanValue(this.retrieveConfigBoolean("entityResolver.useGlobalLDAPResolver", false), false);
    this.globalSqlResolver = this.retrieveConfigString("entityResolver.globalSQLResolver", false);
    this.globalLdapResolver = this.retrieveConfigString("entityResolver.globalLDAPResolver", false);
    this.selectAllSqlOnFull = GrouperUtil.booleanValue(this.retrieveConfigBoolean("entityResolver.selectAllSQLOnFull", false), true);
    this.filterAllLDAPOnFull = GrouperUtil.booleanValue(this.retrieveConfigBoolean("entityResolver.filterAllLDAPOnFull", false), true);
    
    this.entityAttributesSqlExternalSystem = this.retrieveConfigString("entityResolver.sqlConfigId", false);
    this.entityAttributesTableViewName = this.retrieveConfigString("entityResolver.tableOrViewName", false);
    this.entityAttributesColumnNames = this.retrieveConfigString("entityResolver.columnNames", false);
    this.entityAttributesSubjectSourceIdColumn = this.retrieveConfigString("entityResolver.subjectSourceIdColumn", false);
    this.entityAttributesSubjectSearchMatchingColumn = this.retrieveConfigString("entityResolver.subjectSearchMatchingColumn", false);
    this.entityAttributesSqlMappingType = this.retrieveConfigString("entityResolver.sqlMappingType", false);
    this.entityAttributesSqlMappingEntityAttribute = this.retrieveConfigString("entityResolver.sqlMappingEntityAttribute", false);
    this.entityAttributesSqlMappingExpression = this.retrieveConfigString("entityResolver.sqlMappingExpression", false);
    this.entityAttributesLastUpdatedColumn = this.retrieveConfigString("entityResolver.lastUpdatedColumn", false);
    this.entityAttributesLastUpdatedType = this.retrieveConfigString("entityResolver.lastUpdatedType", false);
    
    this.entityAttributesLdapExternalSystem = this.retrieveConfigString("entityResolver.ldapConfigId", false);
    this.entityAttributesLdapBaseDn = this.retrieveConfigString("entityResolver.baseDN", false);
    this.entityAttributesLdapSubjectSource = this.retrieveConfigString("entityResolver.subjectSourceId", false);
    this.entityAttributesLdapSearchScope = this.retrieveConfigString("entityResolver.searchScope", false);
    this.entityAttributesLdapFilterPart = this.retrieveConfigString("entityResolver.filterPart", false);
    this.entityAttributesLdapAttributes = this.retrieveConfigString("entityResolver.attributes", false);
    this.entityAttributesLdapMutliValuedAttributes = this.retrieveConfigString("entityResolver.multiValuedLdapAttributes", false);
    this.entityAttributesLdapMatchingSearchAttribute = this.retrieveConfigString("entityResolver.ldapMatchingSearchAttribute", false);
    this.entityAttributesLdapMappingType = this.retrieveConfigString("entityResolver.ldapMappingType", false);
    this.entityAttributesLdapMappingEntityAttribute = this.retrieveConfigString("entityResolver.ldapMappingEntityAttribute", false);
    this.entityAttributesLdapMatchingExpression = this.retrieveConfigString("entityResolver.ldapMatchingExpression", false);
    this.entityAttributesLdapLastUpdatedAttribute = this.retrieveConfigString("entityResolver.lastUpdatedAttribute", false);
    this.entityAttributesLdapLastUpdatedAttributeFormat = this.retrieveConfigString("entityResolver.lastUpdatedFormat", false);

    this.threadPoolSize = GrouperUtil.intValue(this.retrieveConfigInt("threadPoolSize", false), 5);
    if (this.threadPoolSize < 1) {
      this.threadPoolSize = 1;
    }
    
    if (this.entityAttributesMultivalued == null) {
      this.entityAttributesMultivalued = new HashSet<String>();
    }
    
    if (this.groupAttributesMultivalued == null) {
      this.groupAttributesMultivalued = new HashSet<String>(); 
    }

    this.groupSelectAttributes = new HashSet<String>();

    for (String targetGroupAttributeName : this.targetGroupAttributeNameToConfig.keySet()) {
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = targetGroupAttributeNameToConfig.get(targetGroupAttributeName);
      if (grouperProvisioningConfigurationAttribute.isSelect()) {
        this.groupSelectAttributes.add(targetGroupAttributeName);
      }
      
      if (!StringUtils.isBlank(this.groupMembershipAttributeName) && StringUtils.equals(this.groupMembershipAttributeName, grouperProvisioningConfigurationAttribute.getName())) {
        this.attributeNameForMemberships = targetGroupAttributeName;
        grouperProvisioningConfigurationAttribute.setSelect(this.isSelectMemberships());
        grouperProvisioningConfigurationAttribute.setInsert(this.isInsertMemberships());
      }
      
      if (grouperProvisioningConfigurationAttribute.isMultiValued()) {
        this.groupAttributesMultivalued.add(targetGroupAttributeName);
      }
    }

    this.entityMatchingAttributeSameAsSearchAttribute = GrouperUtil.booleanValue(this.retrieveConfigBoolean("entityMatchingAttributeSameAsSearchAttribute", false), true);
    int entityMatchingAttributeCount = GrouperUtil.intValue(this.retrieveConfigInt("entityMatchingAttributeCount", false), 0);
    this.entityMatchingAttributes = new ArrayList<GrouperProvisioningConfigurationAttribute>();
    this.entitySearchAttributes = new ArrayList<GrouperProvisioningConfigurationAttribute>();
    for (int i=0;i<entityMatchingAttributeCount;i++) {
      String configSuffix = "entityMatchingAttribute" + i + "name";
      String matchingAttributeName = this.retrieveConfigString(configSuffix, true);
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.targetEntityAttributeNameToConfig.get(matchingAttributeName);
      if (grouperProvisioningConfigurationAttribute == null) {
        throw new RuntimeException("Cannot find entity attribute from " + configSuffix + "! '" + matchingAttributeName + "'");
      }
      this.entityMatchingAttributes.add(grouperProvisioningConfigurationAttribute);
      if (entityMatchingAttributeSameAsSearchAttribute) {
        this.entitySearchAttributes.add(grouperProvisioningConfigurationAttribute);
      }
    }
    
    if (!entityMatchingAttributeSameAsSearchAttribute) {
      int entitySearchAttributeCount = this.retrieveConfigInt("entitySearchAttributeCount", true);
      for (int i=0;i<entitySearchAttributeCount;i++) {
        String configSuffix = "entitySearchAttribute" + i + "name";
        String searchAttributeName = this.retrieveConfigString(configSuffix, true);
        GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.targetEntityAttributeNameToConfig.get(searchAttributeName);
        if (grouperProvisioningConfigurationAttribute == null) {
          throw new RuntimeException("Cannot find entity attribute from " + configSuffix + "! '" + searchAttributeName + "'");
        }
        this.entitySearchAttributes.add(grouperProvisioningConfigurationAttribute);
      }
    }

    this.groupMatchingAttributeSameAsSearchAttribute = GrouperUtil.booleanValue(this.retrieveConfigBoolean("groupMatchingAttributeSameAsSearchAttribute", false), true);
    int groupMatchingAttributeCount = GrouperUtil.intValue(this.retrieveConfigInt("groupMatchingAttributeCount", false), 0);
    this.groupMatchingAttributes = new ArrayList<GrouperProvisioningConfigurationAttribute>();
    this.groupSearchAttributes = new ArrayList<GrouperProvisioningConfigurationAttribute>();
    for (int i=0;i<groupMatchingAttributeCount;i++) {
      String configSuffix = "groupMatchingAttribute" + i + "name";
      String matchingAttributeName = this.retrieveConfigString(configSuffix, true);
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.targetGroupAttributeNameToConfig.get(matchingAttributeName);
      if (grouperProvisioningConfigurationAttribute == null) {
        throw new RuntimeException("Cannot find group attribute from " + configSuffix + "! '" + matchingAttributeName + "'");
      }
      this.groupMatchingAttributes.add(grouperProvisioningConfigurationAttribute);
      if (groupMatchingAttributeSameAsSearchAttribute) {
        this.groupSearchAttributes.add(grouperProvisioningConfigurationAttribute);
      }
    }
    
    if (!groupMatchingAttributeSameAsSearchAttribute) {
      int groupSearchAttributeCount = this.retrieveConfigInt("groupSearchAttributeCount", true);
      for (int i=0;i<groupSearchAttributeCount;i++) {
        String configSuffix = "groupSearchAttribute" + i + "name";
        String searchAttributeName = this.retrieveConfigString(configSuffix, true);
        GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.targetGroupAttributeNameToConfig.get(searchAttributeName);
        if (grouperProvisioningConfigurationAttribute == null) {
          throw new RuntimeException("Cannot find group attribute from " + configSuffix + "! '" + searchAttributeName + "'");
        }
        this.groupSearchAttributes.add(grouperProvisioningConfigurationAttribute);
      }
    }

    this.entitySelectAttributes = new HashSet<String>();
    for (String targetEntityAttributeName : this.targetEntityAttributeNameToConfig.keySet()) {
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = targetEntityAttributeNameToConfig.get(targetEntityAttributeName);
      if (grouperProvisioningConfigurationAttribute.isSelect()) {
        this.entitySelectAttributes.add(targetEntityAttributeName);
      }
      
      if (!StringUtils.isBlank(this.entityMembershipAttributeName) 
          && StringUtils.equals(this.entityMembershipAttributeName, grouperProvisioningConfigurationAttribute.getName())) {
        this.attributeNameForMemberships = targetEntityAttributeName;
        grouperProvisioningConfigurationAttribute.setSelect(this.isSelectMemberships());
        grouperProvisioningConfigurationAttribute.setInsert(this.isInsertMemberships());
      }
      
      if (grouperProvisioningConfigurationAttribute.isMultiValued()) {
        this.entityAttributesMultivalued.add(targetEntityAttributeName);
      }

    }

    this.recalculateAllOperations = GrouperUtil.booleanValue(this.retrieveConfigBoolean("recalculateAllOperations", false), false);
    
    if (this.recalculateAllOperations) {
      this.onlyAddMembershipsIfUserExistsInTarget = GrouperUtil.booleanValue(this.retrieveConfigBoolean("onlyAddMembershipsIfUserExistsInTarget", false), false);
    }
    
    this.readOnly = GrouperUtil.booleanValue(this.retrieveConfigBoolean("readOnly", false), false);

    {
      String grouperProvisioningMembershipFieldTypeString = this.retrieveConfigString("membershipFields", false);
      if (StringUtils.isBlank(grouperProvisioningMembershipFieldTypeString) || StringUtils.equalsIgnoreCase("members", grouperProvisioningMembershipFieldTypeString)) {
        this.grouperProvisioningMembershipFieldType = GrouperProvisioningMembershipFieldType.members;
      } else if (StringUtils.equals("admin", grouperProvisioningMembershipFieldTypeString)) {
        this.grouperProvisioningMembershipFieldType = GrouperProvisioningMembershipFieldType.admin;
      } else if (StringUtils.equals("read,admin", grouperProvisioningMembershipFieldTypeString)) {
        this.grouperProvisioningMembershipFieldType = GrouperProvisioningMembershipFieldType.readAdmin;
      } else if (StringUtils.equals("update,admin", grouperProvisioningMembershipFieldTypeString)) {
        this.grouperProvisioningMembershipFieldType = GrouperProvisioningMembershipFieldType.updateAdmin;
      } else {
        throw new RuntimeException("Invalid GrouperProvisioningMembershipFieldType: '" + grouperProvisioningMembershipFieldTypeString + "'");
      }
    }
    
    for (String configItem : new String[] {"grouperToTargetTranslationMembership", "grouperToTargetTranslationEntity",
        "grouperToTargetTranslationGroup", "grouperToTargetTranslationGroupCreateOnly"}) {
      String key = GrouperUtil.stripPrefix(configItem, "grouperToTargetTranslation");
      for (int i=0; i<= 1000; i++) {
        
        String script = this.retrieveConfigString(configItem + "."+i+".script" , false);
        if (StringUtils.isBlank(script)) {
          break;
        }
        List<String> scripts = this.grouperProvisioningToTargetTranslation.get(key);
        if (scripts == null) {
          scripts = new ArrayList<String>();
          this.grouperProvisioningToTargetTranslation.put(key, scripts);
        }
        scripts.add(script);
        
      }
      
    }
    
    this.subjectIdentifierForMemberSyncTable = this.retrieveConfigString("subjectIdentifierForMemberSyncTable", false);
        
    // diagnostics settings
    this.diagnosticsGroupsAllSelect = this.retrieveConfigBoolean("selectAllGroupsDuringDiagnostics", false);
    this.diagnosticsEntitiesAllSelect = this.retrieveConfigBoolean("selectAllEntitiesDuringDiagnostics", false);
    this.diagnosticsMembershipsAllSelect = this.retrieveConfigBoolean("selectAllMembershipsDuringDiagnostics", false);
    this.diagnosticsGroupName = this.retrieveConfigString("testGroupName", false);
    this.diagnosticsSubjectIdOrIdentifier = this.retrieveConfigString("testSubjectIdOrIdentifier", false);
    this.createGroupDuringDiagnostics = this.retrieveConfigBoolean("createGroupDuringDiagnostics", false);
    this.deleteGroupDuringDiagnostics = this.retrieveConfigBoolean("deleteGroupDuringDiagnostics", false);
    this.createEntityDuringDiagnostics = this.retrieveConfigBoolean("createEntityDuringDiagnostics", false);
    this.deleteEntityDuringDiagnostics = this.retrieveConfigBoolean("deleteEntityDuringDiagnostics", false);
    
    //register metadata
    this.getGrouperProvisioner().retrieveGrouperProvisioningObjectMetadata().appendMetadataItemsFromConfig(this.metadataNameToMetadataItem.values());
    
    assignAutoTranslatedGroupsConfiguration();

    assignAutoTranslatedEntitiesConfiguration();
    
  }
  
  private boolean groupMatchingAttributeSameAsSearchAttribute;
  
  
  
  public boolean isGroupMatchingAttributeSameAsSearchAttribute() {
    return groupMatchingAttributeSameAsSearchAttribute;
  }

  private boolean entityMatchingAttributeSameAsSearchAttribute;
  
  
  
  public boolean isEntityMatchingAttributeSameAsSearchAttribute() {
    return entityMatchingAttributeSameAsSearchAttribute;
  }

  public boolean isGroupsRequireMembers() {
    return groupsRequireMembers;
  }

  private boolean groupAttributeValueCacheHas;

  public boolean isGroupAttributeValueCacheHas() {
    return groupAttributeValueCacheHas;
  }

  private Boolean entityAttributeValueCacheHas;

  public boolean isEntityAttributeValueCacheHas() {
    return entityAttributeValueCacheHas;
  }

  private GrouperProvisioningConfigurationAttributeDbCache[] groupAttributeDbCaches = new GrouperProvisioningConfigurationAttributeDbCache[4];
  
  public GrouperProvisioningConfigurationAttributeDbCache[] getGroupAttributeDbCaches() {
    return groupAttributeDbCaches;
  }

  private GrouperProvisioningConfigurationAttributeDbCache[] entityAttributeDbCaches = new GrouperProvisioningConfigurationAttributeDbCache[4];
  
  public GrouperProvisioningConfigurationAttributeDbCache[] getEntityAttributeDbCaches() {
    return entityAttributeDbCaches;
  }

  
  private void assignAutoTranslatedGroupsConfiguration() {
    
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningTranslator().isTranslateGrouperToTargetAutomatically()) {
      
      if (this.targetGroupAttributeNameToConfig.size() == 0) {
        
        GrouperProvisioningConfigurationAttribute nameConfigurationAttribute = new GrouperProvisioningConfigurationAttribute();
        nameConfigurationAttribute.setGrouperProvisioner(grouperProvisioner);
        nameConfigurationAttribute.setUpdate(this.isUpdateGroups());
        nameConfigurationAttribute.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.group);
        nameConfigurationAttribute.setInsert(this.isInsertGroups());
        nameConfigurationAttribute.setName("name");
        nameConfigurationAttribute.setSelect(this.isSelectGroups());
        nameConfigurationAttribute.setValueType(GrouperProvisioningConfigurationAttributeValueType.STRING);
        nameConfigurationAttribute.setTranslateFromGrouperProvisioningGroupField("name");
        this.targetGroupAttributeNameToConfig.put("name", nameConfigurationAttribute);
        
        GrouperProvisioningConfigurationAttribute displayNameConfigurationAttribute = new GrouperProvisioningConfigurationAttribute();
        displayNameConfigurationAttribute.setGrouperProvisioner(grouperProvisioner);
        displayNameConfigurationAttribute.setUpdate(this.isUpdateGroups());
        displayNameConfigurationAttribute.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.group);
        displayNameConfigurationAttribute.setInsert(this.isInsertGroups());
        displayNameConfigurationAttribute.setName("displayName");
        displayNameConfigurationAttribute.setSelect(this.isSelectGroups());
        displayNameConfigurationAttribute.setValueType(GrouperProvisioningConfigurationAttributeValueType.STRING);
        displayNameConfigurationAttribute.setTranslateFromGrouperProvisioningGroupField("displayName");
        this.targetGroupAttributeNameToConfig.put("displayName", displayNameConfigurationAttribute);
        
        GrouperProvisioningConfigurationAttribute idIndexConfigurationAttribute = new GrouperProvisioningConfigurationAttribute();
        idIndexConfigurationAttribute.setGrouperProvisioner(grouperProvisioner);
        idIndexConfigurationAttribute.setUpdate(this.isUpdateGroups());
        idIndexConfigurationAttribute.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.group);
        idIndexConfigurationAttribute.setInsert(this.isInsertGroups());
        idIndexConfigurationAttribute.setName("idIndex");
        idIndexConfigurationAttribute.setSelect(this.isSelectGroups());
        idIndexConfigurationAttribute.setValueType(GrouperProvisioningConfigurationAttributeValueType.STRING);
        idIndexConfigurationAttribute.setTranslateFromGrouperProvisioningGroupField("idIndex");
        this.targetGroupAttributeNameToConfig.put("idIndex", idIndexConfigurationAttribute);
        
        
        GrouperProvisioningConfigurationAttribute descriptionConfigurationAttribute = new GrouperProvisioningConfigurationAttribute();
        descriptionConfigurationAttribute.setGrouperProvisioner(grouperProvisioner);
        descriptionConfigurationAttribute.setUpdate(this.isUpdateGroups());
        descriptionConfigurationAttribute.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.group);
        descriptionConfigurationAttribute.setInsert(this.isInsertGroups());
        descriptionConfigurationAttribute.setName("description");
        descriptionConfigurationAttribute.setSelect(this.isSelectGroups());
        descriptionConfigurationAttribute.setValueType(GrouperProvisioningConfigurationAttributeValueType.STRING);
        descriptionConfigurationAttribute.setTranslateFromGrouperProvisioningGroupField("description");
        this.targetGroupAttributeNameToConfig.put("description", descriptionConfigurationAttribute);
        
      }
      
    }
  }
  
  private void assignAutoTranslatedEntitiesConfiguration() {
    
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningTranslator().isTranslateGrouperToTargetAutomatically()) {
      
      if (this.targetEntityAttributeNameToConfig.size() == 0) {
        
        GrouperProvisioningConfigurationAttribute idConfigurationAttribute = new GrouperProvisioningConfigurationAttribute();
        idConfigurationAttribute.setGrouperProvisioner(grouperProvisioner);
        idConfigurationAttribute.setUpdate(this.isUpdateEntities());
        idConfigurationAttribute.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.entity);
        idConfigurationAttribute.setInsert(this.isInsertEntities());
        idConfigurationAttribute.setName("id");
        idConfigurationAttribute.setSelect(this.isSelectEntities());
        idConfigurationAttribute.setValueType(GrouperProvisioningConfigurationAttributeValueType.STRING);
        idConfigurationAttribute.setTranslateFromGrouperProvisioningEntityField("id");
        this.targetEntityAttributeNameToConfig.put("id", idConfigurationAttribute);
        
        GrouperProvisioningConfigurationAttribute nameConfigurationAttribute = new GrouperProvisioningConfigurationAttribute();
        nameConfigurationAttribute.setGrouperProvisioner(grouperProvisioner);
        nameConfigurationAttribute.setUpdate(this.isUpdateEntities());
        nameConfigurationAttribute.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.entity);
        nameConfigurationAttribute.setInsert(this.isInsertEntities());
        nameConfigurationAttribute.setName("name");
        nameConfigurationAttribute.setSelect(this.isSelectEntities());
        nameConfigurationAttribute.setValueType(GrouperProvisioningConfigurationAttributeValueType.STRING);
        nameConfigurationAttribute.setTranslateFromGrouperProvisioningEntityField("name");
        this.targetEntityAttributeNameToConfig.put("name", nameConfigurationAttribute);
        
        GrouperProvisioningConfigurationAttribute subjectIdConfigurationAttribute = new GrouperProvisioningConfigurationAttribute();
        subjectIdConfigurationAttribute.setGrouperProvisioner(grouperProvisioner);
        subjectIdConfigurationAttribute.setUpdate(this.isUpdateEntities());
        subjectIdConfigurationAttribute.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.entity);
        subjectIdConfigurationAttribute.setInsert(this.isInsertEntities());
        subjectIdConfigurationAttribute.setName("subjectId");
        subjectIdConfigurationAttribute.setSelect(this.isSelectEntities());
        subjectIdConfigurationAttribute.setValueType(GrouperProvisioningConfigurationAttributeValueType.STRING);
        subjectIdConfigurationAttribute.setTranslateFromGrouperProvisioningEntityField("subjectId");
        this.targetEntityAttributeNameToConfig.put("subjectId", subjectIdConfigurationAttribute);
        
        GrouperProvisioningConfigurationAttribute descriptionConfigurationAttribute = new GrouperProvisioningConfigurationAttribute();
        descriptionConfigurationAttribute.setGrouperProvisioner(grouperProvisioner);
        descriptionConfigurationAttribute.setUpdate(this.isUpdateEntities());
        descriptionConfigurationAttribute.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.entity);
        descriptionConfigurationAttribute.setInsert(this.isInsertEntities());
        descriptionConfigurationAttribute.setName("description");
        descriptionConfigurationAttribute.setSelect(this.isSelectEntities());
        descriptionConfigurationAttribute.setValueType(GrouperProvisioningConfigurationAttributeValueType.STRING);
        descriptionConfigurationAttribute.setTranslateFromGrouperProvisioningEntityField("description");
        this.targetEntityAttributeNameToConfig.put("description", descriptionConfigurationAttribute);
        
        GrouperProvisioningConfigurationAttribute subjectSourceIdConfigurationAttribute = new GrouperProvisioningConfigurationAttribute();
        subjectSourceIdConfigurationAttribute.setGrouperProvisioner(grouperProvisioner);
        subjectSourceIdConfigurationAttribute.setUpdate(this.isUpdateEntities());
        subjectSourceIdConfigurationAttribute.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.entity);
        subjectSourceIdConfigurationAttribute.setInsert(this.isInsertEntities());
        subjectSourceIdConfigurationAttribute.setName("subjectSourceId");
        subjectSourceIdConfigurationAttribute.setSelect(this.isSelectEntities());
        subjectSourceIdConfigurationAttribute.setValueType(GrouperProvisioningConfigurationAttributeValueType.STRING);
        subjectSourceIdConfigurationAttribute.setTranslateFromGrouperProvisioningEntityField("subjectSourceId");
        this.targetEntityAttributeNameToConfig.put("subjectSourceId", subjectSourceIdConfigurationAttribute);
        
        GrouperProvisioningConfigurationAttribute subjectIdetifier0ConfigurationAttribute = new GrouperProvisioningConfigurationAttribute();
        subjectIdetifier0ConfigurationAttribute.setGrouperProvisioner(grouperProvisioner);
        subjectIdetifier0ConfigurationAttribute.setUpdate(this.isUpdateEntities());
        subjectIdetifier0ConfigurationAttribute.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.entity);
        subjectIdetifier0ConfigurationAttribute.setInsert(this.isInsertEntities());
        subjectIdetifier0ConfigurationAttribute.setName("subjectIdentifier0");
        subjectIdetifier0ConfigurationAttribute.setSelect(this.isSelectEntities());
        subjectIdetifier0ConfigurationAttribute.setValueType(GrouperProvisioningConfigurationAttributeValueType.STRING);
        subjectIdetifier0ConfigurationAttribute.setTranslateFromGrouperProvisioningEntityField("subjectIdentifier0");
        this.targetEntityAttributeNameToConfig.put("subjectIdentifier0", subjectIdetifier0ConfigurationAttribute);
        
        GrouperProvisioningConfigurationAttribute subjectIdetifier1ConfigurationAttribute = new GrouperProvisioningConfigurationAttribute();
        subjectIdetifier1ConfigurationAttribute.setGrouperProvisioner(grouperProvisioner);
        subjectIdetifier1ConfigurationAttribute.setUpdate(this.isUpdateEntities());
        subjectIdetifier1ConfigurationAttribute.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.entity);
        subjectIdetifier1ConfigurationAttribute.setInsert(this.isInsertEntities());
        subjectIdetifier1ConfigurationAttribute.setName("subjectIdentifier1");
        subjectIdetifier1ConfigurationAttribute.setSelect(this.isSelectEntities());
        subjectIdetifier1ConfigurationAttribute.setValueType(GrouperProvisioningConfigurationAttributeValueType.STRING);
        subjectIdetifier1ConfigurationAttribute.setTranslateFromGrouperProvisioningEntityField("subjectIdentifier1");
        this.targetEntityAttributeNameToConfig.put("subjectIdentifier1", subjectIdetifier1ConfigurationAttribute);
        
        GrouperProvisioningConfigurationAttribute subjectIdetifier2ConfigurationAttribute = new GrouperProvisioningConfigurationAttribute();
        subjectIdetifier2ConfigurationAttribute.setGrouperProvisioner(grouperProvisioner);
        subjectIdetifier2ConfigurationAttribute.setUpdate(this.isUpdateEntities());
        subjectIdetifier2ConfigurationAttribute.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.entity);
        subjectIdetifier2ConfigurationAttribute.setInsert(this.isInsertEntities());
        subjectIdetifier2ConfigurationAttribute.setName("subjectIdentifier2");
        subjectIdetifier2ConfigurationAttribute.setSelect(this.isSelectEntities());
        subjectIdetifier2ConfigurationAttribute.setValueType(GrouperProvisioningConfigurationAttributeValueType.STRING);
        subjectIdetifier2ConfigurationAttribute.setTranslateFromGrouperProvisioningEntityField("subjectIdentifier2");
        this.targetEntityAttributeNameToConfig.put("subjectIdentifier2", subjectIdetifier2ConfigurationAttribute);
        
        GrouperProvisioningConfigurationAttribute idIndexConfigurationAttribute = new GrouperProvisioningConfigurationAttribute();
        idIndexConfigurationAttribute.setGrouperProvisioner(grouperProvisioner);
        idIndexConfigurationAttribute.setUpdate(this.isUpdateEntities());
        idIndexConfigurationAttribute.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.entity);
        idIndexConfigurationAttribute.setInsert(this.isInsertEntities());
        idIndexConfigurationAttribute.setName("idIndex");
        idIndexConfigurationAttribute.setSelect(this.isSelectEntities());
        idIndexConfigurationAttribute.setValueType(GrouperProvisioningConfigurationAttributeValueType.STRING);
        idIndexConfigurationAttribute.setTranslateFromGrouperProvisioningEntityField("idIndex");
        this.targetEntityAttributeNameToConfig.put("idIndex", idIndexConfigurationAttribute);
      }
      
    }
  }
  
  /**
   * operate on grouper entities
   */
  private boolean operateOnGrouperEntities;
  
  /**
   * operate on grouper memberships
   */
  private boolean operateOnGrouperMemberships;
  
  /**
   * operate on grouper groups
   */
  private boolean operateOnGrouperGroups;
  
  /**
   * operate on grouper entities
   * @return is operate
   */
  public boolean isOperateOnGrouperEntities() {
    return operateOnGrouperEntities;
  }

  /**
   * operate on grouper entities
   * @return is operate
   */
  public boolean isOperateOnGrouperMemberships() {
    return operateOnGrouperMemberships;
  }

  
  public boolean isOperateOnGrouperGroups() {
    return operateOnGrouperGroups;
  }

  public boolean isRecalculateAllOperations() {
    return recalculateAllOperations;
  }

  
  public void setRecalculateAllOperations(boolean recalculateAllOperations) {
    this.recalculateAllOperations = recalculateAllOperations;
  }

  /**
   * no need to configure twice if the caller needs to configure before provisioning
   */
  private boolean configured = false;

  /**
   * attribute name to config
   */
  private Map<String, GrouperProvisioningConfigurationAttribute> targetEntityAttributeNameToConfig = new LinkedHashMap<String, GrouperProvisioningConfigurationAttribute>();

  /**
   * attribute name to config
   */
  private Map<String, GrouperProvisioningConfigurationAttribute> targetMembershipAttributeNameToConfig = new LinkedHashMap<String, GrouperProvisioningConfigurationAttribute>();

  public Map<String, GrouperProvisioningConfigurationAttribute> getTargetMembershipAttributeNameToConfig() {
    return targetMembershipAttributeNameToConfig;
  }

  
  public Map<String, GrouperProvisioningConfigurationAttribute> getTargetEntityAttributeNameToConfig() {
    return targetEntityAttributeNameToConfig;
  }

  

  /**
   * configure the provisioner, call super if subclassing
   */
  public void configureProvisioner() {
    
    if (this.configured) {
      return;
    }
    try {
    
      this.preConfigure();
    
      this.configureGenericSettings();
      
      this.configureSpecificSettings();
    
    } catch (RuntimeException re) {
      if (this.grouperProvisioner != null && this.grouperProvisioner.getGcGrouperSyncLog() != null) {
        try {
          this.grouperProvisioner.getGcGrouperSyncLog().setStatus(GcGrouperSyncLogState.CONFIG_ERROR);
          this.grouperProvisioner.getGcGrouperSync().getGcGrouperSyncLogDao().internal_logStore(this.grouperProvisioner.getGcGrouperSyncLog());
        } catch (RuntimeException re2) {
          GrouperClientUtils.injectInException(re, "***** START ANOTHER EXCEPTON *******" + GrouperClientUtils.getFullStackTrace(re2) + "***** END ANOTHER EXCEPTON *******");
        }
      }
      throw re;
    }

    this.configured = true;
  }

  
  
  public boolean isConfigured() {
    return configured;
  }



  public Map<String, Object> getDebugMap() {
    return debugMap;
  }

  
  public void setDebugMap(Map<String, Object> debugMap) {
    this.debugMap = debugMap;
  }

  
  
  public boolean isHasTargetGroupLink() {
    return hasTargetGroupLink;
  }

  
  public void setHasTargetGroupLink(boolean hasTargetGroupLink) {
    this.hasTargetGroupLink = hasTargetGroupLink;
  }

  
  public boolean isHasTargetEntityLink() {
    return hasTargetEntityLink;
  }

  
  public void setHasTargetEntityLink(boolean hasTargetEntityLink) {
    this.hasTargetEntityLink = hasTargetEntityLink;
  }
  
  public Set<String> getSubjectSourcesToProvision() {
    return subjectSourcesToProvision;
  }

  
  public void setSubjectSourcesToProvision(Set<String> subjectSourcesToProvision) {
    this.subjectSourcesToProvision = subjectSourcesToProvision;
  }
  
  public List<GrouperProvisioningConfigurationAttribute> getEntitySearchAttributes() {
    return entitySearchAttributes;
  }

  
  public void setEntitySearchAttributes(List<GrouperProvisioningConfigurationAttribute> userSearchAttributes ) {
    this.entitySearchAttributes = userSearchAttributes;
  }

  
  public List<GrouperProvisioningConfigurationAttribute> getGroupSearchAttributes() {
    return groupSearchAttributes;
  }

  public void setGroupSearchAttributes(List<GrouperProvisioningConfigurationAttribute> groupSearchAttributes) {
    this.groupSearchAttributes = groupSearchAttributes;
  }

  
  public Set<String> getEntityAttributesMultivalued() {
    return entityAttributesMultivalued;
  }

  
  public void setEntityAttributesMultivalued(Set<String> userAttributesMultivalued) {
    this.entityAttributesMultivalued = userAttributesMultivalued;
  }

  
  public Set<String> getGroupAttributesMultivalued() {
    return groupAttributesMultivalued;
  }

  
  public void setGroupAttributesMultivalued(Set<String> groupAttributesMultivalued) {
    this.groupAttributesMultivalued = groupAttributesMultivalued;
  }

  public boolean isInsertEntities() {
    return insertEntities;
  }

  
  public void setInsertEntities(boolean insertEntities) {
    this.insertEntities = insertEntities;
  }

  
  public boolean isInsertGroups() {
    return insertGroups;
  }

  
  public void setInsertGroups(boolean insertGroups) {
    this.insertGroups = insertGroups;
  }

  
  
  public boolean isDeleteGroupsIfNotExistInGrouper() {
    return deleteGroupsIfNotExistInGrouper;
  }

  
  public void setDeleteGroupsIfNotExistInGrouper(boolean deleteGroupsIfNotExistInGrouper) {
    this.deleteGroupsIfNotExistInGrouper = deleteGroupsIfNotExistInGrouper;
  }


  public boolean isDeleteGroupsIfGrouperDeleted() {
    return deleteGroupsIfGrouperDeleted;
  }

  
  public void setDeleteGroupsIfGrouperDeleted(boolean deleteGroupsIfGrouperDeleted) {
    this.deleteGroupsIfGrouperDeleted = deleteGroupsIfGrouperDeleted;
  }

  public GrouperProvisioningMembershipFieldType getGrouperProvisioningMembershipFieldType() {
    return grouperProvisioningMembershipFieldType;
  }

  
  public void setGrouperProvisioningMembershipFieldType(
      GrouperProvisioningMembershipFieldType grouperProvisioningMembershipFieldType) {
    this.grouperProvisioningMembershipFieldType = grouperProvisioningMembershipFieldType;
  }


  public String getEntityAttributesLdapSubjectSource() {
    return entityAttributesLdapSubjectSource;
  }
  
  public int getDaoSleepBeforeSelectAfterInsertMillis() {
    return 0;
  }

  /**
   * finish configuration after figuring out metadata
   */
  public void configureAfterMetadata() {
  }

  
  public Boolean getCreateGroupDuringDiagnostics() {
    return createGroupDuringDiagnostics;
  }

  
  public void setCreateGroupDuringDiagnostics(Boolean createGroupDuringDiagnostics) {
    this.createGroupDuringDiagnostics = createGroupDuringDiagnostics;
  }

  
  public Boolean getDeleteGroupDuringDiagnostics() {
    return deleteGroupDuringDiagnostics;
  }

  
  public void setDeleteGroupDuringDiagnostics(Boolean deleteGroupDuringDiagnostics) {
    this.deleteGroupDuringDiagnostics = deleteGroupDuringDiagnostics;
  }

  
  public Boolean getCreateEntityDuringDiagnostics() {
    return createEntityDuringDiagnostics;
  }

  
  public void setCreateEntityDuringDiagnostics(Boolean createEntityDuringDiagnostics) {
    this.createEntityDuringDiagnostics = createEntityDuringDiagnostics;
  }

  
  public Boolean getDeleteEntityDuringDiagnostics() {
    return deleteEntityDuringDiagnostics;
  }

  
  public void setDeleteEntityDuringDiagnostics(Boolean deleteEntityDuringDiagnostics) {
    this.deleteEntityDuringDiagnostics = deleteEntityDuringDiagnostics;
  }

  
  public Boolean getDiagnosticsGroupsAllSelect() {
    return diagnosticsGroupsAllSelect;
  }

  
  public void setDiagnosticsGroupsAllSelect(Boolean diagnosticsGroupsAllSelect) {
    this.diagnosticsGroupsAllSelect = diagnosticsGroupsAllSelect;
  }

  
  public Boolean getDiagnosticsEntitiesAllSelect() {
    return diagnosticsEntitiesAllSelect;
  }

  
  public void setDiagnosticsEntitiesAllSelect(Boolean diagnosticsEntitiesAllSelect) {
    this.diagnosticsEntitiesAllSelect = diagnosticsEntitiesAllSelect;
  }

  
  public Boolean getDiagnosticsMembershipsAllSelect() {
    return diagnosticsMembershipsAllSelect;
  }

  
  public void setDiagnosticsMembershipsAllSelect(Boolean diagnosticsMembershipsAllSelect) {
    this.diagnosticsMembershipsAllSelect = diagnosticsMembershipsAllSelect;
  }

  
  public Boolean getOnlyProvisionPolicyGroups() {
    return onlyProvisionPolicyGroups;
  }

  
  public void setOnlyProvisionPolicyGroups(Boolean onlyProvisionPolicyGroups) {
    this.onlyProvisionPolicyGroups = onlyProvisionPolicyGroups;
  }

  
  public Boolean getAllowPolicyGroupOverride() {
    return allowPolicyGroupOverride;
  }

  
  public void setAllowPolicyGroupOverride(Boolean allowPolicyGroupOverride) {
    this.allowPolicyGroupOverride = allowPolicyGroupOverride;
  }

  
  public Boolean getAllowProvisionableRegexOverride() {
    return allowProvisionableRegexOverride;
  }

  
  public void setAllowProvisionableRegexOverride(Boolean allowProvisionableRegexOverride) {
    this.allowProvisionableRegexOverride = allowProvisionableRegexOverride;
  }

  
  public Boolean getEntityAttributeValueCacheHas() {
    return entityAttributeValueCacheHas;
  }

  
  public void setEntityAttributeValueCacheHas(Boolean entityAttributeValueCacheHas) {
    this.entityAttributeValueCacheHas = entityAttributeValueCacheHas;
  }

  
  public void setThreadPoolSize(int threadPoolSize) {
    this.threadPoolSize = threadPoolSize;
  }

  
  public void setSubjectIdentifierForMemberSyncTable(
      String subjectIdentifierForMemberSyncTable) {
    this.subjectIdentifierForMemberSyncTable = subjectIdentifierForMemberSyncTable;
  }

  
  public void setGroupsRequireMembers(boolean groupsRequireMembers) {
    this.groupsRequireMembers = groupsRequireMembers;
  }

  
  public void setHasEntityAttributes(boolean hasEntityAttributes) {
    this.hasEntityAttributes = hasEntityAttributes;
  }

  
  public void setResolveAttributesWithSql(boolean resolveAttributesWithSql) {
    this.resolveAttributesWithSql = resolveAttributesWithSql;
  }

  
  public void setResolveAttributesWithLdap(boolean resolveAttributesWithLdap) {
    this.resolveAttributesWithLdap = resolveAttributesWithLdap;
  }

  
  public void setUseGlobalSqlResolver(boolean useGlobalSqlResolver) {
    this.useGlobalSqlResolver = useGlobalSqlResolver;
  }

  
  public void setUseGlobalLdapResolver(boolean useGlobalLdapResolver) {
    this.useGlobalLdapResolver = useGlobalLdapResolver;
  }

  
  public void setGlobalSqlResolver(String globalSqlResolver) {
    this.globalSqlResolver = globalSqlResolver;
  }

  
  public void setGlobalLdapResolver(String globalLdapResolver) {
    this.globalLdapResolver = globalLdapResolver;
  }

  
  public void setSelectAllSqlOnFull(boolean selectAllSqlOnFull) {
    this.selectAllSqlOnFull = selectAllSqlOnFull;
  }

  
  public void setFilterAllLDAPOnFull(boolean filterAllLDAPOnFull) {
    this.filterAllLDAPOnFull = filterAllLDAPOnFull;
  }

  
  public void setLoadEntitiesToGrouperTable(boolean loadEntitiesToGrouperTable) {
    this.loadEntitiesToGrouperTable = loadEntitiesToGrouperTable;
  }

  
  public void setEntityAttributesSqlExternalSystem(
      String entityAttributesSqlExternalSystem) {
    this.entityAttributesSqlExternalSystem = entityAttributesSqlExternalSystem;
  }

  
  public void setEntityAttributesTableViewName(String entityAttributesTableViewName) {
    this.entityAttributesTableViewName = entityAttributesTableViewName;
  }

  
  public void setEntityAttributesColumnNames(String entityAttributesColumnNames) {
    this.entityAttributesColumnNames = entityAttributesColumnNames;
  }

  
  public void setEntityAttributesSubjectSourceIdColumn(
      String entityAttributesSubjectSourceIdColumn) {
    this.entityAttributesSubjectSourceIdColumn = entityAttributesSubjectSourceIdColumn;
  }

  
  public void setEntityAttributesSubjectSearchMatchingColumn(
      String entityAttributesSubjectSearchMatchingColumn) {
    this.entityAttributesSubjectSearchMatchingColumn = entityAttributesSubjectSearchMatchingColumn;
  }

  
  public void setEntityAttributesSqlMappingType(String entityAttributesSqlMappingType) {
    this.entityAttributesSqlMappingType = entityAttributesSqlMappingType;
  }

  
  public void setEntityAttributesSqlMappingEntityAttribute(
      String entityAttributesSqlMappingEntityAttribute) {
    this.entityAttributesSqlMappingEntityAttribute = entityAttributesSqlMappingEntityAttribute;
  }

  
  public void setEntityAttributesSqlMappingExpression(
      String entityAttributesSqlMappingExpression) {
    this.entityAttributesSqlMappingExpression = entityAttributesSqlMappingExpression;
  }

  
  public void setEntityAttributesLastUpdatedColumn(
      String entityAttributesLastUpdatedColumn) {
    this.entityAttributesLastUpdatedColumn = entityAttributesLastUpdatedColumn;
  }

  
  public void setEntityAttributesLastUpdatedType(String entityAttributesLastUpdatedType) {
    this.entityAttributesLastUpdatedType = entityAttributesLastUpdatedType;
  }

  
  public void setEntityAttributesLdapExternalSystem(
      String entityAttributesLdapExternalSystem) {
    this.entityAttributesLdapExternalSystem = entityAttributesLdapExternalSystem;
  }

  
  public void setEntityAttributesLdapBaseDn(String entityAttributesLdapBaseDn) {
    this.entityAttributesLdapBaseDn = entityAttributesLdapBaseDn;
  }

  
  public void setEntityAttributesLdapSubjectSource(
      String entityAttributesLdapSubjectSource) {
    this.entityAttributesLdapSubjectSource = entityAttributesLdapSubjectSource;
  }

  
  public void setEntityAttributesLdapSearchScope(String entityAttributesLdapSearchScope) {
    this.entityAttributesLdapSearchScope = entityAttributesLdapSearchScope;
  }

  
  public void setEntityAttributesLdapFilterPart(String entityAttributesLdapFilterPart) {
    this.entityAttributesLdapFilterPart = entityAttributesLdapFilterPart;
  }

  
  public void setEntityAttributesLdapAttributes(String entityAttributesLdapAttributes) {
    this.entityAttributesLdapAttributes = entityAttributesLdapAttributes;
  }

  
  public void setEntityAttributesLdapMutliValuedAttributes(
      String entityAttributesLdapMutliValuedAttributes) {
    this.entityAttributesLdapMutliValuedAttributes = entityAttributesLdapMutliValuedAttributes;
  }

  
  public void setEntityAttributesLdapMatchingSearchAttribute(
      String entityAttributesLdapMatchingSearchAttribute) {
    this.entityAttributesLdapMatchingSearchAttribute = entityAttributesLdapMatchingSearchAttribute;
  }

  
  public void setEntityAttributesLdapMappingType(String entityAttributesLdapMappingType) {
    this.entityAttributesLdapMappingType = entityAttributesLdapMappingType;
  }

  
  public void setEntityAttributesLdapMappingEntityAttribute(
      String entityAttributesLdapMappingEntityAttribute) {
    this.entityAttributesLdapMappingEntityAttribute = entityAttributesLdapMappingEntityAttribute;
  }

  
  public void setEntityAttributesLdapMatchingExpression(
      String entityAttributesLdapMatchingExpression) {
    this.entityAttributesLdapMatchingExpression = entityAttributesLdapMatchingExpression;
  }

  
  public void setEntityAttributesLdapLastUpdatedAttribute(
      String entityAttributesLdapLastUpdatedAttribute) {
    this.entityAttributesLdapLastUpdatedAttribute = entityAttributesLdapLastUpdatedAttribute;
  }

  
  public void setEntityAttributesLdapLastUpdatedAttributeFormat(
      String entityAttributesLdapLastUpdatedAttributeFormat) {
    this.entityAttributesLdapLastUpdatedAttributeFormat = entityAttributesLdapLastUpdatedAttributeFormat;
  }

  
  public void setGroupIdOfUsersToProvision(String groupIdOfUsersToProvision) {
    this.groupIdOfUsersToProvision = groupIdOfUsersToProvision;
  }

  
  public void setDiagnosticsGroupName(String diagnosticsGroupName) {
    this.diagnosticsGroupName = diagnosticsGroupName;
  }

  
  public void setDiagnosticsSubjectIdOrIdentifier(String diagnosticsSubjectIdOrIdentifier) {
    this.diagnosticsSubjectIdOrIdentifier = diagnosticsSubjectIdOrIdentifier;
  }

  
  public void setProvisionableRegex(String provisionableRegex) {
    this.provisionableRegex = provisionableRegex;
  }

  
  public void setTargetGroupAttributeNameToConfig(
      Map<String, GrouperProvisioningConfigurationAttribute> targetGroupAttributeNameToConfig) {
    this.targetGroupAttributeNameToConfig = targetGroupAttributeNameToConfig;
  }

  
  public void setGrouperProvisioningToTargetTranslation(
      Map<String, List<String>> grouperProvisioningToTargetTranslation) {
    this.grouperProvisioningToTargetTranslation = grouperProvisioningToTargetTranslation;
  }

  
  public void setEntityMatchingAttributes(
      List<GrouperProvisioningConfigurationAttribute> entityMatchingAttributes) {
    this.entityMatchingAttributes = entityMatchingAttributes;
  }

  
  public void setGroupMatchingAttributes(
      List<GrouperProvisioningConfigurationAttribute> groupMatchingAttributes) {
    this.groupMatchingAttributes = groupMatchingAttributes;
  }

  
  public void setGroupMembershipAttributeName(String groupMembershipAttributeName) {
    this.groupMembershipAttributeName = groupMembershipAttributeName;
  }

  
  public void setGroupMembershipAttributeValue(String groupMembershipAttributeValue) {
    this.groupMembershipAttributeValue = groupMembershipAttributeValue;
  }

  
  public void setEntityMembershipAttributeName(String entityMembershipAttributeName) {
    this.entityMembershipAttributeName = entityMembershipAttributeName;
  }

  
  public void setEntityMembershipAttributeValue(String entityMembershipAttributeValue) {
    this.entityMembershipAttributeValue = entityMembershipAttributeValue;
  }

  
  public void setGroupMatchingAttributeSameAsSearchAttribute(
      boolean groupMatchingAttributeSameAsSearchAttribute) {
    this.groupMatchingAttributeSameAsSearchAttribute = groupMatchingAttributeSameAsSearchAttribute;
  }

  
  public void setEntityMatchingAttributeSameAsSearchAttribute(
      boolean entityMatchingAttributeSameAsSearchAttribute) {
    this.entityMatchingAttributeSameAsSearchAttribute = entityMatchingAttributeSameAsSearchAttribute;
  }

  
  public void setGroupAttributeValueCacheHas(boolean groupAttributeValueCacheHas) {
    this.groupAttributeValueCacheHas = groupAttributeValueCacheHas;
  }

  
  public void setGroupAttributeDbCaches(
      GrouperProvisioningConfigurationAttributeDbCache[] groupAttributeDbCaches) {
    this.groupAttributeDbCaches = groupAttributeDbCaches;
  }

  
  public void setEntityAttributeDbCaches(
      GrouperProvisioningConfigurationAttributeDbCache[] entityAttributeDbCaches) {
    this.entityAttributeDbCaches = entityAttributeDbCaches;
  }

  
  public void setOperateOnGrouperEntities(boolean operateOnGrouperEntities) {
    this.operateOnGrouperEntities = operateOnGrouperEntities;
  }

  
  public void setOperateOnGrouperMemberships(boolean operateOnGrouperMemberships) {
    this.operateOnGrouperMemberships = operateOnGrouperMemberships;
  }

  
  public void setOperateOnGrouperGroups(boolean operateOnGrouperGroups) {
    this.operateOnGrouperGroups = operateOnGrouperGroups;
  }

  
  public void setConfigured(boolean configured) {
    this.configured = configured;
  }

  
  public void setTargetEntityAttributeNameToConfig(
      Map<String, GrouperProvisioningConfigurationAttribute> targetEntityAttributeNameToConfig) {
    this.targetEntityAttributeNameToConfig = targetEntityAttributeNameToConfig;
  }

  
  public void setTargetMembershipAttributeNameToConfig(
      Map<String, GrouperProvisioningConfigurationAttribute> targetMembershipAttributeNameToConfig) {
    this.targetMembershipAttributeNameToConfig = targetMembershipAttributeNameToConfig;
  }
  
  
  
  
  
}
