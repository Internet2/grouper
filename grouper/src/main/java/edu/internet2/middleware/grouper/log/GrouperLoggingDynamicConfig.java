package edu.internet2.middleware.grouper.log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

/**
 * 
 * @author mchyzer
 *
 */
public class GrouperLoggingDynamicConfig {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperLoggingDynamicConfig.class);
  /**
   * 
   */
  private static boolean shouldRun = false;

  private static void assignThread() {
    if (grouperLoggingDynamicConfigThread == null) {
      grouperLoggingDynamicConfigThread = new Thread(new Runnable() {
  
        @Override
        public void run() {
          
         while (true) {
            try {
  
              if (!shouldRun) {
                return;
              }
                
              int checkAfterSeconds = GrouperConfig.retrieveConfig().propertyValueInt("grouper.logging.dynamicUpdates.checkAfterSeconds", 60);
              
              if (checkAfterSeconds <=0) {
                checkAfterSeconds = 10;
              }
              
              GrouperUtil.sleep(checkAfterSeconds * 1000);
  
              if (!shouldRun) {
                return;
              }
  
              checkForUpdates();

            } catch (Exception e) {
  
              if (!shouldRun) {
                return;
              }
  
              LOG.error("Error in loggin dynamic config thread", e);
              // dont throw
              
              // no rapid fire errors
              GrouperUtil.sleep(60000);
            }
            
          }
        }
        
      });
      grouperLoggingDynamicConfigThread.setDaemon(true);
  
    }
  }

  public static void startThreadIfNotStarted() {
    
    if (grouperLoggingDynamicConfigThread == null || !grouperLoggingDynamicConfigThread.isAlive()) {
      if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.logging.dynamicUpdates.run", true)) {
        if (grouperLoggingDynamicConfigThread == null) {
          assignThread();
        }
        shouldRun = true;
        grouperLoggingDynamicConfigThread.start();
      }
    }
  }

  private static boolean appendersInitted = false;
  
  private static void initAppendersIfNotInitted() {
    if (appendersInitted) {
      return;
    }
    // lets get the current list of appenders
    final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    final Configuration config = ctx.getConfiguration();
    appenders = new HashSet<String>(config.getAppenders().keySet());
    appendersInitted = true;
  }

  public static void stopThread() {
    shouldRun = false;
  
    if (grouperLoggingDynamicConfigThread == null) {
      return;
    }
  
    try {
      try {
        grouperLoggingDynamicConfigThread.interrupt();
      } catch (Exception e) {
        LOG.debug("error interrupting thread", e);
      }
      grouperLoggingDynamicConfigThread.join(20000);
      grouperLoggingDynamicConfigThread = null;
    } catch (Exception e) {
      //ignore
      LOG.warn("error stopping thread", e);
    }
  }

  /**
   * thread to check for logging updates
   */
  private static Thread grouperLoggingDynamicConfigThread = null;

  /**
   * currently applied customizations
   */
  private static Set<MultiKey> currentLoggingCustomizationsNameLevelAppender = new HashSet<MultiKey>();

  /**
   * original logging name level appenders
   */
  private static Map<String, Level> previousLoggingNameToLevel = new HashMap<String, Level>();

  /**
   * list of appenders
   */
  private static Set<String> appenders = new HashSet<String>();

  /**
   * see if there are changes to the log4j2 config
   */
  public static void checkForUpdates() {
    
    initAppendersIfNotInitted();
    
    //  # this is the package or class to log, required
    //  # {valueType: "string"}
    //  # grouper.logger.myLogger.name = 
    //
    //  # required log level, should be one of: off, fatal, error, warn, info, debug, trace, all
    //  # {valueType: "string", formElement: "dropdown", optionValues: ["off", "fatal", "error", "warn", "info", "debug", "trace", "all"] }
    //  # grouper.logger.myLogger.level = 
    //
    //  # which appender to send logs to (optional), default to the appender for the class logs to, or default to: grouper_error
    //  # CATALINA, stderr, grouper_error, grouper_daemon, grouper_pspng, grouper_provisioning, grouper_ws, grouper_ws_longRunning
    //  # grouper.logger.myLogger.appender = 
    Set<MultiKey> newLoggingCustomizationsNameLevelAppenders = new LinkedHashSet<MultiKey>();
    Map<String, Level> newLoggingCustomizationsNameToLevel = new LinkedHashMap<String, Level>();
    Pattern pattern = Pattern.compile("^grouper\\.logger\\.([^.]+)\\.name$");
    Set<String> loggingConfigIds = GrouperConfig.retrieveConfig().propertyConfigIds(pattern);
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("configIdsSize", GrouperUtil.length(loggingConfigIds));

    try {
      String defaultAppender = appenders.contains("grouper_error") ? "grouper_error" : (appenders.contains("stderr") ? "stderr" : null);
  
      for (String loggingConfigId : loggingConfigIds) {
  
        String name = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.logger." + loggingConfigId + ".name");
        
        try {
          String levelString = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.logger." + loggingConfigId + ".level");
          levelString = levelString.toUpperCase();
  
          // make sure this works
          Level level = Level.getLevel(levelString);

          String appender = GrouperConfig.retrieveConfig().propertyValueString("grouper.logger." + loggingConfigId + ".appender");
          appender = GrouperUtil.defaultIfBlank(appender, defaultAppender);
          GrouperUtil.assertion(!StringUtils.isBlank(appender), "Appender is required since default appender not found (stderr or grouper_error)");
          GrouperUtil.assertion(appenders.contains(appender), "Appender '" + appender + "' not found in log4j2.xml");
  
          newLoggingCustomizationsNameLevelAppenders.add(new MultiKey(name, levelString, appender));
          
          Level existingLevel = newLoggingCustomizationsNameToLevel.get(name);
          if (existingLevel == null || level.isLessSpecificThan(existingLevel)) {
            newLoggingCustomizationsNameToLevel.put(name, level);
          }
  
          
        } catch (Exception e) {
          LOG.error("Invalid logging config: '" + loggingConfigId + "'", e);
        }
        
      }
  
      Set<MultiKey> appendersToAdd = new HashSet<MultiKey>(newLoggingCustomizationsNameLevelAppenders);
      appendersToAdd.removeAll(currentLoggingCustomizationsNameLevelAppender);
      
      Set<MultiKey> appendersToDelete = new HashSet<MultiKey>(currentLoggingCustomizationsNameLevelAppender);
      appendersToDelete.removeAll(newLoggingCustomizationsNameLevelAppenders);
  
      debugMap.put("appendersToAdd", GrouperUtil.length(appendersToAdd));
      debugMap.put("appendersToDelete", GrouperUtil.length(appendersToDelete));
      
      if (appendersToAdd.size() > 0 || appendersToDelete.size() > 0) {
        
        final LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        final Configuration configuration = loggerContext.getConfiguration();
  
        for (MultiKey multiKey : appendersToAdd) {
          String name = (String)multiKey.getKey(0);
          debugMap.put("addingLogger_" + name, true);
          String appender = (String)multiKey.getKey(2);

          LOG.warn("Dynamically adding logger config '" + name + "', appender: '" + appender + "'");

          // get the least specific level
          Level level = newLoggingCustomizationsNameToLevel.get(name);

          debugMap.put("level_" + name, level);
          GrouperUtil.assertion(level != null, "level is null!  shouldnt be possible");
          LoggerConfig loggerConfig = configuration.getLoggerConfig(name);
          
          boolean needsToAddLogger = loggerConfig == null || !StringUtils.equals(name, loggerConfig.getName());

          debugMap.put("needsToAddLogger_" + name, needsToAddLogger);

          boolean needsToChangeLogger = !needsToAddLogger && (loggerConfig.getLevel() == null || level.isLessSpecificThan(loggerConfig.getLevel()));

          debugMap.put("needsToChangeLogger_" + name, needsToChangeLogger);
          debugMap.put("appender_" + name, appender);

          // maybe we dont need a change
          if (needsToAddLogger || needsToChangeLogger) {
            Level currentLevel = null;
            if (needsToAddLogger) {
              AppenderRef ref = AppenderRef.createAppenderRef(appender, null, null);
              AppenderRef[] refs = new AppenderRef[] {ref};
              loggerConfig = LoggerConfig.createLogger(false, level, name, "true", refs, null, configuration, null);
              
              loggerConfig.addAppender(configuration.getAppender(appender), level, null);
              configuration.addLogger(loggerConfig.getName(), loggerConfig);

            } else {
  
              // keep track of previous level
              currentLevel = loggerConfig.getLevel();
              
              debugMap.put("currentLevel_" + name, currentLevel);
            }
            if (!previousLoggingNameToLevel.containsKey(name)) {
              previousLoggingNameToLevel.put(name, currentLevel);
            }

            loggerConfig.setLevel(level);
  
          }
        }
        for (MultiKey multiKey : appendersToDelete) {
          String name = (String)multiKey.getKey(0);
          debugMap.put("needsToRemoveLogger_" + name, true);
          LOG.warn("Dynamically removing logger config '" + name + "'");
          String appender = (String)multiKey.getKey(2);
  
          // get the least specific level
          LoggerConfig loggerConfig = configuration.getLoggerConfig(name);

          Level previousLevel = newLoggingCustomizationsNameToLevel.get(name);
          if (previousLevel == null) {
            previousLevel = previousLoggingNameToLevel.get(name);
            debugMap.put("revertingToOriginalConfig_" + name, previousLevel);
          } else {
            debugMap.put("stillContainsConfig_" + name, previousLevel);
          }
          
          boolean loggerExists = loggerConfig != null && StringUtils.equals(name, loggerConfig.getName());

          debugMap.put("loggerExists_" + name, loggerExists);

          boolean needsToRemoveLogger = loggerExists && previousLevel == null;

          debugMap.put("needsToRemoveLogger_" + name, needsToRemoveLogger);

          boolean needsToChangeLogger = loggerExists && !needsToRemoveLogger;

          debugMap.put("needsToChangeLogger_" + name, needsToChangeLogger);

          if (needsToRemoveLogger || needsToChangeLogger) {
  
            // remove last item from list of previous
            if (!newLoggingCustomizationsNameToLevel.containsKey(name)) {
              previousLoggingNameToLevel.remove(name);
            }
            if (needsToRemoveLogger) {
  
              configuration.removeLogger(loggerConfig.getName());
              
            } else if (needsToChangeLogger) {
  
              // keep track of previous level
              loggerConfig.setLevel(previousLevel);
            }
          }
        }
        loggerContext.updateLoggers();
        // keep track of new list
        currentLoggingCustomizationsNameLevelAppender = new HashSet<MultiKey>(newLoggingCustomizationsNameLevelAppenders);
      }
    } catch (Exception e) {
      LOG.error("exception", e);
    } finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }

  }
}
