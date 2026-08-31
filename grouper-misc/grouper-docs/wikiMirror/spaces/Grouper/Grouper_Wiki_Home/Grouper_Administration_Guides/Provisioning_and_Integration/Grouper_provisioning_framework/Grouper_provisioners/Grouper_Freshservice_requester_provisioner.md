---
title: "Grouper Freshservice requester provisioner"
space: Grouper
pageId: 28554244
version: 10
lastUpdated: 2026-07-01T05:40:51.411Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554244/Grouper+Freshservice+requester+provisioner
---

> The info on this page applies to Grouper v4 and above.

## External System

[Grouper Freshservice external system](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547422/Grouper+Freshservice+external+system)

## Links

- Freshservice URL: https://somedomainname.freshservice.com
- [Freshservice API docs](https://api.freshservice.com/)

## Overview

The Freshservice Requester Provisioner manages **requester users**, **requester groups**, and **group memberships** in a [Freshservice](https://www.freshservice.com/) instance via the [Freshservice REST API v2](https://api.freshservice.com/).

The provisioning type is `membershipObjects`, meaning Grouper independently manages three object types in Freshservice:

- **Entities** — Freshservice requester users (people who submit tickets)
- **Groups** — Freshservice requester groups
- **Memberships** — Associations between requester users and requester groups

Because Freshservice assigns its own numeric IDs to users and groups, the provisioner uses **group and entity link** (target attribute value caches) to track the Freshservice-assigned IDs for each Grouper object.  
The provisioner class is `edu.internet2.middleware.grouper.app.freshServiceRequester.FreshRequesterProvisioner`.

You must track Freshservice agents in your Grouper registry and subtract them as provisionable from your requester provisioner.

Freshservice has a SCIM app that as of Feb 2026 does not follow the SCIM spec and does not work. It is recommended to use this provisioner instead.

## Provisioning attributes

Advice

- Provisioning type is hardcoded to membershipObjects
- Use group and entity link (since there are uuids in the target for groups and entities that need to be looked up)

#### Requester group attributes. [API](https://api.freshservice.com/#requester_groups)

| Grouper Attribute Name | Type | Required? | Freshservice API Field | Description |
| --- | --- | --- | --- | --- |
| `id` | String (though its numeric) | Yes | `id` | Freshservice-assigned group ID. **Read-only / select-only.** Do not translate this from Grouper. Configure it as a target attribute and cache it in an attribute value cache so the provisioner can look up the group by ID on subsequent runs. |
| `name` | String | Yes | `name` | The display name of the requester group in Freshservice. Typically translated from the Grouper group `extension`, `displayExtension`, or a metadata string. Use a single provisionable folder to prevent name collisions. |
| `description` | String | No | `description` | The description of the requester group. Typically translated from the Grouper group `description`. |

#### Requester (user) attributes. [API](https://api.freshservice.com/#requester_attributes)

The provisioner supports the following built-in entity attributes. These appear in the dropdown when configuring target entity attributes in the UI. Deleting an entity through the provisioner performs a soft-delete (deactivation), not a permanent removal.

| Grouper Attribute Name | Type | Required? | Freshservice API Field | Description |
| --- | --- | --- | --- | --- |
| `id` | String (though its numeric) | Yes | `id` | Freshservice-assigned user ID. **Read-only / select-only.** Do not translate from Grouper. Cache it in an attribute value cache for linking. |
| `firstName` | String | Yes (for create) | `first_name` | First name of the requester in Freshservice. |
| `lastName` | String | Yes (for create) | `last_name` | Last name of the requester in Freshservice. |
| `email` | String | Yes | `primary_email` | Primary email address. Note: the Freshservice API field is `primary_email`, not `email`. This is used for entity matching and for the upsert check on create. |
| `externalId` | String | No | `external_id` | External identifier (e.g., institutional ID, pennkey). Can be used as an entity matching attribute for lookups. |
| `jobTitle` | String | No | `job_title` | The user's job title. |
| `workPhoneNumber` | String | No | `work_phone_number` | The user's work phone number. |
| `departmentId` | Long | No | `department_ids` (array) | The Freshservice department ID. The API uses an array (`department_ids`), but Grouper models a single value. On read, the first element is used. On write, the value is wrapped in a single-element array. |
| `reportingManagerId` | Long | No | `reporting_manager_id` | Freshservice ID of the user's reporting manager (must be an existing requester). |
| `address` | String | No | `address` | The user's address. |

### Custom Fields

Freshservice supports custom fields on requester users. These are arbitrary fields defined in your Freshservice instance's admin settings under **Admin → User Management → User Fields (requester fields)**

In Grouper, custom fields are represented as entity attributes with the prefix `customField_`. For example, if your Freshservice instance has a custom field called `pennkey`, configure it in Grouper as `customField_pennkey`.

#### Supported Custom Field Value Types

| Type | Notes |
| --- | --- |
| `String` | Text values |
| `Long` / `Integer` | Whole numbers (integral). Internally normalized to `Long`. |
| `Boolean` | True/false values |

**Decimal numbers (`Float`/`Double`) are not supported** as custom field values and will cause a runtime exception.

#### Custom Field Configuration Example

Custom fields do **not** appear in the entity attribute name dropdown in the UI. You must type the attribute name manually using the `customField_<fieldName>` convention. The `<fieldName>` portion must exactly match the custom field name defined in Freshservice. Set the attribute name with an EL:

```
${'customField_theName'}
```

It is easiest to just have "text" type field, but if you must have numeric, make sure the value type on the custom field in Grouper is Long (recommended)

``

#### Custom Fields as Search/Matching Attributes

Custom fields can be used for entity searching. When searching by a custom field, the Freshservice query uses the format:

`GET /api/v2/requesters?query=<fieldName>:'value' (for String values) GET /api/v2/requesters?query=<fieldName>:12345 (for numeric values)`

### Entity and Group Matching

Matching tells the provisioner how to find existing Freshservice objects that correspond to Grouper objects. This is critical for the provisioner to correctly link Grouper groups/entities to their Freshservice counterparts.

#### Entity Matching

Generally you should have id as the first search/match attribute. Then if you can have externalId or a customField with is opaque and unchanging, that is best to change email changes. Otherwise use id/email. The entity matching system supports searching by the following attribute names:

| Searching/Matching Attribute | Freshservice Lookup Method | Notes |
| --- | --- | --- |
| `id` | `GET /api/v2/requesters/{id}` | Direct lookup by Freshservice-assigned ID. Fastest method. |
| `email` | `GET /api/v2/requesters?email=value` | Search by primary email address. Most common for initial matching. |
| `externalId` | `GET /api/v2/requesters?query=external_id:'value'` | Search by external identifier. |
| `customField_<name>` | `GET /api/v2/requesters?query=<name>:'value'` | Search by any custom field for text. The `customField_` prefix is stripped to get the Freshservice field name. |
| `customField_<name>` | `GET /api/v2/requesters?query=<name>:value` | Search by any custom field for long (integer). The `customField_` prefix is stripped to get the Freshservice field name. |

If multiple requesters match a query, the provisioner throws an exception. Ensure your matching attributes have unique values. Freshservice **agents** (users with `is_agent=true`) are automatically excluded from all search results.

### CRUD Operations

The provisioner supports the following operations. Use `customizeEntityCrud`, `customizeGroupCrud`, and `customizeMembershipCrud` to enable fine-grained control over which operations are allowed.

| Object | Operation | Supported? | Notes |
| --- | --- | --- | --- |
| Entity (User) | Retrieve all | Yes | Retrieves all active requesters (not agents) |
| Retrieve one | Yes | By id, email, externalId, or customField_* |
| Insert | Yes | Upsert behavior: reactivates and updates if email already exists |
| Update | Yes | GET-then-PUT: only changed fields are sent |
| Delete | Yes | **Soft delete** (deactivation), not permanent removal |
| Group | Retrieve all | Yes | Excludes rule_based groups |
| Retrieve one | Yes | By id (direct) or name (client-side filter) |
| Insert | Yes | 409 = already exists |
| Update | Yes | GET-then-PUT |
| Delete | Yes | Permanent deletion |
| Membership | Retrieve by group | Yes | Lists all members of a requester group |
| Insert | Yes | Adds user to group |
| Delete | Yes | Removes user from group |

## Behavioral Notes

### Entity Create is Upsert

When creating a new requester user, the provisioner first checks if a user with the same `primary_email` already exists in Freshservice (including deactivated users). If a match is found:

1. If the existing user is **deactivated**, the provisioner reactivates them first (`PUT /api/v2/requesters/{id}/reactivate`).
2. Then the provisioner **updates** the user with all the provisioned attribute values.

Only if no matching email is found does the provisioner perform a true `POST` create.

### Entity Delete is Soft Delete

When the provisioner deletes an entity, it calls `DELETE /api/v2/requesters/{id}`, which **deactivates** the requester in Freshservice rather than permanently removing them. The user remains in Freshservice in a deactivated state and can be reactivated later.  
A separate GDPR "forget" operation (`DELETE /api/v2/requesters/{id}/forget`) exists in the Freshservice API for permanent deletion, but it is **not** used by the provisioner's standard delete flow.

### Entity Update is GET-then-PUT

When updating a requester user, the provisioner:

1. GETs the current state of the user from Freshservice.
2. Deep-copies the response to a mutable JSON object.
3. Strips read-only fields: `id`, `created_at`, `has_logged_in`, `is_agent`, `updated_at`, `work_schedule_id`, `department_names`, `location_name`.
4. Overlays only the fields that changed (from the provisioning object changes).
5. PUTs the modified object back.

This approach ensures that Freshservice attributes **not managed by Grouper** are preserved.

### Agents are Excluded

Freshservice distinguishes between **requesters** (users who submit tickets) and **agents** (IT staff who handle tickets). This provisioner only manages requesters. Users with `is_agent=true` are automatically filtered out of all retrieval and search operations.

### Rule-Based Groups are Excluded

Freshservice "rule-based" groups (identified by `type: "rule_based"` in the API response) cannot be managed via the API. These groups are automatically excluded from all group retrieval operations.

### Department IDs

The Freshservice API models department associations as an array (`department_ids`), supporting multiple departments per user. However, the Grouper provisioner models this as a single `departmentId` (Long). When reading from Freshservice, only the first department ID in the array is used. When writing, the single value is wrapped in a one-element array. There is an example GSH daemon to manage departments in Freshservice.

You need a table to store the departments locally (with their ID), and you can join to that in your entity attribute SQL resolver for the provisioner. Here is sample code that you can tailor to your environment:

```
import java.io.File;
import java.io.FileReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;

import org.apache.commons.csv.*;
import org.apache.commons.lang3.*;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

import edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.grouper.CompositeSave;
import edu.internet2.middleware.grouper.ai.openai.*;
import edu.internet2.middleware.grouper.app.attestation.*;
import edu.internet2.middleware.grouper.app.azure.GrouperAzureApiCommands;
import edu.internet2.middleware.grouper.app.azure.GrouperAzureGroup;
import edu.internet2.middleware.grouper.app.azure.GrouperAzureUser;
import edu.internet2.middleware.grouper.app.externalSystem.*;
import edu.internet2.middleware.grouper.app.grouperTypes.*;
import edu.internet2.middleware.grouper.app.gsh.template.*;
import edu.internet2.middleware.grouper.app.loader.*;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.app.provisioning.*;
import edu.internet2.middleware.grouper.app.reports.*;
import edu.internet2.middleware.grouper.attr.finder.*;
import edu.internet2.middleware.grouper.attr.value.*;
import edu.internet2.middleware.grouper.attr.*;
import edu.internet2.middleware.grouper.attr.assign.*;
import edu.internet2.middleware.grouper.authentication.*;
import edu.internet2.middleware.grouper.cfg.text.*;
import edu.internet2.middleware.grouper.exception.*;
import edu.internet2.middleware.grouper.group.*;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.ldap.*;
import edu.internet2.middleware.grouper.membership.*;
import edu.internet2.middleware.grouper.misc.*;
import edu.internet2.middleware.grouper.util.*;
import edu.internet2.middleware.grouperClient.jdbc.*;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.*;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.subject.*;
import edu.internet2.middleware.grouper.privs.*;

public class Test159bsdFreshServiceDepartmentSync {

  private static final String FRESH_SERVICE_CONFIG_ID = "bsdFreshService";
  private static final String PCOM_CONNECTION_NAME = "pennCommunity";
  private static final int MAX_PAGE_SIZE = 100;
  private static final Set<String> doNotLogHeaders = GrouperUtil.toSet("authorization");
  private static final Set<String> doNotLogParameters = GrouperUtil.toSet("client_secret");
  private static final String NOT_USED_SUFFIX = " - NOT USED";

  public static void main(String[] args) {

    Hib3GrouperLoaderLog hib3GrouperLoaderLog = OtherJobScript.retrieveHib3GrouperLoaderLogNotNull();

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    // Basic run metadata for troubleshooting (values must be scalar types only).
    debugMap.put("daemonConfigId", "bsdFreshServiceDepartmentSync");
    debugMap.put("freshServiceExternalSystem", FRESH_SERVICE_CONFIG_ID);
    debugMap.put("runTimestamp", new Timestamp(System.currentTimeMillis()).toString());

    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.startRootSession();

      // Step 1: Read what departments should be from the view
      // authzadm.AUTHZ_BSD_FRESHSERVICE_DEPTS_V@pcom has: ORG_CODE, DEPARTMENT_NAME_SHOULD_BE, ORG_DISPLAY_NAME, CENTER_NAME
      List<Object[]> deptsShouldBeRows = new GcDbAccess().connectionName(PCOM_CONNECTION_NAME).
          sql("select ORG_CODE, DEPARTMENT_NAME_SHOULD_BE from authzadm.AUTHZ_BSD_FRESHSERVICE_DEPTS_V").
          selectList(Object[].class);

      debugMap.put("deptsShouldBeCount", GrouperUtil.length(deptsShouldBeRows));

      // orgCode -> departmentNameShouldBe
      Map<String, String> deptsShouldBeMap = new LinkedHashMap<String, String>();
      for (Object[] row : GrouperUtil.nonNull(deptsShouldBeRows)) {
        String orgCode = GrouperUtil.stringValue(row[0]);
        String departmentNameShouldBe = GrouperUtil.stringValue(row[1]);
        if (StringUtils.isNotBlank(orgCode) && StringUtils.isNotBlank(departmentNameShouldBe)) {
          deptsShouldBeMap.put(orgCode, departmentNameShouldBe);
        }
      }

      // Step 2: Read the tracking table of Freshservice departments with their IDs
      // authzadm.AUTHZ_BSD_FRESHSERVICE_DEPTS@pcom has: ORG_CODE, FRESH_SERVICE_DEPT_ID, FRESH_SERVICE_DEPT_NAME_IS
      List<Object[]> deptsTrackingRows = new GcDbAccess().connectionName(PCOM_CONNECTION_NAME).
          sql("select ORG_CODE, FRESH_SERVICE_DEPT_ID, FRESH_SERVICE_DEPT_NAME_IS from authzadm.AUTHZ_BSD_FRESHSERVICE_DEPTS").
          selectList(Object[].class);

      debugMap.put("deptsTrackingCount", GrouperUtil.length(deptsTrackingRows));

      // orgCode -> [freshServiceDeptId, freshServiceDeptNameIs]
      Map<String, Long> trackingOrgCodeToDeptId = new LinkedHashMap<String, Long>();
      Map<String, String> trackingOrgCodeToDeptName = new LinkedHashMap<String, String>();
      for (Object[] row : GrouperUtil.nonNull(deptsTrackingRows)) {
        String orgCode = GrouperUtil.stringValue(row[0]);
        Long freshServiceDeptId = GrouperUtil.longObjectValue(row[1], true);
        String freshServiceDeptNameIs = GrouperUtil.stringValue(row[2]);
        if (StringUtils.isNotBlank(orgCode) && freshServiceDeptId != null) {
          trackingOrgCodeToDeptId.put(orgCode, freshServiceDeptId);
          trackingOrgCodeToDeptName.put(orgCode, freshServiceDeptNameIs);
        }
      }

      // Step 3: Read all Freshservice departments from their API
      List<JsonNode> freshServiceDepts = retrieveDepartments(FRESH_SERVICE_CONFIG_ID);

      debugMap.put("freshServiceDeptsApiCount", GrouperUtil.length(freshServiceDepts));

      // Build a map of freshServiceDeptId -> deptName from the API
      Map<Long, String> freshServiceDeptIdToName = new LinkedHashMap<Long, String>();
      // Also build a map of orgCode prefix -> freshServiceDeptId for matching
      // Department names in Freshservice are prefixed with the ORG_CODE (e.g. "8342 - Penn First Plus")
      Map<String, Long> freshServiceOrgCodeToDeptId = new LinkedHashMap<String, Long>();
      Map<String, String> freshServiceOrgCodeToDeptName = new LinkedHashMap<String, String>();
      for (JsonNode deptNode : GrouperUtil.nonNull(freshServiceDepts)) {
        Long deptId = GrouperUtil.jsonJacksonGetLong(deptNode, "id");
        String deptName = GrouperUtil.jsonJacksonGetString(deptNode, "name");
        if (deptId != null && StringUtils.isNotBlank(deptName)) {
          freshServiceDeptIdToName.put(deptId, deptName);
          // Extract the org code prefix (the part before " - ")
          String orgCodePrefix = extractOrgCodePrefix(deptName);
          if (orgCodePrefix != null) {
            freshServiceOrgCodeToDeptId.put(orgCodePrefix, deptId);
            freshServiceOrgCodeToDeptName.put(orgCodePrefix, deptName);
          }
        }
      }

      // Step 4: Backfill tracking table for any Freshservice department (with an org code prefix)
      // that already exists in the API but is not yet in AUTHZ_BSD_FRESHSERVICE_DEPTS
      int backfillCount = 0;
      for (Map.Entry<String, Long> entry : freshServiceOrgCodeToDeptId.entrySet()) {
        String orgCode = entry.getKey();
        Long freshDeptId = entry.getValue();
        String freshDeptName = freshServiceOrgCodeToDeptName.get(orgCode);
        if (!trackingOrgCodeToDeptId.containsKey(orgCode)) {
          new GcDbAccess().connectionName(PCOM_CONNECTION_NAME).
              sql("insert into authzadm.AUTHZ_BSD_FRESHSERVICE_DEPTS (ORG_CODE, FRESH_SERVICE_DEPT_ID, FRESH_SERVICE_DEPT_NAME_IS) values (?, ?, ?)").
              addBindVar(orgCode).addBindVar(freshDeptId).addBindVar(freshDeptName).
              executeSql();
          trackingOrgCodeToDeptId.put(orgCode, freshDeptId);
          trackingOrgCodeToDeptName.put(orgCode, freshDeptName);
          backfillCount++;
        }
      }

      int insertsCount = 0;
      int updatesCount = 0;
      int notUsedCount = 0;

      // Step 5: Process new departments - org codes in deptsShouldBe but not in Freshservice
      for (Map.Entry<String, String> entry : deptsShouldBeMap.entrySet()) {
        String orgCode = entry.getKey();
        String departmentNameShouldBe = entry.getValue();

        // Check if this org code already exists in Freshservice (by org code prefix in the name)
        if (freshServiceOrgCodeToDeptId.containsKey(orgCode)) {
          // Already exists in Freshservice, will handle update below
          continue;
        }

        // New department: create in Freshservice
        JsonNode createdDept = createDepartment(FRESH_SERVICE_CONFIG_ID, departmentNameShouldBe);
        Long newDeptId = GrouperUtil.jsonJacksonGetLong(createdDept, "id");

        if (newDeptId != null) {
          // Insert into tracking table
          new GcDbAccess().connectionName(PCOM_CONNECTION_NAME).
              sql("insert into authzadm.AUTHZ_BSD_FRESHSERVICE_DEPTS (ORG_CODE, FRESH_SERVICE_DEPT_ID, FRESH_SERVICE_DEPT_NAME_IS) values (?, ?, ?)").
              addBindVar(orgCode).addBindVar(newDeptId).addBindVar(departmentNameShouldBe).
              executeSql();

          // Update local maps so subsequent logic sees the new department
          trackingOrgCodeToDeptId.put(orgCode, newDeptId);
          trackingOrgCodeToDeptName.put(orgCode, departmentNameShouldBe);
          freshServiceOrgCodeToDeptId.put(orgCode, newDeptId);
          freshServiceOrgCodeToDeptName.put(orgCode, departmentNameShouldBe);
          freshServiceDeptIdToName.put(newDeptId, departmentNameShouldBe);

          insertsCount++;
          debugMap.put("createdDept_" + orgCode, departmentNameShouldBe + " (id=" + newDeptId + ")");
        }
      }

      // Step 6: Update departments where name doesn't match
      for (Map.Entry<String, String> entry : deptsShouldBeMap.entrySet()) {
        String orgCode = entry.getKey();
        String departmentNameShouldBe = entry.getValue();

        // Find this org code in Freshservice
        Long freshDeptId = freshServiceOrgCodeToDeptId.get(orgCode);
        if (freshDeptId == null) {
          // This shouldn't happen since we just created missing ones, but skip if so
          continue;
        }

        String currentFreshServiceName = freshServiceOrgCodeToDeptName.get(orgCode);

        // If the current name in Freshservice doesn't match what it should be, update it
        if (!StringUtils.equals(currentFreshServiceName, departmentNameShouldBe)) {
          updateDepartment(FRESH_SERVICE_CONFIG_ID, freshDeptId, departmentNameShouldBe);

          // Update tracking table
          new GcDbAccess().connectionName(PCOM_CONNECTION_NAME).
              sql("update authzadm.AUTHZ_BSD_FRESHSERVICE_DEPTS set FRESH_SERVICE_DEPT_NAME_IS = ? where ORG_CODE = ?").
              addBindVar(departmentNameShouldBe).addBindVar(orgCode).
              executeSql();

          trackingOrgCodeToDeptName.put(orgCode, departmentNameShouldBe);

          updatesCount++;
          debugMap.put("updatedDept_" + orgCode, currentFreshServiceName + " -> " + departmentNameShouldBe);
        }
      }

      // Step 7: Mark departments as NOT USED if their org code is not in deptsShouldBe
      for (Map.Entry<String, Long> entry : freshServiceOrgCodeToDeptId.entrySet()) {
        String orgCode = entry.getKey();
        Long freshDeptId = entry.getValue();
        String currentName = freshServiceOrgCodeToDeptName.get(orgCode);

        // If this org code is NOT in deptsShouldBe, mark as not used
        if (!deptsShouldBeMap.containsKey(orgCode)) {
          // Only add suffix if not already there
          if (currentName != null && !currentName.endsWith(NOT_USED_SUFFIX)) {
            String notUsedName = currentName + NOT_USED_SUFFIX;
            updateDepartment(FRESH_SERVICE_CONFIG_ID, freshDeptId, notUsedName);

            // Update tracking table
            new GcDbAccess().connectionName(PCOM_CONNECTION_NAME).
                sql("update authzadm.AUTHZ_BSD_FRESHSERVICE_DEPTS set FRESH_SERVICE_DEPT_NAME_IS = ? where ORG_CODE = ?").
                addBindVar(notUsedName).addBindVar(orgCode).
                executeSql();

            notUsedCount++;
            debugMap.put("notUsedDept_" + orgCode, currentName + " -> " + notUsedName);
          }
        }
      }

      debugMap.put("backfillCount", backfillCount);
      debugMap.put("insertsCount", insertsCount);
      debugMap.put("updatesCount", updatesCount);
      debugMap.put("notUsedCount", notUsedCount);

      hib3GrouperLoaderLog.setInsertCount(backfillCount + insertsCount);
      hib3GrouperLoaderLog.setUpdateCount(updatesCount + notUsedCount);
      hib3GrouperLoaderLog.setTotalCount(freshServiceOrgCodeToDeptId.size());

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
      String debugMapForLog = GrouperUtil.toStringForLog(debugMap);
      hib3GrouperLoaderLog.setJobMessage(debugMapForLog);
      if (OtherJobScript.retrieveFromThreadLocal() == null) {
        System.out.println(debugMapForLog);
        System.exit(0);
      }
    }

  }

  /**
   * Extract the org code prefix from a Freshservice department name.
   * Department names are prefixed with the org code, e.g. "8342 - Penn First Plus".
   * The org code is the numeric part before " - ".
   * @param deptName the department name
   * @return the org code, or null if the name does not have the expected prefix
   */
  private static String extractOrgCodePrefix(String deptName) {
    if (StringUtils.isBlank(deptName)) {
      return null;
    }
    // Remove " - NOT USED" suffix if present before extracting
    String nameForParsing = deptName;
    if (nameForParsing.endsWith(NOT_USED_SUFFIX)) {
      nameForParsing = nameForParsing.substring(0, nameForParsing.length() - NOT_USED_SUFFIX.length());
    }
    int dashIndex = nameForParsing.indexOf(" - ");
    if (dashIndex > 0) {
      String prefix = nameForParsing.substring(0, dashIndex).trim();
      // Verify it looks like an org code (numeric)
      if (StringUtils.isNumeric(prefix)) {
        return prefix;
      }
    }
    return null;
  }

  /**
   * Execute an HTTP method against the Freshservice API.
   * @param debugMap for logging
   * @param httpMethodName GET, POST, PUT, DELETE
   * @param configId the external system config id
   * @param urlSuffix the URL path after the base endpoint
   * @param allowedReturnCodes set of allowed HTTP return codes
   * @param returnCode array to receive the actual return code
   * @param bodyParam JSON body to send, or null
   * @param page page number for pagination, or null
   * @param addPageSize whether to add per_page parameter
   * @return the parsed JSON response, or null if empty
   */
  private static JsonNode executeMethod(Map<String, Object> debugMap,
      String httpMethodName, String configId, String urlSuffix, Set<Integer> allowedReturnCodes,
      int[] returnCode, String bodyParam, Integer page, boolean addPageSize) {

    GrouperLoaderConfig grouperLoaderConfig = GrouperLoaderConfig.retrieveConfig();
    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();

    grouperHttpClient.assignDoNotLogHeaders(doNotLogHeaders).
        assignDoNotLogParameters(doNotLogParameters);

    WsBearerTokenExternalSystem.
        attachAuthenticationToHttpClient(grouperHttpClient, configId, grouperLoaderConfig, debugMap);

    String url = grouperLoaderConfig.propertyValueStringRequired("grouper.wsBearerToken." + configId + ".endpoint");

    if (url.endsWith("/")) {
      url = url.substring(0, url.length() - 1);
    }
    url += (urlSuffix.startsWith("/") ? "" : "/") + urlSuffix;
    debugMap.put("url", url);

    grouperHttpClient.assignUrl(url);
    grouperHttpClient.assignGrouperHttpMethod(httpMethodName);

    if (StringUtils.isNotBlank(bodyParam)) {
      grouperHttpClient.assignBody(bodyParam);
    }

    if (page != null && page > 0) {
      grouperHttpClient.addUrlParameter("page", Integer.toString(page));
    }

    if (addPageSize) {
      int pageSize = grouperLoaderConfig.propertyValueInt("grouper.wsBearerToken." + configId + ".pageSize", MAX_PAGE_SIZE);
      grouperHttpClient.addUrlParameter("per_page", Integer.toString(pageSize));
    }

    if (httpMethodName.equals("POST") || httpMethodName.equals("PUT")) {
      grouperHttpClient.addHeader("Content-Type", "application/json; charset=utf-8");
    }

    grouperHttpClient.executeRequest();

    int code = -1;
    String json = null;

    try {
      code = grouperHttpClient.getResponseCode();
      returnCode[0] = code;
      json = grouperHttpClient.getResponseBody();
    } catch (Exception e) {
      throw new RuntimeException("Error connecting to '" + debugMap.get("url") + "'", e);
    }

    if (!allowedReturnCodes.contains(code)) {
      throw new RuntimeException(
          "Invalid return code '" + code + "', expecting: " + GrouperUtil.setToString(allowedReturnCodes) +
              ". '" + debugMap.get("url") + "' " + json);
    }

    if (StringUtils.isBlank(json)) {
      return null;
    }

    try {
      JsonNode rootNode = GrouperUtil.jsonJacksonNode(json);
      return rootNode;
    } catch (Exception e) {
      throw new RuntimeException("Error parsing response: '" + json + "'", e);
    }
  }

  /**
   * Retrieve all departments from Freshservice API (paginated).
   * GET /api/v2/departments
   * @param configId the external system config id
   * @return list of department JsonNodes
   */
  private static List<JsonNode> retrieveDepartments(String configId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveDepartments");

    List<JsonNode> results = new ArrayList<JsonNode>();

    try {
      boolean lastPage = false;
      int page = 1;
      GrouperLoaderConfig grouperLoaderConfig = GrouperLoaderConfig.retrieveConfig();

      while (!lastPage) {
        int[] ignoreReturnCode = new int[1];
        ignoreReturnCode[0] = -1;
        JsonNode jsonNode = executeMethod(debugMap, "GET", configId, "api/v2/departments",
            GrouperUtil.toSet(200), ignoreReturnCode, null, page, true);

        ArrayNode departmentsArray = (ArrayNode) jsonNode.get("departments");

        for (int i = 0; i < (departmentsArray == null ? 0 : departmentsArray.size()); i++) {
          results.add(departmentsArray.get(i));
        }

        page++;

        if (departmentsArray == null || departmentsArray.size() < grouperLoaderConfig.propertyValueInt("grouper.wsBearerToken." + configId + ".pageSize", MAX_PAGE_SIZE)) {
          lastPage = true;
        }
      }
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(re));
      throw re;
    }

    return results;
  }

  /**
   * Create a department in Freshservice.
   * POST /api/v2/departments
   * @param configId the external system config id
   * @param name the department name
   * @return the created department JsonNode (contains id, name, etc.)
   */
  private static JsonNode createDepartment(String configId, String name) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "createDepartment");

    try {
      ObjectNode jsonToSend = GrouperUtil.jsonJacksonNode();
      jsonToSend.put("name", name);

      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);

      int[] returnCode = new int[1];
      returnCode[0] = -1;
      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, "api/v2/departments",
          GrouperUtil.toSet(200, 201), returnCode, jsonStringToSend, null, false);

      JsonNode departmentNode = GrouperUtil.jsonJacksonGetNode(jsonNode, "department");
      return departmentNode;

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(re));
      throw re;
    }
  }

  /**
   * Update a department in Freshservice.
   * PUT /api/v2/departments/{id}
   * @param configId the external system config id
   * @param departmentId the Freshservice department ID
   * @param name the new department name
   * @return the updated department JsonNode
   */
  private static JsonNode updateDepartment(String configId, Long departmentId, String name) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "updateDepartment");

    try {
      ObjectNode jsonToSend = GrouperUtil.jsonJacksonNode();
      jsonToSend.put("name", name);

      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);

      int[] returnCode = new int[1];
      returnCode[0] = -1;
      JsonNode jsonNode = executeMethod(debugMap, "PUT", configId, "api/v2/departments/" + String.valueOf(departmentId),
          GrouperUtil.toSet(200), returnCode, jsonStringToSend, null, false);

      JsonNode departmentNode = GrouperUtil.jsonJacksonGetNode(jsonNode, "department");
      return departmentNode;

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(re));
      throw re;
    }
  }

}

// comment out call main for daemon
//Test159bsdFreshServiceDepartmentSync.main(null);

```

### Pagination

All list operations (retrieve all users, groups, memberships) use paginated API calls. The page size is controlled by the `pageSize` property on the external system. The Freshservice API maximum is 100 items per page and defaults to 100 so you shouldn't need to adjust it.

  

## Example configuration

```
provisioner.bsdFreshService.addDisabledFullSyncDaemon = true
provisioner.bsdFreshService.addDisabledIncrementalSyncDaemon = true
provisioner.bsdFreshService.class = edu.internet2.middleware.grouper.app.freshServiceRequester.FreshRequesterProvisioner
provisioner.bsdFreshService.customizeEntityCrud = true
provisioner.bsdFreshService.customizeGroupCrud = true
provisioner.bsdFreshService.customizeMembershipCrud = true
provisioner.bsdFreshService.deleteEntities = true
provisioner.bsdFreshService.deleteEntitiesIfNotExistInGrouper = true
provisioner.bsdFreshService.deleteGroups = true
provisioner.bsdFreshService.deleteGroupsIfNotExistInGrouper = true
provisioner.bsdFreshService.deleteMemberships = true
provisioner.bsdFreshService.deleteMembershipsIfNotExistInGrouper = true
provisioner.bsdFreshService.entity2advanced = true
provisioner.bsdFreshService.entityAttributeValueCache0entityAttribute = id
provisioner.bsdFreshService.entityAttributeValueCache0has = true
provisioner.bsdFreshService.entityAttributeValueCache0source = target
provisioner.bsdFreshService.entityAttributeValueCache0type = entityAttribute
provisioner.bsdFreshService.entityAttributeValueCache1entityAttribute = email
provisioner.bsdFreshService.entityAttributeValueCache1has = true
provisioner.bsdFreshService.entityAttributeValueCache1source = target
provisioner.bsdFreshService.entityAttributeValueCache1type = entityAttribute
provisioner.bsdFreshService.entityAttributeValueCacheHas = true
provisioner.bsdFreshService.entityMatchingAttribute0name = email
provisioner.bsdFreshService.entityMatchingAttribute1name = email
provisioner.bsdFreshService.entityMatchingAttributeCount = 2
provisioner.bsdFreshService.entityResolver.columnNames = penn_id, pennkey, first_name, last_name, name, email, title, department_id, phone_number
provisioner.bsdFreshService.entityResolver.entityAttributesNotInSubjectSource = true
provisioner.bsdFreshService.entityResolver.resolveAttributesWithSQL = true
provisioner.bsdFreshService.entityResolver.selectAllSQLOnFull = false
provisioner.bsdFreshService.entityResolver.sqlConfigId = pennCommunity
provisioner.bsdFreshService.entityResolver.sqlMappingEntityAttribute = subjectId
provisioner.bsdFreshService.entityResolver.sqlMappingType = entityAttribute
provisioner.bsdFreshService.entityResolver.subjectSearchMatchingColumn = penn_id
provisioner.bsdFreshService.entityResolver.subjectSourceIdColumn = pennperson
provisioner.bsdFreshService.entityResolver.tableOrViewName = authz_bsd_freshservice_users_v
provisioner.bsdFreshService.freshserviceExternalSystemConfigId = bsdFreshService
provisioner.bsdFreshService.groupAllowedToView = penn\u003Aevp\u003AbusinessServices\u003Aapps\u003AfreshService\u003Asecurity\u003AfreshServiceProvisioningReaders
provisioner.bsdFreshService.groupAttributeValueCache0groupAttribute = id
provisioner.bsdFreshService.groupAttributeValueCache0has = true
provisioner.bsdFreshService.groupAttributeValueCache0source = target
provisioner.bsdFreshService.groupAttributeValueCache0type = groupAttribute
provisioner.bsdFreshService.groupAttributeValueCache1groupAttribute = name
provisioner.bsdFreshService.groupAttributeValueCache1has = true
provisioner.bsdFreshService.groupAttributeValueCache1source = target
provisioner.bsdFreshService.groupAttributeValueCache1type = groupAttribute
provisioner.bsdFreshService.groupAttributeValueCacheHas = true
provisioner.bsdFreshService.groupIdOfUsersToProvision = penn\u003Aevp\u003AbusinessServices\u003Aapps\u003AfreshService\u003Aservice\u003Apolicy\u003AfreshServiceRequesters
provisioner.bsdFreshService.groupMatchingAttribute0name = id
provisioner.bsdFreshService.groupMatchingAttribute1name = name
provisioner.bsdFreshService.groupMatchingAttributeCount = 2
provisioner.bsdFreshService.hasTargetEntityLink = true
provisioner.bsdFreshService.hasTargetGroupLink = true
provisioner.bsdFreshService.logAllObjectsVerbose = true
provisioner.bsdFreshService.logAllObjectsVerboseToLogFile = false
provisioner.bsdFreshService.logCommandsOnError = true
provisioner.bsdFreshService.logCompareCalculations = true
provisioner.bsdFreshService.makeChangesToEntities = true
provisioner.bsdFreshService.numberOfEntityAttributes = 9
provisioner.bsdFreshService.numberOfGroupAttributes = 3
provisioner.bsdFreshService.operateOnGrouperEntities = true
provisioner.bsdFreshService.operateOnGrouperGroups = true
provisioner.bsdFreshService.operateOnGrouperMemberships = true
provisioner.bsdFreshService.provisioningType = membershipObjects
provisioner.bsdFreshService.removeAccentedChars = true
provisioner.bsdFreshService.selectAllEntities = true
provisioner.bsdFreshService.showAdvanced = true
provisioner.bsdFreshService.showAssigningProvisioning = true
provisioner.bsdFreshService.startWith = this is start with read only
provisioner.bsdFreshService.subjectSourcesToProvision = pennperson
provisioner.bsdFreshService.targetEntityAttribute.0.name = id
provisioner.bsdFreshService.targetEntityAttribute.1.name = email
provisioner.bsdFreshService.targetEntityAttribute.1.showAdvancedAttribute = true
provisioner.bsdFreshService.targetEntityAttribute.1.showAttributeValidation = true
provisioner.bsdFreshService.targetEntityAttribute.1.translateExpressionType = grouperProvisioningEntityField
provisioner.bsdFreshService.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField = subjectIdentifier1
provisioner.bsdFreshService.targetEntityAttribute.1.unprovisionableIfNull = true
provisioner.bsdFreshService.targetEntityAttribute.2.name = firstName
provisioner.bsdFreshService.targetEntityAttribute.2.translateExpression = \u0024{grouperProvisioningEntity.retrieveAttributeValueString('entityAttributeResolverSql__first_name')}
provisioner.bsdFreshService.targetEntityAttribute.2.translateExpressionType = translationScript
provisioner.bsdFreshService.targetEntityAttribute.3.name = lastName
provisioner.bsdFreshService.targetEntityAttribute.3.translateExpression = \u0024{grouperProvisioningEntity.retrieveAttributeValueString('entityAttributeResolverSql__last_name')}
provisioner.bsdFreshService.targetEntityAttribute.3.translateExpressionType = translationScript
provisioner.bsdFreshService.targetEntityAttribute.4.name = jobTitle
provisioner.bsdFreshService.targetEntityAttribute.4.translateExpression = \u0024{grouperProvisioningEntity.retrieveAttributeValueString('entityAttributeResolverSql__title')}
provisioner.bsdFreshService.targetEntityAttribute.4.translateExpressionType = translationScript
provisioner.bsdFreshService.targetEntityAttribute.5.name = workPhoneNumber
provisioner.bsdFreshService.targetEntityAttribute.5.translateExpression = \u0024{grouperProvisioningEntity.retrieveAttributeValueString('entityAttributeResolverSql__phone_number')}
provisioner.bsdFreshService.targetEntityAttribute.5.translateExpressionType = translationScript
provisioner.bsdFreshService.targetEntityAttribute.6.name.elConfig = \u0024{'customField_pennkey'}
provisioner.bsdFreshService.targetEntityAttribute.6.translateExpressionType = grouperProvisioningEntityField
provisioner.bsdFreshService.targetEntityAttribute.6.translateFromGrouperProvisioningEntityField = subjectIdentifier0
provisioner.bsdFreshService.targetEntityAttribute.7.name.elConfig = \u0024{'customField_penn_id'}
provisioner.bsdFreshService.targetEntityAttribute.7.translateExpressionType = grouperProvisioningEntityField
provisioner.bsdFreshService.targetEntityAttribute.7.translateFromGrouperProvisioningEntityField = subjectId
provisioner.bsdFreshService.targetEntityAttribute.8.name = departmentId
provisioner.bsdFreshService.targetEntityAttribute.8.translateExpression = \u0024{grouperProvisioningEntity.retrieveAttributeValueString('entityAttributeResolverSql__department_id')}
provisioner.bsdFreshService.targetEntityAttribute.8.translateExpressionType = translationScript
provisioner.bsdFreshService.targetGroupAttribute.0.name = id
provisioner.bsdFreshService.targetGroupAttribute.1.name = name
provisioner.bsdFreshService.targetGroupAttribute.1.translateExpressionType = grouperProvisioningGroupField
provisioner.bsdFreshService.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField = displayExtension
provisioner.bsdFreshService.targetGroupAttribute.2.name = description
provisioner.bsdFreshService.targetGroupAttribute.2.translateExpressionType = grouperProvisioningGroupField
provisioner.bsdFreshService.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField = description

```

## Freshservice API Endpoints Used

[See developer notes for APIs used](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792517/Grouper+Freshservice+provisioner+developer+notes)
