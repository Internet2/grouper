---
title: "Syncing objects to Grouper from SQL"
space: Grouper
pageId: 28554393
version: 7
lastUpdated: 2026-07-01T05:40:34.854Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554393/Syncing+objects+to+Grouper+from+SQL
---

This feature is available in Grouper v2.5.44+. If you are running large syncs make sure your daemon has a lot of memory and that large jobs do not run at the same time

This example shows you can provide your own SQL. If you are syncing from another Grouper instance you can let [Grouper automatically generate the queries](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555092/Syncing+objects+to+Grouper+from+another+Grouper+instance) and detect which columns are available.

## Example from training environment

1. Get the env up and running, identify some folders to sync. Lets get the app:vpn and the ref folders
2. We need a database connection to that database from our database
3. Note: you need a consistent subject source
  
  
  
  
  ```
  subjectApi.source.ldap.id = ldap
  subjectApi.source.ldap.name = EDU Ldap 
  subjectApi.source.ldap.types = person
  subjectApi.source.ldap.adapterClass = edu.internet2.middleware.grouper.subj.GrouperJndiSourceAdapter
  subjectApi.source.ldap.param.ldapServerId.value = grouperTrainingLdap
  subjectApi.source.ldap.param.SubjectID_AttributeType.value = uid
  subjectApi.source.ldap.param.SubjectID_formatToLowerCase.value = false
  subjectApi.source.ldap.param.Name_AttributeType.value = cn
  subjectApi.source.ldap.param.Description_AttributeType.value = cn
  subjectApi.source.ldap.param.VTLDAP_VALIDATOR.value = ConnectLdapValidator
  
  subjectApi.source.ldap.param.SubjectID_AttributeType.value = uid
  subjectApi.source.ldap.param.SubjectID_formatToLowerCase.value = false
  subjectApi.source.ldap.param.Name_AttributeType.value = cn
  subjectApi.source.ldap.param.Description_AttributeType.value = cn
  subjectApi.source.ldap.param.subjectVirtualAttribute_0_searchAttribute0.value = ${subjectUtils.defaultIfBlank(subject.getAttributeValueOrCommaSeparated('uid'), "")},${subjectUtils.defaultIfBlank(subject.getAttributeValueOrCommaSeparated('cn'), "")},${subjectUtils.defaultIfBlank(subject.getAttributeValueOrCommaSeparated('exampleEduRegId'), "")}
  subjectApi.source.ldap.param.sortAttribute0.value = cn
  subjectApi.source.ldap.param.searchAttribute0.value = searchAttribute0
  
  subjectApi.source.ldap.param.subjectIdentifierAttribute0.value = employeeNumber
  
  subjectApi.source.ldap.search.searchSubject.param.filter.value = (&(uid=%TERM%)(objectclass=person))
  subjectApi.source.ldap.search.searchSubject.param.scope.value = SUBTREE_SCOPE
  subjectApi.source.ldap.search.searchSubject.param.base.value = ou=people,dc=internet2,dc=edu
  
  subjectApi.source.ldap.search.searchSubjectByIdentifier.param.filter.value = (&(employeeNumber=%TERM%)(objectclass=person))
  subjectApi.source.ldap.search.searchSubjectByIdentifier.param.scope.value = SUBTREE_SCOPE
  subjectApi.source.ldap.search.searchSubjectByIdentifier.param.base.value = ou=people,dc=internet2,dc=edu
  
  subjectApi.source.ldap.search.search.param.filter.value = (&(|(|(uid=%TERM%)(cn=*%TERM%*))(uid=%TERM%*))(objectclass=person))
  subjectApi.source.ldap.search.search.param.scope.value = SUBTREE_SCOPE
  subjectApi.source.ldap.search.search.param.base.value = ou=people,dc=internet2,dc=edu
  
  subjectApi.source.ldap.internalAttributes = searchAttribute0
  
  
  ```
