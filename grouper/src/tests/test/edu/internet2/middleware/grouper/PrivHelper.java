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
import  org.apache.commons.logging.*;


/**
 * Privilege helper methods for testing the Grouper API.
 * <p />
 * @author  blair christensen.
 * @version $Id: PrivHelper.java,v 1.15 2005-12-09 07:35:38 blair Exp $
 */
public class PrivHelper {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(PrivHelper.class);


  // Protected Class Methods

  protected static void getPrivs(
    GrouperSession  s , Group g       , Subject subj  , int cnt, 
    boolean admin     , boolean optin , boolean optout,
    boolean read      , boolean update, boolean view
  ) 
  {
    try {
      Member  m   = MemberFinder.findBySubject(s, subj);
      String  msg = subj.getName() + " has " + cnt + " privs on " + g.getName();
      Assert.assertTrue(
        msg, g.getPrivs(subj).size() == cnt
      );
      Assert.assertTrue(
        msg, m.getPrivs(g).size()    == cnt
      );
      hasPriv(g, subj, m, AccessPrivilege.ADMIN,  admin );
      hasPriv(g, subj, m, AccessPrivilege.OPTIN,  optin );
      hasPriv(g, subj, m, AccessPrivilege.OPTOUT, optout);
      hasPriv(g, subj, m, AccessPrivilege.READ,   read  );
      hasPriv(g, subj, m, AccessPrivilege.UPDATE, update);
      hasPriv(g, subj, m, AccessPrivilege.VIEW,   view  );
    }
    catch (MemberNotFoundException eMNF) {
      Assert.fail(eMNF.getMessage());
    }
  } // protected static void getPrivs(s, g, subj, cnt, admin, optin, optout, read, update view)


  protected static void getPrivs(
    GrouperSession s, Stem ns       , Subject subj, 
    int cnt         , boolean create, boolean stem
  ) 
  {
    try {
      Member  m   = MemberFinder.findBySubject(s, subj);
      String  msg = subj.getName() + " has " + cnt + " privs on " + ns.getName();
      Assert.assertTrue(
        msg, ns.getPrivs(subj).size() == cnt
      );
      Assert.assertTrue(
        msg, m.getPrivs(ns).size()    == cnt
      );
      hasPriv(ns, subj, m, NamingPrivilege.CREATE, create);
      hasPriv(ns, subj, m, NamingPrivilege.STEM,   stem);
    }
    catch (MemberNotFoundException eMNF) {
      Assert.fail(eMNF.getMessage());
    }
  } // protected static void getPrivs(s,ns, subj, cnt, create, stem)

  protected static void getSubjsWithPriv(Group g, Set subjs, Privilege priv) {
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
    else {
      Assert.fail("invalid privilege: " + priv);
    }
    _compareCollections(msg, subjs, subjects);
  } // protected static void getSubjsWithPriv(g, subjs, priv)

  protected static void getSubjsWithPriv(Stem ns, Set subjs, Privilege priv) {
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
  } // protected static void getSubjsWithPriv(ns, subjs, priv)

