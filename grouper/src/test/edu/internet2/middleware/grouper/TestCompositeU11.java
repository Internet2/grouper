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

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestCompositeU11.java,v 1.13 2008-09-29 03:38:27 mchyzer Exp $
 */
public class TestCompositeU11 extends GrouperTest {

  // Private Static Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestCompositeU11.class);

  public TestCompositeU11(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

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
      Membership ms = MembershipFinder.findCompositeMembership(r.rs, a, subjA);
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

}

