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
 * @version $Id: MembershipHelper.java,v 1.8 2005-12-06 05:35:03 blair Exp $
 */
public class MembershipHelper {

  // Protected Class Methods

  protected static void testImm(
    GrouperSession s, Group g, Subject subj, String list
  ) 
  {
    try {
      Field   f = FieldFinder.find(list);
      Member  m = MemberFinder.findBySubject(s, subj);
      Assert.assertTrue("m.subj == subj", m.getSubject().equals(subj));
      Assert.assertTrue("g hasMember"   , g.hasMember(subj, f));
      Assert.assertTrue("g hasImmMember", g.hasImmediateMember(subj, f));
      Assert.assertTrue("m isMember"    , m.isMember(g, f));
      Assert.assertTrue("m isImmMember" , m.isImmediateMember(g, f));
      Membership ms = MembershipFinder.findImmediateMembership(
        s, g, subj, f
      );
      Assert.assertNotNull("found ms", ms);
      Assert.assertTrue("ms group   " , ms.getGroup().equals(g) );
      Assert.assertTrue("ms member"   , ms.getMember().equals(m));
      Assert.assertTrue("ms list"     , ms.getList().equals(f)  );
      Assert.assertTrue("ms depth"    , ms.getDepth() == 0      );
      try {
        Group via = ms.getViaGroup();
        Assert.fail("imm ms has via group");
      }
      catch (GroupNotFoundException eGNF) {
        Assert.assertTrue("imm ms has no via group", true);
      }
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // protected static TestImm(s, g, subj, list)

  protected static void testEff(
    GrouperSession s, Group g, Subject subj, String list, Group via, int depth
  ) 
  {
    try {
      // TODO Test via, depth, etc
      Field   f = FieldFinder.find(list);
      Member  m = MemberFinder.findBySubject(s, subj);
      Assert.assertTrue("m.subj == subj", m.getSubject().equals(subj));
      Assert.assertTrue("g hasMember"   , g.hasMember(subj, f));
      Assert.assertTrue("g hasEffMember", g.hasEffectiveMember(subj, f));
      Assert.assertTrue("m isMember"    , m.isMember(g, f));
      Assert.assertTrue("m isEffMember" , m.isEffectiveMember(g, f));
      Membership ms = MembershipFinder.findEffectiveMembership(
        s, g, subj, f, via, depth
      );
      Assert.assertNotNull("found ms", ms);
      Assert.assertTrue("ms group   " , ms.getGroup().equals(g) );
      Assert.assertTrue("ms member"   , ms.getMember().equals(m));
      Assert.assertTrue("ms list"     , ms.getList().equals(f)  );
      Assert.assertTrue("ms depth"    , ms.getDepth() == depth  );
      try {
        Group v = ms.getViaGroup();
        Assert.assertTrue("ms via group", v.equals(via));
      }
      catch (GroupNotFoundException eGNF) {
        Assert.fail("eff ms has no via group");
      }
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // protected static TestImm(s, g, subj, list, via, depth)

  protected static void testNumMship(
    Group g, String list, int m, int i, int e) 
  {
    try {
      testNumMship(g, FieldFinder.find(list), m, i, e);
    }
    catch (Exception e0) {
      Assert.fail(e0.getMessage());
    }
  } // protected static void testNumMship(g, list, m, i, e)
  
  protected static void testNumMship(Group g, Field f, int m, int i, int e) {
    int gotM = g.getMemberships(f).size();
    int gotI = g.getImmediateMemberships(f).size();
    int gotE = g.getEffectiveMemberships(f).size();
    String msg = " mships '" + f.getName() + "' == ";
    Assert.assertTrue(
      g.getName() + msg + gotM + " (exp " + m + ")", gotM == m
    );
    Assert.assertTrue(
      g.getName() + " imm" + msg + gotI + " (exp " + i + ")", gotI == i
    );
    Assert.assertTrue(
      g.getName() + " eff" + msg + gotE + " (exp " + e + ")", gotE == e
    );
  } // protected static void testNumMship(g, f, m, i, e) 
  

  // 'Tis more hateful below
  protected static void testImm(Group g, Subject subj, Member m) {
    // The basics
    Assert.assertTrue("g hasMember m",    g.hasMember(subj));
    Assert.assertTrue("g hasImmMember m", g.hasImmediateMember(subj));
    Assert.assertTrue("m isMember g",     m.isMember(g));
    Assert.assertTrue("m isImmMember g",  m.isImmediateMember(g));
  } // protected static TestImm(g, subj, m)

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

