/**
 * 
 */
package edu.internet2.middleware.grouper.client;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata;
import edu.internet2.middleware.grouper.changeLog.ChangeLogType;
import edu.internet2.middleware.grouper.client.ClientConfig.ClientConnectionConfigBean;
import edu.internet2.middleware.grouper.client.ClientConfig.ClientConnectionSourceConfigBean;
import edu.internet2.middleware.grouper.client.ClientConfig.ClientGroupConfigBean;
import edu.internet2.middleware.grouper.client.ClientConfig.GroupSyncWriteIdentifier;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.api.GcAddMember;
import edu.internet2.middleware.grouperClient.api.GcDeleteMember;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import edu.internet2.middleware.subject.Subject;


/**
 * Change log consumer to sync groups to other grouper incrementally
 * @author mchyzer
 */
public class GroupSyncConsumer extends ChangeLogConsumerBase {

  /**
   * 
   * @author mchyzer
   *
   */
  private static class GroupSyncConsumerBean {
    
    /**
     * cache the subject so we dont have to get it again and again
     */
    private Subject subject;

    /**
     * cache the subject so we dont have to get it again and again
     * @return the subject
     */
    public Subject getSubject() {
      return this.subject;
    }
    
    /**
     * cache the subject so we dont have to get it again and again
     * @param theSubject
     */
    public void setSubject(Subject theSubject) {
      this.subject = theSubject;
    }
  }
  
  /**
   * process events based on event type.  This is the category__action
   *
   */
  private static enum GroupSyncEventType {
  
    /** add membership event */
    membership__addMembership {
  
      /**
       * @see GroupSyncEventType#shouldProcess(ChangeLogType, ChangeLogEntry, GroupSyncConsumerBean)
       */
      @Override
      public boolean shouldProcess(ChangeLogType changeLogType,
          ChangeLogEntry changeLogEntry, GroupSyncConsumerBean groupSyncConsumerBean) {
        
        return shouldProcessMembership(changeLogType, changeLogEntry, groupSyncConsumerBean);
      }

      /**
       * connect with remote server...
       * @see GroupSyncEventType#processEvent(ClientGroupConfigBean)
       */
      @Override
      public void processEvent(ClientGroupConfigBean clientGroupConfigBean, GroupSyncConsumerBean groupSyncConsumerBean) {
        GcAddMember gcAddMember = new GcAddMember();
        Subject subject = groupSyncConsumerBean.getSubject();
        String localGroupName = clientGroupConfigBean.getLocalGroupName();
        
        ClientConnectionConfigBean clientConnectionConfigBean = ClientConfig.clientConnectionConfigBeanCache().get(clientGroupConfigBean.getConnectionName());
        
        ClientConnectionSourceConfigBean clientConnectionSourceConfigBean = clientConnectionConfigBean.getClientConnectionSourceConfigBeans().get(subject.getSourceId());

        WsSubjectLookup wsSubjectLookup = new WsSubjectLookup();
        
        if (!StringUtils.isBlank(clientConnectionSourceConfigBean.getRemoteSourceId())) {
          wsSubjectLookup.setSubjectSourceId(clientConnectionSourceConfigBean.getRemoteSourceId());
        }
        
        String localReadSubjectId = clientConnectionSourceConfigBean.getLocalReadSubjectId();
        String subjectIdentifier = null;
        if (StringUtils.equals("id", localReadSubjectId)) {
          subjectIdentifier = subject.getId();
        } else {
          subjectIdentifier = subject.getAttributeValue(localReadSubjectId);
        }
        
        GroupSyncWriteIdentifier groupSyncWriteIdentifier = (GroupSyncWriteIdentifier)ObjectUtils.defaultIfNull(
            clientConnectionSourceConfigBean.getRemoteWriteSubjectId(), GroupSyncWriteIdentifier.idOrIdentifier);
        
        groupSyncWriteIdentifier.assignIdentifier(wsSubjectLookup, subjectIdentifier);
        
        gcAddMember.addSubjectLookup(wsSubjectLookup);
        
        //dont put false if not configured
        if (clientGroupConfigBean.getAddExternalSubjectIfNotFound() != null && clientGroupConfigBean.getAddExternalSubjectIfNotFound()) {
          gcAddMember.assignAddExternalSubjectIfNotFound(clientGroupConfigBean.getAddExternalSubjectIfNotFound());
        }
        
        gcAddMember.assignGroupName(clientGroupConfigBean.getRemoteGroupName());

        StringBuilder logMessage = new StringBuilder();
        if (LOG.isDebugEnabled()) {
          logMessage.append("Sending add member " + GrouperUtil.subjectToString(subject) + ", " 
              + subjectIdentifier + ", to remote grouper: " + clientConnectionConfigBean.getConnectionId()
              + ", from local group: " + localGroupName + ", to remote group: " + clientGroupConfigBean.getRemoteGroupName() );
        }
        WsAddMemberResults wsAddMemberResults = null;
        boolean success = false;
        try {
          
          wsAddMemberResults = gcAddMember.execute();
          if (LOG.isDebugEnabled()) {
            logMessage.append(", resultCode: " + wsAddMemberResults.getResultMetadata().getResultCode() 
                + ", success: " + wsAddMemberResults.getResultMetadata().getSuccess());
          }
          success = GrouperUtil.booleanValue(wsAddMemberResults.getResultMetadata().getSuccess(), false);
          
        } finally {
          if (success && LOG.isDebugEnabled()) {
            LOG.debug(logMessage);
          }
          if (!success) {
            LOG.error("Error sending add member to remote group: " + logMessage);
          }
        }
      }
    },
    
