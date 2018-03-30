/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouper.pit;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
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
   * @return the number of records
   */
  public static long deleteInactiveRecords(final Date date, final boolean printOutput) {
    
    boolean useTransaction = GrouperConfig.retrieveConfig().propertyValueBoolean("grouperDeleteInactivePitRecordsUseTransaction", false);
    
    if (useTransaction) {

      return (Long)HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
        
        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {
          return deleteInactiveRecordsHelper(date, printOutput);
        }
      });
    
      
    }
      
    return deleteInactiveRecordsHelper(date, printOutput);
  }

  /**
   * Delete point in time records that ended before the given date.  Not in a transaction
   * @param date
   * @param printOutput
   * @return the number of records
   */
  private static long deleteInactiveRecordsHelper(Date date, final boolean printOutput) {
    
    HibUtils.assignDisallowCacheThreadLocal();
    try {
      final Timestamp time = new Timestamp(date.getTime());
      long records = 0;
      
      long tempRecords = GrouperDAOFactory.getFactory().getPITAttributeAssignValue().deleteInactiveRecords(time);
      records += tempRecords;
      if (printOutput) {
        System.out.println("Done deleting " + tempRecords + " attributeAssignValues from point in time that ended before: " + time.toString());
      }
      
      tempRecords = GrouperDAOFactory.getFactory().getPITRoleSet().deleteInactiveRecords(time);
      records += tempRecords;
      if (printOutput) {
        System.out.println("Done deleting " + tempRecords + " roleSets from point in time that ended before: " + time.toString());
      }
      
      tempRecords = GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().deleteInactiveRecords(time);
      records += tempRecords;
      if (printOutput) {
        System.out.println("Done deleting " + tempRecords + " attributeAssignActionSets from point in time that ended before: " + time.toString());
      }
      
      tempRecords = GrouperDAOFactory.getFactory().getPITAttributeAssign().deleteInactiveRecords(time);
      records += tempRecords;
      if (printOutput) {
        System.out.println("Done deleting " + tempRecords + " attributeAssigns from point in time that ended before: " + time.toString());
      }
      
      tempRecords = GrouperDAOFactory.getFactory().getPITAttributeAssignAction().deleteInactiveRecords(time);
      records += tempRecords;
      if (printOutput) {
        System.out.println("Done deleting " + tempRecords + " attributeAssignActions from point in time that ended before: " + time.toString());
      }
      
      tempRecords = GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().deleteInactiveRecords(time);
      records += tempRecords;
      if (printOutput) {
        System.out.println("Done deleting " + tempRecords + " attributeDefNameSets from point in time that ended before: " + time.toString());
      }
      
      tempRecords = GrouperDAOFactory.getFactory().getPITAttributeDefName().deleteInactiveRecords(time);
      records += tempRecords;
      if (printOutput) {
        System.out.println("Done deleting " + tempRecords + " attributeDefNames from point in time that ended before: " + time.toString());
      }
      
      tempRecords = GrouperDAOFactory.getFactory().getPITMembership().deleteInactiveRecords(time);
      records += tempRecords;
      if (printOutput) {
        System.out.println("Done deleting " + tempRecords + " memberships from point in time that ended before: " + time.toString());
      }
      
      tempRecords = GrouperDAOFactory.getFactory().getPITGroupSet().deleteInactiveRecords(time);
      records += tempRecords;
      if (printOutput) {
        System.out.println("Done deleting " + tempRecords + " groupSets from point in time that ended before: " + time.toString());
      }
      
      tempRecords = GrouperDAOFactory.getFactory().getPITGroup().deleteInactiveRecords(time);
      records += tempRecords;
      if (printOutput) {
        System.out.println("Done deleting " + tempRecords + " groups from point in time that ended before: " + time.toString());
      }
      
      tempRecords = GrouperDAOFactory.getFactory().getPITAttributeDef().deleteInactiveRecords(time);
      records += tempRecords;
      if (printOutput) {
        System.out.println("Done deleting " + tempRecords + " attributeDefs from point in time that ended before: " + time.toString());
      }
      
      tempRecords = GrouperDAOFactory.getFactory().getPITStem().deleteInactiveRecords(time);
      records += tempRecords;
      if (printOutput) {
        System.out.println("Done deleting " + tempRecords + " stems from point in time that ended before: " + time.toString());
      }
      
      tempRecords = GrouperDAOFactory.getFactory().getPITField().deleteInactiveRecords(time);
      records += tempRecords;
      if (printOutput) {
        System.out.println("Done deleting " + tempRecords + " fields from point in time that ended before: " + time.toString());
      }
      
      tempRecords = GrouperDAOFactory.getFactory().getPITMember().deleteInactiveRecords(time);
      records += tempRecords;
      if (printOutput) {
        System.out.println("Done deleting " + tempRecords + " members from point in time that ended before: " + time.toString());
      }
      
      if (printOutput) {
        System.out.println("Done deleting total " + records + " records from point in time that ended before: " + time.toString());
      }
      return records;
    } finally {
      HibUtils.clearDisallowCacheThreadLocal();
    }
  }
  
  /**
   * Delete point in time group by group name.  If multiple inactive groups exist by this group name,
   * they will all be deleted.
   * @param groupName
   */
  public static void deleteInactiveGroup(final String groupName) {
    boolean useTransaction = GrouperConfig.retrieveConfig().propertyValueBoolean("grouperDeleteInactivePitRecordsUseTransaction", false);
    
    if (useTransaction) {

      HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
        
        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {
          deleteInactiveGroupHelper(groupName);
          return null;
        }
      });
    
      return;
    }
      
    deleteInactiveGroupHelper(groupName);

  }
  
  /**
   * Delete point in time group by group name.  If multiple inactive groups exist by this group name,
   * they will all be deleted.
   * @param groupName
   */
  private static void deleteInactiveGroupHelper(final String groupName) {
    HibUtils.assignDisallowCacheThreadLocal();
    try {

      Set<PITGroup> groups = GrouperDAOFactory.getFactory().getPITGroup().findByName(groupName, false);
      for (PITGroup group : groups) {
        if (!group.isActive()) {
          deleteInactiveGroup(group);
        }
      }
    } finally {
      HibUtils.clearDisallowCacheThreadLocal();
    }

  }
  
  
  /**
   * Delete point in time group.
   * @param pitGroup
   */
  public static void deleteInactiveGroup(final PITGroup pitGroup) {
    boolean useTransaction = GrouperConfig.retrieveConfig().propertyValueBoolean("grouperDeleteInactivePitRecordsUseTransaction", false);
    
    if (useTransaction) {

      HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
        
        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {
          
          deleteInactiveGroupHelper(pitGroup);
          return null;
        }
      });
    
      return;
    }
      
    deleteInactiveGroupHelper(pitGroup);

  }
  /**
   * Delete point in time group.
   * @param pitGroup
   */
  private static void deleteInactiveGroupHelper(final PITGroup pitGroup) {
    
    HibUtils.assignDisallowCacheThreadLocal();
    try {
      pitGroup.delete();
    } finally {
      HibUtils.clearDisallowCacheThreadLocal();
    }

  }
  
  /**
   * Delete point in time stem by stem name.  If multiple inactive stems exist by this stem name,
   * they will all be deleted.
   * @param stemName
   * @param printOutput 
   */
  public static void deleteInactiveStem(final String stemName, final boolean printOutput) {
    boolean useTransaction = GrouperConfig.retrieveConfig().propertyValueBoolean("grouperDeleteInactivePitRecordsUseTransaction", false);
    
    if (useTransaction) {

      HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
        
        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {
          deleteInactiveStemHelper(stemName, printOutput);
          return null;
        }
      });
    
      return;
    }
      
    deleteInactiveStemHelper(stemName, printOutput);

  }
    
  /**
   * Delete point in time stem by stem name.  If multiple inactive stems exist by this stem name,
   * they will all be deleted.
   * @param stemName
   * @param printOutput 
   */
  private static void deleteInactiveStemHelper(final String stemName, final boolean printOutput) {

    HibUtils.assignDisallowCacheThreadLocal();
    try {

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
    } finally {
      HibUtils.clearDisallowCacheThreadLocal();
    }

  }

  /**
   * Delete point in time stem.
   * @param pitStem
   * @param printOutput 
   */
  public static void deleteInactiveStem(final PITStem pitStem, final boolean printOutput) {
    boolean useTransaction = GrouperConfig.retrieveConfig().propertyValueBoolean("grouperDeleteInactivePitRecordsUseTransaction", false);
    
    if (useTransaction) {

      HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
        
        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {
          deleteInactiveStemHelper(pitStem, printOutput);
          return null;
        }
      });
    
      return;
    }
      
    deleteInactiveStemHelper(pitStem, printOutput);

  }
  /**
   * Delete point in time stem.
   * @param pitStem
   * @param printOutput 
   */
  private static void deleteInactiveStemHelper(final PITStem pitStem, final boolean printOutput) {
    HibUtils.assignDisallowCacheThreadLocal();
    try {
      pitStem.delete(printOutput);
    } finally {
      HibUtils.clearDisallowCacheThreadLocal();
    }


  }

  /**
   * Delete point in time objects in a stem by stem name.  This includes child stems, groups, attribute def names,
   * and attribute defs.  If multiple stems exist by this stem name, inactive objects in all of them will be deleted.
   * @param stemName
   * @param printOutput 
   */
  public static void deleteInactiveObjectsInStem(final String stemName, final boolean printOutput) {
    
    boolean useTransaction = GrouperConfig.retrieveConfig().propertyValueBoolean("grouperDeleteInactivePitRecordsUseTransaction", false);
    
    if (useTransaction) {

      HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
        
        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {
          deleteInactiveObjectsInStemHelper(stemName, printOutput);
          return null;
        }
      });
    
      return;
    }
      
    deleteInactiveObjectsInStemHelper(stemName, printOutput);
    
  }

  /**
   * Delete point in time objects in a stem by stem name.  This includes child stems, groups, attribute def names,
   * and attribute defs.  If multiple stems exist by this stem name, inactive objects in all of them will be deleted.
   * @param stemName
   * @param printOutput 
   */
  private static void deleteInactiveObjectsInStemHelper(final String stemName, final boolean printOutput) {
    
    HibUtils.assignDisallowCacheThreadLocal();
    try {
      Set<PITStem> stems = GrouperDAOFactory.getFactory().getPITStem().findByName(stemName, false);
      for (PITStem stem : stems) {
        deleteInactiveObjectsInStem(stem, printOutput);
      }
    } finally {
      HibUtils.clearDisallowCacheThreadLocal();
    }

  }
  
  /**
   * Delete point in time objects in a stem.  This includes child stems, groups, attribute def names,
   * and attribute defs.
   * @param pitStem
   * @param printOutput 
   */
  public static void deleteInactiveObjectsInStem(final PITStem pitStem, final boolean printOutput) {
    
    boolean useTransaction = GrouperConfig.retrieveConfig().propertyValueBoolean("grouperDeleteInactivePitRecordsUseTransaction", false);
    
    if (useTransaction) {

      HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
        
        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {
          deleteInactiveObjectsInStemHelper(pitStem, printOutput);
          return null;
        }
      });
    
      return;
    }
      
    deleteInactiveObjectsInStemHelper(pitStem, printOutput);

  }
  
  /**
   * Delete point in time objects in a stem.  This includes child stems, groups, attribute def names,
   * and attribute defs.
   * @param pitStem
   * @param printOutput 
   */
  private static void deleteInactiveObjectsInStemHelper(final PITStem pitStem, final boolean printOutput) {
    
    HibUtils.assignDisallowCacheThreadLocal();
    try {
      // delete groups
      Set<PITGroup> groups = GrouperDAOFactory.getFactory().getPITGroup().findByPITStemId(pitStem.getId());
      for (PITGroup group : groups) {
        if (!group.isActive()) {
          GrouperDAOFactory.getFactory().getPITGroup().delete(group);
          if (printOutput) {
            System.out.println("Done deleting group from point in time: " + group.getName() + ", ID=" + group.getId());
          }
        }
      }
      
      // delete attribute def names
      Set<PITAttributeDefName> attrs = GrouperDAOFactory.getFactory().getPITAttributeDefName().findByPITStemId(pitStem.getId());
      for (PITAttributeDefName attr : attrs) {
        if (!attr.isActive()) {
          GrouperDAOFactory.getFactory().getPITAttributeDefName().delete(attr);
          if (printOutput) {
            System.out.println("Done deleting attributeDefName from point in time: " + attr.getName() + ", ID=" + attr.getId());
          }
        }
      }
      
      // delete attribute defs
      Set<PITAttributeDef> defs = GrouperDAOFactory.getFactory().getPITAttributeDef().findByPITStemId(pitStem.getId());
      for (PITAttributeDef def : defs) {
        if (!def.isActive()) {
          GrouperDAOFactory.getFactory().getPITAttributeDef().delete(def);
          if (printOutput) {
            System.out.println("Done deleting attributeDef from point in time: " + def.getName() + ", ID=" + def.getId());
          }
        }
      }
      
      // delete child stems
      Set<PITStem> childStems = GrouperDAOFactory.getFactory().getPITStem().findByParentPITStemId(pitStem.getId());
      for (PITStem childStem : childStems) {
        if (!childStem.isActive()) {
          childStem.delete(printOutput);
        }
      }
    } finally {
      HibUtils.clearDisallowCacheThreadLocal();
    }
  }
}
