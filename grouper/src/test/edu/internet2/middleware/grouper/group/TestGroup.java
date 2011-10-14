/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper.group;
import java.util.Iterator;
import java.util.Set;

import junit.framework.Assert;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.entity.Entity;
import edu.internet2.middleware.grouper.entity.EntitySave;
import edu.internet2.middleware.grouper.exception.GroupAddException;
import edu.internet2.middleware.grouper.exception.GroupModifyAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.GrouperStaleObjectStateException;
import edu.internet2.middleware.grouper.helper.FieldHelper;
import edu.internet2.middleware.grouper.helper.GroupHelper;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.PrivHelper;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3DAO;
import edu.internet2.middleware.grouper.misc.AddMissingGroupSets;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Test {@link Group}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestGroup.java,v 1.4 2009-08-11 20:18:09 mchyzer Exp $
 */
public class TestGroup extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(new TestGroup("testNoLocking"));
    //TestRunner.run(TestGroup.class);
    TestRunner.run(new TestGroup("testEntity"));
    //TestRunner.run(TestGroup.class);
  }
  
  // Private Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestGroup.class);


  // Private Class Variables
  private static Stem           edu;
  private static Group          i2;
  private static Stem           root;
  private static GrouperSession s; 

  
  public TestGroup(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    super.setUp();
    s     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(s);
    edu   = StemHelper.addChildStem(root, "edu", "education");
    i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  /**
   * make sure there are no group sets for group members, or read, update, optin, optout.
   * should only be admin, and view.
   */
  public void testEntity() {
    
    //init the stem
    new StemSave(s).assignCreateParentStemsIfNotExist(true)
      .assignName("test")
      .save();


    //count the groupsets
    int originalGroupSetCount = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_group_set");

    Entity entity = new EntitySave(s).assignCreateParentStemsIfNotExist(true)
      .assignName("test:testEntity")
      .save();
    
    int newGroupSetCount = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_group_set");
    
    assertEquals(originalGroupSetCount + 2, newGroupSetCount);
        
    //fix group sets, should still be same
    new AddMissingGroupSets().showResults(true).addAllMissingGroupSets();
    newGroupSetCount = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_group_set");
    
    assertEquals(originalGroupSetCount + 2, newGroupSetCount);
    
    Group entityGroup = (Group)entity;
    
    try {
      entityGroup.addMember(SubjectFinder.findAllSubject());
      fail("shouldnt get here");
    } catch (Exception e) {
      //good
    }
    
    assertFalse(entity.hasView(SubjectFinder.findAllSubject()));
    
    //make one where default is view
    ApiConfig.testConfig.put("entities.create.grant.all.view", "true");
    try {
      
      Entity entity2 = new EntitySave(s).assignCreateParentStemsIfNotExist(true)
        .assignName("test:testEntity2")
        .save();
      
      assertTrue(entity2.hasView(SubjectFinder.findAllSubject()));
      
    } finally {
      ApiConfig.testConfig.put("entities.create.grant.all.view", "false");
    }

    entity.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    entity.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    entity.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN, false);
    entity.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN, false);
    try {
      entity.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      fail("shouldnt get here");
    } catch (Exception e) {
      //good
    }
    try {
      entity.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.UPDATE, false);
      fail("shouldnt get here");
    } catch (Exception e) {
      //good
    }
    try {
      entity.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.OPTIN, false);
      fail("shouldnt get here");
    } catch (Exception e) {
      //good
    }
    try {
      entity.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.OPTOUT, false);
      fail("shouldnt get here");
    } catch (Exception e) {
      //good
    }
    
    
    
    
  }
  
  /**
   * 
   */
  public void testOptimisticLocking() {
    
    //lets not assume that hibernate is set a certain way:
    try {
      ApiConfig.testConfig.put("dao.optimisticLocking", "true");
      
      //re-init
      Hib3DAO.hibernateInitted = false;
      Hib3DAO.initHibernateIfNotInitted();
      
      final Group group1 = (Group)GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READONLY_NEW, new GrouperTransactionHandler() {

        public Object callback(GrouperTransaction grouperTransaction)
            throws GrouperDAOException {
          
          return GroupFinder.findByName(TestGroup.s, TestGroup.i2.getName(), true);
        }
        
      });
      
      final Group group2 = (Group)GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READONLY_NEW, new GrouperTransactionHandler() {

        public Object callback(GrouperTransaction grouperTransaction)
            throws GrouperDAOException {
          
          return GroupFinder.findByName(TestGroup.s, TestGroup.i2.getName(), true);
        }
        
      });

      //now update one, should be ok
      group1.setDescription("Hello");
      group1.store();
      
      //now update the other, should not be ok
      try {
        group2.setDescription("Good bye");
        group2.store();
        fail("Should throw this exception");
      } catch (GrouperStaleObjectStateException sose) {
        //good
      }
      
      group1.delete();
      
    } finally {
      
      //put hibernate back the way it was
      ApiConfig.testConfig.clear();
      
      //re-init
      Hib3DAO.hibernateInitted = false;
      Hib3DAO.initHibernateIfNotInitted();

    }
    
  }

  /**
   * 
   */
  public void testNoLocking() {
    
    //lets not assume that hibernate is set a certain way:
    try {
      ApiConfig.testConfig.put("dao.optimisticLocking", "false");
      
      //re-init
      Hib3DAO.hibernateInitted = false;
      Hib3DAO.initHibernateIfNotInitted();
      
      final Group group1 = (Group)GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READONLY_NEW, new GrouperTransactionHandler() {

        public Object callback(GrouperTransaction grouperTransaction)
            throws GrouperDAOException {
          
          return GroupFinder.findByName(TestGroup.s, TestGroup.i2.getName(), true);
        }
        
      });
      
      final Group group2 = (Group)GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READONLY_NEW, new GrouperTransactionHandler() {

        public Object callback(GrouperTransaction grouperTransaction)
            throws GrouperDAOException {
          
          return GroupFinder.findByName(TestGroup.s, TestGroup.i2.getName(), true);
        }
        
      });

      //now update one, should be ok
      group1.setDescription("Hello");
      group1.store();
      
      //should not throw the stale exception
      group2.setDescription("Good bye");
      group2.store();
      
      
    } finally {
      
      //put hibernate back the way it was
      
      ApiConfig.testConfig.clear();
      //re-init
      Hib3DAO.hibernateInitted = false;
      Hib3DAO.initHibernateIfNotInitted();
      
    }
    
    
    
  }

  /**
   * 
   */
  public void testGetParentStem() {
    LOG.info("testGetParentStem");
    Stem parent = i2.getParentStem();
    Assert.assertNotNull("group has parent", parent);
    Assert.assertTrue("parent == edu", parent.equals(edu));
    Assert.assertTrue(
      "root has STEM on parent", parent.hasStem(s.getSubject())
    );
  } 

  /**
   * 
   */
  public void testGetTypes() {
    LOG.info("testGetTypes");
    Set types = i2.getTypes();
    Assert.assertTrue("has 1 type/" + types.size(), types.size() == 1);
    Iterator iter = types.iterator();
    while (iter.hasNext()) {
      GroupType type = (GroupType) iter.next();
      Assert.assertNotNull("type !null", type);
      Assert.assertTrue("type instanceof GroupType", type instanceof GroupType);
      Assert.assertTrue("type name == base", type.getName().equals("base"));
      Set fields = type.getFields();
      Assert.assertEquals("type has 7 fields", 7, fields.size());
      Iterator  fIter = fields.iterator();
      FieldHelper.testField( 
        (Field) fIter.next()   , 
        Field.FIELD_NAME_ADMINS              , FieldType.ACCESS,
        AccessPrivilege.ADMIN , AccessPrivilege.ADMIN
      );
      FieldHelper.testField( 
        (Field) fIter.next()   , 
        "members"             , FieldType.LIST,
        AccessPrivilege.READ  , AccessPrivilege.UPDATE
      );
      FieldHelper.testField( 
        (Field) fIter.next()   , 
        Field.FIELD_NAME_OPTINS              , FieldType.ACCESS,
        AccessPrivilege.UPDATE, AccessPrivilege.UPDATE
      );
      FieldHelper.testField( 
        (Field) fIter.next()   , 
        Field.FIELD_NAME_OPTOUTS             , FieldType.ACCESS,
        AccessPrivilege.UPDATE, AccessPrivilege.UPDATE
      );
      FieldHelper.testField( 
        (Field) fIter.next()   , 
        "readers"             , FieldType.ACCESS,
        AccessPrivilege.ADMIN , AccessPrivilege.ADMIN
      );
      FieldHelper.testField( 
        (Field) fIter.next()   , 
        Field.FIELD_NAME_UPDATERS            , FieldType.ACCESS,
        AccessPrivilege.ADMIN , AccessPrivilege.ADMIN
      );
      FieldHelper.testField( 
        (Field) fIter.next()   , 
        "viewers"             , FieldType.ACCESS,
        AccessPrivilege.ADMIN , AccessPrivilege.ADMIN
      );
    }
  } // public void testGetTypes()

  public void testAddChildGroupWithBadExtnOrDisplayExtn() {
    LOG.info("testAddChildGroupWithBadExtnOrDisplayExtn");
    try {
      try {
        edu.addChildGroup(null, "test");
        Assert.fail("added group with null extn");
      }
      catch (GroupAddException eSA) {
        Assert.assertTrue("null extn", true);
      }
      try {
        edu.addChildGroup("", "test");
        Assert.fail("added group with empty extn");
      }
      catch (GroupAddException eSA) {
        Assert.assertTrue("empty extn", true);
      }
      try {
        edu.addChildGroup("a:test", "test");
        Assert.fail("added group with colon-containing extn");
      }
      catch (GroupAddException eSA) {
        Assert.assertTrue("colon-containing extn", true);
      }
      try {
        edu.addChildGroup("test", null);
        Assert.fail("added group with null displayExtn");
      }
      catch (GroupAddException eSA) {
        Assert.assertTrue("null displayExtn", true);
      }
      try {
        edu.addChildGroup("test", "");
        Assert.fail("added group with empty displayextn");
      }
      catch (GroupAddException eSA) {
        Assert.assertTrue("empty displayExtn", true);
      }
      try {
        edu.addChildGroup("test", "a:test");
        Assert.fail("added group with colon-containing displayExtn");
      }
      catch (GroupAddException eSA) {
        Assert.assertTrue("colon-containing displayExtn", true);
      }
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testAddChildGroupWithBadExtnOrDisplayExtn()

  public void testSetBadGroupExtension() {
    LOG.info("testSetBadGroupExtension");
    try {
      try {
        i2.setExtension(null);
        i2.store();
        Assert.fail("set null extn");
      }
      catch (RuntimeException eSA) {
        Assert.assertTrue("null extn", true);
      }
      try {
        i2.setExtension("");
        i2.store();
        Assert.fail("set empty extn");
      }
      catch (RuntimeException eSA) {
        Assert.assertTrue("empty extn", true);
      }
      try {
        i2.setExtension("a:test");
        Assert.fail("set colon-containing extn");
      }
      catch (RuntimeException eSA) {
        Assert.assertTrue("colon-containing extn", true);
      }
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testSetBadGroupExtension()

  public void testSetBadGroupDisplayExtension() {
    LOG.info("testSetBadGroupDisplayExtension");
    try {
      try {
        i2.setDisplayExtension(null);
        i2.store();
        Assert.fail("set null displayExtn");
      }
      catch (RuntimeException eSA) {
        Assert.assertTrue("null displayExtn", true);
      }
      try {
        i2.setDisplayExtension("");
        i2.store();
        Assert.fail("set empty displayExtn");
      }
      catch (RuntimeException eSA) {
        Assert.assertTrue("empty displayExtn", true);
      }
      try {
        i2.setDisplayExtension("a:test");
        i2.store();
        Assert.fail("set colon-containing displayExtn");
      }
      catch (RuntimeException eSA) {
        Assert.assertTrue("colon-containing displayExtn", true);
      }
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testSetBadGroupDisplayExtension()

  public void testGetAndHasPrivs() {
    LOG.info("testGetAndHasPrivs");
    try {
      Subject         subj  = SubjectTestHelper.SUBJ0;
      Subject         subj1 = SubjectTestHelper.SUBJ1;
      Subject         all   = SubjectTestHelper.SUBJA;
      GrouperSession  s     = SessionHelper.getRootSession();
      Group           uofc  = edu.addChildGroup("uofc", "uofc");
      GroupHelper.addMember(uofc, subj, "members");
      GroupHelper.addMember(i2, uofc.toSubject(), "members");
      MemberFinder.findBySubject(s, subj, true);
      PrivHelper.grantPriv(s, i2,   all,  AccessPrivilege.OPTIN);
      PrivHelper.grantPriv(s, uofc, subj, AccessPrivilege.UPDATE);

      // Get access privs
      Assert.assertTrue("admins/i2      == 1",  i2.getAdmins().size()   == 1);
      Assert.assertTrue("optins/i2      == 1",  i2.getOptins().size()   == 1);
      Assert.assertTrue("optouts/i2     == 0",  i2.getOptouts().size()  == 0);
      Assert.assertTrue("readers/i2     == 1",  i2.getReaders().size()  == 1);
      Assert.assertTrue("updaters/i2    == 0",  i2.getUpdaters().size() == 0);
      Assert.assertTrue("viewers/i2     == 1",  i2.getViewers().size()  == 1);

      Assert.assertTrue("admins/uofc    == 1",  uofc.getAdmins().size()   == 1);
      Assert.assertTrue("optins/uofc    == 0",  uofc.getOptins().size()   == 0);
      Assert.assertTrue("optouts/uofc   == 0",  uofc.getOptouts().size()  == 0);
      Assert.assertTrue("readers/uofc   == 1",  uofc.getReaders().size()  == 1);
      Assert.assertTrue("updaters/uofc  == 1",  uofc.getUpdaters().size() == 1);
      Assert.assertTrue("viewers/uofc   == 1",  uofc.getViewers().size()  == 1);

      // Has access privs
      Assert.assertTrue("admin/i2/subj0",     !i2.hasAdmin(subj)      );
      Assert.assertTrue("admin/i2/subj1",     !i2.hasAdmin(subj1)     );
      Assert.assertTrue("admin/i2/subjA",     !i2.hasAdmin(all)       );

      Assert.assertTrue("optin/i2/subj0",     i2.hasOptin(subj)       );
      Assert.assertTrue("optin/i2/subj1",     i2.hasOptin(subj1)      );
      Assert.assertTrue("optin/i2/subjA",     i2.hasOptin(all)        );

      Assert.assertTrue("optout/i2/subj0",    !i2.hasOptout(subj)     );
      Assert.assertTrue("optout/i2/subj1",    !i2.hasOptout(subj1)    );
      Assert.assertTrue("optout/i2/subjA",    !i2.hasOptout(all)      );

      Assert.assertTrue("read/i2/subj0",      i2.hasRead(subj)        );
      Assert.assertTrue("read/i2/subj1",      i2.hasRead(subj1)       );
      Assert.assertTrue("read/i2/subjA",      i2.hasRead(all)         );

      Assert.assertTrue("update/i2/subj0",    !i2.hasUpdate(subj)     );
      Assert.assertTrue("update/i2/subj1",    !i2.hasUpdate(subj1)    );
      Assert.assertTrue("update/i2/subjA",    !i2.hasUpdate(all)      );

      Assert.assertTrue("view/i2/subj0",      i2.hasView(subj)        );
      Assert.assertTrue("view/i2/subj1",      i2.hasView(subj1)       );
      Assert.assertTrue("view/i2/subjA",      i2.hasView(all)         );

      Assert.assertTrue("admin/uofc/subj0",   !uofc.hasAdmin(subj)    );
      Assert.assertTrue("admin/uofc/subj1",   !uofc.hasAdmin(subj1)   );
      Assert.assertTrue("admin/uofc/subjA",   !uofc.hasAdmin(all)     );

      Assert.assertTrue("optin/uofc/subj0",   !uofc.hasOptin(subj)    );
      Assert.assertTrue("optin/uofc/subj1",   !uofc.hasOptin(subj1)   );
      Assert.assertTrue("optin/uofc/subjA",   !uofc.hasOptin(all)     );

      Assert.assertTrue("optout/uofc/subj0",  !uofc.hasOptout(subj)   );
      Assert.assertTrue("optout/uofc/subj1",  !uofc.hasOptout(subj1)  );
      Assert.assertTrue("optout/uofc/subjA",  !uofc.hasOptout(all)    );

      Assert.assertTrue("read/uofc/subj0",    uofc.hasRead(subj)      );
      Assert.assertTrue("read/uofc/subj1",    uofc.hasRead(subj1)     );
      Assert.assertTrue("read/uofc/subjA",    uofc.hasRead(all)       );

      Assert.assertTrue("update/uofc/subj0",  uofc.hasUpdate(subj)    );
      Assert.assertTrue("update/uofc/subj1",  !uofc.hasUpdate(subj1)  );
      Assert.assertTrue("update/uofc/subjA",  !uofc.hasUpdate(all)    );

      Assert.assertTrue("view/uofc/subj0",    uofc.hasView(subj)      );
      Assert.assertTrue("view/uofc/subj1",    uofc.hasView(subj1)     );
      Assert.assertTrue("view/uofc/subjA",    uofc.hasView(all)       );

      s.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  }

  public void testSetDescription() {
    LOG.info("testSetDescription");
    try {
      GrouperSession  s     = SessionHelper.getRootSession();
      String          orig  = i2.getDescription(); 
      String          set   = "this is a group"; 
      i2.setDescription(set);
      i2.store();
      Assert.assertTrue("!orig",  !i2.getDescription().equals(orig));
      Assert.assertTrue("set",    i2.getDescription().equals(set));
      s.stop();
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testSetDescription()

  // Tests
  
  public void testToMember() {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    GroupHelper.toMember(i2);
  } // public void testToMember()

  /**
   * 
   */
  public void testSetExtensionSame() {
    LOG.info("testSetDescription");
    try {
      GrouperSession  s     = SessionHelper.getRootSession();
      
      Group i3    = StemHelper.addChildGroup(edu, "i3", "internet3");
      
      i3.setExtension("i2");
      
      try {
        i3.store();
        fail("Cant set extension to an existing one");
      } catch (GroupModifyAlreadyExistsException mdaee) {
        //good
      }
      
      s.stop();
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }

  }
 
  /**
   * 
   */
  public void testGetAllGroupsSecure() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    QueryOptions queryOptions = new QueryOptions().paging(
        200, 1, true).sortAsc("theGroup.displayNameDb");
    Set<Group> groups = GrouperDAOFactory.getFactory().getGroup().getAllGroupsSecure("%test%", 
        grouperSession, grouperSession.getSubject(), 
        GrouperUtil.toSet(AccessPrivilege.ADMIN, AccessPrivilege.UPDATE), queryOptions);
    
    groups = GrouperDAOFactory.getFactory().getGroup().getAllGroupsSecure("%test%", 
        grouperSession, grouperSession.getSubject(), 
        GrouperUtil.toSet(AccessPrivilege.ADMIN, AccessPrivilege.UPDATE), null);
    
    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    groups = GrouperDAOFactory.getFactory().getGroup().getAllGroupsSecure("%test%", 
        grouperSession, grouperSession.getSubject(), 
        GrouperUtil.toSet(AccessPrivilege.ADMIN, AccessPrivilege.UPDATE), queryOptions);
    
    groups = GrouperDAOFactory.getFactory().getGroup().getAllGroupsSecure("%test%", 
        grouperSession, grouperSession.getSubject(), 
        GrouperUtil.toSet(AccessPrivilege.ADMIN, AccessPrivilege.UPDATE), null);

    
    GrouperSession.stopQuietly(grouperSession);
  }
  
  
  /**
   * make an example group for testing
   * @return an example group
   */
  public static Group exampleGroup() {
    Group group = new Group();
    group.setAlternateNameDb("alternateName");
    group.setContextId("contextId");
    group.setCreateTimeLong(5L);
    group.setCreatorUuid("creatorId");
    group.setDescription("description");
    group.setDisplayExtensionDb("displayExtension");
    group.setDisplayNameDb("displayName");
    group.setExtensionDb("extension");
    group.setHibernateVersionNumber(3L);
    group.setLastMembershipChangeDb(4L);
    group.setModifierUuid("modifierId");
    group.setModifyTimeLong(6L);
    group.setNameDb("name");
    group.setParentUuid("parentUuid");
    group.setTypeOfGroupDb("role");
    group.setUuid("uuid");
    
    return group;
  }
  
  /**
   * make an example group from db for testing
   * @return an example group
   */
  public static Group exampleGroupDb() {
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTest").assignName("test:groupTest").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    return group;
  }

  
  /**
   * retrieve example group from db for testing
   * @return an example group
   */
  public static Group exampleRetrieveGroupDb() {
    Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), "test:groupTest", true);
    return group;
  }

  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlInsert() {
    
    GrouperSession.startRootSession();
    
    Group groupOriginal = new GroupSave(GrouperSession.staticGrouperSession()).assignGroupNameToEdit("test:groupInsert")
      .assignName("test:groupInsert").assignCreateParentStemsIfNotExist(true).save();
    
    //not sure why I need to sleep, but the last membership update gets messed up...
    GrouperUtil.sleep(1000);
    
    //do this because last membership update isnt there, only in db
    groupOriginal = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupOriginal.getUuid(), true, null);
    Group groupCopy = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupOriginal.getUuid(), true, null);
    Group groupCopy2 = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupOriginal.getUuid(), true, null);
    groupCopy.delete();
    
    //lets insert the original
    groupCopy2.xmlSaveBusinessProperties(null);
    groupCopy2.xmlSaveUpdateProperties();

    //refresh from DB
    groupCopy = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupOriginal.getUuid(), true, null);
    
    assertFalse(groupCopy == groupOriginal);
    assertFalse(groupCopy.xmlDifferentBusinessProperties(groupOriginal));
    assertFalse(groupCopy.xmlDifferentUpdateProperties(groupOriginal));
    
  }
  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlDifferentUpdateProperties() {
    
    @SuppressWarnings("unused")
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group group = null;
    Group exampleGroup = null;

    
    //TEST UPDATE PROPERTIES
    {
      group = exampleGroupDb();
      exampleGroup = group.clone();
      
      group.setContextId("abc");
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertTrue(group.xmlDifferentUpdateProperties(exampleGroup));

      group.setContextId(exampleGroup.getContextId());
      group.xmlSaveUpdateProperties();

      group = exampleRetrieveGroupDb();
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));
      
    }
    
    {
      group = exampleGroupDb();
      exampleGroup = group.clone();

      group.setCreateTimeLong(99);
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertTrue(group.xmlDifferentUpdateProperties(exampleGroup));

      group.setCreateTimeLong(exampleGroup.getCreateTimeLong());
      group.xmlSaveUpdateProperties();
      
      group = exampleRetrieveGroupDb();
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));
    }
    
    {
      group = exampleGroupDb();
      exampleGroup = group.clone();

      group.setCreatorUuid("abc");
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertTrue(group.xmlDifferentUpdateProperties(exampleGroup));

      group.setCreatorUuid(exampleGroup.getCreatorUuid());
      group.xmlSaveUpdateProperties();
      
      group = exampleRetrieveGroupDb();
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));
    }
    
    {
      group = exampleGroupDb();
      exampleGroup = group.clone();
      
      group.setModifierUuid("abc");
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertTrue(group.xmlDifferentUpdateProperties(exampleGroup));

      group.setModifierUuid(exampleGroup.getModifierUuid());
      group.xmlSaveUpdateProperties();
      
      group = exampleRetrieveGroupDb();
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));
    }
    
    {
      group = exampleGroupDb();
      exampleGroup = group.clone();

      group.setModifyTimeLong(99);
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertTrue(group.xmlDifferentUpdateProperties(exampleGroup));

      group.setModifyTimeLong(exampleGroup.getModifyTimeLong());
      group.xmlSaveUpdateProperties();
      
      group = exampleRetrieveGroupDb();
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));

    }

    {
      group = exampleGroupDb();
      exampleGroup = group.clone();

      group.setHibernateVersionNumber(99L);
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertTrue(group.xmlDifferentUpdateProperties(exampleGroup));

      group.setHibernateVersionNumber(exampleGroup.getHibernateVersionNumber());
      group.xmlSaveUpdateProperties();
      
      group = exampleRetrieveGroupDb();
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));
    }
    //TEST BUSINESS PROPERTIES
    
    {
      group = exampleGroupDb();
      exampleGroup = group.clone();

      group.setAlternateNameDb("abc");
      
      assertTrue(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));

      group.setAlternateNameDb(exampleGroup.getAlternateNameDb());
      group.xmlSaveBusinessProperties(exampleGroup.clone());
      group.xmlSaveUpdateProperties();
      
      group = exampleRetrieveGroupDb();
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));
    
    }
    
    {
      group = exampleGroupDb();
      exampleGroup = group.clone();

      group.setDescriptionDb("abc");
      
      assertTrue(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));

      group.setDescriptionDb(exampleGroup.getDescriptionDb());
      group.xmlSaveBusinessProperties(exampleGroup.clone());
      group.xmlSaveUpdateProperties();
      
      group = exampleRetrieveGroupDb();
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));
    
    }
    
    {
      group = exampleGroupDb();
      exampleGroup = group.clone();

      group.setDisplayExtensionDb("abc");
      
      assertTrue(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));

      group.setDisplayExtensionDb(exampleGroup.getDisplayExtensionDb());
      group.xmlSaveBusinessProperties(exampleGroup.clone());
      group.xmlSaveUpdateProperties();
      
      group = exampleRetrieveGroupDb();
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));
    
    }
    
    {
      group = exampleGroupDb();
      exampleGroup = group.clone();

      group.setDisplayNameDb("abc");
      
      assertTrue(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));

      group.setDisplayNameDb(exampleGroup.getDisplayNameDb());
      group.xmlSaveBusinessProperties(exampleGroup.clone());
      group.xmlSaveUpdateProperties();
      
      group = exampleRetrieveGroupDb();
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));
    
    }
    
    {
      group = exampleGroupDb();
      exampleGroup = group.clone();

      group.setExtensionDb("abc");
      
      assertTrue(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));

      group.setExtensionDb(exampleGroup.getExtensionDb());
      group.xmlSaveBusinessProperties(exampleGroup.clone());
      group.xmlSaveUpdateProperties();
      
      group = exampleRetrieveGroupDb();
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));
    
    }
    
    {
      group = exampleGroupDb();
      exampleGroup = group.clone();

      group.setNameDb("abc");
      
      assertTrue(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));

      group.setNameDb(exampleGroup.getNameDb());
      group.xmlSaveBusinessProperties(exampleGroup.clone());
      group.xmlSaveUpdateProperties();
      
      group = exampleRetrieveGroupDb();
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));
    
    }
    
    {
      group = exampleGroupDb();
      exampleGroup = group.clone();

      group.setParentUuid("abc");
      
      assertTrue(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));

      group.setParentUuid(exampleGroup.getParentUuid());
      group.xmlSaveBusinessProperties(exampleGroup.clone());
      group.xmlSaveUpdateProperties();
      
      group = exampleRetrieveGroupDb();
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));
    
    }

    {
      group = exampleGroupDb();
      exampleGroup = group.clone();

      group.setTypeOfGroup(TypeOfGroup.role);
      
      assertTrue(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));

      group.setTypeOfGroup(exampleGroup.getTypeOfGroup());
      group.xmlSaveBusinessProperties(exampleGroup.clone());
      group.xmlSaveUpdateProperties();
      
      group = exampleRetrieveGroupDb();
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));
    
    }

    {
      group = exampleGroupDb();
      exampleGroup = group.clone();

      group.setUuid("abc");
      
      assertTrue(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));

      group.setUuid(exampleGroup.getUuid());
      group.xmlSaveBusinessProperties(exampleGroup.clone());
      group.xmlSaveUpdateProperties();
      
      group = exampleRetrieveGroupDb();
      
      assertFalse(group.xmlDifferentBusinessProperties(exampleGroup));
      assertFalse(group.xmlDifferentUpdateProperties(exampleGroup));
    
    }
  }

  
}

