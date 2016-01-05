/**
 * @author mchyzer $Id$
 */
package edu.internet2.middleware.grouperActivemq.permissions;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jivesoftware.smack.packet.Message;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import edu.internet2.middleware.grouperActivemq.config.GrouperActivemqConfig;
import edu.internet2.middleware.grouperActivemq.utils.GrouperActivemqUtils;
import edu.internet2.middleware.grouperClient.api.GcGetPermissionAssignments;
import edu.internet2.middleware.grouperClient.api.GcGetSubjects;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetPermissionAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsPermissionAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppMain;
import edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppMessageHandler;

/**
 * keep track of all permissions.  It retrieves permissions from Grouper, 
 * caches them in memory, and periodically stores them to disk too.  There are two files 
 * on disk, in case one doesnt get written fully.  At the end of the file is a success
 * flag so we know it is complete.  It is failsafe, if Grouper has a problem, and there
 * is a copy in memory or disk, then it will still work.  I believe the disk store is
 * only read on startup, but Im not sure.
 */
public class GrouperActivemqPermissionsEngine implements Job, StatefulJob {

  /**
   * logger
   */
  private static Log log = GrouperClientUtils
      .retrieveLog(GrouperActivemqPermissionsEngine.class);

  /**
   * all permissions for activemq
   * @return the permissions
   */
  static GrouperActivemqPermissionsEngine permissions() {
    
    startupOnce();
    
    GrouperActivemqPermissionsEngine grouperActivemqPermissionsEngineLocal = grouperActivemqPermissionsEngine; 
    
    if (grouperActivemqPermissionsEngineLocal == null) {
      synchronized (GrouperActivemqPermissionsEngine.class) {
        grouperActivemqPermissionsEngineLocal = grouperActivemqPermissionsEngine;
        if (grouperActivemqPermissionsEngineLocal == null) {

          performFullRefresh();
          
          grouperActivemqPermissionsEngineLocal = grouperActivemqPermissionsEngine;
          
          if (grouperActivemqPermissionsEngineLocal == null) {
            throw new RuntimeException("No permissions from Grouper or local cache????");
          }
        }
        
      }
    }    
    return grouperActivemqPermissionsEngineLocal;
    
  }

  /**
   * if the user has any permission
   * @param user is the user to check
   * @return true if the user has any permission
   */
  public static boolean hasAnyPermission(String user) {
    return hasAnyPermissionHelper(permissions(), user);
  }

  /**
   * if the user has any permission
   * @param localGrouperActivemqPermissionsEngine 
   * @param user is the user to check
   * @return true if the user has any permission
   */
  static boolean hasAnyPermissionHelper(GrouperActivemqPermissionsEngine localGrouperActivemqPermissionsEngine, String user) {
    return GrouperClientUtils.length(localGrouperActivemqPermissionsEngine.getCachedGrouperActivemqPermissions().get(user)) > 0;
  }

  /**
   * whitelist of permissions for internal things in ActiveMQ that users need to be able to do
   */
  private static Set<GrouperActivemqPermission> whitelist;
  
  static {
    try {
      whitelist = new HashSet<GrouperActivemqPermission>();
      whitelist.add(new GrouperActivemqPermission(GrouperActivemqPermissionAction.createDestination, "ActiveMQ.Advisory.Connection"));
      whitelist.add(new GrouperActivemqPermission(GrouperActivemqPermissionAction.receiveMessage, "ActiveMQ.Advisory.TempTopic"));
      whitelist.add(new GrouperActivemqPermission(GrouperActivemqPermissionAction.receiveMessage, "ActiveMQ.Advisory.TempQueue"));
    } catch (RuntimeException re) {
      log.error("Error loading whitelist", re);
      throw re;
    }
  }
  

