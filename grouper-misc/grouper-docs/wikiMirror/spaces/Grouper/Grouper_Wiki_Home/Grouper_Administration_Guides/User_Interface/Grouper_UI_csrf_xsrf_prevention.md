---
title: "Grouper UI csrf xsrf prevention"
space: Grouper
pageId: 28545475
version: 31
lastUpdated: 2026-07-01T05:47:11.057Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545475/Grouper+UI+csrf+xsrf+prevention
---

This is in Grouper 2.2 UI. btw, Ive heard this does not work with IE8.

## Logging

Set this in log4j.properties for enhanced logging

```
log4j.logger.edu.internet2.middleware.grouper.grouperUi.csrf.CsrfGuardLogger  = DEBUG
```

## Notes

If you are using nginx make sure to set this setting

```
underscores_in_headers on;
```

## Configure more URLs to protect

If you have logic running (other than what is built in to Grouper) in the Grouper UI application (this is not common), you can add CSRF protection in the grouper-ui.properties

```
# If you have logic running from the Grouper webapp, put some comma-separated URL patterns, starting with slash
# and after the context, e.g. /grouperExternal/public/UiV2Public.index
# ${valueType: "string"}
csrfguard.extraFilterPatterns = 
```

## Troubleshooting

If a request is made that is protected and the CSRF token is not present, an error will occur

You can see a log in the UI of what the request was:

```
2021-05-16 10:34:09,309: [http-nio-8400-exec-10] ERROR CsrfGuardLogger.log(47) -  - potential cross-site request forgery (CSRF) attack thwarted (user:GrouperSystem, ip:0:0:0:0:0:0:0:1, method:GET, uri:/grouper/grouperUi/app/UiV2Main.gif, error:required token is missing from the request)

```

You can unprotect URLs from CSRF in the Owasp.CsrfGuard.overlay.properties file (append to this file in a container script hook)

```
org.owasp.csrfguard.unprotected.<someUniqueKey>=%servletContext%/someUrl/whatever
```
