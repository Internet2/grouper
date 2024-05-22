package edu.internet2.middleware.grouper.abac;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.internal.Engine;
import org.apache.commons.jexl3.parser.ASTAndNode;
import org.apache.commons.jexl3.parser.ASTArguments;
import org.apache.commons.jexl3.parser.ASTArrayLiteral;
import org.apache.commons.jexl3.parser.ASTEQNode;
import org.apache.commons.jexl3.parser.ASTERNode;
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
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperShutdown;
import edu.internet2.middleware.grouper.plugins.GrouperPluginManager;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheGroup;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheGroupDao;
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

  public static void main(String[] args) {

    try {
      GrouperSession.startRootSession();
      
      Subject subject = SubjectFinder.findById("test.subject.1", true);
      
      //System.out.println(analyzeJexlScriptHtml("entity.memberOf('test:testGroup')", subject));
      //System.out.println(analyzeJexlScriptHtml("entity.memberOf('test:testGroup') && entity.memberOf('test:testGroup2')", subject));
      //System.out.println(analyzeJexlScriptHtml("entity.memberOf('test:testGroup') && !entity.memberOf('test:testGroup2')", subject));
      //System.out.println(analyzeJexlScriptHtml("entity.memberOf('test:testGroup') && entity.memberOf('test:testGroup2')", subject));
      //System.out.println(analyzeJexlScriptHtml("entity.hasRow('affiliation', 'name==staff && dept==english')", subject));
      //System.out.println(analyzeJexlScriptHtml("entity.memberOf('test:testGroup') || (entity.memberOf('test:testGroup2') && entity.memberOf('test:testGroup3'))", subject));
      //System.out.println(analyzeJexlScriptHtml("entity.hasRow('affiliation', \"affiliationCode=='staff' && affiliationOrg==1234\") "
      //    + "|| (entity.memberOf('test:testGroup') && !entity.memberOf('test:testGroup2'))", subject));
      
      
      //System.out.println(analyzeJexlScriptHtml("entity.hasRow('cp_user', \"cp_active && !cp_blocked && cp_known && cp_org == 'Perelman School of Medicine' \") "
      //    + "&& entity.memberOf('penn:ref:member') && !entity.memberOf('penn:ref:lockout') && entity.hasAttribute('cp_role', 'desktop-user')", subject));

      System.out.println(analyzeJexlScriptHtml("(entity.hasRow('cp_user', \"(cp_active || !cp_blocked) && cp_known "
          + "&& cp_org == 'Perelman School of Medicine' \") "
          + " || entity.hasRow('cp_user', \"(cp_active || !cp_blocked) && cp_known "
          + "&& cp_org == 'Perelman School of Medicine' \")) && (!entity.memberOf('penn:ref:member') "
          + "|| entity.memberOf('penn:ref:lockout') ) && entity.hasAttribute('cp_role', 'desktop-user')", null, subject));
      
      //System.out.println(GrouperUtil.toStringForLog(analyzeJexlScript("entity.memberOf('test:testGroup')")));
      //System.out.println(GrouperUtil.toStringForLog(analyzeJexlScript("entity.memberOf('test:testGroup') && entity.memberOf('test:testGroup2')")));
      //System.out.println(GrouperUtil.toStringForLog(analyzeJexlScript("entity.memberOf('test:testGroup') && !entity.memberOf('test:testGroup2')")));
      //System.out.println(GrouperUtil.toStringForLog(analyzeJexlScript("entity.memberOf('test:testGroup') || (entity.memberOf('test:testGroup2') && entity.memberOf('test:testGroup3'))")));
      //System.out.println(GrouperUtil.toStringForLog(analyzeJexlScript("entity.hasAttribute('active')")));
      //System.out.println(GrouperUtil.toStringForLog(analyzeJexlScript("entity.hasAttribute('active', 'true')")));
      //System.out.println(GrouperUtil.toStringForLog(analyzeJexlScript("entity.hasRow('affiliation', 'name==staff && dept==english')")));
      
      
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
    } finally {
      GrouperLoader.shutdownIfStarted();
      GrouperPluginManager.shutdownIfStarted();
      GrouperShutdown.shutdown();
    }
  }
  
  public static GrouperJexlScriptAnalysis analyzeJexlScriptHtml(String jexlScript, Subject subject, Subject loggedInSubject) {
    
    Member member = subject != null ? MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, true): null;
    
    GrouperJexlScriptAnalysis grouperJexlScriptAnalysis = analyzeJexlScript(jexlScript);
    
    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    
    grouperDataEngine.loadFieldsAndRows(grouperConfig);

    for (GrouperJexlScriptPart grouperJexlScriptPart : grouperJexlScriptAnalysis.getGrouperJexlScriptParts()) {
      
      GcDbAccess gcDbAccess = new GcDbAccess();
      String whereClause = grouperJexlScriptPart.getWhereClause().toString();
      int argumentIndex = 0;
      
      String previousAttributeAlias = null;
      
      for (MultiKey argument : grouperJexlScriptPart.getArguments()) {
        String argumentString = (String)argument.getKey(0);
        if (StringUtils.equals(argumentString, "group")) {
          String fieldName = (String)argument.getKey(1);
          if (!StringUtils.equals(fieldName, "members")) {
            throw new RuntimeException("Not expecting field: '" + fieldName + "'");
          }
          String groupName = (String)argument.getKey(2);
          //TODO make this more efficient
          SqlCacheGroup sqlCacheGroup = SqlCacheGroupDao.retrieveByGroupNamesFieldNames(GrouperUtil.toList(new MultiKey(groupName, fieldName))).values().iterator().next();
          gcDbAccess.addBindVar(sqlCacheGroup.getInternalId());
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
          fieldDataType.assignValue(grouperDataFieldAssign, value);
          
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
  
        }
        argumentIndex++;
      }   
      String sql = "select count(1) from grouper_members gm where " + whereClause;
  
  //    System.out.println(script);
  //    System.out.println(sql);
      
      int count = gcDbAccess.sql(sql).select(Integer.class);
      grouperJexlScriptPart.setPopulationCount(count);
      
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
  public static GrouperJexlScriptAnalysis analyzeJexlScript(String jexlStript) {

    jexlStript = jexlStript.trim();
    if (jexlStript.startsWith("${") && jexlStript.endsWith("}")) {
      jexlStript = jexlStript.substring(2, jexlStript.length()-1);
    }
    
    JexlEngine jexlEngine = new Engine();
    
    jexlStript = GrouperUtil.replace(jexlStript, "\n", " ");
    jexlStript = GrouperUtil.replace(jexlStript, "\r", " ");
    jexlStript = GrouperUtil.replace(jexlStript, "! ", " ");
    
    JexlExpression expression = (JexlExpression)jexlEngine.createExpression(jexlStript);

    ASTJexlScript astJexlScript = (ASTJexlScript)GrouperUtil.fieldValue(expression, "script");

    GrouperJexlScriptAnalysis grouperJexlScriptAnalysis = new GrouperJexlScriptAnalysis();
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
      grouperJexlScriptPart.getWhereClause().append("exists (select 1 from grouper_sql_cache_mship gscm where gscm.sql_cache_group_internal_id = ? and gscm.member_internal_id = gm.internal_id) ");
      grouperJexlScriptPart.getArguments().add(new MultiKey("group", "members", groupName));
      grouperJexlScriptPart.getDisplayDescription().append(GrouperTextContainer.textOrNull("jexlAnalysisMemberOfGroup"))
        .append(" '").append(GrouperUtil.xmlEscape(groupName)).append("'");
    } else if (StringUtils.equals("hasAttributeAny", astIdentifierAccess.getName())) {
      
      ASTArguments astArguments = (ASTArguments)astMethodNode.jjtGetChild(1);
      if (astArguments.jjtGetNumChildren() != 2) {
        throw new RuntimeException("Not expecting method with this many arguments! " + astArguments.jjtGetNumChildren());
      }
      if (!(astArguments.jjtGetChild(0) instanceof ASTStringLiteral)) {
        throw new RuntimeException("Not expecting argument of type! " + astArguments.jjtGetChild(0).getClass().getName());
      }
      if (!(astArguments.jjtGetChild(1) instanceof ASTArrayLiteral)) {
        throw new RuntimeException("Not expecting argument of type! " + astArguments.jjtGetChild(1).getClass().getName());
      }
      ASTStringLiteral astStringLiteral = (ASTStringLiteral)astArguments.jjtGetChild(0);
      String attributeAlias = astStringLiteral.getLiteral();
      
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
      if (!(astArguments.jjtGetChild(0) instanceof ASTStringLiteral)) {
        throw new RuntimeException("Not expecting argument of type! " + astArguments.jjtGetChild(0).getClass().getName());
      }
      ASTStringLiteral astStringLiteral = (ASTStringLiteral)astArguments.jjtGetChild(0);
      String attributeAlias = astStringLiteral.getLiteral();
      if (astArguments.jjtGetNumChildren() == 1) {

        grouperJexlScriptPart.getWhereClause().append("exists (select 1 from grouper_data_field_assign gdfa where gdfa.data_field_internal_id = ? and gdfa.member_internal_id = gm.internal_id and gdfa.value_integer = 1) ");
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
    } else if (StringUtils.equals("hasRow", astIdentifierAccess.getName())) {
      ASTArguments astArguments = (ASTArguments)astMethodNode.jjtGetChild(1);
      if (astArguments.jjtGetNumChildren() != 2) {
        throw new RuntimeException("Not expecting method with this many arguments! " + astArguments.jjtGetNumChildren());
      }
      if (!(astArguments.jjtGetChild(0) instanceof ASTStringLiteral)) {
        throw new RuntimeException("Not expecting argument of type! " + astArguments.jjtGetChild(0).getClass().getName());
      }
      if (!(astArguments.jjtGetChild(1) instanceof ASTStringLiteral)) {
        throw new RuntimeException("Not expecting argument of type! " + astArguments.jjtGetChild(1).getClass().getName());
      }
      ASTStringLiteral astStringLiteral = (ASTStringLiteral)astArguments.jjtGetChild(0);
      ASTStringLiteral scriptLiteral = (ASTStringLiteral)astArguments.jjtGetChild(1);
      String rowAlias = astStringLiteral.getLiteral();
 
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
      
    } else if (jexlNode instanceof ASTEQNode && 2==jexlNode.jjtGetNumChildren() && jexlNode.jjtGetChild(1) instanceof ASTNullLiteral) {
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

    } else if (jexlNode instanceof ASTEQNode && 2==jexlNode.jjtGetNumChildren()) {
      if (!(jexlNode.jjtGetChild(0) instanceof ASTIdentifier)) {
        throw new RuntimeException("Not expecting node type: " + jexlNode.jjtGetChild(0).getClass().getName() 
            + ", children: " + jexlNode.jjtGetChild(0).jjtGetNumChildren());
      }
      if ((jexlNode instanceof ASTEQNode) && !(jexlNode.jjtGetChild(1) instanceof ASTIdentifier) && !(jexlNode.jjtGetChild(1) instanceof ASTNumberLiteral)
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
  


  /**
   * 
   * @param jexlStript
   * @param arguments first one is type (e.g. group), second is list (e.g. members), third is name (e.g. test:testGroup).  Used for bind variables
   * @return the sql
   */
  public static String convertJexlScriptToSqlWhereClause(String jexlStript, List<MultiKey> arguments) {

    jexlStript = jexlStript.trim();
    if (jexlStript.startsWith("${") && jexlStript.endsWith("}")) {
      jexlStript = jexlStript.substring(2, jexlStript.length()-1);
    }
    
    JexlEngine jexlEngine = new Engine();
    
    jexlStript = GrouperUtil.replace(jexlStript, "\n", " ");
    jexlStript = GrouperUtil.replace(jexlStript, "\r", " ");
    jexlStript = GrouperUtil.replace(jexlStript, "! ", " ");

    JexlExpression expression = (JexlExpression)jexlEngine.createExpression(jexlStript);

    ASTJexlScript astJexlScript = (ASTJexlScript)GrouperUtil.fieldValue(expression, "script");

    StringBuilder result = new StringBuilder();
    convertJexlScriptToSqlHelper(result, astJexlScript, arguments);
    return result.toString();
  }

  /**
   * has two children
   * @param result
   * @param astReference
   */
  public static void convertJexlReferenceTwoChildrenToSqlHelper(StringBuilder result, ASTReference astReference, List<MultiKey> arguments) {
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
      result.append("exists (select 1 from grouper_sql_cache_mship gscm where gscm.sql_cache_group_internal_id = ? and gscm.member_internal_id = gm.internal_id) ");
      arguments.add(new MultiKey("group", "members", groupName));
    } else if (StringUtils.equals("hasAttribute", astIdentifierAccess.getName())) {
      ASTArguments astArguments = (ASTArguments)astMethodNode.jjtGetChild(1);
      if (astArguments.jjtGetNumChildren() != 1 && astArguments.jjtGetNumChildren() != 2) {
        throw new RuntimeException("Not expecting method with this many arguments! " + astArguments.jjtGetNumChildren());
      }
      if (!(astArguments.jjtGetChild(0) instanceof ASTStringLiteral)) {
        throw new RuntimeException("Not expecting argument of type! " + astArguments.jjtGetChild(0).getClass().getName());
      }
      ASTStringLiteral astStringLiteral = (ASTStringLiteral)astArguments.jjtGetChild(0);
      String attributeAlias = astStringLiteral.getLiteral();
      if (astArguments.jjtGetNumChildren() == 1) {
        result.append("exists (select 1 from grouper_data_field_assign gdfa where gdfa.data_field_internal_id = ? and gdfa.member_internal_id = gm.internal_id and gdfa.value_integer = 1) ");
        arguments.add(new MultiKey("attribute", attributeAlias));
      } else if (astArguments.jjtGetNumChildren() == 2) {
        if (astArguments.jjtGetChild(1) instanceof ASTNullLiteral) {
          result.append("exists (select 1 from grouper_data_field_assign gdfa where gdfa.data_field_internal_id = ? "
              + "and gdfa.member_internal_id = gm.internal_id and gdfa.$$ATTRIBUTE_COL_" + (arguments.size()+1) + "$$ is null) ");
          arguments.add(new MultiKey("attribute", attributeAlias));
          arguments.add(new MultiKey("attributeValue", Void.TYPE));
          
        } else {
          result.append("exists (select 1 from grouper_data_field_assign gdfa where gdfa.data_field_internal_id = ? "
              + "and gdfa.member_internal_id = gm.internal_id and gdfa.$$ATTRIBUTE_COL_" + (arguments.size()+1) + "$$ = ?) ");
          arguments.add(new MultiKey("attribute", attributeAlias));
          if (astArguments.jjtGetChild(1) instanceof ASTStringLiteral) {
            arguments.add(new MultiKey("attributeValue", ((ASTStringLiteral)astArguments.jjtGetChild(1)).getLiteral()));
          } else if (astArguments.jjtGetChild(1) instanceof ASTNumberLiteral) {
            arguments.add(new MultiKey("attributeValue", ((ASTNumberLiteral)astArguments.jjtGetChild(1)).getLiteral()));
          } else {
            throw new RuntimeException("Not expecting argument of type! " + astArguments.jjtGetChild(1).getClass().getName());
          }
        }
      }
    } else if (StringUtils.equals("hasRow", astIdentifierAccess.getName())) {
      ASTArguments astArguments = (ASTArguments)astMethodNode.jjtGetChild(1);
      if (astArguments.jjtGetNumChildren() != 2) {
        throw new RuntimeException("Not expecting method with this many arguments! " + astArguments.jjtGetNumChildren());
      }
      if (!(astArguments.jjtGetChild(0) instanceof ASTStringLiteral)) {
        throw new RuntimeException("Not expecting argument of type! " + astArguments.jjtGetChild(0).getClass().getName());
      }
      if (!(astArguments.jjtGetChild(1) instanceof ASTStringLiteral)) {
        throw new RuntimeException("Not expecting argument of type! " + astArguments.jjtGetChild(1).getClass().getName());
      }
      ASTStringLiteral astStringLiteral = (ASTStringLiteral)astArguments.jjtGetChild(0);
      ASTStringLiteral scriptLiteral = (ASTStringLiteral)astArguments.jjtGetChild(1);
      String rowAlias = astStringLiteral.getLiteral();
      
      result.append("exists (select 1 from grouper_data_row_assign gdra where gdra.data_row_internal_id = ? and gdra.member_internal_id = gm.internal_id and ");
      arguments.add(new MultiKey("row", rowAlias));

      convertJexlRowToSqlHelper(result, scriptLiteral.getLiteral(), arguments);

      result.append(") ");
    } else {
      throw new RuntimeException("Not expecting method name: '" + astIdentifierAccess.getName() + "'");
    }
  }

  /**
   * 
   * @param jexlStript
   * @param arguments first one is type (e.g. group), second is list (e.g. members), third is name (e.g. test:testGroup)
   * @return the sql
   */
  public static void convertJexlRowToSqlHelper(StringBuilder result, String jexlStript, List<MultiKey> arguments) {

    jexlStript = jexlStript.trim();
    if (jexlStript.startsWith("${") && jexlStript.endsWith("}")) {
      jexlStript = jexlStript.substring(2, jexlStript.length()-1);
    }
    
    JexlEngine jexlEngine = new Engine();
    
    JexlExpression expression = (JexlExpression)jexlEngine.createExpression(jexlStript);

    ASTJexlScript astJexlScript = (ASTJexlScript)GrouperUtil.fieldValue(expression, "script");

    convertJexlRowToSqlHelper(result, astJexlScript, arguments);
  }


  public static void convertJexlRowToSqlHelper(StringBuilder result, JexlNode jexlNode, List<MultiKey> arguments) {
    
    if (jexlNode instanceof ASTIdentifier && 0==jexlNode.jjtGetNumChildren()) {
      
      result.append("exists (select 1 from grouper_data_row_field_assign gdrfa where data_row_assign_internal_id = gdra.internal_id "
          + "and gdrfa.data_field_internal_id = ? and gdrfa.value_integer = ?) ");
      arguments.add(new MultiKey("attribute", ((ASTIdentifier)jexlNode).getName()));
      arguments.add(new MultiKey("attributeValue", true));
    } else if (jexlNode instanceof ASTEQNode && 2==jexlNode.jjtGetNumChildren() && jexlNode.jjtGetChild(1) instanceof ASTNullLiteral) {
      if (!(jexlNode.jjtGetChild(0) instanceof ASTIdentifier)) {
        throw new RuntimeException("Not expecting node type: " + jexlNode.jjtGetChild(0).getClass().getName() 
            + ", children: " + jexlNode.jjtGetChild(0).jjtGetNumChildren());
      }
      ASTIdentifier leftPart = (ASTIdentifier)jexlNode.jjtGetChild(0);
      
      result.append("exists (select 1 from grouper_data_row_field_assign gdrfa where data_row_assign_internal_id = gdra.internal_id "
          + "and gdrfa.data_field_internal_id = ? and gdrfa.$$ATTRIBUTE_COL_" + (arguments.size()+1) + "$$ is null) ");
      arguments.add(new MultiKey("attribute", leftPart.getName()));
      arguments.add(new MultiKey("attributeValue", Void.TYPE));
      
    } else if (jexlNode instanceof ASTEQNode && 2==jexlNode.jjtGetNumChildren()) {
      if (!(jexlNode.jjtGetChild(0) instanceof ASTIdentifier)) {
        throw new RuntimeException("Not expecting node type: " + jexlNode.jjtGetChild(0).getClass().getName() 
            + ", children: " + jexlNode.jjtGetChild(0).jjtGetNumChildren());
      }
      if (!(jexlNode.jjtGetChild(1) instanceof ASTIdentifier)) {
        throw new RuntimeException("Not expecting node type: " + jexlNode.jjtGetChild(1).getClass().getName() 
            + ", children: " + jexlNode.jjtGetChild(1).jjtGetNumChildren());
      }
      ASTIdentifier leftPart = (ASTIdentifier)jexlNode.jjtGetChild(0);
      ASTIdentifier rightPart = (ASTIdentifier)jexlNode.jjtGetChild(1);
      
      result.append("exists (select 1 from grouper_data_row_field_assign gdrfa where data_row_assign_internal_id = gdra.internal_id "
          + "and gdrfa.data_field_internal_id = ? and gdrfa.$$ATTRIBUTE_COL_" + (arguments.size()+1) + "$$ = ?) ");
      arguments.add(new MultiKey("attribute", leftPart.getName()));
      arguments.add(new MultiKey("attributeValue", rightPart.getName()));
      
    } else if (jexlNode instanceof ASTJexlScript && 1==jexlNode.jjtGetNumChildren()) {
      convertJexlRowToSqlHelper(result, jexlNode.jjtGetChild(0), arguments);
    } else if (jexlNode instanceof ASTReferenceExpression && 1==jexlNode.jjtGetNumChildren()) {
      result.append("(");
      convertJexlRowToSqlHelper(result, jexlNode.jjtGetChild(0), arguments);
      result.append(")");
    } else if (jexlNode instanceof ASTNotNode && 1==jexlNode.jjtGetNumChildren()) {
      result.append("not ");
      convertJexlRowToSqlHelper(result, jexlNode.jjtGetChild(0), arguments);
    } else if (jexlNode instanceof ASTAndNode) {
      
      for (int i=0;i<jexlNode.jjtGetNumChildren(); i++) {
        if (i>0) {
          result.append("and ");
        }
        convertJexlRowToSqlHelper(result, jexlNode.jjtGetChild(i), arguments);
      }
    } else if (jexlNode instanceof ASTOrNode) {
      
      for (int i=0;i<jexlNode.jjtGetNumChildren(); i++) {
        if (i>0) {
          result.append("or ");
        }
        convertJexlRowToSqlHelper(result, jexlNode.jjtGetChild(i), arguments);
      }
      
    } else {
      throw new RuntimeException("Not expecting node type: " + jexlNode.getClass().getName() + ", children: " + jexlNode.jjtGetNumChildren());
    }
  }
  
  public static void convertJexlScriptToSqlHelper(StringBuilder result, JexlNode jexlNode, List<MultiKey> arguments) {
    if (jexlNode instanceof ASTJexlScript && 1==jexlNode.jjtGetNumChildren()) {
      convertJexlScriptToSqlHelper(result, jexlNode.jjtGetChild(0), arguments);
    } else if (jexlNode instanceof ASTReference && 2==jexlNode.jjtGetNumChildren()) {
      convertJexlReferenceTwoChildrenToSqlHelper(result, (ASTReference)jexlNode, arguments);
    } else if (jexlNode instanceof ASTReferenceExpression && 1==jexlNode.jjtGetNumChildren()) {
      result.append("(");
      convertJexlScriptToSqlHelper(result, jexlNode.jjtGetChild(0), arguments);
      result.append(")");
    } else if (jexlNode instanceof ASTNotNode && 1==jexlNode.jjtGetNumChildren()) {
      result.append("not ");
      convertJexlScriptToSqlHelper(result, jexlNode.jjtGetChild(0), arguments);
    } else if (jexlNode instanceof ASTAndNode) {
      
      for (int i=0;i<jexlNode.jjtGetNumChildren(); i++) {
        if (i>0) {
          result.append("and ");
        }
        convertJexlScriptToSqlHelper(result, jexlNode.jjtGetChild(i), arguments);
      }
    } else if (jexlNode instanceof ASTOrNode) {
      
      for (int i=0;i<jexlNode.jjtGetNumChildren(); i++) {
        if (i>0) {
          result.append("or ");
        }
        convertJexlScriptToSqlHelper(result, jexlNode.jjtGetChild(i), arguments);
      }
      
    } else {
      throw new RuntimeException("Not expecting node type: " + jexlNode.getClass().getName() + ", children: " + jexlNode.jjtGetNumChildren());
    }
  }
  
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
      
      for (AttributeAssign attributeAssign : attributeAssigns) {
        GrouperDaemonUtils.stopProcessingIfJobPaused();

        if (StringUtils.isBlank(attributeAssign.getOwnerGroupId())) {
          continue;
        }
        String script = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_JEXL_SCRIPT);

        //System.out.println(script);
        
        GrouperJexlScriptAnalysis analyzeJexlScript = analyzeJexlScript(script);
        GrouperJexlScriptPart grouperJexlScriptPart = analyzeJexlScript.getGrouperJexlScriptParts().get(0);
        List<MultiKey> arguments = grouperJexlScriptPart.getArguments();
        String whereClause = grouperJexlScriptPart.getWhereClause().toString();

        GcDbAccess gcDbAccess = new GcDbAccess();
        int argumentIndex = 0;
        
        String previousAttributeAlias = null;
        
        for (MultiKey argument : arguments) {
          String argumentString = (String)argument.getKey(0);
          if (StringUtils.equals(argumentString, "group")) {
            String fieldName = (String)argument.getKey(1);
            if (!StringUtils.equals(fieldName, "members")) {
              throw new RuntimeException("Not expecting field: '" + fieldName + "'");
            }
            String groupName = (String)argument.getKey(2);
            //TODO make this more efficient
            SqlCacheGroup sqlCacheGroup = SqlCacheGroupDao.retrieveByGroupNamesFieldNames(GrouperUtil.toList(new MultiKey(groupName, fieldName))).values().iterator().next();
            gcDbAccess.addBindVar(sqlCacheGroup.getInternalId());
          } else if (StringUtils.equals(argumentString, "attribute")) {
            String attributeAlias = (String)argument.getKey(1);
            GrouperDataFieldWrapper grouperDataFieldWrapper = grouperDataEngine.getGrouperDataProviderIndex().getFieldWrapperByLowerAlias().get(attributeAlias.toLowerCase());
            GrouperDataField grouperDataField = grouperDataFieldWrapper.getGrouperDataField();
            gcDbAccess.addBindVar(grouperDataField.getInternalId());
            previousAttributeAlias = attributeAlias;
          } else if (StringUtils.equals(argumentString, "row")) {
            String rowAlias = (String)argument.getKey(1);
            GrouperDataRowWrapper grouperDataRowWrapper = grouperDataEngine.getGrouperDataProviderIndex().getRowWrapperByLowerAlias().get(rowAlias.toLowerCase());
            GrouperDataRow grouperDataRow = grouperDataRowWrapper.getGrouperDataRow();
            gcDbAccess.addBindVar(grouperDataRow.getInternalId());

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
            fieldDataType.assignValue(grouperDataFieldAssign, value);
            
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

          }
          argumentIndex++;
        }   
        String sql = "select id from grouper_members gm where " + whereClause;

//        System.out.println(script);
//        System.out.println(sql);
        
        Set<String> memberIds = new HashSet<String>(gcDbAccess.sql(sql).selectList(String.class));
        
        Set<String> previousMemberIds = new HashSet<String>(new GcDbAccess().sql("select member_id from grouper_memberships_lw_v gmlv where group_id = ? and list_name = 'members'").addBindVar(attributeAssign.getOwnerGroupId()).selectList(String.class));
        
        Set<String> insertMemberIds = new HashSet<>(memberIds);
        insertMemberIds.removeAll(previousMemberIds);
        
        Set<String> deleteMemberIds = new HashSet<>(previousMemberIds);
        deleteMemberIds.removeAll(memberIds);
        
        String ownerGroupId = attributeAssign.getOwnerGroupId();
        
        Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), ownerGroupId, true);
        for (String memberId : insertMemberIds) {
          group.addMember(MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), memberId, true).getSubject(), false);
        }

        for (String memberId : deleteMemberIds) {
          group.deleteMember(MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), memberId, true).getSubject(), false);
        }
        
        GrouperUtil.mapAddValue(debugMap, "inserts", insertMemberIds.size());
        otherJobInput.getHib3GrouperLoaderLog().addInsertCount(insertMemberIds.size());
        GrouperUtil.mapAddValue(debugMap, "deletes", deleteMemberIds.size());
        otherJobInput.getHib3GrouperLoaderLog().addDeleteCount(deleteMemberIds.size());

      }

