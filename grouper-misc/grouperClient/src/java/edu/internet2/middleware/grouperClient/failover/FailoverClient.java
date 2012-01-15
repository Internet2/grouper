 
/**
* @author mchyzer
* $Id: FailoverDatabaseLogic.java,v 1.8 2011/12/03 06:59:05 mchyzer Exp $
*/
package edu.internet2.middleware.grouperClient.failover;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.exception.ExceptionUtils;

import edu.internet2.middleware.grouperClient.failover.FailoverConfig.FailoverStrategy;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientCommonUtils;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.util.ExpirableCache.ExpirableCacheUnit;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
 
 
/**
* logic for hitting multiple databases in readonly queries.
*/
@SuppressWarnings("serial")
public class FailoverClient implements Serializable {
 
  /**
   * logger
   */
  private static Log LOG = GrouperClientUtils.retrieveLog(FailoverClient.class);

  /** cache the config as a field */
  private transient FailoverConfig failoverConfig;
  
  /** save the configs in case we unserialize, e.g. for testing */
  private static Map<String, FailoverConfig> failoverConfigMap = new HashMap<String, FailoverConfig>();
  
  /**
   * cache the config as a field
   * @return config
   */
  public FailoverConfig getFailoverConfig() {
    return this.failoverConfig;
  }

  /**
   * cache the config as a field
   * @param failoverConfig1
   */
  public void setFailoverConfig(FailoverConfig failoverConfig1) {
    this.failoverConfig = failoverConfig1;
  }

  /**
   * private constructor is from factory
   */
  private FailoverClient() {
    //nada
  }
  
  /** if the file is calculated */
  static boolean failoverStateFileSet = false;
  
  /** file where failover state is stored */
  static File failoverStateFile = null;
  
  /**
   * file where state is read or stored to or null if not saving state
   * @return the file or null if not saving
   */
  static File fileSaveFailoverClientState() {

    //cache this
    if (failoverStateFileSet) {
      return failoverStateFile;
    }
    
    String cacheDirectoryName = GrouperClientUtils.propertiesValue("grouperClient.cacheDirectory", false);
    boolean hasCacheDirectory = !GrouperClientUtils.isBlank(cacheDirectoryName);
    
    int saveStateEverySeconds = GrouperClientUtils.propertiesValueInt("grouperClient.saveFailoverStateEverySeconds", -1, false);

    boolean hasSaveStateEverySeconds = saveStateEverySeconds > 0;

    //you need both
    if (hasSaveStateEverySeconds && !hasCacheDirectory) {
      
      throw new RuntimeException("You have grouperClient.saveFailoverStateEverySeconds set in the grouper.properties " +
      		"but you do not have grouperClient.cacheDirectory set, you need a directory to save the file");
      
    }
    
    if (!hasSaveStateEverySeconds) {
      return null;
    }
    
    if (cacheDirectoryName.endsWith("/") || cacheDirectoryName.endsWith("\\")) {
      
      cacheDirectoryName = cacheDirectoryName.substring(0, cacheDirectoryName.length()-1);
      
    }
    
    failoverStateFile = new File(cacheDirectoryName + File.separator + "grouperClientFailoverState.bin");
    failoverStateFileSet = true;
    
    return failoverStateFile;
  }
  
  /**
   * dont access this directly, use the factory!  key is the type,
   * value is the FailoverClient.  This is a hashmap since it is serializable
   */
  static HashMap<String,FailoverClient> instanceMapFromType = null;
  
  /**
   * get the instance or read from file
   * @return the map
   */
  static HashMap<String, FailoverClient> instanceMapFromType() {
    
    if (instanceMapFromType == null) {
    
      //save state to file if necessary
      int saveStateEverySeconds = GrouperClientUtils.propertiesValueInt("grouperClient.saveFailoverStateEverySeconds", -1, false);
      File saveStateFile = fileSaveFailoverClientState();
      
      if (saveStateEverySeconds >= 0) {
        
        instanceMapFromType = (HashMap)GrouperClientUtils.unserializeObjectFromFile(saveStateFile, false, true);
        
      }
      if (instanceMapFromType == null) {

        instanceMapFromType = new HashMap<String, FailoverClient>();

      }
    }
    return instanceMapFromType;
  }
    
  

