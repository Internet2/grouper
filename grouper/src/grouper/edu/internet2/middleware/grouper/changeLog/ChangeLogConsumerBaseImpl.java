package edu.internet2.middleware.grouper.changeLog;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cache.EhcacheController;
import edu.internet2.middleware.grouper.pit.PITAttributeAssign;
import edu.internet2.middleware.grouper.pit.PITAttributeDefName;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.grouper.pit.finder.PITAttributeAssignFinder;
import edu.internet2.middleware.grouper.pit.finder.PITAttributeDefNameFinder;
import edu.internet2.middleware.grouper.pit.finder.PITGroupFinder;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;

/**
 * Default @ChangeLogConsumerBase implementation. This class gets instantiated by grouper
 * for every run of processChangeLogEntries().
 */
public class ChangeLogConsumerBaseImpl extends ChangeLogConsumerBase {

  private static final Logger LOG = LoggerFactory
      .getLogger(ChangeLogConsumerBaseImpl.class);

  /**
   * Cache of previously seen grouper folders and groups names and if they are marked for provisioning.
   * folder or group name, marked or not marked
   **/
  private HashMap<String, String> markedFoldersAndGroups = new HashMap<String, String>(
      256);

  private static final String MARKED = "marked";

  private static final String NOT_MARKED = "not marked";

  private static final String FOLDER = "stem";

  private static final String GROUP = "group";

  /** Maps supported changeLogEntry category and action to methods */
  enum ChangeLogEventType {
    group_addGroup {

      public void process(ChangeLogEntry changeLogEntry,
          ChangeLogConsumerBaseImpl consumer) {
        // does this event pertain to us? was the group or one of its parent folders marked for sync
        final String groupName = changeLogEntry
            .retrieveValueForLabel(ChangeLogLabels.GROUP_ADD.name);
        final Group group = GroupFinder.findByName(
            GrouperSession.staticGrouperSession(false), groupName, false);
        if (group != null) {
          if (consumer.isGroupMarkedForSync(groupName)) {
            consumer.addGroup(group, changeLogEntry, consumer);
          } else {
            // skipping changeLogEntry that doesn't pertain to us
            LOG.debug("{} skipping addGroup since {} is not marked for sync",
                consumer.consumerName, groupName);
          }
        } else {
          // group deleted before sync'd to target
          LOG.debug(
              "{} skipping addGroup since {} was deleted from grouper before sync'd to target",
              consumer.consumerName, groupName);
        }
      }
    },
    group_updateGroup {

      public void process(ChangeLogEntry changeLogEntry,
          ChangeLogConsumerBaseImpl consumer) {
        // a group "move" is a change in group name and shows up in change log as a group update
        if ("name".equals(changeLogEntry
            .retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyChanged))) {
          // group was moved to a new folder, get the new name and the old name
          final String newGroupName = changeLogEntry
              .retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyNewValue);
          final String oldGroupName = changeLogEntry
              .retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyOldValue);

