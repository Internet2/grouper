---
title: "Grouper client script from Excel CSV"
space: Grouper
pageId: 28548385
version: 4
lastUpdated: 2026-07-01T05:44:38.779Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548385/Grouper+client+script+from+Excel+CSV
---

End users do not have access to GSH (since that would not be secure). They could automate some actions by writing grouper client command line scripts, which can be generated from something like Excel / CSV. Note you can expand this to do other operations, this example only has "create group" and "add member by subject identifier". Note shows an example using linux, but it could be run from a mac or windows. Just need Java

Here is an example

Use an excel worksheet (this is an example: )

The final column has a formula like this, just drag it down on all rows

```
=SWITCH(A2,"createGroup","java -jar grouperClient-2.5.39.jar --operation=groupSaveWs --name="&B2,"addMember","java -jar grouperClient-2.5.39.jar --operation=addMemberWs --groupName="&B2&" --subjectIdentifiers="&C2,"Error")
```

Download the grouper client

```
[mchyzer@flash grouper-2.5.20]$ wget https://repo1.maven.org/maven2/edu/internet2/middleware/grouper/grouperClient/2.5.39/grouperClient-2.5.39.jar
[mchyzer@flash grouper-2.5.20]$ jar -xf grouperClient-2.5.39.jar grouper.client.usage.example.txt
[mchyzer@flash grouper-2.5.20]$ mv grouper.client.usage.example.txt grouper.client.usage.txt 
[mchyzer@flash grouper-2.5.20]$ vi grouper.client.properties

grouperClient.webService.url = https://grouper.institution.edu/grouper-ws/servicesRest
grouperClient.webService.kerberosPrincipal = some_user_name
grouperClient.webService.password = securePassword!23

```

See documentation

```
[mchyzer@flash grouper-2.5.20]$ java -jar grouperClient-2.5.39.jar 
```

Select the "script" column in excel, copy, and paste to terminal

```
[mchyzer@flash grouper-2.5.20]$ java -jar grouperClient-2.5.39.jar 

#######################################

Run script copied from excel:

[mchyzer@flash grouper-2.5.20]$ java -jar grouperClient-2.5.39.jar --operation=groupSaveWs --name=test:testFolder:testClientScript:group1
Success: T: code: SUCCESS_INSERTED: test:testFolder:testClientScript:group1
[mchyzer@flash grouper-2.5.20]$ java -jar grouperClient-2.5.39.jar --operation=addMemberWs --groupName=test:testFolder:testClientScript:group1 --subjectIdentifiers=jsmith
Index 0: success: T: code: SUCCESS: 12345678
[mchyzer@flash grouper-2.5.20]$ java -jar grouperClient-2.5.39.jar --operation=addMemberWs --groupName=test:testFolder:testClientScript:group1 --subjectIdentifiers=bwilso
Index 0: success: T: code: SUCCESS: 87654321
[mchyzer@flash grouper-2.5.20]$ java -jar grouperClient-2.5.39.jar --operation=groupSaveWs --name=test:testFolder:testClientScript:group2
Success: T: code: SUCCESS_INSERTED: test:testFolder:testClientScript:group2
[mchyzer@flash grouper-2.5.20]$ java -jar grouperClient-2.5.39.jar --operation=addMemberWs --groupName=test:testFolder:testClientScript:group2 --subjectIdentifiers=wgreen
Index 0: success: T: code: SUCCESS: 135798642
[mchyzer@flash grouper-2.5.20]$ 
```
