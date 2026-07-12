---
title: "Web Services FAQ"
space: Grouper
pageId: 28548593
version: 22
lastUpdated: 2026-07-12T15:26:56.253Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548593/Web+Services+FAQ
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

- Can I run Grouper web services against any version of the Grouper DB?

> No, you should use the version of Grouper bundled in the web services. This makes it a little complicated if you have a UI / [GSH](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545249/GrouperShell+gsh) / etc running against your Grouper DB. You will need to keep them in sync.

- Why are there three flavors of web services (SOAP, XML-HTTP, REST-Lite)?

> SOAP is supported since many schools required it. REST-Lite is supported because many schools required it. XML-HTTP is supported because Axis2 gives it for free when building a SOAP web service. This way the same SOAP interface can be accessed by clients who only want to talk HTTP and XML and not SOAP.

- Why is the rampart .aar file named GrouperServiceWssec.aar, but the URL is still /services/GrouperService?

> The URL for ramprt or not is the same. Inside the services.xml in the .aar files, it configures the app name. grouper-ws will not work if you change this (unless to do other build activities also)

- Why is there a "simple" operation for every non simple operation in SOAP and XML-HTTP?

> The non-simple operations are batchable if the client wants to do one operation multiple times with one request. The simple operations are for if the client only needs to do one thing (e.g. assign a member to a group and not assign multiple members to a group). Also, there is a valuable side effect that for the XML-HTTP, if the operation only has scaler input params (and not complex types or arrays), that the input does not need XML, it can be in the query string for GET or in the http form param pairs in the body for POST.
> 
> It is confusing that there are so many different ways to call grouper via web service. The documentation will be improved to make it easier to find the best strategy.

- Why element named "return" (by axis)

> This is an unfortunate "feature" by axis, the default element is named "return" which is a keyword in many programming languages, so if the language automatically converts the xml to an object graph, then it will be broken. Chris will followup with Axis to see if there is a fix for this

- Why returning error codes and messages and not just use SOAP faults?

> For two of three of the flavors of web services SOAP faults are not an option since they are not SOAP. Also, for SOAP batched, the status of each line item needs to be returned for the client to process, and a SOAP fault would preclude that. Also, the fewer SOAP specific features that are used, the more widespread the compatibility will be. All three flavors of web service use the same underlying logic, so the more consistent the better.

- Is there a way to find out about the health of my Grouper web services?

> Yes, use the [Grouper Diagnostics](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548954/Grouper+health+check+endpoint+healthcheck+status+diagnostics)

- Can we add a service for subject search?

> Yes, see [subject search](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548291/Get+Subjects)

- How does always available web service work?

> Grouper WS can be setup with multiple instances. If you have database replication (even readonly), then you can setup Grouper WS application servers in multiple data centers.
