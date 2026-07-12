---
title: "Web Services OpenAPI Swagger"
space: Grouper
pageId: 28549495
version: 15
lastUpdated: 2026-07-01T05:41:54.292Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549495/Web+Services+OpenAPI+Swagger
---

#### 

There is an OpenAPI/Swagger representation of the Grouper Web Service API.

We are using Swagger 1.5.3 (which is OAS 2).

## Access OpenAPI

There is a [demo server static version](https://grouperdemo.internet2.edu/swagger/v4/). The swagger JSON is here:

[https://grouperdemo.internet2.edu/grouper_v4/docs/index.json](https://grouperdemo.internet2.edu/grouper_v4/docs/index.json)

Each WS deployment has its own Swagger page, put "docs" path after the context after the domain, e.g. [http://localhost:8400/grouper-ws/docs](http://localhost:8400/grouper-ws/docs)

## Customize your WS Swagger page

We will have some container params to customize the Swagger JSON file. e.g.

| Param | Sample value | Description |
| --- | --- | --- |
| GROUPER_OPENAPI_TITLE | My institution's group service web services | Title at top of swagger page |
| GROUPER_OPENAPI_BASEURL | https://server.institution.edu | Base URL for WS |
| GROUPER_OPENAPI_DESCRIPTION_SUFFIX | Contact OIT to get a credential for the WS. Pass the user/pass in the HTTP basic auth header. | More info in the description of the page |
