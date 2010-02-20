package edu.internet2.middleware.grouper.changeLog;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.flat.FlatAttributeDef;
import edu.internet2.middleware.grouper.flat.FlatGroup;
import edu.internet2.middleware.grouper.flat.FlatMembership;
import edu.internet2.middleware.grouper.flat.FlatStem;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;

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
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_DELETE)) {
                ChangeLogTempToEntity.processGroupDelete(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.STEM_ADD)) {
                ChangeLogTempToEntity.processStemAdd(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.STEM_DELETE)) {
                ChangeLogTempToEntity.processStemDelete(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_DEF_ADD)) {
                ChangeLogTempToEntity.processAttributeDefAdd(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_DEF_DELETE)) {
                ChangeLogTempToEntity.processAttributeDefDelete(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.PRIVILEGE_ADD)) {
                ChangeLogTempToEntity.processPrivilegeAdd(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.PRIVILEGE_DELETE)) {
                ChangeLogTempToEntity.processPrivilegeDelete(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_ADD)) {
                ChangeLogTempToEntity.processMembershipAdd(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE)) {
                ChangeLogTempToEntity.processMembershipDelete(CHANGE_LOG_ENTRY);
              }
              
              if (!CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_ADD) && 
                  !CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE) &&
                  !CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.PRIVILEGE_ADD) &&
                  !CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.PRIVILEGE_DELETE)) {
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
  
  private static void processGroupAdd(ChangeLogEntry changeLogEntry) {
    FlatGroup flatGroup = new FlatGroup();
    flatGroup.setId(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_ADD.id));
    flatGroup.setContextId(changeLogEntry.getContextId());
    
    try {
      flatGroup.save();
    } catch (GrouperDAOException e) {
      // if the group doesn't exist, it's okay.  otherwise, throw this exception.
      Group group = GrouperDAOFactory.getFactory().getGroup().findByUuid(
          flatGroup.getId(), false, new QueryOptions().secondLevelCache(false));
      if (group == null) {
        return;
      }
      
      throw e;
    }
  }
  
  private static void processGroupDelete(ChangeLogEntry changeLogEntry) {
    FlatGroup flatGroup = GrouperDAOFactory.getFactory().getFlatGroup().findById(
        changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_DELETE.id));
    
    if (flatGroup != null) {
      flatGroup.delete();
    }
  }
  
  private static void processStemAdd(ChangeLogEntry changeLogEntry) {
    FlatStem flatStem = new FlatStem();
    flatStem.setId(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.STEM_ADD.id));
    flatStem.setContextId(changeLogEntry.getContextId());

    try {
      flatStem.save();
    } catch (GrouperDAOException e) {
      // if the stem doesn't exist, it's okay.  otherwise, throw this exception.
      Stem stem = GrouperDAOFactory.getFactory().getStem().findByUuid(
          flatStem.getId(), false, new QueryOptions().secondLevelCache(false));
      if (stem == null) {
        return;
      }
      
      throw e;
    }
  }
  
  private static void processStemDelete(ChangeLogEntry changeLogEntry) {
    FlatStem flatStem = GrouperDAOFactory.getFactory().getFlatStem().findById(
        changeLogEntry.retrieveValueForLabel(ChangeLogLabels.STEM_DELETE.id));
    
    if (flatStem != null) {
      flatStem.delete();
    }
  }
  
  private static void processAttributeDefAdd(ChangeLogEntry changeLogEntry) {
    FlatAttributeDef flatAttributeDef = new FlatAttributeDef();
    flatAttributeDef.setId(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_ADD.id));
    flatAttributeDef.setContextId(changeLogEntry.getContextId());

    try {
      flatAttributeDef.save();
    } catch (GrouperDAOException e) {
      // if the attr def doesn't exist, it's okay.  otherwise, throw this exception.
      AttributeDef attributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findById(
          flatAttributeDef.getId(), false);
      if (attributeDef == null) {
        return;
      }
      
      throw e;
    }
  }
  
  private static void processAttributeDefDelete(ChangeLogEntry changeLogEntry) {
    FlatAttributeDef flatAttributeDef = GrouperDAOFactory.getFactory().getFlatAttributeDef().findById(
        changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_DELETE.id));
    
    if (flatAttributeDef != null) {
      flatAttributeDef.delete();
    }
  }
  
  private static void processPrivilegeAdd(ChangeLogEntry changeLogEntry) {
    String ownerType = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerType);
    String ownerName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerName);
    String ownerId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerId);
    String fieldId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.fieldId);
    String privilegeName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeName);
    String contextId = changeLogEntry.getContextId();
    
    processPrivilegeAdd(ownerType, ownerName, ownerId, fieldId, privilegeName, contextId);
  }
  
  private static void processPrivilegeAdd(String ownerType, String ownerName, String ownerId, String fieldId,
      String privilegeName, String contextId) {
  
    String ownerStemId = null;
    String ownerGroupId = null;
    String ownerAttrDefId = null;
    Set<Member> membersToAdd;
    
    // get the members to add to the flat table
    if (ownerType.equals(Membership.OWNER_TYPE_GROUP)) {
      ownerGroupId = ownerId;
      membersToAdd = GrouperDAOFactory.getFactory().getFlatMembership().findMembersToAddByGroupOwnerAndField(ownerId, fieldId);
    } else if (ownerType.equals(Membership.OWNER_TYPE_STEM)) {
      ownerStemId = ownerId;
      membersToAdd = GrouperDAOFactory.getFactory().getFlatMembership().findMembersToAddByStemOwnerAndField(ownerId, fieldId);
    } else if (ownerType.equals(Membership.OWNER_TYPE_ATTRIBUTE_DEF)) {
      ownerAttrDefId = ownerId;
      membersToAdd = GrouperDAOFactory.getFactory().getFlatMembership().findMembersToAddByAttrDefOwnerAndField(ownerId, fieldId);
    } else {
      throw new RuntimeException("unexpected ownerType: " + ownerType);
    }
    
    // now iterate through the set and add the flat memberships
    Iterator<Member> membersToAddIter = membersToAdd.iterator();
    while (membersToAddIter.hasNext()) {
      Member memberToAdd = membersToAddIter.next();

      // we might have to skip some members if they are groups that are not in the flat group table.
      if (memberToAdd.getSubjectTypeId().equals(SubjectTypeEnum.GROUP.getName())) {
        FlatGroup flatGroup = GrouperDAOFactory.getFactory().getFlatGroup().findById(memberToAdd.getSubjectId());
        if (flatGroup == null) {
          continue;
        }
      }
      
      // now add the flat membership
      FlatMembership flatMship = new FlatMembership();
      flatMship.setId(GrouperUuid.getUuid());
      flatMship.setContextId(contextId);
      flatMship.setFieldId(fieldId);
      flatMship.setMemberId(memberToAdd.getUuid());
      flatMship.setOwnerAttrDefId(ownerAttrDefId);
      flatMship.setOwnerGroupId(ownerGroupId);
      flatMship.setOwnerStemId(ownerStemId);
      flatMship.save();
      
      // now add the change log entry
      new ChangeLogEntry(false, ChangeLogTypeBuiltin.PRIVILEGE_ADD, 
          ChangeLogLabels.PRIVILEGE_ADD.id.name(), null,
          ChangeLogLabels.PRIVILEGE_ADD.privilegeName.name(), privilegeName, 
          ChangeLogLabels.PRIVILEGE_ADD.fieldId.name(), fieldId, 
          ChangeLogLabels.PRIVILEGE_ADD.memberId.name(), memberToAdd.getUuid(),
          ChangeLogLabels.PRIVILEGE_ADD.subjectId.name(), memberToAdd.getSubjectId(),
          ChangeLogLabels.PRIVILEGE_ADD.sourceId.name(), memberToAdd.getSubjectSourceId(),
          ChangeLogLabels.PRIVILEGE_ADD.privilegeType.name(), null,
          ChangeLogLabels.PRIVILEGE_ADD.ownerType.name(), ownerType,
          ChangeLogLabels.PRIVILEGE_ADD.ownerId.name(), ownerId,
          ChangeLogLabels.PRIVILEGE_ADD.ownerName.name(), ownerName).save();
    }
  }
  
  private static void processPrivilegeDelete(ChangeLogEntry changeLogEntry) {
    String ownerType = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerType);
    String ownerName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerName);
    String ownerId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerId);
    String fieldId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.fieldId);
    String privilegeName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeName);
    
    processPrivilegeDelete(ownerType, ownerName, ownerId, fieldId, privilegeName);
  }
  
  private static void processPrivilegeDelete(String ownerType, String ownerName, String ownerId, String fieldId, 
      String privilegeName) {
    Set<FlatMembership> flatMembershipsToRemove;
    
    // get the members to remove from the flat table
    if (ownerType.equals(Membership.OWNER_TYPE_GROUP)) {
      flatMembershipsToRemove = GrouperDAOFactory.getFactory().getFlatMembership().findMembersToDeleteByGroupOwnerAndField(ownerId, fieldId);
    } else if (ownerType.equals(Membership.OWNER_TYPE_STEM)) {
      flatMembershipsToRemove = GrouperDAOFactory.getFactory().getFlatMembership().findMembersToDeleteByStemOwnerAndField(ownerId, fieldId);
    } else if (ownerType.equals(Membership.OWNER_TYPE_ATTRIBUTE_DEF)) {
      flatMembershipsToRemove = GrouperDAOFactory.getFactory().getFlatMembership().findMembersToDeleteByAttrDefOwnerAndField(ownerId, fieldId);
    } else {
      throw new RuntimeException("unexpected ownerType: " + ownerType);
    }
    
    // now iterate through the set and remove the flat memberships
    Iterator<FlatMembership> flatMembershipsToRemoveIter = flatMembershipsToRemove.iterator();
    while (flatMembershipsToRemoveIter.hasNext()) {
      FlatMembership flatMembership = flatMembershipsToRemoveIter.next();
      
      // now remove the flat membership
      flatMembership.delete();
      
      // now add the change log entry
      new ChangeLogEntry(false, ChangeLogTypeBuiltin.PRIVILEGE_DELETE, 
          ChangeLogLabels.PRIVILEGE_DELETE.id.name(), null,
          ChangeLogLabels.PRIVILEGE_DELETE.privilegeName.name(), privilegeName, 
          ChangeLogLabels.PRIVILEGE_DELETE.fieldId.name(), fieldId, 
          ChangeLogLabels.PRIVILEGE_DELETE.memberId.name(), flatMembership.getMemberId(),
          ChangeLogLabels.PRIVILEGE_DELETE.subjectId.name(), flatMembership.getMember().getSubjectId(),
          ChangeLogLabels.PRIVILEGE_DELETE.sourceId.name(), flatMembership.getMember().getSubjectSourceId(),
          ChangeLogLabels.PRIVILEGE_DELETE.privilegeType.name(), null,
          ChangeLogLabels.PRIVILEGE_DELETE.ownerType.name(), ownerType,
          ChangeLogLabels.PRIVILEGE_DELETE.ownerId.name(), ownerId,
          ChangeLogLabels.PRIVILEGE_DELETE.ownerName.name(), ownerName).save();
    }
  }
  
  private static void processMembershipAdd(String groupId, String groupName, String fieldId, String fieldName,
      String contextId) {
    // get the members to add to the flat table
    Set<Member> membersToAdd = GrouperDAOFactory.getFactory().getFlatMembership().findMembersToAddByGroupOwnerAndField(groupId, fieldId);
    
    // now iterate through the set and add the flat memberships
    Iterator<Member> membersToAddIter = membersToAdd.iterator();
    while (membersToAddIter.hasNext()) {
      Member memberToAdd = membersToAddIter.next();

      // we might have to skip some members if they are groups that are not in the flat group table.
      if (memberToAdd.getSubjectTypeId().equals(SubjectTypeEnum.GROUP.getName())) {
        FlatGroup flatGroup = GrouperDAOFactory.getFactory().getFlatGroup().findById(memberToAdd.getSubjectId());
        if (flatGroup == null) {
          continue;
        }
      }
      
      // now add the flat membership
      FlatMembership flatMship = new FlatMembership();
      flatMship.setId(GrouperUuid.getUuid());
      flatMship.setContextId(contextId);
      flatMship.setFieldId(fieldId);
      flatMship.setMemberId(memberToAdd.getUuid());
      flatMship.setOwnerGroupId(groupId);
      flatMship.save();
      
      // now add the change log entry
      new ChangeLogEntry(false, ChangeLogTypeBuiltin.MEMBERSHIP_ADD, 
          ChangeLogLabels.MEMBERSHIP_ADD.id.name(), null,
          ChangeLogLabels.MEMBERSHIP_ADD.fieldName.name(), fieldName, 
          ChangeLogLabels.MEMBERSHIP_ADD.fieldId.name(), fieldId, 
          ChangeLogLabels.MEMBERSHIP_ADD.memberId.name(), memberToAdd.getUuid(),
          ChangeLogLabels.MEMBERSHIP_ADD.subjectId.name(), memberToAdd.getSubjectId(),
          ChangeLogLabels.MEMBERSHIP_ADD.sourceId.name(), memberToAdd.getSubjectSourceId(),
          ChangeLogLabels.MEMBERSHIP_ADD.membershipType.name(), null,
          ChangeLogLabels.MEMBERSHIP_ADD.groupId.name(), groupId,
          ChangeLogLabels.MEMBERSHIP_ADD.groupName.name(), groupName).save();
    }
  }
  
  private static void processMembershipAdd(ChangeLogEntry changeLogEntry) {
    String groupId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupId);
    String groupName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName);
    String fieldId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldId);
    String fieldName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldName);
    String contextId = changeLogEntry.getContextId();
    
    processMembershipAdd(groupId, groupName, fieldId, fieldName, contextId);
    
    // if the field is the default list, then we have to see if the owner is a member of other groups, stems, or attr defs
    if (Group.getDefaultList().getUuid().equals(fieldId)) {
      Member memberGroup = GrouperDAOFactory.getFactory().getMember().findBySubject(groupId, "g:gsa", true);
      Set<FlatMembership> flatMships = GrouperDAOFactory.getFactory().getFlatMembership().findByMemberId(memberGroup.getUuid());
      Iterator<FlatMembership> flatMshipsIter = flatMships.iterator();
      while (flatMshipsIter.hasNext()) {
        FlatMembership currFlatMship = flatMshipsIter.next();
        String currOwnerId = currFlatMship.getOwnerId();
        String currFieldId = currFlatMship.getFieldId();
        Field currField = FieldFinder.findById(currFieldId, true);
        String currFieldName = currField.getName();
        
        if (currFlatMship.getOwnerAttrDefId() != null) {
          AttributeDef currAttrDef = GrouperDAOFactory.getFactory().getAttributeDef().findById(currOwnerId, false);
          if (currAttrDef == null) {
            continue;
          }
          
          processPrivilegeAdd(Membership.OWNER_TYPE_ATTRIBUTE_DEF, currAttrDef.getName(), currOwnerId, currFieldId, currFieldName, contextId);
        } else if (currFlatMship.getOwnerStemId() != null) {
          Stem currStem = GrouperDAOFactory.getFactory().getStem().findByUuid(currOwnerId, false);
          if (currStem == null) {
            continue;
          }
          
          processPrivilegeAdd(Membership.OWNER_TYPE_STEM, currStem.getName(), currOwnerId, currFieldId, currFieldName, contextId);
        } else if (currFlatMship.getOwnerGroupId() != null) {
          Group currGroup = GrouperDAOFactory.getFactory().getGroup().findByUuid(currOwnerId, false);
          if (currGroup == null) {
            continue;
          }
                    
          if (FieldType.ACCESS.equals(currField.getType())) {
            processPrivilegeAdd(Membership.OWNER_TYPE_GROUP, currGroup.getName(), currOwnerId, currFieldId, currFieldName, contextId);
          } else {
            processMembershipAdd(currOwnerId, currGroup.getName(), currFieldId, currFieldName, contextId);
          }
          
        } else {
          throw new RuntimeException("owner id not set");
        }
      }
    }
  }
  
  private static void processMembershipDelete(ChangeLogEntry changeLogEntry) {
    String groupId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupId);
    String groupName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName);
    String fieldId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.fieldId);
    String fieldName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.fieldName);
    
    processMembershipDelete(groupId, groupName, fieldId, fieldName);
    
    // if the field is the default list, then we have to see if the owner is a member of other groups, stems, or attr defs
    if (Group.getDefaultList().getUuid().equals(fieldId)) {
      Member memberGroup = GrouperDAOFactory.getFactory().getMember().findBySubject(groupId, "g:gsa", true);
      Set<FlatMembership> flatMships = GrouperDAOFactory.getFactory().getFlatMembership().findByMemberId(memberGroup.getUuid());
      Iterator<FlatMembership> flatMshipsIter = flatMships.iterator();
      while (flatMshipsIter.hasNext()) {
        FlatMembership currFlatMship = flatMshipsIter.next();
        String currOwnerId = currFlatMship.getOwnerId();
        String currFieldId = currFlatMship.getFieldId();
        Field currField = FieldFinder.findById(currFieldId, true);
        String currFieldName = currField.getName();
        
        if (currFlatMship.getOwnerAttrDefId() != null) {
          AttributeDef currAttrDef = GrouperDAOFactory.getFactory().getAttributeDef().findById(currOwnerId, false);
          if (currAttrDef == null) {
            continue;
          }
          
          processPrivilegeDelete(Membership.OWNER_TYPE_ATTRIBUTE_DEF, currAttrDef.getName(), currOwnerId, currFieldId, currFieldName);
        } else if (currFlatMship.getOwnerStemId() != null) {
          Stem currStem = GrouperDAOFactory.getFactory().getStem().findByUuid(currOwnerId, false);
          if (currStem == null) {
            continue;
          }
          
          processPrivilegeDelete(Membership.OWNER_TYPE_STEM, currStem.getName(), currOwnerId, currFieldId, currFieldName);
        } else if (currFlatMship.getOwnerGroupId() != null) {
          Group currGroup = GrouperDAOFactory.getFactory().getGroup().findByUuid(currOwnerId, false);
          if (currGroup == null) {
            continue;
          }
                    
          if (FieldType.ACCESS.equals(currField.getType())) {
            processPrivilegeDelete(Membership.OWNER_TYPE_GROUP, currGroup.getName(), currOwnerId, currFieldId, currFieldName);
          } else {
            processMembershipDelete(currOwnerId, currGroup.getName(), currFieldId, currFieldName);
          }
          
        } else {
          throw new RuntimeException("owner id not set");
        }
      }
    }
  }    
  
  
  private static void processMembershipDelete(String groupId, String groupName, String fieldId, String fieldName) {
    // get the members to remove from the flat table
    Set<FlatMembership> flatMembershipsToRemove = GrouperDAOFactory.getFactory().getFlatMembership().findMembersToDeleteByGroupOwnerAndField(groupId, fieldId);
    
    // now iterate through the set and remove the flat memberships
    Iterator<FlatMembership> flatMembershipsToRemoveIter = flatMembershipsToRemove.iterator();
    while (flatMembershipsToRemoveIter.hasNext()) {
      FlatMembership flatMembership = flatMembershipsToRemoveIter.next();
      
      // now remove the flat membership
      flatMembership.delete();
      
      // now add the change log entry
      new ChangeLogEntry(false, ChangeLogTypeBuiltin.MEMBERSHIP_DELETE, 
          ChangeLogLabels.MEMBERSHIP_DELETE.id.name(), null,
          ChangeLogLabels.MEMBERSHIP_DELETE.fieldName.name(), fieldName, 
          ChangeLogLabels.MEMBERSHIP_DELETE.fieldId.name(), fieldId, 
          ChangeLogLabels.MEMBERSHIP_DELETE.memberId.name(), flatMembership.getMemberId(),
          ChangeLogLabels.MEMBERSHIP_DELETE.subjectId.name(), flatMembership.getMember().getSubjectId(),
          ChangeLogLabels.MEMBERSHIP_DELETE.sourceId.name(), flatMembership.getMember().getSubjectSourceId(),
          ChangeLogLabels.MEMBERSHIP_DELETE.membershipType.name(), null,
          ChangeLogLabels.MEMBERSHIP_DELETE.groupId.name(), groupId,
          ChangeLogLabels.MEMBERSHIP_DELETE.groupName.name(), groupName).save();
    }
  }
}
