/*
 * @author mchyzer
 * $Id: GrouperClient.java,v 1.10 2008-12-04 07:51:39 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouperClient.api.GcAddMember;
import edu.internet2.middleware.grouperClient.api.GcDeleteMember;
import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import edu.internet2.middleware.grouperClient.api.GcGetMembers;
import edu.internet2.middleware.grouperClient.api.GcGroupSave;
import edu.internet2.middleware.grouperClient.api.GcHasMember;
import edu.internet2.middleware.grouperClient.api.GcLdapSearchAttribute;
import edu.internet2.middleware.grouperClient.commandLine.GcLdapSearchAttributeConfig;
import edu.internet2.middleware.grouperClient.commandLine.GcLdapSearchAttributeConfig.SearchAttributeResultType;
import edu.internet2.middleware.grouperClient.util.GrouperClientLog;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GcTransactionType;
import edu.internet2.middleware.grouperClient.ws.WsMemberFilter;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupDetail;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupSaveResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupSaveResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupToSave;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import edu.internet2.middleware.grouperClientExt.edu.internet2.middleware.morphString.Crypto;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;


/**
 * main class for grouper client.  note, stdout is for output, stderr is for error messages (or logs)
 */
public class GrouperClient {

  /**
   * 
   */
  static Log log = GrouperClientUtils.retrieveLog(GrouperClient.class);

  /** ldap operations from config file */
  private static Map<String, GcLdapSearchAttributeConfig> ldapOperations = null;

  /**
   * lazy load the ldap operations
   * @return the ldap operations
   */
  private static Map<String, GcLdapSearchAttributeConfig> ldapOperations() {
    
    //lazy load if null
    if (ldapOperations == null) {
      ldapOperations = new LinkedHashMap<String, GcLdapSearchAttributeConfig>();
      
      int i=0;
      while (true) {
        String operationName = GrouperClientUtils.propertiesValue("ldapSearchAttribute.operationName." + i, false);
        if (GrouperClientUtils.isBlank(operationName)) {
          break;
        }
        if (ldapOperations.containsKey(operationName)) {
          throw new RuntimeException("There is an ldap operation defined twice in grouper.client.properties: '" + operationName + "'");
        }
        
        GcLdapSearchAttributeConfig gcLdapSearchAttributeConfig = new GcLdapSearchAttributeConfig();
        gcLdapSearchAttributeConfig.setOperationName(operationName);
        
        //ldapSearchAttribute.ldapName.2 = ou=groups
        gcLdapSearchAttributeConfig.setLdapName(GrouperClientUtils.propertiesValue("ldapSearchAttribute.ldapName." + i, true));

        {
          //ldapSearchAttribute.matchingAttributes.2 = pennid
          //ldapSearchAttribute.matchingAttributeLabels.2 = pennid
          String[] matchingAttributeLabels = GrouperClientUtils.splitTrim(
              GrouperClientUtils.propertiesValue("ldapSearchAttribute.matchingAttributeLabels." + i, true), ",");
          String[] ldapMatchingAttributes = GrouperClientUtils.splitTrim(
              GrouperClientUtils.propertiesValue("ldapSearchAttribute.matchingAttributes." + i, true), ",");
          
          if (matchingAttributeLabels.length != ldapMatchingAttributes.length) {
            throw new RuntimeException("ldapSearchAttribute #" + i + " operation: " + operationName
                + " should have the same number of matchingAttributeLabels " 
                + matchingAttributeLabels.length + " and matchingAttributes " + ldapMatchingAttributes.length);
          }
              
          for (int j=0;j<matchingAttributeLabels.length;j++) {
            gcLdapSearchAttributeConfig.addMatchingAttribute(matchingAttributeLabels[j], ldapMatchingAttributes[j]);
          }
        }
        
        {
          //ldapSearchAttribute.returningAttributes.2 = pennname
          String[] ldapReturningAttributes = GrouperClientUtils.splitTrim(
              GrouperClientUtils.propertiesValue("ldapSearchAttribute.returningAttributes." + i, true), ",");
          
          for (int j=0;j<ldapReturningAttributes.length;j++) {
            gcLdapSearchAttributeConfig.addReturningAttribute(ldapReturningAttributes[j]);
          }
        }
        

        //ldapSearchAttribute.outputTemplate.0 = pennid: ${pennid}
        gcLdapSearchAttributeConfig.setOutputTemplate(GrouperClientUtils.propertiesValue("ldapSearchAttribute.outputTemplate." + i, true));

        //ldapSearchAttribute.resultType.2 = string
        gcLdapSearchAttributeConfig.setSearchAttributeResultType(GrouperClientUtils.propertiesValue("ldapSearchAttribute.resultType." + i, true));

        
        ldapOperations.put(operationName, gcLdapSearchAttributeConfig);
        i++;
      }
      
    }
    return ldapOperations;
  }
  
  /** should java exit on error? */
  public static boolean exitOnError = true;
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    try {
      if (GrouperClientUtils.length(args) == 0) {
        usage();
      }
      
      //map of all command line args
      Map<String, String> argMap = GrouperClientUtils.argMap(args);
      
      Map<String, String> argMapNotUsed = new LinkedHashMap<String, String>(argMap);

      boolean debugMode = GrouperClientUtils.argMapBoolean(argMap, argMapNotUsed, "debug", false, false);
      GrouperClientLog.assignDebugToConsole(debugMode);
      
      String operation = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "operation", true);
      
