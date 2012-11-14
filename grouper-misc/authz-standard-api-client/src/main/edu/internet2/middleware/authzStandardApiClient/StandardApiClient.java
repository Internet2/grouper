/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id: StandardApiClient.java,v 1.29 2009-12-30 04:23:02 mchyzer Exp $
 */
package edu.internet2.middleware.authzStandardApiClient;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.internet2.middleware.authzStandardApiClient.api.AsacApiDefaultResource;
import edu.internet2.middleware.authzStandardApiClient.api.AsacApiTestSuite;
import edu.internet2.middleware.authzStandardApiClient.contentType.AsacRestContentType;
import edu.internet2.middleware.authzStandardApiClient.corebeans.AsacDefaultResourceContainer;
import edu.internet2.middleware.authzStandardApiClient.testSuite.AsacTestSuiteResults;
import edu.internet2.middleware.authzStandardApiClient.testSuite.AsacTestSuiteVerbose;
import edu.internet2.middleware.authzStandardApiClient.util.StandardApiClientCommonUtils;
import edu.internet2.middleware.authzStandardApiClient.util.StandardApiClientConfig;
import edu.internet2.middleware.authzStandardApiClient.util.StandardApiClientLog;
import edu.internet2.middleware.authzStandardApiClient.util.StandardApiClientUtils;
import edu.internet2.middleware.authzStandardApiClient.ws.StandardApiClientWs;
import edu.internet2.middleware.authzStandardApiClientExt.edu.internet2.middleware.morphString.Crypto;
import edu.internet2.middleware.authzStandardApiClientExt.org.apache.commons.logging.Log;



/**
 * main class for grouper client.  note, stdout is for output, stderr is for error messages (or logs)
 */
public class StandardApiClient {

  /** timing gate */
  private static long startTime = System.currentTimeMillis();
  
  /**
   * 
   */
  private static Log log = StandardApiClientUtils.retrieveLog(StandardApiClient.class);