4. Configure the sync to grouper from the training env  
    
  Use these queries, adjust the where clause so it matches the folders you want to sync. Run them first and make sure they work.
  
  
  ```
  Folders:
  select name, id, id_index, display_name, description, alternate_name from grouper_stems gs where name = 'app:vpn' or name like 'app:vpn:%' or name = 'ref' or name like 'ref:%'
  
  Groups:
  select name, alternate_name, description, disabled_timestamp, display_name, enabled_timestamp, id, id_index, type_of_group from grouper_groups gg where gg.name like 'app:vpn:%' or gg.name like 'ref:%'
  
  Composites:
  SELECT gc.id, group_owner.name AS owner_name, group_left_factor.name AS left_factor_name, group_right_factor.name AS right_factor_name, gc.type FROM grouper_composites gc, grouper_groups group_owner, grouper_groups group_left_factor, grouper_groups group_right_factor WHERE gc.owner = group_owner.id AND gc.left_factor = group_left_factor.id AND gc.right_factor = group_right_factor.id and ( group_owner.name like 'app:vpn:%' or group_owner.name like 'ref:%' ) 
  
  Memberships:
  v2.4+
  SELECT gmav.immediate_membership_id AS immediate_membership_id, gg.name AS group_name, gm.subject_source AS subject_source_id, gm.subject_id, gm.subject_identifier0 AS subject_identifier, gmav.immediate_mship_disabled_time, gmav.immediate_mship_enabled_time FROM grouper_memberships_all_v gmav, grouper_members gm, grouper_groups gg, grouper_fields gf WHERE gmav.mship_type = 'immediate' AND gmav.owner_group_id = gg.id AND gmav.member_id = gm.id AND gmav.field_id = gf.id AND gf.name = 'members' and ( gg.name like 'app:vpn:%' or gg.name like 'ref:%' ) 
  
  v2.3
  SELECT gmav.immediate_membership_id AS immediate_membership_id, gg.name AS group_name, gm.subject_source AS subject_source_id, gm.subject_id, (select gg2.name from grouper_groups gg2 where gm.subject_source='g:gsa' and gg2.id = gm.subject_id) as subject_identifier, gmav.immediate_mship_disabled_time, gmav.immediate_mship_enabled_time FROM grouper_memberships_all_v gmav, grouper_members gm, grouper_groups gg, grouper_fields gf WHERE gmav.mship_type = 'immediate' AND gmav.owner_group_id = gg.id AND gmav.member_id = gm.id AND gmav.field_id = gf.id AND gf.name = 'members' and ( gg.name like 'app:vpn:%' or gg.name like 'ref:%' )
  
  Group privileges:
  v2.4+
  SELECT gmav.immediate_membership_id AS immediate_membership_id, gg.name AS group_name, gm.subject_source AS subject_source_id, gm.subject_id, gm.subject_identifier0 AS subject_identifier, gf.name as field_name FROM grouper_memberships_all_v gmav, grouper_members gm, grouper_groups gg, grouper_fields gf WHERE gmav.mship_type = 'immediate' AND gmav.immediate_mship_enabled = 'T' AND gmav.owner_group_id = gg.id AND gmav.member_id = gm.id AND gmav.field_id = gf.id AND gf.type = 'access' and ( gg.name like 'app:vpn:%' or gg.name like 'ref:%' ) 
  
  v2.3
  SELECT gmav.immediate_membership_id AS immediate_membership_id, gg.name AS group_name, gm.subject_source AS subject_source_id, gm.subject_id, (select gg2.name from grouper_groups gg2 where gm.subject_source='g:gsa' and gg2.id = gm.subject_id) as subject_identifier, gf.name as field_name FROM grouper_memberships_all_v gmav, grouper_members gm, grouper_groups gg, grouper_fields gf WHERE gmav.mship_type = 'immediate' AND gmav.immediate_mship_enabled = 'T' AND gmav.owner_group_id = gg.id AND gmav.member_id = gm.id AND gmav.field_id = gf.id AND gf.type = 'access' and ( gg.name like 'app:vpn:%' or gg.name like 'ref:%' ) 
  
  
  Folder privileges:
  v2.4+
  SELECT gmav.immediate_membership_id AS immediate_membership_id, gs.name AS stem_name, gm.subject_source AS subject_source_id, gm.subject_id, gm.subject_identifier0 AS subject_identifier, gf.name as field_name FROM grouper_memberships_all_v gmav, grouper_members gm, grouper_stems gs, grouper_fields gf WHERE gmav.mship_type = 'immediate' AND gmav.immediate_mship_enabled = 'T' AND gmav.owner_stem_id = gs.id AND gmav.member_id = gm.id AND gmav.field_id = gf.id AND gf.type = 'naming' and ( gs.name = 'app:vpn' or gs.name like 'app:vpn:%' or gs.name = 'ref' or gs.name like 'ref:%' ) 
  
  v2.3
  SELECT gmav.immediate_membership_id AS immediate_membership_id, gs.name AS stem_name, gm.subject_source AS subject_source_id, gm.subject_id, (select gg2.name from grouper_groups gg2 where gm.subject_source='g:gsa' and gg2.id = gm.subject_id) as subject_identifier, gf.name as field_name FROM grouper_memberships_all_v gmav, grouper_members gm, grouper_stems gs, grouper_fields gf WHERE gmav.mship_type = 'immediate' AND gmav.immediate_mship_enabled = 'T' AND gmav.owner_stem_id = gs.id AND gmav.member_id = gm.id AND gmav.field_id = gf.id AND gf.type = 'naming' and ( gs.name = 'app:vpn' or gs.name like 'app:vpn:%' or gs.name = 'ref' or gs.name like 'ref:%' ) 
  
  
  
  ```
  
  Sample config: grouper-loader.properties
  
  
  ```
  db.grouperTraining.url = jdbc:mysql://localhost:3306/grouper?CharSet=utf8&useUnicode=true&characterEncoding=utf8
  db.grouperTraining.user = root
  ldap.grouperTrainingLdap.pass = password
  ldap.grouperTrainingLdap.searchResultHandlers = org.ldaptive.handler.DnAttributeEntryHandler,edu.internet2.middleware.grouper.ldap.ldaptive.GrouperRangeEntryHandler
  ldap.grouperTrainingLdap.tls = false
  ldap.grouperTrainingLdap.uiTestAttributeName = ou
  ldap.grouperTrainingLdap.uiTestExpectedValue = people
  ldap.grouperTrainingLdap.uiTestFilter = (ou=people)
  ldap.grouperTrainingLdap.uiTestSearchDn = dc=internet2,dc=edu
  ldap.grouperTrainingLdap.uiTestSearchScope = ONELEVEL_SCOPE
  ldap.grouperTrainingLdap.url = ldap://localhost:389/
  ldap.grouperTrainingLdap.user = cn=root,dc=internet2,dc=edu
  otherJob.syncToGrouperFromTrainingDb.class = edu.internet2.middleware.grouper.app.syncToGrouper.SyncToGrouperFromSqlDaemon
  otherJob.syncToGrouperFromTrainingDb.quartzCron = 0 03 5 * * ?
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperAutoconfigureColumns = false
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperCompositeDeleteExtra = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperCompositeInsert = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperCompositeSql = SELECT gc.id, group_owner.name AS owner_name, group_left_factor.name AS left_factor_name, group_right_factor.name AS right_factor_name, gc.type FROM grouper_composites gc, grouper_groups group_owner, grouper_groups group_left_factor, grouper_groups group_right_factor WHERE gc.owner = group_owner.id AND gc.left_factor = group_left_factor.id AND gc.right_factor = group_right_factor.id and ( group_owner.name like 'app:vpn:%' or group_owner.name like 'ref:%' )
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperCompositeSync = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperCompositeSyncFieldIdOnInsert = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperCompositeUpdate = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperDatabaseConfigId = grouperTraining
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperFromAnotherGrouper = false
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperGroupDeleteExtra = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperGroupInsert = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperGroupSql = select name, alternate_name, description, disabled_timestamp, display_name, enabled_timestamp, id, id_index, type_of_group from grouper_groups gg where gg.name like 'app:vpn:%' or gg.name like 'ref:%'
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperGroupSync = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperGroupSyncFieldAlternateName = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperGroupSyncFieldDescription = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperGroupSyncFieldDisplayName = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperGroupSyncFieldEnabledDisabled = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperGroupSyncFieldIdIndexOnInsert = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperGroupSyncFieldIdOnInsert = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperGroupSyncFieldTypeOfGroup = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperGroupUpdate = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperLogOutput = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperMembershipDeleteExtra = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperMembershipInsert = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperMembershipSql = SELECT gmav.immediate_membership_id AS immediate_membership_id, gg.name AS group_name, gm.subject_source AS subject_source_id, gm.subject_id, gm.subject_identifier0 AS subject_identifier, gmav.immediate_mship_disabled_time, gmav.immediate_mship_enabled_time FROM grouper_memberships_all_v gmav, grouper_members gm, grouper_groups gg, grouper_fields gf WHERE gmav.mship_type = 'immediate' AND gmav.owner_group_id = gg.id AND gmav.member_id = gm.id AND gmav.field_id = gf.id AND gf.name = 'members' and ( gg.name like 'app:vpn:%' or gg.name like 'ref:%' )
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperMembershipSync = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperMembershipSyncFieldIdOnInsert = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperMembershipSyncFieldsEnabledDisabled = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperMembershipUpdate = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperPrivilegeGroupDeleteExtra = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperPrivilegeGroupInsert = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperPrivilegeGroupSql = SELECT gmav.immediate_membership_id AS immediate_membership_id, gg.name AS group_name, gm.subject_source AS subject_source_id, gm.subject_id, gm.subject_identifier0 AS subject_identifier, gf.name as field_name FROM grouper_memberships_all_v gmav, grouper_members gm, grouper_groups gg, grouper_fields gf WHERE gmav.mship_type = 'immediate' AND gmav.immediate_mship_enabled = 'T' AND gmav.owner_group_id = gg.id AND gmav.member_id = gm.id AND gmav.field_id = gf.id AND gf.type = 'access' and ( gg.name like 'app:vpn:%' or gg.name like 'ref:%' )
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperPrivilegeGroupSync = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperPrivilegeGroupSyncFieldIdOnInsert = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperPrivilegeStemDeleteExtra = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperPrivilegeStemInsert = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperPrivilegeStemSql = SELECT gmav.immediate_membership_id AS immediate_membership_id, gs.name AS stem_name, gm.subject_source AS subject_source_id, gm.subject_id, gm.subject_identifier0 AS subject_identifier, gf.name as field_name FROM grouper_memberships_all_v gmav, grouper_members gm, grouper_stems gs, grouper_fields gf WHERE gmav.mship_type = 'immediate' AND gmav.immediate_mship_enabled = 'T' AND gmav.owner_stem_id = gs.id AND gmav.member_id = gm.id AND gmav.field_id = gf.id AND gf.type = 'naming' and ( gs.name = 'app:vpn' or gs.name like 'app:vpn:%' or gs.name = 'ref' or gs.name like 'ref:%' )
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperPrivilegeStemSync = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperPrivilegeStemSyncFieldIdOnInsert = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperReadonly = false
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperStemDeleteExtra = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperStemInsert = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperStemSql = select name, id, id_index, display_name, description, alternate_name from grouper_stems gs where name = 'app:vpn' or name like 'app:vpn:%' or name = 'ref' or name like 'ref:%'
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperStemSync = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperStemSyncFieldAlternateName = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperStemSyncFieldDescription = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperStemSyncFieldDisplayName = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperStemSyncFieldIdIndexOnInsert = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperStemSyncFieldIdOnInsert = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperStemUpdate = true
  otherJob.syncToGrouperFromTrainingDb.sqlSyncToGrouperTopLevelStems = app:vpn, ref
  
  
  ```