  /**
   * all permissions for activemq.  See if the user has the action and destination.
   * Note, if there is an internal permissions in ActiveMQ for the user, it is allowed (if supposed to be),
   * and if there is a hierarchical "inherit" permission for an ancestor, it is permitted
   * @param user loginid to activemq
   * @param action action to check (note, if you just have sendMessage,
   * it will check the inherit destinations too
   * @param destination to check
   * @return the permissions
   */
  public static boolean hasPermission(String user, GrouperActivemqPermissionAction action,
      String destination) {
    
    if (destination.startsWith("ActiveMQ.Advisory.Consumer.Queue.")) {
      return hasPermission(user, action, destination.substring("ActiveMQ.Advisory.Consumer.Queue.".length()));
    }
    
    //lets see if someone has a permission from cache
    GrouperActivemqPermissionsEngine engine = permissions();
    
    boolean useCache = cachePermissionsMinutes() != 0;
    
    GrouperActivemqPermission grouperActivemqPermission = new GrouperActivemqPermission(action, 
        destination);
    
    if (whitelist.contains(grouperActivemqPermission)) {
      return true;
    }
    
    if (useCache) {
      
      Map<GrouperActivemqPermission, Boolean> userPermissions = engine.getCachedPermissionDecisions().get(user);
      
      if (userPermissions != null) {
        
        Boolean result = userPermissions.get(grouperActivemqPermission);
        if (result != null) {
          return result;
        }
      }
    }

    //not in cache, get the real answer
    boolean result = engine.hasPermissionNoCache(user, action, destination);
    
    if (useCache) {
      
      ExpirableCache<String, Map<GrouperActivemqPermission, Boolean>> cachedPermissionDecisions2 = engine.getCachedPermissionDecisions();
      
      if (cachedPermissionDecisions2 != null) {
        Map<GrouperActivemqPermission, Boolean> userPermissions = cachedPermissionDecisions2.get(user);
        
        if (userPermissions == null) {
          userPermissions = new HashMap<GrouperActivemqPermission, Boolean>();
          cachedPermissionDecisions2.put(user, userPermissions);
        }
        
        userPermissions.put(grouperActivemqPermission, result);
        
      }      
    }
    
    return result;
  }

  /**
   * @return number of minutes to cache decisions
   */
  private static int cachePermissionsMinutes() {
    return GrouperActivemqConfig.retrieveConfig().propertyValueInt("grouperActivemq.cachePermissionDecisionsForMinutes", 5);
  }

  /**
   * all permissions for activemq
   * @param user loginid to activemq
   * @param action action to check (note, if you just have sendMessage, it will check the inherit destinations too
   * @param destination to check
   * @return the permissions
   */
  boolean hasPermissionNoCache(String user, GrouperActivemqPermissionAction action,
      String destination) {
    Map<String, Set<GrouperActivemqPermission>> allPermissions = this.getCachedGrouperActivemqPermissions();
    return hasPermissionHelper(allPermissions, user, action, destination);
  }

  /**
   * all permissions for activemq, can easily be testable
   * @param allPermissions 
   * @param user loginid to activemq
   * @param action action to check (note, if you just have sendMessage, it will check the inherit destinations too
   * @param destination to check
   * @return the permissions
   */
  static boolean hasPermissionHelper(Map<String, Set<GrouperActivemqPermission>> allPermissions, String user, GrouperActivemqPermissionAction action, String destination) {
    
    //get the permissions for a user
    Set<GrouperActivemqPermission> permissionsForUser = allPermissions.get(user);
    
    if (permissionsForUser == null) {
      return false;
    }
    
    for (GrouperActivemqPermission permission : permissionsForUser) {
      
      //see if that is the exact permission
      if (permission.getAction() == action && GrouperClientUtils.equals(permission.getDestination(), destination)) {
        return true;
      }

      //lets see if this is a inherited action
      if (GrouperClientUtils.equals(permission.getDestination(), destination)
          //if the permission destination is further up the hierarchy then the 
          //requested destination, then we are all good
          || destination.startsWith(permission.getDestination()+":")) {
        if (action.actionAllowedIfArgumentAllowed(permission.getAction())) {
          return true;
        }
      }
      
    }
    
    return false;
    
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

    log.debug("Starting grouper activemq permissions daemon");

    //schedule full refresh job on startup
    //performFullRefresh();

    Map<String, Set<GrouperActivemqPermission>> permissionMap = readPermissionsFromFileCache();
    System.out.println(GrouperClientUtils.toStringForLog(permissionMap));
    
    scheduleQuartzJob();

    //do xmpp loop
    //xmppLoop();

  }

  /** if we have called the startup yet */
  private static boolean started = false;
  
