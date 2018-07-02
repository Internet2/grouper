package edu.internet2.middleware.grouper.app.deprovisioning;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSourceAdapter;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.subj.InternalSourceAdapter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.provider.SourceManager;

/**
 * 
 */
public class GrouperDeprovisioningSettings {

  /**
   * if deprovisioning is enabled
   * @return if deprovisioning enabled
   */
  public static boolean deprovisioningEnabled() {
    // if turned off or if no affiliations then this is not enabled
    boolean deprovisioningEnabled = GrouperConfig.retrieveConfig().propertyValueBoolean("deprovisioning.enable", true) 
        && GrouperUtil.length(GrouperDeprovisioningAffiliation.retrieveDeprovisioningAffiliations()) > 0;
        
    return deprovisioningEnabled;
        
  }

  /**
   * 
   * @return the stem name with no last colon
   */
  public static String deprovisioningStemName() {
    return GrouperUtil.stripEnd(GrouperConfig.retrieveConfig().propertyValueString("deprovisioning.systemFolder", 
        GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects") + ":deprovisioning"), ":");
  }

  /**
   * users in this group who are admins of a affiliation but who are not Grouper SysAdmins
   * @return the group name
   */
  public static String retrieveDeprovisioningAdminGroupName() {
    
    // # users in this group who are admins of a affiliation but who are not Grouper SysAdmins, will be 
    // # able to deprovision from all grouper groups/objects, not just groups they have access to UPDATE/ADMIN
    // deprovisioning.admin.group = $$deprovisioning.systemFolder$$:deprovisioningAdmins
    return GrouperConfig.retrieveConfig().propertyValueString("deprovisioning.admin.group");
  
  }

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
   * cache the sources allowed for a tad
   */
  private static ExpirableCache<Boolean, Set<Source>> retrieveSourcesAllowedToDeprovisionCache = new ExpirableCache<Boolean, Set<Source>>();

}
