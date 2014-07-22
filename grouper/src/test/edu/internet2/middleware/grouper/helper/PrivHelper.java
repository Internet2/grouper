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

package edu.internet2.middleware.grouper.helper;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.exception.GrantPrivilegeException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Privilege helper methods for testing the Grouper API.
 * <p />
 * @author  blair christensen.
 * @version $Id: PrivHelper.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 */
public class PrivHelper {

  private static final Log LOG = GrouperUtil.getLog(PrivHelper.class);


  // public CLASS METHODS //

  public static void getSubjsWithPriv(Group g, Set subjs, Privilege priv) {
    String  msg       = subjs.size() + " subjects with " + priv + " on " + g.getName();
    Set     subjects  = new LinkedHashSet();
    if      (priv.equals(AccessPrivilege.ADMIN) ) {
      subjects = g.getAdmins();
    } 
    else if (priv.equals(AccessPrivilege.OPTIN) ) {
      subjects = g.getOptins();
    } 
    else if (priv.equals(AccessPrivilege.OPTOUT)) {
      subjects = g.getOptouts();
    } 
    else if (priv.equals(AccessPrivilege.READ)  ) {
      subjects = g.getReaders();
    } 
    else if (priv.equals(AccessPrivilege.UPDATE)) {
      subjects = g.getUpdaters();
    } 
    else if (priv.equals(AccessPrivilege.VIEW)  ) {
      subjects = g.getViewers();
    } 
    else if (priv.equals(AccessPrivilege.GROUP_ATTR_READ)  ) {
      subjects = g.getGroupAttrReaders();
    } 
    else if (priv.equals(AccessPrivilege.GROUP_ATTR_UPDATE)  ) {
      subjects = g.getGroupAttrUpdaters();
    } 
    else {
      Assert.fail("invalid privilege: " + priv);
    }
    _compareCollections(msg, subjs, subjects);
  } // public static void getSubjsWithPriv(g, subjs, priv)

  public static void getSubjsWithPriv(Stem ns, Set subjs, Privilege priv) {
    String  msg       = subjs.size() + " subjects with " + priv + " on " + ns.getName();
    Set     subjects  = new LinkedHashSet();
    if      (priv.equals(NamingPrivilege.CREATE)) {
      subjects = ns.getCreators();
    } 
    else if (priv.equals(NamingPrivilege.STEM)) {
      subjects = ns.getStemmers();
    } 
   else {
      Assert.fail("invalid privilege: " + priv);
    }
    _compareCollections(msg, subjs, subjects);
  } // public static void getSubjsWithPriv(ns, subjs, priv)

