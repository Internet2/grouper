# Clear the temp change log before we start.
loaderRunOneJob("CHANGE_LOG_changeLogTempToChangeLog")

# Add group sets for new privileges (groupAttrRead, groupAttrUpdate, stemAttrRead, etc)
new AddMissingGroupSets().addMissingSelfGroupSetsForGroups()
new AddMissingGroupSets().addMissingSelfGroupSetsForStems()
new AddMissingGroupSets().addMissingSelfGroupSetsForAttrDefs()

# Add new privileges to point in time (groupAttrRead, groupAttrUpdate, stemAttrRead, etc)
new SyncPITTables().processMissingActivePITFields()

# Remove old fields (legacy attributes) from point in time.
new SyncPITTables().processMissingInactivePITFields()

# Migrate legacy attributes
new MigrateLegacyAttributes().saveUpdates(true).fullMigration()

# Add stem sets
new SyncStemSets().fullSync()

# Add group sets for new privileges to point in time (groupAttrRead, groupAttrUpdate, stemAttrRead, etc)
new SyncPITTables().sendFlattenedNotifications(false).processMissingActivePITGroupSets()
