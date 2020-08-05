/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.zoom;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.esb.listener.EsbListenerBase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.xmpp.EsbEvent;
import edu.internet2.middleware.grouperClientExt.xmpp.EsbEvents;
import edu.internet2.middleware.grouperClientExt.xmpp.GcDecodeEsbEvents;


/**
 * zoom change log consumer via esb
 */
public class ZoomEsbPublisher extends EsbListenerBase {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperZoomFullSync.class);

  /**
   * 
   */
  public ZoomEsbPublisher() {
  }

  /**
   * @see edu.internet2.middleware.grouper.esb.listener.EsbListenerBase#dispatchEvent(java.lang.String, java.lang.String)
   */
  @Override
  public boolean dispatchEvent(String jsonString, String consumerName) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    long startedNanos = System.nanoTime();

    debugMap.put("method", "ZoomEsbPublisher.dispatchEvent");

    try {

      EsbEvents esbEvents = GcDecodeEsbEvents.decodeEsbEvents(jsonString);
      esbEvents = GcDecodeEsbEvents.unencryptEsbEvents(esbEvents);

      //  {  
      //  "encrypted":false,
      //  "esbEvent":[  
      //     {  
      //        "changeOccurred":false,
      //        "createdOnMicros":1476889916578000,
      //        "eventType":"MEMBERSHIP_DELETE",
      //        "fieldName":"members",
      //        "groupId":"89dd656be8c743e79b2ef24fde6dab36",
      //        "groupName":"box:groups:someGroup",
      //        "id":"c2641b287f964bb28b2f0ddcd05f9fd3",
      //        "membershipType":"flattened",
      //        "sequenceNumber":"618",
      //        "sourceId":"g:isa",
      //        "subjectId":"GrouperSystem"
      //     }
      //  ]
      //}
      
      
      Hib3GrouperLoaderLog hib3GrouperLoaderLog = this.getChangeLogProcessorMetadata() == null ? null : 
        this.getChangeLogProcessorMetadata().getHib3GrouperLoaderLog();

      if (hib3GrouperLoaderLog == null) {
        hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
      }

      String configId = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("changeLog.consumer."
          + consumerName + ".zoomConfigId");

      debugMap.put("configId", configId);

      String folderName = GrouperZoomLocalCommands.folderNameToProvision(configId);
      String groupNameToDeleteUsers = GrouperZoomLocalCommands.groupNameToDeleteUsers(configId);
      String groupNameToDeactivateUsers = GrouperZoomLocalCommands.groupNameToDeactivateUsers(configId);
      
      //not sure why there would be no events in there
      for (EsbEvent esbEvent : GrouperClientUtils.nonNull(esbEvents.getEsbEvent(), EsbEvent.class)) {

        debugMap.put("eventType", esbEvent.getEventType());

        if (esbEvent.getEventType().startsWith("MEMBERSHIP")) {
          
          // sync a membership
          String sourceId = esbEvent.getSourceId();
          debugMap.put("sourceId", sourceId);
          if (!GrouperZoomLocalCommands.configSourcesForSubjects(configId).contains(sourceId)) {
            continue;
          }
          
          debugMap.put("groupName", esbEvent.getGroupName());

          String groupExtension = GrouperUtil.extensionFromName(esbEvent.getGroupName());
          if (!StringUtils.isBlank(folderName) && StringUtils.equals(folderName + ":" + groupExtension, esbEvent.getGroupName())) {
            // is group in folder
            boolean hasMembership = GrouperZoomLocalCommands.groupSourceIdSubjectIdToProvision(configId, 
                groupExtension, esbEvent.getSourceId(), esbEvent.getSubjectId());
            debugMap.put("hasMembership", hasMembership);
            
            String email = esbEvent.subjectAttribute(GrouperZoomLocalCommands.subjectAttributeForZoomEmail(configId));
            debugMap.put("email", email);
            if (StringUtils.isBlank(email)) {
              continue;
            }
            
            Map<String, Object> user = GrouperZoomCommands.retrieveUser(configId, email);
            debugMap.put("userExists", user != null);
            
            if (user == null) {
              continue;
            }
            Map<String, Object> group = GrouperZoomCommands.retrieveGroups(configId).get(groupExtension);

            debugMap.put("groupExists", group != null);

            if (group == null ) {
              // i dont think this would happen
              continue;
            }

            if (hasMembership) {
              GrouperZoomCommands.addGroupMembership(configId, (String)group.get("id"), (String)user.get("id"));
              hib3GrouperLoaderLog.addInsertCount(1);
            } else {
              GrouperZoomCommands.removeGroupMembership(configId, (String)group.get("id"), (String)user.get("id"));
              hib3GrouperLoaderLog.addDeleteCount(1);
            }
          } else if (!StringUtils.isBlank(groupNameToDeleteUsers) && StringUtils.equals(groupNameToDeleteUsers, esbEvent.getGroupName())) {

            // is group in folder
            boolean hasMembership = GrouperZoomLocalCommands.groupSourceIdSubjectIdToDelete(configId, 
                esbEvent.getSourceId(), esbEvent.getSubjectId());
            debugMap.put("hasMembershipToDelete", hasMembership);
            
            if (!hasMembership) {
              continue;
            }
            
            String email = esbEvent.subjectAttribute(GrouperZoomLocalCommands.subjectAttributeForZoomEmail(configId));
            debugMap.put("email", email);
            if (StringUtils.isBlank(email)) {
              continue;
            }
            
            Map<String, Object> user = GrouperZoomCommands.retrieveUser(configId, email);
            debugMap.put("userExists", user != null);
            
            if (user == null) {
              continue;
            }

            GrouperZoomCommands.deleteUser(configId, email);
            hib3GrouperLoaderLog.addDeleteCount(1);
            
          } else if (!StringUtils.isBlank(groupNameToDeactivateUsers) && StringUtils.equals(groupNameToDeactivateUsers, esbEvent.getGroupName())) {

            // is group in folder
            boolean hasMembership = GrouperZoomLocalCommands.groupSourceIdSubjectIdToDeactivate(configId, 
                esbEvent.getSourceId(), esbEvent.getSubjectId());
            debugMap.put("hasMembershipToDeactivate", hasMembership);
            
            if (!hasMembership) {
              continue;
            }
            
            String email = esbEvent.subjectAttribute(GrouperZoomLocalCommands.subjectAttributeForZoomEmail(configId));
            debugMap.put("email", email);
            if (StringUtils.isBlank(email)) {
              continue;
            }
            
            Map<String, Object> user = GrouperZoomCommands.retrieveUser(configId, email);
            debugMap.put("userExists", user != null);
            
            if (user == null) {
              continue;
            }

            final boolean userActive = StringUtils.equals("active", (String)user.get("status"));
            debugMap.put("userActive", userActive);
            
            if (!userActive) {
              continue;
            }

            GrouperZoomCommands.userChangeStatus(configId, email, false);
            hib3GrouperLoaderLog.addDeleteCount(1);
            
          }
        } else if (esbEvent.getEventType().startsWith("GROUP")) {
          
          debugMap.put("groupName", esbEvent.getGroupName());

          String groupExtension = GrouperUtil.extensionFromName(esbEvent.getGroupName());
          if (!StringUtils.equals(folderName + ":" + groupExtension, esbEvent.getGroupName())) {
            // is group in folder
            continue;
          }
          
          boolean groupExistsInGrouper = GrouperZoomLocalCommands.groupExtensionsToProvision(configId).contains(groupExtension);
          
          debugMap.put("groupExistsInGrouper", groupExistsInGrouper);

          Map<String, Object> groupInZoom = GrouperZoomCommands.retrieveGroups(configId).get(groupExtension);
          boolean groupExistsInZoom = groupInZoom != null && !StringUtils.isBlank((String)groupInZoom.get("id"));

          debugMap.put("groupExistsInZoom", groupExistsInZoom);

          if (groupExistsInGrouper == groupExistsInZoom) {
            continue;
          }
          
          if (groupExistsInGrouper) {
            GrouperZoomCommands.createGroup(configId, groupExtension);
            hib3GrouperLoaderLog.addInsertCount(1);
          } else {
            if (GrouperZoomLocalCommands.deleteInTargetIfDeletedInGrouper(configId)) {
              Map<String, Object> groupMap = GrouperZoomCommands.retrieveGroups(configId).get(groupExtension);
              if (groupMap != null) {
                GrouperZoomCommands.deleteGroup(configId, (String)groupMap.get("id"));
                hib3GrouperLoaderLog.addDeleteCount(1);
              }
            } else {
              // remove all members
              List<Map<String, Object>> memberships = GrouperZoomCommands.retrieveGroupMemberships(configId, (String)groupInZoom.get("id"));
              for (Map<String, Object> member : GrouperUtil.nonNull(memberships)) {
                GrouperZoomCommands.removeGroupMembership(configId, (String)groupInZoom.get("id"), (String)member.get("id"));
                hib3GrouperLoaderLog.addDeleteCount(1);

              }
            }
          }
          
        }
        
      }

      
      return true;
    } finally {
      debugMap.put("tookMillis", (System.nanoTime() - startedNanos)/1000000);
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }

    
  }

  /**
   * @see edu.internet2.middleware.grouper.esb.listener.EsbListenerBase#disconnect()
   */
  @Override
  public void disconnect() {
  }


}