  protected static void grantPriv(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
  {
    String msg = subj.getName() + " has " + priv + " on  " + g.getName();
    try {
      Member m = MemberFinder.findBySubject(s, subj);
      g.grantPriv(subj, priv);  
      hasPriv(g, subj, m, priv, true);
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // protected static void grantPriv(s, g, subj, priv)

  protected static void grantPrivFail(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
  {
    try {
      LOG.debug("grantPrivFail.0 " + priv.getName());
      Member m = MemberFinder.findBySubject(s, subj);
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
        hasPriv(g, subj, m, priv, true);
        LOG.debug("grantPrivFail.6 " + priv.getName());
      }
      catch (InsufficientPrivilegeException eIP) {
        LOG.debug("grantPrivFail.7 " + priv.getName());
        Assert.assertTrue("failed to grant " + priv + " (privs)", true);
        LOG.debug("grantPrivFail.8 " + priv.getName());
        hasPriv(g, subj, m, priv, false);
        LOG.debug("grantPrivFail.9 " + priv.getName());
      }
      catch (SchemaException eS) {
        LOG.debug("grantPrivFail.10 " + priv.getName());
        Assert.assertTrue("failed to grant " + priv + " (privs)", true);
        LOG.debug("grantPrivFail.11 " + priv.getName());
        hasPriv(g, subj, m, priv, false);
        LOG.debug("grantPrivFail.12 " + priv.getName());
      }
    }
    catch (MemberNotFoundException eMNF) {
      LOG.debug("grantPrivFail.13 " + priv.getName());
      Assert.fail(eMNF.getMessage());
    }
  } // protected static void grantPrivFail(s, g, subj, priv)

  protected static void grantPriv(
    GrouperSession s, Stem ns, Subject subj, Privilege priv
  )
  {
    String msg = subj.getName() + " has " + priv + " on  " + ns.getName();
    try {
      Member m = MemberFinder.findBySubject(s, subj);
      ns.grantPriv(subj, priv);  
      hasPriv(ns, subj, m, priv, true);
    }
    catch (GrantPrivilegeException eGP) {
      Assert.fail(eGP.getMessage());
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.fail(eIP.getMessage());
    }
    catch (MemberNotFoundException eMNF) {
      Assert.fail(eMNF.getMessage());
    }
    catch (SchemaException eS) {
      Assert.fail(eS.getMessage());
    }
  } // protected static void grantPriv(s, ns, subj, priv)

  protected static void grantPrivFail(
    GrouperSession s, Stem ns, Subject subj, Privilege priv
  )
  {
    try {
      Member m = MemberFinder.findBySubject(s, subj);
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
    }
    catch (MemberNotFoundException eMNF) {
      Assert.fail(eMNF.getMessage());
    }
  } // protected static void grantPrivFail(s, ns, subj, priv)

  protected static void hasPriv(
    GrouperSession s, Group g, Subject subj, Privilege priv, boolean has
  )
  {
    try {
      hasPriv(g, subj, MemberFinder.findBySubject(s, subj), priv, has);  
    }
    catch (MemberNotFoundException eMNF) {
      Assert.fail(eMNF.getMessage());
    }
  } // protected static void hasPriv(s, g, subj, priv, has)

  protected static void hasPriv(
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
    else {
      Assert.fail("unable test priv '" + priv + "'");
    }
  } // protected static void hasPriv(g, subj, m, priv, has)

  protected static void hasPriv(
    GrouperSession s, Stem ns, Subject subj, Privilege priv, boolean has
  )
  {
    try {
      hasPriv(ns, subj, MemberFinder.findBySubject(s, subj), priv, has);  
    }
    catch (MemberNotFoundException eMNF) {
      Assert.fail(eMNF.getMessage());
    }
  } // protected static void hasPriv(s, ns, subj, priv, has)

  protected static void hasPriv(
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
  } // protected static void hasPriv(ns, subj, m, priv, has)

  protected static void revokePriv(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
  {
    try {
      Member m = MemberFinder.findBySubject(s, subj);
      g.revokePriv(subj, priv);  
      hasPriv(g, subj, m, priv, false);
    }
    catch (Exception e) {
      Assert.fail("failed to revoke priv: " + e.getMessage());
    }
  } // protected static void revokePriv(s, g, subj, priv)

  // ALL has been granted this priv as well so even after revoking the
  // priv from this subject they'll show up as having it, at least when
  // calling hasMember or isMember.
  protected static void revokePrivAllHasPriv(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
  {
    LOG.debug("revokePrivAllHasPriv.0");
    try {
      Member m = MemberFinder.findBySubject(s, subj);
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
  } // protected static void revokePrivAllHasPriv(s, g, subj, priv)

  protected static void revokePrivFail(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
  {
    LOG.debug("revokePrivFail.0");
    String msg = subj.getName() + " does not have " + priv + " on  " + g.getName();
    try {
      Member m = MemberFinder.findBySubject(s, subj);
      LOG.debug("revokePrivFail.1");
      g.revokePriv(subj, priv);  
      LOG.debug("revokePrivFail.2");
      Assert.fail("revoked privilege");
    }
    catch (Exception e) {
      Assert.assertTrue("failed to revoke privilege", true);
      LOG.debug("revokePrivFail.3");
    }
  } // protected static void revokePrivFail(s, g, subj, priv)

  protected static void revokePriv(
    GrouperSession s, Stem ns, Subject subj, Privilege priv
  )
  {
    try {
      Member m = MemberFinder.findBySubject(s, subj);
      ns.revokePriv(subj, priv);  
      hasPriv(ns, subj, m, priv, false);
    }
    catch (Exception e) {
      Assert.fail("failed to revoke priv: " + e.getMessage());
    }
  } // protected static void revokePriv(s, ns, subj, priv)

  protected static void revokePrivFail(
    GrouperSession s, Stem ns, Subject subj, Privilege priv
  )
  {
    LOG.debug("revokePrivFail.0");
    String msg = subj.getName() + " does not have " + priv + " on  " + ns.getName();
    try {
      Member m = MemberFinder.findBySubject(s, subj);
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
  } // protected static void revokePriv(s, ns, subj, priv)

  protected static void revokePriv(
    GrouperSession s, Group g, Privilege priv
  )
  {
    try {
      g.revokePriv(priv);  
      getSubjsWithPriv(g, new HashSet(), priv);
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // protected static void revokePriv(s, g, priv)

  protected static void revokePriv(
    GrouperSession s, Stem ns, Privilege priv
  )
  {
    try {
      ns.revokePriv(priv);  
      getSubjsWithPriv(ns, new HashSet(), priv);
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // protected static void revokePriv(s, ns, priv)

  protected static void revokePrivFail(
    GrouperSession s, Stem ns, Privilege priv
  )
  {
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
  } // protected static void revokePriv(s, ns, priv)

  protected static void subjInGroups(
    GrouperSession s, Subject subj, Set groups, Privilege priv
  ) 
  {
    String  msg   = subj.getId() + " has " + priv + " on ";
    Set     where = new HashSet();
    try {
      Member    m     = MemberFinder.findBySubject(s, subj);
      if      (priv.equals(AccessPrivilege.ADMIN))  {
        where = m.hasAdmin();
      } 
      else if (priv.equals(AccessPrivilege.OPTIN))  {
        where = m.hasOptin();
      }
      else if (priv.equals(AccessPrivilege.OPTOUT)) {
        where = m.hasOptout();
      }
      else if (priv.equals(AccessPrivilege.READ))   {
        where = m.hasRead();
      }
      else if (priv.equals(AccessPrivilege.UPDATE)) {
        where = m.hasUpdate();
      }
      else if (priv.equals(AccessPrivilege.VIEW))   {
        where = m.hasView();
      }
      else {
        Assert.fail("invalid priv: " + priv);
      }
      _compareCollections(msg, groups, where);
    }
    catch (MemberNotFoundException eMNF) {
      Assert.fail(eMNF.getMessage());
    }
  } // protected static void subjInGroups(s, subj, groups, priv)

  protected static void subjInStems(
    GrouperSession s, Subject subj, Set stems, Privilege priv
  ) 
  {
    String  msg   = subj.getId() + " has " + priv + " on ";
    Set     where = new HashSet();
    try {
      Member    m     = MemberFinder.findBySubject(s, subj);
      if      (priv.equals(NamingPrivilege.CREATE)) {
        where = m.hasCreate();
      } 
      else if (priv.equals(NamingPrivilege.STEM)) {
        where = m.hasStem();
      }
      else {
        Assert.fail("invalid priv: " + priv);
      }
      _compareCollections(msg, stems, where);
    }
    catch (MemberNotFoundException eMNF) {
      Assert.fail(eMNF.getMessage());
    }
  } // protected static void subjInStems(s, subj, stems, priv)

  // TODO Genericize?
/* TODO
  public void testGetPrivs() {
    Subject subj = SubjectHelper.SUBJ0;
    PrivHelper.grantPriv( s, edu, subj , PRIV);      
    List  creators  = new ArrayList( edu.getCreators() );
    List  stemmers  = new ArrayList( edu.getStemmers() );
    Assert.assertTrue("creators: 0", creators.size() == 0);
    Assert.assertTrue("stemmers: 1", stemmers.size() == 1);
    NamingPrivilege np = (NamingPrivilege) stemmers.get(0);
    Assert.assertNotNull("np !null", np);
    Assert.assertTrue(
      "np instanceof NamingPrivilege", np instanceof NamingPrivilege
    );
    Assert.assertTrue(
      "np implementation name",
      np.getImplementationName().equals(
        "edu.internet2.middleware.grouper.GrouperNamingAdapter"
      )
    );  
    Assert.assertTrue("np revokable", np.isRevokable());
    Assert.assertTrue("np name", np.getName().equals(PRIV.toString()));
    Assert.assertTrue(
      "np object instanceof Stem", np.getObject() instanceof Stem
    );
    Assert.assertTrue(
      "np object == edu", 
      ( (Stem) np.getObject() ).equals(edu)
    );
    try {
      Assert.assertTrue(
        "np owner == subj", np.getOwner().equals(subj)
      );
    }
    catch (SubjectNotFoundException eSNF) {
      Assert.fail("np has no owner");
    }
    Assert.assertTrue(
      "np subject", np.getSubject().equals(subj)
    );
  } // public void testGrantPrivs()
*/

  // Private Class Methods
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

