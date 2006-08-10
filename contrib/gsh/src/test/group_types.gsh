#
# GrouperShell Group Type Tests
# $Id: group_types.gsh,v 1.2 2006-08-10 18:47:53 blair Exp $
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
name = "custom group type"

# first create the type
type    = typeAdd(name)
assertTrue( "type instanceof GroupType" , type instanceof GroupType )
attr    = typeAddAttr(name, "custom attribute", AccessPrivilege.READ, AccessPrivilege.ADMIN, false)
assertTrue( "added attr", attr.getType().equals(FieldType.ATTRIBUTE) )
list    = typeAddList(name, "custom list"     , AccessPrivilege.READ, AccessPrivilege.ADMIN)
assertTrue( "added list", list.getType().equals(FieldType.LIST) )
found   = typeFind(name)
assertTrue( "found custom type", found != null             )
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
assertTrue( "deleted type from group", groupDelType(g.getName(), name) )

# and then remove it
assertTrue( "deleted custom attr", typeDelField(name, "custom attribute") )
assertTrue( "type now has 1 field", fields.size() == 1 )
assertTrue( "deleted group type", typeDel(name) )

#
# TEARDOWN
#
quit

