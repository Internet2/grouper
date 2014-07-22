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
import java.util.Properties;
import java.util.Set;

import junit.framework.Assert;
import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.exception.CompositeNotFoundException;
import edu.internet2.middleware.grouper.exception.GrantPrivilegeException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.filter.GroupAttributeFilter;
import edu.internet2.middleware.grouper.filter.GrouperQuery;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.hibernate.GrouperRollbackType;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * @author  blair christensen.
 * @version $Id: TestGroup1.java,v 1.3 2009-09-17 15:33:05 shilen Exp $
 */
public class TestGroup1 extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestGroup1("testReplaceMembers"));
  }
  
  private static final Log LOG = GrouperUtil.getLog(TestGroup1.class);

  public TestGroup1(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testReplaceMembers() {
    LOG.info("testReplaceMembers");
    try {
      R registry = R.populateRegistry(1, 1, 0);
      Group group = registry.getGroup("a", "a");

      //group has no members, lets replace with some
      Set<Subject> subjects = GrouperUtil.toSet(SubjectTestHelper.SUBJ0, SubjectTestHelper.SUBJ1);
      
      assertEquals(2, group.replaceMembers(subjects));
      assertEquals(2, group.getMembers().size());
      
      assertTrue(group.hasMember(SubjectTestHelper.SUBJ0));
      assertTrue(group.hasMember(SubjectTestHelper.SUBJ1));
      
      subjects = GrouperUtil.toSet(SubjectTestHelper.SUBJ1, SubjectTestHelper.SUBJ2);
      
      assertEquals(2, group.replaceMembers(subjects));
      assertEquals(2, group.getMembers().size());
      
      assertTrue(group.hasMember(SubjectTestHelper.SUBJ1));
      assertTrue(group.hasMember(SubjectTestHelper.SUBJ2));
      
      //lets blank it out
      assertEquals(2, group.replaceMembers(null));
      assertEquals(0, group.getMembers().size());
      
      registry.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testReplaceMembers()

  
  public void testDeleteGroupMemberWithNonGroupMember() {
    LOG.info("testDeleteGroupMemberWithNonGroupMember");
    try {
      R       r     = R.populateRegistry(1, 2, 1);
      Group   a     = r.getGroup("a", "a");
      Subject aSubj = a.toSubject();
      Group   b     = r.getGroup("a", "b");
      Subject subjA = r.getSubject("a");

      Assert.assertFalse( "[0] a !has subjA"  , a.hasMember(subjA)  );
      Assert.assertFalse( "[0] b !has a"      , b.hasMember(aSubj)  );
      Assert.assertFalse( "[0] b !has subjA"  , b.hasMember(subjA)  );

      a.addMember(subjA);
      Assert.assertTrue(  "[1] a has subjA"   , a.hasMember(subjA)  );
      Assert.assertFalse( "[1] b !has a"      , b.hasMember(aSubj)  );
      Assert.assertFalse( "[0] b !has subjA"  , b.hasMember(subjA)  );

      b.addMember(aSubj);
      Assert.assertTrue(  "[2] a has subjA"   , a.hasMember(subjA)  );
      Assert.assertTrue(  "[2] b has a"       , b.hasMember(aSubj)  );
      Assert.assertTrue(  "[2] b has subjA"   , b.hasMember(subjA)  );

      b.deleteMember(aSubj);
      Assert.assertTrue(  "[3] a has subjA"   , a.hasMember(subjA)  );
      Assert.assertFalse( "[3] b !has a"      , b.hasMember(aSubj)  );
      Assert.assertFalse( "[3] b !has subjA"  , b.hasMember(subjA)  );

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDeleteGroupMemberWithNonGroupMember()

  public void testCanReadFieldValidField() {
    LOG.info("testCanReadFieldValidField");
    try {
      R       r     = R.populateRegistry(1, 1, 0);
      Group   a     = r.getGroup("a", "a");
  
      Assert.assertTrue("can read", a.canReadField(Group.getDefaultList()));
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testCanReadFieldValidField()

  public void testCanReadFieldValidFieldNotRoot() {
    LOG.info("testCanReadFieldValidFieldNotRoot");
    try {
      R       r     = R.populateRegistry(1, 1, 1);
      Group   a     = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");
  
      GrouperSession s = GrouperSession.start(subjA);
      Assert.assertTrue("can read", a.canReadField(Group.getDefaultList()));
      s.stop();
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testCanReadFieldValidFieldNotRoot()

  public void testCanReadFieldValidSubjectValidField() {
    LOG.info("testCanReadFieldValidSubjectValidField");
    try {
      R       r     = R.populateRegistry(1, 1, 0);
      Group   a     = r.getGroup("a", "a");
  
      Assert.assertTrue(
        "can read", 
        a.canReadField(r.rs.getSubject(), Group.getDefaultList())
      );
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testCanReadFieldValidSubjectValidField()

  public void testCanReadFieldValidSubjectValidFieldNotRoot() {
    LOG.info("testCanReadFieldValidSubjectValidFieldNotRoot");
    try {
      R       r     = R.populateRegistry(1, 1, 1);
      Group   a     = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");
  
      GrouperSession s = GrouperSession.start(subjA);
      Assert.assertTrue(
        "can read", 
        a.canReadField(subjA, Group.getDefaultList())
      );
      s.stop();
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testCanReadFieldValidSubjectValidFieldNotRoot()

  public void testDeleteMemberFromGroupThatIsMember() {
    LOG.info("testDeleteMemberFromGroupThatIsMember");
    try {
      R       r     = R.populateRegistry(1, 2, 1);
      Group   a     = r.getGroup("a", "a");
      Subject aSubj = a.toSubject();
      Group   b     = r.getGroup("a", "b");
      Subject subjA = r.getSubject("a");
  
      Assert.assertFalse( "[0] a !has subjA"  , a.hasMember(subjA)  );
      Assert.assertFalse( "[0] b !has a"      , b.hasMember(aSubj)  );
      Assert.assertFalse( "[0] b !has subjA"  , b.hasMember(subjA)  );
  
      b.addMember(aSubj);
      Assert.assertFalse( "[1] a !has subjA"  , a.hasMember(subjA)  );
      Assert.assertTrue(  "[1] b has a"       , b.hasMember(aSubj)  );
      Assert.assertFalse( "[1] b !has subjA"  , b.hasMember(subjA)  );
  
      a.addMember(subjA);
      Assert.assertTrue(  "[2] a has subjA"   , a.hasMember(subjA)  );
      Assert.assertTrue(  "[2] b has a"       , b.hasMember(aSubj)  );
      Assert.assertTrue(  "[2] b has subjA"   , b.hasMember(subjA)  );
  
      a.deleteMember(subjA);
      Assert.assertFalse( "[3] a !has subjA"  , a.hasMember(subjA)  );
      Assert.assertTrue(  "[3] b has a"       , b.hasMember(aSubj)  );
      Assert.assertFalse( "[3] b !has subjA"  , b.hasMember(subjA)  );
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDeleteMemberFromGroupThatIsMember()

  public void testFailCanReadFieldNullSubjectInvalidField() {
    LOG.info("testFailCanReadFieldNullSubjectInvalidField");
    try {
      R       r     = R.populateRegistry(1, 1, 0);
      Group   a     = r.getGroup("a", "a");
  
      try {
        a.canReadField(null, FieldFinder.find(Field.FIELD_NAME_STEMMERS, true));
        Assert.fail("IllegalArgumentException not thrown");
      }
      catch (IllegalArgumentException eIA) {
        T.string( "IllegalArgumentException", "subject: null value", eIA.getMessage() );
      }
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanReadFieldNullSubjectInvalidField()

  public void testFailCanReadFieldNullSubjectNullField() {
    LOG.info("testFailCanReadFieldNullSubjectNullField");
    try {
      R       r     = R.populateRegistry(1, 1, 0);
      Group   a     = r.getGroup("a", "a");
  
      try {
        a.canReadField(null, null);
        Assert.fail("IllegalArgumentException not thrown");
      }
      catch (IllegalArgumentException eIA) {
        T.string( "IllegalArgumentException", "subject: null value", eIA.getMessage() );
      }
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanReadFieldNullSubjectNullField()

  public void testFailCanReadFieldNullSubjectValidField() {
    LOG.info("testFailCanReadFieldNullSubjectValidField");
    try {
      R       r     = R.populateRegistry(1, 1, 0);
      Group   a     = r.getGroup("a", "a");
  
      GrouperSession grouperSession = GrouperSession.start( SubjectFinder.findRootSubject() );
  
      GroupType groupType = GroupType.createType(grouperSession, "theGroupType", false); 
      groupType.addList(grouperSession, "theList1", 
            AccessPrivilege.READ, AccessPrivilege.ADMIN);;
      a.addType(groupType, false);
  
      grouperSession.stop();
  
      try {
        a.canReadField(null, FieldFinder.find("theList1", true));
        Assert.fail("IllegalArgumentException not thrown");
      }
      catch (IllegalArgumentException eIA) {
        T.string( "IllegalArgumentException", "subject: null value", eIA.getMessage() );
      }
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanReadFieldNullSubjectValidField()

  public void testFailCanReadFieldValidSubjectValidFieldNotRoot() {
    LOG.info("testFailCanReadFieldValidSubjectValidFieldNotRoot");
    try {
      R       r     = R.populateRegistry(1, 1, 1);
      Group   a     = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");
  
      GrouperSession s = GrouperSession.start(subjA);
      Assert.assertFalse(
        "cannot read", 
        a.canReadField(subjA, FieldFinder.find(Field.FIELD_NAME_ADMINS, true))
      );
      s.stop();
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanReadFieldValidSubjectValidFieldNotRoot()

  public void testFailCanWriteFieldInvalidField() {
    LOG.info("testFailCanWriteFieldInvalidField");
    try {
      R       r     = R.populateRegistry(1, 1, 0);
      Group   a     = r.getGroup("a", "a");
  
      try {
        a.canWriteField(FieldFinder.find(Field.FIELD_NAME_STEMMERS, true));
        Assert.fail("SchemaException not thrown");
      }
      catch (SchemaException eS) {
        T.string("SchemaException", E.FIELD_INVALID_TYPE + "naming", eS.getMessage());
      }
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanWriteFieldInvalidField()

  public void testFailCanWriteFieldNullField() {
    LOG.info("testFailCanWriteFieldNullField");
    try {
      R       r     = R.populateRegistry(1, 1, 0);
      Group   a     = r.getGroup("a", "a");
  
      try {
        a.canWriteField(null);
        Assert.fail("IllegalArgumentException not thrown");
      }
      catch (IllegalArgumentException eIA) {
        T.string( "IllegalArgumentException", "field: null value", eIA.getMessage() );
      }
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanWriteFieldNullField()

  public void testFailCanWriteFieldValidSubjectInvalidField() {
    LOG.info("testFailCanWriteFieldValidSubjectInvalidField");
    try {
      R       r     = R.populateRegistry(1, 1, 0);
      Group   a     = r.getGroup("a", "a");
  
      try {
        a.canWriteField(r.rs.getSubject(), FieldFinder.find(Field.FIELD_NAME_STEMMERS, true));
        Assert.fail("SchemaException not thrown");
      }
      catch (SchemaException eS) {
        T.string("SchemaException", E.FIELD_INVALID_TYPE + "naming", eS.getMessage());
      }
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanWriteFieldValidSubjectInvalidField()

  public void testFailCanWriteFieldValidSubjectNullField() {
    LOG.info("testFailCanWriteFieldValidSubjectNullField");
    try {
      R       r     = R.populateRegistry(1, 1, 0);
      Group   a     = r.getGroup("a", "a");
  
      try {
        a.canWriteField(r.rs.getSubject(), null);
        Assert.fail("IllegalArgumentException not thrown");
      }
      catch (IllegalArgumentException eIA) {
        T.string( "IllegalArgumentException", "field: null value", eIA.getMessage() );
      }
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanWriteFieldValidSubjectNullField()

  public void testCanWriteFieldValidField() {
    LOG.info("testCanWriteFieldValidField");
    try {
      R       r     = R.populateRegistry(1, 1, 0);
      Group   a     = r.getGroup("a", "a");
  
      Assert.assertTrue("can write", a.canWriteField(Group.getDefaultList()));
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testCanWriteFieldValidField()

  public void testCanWriteFieldValidFieldNotRoot() {
    LOG.info("testCanWriteFieldValidFieldNotRoot");
    try {
      R       r     = R.populateRegistry(1, 1, 1);
      Group   a     = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");
      a.grantPriv(subjA, AccessPrivilege.UPDATE);
  
      GrouperSession s = GrouperSession.start(subjA);
      Assert.assertTrue("can write", a.canWriteField(Group.getDefaultList()));
      s.stop();
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testCanWriteFieldValidFieldNotRoot()

  public void testCanWriteFieldValidSubjectValidField() {
    LOG.info("testCanWriteFieldValidSubjectValidField");
    try {
      R       r     = R.populateRegistry(1, 1, 0);
      Group   a     = r.getGroup("a", "a");
  
      Assert.assertTrue(
        "can write", 
        a.canWriteField(r.rs.getSubject(), Group.getDefaultList())
      );
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testCanWriteFieldValidSubjectValidField()

  public void testCanWriteFieldValidSubjectValidFieldNotRoot() {
    LOG.info("testCanWriteFieldValidSubjectValidFieldNotRoot");
    try {
      R       r     = R.populateRegistry(1, 1, 1);
      Group   a     = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");
      a.grantPriv(subjA, AccessPrivilege.UPDATE);
  
      GrouperSession s = GrouperSession.start(subjA);
      Assert.assertTrue(
        "can write", 
        a.canWriteField(subjA, Group.getDefaultList())
      );
      s.stop();
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testCanWriteFieldValidSubjectValidFieldNotRoot()

  public void testCompositeIsDeletedWhenGroupIsDeleted() {
    LOG.info("testCompositeIsDeletedWhenGroupIsDeleted");
    try {
      R           r     = R.populateRegistry(1, 3, 1);
      Group       gA    = r.getGroup("a", "a");
      Group       gB    = r.getGroup("a", "b");
      Group       gC    = r.getGroup("a", "c");
      Subject     subjA = r.getSubject("a");
      gB.addMember(subjA);
      gA.addCompositeMember(CompositeType.UNION, gB, gC);
      Member      mA    = MemberFinder.findBySubject(r.rs, subjA, true);
      T.amount("subjA mships before deletion", 2, mA.getMemberships().size());
      MembershipFinder.findCompositeMembership(r.rs, gA, subjA, true);  
      gA.delete(); 
      mA    = MemberFinder.findBySubject(r.rs, subjA, true);
      T.amount("subjA mships after deletion", 1, mA.getMemberships().size());
      r.rs.stop();
    } catch (Exception e) {
      T.e(e);
    }
  } // public void testCompositeIsDeletedWhenGroupIsDeleted()

  public void testFailCanReadFieldInvalidField() {
    LOG.info("testFailCanReadFieldInvalidField");
    try {
      R       r     = R.populateRegistry(1, 1, 0);
      Group   a     = r.getGroup("a", "a");
  
      try {
        a.canReadField(FieldFinder.find(Field.FIELD_NAME_STEMMERS, true));
        Assert.fail("SchemaException not thrown");
      }
      catch (SchemaException eS) {
        T.string("SchemaException", E.FIELD_INVALID_TYPE + "naming", eS.getMessage());
      }
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanReadFieldInvalidField()

  public void testFailCanReadFieldNullField() {
    LOG.info("testFailCanReadFieldNullField");
    try {
      R       r     = R.populateRegistry(1, 1, 0);
      Group   a     = r.getGroup("a", "a");
  
      try {
        a.canReadField(null);
        Assert.fail("IllegalArgumentException not thrown");
      }
      catch (IllegalArgumentException eIA) {
        T.string( "IllegalArgumentException", "field: null value", eIA.getMessage() );
      }
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanReadFieldNullField()

  public void testFailCanReadFieldValidSubjectInvalidField() {
    LOG.info("testFailCanReadFieldValidSubjectInvalidField");
    try {
      R       r     = R.populateRegistry(1, 1, 0);
      Group   a     = r.getGroup("a", "a");
  
      try {
        a.canReadField(r.rs.getSubject(), FieldFinder.find(Field.FIELD_NAME_STEMMERS, true));
        Assert.fail("SchemaException not thrown");
      }
      catch (SchemaException eS) {
        T.string("SchemaException", E.FIELD_INVALID_TYPE + "naming", eS.getMessage());
      }
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanReadFieldValidSubjectInvalidField()

  public void testFailCanReadFieldValidSubjectNullField() {
    LOG.info("testFailCanReadFieldValidSubjectNullField");
    try {
      R       r     = R.populateRegistry(1, 1, 0);
      Group   a     = r.getGroup("a", "a");
  
      try {
        a.canReadField(r.rs.getSubject(), null);
        Assert.fail("IllegalArgumentException not thrown");
      }
      catch (IllegalArgumentException eIA) {
        T.string( "IllegalArgumentException", "field: null value", eIA.getMessage() );
      }
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanReadFieldValidSubjectNullField()

  public void testFailCanWriteFieldNullSubjectInvalidField() {
    LOG.info("testFailCanWriteFieldNullSubjectInvalidField");
    try {
      R       r     = R.populateRegistry(1, 1, 0);
      Group   a     = r.getGroup("a", "a");
  
      try {
        a.canWriteField(null, FieldFinder.find(Field.FIELD_NAME_STEMMERS, true));
        Assert.fail("IllegalArgumentException not thrown");
      }
      catch (IllegalArgumentException eIA) {
        T.string( "IllegalArgumentException", "subject: null value", eIA.getMessage() );
      }
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanWriteFieldNullSubjectInvalidField()

  public void testFailCanWriteFieldNullSubjectNullField() {
    LOG.info("testFailCanWriteFieldNullSubjectNullField");
    try {
      R       r     = R.populateRegistry(1, 1, 0);
      Group   a     = r.getGroup("a", "a");
  
      try {
        a.canWriteField(null, null);
        Assert.fail("IllegalArgumentException not thrown");
      }
      catch (IllegalArgumentException eIA) {
        T.string( "IllegalArgumentException", "subject: null value", eIA.getMessage() );
      }
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanWriteFieldNullSubjectNullField()

  public void testFailCanWriteFieldNullSubjectValidField() {
    LOG.info("testFailCanWriteFieldNullSubjectValidField");
    try {
      R       r     = R.populateRegistry(1, 1, 0);
      Group   a     = r.getGroup("a", "a");
      
      GrouperSession grouperSession = GrouperSession.start( SubjectFinder.findRootSubject() );
  
      GroupType groupType = GroupType.createType(grouperSession, "theGroupType", false); 
      groupType.addList(grouperSession, "theList1", 
            AccessPrivilege.READ, AccessPrivilege.ADMIN);
      a.addType(groupType, false);
  
      grouperSession.stop();
      
      try {
        a.canWriteField(null, FieldFinder.find("theList1", true));
        Assert.fail("IllegalArgumentException not thrown");
      }
      catch (IllegalArgumentException eIA) {
        T.string( "IllegalArgumentException", "subject: null value", eIA.getMessage() );
      }
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanWriteFieldNullSubjectValidField()

  public void testFailCanWriteFieldValidFieldNotRoot() {
    LOG.info("testFailCanWriteFieldValidFieldNotRoot");
    try {
      R       r     = R.populateRegistry(1, 1, 1);
      Group   a     = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");
  
      GrouperSession s = GrouperSession.start(subjA);
      Assert.assertFalse("cannot write", a.canWriteField(FieldFinder.find(Field.FIELD_NAME_ADMINS, true)));
      s.stop();
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanWriteFieldValidFieldNotRoot()

  public void testFailCanWriteFieldValidSubjectValidFieldNotRoot() {
    LOG.info("testFailCanWriteFieldValidSubjectValidFieldNotRoot");
    try {
      R       r     = R.populateRegistry(1, 1, 1);
      Group   a     = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");
  
      GrouperSession s = GrouperSession.start(subjA);
      Assert.assertFalse(
        "cannot write", 
        a.canWriteField(subjA, FieldFinder.find(Field.FIELD_NAME_ADMINS, true))
      );
      s.stop();
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanWriteFieldValidSubjectValidFieldNotRoot()

  public void testFailToGetViaGroupWhenComposite() {
    LOG.info("testFailToGetViaGroupWhenComposite");
    try {
      R           r     = R.populateRegistry(1, 3, 1);
      Group       gA    = r.getGroup("a", "a");
      Group       gB    = r.getGroup("a", "b");
      Group       gC    = r.getGroup("a", "c");
      Subject     subjA = r.getSubject("a");
      gB.addMember(subjA);
      gA.addCompositeMember(CompositeType.UNION, gB, gC);
      Membership  ms    = MembershipFinder.findCompositeMembership(r.rs, gA, subjA, true);  
      // Fail
      try {
        ms.getViaGroup();
        Assert.fail("FAIL: got via group");
      }
      catch (GroupNotFoundException eGNF) {
        Assert.assertTrue("OK: failed to get !group via", true);
      }
      // Pass 
      try {
        Composite o = ms.getViaComposite();
        Assert.assertTrue("via is composite", o instanceof Composite);
      }
      catch (CompositeNotFoundException eONF) {
        Assert.fail("FAIL: did not get via");
      }
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailToGetViaGroupWhenComposite()

  public void testGetTypesAndRemovableTypesDefault() {
    LOG.info("testGetTypesAndRemovableTypesDefault");
    try {
      R       r   = R.populateRegistry(1, 1, 0);
      Group   gA  = r.getGroup("a", "a");
      T.amount("types", 0, gA.getTypes().size());
      T.amount("removable types", 0, gA.getRemovableTypes().size());
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testGetTypesAndRemovableTypesDefault()

  public void testGetTypesAndRemovableTypesWithCustomType() {
    LOG.info("testGetTypesAndRemovableTypesWithCustomType");
    try {
      R         r       = R.populateRegistry(1, 1, 0);
      GroupType custom  = GroupType.createType(r.rs, "custom");
      Group     gA      = r.getGroup("a", "a");
      gA.addType(custom);
      T.amount("types", 1, gA.getTypesDb().size());
      T.amount("removable types", 1, gA.getRemovableTypes().size());
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testGetTypesAndRemovableTypesWithCustomType()

  public void testGetTypesAndRemovableTypesWithCustomTypeAsNonRootSubject() {
    LOG.info("testGetTypesAndRemovableTypesWithCustomTypeAsNonRootSubject");
    try {
      R         r       = R.populateRegistry(1, 1, 1);
      GroupType custom  = GroupType.createType(r.rs, "custom");
      Group     gA      = r.getGroup("a", "a");
      gA.addType(custom);
      GrouperSession.start( r.getSubject("a") );
      T.amount("types", 1, gA.getTypes(false).size());
      T.amount("removable types", 0, gA.getRemovableTypes().size());
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testGetTypesAndRemovableTypesWithCustomTypeAsNonRootSubject()

  public void testGetTypesAndRemovableTypesWithCustomTypeAsNonRootSubjectWithADMIN() {
    LOG.info("testGetTypesAndRemovableTypesWithCustomTypeAsNonRootSubjectWithADMIN");
    try {
      R         r       = R.populateRegistry(1, 1, 1);
      GroupType custom  = GroupType.createType(r.rs, "custom");
      Group     gA      = r.getGroup("a", "a");
      gA.addType(custom);
      Subject   subjA   = r.getSubject("a");
      gA.grantPriv(subjA, AccessPrivilege.ADMIN);
      custom.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(subjA, AttributeDefPrivilege.ATTR_ADMIN, true);
      GrouperSession.start( subjA );
      T.amount("types", 1, gA.getTypes(false).size());
      T.amount("removable types", 1, gA.getRemovableTypes().size());
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testGetTypesAndRemovableTypesWithCustomTypeAsNonRootSubjectWithADMIN()

  public void testGroupDelete() {
    LOG.info("testGroupDelete");
    try {
      R     r = R.populateRegistry(1, 1, 0);
      Group a = r.getGroup("a", "a");
      a.delete();
      Assert.assertTrue("deleted group", true);
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testGroupDelete()

  public void testGroupDeleteWhenHasMemberViaTwoPaths() {
    LOG.info("testGroupDeleteWhenHasMemberViaTwoPaths");
    try {
      R       r     = R.populateRegistry(1, 4, 1);
      Group   a     = r.getGroup("a", "a");
      Subject aSubj = a.toSubject();
      Group   b     = r.getGroup("a", "b");
      Subject bSubj = b.toSubject();
      Group   c     = r.getGroup("a", "c");
      Subject cSubj = c.toSubject();
      Group   d     = r.getGroup("a", "d");
      Subject subjA = r.getSubject("a");
  
      a.addMember(subjA);
      b.addMember(aSubj);
      c.addMember(aSubj);
      d.addMember(bSubj);
      d.addMember(cSubj);
      T.amount("a has 1 memberships", 1, a.getMemberships().size());
      T.amount("b has 2 memberships", 2, b.getMemberships().size());
      T.amount("c has 2 memberships", 2, c.getMemberships().size());
      T.amount("d has 6 memberships", 6, d.getMemberships().size());
  
      a.delete();
      Assert.assertTrue("group deleted", true);
  
      T.amount("b now has 0 memberships", 0, b.getMemberships().size());
      T.amount("c now has 0 memberships", 0, c.getMemberships().size());
      T.amount("d now has 2 memberships", 2, d.getMemberships().size());
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testGroupDeleteWhenHasMemberViaTwoPaths()

  public void testGroupDeleteWhenMemberAndHasMembers() {
    LOG.info("testGroupDeleteWhenMemberAndHasMembers");
    try {
      R       r     = R.populateRegistry(1, 2, 1);
      Group   a     = r.getGroup("a", "a");
      Subject aSubj = a.toSubject();
      Group   b     = r.getGroup("a", "b");
      Subject subjA = r.getSubject("a");
    
      a.addMember(subjA);
      b.addMember(aSubj);
  
      a.delete();
      Assert.assertTrue("group deleted", true);
      T.amount("no more members", 0, b.getMembers().size());
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testGroupDeleteWhenMemberAndHasMembers()

  public void testRevokeDefaultPrivilege() {
    LOG.info("testRevokeDefaultPrivilege");
    try {
      R         r   = R.populateRegistry(1, 1, 0);
      Group     gA  = r.getGroup("a", "a");
      Subject   all = SubjectFinder.findAllSubject();
      Assert.assertTrue("ALL has VIEW", gA.hasView(all));
      Assert.assertTrue("ALL has READ", gA.hasRead(all));
      gA.revokePriv(all, AccessPrivilege.VIEW);
      gA.revokePriv(all, AccessPrivilege.READ);
      Assert.assertFalse("ALL !has VIEW", gA.hasView(all));
      Assert.assertFalse("ALL !has READ", gA.hasRead(all));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testRevokeDefaultPrivilege()

  public void testRevokeDefaultPrivilegeForEntireGroup() {
    LOG.info("testRevokeDefaultPrivilegeForEntireGroup");
    try {
      R         r   = R.populateRegistry(1, 1, 0);
      Group     gA  = r.getGroup("a", "a");
      Subject   all = SubjectFinder.findAllSubject();
      Assert.assertTrue("ALL has VIEW", gA.hasView(all));
      Assert.assertTrue("ALL has READ", gA.hasRead(all));
      gA.revokePriv(AccessPrivilege.VIEW);
      gA.revokePriv(AccessPrivilege.READ);
      Assert.assertFalse("ALL !has VIEW", gA.hasView(all));
      Assert.assertFalse("ALL !has READ", gA.hasRead(all));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testRevokeDefaultPrivilegeForEntireGroup()

  public void testSetAttributeIsPersistedAcrossSessions() {
    LOG.info("testSetAttributeIsPersistedAcrossSessions");
    try {
      R       r     = R.populateRegistry(1, 1, 0);
      Group   gA    = r.getGroup("a", "a");
      String  de    = "new display extension";
      String  dn    = gA.getParentStem().getDisplayName() + ":" + de;
      String  uuid  = gA.getUuid();
      gA.setDisplayExtension(de);
      gA.store();
      assertTrue( "group has new de", gA.getDisplayExtension().equals(de) );
      assertTrue( "group has new dn", gA.getDisplayName().equals(dn) );
      r.rs.stop();
      GrouperSession s = GrouperSession.start( SubjectFinder.findRootSubject() );
      Group g = GroupFinder.findByUuid(s, uuid, true);
      assertTrue( "group still has new de", g.getDisplayExtension().equals(de) );
      assertTrue( "group still has new dn", g.getDisplayName().equals(dn) );
      s.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testSetAttributeIsPersistedAcrossSessions()

  public void testThrowIPNotGModifyException() {
    LOG.info("testThrowIPNotGModifyException");
    try {
      R         r       = R.populateRegistry(1, 1, 1);
      Group     gA      = r.getGroup("a", "a");
      Subject   subjA   = r.getSubject("a");
  
      GrouperSession grouperSession = GrouperSession.start( SubjectFinder.findRootSubject() );
  
      GroupType groupType = GroupType.createType(grouperSession, "theGroupType", false); 
      groupType.addAttribute(grouperSession, "theAttribute1", 
            false);
      gA.addType(groupType, false);
      gA.setAttribute("theAttribute1", "whatever");
  
      
      GrouperSession  s = GrouperSession.start(subjA);
      Group           a = GroupFinder.findByName(s, gA.getName(), true);
      try {
        a.setAttribute("theAttribute1", "new value");

        Assert.fail("FAIL: set theAttribute1 w/out priv");
      }
      catch (InsufficientPrivilegeException eIP) {
        Assert.assertTrue("OK: threw right exception type", true);
      }
      s.stop();
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testThrowIPNotGModifyException()

  public void testThrowIPNotGModifyExceptionWhenDeleting() {
    LOG.info("testThrowIPNotGModifyExceptionWhenDeleting");
    try {
      R         r       = R.populateRegistry(1, 1, 1);
      Group     gA      = r.getGroup("a", "a");
      
      gA.setDescription(gA.getDisplayName());
  
      GrouperSession grouperSession = GrouperSession.start( SubjectFinder.findRootSubject() );
  
      GroupType groupType = GroupType.createType(grouperSession, "theGroupType", false); 
      groupType.addAttribute(grouperSession, "theAttribute1", 
            false);
      gA.addType(groupType, false);
      gA.setAttribute("theAttribute1", "whatever");
  
      Subject   subjA   = r.getSubject("a");
  
      GrouperSession  s = GrouperSession.start(subjA);
      Group           a = GroupFinder.findByName(s, gA.getName(), true);
      try {
        a.deleteAttribute("theAttribute1");
        Assert.fail("FAIL: deleted description w/out priv");
      }
      catch (InsufficientPrivilegeException eIP) {
        Assert.assertTrue("OK: threw right exception type", true);
      }
      s.stop();
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testThrowIPNotGModifyExceptionWhenDeleting()

  public void testFailCanReadFieldValidFieldNotRoot() {
    LOG.info("testFailCanReadFieldValidFieldNotRoot");
    try {
      R       r     = R.populateRegistry(1, 1, 1);
      Group   a     = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");
  
      GrouperSession s = GrouperSession.start(subjA);
      Assert.assertFalse("cannot read", a.canReadField(FieldFinder.find(Field.FIELD_NAME_ADMINS, true)));
      s.stop();
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanReadFieldValidFieldNotRoot()

  /**
   * perf problem
   */
  public static void runPerfProblem() throws Exception {
    GrouperSession rootSession = null;
    Stem rootStem = null;
    rootSession = SessionHelper.getRootSession();
    rootStem = StemFinder.findRootStem(rootSession); 
    for (int i=0;i<100;i++) {
      try {
        SubjectFinder.findById("GrouperSystem"+i, true);
      } catch (SubjectNotFoundException e) {
        
      }
    }
    
    GrouperQuery gq =
      GrouperQuery.createQuery(rootSession, new GroupAttributeFilter("name",
      "i2:b:a", rootStem));
    Set queryGroups = gq.getGroups();
    runPerfProblemLogic(rootSession, rootStem);
  }

  /**
   * runt he perf problem.  You should have the DB setup with LoadData.
   * @throws Exception
   */
  public static void runPerfProblem2() throws Exception {
    
    GrouperSession session = null;
    Stem rootStem = null;
    session = SessionHelper.getRootSession();
    rootStem = StemFinder.findRootStem(session);
  
    String monitorLabel = "runPerfProblem2Helper";
  
    //init everything
    runPerfProblem2Helper(session, rootStem, false, monitorLabel);
    
    for (int i=0;i<10;i++) {
      runPerfProblem2Helper(session, rootStem, true, monitorLabel);
    }
    
    //print timer report
    @SuppressWarnings("unused")
    Monitor monitor = MonitorFactory.getMonitor(monitorLabel, "ms.");
    
  }

  /**
   * run the logic of the test
   * @param printResults
   */
  private static void runPerfProblem2Helper(GrouperSession session, Stem rootStem, 
      boolean timeResults, String monitorLabel) throws Exception {
    Monitor mon = null;
    if (timeResults) {
      mon = MonitorFactory.start(monitorLabel);
    }
    GrouperQuery gq = GrouperQuery.createQuery(session, 
        new GroupAttributeFilter("name", "SUBJECT1", rootStem));
    Set queryGroups = gq.getGroups();
    if (timeResults) {
      mon.stop();
    }
  }

  /**
     * run perf problem logic
     * @param rootSession
     * @param rootStem
     * @throws QueryException
     */
    private static void runPerfProblemLogic(final GrouperSession rootSession, final Stem rootStem)
        throws Exception {
      long now = System.currentTimeMillis();
      for (int i=0;i<1000;i++) {
  //    HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_NEW,
  //        new HibernateHandler() {
  //
  //          public Object callback(HibernateSession hibernateSession)
  //              throws GrouperDAOException {
                try {
                  GrouperQuery gq =
                    GrouperQuery.createQuery(rootSession, new GroupAttributeFilter("name",
                    "i2:b:a", rootStem));
                  Set queryGroups = gq.getGroups();
                } catch (Exception e) {
                  throw new RuntimeException(e);
                }
  //            return null;
  //          }
  //      
  //    });
      }
      System.out.println("Took: " + (System.currentTimeMillis() - now) + "ms");
    }

  /**
   * run multiple logic together
   * @param grouperSession
   * @param groupName
   * @param groupName2
   * @param displayExtension
   * @param groupDescription
   */
  public void runLogic(GrouperSession grouperSession, String groupName,
      String groupName2, String displayExtension, String groupDescription) {
    try {
  
      //insert a group
      Group.saveGroup(grouperSession, null,null,groupName, displayExtension, groupDescription, 
          SaveMode.INSERT, false);
  
      //insert another group
      Group.saveGroup(grouperSession,  null,null, groupName2,displayExtension, groupDescription, 
          SaveMode.INSERT, false);
    } catch (StemNotFoundException e) {
      throw new RuntimeException("Stem wasnt found", e);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  
  }

  /**
   * test
   */
  public void testAddGroupAsMemberAndThenDeleteAsMember() {
    LOG.info("testAddGroupAsMemberAndThenDeleteAsMember");
    try {
      R r = R.populateRegistry(1, 2, 0);
      Group a = r.getGroup("a", "a");
      Group b = r.getGroup("a", "b");
      Subject bSubj = b.toSubject();
      Assert.assertFalse("a !has b", a.hasMember(bSubj));
      a.addMember(bSubj);
      Assert.assertTrue("a now has b", a.hasMember(bSubj));
      a.deleteMember(bSubj);
      Assert.assertFalse("a no longer has b", a.hasMember(bSubj));
      r.rs.stop();
    } catch (Exception e) {
      T.e(e);
    }
  } // public void testAddGroupAsMemberAndThenDeleteAsMember()

  /**
   * 
   * @throws Exception 
   */
  public void testCompositeMemberRead() throws Exception {
    R.populateRegistry(1, 3, 1);
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group1 = GroupFinder.findByName(grouperSession, "i2:a:a", true);
    Group group2 = GroupFinder.findByName(grouperSession, "i2:a:b", true);
    Group group3 = GroupFinder.findByName(grouperSession, "i2:a:c", true);
    
    Subject subject = SubjectFinder.findById("a", true);
    
    group1.grantPriv(subject, AccessPrivilege.ADMIN);
    group2.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    group2.grantPriv(subject, AccessPrivilege.VIEW, false);
    group3.grantPriv(subject, AccessPrivilege.VIEW, false);
    group3.grantPriv(subject, AccessPrivilege.READ, false);
  
    grouperSession.stop();
    grouperSession = GrouperSession.start(subject);
    try {
      group1.addCompositeMember(CompositeType.UNION,group2, group3);
      fail("Shouldnt be able to add this member without READ priv");
    } catch (InsufficientPrivilegeException e) {
      //this is ok
    }
    grouperSession.stop();
    grouperSession = GrouperSession.startRootSession();
    group2.grantPriv(subject, AccessPrivilege.READ);
    grouperSession.stop();
    grouperSession = GrouperSession.start(subject);
    
    //this should work now with read priv
    group1.addCompositeMember(CompositeType.UNION,group2, group3);
  }

  /**
   * @throws Exception 
   * 
   */
  public void testMemberRead() throws Exception {
    R.populateRegistry(1, 2, 1);
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group1 = GroupFinder.findByName(grouperSession, "i2:a:a", true);
    Group group2 = GroupFinder.findByName(grouperSession, "i2:a:b", true);
    
    Stem stem = StemFinder.findByName(grouperSession, "i2", true);
    
    
    Subject subject = SubjectFinder.findById("a", true);
    
    group1.grantPriv(subject, AccessPrivilege.ADMIN);
    group2.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    group2.grantPriv(subject, AccessPrivilege.VIEW, false);
    
    stem.grantPriv(subject, NamingPrivilege.CREATE);
    stem.grantPriv(subject, NamingPrivilege.STEM);
    
    grouperSession.stop();
    grouperSession = GrouperSession.start(subject);
    try {
      group1.addMember(SubjectFinder.findById(group2.getUuid(), true));
      fail("Shouldnt be able to add this member without READ priv");
    } catch (Exception e) {
      //this is ok
    }
    try {
      group1.grantPriv(SubjectFinder.findById(group2.getUuid(), true), AccessPrivilege.VIEW);
      fail("Shouldnt be able to grant this member without READ priv");
    } catch (GrantPrivilegeException e) {
      //this is ok
    }
    try {
      stem.grantPriv(SubjectFinder.findById(group2.getUuid(), true), NamingPrivilege.CREATE);
      fail("Shouldnt be able to grant this member without READ priv");
    } catch (GrantPrivilegeException e) {
      //this is ok
    }
    grouperSession.stop();
    grouperSession = GrouperSession.startRootSession();
    group2.grantPriv(subject, AccessPrivilege.READ);
    grouperSession.stop();
    grouperSession = GrouperSession.start(subject);
    
    //this should work now with read priv
    group1.addMember(SubjectFinder.findById(group2.getUuid(), true));
    group1.grantPriv(SubjectFinder.findById(group2.getUuid(), true), AccessPrivilege.VIEW);
    stem.grantPriv(SubjectFinder.findById(group2.getUuid(), true), NamingPrivilege.CREATE);
  }

  /**
   * test
   * @throws Exception if problem
   */
  public void testStaticSaveGroup() throws Exception {
  
    R.populateRegistry(1, 2, 0);
  
    String displayExtension = "testing123 display";
    GrouperSession rootSession = SessionHelper.getRootSession();
    String groupDescription = "description";
    try {
      String groupNameNotExist = "whatever:whatever:testing123";
  
      GrouperTest.deleteGroupIfExists(rootSession, groupNameNotExist);
  
      Group.saveGroup(rootSession, groupNameNotExist,null, groupNameNotExist, displayExtension, groupDescription, 
          SaveMode.UPDATE, false);
      fail("this should fail, since stem doesnt exist");
    } catch (StemNotFoundException e) {
      //good, caught an exception
      //e.printStackTrace();
    }
  
  
    //////////////////////////////////
    //this should insert
    String groupName = "i2:a:testing123";
    GrouperTest.deleteGroupIfExists(rootSession, groupName);
    Group createdGroup = Group.saveGroup(rootSession, null, null, groupName, displayExtension,
        groupDescription, 
        SaveMode.INSERT, false);
  
    //now retrieve
    Group foundGroup = GroupFinder.findByName(rootSession, groupName, true);
  
    assertEquals(groupName, createdGroup.getName());
    assertEquals(groupName, foundGroup.getName());
  
    assertEquals(displayExtension, createdGroup.getDisplayExtension());
    assertEquals(displayExtension, foundGroup.getDisplayExtension());
  
    assertEquals(groupDescription, createdGroup.getDescription());
    assertEquals(groupDescription, foundGroup.getDescription());
  
    ///////////////////////////////////
    //this should update by uuid
    createdGroup = Group.saveGroup(rootSession, groupName,createdGroup.getUuid(), groupName, displayExtension,
        groupDescription + "1", 
        SaveMode.INSERT_OR_UPDATE, false);
    assertEquals("this should update by uuid", groupDescription + "1", createdGroup
        .getDescription());
  
    //this should update by name
    createdGroup = Group.saveGroup(rootSession, groupName, null, groupName,  displayExtension,
        groupDescription + "2", 
        SaveMode.UPDATE, false);
    assertEquals("this should update by name", groupDescription + "2", createdGroup
        .getDescription());
  
    /////////////////////////////////////
    //create a group that creates a bunch of stems
    String stemsNotExist = "whatever:heythere:another";
    String groupNameCreateStems = stemsNotExist + ":" + groupName;
    GrouperTest.deleteGroupIfExists(rootSession, groupNameCreateStems);
    GrouperTest.deleteAllStemsIfExists(rootSession, stemsNotExist);
    //lets also delete those stems
    createdGroup = Group.saveGroup(rootSession, groupNameCreateStems, null,groupNameCreateStems, 
        displayExtension, groupDescription, 
        SaveMode.INSERT_OR_UPDATE, true);
  
    assertEquals(groupDescription, createdGroup.getDescription());
  
    rootSession.stop();
  
  }

  /**
   * transaction test.  THIS WILL FAIL WITH HIBERNATE2!!!!!
   * @throws Exception if problem
   */
  public void testStaticSaveGroupTransactions() throws Exception {
    Properties  properties = GrouperHibernateConfig.retrieveConfig().properties();
    //doesnt work with sql server
    if (((String)properties.get("hibernate.connection.url")).contains(":sqlserver:")) {
      return;
    }
    //THIS WILL FAIL WITH HIBERNATE2!!!!!
    
    R.populateRegistry(2, 2, 0);
  
    final GrouperSession rootSession = SessionHelper.getRootSession();
    final String displayExtension = "testing123 display";
    final String groupDescription = "description";
  
    //######################################################
    //this should insert
    final String groupName = "i2:a:testing123";
    final String groupName2 = "i2:b:testing124";
  
    GrouperTest.deleteGroupIfExists(rootSession, groupName);
    GrouperTest.deleteGroupIfExists(rootSession, groupName2);
  
    //this should work
    GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READ_WRITE_NEW,
        new GrouperTransactionHandler() {
  
          public Object callback(GrouperTransaction grouperTransaction)
              throws GrouperDAOException {
  
            try {
              Group.saveGroup(rootSession, null, null, groupName,displayExtension, groupDescription, 
                  SaveMode.INSERT, false);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
  
            return null;
          }
  
        });
  
    //now retrieve
    Group foundGroup = GroupFinder.findByName(rootSession, groupName, true);
  
    assertEquals("Name should be there", groupName, foundGroup.getName());
  
    GrouperTest.deleteGroupIfExists(rootSession, groupName);
    GrouperTest.deleteGroupIfExists(rootSession, groupName2);
  
    //####################################################
    //this should work, same as above, two times is a charm.
    GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READ_WRITE_NEW,
        new GrouperTransactionHandler() {
  
          public Object callback(GrouperTransaction grouperTransaction)
              throws GrouperDAOException {
  
            try {
              Group.saveGroup(rootSession,null, null, groupName, displayExtension, groupDescription + "1", 
                  SaveMode.INSERT, false);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
  
            return null;
          }
  
        });
  
    //now retrieve
    foundGroup = GroupFinder.findByName(rootSession, groupName, true);
  
    assertEquals("Name should be there", groupName, foundGroup.getName());
    assertEquals("Description should be new", groupDescription + "1", foundGroup
        .getDescription());
  
    //##########################################
    //## test committable work in a readonly tx
    GrouperTest.deleteGroupIfExists(rootSession, groupName);
    GrouperTest.deleteGroupIfExists(rootSession, groupName2);
  
    //this should fail if using transactions (will not work under hib2)
    //not good since READONLY txs should not commit
    try {
      GrouperTransaction.callbackGrouperTransaction(
          GrouperTransactionType.READONLY_OR_USE_EXISTING,
          new GrouperTransactionHandler() {
  
            public Object callback(GrouperTransaction grouperTransaction)
                throws GrouperDAOException {
  
              try {
                Group.saveGroup(rootSession,  groupName, null, 
                   groupName, displayExtension,groupDescription, 
                   SaveMode.INSERT, false);
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
  
              return null;
            }
  
          });
      fail("not good since READONLY txs should not commit");
    } catch (Exception e) {
      //good, it failed
      String exception = ExceptionUtils.getFullStackTrace(e).toLowerCase();
      assertTrue(
          "Should be readonly commitable problem, or problem with read/write tx inside readonly...: "
              + exception, exception.contains("read") && exception.contains("only"));
    }
  
    //now retrieve, shouldnt be there
    try {
      GroupFinder.findByName(rootSession, groupName, true);
      fail("Shouldnt find the group");
    } catch (GroupNotFoundException gnfe) {
      //all good
    }
  
    //###########################################
    //## test rolling back in the middle of a transaction by throwing exception
    GrouperTest.deleteGroupIfExists(rootSession, groupName);
    GrouperTest.deleteGroupIfExists(rootSession, groupName2);
  
    //this should fail if using transactions (will not work under hib2)
    //not good since READONLY txs should not commit
    String exception = "";
    try {
      GrouperTransaction.callbackGrouperTransaction(
          GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
          new GrouperTransactionHandler() {
  
            public Object callback(GrouperTransaction grouperTransaction)
                throws GrouperDAOException {
  
              try {
                Group.saveGroup(rootSession, null, null, groupName, displayExtension,
                    groupDescription, 
                    SaveMode.INSERT, false);
  
                Group.saveGroup(rootSession, null, null, groupName2, 
                    displayExtension, groupDescription, SaveMode.INSERT, false);
  
                throw new RuntimeException("Just to cause a rollback");
  
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            }
  
          });
    } catch (Exception e) {
      //good, it failed
      exception = ExceptionUtils.getFullStackTrace(e).toLowerCase();
    }
    assertTrue("Should be from inner exception, have that text in there: " + exception,
        StringUtils.contains(exception, "just to cause a rollback"));
  
    //see if group is there
    //now retrieve, shouldnt be there
    try {
      GroupFinder.findByName(rootSession, groupName, true);
      fail("Shouldnt find the group");
    } catch (GroupNotFoundException gnfe) {
      //all good
    }
    try {
      GroupFinder.findByName(rootSession, groupName2, true);
      fail("Shouldnt find the group");
    } catch (GroupNotFoundException gnfe) {
      //all good
    }
  
    //###########################################
    //## test rolling back in the middle of a transaction manually
    GrouperTest.deleteGroupIfExists(rootSession, groupName);
    GrouperTest.deleteGroupIfExists(rootSession, groupName2);
  
    //note the default is READ_WRITE_OR_USE_EXISTING
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
  
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
  
        try {
          Group.saveGroup(rootSession, null,null,groupName,displayExtension,  groupDescription, 
              SaveMode.INSERT, false);
  
          Group.saveGroup(rootSession, null,null, groupName2,displayExtension, groupDescription, 
              SaveMode.INSERT, false);
  
          assertTrue("Should be active since not rolled back", grouperTransaction
              .isTransactionActive());
          //this will be new, so it should work
          grouperTransaction.rollback(GrouperRollbackType.ROLLBACK_IF_NEW_TRANSACTION);
  
          assertFalse("Rolled back, should be inactive", grouperTransaction
              .isTransactionActive());
  
          return null;
        } catch (Exception e) {
          //note, if specific exceptions need to be handled, do that here
          throw new RuntimeException(e);
        }
      }
  
    });
  
    //see if group is there
    //now retrieve, shouldnt be there
    try {
      GroupFinder.findByName(rootSession, groupName, true);
      fail("Shouldnt find the group");
    } catch (GroupNotFoundException gnfe) {
      //all good
    }
    try {
      GroupFinder.findByName(rootSession, groupName2, true);
      fail("Shouldnt find the group");
    } catch (GroupNotFoundException gnfe) {
      //all good
    }
  
    //###########################################
    //## now do an autonomous transaction... inner commit should not affect outer rollback
    GrouperTest.deleteGroupIfExists(rootSession, groupName);
    GrouperTest.deleteGroupIfExists(rootSession, groupName2);
  
    //note the default is READ_WRITE_OR_USE_EXISTING
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
  
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
  
        try {
          Group.saveGroup(rootSession,null, null,groupName, displayExtension, groupDescription, 
              SaveMode.INSERT, false);
  
          //automous transaction
          GrouperTransaction.callbackGrouperTransaction(
              GrouperTransactionType.READ_WRITE_NEW, new GrouperTransactionHandler() {
  
                public Object callback(GrouperTransaction grouperTransaction)
                    throws GrouperDAOException {
  
                  try {
                    Group.saveGroup(rootSession, null, null, groupName2, displayExtension,
                        groupDescription, 
                        SaveMode.INSERT, false);
                  } catch (Exception e) {
                    throw new RuntimeException(e);
                  }
                  //this will exit normally and commit
                  return null;
                }
  
              });
  
          assertTrue("Should be active since not rolled back", grouperTransaction
              .isTransactionActive());
          //this will be new, so it should work
          grouperTransaction.rollback(GrouperRollbackType.ROLLBACK_NOW);
  
          assertFalse("Rolled back, should be inactive", grouperTransaction
              .isTransactionActive());
  
          return null;
        } catch (Exception e) {
          //note, if specific exceptions need to be handled, do that here
          throw new RuntimeException(e);
        }
      }
  
    });
  
    //see if group is there
    //now retrieve, shouldnt be there since out tx rolled back
    try {
      GroupFinder.findByName(rootSession, groupName, true);
      fail("Shouldnt find the group");
    } catch (GroupNotFoundException gnfe) {
      //all good
    }
    //this one should be there since inner tx committed
    //now retrieve
    Group foundGroup2 = GroupFinder.findByName(rootSession, groupName2, true);
  
    assertEquals("Name should be there", groupName2, foundGroup2.getName());
  
    rootSession.stop();
  
  }

  /**
   * test
   * @throws Exception if problem
   */
  public void testStaticSaveGroupWithDisplayNames() throws Exception {
  
    GrouperSession rootSession = SessionHelper.getRootSession();
  
    String groupNameNotExist = "whatever:whatever1:testing123";
    String groupNameSiblingNotExist = "whatever:whatever1:testing1234";
    String groupDisplayNameNotExist = "what ever:what ever1:testing 123";
    String groupDisplayNameChange = "what ever:what ever1234:testing 123";
    String groupDisplayNameSiblingNotExist = "what ever:what ever234:testing 1234";
    String groupDisplayNameSiblingResult = "what ever:what ever1:testing 1234";
  
    GrouperTest.deleteGroupIfExists(rootSession, groupNameNotExist);
  
    GroupSave groupSave = new GroupSave(rootSession);
  
    groupSave.assignGroupNameToEdit(groupNameNotExist);
    groupSave.assignName(groupNameNotExist).assignDisplayName(groupDisplayNameNotExist);
    groupSave.assignCreateParentStemsIfNotExist(true);
    
    //try with display extension which doesnt match
    groupSave.assignDisplayExtension("abc");
    
    Group group = null;
    
    try {
      group = groupSave.save();
      fail("Should have exception");
    } catch (Exception e) {
      //good
    }
    
    groupSave.assignDisplayExtension(null);
    group = groupSave.save();
    
    assertEquals(SaveResultType.INSERT, groupSave.getSaveResultType());
    assertEquals(groupNameNotExist, group.getName());
    assertEquals(groupDisplayNameNotExist, group.getDisplayName());
    
    //####################################
    //try again but change the name
    
    groupSave = new GroupSave(rootSession);
  
    groupSave.assignGroupNameToEdit(groupNameNotExist);
    groupSave.assignName(groupNameNotExist).assignDisplayName(groupDisplayNameChange);
    groupSave.assignCreateParentStemsIfNotExist(true);
    group = groupSave.save();
    
    assertEquals(SaveResultType.NO_CHANGE, groupSave.getSaveResultType());
    
    assertEquals(groupNameNotExist, group.getName());
  
    //should be original one
    assertEquals(groupDisplayNameNotExist, group.getDisplayName());
    
    //###################################
    //try again but do a new group in same stem, and the same name shouldnt change
    
    groupSave = new GroupSave(rootSession);
  
    groupSave.assignGroupNameToEdit(groupNameSiblingNotExist);
    groupSave.assignName(groupNameSiblingNotExist).assignDisplayName(groupDisplayNameSiblingNotExist);
    groupSave.assignCreateParentStemsIfNotExist(true);
    group = groupSave.save();
    
    assertEquals(SaveResultType.INSERT, groupSave.getSaveResultType());
    
    assertEquals(groupNameSiblingNotExist, group.getName());
  
    //should be original one
    assertEquals(groupDisplayNameSiblingResult, group.getDisplayName());
    
  }

  /**
     * show simple transaction
     * @throws RuntimeException if problem
     */
    public void testTransaction() {
      
  //    if (GrouperDAOFactory.getFactory() instanceof HibernateDAOFactory) {
  //      fail("This doesnt work with hib2 at the moment (only hib3 that I know of)...");
  //    }
      
      final GrouperSession rootSession = SessionHelper.getRootSession();
      final String displayExtension = "testing123 display";
      final String groupDescription = "description";
      final String groupName = "i2:a:testing123";
      final String groupName2 = "i2:b:testing124";
  
      try {
        R.populateRegistry(2, 2, 0);
    
        GrouperTest.deleteGroupIfExists(rootSession, groupName);
        GrouperTest.deleteGroupIfExists(rootSession, groupName2);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      //demonstrate passing back data with final array
      final Integer[] someInt = new Integer[1];
  
      //you can pass back one object from return
      @SuppressWarnings("unused")
      String anythingString = (String) GrouperTransaction
          .callbackGrouperTransaction(new GrouperTransactionHandler() {
  
            public Object callback(GrouperTransaction grouperTransaction)
                throws GrouperDAOException {
              
              //everything in here will run in one transaction, note how to access "this"
              TestGroup1.this.runLogic(rootSession, groupName, groupName2,
                  displayExtension, groupDescription);
              
              //pass data back from final array (if need more than just return value)
              someInt[0] = 5;
  
              //if return with no exception, then it will auto-commit.
              //if exception was thrown it will rollback
              //this can be controlled manually with grouperTransaction.commit()
              //but it would be rare to have to do that
              
              //pass data back from return value
              return "anything";
            }
  
          });
  
    }

}

