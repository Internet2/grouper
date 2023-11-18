#
# GrouperShell XML Tests
# $Id: xml.gsh,v 1.1 2008-07-21 21:01:59 mchyzer Exp $
#

#
# SETUP
#
GSH_DEVEL = true
resetRegistry()
root      = addRootStem("uchicago", "uchicago")
ns        = addStem(root.getName(), "nsit", "nsit")
g         = addGroup(ns.getName(), "nas", "nas")

#
# TEST
#

# String
s = xmlToString()
assertTrue( "to: string"          , s != null               )
delGroup( g.getName() )
delStem( ns.getName() )
assertTrue( "load from: string"   , xmlFromString(s)        )
assertTrue( "update from: string" , xmlUpdateFromString(s)  )

# File
tmp   = File.createTempFile("gsh", ".xml")
tmp.deleteOnExit()
file  = tmp.getCanonicalPath()
assertTrue( "to: file"            , xmlToFile(file)         )
delGroup( g.getName() )
delStem( ns.getName() )
assertTrue( "load from: file"     , xmlFromFile(file)       )
assertTrue( "update from: file"   , xmlUpdateFromFile(file) )

# URL
tmp   = File.createTempFile("gsh", ".xml")
tmp.deleteOnExit()
file  = tmp.getCanonicalPath()
url   = tmp.toURL()
xmlToFile(file)
delGroup( g.getName() )
delStem( ns.getName() )
assertTrue( "load from: url"      , xmlFromURL(url)         )
assertTrue( "update from: url"    , xmlUpdateFromURL(url)   )

#
# TEARDOWN
#
quit

