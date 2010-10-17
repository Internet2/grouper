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
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITAttributeAssign;
import edu.internet2.middleware.grouper.pit.PITAttributeAssignAction;
import edu.internet2.middleware.grouper.pit.PITAttributeAssignActionSet;
import edu.internet2.middleware.grouper.pit.PITAttributeDef;
import edu.internet2.middleware.grouper.pit.PITAttributeDefName;
import edu.internet2.middleware.grouper.pit.PITAttributeDefNameSet;
import edu.internet2.middleware.grouper.pit.PITField;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.grouper.pit.PITMember;
import edu.internet2.middleware.grouper.pit.PITMembership;
import edu.internet2.middleware.grouper.pit.PITRoleSet;
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
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ADD)) {
                ChangeLogTempToEntity.processAttributeAssignAdd(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_DELETE)) {
                ChangeLogTempToEntity.processAttributeAssignDelete(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_DEF_NAME_ADD)) {
                ChangeLogTempToEntity.processAttributeDefNameAdd(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_DEF_NAME_UPDATE)) {
                ChangeLogTempToEntity.processAttributeDefNameUpdate(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_DEF_NAME_DELETE)) {
                ChangeLogTempToEntity.processAttributeDefNameDelete(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_ADD)) {
                ChangeLogTempToEntity.processAttributeAssignActionAdd(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_UPDATE)) {
                ChangeLogTempToEntity.processAttributeAssignActionUpdate(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_DELETE)) {
                ChangeLogTempToEntity.processAttributeAssignActionDelete(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_SET_ADD)) {
                ChangeLogTempToEntity.processAttributeAssignActionSetAdd(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_SET_DELETE)) {
                ChangeLogTempToEntity.processAttributeAssignActionSetDelete(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_DEF_NAME_SET_ADD)) {
                ChangeLogTempToEntity.processAttributeDefNameSetAdd(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_DEF_NAME_SET_DELETE)) {
                ChangeLogTempToEntity.processAttributeDefNameSetDelete(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ROLE_SET_ADD)) {
                ChangeLogTempToEntity.processRoleSetAdd(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ROLE_SET_DELETE)) {
                ChangeLogTempToEntity.processRoleSetDelete(CHANGE_LOG_ENTRY);

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
    pitAttributeDef.setAttributeDefTypeDb(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_ADD.attributeDefType));
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
    boolean includeFlattenedPrivileges = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeFlattenedPrivileges", true);
    boolean includeFlattenedPermissions = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeFlattenedPermissions", true);
    pitMembership.setFlatPermissionNotificationsOnSaveOrUpdate(includeFlattenedPermissions);
    pitMembership.setFlatMembershipNotificationsOnSaveOrUpdate(includeFlattenedMemberships);
    pitMembership.setFlatPrivilegeNotificationsOnSaveOrUpdate(includeFlattenedPrivileges);
    
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
    boolean includeFlattenedPrivileges = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeFlattenedPrivileges", true);
    boolean includeFlattenedPermissions = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeFlattenedPermissions", true);
    pitMembership.setFlatPermissionNotificationsOnSaveOrUpdate(includeFlattenedPermissions);
    pitMembership.setFlatMembershipNotificationsOnSaveOrUpdate(includeFlattenedMemberships);
    pitMembership.setFlatPrivilegeNotificationsOnSaveOrUpdate(includeFlattenedPrivileges);
    
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
    
    boolean includeFlattenedMemberships = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeFlattenedMemberships", true);
    boolean includeFlattenedPrivileges = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeFlattenedPrivileges", true);
    boolean includeFlattenedPermissions = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeFlattenedPermissions", true);
    pitMembership.setFlatPermissionNotificationsOnSaveOrUpdate(includeFlattenedPermissions);
    pitMembership.setFlatMembershipNotificationsOnSaveOrUpdate(includeFlattenedMemberships);
    pitMembership.setFlatPrivilegeNotificationsOnSaveOrUpdate(includeFlattenedPrivileges);
    
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
    
    boolean includeFlattenedMemberships = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeFlattenedMemberships", true);
    boolean includeFlattenedPrivileges = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeFlattenedPrivileges", true);
    boolean includeFlattenedPermissions = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeFlattenedPermissions", true);
    pitMembership.setFlatPermissionNotificationsOnSaveOrUpdate(includeFlattenedPermissions);
    pitMembership.setFlatMembershipNotificationsOnSaveOrUpdate(includeFlattenedMemberships);
    pitMembership.setFlatPrivilegeNotificationsOnSaveOrUpdate(includeFlattenedPrivileges);
    
    pitMembership.update();
  }
  
  /**
   * If an attribute assign gets added, insert into pit table.
   * @param changeLogEntry
   */
  private static void processAttributeAssignAdd(ChangeLogEntry changeLogEntry) {
    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.id);
    String attributeDefNameId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeDefNameId);
    String actionId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeAssignActionId);
    String assignType = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.assignType);
    String ownerId1 = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.ownerId1);    
    String ownerId2 = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.ownerId2);    
    Long time = changeLogEntry.getCreatedOnDb();
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    
    PITAttributeAssign pitAttributeAssign = new PITAttributeAssign();
    pitAttributeAssign.setId(id);
    pitAttributeAssign.setAttributeDefNameId(attributeDefNameId);
    pitAttributeAssign.setAttributeAssignActionId(actionId);
    pitAttributeAssign.setAttributeAssignTypeDb(assignType);
    pitAttributeAssign.setActiveDb("T");
    pitAttributeAssign.setStartTimeDb(time);
    pitAttributeAssign.setContextId(contextId);
    
    if (AttributeAssignType.group.name().equals(assignType)) {
      pitAttributeAssign.setOwnerGroupId(ownerId1);
    } else if (AttributeAssignType.stem.name().equals(assignType)) {
      pitAttributeAssign.setOwnerStemId(ownerId1);
    } else if (AttributeAssignType.member.name().equals(assignType)) {
      pitAttributeAssign.setOwnerMemberId(ownerId1);
    } else if (AttributeAssignType.attr_def.name().equals(assignType)) {
      pitAttributeAssign.setOwnerAttributeDefId(ownerId1);
    } else if (AttributeAssignType.any_mem.name().equals(assignType)) {
      pitAttributeAssign.setOwnerGroupId(ownerId1);
      pitAttributeAssign.setOwnerMemberId(ownerId2);
    } else if (AttributeAssignType.imm_mem.name().equals(assignType)) {
      pitAttributeAssign.setOwnerMembershipId(ownerId1);
    } else {
      // this must be an attribute assign of an attribute assign.  foreign keys will make sure we're right.
      pitAttributeAssign.setOwnerAttributeAssignId(ownerId1);
    }
    
    boolean includeFlattenedPermissions = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeFlattenedPermissions", true);
    pitAttributeAssign.setFlatPermissionNotificationsOnSaveOrUpdate(includeFlattenedPermissions);

    pitAttributeAssign.save();
  }
  
  /**
   * If an attribute assign gets delete, add end time to pit row.
   * @param changeLogEntry
   */
  private static void processAttributeAssignDelete(ChangeLogEntry changeLogEntry) {
    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.id);
    Long time = changeLogEntry.getCreatedOnDb();
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();

    PITAttributeAssign pitAttributeAssign = GrouperDAOFactory.getFactory().getPITAttributeAssign().findById(id);
    pitAttributeAssign.setEndTimeDb(time);
    pitAttributeAssign.setActiveDb("F");
    pitAttributeAssign.setContextId(contextId);
    
    boolean includeFlattenedPermissions = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeFlattenedPermissions", true);
    pitAttributeAssign.setFlatPermissionNotificationsOnSaveOrUpdate(includeFlattenedPermissions);
    
    pitAttributeAssign.update();
  }
  
  /**
   * If an attribute def name gets added, insert into pit table.
   * @param changeLogEntry
   */
  private static void processAttributeDefNameAdd(ChangeLogEntry changeLogEntry) {
    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_ADD.id);
    String attributeDefId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_ADD.attributeDefId);
    String stemId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_ADD.stemId);
    String name = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_ADD.name);
    Long time = changeLogEntry.getCreatedOnDb();
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    
    PITAttributeDefName pitAttributeDefName = new PITAttributeDefName();
    pitAttributeDefName.setId(id);
    pitAttributeDefName.setAttributeDefId(attributeDefId);
    pitAttributeDefName.setStemId(stemId);
    pitAttributeDefName.setNameDb(name);
    pitAttributeDefName.setActiveDb("T");
    pitAttributeDefName.setStartTimeDb(time);
    pitAttributeDefName.setContextId(contextId);

    pitAttributeDefName.saveOrUpdate();
  }
  
  /**
   * If an attribute def name gets updated, we may need to update the PIT table.
   * @param changeLogEntry
   */
  private static void processAttributeDefNameUpdate(ChangeLogEntry changeLogEntry) {
    
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();

    if (changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_UPDATE.propertyChanged).equals("name")) {
      PITAttributeDefName pitAttributeDefName = GrouperDAOFactory.getFactory().getPITAttributeDefName().findById(
          changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_UPDATE.id));
      pitAttributeDefName.setNameDb(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_UPDATE.name));
      pitAttributeDefName.setContextId(contextId);
      pitAttributeDefName.saveOrUpdate();
    }
  }
  
  /**
   * If an attribute def name gets delete, add end time to pit row.
   * @param changeLogEntry
   */
  private static void processAttributeDefNameDelete(ChangeLogEntry changeLogEntry) {
    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_DELETE.id);
    Long time = changeLogEntry.getCreatedOnDb();
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();

    PITAttributeDefName pitAttributeDefName = GrouperDAOFactory.getFactory().getPITAttributeDefName().findById(id);
    pitAttributeDefName.setEndTimeDb(time);
    pitAttributeDefName.setActiveDb("F");
    pitAttributeDefName.setContextId(contextId);
    
    pitAttributeDefName.saveOrUpdate();
  }

  /**
   * If an attribute assign action gets added, insert into pit table.
   * @param changeLogEntry
   */
  private static void processAttributeAssignActionAdd(ChangeLogEntry changeLogEntry) {
    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_ADD.id);
    String attributeDefId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_ADD.attributeDefId);
    String name = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_ADD.name);
    Long time = changeLogEntry.getCreatedOnDb();
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    
    PITAttributeAssignAction pitAttributeAssignAction = new PITAttributeAssignAction();
    pitAttributeAssignAction.setId(id);
    pitAttributeAssignAction.setAttributeDefId(attributeDefId);
    pitAttributeAssignAction.setNameDb(name);
    pitAttributeAssignAction.setActiveDb("T");
    pitAttributeAssignAction.setStartTimeDb(time);
    pitAttributeAssignAction.setContextId(contextId);

    pitAttributeAssignAction.saveOrUpdate();
  }
  
  /**
   * If an attribute assign action gets updated, we may need to update the PIT table.
   * @param changeLogEntry
   */
  private static void processAttributeAssignActionUpdate(ChangeLogEntry changeLogEntry) {
    
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();

    if (changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_UPDATE.propertyChanged).equals("name")) {
      PITAttributeAssignAction pitAttributeAssignAction = GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findById(
          changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_UPDATE.id));
      pitAttributeAssignAction.setNameDb(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_UPDATE.name));
      pitAttributeAssignAction.setContextId(contextId);
      pitAttributeAssignAction.saveOrUpdate();
    }
  }
  
  /**
   * If an attribute assign action gets delete, add end time to pit row.
   * @param changeLogEntry
   */
  private static void processAttributeAssignActionDelete(ChangeLogEntry changeLogEntry) {
    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_DELETE.id);
    Long time = changeLogEntry.getCreatedOnDb();
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();

    PITAttributeAssignAction pitAttributeAssignAction = GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findById(id);
    pitAttributeAssignAction.setEndTimeDb(time);
    pitAttributeAssignAction.setActiveDb("F");
    pitAttributeAssignAction.setContextId(contextId);
    
    pitAttributeAssignAction.saveOrUpdate();
  }
  
  /**
   * If an attribute assign action set gets added, insert into pit table.
   * @param changeLogEntry
   */
  private static void processAttributeAssignActionSetAdd(ChangeLogEntry changeLogEntry) {
    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.id);
    String depth = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.depth);
    String ifHas = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.ifHasAttrAssnActionId);
    String thenHas = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.thenHasAttrAssnActionId);
    String parent = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.parentAttrAssignActionSetId);
    Long time = changeLogEntry.getCreatedOnDb();
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    
    PITAttributeAssignActionSet pitAttributeAssignActionSet = new PITAttributeAssignActionSet();
    pitAttributeAssignActionSet.setId(id);
    pitAttributeAssignActionSet.setDepth(Integer.parseInt(depth));
    pitAttributeAssignActionSet.setIfHasAttrAssignActionId(ifHas);
    pitAttributeAssignActionSet.setThenHasAttrAssignActionId(thenHas);
    pitAttributeAssignActionSet.setParentAttrAssignActionSetId(parent);
    pitAttributeAssignActionSet.setActiveDb("T");
    pitAttributeAssignActionSet.setStartTimeDb(time);
    pitAttributeAssignActionSet.setContextId(contextId);

    boolean includeFlattenedPermissions = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeFlattenedPermissions", true);
    pitAttributeAssignActionSet.setFlatPermissionNotificationsOnSaveOrUpdate(includeFlattenedPermissions);
    
    pitAttributeAssignActionSet.saveOrUpdate();
  }
  
  /**
   * If an attribute assign action set gets delete, add end time to pit row.
   * @param changeLogEntry
   */
  private static void processAttributeAssignActionSetDelete(ChangeLogEntry changeLogEntry) {
    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_DELETE.id);
    Long time = changeLogEntry.getCreatedOnDb();
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();

    PITAttributeAssignActionSet pitAttributeAssignActionSet = GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findById(id);
    pitAttributeAssignActionSet.setEndTimeDb(time);
    pitAttributeAssignActionSet.setActiveDb("F");
    pitAttributeAssignActionSet.setContextId(contextId);
    
    boolean includeFlattenedPermissions = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeFlattenedPermissions", true);
    pitAttributeAssignActionSet.setFlatPermissionNotificationsOnSaveOrUpdate(includeFlattenedPermissions);
    
    pitAttributeAssignActionSet.saveOrUpdate();
  }
  
  /**
   * If an attribute def name set gets added, insert into pit table.
   * @param changeLogEntry
   */
  private static void processAttributeDefNameSetAdd(ChangeLogEntry changeLogEntry) {
    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_ADD.id);
    String depth = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_ADD.depth);
    String ifHas = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_ADD.ifHasAttributeDefNameId);
    String thenHas = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_ADD.thenHasAttributeDefNameId);
    String parent = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_ADD.parentAttrDefNameSetId);
    Long time = changeLogEntry.getCreatedOnDb();
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    
    PITAttributeDefNameSet pitAttributeDefNameSet = new PITAttributeDefNameSet();
    pitAttributeDefNameSet.setId(id);
    pitAttributeDefNameSet.setDepth(Integer.parseInt(depth));
    pitAttributeDefNameSet.setIfHasAttributeDefNameId(ifHas);
    pitAttributeDefNameSet.setThenHasAttributeDefNameId(thenHas);
    pitAttributeDefNameSet.setParentAttrDefNameSetId(parent);
    pitAttributeDefNameSet.setActiveDb("T");
    pitAttributeDefNameSet.setStartTimeDb(time);
    pitAttributeDefNameSet.setContextId(contextId);

    boolean includeFlattenedPermissions = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeFlattenedPermissions", true);
    pitAttributeDefNameSet.setFlatPermissionNotificationsOnSaveOrUpdate(includeFlattenedPermissions);
    
    pitAttributeDefNameSet.saveOrUpdate();
  }
  
  /**
   * If an attribute def name set gets delete, add end time to pit row.
   * @param changeLogEntry
   */
  private static void processAttributeDefNameSetDelete(ChangeLogEntry changeLogEntry) {
    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_DELETE.id);
    Long time = changeLogEntry.getCreatedOnDb();
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();

    PITAttributeDefNameSet pitAttributeDefNameSet = GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findById(id);
    pitAttributeDefNameSet.setEndTimeDb(time);
    pitAttributeDefNameSet.setActiveDb("F");
    pitAttributeDefNameSet.setContextId(contextId);
    
    boolean includeFlattenedPermissions = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeFlattenedPermissions", true);
    pitAttributeDefNameSet.setFlatPermissionNotificationsOnSaveOrUpdate(includeFlattenedPermissions);
    
    pitAttributeDefNameSet.saveOrUpdate();
  }
  
  /**
   * If a role set set gets added, insert into pit table.
   * @param changeLogEntry
   */
  private static void processRoleSetAdd(ChangeLogEntry changeLogEntry) {
    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ROLE_SET_ADD.id);
    String depth = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ROLE_SET_ADD.depth);
    String ifHas = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ROLE_SET_ADD.ifHasRoleId);
    String thenHas = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ROLE_SET_ADD.thenHasRoleId);
    String parent = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ROLE_SET_ADD.parentRoleSetId);
    Long time = changeLogEntry.getCreatedOnDb();
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    
    PITRoleSet pitRoleSet = new PITRoleSet();
    pitRoleSet.setId(id);
    pitRoleSet.setDepth(Integer.parseInt(depth));
    pitRoleSet.setIfHasRoleId(ifHas);
    pitRoleSet.setThenHasRoleId(thenHas);
    pitRoleSet.setParentRoleSetId(parent);
    pitRoleSet.setActiveDb("T");
    pitRoleSet.setStartTimeDb(time);
    pitRoleSet.setContextId(contextId);

    boolean includeFlattenedPermissions = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeFlattenedPermissions", true);
    pitRoleSet.setFlatPermissionNotificationsOnSaveOrUpdate(includeFlattenedPermissions);
    
    pitRoleSet.saveOrUpdate();
  }
  
  /**
   * If a role set set gets delete, add end time to pit row.
   * @param changeLogEntry
   */
  private static void processRoleSetDelete(ChangeLogEntry changeLogEntry) {
    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ROLE_SET_DELETE.id);
    Long time = changeLogEntry.getCreatedOnDb();
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();

    PITRoleSet pitRoleSet = GrouperDAOFactory.getFactory().getPITRoleSet().findById(id);
    pitRoleSet.setEndTimeDb(time);
    pitRoleSet.setActiveDb("F");
    pitRoleSet.setContextId(contextId);
    
    boolean includeFlattenedPermissions = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeFlattenedPermissions", true);
    pitRoleSet.setFlatPermissionNotificationsOnSaveOrUpdate(includeFlattenedPermissions);
    
    pitRoleSet.saveOrUpdate();
  }
}
