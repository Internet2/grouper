/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.poc_secureUserDataChangeLog;

import java.io.StringWriter;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xmpp.XmppConnectionBean;


/**
 *
 */
public class SecureUserDataChangeLogConsumer extends ChangeLogConsumerBase {

  /** 
   * dont send more than one every 30 seconds... the change long runs every minute,
   * so if there is a batch that runs, finish that batch before sending another, 
   * it will not miss anything
   */
  private static long lastFullRefresh = 0;
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    SudChangeLogMessage sudChangeLogMessage = new SudChangeLogMessage();
    sudChangeLogMessage.setChangeType("permissionRefresh");
    XStream xStream = new XStream(new XppDriver());
    xStream.alias(StringUtils.uncapitalize(SudChangeLogMessage.class.getSimpleName()), SudChangeLogMessage.class);
    StringWriter stringWriter = new StringWriter();
    CompactWriter compactWriter = new CompactWriter(stringWriter);
    

    xStream.marshal(sudChangeLogMessage, compactWriter);

    String xml = stringWriter.toString();
    
    System.out.println(xml);
  }
  
  /** */
  private static final Log LOG = GrouperUtil.getLog(SecureUserDataChangeLogConsumer.class);

  /**
   * @see edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase#processChangeLogEntries(java.util.List, edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata)
   */
  @Override
  public long processChangeLogEntries(List<ChangeLogEntry> changeLogEntryList,
      ChangeLogProcessorMetadata changeLogProcessorMetadata) {
    long currentId = -1;

    XmppConnectionBean xmppConnectionBean = null;
    String recipient = null;
    
    //only process full refreshes every 30 seconds
    boolean shouldProcessFullRefreshes = System.currentTimeMillis() - lastFullRefresh > 30 * 1000;

    for (ChangeLogEntry changeLogEntry : changeLogEntryList) {
      //try catch so we can track that we made some progress
      try {

        currentId = changeLogEntry.getSequenceNumber();
        
        SudChangeLogMessage sudChangeLogMessage = null;
        
        //LOG.debug("Processing changeLog #" + currentId + ", " 
        //    + changeLogEntry.getChangeLogType().getChangeLogCategory() + "." 
        //    + changeLogEntry.getChangeLogType().getActionName() + ", shouldProcessFullRefreshes: " + shouldProcessFullRefreshes
        //    );
        //
        //if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_ADD)
        //    || changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE)) {
        //  LOG.debug(
        //      "Parent group stem: " + GrouperUtil.parentStemNameFromName(
        //          changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName))
        //          + ", group name: " + changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName)
        //          + ", sourceId: " +  changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId)
        //          + ", " + (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_ADD)
        //              || changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE))
        //          + ", " + StringUtils.equals("fgac:apps:secureUserData:rowGroups",
        //              GrouperUtil.parentStemNameFromName(
        //                  changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName)))
        //          + ", " + StringUtils.equals(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId), "jdbc"));
        //  
        //}
        //
        //if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.PERMISSION_ADD)
        //        || changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.PERMISSION_DELETE)) {
        //  LOG.debug("Parent attrDefName stem: " + GrouperUtil.parentStemNameFromName(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_ADD.attributeDefNameName)));
        //  
        //}
          
        
        //if it is a membership change, in the right folder, with the right source, and right name prefix 
        if ((changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_ADD)
            || changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE))
            && StringUtils.equals("fgac:apps:secureUserData:rowGroups",
                GrouperUtil.parentStemNameFromName(
                    changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName)))
            && StringUtils.equals(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId), "jdbc")) {
          
          LOG.debug("Membership change: " 
              + changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName)
              + ", " + changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId));

          String extension = GrouperUtil.extensionFromName(
              changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName));
          
          sudChangeLogMessage = new SudChangeLogMessage();
          sudChangeLogMessage.setChangeType("rowGroupChange");
          
          sudChangeLogMessage.setRowGroupExtension(extension);
          sudChangeLogMessage.setRowSubjectId(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId));
          
          //or if there was a permission change and the permission is in the right folder
        } else if (shouldProcessFullRefreshes
            && (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.PERMISSION_ADD)
                || changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.PERMISSION_DELETE))
            && (StringUtils.equals("fgac:apps:secureUserData:permissions:rows",
                GrouperUtil.parentStemNameFromName(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_ADD.attributeDefNameName)))
            || StringUtils.equals("fgac:apps:secureUserData:permissions:columns",
                GrouperUtil.parentStemNameFromName(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_ADD.attributeDefNameName))))) {

          sudChangeLogMessage = new SudChangeLogMessage();
          sudChangeLogMessage.setChangeType("permissionRefresh");

          LOG.debug("Permission change: " 
              + changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_ADD.attributeDefNameName)
              + ", " + changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_ADD.action)
              + ", " + changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_ADD.subjectId));
          
          lastFullRefresh = System.currentTimeMillis();
          shouldProcessFullRefreshes = false;
          
        }
        
        //is there something to send?
        if (sudChangeLogMessage != null) {
          
          XStream xStream = new XStream(new XppDriver());
          xStream.alias(StringUtils.uncapitalize(SudChangeLogMessage.class.getSimpleName()), SudChangeLogMessage.class);
          StringWriter stringWriter = new StringWriter();
          CompactWriter compactWriter = new CompactWriter(stringWriter);
          
          xStream.marshal(sudChangeLogMessage, compactWriter);
          
          String xml = stringWriter.toString();
          
          if (xmppConnectionBean == null) {
 
            String consumerName = changeLogProcessorMetadata.getConsumerName();
            
            recipient = GrouperLoaderConfig.getPropertyString("changeLog.consumer."
                + consumerName + ".publisher.recipient", "");

            String xmppServer = GrouperLoaderConfig.getPropertyString("changeLog.consumer."
                + consumerName + ".publisher.server");
            int port = GrouperLoaderConfig.getPropertyInt("changeLog.consumer." + consumerName
                + ".publisher.port", -1);
            String username = GrouperLoaderConfig.getPropertyString("changeLog.consumer."
                + consumerName + ".publisher.username", "");
            String password = GrouperLoaderConfig.getPropertyString("changeLog.consumer."
                + consumerName + ".publisher.password", "");
            String resource = GrouperLoaderConfig.getPropertyString("changeLog.consumer."
                + consumerName + ".publisher.resource", "");

            xmppConnectionBean = new XmppConnectionBean(xmppServer, port, username, resource, password);
            
          }
          
          xmppConnectionBean.sendMessage(recipient, xml);

        }
        
      } catch (Exception e) {
        //we unsuccessfully processed this record... decide whether to wait, throw, ignore, log, etc...
        LOG.error("problem with id: " + currentId, e);
        changeLogProcessorMetadata.setHadProblem(true);
        changeLogProcessorMetadata.setRecordException(e);
        changeLogProcessorMetadata.setRecordExceptionSequence(currentId);
        //stop here
        return currentId;
        //continue 
      }
    }

    return currentId;
  }

}
