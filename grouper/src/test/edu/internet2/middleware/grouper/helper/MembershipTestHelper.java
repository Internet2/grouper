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

package edu.internet2.middleware.grouper.helper;
import java.util.Iterator;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.exception.CompositeNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.MembershipNotFoundException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * {@link Group} helper methods for testing the Grouper API.
 * <p />
 * @author  blair christensen.
 * @version $Id: MembershipTestHelper.java,v 1.5 2009-12-07 07:31:09 mchyzer Exp $
 */
public class MembershipTestHelper {

  // Private Class Constants
  private static final Log LOG = GrouperUtil.getLog(MembershipTestHelper.class);


  // public Class Methods

  public static Set getEff(
    GrouperSession s, Group g, Subject subj, String list, int depth, Group via
  )
  {
    String msg = "getEff: ";
    LOG.debug(msg);
    try {
      Field   f     = FieldFinder.find(list, true);
      Member  m     = MemberFinder.findBySubject(s, subj, true);
      Set     effs  = MembershipFinder.findEffectiveMemberships(
        s, g, subj, f, via, depth
      );
      if (effs.size() == 0) {
        Assert.fail("eff ms not found");
      }
      Iterator iter = effs.iterator();
      while (iter.hasNext()) {
        Membership ms = (Membership) iter.next();
        Assert.assertNotNull(msg + "found ms", ms);
        Assert.assertTrue(msg + "ms group   " , ms.getGroup().equals(g) );
        Assert.assertTrue(msg + "ms member"   , ms.getMember().equals(m));
        Assert.assertTrue(msg + "ms list"     , ms.getList().equals(f)  );
        Assert.assertTrue(msg + "ms depth"    , ms.getDepth() == depth  );
        try {
          Group v = ms.getViaGroup();
          Assert.assertTrue(msg + "ms via group", v.equals(via));
        }
        catch (GroupNotFoundException eGNF) {
          Assert.fail(msg + "eff ms has no via group");
        }
      }
      return effs;
    }
    catch (Exception e) {
      Assert.fail("eff mship not found: " + e.getMessage());
    }
    throw new RuntimeException("eff mship not found");
  } // public static Membership getImm(s, g, subj, list, depth, via)

  public static Membership getImm(
    GrouperSession s, Group g, Subject subj, String list
  )
  {
    String msg = "getImm ";
    try {
      Field       f   = FieldFinder.find(list, true);
      Member      m   = MemberFinder.findBySubject(s, subj, true);
      Membership  ms  = MembershipFinder.findImmediateMembership(
        s, g, subj, f, true
      );
      Assert.assertNotNull(msg + "found ms", ms);
      Assert.assertTrue(msg + "ms group   " , ms.getGroup().equals(g) );
      Assert.assertTrue(msg + "ms member"   , ms.getMember().equals(m));
      Assert.assertTrue(msg + "ms list"     , ms.getList().equals(f)  );
      Assert.assertTrue(msg + "ms depth"    , ms.getDepth() == 0      );
      try {
        ms.getViaGroup();
        Assert.fail(msg + "imm ms has via group");
      }
      catch (GroupNotFoundException eGNF) {
        Assert.assertTrue(msg + "imm ms has no via group", true);
      }
      return ms;
    }
    catch (Exception e) {
      Assert.fail("imm mship not found: " + e.getMessage());
    }
    throw new RuntimeException("imm mship not found");
  } // public static Membership getImm(s, g, subj, list)

  public static void testChildren(Membership parent, Set children) {
    try {
      Set got = parent.getChildMemberships();
      Assert.assertTrue(
        "children == " + got.size() + " (exp " + children.size() + ")",
        children.size() == got.size()
      );
      Iterator iterA = children.iterator();
      while (iterA.hasNext()) {
        Membership child = (Membership) iterA.next();
        if (got.contains(child)) {
          Assert.assertTrue("found child: " + child, true);
        }
        else {
        } 
      }
      Iterator iterB = got.iterator();
      while (iterB.hasNext()) {
        Membership child = (Membership) iterB.next();
        try {
          Assert.assertTrue(
            child + " parent", child.getParentMembership().equals(parent)
          );
        }
        catch (Exception e) {
          Assert.fail(child + " parent: " + e.getMessage());
        } 
      }
    }
    catch (Exception e) {
      Assert.fail("testChildren: " + e.getMessage());
    }
  } // public static void testChildren(parent, children)

