package edu.internet2.middleware.grouper.app.deprovisioning;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperDaemonUtils;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobOutput;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

@DisallowConcurrentExecution
public class GrouperDeprovisioningCleanupDeprovisionedUsersJob extends OtherJobBase {
  
  private static final Log LOG = GrouperUtil.getLog(GrouperDeprovisioningCleanupDeprovisionedUsersJob.class);
  
  public static void cleanup() {
    
    //get all affiliations
    
    //get all groups, stems, attribute defs etc that have deprovisioning configured for all the affiliations
    
    //check the memberships of the groups, stems, attribute defs from above and 
    
    // check if those people are in usersThatHaveBeenDeprovisioned_ groups and remove if needed
    
    Set<String> affiliations = GrouperDeprovisioningAffiliation.retrieveAllAffiliations().keySet();
    
    for (String affiliation: affiliations) {
      GrouperDaemonUtils.stopProcessingIfJobPaused();

      Set<Subject> deprovisionedSubjectsForAffiliation = GrouperDeprovisioningLogic.deprovisionedSubjectsForAffiliation(affiliation, true);
    }
    
    Map<String,Map<String,GrouperDeprovisioningObjectAttributes>> foldersOfInterestForDeprovisioning = GrouperDeprovisioningDaemonLogic.retrieveAllFoldersOfInterestForDeprovisioning();
    GrouperDaemonUtils.stopProcessingIfJobPaused();

    Map<String,Map<String,GrouperDeprovisioningObjectAttributes>> groupsOfInterestForDeprovisioning = GrouperDeprovisioningDaemonLogic.retrieveAllGroupsOfInterestForDeprovisioning(foldersOfInterestForDeprovisioning);
    GrouperDaemonUtils.stopProcessingIfJobPaused();

    Map<String,Map<String,GrouperDeprovisioningObjectAttributes>> attributeDefsOfInterestForDeprovisioning = GrouperDeprovisioningDaemonLogic.retrieveAllAttributeDefsOfInterestForDeprovisioning();
    GrouperDaemonUtils.stopProcessingIfJobPaused();
    
    for (String affiliation: foldersOfInterestForDeprovisioning.keySet()) {
      GrouperDaemonUtils.stopProcessingIfJobPaused();

      Map<String, GrouperDeprovisioningObjectAttributes> stemNameToDeprovAttributes = foldersOfInterestForDeprovisioning.get(affiliation);
      
      for (String stemName : stemNameToDeprovAttributes.keySet()) {
        Stem stemOnWhichAttributesAreAssigned = StemFinder.findByName(GrouperSession.staticGrouperSession(), stemName, true);
      }
    }
    
  }

  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    try {
      cleanup();
      //otherJobInput.getHib3GrouperLoaderLog().setJobMessage("Finished successfully running deprovisioning full sync logic daemon. \n "+GrouperUtil.mapToString(debugMap));
    } catch (Exception e) {
      LOG.warn("Error while running deprovisioning clean up job", e);
      otherJobInput.getHib3GrouperLoaderLog().setJobMessage("Finished running deprovisioning clean up daemon with an error: " + ExceptionUtils.getFullStackTrace(e));
    } finally {
      otherJobInput.getHib3GrouperLoaderLog().store();
    }
    return null;
  }
}
