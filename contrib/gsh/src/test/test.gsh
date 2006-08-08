#
# GrouperShell Tests
# $Id: test.gsh,v 1.4 2006-08-08 17:56:10 blair Exp $
#

#
# FIXME Split and deprecate
#

#
# SETUP
#
GSH_DEVEL = true
version   = "0.0.2-cvs"
rootExtn  = "uchicago"
nsExtn    = "nsit"
gExtn     = "nbs"
subjType  = "person"
subjIdALL = "GrouperAll"
subjIdGS  = "GrouperSystem"
subjIdA   = "subj.A"
subjNameA = "subject a"

#
# TEST
#
#
assertTrue( "gsh version " + version    , version().equals(version)               )
assertTrue( "reset registry"            , resetRegistry()                         )
assertTrue( "3 sources"                 , getSources().size() == 3                )
subjGS    = findSubject(subjIdGS)
assertTrue( "subjGS !null"              , subjGS != null                          )
assertTrue( "found GrouperSystem"       , subjGS.getId().equals(subjIdGS)         )
subjALL   = findSubject(subjIdALL)
assertTrue( "subjALL !null"             , subjALL != null                         )
assertTrue( "found GrouperAll"          , subjALL.getId().equals(subjIdALL)       )
subjA     = addSubject(subjIdA, subjType, subjNameA)      
assertTrue( "subjA !null"               , subjA != null                           )
subj      = findSubject(subjIdA)
assertTrue( "subj !null: i"             , subj != null                            )
assertTrue( "find subject: i"           , subj.getId().equals(subjA.getId())      )
subj      = findSubject(subjIdA, subjType)
assertTrue( "subj !null: i, t"          , subj != null                            )
assertTrue( "find subject: i, t"        , subj.getId().equals(subjA.getId())      )
subj      = findSubject(subjIdA, subjType, "jdbc")   
assertTrue( "subj !null: i, t, s"       , subj != null                            )
assertTrue( "find subject: i, t, n"     , subj.getId().equals(subjA.getId())      )
stems     = getStems("")
assertTrue( "stems !null after reset"   , stems != null                           )
assertTrue( "0 stems after reset"       , stems.size() == 0                       )
groups    = getGroups("")
assertTrue( "groups !null after reset"  , groups != null                          )
assertTrue( "0 groups after reset"      , groups.size() == 0                      )
root      = addRootStem(rootExtn, rootExtn)
rootName  = root.getName()
assertTrue( "added root stem"           , root instanceof Stem )
assertTrue( "there is now 1 stem"       , getStems("").size() == 1                )
ns        = addStem(rootName, nsExtn, nsExtn)
nsName    = ns.getName()
assertTrue( "added child stem"          , ns instanceof Stem                      )
assertTrue( "there are now 2 stems"     , getStems("").size() == 2                )
g         = addGroup(nsName, nsExtn, gExtn)
gName     = g.getName()
assertTrue( "added child group"         , g instanceof Group                      )
assertTrue( "there is now 1 group"      , getGroups("").size() == 1               )
assertTrue( "group has 0 members"       , getMembers(gName).size() == 0           )
assertTrue( "added member to group"     , addMember(gName, subjIdA)               )
assertTrue( "group now has 1 member"    , getMembers(gName).size() == 1           )
assertTrue( "group hasMember"           , hasMember(gName, subjIdA)               )
assertTrue( "deleted member from group" , delMember(gName, subjIdA)               )
assertTrue( "group now has 0 members"   , getMembers(gName).size() == 0           )
assertTrue( "deleted child group"       , delGroup(gName)                         )
assertTrue( "there are now 0 groups"    , getGroups("").size() == 0               )
assertTrue( "deleted child stem"        , delStem(nsName)                         )
assertTrue( "there is now 1 stem"       , getStems("").size() == 1                )
assertTrue( "deleted root stem"         , delStem(rootName)                       )
assertTrue( "there are now 0 stems"     , getStems("").size() == 0                )
# TODO assertTrue( "history()"                 , history()                               )
# TODO assertTrue( "history(1)"                , history(1)                              )
# TODO assertTrue( "last()"                    , last()                                  )
# TODO assertTrue( "last(0)"                   , last(0)                                 )

#
# TEARDOWN
#
quit