5. Run readonly and see report
  
  
  
  
  ```
  differences: 33
  changeCount: 0
  errors: 0
  stemInserts: 6
  groupInserts: 9
  compositeInserts: 2
  membershipInserts: 10
  groupPrivInserts: 5
  stemPrivInserts: 1
  stemInsertNames: TreeSet size: 6: [0]: app:vpn
  [1]: app:vpn:security
  [2]: app:vpn:service
  [3]: app:vpn:service:basis
  [4]: app:vpn:service:policy
  [5]: app:vpn:service:ref
  
  groupInsertNames: TreeSet size: 9: [0]: app:vpn:security:vpn_ajohnson409_mgr
  [1]: app:vpn:service:basis:someBasis
  [2]: app:vpn:service:policy:vpn_authorized
  [3]: app:vpn:service:policy:vpn_authorized_allow
  [4]: app:vpn:service:policy:vpn_authorized_deny
  [5]: app:vpn:service:ref:adhocAndConsultants
  [6]: app:vpn:service:ref:vpn_adhoc
  [7]: app:vpn:service:ref:vpn_ajohnson409
  [8]: app:vpn:service:ref:vpn_consultants
  
  compositeInsertNames: TreeSet size: 2: [0]: app:vpn:service:policy:vpn_authorized
  [1]: app:vpn:service:ref:adhocAndConsultants
  
  membershipInsertNames: TreeSet size: 10: [0]: app:vpn:security:vpn_ajohnson409_mgr - ajohnson409
  [1]: app:vpn:service:basis:someBasis - dsmith789
  [2]: app:vpn:service:policy:vpn_authorized_allow - app:vpn:service:ref:vpn_adhoc
  [3]: app:vpn:service:policy:vpn_authorized_allow - ref:faculty
  [4]: app:vpn:service:policy:vpn_authorized_allow - ref:staff
  [5]: app:vpn:service:ref:vpn_adhoc - app:vpn:service:ref:vpn_consultants
  [6]: app:vpn:service:ref:vpn_adhoc - ejohnson180
  [7]: app:vpn:service:ref:vpn_adhoc - kdavis311
  [8]: app:vpn:service:ref:vpn_ajohnson409 - bsmith458
  [9]: app:vpn:service:ref:vpn_consultants - jsmith
  
  groupPrivInsertNames: TreeSet size: 5: [0]: app:vpn:service:policy:vpn_authorized - bsmith458 - readers
  [1]: app:vpn:service:policy:vpn_authorized - ejohnson175 - optins
  [2]: app:vpn:service:policy:vpn_authorized - ejohnson175 - viewers
  [3]: app:vpn:service:ref:vpn_ajohnson409 - app:vpn:security:vpn_ajohnson409_mgr - readers
  [4]: app:vpn:service:ref:vpn_ajohnson409 - app:vpn:security:vpn_ajohnson409_mgr - updaters
  
  stemPrivInsertNames: TreeSet size: 1: [0]: app:vpn:service:ref - plangenberg246 - creators
  
  
  ```
