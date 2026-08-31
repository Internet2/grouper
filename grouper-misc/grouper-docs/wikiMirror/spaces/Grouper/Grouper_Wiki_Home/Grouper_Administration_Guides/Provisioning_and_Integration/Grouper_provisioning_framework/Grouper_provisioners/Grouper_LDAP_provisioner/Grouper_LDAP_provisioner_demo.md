---
title: "Grouper LDAP provisioner demo"
space: Grouper
pageId: 28560080
version: 11
lastUpdated: 2026-07-01T05:36:24.664Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560080/Grouper+LDAP+provisioner+demo
---

> The info on this page applies to Grouper 2.6 and above.

[Video of provisioning](https://youtu.be/Rsnl5_sPzWU)

There is an LDAP container that can be used.

Download and run on unix, mac, or windows (in WSL windows subsystem for linux). You need docker installed

```
$ wget https://raw.githubusercontent.com/Internet2/grouper/GROUPER_2_5_BRANCH/grouper-misc/openldap-dinkel-grouper/downloadAndRunLdap.sh
$ chmod +x downloadAndRunLdap.sh
$ ./downloadAndRunLdap.sh

##### cleanup: 
$ docker rm -f openldap-dinkel-grouper
$ docker rmi openldap-dinkel-grouper:latest

```

Make an LDAP external system named: personLdap:

```
configId: personLdap
url: ldap://localhost:389
user: cn=admin,dc=example,dc=edu
pass: secret
```

Import the subject.properties that was downloaded where you installed the LDAP container
