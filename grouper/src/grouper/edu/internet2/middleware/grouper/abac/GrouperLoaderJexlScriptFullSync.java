package edu.internet2.middleware.grouper.abac;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataEngine;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.subject.Subject;

/**
 * 
 * @author mchyzer
 *
 */
@DisallowConcurrentExecution
public class GrouperLoaderJexlScriptFullSync extends OtherJobBase {

  private List<GrouperLoaderJexlScriptGroup> grouperLoaderJexlScriptGroups = null;
  
  /**
   * 
   */
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    RuntimeException runtimeException = null;
    try {
      
      GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
      
      GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
      
      grouperDataEngine.loadFieldsAndRows(grouperConfig);

      // TODO cache this
      AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_MARKER, true);
  
      Collection<AttributeAssign> attributeAssigns = GrouperUtil.nonNull(new AttributeAssignFinder().addAttributeDefNameId(attributeDefName.getId()).findAttributeAssignFinderResults().getIdToAttributeAssignMap()).values();
      
      debugMap.put("jexlScriptGroups", GrouperUtil.length(attributeAssigns));

      if (GrouperUtil.length(attributeAssigns) == 0) {
        return null;
      }
      
      this.grouperLoaderJexlScriptGroups = new ArrayList<GrouperLoaderJexlScriptGroup>();
      
      int groupsWithInvalidScripts = 0;
      
      Pattern groupHasMemberPattern = Pattern.compile("entity\\.memberOf\\s*\\(\\s*'([^']+)'\\s*\\)");
      
      Set<String> allGroupNamesInScript = new HashSet<String>();
      Set<String> allGroupIdOwners = new HashSet<String>();
      
      for (AttributeAssign attributeAssign : attributeAssigns) {
        
        GrouperLoaderJexlScriptGroup grouperLoaderJexlScriptGroup = new GrouperLoaderJexlScriptGroup();
        
        if (StringUtils.isBlank(attributeAssign.getOwnerGroupId())) {
          continue;
        }
        allGroupIdOwners.add(attributeAssign.getOwnerGroupId());
        grouperLoaderJexlScriptGroup.setAttributeAssign(attributeAssign);
        grouperLoaderJexlScriptGroup.setGroupId(attributeAssign.getOwnerGroupId());
        
        grouperLoaderJexlScriptGroup.setAttributeAssignId(attributeAssign.getId());

        String script = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_JEXL_SCRIPT);
        
        if (!StringUtils.isBlank(GrouperAbac.validScript(script, grouperDataEngine))) {
          groupsWithInvalidScripts++;
          continue;
        }
        
        // ${ entity.memberOf('test:testGroup0') }
        Matcher groupHasMemberMatcher = groupHasMemberPattern.matcher(script);
        while (groupHasMemberMatcher.find()) {
          String groupName = groupHasMemberMatcher.group(1);
          
          grouperLoaderJexlScriptGroup.getScriptContainsGroupNames().add(groupName);
          allGroupNamesInScript.add(groupName);
        }
        
        grouperLoaderJexlScriptGroup.setScript(script);

