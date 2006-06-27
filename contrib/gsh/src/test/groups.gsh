#
# GrouperShell Composite Tests
# $Id: groups.gsh,v 1.1 2006-06-27 19:28:29 blair Exp $
#

#
# SETUP
#
GSH_DEVEL = true
GSH_DEBUG = true
resetRegistry()
root      = addRootStem("uchicago", "uchicago")
ns        = addStem(root.getName(), "nsit", "nsit")
g         = addGroup(ns.getName(), "nas", "nas")

#
# TEST
#
assertTrue( "get: description"        , getGroupAttr(g.getName(), "description").equals("")                 )
assertTrue( "set: description"        , setGroupAttr(g.getName(), "description" , "WE AIM TO PLEASE")       )
assertTrue( "get: description (new)"  , getGroupAttr(g.getName(), "description").equals("WE AIM TO PLEASE") )

#
# TEARDOWN
#
quit