  /**
   * 
   */
  public static void startupOnce() {

    if (started) {
      return;
    }
    
    //do not synchronize the entire method so that all calls are not synchronized
    synchronized(GrouperActivemqPermissionsEngine.class) {
      
      if (started) {
        return;
      }

      started = true;
      
      log.debug("Starting grouper activemq permissions daemon");

      //schedule full refresh job on startup
      try {
        performFullRefresh();
      } catch (RuntimeException e) {
        //do not throw so this is always available if Grouper is down
        log.error("Error on startup", e);
      }

      try {
        scheduleQuartzJob();
      } catch (RuntimeException e) {
        //do not throw so this is always available if Grouper is down.  Though not sure why
        //quartz schedule would fail... hmm
        log.error("Error on startup", e);
      }
      Thread thread = new Thread(new Runnable() {

        @Override
        public void run() {
          try {
            //do xmpp loop
            xmppLoop();
          } catch (Exception e) {
            log.error("Error with xmpp", e);
          }
        }
        
      });
      thread.start();
    }
  }

  /**
   * schedule the full refresh job, e.g. nightly
   */
  private static void scheduleQuartzJob() {

    String jobName = "clusterActivemqFullRefreshJob";

    String quartzCronString = GrouperActivemqConfig.retrieveConfig().propertyValueString(
        "grouperActivemq.fullRefreshQuartzCron");

    if (!GrouperClientUtils.isBlank(quartzCronString)) {
    
      log.debug("Scheduling Grouper Activemq permissions daemon for quartzCron string: "
          + quartzCronString);
      
      GrouperClientXmppMain.scheduleJob(jobName, quartzCronString, GrouperActivemqPermissionsEngine.class);

    } else {

      log.debug("Not scheduling Grouper Activemq permissions daemon since not configured");
    
    }
  }

  /** timer */
  private static Timer timer = null;

  /** timer scheduled for */
  private static long timerScheduledFor = -1;

  /**
   * do a full refresh in one minute (batch subsequent requests)
   */
  static void scheduleFullRefresh() {

    //if it is already scheduled, then we are all good
    if (timer != null) {

      log.debug("Job is already scheduled at " + new Date(timerScheduledFor).toString()
          + ", exiting");

      return;
    }
    timer = new Timer(true);

    int timeInFuture = 1000 * 60;

    timerScheduledFor = System.currentTimeMillis() + timeInFuture;

    //schedule in 60 seconds
    timer.schedule(new TimerTask() {

      @Override
      public void run() {
        try {
          performFullRefresh();
        } catch (RuntimeException e) {
          log.error("Error performing refresh", e);
          throw e;
        }
      }
    }, timeInFuture);
  }

  /**
   * base folder for grouper
   * @return the folder, e.g. school:apps:clusterLinux
   */
  static String grouperFolderBase() {
    return GrouperClientUtils.propertiesValue("clusterLinux.grouperFolderBase", true);
  }

  /**
   * @param cachedGrouperActivemqPermissions1
   */
  public GrouperActivemqPermissionsEngine(
      Map<String, Set<GrouperActivemqPermission>> cachedGrouperActivemqPermissions1) {
    this.cachedGrouperActivemqPermissions = cachedGrouperActivemqPermissions1;
    int minutesToCacheDecisions = cachePermissionsMinutes();
    this.cachedPermissionDecisions = minutesToCacheDecisions == 0 ? null 
        : new ExpirableCache<String, Map<GrouperActivemqPermission, Boolean>>(minutesToCacheDecisions);
  }

  /**
   * cached engine from grouper or file (if grouper is not available)
   */
  private static GrouperActivemqPermissionsEngine grouperActivemqPermissionsEngine = null;
  
  /**
   * cache grouper activemq permissions
   */
  private Map<String, Set<GrouperActivemqPermission>> cachedGrouperActivemqPermissions = null;
  
