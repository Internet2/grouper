---
title: "Design document for subject source using data fields ABAC"
space: GrIntDev
pageId: 48792545
version: 13
lastUpdated: 2026-07-12T06:45:27.173Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792545/Design+document+for+subject+source+using+data+fields+ABAC
---

Work in a branch: [6180_data_fields_abac_subject_source](https://github.com/Internet2/grouper/tree/GRP-6180_data_fields_abac_subject_source)

## V5 changes

All resolvable users in a data field subject source has a row in grouper_members

1. Data providers have option for subject source (true/false)
  
  1. If true, ask for required subject source id, if it doesnt exist, ignore
  2. If true, then new users would get inserted in the grouper_members table and then get data added to data field assignments based on member internal id
    
    1. Get all the new users which do not have member rows
    2. Batch those up in transactions (include members table and attributes)
    3. Yes on sort and search columns
    4. Insert member row, get the internal id, insert attribute data, end transaction
    5. Failsafe (do 100, if it fails, do each, its ok if already there, no need update)
  3. New users would make the subject resolvable
  4. Full on the (subject source) data provider makes sure data field assignment values are correct AND makes sure the members table stuff is correct
2. Data field source
  
  1. Not have data field for subject ID (only in members table)
  2. All subject IDs are public
  3. Wizard to configure a new source  
    /grouper/grouperUi/app/UiV2Main.index?operation=UiV2SubjectSource.addSubjectSource  
    DataFieldSubjectSourceConfiguration (in subject source type)  
    **Subject source ID, Name, enabled, Types, Max results size is same**  
    no external system  
    **Extra attributes from source (paste in data field config ids, comma separated)**  
    **yes: Number of subject attributes**  
    same: **everything is same, but remove attribute.i.requireGroupNameForView (add friendly label)**  
    **Change number of subject attributes to 50.**  
    **add this in attribute:**
    
    
    
    1. Privacy data field source: true/false (default false). "Is this attribute one of the possible values of another attributes based on which data field privacy level the user can see?"
    2. (only show if true): Privacy attribute name: drop down of all attribute names (required)
    3. (only show if true: Privacy priority: dropdown of 1-5 (required), validate that no attributes have a privacy data source with same priority. also validate the priorities go from 1 to 5 and do not skip a number. "Select 1 for highest priority, and each priority must be used sequentially for this Privacy attribute name. e.g. "name" is the main attribute. "namePrivate" is priority 1. "nameFerpa" is priority 2. and "namePublic" is priority 3.
    
      
    Search and sort attributes are the same.  
    No SQL settings  
    Subject field mapping is same.  
    No **Attribute name for subject ID**  
     
    
    1. Each attribute can list multiple data fields which controls columns and rows in order privacy priority  
      
      
      1. How many names? 3
        
        1. Priority 1 name: pick the data field (show informational data fields): namePrivate
        2. Priority 2 name: nameFerpa
        3. Priority 3 name: namePublic
      2. How many identifiers? 3
        
        1. How many subject identifier0's? 2
          
          1. Priority 1 subjectIdentifier0: netId
          2. Priority 2 subjectIdentifier0: netIdPublic
      3. How many emails? 2
        
        1. Priority 1 email
          
          1. email
            
            1. Can GrouperSystem see? no
            2. Can GrouperEmail see? no
          2. emailPublic
            
            1. Can GrouperSystem see? yes
            2. Can GrouperEmail see? yes
    2. Associate the 5 search / sort fields with a privacy realm (or use group name if more convenient)
    3. When resolving a subject get the data for the subject in an intelligent way (get all the data for privacy the user can see, and prioritize, fill out subject)
  4. Code to lookup via data field assignments, or search/sort based on members table
    
    1. Will look at data fields, not the members table alone
    2. Subject data fields and privacy realms are cached centrally for 5 minutes
      
      1. We know all the privacy realms for all data field subjects
      2. We know all the group names/ids in the privacy groups used in all data fields subject sources (READers)
    3. Cache memberships of users in privacy groups as needed (a few minutes)
    4. Resolve a subject
      
      1. See if the memberships of readers groups in all data field subject sources if in cache, if so use that, if not, one query, put that in cache
      2. We have the list of subject datafield ids, filter out the ones that the grouper session user cant see
      3. FindById:
        
        1. Join to members table (filter on subject id (and source possibly) have bind variables for all data field ids (highest priority the grouper session subject can see) , get all that data.
      4. FindByIdentifier
        
        1. Join to members to get subject id, but query data field data of subject identifier data field that the user can see
      5. Find
        
        1. Join to members table, search the members table (filter by source), in the search column highest priority, the grouper session user can see
      6. Assembles the subject(s) from the data field values
  5. Subject identifier0
    
    1. In an example there could be 2 subjectIdenfiers0's with different privacy levels
      
      1. One is for public (does not include people who are privacy restricted)
      2. One is for privacy office only.
      3. When the subject is resolved, if the grouper session is not someone in privacy office (can READ the attribute), then its blank
      4. In members table it will be stored
    2. In an example imagine name could be 3 different privacy levels
      
      1. If none match a value, use the subject id (same for description). If the name is privacy restricted, and the grouper session user is not in the privacy office group.
      2. If there is a ferpa name (priority 1), and the grouper session user is ferpa trained staff, show that name
      3. If there is a staff level name (priority 2), and grouper session user is staff but not ferpa trained or in the privacy office, show that name
      4. If theres a public name value (priority 3), and the grouper session user is not staff, ferpa trained or in privacy office, show the public name

## V7 changes

1. Refactor existing jdbc subject source:
  
  1. Dynamically configured with insert statements
    
    1. grouper_config instead of subject.properties (can use traditional jdbc subject source or data field subject source)
    2. make the data fields, provider, data provider query, daemon for the subject and subject attributes table, inserts into members table
2. database search engine: use database full text search in oracle, postgres and postgres
  
  1. I think this needs searching and sorting and needs to be able to replace the grouper_members search and sort columns including privacy
  2. Im kind of talking myself out of this, since it would work differently for the three databases, adds more requirements for the DBAs than what we use currently, and might be difficult to support, but if you have experience with it and are optimistic about it, maybe you could do a POC and we can see if it would work. if you agree this is a can of worms we can just skip this and go down the second path
3. make our own search engine in the database
  
  1. (v7) table grouper_data_field_privacy_realm table:
    
    1. should privacy real config know that it is for subjects
    2. privacy realm internal id (primary key, can just be a bigint type)
    3. privacy realm config id (links this to a privacy realm in the config, the config has the group of people who can see it).
    4. privacy realm priority. integer, larger priority wins. for instance public is 0, member is 1, staff is 2, and ferpa trained staff is 3. whatever the institution wants
    5. institutional_flag T or F if the row is the privacy level that GrouperSystem uses
    6. As privacy realms are edited in UI they can affect this table
  2. (v7) table grouper_subject_privacy table:
    
    1. subject privacy internal id (primary key)
    2. subject privacy realm internal id (we can discuss this later), this needs a good bitmap index or something. There is only one privacy realm for groups which are subjects since that needs to use the flattened membership table to get READ/ADMIN privileges
    3. member internal id
  3. (v7) table grouper_subject_search_sort_entry table (only abac subject source data in v5):
    
    1. subject privacy internal id (from above)
    2. lower case search string (substring search)
    
    
    
    1. lower case sort string. do we need to consider ascii sort? i.e. maybe change all non alpha chars to a pipe (something after all the lowercase letters)
  4. table grouper_subject_identifier table (all three primary key?): (not sure we need this since data is also in data field assignment?)
    
    1. subject privacy internal id
    2. data field id
    3. identifier value
    4. there is something circular where grouper members table is list of subjects, but is resolvable over here, but this refers to that table. do we need another subject id table for the subject side? I was hoping not but it is hurting my brain... hmmm. ie when a new subject is resolvable, do we insert into grouper_members, and use that id to insert over here? seems weird=

Either way we need:

1. Full daemon which syncs privacy realms from config, selects all from each subject source, consults the privacy realms, and all from the tables above and fixes up the data.
2. Incremental daemon which looks for group renames and data field changes to update the search/sort tables
3. Drop the cols in the grouper_members table for search and sort in v7 (just a reminder)
4. Change the code that uses grouper_members search and sort and use these tables instead (v7)
