package edu.internet2.middleware.grouper.app.deprovisioning;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperSourceAdapter;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.subj.InternalSourceAdapter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.provider.SourceManager;

/**
 * attestation daemon
 */
@DisallowConcurrentExecution
public class GrouperDeprovisioningJob extends OtherJobBase {

  /**
   * enter a group or the group which controls a loader job
   * @param group
   * @return true if group should be deprovisioned
   */
  public static boolean deprovisionGroup(Group group) {
    
    //TODO fill in logic
    
    return true;
  }
  
  
  /**
   * if deprovisioning is enabled
   * @return
   */
  public static boolean deprovisioningEnabled() {
    return GrouperConfig.retrieveConfig().propertyValueBoolean("deprovisioning.enable", true);
    
  }
  
  /**
   * group that users who are allowed to deprovision other users are in
   * @return the group name
   */
  public static String retrieveDeprovisioningManagersMustBeInGroupName() {
    
    // group that users who are allowed to deprovision other users are in
    String deprovisioningMustBeInGroupName = GrouperConfig.retrieveConfig().propertyValueStringRequired("deprovisioning.managers.must.be.in.group");
    
    return deprovisioningMustBeInGroupName;
    
  }

  /**
   * group name which has been deprovisioned
   * @return the group name
   */
  public static String retrieveGroupNameWhichHasBeenDeprovisioned() {
    
    // group that deprovisioned users go in (temporarily, but history will always be there)
    String deprovisioningGroupWhichHasBeenDeprovisionedName = GrouperConfig.retrieveConfig().propertyValueStringRequired("deprovisioning.group.which.has.been.deprovisioned");
    
    return deprovisioningGroupWhichHasBeenDeprovisionedName;
  }

  /**
   * run the daemon
   * @param args
   */
  public static void main(String[] args) {
    runDaemonStandalone();
  }

  /**
   * run standalone
   */
  public static void runDaemonStandalone() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
    
    hib3GrouperLoaderLog.setHost(GrouperUtil.hostname());
    String jobName = "OTHER_JOB_deprovisioningDaemon";

    hib3GrouperLoaderLog.setJobName(jobName);
    hib3GrouperLoaderLog.setJobType(GrouperLoaderType.OTHER_JOB.name());
    hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
    hib3GrouperLoaderLog.store();
    
    OtherJobInput otherJobInput = new OtherJobInput();
    otherJobInput.setJobName(jobName);
    otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
    otherJobInput.setGrouperSession(grouperSession);
    new GrouperDeprovisioningJob().run(otherJobInput);
  }

  /**
   * cache the sources allowed for a tad
   */
  private static ExpirableCache<Boolean, Set<Source>> retrieveSourcesAllowedToDeprovisionCache = new ExpirableCache<Boolean, Set<Source>>();
  
  /**
   * get the sources to deprovision, dont include the group source or the internal source
   * @return the sources to deprovision
   */
  public static Set<Source> retrieveSourcesAllowedToDeprovision() {
    
    Set<Source> result = retrieveSourcesAllowedToDeprovisionCache.get(Boolean.TRUE);
    
    if (result == null) {
    
      synchronized(retrieveSourcesAllowedToDeprovisionCache) {
  
        result = retrieveSourcesAllowedToDeprovisionCache.get(Boolean.TRUE);
        
        if (result == null) {
          result = new LinkedHashSet<Source>();
          
          for (Source source : SourceManager.getInstance().getSources()) {
            if (StringUtils.equals(source.getId(), GrouperSourceAdapter.groupSourceId())) {
              continue;
            }
            if (StringUtils.equals(source.getId(), InternalSourceAdapter.ID)) {
              continue;
            }
            result.add(source);
          }
          
          retrieveSourcesAllowedToDeprovisionCache.put(Boolean.TRUE, result);
        }
        
      }
    }
    
    return result;
  }
  
  /**
   * get the list of recently deprovisioned users
   * @return the list of members
   */
  public static Set<Member> retrieveRecentlyDeprovisionedUsers() {
    
    //switch over to admin so attributes work
    return (Set<Member>)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        Group deprovisionedGroup = GroupFinder.findByName(grouperSession, retrieveGroupNameWhichHasBeenDeprovisioned(), true);
        
        Set<Member> members = deprovisionedGroup.getMembers();
        return members;
      }
    });

  }
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperDeprovisioningJob.class);
  
  /**
   * @see edu.internet2.middleware.grouper.app.loader.OtherJobBase#run(edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput)
   */
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    
    if (!deprovisioningEnabled()) {
      LOG.debug("Deprovisioning is not enabled!  Quitting daemon!");
      return null;
    }
    
    return null;
  }
  

}
