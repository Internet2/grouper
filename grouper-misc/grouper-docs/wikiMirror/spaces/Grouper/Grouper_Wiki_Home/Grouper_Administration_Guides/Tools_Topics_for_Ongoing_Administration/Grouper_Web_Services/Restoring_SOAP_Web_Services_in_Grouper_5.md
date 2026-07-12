---
title: "Restoring SOAP Web Services in Grouper 5+"
space: Grouper
pageId: 28549083
version: 10
lastUpdated: 2026-07-01T05:42:51.201Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549083/Restoring+SOAP+Web+Services+in+Grouper+5
---

SOAP web services were removed in Grouper 5.0.0+. If you still need the functionality, you can add it back using the instructions below. However note that this is not recommended especially since the dependencies that are added back have known vulnerabilities. And while this should work for the initial Grouper v5 release, it will likely stop working at some point. Also, be sure to test especially if you're using Rampart authentication.

1. Download [grouper-legacy-soap.tar.gz](https://software.internet2.edu/grouper/release/legacy/grouper-legacy-soap.tar.gz)
2. Enable SOAP using one of the following methods.
  
  1. In grouper.hibernate.properties, set grouper.is.ws.soap = true
  2. Or set env var GROUPER_WS_SOAP='true'
3. Add the files from the extracted tarball into the container in /opt/grouper/grouperWebapp/WEB-INF.
  
  1. If building the files into the image, you can add them directly into WEB-INF
    
    1. with a COPY command (`COPY path/to/extracted/files /opt/grouper/grouperWebapp/WEB-INF`)
    2. or a RUN command (`RUN cd /opt/grouper/grouperWebapp/WEB-INF && tar xzf /path/to/grouper-legacy-soap.tar.gz`)
  2. If mounting at runtime, mount the extracted directory to /opt/grouper/slashRoot/opt/grouper/grouperWebapp/WEB-INF, and it will copy the files to the final location at runtime
4. (v5.18.0+) Also download [commons-httpclient-3.1.jar](https://repo1.maven.org/maven2/commons-httpclient/commons-httpclient/3.1/) and add to the grouperWebapp/WEB-INF/libWs directory

## Upgrading libraries for security

If there is a concern about security from the jar files added in, some vulnerabilities can be mitigated by upgrading or removing libraries without breaking web services:

- commons-fileupload: A more recent version is in Grouper, so this doesn't need to be added
- axis2-*: These can be upgraded to version 1.7.9. The current version 1.8.2 is not compatible with commons-httpclient 3.1
- apache-mime4j-core: This can be upgraded to 0.8.13
- WSS4J doesn't seem to be needed if you are doing local authentication. If so, you can remove: wss4j-1.6.19, xmlsec-1.5.8, opensaml-2.6.4, openws-1.5.4, xmltooling-1.4.4
- XmlSchema 1.4.7 is not compatible with the upgraded axis2; this can be upgraded to xmlschema-core 2.3.1
- After upgrading axis2, commons-httpclient 3.1 is no longer needed and can be removed

The tarball contains the same libraries in both lib/ and libWs/ which isn't necessary. You only need the libWs/ jars because they are only needed for WS containers. You can remove the lib/ versions when you build your custom image.

A tarball usable in v5.18.0+, which include commons-http-client 3.1 and updated libraries can be downloaded at [grouper-legacy-soap-5.18.0.tar.gz](https://software.internet2.edu/grouper/release/legacy/grouper-legacy-soap-5.18.0.tar.gz).