6. Run read/write and see changes
  
  
  
  
  ```
  differences: 33
  changeCount: 33
  errors: 0
  stemInserts: 6
  groupInserts: 9
  compositeInserts: 2
  membershipInserts: 10
  groupPrivInserts: 5
  stemPrivInserts: 1
  output: ArrayList size: 33: [0]: Success inserting folder 'app:vpn
  [1]: Success inserting folder 'app:vpn:security
  [2]: Success inserting folder 'app:vpn:service
  [3]: Success inserting folder 'app:vpn:service:basis
  [4]: Success inserting folder 'app:vpn:service:policy
  [5]: Success inserting folder 'app:vpn:service:ref
  [6]: Success inserting group 'app:vpn:security:vpn_ajohnson409_mgr
  [7]: Success inserting group 'app:vpn:service:basis:someBasis
  [8]: Success inserting group 'app:vpn:service:policy:vpn_authorized
  [9]: Success inserting group 'app:vpn:service:policy:vpn_authorized_allow
  [10]: Success inserting group 'app:vpn:service:policy:vpn_authorized_deny
  [11]: Success inserting group 'app:vpn:service:ref:adhocAndConsultants
  [12]: Success inserting group 'app:vpn:service:ref:vpn_adhoc
  [13]: Success inserting group 'app:vpn:service:ref:vpn_ajohnson409
  [14]: Success inserting group 'app:vpn:service:ref:vpn_consultants
  [15]: Success inserting composite 'app:vpn:service:ref:adhocAndConsultants
  [16]: Success inserting composite 'app:vpn:service:policy:vpn_authorized
  [17]: Success inserting membership 'app:vpn:service:ref:vpn_consultants', 'ldap', 'jsmith'
  [18]: Success inserting membership 'app:vpn:service:ref:vpn_adhoc', 'g:gsa', 'app:vpn:service:ref:vpn_consultants'
  [19]: Success inserting membership 'app:vpn:service:ref:vpn_adhoc', 'ldap', 'ejohnson180'
  [20]: Success inserting membership 'app:vpn:security:vpn_ajohnson409_mgr', 'ldap', 'ajohnson409'
  [21]: Success inserting membership 'app:vpn:service:ref:vpn_adhoc', 'ldap', 'kdavis311'
  [22]: Success inserting membership 'app:vpn:service:basis:someBasis', 'ldap', 'dsmith789'
  [23]: Success inserting membership 'app:vpn:service:policy:vpn_authorized_allow', 'g:gsa', 'ref:staff'
  [24]: Success inserting membership 'app:vpn:service:policy:vpn_authorized_allow', 'g:gsa', 'ref:faculty'
  [25]: Success inserting membership 'app:vpn:service:policy:vpn_authorized_allow', 'g:gsa', 'app:vpn:service:ref:vpn_adhoc'
  [26]: Success inserting membership 'app:vpn:service:ref:vpn_ajohnson409', 'ldap', 'bsmith458'
  [27]: Success inserting privilege group 'app:vpn:service:ref:vpn_ajohnson409', 'g:gsa', 'app:vpn:security:vpn_ajohnson409_mgr', updaters
  [28]: Success inserting privilege group 'app:vpn:service:policy:vpn_authorized', 'ldap', 'ejohnson175', optins
  [29]: Success inserting privilege group 'app:vpn:service:policy:vpn_authorized', 'ldap', 'ejohnson175', viewers
  [30]: Success inserting privilege group 'app:vpn:service:ref:vpn_ajohnson409', 'g:gsa', 'app:vpn:security:vpn_ajohnson409_mgr', readers
  [31]: Success inserting privilege group 'app:vpn:service:policy:vpn_authorized', 'ldap', 'bsmith458', readers
  [32]: Success inserting privilege stem 'app:vpn:service:ref', 'ldap', 'plangenberg246', creators
  
  stemInsertNames: TreeSet size: 6: [0]: app:vpn
  [1]: app:vpn:security
  [2]: app:vpn:service
  [3]: app:vpn:service:basis
  [4]: app:vpn:service:policy
  [5]: app:vpn:service:ref
  
  groupInsertNames: TreeSet size: 9: [0]: app:vpn:security:vpn_ajohnson409_mgr
  [1]: app:vpn:service:basis:someBasis
  [2]: app:vpn:service:policy:vpn_authorized
  [3]: app:vpn:service:policy:vpn_authorized_allow
  [4]: app:vpn:service:policy:vpn_authorized_deny
  [5]: app:vpn:service:ref:adhocAndConsultants
  [6]: app:vpn:service:ref:vpn_adhoc
  [7]: app:vpn:service:ref:vpn_ajohnson409
  [8]: app:vpn:service:ref:vpn_consultants
  
  compositeInsertNames: TreeSet size: 2: [0]: app:vpn:service:policy:vpn_authorized
  [1]: app:vpn:service:ref:adhocAndConsultants
  
  membershipInsertNames: TreeSet size: 10: [0]: app:vpn:security:vpn_ajohnson409_mgr - ajohnson409
  [1]: app:vpn:service:basis:someBasis - dsmith789
  [2]: app:vpn:service:policy:vpn_authorized_allow - app:vpn:service:ref:vpn_adhoc
  [3]: app:vpn:service:policy:vpn_authorized_allow - ref:faculty
  [4]: app:vpn:service:policy:vpn_authorized_allow - ref:staff
  [5]: app:vpn:service:ref:vpn_adhoc - app:vpn:service:ref:vpn_consultants
  [6]: app:vpn:service:ref:vpn_adhoc - ejohnson180
  [7]: app:vpn:service:ref:vpn_adhoc - kdavis311
  [8]: app:vpn:service:ref:vpn_ajohnson409 - bsmith458
  [9]: app:vpn:service:ref:vpn_consultants - jsmith
  
  groupPrivInsertNames: TreeSet size: 5: [0]: app:vpn:service:policy:vpn_authorized - bsmith458 - readers
  [1]: app:vpn:service:policy:vpn_authorized - ejohnson175 - optins
  [2]: app:vpn:service:policy:vpn_authorized - ejohnson175 - viewers
  [3]: app:vpn:service:ref:vpn_ajohnson409 - app:vpn:security:vpn_ajohnson409_mgr - readers
  [4]: app:vpn:service:ref:vpn_ajohnson409 - app:vpn:security:vpn_ajohnson409_mgr - updaters
  
  stemPrivInsertNames: TreeSet size: 1: [0]: app:vpn:service:ref - plangenberg246 - creators
  
  
  
  
  
  ```
