package edu.internet2.middleware.grouper.pspng;

import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabel;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ChangelogHandlingConfig {
    private final static Logger LOG = LoggerFactory.getLogger(ChangelogHandlingConfig.class);

    ////////////////////////////
    // How to find when group-selection might change (mostly attributes and folders)
    public static final Set<ChangeLogTypeBuiltin> changelogTypesThatAreHandledViaFullSync
        = new HashSet<>(Arrays.asList(
            ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ADD,
            ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_DELETE,
            ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_ADD,
            ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_DELETE,
            ChangeLogTypeBuiltin.GROUP_UPDATE,
            ChangeLogTypeBuiltin.STEM_UPDATE));


    // What changes flow into the incremental-syncing part of PSPNG
    public static final Set<ChangeLogTypeBuiltin> changelogTypesThatAreHandledIncrementally
        = new HashSet<>(Arrays.asList(
            ChangeLogTypeBuiltin.GROUP_ADD,
            ChangeLogTypeBuiltin.GROUP_DELETE,
            ChangeLogTypeBuiltin.MEMBERSHIP_ADD,
            ChangeLogTypeBuiltin.MEMBERSHIP_DELETE));


    // What changes are processed at all
    public static final Set<ChangeLogTypeBuiltin> allRelevantChangelogTypes
        = new HashSet<>();
    static {
        allRelevantChangelogTypes.addAll(changelogTypesThatAreHandledViaFullSync);
        allRelevantChangelogTypes.addAll(changelogTypesThatAreHandledIncrementally);
    }


    // Group-information needs to be flushed whenever anything changes, except memberships
    public static final Set<ChangeLogTypeBuiltin> relevantChangesThatNeedGroupCacheFlushing
        = new HashSet<>(allRelevantChangelogTypes);
    static {
        relevantChangesThatNeedGroupCacheFlushing.remove(ChangeLogTypeBuiltin.MEMBERSHIP_ADD);
        relevantChangesThatNeedGroupCacheFlushing.remove(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE);
    }


    ////////////////////
    // How to find attribute names

    private static final Map<ChangeLogTypeBuiltin, ChangeLogLabel> changelogType2attributeNameLabel
            = new HashMap<>();
    static {
        changelogType2attributeNameLabel.put(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_ADD, ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeDefNameName);
        changelogType2attributeNameLabel.put(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_DELETE, ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.attributeDefNameName);
        changelogType2attributeNameLabel.put(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ADD, ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeDefNameName);
        changelogType2attributeNameLabel.put(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_DELETE, ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.attributeDefNameName);
    }


    ///////////////////////
    // How to find group names in changelog entries

    private static final Map<ChangeLogTypeBuiltin, ChangeLogLabel> changelogType2groupNameFields
        = new HashMap<>();
    
    static {
        changelogType2groupNameFields.put(ChangeLogTypeBuiltin.GROUP_ADD, ChangeLogLabels.GROUP_ADD.name);
        changelogType2groupNameFields.put(ChangeLogTypeBuiltin.GROUP_DELETE, ChangeLogLabels.GROUP_DELETE.name);
        changelogType2groupNameFields.put(ChangeLogTypeBuiltin.GROUP_UPDATE, ChangeLogLabels.GROUP_UPDATE.name);
        changelogType2groupNameFields.put(ChangeLogTypeBuiltin.MEMBERSHIP_ADD, ChangeLogLabels.MEMBERSHIP_ADD.groupName);
        changelogType2groupNameFields.put(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE, ChangeLogLabels.MEMBERSHIP_DELETE.groupName);
        changelogType2groupNameFields.put(ChangeLogTypeBuiltin.GROUP_TYPE_ASSIGN, ChangeLogLabels.GROUP_TYPE_ASSIGN.groupName);
        changelogType2groupNameFields.put(ChangeLogTypeBuiltin.GROUP_TYPE_UNASSIGN, ChangeLogLabels.GROUP_TYPE_UNASSIGN.groupName);
    }

    ///////////////////////
    // How to find group idIndex's in changelog entries

    private static final Map<ChangeLogTypeBuiltin, ChangeLogLabel> changelogType2groupIdIndexFields
            = new HashMap<>();

    static {
        changelogType2groupIdIndexFields.put(ChangeLogTypeBuiltin.GROUP_ADD, ChangeLogLabels.GROUP_ADD.idIndex);
        changelogType2groupIdIndexFields.put(ChangeLogTypeBuiltin.GROUP_DELETE, ChangeLogLabels.GROUP_DELETE.idIndex);
    }

    //////////////////////////////////
    // How to find subjects in changelog entries

    private static final Map<ChangeLogTypeBuiltin, ChangeLogLabel> changelogType2subjectIdLookupFields
            = new HashMap<>();
    private static final Map<ChangeLogTypeBuiltin, ChangeLogLabel> changelogType2subjectSourceLookupFields
            = new HashMap<>();
    static {
        changelogType2subjectIdLookupFields.put(ChangeLogTypeBuiltin.MEMBERSHIP_ADD, ChangeLogLabels.MEMBERSHIP_ADD.subjectId);
        changelogType2subjectSourceLookupFields.put(ChangeLogTypeBuiltin.MEMBERSHIP_ADD, ChangeLogLabels.MEMBERSHIP_ADD.sourceId);

        changelogType2subjectIdLookupFields.put(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE, ChangeLogLabels.MEMBERSHIP_DELETE.subjectId);
        changelogType2subjectSourceLookupFields.put(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE, ChangeLogLabels.MEMBERSHIP_DELETE.sourceId);

        changelogType2subjectIdLookupFields.put(ChangeLogTypeBuiltin.MEMBER_ADD, ChangeLogLabels.MEMBER_ADD.subjectId);
        changelogType2subjectSourceLookupFields.put(ChangeLogTypeBuiltin.MEMBER_ADD, ChangeLogLabels.MEMBER_ADD.subjectSourceId);

        changelogType2subjectIdLookupFields.put(ChangeLogTypeBuiltin.MEMBER_DELETE, ChangeLogLabels.MEMBER_DELETE.subjectId);
        changelogType2subjectSourceLookupFields.put(ChangeLogTypeBuiltin.MEMBER_DELETE, ChangeLogLabels.MEMBER_DELETE.subjectSourceId);
    }

    /**
     * Because changeLogEntries can't provide their ChangeLogTypeBuiltin values directly,
     * we can't do set or map lookups. This function essentially does a 'contains()' operation
     * @param entry
     * @param types
     * @return
     */
    public static boolean containsChangelogEntryType(Collection<ChangeLogTypeBuiltin> types, ChangeLogEntry entry) {
        if ( entry == null ) {
            return false;
        }

        for ( ChangeLogTypeBuiltin type : types ) {
            if ( entry.equalsCategoryAndAction(type) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Because changeLogEntries can't provide their ChangeLogTypeBuiltin values directly,
     * but can instead can only be compared, we can't do direct map lookups, but instead
     * have to loop through the keys and use ChangeLogEntry.equalsCategoryAndAction().
     *
     * This function essentially does a Map 'get()' operation based on the changelog entry's
     * type
     * @param entry
     * @param map
     * @return
     */
    private static <T> T getFromChangelogTypesMap(
            String mapName,
            Map<ChangeLogTypeBuiltin, T> map,
            ChangeLogEntry entry) {

        if ( entry == null ) {
            return null;
        }

        T result = null;

        for ( ChangeLogTypeBuiltin type : map.keySet() ) {
            if ( entry.equalsCategoryAndAction(type) ) {
                result = map.get(type);
            }
        }
        LOG.debug("ChangelogConfig: {}.get({}/{})={}",
                mapName, entry.getSequenceNumber(), entry.getChangeLogType(), result);
        return result;
    }

    public static String getGroupName(ChangeLogEntry entry) {
        ChangeLogLabel label = getFromChangelogTypesMap("GroupNameFieldLabels", changelogType2groupNameFields, entry);
        if ( label != null ) {
            return entry.retrieveValueForLabel(label);
        } else {
            return null;
        }
    }

    public static Long getGroupId(ChangeLogEntry entry) {
        ChangeLogLabel label =  getFromChangelogTypesMap("GroupIdFieldLabels", changelogType2groupIdIndexFields, entry);
        if ( label != null ) {
            String groupIdString = entry.retrieveValueForLabel(label);
            if ( groupIdString != null ) {
                return Long.parseLong(groupIdString);
            }
        }
        return null;
    }

    public static String getAttributeName(ChangeLogEntry entry) {
        ChangeLogLabel label =  getFromChangelogTypesMap("AttributeNameFieldLabels", changelogType2attributeNameLabel, entry);
        if ( label != null ) {
            return entry.retrieveValueForLabel(label);
        } else {
            return null;
        }

    }
    public static String getSubjectId(ChangeLogEntry entry) {
        ChangeLogLabel label =  getFromChangelogTypesMap("SubjectIdFieldLabels", changelogType2subjectIdLookupFields, entry);
        if ( label != null ) {
            return entry.retrieveValueForLabel(label);
        } else {
            return null;
        }

    }
    public static String getSubjectSource(ChangeLogEntry entry) {
        ChangeLogLabel label =  getFromChangelogTypesMap("SubjectSourceFieldLabels", changelogType2subjectSourceLookupFields, entry);
        if ( label != null ) {
            return entry.retrieveValueForLabel(label);
        } else {
            return null;
        }

    }
}