  /**
   * get a failover client from memory or disk or make a new one.  this is internal or
   * for testing only
   * @param connectionType connection type for the config we are talking about
   * @return failover client
   */
  static FailoverClient failoverClient(final String connectionType) {
    
    FailoverClient failoverClient = instanceMapFromType().get(connectionType);
    
    if (failoverClient == null) {
      throw new RuntimeException("Why is failover client not initialized for type: '" 
          + connectionType + "', call the initFailoverClient() method before this method");
    }
    
    //maybe this was nulled out due to testing and reading from file
    if (failoverClient.failoverConfig == null) {
      failoverClient.failoverConfig = failoverConfigMap.get(connectionType);
    }
    
    return failoverClient;
  }

  /**
   * key is the connection type, two underscores, connection name, 
   * then an expirable map of minute in question (minute since 1970) to number of errors
   */
  Map<String, ExpirableCache<Long, Long>> errorCountPerMinute = new HashMap<String, ExpirableCache<Long, Long>>();
 
 
  /**
   * key is the connection type, then the cache has Boolean.TRUE, and the connection name which has affinity... 
   * affinity is only for the first one chosen
   */
  ExpirableCache<Boolean, String> connectionAffinityCache = null;
 
  /**
   * minutes since 1970
   * @return the minutes since 1970
   */
  private static long minutesSince1970() {
    return (System.currentTimeMillis()  / 1000) / 60;
  }
 
  /**
   * increment number of errors in the database connection
   * @param connectionType 
   * @param connectionName
   * @param minutesToKeepErrors 
   */
  private void incrementErrorForConnection(String connectionName) {
    
    ExpirableCache<Long, Long> errorCount = errorCountPerMinute(connectionName);
    Long minutesSince1970 = minutesSince1970();
    Long currentCount = GrouperClientUtils.defaultIfNull(errorCount.get(minutesSince1970), 0L);
    currentCount++;
    errorCount.put(minutesSince1970, currentCount);

  }


  /**
   * see how many errors in the database connection
   * @param connectionName
   * @param minutesSince1970
   * @param minutesToCheck 
   * @return the number of errors
   */
  private long errorsForConnection(String connectionName, long minutesSince1970) {

    long errors = 0;

    ExpirableCache<Long, Long> errorCache = errorCountPerMinute(connectionName);

    //get the errors from the last X minutes
    for (int i=0;i<this.failoverConfig.getMinutesToKeepErrors()+1;i++) {
     
      Long currentErrors = errorCache.get(minutesSince1970-i);
     
      if (currentErrors != null) {
        errors += currentErrors;
      }
    }
    return errors;
  }

  /**
   * @param connectionName
   * @param minutesToCheck
   * @return the cache
   */
  private ExpirableCache<Long, Long> errorCountPerMinute(String connectionName) {
    ExpirableCache<Long, Long> errorCache = this.errorCountPerMinute.get(connectionName);
    //doesnt really need to be synchronized...
    if (errorCache == null) {
      
      int minutesToCheck = this.failoverConfig.getMinutesToKeepErrors();

      //I dont remember what this was...
      ////maybe shouldnt be 0
      //if (minutesToCheck == 0 && this.failoverConfig.getMinutesToKeepErrors() > 0) {
      //  minutesToCheck = 1;
      //}
      
      errorCache = new ExpirableCache<Long, Long>(minutesToCheck+1);
      this.errorCountPerMinute.put(connectionName, errorCache);
    }
    return errorCache;

  }

  /** fatal problem count for testing */
  static long fatalProblemCountForTesting = 0;
 
  /** timeout count for testing */
  static long timeoutCountForTesting = 0;
 
  /** error count for testing */
  static long errorCountForTesting = 0;
 
  /** random generator, note this is threadsafe */
  private static Random random = new Random();