  /**
   * cache permission decisions by user and then by permission
   */
  private ExpirableCache<String, Map<GrouperActivemqPermission, Boolean>> cachedPermissionDecisions = null;
  
  
  /**
   * @return the cachedGrouperActivemqPermissions
   */
  private Map<String, Set<GrouperActivemqPermission>> getCachedGrouperActivemqPermissions() {
    return this.cachedGrouperActivemqPermissions;
  }

  
  /**
   * @return the cachedPermissionDecisions
   */
  private ExpirableCache<String, Map<GrouperActivemqPermission, Boolean>> getCachedPermissionDecisions() {
    return this.cachedPermissionDecisions;
  }

  
  /**
   * do a full refresh in one minute (batch subsequent requests).  Note, if cant get from grouper, then get from file
   */
  static synchronized void performFullRefresh() {
    
    if (log.isDebugEnabled()) {
      log.debug("Performing full refresh of Grouper ActiveMQ permissions");
    }
    
    Map<String, Set<GrouperActivemqPermission>> userToPermissionMap = null;
    
    try {
      userToPermissionMap = retrievePermissionsFromGrouper();
      
    } catch (Exception e) {
      log.error("Error getting objects from grouper", e);
    }

    //got it, save to cache and file
    if (userToPermissionMap != null) {
      
      //rewrite the cache
      grouperActivemqPermissionsEngine = new GrouperActivemqPermissionsEngine(userToPermissionMap);
      writePermissionsToFile(userToPermissionMap);

    } else {
      
      //couldnt get from grouper, read again from file
      userToPermissionMap = readPermissionsFromFileCache();
      grouperActivemqPermissionsEngine = new GrouperActivemqPermissionsEngine(userToPermissionMap);
      
    }
    
  }
  
