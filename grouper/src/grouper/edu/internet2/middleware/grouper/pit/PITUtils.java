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
   * @param printOutput
   */
  public static void deleteInactiveRecords(Date date, final boolean printOutput) {
    
    final Timestamp time = new Timestamp(date.getTime());

    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            
            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

            GrouperDAOFactory.getFactory().getPITAttributeAssignValue().deleteInactiveRecords(time);
            if (printOutput) {
              System.out.println("Done deleting attributeAssignValues from point in time that ended before: " + time.toString());
            }
            
            GrouperDAOFactory.getFactory().getPITRoleSet().deleteInactiveRecords(time);
            if (printOutput) {
              System.out.println("Done deleting roleSets from point in time that ended before: " + time.toString());
            }
            
            GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().deleteInactiveRecords(time);
            if (printOutput) {
              System.out.println("Done deleting attributeAssignActionSets from point in time that ended before: " + time.toString());
            }
            
            GrouperDAOFactory.getFactory().getPITAttributeAssign().deleteInactiveRecords(time);
            if (printOutput) {
              System.out.println("Done deleting attributeAssigns from point in time that ended before: " + time.toString());
            }
            
            GrouperDAOFactory.getFactory().getPITAttributeAssignAction().deleteInactiveRecords(time);
            if (printOutput) {
              System.out.println("Done deleting attributeAssignActions from point in time that ended before: " + time.toString());
            }
            
            GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().deleteInactiveRecords(time);
            if (printOutput) {
              System.out.println("Done deleting attributeDefNameSets from point in time that ended before: " + time.toString());
            }
            
            GrouperDAOFactory.getFactory().getPITAttributeDefName().deleteInactiveRecords(time);
            if (printOutput) {
              System.out.println("Done deleting attributeDefNames from point in time that ended before: " + time.toString());
            }
            
            GrouperDAOFactory.getFactory().getPITMembership().deleteInactiveRecords(time);
            if (printOutput) {
              System.out.println("Done deleting memberships from point in time that ended before: " + time.toString());
            }
            
            GrouperDAOFactory.getFactory().getPITGroupSet().deleteInactiveRecords(time);
            if (printOutput) {
              System.out.println("Done deleting groupSets from point in time that ended before: " + time.toString());
            }
            
            GrouperDAOFactory.getFactory().getPITGroup().deleteInactiveRecords(time);
            if (printOutput) {
              System.out.println("Done deleting groups from point in time that ended before: " + time.toString());
            }
            
            GrouperDAOFactory.getFactory().getPITAttributeDef().deleteInactiveRecords(time);
            if (printOutput) {
              System.out.println("Done deleting attributeDefs from point in time that ended before: " + time.toString());
            }
            
            GrouperDAOFactory.getFactory().getPITStem().deleteInactiveRecords(time);
            if (printOutput) {
              System.out.println("Done deleting stems from point in time that ended before: " + time.toString());
            }
            
            GrouperDAOFactory.getFactory().getPITField().deleteInactiveRecords(time);
            if (printOutput) {
              System.out.println("Done deleting fields from point in time that ended before: " + time.toString());
            }
            
            GrouperDAOFactory.getFactory().getPITMember().deleteInactiveRecords(time);
            if (printOutput) {
              System.out.println("Done deleting members from point in time that ended before: " + time.toString());
            }
            
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
   * @param printOutput 
   */
  public static void deleteInactiveStem(final String stemName, final boolean printOutput) {
    
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            
            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

            Set<PITStem> stems = GrouperDAOFactory.getFactory().getPITStem().findByName(stemName, false);
            for (PITStem stem : stems) {
              if (!stem.isActive()) {
                deleteInactiveStem(stem, printOutput);
              } else {
                if (printOutput) {
                  System.out.println("Skipping " + stem.getName() + " (ID=" + stem.getId() + ") since it is active in point in time.");
                }
              }
            }

            return null;
          }
        });
  }
  
  /**
   * Delete point in time stem.
   * @param pitStem
   * @param printOutput 
   */
  public static void deleteInactiveStem(final PITStem pitStem, final boolean printOutput) {
    
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            
            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

            pitStem.delete(printOutput);

            return null;
          }
        });
  }
  
  /**
   * Delete point in time objects in a stem by stem name.  This includes child stems, groups, attribute def names,
   * and attribute defs.  If multiple stems exist by this stem name, inactive objects in all of them will be deleted.
   * @param stemName
   * @param printOutput 
   */
  public static void deleteInactiveObjectsInStem(final String stemName, final boolean printOutput) {
    
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            
            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

            Set<PITStem> stems = GrouperDAOFactory.getFactory().getPITStem().findByName(stemName, false);
            for (PITStem stem : stems) {
              deleteInactiveObjectsInStem(stem, printOutput);
            }

            return null;
          }
        });
  }
  
  /**
   * Delete point in time objects in a stem.  This includes child stems, groups, attribute def names,
   * and attribute defs.
   * @param pitStem
   * @param printOutput 
   */
  public static void deleteInactiveObjectsInStem(final PITStem pitStem, final boolean printOutput) {
    
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
                if (printOutput) {
                  System.out.println("Done deleting group from point in time: " + group.getName() + ", ID=" + group.getId());
                }
              }
            }
            
            // delete attribute def names
            Set<PITAttributeDefName> attrs = GrouperDAOFactory.getFactory().getPITAttributeDefName().findByStemId(pitStem.getId());
            for (PITAttributeDefName attr : attrs) {
              if (!attr.isActive()) {
                GrouperDAOFactory.getFactory().getPITAttributeDefName().delete(attr);
                if (printOutput) {
                  System.out.println("Done deleting attributeDefName from point in time: " + attr.getName() + ", ID=" + attr.getId());
                }
              }
            }
            
            // delete attribute defs
            Set<PITAttributeDef> defs = GrouperDAOFactory.getFactory().getPITAttributeDef().findByStemId(pitStem.getId());
            for (PITAttributeDef def : defs) {
              if (!def.isActive()) {
                GrouperDAOFactory.getFactory().getPITAttributeDef().delete(def);
                if (printOutput) {
                  System.out.println("Done deleting attributeDef from point in time: " + def.getName() + ", ID=" + def.getId());
                }
              }
            }
            
            // delete child stems
            Set<PITStem> childStems = GrouperDAOFactory.getFactory().getPITStem().findByParentStemId(pitStem.getId());
            for (PITStem childStem : childStems) {
              if (!childStem.isActive()) {
                childStem.delete(printOutput);
              }
            }

            return null;
          }
        });
  }
}
