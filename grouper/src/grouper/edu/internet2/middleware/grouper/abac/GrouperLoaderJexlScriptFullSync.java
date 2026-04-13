package edu.internet2.middleware.grouper.abac;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.internal.Engine;
import org.apache.commons.jexl3.parser.ASTAddNode;
import org.apache.commons.jexl3.parser.ASTAndNode;
import org.apache.commons.jexl3.parser.ASTArguments;
import org.apache.commons.jexl3.parser.ASTArrayLiteral;
import org.apache.commons.jexl3.parser.ASTEQNode;
import org.apache.commons.jexl3.parser.ASTERNode;
import org.apache.commons.jexl3.parser.ASTFunctionNode;
import org.apache.commons.jexl3.parser.ASTGENode;
import org.apache.commons.jexl3.parser.ASTGTNode;
import org.apache.commons.jexl3.parser.ASTIdentifier;
import org.apache.commons.jexl3.parser.ASTIdentifierAccess;
import org.apache.commons.jexl3.parser.ASTJexlScript;
import org.apache.commons.jexl3.parser.ASTLENode;
import org.apache.commons.jexl3.parser.ASTLTNode;
import org.apache.commons.jexl3.parser.ASTMethodNode;
import org.apache.commons.jexl3.parser.ASTNENode;
import org.apache.commons.jexl3.parser.ASTNotNode;
import org.apache.commons.jexl3.parser.ASTNullLiteral;
import org.apache.commons.jexl3.parser.ASTNumberLiteral;
import org.apache.commons.jexl3.parser.ASTOrNode;
import org.apache.commons.jexl3.parser.ASTReference;
import org.apache.commons.jexl3.parser.ASTReferenceExpression;
import org.apache.commons.jexl3.parser.ASTStringLiteral;
import org.apache.commons.jexl3.parser.ASTSubNode;
import org.apache.commons.jexl3.parser.ASTUnaryMinusNode;
import org.apache.commons.jexl3.parser.JexlNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningDaemonLogic;
import edu.internet2.middleware.grouper.app.loader.GrouperDaemonUtils;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.dataField.GrouperDataEngine;
import edu.internet2.middleware.grouper.dataField.GrouperDataField;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldAssign;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldType;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldWrapper;
import edu.internet2.middleware.grouper.dataField.GrouperDataRow;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowWrapper;
import edu.internet2.middleware.grouper.dataField.GrouperPrivacyRealmConfig;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperShutdown;
import edu.internet2.middleware.grouper.plugins.GrouperPluginManager;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheDependency;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheDependencyDao;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheDependencyType;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheDependencyTypeDao;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheGroup;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheGroupDao;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheHistoryFullSyncDaemon;
import edu.internet2.middleware.grouper.util.GrouperCallable;
import edu.internet2.middleware.grouper.util.GrouperFuture;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncHeartbeat;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.subject.Subject;

/**
 * 
 * @author mchyzer
 *
 */
@DisallowConcurrentExecution
public class GrouperLoaderJexlScriptFullSync extends OtherJobBase {
  private static final Pattern recentMemberOfTimePeriodPattern = Pattern.compile("^(\\d+)\\s*(days?|hours?)$", Pattern.CASE_INSENSITIVE);

  public static void main(String[] args) {

    try {
      GrouperSession grouperSession = GrouperSession.startRootSession();
      
      GrouperDataEngine grouperDataEngine = new GrouperDataEngine();

      GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();

      grouperDataEngine.loadFieldsAndRows(grouperConfig);

      Subject subject = SubjectFinder.findById("test.subject.1", true);
      
      //System.out.println(analyzeJexlScriptHtml(grouperDataEngine, "entity.memberOf('penn:ref:mfaEnrolled')", subject, grouperSession.getSubject()));
      System.out.println(analyzeJexlScriptHtml(grouperDataEngine, "'penn:ref:mfaEnrolled' && 'penn:ref:mfaEnrolled2'", subject, grouperSession.getSubject(), false, null, false));

      
      
      //System.out.println(analyzeJexlScriptHtml(grouperDataEngine, "entity.memberOf('test:testGroup')", subject, grouperSession.getSubject()));
      //System.out.println(analyzeJexlScriptHtml(grouperDataEngine, "entity.memberOf('test:testGroup') && entity.memberOf('test:testGroup2')", subject, grouperSession.getSubject()));
      //System.out.println(analyzeJexlScriptHtml(grouperDataEngine, "entity.memberOf('test:testGroup') && !entity.memberOf('test:testGroup2')", subject, grouperSession.getSubject()));
      //System.out.println(analyzeJexlScriptHtml(grouperDataEngine, "entity.memberOf('test:testGroup') && entity.memberOf('test:testGroup2')", subject, grouperSession.getSubject()));
      //System.out.println(analyzeJexlScriptHtml(grouperDataEngine, "entity.hasRow('affiliation', 'name==staff && dept==english')", subject, grouperSession.getSubject()));
      //System.out.println(analyzeJexlScriptHtml(grouperDataEngine, "entity.memberOf('test:testGroup') || (entity.memberOf('test:testGroup2') && entity.memberOf('test:testGroup3'))", subject, grouperSession.getSubject()));
      //System.out.println(analyzeJexlScriptHtml(grouperDataEngine, "entity.hasRow('affiliation', \"affiliationCode=='staff' && affiliationOrg==1234\") "
      //    + "|| (entity.memberOf('test:testGroup') && !entity.memberOf('test:testGroup2'))", subject, grouperSession.getSubject()));
      
      
      //System.out.println(analyzeJexlScriptHtml(grouperDataEngine, "entity.hasRow('cp_user', \"cp_active && !cp_blocked && cp_known && cp_org == 'Perelman School of Medicine' \") "
      //    + "&& entity.memberOf('penn:ref:member') && !entity.memberOf('penn:ref:lockout') && entity.hasAttribute('cp_role', 'desktop-user')", subject, grouperSession.getSubject()));

      //  System.out.println(analyzeJexlScriptHtml(grouperDataEngine, "(entity.hasRow('cp_user', \"(cp_active || !cp_blocked) && cp_known "
      //      + "&& cp_org == 'Perelman School of Medicine' \") "
      //      + " || entity.hasRow('cp_user', \"(cp_active || !cp_blocked) && cp_known "
      //      + "&& cp_org == 'Perelman School of Medicine' \")) && (!entity.memberOf('penn:ref:member') "
      //      + "|| entity.memberOf('penn:ref:lockout') ) && entity.hasAttribute('cp_role', 'desktop-user')", null, subject));
      
      //System.out.println(GrouperUtil.toStringForLog(analyzeJexlScript(grouperDataEngine, "entity.memberOf('test:testGroup')")));
      //System.out.println(GrouperUtil.toStringForLog(analyzeJexlScript(grouperDataEngine, "entity.memberOf('test:testGroup') && entity.memberOf('test:testGroup2')")));
      //System.out.println(GrouperUtil.toStringForLog(analyzeJexlScript(grouperDataEngine, "entity.memberOf('test:testGroup') && !entity.memberOf('test:testGroup2')")));
      //System.out.println(GrouperUtil.toStringForLog(analyzeJexlScript(grouperDataEngine, "entity.memberOf('test:testGroup') || (entity.memberOf('test:testGroup2') && entity.memberOf('test:testGroup3'))")));
      //System.out.println(GrouperUtil.toStringForLog(analyzeJexlScript(grouperDataEngine, "entity.hasAttribute('active')")));
      //System.out.println(GrouperUtil.toStringForLog(analyzeJexlScript(grouperDataEngine, "entity.hasAttribute('active', 'true')")));
      //System.out.println(GrouperUtil.toStringForLog(analyzeJexlScript(grouperDataEngine, "entity.hasRow('affiliation', 'name==staff && dept==english')")));
      
      
      // A & !B
      // A and push A
      // B and push B
      // !B and push !B
      // done
      
      // && is resolved before or (require parens?)
      //System.out.println(GrouperUtil.substituteExpressionLanguageScript("${false && true || true}", new HashMap(), true, false, false));
      
  //    List<MultiKey> arguments = new ArrayList<MultiKey>();
  //    System.out.println(convertJexlScriptToSqlWhereClause("entity.memberOf('test:testGroup')", arguments));
  //    System.out.println(GrouperUtil.toStringForLog(arguments));
  //    arguments.clear();
  //    System.out.println(convertJexlScriptToSqlWhereClause("( entity.memberOf('test:testGroup') && !entity.memberOf('etc:lockout') )", arguments));
  //    System.out.println(GrouperUtil.toStringForLog(arguments));
  //    arguments.clear();
  //    System.out.println(convertJexlScriptToSqlWhereClause("entity.hasAttribute('active')", arguments));
  //    System.out.println(GrouperUtil.toStringForLog(arguments));
  //    arguments.clear();
  //    System.out.println(convertJexlScriptToSqlWhereClause("entity.hasAttribute('active', 'true')", arguments));
  //    System.out.println(GrouperUtil.toStringForLog(arguments));
  //    arguments.clear();
  //    System.out.println(convertJexlScriptToSqlWhereClause("entity.hasAttribute('org', 123)", arguments));
  //    System.out.println(GrouperUtil.toStringForLog(arguments));
  //    arguments.clear();
  //    System.out.println(convertJexlScriptToSqlWhereClause("entity.hasRow('affiliation', 'name==staff && dept==english')", arguments));
  //    System.out.println(GrouperUtil.toStringForLog(arguments));
  //    // ASTJexlScript
  //    // - ASTAndNode
  //    //   - ASTEQNode
  //    //     - ASTIdentifier
  //    //     - ASTIdentifier
  //    //   - ASTEQNode
  //    //     - ASTIdentifier
  //    //     - ASTIdentifier
  //    
  //    arguments.clear();
    } catch (RuntimeException re) {
      re.printStackTrace();
    } finally {
      GrouperLoader.shutdownIfStarted();
      GrouperPluginManager.shutdownIfStarted();
      GrouperShutdown.shutdown();
      System.exit(0);
    }
  }
  
  private static void addMembershipHistoryAbacDependencies(SqlCacheDependencyType sqlCacheDependencyTypeMshipHistoryAbac, Collection<SqlCacheGroup> sqlCacheGroupsToCheck, Map<MultiKey, SqlCacheDependency> sqlCacheDependencies) {
    for (SqlCacheGroup sqlCacheGroup : sqlCacheGroupsToCheck) {
      MultiKey multiKey = new MultiKey(sqlCacheGroup.getInternalId(), sqlCacheGroup.getInternalId());
      if (!sqlCacheDependencies.containsKey(multiKey)) {
        // check if other history dependencies
        List<Long> dependenciesFound = new GcDbAccess().sql("select gscdt.internal_id from grouper_sql_cache_dependency gscd, grouper_sql_cache_depend_type gscdt "
            + "where gscd.dep_type_internal_id = gscdt.internal_id and gscdt.dependency_category='mshipHistory' and owner_internal_id = ?")
            .addBindVar(sqlCacheGroup.getInternalId())
            .selectList(Long.class);
        
        // add the dependency - check just in case something else added it in the meantime
        if (!dependenciesFound.contains(sqlCacheDependencyTypeMshipHistoryAbac.getInternalId())) {
          SqlCacheDependency sqlCacheDependency = new SqlCacheDependency();
          sqlCacheDependency.setDependencyTypeInternalId(sqlCacheDependencyTypeMshipHistoryAbac.getInternalId());
          sqlCacheDependency.setOwnerInternalId(sqlCacheGroup.getInternalId());
          sqlCacheDependency.setDependentInternalId(sqlCacheGroup.getInternalId());
          SqlCacheDependencyDao.store(sqlCacheDependency);
          
          sqlCacheDependencies.put(multiKey, sqlCacheDependency);
        }
        
        if (dependenciesFound.size() == 0) {
          // we need to add the history
          SqlCacheHistoryFullSyncDaemon.syncMembershipHistory(sqlCacheGroup, null, null);
        }
      }
    }
  }
  
  /**
   * 
   * @param grouperDataEngine
   * @param jexlScript
   * @param subject
   * @param loggedInSubject
   * @param readOnly - true if only analyzing; false if about to save the script
   * @return
   */
  public static GrouperJexlScriptAnalysis analyzeJexlScriptHtml(GrouperDataEngine grouperDataEngine, String jexlScript,
      Subject subject, Subject loggedInSubject, boolean readOnly) {
    return analyzeJexlScriptHtml(grouperDataEngine, jexlScript, subject, loggedInSubject, readOnly, null, false);
  }

  /**
   * analyze a jexl script and return the analysis with counts and subject checks
   * @param grouperDataEngine
   * @param jexlScript
   * @param subject
   * @param loggedInSubject
   * @param readOnly - true if only analyzing; false if about to save the script
   * @param effectiveSourceIds - the effective subject source IDs to use for the count query, or null for global defaults
   * @param buildVisualization - true if this is being called to build visualization
   * @return
   */
  public static GrouperJexlScriptAnalysis analyzeJexlScriptHtml(GrouperDataEngine grouperDataEngine, String jexlScript,
      Subject subject, Subject loggedInSubject, boolean readOnly, Set<String> effectiveSourceIds, boolean buildVisualization) {
    
    Member member = subject != null ? MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, true): null;
    
    GrouperJexlScriptAnalysis grouperJexlScriptAnalysis = analyzeJexlScript(grouperDataEngine, jexlScript);
      
    if (GrouperUtil.length(grouperJexlScriptAnalysis.getRecentMemberOfGroupNames()) > 0) {
      Set<MultiKey> groupNamesAndFieldNames = new HashSet<>();
      for (String groupName : grouperJexlScriptAnalysis.getRecentMemberOfGroupNames()) {
        groupNamesAndFieldNames.add(new MultiKey(groupName, "members"));
      }
      
      Collection<SqlCacheGroup> sqlCacheGroups = SqlCacheGroupDao.retrieveByGroupNamesFieldNames(groupNamesAndFieldNames).values();
      
      SqlCacheDependencyType sqlCacheDependencyTypeMshipHistoryAbac = SqlCacheDependencyTypeDao.retrieveByDependencyCategoryAndName("mshipHistory", "mshipHistory_abac");
      Set<MultiKey> ownerInternalIdsDependentInternalIds = new HashSet<>();
      for (SqlCacheGroup sqlCacheGroup : sqlCacheGroups) {
        ownerInternalIdsDependentInternalIds.add(new MultiKey(sqlCacheGroup.getInternalId(), sqlCacheGroup.getInternalId()));
      }
      
      Map<MultiKey, SqlCacheDependency> sqlCacheDependencies = SqlCacheDependencyDao.retrieveByDepTypeInternalIdAndOwnerInternalIdsDependentInternalIds(sqlCacheDependencyTypeMshipHistoryAbac.getInternalId(), ownerInternalIdsDependentInternalIds);
      
      // go through and see which ones don't have the mshipHistory_abac dependency
      addMembershipHistoryAbacDependencies(sqlCacheDependencyTypeMshipHistoryAbac, sqlCacheGroups, sqlCacheDependencies);
    }
    
