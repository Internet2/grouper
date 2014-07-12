#
# GrouperShell Composite Tests
# $Id: groups.gsh,v 1.4 2009-11-05 06:10:51 mchyzer Exp $
#

#
# SETUP
#
GSH_DEVEL = true

root      = addRootStem("uchicago", "uchicago")
ns        = addStem(root.getName(), "nsit", "nsit")
ns2        = addStem(root.getName(), "nsit2", "nsit2")
ns3        = addStem(root.getName(), "nsit3", "nsit3")
ns4        = addStem(root.getName(), "nsit4", "nsit4")
g         = addGroup(ns.getName(), "nas", "nas")
grouperSession = GrouperSession.startRootSession();
groupType = GroupType.createType(grouperSession, "aGroupType", false); 
groupType.addAttribute(grouperSession, "aGroupAttribute", false);
g.addType(groupType, false);


#
# TEST
#
assertTrue( "get: description"        , getGroupAttr(g.getName(), "aGroupAttribute").equals("")                 )
assertTrue( "set: description"        , setGroupAttr(g.getName(), "aGroupAttribute" , "WE AIM TO PLEASE")       )
assertTrue( "get: description (new)"  , getGroupAttr(g.getName(), "aGroupAttribute").equals("WE AIM TO PLEASE") )

assertTrue( "before tx", transactionStatus() == 0)

# Transaction test
transactionStart("READ_WRITE_NEW")
assertTrue( "outer tx", transactionStatus() == 1)
g2 = addGroup(ns2.getName(), "nas2", "nas2")
transactionStart("READ_WRITE_NEW")
assertTrue( "inner rollback tx", transactionStatus() == 2)
g3 = addGroup(ns3.getName(), "nas3", "nas3")
transactionRollback("ROLLBACK_NOW")
transactionEnd()
assertTrue( "inner rollback end tx", transactionStatus() == 1)
transactionStart("READ_WRITE_NEW")
assertTrue( "inner commit tx", transactionStatus() == 2)
g4 = addGroup(ns4.getName(), "nas4", "nas4")
transactionEnd()
assertTrue( "inner commit end tx", transactionStatus() == 1)
transactionRollback("ROLLBACK_NOW")
transactionEnd()
assertTrue( "after tx", transactionStatus() == 0)

groups2 = getGroups("uchicago:nsit2:nas2")
groups3 = getGroups("uchicago:nsit3:nas3")
groups4 = getGroups("uchicago:nsit4:nas4")

assertTrue( "outer rollback", groups2.size() == 0)
assertTrue( "inner rollback", groups3.size() == 0)
assertTrue( "inner commit", groups4.size() == 1)

#
# TEARDOWN
#
quit