  public static void testParent(Membership parent, Membership child) {
    try {
      Membership ms = child.getParentMembership();
      Assert.assertTrue("got parent ms", true);
      Assert.assertNotNull("parent ms !null", ms);
      Assert.assertTrue(
        "parent instanceof Membership", ms instanceof Membership
      );
      Assert.assertTrue("right parent", parent.equals(ms));
    }
    catch (Exception e) {
      Assert.fail("testParent: " + e.getMessage());
    }
  } // public static void testParent(parent, child)

  public static void testNoParent(Membership ms) {
    try {
      Membership parent = ms.getParentMembership();
      Assert.fail(ms + " has parent mship: " + parent);
    }
    catch (MembershipNotFoundException eMNF) {
      Assert.assertTrue("no parent mship", true);
    }
  } // public static void testNoParent(ms)

  public static void testNoChildren(Membership ms) {
    Assert.assertTrue(
      ms + " has no children mships", ms.getChildMemberships().size() == 0
    );
  } // public static void testNoChildren(ms)

  public static void testImm(
    GrouperSession s, Group g, Subject subj, String list
  ) 
  {
    String msg = "testImm ";
    try {
      Field   f   = FieldFinder.find(list, true);
      Member  m   = MemberFinder.findBySubject(s, subj, true);
      msg += "["+g.getName()+"]["+m+"]["+f.getName()+"] ";
      Assert.assertTrue(msg + "m.subj == subj", SubjectHelper.eq( m.getSubject(), subj ) );
      Assert.assertTrue(msg + "g hasMember"   , g.hasMember(subj, f));
      Assert.assertTrue(msg + "g hasImmMember", g.hasImmediateMember(subj, f));
      Assert.assertTrue(msg + "m isMember"    , m.isMember(g, f));
      Assert.assertTrue(msg + "m isImmMember" , m.isImmediateMember(g, f));
      getImm(s, g, subj, list);
    }
    catch (Exception e) {
      Assert.fail(msg + e.getMessage());
    }
  } // public static TestImm(s, g, subj, list)

  public static void testEff(
    GrouperSession s, Group g, Subject subj, String list, Group via, int depth
  ) 
  {
    String msg = "testEff ";
    try {
      Field   f   = FieldFinder.find(list, true);
      Member  m   = MemberFinder.findBySubject(s, subj, true);
      msg += "["+g.getName()+"]["+m+"]["+f.getName()+"]"
        + "["+depth+"]["+via.getName()+"] ";
      Assert.assertTrue(msg + "m.subj == subj", SubjectHelper.eq( m.getSubject(), subj ) );
      Assert.assertTrue(msg + "g hasMember"   , g.hasMember(subj, f));
      Assert.assertTrue(msg + "g hasEffMember", g.hasEffectiveMember(subj, f));
      Assert.assertTrue(msg + "m isMember"    , m.isMember(g, f));
      Assert.assertTrue(msg + "m isEffMember" , m.isEffectiveMember(g, f));
      getEff(s, g, subj, list, depth, via);
    }
    catch (Exception e) {
      Assert.fail(msg + e.getMessage());
    }
  } // public static TestImm(s, g, subj, list, via, depth)

  public static void testNumMship(
    Group g, String list, int m, int i, int e) 
  {
    try {
      testNumMship(g, FieldFinder.find(list, true), m, i, e);
    }
    catch (Exception e0) {
      Assert.fail(e0.getMessage());
    }
  } // public static void testNumMship(g, list, m, i, e)
  
