/**
 * Copyright 2012 Internet2
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

package edu.internet2.middleware.grouper;
import junit.framework.Assert;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberAddException;
import edu.internet2.middleware.grouper.exception.MemberDeleteException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestCompositeU.java,v 1.3 2009-03-21 13:35:50 mchyzer Exp $
 * @since   1.0
 */
public class TestCompositeU extends GrouperTest {

  private static final Log LOG = GrouperUtil.getLog(TestCompositeU.class);

  public static void main(String[] args) {
    TestRunner.run(TestCompositeU.class);
    //TestRunner.run(new TestCompositeU("testFailToDeleteMemberWhenHasComposite"));
  }
  
  public TestCompositeU(String name) {
    super(name);
  }

  public void testFailNotPrivilegedToAddCompositeMember() {
    LOG.info("testFailNotPrivilegedToAddCompositeMember");
    try {
      R               r   = R.populateRegistry(1, 3, 1);
      GrouperSession  nrs = GrouperSession.start( r.getSubject("a") );
      Group           a   = r.getGroup("a", "a");
      a.addCompositeMember(
        CompositeType.UNION, r.getGroup("a", "b"), r.getGroup("a", "c")
      );
      r.rs.stop();
      nrs.stop();
      Assert.fail("added composite without privilege to add composite");
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.assertTrue("OK: cannot add union without privileges", true);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailNotPrivilegedToAddCompositeMember()

  public void testAddUnionWithCompositeChildAndNoParents() {
    LOG.info("testAddUnionWithCompositeChildAndNoParents");
    try {
      R       r     = R.populateRegistry(1, 4, 2);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Group   d     = r.getGroup("a", "d");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
      b.addMember(subjA);
      c.addMember(subjB);
      a.addCompositeMember(CompositeType.UNION, b, c);
      d.addCompositeMember(CompositeType.UNION, a, b);
      T.amount("d members", 2, d.getMembers().size());
      Assert.assertTrue("d hasMember subjA", d.hasMember(subjA));
      Assert.assertTrue("d hasMember subjB", d.hasMember(subjB));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithCompositeChildAndNoParents()

  public void testAddUnionWithNoChildrenAndCompositeParent() {
    LOG.info("testAddUnionWithNoChildrenAndCompositeParent");
    try {
      R       r     = R.populateRegistry(1, 5, 0);
      // Feeder Groups
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      // Composite Group
      Group   d     = r.getGroup("a", "d");
      d.addCompositeMember(CompositeType.UNION, a, b);
      // Parent Composite Group
      Group   e     = r.getGroup("a", "e");
      e.addCompositeMember(CompositeType.UNION, c, d);
  
      // And test
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      Assert.assertFalse( "c !hasComposite" , c.hasComposite()  );
      Assert.assertTrue(  "d hasComposite"  , d.hasComposite()  );
      Assert.assertTrue(  "e hasComposite"  , e.hasComposite()  );
  
      Assert.assertTrue(  "a isComposite"   , a.isComposite()   );
      Assert.assertTrue(  "b isComposite"   , b.isComposite()   );
      Assert.assertTrue(  "c isComposite"   , c.isComposite()   );
      Assert.assertTrue(  "d isComposite"   , d.isComposite()   );
      Assert.assertFalse( "e !isComposite"  , e.isComposite()   );
  
      T.amount( "a members", 0, a.getMembers().size() );
      T.amount( "b members", 0, b.getMembers().size() );
      T.amount( "c members", 0, c.getMembers().size() );
      T.amount( "d members", 0, d.getMembers().size() );
      T.amount( "e members", 0, e.getMembers().size() );
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithNoChildrenAndCompositeParent()

  public void testAddUnionWithNoChildrenAndParent() {
    LOG.info("testAddUnionWithNoChildrenAndParent");
    try {
      R       r     = R.populateRegistry(1, 4, 0);
      // Feeder Groups
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      // Composite Group
      Group   c     = r.getGroup("a", "c");
      c.addCompositeMember(CompositeType.UNION, a, b);
      // Parent Group
      Group   d     = r.getGroup("a", "d");
      d.addMember(c.toSubject());
      // And test
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      Assert.assertTrue(  "c hasComposite"  , c.hasComposite()  );
      Assert.assertFalse( "d !hasComposite" , d.hasComposite()  );
  
      Assert.assertTrue(  "a isComposite"   , a.isComposite()   );
      Assert.assertTrue(  "b isComposite"   , b.isComposite()   );
      Assert.assertFalse( "c !isComposite"  , c.isComposite()   );
      Assert.assertFalse( "d !isComposite"  , d.isComposite()   );
  
      T.amount( "a members", 0, a.getMembers().size() );
      T.amount( "b members", 0, b.getMembers().size() );
      T.amount( "c members", 0, c.getMembers().size() );
      T.amount( "d members", 1, d.getMembers().size() );
      Assert.assertTrue(  "d has a:c",  d.hasMember(c.toSubject())  );
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithNoChildrenAndParent()

  public void testAddUnionWithOneChildAndNoParents() {
    LOG.info("testAddUnionWithOneChildAndNoParents");
    try {
      R       r     = R.populateRegistry(1, 3, 1);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Subject subjA = r.getSubject("a");
      Field   f     = Group.getDefaultList();
      b.addMember(subjA);
      a.addCompositeMember(CompositeType.UNION, b, c);
      Assert.assertTrue(  "a hasComposite"  , a.hasComposite()  );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      Assert.assertFalse( "c !hasComposite" , c.hasComposite()  );
      Assert.assertFalse( "a !isComposite"  , a.isComposite()   );
      Assert.assertTrue(  "b isComposite"   , b.isComposite()   );
      Assert.assertTrue(  "c isComposite"   , c.isComposite()   );
      T.amount("a members", 1, a.getMembers().size());
      Membership ms = MembershipFinder.findCompositeMembership(r.rs, a, subjA, true);
      Assert.assertNotNull( "imm ms"    , ms);
      Assert.assertEquals(  "ms group"  , a     , ms.getGroup()   );
      Assert.assertTrue(    "ms subj"   , SubjectHelper.eq(subjA, ms.getMember().getSubject())  );
      Assert.assertEquals(  "ms list"   , f     , ms.getList()    );
      T.amount( "ms depth", 0, ms.getDepth() );
      Composite via = ms.getViaComposite();
      Assert.assertNotNull( "ms via !null"      , via );
      Assert.assertTrue(    "ms via Composite"  , via instanceof Composite  );
      Composite     u   = (Composite) via;
      Assert.assertEquals(  "u owner" , u.getFactorOwnerUuid(),  a.getUuid() );
      Assert.assertEquals(  "u left"  , u.getLeftFactorUuid(),   b.getUuid() );
      Assert.assertEquals(  "u right" , u.getRightFactorUuid(),  c.getUuid() );
      Assert.assertEquals(  "u type"  , CompositeType.UNION , u.getType() );
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithOneChildAndNoParents()

  public void testAddUnionWithOneChildAndParent() {
    LOG.info("testAddUnionWithOneChildAndParent");
    try {
      R       r     = R.populateRegistry(1, 4, 1);
      // Feeder Groups
      Group   a     = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");
      a.addMember(subjA);
      Group   b     = r.getGroup("a", "b");
      // Composite Group
      Group   c     = r.getGroup("a", "c");
      c.addCompositeMember(CompositeType.UNION, a, b);
      // Parent Group
      Group   d     = r.getGroup("a", "d");
      Subject cSubj = c.toSubject();
      d.addMember(cSubj);
  
      // And test
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      Assert.assertTrue(  "c hasComposite"  , c.hasComposite()  );
  
      Assert.assertTrue(  "a isComposite"   , a.isComposite()   );
      Assert.assertTrue(  "b isComposite"   , b.isComposite()   );
      Assert.assertFalse( "c !isComposite"  , c.isComposite()   );
  
      T.amount( "a members", 1, a.getMembers().size() );
      Assert.assertTrue("a has subjA", a.hasMember(subjA));
      T.amount( "b members", 0, b.getMembers().size() );
      T.amount( "c members", 1, c.getMembers().size() );
      Assert.assertTrue("c has subjA", c.hasMember(subjA));
      T.amount( "d members", 2, d.getMembers().size() );
      Assert.assertTrue("d has cSubj", d.hasMember(cSubj));
      Assert.assertTrue("d has subjA", d.hasMember(subjA));
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithOneChildAndParent()

  public void testAddUnionWithTwoChildrenAndNoParents() {
    LOG.info("testAddUnionWithTwoChildrenAndNoParents");
    try {
      R       r     = R.populateRegistry(1, 3, 2);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
      b.addMember(subjA);
      c.addMember(subjB);
      a.addCompositeMember(CompositeType.UNION, b, c);
      T.amount("a members", 2, a.getMembers().size());
      Assert.assertTrue("a hasMember subjA", a.hasMember(subjA));
      Assert.assertTrue("a hasMember subjB", a.hasMember(subjB));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithTwoChildrenAndNoParents()

  public void testAddUnionWithTwoCompositeChildrenAndNoParents() {
    LOG.info("testAddUnionWithTwoCompositeChildrenAndNoParents");
    try {
      R       r     = R.populateRegistry(1, 7, 4);
      // Feeder Groups
      Subject subjA = r.getSubject("a");
      Group   a     = r.getGroup("a", "a");
      a.addMember(subjA);
      Subject subjB = r.getSubject("b");
      Group   b     = r.getGroup("a", "b");
      b.addMember(subjB);
      Subject subjC = r.getSubject("c");
      Group   c     = r.getGroup("a", "c");
      c.addMember(subjC);
      Subject subjD = r.getSubject("d");
      Group   d     = r.getGroup("a", "d");
      d.addMember(subjD);
      // Feeder Composite Groups
      Group   e     = r.getGroup("a", "e");
      e.addCompositeMember(CompositeType.UNION, a, b);
      Group   f     = r.getGroup("a", "f");
      f.addCompositeMember(CompositeType.UNION, c, d);
      // And our ultimate composite group
      Group   g     = r.getGroup("a", "g");
      g.addCompositeMember(CompositeType.UNION, e, f);
      // And test
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      Assert.assertFalse( "c !hasComposite" , c.hasComposite()  );
      Assert.assertFalse( "d !hasComposite" , d.hasComposite()  );
      Assert.assertTrue(  "e hasComposite"  , e.hasComposite()  );
      Assert.assertTrue(  "f hasComposite"  , f.hasComposite()  );
      Assert.assertTrue(  "g hasComposite"  , g.hasComposite()  );
  
      Assert.assertTrue(  "a isComposite"   , a.isComposite()   );
      Assert.assertTrue(  "b isComposite"   , b.isComposite()   );
      Assert.assertTrue(  "c isComposite"   , c.isComposite()   );
      Assert.assertTrue(  "d isComposite"   , d.isComposite()   );
      Assert.assertTrue(  "e isComposite"   , e.isComposite()   );
      Assert.assertTrue(  "f isComposite"   , f.isComposite()   );
      Assert.assertFalse( "g !isComposite"  , g.isComposite()   );
  
      T.amount( "a members", 1, a.getMembers().size() );
      Assert.assertTrue(  "a has subjA",  a.hasMember(subjA)  );
      T.amount( "b members", 1, b.getMembers().size() );
      Assert.assertTrue(  "b has subjB",  b.hasMember(subjB)  );
      T.amount( "c members", 1, c.getMembers().size() );
      Assert.assertTrue(  "c has subjC",  c.hasMember(subjC)  );
      T.amount( "d members", 1, d.getMembers().size() );
      Assert.assertTrue(  "d has subjD",  d.hasMember(subjD)  );
      T.amount( "e members", 2, e.getMembers().size() );
      Assert.assertTrue(  "e has subjA",  e.hasMember(subjA)  );
      Assert.assertTrue(  "e has subjB",  e.hasMember(subjB)  );
      T.amount( "f members", 2, f.getMembers().size() );
      Assert.assertTrue(  "f has subjC",  f.hasMember(subjC)  );
      Assert.assertTrue(  "f has subjD",  f.hasMember(subjD)  );
      T.amount( "g members", 4, g.getMembers().size() );
      Assert.assertTrue(  "g has subjA",  g.hasMember(subjA)  );
      Assert.assertTrue(  "g has subjB",  g.hasMember(subjB)  );
      Assert.assertTrue(  "g has subjC",  g.hasMember(subjC)  );
      Assert.assertTrue(  "g has subjD",  g.hasMember(subjD)  );
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithTwoCompositeChildrenAndNoParents()

  public void testDelUnionWithCompositeChildAndNoParents() {
    LOG.info("testDelUnionWithCompositeChildAndNoParents");
    try {
      R       r     = R.populateRegistry(1, 4, 2);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Group   d     = r.getGroup("a", "d");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
      b.addMember(subjA);
      c.addMember(subjB);
      a.addCompositeMember(CompositeType.UNION, b, c);
      d.addCompositeMember(CompositeType.UNION, a, b);
      d.deleteCompositeMember();
      T.amount("d members", 0, d.getMembers().size());
      Assert.assertFalse("d !hasMember subjA", d.hasMember(subjA));
      Assert.assertFalse("d !hasMember subjB", d.hasMember(subjB));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDelUnionWithCompositeChildAndNoParents()

  public void testDelUnionWithNoChildrenAndCompositeParent() {
    LOG.info("testDelUnionWithNoChildrenAndCompositeParent");
    try {
      R       r     = R.populateRegistry(1, 5, 0);
      // Feeder Groups
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      // Composite Group
      Group   d     = r.getGroup("a", "d");
      d.addCompositeMember(CompositeType.UNION, a, b);
      // Parent Composite Group
      Group   e     = r.getGroup("a", "e");
      e.addCompositeMember(CompositeType.UNION, c, d);
  
      d.deleteCompositeMember();
  
      // And test
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      Assert.assertFalse( "c !hasComposite" , c.hasComposite()  );
      Assert.assertFalse( "d !hasComposite" , d.hasComposite()  );
      Assert.assertTrue(  "e hasComposite"  , e.hasComposite()  );
  
      Assert.assertFalse( "a !isComposite"  , a.isComposite()   );
      Assert.assertFalse( "b !isComposite"  , b.isComposite()   );
      Assert.assertTrue(  "c isComposite"   , c.isComposite()   );
      Assert.assertTrue(  "d isComposite"   , d.isComposite()   );
      Assert.assertFalse( "e !isComposite"  , e.isComposite()   );
  
      T.amount( "a members", 0, a.getMembers().size() );
      T.amount( "b members", 0, b.getMembers().size() );
      T.amount( "c members", 0, c.getMembers().size() );
      T.amount( "d members", 0, d.getMembers().size() );
      T.amount( "e members", 0, e.getMembers().size() );
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDelUnionWithNoChildrenAndCompositeParent()

  public void testDelUnionWithNoChildrenAndNoParents() {
    LOG.info("testDelUnionWithNoChildrenAndNoParents");
    try {
      R     r = R.populateRegistry(1, 3, 0);
      Group a = r.getGroup("a", "a");
      Group b = r.getGroup("a", "b");
      Group c = r.getGroup("a", "c");
      a.addCompositeMember(CompositeType.UNION, b, c);
      a.deleteCompositeMember();
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      Assert.assertFalse( "c !hasComposite" , c.hasComposite()  );
      Assert.assertFalse( "a !isComposite"  , a.isComposite()   );
      Assert.assertFalse( "b !isComposite"  , b.isComposite()   );
      Assert.assertFalse( "c !isComposite"  , c.isComposite()   );
      T.amount("a members", 0, a.getMembers().size());
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDelUnionWithNoChildrenAndNoParents()

  public void testDelUnionWithNoChildrenAndParent() {
    LOG.info("testDelUnionWithNoChildrenAndParent");
    try {
      R       r     = R.populateRegistry(1, 4, 0);
      // Feeder Groups
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      // Composite Group
      Group   c     = r.getGroup("a", "c");
      c.addCompositeMember(CompositeType.UNION, a, b);
      // Parent Group
      Group   d     = r.getGroup("a", "d");
      d.addMember(c.toSubject());
      c.deleteCompositeMember();
  
      // And test
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      Assert.assertFalse( "c !hasComposite" , c.hasComposite()  );
      Assert.assertFalse( "d !hasComposite" , d.hasComposite()  );
  
      Assert.assertFalse( "a !isComposite"  , a.isComposite()   );
      Assert.assertFalse( "b !isComposite"  , b.isComposite()   );
      Assert.assertFalse( "c !isComposite"  , c.isComposite()   );
      Assert.assertFalse( "d !isComposite"  , d.isComposite()   );
  
      T.amount( "a members", 0, a.getMembers().size() );
      T.amount( "b members", 0, b.getMembers().size() );
      T.amount( "c members", 0, c.getMembers().size() );
      T.amount( "d members", 1, d.getMembers().size() );
      Assert.assertTrue(  "d has a:c",  d.hasMember(c.toSubject())  );
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDelUnionWithNoChildrenAndParent()

  public void testDelUnionWithOneChildAndNoParents() {
    LOG.info("testDelUnionWithOneChildAndNoParents");
    try {
      R       r     = R.populateRegistry(1, 3, 1);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Subject subjA = r.getSubject("a");
      b.addMember(subjA);
      a.addCompositeMember(CompositeType.UNION, b, c);
      a.deleteCompositeMember();
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      Assert.assertFalse( "c !hasComposite" , c.hasComposite()  );
      Assert.assertFalse( "a !!isComposite" , a.isComposite()   );
      Assert.assertFalse( "b !isComposite"  , b.isComposite()   );
      Assert.assertFalse( "c !isComposite"  , c.isComposite()   );
      T.amount("a members", 0, a.getMembers().size());
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDelUnionWithOneChildAndNoParents()

  public void testDelUnionWithTwoChildrenAndNoParents() {
    LOG.info("testDelUnionWithTwoChildrenAndNoParents");
    try {
      R       r     = R.populateRegistry(1, 3, 2);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
      b.addMember(subjA);
      c.addMember(subjB);
      a.addCompositeMember(CompositeType.UNION, b, c);
      a.deleteCompositeMember();
      T.amount("a members", 0, a.getMembers().size());
      Assert.assertFalse("a !hasMember subjA", a.hasMember(subjA));
      Assert.assertFalse("a !hasMember subjB", a.hasMember(subjB));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDelUnionWithTwoChildrenAndNoParents()

  public void testDelUnionWithTwoCompositeChildrenAndNoParents() {
    LOG.info("testDelUnionWithTwoCompositeChildrenAndNoParents");
    try {
      R       r     = R.populateRegistry(1, 7, 4);
      // Feeder Groups
      Subject subjA = r.getSubject("a");
      Group   a     = r.getGroup("a", "a");
      a.addMember(subjA);
      Subject subjB = r.getSubject("b");
      Group   b     = r.getGroup("a", "b");
      b.addMember(subjB);
      Subject subjC = r.getSubject("c");
      Group   c     = r.getGroup("a", "c");
      c.addMember(subjC);
      Subject subjD = r.getSubject("d");
      Group   d     = r.getGroup("a", "d");
      d.addMember(subjD);
      // Feeder Composite Groups
      Group   e     = r.getGroup("a", "e");
      e.addCompositeMember(CompositeType.UNION, a, b);
      Group   f     = r.getGroup("a", "f");
      f.addCompositeMember(CompositeType.UNION, c, d);
      // And our ultimate composite group
      Group   g     = r.getGroup("a", "g");
      g.addCompositeMember(CompositeType.UNION, e, f);
      g.deleteCompositeMember();
      // And test
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      Assert.assertFalse( "c !hasComposite" , c.hasComposite()  );
      Assert.assertFalse( "d !hasComposite" , d.hasComposite()  );
      Assert.assertTrue(  "e hasComposite"  , e.hasComposite()  );
      Assert.assertTrue(  "f hasComposite"  , f.hasComposite()  );
      Assert.assertFalse( "g !hasComposite" , g.hasComposite()  );
  
      Assert.assertTrue(  "a isComposite"   , a.isComposite()   );
      Assert.assertTrue(  "b isComposite"   , b.isComposite()   );
      Assert.assertTrue(  "c isComposite"   , c.isComposite()   );
      Assert.assertTrue(  "d isComposite"   , d.isComposite()   );
      Assert.assertFalse( "e !isComposite"  , e.isComposite()   );
      Assert.assertFalse( "f !isComposite"  , f.isComposite()   );
      Assert.assertFalse( "g !isComposite"  , g.isComposite()   );
  
      T.amount( "a members", 1, a.getMembers().size() );
      Assert.assertTrue(  "a has subjA",  a.hasMember(subjA)  );
      T.amount( "b members", 1, b.getMembers().size() );
      Assert.assertTrue(  "b has subjB",  b.hasMember(subjB)  );
      T.amount( "c members", 1, c.getMembers().size() );
      Assert.assertTrue(  "c has subjC",  c.hasMember(subjC)  );
      T.amount( "d members", 1, d.getMembers().size() );
      Assert.assertTrue(  "d has subjD",  d.hasMember(subjD)  );
      T.amount( "e members", 2, e.getMembers().size() );
      Assert.assertTrue(  "e has subjA",  e.hasMember(subjA)  );
      Assert.assertTrue(  "e has subjB",  e.hasMember(subjB)  );
      T.amount( "f members", 2, f.getMembers().size() );
      Assert.assertTrue(  "f has subjC",  f.hasMember(subjC)  );
      Assert.assertTrue(  "f has subjD",  f.hasMember(subjD)  );
      T.amount( "g members", 0, g.getMembers().size() );
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDelUnionWithTwoCompositeChildrenAndNoParents()

  public void testFailHasCompositeWhenNot() {
    LOG.info("testFailHasCompositeWhenNot");
    try {
      R     r = R.populateRegistry(1, 1, 0);
      Group a = r.getGroup("a", "a");
      Assert.assertFalse("hasComposite", a.hasComposite());
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailHasCompositeWhenNot()

  public void testFailNotPrivilegedToDeleteCompositeMember() {
    LOG.info("testFailNotPrivilegedToDeleteCompositeMember");
    try {
      R               r   = R.populateRegistry(1, 3, 1);
      GrouperSession  nrs = GrouperSession.start( r.getSubject("a") );
      Group           a   = r.getGroup("a", "a");
      a.addCompositeMember(
        CompositeType.UNION, r.getGroup("a", "b"), r.getGroup("a", "c")
      );
      a.deleteCompositeMember();
      r.rs.stop();
      nrs.stop();
      Assert.fail("deleted composite without privilege to delete composite");
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.assertTrue("OK: cannot del union without privileges", true);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailNotPrivilegedToDeleteCompositeMember()

  public void testAddUnionWithChildAndCompositeChildAndParent() {
    LOG.info("testAddUnionWithChildAndCompositeChildAndParent");
    try {
      R       r     = R.populateRegistry(1, 7, 3);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Group   d     = r.getGroup("a", "d");
      Group   e     = r.getGroup("a", "e");
      Subject eSubj = e.toSubject();
      Group   f     = r.getGroup("a", "f");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
      Subject subjC = r.getSubject("c");
  
      a.addMember(subjA);
      b.addMember(subjB);
      c.addMember(subjC);
      d.addCompositeMember(CompositeType.UNION, a, b);
      e.addCompositeMember(CompositeType.UNION, c, d);
      f.addMember(eSubj);
  
      Assert.assertTrue(  "a isComposite"   , a.isComposite()   );
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      T.amount("a members", 1, a.getMembers().size());
      Assert.assertTrue("a has subjA", a.hasMember(subjA));
  
      Assert.assertTrue(  "b isComposite"   , b.isComposite()   );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      T.amount("b members", 1, b.getMembers().size());
      Assert.assertTrue("b has subjB", b.hasMember(subjB));
  
      Assert.assertTrue(  "c isComposite"   , c.isComposite()   );
      Assert.assertFalse( "c !hasComposite" , c.hasComposite()  );
      T.amount("c members", 1, c.getMembers().size());
      Assert.assertTrue("c has subjC", c.hasMember(subjC));
  
      Assert.assertTrue(  "d isComposite"   , d.isComposite()   );
      Assert.assertTrue(  "d hasComposite"  , d.hasComposite()  );
      T.amount("d members", 2, d.getMembers().size());
      Assert.assertTrue("d has subjA", d.hasMember(subjA));
      Assert.assertTrue("d has subjB", d.hasMember(subjB));
  
      Assert.assertFalse( "e !isComposite"  , e.isComposite()   );
      Assert.assertTrue(  "e hasComposite"  , e.hasComposite()  );
      T.amount("e members", 3, e.getMembers().size());
      Assert.assertTrue("e has subjA", e.hasMember(subjA));
      Assert.assertTrue("e has subjB", e.hasMember(subjB));
      Assert.assertTrue("e has subjC", e.hasMember(subjC));
  
      Assert.assertFalse( "f !isComposite"  , f.isComposite()   );
      Assert.assertFalse( "f !hasComposite" , f.hasComposite()  );
      T.amount("f members", 4, f.getMembers().size());
      Assert.assertTrue("f has subjA", f.hasMember(subjA));
      Assert.assertTrue("f has subjB", f.hasMember(subjB));
      Assert.assertTrue("f has subjC", f.hasMember(subjC));
      Assert.assertTrue("f has eSubj", f.hasMember(eSubj));
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithChildAndCompositeChildAndParent()

  public void testAddUnionWithOneChildAndCompositeParent() {
    LOG.info("testAddUnionWithOneChildAndCompositeParent");
    try {
      R       r     = R.populateRegistry(1, 5, 1);
      // Feeder Groups
      Group   a     = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");
      a.addMember(subjA);
      Group   b     = r.getGroup("a", "b");
      // Composite Group
      Group   c     = r.getGroup("a", "c");
      c.addCompositeMember(CompositeType.UNION, a, b);
      // Parent Feeder Group
      Group   d     = r.getGroup("a", "d");
      // Parent Group
      Group   e     = r.getGroup("a", "e");
      e.addCompositeMember(CompositeType.UNION, c, d);
  
      // And test
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      Assert.assertTrue(  "c hasComposite"  , c.hasComposite()  );
      Assert.assertFalse( "d !hasComposite" , d.hasComposite()  );
      Assert.assertTrue(  "e hasComposite"  , e.hasComposite()  );
  
      Assert.assertTrue(  "a isComposite"   , a.isComposite()   );
      Assert.assertTrue(  "b isComposite"   , b.isComposite()   );
      Assert.assertTrue(  "c isComposite"   , c.isComposite()   );
      Assert.assertTrue(  "d isComposite"   , d.isComposite()   );
      Assert.assertFalse( "e !isComposite"  , e.isComposite()   );
  
      T.amount( "a members", 1, a.getMembers().size() );
      Assert.assertTrue("a has subjA", a.hasMember(subjA));
  
      T.amount( "b members", 0, b.getMembers().size() );
  
      T.amount( "c members", 1, c.getMembers().size() );
      Assert.assertTrue("c has subjA", c.hasMember(subjA));
  
      T.amount( "d members", 0, d.getMembers().size() );
  
      T.amount( "e members", 1, e.getMembers().size() );
      Assert.assertTrue("e has subjA", e.hasMember(subjA));
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithOneChildAndCompositeParent()

  public void testAddUnionWithOneCompositeChildAndCompositeParent() {
    LOG.info("testAddUnionWithOneCompositeChildAndCompositeParent");
    try {
      R       r     = R.populateRegistry(1, 7, 3);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Group   d     = r.getGroup("a", "d");
      Group   e     = r.getGroup("a", "e");
      Group   f     = r.getGroup("a", "f");
      Group   g     = r.getGroup("a", "g");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
      Subject subjC = r.getSubject("c");
  
      a.addMember(subjA);
      b.addMember(subjB);
      c.addCompositeMember(CompositeType.UNION, a, b);
      d.addMember(subjC);
      e.addCompositeMember(CompositeType.UNION, c, d);
      g.addCompositeMember(CompositeType.UNION, e, f);
  
      Assert.assertTrue(  "a isComposite"   , a.isComposite()   );
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      T.amount("a members", 1, a.getMembers().size());
      Assert.assertTrue("a has subjA", a.hasMember(subjA));
  
      Assert.assertTrue(  "b isComposite"   , b.isComposite()   );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      T.amount("b members", 1, b.getMembers().size());
      Assert.assertTrue("b has subjB", b.hasMember(subjB));
  
      Assert.assertTrue(  "c isComposite"   , c.isComposite()   );
      Assert.assertTrue(  "c hasComposite"  , c.hasComposite()  );
      T.amount("c members", 2, c.getMembers().size());
      Assert.assertTrue("c has subjA", c.hasMember(subjA));
      Assert.assertTrue("c has subjB", c.hasMember(subjB));
  
      Assert.assertTrue(  "d isComposite"   , d.isComposite()   );
      Assert.assertFalse( "d !hasComposite" , d.hasComposite()  );
      T.amount("d members", 1, d.getMembers().size());
      Assert.assertTrue("d has subjC", d.hasMember(subjC));
  
      Assert.assertTrue(  "e isComposite"   , e.isComposite()   );
      Assert.assertTrue(  "e hasComposite"  , e.hasComposite()  );
      T.amount("e members", 3, e.getMembers().size());
      Assert.assertTrue("e has subjA", e.hasMember(subjA));
      Assert.assertTrue("e has subjB", e.hasMember(subjB));
      Assert.assertTrue("e has subjC", e.hasMember(subjC));
  
      Assert.assertTrue(  "f isComposite"   , f.isComposite()   );
      Assert.assertFalse( "f !hasComposite" , f.hasComposite()  );
      T.amount("f members", 0, f.getMembers().size());
  
      Assert.assertFalse( "g !isComposite"  , g.isComposite()   );
      Assert.assertTrue(  "g hasComposite"  , g.hasComposite()  );
      T.amount("g members", 3, g.getMembers().size());
      Assert.assertTrue("g has subjA", g.hasMember(subjA));
      Assert.assertTrue("g has subjB", g.hasMember(subjB));
      Assert.assertTrue("g has subjC", g.hasMember(subjC));
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithOneCompositeChildAndCompositeParent()

  public void testAddUnionWithTwoChildrenAndCompositeParent() {
    LOG.info("testAddUnionWithTwoChildrenAndCompositeParent");
    try {
      R       r     = R.populateRegistry(1, 5, 2);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Group   d     = r.getGroup("a", "d");
      Group   e     = r.getGroup("a", "e");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
  
      a.addMember(subjA);
      b.addMember(subjB);
      c.addCompositeMember(CompositeType.UNION, a, b);
      e.addCompositeMember(CompositeType.UNION, c, d);
  
      Assert.assertTrue(  "a isComposite"   , a.isComposite()   );
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      T.amount("a members", 1, a.getMembers().size());
      Assert.assertTrue("a has subjA", a.hasMember(subjA));
  
      Assert.assertTrue(  "b isComposite"   , b.isComposite()   );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      T.amount("b members", 1, b.getMembers().size());
      Assert.assertTrue("b has subjB", b.hasMember(subjB));
  
      Assert.assertTrue(  "c isComposite"   , c.isComposite()   );
      Assert.assertTrue(  "c hasComposite"  , c.hasComposite()  );
      T.amount("c members", 2, c.getMembers().size());
      Assert.assertTrue("c has subjA", c.hasMember(subjA));
      Assert.assertTrue("c has subjB", c.hasMember(subjB));
  
      Assert.assertTrue(  "d isComposite"   , d.isComposite()   );
      Assert.assertFalse( "d !hasComposite" , d.hasComposite()  );
      T.amount("d members", 0, d.getMembers().size());
  
      Assert.assertFalse( "e !isComposite"  , e.isComposite()   );
      Assert.assertTrue(  "e hasComposite"  , e.hasComposite()  );
      T.amount("e members", 2, e.getMembers().size());
      Assert.assertTrue("e has subjA", e.hasMember(subjA));
      Assert.assertTrue("e has subjB", e.hasMember(subjB));
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithTwoChildrenAndCompositeParent()

  public void testAddUnionWithTwoChildrenAndParent() {
    LOG.info("testAddUnionWithTwoChildrenAndParent");
    try {
      R       r     = R.populateRegistry(1, 4, 2);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Subject cSubj = c.toSubject();
      Group   d     = r.getGroup("a", "d");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
  
      a.addMember(subjA);
      b.addMember(subjB);
      c.addCompositeMember(CompositeType.UNION, a, b);
      d.addMember(cSubj);
  
      T.amount("a members", 1, a.getMembers().size());
      Assert.assertTrue("a has subjA", a.hasMember(subjA));
  
      T.amount("b members", 1, b.getMembers().size());
      Assert.assertTrue("b has subjB", b.hasMember(subjB));
  
      T.amount("c members", 2, c.getMembers().size());
      Assert.assertTrue("c has subjA", c.hasMember(subjA));
      Assert.assertTrue("c has subjB", c.hasMember(subjB));
      T.amount("c comp members", 2, c.getCompositeMembers().size());
      T.amount("c comp mships", 2, c.getCompositeMemberships().size());
  
      T.amount("d members", 3, d.getMembers().size());
      Assert.assertTrue("d has cSubj", d.hasMember(cSubj));
      Assert.assertTrue("d has subjA", d.hasMember(subjA));
      Assert.assertTrue("d has subjB", d.hasMember(subjB));
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithTwoChildrenAndParent()

  public void testDelUnionWithOneChildAndCompositeParent() {
    LOG.info("testDelUnionWithOneChildAndCompositeParent");
    try {
      R       r     = R.populateRegistry(1, 5, 1);
      // Feeder Groups
      Group   a     = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");
      a.addMember(subjA);
      Group   b     = r.getGroup("a", "b");
      // Composite Group
      Group   c     = r.getGroup("a", "c");
      c.addCompositeMember(CompositeType.UNION, a, b);
      // Parent Feeder Group
      Group   d     = r.getGroup("a", "d");
      // Parent Group
      Group   e     = r.getGroup("a", "e");
      e.addCompositeMember(CompositeType.UNION, c, d);
  
      c.deleteCompositeMember();
      
      // And test
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      Assert.assertFalse( "c !hasComposite" , c.hasComposite()  );
      Assert.assertFalse( "d !hasComposite" , d.hasComposite()  );
      Assert.assertTrue(  "e hasComposite"  , e.hasComposite()  );
  
      Assert.assertFalse( "a !isComposite"  , a.isComposite()   );
      Assert.assertFalse( "b !isComposite"  , b.isComposite()   );
      Assert.assertTrue(  "c isComposite"   , c.isComposite()   );
      Assert.assertTrue(  "d isComposite"   , d.isComposite()   );
      Assert.assertFalse( "e !isComposite"  , e.isComposite()   );
  
      T.amount( "a members", 1, a.getMembers().size() );
      Assert.assertTrue("a has subjA", a.hasMember(subjA));
  
      T.amount( "b members", 0, b.getMembers().size() );
  
      T.amount( "c members", 0, c.getMembers().size() );
  
      T.amount( "d members", 0, d.getMembers().size() );
  
      T.amount( "e members", 0, e.getMembers().size() );
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDelUnionWithOneChildAndCompositeParent()

  public void testDelUnionWithOneChildAndParent() {
    LOG.info("testDelUnionWithOneChildAndParent");
    try {
      R       r     = R.populateRegistry(1, 4, 1);
      // Feeder Groups
      Group   a     = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");
      a.addMember(subjA);
      Group   b     = r.getGroup("a", "b");
      // Composite Group
      Group   c     = r.getGroup("a", "c");
      c.addCompositeMember(CompositeType.UNION, a, b);
      // Parent Group
      Group   d     = r.getGroup("a", "d");
      Subject cSubj = c.toSubject();
      d.addMember(cSubj);
  
      c.deleteCompositeMember();
  
      // And test
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      Assert.assertFalse( "c !hasComposite" , c.hasComposite()  );
  
      Assert.assertFalse( "a !isComposite"  , a.isComposite()   );
      Assert.assertFalse( "b !isComposite"  , b.isComposite()   );
      Assert.assertFalse( "c !isComposite"  , c.isComposite()   );
  
      T.amount( "a members", 1, a.getMembers().size() );
      Assert.assertTrue("a has subjA", a.hasMember(subjA));
      T.amount( "b members", 0, b.getMembers().size() );
      T.amount( "c members", 0, c.getMembers().size() );
      T.amount( "d members", 1, d.getMembers().size() );
      Assert.assertTrue("d has cSubj", d.hasMember(cSubj));
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDelUnionWithOneChildAndParent()

  public void testDelUnionWithOneCompositeChildAndCompositeParent() {
    LOG.info("testDelUnionWithOneCompositeChildAndCompositeParent");
    try {
      R       r     = R.populateRegistry(1, 7, 3);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Group   d     = r.getGroup("a", "d");
      Group   e     = r.getGroup("a", "e");
      Group   f     = r.getGroup("a", "f");
      Group   g     = r.getGroup("a", "g");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
      Subject subjC = r.getSubject("c");
  
      a.addMember(subjA);
      b.addMember(subjB);
      c.addCompositeMember(CompositeType.UNION, a, b);
      d.addMember(subjC);
      e.addCompositeMember(CompositeType.UNION, c, d);
      g.addCompositeMember(CompositeType.UNION, e, f);
      c.deleteCompositeMember();
  
      Assert.assertFalse( "a !isComposite"  , a.isComposite()   );
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      T.amount("a members", 1, a.getMembers().size());
      Assert.assertTrue("a has subjA", a.hasMember(subjA));
  
      Assert.assertFalse( "b !isComposite"  , b.isComposite()   );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      T.amount("b members", 1, b.getMembers().size());
      Assert.assertTrue("b has subjB", b.hasMember(subjB));
  
      Assert.assertTrue(  "c isComposite"   , c.isComposite()   );
      Assert.assertFalse( "c !hasComposite" , c.hasComposite()  );
      T.amount("c members", 0, c.getMembers().size());
  
      Assert.assertTrue(  "d isComposite"   , d.isComposite()   );
      Assert.assertFalse( "d !hasComposite" , d.hasComposite()  );
      T.amount("d members", 1, d.getMembers().size());
      Assert.assertTrue("d has subjC", d.hasMember(subjC));
  
      Assert.assertTrue(  "e isComposite"   , e.isComposite()   );
      Assert.assertTrue(  "e hasComposite"  , e.hasComposite()  );
      T.amount("e members", 1, e.getMembers().size());
      Assert.assertTrue("e has subjC", e.hasMember(subjC));
  
      Assert.assertTrue(  "f isComposite"   , f.isComposite()   );
      Assert.assertFalse( "f !hasComposite" , f.hasComposite()  );
      T.amount("f members", 0, f.getMembers().size());
  
      Assert.assertFalse( "g !isComposite"  , g.isComposite()   );
      Assert.assertTrue(  "g hasComposite"  , g.hasComposite()  );
      T.amount("g members", 1, g.getMembers().size());
      Assert.assertTrue("g has subjC", g.hasMember(subjC));
  
      r.rs.stop();
  
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDelUnionWithOneCompositeChildAndCompositeParent()

  public void testDelUnionWithTwoChildrenAndCompositeParent() {
    LOG.info("testDelUnionWithTwoChildrenAndCompositeParent");
    try {
      R       r     = R.populateRegistry(1, 5, 2);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Group   d     = r.getGroup("a", "d");
      Group   e     = r.getGroup("a", "e");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
  
      a.addMember(subjA);
      b.addMember(subjB);
      c.addCompositeMember(CompositeType.UNION, a, b);
      e.addCompositeMember(CompositeType.UNION, c, d);
      c.deleteCompositeMember();
  
      Assert.assertFalse( "a !isComposite"  , a.isComposite()   );
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      T.amount("a members", 1, a.getMembers().size());
      Assert.assertTrue("a has subjA", a.hasMember(subjA));
  
      Assert.assertFalse(  "b !isComposite" , b.isComposite()   );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      T.amount("b members", 1, b.getMembers().size());
      Assert.assertTrue("b has subjB", b.hasMember(subjB));
  
      Assert.assertTrue(  "c isComposite"   , c.isComposite()   );
      Assert.assertFalse( "c !hasComposite" , c.hasComposite()  );
      T.amount("c members", 0, c.getMembers().size());
  
      Assert.assertTrue(  "d isComposite"   , d.isComposite()   );
      Assert.assertFalse( "d !hasComposite" , d.hasComposite()  );
      T.amount("d members", 0, d.getMembers().size());
  
      Assert.assertFalse( "e !isComposite"  , e.isComposite()   );
      Assert.assertTrue(  "e hasComposite"  , e.hasComposite()  );
      T.amount("e members", 0, e.getMembers().size());
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDelUnionWithTwoChildrenAndCompositeParent()

  public void testDelUnionWithTwoChildrenAndParent() {
    LOG.info("testDelUnionWithTwoChildrenAndParent");
    try {
      R       r     = R.populateRegistry(1, 4, 2);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Subject cSubj = c.toSubject();
      Group   d     = r.getGroup("a", "d");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
  
      a.addMember(subjA);
      b.addMember(subjB);
      c.addCompositeMember(CompositeType.UNION, a, b);
      d.addMember(cSubj);
      c.deleteCompositeMember();
  
      T.amount("a members", 1, a.getMembers().size());
      Assert.assertTrue("a has subjA", a.hasMember(subjA));
  
      T.amount("b members", 1, b.getMembers().size());
      Assert.assertTrue("b has subjB", b.hasMember(subjB));
  
      T.amount("c members", 0, c.getMembers().size());
  
      T.amount("d members", 1, d.getMembers().size());
      Assert.assertTrue("d has cSubj", d.hasMember(cSubj));
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDelUnionWithTwoChildrenAndParent()

  public void testFailIsCompositeWhenNot() {
    LOG.info("testFailIsCompositeWhenNot");
    try {
      R     r = R.populateRegistry(1, 1, 0);
      Group a = r.getGroup("a", "a");
      Assert.assertFalse("isComposite", a.isComposite());
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailIsCompositeWhenNot()

  public void testAddMemberToChildOfComposite() {
    LOG.info("testAddMemberToChildOfComposite");
    try {
      R       r     = R.populateRegistry(1, 3, 3);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
      Subject subjC = r.getSubject("c");
  
      a.addMember(subjA);
      b.addMember(subjB);
      c.addCompositeMember(CompositeType.UNION, a, b);
      a.addMember(subjC);
  
      Assert.assertTrue(  "a isComposite"   , a.isComposite()   );
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      T.amount("a members", 2, a.getMembers().size());
      Assert.assertTrue("a has subjA", a.hasMember(subjA));
      Assert.assertTrue("a has subjC", a.hasMember(subjC));
  
      Assert.assertTrue(  "b isComposite"   , b.isComposite()   );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      T.amount("b members", 1, b.getMembers().size());
      Assert.assertTrue("b has subjB", b.hasMember(subjB));
  
      Assert.assertFalse( "c !isComposite"  , c.isComposite()   );
      Assert.assertTrue(  "c hasComposite"  , c.hasComposite()  );
      T.amount("c members", 3, c.getMembers().size());
      Assert.assertTrue("c has subjA", c.hasMember(subjA));
      Assert.assertTrue("c has subjB", c.hasMember(subjB));
      Assert.assertTrue("c has subjC", c.hasMember(subjC));
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddMemberToChildOfComposite()

  public void testAddUnionWithChildAndCompositeChildAndCompositeParent() {
    LOG.info("testAddUnionWithChildAndCompositeChildAndCompositeParent");
    try {
      R       r     = R.populateRegistry(1, 8, 3);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Group   d     = r.getGroup("a", "d");
      Group   e     = r.getGroup("a", "e");
      Group   f     = r.getGroup("a", "f");
      Group   g     = r.getGroup("a", "g");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
      Subject subjC = r.getSubject("c");
  
      a.addMember(subjA);
      b.addMember(subjB);
      c.addMember(subjC);
      d.addCompositeMember(CompositeType.UNION, a, b);
      e.addCompositeMember(CompositeType.UNION, c, d);
      g.addCompositeMember(CompositeType.UNION, e, f);
  
      Assert.assertTrue(  "a isComposite"   , a.isComposite()   );
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      T.amount("a members", 1, a.getMembers().size());
      Assert.assertTrue("a has subjA", a.hasMember(subjA));
  
      Assert.assertTrue(  "b isComposite"   , b.isComposite()   );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      T.amount("b members", 1, b.getMembers().size());
      Assert.assertTrue("b has subjB", b.hasMember(subjB));
  
      Assert.assertTrue(  "c isComposite"   , c.isComposite()   );
      Assert.assertFalse( "c !hasComposite" , c.hasComposite()  );
      T.amount("c members", 1, c.getMembers().size());
      Assert.assertTrue("c has subjC", c.hasMember(subjC));
  
      Assert.assertTrue(  "d isComposite"   , d.isComposite()   );
      Assert.assertTrue(  "d hasComposite"  , d.hasComposite()  );
      T.amount("d members", 2, d.getMembers().size());
      Assert.assertTrue("d has subjA", d.hasMember(subjA));
      Assert.assertTrue("d has subjB", d.hasMember(subjB));
  
      Assert.assertTrue(  "e isComposite"   , e.isComposite()   );
      Assert.assertTrue(  "e hasComposite"  , e.hasComposite()  );
      T.amount("e members", 3, e.getMembers().size());
      Assert.assertTrue("e has subjA", e.hasMember(subjA));
      Assert.assertTrue("e has subjB", e.hasMember(subjB));
      Assert.assertTrue("e has subjC", e.hasMember(subjC));
  
      Assert.assertTrue(  "f isComposite"   , f.isComposite()   );
      Assert.assertFalse( "f !hasComposite" , f.hasComposite()  );
      T.amount("f members", 0, f.getMembers().size());
  
      Assert.assertFalse( "g !isComposite"  , g.isComposite()   );
      Assert.assertTrue(  "g hasComposite"  , g.hasComposite()  );
      T.amount("g members", 3, g.getMembers().size());
      Assert.assertTrue("g has subjA", g.hasMember(subjA));
      Assert.assertTrue("g has subjB", g.hasMember(subjB));
      Assert.assertTrue("g has subjC", g.hasMember(subjC));
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithChildAndCompositeChildAndCompositeParent()

  public void testAddUnionWithTwoCompositeChildrenAndCompositeParent() {
    LOG.info("testAddUnionWithTwoCompositeChildrenAndCompositeParent");
    try {
      R       r     = R.populateRegistry(1, 9, 4);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Group   d     = r.getGroup("a", "d");
      Group   e     = r.getGroup("a", "e");
      Group   f     = r.getGroup("a", "f");
      Group   g     = r.getGroup("a", "g");
      Group   h     = r.getGroup("a", "h");
      Group   i     = r.getGroup("a", "i");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
      Subject subjC = r.getSubject("c");
      Subject subjD = r.getSubject("d");
  
      a.addMember(subjA);
      b.addMember(subjB);
      c.addCompositeMember(CompositeType.UNION, a, b);
      d.addMember(subjC);
      e.addMember(subjD);
      f.addCompositeMember(CompositeType.UNION, d, e);
      g.addCompositeMember(CompositeType.UNION, c, f);
      i.addCompositeMember(CompositeType.UNION, g, h);
  
      Assert.assertTrue(  "a isComposite"   , a.isComposite()   );
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      T.amount("a members", 1, a.getMembers().size());
      Assert.assertTrue("a has subjA", a.hasMember(subjA));
  
      Assert.assertTrue(  "b isComposite"   , b.isComposite()   );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      T.amount("b members", 1, b.getMembers().size());
      Assert.assertTrue("b has subjB", b.hasMember(subjB));
  
      Assert.assertTrue(  "c isComposite"   , c.isComposite()   );
      Assert.assertTrue(  "c hasComposite"  , c.hasComposite()  );
      T.amount("c members", 2, c.getMembers().size());
      Assert.assertTrue("c has subjA", c.hasMember(subjA));
      Assert.assertTrue("c has subjB", c.hasMember(subjB));
  
      Assert.assertTrue(  "d isComposite"   , d.isComposite()   );
      Assert.assertFalse( "d !hasComposite" , d.hasComposite()  );
      T.amount("d members", 1, d.getMembers().size());
      Assert.assertTrue("d has subjC", d.hasMember(subjC));
  
      Assert.assertTrue(  "e isComposite"   , e.isComposite()   );
      Assert.assertFalse( "e !hasComposite" , e.hasComposite()  );
      T.amount("e members", 1, e.getMembers().size());
      Assert.assertTrue("e has subjD", e.hasMember(subjD));
  
      Assert.assertTrue(  "f isComposite"   , f.isComposite()   );
      Assert.assertTrue(  "f hasComposite"  , f.hasComposite()  );
      T.amount("f members", 2, f.getMembers().size());
      Assert.assertTrue("f has subjC", f.hasMember(subjC));
      Assert.assertTrue("f has subjD", f.hasMember(subjD));
  
      Assert.assertTrue(  "g isComposite"   , g.isComposite()   );
      Assert.assertTrue(  "g hasComposite"  , g.hasComposite()  );
      T.amount("g members", 4, g.getMembers().size());
      Assert.assertTrue(  "g has subjA" ,   g.hasMember(subjA)  );
      Assert.assertTrue(  "g has subjB" ,   g.hasMember(subjB)  );
      Assert.assertTrue(  "g has subjC" ,   g.hasMember(subjC)  );
      Assert.assertTrue(  "g has subjD" ,   g.hasMember(subjD)  );
  
      Assert.assertTrue(  "h isComposite"   , h.isComposite()   );
      Assert.assertFalse( "h !hasComposite" , h.hasComposite()  );
      T.amount("h members", 0, h.getMembers().size());
  
      Assert.assertFalse( "i !isComposite"  , i.isComposite()   );
      Assert.assertTrue(  "i hasComposite"  , i.hasComposite()  );
      T.amount("i members", 4, i.getMembers().size());
      Assert.assertTrue(  "i has subjA" ,   i.hasMember(subjA)  );
      Assert.assertTrue(  "i has subjB" ,   i.hasMember(subjB)  );
      Assert.assertTrue(  "i has subjC" ,   i.hasMember(subjC)  );
      Assert.assertTrue(  "i has subjD" ,   i.hasMember(subjD)  );
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithTwoCompositeChildrenAndCompositeParent()

  public void testDelMemberFromChildofComposite() {
    LOG.info("testDelMemberFromChildofComposite");
    try {
      R       r     = R.populateRegistry(1, 3, 2);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
  
      a.addMember(subjA);
      b.addMember(subjB);
      c.addCompositeMember(CompositeType.UNION, a, b);
      a.deleteMember(subjA);
  
      Assert.assertTrue(  "a isComposite"   , a.isComposite()   );
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      T.amount("a members", 0, a.getMembers().size());
  
      Assert.assertTrue(  "b isComposite"   , b.isComposite()   );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      T.amount("b members", 1, b.getMembers().size());
      Assert.assertTrue("b has subjB", b.hasMember(subjB));
  
      Assert.assertFalse( "c !isComposite"  , c.isComposite()   );
      Assert.assertTrue(  "c hasComposite"  , c.hasComposite()  );
      T.amount("c members", 1, c.getMembers().size());
      Assert.assertTrue("c has subjB", c.hasMember(subjB));
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDelMemberFromChildofComposite()

  public void testDelUnionWithChildAndCompositeChildAndCompositeParent() {
    LOG.info("testDelUnionWithChildAndCompositeChildAndCompositeParent");
    try {
      R       r     = R.populateRegistry(1, 8, 3);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Group   d     = r.getGroup("a", "d");
      Group   e     = r.getGroup("a", "e");
      Group   f     = r.getGroup("a", "f");
      Group   g     = r.getGroup("a", "g");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
      Subject subjC = r.getSubject("c");
  
      a.addMember(subjA);
      b.addMember(subjB);
      c.addMember(subjC);
      d.addCompositeMember(CompositeType.UNION, a, b);
      e.addCompositeMember(CompositeType.UNION, c, d);
      g.addCompositeMember(CompositeType.UNION, e, f);
      e.deleteCompositeMember();
  
      Assert.assertTrue(  "a isComposite"   , a.isComposite()   );
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      T.amount("a members", 1, a.getMembers().size());
      Assert.assertTrue("a has subjA", a.hasMember(subjA));
  
      Assert.assertTrue(  "b isComposite"   , b.isComposite()   );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      T.amount("b members", 1, b.getMembers().size());
      Assert.assertTrue("b has subjB", b.hasMember(subjB));
  
      Assert.assertFalse( "c !isComposite"  , c.isComposite()   );
      Assert.assertFalse( "c !hasComposite" , c.hasComposite()  );
      T.amount("c members", 1, c.getMembers().size());
      Assert.assertTrue("c has subjC", c.hasMember(subjC));
  
      Assert.assertFalse( "d !isComposite"  , d.isComposite()   );
      Assert.assertTrue(  "d hasComposite"  , d.hasComposite()  );
      T.amount("d members", 2, d.getMembers().size());
      Assert.assertTrue("d has subjA", d.hasMember(subjA));
      Assert.assertTrue("d has subjB", d.hasMember(subjB));
  
      Assert.assertTrue(  "e isComposite"   , e.isComposite()   );
      Assert.assertFalse( "e !hasComposite" , e.hasComposite()  );
      T.amount("e members", 0, e.getMembers().size());
  
      Assert.assertTrue(  "f isComposite"   , f.isComposite()   );
      Assert.assertFalse( "f !hasComposite" , f.hasComposite()  );
      T.amount("f members", 0, f.getMembers().size());
  
      Assert.assertFalse( "g !isComposite"  , g.isComposite()   );
      Assert.assertTrue(  "g hasComposite"  , g.hasComposite()  );
      T.amount("g members", 0, g.getMembers().size());
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDelUnionWithChildAndCompositeChildAndCompositeParent()

  public void testDelUnionWithChildAndCompositeChildAndParent() {
    LOG.info("testDelUnionWithChildAndCompositeChildAndParent");
    try {
      R       r     = R.populateRegistry(1, 7, 3);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Group   d     = r.getGroup("a", "d");
      Group   e     = r.getGroup("a", "e");
      Subject eSubj = e.toSubject();
      Group   f     = r.getGroup("a", "f");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
      Subject subjC = r.getSubject("c");
  
      a.addMember(subjA);
      b.addMember(subjB);
      c.addMember(subjC);
      d.addCompositeMember(CompositeType.UNION, a, b);
      e.addCompositeMember(CompositeType.UNION, c, d);
      f.addMember(eSubj);
      e.deleteCompositeMember();
  
      Assert.assertTrue(  "a isComposite"   , a.isComposite()   );
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      T.amount("a members", 1, a.getMembers().size());
      Assert.assertTrue("a has subjA", a.hasMember(subjA));
  
      Assert.assertTrue(  "b isComposite"   , b.isComposite()   );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      T.amount("b members", 1, b.getMembers().size());
      Assert.assertTrue("b has subjB", b.hasMember(subjB));
  
      Assert.assertFalse( "c !isComposite"  , c.isComposite()   );
      Assert.assertFalse( "c !hasComposite" , c.hasComposite()  );
      T.amount("c members", 1, c.getMembers().size());
      Assert.assertTrue("c has subjC", c.hasMember(subjC));
  
      Assert.assertFalse( "d !isComposite"  , d.isComposite()   );
      Assert.assertTrue(  "d hasComposite"  , d.hasComposite()  );
      T.amount("d members", 2, d.getMembers().size());
      Assert.assertTrue("d has subjA", d.hasMember(subjA));
      Assert.assertTrue("d has subjB", d.hasMember(subjB));
  
      Assert.assertFalse( "e !isComposite"  , e.isComposite()   );
      Assert.assertFalse( "e !hasComposite" , e.hasComposite()  );
      T.amount("e members", 0, e.getMembers().size());
  
      Assert.assertFalse( "f !isComposite"  , f.isComposite()   );
      Assert.assertFalse( "e !hasComposite" , f.hasComposite()  );
      T.amount("f members", 1, f.getMembers().size());
      Assert.assertTrue("f has eSubj", f.hasMember(eSubj));
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDelUnionWithChildAndCompositeChildAndParent()

  public void testDelUnionWithTwoCompositeChildrenAndCompositeParent() {
    LOG.info("testDelUnionWithTwoCompositeChildrenAndCompositeParent");
    try {
      R       r     = R.populateRegistry(1, 9, 4);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Group   d     = r.getGroup("a", "d");
      Group   e     = r.getGroup("a", "e");
      Group   f     = r.getGroup("a", "f");
      Group   g     = r.getGroup("a", "g");
      Group   h     = r.getGroup("a", "h");
      Group   i     = r.getGroup("a", "i");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
      Subject subjC = r.getSubject("c");
      Subject subjD = r.getSubject("d");
  
      a.addMember(subjA);
      b.addMember(subjB);
      c.addCompositeMember(CompositeType.UNION, a, b);
      d.addMember(subjC);
      e.addMember(subjD);
      f.addCompositeMember(CompositeType.UNION, d, e);
      g.addCompositeMember(CompositeType.UNION, c, f);
      i.addCompositeMember(CompositeType.UNION, g, h);
      g.deleteCompositeMember();
  
      Assert.assertTrue(  "a isComposite"   , a.isComposite()   );
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      T.amount("a members", 1, a.getMembers().size());
      Assert.assertTrue("a has subjA", a.hasMember(subjA));
  
      Assert.assertTrue(  "b isComposite"   , b.isComposite()   );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      T.amount("b members", 1, b.getMembers().size());
      Assert.assertTrue("b has subjB", b.hasMember(subjB));
  
      Assert.assertFalse( "c !isComposite"  , c.isComposite()   );
      Assert.assertTrue(  "c hasComposite"  , c.hasComposite()  );
      T.amount("c members", 2, c.getMembers().size());
      Assert.assertTrue("c has subjA", c.hasMember(subjA));
      Assert.assertTrue("c has subjB", c.hasMember(subjB));
  
      Assert.assertTrue(  "d isComposite"   , d.isComposite()   );
      Assert.assertFalse( "d !hasComposite" , d.hasComposite()  );
      T.amount("d members", 1, d.getMembers().size());
      Assert.assertTrue("d has subjC", d.hasMember(subjC));
  
      Assert.assertTrue(  "e isComposite"   , e.isComposite()   );
      Assert.assertFalse( "e !hasComposite" , e.hasComposite()  );
      T.amount("e members", 1, e.getMembers().size());
      Assert.assertTrue("e has subjD", e.hasMember(subjD));
  
      Assert.assertFalse( "f !isComposite"  , f.isComposite()   );
      Assert.assertTrue(  "f hasComposite"  , f.hasComposite()  );
      T.amount("f members", 2, f.getMembers().size());
      Assert.assertTrue("f has subjC", f.hasMember(subjC));
      Assert.assertTrue("f has subjD", f.hasMember(subjD));
  
      Assert.assertTrue(  "g isComposite"   , g.isComposite()   );
      Assert.assertFalse( "g !hasComposite" , g.hasComposite()  );
      T.amount("g members", 0, g.getMembers().size());
  
      Assert.assertTrue(  "h isComposite"   , h.isComposite()   );
      Assert.assertFalse( "h !hasComposite" , h.hasComposite()  );
      T.amount("h members", 0, h.getMembers().size());
  
      Assert.assertFalse( "i !isComposite"  , i.isComposite()   );
      Assert.assertTrue(  "i hasComposite"  , i.hasComposite()  );
      T.amount("i members", 0, i.getMembers().size());
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDelUnionWithTwoCompositeChildrenAndCompositeParent()

  public void testFailToAddMemberWhenHasComposite() {
    LOG.info("testFailToAddMemberWhenHasComposite");
    try {
      R     r = R.populateRegistry(1, 3, 1);
      Group a = r.getGroup("a", "a");
      Group b = r.getGroup("a", "b");
      Group c = r.getGroup("a", "c");
      a.addCompositeMember(CompositeType.UNION, b, c);
      try {
        a.addMember( r.getSubject("a") );
        Assert.fail("FAIL: expected exception: " + E.GROUP_AMTC);
      }
      catch (MemberAddException eMA) {
        Assert.assertTrue("OK: cannot add member to composite mship", true);
        assertContains("error message", eMA.getMessage(), E.GROUP_AMTC);
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailToAddMemberWhenHasComposite()

  public void testFailToDeleteMemberWhenHasComposite() {
    LOG.info("testFailToDeleteMemberWhenHasComposite");
    try {
      R     r = R.populateRegistry(1, 3, 1);
      Group a = r.getGroup("a", "a");
      Group b = r.getGroup("a", "b");
      Group c = r.getGroup("a", "c");
      a.addCompositeMember(CompositeType.UNION, b, c);
      try {
        a.deleteMember( r.getSubject("a") );
        Assert.fail("FAIL: expected exception: " + E.GROUP_DMFC);
      }
      catch (MemberDeleteException eMD) {
        Assert.assertTrue("OK: cannot del member from composite mship", true);
        assertContains("error message", eMD.getMessage(), E.GROUP_DMFC);
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailToDeleteMemberWhenHasComposite()

} // public class TestCompositeU0

