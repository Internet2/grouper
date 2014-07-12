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
/**
 * 
 */
package edu.internet2.middleware.grouperClient.discovery;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouperClient.GrouperClient;
import edu.internet2.middleware.grouperClient.failover.FailoverClient;
import edu.internet2.middleware.grouperClient.failover.FailoverConfig;
import edu.internet2.middleware.grouperClient.failover.FailoverLogic;
import edu.internet2.middleware.grouperClient.failover.FailoverLogicBean;
import edu.internet2.middleware.grouperClient.failover.FailoverConfig.FailoverStrategy;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientCommonUtils;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientLog;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.edu.internet2.middleware.morphString.Crypto;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.Credentials;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.HttpClient;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.UsernamePasswordCredentials;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.auth.AuthScope;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.methods.GetMethod;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.params.DefaultHttpParams;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.params.HttpMethodParams;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.protocol.Protocol;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;


/**
 * This client will manage discovery, cache results, etc
 * TODO add checksum
 * 
 * @author mchyzer
 */
public class DiscoveryClient {

  /**
   * usage on command line
   */
  private static void usage() {
    
    System.out.println("Grouper Discovery Client USAGE:\n");
    System.out.println("This program downloads a file from a discovery server and failsafe caches the result.");
    System.out.println("The system exit code will be 0 for success, and not 0 for failure.");
    System.out.println("Output data is printed to stdout, error messages are printed to stderr or logs (configured in grouper.client.properties).\n");
    System.out.println("Grouper discovery client webpage: https://spaces.internet2.edu/display/Grouper/Grouper+discovery+client\n");
    System.out.println("Grouper discovery client USAGE:\n");
    System.out.println("Arguments are in the format: --argName=argValue");
    System.out.println("Example argument: --operation=getFile");
    System.out.println("Optional arguments below are in [brackets]\n");
    System.out.println("###############################################");
    System.out.println("## Operations\n");
    System.out.println("getFile usage:");
    System.out.println("Get a file from a discovery server or cache, will output the location of the file on the local machine to stdout.");
    System.out.println("Note, that file is cached, do not move, edit, delete it.");
    System.out.println("  java -cp grouperClient.jar edu.internet2.middleware.grouperClient.discovery.DiscoveryClient --operation=getFile --fileName=someFile.txt");
    System.out.println("  output: /home/whoever/grouperClient/someFile_20120102_132414_123_sd43sdf.txt");
    
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    
    long startTime = System.currentTimeMillis();

    String operation = null;
    try {
      if (GrouperClientUtils.length(args) == 0) {
        usage();
        return;
      }
      
      //map of all command line args
      Map<String, String> argMap = GrouperClientUtils.argMap(args);
      
      Map<String, String> argMapNotUsed = new LinkedHashMap<String, String>(argMap);

      boolean debugMode = GrouperClientUtils.argMapBoolean(argMap, argMapNotUsed, "debug", false, false);
      
      GrouperClientLog.assignDebugToConsole(debugMode);
      
      //init if not already
      GrouperClientConfig.retrieveConfig().properties();
      
      //see where log file came from
      StringBuilder callingLog = new StringBuilder();
      GrouperClientUtils.propertiesFromResourceName("grouper.client.properties", 
          false, true, GrouperClientCommonUtils.class, callingLog);
      
      //see if the message about where it came from is
      //log.debug(callingLog.toString());
      
      operation = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "operation", true);
      
      //where results should go if file
      String saveResultsToFile = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "saveResultsToFile", false);
      boolean shouldSaveResultsToFile = !GrouperClientUtils.isBlank(saveResultsToFile);
      
      if (shouldSaveResultsToFile) {
        log.debug("Will save results to file: " + GrouperClientUtils.fileCanonicalPath(new File(saveResultsToFile)));
      }
      
      String result = null;
      
      if (GrouperClientUtils.equals(operation, "getFile")) {
        
        String fileName = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "fileName", true);
        
