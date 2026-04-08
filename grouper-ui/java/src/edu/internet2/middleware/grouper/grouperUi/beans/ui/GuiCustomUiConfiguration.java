package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.app.customUi.CustomUiConfiguration;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.subject.Subject;

public class GuiCustomUiConfiguration {
  
  private CustomUiConfiguration customUiConfiguration;
  
  /**
   * cache of privs for custom ui
   */
  private static ExpirableCache<MultiKey, Boolean> subjectSourceSubjectIdGroupNameFieldNameCache = new ExpirableCache<MultiKey, Boolean>(2);

  private GuiCustomUiConfiguration(CustomUiConfiguration customUiConfiguration) {
    this.customUiConfiguration = customUiConfiguration;
  }
  
  public CustomUiConfiguration getCustomUiConfiguration() {
    return customUiConfiguration;
  }

  public static GuiCustomUiConfiguration convertFromCustomUiConfiguration(CustomUiConfiguration customUiConfiguration) {
    return new GuiCustomUiConfiguration(customUiConfiguration);
  }
  
  /**
   * check if the logged in user can run this custom ui config.
   * @return true if the logged in user can run
   */
  public boolean isCanRun() {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    String groupOfManagers = this.customUiConfiguration.retrieveAttributeValueFromConfig("groupOfManagers", false);
    
    if (!StringUtils.isBlank(groupOfManagers)) {
      return MembershipFinder.hasMemberCacheNoCheckSecurity(groupOfManagers, loggedInSubject);
    }
    
    // fall back to checking readers && updaters on the config's group
    Boolean canRun = (Boolean) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        String groupUuidOrName = GuiCustomUiConfiguration.this.customUiConfiguration.retrieveAttributeValueFromConfig("groupUUIDOrName", false);
        if (StringUtils.isBlank(groupUuidOrName)) {
          return false;
        }
        Group group = GroupFinder.findByUuid(grouperSession, groupUuidOrName, false);
        if (group == null) {
          group = GroupFinder.findByName(grouperSession, groupUuidOrName, false);
        }
        if (group == null) {
          return false;
        }
        
        MultiKey readerKey = new MultiKey(loggedInSubject.getSourceId(), loggedInSubject.getId(), group.getName(), "readers");
        MultiKey updaterKey = new MultiKey(loggedInSubject.getSourceId(), loggedInSubject.getId(), group.getName(), "updaters");
        Boolean readerResult = null;
        Boolean updaterResult = null;
      
        readerResult = subjectSourceSubjectIdGroupNameFieldNameCache.get(readerKey);
        updaterResult = subjectSourceSubjectIdGroupNameFieldNameCache.get(updaterKey);
        if (readerResult != null && updaterResult != null) {
          return readerResult && updaterResult;
        }
      
        readerResult = group.canHavePrivilege(loggedInSubject, "readers", false);
        updaterResult = group.canHavePrivilege(loggedInSubject, "updaters", false);
    
        subjectSourceSubjectIdGroupNameFieldNameCache.put(readerKey, readerResult);
        subjectSourceSubjectIdGroupNameFieldNameCache.put(updaterKey, updaterResult);
        
        return readerResult && updaterResult;
      }
    });
    
    return canRun;
  }
  
  public static List<GuiCustomUiConfiguration> convertFromCustomUiConfiguration(List<CustomUiConfiguration> customUiConfigurations) {
    
    List<GuiCustomUiConfiguration> guiCustomUiConfigs = new ArrayList<GuiCustomUiConfiguration>();
    
    for (CustomUiConfiguration gshTemplateConfiguration: customUiConfigurations) {
      guiCustomUiConfigs.add(convertFromCustomUiConfiguration(gshTemplateConfiguration));
    }
    
    return guiCustomUiConfigs;
    
  }

}
