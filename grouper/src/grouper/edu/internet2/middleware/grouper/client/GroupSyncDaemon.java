package edu.internet2.middleware.grouper.client;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.client.ClientConfig.ClientConnectionConfigBean;
import edu.internet2.middleware.grouper.client.ClientConfig.ClientGroupConfigBean;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.subject.Subject;

/**
 * process logic for the cron part of a group sync
 * @author mchyzer
 *
 */
public class GroupSyncDaemon {

  /**
   * sync a group by config name from the cron daemon
   * @param configName
   * @return the number of records changed
   */
  public static int syncGroup(String configName) {

    ClientGroupConfigBean clientGroupConfigBean = ClientConfig.clientGroupConfigBeanCache().get(configName);

    if (clientGroupConfigBean == null) {
      throw new RuntimeException("Cant find clientGroupConfigBean by config id: '" + configName + "'");
    }
    
    //get the connection and the grouper session
    String connectionName = clientGroupConfigBean.getConnectionName();
    ClientConnectionConfigBean clientConnectionConfigBean = ClientConfig.clientConnectionConfigBeanCache().get(connectionName);
    
    if (clientConnectionConfigBean == null) {
      throw new RuntimeException("Cant find clientConnectionBean by config id: '"       
          + configName + "', connectionId: '" + connectionName + "'");      
    }
    
    String actAsSubjectString = clientConnectionConfigBean.getLocalActAsSubject();
    Subject actAsSubject = null;
    if (StringUtils.isBlank(actAsSubjectString)) {
      actAsSubject = SubjectFinder.findRootSubject();
      
    } else {
      actAsSubject = SubjectFinder.findByPackedSubjectString(actAsSubjectString, true);
    }    
    
    GrouperSession grouperSession = GrouperSession.start(actAsSubject, false);
    
    try {
      GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {
        
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          // TODO Auto-generated method stub
          return null;
        }
      });
      
      
    } finally {     
      GrouperSession.stopQuietly(grouperSession); 
    }     
    
    
    
    //process the rest
    GroupSyncType groupSyncType = clientGroupConfigBean.getGroupSyncType();
    
    if (groupSyncType == GroupSyncType.pull) {    
      //return syncGroupPull(clientGroupConfigBean);
    } else if (groupSyncType == GroupSyncType.incremental_push || groupSyncType == GroupSyncType.push) {    
      //return syncGroupPush(clientGroupConfigBean);
    } else {    
      throw new RuntimeException("Not expecting configName: " + configName + ", groupSyncType: " + groupSyncType);
    }   
    return -1;
  }

  /**
   * pull a full group from a remote site, and return the number of records changed
   * @param clientGroupConfigBean
   * @return the number of records changed
   */
  private static int syncGroupPull(ClientGroupConfigBean clientGroupConfigBean,     
      ClientConnectionConfigBean clientConnectionConfigBean, GrouperSession grouperSession) {
    return -1;
  }

  /**
   * push a full group to a remote site, and return the number of records changed
   * @param clientGroupConfigBean
   * @return the number of records changed
   */
  private static int syncGroupPush(ClientGroupConfigBean clientGroupConfigBean,     
      ClientConnectionConfigBean clientConnectionConfigBean, GrouperSession grouperSession) {
    return -1;
    
  }
}