  /**
   * do a full refresh in one minute (batch subsequent requests)
   * @return the map of user to set of permissions
   */
  static synchronized Map<String, Set<GrouperActivemqPermission>> retrievePermissionsFromGrouper() {

    //let another timer be scheduled, whether from schedule or xmpp
    timer = null;

    GcGetPermissionAssignments gcGetPermissionAssignments = new GcGetPermissionAssignments();
    
    {
      String nameOfAttributeDefsString = GrouperActivemqConfig.retrieveConfig().propertyValueStringRequired("grouperActivemq.permissionDefinitions");
      
      List<String> namesOfAttributeDefs = GrouperClientUtils.splitTrimToList(nameOfAttributeDefsString, ","); 

      for (String nameOfAttributeDef : namesOfAttributeDefs) {
        gcGetPermissionAssignments.addAttributeDefName(nameOfAttributeDef);
      }
      
    }
    
    {
      
      String theRoleNamesString = GrouperActivemqConfig.retrieveConfig().propertyValueStringRequired("grouperActivemq.roleNames");

      List<String> roleNames = GrouperClientUtils.splitTrimToList(theRoleNamesString, ","); 
      
      for (String roleName : roleNames) {
        gcGetPermissionAssignments.addRoleName(roleName);
      }
      
    }
    Map<String, String> sourceToLoginIdAttributeName = GrouperActivemqConfig
      .retrieveConfig().subjectSourceToLoginAttributeName();
    
    for (String loginAttributeName : sourceToLoginIdAttributeName.values()) {
      if (!GrouperClientUtils.equals("id", loginAttributeName) && !GrouperClientUtils.equals("name", loginAttributeName)) {
        gcGetPermissionAssignments.addSubjectAttributeName(loginAttributeName);
      }
      //hmm, cant add source
    }
    
    //add actions, dont worry about rollups
    for (GrouperActivemqPermissionAction grouperActivemqPermissionAction : GrouperActivemqPermissionAction.values()) {
      gcGetPermissionAssignments.addAction(grouperActivemqPermissionAction.name());
    }
    
    WsGetPermissionAssignmentsResults wsGetPermissionAssignmentsResults = gcGetPermissionAssignments.execute();

    if (log.isDebugEnabled()) {
      log.debug("Received "
            + GrouperClientUtils.length(wsGetPermissionAssignmentsResults
                .getWsPermissionAssigns())
            + " permission entries from Grouper");
    }

    WsSubject[] wsSubjects = wsGetPermissionAssignmentsResults.getWsSubjects();
    
    if (GrouperActivemqConfig.retrieveConfig().propertyValueBoolean("grouperActivemq.separateSubjectQuery")) {

      wsSubjects = retrieveSubjectsFromServer(wsGetPermissionAssignmentsResults, sourceToLoginIdAttributeName.values());
      
    }
    
    Map<String, Set<GrouperActivemqPermission>> userToPermissions = new TreeMap<String, Set<GrouperActivemqPermission>>();
    
    Map<String, String> loginidToSource = new HashMap<String, String>();
    
    String folderPrefix = GrouperActivemqConfig.retrieveConfig().propertyValueString("grouperActivemq.folderPrefix");
    String requireBaseFolder = GrouperActivemqConfig.retrieveConfig().propertyValueString("grouperActivemq.requireBaseFolder");
    
    for (WsPermissionAssign wsPermissionAssign : GrouperClientUtils.nonNull(
          wsGetPermissionAssignmentsResults.getWsPermissionAssigns(),
        WsPermissionAssign.class)) {
      
      //get the subject
      WsSubject wsSubject = retrieveSubject(
            wsSubjects, wsPermissionAssign.getSourceId(),
            wsPermissionAssign.getSubjectId());

      if (wsSubject == null) {
        throw new RuntimeException("Why is wsSubject null??? "
            + wsPermissionAssign.getSourceId()
            + wsPermissionAssign.getSubjectId());
      }

      String permissionName = wsPermissionAssign.getAttributeDefNameName();
      
      //lets throw out permissions that arent in the right namespace
      if (!GrouperClientUtils.isBlank(requireBaseFolder)) {
        if (!permissionName.startsWith(requireBaseFolder)) {
          if (log.isDebugEnabled()) {
            log.debug("Skipping " + permissionName + " since it is not in requireBaseFolder: " + requireBaseFolder);
          }
          continue;
        }
      }
      
      //strip out the prefix if configured
      if (!GrouperClientUtils.isBlank(folderPrefix)) {
        if (!permissionName.startsWith(folderPrefix)) {
          if (log.isDebugEnabled()) {
            log.debug("Skipping " + permissionName + " since it is not in folderPrefix: " + folderPrefix);
          }
          continue;
        }
        
        permissionName = permissionName.substring(folderPrefix.length());
        
      }
      
      String loginIdAttributeName = sourceToLoginIdAttributeName.get(wsSubject.getSourceId());
      
      if (GrouperClientUtils.isBlank(loginIdAttributeName)) {
        String error = "Why is there no grouperActivemq.subjectSource.X.subjectAttributeForLogin for the source: " + wsSubject.getSourceId();
        log.error(error);
        throw new RuntimeException(error);
      }
      
      //lets get the loginid
      String loginid = GrouperActivemqUtils.subjectAttributeValue(wsSubject,
            wsGetPermissionAssignmentsResults.getSubjectAttributeNames(), loginIdAttributeName);

      //not sure why there wouldnt be a pennname, but if not, then skip
      if (GrouperClientUtils.isBlank(loginid)) {
        if (log.isInfoEnabled()) {
          log.info("There is no loginid attribute: " + loginIdAttributeName 
              + " for the subject (source: " + wsSubject.getSourceId() + ", subjectId: " + wsSubject.getId() + ")");
        }
        continue;
      }

      //make sure there arent overlap of login ids across multiple sources
      {
        String expectedSource = loginidToSource.get(wsSubject.getSourceId());
        if (GrouperClientUtils.isBlank(expectedSource)) {
          loginidToSource.put(loginid, wsSubject.getSourceId());
        } else {
          if (!GrouperClientUtils.equals(expectedSource, wsSubject.getSourceId())) {
            String error = "Error, there is a subject with login id: " + loginid + " in source: " 
              + expectedSource + ", and in source: " + wsSubject.getSourceId();
            log.error(error);
            throw new RuntimeException(error);
          }
        }
        
      }
      
      //ok, we have login, permision, action, lets keep track
      Set<GrouperActivemqPermission> permissionsForUser = userToPermissions.get(loginid);
      
      if (permissionsForUser == null) {
        permissionsForUser = new TreeSet<GrouperActivemqPermission>();
        userToPermissions.put(loginid, permissionsForUser);
      }
      
      GrouperActivemqPermission grouperActivemqPermission = new GrouperActivemqPermission();
      GrouperActivemqPermissionAction grouperActivemqPermissionAction = GrouperActivemqPermissionAction.valueOfIgnoreCase(wsPermissionAssign.getAction(), true);
      grouperActivemqPermission.setAction(grouperActivemqPermissionAction);
      grouperActivemqPermission.setDestination(permissionName);
      
      permissionsForUser.add(grouperActivemqPermission);
    }
    
    //update the memory cache
    //cachedGrouperActivemqPermissions = userToPermissions;
    
    return userToPermissions;

  }

