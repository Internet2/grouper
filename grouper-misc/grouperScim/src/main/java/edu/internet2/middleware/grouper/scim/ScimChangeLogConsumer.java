/*
 * Copyright 2013 Internet2.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouper.scim;

import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata;
import edu.internet2.middleware.grouper.changeLog.ChangeLogType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import java.util.List;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;

/**
 *
 * @author davel
 */
public class ScimChangeLogConsumer extends ChangeLogConsumerBase {

	protected static final Log log = GrouperUtil.getLog(ScimChangeLogConsumer.class);

	private ScimEmitter scim;
	private GrouperSession gs;
	
	/**
	 * The change log consumer name from the processor metadata.
	 */
	private String name;

	private void processGroupAdd(ScimChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) {
		scim.createGroup(GroupFinder.findByName(gs, changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_ADD.name), true));
	}

	private void processGroupDelete(ScimChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) {
		scim.deleteGroup(GroupFinder.findByName(gs, changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_DELETE.name), true));
	}

	private void processGroupUpdate(ScimChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) {
		scim.updateGroup(GroupFinder.findByName(gs,changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_UPDATE.name), true));
	}

	private void processMembershipAdd(ScimChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) {
		scim.updateGroup(GroupFinder.findByName(gs,changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName), true));
	}

	private void processMembershipDelete(ScimChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) {
		scim.updateGroup(GroupFinder.findByName(gs,changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName), true));
	}

	/**
	 * Maps change log entry category and action (change log type) to methods.
	 */
	enum ScimEventType {

		/**
		 * Process the add group change log entry type.
		 */
		group__addGroup {
					public void process(ScimChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) throws Exception {
						consumer.processGroupAdd(consumer, changeLogEntry);
					}
				},
		/**
		 * Process the delete group change log entry type.
		 */
		group__deleteGroup {
					public void process(ScimChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) throws Exception {
						consumer.processGroupDelete(consumer, changeLogEntry);
					}
				},
		/**
		 * Process the update group change log entry type.
		 */
		group__updateGroup {
					public void process(ScimChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) throws Exception {
						consumer.processGroupUpdate(consumer, changeLogEntry);
					}
				},
		/**
		 * Process the add membership change log entry type.
		 */
		membership__addMembership {
					public void process(ScimChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) throws Exception {
						consumer.processMembershipAdd(consumer, changeLogEntry);
					}
				},
		/**
		 * Process the delete membership change log entry type.
		 */
		membership__deleteMembership {
					public void process(ScimChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) throws Exception {
						consumer.processMembershipDelete(consumer, changeLogEntry);
					}
				},
		;
		 /**
         * Process the change log entry.
         * 
         * @param consumer the scim change log consumer
         * @param changeLogEntry the change log entry
         * @throws Exception if any error occurs
         */
        public abstract void process(ScimChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) throws Exception;

	};

	public ScimChangeLogConsumer() {
		scim = new ScimEmitter();
		gs = new GrouperSession();
	}

	@Override
	public long processChangeLogEntries(List<ChangeLogEntry> changeLogEntryList, ChangeLogProcessorMetadata changeLogProcessorMetadata) {
		long currentId = -1;

		// initialize this consumer's name from the change log metadata
		if (name == null) {
			name = changeLogProcessorMetadata.getConsumerName();
			log.trace("ScimChangeLog Consumer '" + name + "' - Setting name.");
		}

		for (ChangeLogEntry entry : changeLogEntryList) {
			currentId = entry.getSequenceNumber();
			// find the method to run via the enum
			String enumKey = entry.getChangeLogType().getChangeLogCategory() + "__"
					+ entry.getChangeLogType().getActionName();

			ScimEventType scimEventType = ScimEventType.valueOf(enumKey);

			if (scimEventType == null) {
				log.debug("ScimChangeLog Consumer '" + name + "' - Change log entry '" + entry + "' Unsupported category and action.");
			} else {
				// process the change log event
				log.info("ScimChangeLog Consumer '" + name + "' - Change log entry '" + toStringDeep(entry) + "'");
				try{
					scimEventType.process(this, entry);
				}catch(Exception e){
					log.warn("ScimChangeLog Consumer '"+ name +"' exception "+e.getMessage(),e);
				}
				log.info("ScimChangeLog Consumer '" + name + "' - Change log entry '" + entry + "'");
			}

		}
		return currentId;
	}

	/**
	 * Return a string representing an {ChangeLogEntry}.
	 *
	 * Returns all labels (attributes).
	 *
	 * Uses {
	 *
	 * @ToStringBuilder}.
	 *
	 * @param changeLogEntry the change log entry
	 * @return the string representing the entire change log entry
	 */
	public static String toStringDeep(ChangeLogEntry changeLogEntry) {
		ToStringBuilder toStringBuilder = new ToStringBuilder(changeLogEntry, ToStringStyle.SHORT_PREFIX_STYLE);
		toStringBuilder.append("timestamp", changeLogEntry.getCreatedOn());
		toStringBuilder.append("sequence", changeLogEntry.getSequenceNumber());
		toStringBuilder.append("category", changeLogEntry.getChangeLogType().getChangeLogCategory());
		toStringBuilder.append("actionname", changeLogEntry.getChangeLogType().getActionName());
		toStringBuilder.append("contextId", changeLogEntry.getContextId());

		ChangeLogType changeLogType = changeLogEntry.getChangeLogType();

		for (String label : changeLogType.labels()) {
			toStringBuilder.append(label, changeLogEntry.retrieveValueForLabel(label));
		}

		return toStringBuilder.toString();
	}

}
