#
# GrouperShell Group Type Tests
# $Id: group_types.gsh,v 1.3 2009-11-05 06:10:51 mchyzer Exp $
#

#
# SETUP
#
GSH_DEVEL = true
root      = addRootStem("uchicago", "uchicago")
ns        = addStem(root.getName(), "nsit", "nsit")
g         = addGroup(ns.getName(), "nas", "nas")

#
# TEST
#
name = "custom group type"

# first create the type
type    = typeAdd(name)
assertTrue( "type instanceof GroupType" , type instanceof GroupType )
attr    = typeAddAttr(name, "custom attribute", AccessPrivilege.READ, AccessPrivilege.ADMIN, false)
assertTrue( "added attr", attr.getType().equals(FieldType.ATTRIBUTE) )
list    = typeAddList(name, "custom list"     , AccessPrivilege.READ, AccessPrivilege.ADMIN)
assertTrue( "added list", list.getType().equals(FieldType.LIST) )
found   = typeFind(name)
assertTrue( "found custom type", found != null )
fields  = typeGetFields(name)
assertTrue( "type has 2 fields", fields.size() == 2 )

# now use it on a group
types   = groupGetTypes(g.getName())
assertTrue( "group has 1 type", types.size() == 1 )
assertTrue( "group has base type", groupHasType(g.getName(), "base") )
assertTrue( "added type to group", groupAddType(g.getName(), name) )
types   = groupGetTypes(g.getName())
assertTrue( "group now has 2 types", types.size() == 2 )
assertTrue( "group has custom type", groupHasType(g.getName(), name) )

# test adding and removing memberships using the custom list
subjA     = addSubject("subj.A", "person", "subject a")
assertTrue("subjA is not a member of the custom list of the group", hasMember(g.getName(), "subj.A", list) == false)
assertTrue("subjA is not a member of the default list of the group", hasMember(g.getName(), "subj.A") == false)
assertTrue("Add subjA to the custom list of the group", addMember(g.getName(), "subj.A", list) == true)
assertTrue("subjA is a member of the custom list of the group", hasMember(g.getName(), "subj.A", list) == true)
assertTrue("subjA is not a member of the default list of the group", hasMember(g.getName(), "subj.A") == false)
assertTrue("Delete subjA from the custom list of the group", delMember(g.getName(), "subj.A", list) == true)
assertTrue("subjA is not a member of the custom list of the group", hasMember(g.getName(), "subj.A", list) == false)
assertTrue("subjA is not a member of the default list of the group", hasMember(g.getName(), "subj.A") == false)

# remove the custom type from the list
assertTrue( "deleted type from group", groupDelType(g.getName(), name) )

# and then remove it
assertTrue( "deleted custom attr", typeDelField(name, "custom attribute") )
fields  = typeGetFields(name)
assertTrue( "type now has 1 field", fields.size() == 1 )
assertTrue( "deleted group type", typeDel(name) )

#
# TEARDOWN
#
quit