  /**
   * write permissions to a file
   * @param userToPermissions
   */
  static void writePermissionsToFile(Map<String, Set<GrouperActivemqPermission>> userToPermissions) {
    
    //lets make the permissions file
    StringBuilder fileContents = new StringBuilder(
        "# automatically generated from Grouper-ActiveMQ connector...\n\n");

    //  # automatically generated from Grouper-ActiveMQ connector
    //
    //  activemq.permission.0.user = mchyzer
    //  activemq.permission.0.permission.0 = read__whatever
    //  activemq.permission.0.permission.1 = write__whatever:whatever
    //
    //  # if this is here, the file is complete
    //  activemq.permissionSuccess = true
    
    int userIndex = 0;
    for (String userId : userToPermissions.keySet()) {
      fileContents.append("activemq.permission.").append(userIndex).append(".user = ").append(userId).append("\n");
      
      Set<GrouperActivemqPermission> grouperActivemqPermissions = userToPermissions.get(userId);
      
      int permissionIndex = 0;
      for (GrouperActivemqPermission grouperActivemqPermission : grouperActivemqPermissions) {
        fileContents.append("activemq.permission.").append(userIndex).append(".permission.")
          .append(permissionIndex).append(" = ").append(grouperActivemqPermission.getAction())
          .append("__").append(grouperActivemqPermission.getDestination()).append("\n");
        permissionIndex++;
      }
      
      userIndex++;
    }

    fileContents.append("\n# if this is here, the file is complete\nactivemq.permissionSuccess = true\n");
    
    if (log.isDebugEnabled()) {
      log.debug("Generated file has "
            + fileContents.length()
            + " bytes");
    }

    File fileToSave = fileToSave();

    //save this to file (try 3 times)
    for (int i = 0; i < 3; i++) {

      try {

        boolean updated = GrouperClientUtils.saveStringIntoFile(fileToSave,
            fileContents.toString(), true, true);
        if (updated) {
          log.debug("File: " + fileToSave.getAbsolutePath()
              + " was saved since there were updates from Grouper");
        } else {
          log.debug("File: " + fileToSave.getAbsolutePath()
              + " was not saved since there were no changes from Grouper");
        }

        //we are done
        break;
      } catch (Exception e) {
        log.error("Error saving file", e);
      }
      GrouperClientUtils.sleep(2000);

    }

  }
  
  /**
   * file to save, alternate between activemqGrouperPermissions.1.properties, 
   * and activemqGrouperPermissions.2.properties
   * @return the file
   */
  static File fileToSave() {
    
    String permissionsDirString = GrouperActivemqConfig.retrieveConfig()
      .propertyValueStringRequired("grouperActivemq.permissionsCacheDirectory");
    
    File permissionsDir = new File(permissionsDirString);
    if (!permissionsDir.exists() || !permissionsDir.isDirectory()) {
      throw new RuntimeException("Configured permissions cache dir doesnt exist: " +
      		"grouperActivemq.permissionsCacheDirectory: " + permissionsDir.getAbsolutePath());
    }

    if (!permissionsDirString.endsWith("/") && !permissionsDirString.endsWith("\\")) {
      permissionsDirString += File.separator;
    }
    
    //two files:
    File permissionsFile1 = new File(permissionsDirString + "activemqGrouperPermissions.1.properties");
    File permissionsFile2 = new File(permissionsDirString + "activemqGrouperPermissions.2.properties");

    //let's see if there is a #1 file
    if (!permissionsFile1.exists()) {
      return permissionsFile1;
    }
    
    //let's see if there is a #2 file
    if (!permissionsFile2.exists()) {
      return permissionsFile2;
    }
    
    {
      //let's see if the first file is not complete
      Properties firstFileProperties = GrouperClientUtils.propertiesFromFile(permissionsFile1);
      if (!GrouperClientUtils.propertiesValueBoolean(firstFileProperties, "activemq.permissionSuccess", false)) {
        return permissionsFile1;
      }
      
    }    
    
    {
      //let's see if the second file is not complete
      Properties secondFileProperties = GrouperClientUtils.propertiesFromFile(permissionsFile2);
      if (!GrouperClientUtils.propertiesValueBoolean(secondFileProperties, "activemq.permissionSuccess", false)) {
        return permissionsFile2;
      }
      
    }    
    
    //lets see which file is older, return that one
    if (permissionsFile1.lastModified() < permissionsFile2.lastModified()) {
      return permissionsFile1;
    }
    
    return permissionsFile2;

  }

  /**
   * pattern to match a permission in the permissions file: write__whatever:whatever
   */
  private static Pattern permissionFilePattern = Pattern.compile("^(.*?)__(.*)$");
  
