---
title: "Application performance monitoring"
space: Grouper
pageId: 28544682
version: 2
lastUpdated: 2026-07-01T05:48:13.628Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544682/Application+performance+monitoring
---

For our Grouper implementation, we started with the basics of monitoring Grouper:

- We configured daily Grouper reports (withou grouper-loader.properties set "daily.report.emailTo")
- Via Nagios, we monitor the diagnostic page: [https://YourGrouperEnvironment/grouper/status?diagnosticType=all](https://YourGrouperEnvironment/grouper/status?diagnosticType=all)

But, we found that we wanted to have more information available to us to ensure our Grouper implementation wasn't encountering any issues.

So we added

- [Smart Detection monitoring](https://docs.microsoft.com/en-us/azure/azure-monitor/app/proactive-diagnostics) within Azure Application Insights
- [Availability ping test](https://docs.microsoft.com/en-us/azure/azure-monitor/app/monitor-web-app-availability); this was sort of required for charts within Azure Application Insights to populate

## Smart Detection monitoring

The smart detection is interesting as the alerts are specific to your environment as the Azure service uses machine learning to determine, over time, what is normal within your environment and alerts you accordingly. There are a couple different smart detectors, we ended up enabling them all. Here is an example of an abnormal pattern detected that resulted in a notification for us to investigate.

We can drilled into the actual log messages to examine more details via Azure log analytics.

We can then drill into the log message to further investigate.

Not in the shown example above, but we did have an error that Smart Detection found resulting in us a cache setting to resolve.

Application Insights also has some neat features that allows you to examine your network communications, and drill into any failures/ delays.

## Availability ping test

Also within Application Insights, you can monitor the availability of your application via a simple ping test or more complicated synthetic transaction; we went with the simple ping test. Interestingly, if you don't create a ping test within Application Insights, the reports related to availability show no availability. We also added alerting to our availability test such that if no response is received, after X times we will get a notification to investigate.