    /** delete membership event */
    membership__deleteMembership {
  
      /**
       * @see GroupSyncEventType#shouldProcess(ChangeLogType, ChangeLogEntry, GroupSyncConsumerBean)
       */
      @Override
      public boolean shouldProcess(ChangeLogType changeLogType,
          ChangeLogEntry changeLogEntry, GroupSyncConsumerBean groupSyncConsumerBean) {
        return shouldProcessMembership(changeLogType, changeLogEntry, groupSyncConsumerBean);
      }

      /**
       * connect with remote server...
       * @see GroupSyncEventType#processEvent(ClientGroupConfigBean)
       */
      @Override
      public void processEvent(ClientGroupConfigBean clientGroupConfigBean, GroupSyncConsumerBean groupSyncConsumerBean) {
        GcDeleteMember gcDeleteMember = new GcDeleteMember();
        Subject subject = groupSyncConsumerBean.getSubject();
        String localGroupName = clientGroupConfigBean.getLocalGroupName();
        
        ClientConnectionConfigBean clientConnectionConfigBean = ClientConfig.clientConnectionConfigBeanCache().get(clientGroupConfigBean.getConnectionName());
        
        ClientConnectionSourceConfigBean clientConnectionSourceConfigBean = clientConnectionConfigBean.getClientConnectionSourceConfigBeans().get(subject.getSourceId());

        WsSubjectLookup wsSubjectLookup = new WsSubjectLookup();
        
        if (!StringUtils.isBlank(clientConnectionSourceConfigBean.getRemoteSourceId())) {
          wsSubjectLookup.setSubjectSourceId(clientConnectionSourceConfigBean.getRemoteSourceId());
        }
        
        String localReadSubjectId = clientConnectionSourceConfigBean.getLocalReadSubjectId();
        String subjectIdentifier = null;
        if (StringUtils.equals("id", localReadSubjectId)) {
          subjectIdentifier = subject.getId();
        } else {
          subjectIdentifier = subject.getAttributeValue(localReadSubjectId);
        }
        
        GroupSyncWriteIdentifier groupSyncWriteIdentifier = (GroupSyncWriteIdentifier)ObjectUtils.defaultIfNull(
            clientConnectionSourceConfigBean.getRemoteWriteSubjectId(), GroupSyncWriteIdentifier.idOrIdentifier);
        
        groupSyncWriteIdentifier.assignIdentifier(wsSubjectLookup, subjectIdentifier);
        
        gcDeleteMember.addSubjectLookup(wsSubjectLookup);
        
        gcDeleteMember.assignGroupName(clientGroupConfigBean.getRemoteGroupName());

        StringBuilder logMessage = new StringBuilder();
        if (LOG.isDebugEnabled()) {
          logMessage.append("Sending delete member " + GrouperUtil.subjectToString(subject) + ", " 
              + subjectIdentifier + ", to remote grouper: " + clientConnectionConfigBean.getConnectionId()
              + ", from local group: " + localGroupName + ", to remote group: " + clientGroupConfigBean.getRemoteGroupName() );
        }
        WsDeleteMemberResults wsDeleteMemberResults = null;
        boolean success = false;
        try {
          
          wsDeleteMemberResults = gcDeleteMember.execute();
          if (LOG.isDebugEnabled()) {
            logMessage.append(", resultCode: " + wsDeleteMemberResults.getResultMetadata().getResultCode() 
                + ", success: " + wsDeleteMemberResults.getResultMetadata().getSuccess());
          }
          success = GrouperUtil.booleanValue(wsDeleteMemberResults.getResultMetadata().getSuccess(), false);
          
        } finally {
          if (success && LOG.isDebugEnabled()) {
            LOG.debug(logMessage);
          }
        }
        if (!success) {
          LOG.error("Error sending delete member to remote group: " + logMessage);
        }
      }
    };
  