        File file = retrieveFile(fileName, true);
        if (file == null) {
          
          throw new RuntimeException("Discovery service did not retrieve a success for file: " + fileName);
        }
        result = file.getAbsolutePath();
        
      } else {
        System.err.println("Error: invalid operation: '" + operation + "', for usage help, run: java -cp grouperClient.jar edu.internet2.middleware.grouperClient.discovery.DiscoveryClient" );
        if (GrouperClient.exitOnError) {
          System.exit(1);
        }
        throw new RuntimeException("Invalid usage");
      }
      
      //this already has a newline on it
      if (shouldSaveResultsToFile) {
        GrouperClientUtils.saveStringIntoFile(new File(saveResultsToFile), result);
      } else {
        System.out.print(result);
      }

      GrouperClient.failOnArgsNotUsed(argMapNotUsed);
      
    } catch (Exception e) {
      System.err.println("Error with grouper client, check the logs: " + e.getMessage());
      log.fatal(e.getMessage(), e);
      if (GrouperClient.exitOnError) {
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
      GrouperClientLog.assignDebugToConsole(false);
    }
    
  }

  
  /** format for file names of temp files */
  static final String TEMP_FILE_DATE_FORMAT = "yyyyMMdd_HHmmss_SSS";

  /** extension for download extensions before its done) */
  private static final String DISCOVERYTMP = ".discoverytmp";

  /** connection type for failover config */
  private static final String DISCOVERY_CLIENT_CONNECTION_TYPE = "discoveryClient";

  /**
   * 
   */
  static Log log = GrouperClientUtils.retrieveLog(DiscoveryClient.class);

  /**
   * see if we are configured for discovery
   * @return if we are using discovery
   */
  public static boolean hasDiscovery() {
    
    String firstUrl = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.urlOfDiscovery.0");
    
    return !GrouperClientUtils.isBlank(firstUrl);
    
  }

  /**
   * cache the discovery stuff by local file name to the file
   */
  private static ExpirableCache<String, File> discoveryFileCache = null;
  
  /**
   * cache the discovery file by local file name to the file
   * @return
   */
  private static ExpirableCache<String, File> discoveryFileCache() {
    
    if (discoveryFileCache == null) {
      int cacheDiscoveryFileCacheForSeconds = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.cacheDiscoveryPropertiesForSeconds", 120);
      discoveryFileCache = new ExpirableCache<String, File>(cacheDiscoveryFileCacheForSeconds);
    }
    return discoveryFileCache;
  }
  
  /**
   * the remote file name might have slashes in it, convert to underscores
   * @param fileName
   * @return the local file name (no subdirs)
   */
  private static String convertFileNameToLocalFileName(String fileName) {
    
    String localFileName = GrouperClientUtils.replace(fileName, "/", "_");
    localFileName = GrouperClientUtils.replace(localFileName, "\\", "_");
    
    return localFileName;
  }
  
  /**
   * retrieve a file from the discovery server
   * @param fileName file name on the server
   * @param throwExceptionIfNotFound true if should throw an exception if not found
   * @return the file or throw an exception if not found if supposed to.  If not configured to use
   * discovery, return null
   */
  public static File retrieveFile(String fileName, boolean throwExceptionIfNotFound) {

    Map<String, Object> logMap = log.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;

    if (logMap != null) {
      logMap.put("method", "DiscoveryClient.retrieveFile");
    }

    File file = null;
    try {

      //see if not doing discovery
      if (!hasDiscovery()) {
        if (logMap != null) {
          logMap.put("configuredToUseDiscovery", false);
        }
        return null;
      }
      String localFileName = convertFileNameToLocalFileName(fileName);
      
      
      //check discovery cache
      file = discoveryFileCache().get(localFileName);

      //if we got it from cache, dont put it back into cache
      boolean retrievedFromCache = file != null;
      
      if (logMap != null) {
        logMap.put("existsInDiscoveryFilecache", file != null);
      }

      if (file == null) {
        //lets see what the most recent file is on the file system
        File discoveryLocalFile = mostRecentFileFromFileSystem(localFileName);
        if (discoveryLocalFile != null && discoveryLocalFile.exists()) {
          
          //file.ext would become
          //file_20120102_132414_123_sd43sdf.ext
          Matcher matcher = localCacheDatePattern.matcher(discoveryLocalFile.getName());

          if (!matcher.matches()) {
            throw new RuntimeException("Why does matcher not match???? " + discoveryLocalFile.getAbsolutePath());
          }

          String datePart = matcher.group(2);

          DateFormat dateFormat = new SimpleDateFormat(TEMP_FILE_DATE_FORMAT);
          Date date = null;

          try {
            date = dateFormat.parse(datePart);
          } catch (ParseException pe) {
            
            throw new RuntimeException("Why date format exception???? " + file.getAbsolutePath());
            
          }

          //see if date in range
          if ((System.currentTimeMillis() - date.getTime()) / 1000 < GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.cacheDiscoveryPropertiesForSeconds", 120)) {
            file = discoveryLocalFile;
          }
          
          
        }
        //if this is less than however old, then use it
        if (logMap != null) {
          logMap.put("existsInFilecache", discoveryLocalFile != null);
        }
        if (logMap != null) {
          logMap.put("fileIsYoungEnough", file != null);
        }

      }
      
      if (file == null) {
        
        //lets get it from discovery again, this will return null if problem
        try {
          file = retrieveFileFromDiscoveryServer(fileName, localFileName);
        } catch (Exception e) {
          log.error("Problem retrieving file from discovery server: " + fileName, e);
        }
        if (logMap != null) {
          logMap.put("fileFromServer", file != null);
        }
      }

      if (file == null) {
        //just get whatever we have in the filesystem
        file = mostRecentFileFromFileSystem(localFileName);
        if (logMap != null) {
          logMap.put("fileFromFailsafeLocalSystem", file != null);
        }
      }

      //add back to cache if didnt retrieve from cache
      if (!retrievedFromCache && file != null) {
        synchronized (DiscoveryClient.class) {
          discoveryFileCache().put(localFileName, file);
        }
      }

      
      //end
      if (logMap != null) {
        logMap.put("fileFound", file!=null);
        if (file != null) {
          logMap.put("fileSizeBytes", file.length());
          logMap.put("lastModified", new Date(file.lastModified()));
        }
      }

      if (file == null && throwExceptionIfNotFound) {
        
        throw new RuntimeException("Cant find file from discovery: '" + fileName + "'");
      }

    } finally {

      if (log.isDebugEnabled()) {
        log.debug(GrouperClientUtils.mapToString(logMap));
      }

    }
    return file;
  }
  
  /**
   * configure the failover client once
   */
  static boolean failoverClientConfigured = false;
  
  /**
   * read the grouper.client.properties and configure the failover client once
   */
  private static void configureFailoverClientOnce() {
    if (failoverClientConfigured) {
      return;
    }
    synchronized (DiscoveryClient.class) {
      if (!failoverClientConfigured) {
        

        List<String> discoveryUrls = new ArrayList<String>();
        
        for (int i=0;i<100;i++) {
          String discoveryUrl = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.urlOfDiscovery." + i);
          if (GrouperClientUtils.isBlank(discoveryUrl)) {
            break;
          }
          discoveryUrls.add(discoveryUrl);
        }
        
        //there should be failover servers at this point...
        if (discoveryUrls.size() > 0) {

          //configure it
          FailoverConfig failoverConfig = new FailoverConfig();
          failoverConfig.setConnectionNames(discoveryUrls);
          failoverConfig.setConnectionType(DISCOVERY_CLIENT_CONNECTION_TYPE);
          failoverConfig.setFailoverStrategy(FailoverStrategy.activeActive);
          FailoverClient.initFailoverClient(failoverConfig);
        
          //remember that we just configured it
          failoverClientConfigured = true;

        } else {
          log.error("There are no discovery URLs in grouper.client.properties");
        }
      }
    }
  }
  
  
  /**
   * keep a map of objects to synchronize on based on filename
   */
  private static Map<String, String> synchronizedObjectBasedOnName = new HashMap<String, String>();
  
  /**
   * synchronized object based on name or create if not there
   * @return the object
   */
  private static String synchronizedObjectBasedOnName(String name) {
    String synchronizedObject = synchronizedObjectBasedOnName.get(name);
    if (synchronizedObject == null) {
      synchronized (DiscoveryClient.class) {

        //make sure another thread didnt just grab it
        synchronizedObject = synchronizedObjectBasedOnName.get(name);
        if (synchronizedObject == null) {
          synchronizedObject = new String(name);
          synchronizedObjectBasedOnName.put(name, synchronizedObject);
        }
      }
    }
    return synchronizedObject;
  }
  
  /**
   * get the most recent file from the file system for a given file name
   * @param localFileName
   * @return the file
   */
  static File mostRecentFileFromFileSystem(String localFileName) {

    File cacheDirectory = new File(GrouperClientUtils.cacheDirectoryName());
    
    File[] files = cacheDirectory.listFiles();

    // non tmp file catalog to see if we can delete non tmp file yet
    Map<Date, File> mapOfDateToFile = new TreeMap<Date, File>();
    
    //lets look for tmp files that are 20 minutes old or older
    for (File file : files) {
      
      if (file.getAbsolutePath().endsWith(DISCOVERYTMP)) {
        continue;
      }
        
      //the filename is something like this:
      String fileName = file.getName();

      //file.ext would become
      //file_20120102_132414_123_sd43sdf.ext
      Matcher matcher = localCacheDatePattern.matcher(fileName);

      if (!matcher.matches()) {
        continue;
      }

      //lets catalog the non tmp files
      String prefixFileName = matcher.group(1);
      String suffixFileName = matcher.group(3);
      
      //see if fileName matches
      if (!GrouperClientCommonUtils.equals(prefixFileName + suffixFileName, localFileName)) {
        continue; 
      }
      
      String datePart = matcher.group(2);

      DateFormat dateFormat = new SimpleDateFormat(TEMP_FILE_DATE_FORMAT);
      Date date = null;

      try {
        date = dateFormat.parse(datePart);
      } catch (ParseException pe) {
        
        log.error("Why date format exception???? " + file.getAbsolutePath());
        continue;
        
      }

      //add an entry for this
      mapOfDateToFile.put(date, file);
      
    }
    
    //the last one should be the most recent
    if (mapOfDateToFile.size() == 0) {
      return null;
    }
    
    return new ArrayList<File>(mapOfDateToFile.values()).get(mapOfDateToFile.size()-1);

  }
  
  /**
   * get the discovery local file with unique extension.  if file exists, try again
   * @param localFileName
   * @param isTmpFile true if .discoverytmp should be added to extension
   * @return the local file
   */
  private static File discoveryLocalFileUnique(String localFileName, boolean isTmpFile) {
    File file = null;
    //get a new file if the file already exists (unlikely)
    for (int i=0;i<100;i++) {
      file = discoveryLocalFileUniqueHelper(localFileName, isTmpFile);
      if (!file.exists()) {
        break;
      }
    }
    if (file.exists()) {
      throw new RuntimeException("Why does the file exist?");
    }
    return file;
  }

  
  /**
   * get the discovery local file with unique extension
   * @param localFileName
   * @param isTmpFile true if .discoverytmp should be added to extension
   * @return the local file
   */
  private static File discoveryLocalFileUniqueHelper(String localFileName, boolean isTmpFile) {

    String directoryName = GrouperClientUtils.cacheDirectoryName();

    if (localFileName.contains("/")) {
      throw new RuntimeException("Local file cannot contain / : " + localFileName);
    }

    if (localFileName.contains("\\")) {
      throw new RuntimeException("Local file cannot contain \\ : " + localFileName);
    }
    
    //lets change the local file name
    int dotIndex = localFileName.lastIndexOf('.');

    if (dotIndex == -1) {
      throw new RuntimeException("Local filename must have a dot in it! " + localFileName);
    }

    String fileNamePrefix = localFileName.substring(0, dotIndex);
    String fileNameSuffix = localFileName.substring(dotIndex, localFileName.length());

    DateFormat dateFormat = new SimpleDateFormat(TEMP_FILE_DATE_FORMAT);
    
    //file.ext would become
    //file_20120102_132414_123_sd43sdf.ext
    
    String pathname = directoryName + File.separator + fileNamePrefix + "_" + dateFormat.format(new Date()) + "_" + GrouperClientUtils.uniqueId() + fileNameSuffix;
    
    if (isTmpFile) {
      //file.ext would become
      //file_20120102_132414_123_sd43sdf.ext.discoverytmp
      pathname += DISCOVERYTMP;
    }
    return new File(pathname);

  }

  /**
   * contact the discovery server(s) to get the file
   * @param fileNameOnServer
   * @param localFileName should contain no slashes, just the filename locally which should conflict with no other filenames
   * @return the file or null if problem (and log problem)
   */
  static File retrieveFileFromDiscoveryServer(final String fileNameOnServer, final String localFileName) {

    if (!hasDiscovery()) {
      return null;
    }
    
    //make sure the failover client is configured
    configureFailoverClientOnce();
    
    File file = FailoverClient.failoverLogic(DiscoveryClient.DISCOVERY_CLIENT_CONNECTION_TYPE, new FailoverLogic<File>() {

      /**
       * logic to get the discovery file
       */
      // @Override
      public File logic(FailoverLogicBean failoverLogicBean) {
        
        Map<String, Object> logMap = log.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;

        if (logMap != null) {
          logMap.put("method", "DiscoveryClient.retrieveFileFromDiscoveryServer.logic");
        }

        //this is the URL
        String url = failoverLogicBean.getConnectionName();
                
        //strip last slash
        url = GrouperClientUtils.stripEnd(url, "/");
        
        String fileNameNoSlash = GrouperClientUtils.stripStart(fileNameOnServer, "/");
        
        String fullUrl = url + "/" + fileNameNoSlash;

        if (logMap != null) {
          logMap.put("fullUrl", fullUrl);
        }

        //see if invalid SSL
        String httpsSocketFactoryName = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.https.customSocketFactory");
        
        //is there overhead here?  should only do this once?
        //perhaps give a custom factory
        if (!GrouperClientUtils.isBlank(httpsSocketFactoryName)) {
          Class<? extends SecureProtocolSocketFactory> httpsSocketFactoryClass = GrouperClientUtils.forName(httpsSocketFactoryName);
          SecureProtocolSocketFactory httpsSocketFactoryInstance = GrouperClientUtils.newInstance(httpsSocketFactoryClass);
          Protocol easyhttps = new Protocol("https", httpsSocketFactoryInstance, 443);
          Protocol.registerProtocol("https", easyhttps);
        }

        HttpClient httpClient = new HttpClient();

        DefaultHttpParams.getDefaultParams().setParameter(
            HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));

        int soTimeoutMillis = GrouperClientConfig.retrieveConfig().propertyValueInt(
            "grouperClient.discovery.httpSocketTimeoutMillis", 90000);

        httpClient.getParams().setSoTimeout(soTimeoutMillis);
        httpClient.getParams().setParameter(HttpMethodParams.HEAD_BODY_CHECK_TIMEOUT, soTimeoutMillis);

        int connectionManagerMillis = GrouperClientConfig.retrieveConfig().propertyValueInt(
            "grouperClient.discovery.httpConnectionManagerTimeoutMillis", 90000);

        httpClient.getParams().setConnectionManagerTimeout(connectionManagerMillis);

        String user = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.discover.user");
        
        if (logMap != null) {
          logMap.put("user", user);
        }
        
        if (!GrouperClientUtils.isBlank(user)) {
        
          httpClient.getParams().setAuthenticationPreemptive(true);
  
          boolean disableExternalFileLookup = GrouperClientConfig.retrieveConfig().propertyValueBooleanRequired(
              "encrypt.disableExternalFileLookup");
          
          //lets lookup if file
          String pass = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.discovery.password");
          String passFromFile = GrouperClientUtils.readFromFileIfFile(pass, disableExternalFileLookup);
  
          String passPrefix = null;
  
          if (!GrouperClientUtils.equals(pass, passFromFile)) {
  
            passPrefix = "Discovery pass: reading encrypted value from file: " + pass;
  
            String encryptKey = GrouperClientUtils.encryptKey();
            pass = new Crypto(encryptKey).decrypt(passFromFile);
            
          } else {
            passPrefix = "Discovery pass: reading scalar value from grouper.client.properties";
          }
          
          if (GrouperClientConfig.retrieveConfig().propertyValueBoolean("grouperClient.logging.logMaskedPassword", false)) {
            if (logMap != null) {
              logMap.put(passPrefix, GrouperClientUtils.repeat("*", pass.length()));
            }
          }
  
          Credentials defaultcreds = new UsernamePasswordCredentials(user, pass);
  
          //set auth scope to null and negative so it applies to all hosts and ports
          httpClient.getState().setCredentials(new AuthScope(null, -1), defaultcreds);
        }

        GetMethod getMethod = new GetMethod(fullUrl);

        getMethod.setRequestHeader("Connection", "close");
        
        File file = discoveryLocalFileUnique(localFileName, true);
        
        try {

          int responseCodeInt = httpClient.executeMethod(getMethod);

          if (responseCodeInt != 200) {
            throw new RuntimeException("Expected 200, but received response code: " + responseCodeInt);
          }

          synchronized (synchronizedObjectBasedOnName(localFileName)) {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {

              inputStream = getMethod.getResponseBodyAsStream();
              GrouperClientUtils.deleteFile(file);

              outputStream = new FileOutputStream(file);

              GrouperClientUtils.copy(inputStream, outputStream);

            } catch (RuntimeException re) {
              try {
                GrouperClientUtils.deleteFile(file);
              } catch (Exception e) {
                //dont pre-empt the other exception
                log.error("Cant delete file: " + file.getAbsolutePath(), e);
              }
              throw re;
            } finally {
              GrouperClientUtils.closeQuietly(inputStream);
              GrouperClientUtils.closeQuietly(outputStream);
            }

          }

        } catch (Exception exception) {

          throw new RuntimeException("Problem with url: " + fullUrl + ", and local file: " + file.getAbsolutePath(), exception);
          
        }
        
        return file;
      }
      
      
    });
    
    //lets get the non tmp version
    File realFile = new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - DISCOVERYTMP.length()));

    //we made it, lets delete the real file if it is there, and copy the new one to the old one
    //note, if using command line, this synchronization will not work, but try anyways...
    synchronized (synchronizedObjectBasedOnName(localFileName)) {

      GrouperClientUtils.renameTo(file, realFile);
    }
    
    cleanoutOldFiles();
    
    return realFile;

  }

  /** local cache date pattern */
  private static Pattern localCacheDatePattern = Pattern.compile("(.*)_(\\d{8}_\\d{6}_\\d{3})_.*(\\..*)");
  
  /**
   * if the file is a tmp file and more than 20 minutes old, or if the file is a non temp file, and more than 20
   * min old and there one that is newer (and the one that is newer is 20 minutes old), then delete it.  
   * only log exceptions since there could be two processes doing the same thing and step on toes
   * 
   */
  static void cleanoutOldFiles() {
    
    try {
      File cacheDirectory = new File(GrouperClientUtils.cacheDirectoryName());
      
      File[] files = cacheDirectory.listFiles();

      // non tmp file catalog to see if we can delete non tmp file yet
      Map<String, Map<Date, File>> nonTmpFileCatalog = new HashMap<String, Map<Date, File>>();
      
      //lets look for tmp files that are 20 minutes old or older
      for (File file : files) {

        //the filename is something like this:
        String fileName = file.getName();

        //file.ext would become
        //file_20120102_132414_123_sd43sdf.ext.discoverytmp or file_20120102_132414_123_sd43sdf.ext
        Matcher matcher = localCacheDatePattern.matcher(fileName);

        if (!matcher.matches()) {
          continue;
        }
        String datePart = matcher.group(2);

        DateFormat dateFormat = new SimpleDateFormat(TEMP_FILE_DATE_FORMAT);
        Date date = dateFormat.parse(datePart);

        //see if more than 20 minutes old
        if (System.currentTimeMillis() - date.getTime() < (long)20 * 60 * 1000) {
          continue;
        }
        
        if (file.getAbsolutePath().endsWith(DISCOVERYTMP)) {

          try {
            GrouperClientUtils.deleteFile(file);
          } catch (Exception e) {
            log.error("Cant delete file: " + file.getAbsolutePath(), e);
          }

        } else {

          //lets catalog the non tmp files
          String prefixFileName = matcher.group(1);
          String suffixFileName = matcher.group(3);
          String nonTmpFileName = prefixFileName + suffixFileName;
          
          //get the map of date to file in tree map so it is sorted
          Map<Date, File> mapOfDateToFile = nonTmpFileCatalog.get(nonTmpFileName);
          
          if (mapOfDateToFile == null) {
            mapOfDateToFile = new TreeMap<Date, File>();
            nonTmpFileCatalog.put(nonTmpFileName, mapOfDateToFile);
          }

          //not sure why it would exist, but if it does, delete it
          if (mapOfDateToFile.containsKey(date)) {
            try {
              GrouperClientUtils.deleteFile(file);
            } catch (Exception e) {
              log.error("Cant delete file: " + file.getAbsolutePath(), e);
            }
            
          }
          
          //add an entry for this
          mapOfDateToFile.put(date, file);
          
        }
      }
      
      //now go through the map and look for things to delete
      for (Map<Date, File> mapOfDateToFile : nonTmpFileCatalog.values()) {
        
        if (mapOfDateToFile.size() <= 1) {
          continue;
        }
        
        //dont delete the last one, that one is newest
        //these files are ordered by download date newest last
        int index = 0;
        Collection<File> filesToDelete = mapOfDateToFile.values();
        for (File file : filesToDelete) {
          if (index < filesToDelete.size()-1) {
            try {
              GrouperClientCommonUtils.deleteFile(file);
            } catch (Exception e) {
              log.error("Cant delete file: " + file.getAbsolutePath(), e);
            }
          }
            
          index++;
        }
      }
      
    } catch (Exception e) {
      log.error("Cant clean log directory!", e);
    }
  }

}
