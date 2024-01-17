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
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestCompositeI.java,v 1.2 2009-03-20 19:56:41 mchyzer Exp $
 * @since   1.0
 */
public class TestCompositeI extends GrouperTest {

  public static void main(String[] args) {
    TestRunner.run(TestCompositeI.class);
    //TestRunner.run(new TestCompositeI("testFailNotPrivilegedToAddCompositeMember"));
  }
  
  private static final Log LOG = GrouperUtil.getLog(TestCompositeI.class);

  public TestCompositeI(String name) {
    super(name);
  }

  public void testFailNotPrivilegedToAddCompositeMember() {
    LOG.info("testFailNotPrivilegedToAddCompositeMember");
    try {
      runCompositeMembershipChangeLogConsumer();
      
      R               r   = R.populateRegistry(1, 3, 1);
      GrouperSession  nrs = GrouperSession.start( r.getSubject("a") );
      Group           a   = r.getGroup("a", "a");
      a.addCompositeMember(
        CompositeType.INTERSECTION, r.getGroup("a", "b"), r.getGroup("a", "c")
      );
      r.rs.stop();
      nrs.stop();
      Assert.fail("added composite without privilege to add composite");
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.assertTrue("OK: cannot add union without privileges", true);
      assertContains("error message", eIP.getMessage(), E.CANNOT_UPDATE);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailNotPrivilegedToAddCompositeMember()

  public void testFailNotPrivilegedToDeleteCompositeMember() {
    LOG.info("testFailNotPrivilegedToDeleteCompositeMember");
    try {
      runCompositeMembershipChangeLogConsumer();

      R               r   = R.populateRegistry(1, 3, 1);
      GrouperSession  nrs = GrouperSession.start( r.getSubject("a") );
      Group           a   = r.getGroup("a", "a");
      a.addCompositeMember(
        CompositeType.INTERSECTION, r.getGroup("a", "b"), r.getGroup("a", "c")
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
  public void testDelUnionWithNoChildrenAndNoParents() {
    LOG.info("testDelUnionWithNoChildrenAndNoParents");
    try {
      runCompositeMembershipChangeLogConsumer();

      R     r = R.populateRegistry(1, 3, 0);
      Group a = r.getGroup("a", "a");
      Group b = r.getGroup("a", "b");
      Group c = r.getGroup("a", "c");
      a.addCompositeMember(CompositeType.INTERSECTION, b, c);
      runCompositeMembershipChangeLogConsumer();
      
      a.deleteCompositeMember();
      runCompositeMembershipChangeLogConsumer();

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

  public void testAddUnionWithOneChildAndNoParents() {
    LOG.info("testAddUnionWithOneChildAndNoParents");
    try {
      runCompositeMembershipChangeLogConsumer();

      R       r     = R.populateRegistry(1, 3, 1);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Subject subjA = r.getSubject("a");
      b.addMember(subjA);
      a.addCompositeMember(CompositeType.INTERSECTION, b, c);
      runCompositeMembershipChangeLogConsumer();

      Assert.assertTrue(  "a hasComposite"  , a.hasComposite()  );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      Assert.assertFalse( "c !hasComposite" , c.hasComposite()  );
      Assert.assertFalse( "a !isComposite"  , a.isComposite()   );
      Assert.assertTrue(  "b isComposite"   , b.isComposite()   );
      Assert.assertTrue(  "c isComposite"   , c.isComposite()   );
      T.amount("a members", 0, a.getMembers().size());
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithOneChildAndNoParents()

  public void testDelUnionWithOneChildAndNoParents() {
    LOG.info("testDelUnionWithOneChildAndNoParents");
    try {
      runCompositeMembershipChangeLogConsumer();

      R       r     = R.populateRegistry(1, 3, 1);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Subject subjA = r.getSubject("a");
      b.addMember(subjA);
      a.addCompositeMember(CompositeType.INTERSECTION, b, c);
      runCompositeMembershipChangeLogConsumer();

      a.deleteCompositeMember();
      runCompositeMembershipChangeLogConsumer();

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

  public void testAddUnionWithCompositeChildAndNoParents() {
    LOG.info("testAddUnionWithCompositeChildAndNoParents");
    try {
      runCompositeMembershipChangeLogConsumer();

      R       r     = R.populateRegistry(1, 4, 2);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Group   d     = r.getGroup("a", "d");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
      b.addMember(subjA);                                     // subjA
      c.addMember(subjB);                                     // subjB
      a.addCompositeMember(CompositeType.UNION, b, c);        // subjA, subjB
      d.addCompositeMember(CompositeType.INTERSECTION, a, b); // subjA
      runCompositeMembershipChangeLogConsumer();

      T.amount("d members", 1, d.getMembers().size());
      Assert.assertTrue("d hasMember subjA", d.hasMember(subjA));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithCompositeChildAndNoParents()

  public void testDelUnionWithCompositeChildAndNoParents() {
    LOG.info("testDelUnionWithCompositeChildAndNoParents");
    try {
      runCompositeMembershipChangeLogConsumer();

      R       r     = R.populateRegistry(1, 4, 2);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Group   d     = r.getGroup("a", "d");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
      b.addMember(subjA);
      c.addMember(subjB);
      a.addCompositeMember(CompositeType.INTERSECTION, b, c);
      d.addCompositeMember(CompositeType.INTERSECTION, a, b);
      runCompositeMembershipChangeLogConsumer();

      d.deleteCompositeMember();
      runCompositeMembershipChangeLogConsumer();

      T.amount("d members", 0, d.getMembers().size());
      Assert.assertFalse("d !hasMember subjA", d.hasMember(subjA));
      Assert.assertFalse("d !hasMember subjB", d.hasMember(subjB));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDelUnionWithCompositeChildAndNoParents()

  public void testAddUnionWithTwoChildrenAndNoParents() {
    LOG.info("testAddUnionWithTwoChildrenAndNoParents");
    try {
      runCompositeMembershipChangeLogConsumer();

      R       r     = R.populateRegistry(1, 3, 1);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Subject subjA = r.getSubject("a");
      b.addMember(subjA);
      c.addMember(subjA);
      a.addCompositeMember(CompositeType.INTERSECTION, b, c);
      runCompositeMembershipChangeLogConsumer();

      T.amount("a members", 1, a.getMembers().size());
      Assert.assertTrue("a hasMember subjA", a.hasMember(subjA));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithTwoChildrenAndNoParents()

  public void testDelUnionWithTwoChildrenAndNoParents() {
    LOG.info("testDelUnionWithTwoChildrenAndNoParents");
    try {
      runCompositeMembershipChangeLogConsumer();

      R       r     = R.populateRegistry(1, 3, 2);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
      b.addMember(subjA);
      c.addMember(subjB);
      a.addCompositeMember(CompositeType.INTERSECTION, b, c);
      runCompositeMembershipChangeLogConsumer();

      a.deleteCompositeMember();
      runCompositeMembershipChangeLogConsumer();

      T.amount("a members", 0, a.getMembers().size());
      Assert.assertFalse("a !hasMember subjA", a.hasMember(subjA));
      Assert.assertFalse("a !hasMember subjB", a.hasMember(subjB));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDelUnionWithTwoChildrenAndNoParents()

  public void testAddUnionWithTwoCompositeChildrenAndNoParents() {
    LOG.info("testAddUnionWithTwoCompositeChildrenAndNoParents");
    try {
      runCompositeMembershipChangeLogConsumer();

      R       r     = R.populateRegistry(1, 7, 2);
      // Feeder Groups
      Subject subjA = r.getSubject("a");
      Group   a     = r.getGroup("a", "a");
      a.addMember(subjA);                                       // subjA
      Subject subjB = r.getSubject("b");
      Group   b     = r.getGroup("a", "b");
      b.addMember(subjB);                                       // subjB
      Group   c     = r.getGroup("a", "c");
      c.addMember(subjA);                                       // subjA
      Group   d     = r.getGroup("a", "d");
      d.addMember(subjB);                                       // subjB
      // Feeder Composite Groups
      Group   e     = r.getGroup("a", "e");
      e.addCompositeMember(CompositeType.INTERSECTION, a, b);   // -
      Group   f     = r.getGroup("a", "f");
      f.addCompositeMember(CompositeType.INTERSECTION, c, d);   // -
      // And our ultimate composite group
      Group   g     = r.getGroup("a", "g");
      g.addCompositeMember(CompositeType.INTERSECTION, e, f);   // -
      runCompositeMembershipChangeLogConsumer();

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
      Assert.assertTrue(  "c has subjA",  c.hasMember(subjA)  );
      T.amount( "d members", 1, d.getMembers().size() );
      Assert.assertTrue(  "d has subjB",  d.hasMember(subjB)  );
      T.amount( "e members", 0, e.getMembers().size() );
      T.amount( "f members", 0, f.getMembers().size() );
      T.amount( "g members", 0, g.getMembers().size() );
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithTwoCompositeChildrenAndNoParents()

  public void testDelUnionWithTwoCompositeChildrenAndNoParents() {
    LOG.info("testDelUnionWithTwoCompositeChildrenAndNoParents");
    try {
      runCompositeMembershipChangeLogConsumer();

      R       r     = R.populateRegistry(1, 7, 2);
      // Feeder Groups
      Subject subjA = r.getSubject("a");
      Group   a     = r.getGroup("a", "a");
      a.addMember(subjA);
      Subject subjB = r.getSubject("b");
      Group   b     = r.getGroup("a", "b");
      b.addMember(subjB);
      Group   c     = r.getGroup("a", "c");
      c.addMember(subjA);
      Group   d     = r.getGroup("a", "d");
      d.addMember(subjB);
      // Feeder Composite Groups
      Group   e     = r.getGroup("a", "e");
      e.addCompositeMember(CompositeType.INTERSECTION, a, b);
      Group   f     = r.getGroup("a", "f");
      f.addCompositeMember(CompositeType.INTERSECTION, c, d);
      // And our ultimate composite group
      Group   g     = r.getGroup("a", "g");
      g.addCompositeMember(CompositeType.INTERSECTION, e, f);
      runCompositeMembershipChangeLogConsumer();

      g.deleteCompositeMember();
      runCompositeMembershipChangeLogConsumer();

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
      Assert.assertTrue(  "c has subjA",  c.hasMember(subjA)  );
      T.amount( "d members", 1, d.getMembers().size() );
      Assert.assertTrue(  "d has subjB",  d.hasMember(subjB)  );
      T.amount( "e members", 0, e.getMembers().size() );
      T.amount( "f members", 0, f.getMembers().size() );
      T.amount( "g members", 0, g.getMembers().size() );
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDelUnionWithTwoCompositeChildrenAndNoParents()

  public void testAddUnionWithNoChildrenAndParent() {
    LOG.info("testAddUnionWithNoChildrenAndParent");
    try {
      runCompositeMembershipChangeLogConsumer();

      R       r     = R.populateRegistry(1, 4, 0);
      // Feeder Groups
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      // Composite Group
      Group   c     = r.getGroup("a", "c");
      c.addCompositeMember(CompositeType.INTERSECTION, a, b);
      // Parent Group
      Group   d     = r.getGroup("a", "d");
      d.addMember(c.toSubject());
      runCompositeMembershipChangeLogConsumer();

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

  public void testFailHasCompositeWhenNot() {
    LOG.info("testFailHasCompositeWhenNot");
    try {
      runCompositeMembershipChangeLogConsumer();

      R     r = R.populateRegistry(1, 1, 0);
      Group a = r.getGroup("a", "a");
      Assert.assertFalse("hasComposite", a.hasComposite());
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailHasCompositeWhenNot()

  public void testDelUnionWithNoChildrenAndParent() {
    LOG.info("testDelUnionWithNoChildrenAndParent");
    try {
      runCompositeMembershipChangeLogConsumer();

      R       r     = R.populateRegistry(1, 4, 0);
      // Feeder Groups
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      // Composite Group
      Group   c     = r.getGroup("a", "c");
      c.addCompositeMember(CompositeType.INTERSECTION, a, b);
      runCompositeMembershipChangeLogConsumer();

      // Parent Group
      Group   d     = r.getGroup("a", "d");
      d.addMember(c.toSubject());
      runCompositeMembershipChangeLogConsumer();

      c.deleteCompositeMember();
      runCompositeMembershipChangeLogConsumer();
  
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

  public void testAddUnionWithNoChildrenAndCompositeParent() {
    LOG.info("testAddUnionWithNoChildrenAndCompositeParent");
    try {
      runCompositeMembershipChangeLogConsumer();

      R       r     = R.populateRegistry(1, 5, 0);
      // Feeder Groups
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      // Composite Group
      Group   d     = r.getGroup("a", "d");
      d.addCompositeMember(CompositeType.INTERSECTION, a, b);
      // Parent Composite Group
      Group   e     = r.getGroup("a", "e");
      e.addCompositeMember(CompositeType.INTERSECTION, c, d);
      runCompositeMembershipChangeLogConsumer();

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

  public void testAddUnionWithOneChildAndCompositeParent() {
    LOG.info("testAddUnionWithOneChildAndCompositeParent");
    try {
      runCompositeMembershipChangeLogConsumer();

      R       r     = R.populateRegistry(1, 5, 1);
      // Feeder Groups
      Group   a     = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");
      a.addMember(subjA);
      Group   b     = r.getGroup("a", "b");
      // Composite Group
      Group   c     = r.getGroup("a", "c");
      c.addCompositeMember(CompositeType.INTERSECTION, a, b);
      // Parent Feeder Group
      Group   d     = r.getGroup("a", "d");
      // Parent Group
      Group   e     = r.getGroup("a", "e");
      e.addCompositeMember(CompositeType.INTERSECTION, c, d);
      runCompositeMembershipChangeLogConsumer();

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
  
      T.amount( "c members", 0, c.getMembers().size() );
  
      T.amount( "d members", 0, d.getMembers().size() );
  
      T.amount( "e members", 0, e.getMembers().size() );
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithOneChildAndCompositeParent()

  public void testAddUnionWithOneChildAndParent() {
    LOG.info("testAddUnionWithOneChildAndParent");
    try {
      runCompositeMembershipChangeLogConsumer();

      R       r     = R.populateRegistry(1, 4, 1);
      // Feeder Groups
      Group   a     = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");
      a.addMember(subjA);
      Group   b     = r.getGroup("a", "b");
      // Composite Group
      Group   c     = r.getGroup("a", "c");
      c.addCompositeMember(CompositeType.INTERSECTION, a, b);
      // Parent Group
      Group   d     = r.getGroup("a", "d");
      Subject cSubj = c.toSubject();
      d.addMember(cSubj);
      runCompositeMembershipChangeLogConsumer();

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
      T.amount( "c members", 0, c.getMembers().size() );
      T.amount( "d members", 1, d.getMembers().size() );
      Assert.assertTrue("d has cSubj", d.hasMember(cSubj));
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithOneChildAndParent()

  public void testAddUnionWithTwoChildrenAndParent() {
    LOG.info("testAddUnionWithTwoChildrenAndParent");
    try {
      runCompositeMembershipChangeLogConsumer();

      R       r     = R.populateRegistry(1, 4, 1);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Subject cSubj = c.toSubject();
      Group   d     = r.getGroup("a", "d");
      Subject subjA = r.getSubject("a");
  
      a.addMember(subjA);
      b.addMember(subjA);
      c.addCompositeMember(CompositeType.INTERSECTION, a, b);
      d.addMember(cSubj);
      runCompositeMembershipChangeLogConsumer();

      T.amount("a members", 1, a.getMembers().size());
      Assert.assertTrue("a has subjA", a.hasMember(subjA));
  
      T.amount("b members", 1, b.getMembers().size());
      Assert.assertTrue("b has subjA", b.hasMember(subjA));
  
      T.amount("c members", 1, c.getMembers().size());
      Assert.assertTrue("c has subjA", c.hasMember(subjA));
      T.amount("c comp members", 1, c.getCompositeMembers().size());
      T.amount("c comp mships", 1, c.getCompositeMemberships().size());
  
      T.amount("d members", 2, d.getMembers().size());
      Assert.assertTrue("d has cSubj", d.hasMember(cSubj));
      Assert.assertTrue("d has subjA", d.hasMember(subjA));
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithTwoChildrenAndParent()

  public void testDelUnionWithNoChildrenAndCompositeParent() {
    LOG.info("testDelUnionWithNoChildrenAndCompositeParent");
    try {
      runCompositeMembershipChangeLogConsumer();

      R       r     = R.populateRegistry(1, 5, 0);
      // Feeder Groups
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      // Composite Group
      Group   d     = r.getGroup("a", "d");
      d.addCompositeMember(CompositeType.INTERSECTION, a, b);
      // Parent Composite Group
      Group   e     = r.getGroup("a", "e");
      e.addCompositeMember(CompositeType.INTERSECTION, c, d);
      runCompositeMembershipChangeLogConsumer();

      d.deleteCompositeMember();
      runCompositeMembershipChangeLogConsumer();

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

  public void testDelUnionWithOneChildAndCompositeParent() {
    LOG.info("testDelUnionWithOneChildAndCompositeParent");
    try {
      runCompositeMembershipChangeLogConsumer();

      R       r     = R.populateRegistry(1, 5, 1);
      // Feeder Groups
      Group   a     = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");
      a.addMember(subjA);
      Group   b     = r.getGroup("a", "b");
      // Composite Group
      Group   c     = r.getGroup("a", "c");
      c.addCompositeMember(CompositeType.INTERSECTION, a, b);
      // Parent Feeder Group
      Group   d     = r.getGroup("a", "d");
      // Parent Group
      Group   e     = r.getGroup("a", "e");
      e.addCompositeMember(CompositeType.INTERSECTION, c, d);
      runCompositeMembershipChangeLogConsumer();

      c.deleteCompositeMember();
      runCompositeMembershipChangeLogConsumer();

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
      runCompositeMembershipChangeLogConsumer();

      R       r     = R.populateRegistry(1, 4, 1);
      // Feeder Groups
      Group   a     = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");
      a.addMember(subjA);
      Group   b     = r.getGroup("a", "b");
      // Composite Group
      Group   c     = r.getGroup("a", "c");
      c.addCompositeMember(CompositeType.INTERSECTION, a, b);
      // Parent Group
      Group   d     = r.getGroup("a", "d");
      Subject cSubj = c.toSubject();
      d.addMember(cSubj);
      runCompositeMembershipChangeLogConsumer();

      c.deleteCompositeMember();
      runCompositeMembershipChangeLogConsumer();

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

  public void testDelUnionWithTwoChildrenAndParent() {
    LOG.info("testDelUnionWithTwoChildrenAndParent");
    try {
      runCompositeMembershipChangeLogConsumer();

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
      c.addCompositeMember(CompositeType.INTERSECTION, a, b);
      d.addMember(cSubj);
      runCompositeMembershipChangeLogConsumer();

      c.deleteCompositeMember();
      runCompositeMembershipChangeLogConsumer();
  
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

  public void testAddUnionWithTwoChildrenAndCompositeParent() {
    LOG.info("testAddUnionWithTwoChildrenAndCompositeParent");
    try {
      runCompositeMembershipChangeLogConsumer();

      R       r     = R.populateRegistry(1, 5, 1);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Group   d     = r.getGroup("a", "d");
      Group   e     = r.getGroup("a", "e");
      Subject subjA = r.getSubject("a");
  
      a.addMember(subjA);
      b.addMember(subjA);
      c.addCompositeMember(CompositeType.INTERSECTION, a, b);
      e.addCompositeMember(CompositeType.INTERSECTION, c, d);
      runCompositeMembershipChangeLogConsumer();

      Assert.assertTrue(  "a isComposite"   , a.isComposite()   );
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      T.amount("a members", 1, a.getMembers().size());
      Assert.assertTrue("a has subjA", a.hasMember(subjA));
  
      Assert.assertTrue(  "b isComposite"   , b.isComposite()   );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      T.amount("b members", 1, b.getMembers().size());
      Assert.assertTrue("b has subjA", b.hasMember(subjA));
  
      Assert.assertTrue(  "c isComposite"   , c.isComposite()   );
      Assert.assertTrue(  "c hasComposite"  , c.hasComposite()  );
      T.amount("c members", 1, c.getMembers().size());
      Assert.assertTrue("c has subjA", c.hasMember(subjA));
  
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
  } // public void testAddUnionWithTwoChildrenAndCompositeParent()

  public void testDelUnionWithTwoChildrenAndCompositeParent() {
    LOG.info("testDelUnionWithTwoChildrenAndCompositeParent");
    try {
      runCompositeMembershipChangeLogConsumer();

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
      c.addCompositeMember(CompositeType.INTERSECTION, a, b);
      e.addCompositeMember(CompositeType.INTERSECTION, c, d);
      runCompositeMembershipChangeLogConsumer();

      c.deleteCompositeMember();
      runCompositeMembershipChangeLogConsumer();
  
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

  public void testFailIsCompositeWhenNot() {
    LOG.info("testFailIsCompositeWhenNot");
    try {
      runCompositeMembershipChangeLogConsumer();

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
      runCompositeMembershipChangeLogConsumer();

      R       r     = R.populateRegistry(1, 3, 2);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
  
      a.addMember(subjA);
      b.addMember(subjB);
      c.addCompositeMember(CompositeType.INTERSECTION, a, b);
      a.addMember(subjB);
      runCompositeMembershipChangeLogConsumer();

      Assert.assertTrue(  "a isComposite"   , a.isComposite()   );
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      T.amount("a members", 2, a.getMembers().size());
      Assert.assertTrue("a has subjA", a.hasMember(subjA));
      Assert.assertTrue("a has subjB", a.hasMember(subjB));
  
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
  } // public void testAddMemberToChildOfComposite()

  public void testAddUnionWithChildAndCompositeChildAndCompositeParent() {
    LOG.info("testAddUnionWithChildAndCompositeChildAndCompositeParent");
    try {
      runCompositeMembershipChangeLogConsumer();

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
      d.addCompositeMember(CompositeType.INTERSECTION, a, b);
      e.addCompositeMember(CompositeType.INTERSECTION, c, d);
      g.addCompositeMember(CompositeType.INTERSECTION, e, f);
      runCompositeMembershipChangeLogConsumer();

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
      T.amount("d members", 0, d.getMembers().size());
  
      Assert.assertTrue(  "e isComposite"   , e.isComposite()   );
      Assert.assertTrue(  "e hasComposite"  , e.hasComposite()  );
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
  } // public void testAddUnionWithChildAndCompositeChildAndCompositeParent()

  public void testAddUnionWithChildAndCompositeChildAndParent() {
    LOG.info("testAddUnionWithChildAndCompositeChildAndParent");
    try {
      runCompositeMembershipChangeLogConsumer();

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
      d.addCompositeMember(CompositeType.INTERSECTION, a, b);
      e.addCompositeMember(CompositeType.INTERSECTION, c, d);
      runCompositeMembershipChangeLogConsumer();

      f.addMember(eSubj);
      runCompositeMembershipChangeLogConsumer();

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
      T.amount("d members", 0, d.getMembers().size());
  
      Assert.assertFalse( "e !isComposite"  , e.isComposite()   );
      Assert.assertTrue(  "e hasComposite"  , e.hasComposite()  );
      T.amount("e members", 0, e.getMembers().size());
  
      Assert.assertFalse( "f !isComposite"  , f.isComposite()   );
      Assert.assertFalse( "f !hasComposite" , f.hasComposite()  );
      T.amount("f members", 1, f.getMembers().size());
      Assert.assertTrue("f has eSubj", f.hasMember(eSubj));
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithChildAndCompositeChildAndParent()

  public void testAddUnionWithOneCompositeChildAndCompositeParent() {
    LOG.info("testAddUnionWithOneCompositeChildAndCompositeParent");
    try {
      runCompositeMembershipChangeLogConsumer();

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
      c.addCompositeMember(CompositeType.INTERSECTION, a, b);
      d.addMember(subjC);
      e.addCompositeMember(CompositeType.INTERSECTION, c, d);
      runCompositeMembershipChangeLogConsumer();

      g.addCompositeMember(CompositeType.INTERSECTION, e, f);
      runCompositeMembershipChangeLogConsumer();

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
      T.amount("c members", 0, c.getMembers().size());
  
      Assert.assertTrue(  "d isComposite"   , d.isComposite()   );
      Assert.assertFalse( "d !hasComposite" , d.hasComposite()  );
      T.amount("d members", 1, d.getMembers().size());
      Assert.assertTrue("d has subjC", d.hasMember(subjC));
  
      Assert.assertTrue(  "e isComposite"   , e.isComposite()   );
      Assert.assertTrue(  "e hasComposite"  , e.hasComposite()  );
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
  } // public void testAddUnionWithOneCompositeChildAndCompositeParent()

  public void testAddUnionWithTwoCompositeChildrenAndCompositeParent() {
    LOG.info("testAddUnionWithTwoCompositeChildrenAndCompositeParent");
    try {
      runCompositeMembershipChangeLogConsumer();

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
      c.addCompositeMember(CompositeType.INTERSECTION, a, b);
      d.addMember(subjC);
      e.addMember(subjD);
      f.addCompositeMember(CompositeType.INTERSECTION, d, e);
      g.addCompositeMember(CompositeType.INTERSECTION, c, f);
      i.addCompositeMember(CompositeType.INTERSECTION, g, h);
      runCompositeMembershipChangeLogConsumer();

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
      T.amount("c members", 0, c.getMembers().size());
  
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
      T.amount("f members", 0, f.getMembers().size());
  
      Assert.assertTrue(  "g isComposite"   , g.isComposite()   );
      Assert.assertTrue(  "g hasComposite"  , g.hasComposite()  );
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
  } // public void testAddUnionWithTwoCompositeChildrenAndCompositeParent()

  public void testDelMemberFromChildofComposite() {
    LOG.info("testDelMemberFromChildofComposite");
    try {
      runCompositeMembershipChangeLogConsumer();

      R       r     = R.populateRegistry(1, 3, 1);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Subject subjA = r.getSubject("a");
  
      a.addMember(subjA);
      b.addMember(subjA);
      c.addCompositeMember(CompositeType.INTERSECTION, a, b);
      runCompositeMembershipChangeLogConsumer();

      a.deleteMember(subjA);
      runCompositeMembershipChangeLogConsumer();
  
      Assert.assertTrue(  "a isComposite"   , a.isComposite()   );
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      T.amount("a members", 0, a.getMembers().size());
  
      Assert.assertTrue(  "b isComposite"   , b.isComposite()   );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      T.amount("b members", 1, b.getMembers().size());
      Assert.assertTrue("b has subjA", b.hasMember(subjA));
  
      Assert.assertFalse( "c !isComposite"  , c.isComposite()   );
      Assert.assertTrue(  "c hasComposite"  , c.hasComposite()  );
      T.amount("c members", 0, c.getMembers().size());
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDelMemberFromChildofComposite()

  public void testDelUnionWithChildAndCompositeChildAndCompositeParent() {
    LOG.info("testDelUnionWithChildAndCompositeChildAndCompositeParent");
    try {
      runCompositeMembershipChangeLogConsumer();

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
      d.addCompositeMember(CompositeType.INTERSECTION, a, b);
      e.addCompositeMember(CompositeType.INTERSECTION, c, d);
      g.addCompositeMember(CompositeType.INTERSECTION, e, f);
      runCompositeMembershipChangeLogConsumer();

      e.deleteCompositeMember();
      runCompositeMembershipChangeLogConsumer();
  
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
      T.amount("d members", 0, d.getMembers().size());
  
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
      runCompositeMembershipChangeLogConsumer();

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
      d.addCompositeMember(CompositeType.INTERSECTION, a, b);
      e.addCompositeMember(CompositeType.INTERSECTION, c, d);
      f.addMember(eSubj);
      runCompositeMembershipChangeLogConsumer();

      e.deleteCompositeMember();
      runCompositeMembershipChangeLogConsumer();
  
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
      T.amount("d members", 0, d.getMembers().size());
  
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

  public void testDelUnionWithOneCompositeChildAndCompositeParent() {
    LOG.info("testDelUnionWithOneCompositeChildAndCompositeParent");
    try {
      runCompositeMembershipChangeLogConsumer();

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
      c.addCompositeMember(CompositeType.INTERSECTION, a, b);
      d.addMember(subjC);
      e.addCompositeMember(CompositeType.INTERSECTION, c, d);
      g.addCompositeMember(CompositeType.INTERSECTION, e, f);
      runCompositeMembershipChangeLogConsumer();

      c.deleteCompositeMember();
      runCompositeMembershipChangeLogConsumer();
  
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
  } // public void testDelUnionWithOneCompositeChildAndCompositeParent()

  public void testDelUnionWithTwoCompositeChildrenAndCompositeParent() {
    LOG.info("testDelUnionWithTwoCompositeChildrenAndCompositeParent");
    try {
      runCompositeMembershipChangeLogConsumer();

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
      c.addCompositeMember(CompositeType.INTERSECTION, a, b);
      d.addMember(subjC);
      e.addMember(subjD);
      f.addCompositeMember(CompositeType.INTERSECTION, d, e);
      g.addCompositeMember(CompositeType.INTERSECTION, c, f);
      i.addCompositeMember(CompositeType.INTERSECTION, g, h);
      runCompositeMembershipChangeLogConsumer();

      g.deleteCompositeMember();
      runCompositeMembershipChangeLogConsumer();
  
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
      T.amount("c members", 0, c.getMembers().size());
  
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
      T.amount("f members", 0, f.getMembers().size());
  
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
      runCompositeMembershipChangeLogConsumer();

      R     r = R.populateRegistry(1, 3, 1);
      Group a = r.getGroup("a", "a");
      Group b = r.getGroup("a", "b");
      Group c = r.getGroup("a", "c");
      a.addCompositeMember(CompositeType.INTERSECTION, b, c);
      runCompositeMembershipChangeLogConsumer();

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

  public void testAddUnionWithNoChildrenAndNoParents() {
    LOG.info("testAddUnionWithNoChildrenAndNoParents");
    try {
      runCompositeMembershipChangeLogConsumer();

      R     r = R.populateRegistry(1, 3, 0);
      Group a = r.getGroup("a", "a");
      Group b = r.getGroup("a", "b");
      Group c = r.getGroup("a", "c");
      a.addCompositeMember(CompositeType.INTERSECTION, b, c);
      runCompositeMembershipChangeLogConsumer();

      Assert.assertTrue(  "a hasComposite"  , a.hasComposite()  );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      Assert.assertFalse( "c !hasComposite" , c.hasComposite()  );
      Assert.assertFalse( "a !isComposite"  , a.isComposite()   );
      Assert.assertTrue(  "b isComposite"   , b.isComposite()   );
      Assert.assertTrue(  "c isComposite"   , c.isComposite()   );
      T.amount("a members", 0, a.getMembers().size());
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithNoChildrenAndNoParents()

  public void testFailToAddCompositeMemberWhenHasMember() {
    LOG.info("testFailToAddCompositeMemberWhenHasMember");
    try {
      runCompositeMembershipChangeLogConsumer();

      R     r = R.populateRegistry(1, 3, 1);
      Group a = r.getGroup("a", "a");
      Group b = r.getGroup("a", "b");
      Group c = r.getGroup("a", "c");
      a.addMember( r.getSubject("a") );
      runCompositeMembershipChangeLogConsumer();

      try {
        a.addCompositeMember(CompositeType.INTERSECTION, b, c);
        Assert.fail("FAIL: expected exception: " + E.GROUP_ACTM);
      }
      catch (MemberAddException eMA) {
        Assert.assertTrue("OK: cannot add composite member to group with mship", true);
        assertContains("error message", eMA.getMessage(), E.GROUP_ACTM);
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailToAddCompositeMemberWhenHasMember()

  public void testFailToDeleteCompositeWhenHasMember() {
    LOG.info("testFailToDeleteCompositeWhenHasMember");
    try {
      runCompositeMembershipChangeLogConsumer();

      R     r = R.populateRegistry(1, 3, 1);
      Group a = r.getGroup("a", "a");
      a.addMember( r.getSubject("a") );
      runCompositeMembershipChangeLogConsumer();

      try {
        a.deleteCompositeMember();
        Assert.fail("FAIL: expected exception: " + E.GROUP_DCFC);
      }
      catch (MemberDeleteException eMD) {
        Assert.assertTrue("OK: cannot del composite from group with member", true);
        assertContains("error message", eMD.getMessage(), E.GROUP_DCFC);
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailToDeleteCompositeWhenHasMember()

  public void testFailToDeleteCompositeWhenNotComposite() {
    LOG.info("testFailToDeleteCompositeWhenNotComposite");
    try {
      runCompositeMembershipChangeLogConsumer();

      R     r = R.populateRegistry(1, 3, 1);
      Group a = r.getGroup("a", "a");
      try {
        a.deleteCompositeMember();
        Assert.fail("FAIL: expected exception: " + E.GROUP_DCFC);
      }
      catch (MemberDeleteException eMD) {
        Assert.assertTrue("OK: cannot del composite from group without composite", true);
        assertContains("error message", eMD.getMessage(), E.GROUP_DCFC);
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailToDeleteCompositeWhenNotComposite()

  public void testFailToDeleteMemberWhenHasComposite() {
    LOG.info("testFailToDeleteMemberWhenHasComposite");
    try {
      runCompositeMembershipChangeLogConsumer();

      R     r = R.populateRegistry(1, 3, 1);
      Group a = r.getGroup("a", "a");
      Group b = r.getGroup("a", "b");
      Group c = r.getGroup("a", "c");
      a.addCompositeMember(CompositeType.INTERSECTION, b, c);
      runCompositeMembershipChangeLogConsumer();

      try {
        a.deleteMember( r.getSubject("a") );
        Assert.fail("FAIL: expected exception: " + E.GROUP_DMFC);
      }
      catch (MemberDeleteException eMD) {
        Assert.assertTrue("OK: cannot del member from composite mship", true);
        assertContains("error message", eMD.getMessage(), E.GROUP_DMFC);
      } finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailToDeleteMemberWhenHasComposite()

} // public class TestCompositeI0

