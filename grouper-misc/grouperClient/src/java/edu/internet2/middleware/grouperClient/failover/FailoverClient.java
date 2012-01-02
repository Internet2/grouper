 
/**
* @author mchyzer
* $Id: FailoverDatabaseLogic.java,v 1.8 2011/12/03 06:59:05 mchyzer Exp $
*/
package edu.internet2.middleware.grouperClient.failover;
 
 
/**
* logic for hitting multiple databases in readonly queries.
*/
public class FailoverClient {
 
//  /**
//   * key is the connection name, then an expirable map of minute in question (minute since 1970) to number of errors
//   */
//  static Map<String, ExpirableCache<Long, Long>> errorCountPerMinute = new HashMap<String, ExpirableCache<Long, Long>>();
// 
// 
//  /**
//   * key is the pool name, then the cache has Boolean.TRUE, and the connection which has affinity... affinity is only for the first
//   * one chosen
//   */
//  static Map<String, ExpirableCache<Boolean, String>> connectionPoolAffinityCache = new HashMap<String, ExpirableCache<Boolean, String>>();
// 
//  /**
//   * minutes since 1970
//   * @return the minutes since 1970
//   */
//  private static long minutesSince1970() {
//    return (System.currentTimeMillis()  / 1000) / 60;
//  }
// 
//  /**
//   * increment number of errors in the database connection
//   * @param databaseConnection
//   */
//  private static void incrementErrorForConnection(String databaseConnection) {
//    int minutesToCheck = minutesToKeepErrors(databaseConnection);
//    ExpirableCache<Long, Long> errorCount = errorCountPerMinute(databaseConnection, minutesToCheck);
//    Long minutesSince1970 = minutesSince1970();
//    Long currentCount = FastObjectUtils.defaultIfNull(errorCount.get(minutesSince1970), 0L);
//    currentCount++;
//    errorCount.put(minutesSince1970, currentCount);
//  }
// 
// 
//  /**
//   * see how many errors in the database connection
//   * @param databaseConnection
//   * @param minutesSince1970
//   * @return the number of errors
//   */
//  private static long errorsForConnection(String databaseConnection, long minutesSince1970) {
//   
//    long errors = 0;
//   
//    //minutes to keep errors
//    final int minutesToCheck = minutesToKeepErrors(databaseConnection);
// 
// 
//    ExpirableCache<Long, Long> errorCache = errorCountPerMinute(databaseConnection, minutesToCheck);
// 
//    //get the errors from the last X minutes
//    for (int i=0;i<minutesToCheck+1;i++) {
//     
//      Long currentErrors = errorCache.get(minutesSince1970-i);
//     
//      if (currentErrors != null) {
//        errors += currentErrors;
//      }
//    }
//    return errors;
//  }
// 
// 
// 
//  /**
//   * @param databaseConnection
//   * @param minutesToCheck
//   * @return the cache
//   */
//  private static ExpirableCache<Long, Long> errorCountPerMinute(String databaseConnection, int minutesToCheck) {
//    ExpirableCache<Long, Long> errorCache = errorCountPerMinute.get(databaseConnection);
//    //doesnt really need to be synchronized...
//    if (errorCache == null) {
//      errorCache = new ExpirableCache<Long, Long>(minutesToCheck+1, "errorsInDatabaseFailoverPool", true);
//      errorCountPerMinute.put(databaseConnection, errorCache);
//    }
//    return errorCache;
// 
//  }
// 
//  /** fatal problem count for testing */
// static long fatalProblemCountForTesting = 0;
// 
//  /** timeout count for testing */
//  static long timeoutCountForTesting = 0;
// 
//  /** error count for testing */
//  static long errorCountForTesting = 0;
// 
//  /** random generator, note this is threadsafe */
//  private static Random random = new Random();
// 
//  /**
//   * based on the database name, get the list of database connections to try
//   * @param oneDatabaseName
//   * @return the list, if not pooled, just return list of size one
//   */
//  private static List<String> orderedListOfDatabaseConnectionNames(String oneDatabaseName) {
//   
//    List<String> databaseNames = new ArrayList<String>();
//   
//    DatabaseFailoverPool databaseFailoverPool = DatabaseConfiguration.databaseToPool().get(oneDatabaseName);
//   
//    if (databaseFailoverPool == null) {
//      databaseNames.add(oneDatabaseName);
//      return databaseNames;
//    }
// 
//    ExpirableCache<Boolean, String> affinityCache = connectionPoolAffinityCache.get(databaseFailoverPool.getName());
//   
//    //init the affinity cache if not initted already
//    if (affinityCache == null) {
//     
//      //minutes of affinity
//      int minutesOfAffinity = FastContext.fastContext().getParamIntSafe(
//          "readonlyDatabaseSetLoadBalancingMinutesOfAffinity_" + databaseFailoverPool.getName(), 5);
//     
//      affinityCache = new ExpirableCache<Boolean, String>(minutesOfAffinity, "affinityCache", true);
//      connectionPoolAffinityCache.put(databaseFailoverPool.getName(), affinityCache);
//    }
//   
//    //we have a pool
//    List<String> availableConnectionsFewestErrorsFirst = new ArrayList<String>();
//   
//    long minutesSince1970 = minutesSince1970();
//   
//    Map<Long, Set<String>> connectionsForErrorCount = new HashMap<Long, Set<String>>();
//    Set<Long> numberOfErrors = new TreeSet<Long>();
//   
//    for (String connectionName : databaseFailoverPool.getDatabaseNamesInPool()) {
//     
//      long errors = errorsForConnection(connectionName, minutesSince1970);
//     
//      //keep track of all errors
//      numberOfErrors.add(errors);
//     
//      Set<String> connectionNames = connectionsForErrorCount.get(errors);
//     
//      if (connectionNames == null) {
//       
//        connectionNames = new HashSet<String>();
//        connectionsForErrorCount.put(errors, connectionNames);
//       
//      }
//     
//      connectionNames.add(connectionName);
//     
//    }
//   
//    //for each number of errors, pick the best one
//    for (long errors : numberOfErrors) {
//     
//      Set<String> connectionNames = connectionsForErrorCount.get(errors);
//     
//      //if one, all set
//      if (connectionNames.size() == 1) {
//        availableConnectionsFewestErrorsFirst.add(connectionNames.iterator().next());
//        continue;
//      }
//     
//      //multiple with the same error count
//      if (availableConnectionsFewestErrorsFirst.size() == 0) {
//       
//        //see if there is affinity
//        String affinityConnectionName = affinityCache.get(Boolean.TRUE);
//        if (!GrouperClientUtils.isBlank(affinityConnectionName) && connectionNames.contains(affinityConnectionName)) {
//          availableConnectionsFewestErrorsFirst.add(affinityConnectionName);
//          connectionNames.remove(affinityConnectionName);
//          availableConnectionsFewestErrorsFirst.addAll(connectionNames);
//          continue;
//        }
//       
//        //if not affinity, see if we are active/standby
//        boolean activeActive = false;
//        {
//          String loadBalancingMethod = FastContext.fastContext().getParamStringSafe("readonlyDatabaseSetLoadBalancing_" + databaseFailoverPool.getName());
//          if (StringUtils.isBlank(loadBalancingMethod) || StringUtils.equals(loadBalancingMethod, "active/active")) {
//            activeActive = true;
//          } else {
//            if (!StringUtils.equals(loadBalancingMethod, "active/standby")) {
//              throw new RuntimeException("readonlyDatabaseSetLoadBalancing_" + databaseFailoverPool.getName()
//                  + " needs to be active/active or active/standby, but is :'" + loadBalancingMethod + "'");
//            }
//          }
//        }
//       
//        //if active/active, pick one
//        if (activeActive) {
//         
//          int index = random.nextInt(connectionNames.size());
//          String bestConnection = (String)FastObjectUtils.get(connectionNames, index);
//         
//          availableConnectionsFewestErrorsFirst.add(bestConnection);
//          connectionNames.remove(bestConnection);
//         
//          //if we have affinity, set it
//          affinityCache.put(Boolean.TRUE, bestConnection);
//         
//        } else {
//          //if not active/active, then use the primary one... or the one furthest from right
//          for (String connectionName : databaseFailoverPool.getDatabaseNamesInPool()) {
//           
//            if (connectionNames.contains(connectionName)) {
// 
//              if (availableConnectionsFewestErrorsFirst.size() == 0) {
//                //i guess these have affinity too, not sure if it is best, maybe it should always just be preferred... hmmm
//                affinityCache.put(Boolean.TRUE, connectionName);
//              }
//             
//              availableConnectionsFewestErrorsFirst.add(connectionName);
//              connectionNames.remove(connectionName);
//            }
//          }
//        }
//      }
//      //just add the rest in there based on the number of errors
//      for (String connectionName : connectionNames) {
//        if (!availableConnectionsFewestErrorsFirst.contains(connectionName)) {
//          availableConnectionsFewestErrorsFirst.add(connectionName);
//        }
//      }
// 
//    }
//   
//    //make sure they are all there
//    for (String connectionName : databaseFailoverPool.getDatabaseNamesInPool()) {
//      if (!availableConnectionsFewestErrorsFirst.contains(connectionName)) {
//        availableConnectionsFewestErrorsFirst.add(connectionName);
//      }
//    }
//    return availableConnectionsFewestErrorsFirst;
//  }
// 
//  /**
//   * @param oneDatabaseName
//   * @return the number of minutes
//   */
//  private static int minutesToKeepErrors(String oneDatabaseName) {
//    DatabaseFailoverPool databaseFailoverPool = DatabaseConfiguration.databaseToPool().get(oneDatabaseName);
//    return FastContext.fastContext().getParamIntSafe(
//        "readonlyDatabaseSetLoadBalancingMinutesKeepErrors_" + databaseFailoverPool.getName(), 5);
//  }
// 
//  /**
//   * threadpool
//   */
//  private static ExecutorService executorService = Executors.newCachedThreadPool();
// 
//  /**
//   * if the query is failoverable, then try multiple connections
//   * @param shouldUsePoolIfApplicable
//   * @param hibernateSession2
//   * @param hibOptions
//   * @param theDatabaseConnectionName one of the poolable database connection names
//   * @param failoverDatabaseCode
//   * @return the object that the failover code returns
//   */
//  @SuppressWarnings("unchecked")
//  public static Object failoverLogic(boolean shouldUsePoolIfApplicable, HibernateSession2 hibernateSession2, EnumSet<HibOption> hibOptions, String theDatabaseConnectionName,
//      final FailoverClusterLogic failoverDatabaseCode) {
//   
//    if (theDatabaseConnectionName == null){
//      theDatabaseConnectionName = FastDatabaseFactory.AUTHORIZATION_DATABASE_NAME;
//    }
// 
//    final DatabaseFailoverPool databaseFailoverPool = shouldUsePoolIfApplicable
//      ? DatabaseConfiguration.databaseToPool().get(theDatabaseConnectionName) : null;
//    final List<String> orderedConnections = databaseFailoverPool == null ? null
//        : orderedListOfDatabaseConnectionNames(theDatabaseConnectionName);
// 
//    int timeoutSeconds = 20;
//   
//    if (databaseFailoverPool != null) {
//      String timeoutSecondsString = FastConfig.getParamStringSafe(
//          "readonlyDatabaseSetLoadBalancingTimeoutSeconds_" + databaseFailoverPool.getName());
//     
//      if (!StringUtils.isBlank(timeoutSecondsString)) {
//        timeoutSeconds = FastNumberUtils.intValue(timeoutSecondsString);
//      }
//     
//      //if its not initted we need time for FAST to startup... add 2 minutes...
//      if (!FastContext.fastContext().isInitted()
//          || !ConfigManager.isInitted()) {
//        timeoutSeconds = 180 + timeoutSeconds;
//      }
//    }
//   
//    if (FastContext.fastContext().getParamBooleanSafe("fastDontFailoverDatabaseConnections", false)
//        || HibOption.contains(hibOptions,HibOption.DONT_RUN_ON_POOL)
//        || hibernateSession2 != null || HibOption.needsTransaction(hibOptions)
//        || !shouldUsePoolIfApplicable || databaseFailoverPool == null
//        || FastObjectUtils.length(databaseFailoverPool.getDatabaseNamesInPool()) == 0
//        || FastObjectUtils.length(orderedConnections) <= 1) {
//      if (log.isDebug()) {
//        log.debug("Not pooling database connection: " + theDatabaseConnectionName);
//      }
//      return failoverDatabaseCode.logic(theDatabaseConnectionName);
//    }
// 
//    final Object[] results = new Object[orderedConnections.size()];
//    final Boolean[] successes = new Boolean[orderedConnections.size()];
// 
//    final FastThreadLocalUtils fastThreadLocalUtils = new FastThreadLocalUtils(TransactionBean.staticTransactionBean(), true);
//    fastThreadLocalUtils.setCopyErrorAuditingThreadLocal(true);
//   
//    //lets run the logic with a timeout for each connection
//    for (int i = 0; i < orderedConnections.size(); i++) {
//     
//      final int I = i;
//      final String connectionName = orderedConnections.get(i);
//     
//      //if not initted dont run in thread
//      if (!FastContext.fastContext().isInitted()
//          || !ConfigManager.isInitted()) {
//        try {
//          if (log.isDebug()) {
//            log.debug("Trying database connection (init): " + connectionName);
//          }
//          results[I] = failoverDatabaseCode.logic(connectionName);
//          if (log.isDebug()) {
//            log.debug("Finished database connection (init): " + connectionName);
//          }
//          successes[I] = true;
//        } catch (Throwable e) {
//          if (log.isDebug()) {
//            log.debug("Error database connection (init): " + connectionName + ", " + e.getMessage());
//          }
//          results[I] = e;
//          successes[I] = false;
//        }
//       
//        
//        
//      } else {
//        final String id = log.isDebug() ? FastStringUtils.uniqueId() : null;
//       
//        Callable callable = new Callable() {
// 
//          @Override
//          public Object call() throws Exception {
//            try {
//              fastThreadLocalUtils.assignThreadLocals();
//              testSituations(connectionName);
//              if (log.isDebug()) {
//                log.debug("Trying database connection: " + connectionName + ", id: " + id);
//              }
//              results[I] = failoverDatabaseCode.logic(connectionName);
//              if (log.isDebug()) {
//                log.debug("Finished database connection: " + connectionName + ", " + id);
//              }
//              successes[I] = true;
//            } catch (Throwable e) {
//              if (log.isDebug()) {
//                log.debug("Error database connection: " + connectionName + ", " + e.getMessage() + ", id: " + id);
//              }
//              results[I] = e;
//              successes[I] = false;
//            }
//            return null;
//          }
//        };
//       
//        Future future = executorService.submit(callable);
//        try {
//          future.get(timeoutSeconds, TimeUnit.SECONDS);
//        } catch (Exception e) {
//          log.debug("error:", e);
//        }
//       
//      }
//     
//      if (successes[i] != null && successes[i]) {
//        if (log.isDebug()) {
//          log.debug("Success for database connection: " + connectionName);
//        }
//        return results[i];
//      }
// 
//      //either way this is a problem
//      incrementErrorForConnection(connectionName);
// 
//      boolean isFatal = i == orderedConnections.size() - 1;
//      boolean isException = results[i] instanceof Throwable;
//      String typeString = isException ? "error" : "timeout";
//      RuntimeException re = null;
//      if (isException) {
//        errorCountForTesting++;
//      } else {
//        timeoutCountForTesting++;
//      }
//      if (isException) {
//        if (results[i] instanceof RuntimeException) {
//          re = (RuntimeException)results[i];
//        } else {
//          re = new RuntimeException((Throwable)results[i]);
//        }
//      }
//     
//      if (isFatal) {
//        //see if we can wait a little more...  see if any timeouts have come back yet
//        int waitMore = FastContext.fastContext().getParamIntSafe(
//            "readonlyDatabaseSetLoadBalancingWaitAfterAllTimeoutsSeconds_" + databaseFailoverPool.getName(), 20);
//       
//        if (waitMore >= 0) {
//          WAIT_MORE: for (int seconds=0;seconds<waitMore;seconds++) {
//            {
//              boolean hasTimeouts = false;
//              for (int index = 0; index <= i; index++) {
//                if (successes[index] != null && successes[index]) {
//                  isFatal = false;
//                  //go out, it will find it below
//                  break WAIT_MORE;
//                }
//                if (successes[index] == null) {
//                  hasTimeouts = true;
//                }
//              }
//              //if there are no more timeouts, then all errors, we are done
//              if (!hasTimeouts) {
//                break WAIT_MORE;
//              }
//            }
//            //wait for a second at a time so we can end as soon as possible
//            FastThreadUtils.sleep(1000);
//          }
//        }
//       
//      }
// 
//      String severityString = isFatal ? "FATAL" : "NON-FATAL";
// 
//      //we need to log where this came from:
//      RuntimeException thisStack = new RuntimeException("this stack");
//     
//      //log everything
//      String objectToLog = severityString + " " + typeString + " in database connection: " + connectionName
//          + "," + (isFatal ? "" : " will try others in pool") + " (" + errorCountForTesting
//          + " total errors, "
//          + timeoutCountForTesting + " total timeouts, "
//          + fatalProblemCountForTesting + " total fatal errors): [THIS STACK]: " + ExceptionUtils.getFullStackTrace(thisStack);
//      if (isException) {
//        if (isFatal) {
//          log.error(objectToLog, re);
//        } else {
//          log.warn(objectToLog, re);
//        }
//      } else {
//        if (isFatal) {
//          log.error(objectToLog);
//        } else {
//          log.warn(objectToLog);
//        }
//      }
//      //if fatal, throw something...
//      if (isFatal) {
//        fatalProblemCountForTesting++;
//        if (isException) {
//          FastExceptionUtils.injectInException(re, objectToLog);
//          throw re;
//        }
//        throw new RuntimeException(objectToLog);
//      }
//     
//      //see if any work
//      for (int index = 0; index < successes.length; index++) {
//        if (successes[index] != null && successes[index]) {
//          return results[index];
//        }
//      }
//     
//    }
//    throw new RuntimeException("Shouldnt get here");
//  }
// 
//  /**
//   *
//   * @param connectionName
//   */
//  public static void testSituations(String connectionName) {
//    String propertiesFileLocation = FastContext.fastContext().getParamStringSafe("readonlyDatabaseSetTestingPropertiesFile");
//    if (!FastContext.fastContext().isProduction()) {
//      if (!StringUtils.isBlank(propertiesFileLocation) ) {
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
//        if (FastBooleanUtils.booleanValue(StringUtils.trimToNull((String)properties.get(connectionName + "_error")), false)) {
//          throw new RuntimeException("TESTING ERROR on " + connectionName);
//        }
//        Integer timeoutSeconds = FastNumberUtils.intObjectValue(StringUtils.trimToNull((String)properties.get(connectionName + "_timeoutSeconds")), true);
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
