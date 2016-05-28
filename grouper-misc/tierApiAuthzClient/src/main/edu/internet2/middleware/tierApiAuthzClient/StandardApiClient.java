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
package edu.internet2.middleware.tierApiAuthzClient;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.morphString.Crypto;
import edu.internet2.middleware.tierApiAuthzClient.api.AsacApiDefaultResource;
import edu.internet2.middleware.tierApiAuthzClient.api.AsacApiDefaultVersionResource;
import edu.internet2.middleware.tierApiAuthzClient.api.AsacApiFolderSave;
import edu.internet2.middleware.tierApiAuthzClient.api.AsacApiTestSuite;
import edu.internet2.middleware.tierApiAuthzClient.api.AsacApiVersionResource;
import edu.internet2.middleware.tierApiAuthzClient.contentType.AsacRestContentType;
import edu.internet2.middleware.tierApiAuthzClient.corebeans.AsacDefaultResourceContainer;
import edu.internet2.middleware.tierApiAuthzClient.corebeans.AsacDefaultVersionResourceContainer;
import edu.internet2.middleware.tierApiAuthzClient.corebeans.AsacFolderLookup;
import edu.internet2.middleware.tierApiAuthzClient.corebeans.AsacFolderSaveResponse;
import edu.internet2.middleware.tierApiAuthzClient.corebeans.AsacSaveMode;
import edu.internet2.middleware.tierApiAuthzClient.corebeans.AsacVersionResourceContainer;
import edu.internet2.middleware.tierApiAuthzClient.testSuite.AsacTestSuiteResults;
import edu.internet2.middleware.tierApiAuthzClient.testSuite.AsacTestSuiteVerbose;
import edu.internet2.middleware.tierApiAuthzClient.util.StandardApiClientCommonUtils;
import edu.internet2.middleware.tierApiAuthzClient.util.StandardApiClientConfig;
import edu.internet2.middleware.tierApiAuthzClient.util.StandardApiClientLog;
import edu.internet2.middleware.tierApiAuthzClient.util.StandardApiClientUtils;
import edu.internet2.middleware.tierApiAuthzClient.ws.StandardApiClientWs;
import org.apache.commons.logging.Log;