  /**
   * get a failover client from memory or disk or make a new one
   * @param failoverConfig config with the type we are talking about
   */
  public static synchronized void initFailoverClient(FailoverConfig failoverConfig) {
    FailoverClient failoverClient = instanceMapFromType().get(failoverConfig.getConnectionType());

    if (failoverClient == null) {
      
      failoverClient = new FailoverClient();
      instanceMapFromType().put(failoverConfig.getConnectionType(), failoverClient);
    }
    failoverClient.setFailoverConfig(failoverConfig);
    
    failoverConfigMap.put(failoverConfig.getConnectionType(), failoverConfig);
    
    failoverConfig.getAffinitySeconds();
    int affinitySeconds = failoverConfig.getAffinitySeconds();
    //error if less than 1
    if (affinitySeconds < 1) {
      affinitySeconds = 1;
    }
    failoverClient.connectionAffinityCache = new ExpirableCache<Boolean, String>(ExpirableCacheUnit.SECOND, affinitySeconds);

    
    
  }
  
  /**
   * based on the connection type, get the list of connection names to try
   * @param failoverConfig configuration of the failover
   * @param connectionType
   * @return the list, if not pooled, just return list of size one
   */
  private List<String> orderedListOfConnectionNames() {
   
    List<String> resultConnectionNames = new ArrayList<String>();

    //all connection names
    List<String> connectionNamesInPool = new ArrayList<String>();
    connectionNamesInPool.addAll(GrouperClientUtils.nonNull(this.failoverConfig.getConnectionNames()));
    connectionNamesInPool.addAll(GrouperClientUtils.nonNull(this.failoverConfig.getConnectionNamesSecondTier()));
    

    //see if there is only one
    if (GrouperClientCommonUtils.length(this.failoverConfig.getConnectionNames()) 
        + GrouperClientCommonUtils.length(this.failoverConfig.getConnectionNamesSecondTier()) == 1) {
      
      resultConnectionNames.addAll(connectionNamesInPool);
      return resultConnectionNames;
      
    }
    
    //see if there is affinity
    String affinityConnectionName = this.connectionAffinityCache.get(Boolean.TRUE);
   
    //we have a pool
    List<String> availableConnectionsFewestErrorsFirst = new ArrayList<String>();
   
    long minutesSince1970 = minutesSince1970();
   
    Map<Long, Set<String>> connectionsForErrorCount = new HashMap<Long, Set<String>>();
    Set<Long> numberOfErrors = new TreeSet<Long>();
    
    for (String connectionName : connectionNamesInPool) {
     
      long errors = errorsForConnection(connectionName, minutesSince1970);
     
      //keep track of all errors
      numberOfErrors.add(errors);
     
      Set<String> connectionNames = connectionsForErrorCount.get(errors);
     
      if (connectionNames == null) {
       
        connectionNames = new HashSet<String>();
        connectionsForErrorCount.put(errors, connectionNames);
       
      }
     
      connectionNames.add(connectionName);
     
    }
   
    //for each number of errors, pick the best one
    for (long errors : numberOfErrors) {
     
      Set<String> connectionNames = connectionsForErrorCount.get(errors);
     
      //if one, all set
      if (connectionNames.size() == 1) {
        availableConnectionsFewestErrorsFirst.add(connectionNames.iterator().next());
        continue;
      }
     
      //multiple with the same error count
      if (availableConnectionsFewestErrorsFirst.size() == 0) {
       
        //see if there is affinity
        if (!GrouperClientUtils.isBlank(affinityConnectionName) && connectionNames.contains(affinityConnectionName)) {
          availableConnectionsFewestErrorsFirst.add(affinityConnectionName);
          connectionNames.remove(affinityConnectionName);
          availableConnectionsFewestErrorsFirst.addAll(connectionNames);
          continue;
        }
       
        //if active/active, pick one
        if (this.failoverConfig.getFailoverStrategy() == null || this.failoverConfig.getFailoverStrategy() == FailoverStrategy.activeActive) {
          
          Set<String> bestTierConnectionNames = new HashSet<String>();
          if (GrouperClientUtils.length(this.failoverConfig.getConnectionNamesSecondTier()) == 0) {
            
            bestTierConnectionNames.addAll(this.failoverConfig.getConnectionNames());
            
          } else if (GrouperClientUtils.length(this.failoverConfig.getConnectionNames()) == 0) {
            bestTierConnectionNames.addAll(this.failoverConfig.getConnectionNamesSecondTier());
          } else {
            
            //we have both... see if there are first tier ones there
            for (String firstTierName : GrouperClientUtils.nonNull(this.failoverConfig.getConnectionNames())) {
              if (connectionNames.contains(firstTierName)) {
                bestTierConnectionNames.add(firstTierName);
              }
            }
            //if there arent any, just add all
            if (bestTierConnectionNames.size() == 0) {
              bestTierConnectionNames.addAll(connectionNames);
            }
            
          }
          
          int index = random.nextInt(bestTierConnectionNames.size());
          String bestConnection = (String)GrouperClientUtils.get(bestTierConnectionNames, index);
         
          availableConnectionsFewestErrorsFirst.add(bestConnection);
          connectionNames.remove(bestConnection);
         
          //if we have affinity, set it
          this.connectionAffinityCache.put(Boolean.TRUE, bestConnection);
         
        } else {
          //if not active/active, then use the primary one... or the one furthest from right
          for (String connectionName : connectionNamesInPool) {
           
            if (connectionNames.contains(connectionName)) {
 
              if (availableConnectionsFewestErrorsFirst.size() == 0) {
                //i guess these have affinity too, not sure if it is best, maybe it should always just be preferred... hmmm
                this.connectionAffinityCache.put(Boolean.TRUE, connectionName);
              }
             
              availableConnectionsFewestErrorsFirst.add(connectionName);
              connectionNames.remove(connectionName);
            }
          }
        }
      }
      //just add the rest in there based on the number of errors
      for (String connectionName : connectionNames) {
        if (!availableConnectionsFewestErrorsFirst.contains(connectionName)) {
          availableConnectionsFewestErrorsFirst.add(connectionName);
        }
      }
 
    }
   
    //make sure they are all there
    for (String connectionName : connectionNamesInPool) {
      if (!availableConnectionsFewestErrorsFirst.contains(connectionName)) {
        availableConnectionsFewestErrorsFirst.add(connectionName);
      }
    }
    return availableConnectionsFewestErrorsFirst;
  }

