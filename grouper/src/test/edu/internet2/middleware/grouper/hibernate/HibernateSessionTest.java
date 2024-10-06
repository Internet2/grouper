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
/*
 * @author mchyzer
 * $Id: HibernateSessionTest.java,v 1.11 2009-11-17 02:52:29 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hibernate;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.hibernate.internal.SessionImpl;
import org.hibernate.type.StringType;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperDaemonEnabledDisabledCheck;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.cache.GrouperCacheUtils;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 *
 */
public class HibernateSessionTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new HibernateSessionTest("testRollbackGcDbAccess"));
    //TestRunner.run(HibernateSessionTest.class);
  }
  

  /**
   * @param name
   */
  public HibernateSessionTest(String name) {
    super(name);
  }

  @Override
  protected void setupConfigs() {
    super.setupConfigs();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stem.validateExtensionByDefault", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("group.validateExtensionByDefault", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDef.validateExtensionByDefault", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefName.validateExtensionByDefault", "false");
  }

  /**
   * 
   */
  public void testSelectFromSqlQuery() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup").save();
    
    List<Group> groups = HibernateSession.bySqlStatic().listSelect(Group.class, 
        "select * from grouper_groups where name = ?", GrouperUtil.toListObject("test:testGroup"), HibUtils.listType(StringType.INSTANCE));
    assertTrue(GrouperUtil.length(groups) > 0);
  }

  /**
   * 
   */
  public void testSelectFromHqlQuery() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup").save();
    
    List<Group> groups = HibernateSession.byHqlStatic()
        .createQuery("select theGroup.uuid as uuid, theGroup.hibernateVersionNumber as hibernateVersionNumber, theGroup.lastMembershipChangeDb as lastMembershipChangeDb, "
            + "theGroup.lastImmediateMembershipChangeDb as lastImmediateMembershipChangeDb, theGroup.parentUuid as parentUuid, theGroup.creatorUuid as creatorUuid, "
            + "theGroup.createTimeLong as createTimeLong, theGroup.modifierUuid as modifierUuid, theGroup.modifyTimeLong as modifyTimeLong, theGroup.nameDb as nameDb, theGroup.displayNameDb as displayNameDb, "
            + "theGroup.extensionDb as extensionDb, theGroup.displayExtensionDb as displayExtensionDb, theGroup.descriptionDb as descriptionDb, theGroup.contextId as contextId, "
            + "theGroup.alternateNameDb as alternateNameDb, theGroup.typeOfGroupDb as typeOfGroupDb, theGroup.idIndex as idIndex "
            + "from Group as theGroup where theGroup.nameDb = :theName")
        .setString("theName", "test:testGroup").assignConvertHqlColumnsToObject(true).list(Group.class);

    assertTrue(GrouperUtil.length(groups) > 0);
  }

  /**
   * 
   */
  public void testRollback() {
    
    
    final GrouperSession grouperSession = GrouperSession.startRootSession();
    
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
      
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        
        new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup").save();
        
        grouperTransaction.rollback(GrouperRollbackType.ROLLBACK_NOW);
        
        return null;
      }
    });
    
    Group group = GroupFinder.findByName(grouperSession, "test:testGroup", false);
    assertNull(group);
  }
  
  /**
   * 
   */
  public void testRollbackGcDbAccess() {

    int grouperLoaderLogRowsOrig = new GcDbAccess().sql("select count(1) from grouper_loader_log").select(int.class);
    
    final GrouperSession grouperSession = GrouperSession.startRootSession();

    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {

        new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup").save();
        
        Connection connection = ((SessionImpl)hibernateHandlerBean.getHibernateSession().getSession()).connection();
        
        insertIntoDbGcDbAccess(connection);
        
        hibernateHandlerBean.getHibernateSession().rollback(GrouperRollbackType.ROLLBACK_NOW);
                
        return null;
      }
    });

    Group group = GroupFinder.findByName(grouperSession, "test:testGroup", false);
    assertNull(group);
    
    int grouperLoaderLogRowsNew = new GcDbAccess().sql("select count(1) from grouper_loader_log").select(int.class);

    assertEquals(grouperLoaderLogRowsOrig, grouperLoaderLogRowsNew);
  }
  
  /**
   * make sure only savepoints are used in nested read/write transactions
   */
  public void testNestedTransactionsAndSavepoints() {

    if (GrouperDdlUtils.isSQLServer()) {
      
      //doesnt work with nested transactions and savepoints
      return;
      
    }
    
    final GrouperSession grouperSession = GrouperSession.startRootSession();

    int initialSavepointCount = HibernateSession.savePointCount;
    
    //on a readonly shouldnt have one
    GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READONLY_OR_USE_EXISTING, new GrouperTransactionHandler() {
      
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        GroupFinder.findByName(grouperSession, "a:b:c", false);
        return null;
      }
    });

    assertEquals(initialSavepointCount, HibernateSession.savePointCount);
    
    //######################

    final Group group = new GroupSave(grouperSession).assignName("a:b").assignCreateParentStemsIfNotExist(true).save();

    initialSavepointCount = HibernateSession.savePointCount;
    
    //on a readwrite with nested readonly, shouldnt have one
    GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, new GrouperTransactionHandler() {
      
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        group.addMember(SubjectTestHelper.SUBJ0);
        
        GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READONLY_NEW, new GrouperTransactionHandler() {
          
          public Object callback(GrouperTransaction grouperTransaction)
              throws GrouperDAOException {
            
            GroupFinder.findByName(grouperSession, "a:b:c", false);
            
            return null;
          }
        });
        
        return null;
      }
    });

    assertEquals(initialSavepointCount, HibernateSession.savePointCount);
    
    //######################
    
    initialSavepointCount = HibernateSession.savePointCount;
    
    //on a readonly with nested readonly, shouldnt have one
    GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READONLY_OR_USE_EXISTING, new GrouperTransactionHandler() {
      
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        GroupFinder.findByName(grouperSession, "a:b:c", false);
        
        GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READONLY_NEW, new GrouperTransactionHandler() {
          
          public Object callback(GrouperTransaction grouperTransaction)
              throws GrouperDAOException {
            
            GroupFinder.findByName(grouperSession, "a:b:c", false);
            
            return null;
          }
        });
        
        return null;
      }
    });

    assertEquals(initialSavepointCount, HibernateSession.savePointCount);
    
    //######################
    
    initialSavepointCount = HibernateSession.savePointCount;
    
    //on a readwrite with nested readwrite or use existing, should have none
    GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, new GrouperTransactionHandler() {
      
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        insertIntoDb();
        
        GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, new GrouperTransactionHandler() {
          
          public Object callback(GrouperTransaction grouperTransaction)
              throws GrouperDAOException {
            
            insertIntoDb();
            
            return null;
          }
        });
        
        return null;
      }
    });

    assertEquals(initialSavepointCount, HibernateSession.savePointCount);
    
    //######################
    
    initialSavepointCount = HibernateSession.savePointCount;
    
    //on a readwrite with nested readwrite, should have one
    GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, new GrouperTransactionHandler() {
      
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        insertIntoDb();
        
        GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READ_WRITE_NEW, new GrouperTransactionHandler() {
          
          public Object callback(GrouperTransaction grouperTransaction)
              throws GrouperDAOException {
            
            insertIntoDb();
            
            return null;
          }
        });
        
        return null;
      }
    });

    assertEquals(initialSavepointCount + 1, HibernateSession.savePointCount);
   
    //######################
    
    initialSavepointCount = HibernateSession.savePointCount;
    
    //on a readwrite with nested readwrite and nested readonly, should have one
    GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, new GrouperTransactionHandler() {
      
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        insertIntoDb();
        
        GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READ_WRITE_NEW, new GrouperTransactionHandler() {
          
          public Object callback(GrouperTransaction grouperTransaction)
              throws GrouperDAOException {
            
            insertIntoDb();
            
            GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READONLY_NEW, new GrouperTransactionHandler() {
              
              public Object callback(GrouperTransaction grouperTransaction)
                  throws GrouperDAOException {
                
                GroupFinder.findByName(grouperSession, "a:b:c", false);
                
                return null;
              }
            });
            return null;
          }
        });
        
        return null;
      }
    });

    assertEquals(initialSavepointCount + 1, HibernateSession.savePointCount);
    
    //######################
    
    initialSavepointCount = HibernateSession.savePointCount;
    
    //on a readwrite with nested readwrite and nested readwrite, should have two
    GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, new GrouperTransactionHandler() {
      
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        insertIntoDb();
        
        GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READ_WRITE_NEW, new GrouperTransactionHandler() {
          
          public Object callback(GrouperTransaction grouperTransaction)
              throws GrouperDAOException {
            
            insertIntoDb();
            
            GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READ_WRITE_NEW, new GrouperTransactionHandler() {
              
              public Object callback(GrouperTransaction grouperTransaction)
                  throws GrouperDAOException {
                
                insertIntoDb();
                
                return null;
              }
            });

            return null;
          }
        });
        
        return null;
      }
    });
    
    assertEquals(initialSavepointCount + 2, HibernateSession.savePointCount);
    
    GrouperSession.stopQuietly(grouperSession);
    
  }
  
  public static void insertIntoDb() {
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
    
    hib3GrouperLoaderLog.setHost(GrouperUtil.hostname());
    String jobName = "OTHER_JOB_attestationDaemon";

    hib3GrouperLoaderLog.setJobName(jobName);
    hib3GrouperLoaderLog.setJobType(GrouperLoaderType.OTHER_JOB.name());
    hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.SUCCESS.name());
    HibernateSession.byObjectStatic().saveOrUpdate(hib3GrouperLoaderLog);

  }
  
  public static void insertIntoDbGcDbAccess(Connection connection) {
    new GcDbAccess().connection(connection).sql("insert into grouper_loader_log (id, job_name, status, job_type, started_time) values (?, ?, ?, ?, ?)")
      .addBindVar(GrouperUuid.getUuid()).addBindVar("OTHER_JOB_attestationDaemon").addBindVar("SUCCESS").addBindVar("OTHER_JOB").addBindVar(new Timestamp(System.currentTimeMillis())).executeSql();

  }
  
  /**
   * make sure the caching flag propagates appropriately
   */
  public void testCachingPropagate() {
    
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, 
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
        
        HibernateSession hibernateSession1 = hibernateHandlerBean.getHibernateSession();
        
        assertTrue("Should default to true", hibernateSession1.isCachingEnabled());

        //do an inner callback, see that it is false, set to true
        HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
            AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
              
              public Object callback(HibernateHandlerBean hibernateHandlerBean)
                  throws GrouperDAOException {
                
                HibernateSession hibernateSession2 = hibernateHandlerBean.getHibernateSession();
                assertTrue(hibernateSession2.isCachingEnabled());
                hibernateSession2.setCachingEnabled(false);
                
                //see that it propagates to inner
                HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_NEW,
                    AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
                      
                      public Object callback(HibernateHandlerBean hibernateHandlerBean)
                          throws GrouperDAOException {
                        HibernateSession hibernateSession3 = hibernateHandlerBean.getHibernateSession();
                        
                        assertFalse("should propagate from parent", hibernateSession3.isCachingEnabled());

                        //set to false should not affect parent
                        hibernateSession3.setCachingEnabled(true);

                        return null;
                      }
                    });
                
                assertFalse("child setting should not affect this", hibernateSession2.isCachingEnabled());
                return null;
              }
            });
        
        assertTrue("make sure the inner doesnt affect this", hibernateSession1.isCachingEnabled());
        return null;
      }
    });
    
  }
  
  /**
   * make sure the caching flag propagates appropriately
   */
  public void testCachingPropagateGrouperTransaction() {
    
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
      
      public Object callback(GrouperTransaction grouperTransaction1)
          throws GrouperDAOException {
        
        assertTrue("Should default to true", grouperTransaction1.isCachingEnabled());
        
        GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
          
          public Object callback(GrouperTransaction grouperTransaction2)
              throws GrouperDAOException {
            assertTrue("Should default to true", grouperTransaction2.isCachingEnabled());
            grouperTransaction2.setCachingEnabled(false);

            GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
              
              public Object callback(GrouperTransaction grouperTransaction3)
                  throws GrouperDAOException {
                
                assertFalse("Should inherit from parent", grouperTransaction3.isCachingEnabled());
                
                //make sure this doesnt make it back up the chain
                grouperTransaction3.setCachingEnabled(true);
                
                return null;
              }
            });
            
            assertFalse("Should not inherit from child", grouperTransaction2.isCachingEnabled());
            
            return null;
          }
        });
        
        assertTrue("Should default to true", grouperTransaction1.isCachingEnabled());
        
        return null;
      }
    });
    
  }
  
  /**
   * test paging/sorting
   * @throws Exception 
   */
  public void testResultSort() throws Exception {
    //lets add some members
    GrouperSession grouperSession = SessionHelper.getRootSession();
    Stem root = StemHelper.findRootStem(grouperSession);
    Stem edu = StemHelper.addChildStem(root, "edu", "education");
    Group i2 = StemHelper.addChildGroup(edu, "i2", "internet2");
    i2.addMember(SubjectTestHelper.SUBJ2);
    i2.addMember(SubjectTestHelper.SUBJ0);
    i2.addMember(SubjectTestHelper.SUBJ5);
    i2.addMember(SubjectTestHelper.SUBJ1);
    QueryOptions queryOptions = new QueryOptions().sortAsc("subject_id");
    Set<Member> members = i2.getMembers(Group.getDefaultList(), queryOptions);
    List<Member> memberList = new ArrayList<Member>(members);
    assertEquals(SubjectTestHelper.SUBJ0_ID, memberList.get(0).getSubjectId());
    assertEquals(SubjectTestHelper.SUBJ1_ID, memberList.get(1).getSubjectId());
    assertEquals(SubjectTestHelper.SUBJ2_ID, memberList.get(2).getSubjectId());
    assertEquals(SubjectTestHelper.SUBJ5_ID, memberList.get(3).getSubjectId());
  }
  
  /**
   * test enabled/disabled inline
   * @throws Exception 
   */
  public void testEnabledDisabledInline() throws Exception {
    //lets add some members
    GrouperSession grouperSession = SessionHelper.getRootSession();
    Stem root = StemHelper.findRootStem(grouperSession);
    Stem edu = StemHelper.addChildStem(root, "edu", "education");
    Group parent = StemHelper.addChildGroup(edu, "parent", "parent");
    Group child = StemHelper.addChildGroup(edu, "child", "child");
    
    //lets count the memberships (all view)
    int initialCountMembershipsAllV = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_memberships_all_v");

    int initialCountGroupSet = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_group_set");

    //##################################################
    parent.addMember(child.toSubject());
    child.addMember(SubjectTestHelper.SUBJ1);
    
    int currentGroupCountMembershipsAllV = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_memberships_all_v");

    int currentGroupCountGroupSet = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_group_set");
    
    assertEquals("3 memberships, 1 for subj1 in child, 1 for subj1 in parent, and 1 for child in parent", 
        initialCountMembershipsAllV + 3, currentGroupCountMembershipsAllV);
    assertEquals("1 more group set for child in parent", 
        initialCountGroupSet + 1, currentGroupCountGroupSet);
    
    //####################################################
    //disable
    Membership membership = parent.getImmediateMembership(Group.getDefaultList(), child.toSubject(), true, true);
    membership.setDisabledTime(new Timestamp(System.currentTimeMillis() - 10000));
    membership.update();
    
    currentGroupCountMembershipsAllV = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_memberships_all_v");

    currentGroupCountGroupSet = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_group_set");
    
    assertEquals("2 memberships, 1 for subj1 in child, 1 for disabled child in parent, none for subj1 in parent", 
        initialCountMembershipsAllV + 2, currentGroupCountMembershipsAllV);
    assertEquals("same as original group sets", 
        initialCountGroupSet, currentGroupCountGroupSet);
  }
  
  /**
   * test paging/sorting
   * @throws Exception 
   */
  public void testEnabledDisabled() throws Exception {
    //lets add some members
    GrouperSession grouperSession = SessionHelper.getRootSession();
    Stem root = StemHelper.findRootStem(grouperSession);
    Stem edu = StemHelper.addChildStem(root, "edu", "education");
    Group parent = StemHelper.addChildGroup(edu, "parent", "parent");
    Group child = StemHelper.addChildGroup(edu, "child", "child");
    
    //lets count the memberships (all view)
    int initialCountMembershipsAllV = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_memberships_all_v");

    int initialCountGroupSet = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_group_set");

    //##################################################
    parent.addMember(child.toSubject());
    child.addMember(SubjectTestHelper.SUBJ1);
    
    int currentGroupCountMembershipsAllV = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_memberships_all_v");

    int currentGroupCountGroupSet = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_group_set");
    
    assertEquals("3 memberships, 1 for subj1 in child, 1 for subj1 in parent, and 1 for child in parent", 
        initialCountMembershipsAllV + 3, currentGroupCountMembershipsAllV);
    assertEquals("1 more group set for child in parent", 
        initialCountGroupSet + 1, currentGroupCountGroupSet);
    
    //####################################################
    //disable
    Membership membership = parent.getImmediateMembership(Group.getDefaultList(), child.toSubject(), true, true);
    membership.setDisabledTime(new Timestamp(System.currentTimeMillis() - 10000));
    GrouperDAOFactory.getFactory().getMembership().update(membership);
    
    currentGroupCountMembershipsAllV = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_memberships_all_v");

    currentGroupCountGroupSet = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_group_set");
    
    assertEquals("2 memberships, 1 for subj1 in child, 1 for disabled child in parent, none for subj1 in parent", 
        initialCountMembershipsAllV + 2, currentGroupCountMembershipsAllV);
    assertEquals("same as original group sets", 
        initialCountGroupSet, currentGroupCountGroupSet);
    
    
    //####################################################
    //disable in future
    membership = parent.getImmediateMembership(Group.getDefaultList(), child.toSubject(), false, true);
    membership.setDisabledTime(new Timestamp(System.currentTimeMillis() + 10000));
    GrouperDAOFactory.getFactory().getMembership().update(membership);
    
    currentGroupCountMembershipsAllV = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_memberships_all_v");

    currentGroupCountGroupSet = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_group_set");
    
    assertEquals("3 memberships, 1 for subj1 in child, 1 for disabled child in parent, 1 for subj1 in parent", 
        initialCountMembershipsAllV + 3, currentGroupCountMembershipsAllV);
    assertEquals("1 more group set for parent in child", 
        initialCountGroupSet + 1, currentGroupCountGroupSet);
    
    //####################################################
    //not enabled
    membership.setDisabledTime(null);
    membership.setEnabledTime(new Timestamp(System.currentTimeMillis() + 10000));
    GrouperDAOFactory.getFactory().getMembership().update(membership);
    
    currentGroupCountMembershipsAllV = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_memberships_all_v");

    currentGroupCountGroupSet = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_group_set");
    
    assertEquals("2 memberships, 1 for subj1 in child, 1 for disabled child in parent, none for subj1 in parent", 
        initialCountMembershipsAllV + 2, currentGroupCountMembershipsAllV);
    assertEquals("same as original group sets", 
        initialCountGroupSet, currentGroupCountGroupSet);
    
    
    //####################################################
    //enable in past
    membership.setEnabledTime(new Timestamp(System.currentTimeMillis() - 10000));
    GrouperDAOFactory.getFactory().getMembership().update(membership);
    
    currentGroupCountMembershipsAllV = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_memberships_all_v");

    currentGroupCountGroupSet = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_group_set");
    
    assertEquals("3 memberships, 1 for subj1 in child, 1 for disabled child in parent, 1 for subj1 in parent", 
        initialCountMembershipsAllV + 3, currentGroupCountMembershipsAllV);
    assertEquals("1 more group set for parent in child", 
        initialCountGroupSet + 1, currentGroupCountGroupSet);
    
    
    //####################################################
    //delete the membership (should be 2 less)
    //ByObjectStatic.java.save() line 435, Hib3ChangeLogEntryDAO.java.save() line 29, 
    //ChangeLogEntry.java.save() line 286, Membership.java.addMembershipDeleteChangeLog() line 2621, 
    //Membership.java.addMembershipDeleteChangeLogs() line 2658, GroupSet.java.onPostDelete() line 413, 
    //Hib3GroupSetDAO.java.callback() line 76, Hib3GroupSetDAO.java.delete() line 57, 
    //Membership.java.processPostMembershipDelete() line 1853, Membership.java.onPostDelete() line 1720, 
    //Hib3MembershipDAO.java.delete() line 1255, Membership.java.internal_delImmediateMembership() line 1117, 
    //Group.java.callback() line 1631, Group.java.deleteMember() line 1608, Group.java.deleteMember() line 1525, 
    //Group.java.deleteMember() line 1489
    parent.deleteMember(child.toSubject());

    currentGroupCountMembershipsAllV = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_memberships_all_v");

    currentGroupCountGroupSet = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_group_set");
    
    assertEquals("1 memberships, 1 for subj1 in child", 
        initialCountMembershipsAllV + 1, currentGroupCountMembershipsAllV);
    assertEquals("same as original group sets", 
        initialCountGroupSet, currentGroupCountGroupSet);
    
  }

  /**
   * test paging/sorting
   * @throws Exception 
   */
  public void testEnabledDisabledDaemon() throws Exception {

    //lets add some members
    GrouperSession grouperSession = SessionHelper.getRootSession();
    Stem root = StemHelper.findRootStem(grouperSession);
    Stem edu = StemHelper.addChildStem(root, "edu", "education");
    Group parent = StemHelper.addChildGroup(edu, "parent", "parent");
    Group child = StemHelper.addChildGroup(edu, "child", "child");
    
    //lets count the memberships (all view)
    int initialCountMembershipsAllV = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_memberships_all_v");

    int initialCountGroupSet = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_group_set");

    //##################################################
    parent.addMember(child.toSubject());
    child.addMember(SubjectTestHelper.SUBJ1);
    
    int currentGroupCountMembershipsAllV = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_memberships_all_v");

    int currentGroupCountGroupSet = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_group_set");
    
    assertEquals("3 memberships, 1 for subj1 in child, 1 for subj1 in parent, and 1 for child in parent", 
        initialCountMembershipsAllV + 3, currentGroupCountMembershipsAllV);
    assertEquals("1 more group set for child in parent", 
        initialCountGroupSet + 1, currentGroupCountGroupSet);
    
    //####################################################
    //disable
    Membership membership = parent.getImmediateMembership(Group.getDefaultList(), child.toSubject(), true, true);
    
    //disabled 5 seconds in the future
    HibernateSession.byHqlStatic().createQuery("update ImmediateMembershipEntry " +
    		"set disabledTimeDb = :disabledTime where immediateMembershipId = :theId")
      .setLong("disabledTime", System.currentTimeMillis()+5000)
      .setString("theId", membership.getImmediateMembershipId()).executeUpdate();
    
    //run daemon
    int fixed = GrouperDaemonEnabledDisabledCheck.fixEnabledDisabled();
    assertEquals(0, fixed);
    fixed = GrouperDaemonEnabledDisabledCheck.fixEnabledDisabled();
    assertEquals(0, fixed);
    
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      // ignore
    }
    
    fixed = GrouperDaemonEnabledDisabledCheck.fixEnabledDisabled();
    assertEquals("Should have fixed one record, immediateMembershipId: " + membership.getImmediateMembershipId(), 1, fixed);
    
    currentGroupCountMembershipsAllV = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_memberships_all_v");

    currentGroupCountGroupSet = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_group_set");
    
    assertEquals("2 memberships, 1 for subj1 in child, 1 for disabled child in parent, none for subj1 in parent", 
        initialCountMembershipsAllV + 2, currentGroupCountMembershipsAllV);
    assertEquals("same as original group sets", 
        initialCountGroupSet, currentGroupCountGroupSet);

    assertFalse(parent.hasMember(child.toSubject()));
    assertFalse(parent.hasMember(SubjectTestHelper.SUBJ1));
    
    //###########################################
    //run with nothing to do
    fixed = GrouperDaemonEnabledDisabledCheck.fixEnabledDisabled();
    
    assertEquals("Should have fixed no records", 0, fixed);
    
    //###########################################
    //disabled in the future, should be enabled
    
    GrouperDaemonEnabledDisabledCheck.internal_clearCache();
    
    HibernateSession.byHqlStatic().createQuery("update ImmediateMembershipEntry " +
        "set disabledTimeDb = :disabledTime where immediateMembershipId = :theId")
      .setLong("disabledTime", System.currentTimeMillis()+7200000)
      .setString("theId", membership.getImmediateMembershipId()).executeUpdate();
    
    //run daemon
    fixed = GrouperDaemonEnabledDisabledCheck.fixEnabledDisabled();
    
    assertEquals("Should have fixed one record, immediateMembershipId: " + membership.getImmediateMembershipId(), 1, fixed);
    
    currentGroupCountMembershipsAllV = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_memberships_all_v");
    
    currentGroupCountGroupSet = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_group_set");
    
    assertEquals("3 memberships, 1 for subj1 in child, 1 for enabled child in parent, 1 for subj1 in parent", 
        initialCountMembershipsAllV + 3, currentGroupCountMembershipsAllV);
    assertEquals("1 more group set for child in parent", 
        initialCountGroupSet + 1, currentGroupCountGroupSet);
    
    assertTrue(parent.hasMember(child.toSubject()));
    assertTrue(parent.hasMember(SubjectTestHelper.SUBJ1));
    
    
    //###########################################
    //enabled in future and disabled in the future, should be disabled

    GrouperDaemonEnabledDisabledCheck.internal_clearCache();

    HibernateSession.byHqlStatic().createQuery("update ImmediateMembershipEntry " +
        "set enabledTimeDb = :enabledTime where immediateMembershipId = :theId")
      .setLong("enabledTime", System.currentTimeMillis()+7200000)
      .setString("theId", membership.getImmediateMembershipId()).executeUpdate();
    
    //run daemon
    fixed = GrouperDaemonEnabledDisabledCheck.fixEnabledDisabled();
    
    assertEquals("Should have fixed one record, immediateMembershipId: " + membership.getImmediateMembershipId(), 1, fixed);
    
    currentGroupCountMembershipsAllV = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_memberships_all_v");
    
    currentGroupCountGroupSet = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_group_set");
    
    assertEquals("2 memberships, 1 for subj1 in child, 1 for disabled child in parent", 
        initialCountMembershipsAllV + 2, currentGroupCountMembershipsAllV);
    assertEquals("same group sets as original", 
        initialCountGroupSet, currentGroupCountGroupSet);
    
    assertFalse(parent.hasMember(child.toSubject()));
    assertFalse(parent.hasMember(SubjectTestHelper.SUBJ1));
    
    //###########################################
    //enabled in past (after sleeping) and disabled in the future, should be enabled

    GrouperDaemonEnabledDisabledCheck.internal_clearCache();

    HibernateSession.byHqlStatic().createQuery("update ImmediateMembershipEntry " +
        "set enabledTimeDb = :enabledTime where immediateMembershipId = :theId")
      .setLong("enabledTime", System.currentTimeMillis()+5000)
      .setString("theId", membership.getImmediateMembershipId()).executeUpdate();
    
    //run daemon
    fixed = GrouperDaemonEnabledDisabledCheck.fixEnabledDisabled();
    assertEquals(0, fixed);
    fixed = GrouperDaemonEnabledDisabledCheck.fixEnabledDisabled();
    assertEquals(0, fixed);
    
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      // ignore
    }
    
    fixed = GrouperDaemonEnabledDisabledCheck.fixEnabledDisabled();

    assertEquals("Should have fixed one record, immediateMembershipId: " + membership.getImmediateMembershipId(), 1, fixed);
    
    currentGroupCountMembershipsAllV = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_memberships_all_v");
    
    currentGroupCountGroupSet = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_group_set");
    
    assertEquals("3 memberships, 1 for subj1 in child, 1 for disabled child in parent, 1 for subj1 in parent", 
        initialCountMembershipsAllV + 3, currentGroupCountMembershipsAllV);
    assertEquals("1 more group set for child in parent", 
        initialCountGroupSet + 1, currentGroupCountGroupSet);
    
    assertTrue(parent.hasMember(child.toSubject()));
    assertTrue(parent.hasMember(SubjectTestHelper.SUBJ1));
    
    //###########################################
    //enabled in future and disabled null, should be disabled
    
    GrouperDaemonEnabledDisabledCheck.internal_clearCache();

    HibernateSession.byHqlStatic().createQuery("update ImmediateMembershipEntry " +
        "set enabledTimeDb = :enabledTime, disabledTimeDb = null where immediateMembershipId = :theId")
      .setLong("enabledTime", System.currentTimeMillis()+7200000)
      .setString("theId", membership.getImmediateMembershipId()).executeUpdate();
    
    //run daemon
    fixed = GrouperDaemonEnabledDisabledCheck.fixEnabledDisabled();
    
    assertEquals("Should have fixed one record, immediateMembershipId: " + membership.getImmediateMembershipId(), 1, fixed);
    
    currentGroupCountMembershipsAllV = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_memberships_all_v");
    
    currentGroupCountGroupSet = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_group_set");
    
    assertEquals("2 memberships, 1 for subj1 in child, 1 for disabled child in parent", 
        initialCountMembershipsAllV + 2, currentGroupCountMembershipsAllV);
    assertEquals("same group sets as original", 
        initialCountGroupSet, currentGroupCountGroupSet);
    
    assertFalse(parent.hasMember(child.toSubject()));
    assertFalse(parent.hasMember(SubjectTestHelper.SUBJ1));
    
    
    //###########################################
    //enabled in past and disabled null, should be enabled
    
    GrouperDaemonEnabledDisabledCheck.internal_clearCache();

    HibernateSession.byHqlStatic().createQuery("update ImmediateMembershipEntry " +
        "set enabledTimeDb = :enabledTime, disabledTimeDb = null where immediateMembershipId = :theId")
      .setLong("enabledTime", System.currentTimeMillis()-10000)
      .setString("theId", membership.getImmediateMembershipId()).executeUpdate();
    
    //run daemon
    fixed = GrouperDaemonEnabledDisabledCheck.fixEnabledDisabled();
    
    assertEquals("Should have fixed one record, immediateMembershipId: " + membership.getImmediateMembershipId(), 1, fixed);
    
    currentGroupCountMembershipsAllV = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_memberships_all_v");
    
    currentGroupCountGroupSet = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_group_set");
    
    assertEquals("3 memberships, 1 for subj1 in child, 1 for disabled child in parent, 1 for subj1 in parent", 
        initialCountMembershipsAllV + 3, currentGroupCountMembershipsAllV);
    assertEquals("1 more group set for child leads to parent", 
        initialCountGroupSet + 1, currentGroupCountGroupSet);
    
    assertTrue(parent.hasMember(child.toSubject()));
    assertTrue(parent.hasMember(SubjectTestHelper.SUBJ1));
    
    
    //###########################################
    //enabled in future and disabled null, should be disabled - on a privilege
   
    parent.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.UPDATE, true);
    membership = parent.getImmediateMembership(AccessPrivilege.UPDATE.getField(), SubjectTestHelper.SUBJ0, true, true);
    assertTrue(parent.hasPrivilege(SubjectTestHelper.SUBJ0, "update"));

    GrouperDaemonEnabledDisabledCheck.internal_clearCache();

    HibernateSession.byHqlStatic().createQuery("update ImmediateMembershipEntry " +
        "set enabledTimeDb = :enabledTime, disabledTimeDb = null where immediateMembershipId = :theId")
      .setLong("enabledTime", System.currentTimeMillis()+7200000)
      .setString("theId", membership.getImmediateMembershipId()).executeUpdate();
    
    //run daemon
    fixed = GrouperDaemonEnabledDisabledCheck.fixEnabledDisabled();
    
    assertEquals("Should have fixed one record, immediateMembershipId: " + membership.getImmediateMembershipId(), 1, fixed);
    GrouperCacheUtils.clearAllCaches();

    assertFalse(parent.hasPrivilege(SubjectTestHelper.SUBJ0, "update"));
    
    //###########################################
    //enabled in future and disabled null, should be disabled - on a stem privilege
   
    edu.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ATTR_READ, true);
    membership = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), edu, SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ATTR_READ.getField(), true);
    assertTrue(edu.hasPrivilege(SubjectTestHelper.SUBJ0, "stemAttrRead"));

    GrouperDaemonEnabledDisabledCheck.internal_clearCache();

    HibernateSession.byHqlStatic().createQuery("update ImmediateMembershipEntry " +
        "set enabledTimeDb = :enabledTime, disabledTimeDb = null where immediateMembershipId = :theId")
      .setLong("enabledTime", System.currentTimeMillis()+7200000)
      .setString("theId", membership.getImmediateMembershipId()).executeUpdate();
    
    //run daemon
    fixed = GrouperDaemonEnabledDisabledCheck.fixEnabledDisabled();
    
    assertEquals("Should have fixed one record, immediateMembershipId: " + membership.getImmediateMembershipId(), 1, fixed);
    GrouperCacheUtils.clearAllCaches();

    assertFalse(edu.hasPrivilege(SubjectTestHelper.SUBJ0, "stemAttrRead"));
    
    //###########################################
    //enabled in future and disabled null, should be disabled - on a stem privilege on a group
   
    GrouperDaemonEnabledDisabledCheck.internal_clearCache();

    edu.grantPriv(child.toSubject(), NamingPrivilege.STEM_ATTR_READ, true);
    membership = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), edu, child.toSubject(), NamingPrivilege.STEM_ATTR_READ.getField(), true);
    assertTrue(edu.hasPrivilege(child.toSubject(), "stemAttrRead"));

    HibernateSession.byHqlStatic().createQuery("update ImmediateMembershipEntry " +
        "set enabledTimeDb = :enabledTime, disabledTimeDb = null where immediateMembershipId = :theId")
      .setLong("enabledTime", System.currentTimeMillis()+7200000)
      .setString("theId", membership.getImmediateMembershipId()).executeUpdate();
    
    //run daemon
    fixed = GrouperDaemonEnabledDisabledCheck.fixEnabledDisabled();
    
    assertEquals("Should have fixed one record, immediateMembershipId: " + membership.getImmediateMembershipId(), 1, fixed);
    GrouperCacheUtils.clearAllCaches();

    assertFalse(edu.hasPrivilege(child.toSubject(), "stemAttrRead"));
  }
  
  /**
   * @throws Exception 
   * 
   */
  public void testReadonly() throws Exception {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem root = StemHelper.findRootStem(grouperSession);
    Stem edu = StemHelper.addChildStem(root, "edu", "education");
    Group i2 = StemHelper.addChildGroup(edu, "i2", "internet2");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.api.readonly", "true");

    i2 = GroupFinder.findByName(grouperSession, "edu:i2", true);
    
    assertEquals("edu:i2", i2.getName());
    
    try {
      StemHelper.addChildGroup(edu, "i3", "internet2");
      fail("shouldnt get here");
    } catch (Exception e) {
      //good
    }
    
    GrouperSession.stopQuietly(grouperSession);
  }

  
  /**
   * test paging/sorting
   * @throws Exception 
   */
  public void testResultSize() throws Exception {
    
    //lets add some members
    GrouperSession grouperSession = SessionHelper.getRootSession();
    Stem root = StemHelper.findRootStem(grouperSession);
    Stem edu = StemHelper.addChildStem(root, "edu", "education");
    Group i2 = StemHelper.addChildGroup(edu, "i2", "internet2");
    Group i3 = StemHelper.addChildGroup(edu, "i3", "internet2");
    Group i4 = StemHelper.addChildGroup(edu, "i4", "internet2");
    Group i5 = StemHelper.addChildGroup(edu, "i5", "internet2");
    Group i6 = StemHelper.addChildGroup(edu, "i6", "internet2");
    Group i7 = StemHelper.addChildGroup(edu, "i7", "internet2");

    i2.addMember(SubjectTestHelper.SUBJ0);
    i2.addMember(SubjectTestHelper.SUBJ1);
    i2.addMember(SubjectTestHelper.SUBJ2);
    i2.addMember(SubjectTestHelper.SUBJ3);
    i2.addMember(SubjectFinder.findRootSubject());
    
    //page the members in a group
    QueryOptions queryOptions = new QueryOptions().retrieveCount(true).retrieveResults(false);
    List<Member> members = HibernateSession.byHqlStatic()
      .createQuery("select distinct theMember from Member as theMember, MembershipEntry as theMembership, Field theField "
      + "where theMembership.ownerGroupId = :ownerId and theMember.uuid = theMembership.memberUuid" +
          " and theMembership.fieldId = theField.uuid and theField.typeString = 'list' and theField.name = 'members' and theMembership.enabledDb = 'T'")
          .setString("ownerId", i2.getUuid())
      .options(queryOptions).list(Member.class);
    
    assertEquals(0, members.size());
    assertEquals(5L, queryOptions.getCount().longValue());
    
    members = HibernateSession.byCriteriaStatic().options(queryOptions).list(Member.class, null);
    
    assertEquals(0, members.size());
    assertTrue(5 < queryOptions.getCount().longValue());
    
    Set<Member> memberSet = i2.getMembers(Group.getDefaultList(), queryOptions);
    
    assertEquals(0, memberSet.size());
    assertEquals(5L, queryOptions.getCount().longValue());
    
    
  }
  
  /**
   * test paging/sorting
   * @throws Exception 
   */
  public void testPagingSorting() throws Exception {
    
    //lets add some members
    GrouperSession grouperSession = SessionHelper.getRootSession();
    Stem root = StemHelper.findRootStem(grouperSession);
    Stem edu = StemHelper.addChildStem(root, "edu", "education");
    Group i2 = StemHelper.addChildGroup(edu, "i2", "internet2");
    Group i3 = StemHelper.addChildGroup(edu, "i3", "internet2");
    Group i4 = StemHelper.addChildGroup(edu, "i4", "internet2");
    Group i5 = StemHelper.addChildGroup(edu, "i5", "internet2");
    Group i6 = StemHelper.addChildGroup(edu, "i6", "internet2");
    Group i7 = StemHelper.addChildGroup(edu, "i7", "internet2");

    i2.addMember(SubjectTestHelper.SUBJ0);
    i2.addMember(SubjectTestHelper.SUBJ1);
    i2.addMember(SubjectTestHelper.SUBJ2);
    i2.addMember(SubjectTestHelper.SUBJ3);
    i2.addMember(SubjectFinder.findRootSubject());

    i3.addMember(SubjectTestHelper.SUBJ1);
    i3.addMember(SubjectTestHelper.SUBJ4);

    QueryPaging queryPaging = QueryPaging.page(3, 1, true);
    
    //page the members in a group
    List<Member> members = HibernateSession.byHqlStatic()
      .createQuery("select distinct theMember from Member as theMember, MembershipEntry as theMembership, Field theField "
      + "where theMembership.ownerGroupId = :ownerId and theMember.uuid = theMembership.memberUuid" +
      		" and theMembership.fieldId = theField.uuid and theField.typeString = 'list' and theField.name = 'members' and theMembership.enabledDb = 'T'")
      		.setString("ownerId", i2.getUuid())
      .options(new QueryOptions().sortAsc("theMember.subjectIdDb").paging(queryPaging)).list(Member.class);
    
    assertEquals("GrouperSystem, test.subject.0, test.subject.1", Member.subjectIds(members));
    
    assertEquals(5, queryPaging.getTotalRecordCount());
    assertEquals(2, queryPaging.getNumberOfPages());
    
    //intersection in one query
    List<String> memberUuids = HibernateSession.byHqlStatic()
      .createQuery("select distinct theMember.uuid from Member theMember, " +
      		"MembershipEntry theMembership, MembershipEntry theMembership2, Field theField " +
      		"where theMembership.ownerGroupId = :group1uuid and theMembership2.ownerGroupId = :group2uuid " +
      		"and theMember.uuid = theMembership.memberUuid and theMember.uuid = theMembership2.memberUuid " +
      		"and theMembership.fieldId = theField.uuid and theMembership2.fieldId = theField.uuid " +
      		"and theField.typeString = 'list' and theField.name = 'members' and theMembership.enabledDb = 'T' and theMembership2.enabledDb = 'T'")
      		.setString("group1uuid", i2.getUuid())
      		.setString("group2uuid", i3.getUuid())
          .list(String.class);

    assertEquals(1, memberUuids.size());
    assertEquals(MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1).getUuid(), memberUuids.get(0));

    //complement in one query
    memberUuids = HibernateSession.byHqlStatic()
    .createQuery("select distinct theMember.uuid from Member theMember, " +
        "MembershipEntry theMembership, Field theField " +
        "where theMembership.ownerGroupId = :group1uuid " +
        "and theMember.uuid = theMembership.memberUuid " +
        "and theMembership.fieldId = theField.uuid " +
        "and theField.typeString = 'list' and theField.name = 'members' and theMembership.enabledDb = 'T' " +
        "and not exists (select theMembership2.memberUuid from MembershipEntry theMembership2 " +
        "where theMembership2.memberUuid = theMember.uuid and theMembership.fieldId = theField.uuid " +
        "and theMembership2.ownerGroupId = :group2uuid and theMembership2.enabledDb = 'T') ")
        .setString("group1uuid", i3.getUuid())
        .setString("group2uuid", i2.getUuid())
        .list(String.class);
  
    assertEquals(1, memberUuids.size());
    assertEquals(MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ4).getUuid(), memberUuids.get(0));
    
    //union in one query
    memberUuids = HibernateSession.byHqlStatic()
    .createQuery("select distinct theMember.uuid from Member theMember, " +
        "MembershipEntry theMembership, MembershipEntry theMembership2, Field theField " +
        "where theMembership.ownerGroupId = :group1uuid and theMembership2.ownerGroupId = :group2uuid " +
        "and (theMember.uuid = theMembership.memberUuid or theMember.uuid = theMembership2.memberUuid) " +
        "and theMembership.fieldId = theField.uuid and theMembership2.fieldId = theField.uuid " +
        "and theField.typeString = 'list' and theField.name = 'members' and theMembership.enabledDb = 'T' and theMembership2.enabledDb = 'T'")
        .setString("group1uuid", i2.getUuid())
        .setString("group2uuid", i3.getUuid())
        .list(String.class);
  
    assertEquals(6, memberUuids.size());
    
    i2.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ, false);
    i3.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    i3.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    i4.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ, false);
    i5.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ, false);
    i6.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ, false);
    i7.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);

    List<String> uuids = GrouperUtil.toList(i2.getUuid(), i4.getUuid(), i5.getUuid(), i6.getUuid(), i7.getUuid());
    
    Collections.sort(uuids);
    
    queryPaging = QueryPaging.page(3, 1, true);
    
    List<Group> groups = HibernateSession.byHqlStatic()
    .createQuery("select distinct g from Group as g, MembershipEntry as m, Field as f " +
    		"where g.parentUuid = :parent " +
    		"and m.ownerGroupId = g.uuid and m.fieldId = f.uuid and f.typeString = 'access' " +
    		"and (m.memberUuid = :sessionMemberId or m.memberUuid = :grouperAllUuid) and m.enabledDb = 'T'")
    		.setString("parent", edu.getUuid())
        .setString("sessionMemberId", MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true).getUuid())
        .setString("grouperAllUuid", MemberFinder.findBySubject(grouperSession, SubjectFinder.findAllSubject(), true).getUuid())
        .options(new QueryOptions().paging(queryPaging).sortAsc("g.uuid"))
        .list(Group.class);
    
    assertEquals(3, groups.size());
    assertEquals(5, queryPaging.getTotalRecordCount());
    assertEquals(uuids.get(0), groups.get(0).getUuid());
    assertEquals(uuids.get(1), groups.get(1).getUuid());
    assertEquals(uuids.get(2), groups.get(2).getUuid());
    
    
    
  }



}
