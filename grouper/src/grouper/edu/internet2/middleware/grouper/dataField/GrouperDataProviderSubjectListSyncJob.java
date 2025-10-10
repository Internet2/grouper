package edu.internet2.middleware.grouper.dataField;

import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.dataProvider.GrouperDataProviderLogic;
import edu.internet2.middleware.grouper.app.dataProvider.GrouperDataProviderSync;
import edu.internet2.middleware.grouper.app.dataProvider.GrouperDataProviderSyncType;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.subject.Subject;

public class GrouperDataProviderSubjectListSyncJob {
    
  /**
   * 
   */
  public static void syncSubjects(String dataProviderConfigId, Set<String> subjectIds, Set<String> subjectIdentifiers, Map<String, Set<String>> sourceToSubjectIds, Map<String, Set<String>> sourceToSubjectIdentifiers) {
    
    if (!canSyncSubjects(dataProviderConfigId, GrouperSession.staticGrouperSession().getSubject())) {
      throw new InsufficientPrivilegeException("Not allowed to sync data provider: " + dataProviderConfigId);
    }
    
    GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {

          @Override
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

            final GrouperDataProviderSync grouperDataProviderSync = GrouperDataProviderSync.retrieveDataProviderSync(dataProviderConfigId);
            grouperDataProviderSync.setSyncSubjectsSubjectIds(subjectIds);
            grouperDataProviderSync.setSyncSubjectsSubjectIdentifiers(subjectIdentifiers);
            grouperDataProviderSync.setSyncSubjectsSourceToSubjectIds(sourceToSubjectIds);
            grouperDataProviderSync.setSyncSubjectsSourceToSubjectIdentifiers(sourceToSubjectIdentifiers);
            
            GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
            grouperDataEngine.setDebugMap(grouperDataProviderSync.getDebugMap());
            grouperDataProviderSync.setGrouperDataEngine(grouperDataEngine);
            grouperDataProviderSync.runSync(GrouperDataProviderSyncType.syncSubjects);
            
            return null;
          }
        });
  }
  
  public static boolean canSyncSubjects(String dataProviderConfigId, Subject subject) {
    if (PrivilegeHelper.isWheelOrRoot(subject)) {
      return true;
    }

    return (Boolean)GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {

          @Override
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
            String dataProviderSubjectListSyncAllowedGroupName = GrouperDataProviderLogic.dataProviderSubjectListSyncAllowedGroupName();
            Group groupToCheck = GroupFinder.findByName(grouperSession, dataProviderSubjectListSyncAllowedGroupName, false);
            if (groupToCheck == null) {
              return false;
            }
            return groupToCheck.hasMember(subject);
          }
        });
  }
}
