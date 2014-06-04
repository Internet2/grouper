/*******************************************************************************
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
 ******************************************************************************/
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

import edu.internet2.middleware.grouper.exception.CompositeNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberAddException;
import edu.internet2.middleware.grouper.exception.MemberDeleteException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestComposite.java,v 1.2 2009-03-20 19:56:40 mchyzer Exp $
 */
public class TestComposite extends GrouperTest {

  public static void main(String[] args) {
    //TestRunner.run(TestComposite.class);
    TestRunner.run(new TestComposite("testXmlInsert"));
  }
  
  // Private Static Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestComposite.class);

  public TestComposite(String name) {
    super(name);
  }

  public void testCompositeFinderFindAsFactor() {
    LOG.info("testCompositeFinderFindAsFactor");
    try {
      R       r     = R.populateRegistry(1, 4, 0);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Group   gC    = r.getGroup("a", "c");
      gA.addCompositeMember(CompositeType.UNION, gB, gC);
      Group   gD    = r.getGroup("a", "d");
      gD.addCompositeMember(CompositeType.COMPLEMENT, gA, gB);
    
      T.amount("gA asFactor", 1, CompositeFinder.findAsFactor(gA).size()); 
      T.amount("gB asFactor", 2, CompositeFinder.findAsFactor(gB).size()); 
      T.amount("gC asFactor", 1, CompositeFinder.findAsFactor(gC).size()); 
      T.amount("gD asFactor", 0, CompositeFinder.findAsFactor(gD).size()); 

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } 

  public void testCompositeFinderFindAsOwner() {
    LOG.info("testCompositeFinderFindAsOwner");
    try {
      R       r     = R.populateRegistry(1, 4, 0);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Group   gC    = r.getGroup("a", "c");
      gA.addCompositeMember(CompositeType.UNION, gB, gC);
      Group   gD    = r.getGroup("a", "d");
      gD.addCompositeMember(CompositeType.COMPLEMENT, gA, gB);

      // gA
      try {
        Composite c = CompositeFinder.findAsOwner(gA, true);
        Assert.assertNotNull("c !null for gA", c);
      }
      catch (CompositeNotFoundException eCNF) {
        Assert.fail("FAIL: did not find composite for gA");
      }
      // gB
      try {
        CompositeFinder.findAsOwner(gB, true);
        Assert.fail("FAIL: found composite for gB");
      }
      catch (CompositeNotFoundException eCNF) {
        Assert.assertTrue("OK: did not find composite for gB", true);
      }
      // gC
      try {
        CompositeFinder.findAsOwner(gC, true);
        Assert.fail("FAIL: found composite for gC");
      }
      catch (CompositeNotFoundException eCNF) {
        Assert.assertTrue("OK: did not find composite for gC", true);
      }
      // gD
      try {
        Composite c = CompositeFinder.findAsOwner(gD, true);
        Assert.assertNotNull("c !null for gD", c);
      }
      catch (CompositeNotFoundException eCNF) {
        Assert.fail("FAIL: did not find composite for gD");
      }
    
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testCompositeFinderFindAsOwner()

  public void testCompositeGetType() {
    LOG.info("testCompositeGetType");
    try {
      R       r     = R.populateRegistry(1, 3, 0);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Group   gC    = r.getGroup("a", "c");
      gA.addCompositeMember(CompositeType.UNION, gB, gC);

      Composite     c     = CompositeFinder.findAsOwner(gA, true);
      CompositeType type  = c.getType();
      Assert.assertNotNull("type !null", type);
      Assert.assertTrue("type instanceof CompositeType", type instanceof CompositeType);
      Assert.assertEquals("right type", CompositeType.UNION, type);

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testCompositeGetType()

  public void testCompositeGetOwnerGroup() {
    LOG.info("testCompositeGetOwnerGroup");
    try {
      R       r     = R.populateRegistry(1, 3, 0);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Group   gC    = r.getGroup("a", "c");
      gA.addCompositeMember(CompositeType.UNION, gB, gC);

      Composite     c = CompositeFinder.findAsOwner(gA, true);
      Group         g = c.getOwnerGroup();
      Assert.assertNotNull("g !null", g);
      Assert.assertTrue("g instanceof Group", g instanceof Group);
      Assert.assertEquals("owner == gA", gA, g);

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testCompositeGetOwnerGroup()

  public void testCompositeGetLeftGroup() {
    LOG.info("testCompositeGetLeftGroup");
    try {
      R       r     = R.populateRegistry(1, 3, 0);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Group   gC    = r.getGroup("a", "c");
      gA.addCompositeMember(CompositeType.UNION, gB, gC);

      Composite     c = CompositeFinder.findAsOwner(gA, true);
      Group         g = c.getLeftGroup();
      Assert.assertNotNull("g !null", g);
      Assert.assertTrue("g instanceof Group", g instanceof Group);
      Assert.assertEquals("left == gB", gB, g);

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testCompositeGetLeftGroup()

  public void testCompositeGetRightGroup() {
    LOG.info("testCompositeGetRightGroup");
    try {
      R       r     = R.populateRegistry(1, 3, 0);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Group   gC    = r.getGroup("a", "c");
      gA.addCompositeMember(CompositeType.UNION, gB, gC);

      Composite     c = CompositeFinder.findAsOwner(gA, true);
      Group         g = c.getRightGroup();
      Assert.assertNotNull("g !null", g);
      Assert.assertTrue("g instanceof Group", g instanceof Group);
      Assert.assertEquals("right == gC", gC, g);

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testCompositeGetRightGroup()

  public void testFailNotPrivilegedToAddCompositeMember() {
    LOG.info("testFailNotPrivilegedToAddCompositeMember");
    try {
      R               r   = R.populateRegistry(1, 3, 1);
      GrouperSession  nrs = GrouperSession.start( r.getSubject("a") );
      Group           a   = r.getGroup("a", "a");
      a.addCompositeMember(
        CompositeType.COMPLEMENT, r.getGroup("a", "b"), r.getGroup("a", "c")
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
      R               r   = R.populateRegistry(1, 3, 1);
      GrouperSession  nrs = GrouperSession.start( r.getSubject("a") );
      Group           a   = r.getGroup("a", "a");
      a.addCompositeMember(
        CompositeType.COMPLEMENT, r.getGroup("a", "b"), r.getGroup("a", "c")
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
      R     r = R.populateRegistry(1, 3, 0);
      Group a = r.getGroup("a", "a");
      Group b = r.getGroup("a", "b");
      Group c = r.getGroup("a", "c");
      a.addCompositeMember(CompositeType.COMPLEMENT, b, c);
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

  public void testAddUnionWithOneChildAndNoParents() {
    LOG.info("testAddUnionWithOneChildAndNoParents");
    try {
      R       r     = R.populateRegistry(1, 3, 1);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Subject subjA = r.getSubject("a");
      b.addMember(subjA);
      a.addCompositeMember(CompositeType.COMPLEMENT, b, c);
      Assert.assertTrue(  "a hasComposite"  , a.hasComposite()  );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      Assert.assertFalse( "c !hasComposite" , c.hasComposite()  );
      Assert.assertFalse( "a !isComposite"  , a.isComposite()   );
      Assert.assertTrue(  "b isComposite"   , b.isComposite()   );
      Assert.assertTrue(  "c isComposite"   , c.isComposite()   );
      T.amount("a members", 1, a.getMembers().size());
      Assert.assertTrue("a has subjA", a.hasMember(subjA));

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithOneChildAndNoParents()

  public void testDelUnionWithOneChildAndNoParents() {
    LOG.info("testDelUnionWithOneChildAndNoParents");
    try {
      R       r     = R.populateRegistry(1, 3, 1);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Subject subjA = r.getSubject("a");
      b.addMember(subjA);
      a.addCompositeMember(CompositeType.COMPLEMENT, b, c);
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
      b.addMember(subjA);                                     // subjA
      c.addMember(subjB);                                     // subjB
      a.addCompositeMember(CompositeType.UNION, b, c);        // subjA, subjB
      d.addCompositeMember(CompositeType.COMPLEMENT, a, b); // subjA
      T.amount("d members", 1, d.getMembers().size());
      Assert.assertTrue("d hasMember subjB", d.hasMember(subjB));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithCompositeChildAndNoParents()

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
      a.addCompositeMember(CompositeType.COMPLEMENT, b, c);
      d.addCompositeMember(CompositeType.COMPLEMENT, a, b);
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

  public void testAddUnionWithTwoChildrenAndNoParents() {
    LOG.info("testAddUnionWithTwoChildrenAndNoParents");
    try {
      R       r     = R.populateRegistry(1, 3, 1);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Subject subjA = r.getSubject("a");
      b.addMember(subjA);
      c.addMember(subjA);
      a.addCompositeMember(CompositeType.COMPLEMENT, b, c);
      T.amount("a members", 0, a.getMembers().size());
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithTwoChildrenAndNoParents()

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
      a.addCompositeMember(CompositeType.COMPLEMENT, b, c);
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

  public void testAddUnionWithTwoCompositeChildrenAndNoParents() {
    LOG.info("testAddUnionWithTwoCompositeChildrenAndNoParents");
    try {
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
      e.addCompositeMember(CompositeType.COMPLEMENT, a, b);     // subjA
      Group   f     = r.getGroup("a", "f");
      f.addCompositeMember(CompositeType.COMPLEMENT, c, d);     // subjA
      // And our ultimate composite group
      Group   g     = r.getGroup("a", "g");
      g.addCompositeMember(CompositeType.COMPLEMENT, e, f);     // -
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

      T.amount( "a members", 1, a.getImmediateMembers().size() );
      Assert.assertTrue(  "a has subjA",  a.hasMember(subjA)  );
      T.amount( "b members", 1, b.getImmediateMembers().size() );
      Assert.assertTrue(  "b has subjB",  b.hasMember(subjB)  );
      T.amount( "c members", 1, c.getImmediateMembers().size() );
      Assert.assertTrue(  "c has subjA",  c.hasMember(subjA)  );
      T.amount( "d members", 1, d.getImmediateMembers().size() );
      Assert.assertTrue(  "d has subjB",  d.hasMember(subjB)  );
      T.amount( "e members", 1, e.getMembers().size() );
      Assert.assertTrue(  "e has subjA",  e.hasMember(subjA)  );
      T.amount( "f members", 1, f.getMembers().size() );
      Assert.assertTrue(  "f has subjA",  f.hasMember(subjA)  );
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
      e.addCompositeMember(CompositeType.COMPLEMENT, a, b); // subjA - subjB
      Group   f     = r.getGroup("a", "f");
      f.addCompositeMember(CompositeType.COMPLEMENT, c, d); // subjA - subjB
      // And our ultimate composite group
      Group   g     = r.getGroup("a", "g");
      g.addCompositeMember(CompositeType.COMPLEMENT, e, f);
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

      T.amount( "a members", 1, a.getImmediateMembers().size() );
      Assert.assertTrue(  "a has subjA",  a.hasMember(subjA)  );
      T.amount( "b members", 1, b.getImmediateMembers().size() );
      Assert.assertTrue(  "b has subjB",  b.hasMember(subjB)  );
      T.amount( "c members", 1, c.getImmediateMembers().size() );
      Assert.assertTrue(  "c has subjA",  c.hasMember(subjA)  );
      T.amount( "d members", 1, d.getImmediateMembers().size() );
      Assert.assertTrue(  "d has subjB",  d.hasMember(subjB)  );
      T.amount( "e members", 1, e.getMembers().size() );
      Assert.assertTrue(  "e has subjA",  e.hasMember(subjA)  );
      T.amount( "f members", 1, f.getMembers().size() );
      Assert.assertTrue(  "f has subjA",  f.hasMember(subjA)  );
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
      R       r     = R.populateRegistry(1, 4, 0);
      // Feeder Groups
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      // Composite Group
      Group   c     = r.getGroup("a", "c");
      c.addCompositeMember(CompositeType.COMPLEMENT, a, b);
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
      T.amount( "d members", 1, d.getImmediateMembers().size() );
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
      R       r     = R.populateRegistry(1, 4, 0);
      // Feeder Groups
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      // Composite Group
      Group   c     = r.getGroup("a", "c");
      c.addCompositeMember(CompositeType.COMPLEMENT, a, b);
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
      T.amount( "d members", 1, d.getImmediateMembers().size() );
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
      R       r     = R.populateRegistry(1, 5, 0);
      // Feeder Groups
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      // Composite Group
      Group   d     = r.getGroup("a", "d");
      d.addCompositeMember(CompositeType.COMPLEMENT, a, b);
      // Parent Composite Group
      Group   e     = r.getGroup("a", "e");
      e.addCompositeMember(CompositeType.COMPLEMENT, c, d);

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
      d.addCompositeMember(CompositeType.COMPLEMENT, a, b);
      // Parent Composite Group
      Group   e     = r.getGroup("a", "e");
      e.addCompositeMember(CompositeType.COMPLEMENT, c, d);

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
      c.addCompositeMember(CompositeType.COMPLEMENT, a, b); // subjA - 
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

      T.amount( "a members", 1, a.getImmediateMembers().size() );
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
      c.addCompositeMember(CompositeType.COMPLEMENT, a, b);
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

      T.amount( "a members", 1, a.getImmediateMembers().size() );
      Assert.assertTrue("a has subjA", a.hasMember(subjA));
      T.amount( "b members", 0, b.getMembers().size() );
      T.amount( "c members", 0, c.getMembers().size() );
      T.amount( "d members", 1, d.getImmediateMembers().size() );
      Assert.assertTrue("d has cSubj", d.hasMember(cSubj));

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDelUnionWithOneChildAndParent()

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
      c.addCompositeMember(CompositeType.COMPLEMENT, a, b); // subjA - 
      // Parent Feeder Group
      Group   d     = r.getGroup("a", "d");
      // Parent Group
      Group   e     = r.getGroup("a", "e");
      e.addCompositeMember(CompositeType.COMPLEMENT, c, d); // subjA - 

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
      c.addCompositeMember(CompositeType.COMPLEMENT, a, b);
      // Parent Feeder Group
      Group   d     = r.getGroup("a", "d");
      // Parent Group
      Group   e     = r.getGroup("a", "e");
      e.addCompositeMember(CompositeType.COMPLEMENT, c, d);

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
  public void testAddUnionWithTwoChildrenAndParent() {
    LOG.info("testAddUnionWithTwoChildrenAndParent");
    try {
      R       r     = R.populateRegistry(1, 4, 1);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Subject cSubj = c.toSubject();
      Group   d     = r.getGroup("a", "d");
      Subject subjA = r.getSubject("a");

      a.addMember(subjA);
      b.addMember(subjA);
      c.addCompositeMember(CompositeType.COMPLEMENT, a, b); // subjA - subjA
      d.addMember(cSubj);

      T.amount("a members", 1, a.getMembers().size());
      Assert.assertTrue("a has subjA", a.hasMember(subjA));

      T.amount("b members", 1, b.getMembers().size());
      Assert.assertTrue("b has subjA", b.hasMember(subjA));

      T.amount("c members", 0, c.getMembers().size());
      T.amount("c comp members", 0, c.getCompositeMembers().size());
      T.amount("c comp mships", 0, c.getCompositeMemberships().size());

      T.amount("d members", 1, d.getMembers().size());
      Assert.assertTrue("d has cSubj", d.hasMember(cSubj));

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithTwoChildrenAndParent()
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
      c.addCompositeMember(CompositeType.COMPLEMENT, a, b);
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
  public void testAddUnionWithTwoChildrenAndCompositeParent() {
    LOG.info("testAddUnionWithTwoChildrenAndCompositeParent");
    try {
      R       r     = R.populateRegistry(1, 5, 1);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Group   d     = r.getGroup("a", "d");
      Group   e     = r.getGroup("a", "e");
      Subject subjA = r.getSubject("a");

      a.addMember(subjA);
      b.addMember(subjA);
      c.addCompositeMember(CompositeType.COMPLEMENT, a, b); // subjA - subjA
      e.addCompositeMember(CompositeType.COMPLEMENT, c, d); // - - -

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
  } // public void testAddUnionWithTwoChildrenAndCompositeParent()
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
      c.addCompositeMember(CompositeType.COMPLEMENT, a, b);
      e.addCompositeMember(CompositeType.COMPLEMENT, c, d);
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
      c.addCompositeMember(CompositeType.COMPLEMENT, a, b); // subjA - subjB
      d.addMember(subjC);
      e.addCompositeMember(CompositeType.COMPLEMENT, c, d); // subjA - subjC
      g.addCompositeMember(CompositeType.COMPLEMENT, e, f); // subjA - -

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
      T.amount("c members", 1, c.getMembers().size());
      Assert.assertTrue("c has subjA", c.hasMember(subjA));

      Assert.assertTrue(  "d isComposite"   , d.isComposite()   );
      Assert.assertFalse( "d !hasComposite" , d.hasComposite()  );
      T.amount("d members", 1, d.getMembers().size());
      Assert.assertTrue("d has subjC", d.hasMember(subjC));

      Assert.assertTrue(  "e isComposite"   , e.isComposite()   );
      Assert.assertTrue(  "e hasComposite"  , e.hasComposite()  );
      T.amount("e members", 1, e.getMembers().size());
      Assert.assertTrue("e hasMember subjA", e.hasMember(subjA));

      Assert.assertTrue(  "f isComposite"   , f.isComposite()   );
      Assert.assertFalse( "f !hasComposite" , f.hasComposite()  );
      T.amount("f members", 0, f.getMembers().size());

      Assert.assertFalse( "g !isComposite"  , g.isComposite()   );
      Assert.assertTrue(  "g hasComposite"  , g.hasComposite()  );
      T.amount("g members", 1, g.getMembers().size());
      Assert.assertTrue("g hasMember subjA", g.hasMember(subjA));

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithOneCompositeChildAndCompositeParent()
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
      c.addCompositeMember(CompositeType.COMPLEMENT, a, b);
      d.addMember(subjC);
      e.addCompositeMember(CompositeType.COMPLEMENT, c, d);
      g.addCompositeMember(CompositeType.COMPLEMENT, e, f);
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
      d.addCompositeMember(CompositeType.COMPLEMENT, a, b); // subjA - subjB
      e.addCompositeMember(CompositeType.COMPLEMENT, c, d); // subjC - subjA
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
      T.amount("d members", 1, d.getMembers().size());
      Assert.assertTrue("d hasMember subjA", d.hasMember(subjA));

      Assert.assertFalse( "e !isComposite"  , e.isComposite()   );
      Assert.assertTrue(  "e hasComposite"  , e.hasComposite()  );
      T.amount("e members", 1, e.getMembers().size());
      Assert.assertTrue("e hasMember subjC", e.hasMember(subjC));

      Assert.assertFalse( "f !isComposite"  , f.isComposite()   );
      Assert.assertFalse( "f !hasComposite" , f.hasComposite()  );
      T.amount("f members", 2, f.getMembers().size());
      Assert.assertTrue("f has eSubj", f.hasMember(eSubj));
      Assert.assertTrue("f has subjC", f.hasMember(subjC));

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithChildAndCompositeChildAndParent()
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
      d.addCompositeMember(CompositeType.COMPLEMENT, a, b); // subjA - subjB
      e.addCompositeMember(CompositeType.COMPLEMENT, c, d); // subjC - subjA
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
      T.amount("d members", 1, d.getMembers().size());
      Assert.assertTrue("d hasMember subjA", d.hasMember(subjA));

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
      d.addCompositeMember(CompositeType.COMPLEMENT, a, b); // subjA - subjB
      e.addCompositeMember(CompositeType.COMPLEMENT, c, d); // subjC - subjA
      g.addCompositeMember(CompositeType.COMPLEMENT, e, f); // subjC - -

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
      T.amount("d members", 1, d.getMembers().size());
      Assert.assertTrue("d hasMember subjA", d.hasMember(subjA));

      Assert.assertTrue(  "e isComposite"   , e.isComposite()   );
      Assert.assertTrue(  "e hasComposite"  , e.hasComposite()  );
      T.amount("e members", 1, e.getMembers().size());
      Assert.assertTrue("e hasMember subjC", e.hasMember(subjC));

      Assert.assertTrue(  "f isComposite"   , f.isComposite()   );
      Assert.assertFalse( "f !hasComposite" , f.hasComposite()  );
      T.amount("f members", 0, f.getMembers().size());

      Assert.assertFalse( "g !isComposite"  , g.isComposite()   );
      Assert.assertTrue(  "g hasComposite"  , g.hasComposite()  );
      T.amount("g members", 1, g.getMembers().size());
      Assert.assertTrue("g hasMember subjC", g.hasMember(subjC));

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithChildAndCompositeChildAndCompositeParent()
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
      d.addCompositeMember(CompositeType.COMPLEMENT, a, b); // subjA - subjB
      e.addCompositeMember(CompositeType.COMPLEMENT, c, d); // subjC - subjA
      g.addCompositeMember(CompositeType.COMPLEMENT, e, f); // subJC - -
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
      T.amount("d members", 1, d.getMembers().size());
      Assert.assertTrue("d hasMember subjA", d.hasMember(subjA));

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
      c.addCompositeMember(CompositeType.COMPLEMENT, a, b); // subjA - subjB
      d.addMember(subjC);
      e.addMember(subjD);
      f.addCompositeMember(CompositeType.COMPLEMENT, d, e); // subjC - subjD
      g.addCompositeMember(CompositeType.COMPLEMENT, c, f); // subjA - subjC
      i.addCompositeMember(CompositeType.COMPLEMENT, g, h); // subjA - -

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
      T.amount("c members", 1, c.getMembers().size());
      Assert.assertTrue("c hasMember subjA", c.hasMember(subjA));

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
      T.amount("f members", 1, f.getMembers().size());
      Assert.assertTrue("f hasMember subjC", f.hasMember(subjC));

      Assert.assertTrue(  "g isComposite"   , g.isComposite()   );
      Assert.assertTrue(  "g hasComposite"  , g.hasComposite()  );
      T.amount("g members", 1, g.getMembers().size());
      Assert.assertTrue("g hasMember subjA", g.hasMember(subjA));

      Assert.assertTrue(  "h isComposite"   , h.isComposite()   );
      Assert.assertFalse( "h !hasComposite" , h.hasComposite()  );
      T.amount("h members", 0, h.getMembers().size());

      Assert.assertFalse( "i !isComposite"  , i.isComposite()   );
      Assert.assertTrue(  "i hasComposite"  , i.hasComposite()  );
      T.amount("i members", 1, i.getMembers().size());
      Assert.assertTrue("i hasMember subjA", i.hasMember(subjA));

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithTwoCompositeChildrenAndCompositeParent()
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
      c.addCompositeMember(CompositeType.COMPLEMENT, a, b); // subjA - subjB
      d.addMember(subjC);
      e.addMember(subjD);
      f.addCompositeMember(CompositeType.COMPLEMENT, d, e); // subjC - subjD
      g.addCompositeMember(CompositeType.COMPLEMENT, c, f); // subjA - subjC
      i.addCompositeMember(CompositeType.COMPLEMENT, g, h); // subjA - -
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
      T.amount("c members", 1, c.getMembers().size());
      Assert.assertTrue("c hasMember subjA", c.hasMember(subjA));

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
      T.amount("f members", 1, f.getMembers().size());
      Assert.assertTrue("f hasMember subjC", f.hasMember(subjC));

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
  public void testAddMemberToChildOfComposite() {
    LOG.info("testAddMemberToChildOfComposite");
    try {
      R       r     = R.populateRegistry(1, 3, 2);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
  
      a.addMember(subjA);
      b.addMember(subjB);
      c.addCompositeMember(CompositeType.COMPLEMENT, a, b); // subjA - subjB
      a.addMember(subjB);
  
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
      Assert.assertTrue("c has subjA", c.hasMember(subjA));

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddMemberToChildOfComposite()

  public void testFailToAddMemberWhenHasComposite() {
    LOG.info("testFailToAddMemberWhenHasComposite");
    try {
      R     r = R.populateRegistry(1, 3, 1);
      Group a = r.getGroup("a", "a");
      Group b = r.getGroup("a", "b");
      Group c = r.getGroup("a", "c");
      a.addCompositeMember(CompositeType.COMPLEMENT, b, c);
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
  public void testDelMemberFromChildofComposite() {
    LOG.info("testDelMemberFromChildofComposite");
    try {
      R       r     = R.populateRegistry(1, 3, 1);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Subject subjA = r.getSubject("a");
  
      a.addMember(subjA);
      b.addMember(subjA);
      c.addCompositeMember(CompositeType.COMPLEMENT, a, b);
      a.deleteMember(subjA);
  
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

  public void testFailToDeleteMemberWhenHasComposite() {
    LOG.info("testFailToDeleteMemberWhenHasComposite");
    try {
      R     r = R.populateRegistry(1, 3, 1);
      Group a = r.getGroup("a", "a");
      Group b = r.getGroup("a", "b");
      Group c = r.getGroup("a", "c");
      a.addCompositeMember(CompositeType.COMPLEMENT, b, c);
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
  public void testFailToAddCompositeMemberWhenHasMember() {
    LOG.info("testFailToAddCompositeMemberWhenHasMember");
    try {
      R     r = R.populateRegistry(1, 3, 1);
      Group a = r.getGroup("a", "a");
      Group b = r.getGroup("a", "b");
      Group c = r.getGroup("a", "c");
      a.addMember( r.getSubject("a") );
      try {
        a.addCompositeMember(CompositeType.COMPLEMENT, b, c);
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
      R     r = R.populateRegistry(1, 3, 1);
      Group a = r.getGroup("a", "a");
      a.addMember( r.getSubject("a") );
      try {
        a.deleteCompositeMember();
        Assert.fail("FAIL: expected exception: " + E.GROUP_DCFC);
      }
      catch (MemberDeleteException eMD) {
        Assert.assertTrue("OK: cannot del composite from group with member", true);
        assertContains("error message: " + GrouperUtil.getFullStackTrace(eMD), eMD.getMessage(), E.GROUP_DCFC);
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
  public void testAddUnionWithNoChildrenAndNoParents() {
    LOG.info("testAddUnionWithNoChildrenAndNoParents");
    try {
      R     r = R.populateRegistry(1, 3, 0);
      Group a = r.getGroup("a", "a");
      Group b = r.getGroup("a", "b");
      Group c = r.getGroup("a", "c");
      a.addCompositeMember(CompositeType.COMPLEMENT, b, c);
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

  
  /**
   * make an example composite for testing
   * @return an example composite
   */
  public static Composite exampleComposite() {
    Composite composite = new Composite();
    composite.setContextId("contextId");
    composite.setCreateTime(3L);
    composite.setCreatorUuid("creatorId");
    composite.setHibernateVersionNumber(3L);
    composite.setLeftFactorUuid("leftFactor");
    composite.setFactorOwnerUuid("owner");
    composite.setRightFactorUuid("rightFactor");
    composite.setTypeDb("type");
    composite.setUuid("uuid");
    
    return composite;
  }
  
  /**
   * make an example composite for testing
   * @return an example composite
   */
  public static Composite exampleCompositeDb() {
    Group owner = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:owner").assignName("test:owner").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    Group left = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:left").assignName("test:left").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    Group right = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:right").assignName("test:right").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    Composite composite = GrouperDAOFactory.getFactory().getComposite().findByUuidOrName(null, owner.getId(),
        left.getId(), right.getId(), CompositeType.COMPLEMENT.getName(), false);
    if (composite == null) {
      composite = owner.addCompositeMember(CompositeType.COMPLEMENT, left, right);
    }
    return composite;
  }

  
  /**
   * make an example composite for testing
   * @return an example composite
   */
  public static Composite exampleRetrieveCompositeDb() {
    Group owner = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
    .assignGroupNameToEdit("test:owner").assignName("test:owner").assignCreateParentStemsIfNotExist(true)
    .assignDescription("description").save();
  Group left = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
    .assignGroupNameToEdit("test:left").assignName("test:left").assignCreateParentStemsIfNotExist(true)
    .assignDescription("description").save();
  Group right = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
    .assignGroupNameToEdit("test:right").assignName("test:right").assignCreateParentStemsIfNotExist(true)
    .assignDescription("description").save();
  Composite composite = GrouperDAOFactory.getFactory().getComposite().findByUuidOrName(null, owner.getId(),
      left.getId(), right.getId(), CompositeType.COMPLEMENT.getName(), true);
  return composite;
  }

  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlInsert() {
    
    GrouperSession.startRootSession();
    
    Group owner = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:ownerInsert").assignName("test:ownerInsert").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    Group left = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:leftInsert").assignName("test:leftInsert").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    Group right = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:rightInsert").assignName("test:rightInsert").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    Composite compositeOriginal = owner.addCompositeMember(CompositeType.COMPLEMENT, left, right);
    
    //do this because last membership update isnt there, only in db
    compositeOriginal = GrouperDAOFactory.getFactory().getComposite().findByUuidOrName(
        compositeOriginal.getUuid(), null, null, null, null, true);
    Composite compositeCopy = GrouperDAOFactory.getFactory().getComposite().findByUuidOrName(
        compositeOriginal.getUuid(), null, null, null, null, true);
    Composite compositeCopy2 = GrouperDAOFactory.getFactory().getComposite().findByUuidOrName(
        compositeOriginal.getUuid(), null, null, null, null, true);
    owner.deleteCompositeMember();
    
    //lets insert the original
    compositeCopy2.xmlSaveBusinessProperties(null);
    compositeCopy2.xmlSaveUpdateProperties();

    //refresh from DB
    compositeCopy = GrouperDAOFactory.getFactory().getComposite().findByUuidOrName(
        null, owner.getId(), left.getId(), right.getId(), CompositeType.COMPLEMENT.getName(), true);
    
    assertFalse(compositeCopy == compositeOriginal);
    assertFalse(compositeCopy.xmlDifferentBusinessProperties(compositeOriginal));
    assertFalse(compositeCopy.xmlDifferentUpdateProperties(compositeOriginal));
    
  }
  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlDifferentUpdateProperties() {
    
    @SuppressWarnings("unused")
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Composite composite = null;
    Composite exampleComposite = null;

    
    //TEST UPDATE PROPERTIES
    {
      composite = exampleCompositeDb();
      exampleComposite = composite.clone();
      
      composite.setContextId("abc");
      
      assertFalse(composite.xmlDifferentBusinessProperties(exampleComposite));
      assertTrue(composite.xmlDifferentUpdateProperties(exampleComposite));

      composite.setContextId(exampleComposite.getContextId());
      composite.xmlSaveUpdateProperties();

      composite = exampleRetrieveCompositeDb();
      
      assertFalse(composite.xmlDifferentBusinessProperties(exampleComposite));
      assertFalse(composite.xmlDifferentUpdateProperties(exampleComposite));
      
    }
    
    {
      composite = exampleCompositeDb();
      exampleComposite = composite.clone();

      composite.setCreateTime(99);
      
      assertFalse(composite.xmlDifferentBusinessProperties(exampleComposite));
      assertTrue(composite.xmlDifferentUpdateProperties(exampleComposite));

      composite.setCreateTime(exampleComposite.getCreateTime());
      composite.xmlSaveUpdateProperties();
      
      composite = exampleRetrieveCompositeDb();
      
      assertFalse(composite.xmlDifferentBusinessProperties(exampleComposite));
      assertFalse(composite.xmlDifferentUpdateProperties(exampleComposite));
    }
    
    {
      composite = exampleCompositeDb();
      exampleComposite = composite.clone();

      composite.setCreatorUuid("abc");
      
      assertFalse(composite.xmlDifferentBusinessProperties(exampleComposite));
      assertTrue(composite.xmlDifferentUpdateProperties(exampleComposite));

      composite.setCreatorUuid(exampleComposite.getCreatorUuid());
      composite.xmlSaveUpdateProperties();
      
      composite = exampleRetrieveCompositeDb();
      
      assertFalse(composite.xmlDifferentBusinessProperties(exampleComposite));
      assertFalse(composite.xmlDifferentUpdateProperties(exampleComposite));
    }
    
    {
      composite = exampleCompositeDb();
      exampleComposite = composite.clone();

      composite.setHibernateVersionNumber(99L);
      
      assertFalse(composite.xmlDifferentBusinessProperties(exampleComposite));
      assertTrue(composite.xmlDifferentUpdateProperties(exampleComposite));

      composite.setHibernateVersionNumber(exampleComposite.getHibernateVersionNumber());
      composite.xmlSaveUpdateProperties();
      
      composite = exampleRetrieveCompositeDb();
      
      assertFalse(composite.xmlDifferentBusinessProperties(exampleComposite));
      assertFalse(composite.xmlDifferentUpdateProperties(exampleComposite));
    }
    //TEST BUSINESS PROPERTIES
    
    {
      composite = exampleCompositeDb();
      exampleComposite = composite.clone();

      composite.setFactorOwnerUuid("abc");
      
      assertTrue(composite.xmlDifferentBusinessProperties(exampleComposite));
      assertFalse(composite.xmlDifferentUpdateProperties(exampleComposite));

      composite.setFactorOwnerUuid(exampleComposite.getFactorOwnerUuid());
      composite.xmlSaveBusinessProperties(exampleComposite.clone());
      composite.xmlSaveUpdateProperties();
      
      composite = exampleRetrieveCompositeDb();
      
      assertFalse(composite.xmlDifferentBusinessProperties(exampleComposite));
      assertFalse(composite.xmlDifferentUpdateProperties(exampleComposite));
    
    }
    
    {
      composite = exampleCompositeDb();
      exampleComposite = composite.clone();

      composite.setLeftFactorUuid("abc");
      
      assertTrue(composite.xmlDifferentBusinessProperties(exampleComposite));
      assertFalse(composite.xmlDifferentUpdateProperties(exampleComposite));

      composite.setLeftFactorUuid(exampleComposite.getLeftFactorUuid());
      composite.xmlSaveBusinessProperties(exampleComposite.clone());
      composite.xmlSaveUpdateProperties();
      
      composite = exampleRetrieveCompositeDb();
      
      assertFalse(composite.xmlDifferentBusinessProperties(exampleComposite));
      assertFalse(composite.xmlDifferentUpdateProperties(exampleComposite));
    
    }
    
    {
      composite = exampleCompositeDb();
      exampleComposite = composite.clone();

      composite.setRightFactorUuid("abc");
      
      assertTrue(composite.xmlDifferentBusinessProperties(exampleComposite));
      assertFalse(composite.xmlDifferentUpdateProperties(exampleComposite));

      composite.setRightFactorUuid(exampleComposite.getRightFactorUuid());
      composite.xmlSaveBusinessProperties(exampleComposite.clone());
      composite.xmlSaveUpdateProperties();
      
      composite = exampleRetrieveCompositeDb();
      
      assertFalse(composite.xmlDifferentBusinessProperties(exampleComposite));
      assertFalse(composite.xmlDifferentUpdateProperties(exampleComposite));
    
    }
    
    {
      composite = exampleCompositeDb();
      exampleComposite = composite.clone();

      composite.setTypeDb(CompositeType.UNION.name());
      
      assertTrue(composite.xmlDifferentBusinessProperties(exampleComposite));
      assertFalse(composite.xmlDifferentUpdateProperties(exampleComposite));

      composite.setTypeDb(exampleComposite.getTypeDb());
      composite.xmlSaveBusinessProperties(exampleComposite.clone());
      composite.xmlSaveUpdateProperties();
      
      composite = exampleRetrieveCompositeDb();
      
      assertFalse(composite.xmlDifferentBusinessProperties(exampleComposite));
      assertFalse(composite.xmlDifferentUpdateProperties(exampleComposite));
    
    }
    
    {
      composite = exampleCompositeDb();
      exampleComposite = composite.clone();

      composite.setUuid("abc");
      
      assertTrue(composite.xmlDifferentBusinessProperties(exampleComposite));
      assertFalse(composite.xmlDifferentUpdateProperties(exampleComposite));

      composite.setUuid(exampleComposite.getUuid());
      composite.xmlSaveBusinessProperties(exampleComposite.clone());
      composite.xmlSaveUpdateProperties();
      
      composite = exampleRetrieveCompositeDb();
      
      assertFalse(composite.xmlDifferentBusinessProperties(exampleComposite));
      assertFalse(composite.xmlDifferentUpdateProperties(exampleComposite));
    
    }
  }

  
  
}

