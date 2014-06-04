#
# GrouperShell Composite Tests
# $Id: privs.gsh,v 1.5 2009-11-05 17:59:40 mchyzer Exp $
#

#
# SETUP
#
GSH_DEVEL = true
root      = addRootStem("uchicago", "uchicago")
ns        = addStem(root.getName(), "nsit", "nsit")
g         = addGroup(ns.getName() , "nas", "nas")
subj      = SubjectFinder.findAllSubject()

#
# TEST
#
assertTrue( "ALL !CREATE"       , !hasPriv(ns.getName()   , subj.getId(), NamingPrivilege.CREATE) )
assertTrue( "ALL !STEM"         , !hasPriv(ns.getName()   , subj.getId(), NamingPrivilege.STEM)   )
assertTrue( "grant ALL CREATE"  , grantPriv(ns.getName()  , subj.getId(), NamingPrivilege.CREATE) )
assertTrue( "ALL now CREATE"    , hasPriv(ns.getName()    , subj.getId(), NamingPrivilege.CREATE) )
assertTrue( "revoke ALL CREATE" , revokePriv(ns.getName() , subj.getId(), NamingPrivilege.CREATE) )
assertTrue( "ALL !ADMIN"        , !hasPriv(g.getName()    , subj.getId(), AccessPrivilege.ADMIN)  )
assertTrue( "ALL !OPTIN"        , !hasPriv(g.getName()    , subj.getId(), AccessPrivilege.OPTIN)  )
assertTrue( "ALL !OPTOUT"       , !hasPriv(g.getName()    , subj.getId(), AccessPrivilege.OPTOUT) )
assertTrue( "ALL READ"          , hasPriv(g.getName()    , subj.getId(), AccessPrivilege.READ)    )
assertTrue( "ALL !UPDATE"       , !hasPriv(g.getName()    , subj.getId(), AccessPrivilege.UPDATE) )
assertTrue( "ALL VIEW"          , hasPriv(g.getName()    , subj.getId(), AccessPrivilege.VIEW)    )
assertTrue( "grant ALL ADMIN"   , grantPriv(g.getName()   , subj.getId(), AccessPrivilege.ADMIN)  )
assertTrue( "ALL now ADMIN"     , hasPriv(g.getName()     , subj.getId(), AccessPrivilege.ADMIN)  )
assertTrue( "revoke ALL ADMIN"  , revokePriv(g.getName()  , subj.getId(), AccessPrivilege.ADMIN)  )

grouperSession = grouperSession = GrouperSession.startRootSession();
member = MemberFinder.findBySubject(grouperSession, subj);
member.changeSubject(subj);
member.changeSubject(subj, !Member.DELETE_OLD_MEMBER);
member.changeSubjectReport(subj, Member.DELETE_OLD_MEMBER);

#
# TEARDOWN
#
quit