          // if newGroupName is marked for sync then rename or add group and membership at target
          // this covers three cases:
          //  1) directly marked group moved
          //  2) indirectly marked group moved and is still indirectly marked
          //  3) unmarked group move and is now marked (indirectly)
          if (consumer.isGroupMarkedForSync(newGroupName)) {
            consumer.renameGroup(oldGroupName, newGroupName, changeLogEntry, consumer);
            LOG.debug(
                "{} processed groupUpdate for group move. group {} is marked so calling renameGroup for old group {}.",
                new Object[] { consumer.consumerName, newGroupName, oldGroupName });
          } else {
            // newGroupName not marked, check oldGroupName folders for indirect mark, if so delete at target, otherwise no-op

            // get the parent folder path name from some:parent:path:oldGroupName
            final int lastPathSeparator = oldGroupName.lastIndexOf(":");
            final String oldGroupNameFolderPath = oldGroupName.substring(0,
                lastPathSeparator);
            final Stem oldGroupParentFolder = StemFinder
                .findByName(GrouperSession.staticGrouperSession(false),
                    oldGroupNameFolderPath, false);
            if (oldGroupParentFolder != null
                && oldGroupParentFolder.getAttributeDelegate()
                    .hasAttributeOrAncestorHasAttribute(consumer.syncAttribute.getName(),
                        false)) {
              // oldGroupName previously marked, so remove from target
              consumer.removeMovedGroup(oldGroupName, changeLogEntry, consumer);
              LOG.debug(
                  "{} processed groupUpdate for group move. group {} is no longer marked so calling removeMovedGroup for old group {}.",
                  new Object[] { consumer.consumerName, newGroupName, oldGroupName });
            } else {
              // could find moved parent folder (already deleted?) or not previously marked, no-op in either case
              LOG.debug(
                  "{} processed groupUpdate for group {} move. Couldn't find parent folder for old group {}, or it was not previously marked, no-op in either case.",
                  new Object[] { consumer.consumerName, newGroupName, oldGroupName });
            }
          }
        } else {
          // some other group property update; description, parentStemId, displayName, etc
          final String propertyChanged = changeLogEntry
              .retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyChanged);
          final String groupName = changeLogEntry
              .retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.name);
          LOG.debug(
              "{} processed groupUpdate for group {}. Property changed {} is not supported, so nothing to do.",
              new Object[] { consumer.consumerName, groupName, propertyChanged });
        }
      }
    },
    group_deleteGroup {

      // case when group with marker on a parent folder is deleted, remove the group from target
      public void process(ChangeLogEntry changeLogEntry,
          ChangeLogConsumerBaseImpl consumer) {
        final String groupName = changeLogEntry
            .retrieveValueForLabel(ChangeLogLabels.GROUP_DELETE.name);
        PITGroup pitGroup = PITGroupFinder.findMostRecentByName(groupName, false);
        if (pitGroup != null) {
          if (consumer.isGroupMarkedForSync(pitGroup.getName())) {
            // case when marker is on parent folder of deleted group
            // TODO handle deleted group when marker is on parent folder of deleted group...
            LOG.debug(
                "{} found attributeSync marker for deleted group {}, calling removeDeletedGroup",
                consumer.consumerName, groupName);
            consumer.removeDeletedGroup(pitGroup, changeLogEntry, consumer);
          } else {
            // skipping changeLogEntry that doesn't pertain to us
            LOG.debug("{} skipping deleteGroup since group {} is not marked for sync",
                consumer.consumerName, groupName);
          }
        } else {
          // should ever get here?
          LOG.debug(
              "{} Couldn't find deleted group {} in PIT, already purged?. Let fullSync sort it out",
              consumer.consumerName, groupName);
        }
      }
    },
    membership_addMembership {

      public void process(ChangeLogEntry changeLogEntry,
          ChangeLogConsumerBaseImpl consumer) {
        final String groupName = changeLogEntry
            .retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName);
        final Group group = GroupFinder.findByName(
            GrouperSession.staticGrouperSession(false), groupName, false);
        final String subjectId = changeLogEntry
            .retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId);
        final Subject subject = SubjectFinder.findById(subjectId, false);
        // we only care about effective membership for people in marked groups, skip addMembership for everything else
        if (!subject.getType().equals(SubjectTypeEnum.PERSON)) {
          LOG.debug("{} skipping addMembership since subject {} is not a person",
              consumer.consumerName, subject.getName());
        } else {
          if (consumer.isGroupMarkedForSync(groupName)) {
            consumer.addMembership(subject, group, changeLogEntry, consumer);
          } else {
            // skipping changeLogEntry that doesn't pertain to us
            LOG.debug(
                "{} skipping addMembership for subject {} since group {} is not marked for sync",
                new Object[] { consumer.consumerName, subject.getName(), groupName });
          }
        }
      }
    },
    membership_deleteMembership {

      public void process(ChangeLogEntry changeLogEntry,
          ChangeLogConsumerBaseImpl consumer) {
        final String groupName = changeLogEntry
            .retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName);
        final Group group = GroupFinder.findByName(
            GrouperSession.staticGrouperSession(false), groupName, false);
        final String subjectId = changeLogEntry
            .retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.subjectId);
        final Subject subject = SubjectFinder.findById(subjectId, false);
        // we only care about effective membership for people in marked groups, skip deleteMembership for everything else
        if (!subject.getType().equals(SubjectTypeEnum.PERSON)) {
          LOG.debug("{} skipping deleteMembership since subject {} is not a person",
              consumer.consumerName, subject.getName());
        } else {
          if (consumer.isGroupMarkedForSync(groupName)) {
            if (group != null) {
              consumer.removeMembership(subject, group, changeLogEntry, consumer);
            } else {
              // ignore for deleted groups, since group will be removed at target anyway.
              LOG.debug(
                  "{} skipping deleteMembership for subject {} since group {} is already deleted",
                  new Object[] { consumer.consumerName, subject.getName(), groupName });
            }
          } else {
            // skipping changeLogEntry that doesn't pertain to us
            LOG.debug(
                "{} skipping deleteMembership for subject {} since group {} is not marked for sync",
                new Object[] { consumer.consumerName, subject.getName(), groupName });
          }
        }
      }
    },
    attributeAssign_addAttributeAssign {

      /**
       * On assignment of the syncAttribute marker, create all the groups or group (if directly assigned)
       * and any current memberships at the target.
       */
      public void process(ChangeLogEntry changeLogEntry,
          ChangeLogConsumerBaseImpl consumer) {
        // check if this is our syncAttribute, otherwise nothing to do.
        final String attributeDefNameName = changeLogEntry
            .retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeDefNameName);
        if (consumer.syncAttribute.getName().equals(attributeDefNameName)) {
          // syncAttribute applied to a group, then create the group at the target
          String assignType = changeLogEntry
              .retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.assignType);
          String ownerId1 = changeLogEntry
              .retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.ownerId1);
          if (GROUP.equals(assignType)) {
            Group markedGroup = GroupFinder.findByUuid(
                GrouperSession.staticGrouperSession(false), ownerId1, false);
            if (markedGroup != null) {
              consumer.addGroupAndMemberships(markedGroup, changeLogEntry, consumer);
            } // couldn't find group, already deleted?
          } else if (FOLDER.equals(assignType)) {
            // syncAttribute applied to a folder, get all the groups below this folder and sub folders and create them at the target
            Stem markedFolder = StemFinder.findByUuid(
                GrouperSession.staticGrouperSession(false), ownerId1, false);
            if (markedFolder != null) {
              Set<Group> markedGroups = markedFolder.getChildGroups(Stem.Scope.SUB);
              for (Group group : markedGroups) {
                consumer.addGroupAndMemberships(group, changeLogEntry, consumer);
              }
            } // couldn't find folder, already deleted?
          }
        } else {
          // not our syncAttribute, so nothing to do
          LOG.debug("{} skipping addAttributeAssign, {} is not our syncAttribute");
        }
      }
    },
    attributeAssign_deleteAttributeAssign {

      /**
       * On removal of the syncAttribute marker, delete all the groups or group (if directly assigned) at the target, unless
       * otherwise still marked by direct assignment or a parent folder.
       */
      public void process(ChangeLogEntry changeLogEntry,
          ChangeLogConsumerBaseImpl consumer) {
        // is this our syncAttribute? otherwise nothing to do.
        final String attributeDefNameName = changeLogEntry
            .retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.attributeDefNameName);
        if (consumer.syncAttribute.getName().equals(attributeDefNameName)) {
          String assignType = changeLogEntry
              .retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.assignType);
          String ownerId1 = changeLogEntry
              .retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.ownerId1);
          // is it for a group? then delete the group at the target, unless a parent folder is also marked.
          if (GROUP.equals(assignType)) {
            // get the group and then check for a marked parent folder before deleting at target.
            Group group = GroupFinder.findByUuid(
                GrouperSession.staticGrouperSession(false), ownerId1, false);
            if (group != null) {
              // case when group had a direct syncAttribute marker assignment removed, does it still have a parent marker?
              // need to flush edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate.objectHasAttributeCache to make sure syncAttribute is cleared
              EhcacheController
                  .ehcacheController()
                  .getGrouperCache(
                      "edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate.objectHasAttributeCache")
                  .clear();
              if (group.getAttributeDelegate().hasAttributeOrAncestorHasAttribute(
                  consumer.syncAttribute.getName(), false)) {
                LOG.debug(
                    "{} processed deleteAttributeAssign {} for group {}, but still marked by a parent folder.",
                    new Object[] { consumer.consumerName, attributeDefNameName,
                        group.getName() });
              } else {
                // marker syncAttribute removed from group and no other parent folder marked so delete at target
                LOG.debug(
                    "{} processed deleteAttributeAssign {} for group {}, no other mark found so calling removeGroup",
                    new Object[] { consumer.consumerName, attributeDefNameName,
                        group.getName() });
                consumer.removeGroup(group, changeLogEntry, consumer);
              }
            } else {
              // case when a group which had a direct syncAttribute marker was deleted, always delete at target
              PITGroup pitGroup = PITGroupFinder.findBySourceId(ownerId1, false)
                  .iterator().next();
              if (pitGroup != null) {
                String pitGroupName = pitGroup.getName();
                // marker syncAttribute removed when deleting a group, always delete at target
                LOG.debug(
                    "{} processed deleteAttributeAssign {} for deleted group {}, calling removeDeletedGroup",
                    new Object[] { consumer.consumerName, attributeDefNameName,
                        pitGroupName });
                consumer.removeDeletedGroup(pitGroup, changeLogEntry, consumer);
              } else {
                // couldn't find group anywhere? so can't determine its name.
                LOG.error(
                    "{} failed to find group when processing deleteAttributeAssign so can't determine the group name, let fullSync sort it out.",
                    consumer.consumerName);
              }
            }
          } else if (FOLDER.equals(assignType)) {
            // is it a folder, then delete all the containing groups at the target, unless they are still marked otherwise (direct or indirect)
            Stem unMarkedFolder = StemFinder.findByUuid(
                GrouperSession.staticGrouperSession(false), ownerId1, false);
            if (unMarkedFolder != null) {
              // get all the groups below this folder and sub folders and to see if they are still marked, otherwise delete them at the target
              Set<Group> unMarkedGroups = unMarkedFolder.getChildGroups(Stem.Scope.SUB);
              // need to flush edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate.objectHasAttributeCache to make sure syncAttribute is cleared
              EhcacheController
                  .ehcacheController()
                  .getGrouperCache(
                      "edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate.objectHasAttributeCache")
                  .clear();
              for (Group group : unMarkedGroups) {
                // check that the group isn't marked directly or from some other parent folder
                if (consumer.isGroupMarkedForSync(group.getName())) {
                  LOG.debug(
                      "{} processed deleteAttributeAssign {} for folder {}, found mark for group {} so nothing to do.",
                      new Object[] { consumer.consumerName, attributeDefNameName,
                          unMarkedFolder.getName(), group.getName() });
                } else {
                  LOG.debug(
                      "{} processed deleteAttributeAssign {} for folder {}, no other mark found for group {} so calling removeGroup",
                      new Object[] { consumer.consumerName, attributeDefNameName,
                          unMarkedFolder.getName(), group.getName(), group.getName() });
                  consumer.removeGroup(group, changeLogEntry, consumer);
                }
              }
            } else {
              // can't delete a folder without first deleting child objects, so there should be nothing to delete at the target
              // deleting a folder with an attribute assign will cause a deleteAttributeAssign to come after folder already deleted.
              LOG.error(
                  "{} processing deleteAttributeAssign for deleted folder, nothing to do.",
                  consumer.consumerName);
            }
          }
        }
      }
    };

    /**
     * Placeholder method for mapping category_action to methods.
     * @param changeLogEntry
     * @param changeLogConsumerBaseImpl
     */
    public abstract void process(ChangeLogEntry changeLogEntry,
        ChangeLogConsumerBaseImpl changeLogConsumerBaseImpl);
  }

  /**
   * These methods are expected to be overidden in a subclass that is specific to a provisioning target. (e.g. Google Apps)
   */
  protected void addGroup(Group group, ChangeLogEntry changeLogEntry,
      ChangeLogConsumerBaseImpl consumer) {
    LOG.debug("{} addGroup {} dispatched but not implemented in subclass {}",
        consumerName, consumerClassName);
  }

  protected void updateGroup(Group group, ChangeLogEntry changeLogEntry,
      ChangeLogConsumerBaseImpl consumer) {
    LOG.debug("{} updateGroup {} dispatched but not implemented in subclass {}",
        consumerName, consumerClassName);
  }

  /**
   * renameGroup may be called for groups that have not previously been marked, need to check the target to be sure.
   * If the oldGroupName doesn't exist at the target, then add the newGroupName and all its memberships.
   */
  protected void renameGroup(String oldGroupName, String newGroupName,
      ChangeLogEntry changeLogEntry, ChangeLogConsumerBaseImpl consumer) {
    LOG.debug("{} renameGroup {} to {} dispatched but not implemented in subclass {}",
        new Object[] { consumerName, oldGroupName, newGroupName, consumerClassName });
  }

  protected void removeGroup(Group group, ChangeLogEntry changeLogEntry,
      ChangeLogConsumerBaseImpl consumer) {
    LOG.debug("{} removeGroup {} dispatched but not implemented in subclass {}",
        consumerName, consumerClassName);
  }

  /**
   * removeMovedGroup may be called for groups that have not previously been marked, need to check the target to be sure.
   * If the oldGroupName doesn't exist at the target, this call should be a no-op.
   */
  protected void removeMovedGroup(String oldGroupName, ChangeLogEntry changeLogEntry,
      ChangeLogConsumerBaseImpl consumer) {
    LOG.debug("{} removeMovedGroup {} dispatched but not implemented in subclass {}",
        consumerName, consumerClassName);
  }

  protected void removeDeletedGroup(PITGroup pitGroup, ChangeLogEntry changeLogEntry,
      ChangeLogConsumerBaseImpl consumer) {
    LOG.debug("{} removeDeletedGroup {} dispatched but not implemented in subclass {}",
        consumerName, consumerClassName);
  }

  protected void addMembership(Subject subject, Group group,
      ChangeLogEntry changeLogEntry, ChangeLogConsumerBaseImpl consumer) {
    LOG.debug(
        "{} addMembership {} to group {} dispatched but not implemented in subclass {}",
        new Object[] { consumerName, subject.getName(), group.getName(),
            consumerClassName });
  }

  protected void removeMembership(Subject subject, Group group,
      ChangeLogEntry changeLogEntry, ChangeLogConsumerBaseImpl consumer) {
    LOG.debug(
        "{} dispatched removeMembership {} from group {}, but not implemented in subclass {}",
        new Object[] { consumerName, subject.getName(), group.getName(),
            consumerClassName });
  }

  protected void addGroupAndMemberships(Group group, ChangeLogEntry changeLogEntry,
      ChangeLogConsumerBaseImpl consumer) {
    LOG.debug(
        "{} dispatched createGroupAndMemberships for group {}, but method not implemented in subclass {}",
        new Object[] { consumerName, group.getName(), consumerClassName });
  }

  /**
   * If syncAttribute was applied to the group or one of the parent folders return true
   * Method keeps an internal cache of results per run in markedFoldersAndGroups
   * Will also check the PIT for recently deleted groups
   */
  private boolean isGroupMarkedForSync(String groupName) {

    boolean markedForSync;

    // have we seen this group already in this run
    if (markedFoldersAndGroups.containsKey(groupName)) {
      markedForSync = markedFoldersAndGroups.get(groupName).equals(MARKED);
      LOG.debug(
          "{} found group {} in markedFoldersAndGroups cache, isGroupMarkedForSync: {}",
          new Object[] { consumerName, groupName, markedForSync });
      return markedForSync;
    }

    // looking for the group
    final Group group = GroupFinder.findByCurrentName(
        GrouperSession.staticGrouperSession(false), groupName, false);

    if (group != null) {
      // is it marked with the syncAttribute?
      markedForSync = group.getAttributeDelegate().hasAttributeOrAncestorHasAttribute(
          syncAttribute.getName(), false);
      LOG.debug("{} found group {}, isGroupMarkedForSync: {}", new Object[] {
          consumerName, groupName, markedForSync });
    } else {
      // looking for the deleted group in the PIT
      PITGroup pitGroup = PITGroupFinder.findMostRecentByName(groupName, false);
      if (pitGroup != null) {
        // looking for syncAttribute assignment in the PIT
        Set<PITAttributeDefName> pitSyncAttributes = PITAttributeDefNameFinder
            .findByName(syncAttribute.getName(), false, true);
        PITAttributeDefName pitSyncAttribute = pitSyncAttributes.iterator().next();
        Set<PITAttributeAssign> pitAttributeAssigns = PITAttributeAssignFinder
            .findByOwnerPITGroupAndPITAttributeDefName(pitGroup, pitSyncAttribute,
                pitGroup.getStartTime(), pitGroup.getEndTime());
        // only gets direct group assignments, check folder next if no direct mark
        markedForSync = pitAttributeAssigns.isEmpty();
        if (!markedForSync) {
          // check for folders for mark
          Stem folder = StemFinder.findByUuid(GrouperSession.staticGrouperSession(false),
              pitGroup.getStemId(), false);
          if (folder != null) {
            markedForSync = folder.getAttributeDelegate()
                .hasAttributeOrAncestorHasAttribute(syncAttribute.getName(), false);
            LOG.debug("{} found pitGroup {}, isGroupMarkedForSync: {}", new Object[] {
                consumerName, pitGroup.getName(), markedForSync });
          }
        }
        LOG.debug("{} found pitGroup {}, isGroupMarkedForSync: {}", new Object[] {
            consumerName, pitGroup.getName(), markedForSync });
      } else {
        // couldn't find group anywhere including the PIT
        LOG.debug(
            "{} checking for {} marker, but could not find group {} anywhere, including the PIT.",
            new Object[] { consumerName, syncAttributeName, groupName });
        markedForSync = false;
      }
    }

    // remember this for next time
    if (markedForSync) {
      markedFoldersAndGroups.put(groupName, MARKED);
      return true;
    } else {
      markedFoldersAndGroups.put(groupName, NOT_MARKED);
      return false;
    }
  }

  private String consumerName;

  private String consumerClassName;

  public String getConsumerName() {
    return consumerName;
  }

  /** Name of marker attribute defined in changeLog.consumer.<consumerName>.syncAttributeName */
  private String syncAttributeName;

  private AttributeDefName syncAttribute;

  public static String ATTRIBUTE_CONFIG_FOLDER_NAME = "etc:attribute";

  public static String CONFIG_FOLDER_NAME = "changeLogConsumer";

  /** Property name for marker attribute defined in changeLog.consumer.<consumerName>.syncAttributeName */
  public static String SYNC_ATTRIBUTE_NAME = "syncAttributeName";

  /** Property name for subclassed target consumer defined in changeLog.consumer.<consumerName>.class */
  public static String CONSUMER_CLASS_NAME = "class";

  /**
   * expected to be overidden
   * @param consumerName
   * @return
   */
  protected boolean isFullSyncRunning(String consumerName) {
    return false;
  }

  /**
   * Process the list of changeLogEntries since the last time this consumer was run.
   * This method will be called by grouper daemon (aka grouper loader).
   *
   * @param changeLogEntryList
   * @param changeLogProcessorMetadata
   * @return last processed changeLogEntry Id
   */
  @Override
  public long processChangeLogEntries(List<ChangeLogEntry> changeLogEntryList,
      ChangeLogProcessorMetadata changeLogProcessorMetadata) {

    // Name of the consumer configured in grouper-loader.properties that extends this class
    // e.g. changeLog.consumer.<consumerName>.class = edu.example.changeLogConsumer
    if (consumerName == null) {
      consumerName = changeLogProcessorMetadata.getConsumerName();
    }

    GrouperLoaderConfig config = GrouperLoaderConfig.retrieveConfig();
    syncAttributeName = config.propertyValueStringRequired("changeLog.consumer." + consumerName + "."
        + SYNC_ATTRIBUTE_NAME);
    consumerClassName = config.propertyValueStringRequired("changeLog.consumer." + consumerName + "."
        + CONSUMER_CLASS_NAME);

    // syncAttribute name configured in grouper-loader.properties
    // e.g. changeLog.consumer.<consumerName>.<syncAttributeName> = o365
    if (syncAttribute == null) {
      syncAttribute = AttributeDefNameFinder.findByName(syncAttributeName, false);
      if (syncAttribute == null) {
        throw new RuntimeException("Attribute name doesnt exist, you need to create it via GSH or the UI, something like\n\n"
            + "GrouperSession grouperSession = GrouperSession.startRootSession();\n"
            + "provisioningMarkerAttributeDef = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName(\"" + syncAttributeName + "Def\").assignToStem(true).assignToGroup(true).save();\n"
            + "this.provisioningMarkerAttributeName = new AttributeDefNameSave(grouperSession, this.provisioningMarkerAttributeDef).assignName(\"" + syncAttributeName + "\").save();\n\n");
//        new AttributeDefSave
//        // missing sync attribute, so let's create it
//        // first check for etc:attribute:changeLogConsumer, and create if missing
//        Stem configFolder = StemFinder.findByName(GrouperSession.staticGrouperSession(),
//            ATTRIBUTE_CONFIG_FOLDER_NAME + ":" + CONFIG_FOLDER_NAME, false);
//        if (configFolder == null) {
//          final Stem etcAttributeFolder = StemFinder.findByName(
//              GrouperSession.staticGrouperSession(), ATTRIBUTE_CONFIG_FOLDER_NAME, false);
//          configFolder = etcAttributeFolder.addChildStem(CONFIG_FOLDER_NAME,
//              CONFIG_FOLDER_NAME);
//        }
//        // next check for attribute definition etc:attribute:changeLogConsumer:<syncAttributeName>AttributeDef, and create if missing
//        AttributeDef syncAttrDef = AttributeDefFinder.findByName(syncAttributeName
//            + "Def", false);
//        if (syncAttrDef == null) {
//          LOG.info("{} attribute definition {} not found, creating it now.",
//              consumerName, syncAttributeName + "Def");
//          syncAttrDef = configFolder.addChildAttributeDef(syncAttributeName + "Def",
//              AttributeDefType.attr);
//          syncAttrDef.setAssignToGroup(true);
//          syncAttrDef.setAssignToStem(true);
//          syncAttrDef.store();
//        }
//
//        // finally create the attribute etc:attribute:changeLogConsumer:<syncAttributeName>. This is the marker attribute for the consumer.
//        syncAttribute = configFolder.addChildAttributeDefName(syncAttrDef,
//            syncAttributeName, syncAttributeName);
//        LOG.info("{} created attribute name {}.", consumerName, syncAttributeName);
      }
    }

    // change log sequence to return. will be updated in grouper db to keep track of processing progress.
    long changeLogEntrySequenceNumber = -1;

    // ContextId groups multiple changeLogEntries into a logical change set.
    // Tracking this for performance purposes. is this really needed?
    String currentChangeLogEntryContextId = null;

    LOG.debug("{} ** starting processing run at change log entry {} ** ", consumerName,
        changeLogEntryList.get(0).getSequenceNumber());
    for (ChangeLogEntry changeLogEntry : changeLogEntryList) {

      // TODO bail out as soon as we can determine this change doesn't apply to us
      // is the earliest we can tell?
      // only supporting provisioing attributes on groups and folders (i.e. groups within folders)?
      // capture all group and membership create/updates/delete for marked groups and folders
      // is this a group or membership event?

      changeLogEntrySequenceNumber = changeLogEntry.getSequenceNumber();

      // If full sync is running abort this run and try again later
      if (isFullSyncRunning(consumerName)) {
        return changeLogEntrySequenceNumber - 1;
      }

      // if first time through or new contextID, update the contextId
      if (currentChangeLogEntryContextId == null
          || !changeLogEntry.getContextId().equals(currentChangeLogEntryContextId)) {
        currentChangeLogEntryContextId = changeLogEntry.getContextId();
      }

      try {
        // process the changeLogEntry
        processChangeLogEntry(changeLogEntry);
      } catch (Exception e) {
        String message = consumerName
            + " threw an exception processing change log entry sequence number "
            + changeLogEntrySequenceNumber + ".";
        LOG.error(message, e);
        changeLogProcessorMetadata.registerProblem(e, message,
            changeLogEntrySequenceNumber);
        changeLogProcessorMetadata.setHadProblem(true);
        changeLogProcessorMetadata.setRecordException(e);
        changeLogProcessorMetadata
            .setRecordExceptionSequence(changeLogEntrySequenceNumber);
        // TODO then return previous seq number on retryOnError = true?
        // or just log and keep processing and let full sync clean it up?
      }

    }

    // TODO how would we get here?
    if (changeLogEntrySequenceNumber == -1) {
      throw new RuntimeException(consumerName + "was unable to dispatch any records.");
    }

    LOG.info("{} ** finished processing run at change log entry {} **", consumerName,
        changeLogEntrySequenceNumber);

    // the last changeLogEntrySequence processed
    return changeLogEntrySequenceNumber;
  }

  /**
   * Call the method of the {@link ChangeLogEventType} enum which matches the {@link ChangeLogEntry} category and action (i.e. the change log event type).
   *
   * @param changeLogEntry the change log entry
   */
  private void processChangeLogEntry(ChangeLogEntry changeLogEntry) {

    // construct key from changeLogEntry in the form of <category>_<action>
    final String changeLogEventTypeKey = changeLogEntry.getChangeLogType()
        .getChangeLogCategory() + "_"
        + changeLogEntry.getChangeLogType().getActionName();

    ChangeLogEventType changeLogEventType = null;

    // look up method to map to call it
    try {
      changeLogEventType = ChangeLogEventType.valueOf(changeLogEventTypeKey);
      LOG.debug("{} dispatching change log event {} for change log {}.", new Object[] {
          consumerName, changeLogEventTypeKey, changeLogEntry.getSequenceNumber() });
      changeLogEventType.process(changeLogEntry, this);
    } catch (IllegalArgumentException e) {
      LOG.debug("{} unsupported event {} in change log {}.", new Object[] { consumerName,
          changeLogEventTypeKey, changeLogEntry.getSequenceNumber() });
    }
  }

  // TODO implement change log type mapping to methods
  // TODO in implement class override methods for specific functionality?

}