  public static void grantPriv(GrouperSession s, final Group g, final Subject subj, final Privilege priv) {
    GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        try {
          Member m = MemberFinder.findBySubject(grouperSession, subj, true);
          g.grantPriv(subj, priv);  
          hasPriv(g, subj, m, priv, true);
        }
        catch (Exception e) {
          throw new RuntimeException(e);
        }
        return null;
      }
      
    });
  } // public static void grantPriv(s, g, subj, priv)

  public static void grantPrivFail(GrouperSession s, 
      final Group g, final Subject subj, final Privilege priv) {
    GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        LOG.debug("grantPrivFail.0 " + priv.getName());
        MemberFinder.findBySubject(grouperSession, subj, true);
        LOG.debug("grantPrivFail.1 " + priv.getName());
        try {
          g.grantPriv(subj, priv);  
          LOG.debug("grantPrivFail.2 " + priv.getName());
          Assert.fail("granted " + priv);
          LOG.debug("grantPrivFail.3 " + priv.getName());
        }
        catch (GrantPrivilegeException eGP) {
          LOG.debug("grantPrivFail.4 " + priv.getName());
          Assert.assertTrue("failed to grant " + priv + " (exists)", true);
          LOG.debug("grantPrivFail.5 " + priv.getName());
        }
        catch (InsufficientPrivilegeException eIP) {
          LOG.debug("grantPrivFail.7 " + priv.getName());
          Assert.assertTrue("failed to grant " + priv + " (privs)", true);
          LOG.debug("grantPrivFail.8 " + priv.getName());
        }
        catch (SchemaException eS) {
          LOG.debug("grantPrivFail.10 " + priv.getName());
          Assert.assertTrue("failed to grant " + priv + " (privs)", true);
          LOG.debug("grantPrivFail.11 " + priv.getName());
        }
        return null;
      }
      
    });
  } // public static void grantPrivFail(s, g, subj, priv)

  public static void grantPriv(GrouperSession s, final Stem ns, final Subject subj, final Privilege priv)
  {
    GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        try {
          Member m = MemberFinder.findBySubject(grouperSession, subj, true);
          ns.grantPriv(subj, priv);  
          hasPriv(ns, subj, m, priv, true);
        }
        catch (GrantPrivilegeException eGP) {
          Assert.fail(eGP.getMessage());
        }
        catch (InsufficientPrivilegeException eIP) {
          Assert.fail(eIP.getMessage());
        }
        catch (SchemaException eS) {
          Assert.fail(eS.getMessage());
        }
        return null;
      }
      
    });
  } // public static void grantPriv(s, ns, subj, priv)

  public static void grantPrivFail(GrouperSession s, final Stem ns, final Subject subj, final Privilege priv)
  {
    GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        Member m = MemberFinder.findBySubject(grouperSession, subj, true);
        try {
          ns.grantPriv(subj, priv);  
          Assert.fail("granted " + priv);
        }
        catch (GrantPrivilegeException eGP) {
          Assert.assertTrue("failed to grant " + priv + " (exists)", true);
          hasPriv(ns, subj, m, priv, true);
        }
        catch (InsufficientPrivilegeException eIP) {
          Assert.assertTrue("failed to grant " + priv + " (privs)", true);
          hasPriv(ns, subj, m, priv, false);
        }
        catch (SchemaException eS) {
          Assert.assertTrue("failed to grant " + priv + " (privs)", true);
          hasPriv(ns, subj, m, priv, false);
        }
        return null;
      }
      
    });
  } // public static void grantPrivFail(s, ns, subj, priv)

  public static void hasPriv(
    GrouperSession s, Group g, Subject subj, Privilege priv, boolean has
  )
  {
    hasPriv(g, subj, MemberFinder.findBySubject(s, subj, true), priv, has);  
  } // public static void hasPriv(s, g, subj, priv, has)

  public static void hasPriv(
    Group g, Subject subj, Member m, Privilege priv, boolean has
  ) 
  {
    String msg  = subj.getName();
    if (msg.equals("GrouperSystem")) {
      has = true;
    }
    if (has == true) {
      msg += " has ";
    }
    else {
      msg += " does not have ";
    }
    if      (priv.equals(AccessPrivilege.ADMIN)) {
      Assert.assertTrue(msg + " ADMIN",   g.hasAdmin(subj)  == has  );
      Assert.assertTrue(msg + " ADMIN",   m.hasAdmin(g)     == has  );
    }
    else if (priv.equals(AccessPrivilege.OPTIN)) {
      Assert.assertTrue(msg + " OPTIN",   g.hasOptin(subj)  == has  );
      Assert.assertTrue(msg + " OPTIN",   m.hasOptin(g)     == has  );
    }
    else if (priv.equals(AccessPrivilege.OPTOUT)) {
      Assert.assertTrue(msg + " OPTOUT",  g.hasOptout(subj) == has  );
      Assert.assertTrue(msg + " OPTOUT",  m.hasOptout(g)    == has  );
    }
    else if (priv.equals(AccessPrivilege.READ)) {
      Assert.assertTrue(msg + " READ",    g.hasRead(subj)   == has  );
      Assert.assertTrue(msg + " READ",    m.hasRead(g)      == has  );
    }
    else if (priv.equals(AccessPrivilege.UPDATE)) {
      Assert.assertTrue(msg + " UPDATE",  g.hasUpdate(subj) == has  );
      Assert.assertTrue(msg + " UPDATE",  m.hasUpdate(g)    == has  );
    }
    else if (priv.equals(AccessPrivilege.VIEW)) {
      Assert.assertTrue(msg + " VIEW",    g.hasView(subj)   == has  );
      Assert.assertTrue(msg + " VIEW",    m.hasView(g)      == has  );
    }
    else if (priv.equals(AccessPrivilege.GROUP_ATTR_READ)) {
      Assert.assertTrue(msg + " GROUP_ATTR_READ",    g.hasGroupAttrRead(subj)   == has  );
      Assert.assertTrue(msg + " GROUP_ATTR_READ",    m.hasGroupAttrRead(g)      == has  );
    }
    else if (priv.equals(AccessPrivilege.GROUP_ATTR_UPDATE)) {
      Assert.assertTrue(msg + " GROUP_ATTR_UPDATE",    g.hasGroupAttrUpdate(subj)   == has  );
      Assert.assertTrue(msg + " GROUP_ATTR_UPDATE",    m.hasGroupAttrUpdate(g)      == has  );
    }
    else {
      Assert.fail("unable test priv '" + priv + "'");
    }
  } // public static void hasPriv(g, subj, m, priv, has)

  public static void hasPriv(
    GrouperSession s, Stem ns, Subject subj, Privilege priv, boolean has
  )
  {
    hasPriv(ns, subj, MemberFinder.findBySubject(s, subj, true), priv, has);  
  } // public static void hasPriv(s, ns, subj, priv, has)

  public static void hasPriv(
    Stem ns, Subject subj, Member m, Privilege priv, boolean has
  ) 
  {
    String msg  = subj.getName();
    if (msg.equals("GrouperSystem")) {
      has = true;
    }
    if (has == true) {
      msg += " has ";
    }
    else {
      msg += " does not have ";
    }
    if      (priv.equals(NamingPrivilege.CREATE)) {
      Assert.assertTrue(msg + " CREATE",  ns.hasCreate(subj)  == has  );
      Assert.assertTrue(msg + " CREATE",  m.hasCreate(ns)     == has  );
    }
    else if (priv.equals(NamingPrivilege.STEM)) {
      Assert.assertTrue(msg + " STEM",    ns.hasStem(subj)    == has  );
      Assert.assertTrue(msg + " STEM",    m.hasStem(ns)       == has  );
    }
    else {
      Assert.fail("unable test priv '" + priv + "'");
    } 
  } // public static void hasPriv(ns, subj, m, priv, has)

  public static void revokePriv(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
  {
    try {
      Member m = MemberFinder.findBySubject(s, subj, true);
      g.revokePriv(subj, priv);  
      if      (priv.equals(AccessPrivilege.READ)) {  
        // Granted to ALL by default - but possibly revoked
        if (SubjectHelper.eq(subj, SubjectTestHelper.SUBJA)) { 
          hasPriv(g, subj, m, priv, false);
        }
        else {
          hasPriv(g, subj, m, priv, true);
        }
      }
      else if (priv.equals(AccessPrivilege.VIEW)) {
        // Granted to ALL by default - but possibly revoked
        if (SubjectHelper.eq(subj, SubjectTestHelper.SUBJA)) { 
          hasPriv(g, subj, m, priv, false);
        }
        else {
          hasPriv(g, subj, m, priv, true);
        }
      }
      else {
        hasPriv(g, subj, m, priv, false);
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public static void revokePriv(s, g, subj, priv)

  // ALL has been granted this priv as well so even after revoking the
  // priv from this subject they'll show up as having it, at least when
  // calling hasMember or isMember.
  public static void revokePrivAllHasPriv(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
  {
    LOG.debug("revokePrivAllHasPriv.0");
    try {
      Member m = MemberFinder.findBySubject(s, subj, true);
      LOG.debug("revokePrivAllHasPriv.1");
      g.revokePriv(subj, priv);  
      LOG.debug("revokePrivAllHasPriv.2");
      hasPriv(g, subj, m, priv, true);
      LOG.debug("revokePrivAllHasPriv.3");
    }
    catch (Exception e) {
      LOG.debug("revokePrivAllHasPriv.4");
      Assert.fail("failed to revoke priv: " + e.getMessage());
    }
  } // public static void revokePrivAllHasPriv(s, g, subj, priv)

  public static void revokePrivFail(
    GrouperSession s, final Group g, final Subject subj, final Privilege priv) {
    GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        LOG.debug("revokePrivFail.0");
        try {
          MemberFinder.findBySubject(grouperSession, subj, true);
          LOG.debug("revokePrivFail.1");
          g.revokePriv(subj, priv);  
          LOG.debug("revokePrivFail.2");
          Assert.fail("revoked privilege");
        }
        catch (Exception e) {
          Assert.assertTrue("failed to revoke privilege", true);
          LOG.debug("revokePrivFail.3");
        }
        return null;
      }
      
    });
  } // public static void revokePrivFail(s, g, subj, priv)

  public static void revokePriv(
    GrouperSession s, Stem ns, Subject subj, Privilege priv
  )
  {
    try {
      Member m = MemberFinder.findBySubject(s, subj, true);
      ns.revokePriv(subj, priv);  
      hasPriv(ns, subj, m, priv, false);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public static void revokePriv(s, ns, subj, priv)

  public static void revokePrivFail(
    GrouperSession s, Stem ns, Subject subj, Privilege priv
  )
  {
    LOG.debug("revokePrivFail.0");
    try {
      MemberFinder.findBySubject(s, subj, true);
      LOG.debug("revokePrivFail.1");
      ns.revokePriv(subj, priv);  
      LOG.debug("revokePrivFail.2");
      Assert.fail("revoked privilege");
    }
    catch (InsufficientPrivilegeException eIP) {
      LOG.debug("revokePrivFail.3");
      Assert.assertTrue("failed to revoke priv", true);
    }
    catch (Exception e) {
      LOG.debug("revokePrivFail.4");
      Assert.fail(e.getMessage());
    }
  } // public static void revokePriv(s, ns, subj, priv)
  
  /**
   * TODO 20070813 deprecate
   * @since  1.2.1
   */
  public static void revokePriv(Stem ns, Privilege priv) {
    try {
      ns.revokePriv(priv);  
      getSubjsWithPriv(ns, new HashSet(), priv);
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public static void revokePriv(s, ns, priv)

  /**
   * TODO 20070813 deprecate
   * @since  1.2.1
   */
  public static void revokePrivFail(Stem ns, Privilege priv) {
    LOG.debug("revokePrivFail.0");
    try {
      ns.revokePriv(priv);  
      LOG.debug("revokePrivFail.1");
      Assert.fail("revoked priv");
    }
    catch (InsufficientPrivilegeException eIP) {
      LOG.debug("revokePrivFail.2");
      Assert.assertTrue("failed to revoke priv", true);
    }
    catch (Exception e) {
      LOG.debug("revokePrivFail.3");
      Assert.fail(e.getMessage());
    }
  }


  // PRIVATE CLASS METHODS //

  private static void _compareCollections(String msg, Set exp, Set got) {
    Set check = new HashSet();
    Assert.assertTrue(
      msg + ": " + got.size(), got.size() == exp.size()
    );
    Iterator expIter = exp.iterator();
    while (expIter.hasNext()) {
      check.add( _getId( expIter.next() ) );
    }
    Iterator gotIter = got.iterator();
    while (gotIter.hasNext()) {
      String  id  = _getId( gotIter.next() );
      Assert.assertTrue("has priv: " + id, check.remove(id));
    }
    if (check.size() > 0) {
      Assert.fail("did not have priv: " + check);
    }
  } // private static void _compareCollections(msg, exp, got)

  private static String _getId(Object o) {
    String id = null;
    if      (o instanceof Group)    {
      id = (String) ( (Group) o).getUuid();
    }
    else if (o instanceof Stem)     {
      id = (String) ( (Stem) o).getUuid();
    }
    else if (o instanceof Subject)  {
      id = (String) ( (Subject) o).getId();
    } 
    else {
      Assert.fail("unknown object: " + o);
    }
    return id;
  } // private static void _getId(o)
}

