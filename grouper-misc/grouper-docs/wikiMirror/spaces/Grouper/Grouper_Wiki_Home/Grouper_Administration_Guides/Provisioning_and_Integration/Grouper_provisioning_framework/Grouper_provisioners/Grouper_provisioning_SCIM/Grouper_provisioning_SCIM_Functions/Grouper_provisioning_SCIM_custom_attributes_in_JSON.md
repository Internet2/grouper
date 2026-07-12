---
title: "Grouper provisioning SCIM custom attributes in JSON"
space: Grouper
pageId: 28564290
version: 5
lastUpdated: 2026-07-01T05:35:22.417Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28564290/Grouper+provisioning+SCIM+custom+attributes+in+JSON
---

For users and groups you can put in endpoint specific custom JSON attributes.

## Full JSON of group (could be user too)

Note the service now group attribute, that description is the custom part (this is not a real service now attribute, this is just an example)

```
{
   "id":"018d51b6485b4fa7a7972198a8d7b279",
   "meta":{
      "created":"2024-06-19T18:52:50Z",
      "lastModified":"2024-06-19T18:52:50Z"
   },
   "displayName":"testGroupWithDescription",
   "active":true,
   "urn:ietf:params:scim:schemas:extension:servicenow:2.0:Group":{
      "description":"test description"
   }
}
```

## Configure an attribute

Configure an attribute with a different name than the built in ones

Use expression language since the attribute name drop down has the built in attributes.

You should probably use a unique name that will never be a SCIM attribute in the future.

The value is EL for a label: e.g. ${'custom_description'}

## Map this attribute to JSON

Use a simple [JSON pointer](https://www.baeldung.com/json-pointer)

This is an example of drilling down in an object and a field of that object:

```
/urn:ietf:params:scim:schemas:extension:servicenow:2.0:Group/description
```