    /** 
     * if this record should be processed
     * @param changeLogType
     * @param changeLogEntry
     * @param groupSyncConsumerBean
     * @return true if the record should be processed
     */
    public abstract boolean shouldProcess(ChangeLogType changeLogType, ChangeLogEntry changeLogEntry, GroupSyncConsumerBean groupSyncConsumerBean);
    
    /**
     * process an event
     * @param clientGroupConfigBean
     * @param groupSyncConsumerBean 
     */
    public abstract void processEvent(ClientGroupConfigBean clientGroupConfigBean, GroupSyncConsumerBean groupSyncConsumerBean);      
    
    /**
     * 
     * @param changeLogEntry
     * @param groupSyncConsumerBean 
     */
    public void processEvent(ChangeLogEntry changeLogEntry, GroupSyncConsumerBean groupSyncConsumerBean) {
      //get the config
      Map<String, ClientGroupConfigBean> clientGroupConfigMap = ClientConfig.clientGroupConfigBeanCache();
      
      //get the group name, see if we have a config for it
      String groupName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName);
      
      ClientGroupConfigBean clientGroupConfigBean = clientGroupConfigMap.get(groupName);
      
      if (clientGroupConfigBean == null) {
        return;
      }
      
      //get the connection, and set it up
      String connectionName = clientGroupConfigBean.getConnectionName();
      
      ClientCustomizerContext clientCustomizerContext = new ClientCustomizerContext();
      clientCustomizerContext.setConnectionName(connectionName);
      ClientCustomizer clientCustomizer = new ClientCustomizer();
      clientCustomizer.init(clientCustomizerContext);
      clientCustomizer.setupConnection();
      
      try {
        
        this.processEvent(clientGroupConfigBean, groupSyncConsumerBean);
        
      } finally {
        clientCustomizer.teardownConnection();
      }
    }