  /**
   * threadpool
   */
  private static ExecutorService executorService = Executors.newCachedThreadPool();

  /**
   * run failover logic, return the result from the logic
   * @param <T>
   * @param connectionType is the type of connection
   * @param failoverLogic
   * @return the result from the logic
   */
  public static <T> T failoverLogic(String connectionType, FailoverLogic<T> failoverLogic) {
    return failoverLogic(connectionType, true, failoverLogic);
  }
  
  /**
   * run failover logic, return the result from the logic
   * @param <T>
   * @param connectionType is the type of connection
   * @param useThreads is true if we should use threads, maybe pass false e.g. 
   * if the system is not initted and it relies on something to get configuration.
   * generally true though
   * @param failoverLogic
   * @return the result from the logic
   */
  public static <T> T failoverLogic(String connectionType, boolean useThreads, FailoverLogic<T> failoverLogic) {
    
    FailoverClient failoverClient = failoverClient(connectionType);

    try {
      return failoverClient.internal_failoverLogic(useThreads, failoverLogic);
    } finally {
    
      //save state to file if necessary
      int saveStateEverySeconds = GrouperClientUtils.propertiesValueInt("grouperClient.saveFailoverStateEverySeconds", -1, false);
      File saveStateFile = fileSaveFailoverClientState();
      
      if (saveStateEverySeconds > -1) {
        
        //if we are saving each time, or if we havent saved in the period that we need to save
        boolean saveStateNow = saveStateEverySeconds == 0 || saveStateFile.lastModified() < System.currentTimeMillis() - (saveStateEverySeconds * 1000);
        
        if (saveStateNow) {
          //long now = System.nanoTime();
          GrouperClientUtils.serializeObjectToFile(instanceMapFromType, saveStateFile);
          //System.out.println("Serialized in " + ((System.nanoTime() - now) / 1000000) + "ms");
        }
        
      }
      
    }
  }

  /**
   * if we are starting up, add time to the timeout so we have time for classes to load etc...
   */
  private static Long startupMillis = null;
  
