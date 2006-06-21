#
# GrouperShell Tests
# $Id: test.gsh,v 1.1 2006-06-21 22:33:53 blair Exp $
#
rootExtn  = "uchicago"
nsExtn    = "nsit"
gExtn     = "nbs"
assert( "reset registry"          , resetRegistry()                         )
assert( "0 stems after reset"     , getStems("").size() == 0                )
assert( "0 groups after reset"    , getGroups("").size() == 0               )
root      = addRootStem(rootExtn, rootExtn)
rootName  = root.getName()
assert( "added root stem"         , root instanceof Stem )
assert( "there is now 1 stem"     , getStems("").size() == 1                )
ns        = addStem(rootName, nsExtn, nsExtn)
nsName    = ns.getName()
assert( "added child stem"        , ns instanceof Stem                      )
assert( "there are now 2 stems"   , getStems("").size() == 2                )
g         = addGroup(nsName, nsExtn, gExtn)
gName     = g.getName()
assert( "added child group"       , g instanceof Group                      )
assert( "there is now 1 group"    , getGroups("").size() == 1               )
assert( "deleted child group"     , delGroup(gName)                         )
assert( "there are now 0 groups"  , getGroups("").size() == 0               )
assert( "deleted child stem"      , delStem(nsName)                         )
assert( "there is now 1 stem"     , getStems("").size() == 1                )
assert( "deleted root stem"       , delStem(rootName)                       )
assert( "there are now 0 stems"   , getStems("").size() == 0                )
assert( "history()"               , history()                               )
assert( "history(1)"              , history(1)                              )
assert( "last()"                  , last()                                  )
assert( "last(0)"                 , last(0)                                 )
quit
