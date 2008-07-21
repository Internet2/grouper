#
# GrouperShell Composite Tests
# $Id: stems.gsh,v 1.1 2008-07-21 21:01:59 mchyzer Exp $
#

#
# SETUP
#
GSH_DEVEL = true
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

