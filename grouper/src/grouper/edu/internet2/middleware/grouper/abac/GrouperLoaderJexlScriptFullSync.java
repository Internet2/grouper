package edu.internet2.middleware.grouper.abac;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.internal.Engine;
import org.apache.commons.jexl3.parser.ASTAndNode;
import org.apache.commons.jexl3.parser.ASTArguments;
import org.apache.commons.jexl3.parser.ASTArrayLiteral;
import org.apache.commons.jexl3.parser.ASTAssignment;
import org.apache.commons.jexl3.parser.ASTEQNode;
import org.apache.commons.jexl3.parser.ASTERNode;
import org.apache.commons.jexl3.parser.ASTFunctionNode;
import org.apache.commons.jexl3.parser.ASTIdentifier;
import org.apache.commons.jexl3.parser.ASTIdentifierAccess;
import org.apache.commons.jexl3.parser.ASTJexlScript;
import org.apache.commons.jexl3.parser.ASTMethodNode;
import org.apache.commons.jexl3.parser.ASTNotNode;
import org.apache.commons.jexl3.parser.ASTNullLiteral;
import org.apache.commons.jexl3.parser.ASTNumberLiteral;
import org.apache.commons.jexl3.parser.ASTOrNode;
import org.apache.commons.jexl3.parser.ASTReference;
import org.apache.commons.jexl3.parser.ASTReferenceExpression;
import org.apache.commons.jexl3.parser.ASTStringLiteral;
import org.apache.commons.jexl3.parser.JexlNode;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
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

  private static ThreadLocal<Set<String>> recentMemberOfGroupNames = new ThreadLocal<Set<String>>();
  
  public static void main(String[] args) {

    try {
      GrouperSession grouperSession = GrouperSession.startRootSession();
      
      GrouperDataEngine grouperDataEngine = new GrouperDataEngine();

      GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();

      grouperDataEngine.loadFieldsAndRows(grouperConfig);

      Subject subject = SubjectFinder.findById("test.subject.1", true);
      
      //System.out.println(analyzeJexlScriptHtml(grouperDataEngine, "entity.memberOf('penn:ref:mfaEnrolled')", subject, grouperSession.getSubject()));
      System.out.println(analyzeJexlScriptHtml(grouperDataEngine, "'penn:ref:mfaEnrolled' && 'penn:ref:mfaEnrolled2'", subject, grouperSession.getSubject(), false));

      
      
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
        List<Long> dependenciesFound = new GcDbAccess().sql("select gscdt.internal_id from grouper_sql_cache_dependency gscd, grouper_sql_cache_depend_type gscdt where gscd.dep_type_internal_id = gscdt.internal_id and gscdt.dependency_category='mshipHistory' and owner_internal_id = ?")
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
          SqlCacheHistoryFullSyncDaemon.syncMembershipHistory(sqlCacheGroup, null);
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
    
    Member member = subject != null ? MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, true): null;
    
    GrouperJexlScriptAnalysis grouperJexlScriptAnalysis;
    try {
      grouperJexlScriptAnalysis = analyzeJexlScript(grouperDataEngine, jexlScript);
      
      if (recentMemberOfGroupNames.get() != null && recentMemberOfGroupNames.get().size() > 0) {
        Set<MultiKey> groupNamesAndFieldNames = new HashSet<>();
        for (String groupName : recentMemberOfGroupNames.get()) {
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
    } finally {
      recentMemberOfGroupNames.remove();
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
          
          String highestLevelAccess = GrouperDataEngine.calculateHighestLevelAccess(grouperPrivacyRealmConfig, loggedInSubject);
           
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
        }
        argumentIndex++;
      }   
      String sql = "select count(1) from grouper_members gm where " + whereClause;
  
  //    System.out.println(script);
  //    System.out.println(sql);
      
      int count = gcDbAccess.sql(sql).select(Integer.class);
      grouperJexlScriptPart.setPopulationCount(count);

      if (partsHaveMissingGroup) {
        StringBuilder newDescription = new StringBuilder(grouperJexlScriptPart.getDisplayDescription());
        newDescription.append(GrouperTextContainer.textOrNull("jexlAnalysisMemberOfGroupMissingWarning"));
        grouperJexlScriptPart.setDisplayDescription(newDescription);
      }

      if (subject != null) {
        sql += " and gm.id = ?";
        count = gcDbAccess.sql(sql).addBindVar(member.getId()).select(Integer.class);
        grouperJexlScriptPart.setContainsSubject(count>0);
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
      analyzeJexlScriptToSqlHelper(grouperJexlScriptAnalysis, theGrouperJexlScriptPart, jexlNode.jjtGetChild(0), clonePart);
      if (clonePart) {
        grouperJexlScriptPartClone = new GrouperJexlScriptPart();
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
      if (recentMemberOfGroupNames.get() == null) {
        recentMemberOfGroupNames.set(new HashSet<String>());
      }
      recentMemberOfGroupNames.get().add(groupName);
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

        } else {
          throw new RuntimeException("Not expecting argument of type! " + jjtGetChild.getClass().getName());
        }
        
      }
      
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
        + " and gscm.member_internal_id = gm.internal_id and gm.subject_source != 'g:gsa') ");
    grouperJexlScriptPart.getArguments().add(new MultiKey("group", "members", groupName));
    grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisMemberOfGroup"))
      .append(" '").append(GrouperUtil.xmlEscape(groupName)).append("'");
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
        + " and gscmh.member_internal_id = gm.internal_id and gm.subject_source != 'g:gsa') ");
    grouperJexlScriptPart.getArguments().add(new MultiKey("group", "members", groupName));
    grouperJexlScriptPart.getArguments().add(new MultiKey("bindVar", (System.currentTimeMillis() * 1000L) - timePeriodMicros));
    
    grouperJexlScriptPart.getWhereClause().append("and not exists (select 1 from grouper_sql_cache_mship gscm where gscm.sql_cache_group_internal_id = ? "
        + " and gscm.member_internal_id = gm.internal_id and gm.subject_source != 'g:gsa') ");
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

      
      
    } else if ((jexlNode instanceof ASTEQNode || jexlNode instanceof ASTAssignment) && 2==jexlNode.jjtGetNumChildren() && jexlNode.jjtGetChild(1) instanceof ASTNullLiteral) {
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

    } else if ((jexlNode instanceof ASTEQNode || jexlNode instanceof ASTAssignment) && 2==jexlNode.jjtGetNumChildren()) {
      if (!(jexlNode.jjtGetChild(0) instanceof ASTIdentifier)) {
        throw new RuntimeException("Not expecting node type: " + jexlNode.jjtGetChild(0).getClass().getName() 
            + ", children: " + jexlNode.jjtGetChild(0).jjtGetNumChildren());
      }
      if (!(jexlNode.jjtGetChild(1) instanceof ASTIdentifier) && !(jexlNode.jjtGetChild(1) instanceof ASTNumberLiteral)
          && !(jexlNode.jjtGetChild(1) instanceof ASTStringLiteral) ) {
        throw new RuntimeException("Not expecting node type: " + jexlNode.jjtGetChild(1).getClass().getName() 
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
      } 
      
      grouperJexlScriptPart.getWhereClause().append("exists (select 1 from grouper_data_row_field_assign gdrfa where data_row_assign_internal_id = gdra.internal_id "
          + "and gdrfa.data_field_internal_id = ? and gdrfa.$$ATTRIBUTE_COL_" + (grouperJexlScriptPart.getArguments().size()+1) + "$$ = ?) ");
      grouperJexlScriptPart.getArguments().add(new MultiKey("attribute", leftPart.getName()));
      grouperJexlScriptPart.getArguments().add(new MultiKey("attributeValue", rightPartValue));
      
      grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisHasRowAttributeValue1"))
        .append(" '").append(GrouperUtil.xmlEscape(leftPart.getName())).append("' ").append(GrouperTextContainer.textOrNull("jexlAnalysisHasRowAttributeValue2")).append(" '")
        .append(GrouperUtil.xmlEscape(rightPartValue)).append("'");

    }  
    
    else if (jexlNode instanceof ASTERNode && 2==jexlNode.jjtGetNumChildren()) {
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
  


  private List<GrouperLoaderJexlScriptGroup> grouperLoaderJexlScriptGroups = null;
  
  /**
   * 
   */
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    
    Map<String, Object> debugMap = Collections.synchronizedMap(new LinkedHashMap<String, Object>());
    RuntimeException runtimeException = null;
    try {
      
      GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
      
      GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
      
      grouperDataEngine.loadFieldsAndRows(grouperConfig);

      // TODO cache this
      AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_MARKER, true);
  
      Collection<AttributeAssign> attributeAssigns = GrouperUtil.nonNull(new AttributeAssignFinder().addAttributeDefNameId(attributeDefName.getId()).findAttributeAssignFinderResults().getIdToAttributeAssignMap()).values();
      
      debugMap.put("jexlScriptGroups", GrouperUtil.length(attributeAssigns));

      List<SqlCacheDependencyType> sqlCacheDependencyTypes = SqlCacheDependencyTypeDao.retrieveByDependencyCategory("abac");
      Map<String, SqlCacheDependencyType> nameToSqlCacheDependencyType = new HashMap<String, SqlCacheDependencyType>();
      for (SqlCacheDependencyType sqlCacheDependencyType : sqlCacheDependencyTypes) {
        nameToSqlCacheDependencyType.put(sqlCacheDependencyType.getName(), sqlCacheDependencyType);
      }
      
      SqlCacheDependencyType sqlCacheDependencyTypeAbacAttribute = nameToSqlCacheDependencyType.get("abac_attribute");
      SqlCacheDependencyType sqlCacheDependencyTypeMshipHistoryAbac = SqlCacheDependencyTypeDao.retrieveByDependencyCategoryAndName("mshipHistory", "mshipHistory_abac");        
      List<SqlCacheDependency> allMshipHistoryAbacSqlCacheDependencies = SqlCacheDependencyDao.retrieveByDependencyTypeInternalId(sqlCacheDependencyTypeMshipHistoryAbac.getInternalId());
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
                
            String script = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_JEXL_SCRIPT);
    
            //  String includeInternalSourcesString = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_INCLUDE_INTERNAL_SOURCES);
            //  boolean includeInternalSources = GrouperUtil.booleanValue(includeInternalSourcesString, false);
            
            //System.out.println(script);
            
            try {
              GrouperJexlScriptAnalysis analyzeJexlScript = analyzeJexlScript(grouperDataEngine, script);
              GrouperJexlScriptPart grouperJexlScriptPart = analyzeJexlScript.getGrouperJexlScriptParts().get(0);
              List<MultiKey> arguments = grouperJexlScriptPart.getArguments();
              String whereClause = grouperJexlScriptPart.getWhereClause().toString();
      
              GcDbAccess gcDbAccess = new GcDbAccess();
              int argumentIndex = 0;
              
              String previousAttributeAlias = null;
              
              Set<Long> attributeInternalIds = new HashSet<Long>();
              
              Map<MultiKey, SqlCacheGroup> allSqlCacheGroupsForCurrentJexl = new HashMap<>();
                      
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
                  allSqlCacheGroupsForCurrentJexl.putAll(sqlCacheGroups);
                  // if group not found, consider it empty
                  long sqlCacheGroupInternalId = -1;
                  if (GrouperUtil.length(sqlCacheGroups) == 1) {
                    sqlCacheGroupInternalId = sqlCacheGroups.values().iterator().next().getInternalId();
      
                  }
                  gcDbAccess.addBindVar(sqlCacheGroupInternalId);
                } else if (StringUtils.equals(argumentString, "attribute")) {
                  String attributeAlias = (String)argument.getKey(1);
                  GrouperDataFieldWrapper grouperDataFieldWrapper = grouperDataEngine.getGrouperDataProviderIndex().getFieldWrapperByLowerAlias().get(attributeAlias.toLowerCase());
                  GrouperDataField grouperDataField = grouperDataFieldWrapper.getGrouperDataField();
                  gcDbAccess.addBindVar(grouperDataField.getInternalId());
                  attributeInternalIds.add(grouperDataField.getInternalId());
                  previousAttributeAlias = attributeAlias;
                } else if (StringUtils.equals(argumentString, "row")) {
                  String rowAlias = (String)argument.getKey(1);
                  GrouperDataRowWrapper grouperDataRowWrapper = grouperDataEngine.getGrouperDataProviderIndex().getRowWrapperByLowerAlias().get(rowAlias.toLowerCase());
                  GrouperDataRow grouperDataRow = grouperDataRowWrapper.getGrouperDataRow();
                  gcDbAccess.addBindVar(grouperDataRow.getInternalId());
      
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
      
                } else {
                  throw new RuntimeException("not expecting argument string: " + argumentString);
                }
                argumentIndex++;
              } 
              
              String ownerGroupId = attributeAssign.getOwnerGroupId();
              
              Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), ownerGroupId, false);
      
              if (group != null) {
      
                //  grouper_sql_cache_group
                //  group_internal_id
                //  field_internal_id
                MultiKey groupInternalIdFieldInternalId = new MultiKey(group.getInternalId(), Group.getDefaultList().getInternalId());
                Map<MultiKey, SqlCacheGroup> groupInternalIdsFieldInternalIdToSqlCacheGroup = SqlCacheGroupDao.retrieveByGroupInternalIdsFieldInternalIds(GrouperUtil.toSet(groupInternalIdFieldInternalId));
                SqlCacheGroup sqlCacheGroup = groupInternalIdsFieldInternalIdToSqlCacheGroup.get(groupInternalIdFieldInternalId);
                if (sqlCacheGroup != null) {
                  List<SqlCacheDependency> sqlCacheDependencies = SqlCacheDependencyDao.retrieveAllByDependentId(sqlCacheGroup.getInternalId());
                  Set<Long> attributeInternalIdsInDatabase = new HashSet<>();
                  for (SqlCacheDependency sqlCacheDependency : sqlCacheDependencies) {
                    if (GrouperUtil.equals(sqlCacheDependencyTypeAbacAttribute.getInternalId(), sqlCacheDependency.getDependencyTypeInternalId())) {
                      if (!attributeInternalIds.contains(sqlCacheDependency.getOwnerInternalId())) {
                        SqlCacheDependencyDao.delete(sqlCacheDependency);
                      } else {
                        attributeInternalIdsInDatabase.add(sqlCacheDependency.getOwnerInternalId());
                      }
                    }
                  }
                  
                  for (Long attributeInternalId : attributeInternalIds) {
                    if (!attributeInternalIdsInDatabase.contains(attributeInternalId)) {
                      SqlCacheDependency sqlCacheDependency = new SqlCacheDependency();
                      sqlCacheDependency.setDependencyTypeInternalId(sqlCacheDependencyTypeAbacAttribute.getInternalId());
                      sqlCacheDependency.setDependentInternalId(sqlCacheGroup.getInternalId());
                      sqlCacheDependency.setOwnerInternalId(attributeInternalId);
                      SqlCacheDependencyDao.store(sqlCacheDependency);
                    }
                  }
                  
                }
                
              }
              
              // add mship history abac dependency before the query runs
              if (recentMemberOfGroupNames.get() != null && recentMemberOfGroupNames.get().size() > 0) {
                Collection<SqlCacheGroup> sqlCacheGroupsToCheck = new HashSet<>();
                for (MultiKey groupNameFieldName : allSqlCacheGroupsForCurrentJexl.keySet()) {
                  String groupName = (String)groupNameFieldName.getKey(0);
                  String fieldName = (String)groupNameFieldName.getKey(1);
                  
                  if (!"members".equals(fieldName)) {
                    throw new RuntimeException("Unexpected");
                  }
                  
                  if (recentMemberOfGroupNames.get().contains(groupName)) {
                    SqlCacheGroup sqlCacheGroup = allSqlCacheGroupsForCurrentJexl.get(groupNameFieldName);
                    sqlCacheGroupsToCheck.add(sqlCacheGroup);
                    sqlCacheGroupInternalIdsStillNeedingMshipHistory.add(sqlCacheGroup.getInternalId());
                  }
                }
                        
                // go through and see which ones don't have the mshipHistory_abac dependency
                addMembershipHistoryAbacDependencies(sqlCacheDependencyTypeMshipHistoryAbac, sqlCacheGroupsToCheck, allMshipHistoryAbacSqlCacheDependenciesMap);                
              }
              
              String sql = "select id from grouper_members gm where " + whereClause;
      
      //        System.out.println(script);
      //        System.out.println(sql);
      
              Set<String> memberIds = new HashSet<String>(gcDbAccess.sql(sql).selectList(String.class));
              
              Set<String> previousMemberIds = new HashSet<String>(new GcDbAccess().sql("select member_id from grouper_memberships gm "
                  + "where owner_group_id = ? and field_id = ? and mship_type = 'immediate'")
                  .addBindVar(attributeAssign.getOwnerGroupId())
                  .addBindVar(Group.getDefaultList().getId())
                  .selectList(String.class));
              
              Set<String> insertMemberIds = new HashSet<>(memberIds);
              insertMemberIds.removeAll(previousMemberIds);
              
              Set<String> deleteMemberIds = new HashSet<>(previousMemberIds);
              deleteMemberIds.removeAll(memberIds);
              
              if (group == null) {
                LOG.error("Error group not found '" + ownerGroupId + "'");
                GrouperUtil.mapAddValue(debugMap, "errorsGroupNull", 1);
                return null;
              }
      
              for (String memberId : insertMemberIds) {
                try {
                  group.addMember(MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), memberId, true).getSubject(), false);
                } catch (RuntimeException re) {
                  GrouperUtil.mapAddValue(debugMap, "errorsAddMember", 1);
                  debugMap.put("exceptionAddGroupName", group.getName());
                  debugMap.put("exceptionAddMemberId", memberId);
                  debugMap.put("exceptionAddMember", GrouperUtil.getFullStackTrace(re));
                  LOG.error("Error adding memberId '" + memberId + "' to group: '" + group.getName() + "'", re);
                }
              }
      
              for (String memberId : deleteMemberIds) {
                try {
                  group.deleteMember(MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), memberId, true).getSubject(), false);
                } catch (RuntimeException re) {
                  GrouperUtil.mapAddValue(debugMap, "errorsDeleteMember", 1);
                  debugMap.put("exceptionDeleteGroupName", group.getName());
                  debugMap.put("exceptionDeleteMemberId", memberId);
                  debugMap.put("exceptionDeleteMember", GrouperUtil.getFullStackTrace(re));
                  LOG.error("Error deleting memberId '" + memberId + "' from group: '" + group.getName() + "'", re);
                }
              }
              
              GrouperUtil.mapAddValue(debugMap, "inserts", insertMemberIds.size());
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(insertMemberIds.size());
              GrouperUtil.mapAddValue(debugMap, "deletes", deleteMemberIds.size());
              otherJobInput.getHib3GrouperLoaderLog().addDeleteCount(deleteMemberIds.size());
            } finally {
              recentMemberOfGroupNames.remove();
            }
            
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
      otherJobInput.getHib3GrouperLoaderLog().setJobMessage(GrouperUtil.mapToString(debugMap));
    }
    
    if (runtimeException != null) {
      throw runtimeException;
    }

    if (GrouperUtil.intValue(debugMap.get("errors"), 0) > 0) {
      throw new RuntimeException("Had " + debugMap.get("errors") + " errors, check logs");
    }
    return null;
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

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperLoaderJexlScriptFullSync.class);

}
