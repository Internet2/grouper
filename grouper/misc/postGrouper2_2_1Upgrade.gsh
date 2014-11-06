# Remove MEMBER, ADMIN, UPDATE, and GROUP_ATTR_UPDATE privileges assigned to GrouperAll
System.out.println("\n\n##########################################\n# Grouper 2.2.1 Upgrade Step 1/1: Remove MEMBER, ADMIN, UPDATE, and GROUP_ATTR_UPDATE privileges assigned to GrouperAll\n##########################################");
FindBadMemberships.checkAndFixGrouperAll()
