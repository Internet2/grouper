/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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
import  edu.internet2.middleware.subject.*;
import  java.util.Date;
import  junit.framework.*;

/**
 * Grouper-specific JUnit assertions.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperTest.java,v 1.3 2006-09-26 19:00:40 blair Exp $
 * @since   1.1.0
 */
public class GrouperTest extends TestCase {

  // PRIVATE CLASS CONSTANTS //
  private static final String G   = "group";
  private static final String NS  = "stem";


  // CONSTRUCTORS //

  /** 
   * @since   1.1.0
   */
  public GrouperTest(String name) {
    super(name);
  } // public GrouperTest()


  // PUBLIC INSTANCE METHODS //

  /**  
   * @since   1.1.0
   */
  public void assertDoNotFindGroupByName(GrouperSession s, String name) {
    try {
      GroupFinder.findByName(s, name);
      fail("unexpectedly found group by name: " + name);
    }
    catch (GroupNotFoundException eGNF) {
      assertTrue(true);
    }
  } // public void assertDoNotFindGroupByName(s, name)

  /**  
   * @since   1.1.0
   */
  public void assertDoNotFindStemByName(GrouperSession s, String name) {
    try {
      StemFinder.findByName(s, name);
      fail("unexpectedly found stem by name: " + name);
    }
    catch (StemNotFoundException eNSNF) {
      assertTrue(true);
    }
  } // public void assertDoNotFindStemByName(s, name)

  /**  
   * @return  Retrieved {@link Group}.
   * @since   1.1.0
   */
  public Group assertFindGroupByName(GrouperSession s, String name) {
    Group g = null;
    try {
      g = GroupFinder.findByName(s, name);
      assertTrue(true);
    }
    catch (GroupNotFoundException eGNF) {
      fail("did not find group (" + name + ") by name: " + eGNF.getMessage());
    }
    return g;
  } // public Group assertFindGroupByName(s, name)

  /**  
   * @return  Retrieved {@link Stem}.
   * @since   1.1.0
   */
  public Stem assertFindStemByName(GrouperSession s, String name) {
    Stem ns = null;
    try {
      ns = StemFinder.findByName(s, name);
      assertTrue(true);
    }
    catch (StemNotFoundException eNSNF) {
      fail("did not find stem (" + name + ") by name: " + eNSNF.getMessage());
    }
    return ns;
  } // public Stem assertFindStemByName(s, name)

  /** 
   * @since   1.1.0
   */
  public void assertGroupCreateSubject(Group g, Subject subj) {
    try {
      _assertSubject(G, g.getName(), "createSubject", g.getCreateSubject(), subj);
    }
    catch (SubjectNotFoundException eSNF) {
      fail("group (" + g.getName() + "): " + eSNF.getMessage());
    }
  } // public void assertStemCreateSubject(ns, subj)

  /** 
   * @since   1.1.0
   */
  public void assertGroupCreateTime(Group g, Date d) {
    _assertDate(G, g.getName(), "createTime", d, g.getCreateTime());
  } // public void assertStemCreateTime(ns, d)

  /**
   * @since   1.1.0
   */
  public void assertGroupDescription(Group g, String val) {
    _assertString(G, g.getName(), "description", val, g.getDescription());
  } // public void assertGroupDescription(g, val)

  /**
   * @since   1.1.0
   */
  public void assertGroupDisplayExtension(Group g, String val) {
    _assertString(G, g.getName(), "displayExtension", val, g.getDisplayExtension());
  } // public void assertGroupDisplayExtension(g, val)

  /**
   * @since   1.1.0
   */
  public void assertGroupDisplayName(Group g, String val) {
    _assertString(G, g.getName(), "displayName", val, g.getDisplayName());
  } // public void assertGroupDisplayName(g, val)

  /**
   * @since   1.1.0
   */
  public void assertGroupExtension(Group g, String val) {
    _assertString(G, g.getName(), "extension", val, g.getExtension());
  } // public void assertGroupDisplayExtension(g, val)

  /**
   * @since   1.1.0
   */
  public void assertGroupHasAdmin(Group g, Subject subj, boolean exp) {
    _assertPriv(G, g.getName(), subj, "ADMIN", exp, g.hasAdmin(subj));
  } // public void assertGroupHasAdmin(g, subj, exp)

  /**
   * @since   1.1.0
   */
  public void assertGroupHasMember(Group g, Subject subj, boolean exp) {
    boolean got = g.hasMember(subj);
    if (got == exp) {
      assertTrue(true);
    }
    else {
      _fail(
        G, g.getName(), SubjectHelper.getPretty(subj)  + " is member",
        Boolean.toString(exp), Boolean.toString(got)
      );
    }
  } // public void assertGroupHasMember(g, subj, exp)

  /**
   * @since   1.1.0
   */
  public void assertGroupHasOptin(Group g, Subject subj, boolean exp) {
    _assertPriv(G, g.getName(), subj, "OPTIN", exp, g.hasOptin(subj));
  } // public void assertGroupHasOptin(g, subj, exp)

  /**
   * @since   1.1.0
   */
  public void assertGroupHasOptout(Group g, Subject subj, boolean exp) {
    _assertPriv(G, g.getName(), subj, "OPTOUT", exp, g.hasOptout(subj));
  } // public void assertGroupHasOptout(g, subj, exp)

  /**
   * @since   1.1.0
   */
  public void assertGroupHasRead(Group g, Subject subj, boolean exp) {
    _assertPriv(G, g.getName(), subj, "READ", exp, g.hasRead(subj));
  } // public void assertGroupHasRead(g, subj, exp)