  /**
   * run failover logic, return the result from the logic
   * @param useThreads is true if we should use threads, maybe pass false e.g. 
   * if the system is not initted and it relies on something to get configuration.
   * generally true though
   * @param <T>
   * @param failoverLogic
   * @return the result from the logic
   */
  public <T> T internal_failoverLogic(boolean useThreads, final FailoverLogic<T> failoverLogic) {

    final List<String> orderedConnections = this.orderedListOfConnectionNames();

    int timeoutSeconds = this.failoverConfig.getTimeoutSeconds();

    if (startupMillis == null) {
      startupMillis = System.currentTimeMillis();
    }
    
    if (this.failoverConfig.getSecondsForClassesToLoad() > 0) {
      //see if that amount of time has passed yet
      long secondsSinceStartup = (System.currentTimeMillis() - startupMillis) / 1000;
      if (secondsSinceStartup < this.failoverConfig.getSecondsForClassesToLoad() ) {
        //add that amount of time to the timeout so we can wait for things to get started
        timeoutSeconds += (this.failoverConfig.getSecondsForClassesToLoad() - secondsSinceStartup);
      }
    }
    
    if (GrouperClientUtils.length(orderedConnections) == 0) {
      throw new RuntimeException("Why are there no connections for type: " + this.failoverConfig.getConnectionType());
    }
    
    if (GrouperClientUtils.length(orderedConnections) == 1) {
      String theDatabaseConnectionName = orderedConnections.get(0);
      if (LOG.isDebugEnabled()) {
        LOG.debug("Not load balancing connection type: " + this.failoverConfig.getConnectionType() + ", name: " + theDatabaseConnectionName);
      }
      return failoverLogic.logic(new FailoverLogicBean(false, theDatabaseConnectionName));
    }
 
    final Object[] results = new Object[orderedConnections.size()];
    final Boolean[] successes = new Boolean[orderedConnections.size()];
 
    //lets run the logic with a timeout for each connection
    for (int i = 0; i < orderedConnections.size(); i++) {
     
      final int I = i;
      final String connectionName = orderedConnections.get(i);
     
      //if not initted dont run in thread
      if (!useThreads) {
        try {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Trying connection (init): " + connectionName);
          }
          results[I] = failoverLogic.logic(new FailoverLogicBean(false, connectionName));
          if (LOG.isDebugEnabled()) {
            LOG.debug("Finished database connection (init): " + connectionName);
          }
          successes[I] = true;
        } catch (Throwable e) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Error database connection (init): " + connectionName + ", " + e.getMessage());
          }
          results[I] = e;
          successes[I] = false;
        }
       
        
        
      } else {
        final String id = LOG.isDebugEnabled() ? GrouperClientUtils.uniqueId() : null;
       
        Callable<T> callable = new Callable<T>() {
 
          @Override
          public T call() throws Exception {
            try {

              if (LOG.isDebugEnabled()) {
                LOG.debug("Trying database connection: " + connectionName + ", id: " + id);
              }
              results[I] = failoverLogic.logic(new FailoverLogicBean(true, connectionName));
              if (LOG.isDebugEnabled()) {
                LOG.debug("Finished database connection: " + connectionName + ", " + id);
              }
              successes[I] = true;
            } catch (Throwable e) {
              if (LOG.isDebugEnabled()) {
                LOG.debug("Error database connection: " + connectionName + ", " + e.getMessage() + ", id: " + id);
              }
              results[I] = e;
              successes[I] = false;
            }
            return null;
          }
        };
       
        Future<T> future = executorService.submit(callable);
        try {
          future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
          LOG.debug("error:", e);
        }
       
      }
     
      if (successes[i] != null && successes[i]) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Success for database connection: " + connectionName);
        }
        return (T)results[i];
      }
 
      //either way this is a problem
      incrementErrorForConnection(connectionName);
 
      boolean isFatal = i == orderedConnections.size() - 1;
      boolean isException = results[i] instanceof Throwable;
      String typeString = isException ? "error" : "timeout";
      RuntimeException re = null;
      if (isException) {
        errorCountForTesting++;
      } else {
        timeoutCountForTesting++;
      }
      if (isException) {
        if (results[i] instanceof RuntimeException) {
          re = (RuntimeException)results[i];
        } else {
          re = new RuntimeException((Throwable)results[i]);
        }
      }
     
      if (isFatal) {
        //see if we can wait a little more...  see if any timeouts have come back yet
        int waitMore = this.failoverConfig.getExtraTimeoutSeconds();
       
        if (waitMore >= 0) {
          WAIT_MORE: for (int seconds=0;seconds<waitMore;seconds++) {
            {
              boolean hasTimeouts = false;
              for (int index = 0; index <= i; index++) {
                if (successes[index] != null && successes[index]) {
                  isFatal = false;
                  //go out, it will find it below
                  break WAIT_MORE;
                }
                if (successes[index] == null) {
                  hasTimeouts = true;
                }
              }
              //if there are no more timeouts, then all errors, we are done
              if (!hasTimeouts) {
                break WAIT_MORE;
              }
            }
            //wait for a second at a time so we can end as soon as possible
            GrouperClientUtils.sleep(1000);
          }
        }
       
      }
 
      String severityString = isFatal ? "FATAL" : "NON-FATAL";
 
      //we need to log where this came from:
      RuntimeException thisStack = new RuntimeException("this stack");
     
      //log everything
      String objectToLog = severityString + " " + typeString + " in database connection: " + connectionName
          + "," + (isFatal ? "" : " will try others in pool") + " (" + errorCountForTesting
          + " total errors, "
          + timeoutCountForTesting + " total timeouts, "
          + fatalProblemCountForTesting + " total fatal errors): [THIS STACK]: " + ExceptionUtils.getFullStackTrace(thisStack);
      if (isException) {
        if (isFatal) {
          LOG.error(objectToLog, re);
        } else {
          LOG.warn(objectToLog, re);
        }
      } else {
        if (isFatal) {
          LOG.error(objectToLog);
        } else {
          LOG.warn(objectToLog);
        }
      }
      //if fatal, throw something...
      if (isFatal) {
        fatalProblemCountForTesting++;
        if (isException) {
          GrouperClientUtils.injectInException(re, objectToLog);
          throw re;
        }
        throw new RuntimeException(objectToLog);
      }
     
      //see if any work
      for (int index = 0; index < successes.length; index++) {
        if (successes[index] != null && successes[index]) {
          return (T)results[index];
        }
      }
     
    }

    throw new RuntimeException("Shouldnt get here");
  }
