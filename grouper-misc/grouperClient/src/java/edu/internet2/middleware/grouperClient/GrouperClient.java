/*
 * @author mchyzer
 * $Id: GrouperClient.java,v 1.2 2008-11-28 23:45:27 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient;

import java.util.LinkedHashMap;
import java.util.Map;

import edu.internet2.middleware.grouperClient.api.GcLdapSearchAttribute;
import edu.internet2.middleware.grouperClient.commandLine.GcLdapSearchAttributeConfig;
import edu.internet2.middleware.grouperClient.commandLine.GcLdapSearchAttributeConfig.SearchAttributeResultType;
import edu.internet2.middleware.grouperClient.ext.edu.internet2.middleware.morphString.Crypto;
import edu.internet2.middleware.grouperClient.ext.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


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
        gcLdapSearchAttributeConfig.setLdapName(GrouperClientUtils.propertiesValue("ldapSearchAttribute.operationName." + i, true));

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
          //ldapSearchAttribute.returningAttributeLabels.2 = pennname
          String[] returningAttributeLabels = GrouperClientUtils.splitTrim(
              GrouperClientUtils.propertiesValue("ldapSearchAttribute.returningAttributeLabels." + i, true), ",");
          String[] ldapReturningAttributes = GrouperClientUtils.splitTrim(
              GrouperClientUtils.propertiesValue("ldapSearchAttribute.returningAttributes." + i, true), ",");
          
          if (returningAttributeLabels.length != ldapReturningAttributes.length) {
            throw new RuntimeException("ldapSearchAttribute #" + i + " operation: " + operationName
                + " should have the same number of returningAttributeLabels " 
                + returningAttributeLabels.length + " and returningAttributes " + ldapReturningAttributes.length);
          }
              
          for (int j=0;j<returningAttributeLabels.length;j++) {
            gcLdapSearchAttributeConfig.addReturningAttribute(returningAttributeLabels[j], ldapReturningAttributes[j]);
          }
        }
        

        //ldapSearchAttribute.resultType.2 = string
        gcLdapSearchAttributeConfig.setSearchAttributeResultType(GrouperClientUtils.propertiesValue("ldapSearchAttribute.operationName." + i, true));

        
        ldapOperations.put(operationName, gcLdapSearchAttributeConfig);
        i++;
      }
      
    }
    return ldapOperations;
  }
  
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
      
      String operation = GrouperClientUtils.argMapString(argMap, "operation", true);
      
      if (GrouperClientUtils.equals(operation, "encryptPassword")) {
        
        boolean dontMask = GrouperClientUtils.argMapBoolean(argMap, "dontMask", false, false);
        
        String encryptKey = GrouperClientUtils.propertiesValue("encrypt.key", true);
        
        boolean disableExternalFileLookup = GrouperClientUtils.propertiesValueBoolean(
            "encrypt.disableExternalFileLookup", false, true);
        
        //lets lookup if file
        encryptKey = GrouperClientUtils.readFromFileIfFile(encryptKey, disableExternalFileLookup);
        
        //lets get the password from stdin
        String password = GrouperClientUtils.retrievePasswordFromStdin(dontMask, 
            "Type the string to encrypt (note: pasting might echo it back): ");
        
        String encrypted = new Crypto(encryptKey).encrypt(password);
        
        System.out.println("Encrypted password: " + encrypted);
        
      } else if (ldapOperations().containsKey(operation)) {
        
        //ldap operation
        GcLdapSearchAttributeConfig gcLdapSearchAttributeConfig = ldapOperations().get(operation);
        
        GcLdapSearchAttribute gcLdapSearchAttribute = new GcLdapSearchAttribute();
        gcLdapSearchAttribute.assignLdapName(gcLdapSearchAttributeConfig.getLdapName());
        
        //go through the matching attributes and get from command line
        for (String matchingAttributeLabel : gcLdapSearchAttributeConfig.getMatchingAttributes().keySet()) {
          String matchingAttributeValue = GrouperClientUtils.argMapString(argMap, matchingAttributeLabel, true);
          gcLdapSearchAttribute.addMatchingAttribute(
              gcLdapSearchAttributeConfig.getMatchingAttributes().get(matchingAttributeLabel), matchingAttributeValue);
        }
        
        //go through the returning attributes and assign to query
        for (String returningAttributeLabel : gcLdapSearchAttributeConfig.getReturningAttributes().keySet()) {
          gcLdapSearchAttribute.addReturningAttribute(
              gcLdapSearchAttributeConfig.getReturningAttributes().get(returningAttributeLabel));
        }
        
        gcLdapSearchAttribute.execute();
        
        SearchAttributeResultType searchAttributeResultType = gcLdapSearchAttributeConfig.getSearchAttributeResultTypeEnum();
        
        String results = searchAttributeResultType.processOutput(gcLdapSearchAttributeConfig, gcLdapSearchAttribute);
        
        //this already has a newline on it
        System.out.print(results);
        
      } else {
        usage();
      }
    } catch (Exception e) {
      System.err.println(e.getMessage());
      log.fatal(e);
      System.exit(1);
    }
  }

  /**
   * print usage and exit
   */
  public static void usage() {
    //read in the usage file
    String usage = GrouperClientUtils.readResourceIntoString("grouper.client.usage.txt", false);
    System.err.println(usage);
    System.exit(1);
  }

}