/**
 * <pre>
 * main class for grouper client.  note, stdout is for output, stderr is for error messages (or logs)
 * 
 * --operation=testSuite --verbose=high
 * 
 * </pre>
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

      } else if (StandardApiClientUtils.equals(operation, "defaultVersionResourceWs")) {
        result = defaultVersionResourceWs(argMap, argMapNotUsed);

      } else if (StandardApiClientUtils.equals(operation, "versionResourceWs")) {
        result = versionResourceWs(argMap, argMapNotUsed);

      } else if (StandardApiClientUtils.equals(operation, "folderSaveWs")) {
        result = folderSaveWs(argMap, argMapNotUsed);

      } else if (StandardApiClientUtils.equals(operation, "testSuite")) {
        result = testSuite(argMap, argMapNotUsed);

      } else {
        System.err.println("Error: invalid operation: '" + operation + "', for usage help, run: java -jar authzStandardApiClient.jar" );
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
          "authzStandardApiClient.failOnExtraCommandLineArgs");
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
      
      boolean indent = StandardApiClientUtils.argMapBoolean(argMap, argMapNotUsed, "indent", false, false);
      
      asacApiDefaultResource.assignIndent(indent);

      //register that we will use this
      StandardApiClientUtils.argMapString(argMap, argMapNotUsed, "outputTemplate", false);
      failOnArgsNotUsed(argMapNotUsed);
  
      AsacDefaultResourceContainer asacDefaultResourceContainer = asacApiDefaultResource.execute();
      
      StringBuilder result = new StringBuilder();
      
      Map<String, Object> substituteMap = new LinkedHashMap<String, Object>();
  
      substituteMap.put("defaultResourceContainer", asacDefaultResourceContainer);
      substituteMap.put("defaultResource", asacDefaultResourceContainer.getDefaultResource());
      substituteMap.put("meta", asacDefaultResourceContainer.getMeta());
      substituteMap.put("responseMeta", asacDefaultResourceContainer.getResponseMeta());
      substituteMap.put("serviceMeta", asacDefaultResourceContainer.getServiceMeta());
  
      String outputTemplate = null;
  
      if (argMap.containsKey("outputTemplate")) {
        outputTemplate = StandardApiClientUtils.argMapString(argMap, argMapNotUsed, "outputTemplate", true);
        outputTemplate = StandardApiClientUtils.substituteCommonVars(outputTemplate);
      } else {
        outputTemplate = StandardApiClientConfig.retrieveConfig().propertyValueStringRequired("webService.defaultResourceWs.output");
      }
      log.debug("Output template: " + StandardApiClientUtils.trim(outputTemplate) + ", available variables: defaultResourceContainer, " +
        "defaultResource, meta, responseMeta, serviceMeta");

      
      String output = StandardApiClientUtils.substituteExpressionLanguage(outputTemplate, substituteMap);
      result.append(output);
      
      return result.toString();
    }

  /**
   * @param argMap
   * @param argMapNotUsed
   * @return result
   */
  @SuppressWarnings("unused")
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
    
    boolean indentOutput = StandardApiClientUtils.argMapBoolean(argMap, argMapNotUsed, "indent", false, true);
    
    String contentType = StandardApiClientUtils.argMapString(argMap, argMapNotUsed, "contentType", false);

    failOnArgsNotUsed(argMapNotUsed);
    
    StandardApiClientWs<AsacDefaultResourceContainer> authzStandardApiClientWs 
      = new StandardApiClientWs<AsacDefaultResourceContainer>();
    
    if (StandardApiClientUtils.isNotBlank(contentType)) {
      
    }
    
    try {
      //assume the url suffix is already escaped...
      String results = (String)(Object)authzStandardApiClientWs.executeService(urlSuffix, 
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
      
    boolean indent = StandardApiClientUtils.argMapBoolean(argMap, argMapNotUsed, "indent", false, false);

    List<String> tests = StandardApiClientUtils.argMapList(argMap, argMapNotUsed, "tests", false);
    
    asacApiTestSuite.addTests(tests);
    
    failOnArgsNotUsed(argMapNotUsed);
    
    AsacTestSuiteResults asacTestSuiteResults = asacApiTestSuite.assignIndent(indent).execute();
    
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

  /**
   * @param argMap
   * @param argMapNotUsed
   * @return result
   */
  private static String defaultVersionResourceWs(Map<String, String> argMap,
      Map<String, String> argMapNotUsed) {
    
    AsacApiDefaultVersionResource asacApiDefaultVersionResource = new AsacApiDefaultVersionResource();        
    
    String formatString = StandardApiClientUtils.argMapString(argMap, argMapNotUsed, "format", false);
    
    AsacRestContentType asacRestContentType = AsacRestContentType.valueOfIgnoreCase(formatString, false);
    
    if (asacRestContentType != null) {
      asacApiDefaultVersionResource.setContentType(asacRestContentType);
    }
    
    boolean indent = StandardApiClientUtils.argMapBoolean(argMap, argMapNotUsed, "indent", false, false);
    
    asacApiDefaultVersionResource.assignIndent(indent);
  
    //register that we will use this
    StandardApiClientUtils.argMapString(argMap, argMapNotUsed, "outputTemplate", false);
    failOnArgsNotUsed(argMapNotUsed);
  
    AsacDefaultVersionResourceContainer asacDefaultVersionResourceContainer = asacApiDefaultVersionResource.execute();
    
    StringBuilder result = new StringBuilder();
    
    Map<String, Object> substituteMap = new LinkedHashMap<String, Object>();
  
    substituteMap.put("defaultVersionResourceContainer", asacDefaultVersionResourceContainer);
    substituteMap.put("defaultVersionResource", asacDefaultVersionResourceContainer.getDefaultVersionResource());
    substituteMap.put("meta", asacDefaultVersionResourceContainer.getMeta());
    substituteMap.put("responseMeta", asacDefaultVersionResourceContainer.getResponseMeta());
    substituteMap.put("serviceMeta", asacDefaultVersionResourceContainer.getServiceMeta());
  
    String outputTemplate = null;
  
    if (argMap.containsKey("outputTemplate")) {
      outputTemplate = StandardApiClientUtils.argMapString(argMap, argMapNotUsed, "outputTemplate", true);
      outputTemplate = StandardApiClientUtils.substituteCommonVars(outputTemplate);
    } else {
      outputTemplate = StandardApiClientConfig.retrieveConfig().propertyValueStringRequired("webService.defaultVersionResourceWs.output");
    }
    log.debug("Output template: " + StandardApiClientUtils.trim(outputTemplate) + ", available variables: defaultVersionResourceContainer, " +
      "defaultVersionResource, meta, responseMeta, serviceMeta");
  
    
    String output = StandardApiClientUtils.substituteExpressionLanguage(outputTemplate, substituteMap);
    result.append(output);
    
    return result.toString();
  }

  /**
   * @param argMap
   * @param argMapNotUsed
   * @return result
   */
  private static String versionResourceWs(Map<String, String> argMap,
      Map<String, String> argMapNotUsed) {
    
    AsacApiVersionResource asacApiVersionResource = new AsacApiVersionResource();        
    
    String formatString = StandardApiClientUtils.argMapString(argMap, argMapNotUsed, "format", false);
    
    AsacRestContentType asacRestContentType = AsacRestContentType.valueOfIgnoreCase(formatString, false);
    
    if (asacRestContentType != null) {
      asacApiVersionResource.setContentType(asacRestContentType);
    }
    
    boolean indent = StandardApiClientUtils.argMapBoolean(argMap, argMapNotUsed, "indent", false, false);
    
    asacApiVersionResource.assignIndent(indent);
  
    //register that we will use this
    StandardApiClientUtils.argMapString(argMap, argMapNotUsed, "outputTemplate", false);
    failOnArgsNotUsed(argMapNotUsed);
  
    AsacVersionResourceContainer asacVersionResourceContainer = asacApiVersionResource.execute();
    
    StringBuilder result = new StringBuilder();
    
    Map<String, Object> substituteMap = new LinkedHashMap<String, Object>();

    substituteMap.put("versionResourceContainer", asacVersionResourceContainer);
    substituteMap.put("versionResource", asacVersionResourceContainer.getVersionResource());
    substituteMap.put("meta", asacVersionResourceContainer.getMeta());
    substituteMap.put("responseMeta", asacVersionResourceContainer.getResponseMeta());
    substituteMap.put("serviceMeta", asacVersionResourceContainer.getServiceMeta());
  
    String outputTemplate = null;
  
    if (argMap.containsKey("outputTemplate")) {
      outputTemplate = StandardApiClientUtils.argMapString(argMap, argMapNotUsed, "outputTemplate", true);
      outputTemplate = StandardApiClientUtils.substituteCommonVars(outputTemplate);
    } else {
      outputTemplate = StandardApiClientConfig.retrieveConfig().propertyValueStringRequired("webService.versionResourceWs.output");
    }
    log.debug("Output template: " + StandardApiClientUtils.trim(outputTemplate) + ", available variables: versionResourceContainer, " +
      "versionResource, meta, responseMeta, serviceMeta");
  
    
    String output = StandardApiClientUtils.substituteExpressionLanguage(outputTemplate, substituteMap);
    result.append(output);
    
    return result.toString();
  }

  /**
   * @param argMap
   * @param argMapNotUsed
   * @return result
   */
  private static String folderSaveWs(Map<String, String> argMap,
      Map<String, String> argMapNotUsed) {
    
    AsacApiFolderSave asacApiFolderSave = new AsacApiFolderSave();        
    
    String formatString = StandardApiClientUtils.argMapString(argMap, argMapNotUsed, "format", false);
    
    AsacRestContentType asacRestContentType = AsacRestContentType.valueOfIgnoreCase(formatString, false);
    
    if (asacRestContentType != null) {
      asacApiFolderSave.setContentType(asacRestContentType);
    }
    
    boolean indent = StandardApiClientUtils.argMapBoolean(argMap, argMapNotUsed, "indent", false, false);
    
    asacApiFolderSave.assignIndent(indent);
  
    //register that we will use this
    StandardApiClientUtils.argMapString(argMap, argMapNotUsed, "outputTemplate", false);
    failOnArgsNotUsed(argMapNotUsed);
  
    String saveMode = StandardApiClientUtils.argMapString(argMap, argMapNotUsed, "saveMode", false);
    
    if (!StandardApiClientUtils.isBlank(saveMode)) {
      AsacSaveMode asacSaveMode = AsacSaveMode.valueOfIgnoreCase(saveMode, true);
      
      asacApiFolderSave.assignSaveMode(asacSaveMode);
    }
    
    Boolean createParentFoldersIfNotExist = StandardApiClientUtils.argMapBoolean(argMap, 
        argMapNotUsed, "createParentFoldersIfNotExist");
    if (createParentFoldersIfNotExist != null) {
      asacApiFolderSave.assignCreateParentFoldersIfNotExist(createParentFoldersIfNotExist);
    }
    
    AsacFolderLookup asacFolderLookup = StandardApiClientUtils.argFolderLookup(argMap, argMapNotUsed);
    
    if (asacFolderLookup != null) {
      asacApiFolderSave.assignFolderLookup(asacFolderLookup);
    }
    
    AsacFolderSaveResponse asacFolderSaveResponse = asacApiFolderSave.execute();
    
    StringBuilder result = new StringBuilder();
    
    Map<String, Object> substituteMap = new LinkedHashMap<String, Object>();
  
    substituteMap.put("versionResourceContainer", asacFolderSaveResponse);
    //substituteMap.put("versionResource", asacFolderSaveResponse.getVersionResource());
    substituteMap.put("meta", asacFolderSaveResponse.getMeta());
    substituteMap.put("responseMeta", asacFolderSaveResponse.getResponseMeta());
    substituteMap.put("serviceMeta", asacFolderSaveResponse.getServiceMeta());
  
    String outputTemplate = null;
  
    if (argMap.containsKey("outputTemplate")) {
      outputTemplate = StandardApiClientUtils.argMapString(argMap, argMapNotUsed, "outputTemplate", true);
      outputTemplate = StandardApiClientUtils.substituteCommonVars(outputTemplate);
    } else {
      outputTemplate = StandardApiClientConfig.retrieveConfig().propertyValueStringRequired("webService.versionResourceWs.output");
    }
    log.debug("Output template: " + StandardApiClientUtils.trim(outputTemplate) + ", available variables: versionResourceContainer, " +
      "versionResource, meta, responseMeta, serviceMeta");
  
    
    String output = StandardApiClientUtils.substituteExpressionLanguage(outputTemplate, substituteMap);
    result.append(output);
    
    return result.toString();
  }

}
