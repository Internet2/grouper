---
title: "SCIM server rewrite to not depend on J2EE"
space: GrIntDev
pageId: 48792648
version: 3
lastUpdated: 2026-07-12T07:01:10.173Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792648/SCIM+server+rewrite+to+not+depend+on+J2EE
---

## Incorporation into Grouper web services

The initial implementation of SCIM 2 services, using the pingidentity library, has been added to Grouper web services as of 2.6.17. The endpoint under grouper-ws base URI will be /scim/v2/{endpoint}; e.g. [http://localhost:8080/grouper-ws/scim/v2/Groups/systemName:etc:sysadmingroup](http://localhost:8080/grouper-ws/scim/v2/Groups/systemName:etc:sysadmingroup). This is currently disabled by default, but can be enabled using the same hibernate property `grouper.is.scim` or environment variable GROUPER_SCIM that enables the Penn State J2EE SCIM service. In other words, either both services are enabled, or both disabled. Unlike the Penn State service which only supported Basic and external auth, the SCIM under Grouper WS uses the same auth as Grouper WS, which has more options.

The functionality matches as closely as possible the existing SCIM 2 implementation using the Penn State library, in terms of what it uses for ids/names/display names and the searches and filters it can do. It continues to support custom group id endpoints using "/Groups/systemName:{group name}" and "/Groups/idIndex:{id index}". It does differ in json objects returned, mainly by being closer to the standards of the [core schema](https://www.rfc-editor.org/rfc/rfc7643.html) (RFC 7643).

Key ways the format differs in the WS implementation, versus the PSU servlet:

- No longer sending non-standard attribute "baseUrn"
- No longer sending non-standard attribute "resourceType" (this should be meta.resourceType instead)
- No longer sending TIER extensions "schemas[]" attribute that was replicating its own key
- No longer wrapping TIER extensions in a non-standard "extensions" object; these are now at the top level.
- Reference objects key is "$ref" and not "ref"
- In list responses, renamed "resources" to standard "Resources"
- /Memberships removed owner.systemName which is not standard

Other functionality has been fixed or improved:

- Groups newly sending group name in standard core field "externalId"
- Groups newly sending members[].display which is the subject's name
- Groups now able to set description via the TierGroupExtension extension
- Add/replace memberships via /Groups POST/PUT working
- For PUT /Group, description is being set correctly and not cleared out
- For /Users. extension schema "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User" included by default. Field "employeeNumber" set to subjectId
- User object returning a list of group memberships
- PUT /Membership object able to set enabled and disabled dates without JSON conversion errors

This is the initial implementation, and there are still some improvements to make. Batch and Patch support should be added in order to make the service more useful. The /Schema endpoint is not implemented yet, which is needed for references to the TIER object extensions to external clients. Group and Membership objects are not including created and modified times, as the pingidentity library returns these as Long integers, not DateTime format as required by the SCIM RFC.

## Analysis done September 2022

1) Status as of 2.6.15 - PSU SCIM library, AKA "SCIMple-Identity"   
  
[https://github.com/PennState/SCIMple-Identity](https://github.com/PennState/SCIMple-Identity)   
  
Grouper is using v2.22 released April 2018. The latest version is v2.22.4 from October 2018.   
  
The grouper-ws-scim module has a runtime dependency on J2EE. This is mainly from the ProviderRegistry class, which is a @startup @singleton, and also dependency injection of the provider classes that get stored in this registry. There was little gained by writing it this way. However it's embedded in library classes, so not easy to work around without forking the project.   
  
There are a number of ways the output differs from the official RFC 7643 spec. The User resource not including userName which is required by spec. Also has non-standard output: baseUrn, ResourceTypes endpoint returns ListResponse scim resource, not a json array.   
  
A number of endpoint methods are not supported. PATCH is not supported, Adding members via a Group POST or PUT is quietly ignored. Filters only support the EQ operator. Some operations -- Bulk updates, PUT requests with extensions -- fail due to "class edu.psu.swe.scim.spec.resources.ScimResource not instantiable". Endpoint /Groups/.search fails due to "Missing a Converter for type class edu.psu.swe.scim.spec.protocol.search.Filter". Endpoint for /Schemas fails due to an infinite loop in serialization. At least some of these issues were fixed in versions newer than the v2.22 used in Grouper.   
  
This project moved to the Apache Directory project in July 2018.   
  
2) Alternative open source SCIM libraries   
  
2a) wso2 charon3   
  
[https://github.com/wso2/charon](https://github.com/wso2/charon)   
  
There was no documentation on implementation, so it needs to be reverse engineered to get working. Although the project advertises a pluggable architecture for adding schema extensions, I was never successful in getting this working, except by creating classes for every attribute. I was not able to understand the connection between schemas and attributes that are part of those schemas. I ended up with a basic group GET after a number of hours, with an amount of code that approached a REST application without their library.   
  
Since this needed to work in Tomcat without J2EE, I used Jersey 2.36 for JAX-RS support.   
  
2b) Ping identity   
  
[https://github.com/pingidentity/scim2](https://github.com/pingidentity/scim2)   
  
While this is also undocumented, I was more successful in getting this working. There is a clearer interface for how to add object extensions; they are POJO subclasses of BaseScimResource that get added to the main object, and get serialized in a reasonable way. There is no base class implementing the endpoints, but it was straightforward to the REST methods.   
  
This library seemed to match closely with the SCIM standard. The user object fetch returned a lot of empty fields for addresses, emails, etc. that aren't required to be returned, but this is more harmless noise.   
  
I also used Jersey 2.36 for JAX-RS support.   
  
2c) [https://github.com/imulab/kumiq-identity](https://github.com/imulab/kumiq-identity)   
  
Did not try. Has not been updated in 7 years, and latest tag is 0.1.0   
  
2d) Rewrite from scratch as plain REST   
  
If all the open source options were unworkable, we could just write this as a regular REST application, accepting input and formatting the output to conform with SCIM2 specs.   
  
  
3) Next steps   
  
To rewrite the Grouper WS-SCIM service to eliminate the need for J2EE and TomEE, I recommend the Ping identity library as the cleanest solution.   
  
The Ping identity framework adheres more closely to the SCIM2 spec in RFC 7643 and 7644 than the current PSU library does. We should identify who the current users of WS-SCIM are, and work with them to understand the impact of changes in data formats.   
  
Other recommendations:   
  
- Current user search is id OR identifier and not source-specific. Is there a danger of failure due to multiple results?   
- Implement bulk operations   
- Implement PATCH so that group members can be updated from the Group object   
- User object to return sourceId in userType   
- Use group members to sync memberships? Or continue to use the Membership endpoints to manage?
