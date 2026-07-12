---
title: "Grouper Web Services Versioning"
space: Grouper
pageId: 28548558
version: 20
lastUpdated: 2026-07-01T05:44:11.505Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548558/Grouper+Web+Services+Versioning
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

Grouper 2.0 web service servers will accept clients coded against the Grouper 1.6 or previous WS API's. The 2.0 protocol is not exactly compatible with the 1.6 protocol, so both protocols run in the Grouper 2.0 WS server.

Grouper RESTlike is automatic marshaling to/from XML/JSON based on Javabean, but the marshaling will not marshal null data (this is good). The Grouper WS client passes in its version, and if it is less than 2.0, the new fields will be null, and the response is backwards compatible (does not contain the field).

Grouper SOAP uses auto-marshaling from Axis2 which does not support conditional fields (if null, it send back the field and says it is null). So there is a new source path for 2.0, WSDL, and a new endpoint, aar, build script entry, etc. The [1.6 WSDL](https://raw.githubusercontent.com/Internet2/grouper/GROUPER_1_6_BRANCH/grouper-ws/grouper-ws-java-generated-client/GrouperService.wsdl) is on the 2.0 server under the same name, the new one is:[GrouperService_v2_0.wsdl](https://raw.githubusercontent.com/Internet2/grouper/GROUPER_2_0_BRANCH/grouper-ws/grouper-ws-java-generated-client/GrouperService_v2_0.wsdl). the new WSDL does have a new target namespace too:

```
<wsdl:definitions xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/"
xmlns:axis2="http://soap_v2_0.ws.grouper.middleware.internet2.edu"
xmlns:ns1="http://org.apache.axis2/xsd"
xmlns:ns="http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd"
xmlns:wsaw="http://www.w3.org/2006/05/addressing/wsdl"
xmlns:http="http://schemas.xmlsoap.org/wsdl/http/"
xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:mime="http://schemas.xmlsoap.org/wsdl/mime/"
xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/"
xmlns:soap12="http://schemas.xmlsoap.org/wsdl/soap12/"
targetNamespace="http://soap_v2_0.ws.grouper.middleware.internet2.edu">

```

The 1.6 Javabeans are in a new source folder in 2.0, and there are facilities to auto-transform the 1.6 beans into 2.0 beans and vice versa.

Bottom-line, the SOAP and RESTlike web services are completely backward compatible, and for SOAP, if you are coding against 2.0 (or upgrading), change the endpoint URL from GrouperService to GrouperService_v2_0. Note, it will be 2.0 until there is a change that is not backwards compatible... that might be 2.1, or 2.2, or whatever.

| Grouper client version | v1.4, v1.5, v1.6, v2.0 (server version) Endpoint | WSDL from server | WSDL from Git |
| --- | --- | --- | --- |
| 1.4 | [https://server.address/grouperWs/services/GrouperService](https://server.address/grouperWs/services/GrouperService) | [https://server.address/grouperWs/services/GrouperService?wsdl](https://server.address/grouperWs/services/GrouperService?wsdl) | [1.4 WSDL](https://raw.githubusercontent.com/Internet2/grouper/GROUPER_1_4_BRANCH/grouper-ws/grouper-ws-java-generated-client/GrouperService.wsdl) |
| 1.5 | [https://server.address/grouperWs/services/GrouperService](https://server.address/grouperWs/services/GrouperService) | [https://server.address/grouperWs/services/GrouperService?wsdl](https://server.address/grouperWs/services/GrouperService?wsdl) | [1.5 WSDL](https://raw.githubusercontent.com/Internet2/grouper/GROUPER_1_5_BRANCH/grouper-ws/grouper-ws-java-generated-client/GrouperService.wsdl) |
| 1.6 | [https://server.address/grouperWs/services/GrouperService](https://server.address/grouperWs/services/GrouperService) | [https://server.address/grouperWs/services/GrouperService?wsdl](https://server.address/grouperWs/services/GrouperService?wsdl) | [1.6 WSDL](https://raw.githubusercontent.com/Internet2/grouper/GROUPER_1_6_BRANCH/grouper-ws/grouper-ws-java-generated-client/GrouperService.wsdl), [1.6 WSDL in v2.0](https://raw.githubusercontent.com/Internet2/grouper/GROUPER_2_0_BRANCH/grouper-ws/grouper-ws-java-generated-client/GrouperService.wsdl) |
| 2.0 | [https://server.address/grouperWs/services/GrouperService_v2_0](https://server.address/grouperWs/services/GrouperService_v2_0) | [https://server.address/grouperWs/services/GrouperService_v2_0?wsdl](https://server.address/grouperWs/services/GrouperService_v2_0?wsdl) | [2.0 WSDL](https://raw.githubusercontent.com/Internet2/grouper/GROUPER_2_0_BRANCH/grouper-ws/grouper-ws-java-generated-client/GrouperService_v2_0.wsdl) |
| 2.1 | [https://server.address/grouperWs/services/GrouperService_v2_](https://server.address/grouperWs/services/GrouperService_v2_0)1 | [https://server.address/grouperWs/services/GrouperService_v2_1?wsdl](https://server.address/grouperWs/services/GrouperService_v2_0?wsdl) |  |
| 2.2 | [https://server.address/grouperWs/services/GrouperService_v2_](https://server.address/grouperWs/services/GrouperService_v2_0)2 | [https://server.address/grouperWs/services/GrouperService_v2_2?wsdl](https://server.address/grouperWs/services/GrouperService_v2_0?wsdl) |  |
| 2.3 | [https://server.address/grouperWs/services/GrouperService_v2_](https://server.address/grouperWs/services/GrouperService_v2_0)3 | [https://server.address/grouperWs/services/GrouperService_v2_3?wsdl](https://server.address/grouperWs/services/GrouperService_v2_0?wsdl) |  |

Note, if your servlet is not grouperWs (e.g. grouper-ws) then adjust accordingly.

#### Developer information

Note, this means developer as in developers who work on Grouper itself, not developers who are writing web service clients or implementing Grouper.

If you are changing a transfer object, or editing the signatures in coresoap.GrouperService, then you also need to edit the corresponding GrouperService that you want it to be available for. Typically we dont edit the historical backwards compatible versions, you would only add it to the latest version, e.g. soap_v2_0.GrouperService, or the soap_v2_0.WhateverTransferObject
