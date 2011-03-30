package edu.internet2.middleware.grouper.pit;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;

/**
 * @author shilen
 * $Id$
 */
public class PITUtils {

  /**
   * Delete point in time records that ended before the given date.
   * @param date
   */
  public static void deleteInactiveRecords(Date date) {
    
    final Timestamp time = new Timestamp(date.getTime());

    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            
            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

            GrouperDAOFactory.getFactory().getPITAttributeAssignValue().deleteInactiveRecords(time);
            GrouperDAOFactory.getFactory().getPITRoleSet().deleteInactiveRecords(time);
            GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().deleteInactiveRecords(time);
            GrouperDAOFactory.getFactory().getPITAttributeAssign().deleteInactiveRecords(time);
            GrouperDAOFactory.getFactory().getPITAttributeAssignAction().deleteInactiveRecords(time);
            GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().deleteInactiveRecords(time);
            GrouperDAOFactory.getFactory().getPITAttributeDefName().deleteInactiveRecords(time);
            GrouperDAOFactory.getFactory().getPITMembership().deleteInactiveRecords(time);
            GrouperDAOFactory.getFactory().getPITGroupSet().deleteInactiveRecords(time);
            GrouperDAOFactory.getFactory().getPITGroup().deleteInactiveRecords(time);
            GrouperDAOFactory.getFactory().getPITStem().deleteInactiveRecords(time);
            GrouperDAOFactory.getFactory().getPITAttributeDef().deleteInactiveRecords(time);
            GrouperDAOFactory.getFactory().getPITField().deleteInactiveRecords(time);
            GrouperDAOFactory.getFactory().getPITMember().deleteInactiveRecords(time);

            return null;
          }
        });
  }
  
  /**
   * Delete point in time group by group name.  If multiple inactive groups exist by this group name,
   * they will all be deleted.
   * @param groupName
   */
  public static void deleteInactiveGroup(final String groupName) {
    
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            
            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

            Set<PITGroup> groups = GrouperDAOFactory.getFactory().getPITGroup().findByName(groupName, false);
            for (PITGroup group : groups) {
              if (!group.isActive()) {
                deleteInactiveGroup(group);
              }
            }

            return null;
          }
        });
  }
  
  /**
   * Delete point in time group.
   * @param pitGroup
   */
  public static void deleteInactiveGroup(final PITGroup pitGroup) {
    
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            
            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

            pitGroup.delete();

            return null;
          }
        });
  }
  
  /**
   * Delete point in time stem by stem name.  If multiple inactive stems exist by this stem name,
   * they will all be deleted.
   * @param stemName
   */
  public static void deleteInactiveStem(final String stemName) {
    
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            
            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

            Set<PITStem> stems = GrouperDAOFactory.getFactory().getPITStem().findByName(stemName, false);
            for (PITStem stem : stems) {
              if (!stem.isActive()) {
                deleteInactiveStem(stem);
              }
            }

            return null;
          }
        });
  }
  
  /**
   * Delete point in time stem.
   * @param pitStem
   */
  public static void deleteInactiveStem(final PITStem pitStem) {
    
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            
            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

            pitStem.delete();

            return null;
          }
        });
  }
  
  /**
   * Delete point in time objects in a stem by stem name.  This includes child stems, groups, attribute def names,
   * and attribute defs.  If multiple stems exist by this stem name, inactive objects in all of them will be deleted.
   * @param stemName
   */
  public static void deleteInactiveObjectsInStem(final String stemName) {
    
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            
            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

            Set<PITStem> stems = GrouperDAOFactory.getFactory().getPITStem().findByName(stemName, false);
            for (PITStem stem : stems) {
              deleteInactiveObjectsInStem(stem);
            }

            return null;
          }
        });
  }
  
  /**
   * Delete point in time objects in a stem.  This includes child stems, groups, attribute def names,
   * and attribute defs.
   * @param pitStem
   */
  public static void deleteInactiveObjectsInStem(final PITStem pitStem) {
    
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            
            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);
            
            // delete groups
            Set<PITGroup> groups = GrouperDAOFactory.getFactory().getPITGroup().findByStemId(pitStem.getId());
            for (PITGroup group : groups) {
              if (!group.isActive()) {
                GrouperDAOFactory.getFactory().getPITGroup().delete(group);
              }
            }
            
            // delete attribute def names
            Set<PITAttributeDefName> attrs = GrouperDAOFactory.getFactory().getPITAttributeDefName().findByStemId(pitStem.getId());
            for (PITAttributeDefName attr : attrs) {
              if (!attr.isActive()) {
                GrouperDAOFactory.getFactory().getPITAttributeDefName().delete(attr);
              }
            }
            
            // delete attribute defs
            Set<PITAttributeDef> defs = GrouperDAOFactory.getFactory().getPITAttributeDef().findByStemId(pitStem.getId());
            for (PITAttributeDef def : defs) {
              if (!def.isActive()) {
                GrouperDAOFactory.getFactory().getPITAttributeDef().delete(def);
              }
            }
            
            // delete child stems
            Set<PITStem> childStems = GrouperDAOFactory.getFactory().getPITStem().findByParentStemId(pitStem.getId());
            for (PITStem childStem : childStems) {
              if (!childStem.isActive()) {
                GrouperDAOFactory.getFactory().getPITStem().delete(childStem);
              }
            }

            return null;
          }
        });
  }
}