        boolean includeInternalSources = GrouperUtil.booleanValue(
            attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAbac.jexlScriptStemName() 
                + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_INCLUDE_INTERNAL_SOURCES), false);
        grouperLoaderJexlScriptGroup.setIncludeInternalSubjectSourceForEntities(includeInternalSources);

        this.grouperLoaderJexlScriptGroups.add(grouperLoaderJexlScriptGroup);
        
      }
      debugMap.put("groupsWithInvalidScripts", groupsWithInvalidScripts);
      debugMap.put("distinctGroupsInScripts", allGroupNamesInScript.size());
      
      Map<String, String> groupNameToId = new HashMap<String, String>();
      Map<String, String> groupIdToName = new HashMap<String, String>();
      for (Group group : GrouperUtil.nonNull(new GroupFinder().assignGroupNames(allGroupNamesInScript).findGroups())) {
        groupNameToId.put(group.getName(), group.getId());
        groupIdToName.put(group.getId(), group.getName());
      }

      Set<String> allGroupIds = new HashSet<String>(allGroupIdOwners);
      allGroupIds.addAll(groupIdToName.keySet());
      List<String> allGroupIdsList = new ArrayList<String>(allGroupIds);
      int batchSize = 900;
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(allGroupIdsList, 900, false);
      Map<String, Set<String>> groupIdToMemberIds = new HashMap<String, Set<String>>();
      Map<String, Set<String>> memberIdToGroupIds = new HashMap<String, Set<String>>();
      Map<String, String> memberIdToSourceId = new HashMap<String, String>();

      // get all memberships

      for (int i=0;i<numberOfBatches;i++) {
        List<String> groupIdsBatch = GrouperUtil.batchList(allGroupIdsList, batchSize, i);
        GcDbAccess gcDbAccess = new GcDbAccess();
        String sql = "select group_id, member_id, subject_source from grouper_memberships_lw_v where group_id in (" 
            + GrouperClientUtils.appendQuestions(GrouperUtil.length(groupIdsBatch)) + ") and list_name = 'members'";
        List<Object[]> results = gcDbAccess.sql(sql).bindVars(GrouperUtil.toArray(groupIdsBatch, Object.class)).selectList(Object[].class);
        for (Object[] row : results) {
          String groupId = (String)row[0];
          String memberId = (String)row[1];
          String subjectSource = (String)row[2];
          
          memberIdToSourceId.put(memberId, subjectSource);

          Set<String> memberIds = groupIdToMemberIds.get(groupId);
          if (memberIds == null) {
            memberIds = new HashSet<String>();
            groupIdToMemberIds.put(groupId, memberIds);
          }
          memberIds.add(memberId);
          
          Set<String> groupIds = memberIdToGroupIds.get(memberId);
          if (groupIds == null) {
            groupIds = new HashSet<String>();
            memberIdToGroupIds.put(memberId, groupIds);
          }
          groupIds.add(groupId);
          
        }
      }

      Map<String, Map<String, Set<Object>>> memberIdToDataFieldAliasToValues = new HashMap<String, Map<String, Set<Object>>>();
      {
        // get attribute assignments
        String sql = "select gda.name, gdfav.member_id, gdfav.subject_source_id, gdfav.value_text, gdfav.value_integer, "
            + " gdfav.data_field_config_id"
            + " from grouper_data_field_assign_v gdfav, grouper_data_alias gda where gda.data_field_internal_id = gdfav.data_field_internal_id ";
        List<Object[]> results = new GcDbAccess().sql(sql).selectList(Object[].class);
        for (Object[] row : results) {
          String aliasName = ((String)row[0]).toLowerCase();
          String memberId = (String)row[1];
          String subjectSource = (String)row[2];
          String valueString = (String)row[3];
          Long valueInt = GrouperUtil.longObjectValue(row[4], true);
          String dataFieldConfigId = (String)row[5];
          
          memberIdToSourceId.put(memberId, subjectSource);
          
          Map<String, Set<Object>> dataFieldAliasToValues = memberIdToDataFieldAliasToValues.get(memberId);
          if (dataFieldAliasToValues == null) {
            dataFieldAliasToValues = new HashMap<>();
            memberIdToDataFieldAliasToValues.put(memberId, dataFieldAliasToValues);
          }
          
          Set<Object> values = dataFieldAliasToValues.get(aliasName);
          if (values == null) {
            values = new HashSet<Object>();
            dataFieldAliasToValues.put(aliasName, values);
          }
          GrouperDataFieldConfig grouperDataFieldConfig = grouperDataEngine.getFieldConfigByConfigId().get(dataFieldConfigId);
          Object value = grouperDataFieldConfig.getFieldDataType().convertValue(valueInt, valueString);
          values.add(value);
        }
      }        
      Map<String, Map<Long, Map<String, Set<Object>>>> memberIdToDataRowAssignInternalIdToDataFieldAliasToValues = new HashMap<>();
      Map<Long, String> rowAssignInternalIdToRowConfigId = new HashMap<>(); 

      {
        Map<Long, String> rowAssignInternalIdToMemberId = new HashMap<>(); 
        // get row assignments
        String sql = "select distinct gda.name, gdrav.member_id  , gdrav.subject_source_id , gdrav.data_row_config_id, gdrav.data_row_internal_id "
            + ", gdrav.data_row_assign_internal_id  from grouper_data_row_assign_v gdrav, grouper_data_alias gda where gda.data_row_internal_id = gdrav.data_row_internal_id ";
        List<Object[]> results = new GcDbAccess().sql(sql).selectList(Object[].class);
        for (Object[] row : results) {
          String aliasName = ((String)row[0]).toLowerCase();
          String memberId = (String)row[1];
          String subjectSource = (String)row[2];
          String dataRowConfigId = (String)row[3];
          Long rowInternalId = GrouperUtil.longValue(row[4]);
          Long rowAssignInternalId = GrouperUtil.longValue(row[5]);
          
          memberIdToSourceId.put(memberId, subjectSource);
          
          Map<String, Set<Object>> dataFieldAliasToValues = memberIdToDataFieldAliasToValues.get(memberId);
          if (dataFieldAliasToValues == null) {
            dataFieldAliasToValues = new HashMap<>();
            memberIdToDataFieldAliasToValues.put(memberId, dataFieldAliasToValues);
          }
          
          rowAssignInternalIdToMemberId.put(rowAssignInternalId, memberId);
          rowAssignInternalIdToRowConfigId.put(rowAssignInternalId, dataRowConfigId);
        }
        
        sql = "select gda.name, gdrfav.value_text, gdrfav.value_integer, gdrfav.data_field_config_id, gdrfav.data_row_assign_internal_id "
          + " from grouper_data_row_field_asgn_v gdrfav, grouper_data_alias gda where gdrfav.data_field_internal_id = gda.data_field_internal_id";

        results = new GcDbAccess().sql(sql).selectList(Object[].class);
        for (Object[] row : results) {
          
          String aliasName = ((String)row[0]).toLowerCase();
          String valueString = (String)row[1];
          Long valueInt = GrouperUtil.longObjectValue(row[2], true);
          String dataFieldConfigId = (String)row[3];
          Long rowAssignInternalId = GrouperUtil.longValue(row[4]);
          
          String memberId = rowAssignInternalIdToMemberId.get(rowAssignInternalId);

          Map<Long, Map<String, Set<Object>>> dataRowAssignInternalIdToDataFieldAliasToValues = memberIdToDataRowAssignInternalIdToDataFieldAliasToValues.get(memberId);
          if (dataRowAssignInternalIdToDataFieldAliasToValues == null) {
            dataRowAssignInternalIdToDataFieldAliasToValues = new HashMap<>();
            memberIdToDataRowAssignInternalIdToDataFieldAliasToValues.put(memberId, dataRowAssignInternalIdToDataFieldAliasToValues);
          }
          
          Map<String, Set<Object>> rowDataFieldAliasToValues = dataRowAssignInternalIdToDataFieldAliasToValues.get(rowAssignInternalId);
          if (rowDataFieldAliasToValues == null) {
            rowDataFieldAliasToValues = new HashMap<>();
            dataRowAssignInternalIdToDataFieldAliasToValues.put(rowAssignInternalId, rowDataFieldAliasToValues);
          }
          
          Set<Object> values = rowDataFieldAliasToValues.get(aliasName);
          if (values == null) {
            values = new HashSet<Object>();
            rowDataFieldAliasToValues.put(aliasName, values);
          }
          GrouperDataFieldConfig grouperDataFieldConfig = grouperDataEngine.getFieldConfigByConfigId().get(dataFieldConfigId);
          Object value = grouperDataFieldConfig.getFieldDataType().convertValue(valueInt, valueString);
          values.add(value);
        }
        
      }        
      

      
      //TODO put things in methods
      //TODO recalc groups which depend on other abac groups
      //TODO add test cases
      // TODO add loader metadata
      int deleteCount = 0;
      int insertCount = 0;
      int errorCount = 0;
      // loop through each owner group
      for (GrouperLoaderJexlScriptGroup grouperLoaderJexlScriptGroup : this.grouperLoaderJexlScriptGroups) {
        
        // lets get all members in all groups
        Set<String> allMemberIdsInAllRelatedGroups = new HashSet<String>();
        Set<String> memberIdsOfOwnerGroup = GrouperUtil.nonNull(groupIdToMemberIds.get(grouperLoaderJexlScriptGroup.getGroupId()));
        
        Group group = grouperLoaderJexlScriptGroup.getGroup();
        for (String memberId : GrouperUtil.nonNull(memberIdsOfOwnerGroup)) {
          String sourceId = memberIdToSourceId.get(memberId);
          if (!grouperLoaderJexlScriptGroup.isIncludeInternalSubjectSourceForEntities() && GrouperAbac.internalSourceId(sourceId)) {
            group.deleteMember(MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), memberId, true));
            deleteCount++;
          } else {
            allMemberIdsInAllRelatedGroups.add(memberId);
          }
        }
        
        allMemberIdsInAllRelatedGroups.addAll(GrouperUtil.nonNull(memberIdsOfOwnerGroup));
        allMemberIdsInAllRelatedGroups.addAll(GrouperUtil.nonNull(memberIdToDataFieldAliasToValues).keySet());

        Set<String> scriptContainsGroupIds = new HashSet<String>();
        for (String groupName : GrouperUtil.nonNull(grouperLoaderJexlScriptGroup.getScriptContainsGroupNames())) {
          String groupId = groupNameToId.get(groupName);
          scriptContainsGroupIds.add(groupId);
          Set<String> theseMemberIds = groupIdToMemberIds.get(groupId);
          for (String memberId : GrouperUtil.nonNull(theseMemberIds)) {
            String sourceId = memberIdToSourceId.get(memberId);
            if (!grouperLoaderJexlScriptGroup.isIncludeInternalSubjectSourceForEntities() && GrouperAbac.internalSourceId(sourceId)) {
              continue;
            }
            allMemberIdsInAllRelatedGroups.add(memberId);
          }
        }
        allMemberIdsInAllRelatedGroups.addAll(memberIdToDataRowAssignInternalIdToDataFieldAliasToValues.keySet());
        
        Map<String, Object> variableMap = new HashMap<String, Object>();

        for (String memberId : allMemberIdsInAllRelatedGroups) {
          
          GrouperAbacEntity grouperAbacEntity = new GrouperAbacEntity();
          grouperAbacEntity.setMemberId(memberId);
          
          Set<String> memberOf = new HashSet<String>();
          grouperAbacEntity.setMemberOfGroupNames(memberOf);
          
          Set<String> groupIds = new HashSet<String>(GrouperUtil.nonNull(memberIdToGroupIds.get(memberId)));
          groupIds.retainAll(scriptContainsGroupIds);
          
          for (String groupId : groupIds) {
            String groupName = groupIdToName.get(groupId);
            memberOf.add(groupName);
          }
          
          grouperAbacEntity.setGrouperDataEngine(grouperDataEngine);
          Map<String, Set<Object>> dataFieldAliasToValues = memberIdToDataFieldAliasToValues.get(memberId);
          grouperAbacEntity.setDataAliasToValues(dataFieldAliasToValues);
          
          Map<Long, Map<String, Set<Object>>> dataRowAssignInternalIdToDataFieldConfigIdToValues  = memberIdToDataRowAssignInternalIdToDataFieldAliasToValues.get(memberId);
          grouperAbacEntity.setDataRowAssignInternalIdToDataFieldAliasToValues(dataRowAssignInternalIdToDataFieldConfigIdToValues);

          variableMap.put("entity", grouperAbacEntity);
          
          try {
            Object result = GrouperUtil.substituteExpressionLanguageScript(grouperLoaderJexlScriptGroup.getScript(), variableMap, true, false, true);
            boolean shouldBeInGroup = GrouperUtil.booleanValue(result, false);
            boolean currentlyInGroup = memberIdsOfOwnerGroup.contains(memberId);
            if (shouldBeInGroup != currentlyInGroup) {
              Member member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), memberId, true);
              if (shouldBeInGroup) {
                Subject subject = member.getSubject();
                group.addMember(subject, false);
                insertCount++;
              } else {
                group.deleteMember(member, false);
                deleteCount++;
              }
            }
            
            
          } catch (RuntimeException re) {
            runtimeException = re;
            LOG.error("Error on memberId: " + memberId, re);
            errorCount++;
          }
          
        }
      }

      debugMap.put("inserts", insertCount);
      otherJobInput.getHib3GrouperLoaderLog().setInsertCount(insertCount);
      debugMap.put("deletes", deleteCount);
      debugMap.put("errors", errorCount);
      otherJobInput.getHib3GrouperLoaderLog().setDeleteCount(deleteCount);
    } catch (RuntimeException re) {
      runtimeException = re;
      debugMap.put("exception", GrouperUtil.getFullStackTrace(re));

    } finally {
      otherJobInput.getHib3GrouperLoaderLog().setJobMessage(GrouperUtil.mapToString(debugMap));
    }
    
    if (runtimeException != null) {
      throw runtimeException;
    }

    if (GrouperUtil.intValue(debugMap.get("errors")) > 0) {
      throw new RuntimeException("Had " + debugMap.get("errors") + " errors, check logs");
    }
    return null;
  }
  
  
  /**
   * run standalone
   */
  public static void runDaemonStandalone() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
    
    hib3GrouperLoaderLog.setHost(GrouperUtil.hostname());
    String jobName = "OTHER_JOB_grouperLoaderJexlScriptFullSync";

    hib3GrouperLoaderLog.setJobName(jobName);
    hib3GrouperLoaderLog.setJobType(GrouperLoaderType.OTHER_JOB.name());
    hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
    hib3GrouperLoaderLog.store();
    
    OtherJobInput otherJobInput = new OtherJobInput();
    otherJobInput.setJobName(jobName);
    otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
    otherJobInput.setGrouperSession(grouperSession);
    new GrouperLoaderJexlScriptFullSync().run(otherJobInput);
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperLoaderJexlScriptFullSync.class);

}
