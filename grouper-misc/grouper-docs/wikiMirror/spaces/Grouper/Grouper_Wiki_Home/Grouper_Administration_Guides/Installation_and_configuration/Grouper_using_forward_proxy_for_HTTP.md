---
title: "Grouper using forward proxy for HTTP"
space: Grouper
pageId: 28544676
version: 9
lastUpdated: 2021-11-29T15:37:19.650Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544676/Grouper+using+forward+proxy+for+HTTP
---

This document shows how to configure Grouper to use a forward proxy to make HTTP/FTP calls. This document is written for Grouper version v2.6.5+

Different parts of Grouper use HTTP different ways, so this assumes that the part of Grouper you are using is coded with the GrouperHttpClient class. As of v2.6.5+ all the HTTP traffic implemented in the grouper.jar (main java API) uses GrouperHttpClient.

There are three ways to configure proxies in Grouper

1. GrouperHttpClient uses Apache commons HTTP client and it is intended to accept JVM variables to configure the proxy globally. [https://docs.oracle.com/javase/6/docs/technotes/guides/net/proxies.html](https://docs.oracle.com/javase/6/docs/technotes/guides/net/proxies.html)  
  Note: if you are setting a global proxy then you shouldn't set a grouper.properties proxy or a configuration specific proxy
  
  
  ```
  -Dhttps.proxyHost=proxy.example.com
  -Dhttps.proxyPort=1234
  -Dhttp.proxyHost=proxy.example.com
  -Dhttp.proxyPort=1234
  -Dhttp.nonProxyHosts=localhost|127.0.0.1|*.school.edu
  
  and if you need authentication, you can also set these:
  
  -Dhttps.nonProxyHosts
  -Dhttps.proxyUser
  -Dhttps.proxyPassword
  -Dhttp.nonProxyHosts
  -Dhttp.proxyUser
  -Dhttp.proxyPassword
  
  you can set this via container env var (e.g. for a simple config):
  
  GROUPER_EXTRA_CATALINA_OPTS='-Dhttps.proxyHost=1.2.3.4 -Dhttps.proxyPort=1234 -Dhttp.nonProxyHosts=localhost|127.0.0.1|*.school.edu'
  
  
  ```
2. grouper.properties has proxy configuration for regexes of URLs
  
  
  ```
  ##################################
  ## Proxy config
  ##################################
  
  # proxy requests here, e.g. https://server:1234
  # {valueType: "string"}
  grouper.http.proxy.url =
  
  # socks or http
  # {valueType: "string", formElement: "dropdown", optionValues: ["PROXY_HTTP", "PROXY_SOCKS5"]}
  grouper.http.proxy.type = 
  
  # if this is blank then all urls are included by default.  If there is a regex here, then only include urls that match, e.g. ^abc$
  # {valueType: "string"}
  grouper.http.proxy.includeUrlRegexPattern = 
  
  
  # if this is blank then excludes are not considered by default.  If there is a regex here, then only exclude urls that match, e.g. ^abc$
  # {valueType: "string"}
  grouper.http.proxy.excludeUrlRegexPattern =
  
  
  ```
3. The specific function you are working with has configuration also (e.g. each external system)

## Setting up a test proxy

```
docker run --name squid -d --restart=always --publish 3128:3128 sameersbn/squid:3.5.27-2
docker exec -it squid bash
sudo apt-get update
apt-get install vim
vim /etc/squid/squid.conf

// open up access for a workstation (assume firewalled off)
acl all src 0.0.0.0/0
http_access allow all

docker restart squid

tail -f /var/log/squid/access.log
```

This will then work

```
    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
    grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.get);
    grouperHttpClient.assignUrl("https://grouperdemo.internet2.edu");
    
    grouperHttpClient.assignProxyType(GrouperProxyType.PROXY_HTTP);    
    grouperHttpClient.assignProxyUrl("http://1.2.3.4:3128");  // IP address of docker
    
    grouperHttpClient.executeRequest();
    System.out.println(grouperHttpClient.getResponseCode() + ", " + grouperHttpClient.getResponseBody());

```
