/*
 * @author Rob Hebron
 */

package edu.internet2.middleware.grouper.changeLog.esb.consumer;

import java.util.List;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.SubjectFinder;
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

  private EsbListenerBase esbPublisherBase;

  private static final Log LOG = GrouperUtil.getLog(ChangeLogConsumerBase.class);

  @Override
  public long processChangeLogEntries(
      List<ChangeLogEntry> changeLogEntryList,
      ChangeLogProcessorMetadata changeLogProcessorMetadata) {
    String consumerName = changeLogProcessorMetadata.getConsumerName();
    long currentId = -1;

    //try catch so we can track that we made some progress
    try {
      for (ChangeLogEntry changeLogEntry : changeLogEntryList) {

        currentId = changeLogEntry.getSequenceNumber();
        if (LOG.isDebugEnabled()) {
          LOG.debug("Processing event number " + currentId);
        }
        EsbEvent event = new EsbEvent();
        //if this is a group type add action and category
        if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_ADD)) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Event is GROUP_ADD");
          }
          event.setEventType("GROUP_ADD");
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
          if (LOG.isDebugEnabled()) {
            LOG.debug("Event is GROUP_DELETE");
          }
          event.setEventType("GROUP_DELETE");
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

        } else if (changeLogEntry
            .equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_FIELD_ADD)) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Event is GROUP_FIELD_ADD");
          }
          event.setEventType("GROUP_FIELD_ADD");
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
          if (LOG.isDebugEnabled()) {
            LOG.debug("Event is GROUP_FIELD_DELETE");
          }
          event.setEventType("GROUP_FIELD_DELETE");
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
          if (LOG.isDebugEnabled()) {
            LOG.debug("Event is GROUP_FIELD_UPDATE");
          }
          event.setEventType("GROUP_FIELD_UPDATE");
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
            .equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_TYPE_ADD)) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Event is GROUP_TYPE_ADD");
          }
          event.setEventType("GROUP_TYPE_ADD");
          event.setId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_TYPE_ADD.id));
          event.setName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_TYPE_ADD.name));

        } else if (changeLogEntry
            .equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_TYPE_DELETE)) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Event is GROUP_TYPE_DELETE");
          }
          event.setEventType("GROUP_TYPE_DELETE");

          event.setId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_TYPE_DELETE.id));
          event.setName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_TYPE_DELETE.name));

        } else if (changeLogEntry
            .equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_TYPE_UPDATE)) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Event is GROUP_TYPE_UPDATE");
          }
          event.setEventType("GROUP_TYPE_UPDATE");
          event.setId(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_TYPE_UPDATE.id));
          event.setName(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_TYPE_UPDATE.name));
          event.setPropertyChanged(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_TYPE_UPDATE.propertyChanged));
          event.setPropertyOldValue(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_TYPE_UPDATE.propertyOldValue));
          event.setPropertyNewValue(this.getLabelValue(changeLogEntry,
              ChangeLogLabels.GROUP_TYPE_UPDATE.propertyNewValue));

        } else if (changeLogEntry
            .equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_UPDATE)) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Event is GROUP_UPDATE");
          }
          event.setEventType("GROUP_UPDATE");
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
            .equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_ADD)) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Event is MEMBERSHIP_ADD");
          }
          event.setEventType("MEMBERSHIP_ADD");
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
          if (LOG.isDebugEnabled()) {
            LOG.debug("Event is MEMBERSHIP_DELETE");
          }
          event.setEventType("MEMBERSHIP_DELETE");
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
          if (LOG.isDebugEnabled()) {
            LOG.debug("Event is MEMBERSHIP_UPDATE");
          }
          event.setEventType("MEMBERSHIP_UPDATE");
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
          if (LOG.isDebugEnabled()) {
            LOG.debug("Event is PRIVILEGE_ADD");
          }
          event.setEventType("PRIVILEGE_ADD");
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
          if (LOG.isDebugEnabled()) {
            LOG.debug("Event is PRIVILEGE_DELETE");
          }
          event.setEventType("PRIVILEGE_DELETE");
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
          if (LOG.isDebugEnabled()) {
            LOG.debug("Event is PRIVILEGE_UPDATE");
          }
          event.setEventType("PRIVILEGE_UPDATE");
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
          if (LOG.isDebugEnabled()) {
            LOG.debug("Event is STEM_ADD");
          }
          event.setEventType("STEM_ADD");
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
          if (LOG.isDebugEnabled()) {
            LOG.debug("Event is STEM_DELETE");
          }
          event.setEventType("STEM_DELETE");
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

          event.setEventType("STEM_UPDATE");
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
        if (event.getEventType() != null) {
          // convert to JSON and process

          if (!GrouperLoaderConfig.getPropertyString(
              "changeLog.consumer." + consumerName + ".publisher.addSubjectAttributes",
              "").equals("")) {
            // add subject attributes if configured
            event = this.addSubjectAttributes(event, GrouperLoaderConfig
                .getPropertyString("changeLog.consumer." + consumerName
                    + ".publisher.addSubjectAttributes"));
          }
          String eventJsonString = GrouperUtil.jsonConvertToNoWrap(event);
          //String eventJsonString = gson.toJson(event);
          // add indenting for debugging
          // add subject attributes if configured

          if (GrouperLoaderConfig.getPropertyBoolean("changeLog.consumer." + consumerName
              + ".publisher.debug", false)) {
            eventJsonString = GrouperUtil.indent(eventJsonString, false);
          }
          //System.out.println(eventJsonString);
          if (this.esbPublisherBase == null) {
            String theClassName = GrouperLoaderConfig
                .getPropertyString("changeLog.consumer." + consumerName
                    + ".publisher.class");
            Class<?> theClass = GrouperUtil.forName(theClassName);
            if (LOG.isDebugEnabled()) {
              LOG.debug("Creating instance of class " + theClass.getCanonicalName()
                  + " to process event");
            }
            esbPublisherBase = (EsbListenerBase) GrouperUtil.newInstance(theClass);
          }
          String elFilter = GrouperLoaderConfig.getPropertyString("changeLog.consumer."
              + consumerName + ".elfilter", "");
          if (elFilter != null && !elFilter.equals("")) {
            JexlEngine jexl = new JexlEngine();
            Expression e = jexl.createExpression(elFilter);
            JexlContext jc = new MapContext();
            jc.set("event", event);
            if (!(Boolean) e.evaluate(jc)) {
              if (LOG.isDebugEnabled()) {
                LOG.debug("Event does not match consumer filter " + elFilter);
              }
            } else {
              if (LOG.isDebugEnabled()) {
                LOG.debug("Event matches filter " + elFilter + ", processing");
              }
              if (esbPublisherBase.dispatchEvent(eventJsonString, consumerName)) {
                //OK;
                if (LOG.isDebugEnabled()) {
                  LOG.debug("Event " + currentId + " processed");
                }
              } else {
                // error, need to retry
                changeLogProcessorMetadata.registerProblem(null,
                    "Error processing record", currentId);
                //we made it to this -1
                return currentId - 1;
              }
            }
          } else if (esbPublisherBase.dispatchEvent(eventJsonString, consumerName)) {
            //OK;
            if (LOG.isDebugEnabled()) {
              LOG.debug("No filter configured, event processed");
            }
          } else {
            // error, need to retry
            if (LOG.isDebugEnabled()) {
              LOG.debug("No filter configured, event processed");
            }
            changeLogProcessorMetadata.registerProblem(null, "Error processing record",
                currentId);
            //we made it to this -1
            return currentId - 1;
          }
        } else {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Unsupported event " + event.getType());
          }
        }

      }
      //we successfully processed this record

    } catch (Exception e) {
      changeLogProcessorMetadata.registerProblem(e, "Error processing record", currentId);
      //we made it to this -1
      return currentId - 1;
    }
    if (currentId == -1) {
      throw new RuntimeException("Couldn't process any records");
    }
    this.esbPublisherBase.disconnect();
    return currentId;
  }

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
    if (esbEvent.getSubjectId() != null) {
      Subject subject = SubjectFinder.findById(esbEvent.getSubjectId(), false);
      String[] attributesArray = attributes.split(",");
      for (int i = 0; i < attributesArray.length; i++) {
        String attributeName = attributesArray[i];
        String attributeValue = subject.getAttributeValueOrCommaSeparated(attributeName);
        if (attributeValue != null) {
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
}