  /**
   * read permissions from file cache.
   * 
   * @return the permissions or null if not found
   */
  static Map<String, Set<GrouperActivemqPermission>> readPermissionsFromFileCache() {
    
    Properties properties = fileToRead();
    
    if (properties == null) {
      return null;
    }
    
    Map<String, Set<GrouperActivemqPermission>> result = new HashMap<String, Set<GrouperActivemqPermission>>();
    
    //we have properties.  for loop so we dont have an endless loop somehow
    for (int userIndex=0;userIndex<properties.size();userIndex++) {
      
      //  # automatically generated from Grouper-ActiveMQ connector
      //
      //  activemq.permission.0.user = mchyzer
      //  activemq.permission.0.permission.0 = read__whatever
      //  activemq.permission.0.permission.1 = write__whatever:whatever
      //
      //  # if this is here, the file is complete
      //  activemq.permissionSuccess = true
      
      String user = GrouperClientUtils.propertiesValue(properties, "activemq.permission." + userIndex + ".user");
      if (GrouperClientUtils.isBlank(user)) {
        break;
      }
      
      Set<GrouperActivemqPermission> permissions = new HashSet<GrouperActivemqPermission>();
      
      result.put(user, permissions);
      
      for (int permissionIndex=0;permissionIndex<10000;permissionIndex++) {
        
        String permissionString = GrouperClientUtils.propertiesValue(properties, 
            "activemq.permission." + userIndex + ".permission." + permissionIndex);
        
        if (GrouperClientUtils.isBlank(permissionString)) {
          break;
        }
        
        Matcher matcher = permissionFilePattern.matcher(permissionString);
        if (!matcher.matches()) {
          //hmmm, what to do...
          log.error("Error: invalid permission in file: " + permissionString);
          //i guess keep going
          continue;
        }

        String actionString = matcher.group(1);
        String permissionResource = matcher.group(2);
        
        GrouperActivemqPermissionAction grouperActivemqPermissionAction 
          = GrouperActivemqPermissionAction.valueOfIgnoreCase(actionString, false);
        
        if (grouperActivemqPermissionAction == null) {
          log.error("Error: invalid action in file: " + actionString + ", " + permissionString);
          continue;
        }

        //ok, we have everything
        GrouperActivemqPermission grouperActivemqPermission = 
          new GrouperActivemqPermission(grouperActivemqPermissionAction, permissionResource);
        
        permissions.add(grouperActivemqPermission);
        
      }
      
    }
    
    return result;
  }
  
  /**
   * file to read, newest activemqGrouperPermissions.1.properties, 
   * or activemqGrouperPermissions.2.properties
   * @return the properties
   */
  static Properties fileToRead() {
    
    String permissionsDirString = GrouperActivemqConfig.retrieveConfig()
      .propertyValueStringRequired("grouperActivemq.permissionsCacheDirectory");
    
    File permissionsDir = new File(permissionsDirString);
    if (!permissionsDir.exists() || !permissionsDir.isDirectory()) {
      throw new RuntimeException("Configured permissions cache dir doesnt exist: " +
          "grouperActivemq.permissionsCacheDirectory: " + permissionsDir.getAbsolutePath());
    }

    if (!permissionsDirString.endsWith("/") && !permissionsDirString.endsWith("\\")) {
      permissionsDirString += File.separator;
    }
    
    //two files:
    File permissionsFile1 = new File(permissionsDirString + "activemqGrouperPermissions.1.properties");
    File permissionsFile2 = new File(permissionsDirString + "activemqGrouperPermissions.2.properties");

    boolean permissionsFile1candidate = true;
    boolean permissionsFile2candidate = true;
    
    //let's see if there is a #1 file
    if (!permissionsFile1.exists()) {
      permissionsFile1candidate = false;
    }
    
    //let's see if there is a #2 file
    if (!permissionsFile2.exists()) {
      permissionsFile2candidate = false;
    }
    
    Properties firstFileProperties = null;
    
    if (permissionsFile1candidate) {
      //let's see if the first file is not complete
      firstFileProperties = GrouperClientUtils.propertiesFromFile(permissionsFile1);
      if (!GrouperClientUtils.propertiesValueBoolean(firstFileProperties, "activemq.permissionSuccess", false)) {
        permissionsFile1candidate = false;
      }
      
    }    
    
    Properties secondFileProperties = null;
    if (permissionsFile2candidate) {
      //let's see if the second file is not complete
      secondFileProperties = GrouperClientUtils.propertiesFromFile(permissionsFile2);
      if (!GrouperClientUtils.propertiesValueBoolean(secondFileProperties, "activemq.permissionSuccess", false)) {
        permissionsFile2candidate = false;
      }

    }

    if (!permissionsFile1candidate && !permissionsFile2candidate) {
      return null;
    }

    if (!permissionsFile1candidate) {
      return firstFileProperties;
    }

    if (!permissionsFile2candidate) {
      return secondFileProperties;
    }

    //lets see which file is newer, return that one
    if (permissionsFile1.lastModified() > permissionsFile2.lastModified()) {
      return firstFileProperties;
    }

    return secondFileProperties;

  }

  
  
