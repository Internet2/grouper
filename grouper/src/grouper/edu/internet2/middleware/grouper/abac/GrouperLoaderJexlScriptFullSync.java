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

import org.apache.commons.jexl2.ExpressionImpl;
import org.apache.commons.jexl2.parser.ASTJexlScript;
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
import edu.internet2.middleware.grouper.app.serviceLifecycle.GrouperRecentMemberships;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
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
        
        if (!StringUtils.isBlank(GrouperAbac.validScript(script))) {
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
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(allGroupIdsList, 900);
      Map<String, Set<String>> groupIdToMemberIds = new HashMap<String, Set<String>>();
      Map<String, Set<String>> memberIdToGroupIds = new HashMap<String, Set<String>>();
      Map<String, String> memberIdToSourceId = new HashMap<String, String>();

      // get all memberships

      for (int i=0;i<numberOfBatches;i++) {
        List<String> groupIdsBatch = GrouperUtil.batchList(allGroupIdsList, batchSize, i);
        GcDbAccess gcDbAccess = new GcDbAccess();
        String sql = "select group_id, member_id, subject_source from grouper_memberships_lw_v where group_id in (" 
            + GrouperClientUtils.appendQuestions(GrouperUtil.length(groupIdsBatch)) + ")";
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

        Map<String, Object> variableMap = new HashMap<String, Object>();

        for (String memberId : allMemberIdsInAllRelatedGroups) {
          
          GrouperAbacEntity grouperAbacEntity = new GrouperAbacEntity();
          Set<String> memberOf = new HashSet<String>();
          grouperAbacEntity.setMemberOfGroupNames(memberOf);
          
          Set<String> groupIds = new HashSet<String>(GrouperUtil.nonNull(memberIdToGroupIds.get(memberId)));
          groupIds.retainAll(scriptContainsGroupIds);
          
          for (String groupId : groupIds) {
            String groupName = groupIdToName.get(groupId);
            memberOf.add(groupName);
          }
          variableMap.put("entity", grouperAbacEntity);
          
          try {
            Object result = GrouperUtil.substituteExpressionLanguageScript(grouperLoaderJexlScriptGroup.getScript(), variableMap, true, false, true);
            boolean shouldBeInGroup = GrouperUtil.booleanValue(result);
            boolean currentlyInGroup = memberIdsOfOwnerGroup.contains(memberId);
            if (shouldBeInGroup != currentlyInGroup) {
              Member member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), memberId, true);
              if (shouldBeInGroup) {
                Subject subject = member.getSubject();
                group.addMember(subject);
                insertCount++;
              } else {
                group.deleteMember(member);
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
