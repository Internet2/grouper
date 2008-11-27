/*
 * @author mchyzer
 * $Id: GrouperClient.java,v 1.1 2008-11-27 14:25:48 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient;

import java.util.Map;

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
