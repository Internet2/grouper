package edu.internet2.middleware.grouper.changeLog;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITAttributeDef;
import edu.internet2.middleware.grouper.pit.PITField;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.grouper.pit.PITMember;
import edu.internet2.middleware.grouper.pit.PITMembership;
import edu.internet2.middleware.grouper.pit.PITStem;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * convert the temp objects to regular objects
 * @author mchyzer
 *
 */
public class ChangeLogTempToEntity {

  /**
   * convert the temps to regulars, assign id's
   * @param hib3GrouperLoaderLog is the log object to post updates, can be null
   * @return the number of records converted
   */
  public static int convertRecords() {
    return convertRecords(null);
  }
  
  /**
   * convert the temps to regulars, assign id's
   * @param hib3GrouperLoaderLog is the log object to post updates, can be null
   * @return the number of records converted
   */
  public static int convertRecords(Hib3GrouperLoaderLog hib3GrouperLoaderLog) {
    
    final boolean includeNonFlattenedMemberships = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeNonFlattenedMemberships", false);
    final boolean includeNonFlattenedPrivileges = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeNonFlattenedPrivileges", false);
    
    int count = 0;
    
    //first select the temp records
    List<ChangeLogEntry> changeLogEntryList = HibernateSession.byHqlStatic().createQuery("from ChangeLogEntryTemp order by createdOnDb")
      .options(new QueryOptions().paging(1000, 1, false)).list(ChangeLogEntry.class);
    
    //note: this is not in a transaction, though the inner one is
    for (ChangeLogEntry changeLogEntry : changeLogEntryList) {
      
      final ChangeLogEntry CHANGE_LOG_ENTRY = changeLogEntry;
      
      HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, 
          AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {
              
              if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_ADD)) {
                ChangeLogTempToEntity.processGroupAdd(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_UPDATE)) {
                ChangeLogTempToEntity.processGroupUpdate(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_DELETE)) {
                ChangeLogTempToEntity.processGroupDelete(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.STEM_ADD)) {
                ChangeLogTempToEntity.processStemAdd(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.STEM_UPDATE)) {
                ChangeLogTempToEntity.processStemUpdate(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.STEM_DELETE)) {
                ChangeLogTempToEntity.processStemDelete(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_DEF_ADD)) {
                ChangeLogTempToEntity.processAttributeDefAdd(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_DEF_UPDATE)) {
                ChangeLogTempToEntity.processAttributeDefUpdate(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_DEF_DELETE)) {
                ChangeLogTempToEntity.processAttributeDefDelete(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_FIELD_ADD)) {
                ChangeLogTempToEntity.processFieldAdd(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_FIELD_UPDATE)) {
                ChangeLogTempToEntity.processFieldUpdate(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_FIELD_DELETE)) {
                ChangeLogTempToEntity.processFieldDelete(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_TYPE_ASSIGN)) {
                ChangeLogTempToEntity.processGroupTypeAssign(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_TYPE_UNASSIGN)) {
                ChangeLogTempToEntity.processGroupTypeUnassign(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBER_ADD)) {
                ChangeLogTempToEntity.processMemberAdd(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBER_UPDATE)) {
                ChangeLogTempToEntity.processMemberUpdate(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_ADD)) {
                ChangeLogTempToEntity.processMembershipAdd(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE)) {
                ChangeLogTempToEntity.processMembershipDelete(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.PRIVILEGE_ADD)) {
                ChangeLogTempToEntity.processPrivilegeAdd(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.PRIVILEGE_DELETE)) {
                ChangeLogTempToEntity.processPrivilegeDelete(CHANGE_LOG_ENTRY);
              }
              
              if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_ADD) ||
                  CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE)) {
                
                if (includeNonFlattenedMemberships) {
                  //insert into the non temp table
                  CHANGE_LOG_ENTRY.setTempObject(false);
                  CHANGE_LOG_ENTRY.save();
                }
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.PRIVILEGE_ADD) ||
                  CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.PRIVILEGE_DELETE)) {
                
                if (includeNonFlattenedPrivileges) {
                  //insert into the non temp table
                  CHANGE_LOG_ENTRY.setTempObject(false);
                  CHANGE_LOG_ENTRY.save();
                }
              } else {
                
                //insert into the non temp table
                CHANGE_LOG_ENTRY.setTempObject(false);
                CHANGE_LOG_ENTRY.save();
              }
              
              //delete from the temp
              CHANGE_LOG_ENTRY.setTempObject(true);
              CHANGE_LOG_ENTRY.delete();
              return null;
            }
        
      });
    }
    
    count += changeLogEntryList.size();

    if (count > 0 && hib3GrouperLoaderLog != null) {
      hib3GrouperLoaderLog.addTotalCount(count);
      hib3GrouperLoaderLog.store();
    }
    
    if (changeLogEntryList.size() == 1000) {
      count += convertRecords(hib3GrouperLoaderLog);
    }
    
    return count;
  }
  
  /**
   * If a group gets added, we need to add it to the PIT table.
   * @param changeLogEntry
   */
  private static void processGroupAdd(ChangeLogEntry changeLogEntry) {
    
    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_ADD.id);
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    
    PITGroup pitGroup = new PITGroup();
    pitGroup.setId(id);
    pitGroup.setNameDb(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_ADD.name));    
    pitGroup.setContextId(contextId);
    
    pitGroup.saveOrUpdate();
    
    // Add PIT group sets
    GrouperDAOFactory.getFactory().getPITGroupSet().insertSelfGroupSetsByOwner(id, changeLogEntry.getCreatedOnDb(), contextId, false);
  }
  
  /**
   * If a group gets updated, we may need to update the PIT table.
   * @param changeLogEntry
   */
  private static void processGroupUpdate(ChangeLogEntry changeLogEntry) {
    
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    
    if (changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyChanged).equals("name")) {
      PITGroup pitGroup = GrouperDAOFactory.getFactory().getPITGroup().findById(
          changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.id));
      pitGroup.setNameDb(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.name));
      pitGroup.setContextId(contextId);
      pitGroup.saveOrUpdate();
    }
  }
  
  /**
   * Need to update groupSets
   * @param changeLogEntry
   */
  private static void processGroupDelete(ChangeLogEntry changeLogEntry) {
    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_DELETE.id);
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    Long endTime = changeLogEntry.getCreatedOnDb();
    
    GrouperDAOFactory.getFactory().getPITGroupSet().updateEndTimeByOwner(id, endTime, contextId);
  }
  
  /**
   * If a stem gets added, we need to add it to the PIT table.
   * @param changeLogEntry
   */
  private static void processStemAdd(ChangeLogEntry changeLogEntry) {
    PITStem pitStem = new PITStem();
    
    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.STEM_ADD.id);
    String name = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.STEM_ADD.name);
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();

    if (GrouperUtil.isEmpty(name)) {
      // is this the root stem??
      name = null;
      Stem root = GrouperDAOFactory.getFactory().getStem().findByName(Stem.ROOT_INT, true, null);
      if (root.getUuid().equals(id)) {
        name = Stem.ROOT_INT;
      }
    }
    
    pitStem.setId(id);
    pitStem.setNameDb(name);
    pitStem.setContextId(contextId);
    
    pitStem.saveOrUpdate();
    
    // Add PIT group sets
    GrouperDAOFactory.getFactory().getPITGroupSet().insertSelfGroupSetsByOwner(id, changeLogEntry.getCreatedOnDb(), contextId, false);
  }
  
  /**
   * If a stem gets updated, we may need to update the PIT table.
   * @param changeLogEntry
   */
  private static void processStemUpdate(ChangeLogEntry changeLogEntry) {

    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();

    if (changeLogEntry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.propertyChanged).equals("name")) {
      PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findById(
          changeLogEntry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.id));
      pitStem.setNameDb(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.name));
      pitStem.setContextId(contextId);
      pitStem.saveOrUpdate();
    }
  }
  
  /**
   * Need to update groupSets
   * @param changeLogEntry
   */
  private static void processStemDelete(ChangeLogEntry changeLogEntry) {
    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.STEM_DELETE.id);
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    Long endTime = changeLogEntry.getCreatedOnDb();
    
    GrouperDAOFactory.getFactory().getPITGroupSet().updateEndTimeByOwner(id, endTime, contextId);
  }
  
  /**
   * If an attribute def gets added, we need to add it to the PIT table.
   * @param changeLogEntry
   */
  private static void processAttributeDefAdd(ChangeLogEntry changeLogEntry) {
    
    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_ADD.id);
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();

    PITAttributeDef pitAttributeDef = new PITAttributeDef();
    pitAttributeDef.setId(id);
    pitAttributeDef.setNameDb(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_ADD.name));
    pitAttributeDef.setContextId(contextId);

    pitAttributeDef.saveOrUpdate();
    
    // Add PIT group sets
    GrouperDAOFactory.getFactory().getPITGroupSet().insertSelfGroupSetsByOwner(id, changeLogEntry.getCreatedOnDb(), contextId, false);
  }
  
  /**
   * If an attribute def gets updated, we may need to update the PIT table.
   * @param changeLogEntry
   */
  private static void processAttributeDefUpdate(ChangeLogEntry changeLogEntry) {
    
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();

    if (changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_UPDATE.propertyChanged).equals("name")) {
      PITAttributeDef pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findById(
          changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_UPDATE.id));
      pitAttributeDef.setNameDb(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_UPDATE.name));
      pitAttributeDef.setContextId(contextId);
      pitAttributeDef.saveOrUpdate();
    }
  }
  
  /**
   * Need to update groupSets
   * @param changeLogEntry
   */
  private static void processAttributeDefDelete(ChangeLogEntry changeLogEntry) {
    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_DELETE.id);
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    Long endTime = changeLogEntry.getCreatedOnDb();
    
    GrouperDAOFactory.getFactory().getPITGroupSet().updateEndTimeByOwner(id, endTime, contextId);
  }
  
  /**
   * If a field gets added, we need to add it to the PIT table.
   * @param changeLogEntry
   */
  private static void processFieldAdd(ChangeLogEntry changeLogEntry) {
    PITField pitField = new PITField();
    
    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_ADD.id);
    String name = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_ADD.name);
    String type = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_ADD.type);
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();

    pitField.setId(id);
    pitField.setNameDb(name);
    pitField.setTypeDb(type);
    pitField.setContextId(contextId);
    
    pitField.saveOrUpdate();
    
    // might have to add PIT group sets...
    Field field = FieldFinder.findById(id, false);
    if (field != null && field.isGroupListField()) {      
      GrouperDAOFactory.getFactory().getPITGroupSet().insertSelfGroupSetsByField(id, changeLogEntry.getCreatedOnDb(), contextId);
    }
  }
  
  private static void processFieldDelete(ChangeLogEntry changeLogEntry) {
    
    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_DELETE.id);
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    Long endTime = changeLogEntry.getCreatedOnDb();
    
    GrouperDAOFactory.getFactory().getPITGroupSet().updateEndTimeByField(id, endTime, contextId);
  }
  
  /**
   * If a field gets updated, we may need to update the PIT table.
   * @param changeLogEntry
   */
  private static void processFieldUpdate(ChangeLogEntry changeLogEntry) {

    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();

    if (changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_UPDATE.propertyChanged).equals("name") ||
        changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_UPDATE.propertyChanged).equals("type")) {
      PITField pitField = GrouperDAOFactory.getFactory().getPITField().findById(
          changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_UPDATE.id));
      pitField.setNameDb(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_UPDATE.name));
      pitField.setTypeDb(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_UPDATE.type));
      pitField.setContextId(contextId);
      pitField.saveOrUpdate();
    }
  }
  
  /**
   * @param changeLogEntry
   */
  private static void processGroupTypeAssign(ChangeLogEntry changeLogEntry) {
    String groupId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_TYPE_ASSIGN.groupId);
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    
    // add group sets
    GrouperDAOFactory.getFactory().getPITGroupSet().insertSelfGroupSetsByOwner(groupId, changeLogEntry.getCreatedOnDb(), contextId, true);
  }
  
  /**
   * @param changeLogEntry
   */
  private static void processGroupTypeUnassign(ChangeLogEntry changeLogEntry) {
    
    String typeId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_TYPE_ASSIGN.typeId);
    String groupId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_TYPE_ASSIGN.groupId);
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    Long endTime = changeLogEntry.getCreatedOnDb();

    // remove group sets
    Set<Field> fields = FieldFinder.findAllByGroupType(typeId);
    Iterator<Field> iter = fields.iterator();
    
    while (iter.hasNext()) {
      Field field = iter.next();
      if (field.isGroupListField()) {
        GrouperDAOFactory.getFactory().getPITGroupSet().updateEndTimeByOwnerAndField(groupId, field.getUuid(), endTime, contextId);
      }
    }
  }
  
  /**
   * If a member gets added, we need to add it to the PIT table.
   * @param changeLogEntry
   */
  private static void processMemberAdd(ChangeLogEntry changeLogEntry) {
    PITMember pitMember = new PITMember();
    
    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_ADD.id);
    String subjectId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_ADD.subjectId);
    String subjectSourceId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_ADD.subjectSourceId);
    String subjectTypeId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_ADD.subjectTypeId);
    
    pitMember.setId(id);
    pitMember.setSubjectId(subjectId);
    pitMember.setSubjectSourceId(subjectSourceId);
    pitMember.setSubjectTypeId(subjectTypeId);

    if (!GrouperUtil.isEmpty(changeLogEntry.getContextId())) {
      pitMember.setContextId(changeLogEntry.getContextId());
    }
    
    pitMember.saveOrUpdate();
  }
  
  /**
   * If a member gets updated, we may need to update the PIT table.
   * @param changeLogEntry
   */
  private static void processMemberUpdate(ChangeLogEntry changeLogEntry) {

    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();

    if (changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyChanged).equals("subjectId") ||
        changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyChanged).equals("subjectSourceId") ||
        changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyChanged).equals("subjectTypeId")) {
      PITMember pitMember = GrouperDAOFactory.getFactory().getPITMember().findById(
          changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.id));
      pitMember.setSubjectId(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectId));
      pitMember.setSubjectSourceId(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectSourceId));
      pitMember.setSubjectTypeId(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectTypeId));
      pitMember.setContextId(contextId);
      pitMember.saveOrUpdate();
    }
  }
 
  /**
   * If a membership gets added, then the membership needs to
   * get added to the PIT table.
   * @param changeLogEntry
   */
  private static void processMembershipAdd(ChangeLogEntry changeLogEntry) {
    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.id);
    String groupId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupId);
    String fieldId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldId);
    String memberId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.memberId);
    Long time = changeLogEntry.getCreatedOnDb();
    
    PITMembership pitMembership = new PITMembership();
    pitMembership.setId(id);
    pitMembership.setOwnerGroupId(groupId);
    pitMembership.setMemberId(memberId);
    pitMembership.setFieldId(fieldId);
    pitMembership.setActiveDb("T");
    pitMembership.setStartTimeDb(time);
    
    if (!GrouperUtil.isEmpty(changeLogEntry.getContextId())) {
      pitMembership.setContextId(changeLogEntry.getContextId());
    }
    
    boolean includeFlattenedMemberships = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeFlattenedMemberships", true);
    pitMembership.setFlatNotificationsOnSaveOrUpdate(includeFlattenedMemberships);
    
    pitMembership.save();
  }
  
  /**
   * If a membership gets deleted, then the membership needs to
   * get deleted from the PIT table.
   * @param changeLogEntry
   */
  private static void processMembershipDelete(ChangeLogEntry changeLogEntry) {
    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.id);
    Long time = changeLogEntry.getCreatedOnDb();

    PITMembership pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findById(id);
    pitMembership.setEndTimeDb(time);
    pitMembership.setActiveDb("F");

    if (!GrouperUtil.isEmpty(changeLogEntry.getContextId())) {
      pitMembership.setContextId(changeLogEntry.getContextId());
    }
    
    boolean includeFlattenedMemberships = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeFlattenedMemberships", true);
    pitMembership.setFlatNotificationsOnSaveOrUpdate(includeFlattenedMemberships);
    
    pitMembership.update();
  }
  
  /**
   * If an access, naming, or attr def privilege gets added, the privilege needs to
   * get added to the PIT table.
   * @param changeLogEntry
   */
  private static void processPrivilegeAdd(ChangeLogEntry changeLogEntry) {
    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.id);
    String ownerType = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerType);
    String ownerId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerId);
    String fieldId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.fieldId);
    String memberId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.memberId);    
    Long time = changeLogEntry.getCreatedOnDb();
    
    PITMembership pitMembership = new PITMembership();
    pitMembership.setId(id);
    pitMembership.setMemberId(memberId);
    pitMembership.setFieldId(fieldId);
    pitMembership.setActiveDb("T");
    pitMembership.setStartTimeDb(time);
    
    if (ownerType.equals(Membership.OWNER_TYPE_GROUP)) {
      pitMembership.setOwnerGroupId(ownerId);
    } else if (ownerType.equals(Membership.OWNER_TYPE_STEM)) {
      pitMembership.setOwnerStemId(ownerId);
    } else if (ownerType.equals(Membership.OWNER_TYPE_ATTRIBUTE_DEF)) {
      pitMembership.setOwnerAttrDefId(ownerId);
    } else {
      throw new RuntimeException("unexpected ownerType: " + ownerType);
    }
    
    if (!GrouperUtil.isEmpty(changeLogEntry.getContextId())) {
      pitMembership.setContextId(changeLogEntry.getContextId());
    }
    
    boolean includeFlattenedPrivileges = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeFlattenedPrivileges", true);
    pitMembership.setFlatNotificationsOnSaveOrUpdate(includeFlattenedPrivileges);
    
    pitMembership.save();
  }
  
  
  /**
   * If an access, naming, or attr def privilege gets deleted, the privilege needs to
   * get deleted from the PIT table.
   * @param changeLogEntry
   */
  private static void processPrivilegeDelete(ChangeLogEntry changeLogEntry) {
    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.id);
    Long time = changeLogEntry.getCreatedOnDb();

    PITMembership pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findById(id);
    pitMembership.setEndTimeDb(time);
    pitMembership.setActiveDb("F");
    
    if (!GrouperUtil.isEmpty(changeLogEntry.getContextId())) {
      pitMembership.setContextId(changeLogEntry.getContextId());
    }
    
    boolean includeFlattenedPrivileges = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeFlattenedPrivileges", true);
    pitMembership.setFlatNotificationsOnSaveOrUpdate(includeFlattenedPrivileges);
    
    pitMembership.update();
  }
}
