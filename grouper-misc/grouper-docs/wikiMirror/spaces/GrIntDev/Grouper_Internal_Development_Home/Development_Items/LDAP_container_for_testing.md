---
title: "LDAP container for testing"
space: GrIntDev
pageId: 48792780
version: 7
lastUpdated: 2026-07-12T07:01:16.322Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792780/LDAP+container+for+testing
---

The following allows you to build and run an LDAP container (openldap) with data that's used for testing. Note that test cases will build the image and start the container so you don't need to do this. You just need to have docker installed and running as a user that can manage containers.

Go to directory:

```
cd grouper-misc/openldap-dinkel-grouper
```

Building:

```
Windows:
.\build.bat

Unix:
./build.sh
```

Running:

```
Windows: 
.\run.bat

Unix:
./run.sh
```

LDAP tests have methods in LdapProvisionerTestUtils that will remove and recreate containers for each test. It currently expects to find docker in /usr/bin, /usr/local/bin, or /bin. For Windows, it'll expect "docker" to be in the path as seen by Java.
