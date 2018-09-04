package edu.internet2.middleware.grouper.pspng;

import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabel;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;

import java.util.*;

public class ChangelogHandlingConfig {
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

    public static final Map<ChangeLogTypeBuiltin, ChangeLogLabel> changelogType2attributeNameLabel
            = new HashMap<>();
    static {
        changelogType2attributeNameLabel.put(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_ADD, ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeDefNameName);
        changelogType2attributeNameLabel.put(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_DELETE, ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.attributeDefNameName);
        changelogType2attributeNameLabel.put(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ADD, ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeDefNameName);
        changelogType2attributeNameLabel.put(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_DELETE, ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.attributeDefNameName);
    }


    ///////////////////////
    // How to find groups in changelog entries

    public static final Map<ChangeLogTypeBuiltin, ChangeLogLabel> changelogType2groupLookupFields
        = new HashMap<>();
    
    static {
        changelogType2groupLookupFields.put(ChangeLogTypeBuiltin.GROUP_ADD, ChangeLogLabels.GROUP_ADD.name);
        changelogType2groupLookupFields.put(ChangeLogTypeBuiltin.GROUP_DELETE, ChangeLogLabels.GROUP_DELETE.name);
        changelogType2groupLookupFields.put(ChangeLogTypeBuiltin.GROUP_UPDATE, ChangeLogLabels.GROUP_UPDATE.name);
        changelogType2groupLookupFields.put(ChangeLogTypeBuiltin.MEMBERSHIP_ADD, ChangeLogLabels.MEMBERSHIP_ADD.groupName);
        changelogType2groupLookupFields.put(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE, ChangeLogLabels.MEMBERSHIP_DELETE.groupName);
        changelogType2groupLookupFields.put(ChangeLogTypeBuiltin.GROUP_TYPE_ASSIGN, ChangeLogLabels.GROUP_TYPE_ASSIGN.groupName);
        changelogType2groupLookupFields.put(ChangeLogTypeBuiltin.GROUP_TYPE_UNASSIGN, ChangeLogLabels.GROUP_TYPE_UNASSIGN.groupName);
    }


    //////////////////////////////////
    // How to find subjects in changelog entries

    public static final Map<ChangeLogTypeBuiltin, ChangeLogLabel> changelogType2subjectIdLookupFields
            = new HashMap<>();
    public static final Map<ChangeLogTypeBuiltin, ChangeLogLabel> changelogType2subjectSourceLookupFields
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
     * we can't do set or map lookups. This function essentially does a Map 'get()' operation
     * @param entry
     * @param map
     * @return
     */
    public static <T> T getFromChangelogTypesMap(
            Map<ChangeLogTypeBuiltin, T> map, ChangeLogEntry entry) {

        if ( entry == null ) {
            return null;
        }

        for ( ChangeLogTypeBuiltin type : map.keySet() ) {
            if ( entry.equalsCategoryAndAction(type) ) {
                return map.get(type);
            }
        }
        return null;
    }


}
