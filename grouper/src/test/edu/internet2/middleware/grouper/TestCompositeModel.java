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
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author  blair christensen.
 * @version $Id: TestCompositeModel.java,v 1.2 2009-03-20 19:56:40 mchyzer Exp $
 */
public class TestCompositeModel extends GrouperTest {

  public static void main(String[] args) {
    TestRunner.run(TestCompositeModel.class);
  }
  
  // Private Static Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestCompositeModel.class);

  public TestCompositeModel(String name) {
    super(name);
  }

  public void testFailInvalidSession() {
    LOG.info("testFailInvalidSession");
    assertTrue("TODO 20070131 this test no longer works", true);
/*
    try {
      new Composite(null, null, null, null, null);
      Assert.fail("created composite with invalid session");
    }
    catch (IllegalStateException eIS) {
      Assert.assertTrue("OK: did not create composite with null session", true);  
    }
    catch (Exception e) {
      T.e(e);
    }
*/
  } // public void testFailInvalidSession()

  public void testFailNullOwner() {
      LOG.info("testFailNullOwner");
      assertTrue("TODO 20070131 this test no longer works", true);
  /*
      try {
        R r = R.populateRegistry(0, 0, 0);
        new Composite(r.rs, null, null, null, null);
        r.rs.stop();
        Assert.fail("created composite with null owner");
      }
      catch (ModelException eM) {
        Assert.assertTrue("OK: did not create composite with null owner", true);
        T.string("error message", E.COMP_O, eM.getMessage());
      }
      catch (Exception e) {
        T.e(e);
      }
  */
    } // public void testFailNullOwner()

  public void testIntersectionComposite() {
      LOG.info("testIntersectionComposite");
      assertTrue("TODO 20070131 this test no longer works", true);
  /*
      try {
        R         r     = R.populateRegistry(1, 2, 0);
        Owner     owner = r.ns;
        Owner     left  = r.getGroup("a", "a");
        Owner     right = r.getGroup("a", "b");
        Composite c     = new Composite(
          r.rs, owner, left, right, CompositeType.INTERSECTION
        );
        Assert.assertTrue("created intersection composite", true);
        Assert.assertTrue("instanceof Composite", c instanceof Composite);
        Assert.assertEquals( "owner", owner.getUuid(),            c.getOwner() );
        Assert.assertEquals( "left",  left.getUuid(),             c.getLeft()  );
        Assert.assertEquals( "right", right.getUuid(),            c.getRight() );
        Assert.assertEquals( "type",  CompositeType.INTERSECTION, c.getType()  );
        r.rs.stop();
      }
      catch (ModelException eM) {
        Assert.fail("could not create intersection composite: " + eM.getMessage());
      }
      catch (Exception e) {
        T.e(e);
      }
  */
    } // public void testIntersectionComposite()

  public void testComplementComposite() {
      LOG.info("testComplementComposite");
      assertTrue("TODO 20070131 this test no longer works", true);
  /*
      try {
        R         r     = R.populateRegistry(1, 2, 0);
        Owner     owner = r.ns;
        Owner     left  = r.getGroup("a", "a");
        Owner     right = r.getGroup("a", "b");
        Composite c     = new Composite(
          r.rs, owner, left, right, CompositeType.COMPLEMENT
        );
        Assert.assertTrue("created complement composite", true);
        Assert.assertTrue("instanceof Composite", c instanceof Composite);
        Assert.assertEquals("owner",  owner.getUuid(),          c.getOwner() );
        Assert.assertEquals("left",   left.getUuid(),           c.getLeft()  );
        Assert.assertEquals("right",  right.getUuid(),          c.getRight() );
        Assert.assertEquals("type",   CompositeType.COMPLEMENT, c.getType()  );
        r.rs.stop();
      }
      catch (ModelException eM) {
        Assert.fail("could not create complement composite: " + eM.getMessage());
      }
      catch (Exception e) {
        T.e(e);
      }
  */
    } // public void testComplementComposite()

