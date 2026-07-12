---
title: "Migrating to data fields subject source"
space: Grouper
pageId: 28549728
version: 7
lastUpdated: 2025-11-14T16:21:33.157Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549728/Migrating+to+data+fields+subject+source
---

This page describes how to migrate to the data fields subject source introduced in Grouper v5. At a high level, data fields will be loaded into Grouper that represent your subject data. A new **disabled** subject source would be added and then you can test and compare between both sources. Then when all that looks good, you can swap the enabled flag and change the source IDs on both sources. So in the end after migration, you will still use the same source ID and you will not need new rows in the grouper_members table.

Note that after migration, new subjects won't be resolvable by Grouper until they have been loaded by the data provider. So in addition to running data provider full syncs, you may want to run incremental syncs using a change log as well. Or have your IAM system call a web service ( [Data Provider Subject List Sync](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549041/Data+Provider+Subject+List+Sync) ) when new subjects are available in your data provider source.

1. Add data fields for the new subject source.
  
  1. This involves creating a privacy realm, data fields that would map to your subject data, a data provider, and data provider queries.
  2. Details are available here: [Subject source using data fields](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549032/Subject+source+using+data+fields)
  3. For now, do not configure the data provider as a subject source.
2. Go to Miscellaneous → Daemon jobs in the UI and add a new daemon for the data provider.
  
  1. Select the Daemon type as "Data provider full sync".
  2. Run the daemon to load the data fields.
3. Go to Miscellaneous → Subject sources in the UI and add a new subject source of type DataFieldSubjectSourceConfiguration.
  
  1. Choose a "Subject source ID" that's different from your existing source.
  2. Set "Enabled" to false.
  3. Finish configuring the rest of the subject source with attributes that match your existing subject source and equivalent results for other parts of the configuration, e.g. subject identifiers, email, sort/search attributes, etc.
4. Go to Miscellaneous → Subject sources in the UI, under Actions (for either your old or new subject source), choose "Compare with another source".
  
  1. You can select various different Subject IDs, Subject Identifiers, and search strings and compare the results between both sources.
  2. Adjust the new source configuration as needed.
  3. 
  4.
5. Now it's time for the actual migration. Turn Grouper off - this includes all daemon nodes, UI, WS.
6. Change enabled flags and source IDs.
  
  1. Set the old source to enabled = false.
  2. Set the old source ID to another value.
  3. Set the new source ID to the old source ID (before it was renamed).
  4. Set the new source to enabled = true.
  5. If configuration is stored in the database, this can be edited using GSH. If you don't have access to run GSH, then the properties can be updated directly in the database in the grouper_config table. If the configuration is in files, then update the files instead.
    
    1. For example, to update the configuration (using GSH) on the new data field source if the new data field source config id is "dataFieldSubjectSource" and the old source ID is "jdbc":
    2. ```
      groovy:000> import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
      
      groovy:000> new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.id").value("jdbc").store();
      ===> Success: subject.properties property 'subjectApi.source.dataFieldSubjectSource.id' existed and was changed to 'jdbc' and saved
      
      groovy:000> new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.enabled").value("true").store();
      ===> Success: subject.properties property 'subjectApi.source.dataFieldSubjectSource.enabled' existed and was changed to 'true' and saved
      ```
    3. And you would have to similarly update the old source as well.
7. Restart GSH and sanity test.
  
  1. SubjectFinder.findById("some id", false)
  2. SubjectFinder.findByIdentifier("some identifier", false)
  3. SubjectFinder.findAll("first last")
8. Start the rest of the Grouper components
9. Edit the Data provider and select "true" for "Subject Source?" and specify the source ID.
10. Query the grouper_members table and confirm that there aren't any rows with either the temporary Source ID that was used in Step 3 or the Source ID for the old source that was set in Step 6b.
  
  1. select * from grouper_members where subject_source in (?, ?);
