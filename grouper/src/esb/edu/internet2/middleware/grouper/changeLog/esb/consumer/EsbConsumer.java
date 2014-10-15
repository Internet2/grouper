/**
 * Copyright 2014 Internet2
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
 */
/*
 * @author Rob Hebron
 */

package edu.internet2.middleware.grouper.changeLog.esb.consumer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabel;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.esb.listener.EsbListenerBase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Class to dispatch individual events to external systems through configured classes.
 * HTTP, HTTTPS and XMPP currently supported.
 * Configure in grouper-loader.properties
 */
public class EsbConsumer extends ChangeLogConsumerBase {

  /** */
  private EsbListenerBase esbPublisherBase;

  /** */
  private static final Log LOG = GrouperUtil.getLog(EsbConsumer.class);

  /**
   * @see ChangeLogConsumerBase#processChangeLogEntries(List, ChangeLogProcessorMetadata)
   */
  @Override
  public long processChangeLogEntries(
      List<ChangeLogEntry> changeLogEntryList,
      ChangeLogProcessorMetadata changeLogProcessorMetadata) {
    String consumerName = changeLogProcessorMetadata.getConsumerName();
    long currentId = -1;

    Map<String, Object> debugMap = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;
    
    //try catch so we can track that we made some progress
    try {
      for (ChangeLogEntry changeLogEntry : changeLogEntryList) {

        currentId = changeLogEntry.getSequenceNumber();
        if (LOG.isDebugEnabled()) {
          debugMap.put("eventNumber", currentId);
        }
        EsbEvent event = new EsbEvent();
        event.setSequenceNumber(Long.toString(currentId));
        //if this is a group type add action and category
        if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_ADD)) {
          event.setEventType(EsbEvent.EsbEventType.GROUP_ADD.name());
          event.setId(this.getLabelValue(changeLogEntry, ChangeLogLabels.GROUP_ADD.id));
          event.setName(this
              .getLabelValue(changeLogEntry, ChangeLogLabels.GROUP_ADD.name));
          event.setParentStemId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_ADD.parentStemId));
          event.setDisplayName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_ADD.displayName));
          event.setDescription(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_ADD.description));

        } else if (changeLogEntry
            .equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_DELETE)) {
          event.setEventType(EsbEvent.EsbEventType.GROUP_DELETE.name());
          event
              .setId(this.getLabelValue(changeLogEntry, ChangeLogLabels.GROUP_DELETE.id));
          event.setName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_DELETE.name));
          event.setParentStemId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_DELETE.parentStemId));
          event.setDisplayName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_DELETE.displayName));
          event.setDescription(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_DELETE.description));

        } else if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.ENTITY_ADD)) {
          event.setEventType(EsbEvent.EsbEventType.ENTITY_ADD.name());
          event.setId(this.getLabelValue(changeLogEntry, ChangeLogLabels.ENTITY_ADD.id));
          event.setName(this
              .getLabelValue(changeLogEntry, ChangeLogLabels.ENTITY_ADD.name));
          event.setParentStemId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.ENTITY_ADD.parentStemId));
          event.setDisplayName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.ENTITY_ADD.displayName));
          event.setDescription(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.ENTITY_ADD.description));

        } else if (changeLogEntry
            .equalsCategoryAndAction(ChangeLogTypeBuiltin.ENTITY_DELETE)) {
          event.setEventType(EsbEvent.EsbEventType.ENTITY_DELETE.name());
          event
              .setId(this.getLabelValue(changeLogEntry, ChangeLogLabels.ENTITY_DELETE.id));
          event.setName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.ENTITY_DELETE.name));
          event.setParentStemId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.ENTITY_DELETE.parentStemId));
          event.setDisplayName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.ENTITY_DELETE.displayName));
          event.setDescription(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.ENTITY_DELETE.description));

        } else if (changeLogEntry
            .equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_FIELD_ADD)) {
          event.setEventType(EsbEvent.EsbEventType.GROUP_FIELD_ADD.name());
          event.setId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_FIELD_ADD.id));
          event.setName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_FIELD_ADD.name));
          event.setGroupTypeId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_FIELD_ADD.groupTypeId));
          event.setGroupTypeName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_FIELD_ADD.groupTypeName));
          event.setType(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_FIELD_ADD.type));

        } else if (changeLogEntry
            .equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_FIELD_DELETE)) {
          event.setEventType(EsbEvent.EsbEventType.GROUP_FIELD_DELETE.name());
          event.setId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_FIELD_DELETE.id));
          event.setName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_FIELD_DELETE.name));
          event.setGroupTypeId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_FIELD_DELETE.groupTypeId));
          event.setGroupTypeName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_FIELD_DELETE.groupTypeName));
          event.setType(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_FIELD_DELETE.type));

        } else if (changeLogEntry
            .equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_FIELD_UPDATE)) {
          event.setEventType(EsbEvent.EsbEventType.GROUP_FIELD_UPDATE.name());
          event.setId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_FIELD_UPDATE.id));
          event.setName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_FIELD_UPDATE.name));
          event.setGroupTypeId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_FIELD_UPDATE.groupTypeId));
          event.setGroupTypeName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_FIELD_UPDATE.groupTypeName));
          event.setType(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_FIELD_UPDATE.type));
          event.setReadPrivilege(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_FIELD_UPDATE.readPrivilege));
          event.setWritePrivilege(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_FIELD_UPDATE.writePrivilege));
          event.setPropertyChanged(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_FIELD_UPDATE.propertyChanged));
          event.setPropertyOldValue(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_FIELD_UPDATE.propertyOldValue));
          event.setPropertyNewValue(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_FIELD_UPDATE.propertyNewValue));

        } else if (changeLogEntry
            .equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_UPDATE)) {
          event.setEventType(EsbEvent.EsbEventType.GROUP_UPDATE.name());
          event
              .setId(this.getLabelValue(changeLogEntry, ChangeLogLabels.GROUP_UPDATE.id));
          event.setName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_UPDATE.name));
          event.setParentStemId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_UPDATE.parentStemId));
          event.setDisplayName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_UPDATE.displayName));
          event.setDisplayExtension(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_UPDATE.displayExtension));
          event.setDescription(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_UPDATE.description));
          event.setPropertyChanged(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_UPDATE.propertyChanged));
          event.setPropertyOldValue(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_UPDATE.propertyOldValue));
          event.setPropertyNewValue(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_UPDATE.propertyNewValue));

        } else if (changeLogEntry
            .equalsCategoryAndAction(ChangeLogTypeBuiltin.ENTITY_UPDATE)) {
          event.setEventType(EsbEvent.EsbEventType.ENTITY_UPDATE.name());
          event
              .setId(this.getLabelValue(changeLogEntry, ChangeLogLabels.ENTITY_UPDATE.id));
          event.setName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.ENTITY_UPDATE.name));
          event.setParentStemId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.ENTITY_UPDATE.parentStemId));
          event.setDisplayName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.ENTITY_UPDATE.displayName));
          event.setDisplayExtension(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.ENTITY_UPDATE.displayExtension));
          event.setDescription(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.ENTITY_UPDATE.description));
          event.setPropertyChanged(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.ENTITY_UPDATE.propertyChanged));
          event.setPropertyOldValue(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.ENTITY_UPDATE.propertyOldValue));
          event.setPropertyNewValue(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.ENTITY_UPDATE.propertyNewValue));

        } else if (changeLogEntry
            .equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_ADD)) {
          event.setEventType(EsbEvent.EsbEventType.MEMBERSHIP_ADD.name());
          // throws error
          event.setId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.MEMBERSHIP_ADD.id));
          event.setFieldName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.MEMBERSHIP_ADD.fieldName));
          event.setSubjectId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.MEMBERSHIP_ADD.subjectId));
          event.setSourceId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.MEMBERSHIP_ADD.sourceId));
          // throws error
          event.setMembershipType(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.MEMBERSHIP_ADD.membershipType));
          event.setGroupId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.MEMBERSHIP_ADD.groupId));
          event.setGroupName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.MEMBERSHIP_ADD.groupName));

        } else if (changeLogEntry
            .equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE)) {
          event.setEventType(EsbEvent.EsbEventType.MEMBERSHIP_DELETE.name());
          event.setId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.MEMBERSHIP_DELETE.id));
          event.setFieldName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.MEMBERSHIP_DELETE.fieldName));
          event.setSubjectId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.MEMBERSHIP_DELETE.subjectId));
          event.setSourceId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.MEMBERSHIP_DELETE.sourceId));
          event.setMembershipType(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.MEMBERSHIP_DELETE.membershipType));
          event.setGroupId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.MEMBERSHIP_DELETE.groupId));
          event.setGroupName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.MEMBERSHIP_DELETE.groupName));

        } else if (changeLogEntry
            .equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_UPDATE)) {
          event.setEventType(EsbEvent.EsbEventType.MEMBERSHIP_UPDATE.name());
          event.setId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.MEMBERSHIP_UPDATE.id));
          event.setFieldName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.MEMBERSHIP_UPDATE.fieldName));
          event.setSubjectId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.MEMBERSHIP_UPDATE.subjectId));
          event.setSourceId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.MEMBERSHIP_UPDATE.sourceId));
          event.setMembershipType(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.MEMBERSHIP_UPDATE.membershipType));
          event.setGroupId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.MEMBERSHIP_UPDATE.groupId));
          event.setGroupName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.MEMBERSHIP_UPDATE.groupName));
          event.setPropertyChanged(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.MEMBERSHIP_UPDATE.propertyChanged));
          event.setPropertyOldValue(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.MEMBERSHIP_UPDATE.propertyOldValue));
          event.setPropertyNewValue(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.MEMBERSHIP_UPDATE.propertyNewValue));

        } else if (changeLogEntry
            .equalsCategoryAndAction(ChangeLogTypeBuiltin.PRIVILEGE_ADD)) {
          event.setEventType(EsbEvent.EsbEventType.PRIVILEGE_ADD.name());
          // next line throws error, so removed
          //event.setId(this.getLabelValue(changeLogEntry, ChangeLogLabels.PRIVILEGE_ADD.id));
          event.setPrivilegeName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.PRIVILEGE_ADD.privilegeName));
          event.setSubjectId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.PRIVILEGE_ADD.subjectId));
          event.setSourceId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.PRIVILEGE_ADD.sourceId));
          event.setPrivilegeType(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.PRIVILEGE_ADD.privilegeType));
          event.setOwnerType(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.PRIVILEGE_ADD.ownerType));
          event.setOwnerId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.PRIVILEGE_ADD.ownerId));
          event.setOwnerName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.PRIVILEGE_ADD.ownerName));

        } else if (changeLogEntry
            .equalsCategoryAndAction(ChangeLogTypeBuiltin.PRIVILEGE_DELETE)) {
          event.setEventType(EsbEvent.EsbEventType.PRIVILEGE_DELETE.name());
          event.setId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.PRIVILEGE_DELETE.id));
          event.setPrivilegeName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.PRIVILEGE_DELETE.privilegeName));
          event.setSubjectId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.PRIVILEGE_DELETE.subjectId));
          event.setSourceId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.PRIVILEGE_DELETE.sourceId));
          event.setPrivilegeType(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.PRIVILEGE_DELETE.privilegeType));
          event.setOwnerType(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.PRIVILEGE_DELETE.ownerType));
          event.setOwnerId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.PRIVILEGE_DELETE.ownerId));
          event.setOwnerName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.PRIVILEGE_DELETE.ownerName));

        } else if (changeLogEntry
            .equalsCategoryAndAction(ChangeLogTypeBuiltin.PRIVILEGE_UPDATE)) {
          event.setEventType(EsbEvent.EsbEventType.PRIVILEGE_UPDATE.name());
          event.setId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.PRIVILEGE_UPDATE.id));
          event.setPrivilegeName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.PRIVILEGE_UPDATE.privilegeName));
          event.setSubjectId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.PRIVILEGE_UPDATE.subjectId));
          event.setSourceId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.PRIVILEGE_UPDATE.sourceId));
          event.setPrivilegeType(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.PRIVILEGE_UPDATE.privilegeType));
          event.setOwnerType(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.PRIVILEGE_UPDATE.ownerType));
          event.setOwnerId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.PRIVILEGE_UPDATE.ownerId));
          event.setOwnerName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.PRIVILEGE_UPDATE.ownerName));

        } else if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.STEM_ADD)) {
          event.setEventType(EsbEvent.EsbEventType.STEM_ADD.name());
          event.setId(this.getLabelValue(changeLogEntry, ChangeLogLabels.STEM_ADD.id));
          event
              .setName(this.getLabelValue(changeLogEntry, ChangeLogLabels.STEM_ADD.name));
          event.setParentStemId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.STEM_ADD.parentStemId));
          event.setDisplayName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.STEM_ADD.displayName));
          event.setDescription(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.STEM_ADD.description));

        } else if (changeLogEntry
            .equalsCategoryAndAction(ChangeLogTypeBuiltin.STEM_DELETE)) {
          event.setEventType(EsbEvent.EsbEventType.STEM_DELETE.name());
          event.setId(this.getLabelValue(changeLogEntry, ChangeLogLabels.STEM_DELETE.id));
          event.setName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.STEM_DELETE.name));
          event.setParentStemId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.STEM_DELETE.parentStemId));
          event.setDisplayName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.STEM_DELETE.displayName));
          event.setDescription(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.STEM_DELETE.description));

        } else if (changeLogEntry
            .equalsCategoryAndAction(ChangeLogTypeBuiltin.STEM_UPDATE)) {

          event.setEventType(EsbEvent.EsbEventType.STEM_UPDATE.name());
          event.setId(this.getLabelValue(changeLogEntry, ChangeLogLabels.STEM_UPDATE.id));
          event.setName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.STEM_UPDATE.name));
          event.setParentStemId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.STEM_UPDATE.parentStemId));
          event.setDisplayName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.STEM_UPDATE.displayName));
          event.setDisplayExtension(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.STEM_UPDATE.displayExtension));
          event.setDescription(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.STEM_UPDATE.description));
          event.setPropertyChanged(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.STEM_UPDATE.propertyChanged));
          event.setPropertyOldValue(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.STEM_UPDATE.propertyOldValue));
          event.setPropertyNewValue(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.STEM_UPDATE.propertyNewValue));

        }
        if (LOG.isDebugEnabled()) {
          debugMap.put("eventType", event.getEventType());
        }

        if (event.getEventType() != null) {
          // convert to JSON and process

          if (!GrouperLoaderConfig.retrieveConfig().propertyValueString(
              "changeLog.consumer." + consumerName + ".publisher.addSubjectAttributes",
              "").equals("")) {
            // add subject attributes if configured
            event = this.addSubjectAttributes(event, GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer." + consumerName
                    + ".publisher.addSubjectAttributes"));
          }
          // add event to array, only one event supported for now
          EsbEvents events = new EsbEvents();
          events.addEsbEvent(event);
          String eventJsonString = null;
          
          eventJsonString = GrouperUtil.jsonConvertToNoWrap(events);
          //String eventJsonString = gson.toJson(event);
          // add indenting for debugging
          // add subject attributes if configured

          if (GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.consumer." + consumerName
              + ".publisher.debug", false)) {
            eventJsonString = GrouperUtil.indent(eventJsonString, false);
          }
          //System.out.println(eventJsonString);
          if (this.esbPublisherBase == null) {
            String theClassName = GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer." + consumerName
                    + ".publisher.class");
            Class<?> theClass = GrouperUtil.forName(theClassName);
            if (LOG.isDebugEnabled()) {
              debugMap.put("publisherClass", theClassName);
            }
            esbPublisherBase = (EsbListenerBase) GrouperUtil.newInstance(theClass);
          }
          String elFilter = GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer."
              + consumerName + ".elfilter", "");
          if (LOG.isDebugEnabled()) {
            debugMap.put("elFilter", elFilter);
          }
          boolean processEvent = true;
          
          if (!StringUtils.isBlank(elFilter)) {
            boolean matchesFilter = matchesFilter(event, elFilter);
              if (LOG.isDebugEnabled()) {
              debugMap.put("matchesFilter", matchesFilter);
            }
            if (!matchesFilter) {
              processEvent = false;
            }
          }
          if (processEvent) {
              if (esbPublisherBase.dispatchEvent(eventJsonString, consumerName)) {
                //OK;
                if (LOG.isDebugEnabled()) {
                  debugMap.put("processed", true);
                }
              } else {
              if (LOG.isDebugEnabled()) {
                debugMap.put("processed", false);
              }
                // error, need to retry
                changeLogProcessorMetadata.registerProblem(null,
                    "Error processing record " + event.getSequenceNumber(), currentId);
                //we made it to this -1
                return currentId - 1;
              }
            }
          } else {
            if (LOG.isDebugEnabled()) {
              debugMap.put("unsupportedEvant", event.getType());
            }
          }

        }
        //we successfully processed this record

    } catch (Exception e) {
      LOG.error("problem", e);
      changeLogProcessorMetadata.registerProblem(e, "Error processing record " + currentId, currentId);
      //we made it to this -1
      return currentId - 1;
    } finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
    }
    }
    
    if (currentId == -1) {
      throw new RuntimeException("Couldn't process any records");
    }
    if (this.esbPublisherBase != null) {
      this.esbPublisherBase.disconnect();
    }
    return currentId;
  }

  /**
   * 
   * @param changeLogEntry
   * @param changeLogLabel
   * @return label value
   */
  private String getLabelValue(ChangeLogEntry changeLogEntry,
      ChangeLogLabel changeLogLabel) {
    try {
      return changeLogEntry.retrieveValueForLabel(changeLogLabel);
    } catch (Exception e) {
      //cannot get value for label
      if (LOG.isDebugEnabled()) {
        LOG.debug("Cannot get value for label: " + changeLogLabel.name());
      }
      return null;
    }
  }

  /**
   * Add subject attributes to event
   * @param esbEvent
   * @param attributes (comma delimited)
   * @return esbEvent 
   */
  private EsbEvent addSubjectAttributes(EsbEvent esbEvent, String attributes) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Adding subject attributes to event");
    }
    Subject subject = esbEvent.retrieveSubject();
    if (subject != null) {
      String[] attributesArray = attributes.split(",");
      for (int i = 0; i < attributesArray.length; i++) {
        String attributeName = attributesArray[i];
        String attributeValue = subject.getAttributeValueOrCommaSeparated(attributeName);
        if (GrouperUtil.isBlank(attributeValue)) {
          if (StringUtils.equals("name", attributeName)) {
            attributeValue = subject.getName();
          } else if (StringUtils.equals("description", attributeName)) {
            attributeValue = subject.getDescription();
          } 
        }
        if (!StringUtils.isBlank(attributeValue)) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Adding subject attribute " + attributeName + " value "
                + attributeValue);
          }
          esbEvent.addSubjectAttribute(attributeName, attributeValue);
        }
      }
    }
    return esbEvent;

  }

  /**
   * see if the esb event matches an EL filter.  Note the available objects are
   * event for the EsbEvent, and grouperUtil for the GrouperUtil class which has
   * a lot of utility methods
   * @param filterString
   * @param esbEvent
   * @return true if matches, false if doesnt
   */
  public static boolean matchesFilter(EsbEvent esbEvent, String filterString) {
    
    Map<String, Object> elVariables = new HashMap<String, Object>();
    elVariables.put("event", esbEvent);
    elVariables.put("grouperUtilElSafe", new GrouperUtil());
    
    String resultString = GrouperUtil.substituteExpressionLanguage("${" + filterString + "}", elVariables, true, true, true);
    
    boolean result = GrouperUtil.booleanValue(resultString, false);
    
    return result;
  }
}
