---
title: "Grouper loader configure via web service"
space: Grouper
pageId: 28555044
version: 8
lastUpdated: 2026-06-16T17:13:29.924Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555044/Grouper+loader+configure+via+web+service
---

> This page shows how to configure a SQL **loader** job on an existing group by assigning the Grouper loader attributes through the Grouper web service, using the grouperClient `assignAttributesWs` operation. This is a scripted alternative to configuring the loader in the Grouper UI or with GrouperShell (GSH).
> 
>  The loader attributes used here (`etc:legacy:attribute:legacyGroupType_grouperLoader` and the `etc:legacy:attribute:legacyAttribute_grouperLoader*` values) are auto-created by Grouper and are present in all currently supported releases (confirmed in `GrouperCheckConfig`), so this approach still works. The example output below was captured on the v2.3.0 demo server, so the web service URL and version strings reflect that release.

 > **Required privileges:** the web service user must be able to manage the target group (group ADMIN/UPDATE) and assign the Grouper loader attributes. Those attribute definitions live in the reserved `etc:legacy:attribute` area, so in practice this is a Grouper administrator. Configuring the database connection named by `grouperLoaderDbName` (here, `grouper`) requires that connection to be defined server-side in `grouper-loader.properties`.

  

## Configure the grouperClient

 Point the grouperClient at your web service in `grouper.client.properties`:

 
```text
# url of web service, should include everything up to the first resource to access
# e.g. http://groups.school.edu:8090/grouperWs/servicesRest
# e.g. https://groups.school.edu/grouperWs/servicesRest
grouperClient.webService.url = https://grouperdemo.internet2.edu/grouper-ws_v2_3/servicesRest

# kerberos principal used to connect to web service
# e.g. name/server.whatever.upenn.edu
grouperClient.webService.login = test

# password for shared secret authentication to web service
# or you can put a filename with an encrypted password
grouperClient.webService.password = XXXXXXXXXXX
```

 

## Loader attributes to assign

 Assigning the loader configuration is a two-part process. First assign the **marker** attribute to the group; this marks the group as a loader job and returns an attribute-assign id. Then assign each **value** attribute, using that marker assign id as the owner (`--ownerAttributeAssignUuids`).

 

| Step | Attribute (under `etc:legacy:attribute:`) | Assign type | Example value | What it sets |
| --- | --- | --- | --- | --- |
| 1 | `legacyGroupType_grouperLoader` | `group` (marker) | (none) | Marks the group as a loader job. The response returns an attribute-assign id used as the owner for steps 2–6. |
| 2 | `legacyAttribute_grouperLoaderType` | `group_asgn` | `SQL_SIMPLE` | Loader job type (a single group loaded from a query). |
| 3 | `legacyAttribute_grouperLoaderDbName` | `group_asgn` | `grouper` | Database connection name, defined in `grouper-loader.properties`. |
| 4 | `legacyAttribute_grouperLoaderQuartzCron` | `group_asgn` | `0 0 7 * * ?` | Quartz cron schedule (here, daily at 07:00). |
| 5 | `legacyAttribute_grouperLoaderQuery` | `group_asgn` | `SELECT subjectId AS subject_id FROM subject WHERE NAME LIKE 'Steven%'` | SQL returning the member subject ids for the group. |
| 6 | `legacyAttribute_grouperLoaderScheduleType` | `group_asgn` | `CRON` | Schedule type (`CRON` uses the Quartz cron above). |

 

## Assign the attributes via grouperClient

 Run the marker assignment first, against the group (here `test:testLoaderFromWebService`):

 
```bash
java -jar grouperClient.jar --operation=assignAttributesWs \
  --attributeAssignType=group --attributeAssignOperation=assign_attr \
  --attributeDefNameNames=etc:legacy:attribute:legacyGroupType_grouperLoader \
  --ownerGroupNames=test:testLoaderFromWebService
```

 The response includes a `WsAttributeAssign` with an `id` (in this run, `fca574eb763c412394bbf0916cc98e3f`). Use that id as `--ownerAttributeAssignUuids` for each value attribute:

 