  public void testFailInvalidType() {
      LOG.info("testFailInvalidType");
      assertTrue("TODO 20070131 this test no longer works", true);
  /*
      try {
        R r = R.populateRegistry(1, 2, 0);
        new Composite(
          r.rs, r.ns, r.getGroup("a", "a"), r.getGroup("a", "b"), 
          CompositeType.getInstance("invalid type")
        );
        r.rs.stop();
        Assert.fail("created composite with null type");
      }
      catch (ModelException eM) {
        Assert.assertTrue("OK: did not create composite with invalid type", true);
        T.string("error message", E.COMP_T, eM.getMessage());
      }
      catch (Exception e) {
        Assert.fail("unexpected exception: " + e.getMessage());
      }
  */
    } // public void testFailInvalidType()

  public void testFailLeftEqualsRight() {
      LOG.info("testFailLeftEqualsRight");
      assertTrue("TODO 20070131 this test no longer works", true);
  /*
      try {
        R r = R.populateRegistry(1, 2, 0);
        new Composite(
          r.rs, r.getGroup("a", "a"), r.getGroup("a", "b"), r.getGroup("a", "b"), CompositeType.UNION
        );
        r.rs.stop();
        Assert.fail("created composite with left == right");
      }
      catch (ModelException eM) {
        Assert.assertTrue("OK: did not create composite with left == right", true);
        T.string("error message", E.COMP_LR, eM.getMessage());
      }
      catch (Exception e) {
        T.e(e);
      }
  */
    } // public void testFailLeftEqualsRight()

  public void testFailLeftNotGroup() {
      LOG.info("testFailLeftNotGroup");
      assertTrue("TODO 20070131 this test no longer works", true);
  /*
      try {
        R r = R.populateRegistry(1, 2, 0);
        new Composite(
          r.rs, r.ns, r.getStem("a"), r.getGroup("a", "b"), 
          CompositeType.getInstance("union")
        );
        r.rs.stop();
        Assert.fail("created composite with !group left");
      }
      catch (ModelException eM) {
        Assert.assertTrue("OK: did not create composite with !group left", true);
        T.string("error message", E.COMP_LC, eM.getMessage());
      }
      catch (Exception e) {
        Assert.fail("unexpected exception: " + e.getMessage());
      }
  */
    } // public void testFailLeftNotGroup()

  public void testFailNullLeft() {
      LOG.info("testFailNullLeft");
      assertTrue("TODO 20070131 this test no longer works", true);
  /*
      try {
        R r = R.populateRegistry(0, 0, 0);
        new Composite(r.rs, r.ns, null, null, null);
        r.rs.stop();
        Assert.fail("created composite with null left");
      }
      catch (ModelException eM) {
        Assert.assertTrue("OK: did not create composite with null left", true);
        T.string("error message", E.COMP_L, eM.getMessage());
      }
      catch (Exception e) {
        Assert.fail("unexpected exception: " + e.getMessage());
      }
  */
    } // public void testFailNullLeft()

  public void testFailNullRight() {
      LOG.info("testFailNullRight");
      assertTrue("TODO 20070131 this test no longer works", true);
  /*
      try {
        R r = R.populateRegistry(1, 2, 0);
        new Composite(r.rs, r.ns, r.getGroup("a", "a"), null, null);
        r.rs.stop();
        Assert.fail("created composite with null right");
      }
      catch (ModelException eM) {
        Assert.assertTrue("OK: did not create composite with null right", true);
        T.string("error message", E.COMP_R, eM.getMessage());
      }
      catch (Exception e) {
        Assert.fail("unexpected exception: " + e.getMessage());
      }
  */
    } // public void testFailNullRight()

  public void testFailNullType() {
      LOG.info("testFailNullType");
      assertTrue("TODO 20070131 this test no longer works", true);
  /*
      try {
        R r = R.populateRegistry(1, 2, 0);
        new Composite(
          r.rs, r.ns, r.getGroup("a", "a"), r.getGroup("a", "b"), null
        );
        r.rs.stop();
        Assert.fail("created composite with null type");
      }
      catch (ModelException eM) {
        Assert.assertTrue("OK: did not create composite with null type", true);
        T.string("error message", E.COMP_T, eM.getMessage());
      }
      catch (Exception e) {
        Assert.fail("unexpected exception: " + e.getMessage());
      }
  */
    } // public void testFailNullType()