    for (GrouperJexlScriptPart grouperJexlScriptPart : grouperJexlScriptAnalysis.getGrouperJexlScriptParts()) {
      
      GcDbAccess gcDbAccess = new GcDbAccess();
      String whereClause = grouperJexlScriptPart.getWhereClause().toString();
      int argumentIndex = 0;
      
      String previousAttributeAlias = null;
      
      boolean partsHaveMissingGroup = false;
      for (MultiKey argument : grouperJexlScriptPart.getArguments()) {
        String argumentString = (String)argument.getKey(0);
        if (StringUtils.equals(argumentString, "group")) {
          String fieldName = (String)argument.getKey(1);
          if (!StringUtils.equals(fieldName, "members")) {
            throw new RuntimeException("Not expecting field: '" + fieldName + "'");
          }
          String groupName = (String)argument.getKey(2);
          //TODO make this more efficient
          Map<MultiKey, SqlCacheGroup> sqlCacheGroups = SqlCacheGroupDao.retrieveByGroupNamesFieldNames(GrouperUtil.toList(new MultiKey(groupName, fieldName)));
          // if group not found, consider it empty
          long sqlCacheGroupInternalId = -1;
          boolean hasRead = false;
          if (GrouperUtil.length(sqlCacheGroups) == 1) {
            sqlCacheGroupInternalId = sqlCacheGroups.values().iterator().next().getInternalId();
            
            hasRead = (boolean) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
              
              @Override
              public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
                Group group = GroupFinder.findByName(grouperSession, groupName, false);
                if (group != null) {
                  return group.canHavePrivilege(loggedInSubject, AccessPrivilege.READ.getName(), false);
                }
                return false;
              }
            });
            if (!hasRead) {
              String errorMessage = GrouperTextContainer.textOrNull(
                  "grouperLoaderEditJexlScriptAnalysisUserNotAllowedToReadGroup");
              grouperJexlScriptAnalysis
                  .setErrorMessage(errorMessage + " '" + groupName + "'");
              return grouperJexlScriptAnalysis;
            }
            
          } else {
            // note non-existent group
            partsHaveMissingGroup = true;
          }
          gcDbAccess.addBindVar(sqlCacheGroupInternalId);
          //TODO are we considering group READ like we do with attributes below?
        } else if (StringUtils.equals(argumentString, "attribute")) {
          String attributeAlias = (String)argument.getKey(1);
          GrouperDataFieldWrapper grouperDataFieldWrapper = grouperDataEngine.getGrouperDataProviderIndex().getFieldWrapperByLowerAlias().get(attributeAlias.toLowerCase());
          if (grouperDataFieldWrapper == null) {
            throw new RuntimeException("Data field '" + attributeAlias + "' not found!");
          }
          
          GrouperDataFieldConfig grouperDataFieldConfig = grouperDataEngine.getFieldConfigByAlias().get(attributeAlias.toLowerCase());
          
          String grouperPrivacyRealmConfigId = grouperDataFieldConfig.getGrouperPrivacyRealmConfigId();
          
          GrouperPrivacyRealmConfig grouperPrivacyRealmConfig = grouperDataEngine.getPrivacyRealmConfigByConfigId().get(grouperPrivacyRealmConfigId);
          
          String highestLevelAccess = grouperDataEngine.calculateHighestLevelAccess(grouperPrivacyRealmConfig, loggedInSubject);
           
          if (!readOnly && !StringUtils.equals(highestLevelAccess, "update")) {
            String warningMessage = GrouperTextContainer.textOrNull("grouperLoaderEditJexlScriptAnalysisUserNotAllowedToEditPolicy");
            grouperJexlScriptAnalysis.setErrorMessage(warningMessage + " '"+attributeAlias + "'");
            return grouperJexlScriptAnalysis;
          }
          
          if (StringUtils.equals(highestLevelAccess, "read")) {
            String warningMessage = GrouperTextContainer.textOrNull("grouperLoaderEditJexlScriptAnalysisUserNotAllowedToEditPolicy");
            grouperJexlScriptAnalysis.setWarningMessage(warningMessage + " '"+attributeAlias + "'");
          } else if (StringUtils.equals(highestLevelAccess, "") || StringUtils.equals(highestLevelAccess, "view")) {
            String errorMessage = GrouperTextContainer.textOrNull("grouperLoaderEditJexlScriptAnalysisUserNotAllowedToViewAttribute");
            grouperJexlScriptAnalysis.setErrorMessage(errorMessage + " '"+attributeAlias + "'");
            return grouperJexlScriptAnalysis;
          }
          
          GrouperDataField grouperDataField = grouperDataFieldWrapper.getGrouperDataField();
          gcDbAccess.addBindVar(grouperDataField.getInternalId());
          
          previousAttributeAlias = attributeAlias;
          
        } else if (StringUtils.equals(argumentString, "row")) {
          String rowAlias = (String)argument.getKey(1);
          GrouperDataRowWrapper grouperDataRowWrapper = grouperDataEngine.getGrouperDataProviderIndex().getRowWrapperByLowerAlias().get(rowAlias.toLowerCase());
          GrouperDataRow grouperDataRow = grouperDataRowWrapper.getGrouperDataRow();
          gcDbAccess.addBindVar(grouperDataRow.getInternalId());
  
        } else if (StringUtils.equals(argumentString, "attributeValue")) {
          
          MultiKey argumentNameMultiKey = grouperJexlScriptPart.getArguments().get(argumentIndex-1);
          String argumentPreviousString = (String)argumentNameMultiKey.getKey(0);
          boolean isAttribute = StringUtils.equals(argumentPreviousString, "attribute");
          
          GrouperDataFieldWrapper grouperDataFieldWrapper = grouperDataEngine.getGrouperDataProviderIndex().getFieldWrapperByLowerAlias().get(previousAttributeAlias.toLowerCase());
          GrouperDataField grouperDataField = grouperDataFieldWrapper.getGrouperDataField();
          
          GrouperDataFieldConfig grouperDataFieldConfig = grouperDataEngine.getFieldConfigByAlias().get(previousAttributeAlias.toLowerCase());
          GrouperDataFieldType fieldDataType = grouperDataFieldConfig.getFieldDataType();
          GrouperDataFieldAssign grouperDataFieldAssign = new GrouperDataFieldAssign();
          
          Object value = argument.getKey(1);
          fieldDataType.assignValue(grouperDataFieldAssign, value, grouperDataEngine.getGrouperDataProviderIndex().getDictionaryTextByString());
          
          if (fieldDataType == GrouperDataFieldType.bool || fieldDataType == GrouperDataFieldType.integer || fieldDataType == GrouperDataFieldType.timestamp) {
            
            if (grouperDataFieldAssign.getValueInteger() != null) {
            
              gcDbAccess.addBindVar(grouperDataFieldAssign.getValueInteger());
            
            }
            
            if (isAttribute) {                
              whereClause = StringUtils.replace(whereClause, "$$ATTRIBUTE_COL_" + argumentIndex + "$$", "value_integer");
            }
            
          } else if (fieldDataType == GrouperDataFieldType.string) {

            if (grouperDataFieldAssign.getValueDictionaryInternalId() != null) {

              gcDbAccess.addBindVar(grouperDataFieldAssign.getValueDictionaryInternalId());

            }
            if (isAttribute) {
              whereClause = StringUtils.replace(whereClause, "$$ATTRIBUTE_COL_" + argumentIndex + "$$", "value_dictionary_internal_id");
            }
  
          } else {
            throw new RuntimeException("not expecting type: " + fieldDataType.getClass().getName());
          }
  
        } else if (StringUtils.equals(argumentString, "bindVar")) {
          
          Object value = argument.getKey(1);
          gcDbAccess.addBindVar(value);
        } else if (StringUtils.equals(argumentString, "attributeCompareLeft") || StringUtils.equals(argumentString, "attributeCompareRight")) {

          String fieldAlias = (String)argument.getKey(1);
          Long mathOffset = (Long)argument.getKey(2);

          GrouperDataFieldWrapper grouperDataFieldWrapper = grouperDataEngine.getGrouperDataProviderIndex().getFieldWrapperByLowerAlias().get(fieldAlias.toLowerCase());
          GrouperDataField grouperDataField = grouperDataFieldWrapper.getGrouperDataField();
          gcDbAccess.addBindVar(grouperDataField.getInternalId());

          if (mathOffset != null) {
            gcDbAccess.addBindVar(mathOffset);
          }
        }
        argumentIndex++;
      }
      MultiKey sourceInClause = GrouperAbac.subjectSourceInClause(effectiveSourceIds);
      String sourceInSql = (String)sourceInClause.getKey(0);
      List<String> sourceBindVars = (List<String>)sourceInClause.getKey(1);

      // prepend source bind vars before the existing ones
      List<Object> allBindVars = new ArrayList<Object>();
      allBindVars.addAll(sourceBindVars);
      if (gcDbAccess.getBindVars() != null) {
        allBindVars.addAll(gcDbAccess.getBindVars());
      }

      String sql = "select count(1) from grouper_members gm where " + sourceInSql + " and gm.subject_resolution_deleted = 'F' and gm.subject_resolution_resolvable = 'T' and ( " + whereClause + " )";

  //    System.out.println(script);
  //    System.out.println(sql);

      int count = gcDbAccess.bindVars(allBindVars).sql(sql).select(Integer.class);
      grouperJexlScriptPart.setPopulationCount(count);

      if (partsHaveMissingGroup) {
        StringBuilder newDescription = new StringBuilder(grouperJexlScriptPart.getDisplayDescription());
        newDescription.append(GrouperTextContainer.textOrNull("jexlAnalysisMemberOfGroupMissingWarning"));
        grouperJexlScriptPart.setDisplayDescription(newDescription);
      }

      if (subject != null) {
        sql += " and gm.id = ?";
        allBindVars.add(member.getId());
        count = gcDbAccess.bindVars(allBindVars).sql(sql).select(Integer.class);
        grouperJexlScriptPart.setContainsSubject(count>0);
      }
      
    }

    // check for unresolvable or deleted subjects matching the overall script
    {
      GcDbAccess gcDbAccessUnresolvable = new GcDbAccess();
      GrouperJexlScriptSql grouperJexlScriptSqlUnresolvable = generateJexlSql(grouperDataEngine, gcDbAccessUnresolvable, grouperJexlScriptAnalysis);

      MultiKey sourceInClauseUnresolvable = GrouperAbac.subjectSourceInClause(effectiveSourceIds);
      String sourceInSqlUnresolvable = (String)sourceInClauseUnresolvable.getKey(0);
      List<String> sourceBindVarsUnresolvable = (List<String>)sourceInClauseUnresolvable.getKey(1);

      List<Object> allBindVarsUnresolvable = new ArrayList<Object>();
      allBindVarsUnresolvable.addAll(sourceBindVarsUnresolvable);
      if (gcDbAccessUnresolvable.getBindVars() != null) {
        allBindVarsUnresolvable.addAll(gcDbAccessUnresolvable.getBindVars());
      }

      String unresolvableSql = "select count(1) from grouper_members gm where " + sourceInSqlUnresolvable
          + " and (gm.subject_resolution_deleted = 'T' or gm.subject_resolution_resolvable = 'F')"
          + " and ( " + grouperJexlScriptSqlUnresolvable.getWhereClause() + " )";

      int unresolvableCount = new GcDbAccess().bindVars(allBindVarsUnresolvable).sql(unresolvableSql).select(Integer.class);

      if (unresolvableCount > 0) {
        // get up to 10 example subject IDs
        String exampleSql = "select gm.subject_id from grouper_members gm where " + sourceInSqlUnresolvable
            + " and (gm.subject_resolution_deleted = 'T' or gm.subject_resolution_resolvable = 'F')"
            + " and ( " + grouperJexlScriptSqlUnresolvable.getWhereClause() + " )";

        List<Object> exampleBindVars = new ArrayList<Object>();
        exampleBindVars.addAll(sourceBindVarsUnresolvable);
        if (gcDbAccessUnresolvable.getBindVars() != null) {
          exampleBindVars.addAll(gcDbAccessUnresolvable.getBindVars());
        }

        List<String> unresolvableSubjectIds = new GcDbAccess().bindVars(exampleBindVars).sql(exampleSql).selectList(String.class);
        int exampleCount = Math.min(unresolvableSubjectIds.size(), 10);
        List<String> examples = unresolvableSubjectIds.subList(0, exampleCount);

        String warningText = GrouperTextContainer.textOrNull("jexlAnalysisUnresolvableSubjectsWarning");
        if (StringUtils.isBlank(warningText)) {
          warningText = "There are " + unresolvableCount + " unresolvable or deleted entities matching this script, first " + exampleCount + ": " + StringUtils.join(examples, ", ");
        } else {
          warningText = StringUtils.replace(warningText, "##unresolvableCount##", String.valueOf(unresolvableCount));
          warningText = StringUtils.replace(warningText, "##exampleCount##", String.valueOf(exampleCount));
          warningText = StringUtils.replace(warningText, "##subjectIds##", StringUtils.join(examples, ", "));
        }

        String existingWarning = grouperJexlScriptAnalysis.getWarningMessage();
        if (StringUtils.isNotBlank(existingWarning)) {
          grouperJexlScriptAnalysis.setWarningMessage(existingWarning + "<br />" + warningText);
        } else {
          grouperJexlScriptAnalysis.setWarningMessage(warningText);
        }
      }
    }
    
    if (buildVisualization) {
      // Build visualization tree after population counts are computed
      try {
        grouperJexlScriptAnalysis.setVisualizationReferences(
            buildVisualizationTreeFromParts(grouperJexlScriptAnalysis.getGrouperJexlScriptParts()));
      } catch (Exception e) {
        LOG.warn("Error building visualization references: " + e.getMessage(), e);
        grouperJexlScriptAnalysis.setVisualizationReferences(new ArrayList<AbacReference>());
      }
    }

    return grouperJexlScriptAnalysis;
  }

  /**
   *
   * @param jexlStript
   * @param arguments first one is type (e.g. group), second is list (e.g. members), third is name (e.g. test:testGroup).  Used for bind variables
   * @return the sql
   */
  public static GrouperJexlScriptAnalysis analyzeJexlScript(GrouperDataEngine grouperDataEngine, String jexlStript) {

    jexlStript = jexlStript.trim();
    if (jexlStript.startsWith("${") && jexlStript.endsWith("}")) {
      jexlStript = jexlStript.substring(2, jexlStript.length()-1);
    }
    
    JexlEngine jexlEngine = new Engine();

    // TODO dont mess with values in strings
    jexlStript = GrouperUtil.replace(jexlStript, "\n", " ");
    jexlStript = GrouperUtil.replace(jexlStript, "\r", " ");
    jexlStript = jexlStript.replaceAll("!\\s+", "!");
    
    JexlExpression expression = (JexlExpression)jexlEngine.createExpression(jexlStript);

    ASTJexlScript astJexlScript = (ASTJexlScript)GrouperUtil.fieldValue(expression, "script");

    GrouperJexlScriptAnalysis grouperJexlScriptAnalysis = new GrouperJexlScriptAnalysis();
    grouperJexlScriptAnalysis.setGrouperDataEngine(grouperDataEngine);
    GrouperJexlScriptPart grouperJexlScriptPart = new GrouperJexlScriptPart();
    grouperJexlScriptPart.setPartRole(GrouperJexlScriptPart.PartRole.FULL_EXPRESSION);
    grouperJexlScriptAnalysis.getGrouperJexlScriptParts().add(grouperJexlScriptPart);

    analyzeJexlScriptToSqlHelper(grouperJexlScriptAnalysis, grouperJexlScriptPart, astJexlScript, true);

    for (GrouperJexlScriptPart currentGrouperJexlScriptPart : grouperJexlScriptAnalysis.getGrouperJexlScriptParts()) {
      if (currentGrouperJexlScriptPart.getDisplayDescription().length() > 0) {
        currentGrouperJexlScriptPart.getDisplayDescription().setCharAt(0, Character.toUpperCase(currentGrouperJexlScriptPart.getDisplayDescription().charAt(0)));
      }
    }
    return grouperJexlScriptAnalysis;
  }

  public static void analyzeJexlScriptToSqlHelper(GrouperJexlScriptAnalysis grouperJexlScriptAnalysis,
      GrouperJexlScriptPart theGrouperJexlScriptPart, JexlNode jexlNode, boolean clonePart) {
    GrouperJexlScriptPart grouperJexlScriptPartClone = null;
    if (jexlNode instanceof ASTJexlScript && 1==jexlNode.jjtGetNumChildren()) {
      analyzeJexlScriptToSqlHelper(grouperJexlScriptAnalysis, theGrouperJexlScriptPart, jexlNode.jjtGetChild(0), clonePart);
      return;
    } else if (jexlNode instanceof ASTReference && 2==jexlNode.jjtGetNumChildren()) {
      analyzeJexlReferenceTwoChildrenToSqlHelper(grouperJexlScriptAnalysis, theGrouperJexlScriptPart, (ASTReference)jexlNode, clonePart);
      return;
    }

    if (jexlNode instanceof ASTStringLiteral) {
      String literal = ((ASTStringLiteral)jexlNode).getLiteral();
      if (literal != null && literal.contains(":")) {
        analyzeJexlMemberOf(theGrouperJexlScriptPart, literal);
        return;
      }
      throw new RuntimeException("Not expecting literal: '" + literal + "'");
    }

    if (jexlNode instanceof ASTReferenceExpression && 1==jexlNode.jjtGetNumChildren()) {
      theGrouperJexlScriptPart.getWhereClause().append("(");
      theGrouperJexlScriptPart.getDisplayDescription().append("(");
      analyzeJexlScriptToSqlHelper(grouperJexlScriptAnalysis, theGrouperJexlScriptPart, jexlNode.jjtGetChild(0), clonePart);
      theGrouperJexlScriptPart.getDisplayDescription().append(")");
      theGrouperJexlScriptPart.getWhereClause().append(")");

//      if (clonePart) {
//        grouperJexlScriptPartClone = new GrouperJexlScriptPart();
//        grouperJexlScriptAnalysis.getGrouperJexlScriptParts().add(grouperJexlScriptPartClone);
//        analyzeJexlScriptToSqlHelper(grouperJexlScriptAnalysis, grouperJexlScriptPartClone, jexlNode.jjtGetChild(0), false);
//      }

    } else if (jexlNode instanceof ASTNotNode && 1==jexlNode.jjtGetNumChildren()) {
      theGrouperJexlScriptPart.getWhereClause().append(" not ");
      theGrouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisNot")).append(" ");
      theGrouperJexlScriptPart.setNegated(true);
      analyzeJexlScriptToSqlHelper(grouperJexlScriptAnalysis, theGrouperJexlScriptPart, jexlNode.jjtGetChild(0), clonePart);
      if (clonePart) {
        grouperJexlScriptPartClone = new GrouperJexlScriptPart();
        grouperJexlScriptPartClone.setPartRole(GrouperJexlScriptPart.PartRole.NOT_POSITIVE);
        grouperJexlScriptAnalysis.getGrouperJexlScriptParts().add(grouperJexlScriptPartClone);
        analyzeJexlScriptToSqlHelper(grouperJexlScriptAnalysis, grouperJexlScriptPartClone, jexlNode.jjtGetChild(0), false);
      }
    } else if (jexlNode instanceof ASTAndNode) {
      for (int j=0;j<jexlNode.jjtGetNumChildren(); j++) {
        if (j>0) {
          theGrouperJexlScriptPart.getWhereClause().append(" and ");
          theGrouperJexlScriptPart.getDisplayDescription().append(" ").append(GrouperTextContainer.textOrNull("jexlAnalysisAnd")).append(" ");
        }
        analyzeJexlScriptToSqlHelper(grouperJexlScriptAnalysis, theGrouperJexlScriptPart, jexlNode.jjtGetChild(j), clonePart);
        if (clonePart) {
          grouperJexlScriptPartClone = new GrouperJexlScriptPart();
          grouperJexlScriptPartClone.setPartRole(GrouperJexlScriptPart.PartRole.AND_CHILD);
          grouperJexlScriptAnalysis.getGrouperJexlScriptParts().add(grouperJexlScriptPartClone);
          analyzeJexlScriptToSqlHelper(grouperJexlScriptAnalysis, grouperJexlScriptPartClone, jexlNode.jjtGetChild(j), false);
        }
      }
      return;
    } else if (jexlNode instanceof ASTOrNode) {

      for (int j=0;j<jexlNode.jjtGetNumChildren(); j++) {
        if (j>0) {
          theGrouperJexlScriptPart.getWhereClause().append(" or ");
          theGrouperJexlScriptPart.getDisplayDescription().append(" ").append(GrouperTextContainer.textOrNull("jexlAnalysisOr")).append(" ");
        }
        analyzeJexlScriptToSqlHelper(grouperJexlScriptAnalysis, theGrouperJexlScriptPart, jexlNode.jjtGetChild(j), clonePart);
        if (clonePart) {
          grouperJexlScriptPartClone = new GrouperJexlScriptPart();
          grouperJexlScriptPartClone.setPartRole(GrouperJexlScriptPart.PartRole.OR_CHILD);
          grouperJexlScriptAnalysis.getGrouperJexlScriptParts().add(grouperJexlScriptPartClone);
          analyzeJexlScriptToSqlHelper(grouperJexlScriptAnalysis, grouperJexlScriptPartClone, jexlNode.jjtGetChild(j), false);
        }
      }
      return;
    } else {
      throw new RuntimeException("Not expecting node type: " + jexlNode.getClass().getName() + ", children: " + jexlNode.jjtGetNumChildren());
    }

  }

  /**
   * has two children
   * @param result
   * @param astReference
   */
  public static void analyzeJexlReferenceTwoChildrenToSqlHelper(GrouperJexlScriptAnalysis grouperJexlScriptAnalysis, 
      GrouperJexlScriptPart grouperJexlScriptPart, ASTReference astReference, boolean clonePart) {
    ASTIdentifier astIdentifier = (ASTIdentifier)astReference.jjtGetChild(0);
    if (!StringUtils.equals("entity", astIdentifier.getName())) {
      throw new RuntimeException("Not expecting non-entity: '" + astIdentifier.getName() + "'");
    }
    ASTMethodNode astMethodNode = (ASTMethodNode)astReference.jjtGetChild(1);
    ASTIdentifierAccess astIdentifierAccess = (ASTIdentifierAccess)astMethodNode.jjtGetChild(0);
    if (StringUtils.equals("memberOf", astIdentifierAccess.getName())) {
      ASTArguments astArguments = (ASTArguments)astMethodNode.jjtGetChild(1);
      if (astArguments.jjtGetNumChildren() != 1) {
        throw new RuntimeException("Not expecting method with more than one argument! " + astArguments.jjtGetNumChildren());
      }
      if (!(astArguments.jjtGetChild(0) instanceof ASTStringLiteral)) {
        throw new RuntimeException("Not expecting argument of type! " + astArguments.jjtGetChild(0).getClass().getName());
      }
      ASTStringLiteral astStringLiteral = (ASTStringLiteral)astArguments.jjtGetChild(0);
      String groupName = astStringLiteral.getLiteral();
      analyzeJexlMemberOf(grouperJexlScriptPart, groupName);
    } else if (StringUtils.equals("memberOfAny", astIdentifierAccess.getName())) {
      ASTArguments astArguments = (ASTArguments)astMethodNode.jjtGetChild(1);
      if (astArguments.jjtGetNumChildren() != 1) {
        throw new RuntimeException("Not expecting method with more than one argument! " + astArguments.jjtGetNumChildren());
      }
      if (!(astArguments.jjtGetChild(0) instanceof ASTArrayLiteral)) {
        throw new RuntimeException("Not expecting argument of type! " + astArguments.jjtGetChild(0).getClass().getName());
      }
      Set<String> groupNames = new LinkedHashSet<>();
      ASTArrayLiteral astArrayLiteral = (ASTArrayLiteral)astArguments.jjtGetChild(0);
      for (int i=0; i<astArrayLiteral.jjtGetNumChildren(); i++) {
        JexlNode jjtGetChild = astArrayLiteral.jjtGetChild(i);
        if (jjtGetChild instanceof ASTStringLiteral) {
          String value = ((ASTStringLiteral)jjtGetChild).getLiteral();
          groupNames.add(value);
        } else {
          throw new RuntimeException("Not expecting argument of type! " + jjtGetChild.getClass().getName());
        }
      }
      if (groupNames.size() == 0) {
        throw new RuntimeException("Expecting at least one group name!");
      }
      analyzeJexlMemberOfAny(grouperJexlScriptPart, groupNames);
    } else if (StringUtils.equals("recentMemberOf", astIdentifierAccess.getName())) {
      ASTArguments astArguments = (ASTArguments)astMethodNode.jjtGetChild(1);
      if (astArguments.jjtGetNumChildren() != 2) {
        throw new RuntimeException("Expecting method with exactly 2 arguments! " + astArguments.jjtGetNumChildren());
      }
      if (!(astArguments.jjtGetChild(0) instanceof ASTStringLiteral)) {
        throw new RuntimeException("Not expecting first argument of type! " + astArguments.jjtGetChild(0).getClass().getName());
      }
      if (!(astArguments.jjtGetChild(1) instanceof ASTStringLiteral)) {
        throw new RuntimeException("Not expecting second argument of type! " + astArguments.jjtGetChild(0).getClass().getName());
      }
      String groupName = ((ASTStringLiteral)astArguments.jjtGetChild(0)).getLiteral();
      String timePeriodString = ((ASTStringLiteral)astArguments.jjtGetChild(1)).getLiteral();
      
      analyzeJexlRecentMemberOf(grouperJexlScriptPart, groupName, timePeriodString);
      
      grouperJexlScriptAnalysis.getRecentMemberOfGroupNames().add(groupName);

    } else if (StringUtils.equals("hasAttributeAny", astIdentifierAccess.getName())) {
      
      ASTArguments astArguments = (ASTArguments)astMethodNode.jjtGetChild(1);
      if (astArguments.jjtGetNumChildren() != 2) {
        throw new RuntimeException("Not expecting method with this many arguments! " + astArguments.jjtGetNumChildren());
      }
      if (!(astArguments.jjtGetChild(0) instanceof ASTStringLiteral) && !(astArguments.jjtGetChild(0) instanceof ASTIdentifier)) {
        throw new RuntimeException("Not expecting argument of type! " + astArguments.jjtGetChild(0).getClass().getName());
      }
      if (!(astArguments.jjtGetChild(1) instanceof ASTArrayLiteral)) {
        throw new RuntimeException("Not expecting argument of type! " + astArguments.jjtGetChild(1).getClass().getName());
      }
      String attributeAlias = null;
      
      if (astArguments.jjtGetChild(0) instanceof ASTStringLiteral) {
        ASTStringLiteral astStringLiteral = (ASTStringLiteral)astArguments.jjtGetChild(0);
        attributeAlias = astStringLiteral.getLiteral();
      } else if (astArguments.jjtGetChild(0) instanceof ASTIdentifier) {
        attributeAlias = ((ASTIdentifier)astArguments.jjtGetChild(0)).getName();
      } else {
        GrouperUtil.assertion(false, "Not expecting type of first argument");
      }
      
      ASTArrayLiteral astArrayLiteral = (ASTArrayLiteral)astArguments.jjtGetChild(1);
      
      grouperJexlScriptPart.getWhereClause().append("exists (select 1 from grouper_data_field_assign gdfa where gdfa.data_field_internal_id = ? "
          + "and gdfa.member_internal_id = gm.internal_id and gdfa.$$ATTRIBUTE_COL_" + (grouperJexlScriptPart.getArguments().size()+1) + "$$ in ("+ GrouperClientUtils.appendQuestions(astArrayLiteral.jjtGetNumChildren()) + ")) ");
      grouperJexlScriptPart.getArguments().add(new MultiKey("attribute", attributeAlias));
      
      for (int i=0; i<astArrayLiteral.jjtGetNumChildren(); i++) {
        
        JexlNode jjtGetChild = astArrayLiteral.jjtGetChild(i);
        
        GrouperJexlScriptPart grouperJexlScriptPartClone = new GrouperJexlScriptPart();
        grouperJexlScriptPartClone.setPartRole(GrouperJexlScriptPart.PartRole.HAS_ATTRIBUTE_ANY_DETAIL);
        grouperJexlScriptAnalysis.getGrouperJexlScriptParts().add(grouperJexlScriptPartClone);

        grouperJexlScriptPartClone.getWhereClause().append("exists (select 1 from grouper_data_field_assign gdfa where gdfa.data_field_internal_id = ? "
            + "and gdfa.member_internal_id = gm.internal_id and gdfa.$$ATTRIBUTE_COL_" + (grouperJexlScriptPartClone.getArguments().size()+1) + "$$ = ?) ");
        grouperJexlScriptPartClone.getArguments().add(new MultiKey("attribute", attributeAlias));
       
       
        if (jjtGetChild instanceof ASTStringLiteral) {
          String value = ((ASTStringLiteral)jjtGetChild).getLiteral();
          grouperJexlScriptPart.getArguments().add(new MultiKey("attributeValue", value));
          grouperJexlScriptPartClone.getArguments().add(new MultiKey("attributeValue", value));
          if (i == 0) {
            grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasAttributeValue1"))
            .append(" '").append(GrouperUtil.xmlEscape(attributeAlias)).append("' ").append(GrouperTextContainer.textOrNull("jexlAnalysisHasAttributeAnyValue")).append(" '")
            .append(GrouperUtil.xmlEscape(value)).append("'");
          } else {
            grouperJexlScriptPart.getDisplayDescription().append(", ").append("' ").append(GrouperUtil.xmlEscape(value)).append("' ");
          }
          
          grouperJexlScriptPartClone.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasAttributeValue1"))
          .append(" '").append(GrouperUtil.xmlEscape(attributeAlias)).append("' ").append(GrouperTextContainer.textOrNull("jexlAnalysisHasAttributeValue2")).append(" '")
          .append(GrouperUtil.xmlEscape(value)).append("'");
          
        } else if (jjtGetChild instanceof ASTNumberLiteral) {
          Number value = ((ASTNumberLiteral)jjtGetChild).getLiteral();
          grouperJexlScriptPart.getArguments().add(new MultiKey("attributeValue", value));
          grouperJexlScriptPartClone.getArguments().add(new MultiKey("attributeValue", value));
          if (i == 0) {
            grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasAttributeValue1"))
            .append(" '").append(GrouperUtil.xmlEscape(attributeAlias)).append("' ").append(GrouperTextContainer.textOrNull("jexlAnalysisHasAttributeAnyValue"))
            .append(value);
          } else {
            grouperJexlScriptPart.getDisplayDescription().append(", ").append(value);
          }
          
          grouperJexlScriptPartClone.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasAttributeValue1"))
          .append(" '").append(attributeAlias).append("' ").append(GrouperTextContainer.textOrNull("jexlAnalysisHasAttributeValue2")).append(" ")
          .append(value);

        } else if (jjtGetChild instanceof ASTUnaryMinusNode) {

          Number value = negate((ASTNumberLiteral)jjtGetChild.jjtGetChild(0));
          grouperJexlScriptPart.getArguments().add(new MultiKey("attributeValue", value));
          grouperJexlScriptPartClone.getArguments().add(new MultiKey("attributeValue", value));
          if (i == 0) {
            grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasAttributeValue1"))
            .append(" '").append(GrouperUtil.xmlEscape(attributeAlias)).append("' ").append(GrouperTextContainer.textOrNull("jexlAnalysisHasAttributeAnyValue"))
            .append(value);
          } else {
            grouperJexlScriptPart.getDisplayDescription().append(", ").append(value);
          }
          
          grouperJexlScriptPartClone.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasAttributeValue1"))
          .append(" '").append(attributeAlias).append("' ").append(GrouperTextContainer.textOrNull("jexlAnalysisHasAttributeValue2")).append(" ")
          .append(value);

        } else {
          throw new RuntimeException("Not expecting argument of type! " + jjtGetChild.getClass().getName());
        }
        
      }
    } else if (StringUtils.equals("hasAttributeLessThan", astIdentifierAccess.getName()) || StringUtils.equals("hasAttributeLessThanOrEqual", astIdentifierAccess.getName())
        || StringUtils.equals("hasAttributeGreaterThan", astIdentifierAccess.getName()) || StringUtils.equals("hasAttributeGreaterThanOrEqual", astIdentifierAccess.getName())) {
      
      ASTArguments astArguments = (ASTArguments)astMethodNode.jjtGetChild(1);
      if (astArguments.jjtGetNumChildren() != 2) {
        throw new RuntimeException("Not expecting method with this many arguments! " + astArguments.jjtGetNumChildren());
      }
      if (!(astArguments.jjtGetChild(0) instanceof ASTStringLiteral) && !(astArguments.jjtGetChild(0) instanceof ASTIdentifier)) {
        throw new RuntimeException("Not expecting argument of type! " + astArguments.jjtGetChild(0).getClass().getName());
      }

      String attributeAlias = null;
      
      if (astArguments.jjtGetChild(0) instanceof ASTStringLiteral) {
        ASTStringLiteral astStringLiteral = (ASTStringLiteral)astArguments.jjtGetChild(0);
        attributeAlias = astStringLiteral.getLiteral();
      } else if (astArguments.jjtGetChild(0) instanceof ASTIdentifier) {
        attributeAlias = ((ASTIdentifier)astArguments.jjtGetChild(0)).getName();
      } else {
        GrouperUtil.assertion(false, "Not expecting type of first argument");
      }
      
      String operator = null;
      String label = null;
      if (StringUtils.equals("hasAttributeLessThan", astIdentifierAccess.getName())) {
        operator = "<";
        label = "jexlAnalysisHasAttributeValueLessThan2";
      } else if (StringUtils.equals("hasAttributeLessThanOrEqual", astIdentifierAccess.getName())) {
        operator = "<=";
        label = "jexlAnalysisHasAttributeValueLessThanEqual2";
      } else if (StringUtils.equals("hasAttributeGreaterThan", astIdentifierAccess.getName())) {
        operator = ">";
        label = "jexlAnalysisHasAttributeValueGreaterThan2";
      } else if (StringUtils.equals("hasAttributeGreaterThanOrEqual", astIdentifierAccess.getName())) {
        operator = ">=";
        label = "jexlAnalysisHasAttributeValueGreaterThanEqual2";
      } else {
        throw new RuntimeException("Not expecting method: " + astIdentifierAccess.getName());
      }

      // GRP-6828: ordering comparisons on string fields are not supported; use hasAttributeBetween() instead
      GrouperDataFieldConfig grouperDataFieldConfigCheck = grouperJexlScriptAnalysis.getGrouperDataEngine().getFieldConfigByAlias().get(attributeAlias.toLowerCase());
      if (grouperDataFieldConfigCheck != null && grouperDataFieldConfigCheck.getFieldDataType() == GrouperDataFieldType.string) {
        throw new RuntimeException("Ordering comparisons (<, <=, >, >=) are not supported on string fields. "
            + "Use hasAttributeBetween() for string range queries. Field: '" + attributeAlias + "'");
      }

      grouperJexlScriptPart.getWhereClause().append("exists (select 1 from grouper_data_field_assign gdfa where gdfa.data_field_internal_id = ? "
          + "and gdfa.member_internal_id = gm.internal_id and gdfa.$$ATTRIBUTE_COL_" + (grouperJexlScriptPart.getArguments().size()+1) + "$$ " + operator + " ?) ");
      grouperJexlScriptPart.getArguments().add(new MultiKey("attribute", attributeAlias));
      if (astArguments.jjtGetChild(1) instanceof ASTStringLiteral) {
        String value = ((ASTStringLiteral)astArguments.jjtGetChild(1)).getLiteral();
        grouperJexlScriptPart.getArguments().add(new MultiKey("attributeValue", value));

        grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasAttributeValue1"))
          .append(" '").append(GrouperUtil.xmlEscape(attributeAlias)).append("' ").append(GrouperTextContainer.textOrNull(label)).append(" '")
          .append(GrouperUtil.xmlEscape(value)).append("'");

      } else if (astArguments.jjtGetChild(1) instanceof ASTNumberLiteral) {
        Number value = ((ASTNumberLiteral)astArguments.jjtGetChild(1)).getLiteral();
        grouperJexlScriptPart.getArguments().add(new MultiKey("attributeValue", value));

        grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasAttributeValue1"))
          .append(" '").append(attributeAlias).append("' ").append(GrouperTextContainer.textOrNull(label)).append(" ")
          .append(value);

      } else if (astArguments.jjtGetChild(1) instanceof ASTUnaryMinusNode) {
        Number value = negate((ASTNumberLiteral)astArguments.jjtGetChild(1).jjtGetChild(0));
        grouperJexlScriptPart.getArguments().add(new MultiKey("attributeValue", value));
        
        grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasAttributeValue1"))
          .append(" '").append(attributeAlias).append("' ").append(GrouperTextContainer.textOrNull(label)).append(" ")
          .append(value);

      } else {
        throw new RuntimeException("Not expecting argument of type! " + astArguments.jjtGetChild(1).getClass().getName());
      }

      
    } else if (StringUtils.equals("hasAttributeBetween", astIdentifierAccess.getName())) {

      ASTArguments astArguments = (ASTArguments)astMethodNode.jjtGetChild(1);
      if (astArguments.jjtGetNumChildren() != 2) {
        throw new RuntimeException("hasAttributeBetween expects 2 comparison arguments, e.g. hasAttributeBetween('low' <= field, field <= 'high'), got: " + astArguments.jjtGetNumChildren());
      }

      MultiKey lowerParsed = parseBetweenComparisonArg(astArguments.jjtGetChild(0));
      MultiKey upperParsed = parseBetweenComparisonArg(astArguments.jjtGetChild(1));

      String lowerFieldAlias = (String) lowerParsed.getKey(0);
      String lowerValue = (String) lowerParsed.getKey(1);
      String lowerOp = (String) lowerParsed.getKey(2);

      String upperFieldAlias = (String) upperParsed.getKey(0);
      String upperValue = (String) upperParsed.getKey(1);
      String upperOp = (String) upperParsed.getKey(2);

      if (!StringUtils.equalsIgnoreCase(lowerFieldAlias, upperFieldAlias)) {
        throw new RuntimeException("hasAttributeBetween both comparisons must reference the same field, got: '"
            + lowerFieldAlias + "' and '" + upperFieldAlias + "'");
      }

      // lowerOp should be > or >= (lower bound), upperOp should be < or <= (upper bound)
      if (!StringUtils.equals(lowerOp, ">") && !StringUtils.equals(lowerOp, ">=")) {
        throw new RuntimeException("hasAttributeBetween first argument must establish a lower bound (field > value or field >= value), got operator: " + lowerOp);
      }
      if (!StringUtils.equals(upperOp, "<") && !StringUtils.equals(upperOp, "<=")) {
        throw new RuntimeException("hasAttributeBetween second argument must establish an upper bound (field < value or field <= value), got operator: " + upperOp);
      }

      String attributeAlias = lowerFieldAlias;
      GrouperDataFieldConfig grouperDataFieldConfig = grouperJexlScriptAnalysis.getGrouperDataEngine().getFieldConfigByAlias().get(attributeAlias.toLowerCase());
      if (grouperDataFieldConfig == null) {
        throw new RuntimeException("hasAttributeBetween field not found: '" + attributeAlias + "'");
      }
      GrouperDataFieldType fieldDataType = grouperDataFieldConfig.getFieldDataType();

      if (fieldDataType == GrouperDataFieldType.string) {
        grouperJexlScriptPart.getWhereClause().append("exists (select 1 from grouper_data_field_assign gdfa where gdfa.data_field_internal_id = ? "
            + "and gdfa.member_internal_id = gm.internal_id and gdfa.value_dictionary_internal_id in "
            + "(select gd.internal_id from grouper_dictionary gd where gd.the_text " + lowerOp + " ? and gd.the_text " + upperOp + " ?)) ");
      } else if (fieldDataType == GrouperDataFieldType.integer || fieldDataType == GrouperDataFieldType.timestamp) {
        grouperJexlScriptPart.getWhereClause().append("exists (select 1 from grouper_data_field_assign gdfa where gdfa.data_field_internal_id = ? "
            + "and gdfa.member_internal_id = gm.internal_id and gdfa.value_integer " + lowerOp + " ? and gdfa.value_integer " + upperOp + " ?) ");
      } else {
        throw new RuntimeException("hasAttributeBetween not supported for type: " + fieldDataType.name());
      }

      grouperJexlScriptPart.getArguments().add(new MultiKey("attribute", attributeAlias));
      grouperJexlScriptPart.getArguments().add(new MultiKey("bindVar", fieldDataType == GrouperDataFieldType.string ? lowerValue : GrouperUtil.longObjectValue(lowerValue, true)));
      grouperJexlScriptPart.getArguments().add(new MultiKey("bindVar", fieldDataType == GrouperDataFieldType.string ? upperValue : GrouperUtil.longObjectValue(upperValue, true)));

      grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasAttributeValue1"))
        .append(" '").append(GrouperUtil.xmlEscape(attributeAlias)).append("' ")
        .append(lowerOp).append(" '").append(GrouperUtil.xmlEscape(lowerValue)).append("' ")
        .append(GrouperTextContainer.textOrNull("jexlAnalysisHasAttributeBetweenAnd"))
        .append(" ").append(upperOp).append(" '").append(GrouperUtil.xmlEscape(upperValue)).append("'");

    } else if (StringUtils.equals("hasAttribute", astIdentifierAccess.getName())) {
      ASTArguments astArguments = (ASTArguments)astMethodNode.jjtGetChild(1);
      if (astArguments.jjtGetNumChildren() != 1 && astArguments.jjtGetNumChildren() != 2) {
        throw new RuntimeException("Not expecting method with this many arguments! " + astArguments.jjtGetNumChildren());
      }
      if (!(astArguments.jjtGetChild(0) instanceof ASTStringLiteral) && !(astArguments.jjtGetChild(0) instanceof ASTIdentifier)) {
        throw new RuntimeException("Not expecting argument of type! " + astArguments.jjtGetChild(0).getClass().getName());
      }
      String attributeAlias = null;

      if (astArguments.jjtGetChild(0) instanceof ASTStringLiteral) {
        ASTStringLiteral astStringLiteral = (ASTStringLiteral)astArguments.jjtGetChild(0);
        attributeAlias = astStringLiteral.getLiteral();
      } else if (astArguments.jjtGetChild(0) instanceof ASTIdentifier) {
        attributeAlias = ((ASTIdentifier)astArguments.jjtGetChild(0)).getName();
      } else {
        GrouperUtil.assertion(false, "Not expecting type of first argument");
      }

      if (astArguments.jjtGetNumChildren() == 1) {

        GrouperDataFieldWrapper grouperDataFieldWrapper = grouperJexlScriptAnalysis.getGrouperDataEngine().getGrouperDataProviderIndex().getFieldWrapperByLowerAlias().get(attributeAlias.toLowerCase());
        
        GrouperDataFieldConfig grouperDataFieldConfig = grouperJexlScriptAnalysis.getGrouperDataEngine().getFieldConfigByAlias().get(attributeAlias.toLowerCase());
        if (grouperDataFieldConfig == null) {
          throw new RuntimeException("Data field '" + attributeAlias + "' not found!");
        }
        if (grouperDataFieldConfig.getFieldDataType() == GrouperDataFieldType.bool) { 
          grouperJexlScriptPart.getWhereClause().append("exists (select 1 from grouper_data_field_assign gdfa where gdfa.data_field_internal_id = ? and gdfa.member_internal_id = gm.internal_id and gdfa.value_integer = 1) ");
        } else {
          grouperJexlScriptPart.getWhereClause().append("exists (select 1 from grouper_data_field_assign gdfa where gdfa.data_field_internal_id = ? and gdfa.member_internal_id = gm.internal_id) ");
        }
        grouperJexlScriptPart.getArguments().add(new MultiKey("attribute", attributeAlias));

        grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasAttribute"))
          .append(" '").append(GrouperUtil.xmlEscape(attributeAlias)).append("'");

      } else if (astArguments.jjtGetNumChildren() == 2) {

        if (astArguments.jjtGetChild(1) instanceof ASTNullLiteral) {
          
          grouperJexlScriptPart.getWhereClause().append("exists (select 1 from grouper_data_field_assign gdfa where gdfa.data_field_internal_id = ? "
              + "and gdfa.member_internal_id = gm.internal_id and gdfa.$$ATTRIBUTE_COL_" + (grouperJexlScriptPart.getArguments().size()+1) + "$$ is null) ");
          grouperJexlScriptPart.getArguments().add(new MultiKey("attribute", attributeAlias));
          grouperJexlScriptPart.getArguments().add(new MultiKey("attributeValue", Void.TYPE));

          grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasAttributeValue1"))
            .append(" '").append(attributeAlias).append("' ").append(GrouperTextContainer.textOrNull("jexlAnalysisHasAttributeValue2")).append(" null");

        } else {

          grouperJexlScriptPart.getWhereClause().append("exists (select 1 from grouper_data_field_assign gdfa where gdfa.data_field_internal_id = ? "
              + "and gdfa.member_internal_id = gm.internal_id and gdfa.$$ATTRIBUTE_COL_" + (grouperJexlScriptPart.getArguments().size()+1) + "$$ = ?) ");
          grouperJexlScriptPart.getArguments().add(new MultiKey("attribute", attributeAlias));
          if (astArguments.jjtGetChild(1) instanceof ASTStringLiteral) {
            String value = ((ASTStringLiteral)astArguments.jjtGetChild(1)).getLiteral();
            grouperJexlScriptPart.getArguments().add(new MultiKey("attributeValue", value));
            
            grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasAttributeValue1"))
              .append(" '").append(GrouperUtil.xmlEscape(attributeAlias)).append("' ").append(GrouperTextContainer.textOrNull("jexlAnalysisHasAttributeValue2")).append(" '")
              .append(GrouperUtil.xmlEscape(value)).append("'");
  
          } else if (astArguments.jjtGetChild(1) instanceof ASTNumberLiteral) {
            Number value = ((ASTNumberLiteral)astArguments.jjtGetChild(1)).getLiteral();
            grouperJexlScriptPart.getArguments().add(new MultiKey("attributeValue", value));
            
            grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasAttributeValue1"))
              .append(" '").append(attributeAlias).append("' ").append(GrouperTextContainer.textOrNull("jexlAnalysisHasAttributeValue2")).append(" ")
              .append(value);
  
          } else if (astArguments.jjtGetChild(1) instanceof ASTUnaryMinusNode) {
            Number value = negate((ASTNumberLiteral)astArguments.jjtGetChild(1).jjtGetChild(0));
            grouperJexlScriptPart.getArguments().add(new MultiKey("attributeValue", value));
            
            grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasAttributeValue1"))
              .append(" '").append(attributeAlias).append("' ").append(GrouperTextContainer.textOrNull("jexlAnalysisHasAttributeValue2")).append(" ")
              .append(value);
  
          } else {
            throw new RuntimeException("Not expecting argument of type! " + astArguments.jjtGetChild(1).getClass().getName());
          }
        }
      }
    } else if (StringUtils.equals("hasAttributeLike", astIdentifierAccess.getName())) {
      ASTArguments astArguments = (ASTArguments)astMethodNode.jjtGetChild(1);
      if (astArguments.jjtGetNumChildren() != 2) {
        throw new RuntimeException("Not expecting method with this many arguments! " + astArguments.jjtGetNumChildren());
      }
      if (!(astArguments.jjtGetChild(0) instanceof ASTStringLiteral) && !(astArguments.jjtGetChild(0) instanceof ASTIdentifier)) {
        throw new RuntimeException("Not expecting argument of type! " + astArguments.jjtGetChild(0).getClass().getName());
      }
      if (!(astArguments.jjtGetChild(1) instanceof ASTStringLiteral)) {
        throw new RuntimeException("Not expecting argument of type! " + astArguments.jjtGetChild(1).getClass().getName());
      }
      String attributeAlias = null;
      
      if (astArguments.jjtGetChild(0) instanceof ASTStringLiteral) {
        ASTStringLiteral astStringLiteral = (ASTStringLiteral)astArguments.jjtGetChild(0);
        attributeAlias = astStringLiteral.getLiteral();
      } else if (astArguments.jjtGetChild(0) instanceof ASTIdentifier) {
        attributeAlias = ((ASTIdentifier)astArguments.jjtGetChild(0)).getName();
      } else {
        GrouperUtil.assertion(false, "Not expecting type of first argument");
      }
      
      ASTStringLiteral astStringLiteral = (ASTStringLiteral)astArguments.jjtGetChild(1);
      String likeString = astStringLiteral.getLiteral();

      GrouperDataFieldConfig grouperDataFieldConfig = grouperJexlScriptAnalysis.getGrouperDataEngine().getFieldConfigByAlias().get(attributeAlias.toLowerCase());
      GrouperDataFieldType fieldDataType = grouperDataFieldConfig.getFieldDataType();
      
      if (fieldDataType != GrouperDataFieldType.integer && fieldDataType != GrouperDataFieldType.string) {
        throw new RuntimeException("The 'hasAttributeLike' function must be used on strings or integers! " + attributeAlias + " -> " + fieldDataType.name());
      }
      
      if (fieldDataType == GrouperDataFieldType.string) {
        grouperJexlScriptPart.getWhereClause().append("exists (select 1 from grouper_data_field_assign gdfa, grouper_dictionary gd where gdfa.data_field_internal_id = ? "
            + "and gdfa.member_internal_id = gm.internal_id and gd.the_text like ? " 
            + (GrouperDdlUtils.isOracle() ? " escape '\\' " : "") + " and gdfa.value_dictionary_internal_id = gd.internal_id ) ");
      } else if (fieldDataType == GrouperDataFieldType.integer) {
        grouperJexlScriptPart.getWhereClause().append("exists (select 1 from grouper_data_field_assign gdfa where gdfa.data_field_internal_id = ? "
            + "and gdfa.member_internal_id = gm.internal_id and gdfa.value_integer like ? " 
            + (GrouperDdlUtils.isOracle() ? " escape '\\' " : "") + " ) ");
      }
      grouperJexlScriptPart.getArguments().add(new MultiKey("attribute", attributeAlias));
      grouperJexlScriptPart.getArguments().add(new MultiKey("bindVar", likeString));
      
      grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasAttributeValue1"))
        .append(" '").append(GrouperUtil.xmlEscape(attributeAlias)).append("' ").append(GrouperTextContainer.textOrNull("jexlAnalysisHasAttributeLikeValue")).append(" '")
        .append(GrouperUtil.xmlEscape(likeString)).append("'");

    } else if (StringUtils.equals("hasAttributeRegex", astIdentifierAccess.getName())) {
      ASTArguments astArguments = (ASTArguments)astMethodNode.jjtGetChild(1);
      if (astArguments.jjtGetNumChildren() != 2) {
        throw new RuntimeException("Not expecting method with this many arguments! " + astArguments.jjtGetNumChildren());
      }
      if (!(astArguments.jjtGetChild(0) instanceof ASTStringLiteral) && !(astArguments.jjtGetChild(0) instanceof ASTIdentifier)) {
        throw new RuntimeException("Not expecting argument of type! " + astArguments.jjtGetChild(0).getClass().getName());
      }
      if (!(astArguments.jjtGetChild(1) instanceof ASTStringLiteral)) {
        throw new RuntimeException("Not expecting argument of type! " + astArguments.jjtGetChild(1).getClass().getName());
      }
      String attributeAlias = null;
      
      if (astArguments.jjtGetChild(0) instanceof ASTStringLiteral) {
        ASTStringLiteral astStringLiteral = (ASTStringLiteral)astArguments.jjtGetChild(0);
        attributeAlias = astStringLiteral.getLiteral();
      } else if (astArguments.jjtGetChild(0) instanceof ASTIdentifier) {
        attributeAlias = ((ASTIdentifier)astArguments.jjtGetChild(0)).getName();
      } else {
        GrouperUtil.assertion(false, "Not expecting type of first argument");
      }

      ASTStringLiteral astStringLiteral = (ASTStringLiteral)astArguments.jjtGetChild(1);
      String regexString = astStringLiteral.getLiteral();

      GrouperDataFieldConfig grouperDataFieldConfig = grouperJexlScriptAnalysis.getGrouperDataEngine().getFieldConfigByAlias().get(attributeAlias.toLowerCase());
      GrouperDataFieldType fieldDataType = grouperDataFieldConfig.getFieldDataType();
      
      if (fieldDataType != GrouperDataFieldType.integer && fieldDataType != GrouperDataFieldType.string) {
        throw new RuntimeException("The 'hasAttributeRegex' function must be used on strings or integers! " + attributeAlias + " -> " + fieldDataType.name());
      }
      
      String regexPart = null;
      
      if (fieldDataType == GrouperDataFieldType.string) {
        if (GrouperDdlUtils.isOracle() || GrouperDdlUtils.isMysql()) {
          regexPart = "REGEXP_LIKE (gd.the_text, ?) ";
        } else if (GrouperDdlUtils.isPostgres()) {
          regexPart = "gd.the_text ~ ? ";
        } else {
          throw new RuntimeException("Not expecting database!");
        }
        grouperJexlScriptPart.getWhereClause().append("exists (select 1 from grouper_data_field_assign gdfa, grouper_dictionary gd where gdfa.data_field_internal_id = ? "
            + "and gdfa.member_internal_id = gm.internal_id and " + regexPart 
            + " and gdfa.value_dictionary_internal_id = gd.internal_id ) ");
      } else if (fieldDataType == GrouperDataFieldType.integer) {
        if (GrouperDdlUtils.isOracle() || GrouperDdlUtils.isMysql()) {
          regexPart = "REGEXP_LIKE (gdfa.value_integer, ?) ";
        } else if (GrouperDdlUtils.isPostgres()) {
          regexPart = "cast(gdfa.value_integer as varchar) ~ ? ";
        } else {
          throw new RuntimeException("Not expecting database!");
        }
        grouperJexlScriptPart.getWhereClause().append("exists (select 1 from grouper_data_field_assign gdfa where gdfa.data_field_internal_id = ? "
            + "and gdfa.member_internal_id = gm.internal_id and " + regexPart  + " ) ");
      }
      grouperJexlScriptPart.getArguments().add(new MultiKey("attribute", attributeAlias));
      grouperJexlScriptPart.getArguments().add(new MultiKey("bindVar", regexString));
      
      grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasAttributeValue1"))
        .append(" '").append(GrouperUtil.xmlEscape(attributeAlias)).append("' ").append(GrouperTextContainer.textOrNull("jexlAnalysisHasAttributeRegexValue")).append(" '")
        .append(GrouperUtil.xmlEscape(regexString)).append("'");

    } else if (StringUtils.equals("hasRow", astIdentifierAccess.getName())) {
      ASTArguments astArguments = (ASTArguments)astMethodNode.jjtGetChild(1);
      if (astArguments.jjtGetNumChildren() != 2) {
        throw new RuntimeException("Not expecting method with this many arguments! " + astArguments.jjtGetNumChildren());
      }
      if (!(astArguments.jjtGetChild(0) instanceof ASTStringLiteral) && !(astArguments.jjtGetChild(0) instanceof ASTIdentifier)) {
        throw new RuntimeException("Not expecting argument of type! " + astArguments.jjtGetChild(0).getClass().getName());
      }
      if (!(astArguments.jjtGetChild(1) instanceof ASTStringLiteral)) {
        throw new RuntimeException("Not expecting argument of type! " + astArguments.jjtGetChild(1).getClass().getName());
      }
      String rowAlias = null;
      
      if (astArguments.jjtGetChild(0) instanceof ASTStringLiteral) {
        ASTStringLiteral astStringLiteral = (ASTStringLiteral)astArguments.jjtGetChild(0);
        rowAlias = astStringLiteral.getLiteral();
      } else if (astArguments.jjtGetChild(0) instanceof ASTIdentifier) {
        rowAlias = ((ASTIdentifier)astArguments.jjtGetChild(0)).getName();
      } else {
        GrouperUtil.assertion(false, "Not expecting type of first argument");
      }
      
      ASTStringLiteral scriptLiteral = (ASTStringLiteral)astArguments.jjtGetChild(1);
 
      GrouperJexlScriptPart rowJexlScriptPart = new GrouperJexlScriptPart();
      rowJexlScriptPart.getWhereClause().append(" exists (select 1 from grouper_data_row_assign gdra where gdra.data_row_internal_id = ? and gdra.member_internal_id = gm.internal_id and ( ");
      rowJexlScriptPart.getArguments().add(new MultiKey("row", rowAlias));
      rowJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasRow"))
      .append(" '").append(GrouperUtil.xmlEscape(rowAlias)).append("' ");
      
      grouperJexlScriptPart.getWhereClause().append(rowJexlScriptPart.getWhereClause());
      grouperJexlScriptPart.getArguments().add(new MultiKey(rowJexlScriptPart.getArguments().get(0).getKeys()));

      grouperJexlScriptPart.getDisplayDescription().append(rowJexlScriptPart.getDisplayDescription());

      analyzeJexlRowToSqlHelper(grouperJexlScriptAnalysis, grouperJexlScriptPart, rowJexlScriptPart, scriptLiteral.getLiteral(), clonePart);
    
      grouperJexlScriptPart.getWhereClause().append(" ) ) ");
    } else {
      throw new RuntimeException("Not expecting method name: '" + astIdentifierAccess.getName() + "'");
    }
  }

  private static void analyzeJexlMemberOf(GrouperJexlScriptPart grouperJexlScriptPart,
      String groupName) {
    grouperJexlScriptPart.getWhereClause().append("exists (select 1 from grouper_sql_cache_mship gscm where gscm.sql_cache_group_internal_id = ? "
        + " and gscm.member_internal_id = gm.internal_id) ");
    grouperJexlScriptPart.getArguments().add(new MultiKey("group", "members", groupName));
    grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisMemberOfGroup"))
      .append(" '").append(GrouperUtil.xmlEscape(groupName)).append("'");
  }

  private static void analyzeJexlMemberOfAny(GrouperJexlScriptPart grouperJexlScriptPart,
      Set<String> groupNames) {
    grouperJexlScriptPart.getWhereClause().append("exists (select 1 from grouper_sql_cache_mship gscm "
        + "where gscm.sql_cache_group_internal_id in (" + GrouperClientUtils.appendQuestions(groupNames.size()) + ") "
        + " and gscm.member_internal_id = gm.internal_id) ");
    
    grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisMemberOfAnyGroup"));

    Iterator<String> groupNamesIterator = groupNames.iterator();
    while (groupNamesIterator.hasNext()) {
      String groupName = groupNamesIterator.next();
      grouperJexlScriptPart.getArguments().add(new MultiKey("group", "members", groupName));
      grouperJexlScriptPart.getDisplayDescription().append(" '").append(GrouperUtil.xmlEscape(groupName)).append("'");
      if (groupNamesIterator.hasNext()) {
        grouperJexlScriptPart.getDisplayDescription().append(",");
      }
    }    
  }
  
  private static void analyzeJexlRecentMemberOf(GrouperJexlScriptPart grouperJexlScriptPart,
      String groupName, String timePeriodString) {
    
    Matcher matcher = recentMemberOfTimePeriodPattern.matcher(timePeriodString);
    if (!matcher.matches()) {
      throw new RuntimeException("Invalid format for time period (expecting a number followed by hours or days), e.g. 2 days or 1 hour, but found: " + timePeriodString);
    }
    
    int timePeriodNumber = Integer.parseInt(matcher.group(1));
    String timePeriodHoursOrDaysString = matcher.group(2);
    int timePeriodHours;
    
    if (timePeriodHoursOrDaysString.equalsIgnoreCase("day") || timePeriodHoursOrDaysString.equalsIgnoreCase("days")) {
      timePeriodHours = timePeriodNumber * 24;
    } else if (timePeriodHoursOrDaysString.equalsIgnoreCase("hour") || timePeriodHoursOrDaysString.equalsIgnoreCase("hours")) {
      timePeriodHours = timePeriodNumber;
    } else {
      throw new RuntimeException("Unexpected: " + timePeriodString);
    }
    
    if (timePeriodHours > 17520) {
      throw new RuntimeException("Invalid time period.  Cannot be more than 2 years: " + timePeriodString);
    }
    
    long timePeriodMicros = timePeriodHours * 60L * 60 * 1000 * 1000;
    
    grouperJexlScriptPart.getWhereClause().append("(");
    
    grouperJexlScriptPart.getWhereClause().append("exists (select 1 from grouper_sql_cache_mship_hst gscmh where gscmh.sql_cache_group_internal_id = ? "
        + " and gscmh.end_time >= ? "
        + " and gscmh.member_internal_id = gm.internal_id) ");
    grouperJexlScriptPart.getArguments().add(new MultiKey("group", "members", groupName));
    grouperJexlScriptPart.getArguments().add(new MultiKey("bindVar", (System.currentTimeMillis() * 1000L) - timePeriodMicros));

    grouperJexlScriptPart.getWhereClause().append("and not exists (select 1 from grouper_sql_cache_mship gscm where gscm.sql_cache_group_internal_id = ? "
        + " and gscm.member_internal_id = gm.internal_id) ");
    grouperJexlScriptPart.getArguments().add(new MultiKey("group", "members", groupName));
    
    grouperJexlScriptPart.getWhereClause().append(")");
    
    String analysisString = GrouperTextContainer.textOrNull("jexlAnalysisRecentMemberOfGroup")
        .replace("##groupName##", GrouperUtil.xmlEscape(groupName))
        .replace("##timePeriodString##", timePeriodString);

    grouperJexlScriptPart.getDisplayDescription().append(analysisString);
  }

  /**
   * 
   * @param jexlStript
   * @param arguments first one is type (e.g. group), second is list (e.g. members), third is name (e.g. test:testGroup)
   * @return the sql
   */
  public static void analyzeJexlRowToSqlHelper(GrouperJexlScriptAnalysis grouperJexlScriptAnalysis, 
      GrouperJexlScriptPart grouperJexlScriptPart, GrouperJexlScriptPart rowJexlScriptPart, String jexlStript, boolean clonePart) {

    jexlStript = jexlStript.trim();
    
    JexlEngine jexlEngine = new Engine();
    
    JexlExpression expression = (JexlExpression)jexlEngine.createExpression(jexlStript);

    ASTJexlScript astJexlScript = (ASTJexlScript)GrouperUtil.fieldValue(expression, "script");

    analyzeJexlRowToSqlHelper(grouperJexlScriptAnalysis, grouperJexlScriptPart, rowJexlScriptPart, astJexlScript, clonePart);
  }


  public static void analyzeJexlRowToSqlHelper(GrouperJexlScriptAnalysis grouperJexlScriptAnalysis, 
      GrouperJexlScriptPart grouperJexlScriptPart, GrouperJexlScriptPart rowJexlScriptPart, JexlNode jexlNode, boolean clonePart) {
    
    GrouperJexlScriptPart grouperJexlScriptPartClone = new GrouperJexlScriptPart();
        
    if (jexlNode instanceof ASTIdentifier && 0==jexlNode.jjtGetNumChildren()) {
      
      String sql = "exists (select 1 from grouper_data_row_field_assign gdrfa where data_row_assign_internal_id = gdra.internal_id "
          + "and gdrfa.data_field_internal_id = ? and gdrfa.value_integer = ?) ";

      grouperJexlScriptPart.getWhereClause().append(sql);
      String rowAlias = ((ASTIdentifier)jexlNode).getName();
      grouperJexlScriptPart.getArguments().add(new MultiKey("attribute", rowAlias));
      grouperJexlScriptPart.getArguments().add(new MultiKey("attributeValue", true));
      
      grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasRowAttribute"))
        .append(" '").append(GrouperUtil.xmlEscape(rowAlias)).append("'");

    } else if (jexlNode instanceof ASTFunctionNode && jexlNode.jjtGetNumChildren() > 0
        && jexlNode.jjtGetChild(0) instanceof ASTIdentifier 
        && StringUtils.equalsIgnoreCase("hasAttributeRegex", ((ASTIdentifier)jexlNode.jjtGetChild(0)).getName())) {
      
      if (jexlNode.jjtGetNumChildren() != 2 || (!(jexlNode.jjtGetChild(1) instanceof ASTArguments))) {
        throw new RuntimeException("Expecting two JEXL children: " + jexlNode.getClass().getName() + ", children: " 
            + jexlNode.jjtGetNumChildren() + ", jexlNode: " + jexlNode);
      }
      
      ASTArguments astArguments = (ASTArguments)jexlNode.jjtGetChild(1);

      if (astArguments.jjtGetNumChildren() != 2) {
        throw new RuntimeException("Expecting two arguments: " + astArguments.getClass().getName() + ", children: " 
            + astArguments.jjtGetNumChildren() + ", jexlNode: " + jexlNode);
      }
      String attributeAlias = null;

      if (astArguments.jjtGetChild(0) instanceof ASTIdentifier) {
        attributeAlias = ((ASTIdentifier)astArguments.jjtGetChild(0)).getName();
      } else {
        throw new RuntimeException("Expecting first argument to be identifer: " 
            + astArguments.jjtGetChild(0).getClass().getName() + ", jexlNode: " + jexlNode);
      }
      
      String regexString = null;

      if (astArguments.jjtGetChild(1) instanceof ASTStringLiteral) {
        regexString = ((ASTStringLiteral)astArguments.jjtGetChild(1)).getLiteral();
      } else {
        throw new RuntimeException("Expecting second argument to be string: " 
            + astArguments.jjtGetChild(1).getClass().getName() + ", jexlNode: " + jexlNode);
      }

      GrouperDataFieldConfig grouperDataFieldConfig = grouperJexlScriptAnalysis.getGrouperDataEngine().getFieldConfigByAlias().get(attributeAlias.toLowerCase());
      GrouperDataFieldType fieldDataType = grouperDataFieldConfig.getFieldDataType();
      
      if (fieldDataType != GrouperDataFieldType.integer && fieldDataType != GrouperDataFieldType.string) {
        throw new RuntimeException("The 'hasAttributeRegex' function must be used on strings or integers! " + attributeAlias + " -> " + fieldDataType.name());
      }

      String regexPart = null;
      
      if (fieldDataType == GrouperDataFieldType.string) {
        if (GrouperDdlUtils.isOracle() || GrouperDdlUtils.isMysql()) {
          regexPart = "REGEXP_LIKE (gd.the_text, ?) ";
        } else if (GrouperDdlUtils.isPostgres()) {
          regexPart = "gd.the_text ~ ? ";
        } else {
          throw new RuntimeException("Not expecting database!");
        }
        grouperJexlScriptPart.getWhereClause().append("exists (select 1 from grouper_data_row_field_assign gdrfa, grouper_dictionary gd where gdrfa.data_field_internal_id = ? "
            + " and data_row_assign_internal_id = gdra.internal_id and " + regexPart 
            + " and gdrfa.value_dictionary_internal_id = gd.internal_id ) ");
      } else if (fieldDataType == GrouperDataFieldType.integer) {
        if (GrouperDdlUtils.isOracle() || GrouperDdlUtils.isMysql()) {
          regexPart = "REGEXP_LIKE (gdfa.value_integer, ?) ";
        } else if (GrouperDdlUtils.isPostgres()) {
          regexPart = "cast(gdfa.value_integer as varchar) ~ ? ";
        } else {
          throw new RuntimeException("Not expecting database!");
        }
        grouperJexlScriptPart.getWhereClause().append("exists (select 1 from grouper_data_row_field_assign gdrfa where gdrfa.data_field_internal_id = ? "
            + " and data_row_assign_internal_id = gdra.internal_id and gdrfa.value_integer " + regexPart 
            + " ) ");
      }
      grouperJexlScriptPart.getArguments().add(new MultiKey("attribute", attributeAlias));
      grouperJexlScriptPart.getArguments().add(new MultiKey("bindVar", regexString));
      
      grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasRowAttributeValue1"))
        .append(" '").append(GrouperUtil.xmlEscape(attributeAlias)).append("' ").append(GrouperTextContainer.textOrNull("jexlAnalysisHasRowAttributeRegexValue")).append(" '")
        .append(GrouperUtil.xmlEscape(regexString)).append("'");

      
    } else if (jexlNode instanceof ASTFunctionNode && jexlNode.jjtGetNumChildren() > 0
        && jexlNode.jjtGetChild(0) instanceof ASTIdentifier
        && StringUtils.equalsIgnoreCase("hasAttributeBetween", ((ASTIdentifier)jexlNode.jjtGetChild(0)).getName())) {

      if (jexlNode.jjtGetNumChildren() != 2 || (!(jexlNode.jjtGetChild(1) instanceof ASTArguments))) {
        throw new RuntimeException("Expecting two JEXL children: " + jexlNode.getClass().getName() + ", children: "
            + jexlNode.jjtGetNumChildren() + ", jexlNode: " + jexlNode);
      }

      ASTArguments astArguments = (ASTArguments)jexlNode.jjtGetChild(1);

      if (astArguments.jjtGetNumChildren() != 2) {
        throw new RuntimeException("hasAttributeBetween expects 2 comparison arguments, e.g. hasAttributeBetween('low' <= field, field <= 'high'), got: "
            + astArguments.jjtGetNumChildren() + ", jexlNode: " + jexlNode);
      }

      MultiKey lowerParsed = parseBetweenComparisonArg(astArguments.jjtGetChild(0));
      MultiKey upperParsed = parseBetweenComparisonArg(astArguments.jjtGetChild(1));

      String lowerFieldAlias = (String) lowerParsed.getKey(0);
      String lowerValue = (String) lowerParsed.getKey(1);
      String lowerOp = (String) lowerParsed.getKey(2);

      String upperFieldAlias = (String) upperParsed.getKey(0);
      String upperValue = (String) upperParsed.getKey(1);
      String upperOp = (String) upperParsed.getKey(2);

      if (!StringUtils.equalsIgnoreCase(lowerFieldAlias, upperFieldAlias)) {
        throw new RuntimeException("hasAttributeBetween both comparisons must reference the same field, got: '"
            + lowerFieldAlias + "' and '" + upperFieldAlias + "'");
      }

      if (!StringUtils.equals(lowerOp, ">") && !StringUtils.equals(lowerOp, ">=")) {
        throw new RuntimeException("hasAttributeBetween first argument must establish a lower bound (field > value or field >= value), got operator: " + lowerOp);
      }
      if (!StringUtils.equals(upperOp, "<") && !StringUtils.equals(upperOp, "<=")) {
        throw new RuntimeException("hasAttributeBetween second argument must establish an upper bound (field < value or field <= value), got operator: " + upperOp);
      }

      String attributeAlias = lowerFieldAlias;
      GrouperDataFieldConfig grouperDataFieldConfig = grouperJexlScriptAnalysis.getGrouperDataEngine().getFieldConfigByAlias().get(attributeAlias.toLowerCase());
      if (grouperDataFieldConfig == null) {
        throw new RuntimeException("hasAttributeBetween field not found: '" + attributeAlias + "'");
      }
      GrouperDataFieldType fieldDataType = grouperDataFieldConfig.getFieldDataType();

      if (fieldDataType == GrouperDataFieldType.string) {
        grouperJexlScriptPart.getWhereClause().append("exists (select 1 from grouper_data_row_field_assign gdrfa where gdrfa.data_field_internal_id = ? "
            + "and data_row_assign_internal_id = gdra.internal_id and gdrfa.value_dictionary_internal_id in "
            + "(select gd.internal_id from grouper_dictionary gd where gd.the_text " + lowerOp + " ? and gd.the_text " + upperOp + " ?)) ");
      } else if (fieldDataType == GrouperDataFieldType.integer || fieldDataType == GrouperDataFieldType.timestamp) {
        grouperJexlScriptPart.getWhereClause().append("exists (select 1 from grouper_data_row_field_assign gdrfa where gdrfa.data_field_internal_id = ? "
            + "and data_row_assign_internal_id = gdra.internal_id and gdrfa.value_integer " + lowerOp + " ? and gdrfa.value_integer " + upperOp + " ?) ");
      } else {
        throw new RuntimeException("hasAttributeBetween not supported for type: " + fieldDataType.name());
      }

      grouperJexlScriptPart.getArguments().add(new MultiKey("attribute", attributeAlias));
      grouperJexlScriptPart.getArguments().add(new MultiKey("bindVar", fieldDataType == GrouperDataFieldType.string ? lowerValue : GrouperUtil.longObjectValue(lowerValue, true)));
      grouperJexlScriptPart.getArguments().add(new MultiKey("bindVar", fieldDataType == GrouperDataFieldType.string ? upperValue : GrouperUtil.longObjectValue(upperValue, true)));

      grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasRowAttributeValue1"))
        .append(" '").append(GrouperUtil.xmlEscape(attributeAlias)).append("' ")
        .append(lowerOp).append(" '").append(GrouperUtil.xmlEscape(lowerValue)).append("' ")
        .append(GrouperTextContainer.textOrNull("jexlAnalysisHasRowAttributeBetweenAnd"))
        .append(" ").append(upperOp).append(" '").append(GrouperUtil.xmlEscape(upperValue)).append("'");

    } else if (jexlNode instanceof ASTFunctionNode && jexlNode.jjtGetNumChildren() > 0
        && jexlNode.jjtGetChild(0) instanceof ASTIdentifier
        && StringUtils.equalsIgnoreCase("hasAttributeLike", ((ASTIdentifier)jexlNode.jjtGetChild(0)).getName())) {

      if (jexlNode.jjtGetNumChildren() != 2 || (!(jexlNode.jjtGetChild(1) instanceof ASTArguments))) {
        throw new RuntimeException("Expecting two JEXL children: " + jexlNode.getClass().getName() + ", children: "
            + jexlNode.jjtGetNumChildren() + ", jexlNode: " + jexlNode);
      }

      ASTArguments astArguments = (ASTArguments)jexlNode.jjtGetChild(1);

      if (astArguments.jjtGetNumChildren() != 2) {
        throw new RuntimeException("Expecting two arguments: " + astArguments.getClass().getName() + ", children: "
            + astArguments.jjtGetNumChildren() + ", jexlNode: " + jexlNode);
      }
      String attributeAlias = null;

      if (astArguments.jjtGetChild(0) instanceof ASTIdentifier) {
        attributeAlias = ((ASTIdentifier)astArguments.jjtGetChild(0)).getName();
      } else {
        throw new RuntimeException("Expecting first argument to be identifer: "
            + astArguments.jjtGetChild(0).getClass().getName() + ", jexlNode: " + jexlNode);
      }

      String likeString = null;

      if (astArguments.jjtGetChild(1) instanceof ASTStringLiteral) {
        likeString = ((ASTStringLiteral)astArguments.jjtGetChild(1)).getLiteral();
      } else {
        throw new RuntimeException("Expecting second argument to be string: " 
            + astArguments.jjtGetChild(1).getClass().getName() + ", jexlNode: " + jexlNode);
      }

      GrouperDataFieldConfig grouperDataFieldConfig = grouperJexlScriptAnalysis.getGrouperDataEngine().getFieldConfigByAlias().get(attributeAlias.toLowerCase());
      GrouperDataFieldType fieldDataType = grouperDataFieldConfig.getFieldDataType();
      
      if (fieldDataType != GrouperDataFieldType.integer && fieldDataType != GrouperDataFieldType.string) {
        throw new RuntimeException("The 'hasAttributeLike' function must be used on strings or integers! " + attributeAlias + " -> " + fieldDataType.name());
      }
      
      if (fieldDataType == GrouperDataFieldType.string) {
        grouperJexlScriptPart.getWhereClause().append("exists (select 1 from grouper_data_row_field_assign gdrfa, grouper_dictionary gd where gdrfa.data_field_internal_id = ? "
            + "and data_row_assign_internal_id = gdra.internal_id and gd.the_text like ? " 
            + (GrouperDdlUtils.isOracle() ? " escape '\\' " : "") + " and gdrfa.value_dictionary_internal_id = gd.internal_id ) ");
      } else if (fieldDataType == GrouperDataFieldType.integer) {
        grouperJexlScriptPart.getWhereClause().append("exists (select 1 from grouper_data_row_field_assign gdrfa where gdrfa.data_field_internal_id = ? "
            + "and data_row_assign_internal_id = gdra.internal_id and gdrfa.value_integer like ? " 
            + (GrouperDdlUtils.isOracle() ? " escape '\\' " : "") + " ) ");
      }
      grouperJexlScriptPart.getArguments().add(new MultiKey("attribute", attributeAlias));
      grouperJexlScriptPart.getArguments().add(new MultiKey("bindVar", likeString));
      
      grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasRowAttributeValue1"))
        .append(" '").append(GrouperUtil.xmlEscape(attributeAlias)).append("' ").append(GrouperTextContainer.textOrNull("jexlAnalysisHasRowAttributeLikeValue")).append(" '")
        .append(GrouperUtil.xmlEscape(likeString)).append("'");



    } else if (jexlNode instanceof ASTFunctionNode && jexlNode.jjtGetNumChildren() > 0
        && jexlNode.jjtGetChild(0) instanceof ASTIdentifier
        && StringUtils.equalsIgnoreCase("attributeCompare", ((ASTIdentifier)jexlNode.jjtGetChild(0)).getName())) {

      if (jexlNode.jjtGetNumChildren() != 2 || (!(jexlNode.jjtGetChild(1) instanceof ASTArguments))) {
        throw new RuntimeException("Expecting two JEXL children for attributeCompare: " + jexlNode.getClass().getName() + ", children: "
            + jexlNode.jjtGetNumChildren() + ", jexlNode: " + jexlNode);
      }

      ASTArguments astArguments = (ASTArguments)jexlNode.jjtGetChild(1);

      if (astArguments.jjtGetNumChildren() != 1) {
        throw new RuntimeException("Expecting one argument (a comparison expression) for attributeCompare: " + astArguments.getClass().getName() + ", children: "
            + astArguments.jjtGetNumChildren() + ", jexlNode: " + jexlNode);
      }

      JexlNode comparisonNode = astArguments.jjtGetChild(0);

      // must be a comparison operator
      if (!(comparisonNode instanceof ASTEQNode) && !(comparisonNode instanceof ASTNENode)
          && !(comparisonNode instanceof ASTLTNode) && !(comparisonNode instanceof ASTLENode)
          && !(comparisonNode instanceof ASTGTNode) && !(comparisonNode instanceof ASTGENode)) {
        throw new RuntimeException("Expecting a comparison operator (==, !=, <, <=, >, >=) inside attributeCompare, but got: "
            + comparisonNode.getClass().getName() + ", jexlNode: " + jexlNode);
      }

      if (comparisonNode.jjtGetNumChildren() != 2) {
        throw new RuntimeException("Expecting two children in comparison for attributeCompare: " + comparisonNode.getClass().getName()
            + ", children: " + comparisonNode.jjtGetNumChildren() + ", jexlNode: " + jexlNode);
      }

      // parse left side: either ASTIdentifier or ASTAddNode/ASTSubNode(ASTIdentifier, ASTNumberLiteral)
      String leftFieldAlias = null;
      Long leftMathOffset = null;
      JexlNode leftNode = comparisonNode.jjtGetChild(0);
      if (leftNode instanceof ASTIdentifier) {
        leftFieldAlias = ((ASTIdentifier)leftNode).getName();
      } else if (leftNode instanceof ASTAddNode && leftNode.jjtGetNumChildren() == 2
          && leftNode.jjtGetChild(0) instanceof ASTIdentifier && leftNode.jjtGetChild(1) instanceof ASTNumberLiteral) {
        leftFieldAlias = ((ASTIdentifier)leftNode.jjtGetChild(0)).getName();
        leftMathOffset = ((ASTNumberLiteral)leftNode.jjtGetChild(1)).getLiteral().longValue();
      } else if (leftNode instanceof ASTSubNode && leftNode.jjtGetNumChildren() == 2
          && leftNode.jjtGetChild(0) instanceof ASTIdentifier && leftNode.jjtGetChild(1) instanceof ASTNumberLiteral) {
        leftFieldAlias = ((ASTIdentifier)leftNode.jjtGetChild(0)).getName();
        leftMathOffset = -1L * ((ASTNumberLiteral)leftNode.jjtGetChild(1)).getLiteral().longValue();
      } else {
        throw new RuntimeException("Left side of attributeCompare must be a field alias or field alias +/- number, but got: "
            + leftNode.getClass().getName() + ", jexlNode: " + jexlNode);
      }

      // parse right side: same pattern
      String rightFieldAlias = null;
      Long rightMathOffset = null;
      JexlNode rightNode = comparisonNode.jjtGetChild(1);
      if (rightNode instanceof ASTIdentifier) {
        rightFieldAlias = ((ASTIdentifier)rightNode).getName();
      } else if (rightNode instanceof ASTAddNode && rightNode.jjtGetNumChildren() == 2
          && rightNode.jjtGetChild(0) instanceof ASTIdentifier && rightNode.jjtGetChild(1) instanceof ASTNumberLiteral) {
        rightFieldAlias = ((ASTIdentifier)rightNode.jjtGetChild(0)).getName();
        rightMathOffset = ((ASTNumberLiteral)rightNode.jjtGetChild(1)).getLiteral().longValue();
      } else if (rightNode instanceof ASTSubNode && rightNode.jjtGetNumChildren() == 2
          && rightNode.jjtGetChild(0) instanceof ASTIdentifier && rightNode.jjtGetChild(1) instanceof ASTNumberLiteral) {
        rightFieldAlias = ((ASTIdentifier)rightNode.jjtGetChild(0)).getName();
        rightMathOffset = -1L * ((ASTNumberLiteral)rightNode.jjtGetChild(1)).getLiteral().longValue();
      } else {
        throw new RuntimeException("Right side of attributeCompare must be a field alias or field alias +/- number, but got: "
            + rightNode.getClass().getName() + ", jexlNode: " + jexlNode);
      }

      // validate both fields exist and are row columns
      GrouperDataFieldConfig leftFieldConfig = grouperJexlScriptAnalysis.getGrouperDataEngine().getFieldConfigByAlias().get(leftFieldAlias.toLowerCase());
      if (leftFieldConfig == null) {
        throw new RuntimeException("attributeCompare left field not found: '" + leftFieldAlias + "'");
      }
      GrouperDataFieldConfig rightFieldConfig = grouperJexlScriptAnalysis.getGrouperDataEngine().getFieldConfigByAlias().get(rightFieldAlias.toLowerCase());
      if (rightFieldConfig == null) {
        throw new RuntimeException("attributeCompare right field not found: '" + rightFieldAlias + "'");
      }

      GrouperDataFieldType leftFieldType = leftFieldConfig.getFieldDataType();
      GrouperDataFieldType rightFieldType = rightFieldConfig.getFieldDataType();

      // validate types are compatible
      boolean leftIsInteger = (leftFieldType == GrouperDataFieldType.integer || leftFieldType == GrouperDataFieldType.timestamp || leftFieldType == GrouperDataFieldType.bool);
      boolean rightIsInteger = (rightFieldType == GrouperDataFieldType.integer || rightFieldType == GrouperDataFieldType.timestamp || rightFieldType == GrouperDataFieldType.bool);
      boolean leftIsString = (leftFieldType == GrouperDataFieldType.string);
      boolean rightIsString = (rightFieldType == GrouperDataFieldType.string);

      if (leftIsInteger != rightIsInteger) {
        throw new RuntimeException("attributeCompare fields must be compatible types. Left: " + leftFieldAlias + " (" + leftFieldType
            + "), Right: " + rightFieldAlias + " (" + rightFieldType + ")");
      }

      // math only allowed on integer types
      if (leftMathOffset != null && !leftIsInteger) {
        throw new RuntimeException("Math expressions (+/-) are only allowed on integer/timestamp/boolean fields in attributeCompare. Field: " + leftFieldAlias + " (" + leftFieldType + ")");
      }
      if (rightMathOffset != null && !rightIsInteger) {
        throw new RuntimeException("Math expressions (+/-) are only allowed on integer/timestamp/boolean fields in attributeCompare. Field: " + rightFieldAlias + " (" + rightFieldType + ")");
      }

      // determine operator
      String operator = null;
      String label = null;
      if (comparisonNode instanceof ASTEQNode) {
        operator = "=";
        label = "=";
      } else if (comparisonNode instanceof ASTNENode) {
        operator = "!=";
        label = "!=";
      } else if (comparisonNode instanceof ASTLTNode) {
        operator = "<";
        label = "<";
      } else if (comparisonNode instanceof ASTLENode) {
        operator = "<=";
        label = "<=";
      } else if (comparisonNode instanceof ASTGTNode) {
        operator = ">";
        label = ">";
      } else if (comparisonNode instanceof ASTGENode) {
        operator = ">=";
        label = ">=";
      }

      // build SQL
      // bind var order must match execution order: left_field_id, [left_offset], right_field_id, [right_offset]
      boolean isOrderingComparison = (comparisonNode instanceof ASTLTNode || comparisonNode instanceof ASTLENode
          || comparisonNode instanceof ASTGTNode || comparisonNode instanceof ASTGENode);

      // GRP-6828: ordering comparisons on string fields are not supported; use hasAttributeBetween() instead
      if (leftIsString && isOrderingComparison) {
        throw new RuntimeException("Ordering comparisons (<, <=, >, >=) are not supported on string fields in attributeCompare. "
            + "Use hasAttributeBetween() for string range queries. Fields: '" + leftFieldAlias + "', '" + rightFieldAlias + "'");
      }

      String leftCol = leftIsString ? "value_dictionary_internal_id" : "value_integer";
      String rightCol = rightIsString ? "value_dictionary_internal_id" : "value_integer";

      String leftExpr = "gdrfa_cc1." + leftCol;
      if (leftMathOffset != null) {
        leftExpr = "(" + leftExpr + " + ?)";
      }
      String rightExpr = "gdrfa_cc2." + rightCol;
      if (rightMathOffset != null) {
        rightExpr = "(" + rightExpr + " + ?)";
      }

      // SQL: left_field_id bind, then left_offset bind (if any), then comparison with right subquery
      // right_field_id bind, then right_offset bind (if any)
      grouperJexlScriptPart.getWhereClause().append("exists (select 1 from grouper_data_row_field_assign gdrfa_cc1 "
          + "where gdrfa_cc1.data_row_assign_internal_id = gdra.internal_id "
          + "and gdrfa_cc1.data_field_internal_id = ? "
          + "and " + leftExpr + " " + operator + " "
          + "(select gdrfa_cc2." + rightCol + (rightMathOffset != null ? " + ?" : "")
          + " from grouper_data_row_field_assign gdrfa_cc2 "
          + "where gdrfa_cc2.data_row_assign_internal_id = gdra.internal_id "
          + "and gdrfa_cc2.data_field_internal_id = ?)) ");

      grouperJexlScriptPart.getArguments().add(new MultiKey("attributeCompareLeft", leftFieldAlias, leftMathOffset));
      grouperJexlScriptPart.getArguments().add(new MultiKey("attributeCompareRight", rightFieldAlias, rightMathOffset));

      // build display description
      String leftDisplay = leftFieldAlias + (leftMathOffset != null ? (leftMathOffset >= 0 ? " + " + leftMathOffset : " - " + (-leftMathOffset)) : "");
      String rightDisplay = rightFieldAlias + (rightMathOffset != null ? (rightMathOffset >= 0 ? " + " + rightMathOffset : " - " + (-rightMathOffset)) : "");
      grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisAttributeCompare"))
        .append(" ").append(GrouperUtil.xmlEscape(leftDisplay)).append(" ").append(label).append(" ").append(GrouperUtil.xmlEscape(rightDisplay));

    } else if ((jexlNode instanceof ASTEQNode) && 2==jexlNode.jjtGetNumChildren() && jexlNode.jjtGetChild(1) instanceof ASTNullLiteral) {
      if (!(jexlNode.jjtGetChild(0) instanceof ASTIdentifier)) {
        throw new RuntimeException("Not expecting node type: " + jexlNode.jjtGetChild(0).getClass().getName() 
            + ", children: " + jexlNode.jjtGetChild(0).jjtGetNumChildren());
      }
      
      ASTIdentifier leftPart = (ASTIdentifier)jexlNode.jjtGetChild(0);
      
      grouperJexlScriptPart.getWhereClause().append("exists (select 1 from grouper_data_row_field_assign gdrfa where data_row_assign_internal_id = gdra.internal_id "
          + "and gdrfa.data_field_internal_id = ? and gdrfa.$$ATTRIBUTE_COL_" + (grouperJexlScriptPart.getArguments().size()+1) + "$$ is null) ");
      grouperJexlScriptPart.getArguments().add(new MultiKey("attribute", leftPart.getName()));
      grouperJexlScriptPart.getArguments().add(new MultiKey("attributeValue", Void.TYPE));
      
      grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasRowAttributeValue1"))
        .append(" '").append(GrouperUtil.xmlEscape(leftPart.getName())).append("' ").append(GrouperTextContainer.textOrNull("jexlAnalysisHasRowAttributeValue2"))
        .append(" null");

    } else if ((jexlNode instanceof ASTEQNode || jexlNode instanceof ASTLTNode || jexlNode instanceof ASTLENode 
        || jexlNode instanceof ASTGTNode || jexlNode instanceof ASTGENode) && 2==jexlNode.jjtGetNumChildren()) {
      if (!(jexlNode.jjtGetChild(0) instanceof ASTIdentifier)) {
        throw new RuntimeException("Not expecting node type: " + jexlNode.jjtGetChild(0).getClass().getName() 
            + ", children: " + jexlNode.jjtGetChild(0).jjtGetNumChildren());
      }
      if (!(jexlNode.jjtGetChild(1) instanceof ASTIdentifier) && !(jexlNode.jjtGetChild(1) instanceof ASTNumberLiteral)
          && !(jexlNode.jjtGetChild(1) instanceof ASTStringLiteral) && !(jexlNode.jjtGetChild(1) instanceof ASTUnaryMinusNode) ) {
        throw new RuntimeException("Not expecting node type: " + jexlNode.jjtGetChild(1).getClass().getName() 
            + ", children: " + jexlNode.jjtGetChild(1).jjtGetNumChildren());
      }
      
      if (jexlNode.jjtGetChild(1) instanceof ASTUnaryMinusNode && (jexlNode.jjtGetChild(1).jjtGetNumChildren() != 1 
          || !(jexlNode.jjtGetChild(1).jjtGetChild(0) instanceof ASTNumberLiteral))) {
        throw new RuntimeException("Not expecting child node type for negative: " 
          + (jexlNode.jjtGetChild(1).jjtGetNumChildren() > 0 ? jexlNode.jjtGetChild(0).getClass().getName() : "0 children!")
            + ", children: " + jexlNode.jjtGetChild(1).jjtGetNumChildren());
        
      }
      
      ASTIdentifier leftPart = (ASTIdentifier)jexlNode.jjtGetChild(0);
      String rightPartValue = null;
      if (jexlNode.jjtGetChild(1) instanceof ASTIdentifier) {
        rightPartValue = ((ASTIdentifier)jexlNode.jjtGetChild(1)).getName();
      } else if (jexlNode.jjtGetChild(1) instanceof ASTNumberLiteral) {
        rightPartValue = GrouperUtil.stringValue(((ASTNumberLiteral)jexlNode.jjtGetChild(1)).getLiteral());
      } else if (jexlNode.jjtGetChild(1) instanceof ASTStringLiteral) {
        rightPartValue = ((ASTStringLiteral)jexlNode.jjtGetChild(1)).getLiteral();
      } else if (jexlNode.jjtGetChild(1) instanceof ASTUnaryMinusNode) {
        rightPartValue = GrouperUtil.stringValue(negate((ASTNumberLiteral)jexlNode.jjtGetChild(1).jjtGetChild(0)));
      } 
      String operator = null;
      String label = null;
      if (jexlNode instanceof ASTEQNode) {
        operator = "=";
        label = "jexlAnalysisHasRowAttributeValue2";
      } else if (jexlNode instanceof ASTLTNode) {
        operator = "<";
        label = "jexlAnalysisHasRowAttributeValueLessThan2";
      } else if (jexlNode instanceof ASTLENode) {
        operator = "<=";
        label = "jexlAnalysisHasRowAttributeValueLessThanEqual2";
      } else if (jexlNode instanceof ASTGTNode) {
        operator = ">";
        label = "jexlAnalysisHasRowAttributeValueGreaterThan2";
      } else if (jexlNode instanceof ASTGENode) {
        operator = ">=";
        label = "jexlAnalysisHasRowAttributeValueGreaterThanEqual2";
      } else {
        throw new RuntimeException("Not expecting node type: " + jexlNode
            + ", children: " + jexlNode.jjtGetNumChildren());
      }
      boolean isOrderingOperator = (jexlNode instanceof ASTLTNode || jexlNode instanceof ASTLENode
          || jexlNode instanceof ASTGTNode || jexlNode instanceof ASTGENode);
      if (isOrderingOperator) {
        GrouperDataFieldConfig grouperDataFieldConfigCheck = grouperJexlScriptAnalysis.getGrouperDataEngine().getFieldConfigByAlias().get(leftPart.getName().toLowerCase());
        if (grouperDataFieldConfigCheck != null && grouperDataFieldConfigCheck.getFieldDataType() == GrouperDataFieldType.string) {
          throw new RuntimeException("Ordering comparisons (<, <=, >, >=) are not supported on string fields in hasRow. "
              + "Use hasAttributeBetween() for string range queries. Field: '" + leftPart.getName() + "'");
        }
      }
      grouperJexlScriptPart.getWhereClause().append("exists (select 1 from grouper_data_row_field_assign gdrfa where data_row_assign_internal_id = gdra.internal_id "
          + "and gdrfa.data_field_internal_id = ? and gdrfa.$$ATTRIBUTE_COL_" + (grouperJexlScriptPart.getArguments().size()+1) + "$$ " + operator + " ?) ");
      grouperJexlScriptPart.getArguments().add(new MultiKey("attribute", leftPart.getName()));
      grouperJexlScriptPart.getArguments().add(new MultiKey("attributeValue", rightPartValue));
      
      grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasRowAttributeValue1"))
        .append(" '").append(GrouperUtil.xmlEscape(leftPart.getName())).append("' ").append(GrouperTextContainer.textOrNull(label)).append(" '")
        .append(GrouperUtil.xmlEscape(rightPartValue)).append("'");

    } else if (jexlNode instanceof ASTNENode && 2==jexlNode.jjtGetNumChildren()) {
      if (!(jexlNode.jjtGetChild(0) instanceof ASTIdentifier)) {
        throw new RuntimeException("Not expecting node type: " + jexlNode.jjtGetChild(0).getClass().getName() 
            + ", children: " + jexlNode.jjtGetChild(0).jjtGetNumChildren());
      }
      if (!(jexlNode.jjtGetChild(1) instanceof ASTIdentifier) && !(jexlNode.jjtGetChild(1) instanceof ASTNumberLiteral)
          && !(jexlNode.jjtGetChild(1) instanceof ASTStringLiteral) && !(jexlNode.jjtGetChild(1) instanceof ASTNullLiteral) 
          && !(jexlNode.jjtGetChild(1) instanceof ASTUnaryMinusNode)) {
        throw new RuntimeException("Not expecting node type: " + jexlNode.jjtGetChild(1).getClass().getName() 
            + ", children: " + jexlNode.jjtGetChild(1).jjtGetNumChildren());
      }
      
      if (jexlNode.jjtGetChild(1) instanceof ASTUnaryMinusNode && (jexlNode.jjtGetChild(1).jjtGetNumChildren() != 1 
          || !(jexlNode.jjtGetChild(1).jjtGetChild(0) instanceof ASTNumberLiteral))) {
        throw new RuntimeException("Not expecting child node type for negative: " 
          + (jexlNode.jjtGetChild(1).jjtGetNumChildren() > 0 ? jexlNode.jjtGetChild(0).getClass().getName() : "0 children!")
            + ", children: " + jexlNode.jjtGetChild(1).jjtGetNumChildren());
        
      }
      ASTIdentifier leftPart = (ASTIdentifier)jexlNode.jjtGetChild(0);
      String rightPartValue = null;
      boolean rightPartNull = false;
      if (jexlNode.jjtGetChild(1) instanceof ASTIdentifier) {
        rightPartValue = ((ASTIdentifier)jexlNode.jjtGetChild(1)).getName();
      } else if (jexlNode.jjtGetChild(1) instanceof ASTNumberLiteral) {
        rightPartValue = GrouperUtil.stringValue(((ASTNumberLiteral)jexlNode.jjtGetChild(1)).getLiteral());
      } else if (jexlNode.jjtGetChild(1) instanceof ASTStringLiteral) {
        rightPartValue = ((ASTStringLiteral)jexlNode.jjtGetChild(1)).getLiteral();
      } else if (jexlNode.jjtGetChild(1) instanceof ASTNullLiteral) {
        rightPartNull = true;
      } else if (jexlNode.jjtGetChild(1) instanceof ASTUnaryMinusNode) {
        rightPartValue = GrouperUtil.stringValue(negate((ASTNumberLiteral)jexlNode.jjtGetChild(1).jjtGetChild(0)));
      } 

      grouperJexlScriptPart.getWhereClause().append((rightPartNull ? "" : "not ") 
          + "exists (select 1 from grouper_data_row_field_assign gdrfa where data_row_assign_internal_id = gdra.internal_id "
          + "and gdrfa.data_field_internal_id = ? and gdrfa.$$ATTRIBUTE_COL_" + (grouperJexlScriptPart.getArguments().size()+1) + "$$ " + (rightPartNull ? " is not null" : "= ?")  + ") ");
      
      grouperJexlScriptPart.getArguments().add(new MultiKey("attribute", leftPart.getName()));
      grouperJexlScriptPart.getArguments().add(new MultiKey("attributeValue", rightPartNull ? Void.TYPE : rightPartValue));
      
      grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasRowAttributeValue1"))
        .append(" '").append(GrouperUtil.xmlEscape(leftPart.getName())).append("' ").append(GrouperTextContainer.textOrNull("jexlAnalysisHasRowWithoutAttributeValue2")).append(" '")
        .append(GrouperUtil.xmlEscape(rightPartNull ? "null" : rightPartValue)).append("'");

    } else if ((jexlNode instanceof ASTEQNode) && 2==jexlNode.jjtGetNumChildren() && jexlNode.jjtGetChild(1) instanceof ASTNullLiteral) {
      if (!(jexlNode.jjtGetChild(0) instanceof ASTIdentifier)) {
        throw new RuntimeException("Not expecting node type: " + jexlNode.jjtGetChild(0).getClass().getName() 
            + ", children: " + jexlNode.jjtGetChild(0).jjtGetNumChildren());
      }
      
      ASTIdentifier leftPart = (ASTIdentifier)jexlNode.jjtGetChild(0);
      
      grouperJexlScriptPart.getWhereClause().append("exists (select 1 from grouper_data_row_field_assign gdrfa where data_row_assign_internal_id = gdra.internal_id "
          + "and gdrfa.data_field_internal_id = ? and gdrfa.$$ATTRIBUTE_COL_" + (grouperJexlScriptPart.getArguments().size()+1) + "$$ is null) ");
      grouperJexlScriptPart.getArguments().add(new MultiKey("attribute", leftPart.getName()));
      grouperJexlScriptPart.getArguments().add(new MultiKey("attributeValue", Void.TYPE));
      
      grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasRowAttributeValue1"))
        .append(" '").append(GrouperUtil.xmlEscape(leftPart.getName())).append("' ").append(GrouperTextContainer.textOrNull("jexlAnalysisHasRowAttributeValue2"))
        .append(" null");

    } else if (jexlNode instanceof ASTERNode && 2==jexlNode.jjtGetNumChildren()) {
      if (!(jexlNode.jjtGetChild(0) instanceof ASTIdentifier)) {
        throw new RuntimeException("Not expecting node type: " + jexlNode.jjtGetChild(0).getClass().getName() 
            + ", children: " + jexlNode.jjtGetChild(0).jjtGetNumChildren());
      }
      if (!(jexlNode.jjtGetChild(1) instanceof ASTArrayLiteral)) {
        throw new RuntimeException("Not expecting node type: " + jexlNode.jjtGetChild(1).getClass().getName() 
            + ", children: " + jexlNode.jjtGetChild(1).jjtGetNumChildren());
      }
      
      ASTIdentifier leftPart = (ASTIdentifier)jexlNode.jjtGetChild(0);
      
      ASTArrayLiteral astArrayLiteral = (ASTArrayLiteral)jexlNode.jjtGetChild(1);
      
      grouperJexlScriptPart.getWhereClause().append("exists (select 1 from grouper_data_row_field_assign gdrfa where data_row_assign_internal_id = gdra.internal_id "
          + "and gdrfa.data_field_internal_id = ? and gdrfa.$$ATTRIBUTE_COL_" + (grouperJexlScriptPart.getArguments().size()+1) + "$$ in ("+ GrouperClientUtils.appendQuestions(astArrayLiteral.jjtGetNumChildren()) +")) ");
      grouperJexlScriptPart.getArguments().add(new MultiKey("attribute", leftPart.getName()));
     
      
      for (int i=0; i < astArrayLiteral.jjtGetNumChildren(); i++) {
        JexlNode jjtGetChild = astArrayLiteral.jjtGetChild(i);
        String rightPartSingleValue = null;
        
//        GrouperJexlScriptPart grouperJexlScriptPartClone2 = new GrouperJexlScriptPart();
//        grouperJexlScriptAnalysis.getGrouperJexlScriptParts().add(grouperJexlScriptPartClone2);
        
//        grouperJexlScriptPartClone2.getWhereClause().append("exists (select 1 from grouper_data_row_field_assign gdrfa where data_row_assign_internal_id = gdra.internal_id "
//            + "and gdrfa.data_field_internal_id = ? and gdrfa.$$ATTRIBUTE_COL_" + (grouperJexlScriptPart.getArguments().size()+1) + "$$ = ? )");
//        grouperJexlScriptPartClone2.getArguments().add(new MultiKey("attribute", leftPart.getName()));
        
        if (jjtGetChild instanceof ASTIdentifier) {
          rightPartSingleValue = ((ASTIdentifier)jjtGetChild).getName();
          
          if (i == 0) {
            grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasRowAttributeValue1"))
            .append(" '").append(GrouperUtil.xmlEscape(leftPart.getName())).append("' ").append(GrouperTextContainer.textOrNull("jexlAnalysisHasRowAttributeAnyValue")).append(" '")
            .append(GrouperUtil.xmlEscape(rightPartSingleValue)).append("'");
          } else {
            grouperJexlScriptPart.getDisplayDescription().append(", '").append(GrouperUtil.xmlEscape(rightPartSingleValue)).append("'");
          }
          
//          grouperJexlScriptPartClone2.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasRowAttributeValue1"))
//          .append(" '").append(GrouperUtil.xmlEscape(leftPart.getName())).append("' ").append(GrouperTextContainer.textOrNull("jexlAnalysisHasRowAttributeValue2")).append(" '")
//          .append(GrouperUtil.xmlEscape(rightPartSingleValue)).append("'");
          
        } else if (jexlNode.jjtGetChild(1) instanceof ASTUnaryMinusNode) {
          rightPartSingleValue = GrouperUtil.stringValue(negate((ASTNumberLiteral)jexlNode.jjtGetChild(1).jjtGetChild(0)));

          if (i == 0) {
            grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasRowAttributeValue1"))
            .append(" '").append(GrouperUtil.xmlEscape(leftPart.getName())).append("' ").append(GrouperTextContainer.textOrNull("jexlAnalysisHasRowAttributeAnyValue"))
            .append(GrouperUtil.xmlEscape(rightPartSingleValue));
          } else {
            grouperJexlScriptPart.getDisplayDescription().append(", ").append(GrouperUtil.xmlEscape(rightPartSingleValue));
          }

        } else if (jjtGetChild instanceof ASTNumberLiteral) {
          rightPartSingleValue = GrouperUtil.stringValue(((ASTNumberLiteral)jjtGetChild).getLiteral());
          
          if (i == 0) {
            grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasRowAttributeValue1"))
            .append(" '").append(GrouperUtil.xmlEscape(leftPart.getName())).append("' ").append(GrouperTextContainer.textOrNull("jexlAnalysisHasRowAttributeAnyValue"))
            .append(GrouperUtil.xmlEscape(rightPartSingleValue));
          } else {
            grouperJexlScriptPart.getDisplayDescription().append(", ").append(GrouperUtil.xmlEscape(rightPartSingleValue));
          }
          
//          grouperJexlScriptPartClone2.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasRowAttributeValue1"))
//          .append(" '").append(GrouperUtil.xmlEscape(leftPart.getName())).append("' ").append(GrouperTextContainer.textOrNull("jexlAnalysisHasRowAttributeValue2"))
//          .append(GrouperUtil.xmlEscape(rightPartSingleValue));
          
        } else if (jjtGetChild instanceof ASTStringLiteral) {
          rightPartSingleValue = ((ASTStringLiteral)jjtGetChild).getLiteral();
          if (i == 0) {
            grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasRowAttributeValue1"))
            .append(" '").append(GrouperUtil.xmlEscape(leftPart.getName())).append("' ").append(GrouperTextContainer.textOrNull("jexlAnalysisHasRowAttributeAnyValue")).append(" '")
            .append(GrouperUtil.xmlEscape(rightPartSingleValue)).append("'");
          } else {
            grouperJexlScriptPart.getDisplayDescription().append(", '").append(GrouperUtil.xmlEscape(rightPartSingleValue)).append("'");
          }
          
//          grouperJexlScriptPartClone2.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasRowAttributeValue1"))
//          .append(" '").append(GrouperUtil.xmlEscape(leftPart.getName())).append("' ").append(GrouperTextContainer.textOrNull("jexlAnalysisHasRowAttributeValue2")).append(" '")
//          .append(GrouperUtil.xmlEscape(rightPartSingleValue)).append("'");
        } 
        
        grouperJexlScriptPart.getArguments().add(new MultiKey("attributeValue", rightPartSingleValue));
//        grouperJexlScriptPartClone2.getArguments().add(new MultiKey("attributeValue", rightPartSingleValue));
        
      }

    }
    
    else if (jexlNode instanceof ASTJexlScript && 1==jexlNode.jjtGetNumChildren()) {
      analyzeJexlRowToSqlHelper(grouperJexlScriptAnalysis, grouperJexlScriptPart, rowJexlScriptPart, jexlNode.jjtGetChild(0), clonePart);
    } else if (jexlNode instanceof ASTReferenceExpression && 1==jexlNode.jjtGetNumChildren()) {
      grouperJexlScriptPart.getWhereClause().append("(");
      grouperJexlScriptPart.getDisplayDescription().append("(");
      analyzeJexlRowToSqlHelper(grouperJexlScriptAnalysis, grouperJexlScriptPart, rowJexlScriptPart, jexlNode.jjtGetChild(0), clonePart);
      grouperJexlScriptPart.getWhereClause().append(")");
      grouperJexlScriptPart.getDisplayDescription().append(")");
//      if (clonePart) {
//        grouperJexlScriptPartClone = rowJexlScriptPart.clone();
//        grouperJexlScriptAnalysis.getGrouperJexlScriptParts().add(grouperJexlScriptPartClone);
//        analyzeJexlRowToSqlHelper(grouperJexlScriptAnalysis, grouperJexlScriptPartClone, rowJexlScriptPart, jexlNode.jjtGetChild(0), false);
//        grouperJexlScriptPartClone.getWhereClause().append(")");
//      }
    } else if (jexlNode instanceof ASTNotNode && 1==jexlNode.jjtGetNumChildren()) {
      grouperJexlScriptPart.getWhereClause().append(" not ");
      grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisNot")).append(" ");
      analyzeJexlRowToSqlHelper(grouperJexlScriptAnalysis, grouperJexlScriptPart, rowJexlScriptPart, jexlNode.jjtGetChild(0), clonePart);
      
      if (clonePart) {
        grouperJexlScriptPartClone = rowJexlScriptPart.clone();
        grouperJexlScriptAnalysis.getGrouperJexlScriptParts().add(grouperJexlScriptPartClone);
        analyzeJexlRowToSqlHelper(grouperJexlScriptAnalysis, grouperJexlScriptPartClone, rowJexlScriptPart, jexlNode.jjtGetChild(0), false);
        
        // TODO improve this
        grouperJexlScriptPartClone.getWhereClause().append(StringUtils.repeat(" ) ", 
            StringUtils.countMatches(grouperJexlScriptPartClone.getWhereClause().toString(), "(")
            - StringUtils.countMatches(grouperJexlScriptPartClone.getWhereClause().toString(), ")")));
        
      }

      
    } else if (jexlNode instanceof ASTAndNode) {
      for (int i=0;i<jexlNode.jjtGetNumChildren(); i++) {
        if (i>0) {
          grouperJexlScriptPart.getWhereClause().append(" and ");
          grouperJexlScriptPart.getDisplayDescription().append(" ").append(GrouperTextContainer.textOrNull("jexlAnalysisAnd")).append(" ");
        }
        analyzeJexlRowToSqlHelper(grouperJexlScriptAnalysis, grouperJexlScriptPart, rowJexlScriptPart, jexlNode.jjtGetChild(i), clonePart);
        if (clonePart) {
          grouperJexlScriptPartClone = rowJexlScriptPart.clone();
          grouperJexlScriptAnalysis.getGrouperJexlScriptParts().add(grouperJexlScriptPartClone);
          analyzeJexlRowToSqlHelper(grouperJexlScriptAnalysis, grouperJexlScriptPartClone, rowJexlScriptPart, jexlNode.jjtGetChild(i), false);
          // TODO improve this
          grouperJexlScriptPartClone.getWhereClause().append(StringUtils.repeat(" ) ", 
              StringUtils.countMatches(grouperJexlScriptPartClone.getWhereClause().toString(), "(")
              - StringUtils.countMatches(grouperJexlScriptPartClone.getWhereClause().toString(), ")")));
        }
      }
    } else if (jexlNode instanceof ASTOrNode) {
      
      for (int i=0;i<jexlNode.jjtGetNumChildren(); i++) {
        if (i>0) {
          grouperJexlScriptPart.getWhereClause().append(" or ");
          grouperJexlScriptPart.getDisplayDescription().append(" ").append(GrouperTextContainer.textOrNull("jexlAnalysisOr")).append(" ");
        }
        analyzeJexlRowToSqlHelper(grouperJexlScriptAnalysis, grouperJexlScriptPart, rowJexlScriptPart, jexlNode.jjtGetChild(i), clonePart);
        if (clonePart) {
          grouperJexlScriptPartClone = rowJexlScriptPart.clone();
          grouperJexlScriptAnalysis.getGrouperJexlScriptParts().add(grouperJexlScriptPartClone);
          analyzeJexlRowToSqlHelper(grouperJexlScriptAnalysis, grouperJexlScriptPartClone, rowJexlScriptPart, jexlNode.jjtGetChild(i), false);
          // TODO improve this
          grouperJexlScriptPartClone.getWhereClause().append(StringUtils.repeat(" ) ", 
              StringUtils.countMatches(grouperJexlScriptPartClone.getWhereClause().toString(), "(")
              - StringUtils.countMatches(grouperJexlScriptPartClone.getWhereClause().toString(), ")")));
        }
      }
      
    } else {
      throw new RuntimeException("Not expecting node type: " + jexlNode.getClass().getName() + ", children: " + jexlNode.jjtGetNumChildren() + ", jexlNode: " + jexlNode);
    }
  }
  


  /**
   * extract a string value from a JEXL literal node (string, number, or negative number)
   */
  private static String extractLiteralValue(JexlNode node) {
    if (node instanceof ASTStringLiteral) {
      return ((ASTStringLiteral) node).getLiteral();
    } else if (node instanceof ASTNumberLiteral) {
      return GrouperUtil.stringValue(((ASTNumberLiteral) node).getLiteral());
    } else if (node instanceof ASTUnaryMinusNode && node.jjtGetNumChildren() == 1
        && node.jjtGetChild(0) instanceof ASTNumberLiteral) {
      return GrouperUtil.stringValue(negate((ASTNumberLiteral) node.jjtGetChild(0)));
    } else if (node instanceof ASTIdentifier) {
      return ((ASTIdentifier) node).getName();
    }
    throw new RuntimeException("Expected a literal value, got: " + node.getClass().getName());
  }

  /**
   * Parse a hasAttributeBetween comparison argument.
   * Supports patterns like: 'value' < field, 'value' <= field, field < 'value', field <= 'value',
   * field > 'value', field >= 'value', 'value' > field, 'value' >= field.
   * @return MultiKey with (fieldAlias, literalValue, sqlOperator) where sqlOperator is the operator
   *   relative to the field (e.g., ">=" for inclusive lower bound, "<" for exclusive upper bound)
   */
  private static MultiKey parseBetweenComparisonArg(JexlNode node) {
    if (!(node instanceof ASTLTNode) && !(node instanceof ASTLENode)
        && !(node instanceof ASTGTNode) && !(node instanceof ASTGENode)) {
      throw new RuntimeException("hasAttributeBetween arguments must be comparison expressions (<, <=, >, >=), got: "
          + node.getClass().getName());
    }
    if (node.jjtGetNumChildren() != 2) {
      throw new RuntimeException("hasAttributeBetween comparison must have 2 children, got: " + node.jjtGetNumChildren());
    }

    JexlNode left = node.jjtGetChild(0);
    JexlNode right = node.jjtGetChild(1);

    boolean leftIsField = (left instanceof ASTIdentifier);
    boolean rightIsField = (right instanceof ASTIdentifier);

    if (leftIsField == rightIsField) {
      throw new RuntimeException("hasAttributeBetween comparison must have one field and one literal value");
    }

    String fieldAlias;
    String literalValue;
    String sqlOperator;

    if (leftIsField) {
      // field op literal: e.g., affiliationOrg <= 'math'
      fieldAlias = ((ASTIdentifier) left).getName();
      literalValue = extractLiteralValue(right);
      if (node instanceof ASTLTNode) {
        sqlOperator = "<";
      } else if (node instanceof ASTLENode) {
        sqlOperator = "<=";
      } else if (node instanceof ASTGTNode) {
        sqlOperator = ">";
      } else {
        sqlOperator = ">=";
      }
    } else {
      // literal op field: e.g., 'engl' < affiliationOrg  =>  affiliationOrg > 'engl'
      fieldAlias = ((ASTIdentifier) right).getName();
      literalValue = extractLiteralValue(left);
      // flip the operator since we're reversing the sides
      if (node instanceof ASTLTNode) {
        sqlOperator = ">";
      } else if (node instanceof ASTLENode) {
        sqlOperator = ">=";
      } else if (node instanceof ASTGTNode) {
        sqlOperator = "<";
      } else {
        sqlOperator = "<=";
      }
    }

    return new MultiKey(fieldAlias, literalValue, sqlOperator);
  }

  private static Number negate(ASTNumberLiteral jjtGetChild) {
    if (jjtGetChild.isInteger()) {
      return -1 * jjtGetChild.getLiteral().longValue();
    }
    return -1 * jjtGetChild.getLiteral().doubleValue();
  }


  private List<GrouperLoaderJexlScriptGroup> grouperLoaderJexlScriptGroups = null;
  
  /**
   * 
   */
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    
    Map<String, Object> debugMap = Collections.synchronizedMap(new LinkedHashMap<String, Object>());
    RuntimeException runtimeException = null;

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(GcGrouperSync.SCRIPTED_GROUPS);
    
    gcGrouperSync.setSyncEngine(GcGrouperSync.SCRIPTED_GROUPS);
    gcGrouperSync.getGcGrouperSyncDao().store();
    GcGrouperSyncJob gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveOrCreateBySyncType("full");
    gcGrouperSyncJob.waitForRelatedJobsToFinishThenRun(true);
    
    GcGrouperSyncHeartbeat gcGrouperSyncHeartbeat = new GcGrouperSyncHeartbeat();
    gcGrouperSyncHeartbeat.setGcGrouperSyncJob(gcGrouperSyncJob);
    gcGrouperSyncHeartbeat.setFullSync(true);
    gcGrouperSyncHeartbeat.addHeartbeatLogic(new Runnable() {
      @Override
      public void run() {
        
      }
    });

    try {
      if (!gcGrouperSyncHeartbeat.isStarted()) {
        gcGrouperSyncHeartbeat.runHeartbeatThread();
      }

      boolean readOnly = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("otherJob.grouperLoaderJexlScriptFullSync.jexlDaemonsReadonly", false);
      if (readOnly) {
        debugMap.put("readOnly", true);
      }

      // all scripts and sync them with dependency tables

      //  GrouperDaemonUtils.stopProcessingIfJobPaused();

      GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
      
      GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
      
      grouperDataEngine.loadFieldsAndRows(grouperConfig);

      // TODO cache this
      AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_MARKER, true);
  
      Collection<AttributeAssign> attributeAssigns = GrouperUtil.nonNull(new AttributeAssignFinder().addAttributeDefNameId(attributeDefName.getId()).findAttributeAssignFinderResults().getIdToAttributeAssignMap()).values();
      
      debugMap.put("jexlScriptGroups", GrouperUtil.length(attributeAssigns));

      Set<String> groupIds = new HashSet<String>();
      
      for (AttributeAssign attributeAssign : GrouperUtil.nonNull(attributeAssigns)) {

        String ownerGroupId = attributeAssign.getOwnerGroupId();
        if (StringUtils.isBlank(ownerGroupId)) {
          continue;
        }
        groupIds.add(ownerGroupId);
      }
      
      Set<Group> groups = GrouperDAOFactory.getFactory().getGroup().findByUuids(groupIds, false);
      
      Map<String, Group> groupIdToGroup = new HashMap<String, Group>();
      for (Group group : GrouperUtil.nonNull(groups)) {
        groupIdToGroup.put(group.getId(), group);
      }
      
      List<SqlCacheDependency> allMshipHistoryAbacSqlCacheDependencies = null;
      {
        SqlCacheDependencyType sqlCacheDependencyTypeMshipHistoryAbac = SqlCacheDependencyTypeDao.retrieveByName(SqlCacheDependencyTypeDao.NAME_MSHIP_HISTORY_ABAC);
        allMshipHistoryAbacSqlCacheDependencies = SqlCacheDependencyDao.retrieveByDependencyTypeInternalId(sqlCacheDependencyTypeMshipHistoryAbac.getInternalId());
      }
      Map<MultiKey, SqlCacheDependency> allMshipHistoryAbacSqlCacheDependenciesMap = Collections.synchronizedMap(new HashMap<>());
      for (SqlCacheDependency sqlCacheDependency : allMshipHistoryAbacSqlCacheDependencies) {
        allMshipHistoryAbacSqlCacheDependenciesMap.put(new MultiKey(sqlCacheDependency.getOwnerInternalId(), sqlCacheDependency.getDependentInternalId()), sqlCacheDependency);
      }
      
      Set<Long> sqlCacheGroupInternalIdsStillNeedingMshipHistory = Collections.synchronizedSet(new HashSet<Long>());
      
      int threadPoolSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("otherJob.grouperLoaderJexlScriptFullSync.threadPoolSize", 10);
      boolean useThreads = true;
      if (threadPoolSize <= 1) {
        useThreads = false;
      }
      
      List<GrouperFuture> futures = new ArrayList<GrouperFuture>();
      List<GrouperCallable> callablesWithProblems = new ArrayList<GrouperCallable>();      

      for (AttributeAssign attributeAssign : attributeAssigns) {
        GrouperDaemonUtils.stopProcessingIfJobPaused();

        if (StringUtils.isBlank(attributeAssign.getOwnerGroupId())) {
          continue;
        }
        
        GrouperCallable<Void> grouperCallable = new GrouperCallable<Void>("grouperLoaderJexlSyncForOneGroup: " + attributeAssign.getOwnerGroupId()) {

          @Override
          public Void callLogic() {

            Hib3GrouperLoaderLog hib3GrouperLoaderLog = otherJobInput.getHib3GrouperLoaderLog();
            Group group = groupIdToGroup.get(attributeAssign.getOwnerGroupId());

            syncFullGroup(debugMap, hib3GrouperLoaderLog, grouperDataEngine,
                attributeAssign, group, allMshipHistoryAbacSqlCacheDependenciesMap,
                sqlCacheGroupInternalIdsStillNeedingMshipHistory, readOnly);

            return null;
          }
        };
        
        if (!useThreads) {
          grouperCallable.callLogic();
        } else {
          GrouperFuture<Void> future = GrouperUtil.executorServiceSubmit(GrouperUtil.retrieveExecutorService(), grouperCallable, true);
          futures.add(future);
          
          GrouperFuture.waitForJob(futures, threadPoolSize, callablesWithProblems);
        }
      }
      
      //wait for the rest
      GrouperFuture.waitForJob(futures, 0, callablesWithProblems);

      GrouperCallable.tryCallablesWithProblems(callablesWithProblems);
      
      // delete mship history abac dependency (but don't bother deleting the mship history here)
      for (MultiKey multiKey : allMshipHistoryAbacSqlCacheDependenciesMap.keySet()) {
        Long sqlCacheGroupInternalId = (Long)multiKey.getKey(0);
        if (!sqlCacheGroupInternalIdsStillNeedingMshipHistory.contains(sqlCacheGroupInternalId)) {
          SqlCacheDependency sqlCacheDependency = allMshipHistoryAbacSqlCacheDependenciesMap.get(multiKey);
          
          // only delete if this was created more than 2 hours ago to avoid issues if this was just created while analyzing in the UI without saving yet
          if ((System.currentTimeMillis() * 1000 - sqlCacheDependency.getCreatedOn()) > 7200000000L) {
            SqlCacheDependencyDao.delete(sqlCacheDependency);
          }
        }
      }
    } catch (RuntimeException re) {
      runtimeException = re;
      debugMap.put("exception", GrouperUtil.getFullStackTrace(re));

    } finally {
      
      GcGrouperSyncHeartbeat.endAndWaitForThread(gcGrouperSyncHeartbeat);
      debugMap.put("finalLog", true);
      synchronized (GrouperDeprovisioningDaemonLogic.class) {
        try {
          if (gcGrouperSyncJob != null) {
            gcGrouperSyncJob.assignHeartbeatAndEndJob();
          }
        } catch (RuntimeException re2) {
          debugMap.put("exception2", GrouperClientUtils.getFullStackTrace(re2));
          if (runtimeException == null) {
            throw re2;
          }
          
        }
      }

      // remove internal count keys used for example tracking
      debugMap.remove("insertExamples_count");
      debugMap.remove("deleteExamples_count");

      otherJobInput.getHib3GrouperLoaderLog().setJobMessage(GrouperUtil.mapToString(debugMap));

      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }

    }


    if (GrouperUtil.intValue(debugMap.get("errors"), 0) > 0) {
      throw new RuntimeException("Had " + debugMap.get("errors") + " errors, check logs");
    }
    return null;
  }


  public static void syncFullGroup(Map<String, Object> debugMap,
      Hib3GrouperLoaderLog hib3GrouperLoaderLog, GrouperDataEngine grouperDataEngine,
      AttributeAssign attributeAssign, Group group,
      Map<MultiKey, SqlCacheDependency> allMshipHistoryAbacSqlCacheDependenciesMap,
      Set<Long> sqlCacheGroupInternalIdsStillNeedingMshipHistory, boolean readOnly) {
    Group theGroup = group;
    
    GcDbAccess gcDbAccess = new GcDbAccess();
    String script = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_JEXL_SCRIPT);
    GrouperJexlScriptAnalysis analyzeJexlScript = analyzeJexlScript(grouperDataEngine, script);

    
    String perGroupSourceIds = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_SUBJECT_SOURCE_IDS);
    Set<String> effectiveSourceIds = GrouperAbac.effectiveSubjectSourceIds(perGroupSourceIds);

    //System.out.println(script);
    
    
    GrouperJexlScriptSql grouperJexlScriptSql = generateJexlSql(grouperDataEngine, gcDbAccess, analyzeJexlScript); 
    
    if (theGroup != null) {

      MultiKey groupInternalIdFieldInternalId = new MultiKey(theGroup.getInternalId(), Group.getDefaultList().getInternalId());
      Map<MultiKey, SqlCacheGroup> groupInternalIdsFieldInternalIdToSqlCacheGroup = SqlCacheGroupDao.retrieveByGroupInternalIdsFieldInternalIds(GrouperUtil.toSet(groupInternalIdFieldInternalId));
      SqlCacheGroup sqlCacheGroup = groupInternalIdsFieldInternalIdToSqlCacheGroup.get(groupInternalIdFieldInternalId);
      List<SqlCacheDependency> sqlCacheDependencies = SqlCacheDependencyDao.retrieveAllByDependentId(sqlCacheGroup.getInternalId());
      if (sqlCacheGroup != null) {

        // sync up attribute dependencies on group
        {
          SqlCacheDependencyType sqlCacheDependencyTypeAbacAttribute = SqlCacheDependencyTypeDao.retrieveByName(SqlCacheDependencyTypeDao.NAME_ABAC_ATTRIBUTE);

          //  grouper_sql_cache_group
          //  group_internal_id
          //  field_internal_id
          Set<Long> attributeInternalIdsInDatabase = new HashSet<>();
          for (SqlCacheDependency sqlCacheDependency : sqlCacheDependencies) {
            if (GrouperUtil.equals(sqlCacheDependencyTypeAbacAttribute.getInternalId(), sqlCacheDependency.getDependencyTypeInternalId())) {
              if (!grouperJexlScriptSql.getAttributeInternalIds().contains(sqlCacheDependency.getOwnerInternalId())) {
                SqlCacheDependencyDao.delete(sqlCacheDependency);
              } else {
                attributeInternalIdsInDatabase.add(sqlCacheDependency.getOwnerInternalId());
              }
            }
          }
          
          for (Long attributeInternalId : grouperJexlScriptSql.getAttributeInternalIds()) {
            if (!attributeInternalIdsInDatabase.contains(attributeInternalId)) {
              SqlCacheDependency sqlCacheDependency = new SqlCacheDependency();
              sqlCacheDependency.setDependencyTypeInternalId(sqlCacheDependencyTypeAbacAttribute.getInternalId());
              sqlCacheDependency.setDependentInternalId(sqlCacheGroup.getInternalId());
              sqlCacheDependency.setOwnerInternalId(attributeInternalId);
              SqlCacheDependencyDao.store(sqlCacheDependency);
            }
          }
          
        }

        // sync up row dependencies on group
        {
          SqlCacheDependencyType sqlCacheDependencyTypeAbacRow = SqlCacheDependencyTypeDao.retrieveByName(SqlCacheDependencyTypeDao.NAME_ABAC_ROW);

          //  grouper_sql_cache_group
          //  group_internal_id
          //  field_internal_id
          Set<Long> rowInternalIdsInDatabase = new HashSet<>();
          for (SqlCacheDependency sqlCacheDependency : sqlCacheDependencies) {
            if (GrouperUtil.equals(sqlCacheDependencyTypeAbacRow.getInternalId(), sqlCacheDependency.getDependencyTypeInternalId())) {
              if (!grouperJexlScriptSql.getRowInternalIds().contains(sqlCacheDependency.getOwnerInternalId())) {
                SqlCacheDependencyDao.delete(sqlCacheDependency);
              } else {
                rowInternalIdsInDatabase.add(sqlCacheDependency.getOwnerInternalId());
              }
            }
          }
          
          for (Long rowInternalId : grouperJexlScriptSql.getRowInternalIds()) {
            if (!rowInternalIdsInDatabase.contains(rowInternalId)) {
              SqlCacheDependency sqlCacheDependency = new SqlCacheDependency();
              sqlCacheDependency.setDependencyTypeInternalId(sqlCacheDependencyTypeAbacRow.getInternalId());
              sqlCacheDependency.setDependentInternalId(sqlCacheGroup.getInternalId());
              sqlCacheDependency.setOwnerInternalId(rowInternalId);
              SqlCacheDependencyDao.store(sqlCacheDependency);
            }
          }
          
        }

        // sync up group dependencies on group
        {
          //  grouper_sql_cache_group
          //  group_internal_id
          //  field_internal_id
 
            SqlCacheDependencyType sqlCacheDependencyTypeAbacGroup = SqlCacheDependencyTypeDao.retrieveByName(SqlCacheDependencyTypeDao.NAME_ABAC_GROUP);

            Set<Long> groupInternalIdsInDatabase = new HashSet<>();
            for (SqlCacheDependency sqlCacheDependency : sqlCacheDependencies) {
              
              // TODO this should be in the Dao retrieve above
            if (GrouperUtil.equals(sqlCacheDependencyTypeAbacGroup.getInternalId(), sqlCacheDependency.getDependencyTypeInternalId())) {
              if (!grouperJexlScriptSql.getGroupInternalIds().contains(sqlCacheDependency.getOwnerInternalId())) {
                SqlCacheDependencyDao.delete(sqlCacheDependency);
              } else {
                groupInternalIdsInDatabase.add(sqlCacheDependency.getOwnerInternalId());
              }
            }
          }
          
          for (Long groupInternalId : grouperJexlScriptSql.getGroupInternalIds()) {
            if (!groupInternalIdsInDatabase.contains(groupInternalId)) {
              SqlCacheDependency sqlCacheDependency = new SqlCacheDependency();
              sqlCacheDependency.setDependencyTypeInternalId(sqlCacheDependencyTypeAbacGroup.getInternalId());
              sqlCacheDependency.setDependentInternalId(sqlCacheGroup.getInternalId());
              sqlCacheDependency.setOwnerInternalId(groupInternalId);
              SqlCacheDependencyDao.store(sqlCacheDependency);
            }
          }
        }
      }
    }
    
    // add mship history abac dependency before the query runs
    if (GrouperUtil.length(analyzeJexlScript.getRecentMemberOfGroupNames()) > 0) {
      Collection<SqlCacheGroup> sqlCacheGroupsToCheck = new HashSet<>();
      for (MultiKey groupNameFieldName : grouperJexlScriptSql.getAllSqlCacheGroupsForCurrentJexl().keySet()) {
        String groupName = (String)groupNameFieldName.getKey(0);
        String fieldName = (String)groupNameFieldName.getKey(1);
        
        if (!"members".equals(fieldName)) {
          throw new RuntimeException("Unexpected");
        }
        
        if (analyzeJexlScript.getRecentMemberOfGroupNames().contains(groupName)) {
          SqlCacheGroup sqlCacheGroup = grouperJexlScriptSql.getAllSqlCacheGroupsForCurrentJexl().get(groupNameFieldName);
          sqlCacheGroupsToCheck.add(sqlCacheGroup);
          sqlCacheGroupInternalIdsStillNeedingMshipHistory.add(sqlCacheGroup.getInternalId());
        }
      }
              
      SqlCacheDependencyType sqlCacheDependencyTypeMshipHistoryAbac = SqlCacheDependencyTypeDao.retrieveByName(SqlCacheDependencyTypeDao.NAME_MSHIP_HISTORY_ABAC);
      // go through and see which ones don't have the mshipHistory_abac dependency
      addMembershipHistoryAbacDependencies(sqlCacheDependencyTypeMshipHistoryAbac, sqlCacheGroupsToCheck, allMshipHistoryAbacSqlCacheDependenciesMap);                
    }
    
    MultiKey sourceInClause = GrouperAbac.subjectSourceInClause(effectiveSourceIds);
    String sourceInSql = (String)sourceInClause.getKey(0);
    List<String> sourceBindVars = (List<String>)sourceInClause.getKey(1);

    // prepend source bind vars before the existing ones
    List<Object> allBindVars = new ArrayList<Object>();
    allBindVars.addAll(sourceBindVars);
    if (gcDbAccess.getBindVars() != null) {
      allBindVars.addAll(gcDbAccess.getBindVars());
    }

    String sql = "select id from grouper_members gm where " + sourceInSql + " and gm.subject_resolution_deleted = 'F' and gm.subject_resolution_resolvable = 'T' and ( " + grouperJexlScriptSql.getWhereClause() + " )";

 //        System.out.println(script);
 //        System.out.println(sql);

    Set<String> memberIds = new HashSet<String>(gcDbAccess.bindVars(allBindVars).sql(sql).selectList(String.class));
    
    Set<String> previousMemberIds = new HashSet<String>(new GcDbAccess().sql("select member_id from grouper_memberships gm "
        + "where owner_group_id = ? and field_id = ? and mship_type = 'immediate'")
        .addBindVar(attributeAssign.getOwnerGroupId())
        .addBindVar(Group.getDefaultList().getId())
        .selectList(String.class));
    
    Set<String> insertMemberIds = new HashSet<>(memberIds);
    insertMemberIds.removeAll(previousMemberIds);
    
    Set<String> deleteMemberIds = new HashSet<>(previousMemberIds);
    deleteMemberIds.removeAll(memberIds);
    
    if (theGroup == null) {
      LOG.error("Error group not found '" + attributeAssign.getOwnerGroupId() + "'");
      GrouperUtil.mapAddValue(debugMap, "errorsGroupNull", 1);
      return;
    }
 
    Set<String> memberIdsToInsertOrDelete = new HashSet<String>(insertMemberIds);
    memberIdsToInsertOrDelete.addAll(deleteMemberIds);
    
    Set<Member> members = GrouperDAOFactory.getFactory().getMember().findByIds(memberIdsToInsertOrDelete, null);
    
    Map<String, Member> memberIdToUser = new HashMap<String, Member>();
    
    for (Member member : GrouperUtil.nonNull(members)) {
      memberIdToUser.put(member.getId(), member);
    }

    if (!readOnly) {
      for (String memberId : insertMemberIds) {
        try {
          Member member = memberIdToUser.get(memberId);
          theGroup.addMember(member.getSubject(), false);
        } catch (HookVeto hv) {
          GrouperUtil.mapAddValue(debugMap, "vetoesAddMember", 1);
          if (GrouperUtil.intValue(debugMap.get("vetoesAddMember"), 0) <= 20) {
            debugMap.put("vetoInsert_" + GrouperUtil.intValue(debugMap.get("vetoesAddMember"), 0), "group: " + theGroup.getName() + ", subjectId: " + memberId + ", " + hv.getMessage());
          }
          LOG.warn("Veto adding memberId '" + memberId + "' to group: '" + theGroup.getName() + "': " + hv.getMessage());
        } catch (RuntimeException re) {
          int errIndex = GrouperUtil.intValue(debugMap.get("errorsAddMember"), 0);
          GrouperUtil.mapAddValue(debugMap, "errorsAddMember", 1);
          if (errIndex < 20) {
            debugMap.put("errInsert_" + errIndex, "group: " + theGroup.getName() + ", subjectId: " + memberId + ", " + re.getMessage() + ", " + GrouperUtil.stack(re));
          }
          LOG.error("Error adding memberId '" + memberId + "' to group: '" + theGroup.getName() + "'", re);
        }
      }

      for (String memberId : deleteMemberIds) {
        try {
          Member member = memberIdToUser.get(memberId);
          theGroup.deleteMember(member.getSubject(), false);
        } catch (HookVeto hv) {
          GrouperUtil.mapAddValue(debugMap, "vetoesDeleteMember", 1);
          if (GrouperUtil.intValue(debugMap.get("vetoesDeleteMember"), 0) <= 20) {
            debugMap.put("vetoDelete_" + GrouperUtil.intValue(debugMap.get("vetoesDeleteMember"), 0), "group: " + theGroup.getName() + ", subjectId: " + memberId + ", " + hv.getMessage());
          }
          LOG.warn("Veto deleting memberId '" + memberId + "' from group: '" + theGroup.getName() + "': " + hv.getMessage());
        } catch (RuntimeException re) {
          int errIndex = GrouperUtil.intValue(debugMap.get("errorsDeleteMember"), 0);
          GrouperUtil.mapAddValue(debugMap, "errorsDeleteMember", 1);
          if (errIndex < 20) {
            debugMap.put("errDelete_" + errIndex, "group: " + theGroup.getName() + ", subjectId: " + memberId + ", " + re.getMessage() + ", " + GrouperUtil.stack(re));
          }
          LOG.error("Error deleting memberId '" + memberId + "' from group: '" + theGroup.getName() + "'", re);
        }
      }
    }
    
    GrouperUtil.mapAddValue(debugMap, "inserts", insertMemberIds.size());
    if (hib3GrouperLoaderLog != null) {
      hib3GrouperLoaderLog.addInsertCount(insertMemberIds.size());
    }
    GrouperUtil.mapAddValue(debugMap, "deletes", deleteMemberIds.size());
    if (hib3GrouperLoaderLog != null) {
      hib3GrouperLoaderLog.addDeleteCount(deleteMemberIds.size());
    }
    
    if (hib3GrouperLoaderLog != null) {
      hib3GrouperLoaderLog.addTotalCount(memberIds.size());
    }

    // add examples of inserts and deletes to the debug map (up to 20 each)
    {
      int maxExamples = 20;

      for (String memberId : insertMemberIds) {
        int index = GrouperUtil.intValue(debugMap.get("insertExamples_count"), 0);
        if (index >= maxExamples) {
          break;
        }
        Member member = memberIdToUser.get(memberId);
        String subjectId = member != null ? member.getSubjectId() : memberId;
        debugMap.put("insert_" + index, "group: " + theGroup.getName() + ", subjectId: " + subjectId);
        debugMap.put("insertExamples_count", index + 1);
      }

      for (String memberId : deleteMemberIds) {
        int index = GrouperUtil.intValue(debugMap.get("deleteExamples_count"), 0);
        if (index >= maxExamples) {
          break;
        }
        Member member = memberIdToUser.get(memberId);
        String subjectId = member != null ? member.getSubjectId() : memberId;
        debugMap.put("delete_" + index, "group: " + theGroup.getName() + ", subjectId: " + subjectId);
        debugMap.put("deleteExamples_count", index + 1);
      }
    }
  }

  public static GrouperJexlScriptSql generateJexlSql(GrouperDataEngine grouperDataEngine,
      GcDbAccess gcDbAccess, GrouperJexlScriptAnalysis analyzeJexlScript) {
    GrouperJexlScriptSql grouperJexlScriptSql = new GrouperJexlScriptSql();
    GrouperJexlScriptPart grouperJexlScriptPart = analyzeJexlScript.getGrouperJexlScriptParts().get(0);
    List<MultiKey> arguments = grouperJexlScriptPart.getArguments();
    grouperJexlScriptSql.setWhereClause(grouperJexlScriptPart.getWhereClause().toString());
 
    int argumentIndex = 0;
    
    String previousAttributeAlias = null;
            
    // TODO put this in the analysis script so all the bind vars are right
    for (MultiKey argument : arguments) {
      String argumentString = (String)argument.getKey(0);
      if (StringUtils.equals(argumentString, "group")) {
        String fieldName = (String)argument.getKey(1);
        if (!StringUtils.equals(fieldName, "members")) {
          throw new RuntimeException("Not expecting field: '" + fieldName + "'");
        }
        String groupName = (String)argument.getKey(2);
        //TODO make this more efficient
        Map<MultiKey, SqlCacheGroup> sqlCacheGroups = SqlCacheGroupDao.retrieveByGroupNamesFieldNames(GrouperUtil.toList(new MultiKey(groupName, fieldName)));
        grouperJexlScriptSql.getAllSqlCacheGroupsForCurrentJexl().putAll(sqlCacheGroups);
        // if group not found, consider it empty
        long sqlCacheGroupInternalId = -1;
        if (GrouperUtil.length(sqlCacheGroups) == 1) {
          sqlCacheGroupInternalId = sqlCacheGroups.values().iterator().next().getInternalId();
          grouperJexlScriptSql.getGroupInternalIds().add(sqlCacheGroupInternalId);
        }
        gcDbAccess.addBindVar(sqlCacheGroupInternalId);
      } else if (StringUtils.equals(argumentString, "attribute")) {
        String attributeAlias = (String)argument.getKey(1);
        GrouperDataFieldWrapper grouperDataFieldWrapper = grouperDataEngine.getGrouperDataProviderIndex().getFieldWrapperByLowerAlias().get(attributeAlias.toLowerCase());
        GrouperDataField grouperDataField = grouperDataFieldWrapper.getGrouperDataField();
        gcDbAccess.addBindVar(grouperDataField.getInternalId());
        grouperJexlScriptSql.getAttributeInternalIds().add(grouperDataField.getInternalId());
        previousAttributeAlias = attributeAlias;
      } else if (StringUtils.equals(argumentString, "row")) {
        String rowAlias = (String)argument.getKey(1);
        GrouperDataRowWrapper grouperDataRowWrapper = grouperDataEngine.getGrouperDataProviderIndex().getRowWrapperByLowerAlias().get(rowAlias.toLowerCase());
        GrouperDataRow grouperDataRow = grouperDataRowWrapper.getGrouperDataRow();
        gcDbAccess.addBindVar(grouperDataRow.getInternalId());
        grouperJexlScriptSql.getRowInternalIds().add(grouperDataRow.getInternalId());
      } else if (StringUtils.equals(argumentString, "bindVar")) {
        
        Object value = argument.getKey(1);
        gcDbAccess.addBindVar(value);
 
      } else if (StringUtils.equals(argumentString, "attributeValue")) {
        
        MultiKey argumentNameMultiKey = arguments.get(argumentIndex-1);
        
        String argumentPreviousString = (String)argumentNameMultiKey.getKey(0);
        boolean isAttribute = StringUtils.equals(argumentPreviousString, "attribute");
        GrouperDataFieldWrapper grouperDataFieldWrapper = grouperDataEngine.getGrouperDataProviderIndex().getFieldWrapperByLowerAlias().get(previousAttributeAlias.toLowerCase());
        GrouperDataField grouperDataField = grouperDataFieldWrapper.getGrouperDataField();
        
        GrouperDataFieldConfig grouperDataFieldConfig = grouperDataEngine.getFieldConfigByAlias().get(previousAttributeAlias.toLowerCase());
        GrouperDataFieldType fieldDataType = grouperDataFieldConfig.getFieldDataType();
        GrouperDataFieldAssign grouperDataFieldAssign = new GrouperDataFieldAssign();
        
        Object value = argument.getKey(1);
        fieldDataType.assignValue(grouperDataFieldAssign, value, grouperDataEngine.getGrouperDataProviderIndex().getDictionaryTextByString());
        
        if (fieldDataType == GrouperDataFieldType.bool || fieldDataType == GrouperDataFieldType.integer || fieldDataType == GrouperDataFieldType.timestamp) {
          if (grouperDataFieldAssign.getValueInteger() != null) {
            gcDbAccess.addBindVar(grouperDataFieldAssign.getValueInteger());
          }
          if (isAttribute) {                
            grouperJexlScriptSql.setWhereClause(StringUtils.replace(grouperJexlScriptSql.getWhereClause(), "$$ATTRIBUTE_COL_" + argumentIndex + "$$", "value_integer"));
          }
          
        } else if (fieldDataType == GrouperDataFieldType.string) {
          if (grouperDataFieldAssign.getValueDictionaryInternalId() != null) {
            gcDbAccess.addBindVar(grouperDataFieldAssign.getValueDictionaryInternalId());
          }
          if (isAttribute) {
            grouperJexlScriptSql.setWhereClause(StringUtils.replace(grouperJexlScriptSql.getWhereClause(), "$$ATTRIBUTE_COL_" + argumentIndex + "$$", "value_dictionary_internal_id"));
          }

        } else {
          throw new RuntimeException("not expecting type: " + fieldDataType.getClass().getName());
        }

      } else if (StringUtils.equals(argumentString, "attributeCompareLeft") || StringUtils.equals(argumentString, "attributeCompareRight")) {

        String fieldAlias = (String)argument.getKey(1);
        Long mathOffset = (Long)argument.getKey(2);

        GrouperDataFieldWrapper grouperDataFieldWrapper = grouperDataEngine.getGrouperDataProviderIndex().getFieldWrapperByLowerAlias().get(fieldAlias.toLowerCase());
        GrouperDataField grouperDataField = grouperDataFieldWrapper.getGrouperDataField();
        gcDbAccess.addBindVar(grouperDataField.getInternalId());
        grouperJexlScriptSql.getAttributeInternalIds().add(grouperDataField.getInternalId());

        if (mathOffset != null) {
          gcDbAccess.addBindVar(mathOffset);
        }

      } else {
        throw new RuntimeException("not expecting argument string: " + argumentString);
      }
      argumentIndex++;
    }
    return grouperJexlScriptSql;
  }

  /**
   * run standalone
   */
  public static void runDaemonStandalone() {
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
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
        return null;
      }
    });
  }

  /**
   * Builds the visualization reference tree from the annotated analysis parts.
   * Each part has a PartRole set during the analyzeJexlScriptToSqlHelper walk.
   * This avoids any duplicate JEXL AST parsing.
   *
   * The algorithm:
   * - Only process OR_CHILD and AND_CHILD parts; skip all others
   * - Buffer consecutive OR_CHILD parts
   * - On AND_CHILD: if OR buffer non-empty, create compound OR with buffered children; else create leaf
   * - At end: remaining OR_CHILD buffer becomes top-level leaves (pure OR expression)
   *
   * @param parts the annotated analysis parts
   * @return list of AbacReference objects forming the visualization tree
   */
  static List<AbacReference> buildVisualizationTreeFromParts(List<GrouperJexlScriptPart> parts) {
    List<AbacReference> result = new ArrayList<AbacReference>();
    List<AbacReference> orBuffer = new ArrayList<AbacReference>();

    for (int i = 0; i < parts.size(); i++) {
      GrouperJexlScriptPart part = parts.get(i);
      GrouperJexlScriptPart.PartRole role = part.getPartRole();

      if (role == GrouperJexlScriptPart.PartRole.OR_CHILD) {
        AbacReference orLeaf = createAbacReferenceFromPart(part, AbacReference.Connective.OR);
        if (orLeaf != null) {
          orBuffer.add(orLeaf);
        }
        continue;
      }

      if (role == GrouperJexlScriptPart.PartRole.AND_CHILD) {
        if (!orBuffer.isEmpty()) {
          // This AND_CHILD is the compound summary for the preceding OR children
          AbacReference compound = new AbacReference(AbacReference.Connective.OR, part.isNegated(), AbacReference.Connective.AND);
          for (AbacReference orChild : orBuffer) {
            compound.addChild(orChild);
          }
          compound.setPopulationCount(part.getPopulationCount());
          compound.setContainsSubject(part.isContainsSubject());
          String desc = part.getDisplayDescription().toString().trim();
          if (desc.length() > 0) {
            compound.setDisplayDescription(desc);
          }
          result.add(compound);
          orBuffer.clear();
        } else {
          AbacReference leaf = createAbacReferenceFromPart(part, AbacReference.Connective.AND);
          if (leaf != null) {
            result.add(leaf);
          }
        }
      }
    }

    // Remaining OR buffer = top-level pure OR expression
    if (!orBuffer.isEmpty()) {
      for (AbacReference orChild : orBuffer) {
        result.add(orChild);
      }
      orBuffer.clear();
    }

    // Single expression with no AND/OR: create a leaf from part 0
    if (result.isEmpty() && !parts.isEmpty()
        && parts.get(0).getPartRole() == GrouperJexlScriptPart.PartRole.FULL_EXPRESSION) {
      AbacReference leaf = createAbacReferenceFromPart(parts.get(0), AbacReference.Connective.AND);
      if (leaf != null) {
        result.add(leaf);
      }
    }

    // Handle outer NOT wrapping a compound: !(A && B) or !(A || B).
    // Part 0 (FULL_EXPRESSION) is negated but none of the individual children are,
    // meaning the NOT wraps the whole compound, not individual parts.
    // Wrap the children in a single negated compound node to preserve the correct semantics.
    if (result.size() > 1 && !parts.isEmpty()
        && parts.get(0).getPartRole() == GrouperJexlScriptPart.PartRole.FULL_EXPRESSION
        && parts.get(0).isNegated()) {
      boolean anyChildNegated = false;
      for (AbacReference ref : result) {
        if (ref.isNegated()) {
          anyChildNegated = true;
          break;
        }
      }
      if (!anyChildNegated) {
        // Determine compound type from the children's connective
        AbacReference.Connective compoundConnective = result.get(0).getConnective();
        // negated=true to show red minus edge, matching the convention for negated leaves like !entity.memberOfAny(...)
        AbacReference compound = new AbacReference(compoundConnective, true, AbacReference.Connective.AND);
        String desc = parts.get(0).getDisplayDescription().toString().trim();
        if (desc.length() > 0) {
          compound.setDisplayDescription(desc);
        }
        compound.setPopulationCount(parts.get(0).getPopulationCount());
        compound.setContainsSubject(parts.get(0).isContainsSubject());
        for (AbacReference child : result) {
          compound.addChild(child);
        }
        result.clear();
        result.add(compound);
      }
    }

    return result;
  }

  /**
   * Creates an AbacReference leaf from an analysis part by inspecting its arguments.
   *
   * @param part the analysis part
   * @param connective the connective context (AND or OR)
   * @return the AbacReference, or null if the part has no recognizable arguments
   */
  private static AbacReference createAbacReferenceFromPart(GrouperJexlScriptPart part, AbacReference.Connective connective) {
    List<MultiKey> arguments = part.getArguments();
    if (arguments == null || arguments.isEmpty()) {
      return null;
    }

    String firstArgType = (String) arguments.get(0).getKey(0);
    String desc = part.getDisplayDescription().toString().trim();

    if ("group".equals(firstArgType)) {
      // Count how many group arguments there are
      List<String> groupNames = new ArrayList<String>();
      for (MultiKey arg : arguments) {
        if ("group".equals(arg.getKey(0)) && "members".equals(arg.getKey(1))) {
          groupNames.add((String) arg.getKey(2));
        }
      }

      AbacReference ref;
      if (groupNames.size() > 1) {
        // memberOfAny: use first group name as the node name, value holds display description
        ref = new AbacReference(AbacReference.RefType.GROUP, groupNames.get(0),
            desc.length() > 0 ? desc : null, part.isNegated(), connective);
      } else if (groupNames.size() == 1) {
        ref = new AbacReference(AbacReference.RefType.GROUP, groupNames.get(0), null, part.isNegated(), connective);
      } else {
        return null;
      }
      ref.setPopulationCount(part.getPopulationCount());
      ref.setContainsSubject(part.isContainsSubject());
      if (desc.length() > 0) {
        ref.setDisplayDescription(desc);
      }
      return ref;
    } else if ("attribute".equals(firstArgType)) {
      String attributeAlias = (String) arguments.get(0).getKey(1);
      // Collect attribute values for display
      String value = null;
      for (MultiKey arg : arguments) {
        if ("attributeValue".equals(arg.getKey(0))) {
          value = String.valueOf(arg.getKey(1));
          break;
        }
      }
      AbacReference ref = new AbacReference(AbacReference.RefType.ATTRIBUTE, attributeAlias, value, part.isNegated(), connective);
      ref.setPopulationCount(part.getPopulationCount());
      ref.setContainsSubject(part.isContainsSubject());
      if (desc.length() > 0) {
        ref.setDisplayDescription(desc);
      }
      return ref;
    } else if ("row".equals(firstArgType)) {
      String rowAlias = (String) arguments.get(0).getKey(1);
      AbacReference ref = new AbacReference(AbacReference.RefType.ROW, rowAlias, null, part.isNegated(), connective);
      ref.setPopulationCount(part.getPopulationCount());
      ref.setContainsSubject(part.isContainsSubject());
      if (desc.length() > 0) {
        ref.setDisplayDescription(desc);
      }
      return ref;
    }

    return null;
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperLoaderJexlScriptFullSync.class);

}