  /**
   * @since   1.1.0
   */
  public void assertGroupHasUpdate(Group g, Subject subj, boolean exp) {
    _assertPriv(G, g.getName(), subj, "UPDATE", exp, g.hasUpdate(subj));
  } // public void assertGroupHasUpdate(g, subj, exp)

  /**
   * @since   1.1.0
   */
  public void assertGroupHasView(Group g, Subject subj, boolean exp) {
    _assertPriv(G, g.getName(), subj, "VIEW", exp, g.hasView(subj));
  } // public void assertGroupHasView(g, subj, exp)

  /**
   * @since   1.1.0
   */
  public void assertGroupName(Group g, String val) {
    _assertString(G, g.getName(), "name", val, g.getName());
  } // public void assertGroupName(g, val)

  /**
   * @since   1.1.0
   */
  public void assertGroupUuid(Group g, String val) {
    _assertString(G, g.getName(), "uuid", val, g.getUuid());
  } // public void assertGroupUuid(g, val)

  /** 
   * @since   1.1.0
   */
  public void assertStemCreateSubject(Stem ns, Subject subj) {
    try {
      _assertSubject(NS, ns.getName(), "createSubject", subj, ns.getCreateSubject());
    }
    catch (SubjectNotFoundException eSNF) {
      fail("stem (" + ns.getName() + "): " + eSNF.getMessage());
    }
  } // public void assertStemCreateSubject(ns, subj)

  /** 
   * @since   1.1.0
   */
  public void assertStemCreateTime(Stem ns, Date d) {
    _assertDate(NS, ns.getName(), "createTime", d, ns.getCreateTime());
  } // public void assertStemCreateTime(ns, d)

  /**
   * @since   1.1.0
   */
  public void assertStemDescription(Stem ns, String val) {
    _assertString(NS, ns.getName(), "description", val, ns.getDescription());
  } // public void assertStemDescription(ns, val)

  /**
   * @since   1.1.0
   */
  public void assertStemDisplayExtension(Stem ns, String val) {
    _assertString(NS, ns.getName(), "displayExtension", val, ns.getDisplayExtension());
  } // public void assertStemDisplayExtension(ns, val)

  /**
   * @since   1.1.0
   */
  public void assertStemDisplayName(Stem ns, String val) {
    _assertString(NS, ns.getName(), "displayName", val, ns.getDisplayName());
  } // public void assertStemDisplayName(ns, val)

  /**
   * @since   1.1.0
   */
  public void assertStemExtension(Stem ns, String val) {
    _assertString(NS, ns.getName(), "extension", val, ns.getExtension());
  } // public void assertStemExtension(ns, val)

  /**
   * @since   1.1.0
   */
  public void assertStemHasCreate(Stem ns, Subject subj, boolean exp) {
    _assertPriv(NS, ns.getName(), subj, "CREATE", exp, ns.hasCreate(subj));
  } // public void assertStemHasCreate(ns, subj, exp)

  /**
   * @since   1.1.0
   */
  public void assertStemHasStem(Stem ns, Subject subj, boolean exp) {
    _assertPriv(NS, ns.getName(), subj, "STEM", exp, ns.hasStem(subj));
  } // public void assertStemHasStem(ns, subj, exp)

  /**
   * @since   1.1.0
   */
  public void assertStemName(Stem ns, String val) {
    _assertString(NS, ns.getName(), "name", val, ns.getName());
  } // public void assertStemName(ns, val)

  /**
   * @since   1.1.0
   */
  public void assertStemUuid(Stem ns, String val) {
    _assertString(NS, ns.getName(), "uuid", val, ns.getUuid());
  } // public void assertStemDescription(ns, val)


  // PRIVATE INSTANCE METHODS //

  // @since   1.1.0
  private void _assertDate(String type, String who, String what, Date exp, Date got) {
    if (exp.equals(got)) {
      assertTrue(true);
    }
    else {
      _fail(type, who, what, exp + "/" + exp.getTime(), got + "/" + got.getTime());
    }
  } // private void _assertDate(type, who, what, exp, got)

  // @since   1.1.0
  private void _assertPriv(String type, String who, Subject subj, String what, boolean exp, boolean got) {
    if (exp == got) {
      assertTrue(true);
    }
    else {
      _fail(
        type, who, SubjectHelper.getPretty(subj) + " has " + what, 
        Boolean.toString(exp), Boolean.toString(got)
      );
    }
  } // private void _assertPriv(type, who, subj, what, exp, got)

  // @since   1.1.0
  private void _assertString(String type, String who, String what, String exp, String got) {
    if (exp.equals(got)) {
      assertTrue(true);
    }
    else {
      _fail(type, who, what, exp, got);
    }
  } // private void _assertString(who, what, exp, got)

  // @since   1.1.0
  private void _assertSubject(String type, String who, String what, Subject exp, Subject got) {
    if (SubjectHelper.eq(exp, got)) {
      assertTrue(true);
    }
    else {
      _fail(type, who, what, SubjectHelper.getPretty(exp), SubjectHelper.getPretty(got));
    }
  } // private void _assertSubject(type, who, what, exp, got)

  // @since   1.1.0
  private void _fail(String type, String who, String what, String exp, String got) {
    fail(type + "=(" + who + "): testing=(" + what + ") expected=(" + exp + ") got=(" + got + ")");
  } // private void _fail(type, who, what, exp, got)

} // public class GrouperTest