    /**
     * do a case-insensitive matching
     * 
     * @param string
     * @param exceptionOnNull will not allow null or blank entries
     * @param exceptionIfInvalid true if there should be an exception if invalid
     * @return the enum or null or exception if not found
     */
    public static GroupSyncEventType valueOfIgnoreCase(String string, boolean exceptionOnNull, boolean exceptionIfInvalid) {
      return GrouperUtil.enumValueOfIgnoreCase(GroupSyncEventType.class, 
          string, exceptionOnNull, exceptionIfInvalid);
    }
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GroupSyncConsumer.class);

  /**
   * This is going to do best efforts, but if it cant get through, then log and continue...
   * @see edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase#processChangeLogEntries(java.util.List, edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata)
   */
  @Override
  public long processChangeLogEntries(List<ChangeLogEntry> changeLogEntryList,
      ChangeLogProcessorMetadata changeLogProcessorMetadata) {

    long currentId = -1;

    for (final ChangeLogEntry changeLogEntry : changeLogEntryList) {

      //try catch so we can track that we made some progress
      try {
        final ChangeLogType changeLogType = changeLogEntry.getChangeLogType();

        currentId = changeLogEntry.getSequenceNumber();
        
        if (LOG.isDebugEnabled()) {
          LOG.debug("Processing group sync event number " + currentId + ", " 
              + changeLogType.getChangeLogCategory() + ", " + changeLogType.getActionName());
        }

        String enumKey = changeLogType.getChangeLogCategory() + "__" + changeLogType.getActionName();

        final GroupSyncEventType groupSyncEventType = GroupSyncEventType.valueOfIgnoreCase(enumKey, false, false);
        
        if (groupSyncEventType != null) {
          
          GroupSyncConsumerBean groupSyncConsumerBean = new GroupSyncConsumerBean();
          
          if (!groupSyncEventType.shouldProcess(changeLogType, changeLogEntry, groupSyncConsumerBean)) {
            continue;
          }

          groupSyncEventType.processEvent(changeLogEntry, groupSyncConsumerBean);

        } else {
          
          if (LOG.isDebugEnabled()) {
            LOG.debug("Unsupported event " + changeLogType.getChangeLogCategory() + ", " 
                + changeLogType.getActionName() + ", " + changeLogEntry.getSequenceNumber());
          }

        }
        
      } catch (Exception e) {
        //we successfully processed this record
        LOG.error("problem with id: " + currentId, e);
        //continue
      }
    }

    return currentId;

  }

  /**
   * 
   * @param changeLogType
   * @param changeLogEntry
   * @param groupSyncConsumerBean holds state like the subject so we dont have to find it multiple times
   * @return true if should process, false if not
   */
  private static boolean shouldProcessMembership(final ChangeLogType changeLogType, final ChangeLogEntry changeLogEntry, 
      final GroupSyncConsumerBean groupSyncConsumerBean) {
    
    String fieldId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.fieldId);
    
    //lets only do members list for now
    if (!StringUtils.equals(fieldId, Group.getDefaultList().getUuid())) {
      return false;
    }
  
    //must be flattened
    String membershipType = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.membershipType);
    
    if (!StringUtils.equals("flattened", membershipType)) {
      return false;
    }
    
    //lets see if we are configured to process it
    Map<String, ClientGroupConfigBean> clientGroupConfigMap = ClientConfig.clientGroupConfigBeanCache();
    
    //get the group name, see if we have a config for it
    String groupName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName);
    
    ClientGroupConfigBean clientGroupConfigBean = clientGroupConfigMap.get(groupName);
    
    if (clientGroupConfigBean == null) {
      return false;
    }
    
    //see if we are incremental
    if (clientGroupConfigBean.getGroupSyncType() == null || !clientGroupConfigBean.getGroupSyncType().isIncremental()) {
      return false;
    }

    //get the connection, and set it up
    String connectionName = clientGroupConfigBean.getConnectionName();
    if (StringUtils.isBlank(connectionName)) {
      LOG.error("No connection name found: " + clientGroupConfigBean.getConfigId());
      return false;
    }
    
    //get connection from config
    ClientConnectionConfigBean clientConnectionConfigBean = ClientConfig.clientConnectionConfigBeanCache().get(connectionName);
    
    if (clientConnectionConfigBean == null) {
      LOG.error("Cant find client connection: " + connectionName);
      return false;
    }
    
    final String sourceId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.sourceId);
    
    ClientConnectionSourceConfigBean clientConnectionSourceConfigBean = clientConnectionConfigBean.getClientConnectionSourceConfigBeans().get(sourceId);
    
    //source not configured
    if (clientConnectionSourceConfigBean == null) {
      return false;
    }
    
    GrouperSession grouperSession = GrouperSession.startRootSession(false);
    Subject subject = (Subject)GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        String subjectId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.subjectId);

        return SubjectFinder.findByIdAndSource(subjectId, sourceId, false);
        
      }
    });
    
    if (subject == null) {
      return false;
    }
    
    //make sure subject has appropriate
    //cache this for later
    groupSyncConsumerBean.setSubject(subject);
    
    String localReadSubjectId = clientConnectionSourceConfigBean.getLocalReadSubjectId();
    if (StringUtils.isBlank(localReadSubjectId)) {

      LOG.error("Needs a local read subject id: " + connectionName + ", " + sourceId);
      return false;
    }

    //check subject attribute
    if (!StringUtils.equals("id", localReadSubjectId)) {
      String attributeValue = subject.getAttributeValue(localReadSubjectId);
      if (StringUtils.isBlank(attributeValue)) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("subject doesnt have required attribute so isnt synced: " + localReadSubjectId);
        }
        return false;
      }
    }
    
    //we are good to go
    return true;
  }

}
