# Clear the temp change log before we start.
System.out.println("\n\n##########################################\n# Grouper 2.2 Upgrade Step 1/8: Clear the temp change log before we start\n##########################################")
loaderRunOneJob("CHANGE_LOG_changeLogTempToChangeLog")

# Add group sets for new privileges (groupAttrRead, groupAttrUpdate, stemAttrRead, etc)
System.out.println("\n\n##########################################\n# Grouper 2.2 Upgrade Step 2/8: Add group sets for new privileges (groupAttrRead, groupAttrUpdate, stemAttrRead, etc)\n##########################################");
new AddMissingGroupSets().addMissingSelfGroupSetsForGroups()
new AddMissingGroupSets().addMissingSelfGroupSetsForStems()
new AddMissingGroupSets().addMissingSelfGroupSetsForAttrDefs()

# Add new privileges to point in time (groupAttrRead, groupAttrUpdate, stemAttrRead, etc)
System.out.println("\n\n##########################################\n# Grouper 2.2 Upgrade Step 3/8: Add new privileges to point in time (groupAttrRead, groupAttrUpdate, stemAttrRead, etc)\n##########################################");
new SyncPITTables().processMissingActivePITFields()

# Remove old fields (legacy attributes) from point in time.
System.out.println("\n\n##########################################\n# Grouper 2.2 Upgrade Step 4/8: Remove old fields (legacy attributes) from point in time.\n##########################################");
new SyncPITTables().processMissingInactivePITFields()

# Migrate legacy attributes
System.out.println("\n\n##########################################\n# Grouper 2.2 Upgrade Step 5/8: Migrate legacy attributes\n##########################################");
new MigrateLegacyAttributes().saveUpdates(true).fullMigration()

# Add stem sets
System.out.println("\n\n##########################################\n# Grouper 2.2 Upgrade Step 6/8: Add stem sets\n##########################################");
new SyncStemSets().fullSync()

# Add group sets for new privileges to point in time (groupAttrRead, groupAttrUpdate, stemAttrRead, etc)
System.out.println("\n\n##########################################\n# Grouper 2.2 Upgrade Step 7/8: Add group sets for new privileges to point in time (groupAttrRead, groupAttrUpdate, stemAttrRead, etc)\n##########################################");
new SyncPITTables().sendFlattenedNotifications(false).processMissingActivePITGroupSets()

# Remove MEMBER, ADMIN, UPDATE, and GROUP_ATTR_UPDATE privileges assigned to GrouperAll
System.out.println("\n\n##########################################\n# Grouper 2.2 Upgrade Step 8/8: Remove MEMBER, ADMIN, UPDATE, and GROUP_ATTR_UPDATE privileges assigned to GrouperAll\n##########################################");
FindBadMemberships.checkAndFixGrouperAll()