// 
//  /**
//   *
//   * @param connectionName
//   */
//  public static void testSituations(String connectionName) {
//    String propertiesFileLocation = FastContext.fastContext().getParamStringSafe("readonlyDatabaseSetTestingPropertiesFile");
//    if (!FastContext.fastContext().isProduction()) {
//      if (!GrouperClientUtils.isBlank(propertiesFileLocation) ) {
//        URL url = FastExternalUtils.computeUrl(propertiesFileLocation, false);
//        InputStream inputStream = null;
//        Properties properties = new Properties();
//        try {
//          inputStream = url.openStream();
//          properties.load(inputStream);
//        } catch (Exception e) {
//          throw new RuntimeException("Error opening resource: " + propertiesFileLocation, e);
//        } finally {
//          FastExternalUtils.closeQuietly(inputStream);
// 
//        }
//        if (FastBooleanUtils.booleanValue(GrouperClientUtils.trimToNull((String)properties.get(connectionName + "_error")), false)) {
//          throw new RuntimeException("TESTING ERROR on " + connectionName);
//        }
//        Integer timeoutSeconds = FastNumberUtils.intObjectValue(GrouperClientUtils.trimToNull((String)properties.get(connectionName + "_timeoutSeconds")), true);
//        if (timeoutSeconds != null) {
//          FastThreadUtils.sleep(timeoutSeconds * 1000);
//        }
//      }     
//      if (FastContext.fastContext().getParamBooleanSafe("readonlyDatabaseFailConnection_" + connectionName, false)) {
//        throw new RuntimeException("TESTING ERROR on " + connectionName);
//      }
//      int timeout = FastContext.fastContext().getParamIntSafe("readonlyDatabaseTimeoutSecondsConnection_" + connectionName, -1);
//      if (timeout > 0) {
//        FastThreadUtils.sleep(timeout * 1000);
//      }
//    }
//   
//  }
// 
//  /**
//   * logger
//   */
//  @SuppressWarnings("unused")
//  private static FastLogger log = new FastLogger(FailoverDatabaseLogic.class);
 
}
