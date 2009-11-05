#
# GrouperShell Composite Tests
# $Id: composites.gsh,v 1.2 2009-11-05 06:10:51 mchyzer Exp $
#

#
# SETUP
#
GSH_DEVEL = true
root      = addRootStem("uchicago", "uchicago")
ns        = addStem(root.getName(), "nsit", "nsit")
gA        = addGroup(ns.getName(), "nbs", "nbs")
gB        = addGroup(ns.getName(), "nas", "nas")
gC        = addGroup(ns.getName(), "nsd", "nsd")
hsA       = addSubject("subjectA", "person", "subject a")
hsB       = addSubject("subjectB", "person", "subject b")

#
# TEST
#
assertTrue( "add hsA to gA"         , addMember(gA.getName(), hsA.getId())  )
assertTrue( "add hsB to gB"         , addMember(gB.getName(), hsB.getId())  )
assertTrue( "add U(gA,gB) to gC"    , addComposite(gC.getName(), CompositeType.UNION, gA.getName(), gB.getName()) )
assertTrue( "gC members == 2"       , getMembers(gC.getName()).size() == 2  )
assertTrue( "del U(gA,gB) from gC"  , delComposite(gC.getName())            )
assertTrue( "gC members == 0"       , getMembers(gC.getName()).size() == 0  )

#
# TEARDOWN
#
quit