```bash
# loader type
java -jar grouperClient.jar --operation=assignAttributesWs \
  --attributeAssignType=group_asgn --attributeAssignOperation=assign_attr \
  --attributeDefNameNames=etc:legacy:attribute:legacyAttribute_grouperLoaderType \
  --ownerAttributeAssignUuids=fca574eb763c412394bbf0916cc98e3f \
  --attributeAssignValueOperation=assign_value --values0System=SQL_SIMPLE

# database connection name
java -jar grouperClient.jar --operation=assignAttributesWs \
  --attributeAssignType=group_asgn --attributeAssignOperation=assign_attr \
  --attributeDefNameNames=etc:legacy:attribute:legacyAttribute_grouperLoaderDbName \
  --ownerAttributeAssignUuids=fca574eb763c412394bbf0916cc98e3f \
  --attributeAssignValueOperation=assign_value --values0System=grouper

# quartz cron schedule
java -jar grouperClient.jar --operation=assignAttributesWs \
  --attributeAssignType=group_asgn --attributeAssignOperation=assign_attr \
  --attributeDefNameNames=etc:legacy:attribute:legacyAttribute_grouperLoaderQuartzCron \
  --ownerAttributeAssignUuids=fca574eb763c412394bbf0916cc98e3f \
  --attributeAssignValueOperation=assign_value --values0System="0 0 7 * * ?"

# loader query
java -jar grouperClient.jar --operation=assignAttributesWs \
  --attributeAssignType=group_asgn --attributeAssignOperation=assign_attr \
  --attributeDefNameNames=etc:legacy:attribute:legacyAttribute_grouperLoaderQuery \
  --ownerAttributeAssignUuids=fca574eb763c412394bbf0916cc98e3f \
  --attributeAssignValueOperation=assign_value --values0System="SELECT subjectId AS subject_id FROM subject WHERE NAME LIKE 'Steven%'"

# schedule type
java -jar grouperClient.jar --operation=assignAttributesWs \
  --attributeAssignType=group_asgn --attributeAssignOperation=assign_attr \
  --attributeDefNameNames=etc:legacy:attribute:legacyAttribute_grouperLoaderScheduleType \
  --ownerAttributeAssignUuids=fca574eb763c412394bbf0916cc98e3f \
  --attributeAssignValueOperation=assign_value --values0System=CRON
```

 Add `--debug=true` to any command to see the full REST request and response. The marker assignment looks like this:

  
```xml
################ REQUEST START (indented) ###############
POST /grouper-ws_v2_3/servicesRest/v2_3_000/attributeAssignments HTTP/1.1
Content-Type: text/xml; charset=UTF-8
<WsRestAssignAttributesRequest>
  <attributeAssignOperation>assign_attr</attributeAssignOperation>
  <attributeAssignType>group</attributeAssignType>
  <wsAttributeDefNameLookups>
    <WsAttributeDefNameLookup>
      <name>etc:legacy:attribute:legacyGroupType_grouperLoader</name>
    </WsAttributeDefNameLookup>
  </wsAttributeDefNameLookups>
  <wsOwnerGroupLookups>
    <WsGroupLookup>
      <groupName>test:testLoaderFromWebService</groupName>
    </WsGroupLookup>
  </wsOwnerGroupLookups>
</WsRestAssignAttributesRequest>
################ REQUEST END ###############
################ RESPONSE START (indented) ###############
HTTP/1.1 200 OK
X-Grouper-success: T
<WsAssignAttributesResults>
  <wsAttributeAssignResults>
    <WsAssignAttributeResult>
      <wsAttributeAssigns>
        <WsAttributeAssign>
          <attributeAssignType>group</attributeAssignType>
          <attributeDefNameName>etc:legacy:attribute:legacyGroupType_grouperLoader</attributeDefNameName>
          <enabled>T</enabled>
          <id>fca574eb763c412394bbf0916cc98e3f</id>
          <ownerGroupName>test:testLoaderFromWebService</ownerGroupName>
        </WsAttributeAssign>
      </wsAttributeAssigns>
      <changed>T</changed>
    </WsAssignAttributeResult>
  </wsAttributeAssignResults>
  <resultMetadata>
    <resultCode>SUCCESS</resultCode>
    <success>T</success>
  </resultMetadata>
  <responseMetadata>
    <serverVersion>2.3.0</serverVersion>
  </responseMetadata>
</WsAssignAttributesResults>
################ RESPONSE END ###############
(attribute and attributeDefName metadata omitted for brevity)
```

  After all six assignments, the group carries the loader marker plus the five value attributes shown in the table above.
