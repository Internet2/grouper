/*
 * @author mchyzer
 * $Id: GrouperClient.java,v 1.3 2008-11-30 10:57:19 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouperClient.api.GcAddMember;
import edu.internet2.middleware.grouperClient.api.GcLdapSearchAttribute;
import edu.internet2.middleware.grouperClient.commandLine.GcLdapSearchAttributeConfig;
import edu.internet2.middleware.grouperClient.commandLine.GcLdapSearchAttributeConfig.SearchAttributeResultType;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
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
      } else if (GrouperClientUtils.equals(operation, "addMember")) {
        result = addMember(argMap, argMapNotUsed);

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
    String subjectIds = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "subjectIds", false);
    List<String> subjectIdsList = GrouperClientUtils.splitTrimToList(subjectIds, ","); 
    String subjectIdentifiers = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "subjectIdentifiers", false);
    List<String> subjectIdentifiersList = GrouperClientUtils.splitTrimToList(subjectIdentifiers, ",");
    String sourceIds = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "sourceIds", false);
    List<String> sourceIdsList = GrouperClientUtils.splitTrimToList(sourceIds, ",");
    boolean replaceAllExisting = GrouperClientUtils.argMapBoolean(argMap, argMapNotUsed, "replaceAllExisting", false, false);
    // add two subjects to the group
    int subjectIdLength = GrouperClientUtils.length(subjectIdsList);
    int subjectIdentifierLength = GrouperClientUtils.length(subjectIdentifiersList);
    int sourceIdLength = GrouperClientUtils.length(sourceIdsList);
    
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
    GcAddMember gcAddMember = new GcAddMember();        

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
      gcAddMember.addSubjectLookup(wsSubjectLookup);
    }
    
    gcAddMember.assignGroupName(groupName);
    
    // set the act as id
    WsSubjectLookup actAsSubject = new WsSubjectLookup("GrouperSystem", null, null);
    gcAddMember.assignActAsSubject(actAsSubject);
    
    gcAddMember.assignReplaceAllExisting(replaceAllExisting);
    
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