//      this.grouperLoaderJexlScriptGroups = new ArrayList<GrouperLoaderJexlScriptGroup>();
//      
//      int groupsWithInvalidScripts = 0;
//      
//      Pattern groupHasMemberPattern = Pattern.compile("entity\\.memberOf\\s*\\(\\s*'([^']+)'\\s*\\)");
//      
//      Set<String> allGroupNamesInScript = new HashSet<String>();
//      Set<String> allGroupIdOwners = new HashSet<String>();
//      
//      for (AttributeAssign attributeAssign : attributeAssigns) {
//        
//        GrouperLoaderJexlScriptGroup grouperLoaderJexlScriptGroup = new GrouperLoaderJexlScriptGroup();
//        
//        if (StringUtils.isBlank(attributeAssign.getOwnerGroupId())) {
//          continue;
//        }
//        allGroupIdOwners.add(attributeAssign.getOwnerGroupId());
//        grouperLoaderJexlScriptGroup.setAttributeAssign(attributeAssign);
//        grouperLoaderJexlScriptGroup.setGroupId(attributeAssign.getOwnerGroupId());
//        
//        grouperLoaderJexlScriptGroup.setAttributeAssignId(attributeAssign.getId());
//
//        String script = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_JEXL_SCRIPT);
//        
//        if (!StringUtils.isBlank(GrouperAbac.validScript(script, grouperDataEngine))) {
//          groupsWithInvalidScripts++;
//          continue;
//        }
//        
//        // ${ entity.memberOf('test:testGroup0') }
//        Matcher groupHasMemberMatcher = groupHasMemberPattern.matcher(script);
//        while (groupHasMemberMatcher.find()) {
//          String groupName = groupHasMemberMatcher.group(1);
//          
//          grouperLoaderJexlScriptGroup.getScriptContainsGroupNames().add(groupName);
//          allGroupNamesInScript.add(groupName);
//        }
//        
//        grouperLoaderJexlScriptGroup.setScript(script);
//
//        boolean includeInternalSources = GrouperUtil.booleanValue(
//            attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAbac.jexlScriptStemName() 
//                + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_INCLUDE_INTERNAL_SOURCES), false);
//        grouperLoaderJexlScriptGroup.setIncludeInternalSubjectSourceForEntities(includeInternalSources);
//
//        this.grouperLoaderJexlScriptGroups.add(grouperLoaderJexlScriptGroup);
//        
//      }
//      debugMap.put("groupsWithInvalidScripts", groupsWithInvalidScripts);
//      debugMap.put("distinctGroupsInScripts", allGroupNamesInScript.size());
//      
//      Map<String, String> groupNameToId = new HashMap<String, String>();
//      Map<String, String> groupIdToName = new HashMap<String, String>();
//      for (Group group : GrouperUtil.nonNull(new GroupFinder().assignGroupNames(allGroupNamesInScript).findGroups())) {
//        groupNameToId.put(group.getName(), group.getId());
//        groupIdToName.put(group.getId(), group.getName());
//      }
//
//      Set<String> allGroupIds = new HashSet<String>(allGroupIdOwners);
//      allGroupIds.addAll(groupIdToName.keySet());
//      List<String> allGroupIdsList = new ArrayList<String>(allGroupIds);
//      int batchSize = 900;
//      int numberOfBatches = GrouperUtil.batchNumberOfBatches(allGroupIdsList, 900, false);
//      Map<String, Set<String>> groupIdToMemberIds = new HashMap<String, Set<String>>();
//      Map<String, Set<String>> memberIdToGroupIds = new HashMap<String, Set<String>>();
//      Map<String, String> memberIdToSourceId = new HashMap<String, String>();
//
//      // get all memberships
//
//      for (int i=0;i<numberOfBatches;i++) {
//        List<String> groupIdsBatch = GrouperUtil.batchList(allGroupIdsList, batchSize, i);
//        GcDbAccess gcDbAccess = new GcDbAccess();
//        String sql = "select group_id, member_id, subject_source from grouper_memberships_lw_v where group_id in (" 
//            + GrouperClientUtils.appendQuestions(GrouperUtil.length(groupIdsBatch)) + ") and list_name = 'members'";
//        List<Object[]> results = gcDbAccess.sql(sql).bindVars(GrouperUtil.toArray(groupIdsBatch, Object.class)).selectList(Object[].class);
//        for (Object[] row : results) {
//          String groupId = (String)row[0];
//          String memberId = (String)row[1];
//          String subjectSource = (String)row[2];
//          
//          memberIdToSourceId.put(memberId, subjectSource);
//
//          Set<String> memberIds = groupIdToMemberIds.get(groupId);
//          if (memberIds == null) {
//            memberIds = new HashSet<String>();
//            groupIdToMemberIds.put(groupId, memberIds);
//          }
//          memberIds.add(memberId);
//          
//          Set<String> groupIds = memberIdToGroupIds.get(memberId);
//          if (groupIds == null) {
//            groupIds = new HashSet<String>();
//            memberIdToGroupIds.put(memberId, groupIds);
//          }
//          groupIds.add(groupId);
//          
//        }
//      }
//
//      Map<String, Map<String, Set<Object>>> memberIdToDataFieldAliasToValues = new HashMap<String, Map<String, Set<Object>>>();
//      {
//        // get attribute assignments
//        String sql = "select gda.name, gdfav.member_id, gdfav.subject_source_id, gdfav.value_text, gdfav.value_integer, "
//            + " gdfav.data_field_config_id"
//            + " from grouper_data_field_assign_v gdfav, grouper_data_alias gda where gda.data_field_internal_id = gdfav.data_field_internal_id ";
//        List<Object[]> results = new GcDbAccess().sql(sql).selectList(Object[].class);
//        for (Object[] row : results) {
//          String aliasName = ((String)row[0]).toLowerCase();
//          String memberId = (String)row[1];
//          String subjectSource = (String)row[2];
//          String valueString = (String)row[3];
//          Long valueInt = GrouperUtil.longObjectValue(row[4], true);
//          String dataFieldConfigId = (String)row[5];
//          
//          memberIdToSourceId.put(memberId, subjectSource);
//          
//          Map<String, Set<Object>> dataFieldAliasToValues = memberIdToDataFieldAliasToValues.get(memberId);
//          if (dataFieldAliasToValues == null) {
//            dataFieldAliasToValues = new HashMap<>();
//            memberIdToDataFieldAliasToValues.put(memberId, dataFieldAliasToValues);
//          }
//          
//          Set<Object> values = dataFieldAliasToValues.get(aliasName);
//          if (values == null) {
//            values = new HashSet<Object>();
//            dataFieldAliasToValues.put(aliasName, values);
//          }
//          GrouperDataFieldConfig grouperDataFieldConfig = grouperDataEngine.getFieldConfigByConfigId().get(dataFieldConfigId);
//          Object value = grouperDataFieldConfig.getFieldDataType().convertValue(valueInt, valueString);
//          values.add(value);
//        }
//      }        
//      Map<String, Map<Long, Map<String, Set<Object>>>> memberIdToDataRowAssignInternalIdToDataFieldAliasToValues = new HashMap<>();
//      Map<Long, String> rowAssignInternalIdToRowConfigId = new HashMap<>(); 
//
//      {
//        Map<Long, String> rowAssignInternalIdToMemberId = new HashMap<>(); 
//        // get row assignments
//        String sql = "select distinct gda.name, gdrav.member_id  , gdrav.subject_source_id , gdrav.data_row_config_id, gdrav.data_row_internal_id "
//            + ", gdrav.data_row_assign_internal_id  from grouper_data_row_assign_v gdrav, grouper_data_alias gda where gda.data_row_internal_id = gdrav.data_row_internal_id ";
//        List<Object[]> results = new GcDbAccess().sql(sql).selectList(Object[].class);
//        for (Object[] row : results) {
//          String aliasName = ((String)row[0]).toLowerCase();
//          String memberId = (String)row[1];
//          String subjectSource = (String)row[2];
//          String dataRowConfigId = (String)row[3];
//          Long rowInternalId = GrouperUtil.longValue(row[4]);
//          Long rowAssignInternalId = GrouperUtil.longValue(row[5]);
//          
//          memberIdToSourceId.put(memberId, subjectSource);
//          
//          Map<String, Set<Object>> dataFieldAliasToValues = memberIdToDataFieldAliasToValues.get(memberId);
//          if (dataFieldAliasToValues == null) {
//            dataFieldAliasToValues = new HashMap<>();
//            memberIdToDataFieldAliasToValues.put(memberId, dataFieldAliasToValues);
//          }
//          
//          rowAssignInternalIdToMemberId.put(rowAssignInternalId, memberId);
//          rowAssignInternalIdToRowConfigId.put(rowAssignInternalId, dataRowConfigId);
//        }
//        
//        sql = "select gda.name, gdrfav.value_text, gdrfav.value_integer, gdrfav.data_field_config_id, gdrfav.data_row_assign_internal_id "
//          + " from grouper_data_row_field_asgn_v gdrfav, grouper_data_alias gda where gdrfav.data_field_internal_id = gda.data_field_internal_id";
//
//        results = new GcDbAccess().sql(sql).selectList(Object[].class);
//        for (Object[] row : results) {
//          
//          String aliasName = ((String)row[0]).toLowerCase();
//          String valueString = (String)row[1];
//          Long valueInt = GrouperUtil.longObjectValue(row[2], true);
//          String dataFieldConfigId = (String)row[3];
//          Long rowAssignInternalId = GrouperUtil.longValue(row[4]);
//          
//          String memberId = rowAssignInternalIdToMemberId.get(rowAssignInternalId);
//
//          Map<Long, Map<String, Set<Object>>> dataRowAssignInternalIdToDataFieldAliasToValues = memberIdToDataRowAssignInternalIdToDataFieldAliasToValues.get(memberId);
//          if (dataRowAssignInternalIdToDataFieldAliasToValues == null) {
//            dataRowAssignInternalIdToDataFieldAliasToValues = new HashMap<>();
//            memberIdToDataRowAssignInternalIdToDataFieldAliasToValues.put(memberId, dataRowAssignInternalIdToDataFieldAliasToValues);
//          }
//          
//          Map<String, Set<Object>> rowDataFieldAliasToValues = dataRowAssignInternalIdToDataFieldAliasToValues.get(rowAssignInternalId);
//          if (rowDataFieldAliasToValues == null) {
//            rowDataFieldAliasToValues = new HashMap<>();
//            dataRowAssignInternalIdToDataFieldAliasToValues.put(rowAssignInternalId, rowDataFieldAliasToValues);
//          }
//          
//          Set<Object> values = rowDataFieldAliasToValues.get(aliasName);
//          if (values == null) {
//            values = new HashSet<Object>();
//            rowDataFieldAliasToValues.put(aliasName, values);
//          }
//          GrouperDataFieldConfig grouperDataFieldConfig = grouperDataEngine.getFieldConfigByConfigId().get(dataFieldConfigId);
//          Object value = grouperDataFieldConfig.getFieldDataType().convertValue(valueInt, valueString);
//          values.add(value);
//        }
//        
//      }        
//      
//
//      
//      //TODO put things in methods
//      //TODO recalc groups which depend on other abac groups
//      //TODO add test cases
//      // TODO add loader metadata
//      int deleteCount = 0;
//      int insertCount = 0;
//      int errorCount = 0;
//      // loop through each owner group
//      for (GrouperLoaderJexlScriptGroup grouperLoaderJexlScriptGroup : this.grouperLoaderJexlScriptGroups) {
//        
//        // lets get all members in all groups
//        Set<String> allMemberIdsInAllRelatedGroups = new HashSet<String>();
//        Set<String> memberIdsOfOwnerGroup = GrouperUtil.nonNull(groupIdToMemberIds.get(grouperLoaderJexlScriptGroup.getGroupId()));
//        
//        Group group = grouperLoaderJexlScriptGroup.getGroup();
//        for (String memberId : GrouperUtil.nonNull(memberIdsOfOwnerGroup)) {
//          String sourceId = memberIdToSourceId.get(memberId);
//          if (!grouperLoaderJexlScriptGroup.isIncludeInternalSubjectSourceForEntities() && GrouperAbac.internalSourceId(sourceId)) {
//            group.deleteMember(MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), memberId, true));
//            deleteCount++;
//          } else {
//            allMemberIdsInAllRelatedGroups.add(memberId);
//          }
//        }
//        
//        allMemberIdsInAllRelatedGroups.addAll(GrouperUtil.nonNull(memberIdsOfOwnerGroup));
//        allMemberIdsInAllRelatedGroups.addAll(GrouperUtil.nonNull(memberIdToDataFieldAliasToValues).keySet());
//
//        Set<String> scriptContainsGroupIds = new HashSet<String>();
//        for (String groupName : GrouperUtil.nonNull(grouperLoaderJexlScriptGroup.getScriptContainsGroupNames())) {
//          String groupId = groupNameToId.get(groupName);
//          scriptContainsGroupIds.add(groupId);
//          Set<String> theseMemberIds = groupIdToMemberIds.get(groupId);
//          for (String memberId : GrouperUtil.nonNull(theseMemberIds)) {
//            String sourceId = memberIdToSourceId.get(memberId);
//            if (!grouperLoaderJexlScriptGroup.isIncludeInternalSubjectSourceForEntities() && GrouperAbac.internalSourceId(sourceId)) {
//              continue;
//            }
//            allMemberIdsInAllRelatedGroups.add(memberId);
//          }
//        }
//        allMemberIdsInAllRelatedGroups.addAll(memberIdToDataRowAssignInternalIdToDataFieldAliasToValues.keySet());
//        
//        Map<String, Object> variableMap = new HashMap<String, Object>();
//
//        for (String memberId : allMemberIdsInAllRelatedGroups) {
//          
//          GrouperAbacEntity grouperAbacEntity = new GrouperAbacEntity();
//          grouperAbacEntity.setMemberId(memberId);
//          
//          Set<String> memberOf = new HashSet<String>();
//          grouperAbacEntity.setMemberOfGroupNames(memberOf);
//          
//          Set<String> groupIds = new HashSet<String>(GrouperUtil.nonNull(memberIdToGroupIds.get(memberId)));
//          groupIds.retainAll(scriptContainsGroupIds);
//          
//          for (String groupId : groupIds) {
//            String groupName = groupIdToName.get(groupId);
//            memberOf.add(groupName);
//          }
//          
//          grouperAbacEntity.setGrouperDataEngine(grouperDataEngine);
//          Map<String, Set<Object>> dataFieldAliasToValues = memberIdToDataFieldAliasToValues.get(memberId);
//          grouperAbacEntity.setDataAliasToValues(dataFieldAliasToValues);
//          
//          Map<Long, Map<String, Set<Object>>> dataRowAssignInternalIdToDataFieldConfigIdToValues  = memberIdToDataRowAssignInternalIdToDataFieldAliasToValues.get(memberId);
//          grouperAbacEntity.setDataRowAssignInternalIdToDataFieldAliasToValues(dataRowAssignInternalIdToDataFieldConfigIdToValues);
//
//          variableMap.put("entity", grouperAbacEntity);
//          
//          try {
//            Object result = GrouperUtil.substituteExpressionLanguageScript(grouperLoaderJexlScriptGroup.getScript(), variableMap, true, false, true);
//            boolean shouldBeInGroup = GrouperUtil.booleanValue(result, false);
//            boolean currentlyInGroup = memberIdsOfOwnerGroup.contains(memberId);
//            if (shouldBeInGroup != currentlyInGroup) {
//              Member member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), memberId, true);
//              if (shouldBeInGroup) {
//                Subject subject = member.getSubject();
//                group.addMember(subject, false);
//                insertCount++;
//              } else {
//                group.deleteMember(member, false);
//                deleteCount++;
//              }
//            }
//            
//            
//          } catch (RuntimeException re) {
//            runtimeException = re;
//            LOG.error("Error on memberId: " + memberId, re);
//            errorCount++;
//          }
//          
//        }
//      }
//
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