      //where results should go if file
      String saveResultsToFile = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "saveResultsToFile", false);
      boolean shouldSaveResultsToFile = !GrouperClientUtils.isBlank(saveResultsToFile);
      
      if (shouldSaveResultsToFile) {
        log.debug("Will save results to file: " + GrouperClientUtils.fileCanonicalPath(new File(saveResultsToFile)));
      }
      
      String result = null;
      
      if (GrouperClientUtils.equals(operation, "encryptPassword")) {
        
        result = encryptText(argMap, argMapNotUsed, shouldSaveResultsToFile);
        
      } else if (ldapOperations().containsKey(operation)) {
        
        result = ldapSearchAttribute(argMap, argMapNotUsed, operation);
      } else if (GrouperClientUtils.equals(operation, "addMemberWs")) {
        result = addMember(argMap, argMapNotUsed);

      } else if (GrouperClientUtils.equals(operation, "deleteMemberWs")) {
        result = deleteMember(argMap, argMapNotUsed);

      } else if (GrouperClientUtils.equals(operation, "getMembersWs")) {
        result = getMembers(argMap, argMapNotUsed);

      } else if (GrouperClientUtils.equals(operation, "hasMemberWs")) {
        result = hasMember(argMap, argMapNotUsed);

      } else if (GrouperClientUtils.equals(operation, "getGroupsWs")) {
        result = getGroups(argMap, argMapNotUsed);

      } else if (GrouperClientUtils.equals(operation, "groupSaveWs")) {
        result = groupSave(argMap, argMapNotUsed);

      } else {
        usage();
      }
      
      //this already has a newline on it
      if (shouldSaveResultsToFile) {
        GrouperClientUtils.saveStringIntoFile(new File(saveResultsToFile), result);
      } else {
        System.out.print(result);
      }

      if (argMapNotUsed.size() > 0) {
        boolean failOnExtraParams = GrouperClientUtils.propertiesValueBoolean(
            "grouperClient.failOnExtraCommandLineArgs", true, true);
        String error = "Invalid command line arguments: " + argMapNotUsed.keySet();
        if (failOnExtraParams) {
          throw new RuntimeException(error);
        }
        log.error(error);
      }
      
    } catch (Exception e) {
      System.err.println("Error with grouper client, check the logs: " + e.getMessage());
      log.fatal(e.getMessage(), e);
      if (exitOnError) {
        System.exit(1);
      }
      if (e instanceof RuntimeException) {
        throw (RuntimeException)e;
      }
      throw new RuntimeException(e.getMessage(), e);
    } finally {
      GrouperClientLog.assignDebugToConsole(false);
    }
  }

  /**
   * @param argMap
   * @param argMapNotUsed
   * @param shouldSaveResultsToFile
   * @return result
   */
  private static String encryptText(Map<String, String> argMap,
      Map<String, String> argMapNotUsed,
      boolean shouldSaveResultsToFile) {
    boolean dontMask = GrouperClientUtils.argMapBoolean(argMap, argMapNotUsed, "dontMask", false, false);
    
    String encryptKey = GrouperClientUtils.propertiesValue("encrypt.key", true);
    
    boolean disableExternalFileLookup = GrouperClientUtils.propertiesValueBoolean(
        "encrypt.disableExternalFileLookup", false, true);
    
    //lets lookup if file
    encryptKey = GrouperClientUtils.readFromFileIfFile(encryptKey, disableExternalFileLookup);
    
    //lets get the password from stdin
    String password = GrouperClientUtils.retrievePasswordFromStdin(dontMask, 
        "Type the string to encrypt (note: pasting might echo it back): ");
    
    String encrypted = new Crypto(encryptKey).encrypt(password);
    
    if (shouldSaveResultsToFile) {
      return encrypted;
    }
    return "Encrypted password: " + encrypted;
  }

  /**
   * @param argMap
   * @param argMapNotUsed
   * @param operation
   * @return the output
   */
  private static String ldapSearchAttribute(Map<String, String> argMap,
      Map<String, String> argMapNotUsed, String operation) {
    //ldap operation
    GcLdapSearchAttributeConfig gcLdapSearchAttributeConfig = ldapOperations().get(operation);
    
    GcLdapSearchAttribute gcLdapSearchAttribute = new GcLdapSearchAttribute();
    gcLdapSearchAttribute.assignLdapName(gcLdapSearchAttributeConfig.getLdapName());
    
    //go through the matching attributes and get from command line
    for (String matchingAttributeLabel : gcLdapSearchAttributeConfig.getMatchingAttributes().keySet()) {
      String matchingAttributeValue = GrouperClientUtils.argMapString(argMap, argMapNotUsed, matchingAttributeLabel, true);
      gcLdapSearchAttribute.addMatchingAttribute(
          gcLdapSearchAttributeConfig.getMatchingAttributes().get(matchingAttributeLabel), matchingAttributeValue);
    }
    
    //go through the returning attributes and assign to query
    for (String ldapAttribute : gcLdapSearchAttributeConfig.getReturningAttributes()) {
      gcLdapSearchAttribute.addReturningAttribute(ldapAttribute);
    }
    
    gcLdapSearchAttribute.execute();
    
    SearchAttributeResultType searchAttributeResultType = gcLdapSearchAttributeConfig.getSearchAttributeResultTypeEnum();
    
    log.debug("LDAP search attribute result type: " + searchAttributeResultType);
    
    String outputTemplate = null;
    if (argMap.containsKey("outputTemplate")) {
      outputTemplate = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "outputTemplate", false);
      outputTemplate = GrouperClientUtils.substituteCommonVars(outputTemplate);
    } else {
      outputTemplate = gcLdapSearchAttributeConfig.getOutputTemplate();
    }
    
    
    
    String results = searchAttributeResultType.processOutput(gcLdapSearchAttributeConfig, gcLdapSearchAttribute, outputTemplate);
    
    return results;
  }

  /**
     * @param argMap
     * @param argMapNotUsed
     * @return result
     */
    private static String addMember(Map<String, String> argMap,
        Map<String, String> argMapNotUsed) {
      
      String groupName = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "groupName", true);
      String fieldName = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "fieldName", false);
      String txType = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "txType", false);
     
      Boolean replaceAllExisting = GrouperClientUtils.argMapBoolean(argMap, argMapNotUsed, "replaceAllExisting");
  
      Boolean includeGroupDetail = GrouperClientUtils.argMapBoolean(argMap, argMapNotUsed, "includeGroupDetail");
      
      Boolean includeSubjectDetail = GrouperClientUtils.argMapBoolean(argMap, argMapNotUsed, "includeSubjectDetail");
  
      Set<String> subjectAttributeNames = GrouperClientUtils.argMapSet(argMap, argMapNotUsed, "subjectAttributeNames", false);
  
      List<WsParam> params = retrieveParamsFromArgs(argMap, argMapNotUsed);
      
      GcAddMember gcAddMember = new GcAddMember();        
  
      String clientVersion = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "clientVersion", false);
      gcAddMember.assignClientVersion(clientVersion);

      for (WsParam param : params) {
        gcAddMember.addParam(param);
      }
      
      List<WsSubjectLookup> wsSubjectLookupList = retrieveSubjectsFromArgs(argMap,
          argMapNotUsed);
      
      for (WsSubjectLookup wsSubjectLookup : wsSubjectLookupList) {
        gcAddMember.addSubjectLookup(wsSubjectLookup);
      }
      
      gcAddMember.assignGroupName(groupName);
      
      WsSubjectLookup actAsSubject = retrieveActAsSubjectFromArgs(argMap, argMapNotUsed);
      
      gcAddMember.assignActAsSubject(actAsSubject);
      
      gcAddMember.assignReplaceAllExisting(replaceAllExisting);
      gcAddMember.assignIncludeGroupDetail(includeGroupDetail);
      gcAddMember.assignIncludeSubjectDetail(includeSubjectDetail);
      
      gcAddMember.assignFieldName(fieldName);
      gcAddMember.assignTxType(GcTransactionType.valueOfIgnoreCase(txType));
  
      for (String subjectAttribute : GrouperClientUtils.nonNull(subjectAttributeNames)) {
        gcAddMember.addSubjectAttributeName(subjectAttribute);
      }
      
      WsAddMemberResults wsAddMemberResults = gcAddMember.execute();
      
      StringBuilder result = new StringBuilder();
      int index = 0;
      
      Map<String, Object> substituteMap = new LinkedHashMap<String, Object>();
  
      substituteMap.put("wsAddMemberResults", wsAddMemberResults);
  
      String outputTemplate = null;
  
      if (argMap.containsKey("outputTemplate")) {
        outputTemplate = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "outputTemplate", true);
        outputTemplate = GrouperClientUtils.substituteCommonVars(outputTemplate);
      } else {
        outputTemplate = GrouperClientUtils.propertiesValue("webService.addMember.output", true);
      }
  
      for (WsAddMemberResult wsAddMemberResult : wsAddMemberResults.getResults()) {
        
        substituteMap.put("index", index);
        substituteMap.put("wsAddMemberResult", wsAddMemberResult);
        
  //          result.append("Index " + index + ": success: " + wsAddMemberResult.getResultMetadata().getSuccess()
  //              + ": code: " + wsAddMemberResult.getResultMetadata().getResultCode() + ": " 
  //              + wsAddMemberResult.getWsSubject().getId() + "\n");
        
        String output = GrouperClientUtils.substituteExpressionLanguage(outputTemplate, substituteMap);
        result.append(output);
        
        index++;
      }
      
      return result.toString();
    }

  /**
     * @param argMap
     * @param argMapNotUsed
     * @return result
     */
    private static String groupSave(Map<String, String> argMap,
        Map<String, String> argMapNotUsed) {
      
      String txType = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "txType", false);
       
      Boolean includeGroupDetail = GrouperClientUtils.argMapBoolean(argMap, argMapNotUsed, "includeGroupDetail");
      
      List<WsParam> params = retrieveParamsFromArgs(argMap, argMapNotUsed);
      
      GcGroupSave gcGroupSave = new GcGroupSave();        
  
      for (WsParam param : params) {
        gcGroupSave.addParam(param);
      }
      
      WsGroupToSave wsGroupToSave = new WsGroupToSave();
      gcGroupSave.addGroupToSave(wsGroupToSave);
      WsGroup wsGroup = new WsGroup();
      wsGroupToSave.setWsGroup(wsGroup);

      String groupLookupName = GrouperClientUtils.argMapString(argMap, 
          argMapNotUsed, "groupLookupName", false);
      String groupLookupUuid = GrouperClientUtils.argMapString(argMap, 
          argMapNotUsed, "groupLookupUuid", false);
      
      String clientVersion = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "clientVersion", false);
      gcGroupSave.assignClientVersion(clientVersion);
      
      String name = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "name", true);
      wsGroup.setName(name);
      
      WsGroupLookup wsGroupLookup = new WsGroupLookup();
      wsGroupToSave.setWsGroupLookup(wsGroupLookup);
      //do the lookup if an edit
      if (!GrouperClientUtils.isBlank(groupLookupName) || !GrouperClientUtils.isBlank(groupLookupUuid)) {
        if (!GrouperClientUtils.isBlank(groupLookupName)) {
          wsGroupLookup.setGroupName(groupLookupName);
        }
        if (!GrouperClientUtils.isBlank(groupLookupUuid)) {
          wsGroupLookup.setUuid(groupLookupUuid);
        }
      } else {
        //just edit the name passed in
        wsGroupLookup.setGroupName(name);
      }
      
      //save mode
      String saveMode = GrouperClientUtils.argMapString(argMap, 
          argMapNotUsed, "saveMode", false);
      if (saveMode != null) {
        wsGroupToSave.setSaveMode(saveMode);
      }
      
      String description = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "description", false);
      if (!GrouperClientUtils.isBlank(description)) {
        wsGroup.setDescription(description);
      }
      
      String displayExtension = GrouperClientUtils.argMapString(argMap, argMapNotUsed, 
          "displayExtension", false);

      //just default to the id
      if (GrouperClientUtils.isBlank(displayExtension)) {
        displayExtension = GrouperClientUtils.substringAfterLast(name, ":");
      }
      
      wsGroup.setDisplayExtension(displayExtension);

      boolean hasAttribute0 = GrouperClientUtils.isNotBlank(GrouperClientUtils.argMapString(
          argMap, argMapNotUsed, "attributeName0", false));
      boolean hasGroupDetailParamName0 = GrouperClientUtils.isNotBlank(GrouperClientUtils.argMapString(
          argMap, argMapNotUsed, "groupDetailParamName0", false));
      boolean hasCompositeType = GrouperClientUtils.isNotBlank(GrouperClientUtils.argMapString(
          argMap, argMapNotUsed, "compositeType", false));
      boolean hasLeftGroupName = GrouperClientUtils.isNotBlank(GrouperClientUtils.argMapString(
          argMap, argMapNotUsed, "leftGroupName", false));
      boolean hasRightGroupName = GrouperClientUtils.isNotBlank(GrouperClientUtils.argMapString(
          argMap, argMapNotUsed, "rightGroupName", false));
      boolean hasTypeNames = GrouperClientUtils.isNotBlank(GrouperClientUtils.argMapString(
          argMap, argMapNotUsed, "typeNames", false));
      
      if (hasAttribute0 || hasGroupDetailParamName0 || hasCompositeType 
          || hasLeftGroupName || hasRightGroupName || hasTypeNames) {
        
        WsGroupDetail wsGroupDetail = new WsGroupDetail();
        wsGroup.setDetail(wsGroupDetail);

        //attributes
        if (hasAttribute0) {
          int i=0;
          List<String> attributeNameList = new ArrayList<String>();
          List<String> attributeValueList = new ArrayList<String>();
          while (true) {
            String attributeName = GrouperClientUtils.argMapString(
                argMap, argMapNotUsed, "attributeName" + i, false);
            if (GrouperClientUtils.isBlank(attributeName)) {
              break;
            }
            String attributeValue = GrouperClientUtils.argMapString(
                argMap, argMapNotUsed, "attributeValue" + i, true);
            attributeNameList.add(attributeName);
            attributeValueList.add(attributeValue);
            i++;
          }
          wsGroupDetail.setAttributeNames(GrouperClientUtils.toArray(attributeNameList, String.class));
          wsGroupDetail.setAttributeValues(GrouperClientUtils.toArray(attributeValueList, String.class));
        }
        
        //params
        if (hasGroupDetailParamName0) {
          int i=0;
          List<WsParam> paramList = new ArrayList<WsParam>();
          while (true) {
            String paramName = GrouperClientUtils.argMapString(
                argMap, argMapNotUsed, "groupDetailParamName" + i, false);
            if (GrouperClientUtils.isBlank(paramName)) {
              break;
            }
            String paramValue = GrouperClientUtils.argMapString(
                argMap, argMapNotUsed, "groupDetailParamValue" + i, true);
            paramList.add(new WsParam(paramName, paramValue));
            i++;
          }
          wsGroupDetail.setParams(GrouperClientUtils.toArray(paramList, WsParam.class));
        }

        if (hasCompositeType) {
          wsGroupDetail.setHasComposite("T");
          String compositeType = GrouperClientUtils.argMapString(
              argMap, argMapNotUsed, "compositeType", true);
          wsGroupDetail.setCompositeType(compositeType);
          {
            String leftGroupName = GrouperClientUtils.argMapString(
                argMap, argMapNotUsed, "leftGroupName", true);
            WsGroup leftGroup = new WsGroup();
            leftGroup.setName(leftGroupName);
            wsGroupDetail.setLeftGroup(leftGroup);
          }
          {
            String rightGroupName = GrouperClientUtils.argMapString(
                argMap, argMapNotUsed, "rightGroupName", true);
            WsGroup rightGroup = new WsGroup();
            rightGroup.setName(rightGroupName);
            wsGroupDetail.setRightGroup(rightGroup);
          }
        }

        if (hasTypeNames) {
          List<String> typeNamesList = GrouperClientUtils.argMapList(
              argMap, argMapNotUsed, "typeNames", true);
          String[] typeNamesArray = GrouperClientUtils.toArray(typeNamesList, String.class);
          wsGroupDetail.setTypeNames(typeNamesArray);
        }
        
      }
      
      WsSubjectLookup actAsSubject = retrieveActAsSubjectFromArgs(argMap, argMapNotUsed);
      
      gcGroupSave.assignActAsSubject(actAsSubject);
      
      gcGroupSave.assignIncludeGroupDetail(includeGroupDetail);
      
      gcGroupSave.assignTxType(GcTransactionType.valueOfIgnoreCase(txType));
  
      WsGroupSaveResults wsGroupSaveResults = gcGroupSave.execute();
      
      StringBuilder result = new StringBuilder();
      int index = 0;
      
      Map<String, Object> substituteMap = new LinkedHashMap<String, Object>();
  
      substituteMap.put("wsGroupSaveResults", wsGroupSaveResults);
  
      String outputTemplate = null;
  
      if (argMap.containsKey("outputTemplate")) {
        outputTemplate = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "outputTemplate", true);
        outputTemplate = GrouperClientUtils.substituteCommonVars(outputTemplate);
      } else {
        outputTemplate = GrouperClientUtils.propertiesValue("webService.groupSave.output", true);
      }

      //there is one result...  but loop anyways
      for (WsGroupSaveResult wsGroupSaveResult : wsGroupSaveResults.getResults()) {
        
        substituteMap.put("index", index);
        substituteMap.put("wsGroupSaveResult", wsGroupSaveResult);
        wsGroupSaveResult.getWsGroup();
        substituteMap.put("wsGroup", wsGroup);
        
        String output = GrouperClientUtils.substituteExpressionLanguage(outputTemplate, substituteMap);
        result.append(output);
        
        index++;
      }
      
      return result.toString();
    }

  /**
     * @param argMap
     * @param argMapNotUsed
     * @return result
     */
    private static String hasMember(Map<String, String> argMap,
        Map<String, String> argMapNotUsed) {
      
      String groupName = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "groupName", true);
      String fieldName = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "fieldName", false);
     
      Boolean includeGroupDetail = GrouperClientUtils.argMapBoolean(argMap, argMapNotUsed, "includeGroupDetail");
      
      Boolean includeSubjectDetail = GrouperClientUtils.argMapBoolean(argMap, argMapNotUsed, "includeSubjectDetail");
  
      String memberFilter = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "memberFilter", false);

      Set<String> subjectAttributeNames = GrouperClientUtils.argMapSet(argMap, argMapNotUsed, "subjectAttributeNames", false);
  
      List<WsParam> params = retrieveParamsFromArgs(argMap, argMapNotUsed);
      
      GcHasMember gcHasMember = new GcHasMember();        
      
      String clientVersion = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "clientVersion", false);
      gcHasMember.assignClientVersion(clientVersion);

      for (WsParam param : params) {
        gcHasMember.addParam(param);
      }
      
      List<WsSubjectLookup> wsSubjectLookupList = retrieveSubjectsFromArgs(argMap,
          argMapNotUsed);
      
      for (WsSubjectLookup wsSubjectLookup : wsSubjectLookupList) {
        gcHasMember.addSubjectLookup(wsSubjectLookup);
      }
      
      gcHasMember.assignGroupName(groupName);
      
      WsSubjectLookup actAsSubject = retrieveActAsSubjectFromArgs(argMap, argMapNotUsed);
      
      gcHasMember.assignActAsSubject(actAsSubject);
      
      gcHasMember.assignMemberFilter(WsMemberFilter.valueOfIgnoreCase(memberFilter));

      gcHasMember.assignIncludeGroupDetail(includeGroupDetail);
      gcHasMember.assignIncludeSubjectDetail(includeSubjectDetail);
      
      gcHasMember.assignFieldName(fieldName);

      
      for (String subjectAttribute : GrouperClientUtils.nonNull(subjectAttributeNames)) {
        gcHasMember.addSubjectAttributeName(subjectAttribute);
      }
      
      WsHasMemberResults wsHasMemberResults = gcHasMember.execute();
      
      StringBuilder result = new StringBuilder();
      int index = 0;
      
      Map<String, Object> substituteMap = new LinkedHashMap<String, Object>();
  
      substituteMap.put("wsHasMemberResults", wsHasMemberResults);
  
      String outputTemplate = null;
  
      if (argMap.containsKey("outputTemplate")) {
        outputTemplate = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "outputTemplate", true);
        outputTemplate = GrouperClientUtils.substituteCommonVars(outputTemplate);
      } else {
        outputTemplate = GrouperClientUtils.propertiesValue("webService.hasMember.output", true);
      }
  
      for (WsHasMemberResult wsHasMemberResult : wsHasMemberResults.getResults()) {
        
        substituteMap.put("index", index);
        substituteMap.put("wsHasMemberResult", wsHasMemberResult);
        String resultCode = wsHasMemberResult.getResultMetadata().getResultCode();
        substituteMap.put("hasMember", GrouperClientUtils.equals("IS_MEMBER", resultCode));
//            result.append("Index " + index + ": success: " + wsHasMemberResult.getResultMetadata().getSuccess()
//                + ": code: " + resultCode + ": " 
//                + wsHasMemberResult.getWsSubject().getId() + "\n");
        
        String output = GrouperClientUtils.substituteExpressionLanguage(outputTemplate, substituteMap);
        result.append(output);
        
        index++;
      }
      
      return result.toString();
    }

  /**
     * @param argMap
     * @param argMapNotUsed
     * @return result
     */
    private static String deleteMember(Map<String, String> argMap,
        Map<String, String> argMapNotUsed) {
      
      String groupName = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "groupName", true);
      String fieldName = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "fieldName", false);
      String txType = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "txType", false);
     
      Boolean includeGroupDetail = GrouperClientUtils.argMapBoolean(argMap, argMapNotUsed, "includeGroupDetail");
      
      Boolean includeSubjectDetail = GrouperClientUtils.argMapBoolean(argMap, argMapNotUsed, "includeSubjectDetail");
  
      Set<String> subjectAttributeNames = GrouperClientUtils.argMapSet(argMap, argMapNotUsed, "subjectAttributeNames", false);
  
      List<WsParam> params = retrieveParamsFromArgs(argMap, argMapNotUsed);
      
      GcDeleteMember gcDeleteMember = new GcDeleteMember();        
  
      String clientVersion = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "clientVersion", false);
      gcDeleteMember.assignClientVersion(clientVersion);

      for (WsParam param : params) {
        gcDeleteMember.addParam(param);
      }
      
      List<WsSubjectLookup> wsSubjectLookupList = retrieveSubjectsFromArgs(argMap,
          argMapNotUsed);
      
      for (WsSubjectLookup wsSubjectLookup : wsSubjectLookupList) {
        gcDeleteMember.addSubjectLookup(wsSubjectLookup);
      }
      
      gcDeleteMember.assignGroupName(groupName);
      
      WsSubjectLookup actAsSubject = retrieveActAsSubjectFromArgs(argMap, argMapNotUsed);
      
      gcDeleteMember.assignActAsSubject(actAsSubject);
      
      gcDeleteMember.assignIncludeGroupDetail(includeGroupDetail);
      gcDeleteMember.assignIncludeSubjectDetail(includeSubjectDetail);
      
      gcDeleteMember.assignFieldName(fieldName);
      gcDeleteMember.assignTxType(GcTransactionType.valueOfIgnoreCase(txType));
  
      for (String subjectAttribute : GrouperClientUtils.nonNull(subjectAttributeNames)) {
        gcDeleteMember.addSubjectAttributeName(subjectAttribute);
      }
      
      WsDeleteMemberResults wsDeleteMemberResults = gcDeleteMember.execute();
      
      StringBuilder result = new StringBuilder();
      int index = 0;
      
      Map<String, Object> substituteMap = new LinkedHashMap<String, Object>();
  
      substituteMap.put("wsDeleteMemberResults", wsDeleteMemberResults);
  
      String outputTemplate = null;
  
      if (argMap.containsKey("outputTemplate")) {
        outputTemplate = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "outputTemplate", true);
        outputTemplate = GrouperClientUtils.substituteCommonVars(outputTemplate);
      } else {
        outputTemplate = GrouperClientUtils.propertiesValue("webService.deleteMember.output", true);
      }
  
      for (WsDeleteMemberResult wsDeleteMemberResult : wsDeleteMemberResults.getResults()) {
        
        substituteMap.put("index", index);
        substituteMap.put("wsDeleteMemberResult", wsDeleteMemberResult);
        
  //          result.append("Index " + index + ": success: " + wsDeleteMemberResult.getResultMetadata().getSuccess()
  //              + ": code: " + wsDeleteMemberResult.getResultMetadata().getResultCode() + ": " 
  //              + wsDeleteMemberResult.getWsSubject().getId() + "\n");
        
        String output = GrouperClientUtils.substituteExpressionLanguage(outputTemplate, substituteMap);
        result.append(output);
        
        index++;
      }
      
      return result.toString();
    }

  /**
   * @param argMap
   * @param argMapNotUsed
   * @return result
   */
  private static String getMembers(Map<String, String> argMap,
      Map<String, String> argMapNotUsed) {
    List<String> groupNames = GrouperClientUtils.argMapList(argMap, argMapNotUsed, "groupNames", true);
    String fieldName = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "fieldName", false);
  
    String memberFilter = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "memberFilter", false);
  
    Boolean includeGroupDetail = GrouperClientUtils.argMapBoolean(argMap, argMapNotUsed, "includeGroupDetail");
    
    Boolean includeSubjectDetail = GrouperClientUtils.argMapBoolean(argMap, argMapNotUsed, "includeSubjectDetail");
  
    Set<String> subjectAttributeNames = GrouperClientUtils.argMapSet(argMap, argMapNotUsed, "subjectAttributeNames", false);
  
    GcGetMembers gcGetMembers = new GcGetMembers();        
  
    String clientVersion = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "clientVersion", false);
    gcGetMembers.assignClientVersion(clientVersion);

    for (String groupName: groupNames) {
      gcGetMembers.addGroupName(groupName);
    }
    
    WsSubjectLookup actAsSubject = retrieveActAsSubjectFromArgs(argMap, argMapNotUsed);
    
    gcGetMembers.assignActAsSubject(actAsSubject);
    
    gcGetMembers.assignMemberFilter(WsMemberFilter.valueOfIgnoreCase(memberFilter));
  
    gcGetMembers.assignIncludeGroupDetail(includeGroupDetail);
    gcGetMembers.assignIncludeSubjectDetail(includeSubjectDetail);
    
    gcGetMembers.assignFieldName(fieldName);
  
    for (String subjectAttribute : GrouperClientUtils.nonNull(subjectAttributeNames)) {
      gcGetMembers.addSubjectAttributeName(subjectAttribute);
    }
    
    List<WsParam> params = retrieveParamsFromArgs(argMap, argMapNotUsed);
    
    for (WsParam param : params) {
      gcGetMembers.addParam(param);
    }
    
    WsGetMembersResults wsGetMembersResults = gcGetMembers.execute();
    
    StringBuilder result = new StringBuilder();
    int groupIndex = 0;
    
    Map<String, Object> substituteMap = new LinkedHashMap<String, Object>();
  
    substituteMap.put("wsGetMembersResults", wsGetMembersResults);
  
    String outputTemplate = null;
  
    if (argMap.containsKey("outputTemplate")) {
      outputTemplate = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "outputTemplate", true);
      outputTemplate = GrouperClientUtils.substituteCommonVars(outputTemplate);
    } else {
      outputTemplate = GrouperClientUtils.propertiesValue("webService.getMembers.output", true);
    }
  
    for (WsGetMembersResult wsGetMembersResult : GrouperClientUtils.nonNull(wsGetMembersResults.getResults(), WsGetMembersResult.class)) {
      
      substituteMap.put("groupIndex", groupIndex);
      substituteMap.put("wsGetMembersResult", wsGetMembersResult);
      
      int subjectIndex = 0;
      for (WsSubject wsSubject : GrouperClientUtils.nonNull(wsGetMembersResult.getWsSubjects(), WsSubject.class)) {
        
        substituteMap.put("subjectIndex", subjectIndex);
        substituteMap.put("wsSubject", wsSubject);
        
        String output = GrouperClientUtils.substituteExpressionLanguage(outputTemplate, substituteMap);
        result.append(output);
        
        subjectIndex++;
      }
      
      groupIndex++;
    }
    
    return result.toString();
  }

  /**
   * @param argMap
   * @param argMapNotUsed
   * @return result
   */
  private static String getGroups(Map<String, String> argMap,
      Map<String, String> argMapNotUsed) {
   
    String memberFilter = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "memberFilter", false);

    Boolean includeGroupDetail = GrouperClientUtils.argMapBoolean(argMap, argMapNotUsed, "includeGroupDetail");
    
    Boolean includeSubjectDetail = GrouperClientUtils.argMapBoolean(argMap, argMapNotUsed, "includeSubjectDetail");

    Set<String> subjectAttributeNames = GrouperClientUtils.argMapSet(argMap, argMapNotUsed, "subjectAttributeNames", false);

    GcGetGroups gcGetGroups = new GcGetGroups();        

    String clientVersion = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "clientVersion", false);
    gcGetGroups.assignClientVersion(clientVersion);

    WsSubjectLookup actAsSubject = retrieveActAsSubjectFromArgs(argMap, argMapNotUsed);
    
    gcGetGroups.assignActAsSubject(actAsSubject);
    
    gcGetGroups.assignMemberFilter(WsMemberFilter.valueOfIgnoreCase(memberFilter));

    gcGetGroups.assignIncludeGroupDetail(includeGroupDetail);
    gcGetGroups.assignIncludeSubjectDetail(includeSubjectDetail);
    
    List<WsSubjectLookup> wsSubjectLookupList = retrieveSubjectsFromArgs(argMap, argMapNotUsed);
    
    for (WsSubjectLookup wsSubjectLookup : wsSubjectLookupList) {
      gcGetGroups.addSubjectLookup(wsSubjectLookup);
    }
    
    for (String subjectAttribute : GrouperClientUtils.nonNull(subjectAttributeNames)) {
      gcGetGroups.addSubjectAttributeName(subjectAttribute);
    }
    
    List<WsParam> params = retrieveParamsFromArgs(argMap, argMapNotUsed);
    
    for (WsParam param : params) {
      gcGetGroups.addParam(param);
    }
    
    WsGetGroupsResults wsGetGroupsResults = gcGetGroups.execute();
    
    StringBuilder result = new StringBuilder();
    int subjectIndex = 0;
    
    Map<String, Object> substituteMap = new LinkedHashMap<String, Object>();

    substituteMap.put("wsGetGroupsResults", wsGetGroupsResults);

    String outputTemplate = null;

    if (argMap.containsKey("outputTemplate")) {
      outputTemplate = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "outputTemplate", true);
      outputTemplate = GrouperClientUtils.substituteCommonVars(outputTemplate);
    } else {
      outputTemplate = GrouperClientUtils.propertiesValue("webService.getGroups.output", true);
    }

    for (WsGetGroupsResult wsGetGroupsResult : GrouperClientUtils.nonNull(wsGetGroupsResults.getResults(), WsGetGroupsResult.class)) {
      
      substituteMap.put("subjectIndex", subjectIndex);
      substituteMap.put("wsGetGroupsResult", wsGetGroupsResult);
      
      int groupIndex = 0;
      for (WsGroup wsGroup : GrouperClientUtils.nonNull(wsGetGroupsResult.getWsGroups(), WsGroup.class)) {
        
        substituteMap.put("groupIndex", groupIndex);
        substituteMap.put("wsGroup", wsGroup);
        
        String output = GrouperClientUtils.substituteExpressionLanguage(outputTemplate, substituteMap);
        result.append(output);
        
        groupIndex++;
      }
      
      subjectIndex++;
    }
    
    return result.toString();
  }

  /**
   * @param argMap
   * @param argMapNotUsed
   * @return the lookup
   */
  private static WsSubjectLookup retrieveActAsSubjectFromArgs(Map<String, String> argMap,
      Map<String, String> argMapNotUsed) {
    
    String argMapActAsSubjectIdKey = "actAsSubjectId";
    
    //see if we have an alias
    {
      String aliasSubjectId = GrouperClientUtils.propertiesValue(
          "grouperClient.alias.SubjectId", false);
      
      if (!GrouperClientUtils.isBlank(aliasSubjectId)) {
        String aliasKey = "actAs" + aliasSubjectId;
        boolean containsAliasKey = argMap.containsKey(aliasKey);
        if (argMap.containsKey(argMapActAsSubjectIdKey) && containsAliasKey) {
          throw new RuntimeException("You cannot pass both arguments actAsSubjectId and " + aliasKey + ", choose one or the other");
        }
        argMapActAsSubjectIdKey = containsAliasKey ? aliasKey : argMapActAsSubjectIdKey;
      }
    }    
    
    String argMapActAsSubjectIdentifierKey = "actAsSubjectIdentifier";
    {
      String aliasSubjectIdentifier = GrouperClientUtils.propertiesValue(
          "grouperClient.alias.SubjectIdentifier", false);
      
      if (!GrouperClientUtils.isBlank(aliasSubjectIdentifier)) {
        String aliasKey = "actAs" + aliasSubjectIdentifier;
        boolean containsAliasKey = argMap.containsKey(aliasKey);
        if (argMap.containsKey(argMapActAsSubjectIdentifierKey) && containsAliasKey) {
          throw new RuntimeException("You cannot pass both arguments actAsSubjectIdentifier and " + aliasKey + ", choose one or the other");
        }
        argMapActAsSubjectIdentifierKey = containsAliasKey ? aliasKey : argMapActAsSubjectIdentifierKey;
      }
    }
    
    // set the act as id
    String actAsSubjectId = GrouperClientUtils.argMapString(argMap, argMapNotUsed, argMapActAsSubjectIdKey, false);
    String actAsSubjectIdentifier = GrouperClientUtils.argMapString(argMap, argMapNotUsed, argMapActAsSubjectIdentifierKey, false);
    String actAsSubjectSource = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "actAsSubjectSource", false);
    
    if (GrouperClientUtils.isBlank(actAsSubjectId) 
        && GrouperClientUtils.isBlank(actAsSubjectIdentifier)
        && GrouperClientUtils.isBlank(actAsSubjectSource)) {
      return null;
    }
    
    WsSubjectLookup actAsSubject = new WsSubjectLookup(actAsSubjectId, actAsSubjectIdentifier, actAsSubjectSource);
    return actAsSubject;
  }

  /**
   * retrieve params from args
   * @param argMap
   * @param argMapNotUsed
   * @return the list of params or empty list if none
   */
  private static List<WsParam> retrieveParamsFromArgs(
      Map<String, String> argMap, Map<String, String> argMapNotUsed) {

    List<WsParam> params = new ArrayList<WsParam>();
    int index = 0;
    while (true) {

      String argName = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "paramName" + index, false);
      if (GrouperClientUtils.isBlank(argName)) {
        break;
      }
      String argValue = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "paramValue" + index, true);
      params.add(new WsParam(argName, argValue));
      index++;
    }
    return params;
  }
  
  /**
   * @param argMap
   * @param argMapNotUsed
   * @return the subjects
   */
  private static List<WsSubjectLookup> retrieveSubjectsFromArgs(
      Map<String, String> argMap, Map<String, String> argMapNotUsed) {
    
    
    String argMapSubjectIdsKey = "subjectIds";
    
    //see if we have an alias
    {
      String aliasSubjectIds = GrouperClientUtils.propertiesValue(
          "grouperClient.alias.subjectIds", false);
      
      if (!GrouperClientUtils.isBlank(aliasSubjectIds)) {
        boolean containsAliasKey = argMap.containsKey(aliasSubjectIds);
        if (argMap.containsKey(argMapSubjectIdsKey) && containsAliasKey) {
          throw new RuntimeException("You cannot pass both arguments subjectIds and " + aliasSubjectIds + ", choose one or the other");
        }
        argMapSubjectIdsKey = containsAliasKey ? aliasSubjectIds : argMapSubjectIdsKey;
      }
    }    

    String argMapSubjectIdentifiersKey = "subjectIdentifiers";
    
    //see if we have an alias
    {
      String aliasSubjectIdentifiers = GrouperClientUtils.propertiesValue(
          "grouperClient.alias.subjectIdentifiers", false);
      
      if (!GrouperClientUtils.isBlank(aliasSubjectIdentifiers)) {
        boolean containsAliasKey = argMap.containsKey(aliasSubjectIdentifiers);
        if (argMap.containsKey(argMapSubjectIdentifiersKey) && containsAliasKey) {
          throw new RuntimeException("You cannot pass both arguments subjectIdentifiers and " + aliasSubjectIdentifiers + ", choose one or the other");
        }
        argMapSubjectIdentifiersKey = containsAliasKey ? aliasSubjectIdentifiers : argMapSubjectIdentifiersKey;
      }
    }    
    
    String argMapSubjectIdsFileKey = "subjectIdsFile";
    
    //see if we have an alias
    {
      String aliasSubjectIds = GrouperClientUtils.propertiesValue(
          "grouperClient.alias.subjectIds", false);
      
      if (!GrouperClientUtils.isBlank(aliasSubjectIds)) {
        String aliasSubjectIdsFile = aliasSubjectIds + "File";
        boolean containsAliasKey = argMap.containsKey(aliasSubjectIdsFile);
        if (argMap.containsKey(argMapSubjectIdsFileKey) && containsAliasKey) {
          throw new RuntimeException("You cannot pass both arguments subjectIdsFile and " 
              + aliasSubjectIdsFile + ", choose one or the other");
        }
        argMapSubjectIdsFileKey = containsAliasKey ? aliasSubjectIdsFile : argMapSubjectIdsFileKey;
      }
    }    

    String argMapSubjectIdentifiersFileKey = "subjectIdentifiersFile";
    
    //see if we have an alias
    {
      String aliasSubjectIdentifiers = GrouperClientUtils.propertiesValue(
          "grouperClient.alias.subjectIdentifiers", false);
      
      if (!GrouperClientUtils.isBlank(aliasSubjectIdentifiers)) {
        String aliasSubjectIdentifiersFile = aliasSubjectIdentifiers + "File";
        boolean containsAliasKey = argMap.containsKey(aliasSubjectIdentifiersFile);
        if (argMap.containsKey(argMapSubjectIdentifiersFileKey) && containsAliasKey) {
          throw new RuntimeException("You cannot pass both arguments subjectIdentifiersFile and " 
              + aliasSubjectIdentifiersFile + ", choose one or the other");
        }
        argMapSubjectIdentifiersFileKey = containsAliasKey ? aliasSubjectIdentifiersFile : argMapSubjectIdentifiersFileKey;
      }
    }    
    
    List<String> subjectIdsList = GrouperClientUtils.argMapList(argMap, argMapNotUsed, argMapSubjectIdsKey, false);
    
    List<String> subjectIdentifiersList = GrouperClientUtils.argMapList(argMap, argMapNotUsed, 
        argMapSubjectIdentifiersKey, false);
    
    List<String> sourceIdsList = GrouperClientUtils.argMapList(argMap, argMapNotUsed, "sourceIds", false);
    
    String defaultSubjectSource = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "defaultSubjectSource", false);
    
    // add two subjects to the group
    int subjectIdLength = GrouperClientUtils.length(subjectIdsList);
    int subjectIdentifierLength = GrouperClientUtils.length(subjectIdentifiersList);
    int sourceIdLength = GrouperClientUtils.length(sourceIdsList);
    
    if (argMap.containsKey(argMapSubjectIdsFileKey)) {
      if (subjectIdLength > 0) {
        throw new RuntimeException("Cant pass in " + argMapSubjectIdsKey + " and " 
            + argMapSubjectIdsFileKey + ", use one or the other");
      }
      subjectIdsList = GrouperClientUtils.argMapFileList(argMap, argMapNotUsed, argMapSubjectIdsFileKey, true);
      subjectIdLength = GrouperClientUtils.length(subjectIdsList);
    }
    
    if (argMap.containsKey(argMapSubjectIdentifiersFileKey)) {
      if (subjectIdentifierLength > 0) {
        throw new RuntimeException("Cant pass in " + argMapSubjectIdentifiersKey 
            + " and " + argMapSubjectIdentifiersFileKey + ", use one or the other");
      }
      subjectIdentifiersList = GrouperClientUtils.argMapFileList(argMap, argMapNotUsed, 
          argMapSubjectIdentifiersFileKey, true);
      subjectIdentifierLength = GrouperClientUtils.length(subjectIdentifiersList);
    }
    
    if (argMap.containsKey("sourceIdsFile")) {
      if (sourceIdLength > 0) {
        throw new RuntimeException("Cant pass in sourceIds and sourceIdsFile, use one or the other");
      }
      sourceIdsList = GrouperClientUtils.argMapFileList(argMap, argMapNotUsed, "sourceIdsFile", true);
      sourceIdLength = GrouperClientUtils.length(sourceIdsList);
    }
    
    if (!GrouperClientUtils.isBlank(defaultSubjectSource) && sourceIdLength > 0) {
      throw new RuntimeException("Cant specify a default subject source, and source ids");
    }
    
    if (subjectIdLength == 0 && subjectIdentifierLength == 0) {
      throw new RuntimeException("Cant pass no subject ids and no subject identifiers!");
    }
    if (subjectIdLength != 0 && subjectIdentifierLength != 0) {
      throw new RuntimeException("Cant pass subject ids and subject identifiers! (pass one of the other)");
    }
    
    if (sourceIdLength > 0 && sourceIdLength != subjectIdLength 
        && sourceIdLength != subjectIdentifierLength) {
      throw new RuntimeException("If source ids are passed in, you " +
          "must pass the same number as subjectIds or subjectIdentifiers");
    }
    
    int subjectsLength = Math.max(subjectIdLength, subjectIdentifierLength);
    List<WsSubjectLookup> wsSubjectLookupList = new ArrayList<WsSubjectLookup>();
    
    for (int i=0;i<subjectsLength;i++) {
      WsSubjectLookup wsSubjectLookup = new WsSubjectLookup();
      if (subjectIdLength > 0) {
        wsSubjectLookup.setSubjectId(subjectIdsList.get(i));
      }
      if (subjectIdentifierLength > 0) {
        wsSubjectLookup.setSubjectIdentifier(subjectIdentifiersList.get(i));
      }
      if (sourceIdLength > 0) {
        wsSubjectLookup.setSubjectSourceId(sourceIdsList.get(i));
      }
      if (!GrouperClientUtils.isBlank(defaultSubjectSource)) {
        wsSubjectLookup.setSubjectSourceId(defaultSubjectSource);
      }
      wsSubjectLookupList.add(wsSubjectLookup);
    }
    return wsSubjectLookupList;
  }

  /**
   * print usage and exit
   */
  public static void usage() {
    //read in the usage file
    String usage = GrouperClientUtils.readResourceIntoString("grouper.client.usage.txt", false);
    System.err.println(usage);
    if (exitOnError) {
      System.exit(1);
    }
    throw new RuntimeException("Invalid usage");
  }

}