  public static void testNumMship(Group g, Field f, int m, int i, int e) {
    try {
      int gotM = g.getMemberships(f).size();
      int gotI = g.getImmediateMemberships(f).size();
      int gotE = g.getEffectiveMemberships(f).size();
      String msg = " mships '" + f.getName() + "' == ";
      if (gotM == m) {
        Assert.assertTrue(
          g.getName() + msg + gotM + " (exp " + m + ")", gotM == m
        );
      }
      else {
        Iterator iter = g.getMemberships(f).iterator();
        while (iter.hasNext()) {
          Membership ms = (Membership) iter.next();
          System.err.println("GOT: " + ms);
        }
        Assert.fail("GOT: " + gotM + " EXP: " + m);
      }
      Assert.assertTrue(
        g.getName() + " imm" + msg + gotI + " (exp " + i + ")", gotI == i
      );
      Assert.assertTrue(
        g.getName() + " eff" + msg + gotE + " (exp " + e + ")", gotE == e
      );
    }
    catch (SchemaException eS) {
      Assert.fail(eS.getMessage());
    }
  } // public static void testNumMship(g, f, m, i, e) 
  

  // 'Tis more hateful below
  public static void testImm(Group g, Subject subj, Member m) {
    // The basics
    Assert.assertTrue("g hasMember m",    g.hasMember(subj));
    Assert.assertTrue("g hasImmMember m", g.hasImmediateMember(subj));
    Assert.assertTrue("m isMember g",     m.isMember(g));
    Assert.assertTrue("m isImmMember g",  m.isImmediateMember(g));
  } // public static TestImm(g, subj, m)

  public static void testEff(Group g, Group gm, Member m) {
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
  } // public static void testEff(g, gm, m)

  public static void testEffMship(
    GrouperSession s, Group g, Subject subj, Field f, Group v, int d
  ) 
  {
    try {
      Set effs = MembershipFinder.findEffectiveMemberships(
        s, g, subj, f, v, d
      );
      if (effs.size() > 0) {
        Assert.assertTrue("eff mship found", true);
      }
      else {
        Assert.fail("eff mship not found");
      }
    }
    catch (MembershipNotFoundException eMNF) {
      Assert.fail(
        "eff membership not found: '" + g.getName() + "/" + subj.getName() 
        + "/" + f.getName() + "/" + d + "/" + v.getName() + "'"
      );
    }
    catch (SchemaException eS) {
      Assert.fail(eS.getMessage());
    }
  } // public static void testEffMship(s, g, subj, f, v, d)

  public static void testImmMship(GrouperSession s, Group g, Group m, Field f) {
    testImmMship(s, g, m.toSubject(), f);
  } // public static void testImmMship(s, g, m, f)

  public static void testImmMship(GrouperSession s, Group g, Subject subj, Field f) {
    try {
      MembershipFinder.findImmediateMembership(s, g, subj, f, true);
      Assert.assertTrue("imm mship found", true);
    }
    catch (MembershipNotFoundException eMNF) {
      Assert.fail(
        "imm membership not found: '" + g.getName() + "/" + subj.getName() 
        + "/" + f.getName() + "'"
      );
    }
    catch (SchemaException eS) {
      Assert.fail(eS.getMessage());
    }
  } // public static void testImmMship(s, g, subj, f)

  
  // PRIVATE CLASS METHODS //

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
        ms.getViaGroup();
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

  private static Membership findImmediateMembership(GrouperSession s, Group g, Subject subj, Field f) {

    Membership mship = null;

    try {
      mship = MembershipFinder.findImmediateMembership(s, g, subj, f, true);
    } catch (Exception e) {
      // do nothing
    }
      
    return mship;
  }     
        
  private static Membership findImmediateMembership(GrouperSession s, Stem stem, Subject subj, Field f) {
    
    Membership mship = null;
    
    try {
      Member m = MemberFinder.findBySubject(s, subj, true);
      mship = GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(
          stem.getUuid(), m.getUuid(), f, MembershipType.IMMEDIATE.getTypeString(), true, true
        );
    } catch (Exception e) {
      mship = null;
    }
    
    return mship;
  }   

  public static Membership findCompositeMembership(GrouperSession s, Group g, Subject subj) {

    return MembershipFinder.findCompositeMembership(s, g, subj, false);

  }      