  /**
   * this will be run when the quertz nightly job fires
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */
  // @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {

    String jobName = null;
    try {
      jobName = context.getJobDetail().getKey().getName();
      if (log.isDebugEnabled()) {
        log.debug("Scheduling full refresh on job: " + jobName);
      }
      scheduleFullRefresh();
    } catch (RuntimeException re) {
      log.error("Error in job: " + jobName, re);
      throw re;
    }

  }

  /**
   * connect to xmpp, listen for messages from a certain sender (the grouper server)
   */
  private static void xmppLoop() {

    if (!GrouperActivemqConfig.retrieveConfig().propertyValueBoolean("grouperActivemq.doXmppLoop", true)) {
      return;
    }
    
    //this method loops with a try catch so it will never crash...
    GrouperClientXmppMain.xmppLoop(new GrouperClientXmppMessageHandler() {

      @Override
      public void handleMessage(Message message) {

        if (log.isDebugEnabled()) {
          log.debug("Received message: " + message.getBody());
        }
        
        //whatever message we get, we know it is from the right sender based on
        //config, so just schedule a full refresh a minute from now if its not already scheduled
        scheduleFullRefresh();

      }
    });

  }

  /**
   * until Penn is upgraded to Grouper 2.1, we need another query to get subjects based on bug in grouper.
   * Once this is fixed this wont be needed, the subjects will be in the permissions response
   * @param wsGetPermissionAssignmentsResults
   * @param subjectAttributeNames
   * @return subjects
   */
  static WsSubject[] retrieveSubjectsFromServer(
      WsGetPermissionAssignmentsResults wsGetPermissionAssignmentsResults, Collection<String> subjectAttributeNames) {
    
    //make sure no dupes
    Set<MultiKey> subjectSourceAndIds = new HashSet<MultiKey>();
    
    for (WsPermissionAssign wsPermissionAssign : wsGetPermissionAssignmentsResults.getWsPermissionAssigns()) {
      subjectSourceAndIds.add(new MultiKey(wsPermissionAssign.getSourceId(), wsPermissionAssign.getSubjectId()));
    }
    GcGetSubjects gcGetSubjects = new GcGetSubjects();
    
    for (String subjectAttributeName : subjectAttributeNames) {
      gcGetSubjects.addSubjectAttributeName(subjectAttributeName);
    }
    
    //add those to the get subjects request
    for (MultiKey subjectSourceAndId : subjectSourceAndIds) {
      gcGetSubjects.addWsSubjectLookup(new WsSubjectLookup((String)subjectSourceAndId.getKey(1),
          (String)subjectSourceAndId.getKey(0), null));
    }
    
    WsGetSubjectsResults wsGetSubjectsResults = gcGetSubjects.execute();
    
    return wsGetSubjectsResults.getWsSubjects();
    
  }

  /**
   * lookup a subject by subject id and source id
   * @param subjects
   * @param sourceId
   * @param subjectId
   * @return probably shouldnt be null, but if cant be found, then will be null
   */
  static WsSubject retrieveSubject(WsSubject[] subjects, String sourceId,
      String subjectId) {

    for (WsSubject wsSubject : GrouperClientUtils.nonNull(subjects, WsSubject.class)) {
      if (GrouperClientUtils.equals(sourceId, wsSubject.getSourceId())
           && GrouperClientUtils.equals(subjectId, wsSubject.getId())) {
        return wsSubject;
      }
    }
    return null;
  }

}