  public void testFailOwnerEqualsLeftFactor() {
      LOG.info("testFailOwnerEqualsLeftFactor");
      assertTrue("TODO 20070131 this test no longer works", true);
  /*
      try {
        R r = R.populateRegistry(1, 2, 0);
        new Composite(
          r.rs, r.getGroup("a", "a"), r.getGroup("a", "a"), r.getGroup("a", "b"), CompositeType.UNION
        );
        r.rs.stop();
        Assert.fail("created composite where left == owner");
      }
      catch (ModelException eM) {
        Assert.assertTrue("OK: cannot create composites where left == owner", true);
        T.string("error message", E.COMP_CL, eM.getMessage());
      }
      catch (Exception e) {
        Assert.fail("unexpected exception: " + e.getMessage());
      }
  */
    } // public void testFailOwnerEqualsLeftFactor()

  public void testFailOwnerEqualsRightFactor() {
      LOG.info("testFailOwnerEqualsRightFactor");
      assertTrue("TODO 20070131 this test no longer works", true);
  /*
      try {
        R r = R.populateRegistry(1, 2, 0);
        new Composite(
          r.rs, r.getGroup("a", "b"), r.getGroup("a", "a"), r.getGroup("a", "b"), CompositeType.UNION
        );
        r.rs.stop();
        Assert.fail("created composite where right == owner");
      }
      catch (ModelException eM) {
        Assert.assertTrue("OK: cannot create composites where right == owner", true);
        T.string("error message", E.COMP_CR, eM.getMessage());
      }
      catch (Exception e) {
        Assert.fail("unexpected exception: " + e.getMessage());
      }
  */
    } // public void testFailOwnerEqualsRightFactor()

  public void testFailOwnerNotGroupOrStem() {
      LOG.info("testFailOwnerNotGroupOrStem");
      assertTrue("TODO 20070131 this test no longer works", true);
  /*
      try {
        R r = R.populateRegistry(1, 2, 0);
        Composite c0 = new Composite(
          r.rs, r.ns, r.getGroup("a", "a"), r.getGroup("a", "b"), CompositeType.UNION
        );
        try {
          new Composite(
          r.rs, c0, r.getGroup("a", "a"), r.getGroup("a", "b"), CompositeType.UNION
          );
          Assert.fail("created composite with composite as owner");
        }
        catch (ModelException eM) {
          Assert.assertTrue("OK: cannot create composites with !(group|stem) as owner", true);
          T.string("error message", E.COMP_OC, eM.getMessage());
        }
        finally {
          r.rs.stop();
        }
      }
      catch (Exception e) {
        T.e(e);
      }
  */
    } // public void testFailOwnerNotGroupOrStem()

  public void testFailRightNotGroup() {
      LOG.info("testFailRightNotGroup");
      assertTrue("TODO 20070131 this test no longer works", true);
  /*
      try {
        R r = R.populateRegistry(1, 2, 0);
        new Composite(
          r.rs, r.ns, r.getGroup("a", "b"), r.getStem("a"),
          CompositeType.getInstance("union")
        );
        r.rs.stop();
        Assert.fail("created composite with !group rifht");
      }
      catch (ModelException eM) {
        Assert.assertTrue("OK: did not create composite with !group right", true);
        T.string("error message", E.COMP_RC, eM.getMessage());
      }
      catch (Exception e) {
        Assert.fail("unexpected exception: " + e.getMessage());
      }
  */
    } // public void testFailRightNotGroup()

  public void testUnionComposite() {
      LOG.info("testUnionComposite");
      assertTrue("TODO 20070131 this test no longer works", true);
  /*
      try {
        R         r     = R.populateRegistry(1, 2, 0);
        Owner     owner = r.ns;
        Owner     left  = r.getGroup("a", "a");
        Owner     right = r.getGroup("a", "b");
        Composite c     = new Composite(
          r.rs, owner, left, right, CompositeType.UNION
        );
        Assert.assertTrue("created union composite", true);
        Assert.assertTrue("instanceof Composite", c instanceof Composite);
        Assert.assertEquals( "owner", owner.getUuid(),      c.getOwner() );
        Assert.assertEquals( "left",  left.getUuid(),       c.getLeft()  ) ;
        Assert.assertEquals( "right", right.getUuid(),      c.getRight() );
        Assert.assertEquals( "type",  CompositeType.UNION,  c.getType()  );
        r.rs.stop();
      }
      catch (ModelException eM) {
        Assert.fail("could not create union composite: " + eM.getMessage());
      }
      catch (Exception e) {
        T.e(e);
      }
  */
    } // public void testUnionComposite()

}

