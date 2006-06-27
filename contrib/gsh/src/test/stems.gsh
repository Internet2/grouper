#
# GrouperShell Composite Tests
# $Id: stems.gsh,v 1.1 2006-06-27 19:28:29 blair Exp $
#

#
# SETUP
#
GSH_DEVEL = true
GSH_DEBUG = true
resetRegistry()
root      = addRootStem("uchicago", "uchicago")
ns        = addStem(root.getName(), "nsit", "nsit")

#
# TEST
#
assertTrue( "get: description"            , getStemAttr(ns.getName(), "description").equals("")                 )
assertTrue( "get: displayExtension"       , getStemAttr(ns.getName(), "displayExtension").equals("nsit")        )
assertTrue( "set: description"            , setStemAttr(ns.getName(), "description"     , "WE AIM TO PLEASE")   )
assertTrue( "set: displayExtension"       , setStemAttr(ns.getName(), "displayExtension", "NSIT!")              )
assertTrue( "get: description (new)"      , getStemAttr(ns.getName(), "description").equals("WE AIM TO PLEASE") )
assertTrue( "get: displayExtension (new)" , getStemAttr(ns.getName(), "displayExtension").equals("NSIT!")       )

#
# TEARDOWN
#
quit

