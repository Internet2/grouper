#
# GrouperShell Group Type Tests
# $Id: group_types.gsh,v 1.1 2006-08-08 17:56:10 blair Exp $
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
assertTrue( "deleted custom attr", typeDelField(name, "custom attribute") )
assertTrue( "type now has 1 field", fields.size() == 1 )
assertTrue( "deleted group type", typeDel(name) )

#
# TEARDOWN
#
quit

