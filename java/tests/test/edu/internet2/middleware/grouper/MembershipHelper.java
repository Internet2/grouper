/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  junit.framework.*;

/**
 * {@link Group} helper methods for testing the Grouper API.
 * <p />
 * @author  blair christensen.
 * @version $Id: MembershipHelper.java,v 1.6 2005-12-02 17:17:01 blair Exp $
 */
public class MembershipHelper {

  // Protected Class Methods

  protected static void testImm(Group g, Subject subj, Member m) {
    // The basics
    Assert.assertTrue("g hasMember m",    g.hasMember(subj));
    Assert.assertTrue("g hasImmMember m", g.hasImmediateMember(subj));
    Assert.assertTrue("m isMember g",     m.isMember(g));
    Assert.assertTrue("m isImmMember g",  m.isImmediateMember(g));
  } // protected static TestImm(g, m)

  protected static void testEff(Group g, Group gm, Member m) {
    // Get memberships
    Set isMember    = g.toMember().getMemberships();
    Set hasMembers  = gm.getMemberships();
    Iterator iter   = isMember.iterator();
    while (iter.hasNext()) {
      Membership is = (Membership) iter.next();
      // g-isMember hasMember m
      _testEff(is, m);
      Iterator iterHM = hasMembers.iterator();
      while (iterHM.hasNext()) {
      Membership hs = (Membership) iterHM.next();
        // g hasMember m-hasMember
        _testEff(g, hs);
        // g-isMember hasMember m-hasMember
        _testEff(is, hs);
      }
    }
  } // protected static void testEff(g, gm, m)

  protected static void testEffMship(
    GrouperSession s, Group g, Subject subj, Field f, Group v, int d
  ) 
  {
    try {
      Membership  ms  = MembershipFinder.findEffectiveMembership(
        s, g, subj, f, v, d
      );
      Assert.assertTrue("eff mship found", true);
    }
    catch (MembershipNotFoundException eMNF) {
      Assert.fail(
        "eff membership not found: '" + g.getName() + "/" + subj.getName() 
        + "/" + f.getName() + "/" + d + "/" + v.getName() + "'"
      );
    }
  } // protected static void testEffMship(s, g, subj, f, v, d)

  protected static void testImmMship(GrouperSession s, Group g, Group m, Field f) {
    testImmMship(s, g, m.toSubject(), f);
  } // protected static void testImmMship(s, g, m, f)

  protected static void testImmMship(GrouperSession s, Group g, Subject subj, Field f) {
    try {
      Membership  ms  = MembershipFinder.findImmediateMembership(s, g, subj, f);
      Assert.assertTrue("imm mship found", true);
    }
    catch (MembershipNotFoundException eMNF) {
      Assert.fail(
        "imm membership not found: '" + g.getName() + "/" + subj.getName() 
        + "/" + f.getName() + "'"
      );
    }
  } // protected static void testImmMship(s, g, subj, f)

  protected static void testNumMship(Group g, Field f, int m, int i, int e) {
    Assert.assertTrue(
      g.getName() + " mships = " + m,
      g.getMemberships().size() == m
    );
    Assert.assertTrue(
      g.getName() + " imm mships = " + i,
      g.getImmediateMemberships().size() == i
    );
    Assert.assertTrue(
      g.getName() + " eff mships = " + e,
      g.getEffectiveMemberships().size() == e
    );
  } // protected static void testNumMship(g, f, m, i, e) 
  
  
  // Private Class Methods

  private static void _testEff(Group g, Subject subj, Member m) {
    // The basics
    Assert.assertTrue("g hasMember m",    g.hasMember(subj));
    Assert.assertTrue("g hasEffMember m", g.hasEffectiveMember(subj));
    Assert.assertTrue("m isMember g",     m.isMember(g));
    Assert.assertTrue("m isEffMember g",  m.isEffectiveMember(g));
    // Now try for a little more
    Iterator iter = g.getEffectiveMemberships().iterator();
    while (iter.hasNext()) {
      Membership ms = (Membership) iter.next();
      try {
        Assert.assertNotNull("eff ms g !null", ms.getGroup());  
        Assert.assertTrue(
          "eff ms g", 
          ms.getGroup().getUuid().equals(g.getUuid())
        );
      }
      catch (GroupNotFoundException eGNF) {
        Assert.fail("eff ms g: " + eGNF.getMessage());
      }
      try {
        Assert.assertNotNull("eff ms m !null", ms.getMember());
        Assert.assertTrue(
          "eff ms m", 
          ms.getMember().getUuid().equals(m.getUuid())
        );
      }
      catch (MemberNotFoundException eMNF) {
        Assert.fail("eff ms m: " + eMNF.getMessage());
      }
      Assert.assertNotNull("eff ms l !null", ms.getList());  
      Assert.assertTrue( "eff ms l", ms.getList().equals(Group.getDefaultList()));
      Assert.assertTrue("eff ms depth", ms.getDepth() > 0);
      try {
        Group via = ms.getViaGroup();
        Assert.assertTrue("eff ms has via", true);
      }
      catch (GroupNotFoundException eGNF) {
        Assert.fail("eff ms has no via");
      }
    }
  } // private static void testEff(g, m)

  private static void _testEff(Group g, Membership ms) {
    try {
      _testEff(g, ms.getMember().getSubject(), ms.getMember());
    }
    catch (MemberNotFoundException eMNF) {
      Assert.fail(eMNF.getMessage());
    }
    catch (SubjectNotFoundException eSNF) {
      Assert.fail(eSNF.getMessage());
    }
  } // private static void _testEff(g, ms)

  private static void _testEff(Membership ms, Member m) {
    try {
      _testEff(ms.getGroup(), m.getSubject(), m);
    }
    catch (GroupNotFoundException eGNF) {
      Assert.fail(eGNF.getMessage());
    }
    catch (SubjectNotFoundException eSNF) {
      Assert.fail(eSNF.getMessage());
    }
  } // private static void _testEff(ms, m)

  private static void _testEff(Membership gms, Membership mms) {
    try {
      _testEff(gms.getGroup(), mms.getMember().getSubject(), mms.getMember());
    }
    catch (GroupNotFoundException eGNF) {
      Assert.fail(eGNF.getMessage());
    }
    catch (MemberNotFoundException eMNF) {
      Assert.fail(eMNF.getMessage());
    }
    catch (SubjectNotFoundException eSNF) {
      Assert.fail(eSNF.getMessage());
    }
  } // private static void _testEff(gms, mms)

}