  public static Membership findEffectiveMembership(GrouperSession s, Group g, Subject subj, Group via, int depth, Field f) {
    Membership mship = null;
    try {
      Set<Membership> memberships = MembershipFinder.findEffectiveMemberships(s, g, subj,
        f, via, depth);
  
      Iterator<Membership> it = memberships.iterator();
      if (it.hasNext()) {
        mship = it.next();
      }
    } catch (Exception e) {
      // do nothing
    }
    
    return mship;
  }

  public static Membership findEffectiveMembership(GrouperSession s, Group g, Subject subj, Group via, int depth, Field f, Membership parent) {
    try {
      Set<Membership> memberships = MembershipFinder.findEffectiveMemberships(s, g, subj,
        f, via, depth);
      
      Iterator<Membership> it = memberships.iterator();
      while (it.hasNext()) {
        Membership curr = it.next();
        Membership parentOfCurr = curr.getParentMembership();
        if (parent.equals(parentOfCurr)) {
          return curr;
        }
      }
    } catch (Exception e) {
      // do nothing 
    }
    
    return null;
  } 

  /**
   * 
   * @param s
   * @param stem
   * @param subj
   * @param via
   * @param depth
   * @param f
   * @return membership
   */
  public static Membership findEffectiveMembership(GrouperSession s, Stem stem, Subject subj, Group via, int depth, Field f) {
  
    Membership mship = null;
    
    try {
      Member m = MemberFinder.findBySubject(s, subj, true);
      Iterator<Membership> it = GrouperDAOFactory.getFactory().getMembership().findAllEffectiveByStemOwner(
        stem.getUuid(), m.getUuid(), f, via.getUuid(), depth, true).iterator();
        
      if (it.hasNext()) {
        mship = it.next();
      } 
    } catch (Exception e) {
      mship = null;
    } 
    
    return mship;
  } 
  
  /**
   * 
   * @param s
   * @param comment
   * @param childGroup
   * @param childSubject
   * @param f
   * @return membership
   */
  public static Membership verifyImmediateMembership(GrouperSession s, String comment, Group childGroup, Subject childSubject, Field f) {
  
    // first verify that the membership exists
    Membership childMembership = findImmediateMembership(s, childGroup, childSubject, f);
    Assert.assertNotNull(comment + ": find membership", childMembership);
    

    // second verify that there's no via_id
    Group viaGroup = null;
    try {
      viaGroup = childMembership.getViaGroup();
    } catch (GroupNotFoundException e) {
      // do nothing
    }

    Assert.assertNull(comment + ": find via group", viaGroup);


    // third verify that there's no parent membership
    Membership parentMembership = null;
    try {
      parentMembership = childMembership.getParentMembership();
    } catch (MembershipNotFoundException e) {
      // do nothing
    }

    Assert.assertNull(comment + ": find parent membership", parentMembership);

    return childMembership;
  }

  public static Membership verifyImmediateMembership(GrouperSession s, String comment, Group childGroup, Subject childSubject) {
    return verifyImmediateMembership(s, comment, childGroup, childSubject, Group.getDefaultList());
  }

  public static void verifyImmediateMembership(GrouperSession s, String comment, Stem childStem, Subject childSubject, Field f) {

    // first verify that the membership exists
    Membership childMembership = findImmediateMembership(s, childStem, childSubject, f);
    Assert.assertNotNull(comment + ": find membership", childMembership);


    // second verify that there's no via_id
    Group viaGroup = null;
    try {
      viaGroup = childMembership.getViaGroup();
    } catch (GroupNotFoundException e) {
      // do nothing
    }

    Assert.assertNull(comment + ": find via group", viaGroup);


    // third verify that there's no parent membership
    Membership parentMembership = null;
    try {
      parentMembership = childMembership.getParentMembership();
    } catch (MembershipNotFoundException e) {
      // do nothing
    }

    Assert.assertNull(comment + ": find parent membership", parentMembership);
  }