  /** should java exit on error? */
  public static boolean exitOnError = true;
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    
    String operation = null;
    try {
      if (StandardApiClientUtils.length(args) == 0) {
        usage();
        return;
      }
      
      //map of all command line args
      Map<String, String> argMap = StandardApiClientUtils.argMap(args);
      
      Map<String, String> argMapNotUsed = new LinkedHashMap<String, String>(argMap);

      boolean debugMode = StandardApiClientUtils.argMapBoolean(argMap, argMapNotUsed, "debug", false, false);
      
      StandardApiClientLog.assignDebugToConsole(debugMode);
      
      //init if not already
      StandardApiClientConfig.retrieveConfig().properties();
      
      //see where log file came from
      StringBuilder callingLog = new StringBuilder();
      StandardApiClientUtils.propertiesFromResourceName("authzStandardApi.client.properties", 
          false, true, StandardApiClientCommonUtils.class, callingLog);
      
      //see if the message about where it came from is
      //log.debug(callingLog.toString());
      
      operation = StandardApiClientUtils.argMapString(argMap, argMapNotUsed, "operation", true);
      
      //where results should go if file
      String saveResultsToFile = StandardApiClientUtils.argMapString(argMap, argMapNotUsed, "saveResultsToFile", false);
      boolean shouldSaveResultsToFile = !StandardApiClientUtils.isBlank(saveResultsToFile);
      
      if (shouldSaveResultsToFile) {
        log.debug("Will save results to file: " + StandardApiClientUtils.fileCanonicalPath(new File(saveResultsToFile)));
      }
      
      String result = null;
      
      if (customOperations().containsKey(operation)) {
        
        Class<ClientOperation> operationClass = customOperations().get(operation);
        ClientOperation clientOperation = StandardApiClientUtils.newInstance(operationClass);
        
        OperationParams operationParams = new OperationParams();
        operationParams.setArgMap(argMap);
        operationParams.setArgMapNotUsed(argMapNotUsed);
        operationParams.setShouldSaveResultsToFile(shouldSaveResultsToFile);
        
        result = clientOperation.operate(operationParams);
        
      } else if (StandardApiClientUtils.equals(operation, "encryptPassword")) {
        
        result = encryptText(argMap, argMapNotUsed, shouldSaveResultsToFile);
        
      } else if (StandardApiClientUtils.equals(operation, "defaultResourceWs")) {
        result = defaultResourceWs(argMap, argMapNotUsed);

      } else if (StandardApiClientUtils.equals(operation, "testSuite")) {
        result = testSuite(argMap, argMapNotUsed);

      } else {
        System.err.println("Error: invalid operation: '" + operation + "', for usage help, run: java -jar grouperClient.jar" );
        if (exitOnError) {
          System.exit(1);
        }
        throw new RuntimeException("Invalid usage");
      }
      
      //this already has a newline on it
      if (shouldSaveResultsToFile) {
        StandardApiClientUtils.saveStringIntoFile(new File(saveResultsToFile), result);
      } else {
        System.out.print(result);
      }

      failOnArgsNotUsed(argMapNotUsed);
      
    } catch (Exception e) {
      System.err.println("Error with authz standard api client, check the logs: " + e.getMessage());
      log.fatal(e.getMessage(), e);
      if (exitOnError) {
        System.exit(1);
      }
      if (e instanceof RuntimeException) {
        throw (RuntimeException)e;
      }
      throw new RuntimeException(e.getMessage(), e);
    } finally {
      try {
        log.debug("Elapsed time: " + (System.currentTimeMillis() - startTime) + "ms");
      } catch (Exception e) {}
      StandardApiClientLog.assignDebugToConsole(false);
    }
    
  }

  /**
   * @param argMapNotUsed
   */
  public static void failOnArgsNotUsed(Map<String, String> argMapNotUsed) {
    if (argMapNotUsed.size() > 0) {
      boolean failOnExtraParams = StandardApiClientConfig.retrieveConfig().propertyValueBooleanRequired(
          "grouperClient.failOnExtraCommandLineArgs");
      String error = "Invalid command line arguments: " + argMapNotUsed.keySet();
      if (failOnExtraParams) {
        throw new RuntimeException(error);
      }
      log.error(error);
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
    boolean dontMask = StandardApiClientUtils.argMapBoolean(argMap, argMapNotUsed, "dontMask", false, false);
    
    String encryptKey = StandardApiClientUtils.encryptKey();
    
    //lets get the password from stdin
    String password = StandardApiClientUtils.retrievePasswordFromStdin(dontMask, 
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
     * @return result
     */
    private static String defaultResourceWs(Map<String, String> argMap,
        Map<String, String> argMapNotUsed) {
      
      AsacApiDefaultResource asacApiDefaultResource = new AsacApiDefaultResource();        
      
      String formatString = StandardApiClientUtils.argMapString(argMap, argMapNotUsed, "format", false);
      
      AsacRestContentType asacRestContentType = AsacRestContentType.valueOfIgnoreCase(formatString, false);
      
      if (asacRestContentType != null) {
        asacApiDefaultResource.setContentType(asacRestContentType);
      }
      
      boolean indent = StandardApiClientUtils.argMapBoolean(argMap, argMapNotUsed, "indent", false, false);
      
      asacApiDefaultResource.setIndent(indent);

      //register that we will use this
      StandardApiClientUtils.argMapString(argMap, argMapNotUsed, "outputTemplate", false);
      failOnArgsNotUsed(argMapNotUsed);
  
      AsacDefaultResourceContainer asacDefaultResourceContainer = asacApiDefaultResource.execute();
      
      StringBuilder result = new StringBuilder();
      
      Map<String, Object> substituteMap = new LinkedHashMap<String, Object>();
  
      substituteMap.put("asacDefaultResourceContainer", asacDefaultResourceContainer);
      substituteMap.put("asacDefaultResource", asacDefaultResourceContainer.getAsasDefaultResource());
      substituteMap.put("meta", asacDefaultResourceContainer.getMeta());
      substituteMap.put("responseMeta", asacDefaultResourceContainer.getResponseMeta());
      substituteMap.put("serviceMeta", asacDefaultResourceContainer.getServiceMeta());
  
      String outputTemplate = null;
  
      if (argMap.containsKey("outputTemplate")) {
        outputTemplate = StandardApiClientUtils.argMapString(argMap, argMapNotUsed, "outputTemplate", true);
        outputTemplate = StandardApiClientUtils.substituteCommonVars(outputTemplate);
      } else {
        outputTemplate = StandardApiClientConfig.retrieveConfig().propertyValueStringRequired("webService.defaultResource.output");
      }
      log.debug("Output template: " + StandardApiClientUtils.trim(outputTemplate) + ", available variables: asacDefaultResourceContainer, " +
        "asacDefaultResource, meta, responseMeta, serviceMeta");

      
      String output = StandardApiClientUtils.substituteExpressionLanguage(outputTemplate, substituteMap);
      result.append(output);
      
      return result.toString();
    }

  /**
     * @param argMap
     * @param argMapNotUsed
     * @return result
     */
    private static String sendFile(Map<String, String> argMap,
        Map<String, String> argMapNotUsed) {
      
      String clientVersion = StandardApiClientUtils.argMapString(argMap, argMapNotUsed, "clientVersion", false);
      
      String fileContents = StandardApiClientUtils.argMapString(argMap, argMapNotUsed, "fileContents", false);
      
      String theFileName = "[contents on command line]";
      if (StandardApiClientUtils.isBlank(fileContents)) {
        String fileName = StandardApiClientUtils.argMapString(argMap, argMapNotUsed, "fileName", true);

        fileContents = StandardApiClientUtils.readFileIntoString(new File(fileName));
        
        theFileName = StandardApiClientUtils.fileCanonicalPath(new File(fileName));
      }
      
      if (fileContents.startsWith("POST") || fileContents.startsWith("GET")
          || fileContents.startsWith("PUT") || fileContents.startsWith("DELETE")
          || fileContents.startsWith("Connection:")) {
        throw new RuntimeException("The file is detected as containing HTTP headers, it should only contain the payload (e.g. the XML): " + theFileName);
      }
      
      String urlSuffix = StandardApiClientUtils.argMapString(argMap, argMapNotUsed, "urlSuffix", true);

      //this is part of the log file if logging output
      String labelForLog = StandardApiClientUtils.argMapString(argMap, argMapNotUsed, "labelForLog", false);
      
      labelForLog = StandardApiClientUtils.defaultIfBlank(labelForLog, "sendFile");
      
      boolean indentOutput = StandardApiClientUtils.argMapBoolean(argMap, argMapNotUsed, "indentOutput", false, true);
      
      String contentType = StandardApiClientUtils.argMapString(argMap, argMapNotUsed, "contentType", false);

      failOnArgsNotUsed(argMapNotUsed);
      
      StandardApiClientWs<AsacDefaultResourceContainer> grouperClientWs 
        = new StandardApiClientWs<AsacDefaultResourceContainer>();
      
      if (StandardApiClientUtils.isNotBlank(contentType)) {
        
      }
      
      try {
        //assume the url suffix is already escaped...
        String results = (String)(Object)grouperClientWs.executeService(urlSuffix, 
            fileContents, labelForLog, clientVersion, AsacRestContentType.json, null, null);

        if (indentOutput) {
          results = StandardApiClientUtils.indent(results, false);
        }
        
        return results;
        
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

  /**
   * print usage and exit
   */
  public static void usage() {
    //read in the usage file
    String usage = StandardApiClientUtils.readResourceIntoString("authzStandardApi.client.usage.txt", StandardApiClientCommonUtils.class);
    System.err.println(usage);
  }


  /**
   * get custom operation classes configured in the authzStandardApi.client.properties
   * @return the map of operations
   */
  @SuppressWarnings({ "unchecked", "cast" })
  private static Map<String, Class<ClientOperation>> customOperations() {
    
    if (customOperations == null) {
      
      customOperations = new LinkedHashMap<String, Class<ClientOperation>>();
      
      int i=0;
      String operationName = null;
      while (true) {
        operationName = null;
        operationName = StandardApiClientConfig.retrieveConfig().propertyValueString("customOperation.name." + i);
        if (StandardApiClientUtils.isBlank(operationName)) {
          break;
        }
        if (customOperations.containsKey(operationName)) {
          throw new RuntimeException("There is a custom operation defined twice in authzStandardApi.client.properties: '" + operationName + "'");
        }
        try {
  
          String operationClassName = StandardApiClientConfig.retrieveConfig().propertyValueStringRequired("customOperation.class." + i);
          Class<ClientOperation> operationClass = (Class<ClientOperation>)StandardApiClientUtils.forName(operationClassName);
          customOperations.put(operationName, operationClass);
  
        } catch (RuntimeException re) {
          throw new RuntimeException("Problem with custom operation: " + operationName, re);
        }
        i++;
      }
    }
    
    return customOperations;
    
  }


  /** custom operations from config file */
  private static Map<String, Class<ClientOperation>> customOperations = null;

  /**
   * run non-destructive tests against the server
   * @param argMap
   * @param argMapNotUsed
   * @return result
   */
  private static String testSuite(Map<String, String> argMap,
      Map<String, String> argMapNotUsed) {
    
    String verboseString = StandardApiClientUtils.argMapString(argMap, argMapNotUsed, "verbose", false);
    
    AsacTestSuiteVerbose asacTestSuiteVerbose = AsacTestSuiteVerbose.valueOfIgnoreCase(verboseString, false);
    asacTestSuiteVerbose = StandardApiClientUtils.defaultIfNull(asacTestSuiteVerbose, AsacTestSuiteVerbose.medium);

    AsacApiTestSuite asacApiTestSuite = new AsacApiTestSuite();        
    
    asacApiTestSuite.assignVerbose(asacTestSuiteVerbose);
      
    failOnArgsNotUsed(argMapNotUsed);
  
    AsacTestSuiteResults asacTestSuiteResults = asacApiTestSuite.execute();
    
    StringBuilder result = new StringBuilder();
    
    Map<String, Object> substituteMap = new LinkedHashMap<String, Object>();
  
    substituteMap.put("asacTestSuiteResults", asacTestSuiteResults);
  
    String outputTemplate = null;
  
    if (argMap.containsKey("outputTemplate")) {
      outputTemplate = StandardApiClientUtils.argMapString(argMap, argMapNotUsed, "outputTemplate", true);
      outputTemplate = StandardApiClientUtils.substituteCommonVars(outputTemplate);
    } else {
      outputTemplate = StandardApiClientConfig.retrieveConfig().propertyValueStringRequired("webService.testSuite.output");
    }
    log.debug("Output template: " + StandardApiClientUtils.trim(outputTemplate) + ", available variables: asacTestSuiteResults");
  
    
    String output = StandardApiClientUtils.substituteExpressionLanguage(outputTemplate, substituteMap);
    result.append(output);
    
    return result.toString();
  }

}
