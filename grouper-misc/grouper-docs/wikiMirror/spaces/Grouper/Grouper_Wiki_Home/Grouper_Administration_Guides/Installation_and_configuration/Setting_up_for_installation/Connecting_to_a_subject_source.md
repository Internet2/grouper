---
title: "Connecting to a subject source"
space: Grouper
pageId: 28548079
version: 9
lastUpdated: 2026-02-24T21:42:57.467Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548079/Connecting+to+a+subject+source
---

# What are subject sources?

Subject sources are places where entities(i.e. the UI term for subjects), can be looked up in order to provide additional attributes from sources of authority such as:

- HRS, SIS
- Active Directory
- LDAP

These attributes can include:

- Unique identifiers
- Names (legal, preferred, etc)
- Contact (email, phone, address, etc)
- Affiliations (emeritus, faculty, staff, student, etc)
- Entitlements

While the most common subject sources provide information on individuals or accounts, Grouper can also consume subject sources for:

- Computers
- Services
- Other objects maintained by a back-end identity store.

# Configuring a subject source

Before running Grouper you need to configure a subject source. This is done in Home → Miscellaneous → Subject sources.  
  
  
To add a subject source:

1. Go to Actions → Add subject source
2. Add:  
    
    
    
  
  
  1. **Config ID** **(Required)**- the alphanumeric key in the config file that identifies this subject source. This is not necessarily the same as the source ID which is what you use in code or in API's to refer to this subject source
  2. **Subject source type **(Required)**** (LDAPSubjectSourceConfiguration or SqlSubjectSourceConfiguration) - Currently Grouper supports these source types. If there is not a source type that you need you need to ETL the data into a database, or build a subject source, or contact the Grouper dev team
3. Click **Submit**. The page will update with new sections based on your Subject source type.
4. Configure General settings by adding:
  
    
    
  
  
  - **Subject source ID (Required)** - The identifier of this source in code, API, loader, etc. This should be something descriptive, short, and alphanumeric. e.g. uncgperson
  - **Subject source name** **(Required)** - Friendly identifier for this source which can consist of friendly characters (e.g. spaces). This is used in drop downs in the UI
  - **Enabled** - Enables the subject source
  - **Types (Required)** - Pick 'person' if your source has mostly people or 'application' is mostly service principals
  - **External system (Required)**- LDAP or SQL external system to connect to. [Add another connection in the "external systems" config](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544712/Grouper+External+systems+configuration).
  - **Max results size** -  Max results size. Default value is '100'.
  - **Extra attributes from source** - more source attributes to retrieve from the source if they are not mapped to a subject attribute. e.g. in SQL this is extra columns. in LDAP this is extra LDAP attributes. These attributes are only used in translation scripts. Generally you will not have many of these.
  - **Number of subject attributes (Required) -**Subject will have attributes available to be called, and "internal" attributes which are used by Grouper but not visible otherwise. Select the count of the internal and non-internal subject attributes. There should be an attribute for a subject ID (opaque unchanging identifier), zero or many identifiers (don't have to be opaque, can change, e.g. netId or EPPN), name (first last), description (some descriptive text about a subject so people pick the correct search result), email (optional), etc.
5. For each subject attribute, configure:
  
  
  
  1. **name (required)**
  2. **format to lower case** - If the value of the subject attribute should be converted to lower case in the subject
  3. **translation type (required) -** 'Subject attribute' is the attribute name on the Grouper side. 'Source attribute' is the attribute in the source, e.g. the ldap attribute, sql column, etc. Three options. The subject attribute (Grouper side) has an attribute name that is the same name as the source side (e.g. SQL column name, LDAP attribute, etc): same. Or you can pick a sourceAttribute to map this subject attribute to (e.g. same value but rename it): rename. Or you can write a scriptlet (translation) to make it more dynamic and adjust the value or use multiple source or subject attributes: translation script.
    
    
    
    1. For translation scripts, We may need variables from source, and variables for other subject attributes. suggestion is ${source_attribute__first_name} - gets an attribute from the source query or filter. in this case 'first_name' column ${subject_attribute__description} - references a built in subject field, in this case the description field ${subject_attribute__emailaddress} - references a previously configured subject attribute. in this case "emailAddress". The key is lower case. 
      
      1. Description example: `${source_attribute__sn + ', ' + source_attribute__givenname + ' (' + subject_attribute__cn + ') (ADM)'}```
      2. Longer description example, using the ternary operator to exclude separators for potentially missing fields: 
        ```
        ${source_attribute__displayname
        + " ("
        + source_attribute__uid
        + (source_attribute__edupersonuniqueid ? "/" + source_attribute__edupersonuniqueid : "")
        + ") - "
        + source_attribute__edupersonprimaryaffiliation
        + (source_attribute__title ? " - " + source_attribute__title : "" )
        + (source_attribute__ou ? "/" + source_attribute__ou :  "")
        }
        ```
  4. **subject identifier** - If this is a subject identifier. A subject identifier is a value that uniquely identifies a subject, though this identifier might change over time (e.g. a netId might change when a name changes). Default value is 'false'.
  5. **internal** - Internal attributes are not available in the UI or WS, they are just used internally for Grouper. e.g. an attribute used for sorting. Default value is 'false'.
  6. **export header** - When exporting a group's memberships, this is the header (e.g. for CSV). Should be alphanumeric and should not be an internal label: subjectid, entityid, sourceid, memberid, name, description, screenlabel
  7. **attribute.i.requireGroupNameForView** - When viewing a subject, if there is a group assigned here, then make sure the calling user is a member of this group, or else null out this attribute
6. Map subject fields by selecting the desired attributes for:  
    
    
    
  
  
  1. **Subject ID **(Required)****
  2. **Name **(Required)****
  3. **Description **(Required)****
  4. **Email**
  5. **Net ID**
  6. **subjectIdentifier0-2**
7. Specify the number of attributes subjects can be searched by [1-5], then select the fields in the resulting drop-downs **(Required)**
8. Configure your LDAP Settings  
    
    
    
  
  
  1. **Subject ID search filter**
  2. **Search subject scope**
  3. **Search subject base (Required)**
  4. **Subject identifier search filter (Required)**
  5. **Subject free-form search filter**
  6. **Fetch multiple results**(Default: False)
  7. **Throw error on find all failure**(Default: True)
  8. **Max page size**
  9. **Error on max results**(Default: True)
  10. **multivaluedLdapAttributes** - comma-separated list of lower-cased attribute names
9. Configure the Configuration Check and Subject Source Diagnostics. Optional, but useful in troubleshooting and initial configuration (TODO: Steps and Screenshots)
10. Click Submit to save your configuration.