  public static void verifyCompositeMembership(GrouperSession s, String comment, Group childGroup, Subject childSubject) {
    
    // first verify that the membership exists
    Membership childMembership = findCompositeMembership(s, childGroup, childSubject);
    Assert.assertNotNull(comment + ": find membership", childMembership);

    
    // second verify the via_id
    Composite c = null;
    try { 
      c = childMembership.getViaComposite();
    } catch (CompositeNotFoundException e) {
      // do nothing
    }
    
    Assert.assertNotNull(comment + ": find via_id", c);
    
    Group viaGroup = null;
    try {
      viaGroup = c.getOwnerGroup(); 
    } catch (GroupNotFoundException e) {
      // do nothing
    }

    Assert.assertNotNull(comment + ": find owner group of via_id", viaGroup);

    Assert.assertEquals(comment + ": verify via_id", childGroup.getName(), viaGroup.getName());


    // third verify that there's no parent membership
    Membership parentMembership = null;
    try {
      parentMembership = childMembership.getParentMembership();
    } catch (MembershipNotFoundException e) {
      // do nothing
    }

    Assert.assertNull(comment + ": find parent membership", parentMembership);
  }

  public static Membership verifyEffectiveMembership(GrouperSession s, String comment, Group childGroup, Subject childSubject, Group childVia,
    int childDepth, Group parentGroup, Subject parentSubject, Group parentVia, int parentDepth, Field f) {

    // first verify that the membership exists
    Membership childMembership = findEffectiveMembership(s, childGroup, childSubject, childVia, childDepth, f);
    Assert.assertNotNull(comment + ": find membership", childMembership);


    // second verify that the parent membership exists based on data from the method parameters
    Membership parentMembership = null;
    if (parentDepth == 0) {
      parentMembership = findImmediateMembership(s, parentGroup, parentSubject, f);
    } else {
      parentMembership = findEffectiveMembership(s, parentGroup, parentSubject, parentVia, parentDepth, f);
    }

    Assert.assertNotNull(comment + ": find parent membership", parentMembership);


    // third verify that the parent membership of the child is correct
    boolean parentCheck = false;
    try {
      parentCheck = parentMembership.equals(childMembership.getParentMembership());
    } catch (MembershipNotFoundException e) {
      // do nothing
    }
    Assert.assertTrue(comment + ": verify parent membership", parentCheck);

    return childMembership;
  }

  public static Membership verifyEffectiveMembership(GrouperSession s, String comment, Group childGroup, Subject childSubject, Group childVia,
    int childDepth, Membership parent) {

    // verify that the membership exists
    Membership childMembership = findEffectiveMembership(s, childGroup, childSubject, childVia, childDepth, Group.getDefaultList(), parent);
    Assert.assertNotNull(comment + ": find membership", childMembership);

    return childMembership;
  }

  public static Membership verifyEffectiveMembership(GrouperSession s, String comment, Group childGroup, Subject childSubject, Group childVia,
    int childDepth, Group parentGroup, Subject parentSubject, Group parentVia, int parentDepth) {
    return verifyEffectiveMembership(s, comment, childGroup, childSubject, childVia, childDepth, parentGroup, parentSubject,
      parentVia, parentDepth, Group.getDefaultList());
  }

  public static void verifyEffectiveMembership(GrouperSession s, String comment, Stem childStem, Subject childSubject, Group childVia,
    int childDepth, Stem parentStem, Subject parentSubject, Group parentVia, int parentDepth, Field f) {

    // first verify that the membership exists
    Membership childMembership = findEffectiveMembership(s, childStem, childSubject, childVia, childDepth, f);
    Assert.assertNotNull(comment + ": find membership", childMembership);


    // second verify that the parent membership exists based on data from the method parameters
    Membership parentMembership = null;
    if (parentDepth == 0) {
      parentMembership = findImmediateMembership(s, parentStem, parentSubject, f);
    } else {
      parentMembership = findEffectiveMembership(s, parentStem, parentSubject, parentVia, parentDepth, f);
    }

    Assert.assertNotNull(comment + ": find parent membership", parentMembership);


    // third verify that the parent membership of the child is correct
    boolean parentCheck = false;
    try {
      parentCheck = parentMembership.equals(childMembership.getParentMembership());
    } catch (MembershipNotFoundException e) {
      // do nothing
    }
    Assert.assertTrue(comment + ": verify parent membership", parentCheck);
  }


}

