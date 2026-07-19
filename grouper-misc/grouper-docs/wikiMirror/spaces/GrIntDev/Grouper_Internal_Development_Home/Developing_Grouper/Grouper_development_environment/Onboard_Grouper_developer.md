---
title: "Onboard Grouper developer"
space: GrIntDev
pageId: 48793813
version: 4
lastUpdated: 2026-07-12T07:02:06.159Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793813/Onboard+Grouper+developer
---

1. Request to Grouper lead
  
  1. Add to groups in [https://grouper.at.internet2.edu/](https://grouper.at.internet2.edu/)
  2. Grouper lead request to I2 staff member
2. Need to new dev public SSH key
3. Need username for unix
4. All requests coming from same IP address (or cidr)?
5. Need github username

Internet2 will

1. Give commit access to github for Bill
2. Add account to login.internet2.edu server
3. Add account to i2midev6
4. Give sudo root or whatever groups other devs have
5. Add account to [software.internet2.edu](http://software.internet2.edu)
6. Give same groups as other grouper devs. Is there a setgid bit or something so we can share files?

Test:

1. ssh to [login.internet2.edu](http://login.internet2.edu) with your key.
  
  
  ```
  Add this under .ssh/config on login.internet2.edu
   
  Host *
      ForwardAgent    yes
  ```
2. From login.internet2.edu, try to ssh to i2midev6.
3. From i2midev6 try to: sudo su -
4. From [login.internet2.edu](http://login.internet2.edu), try to ssh to webprod3.
5. From webprod3, create and delete a file here:  
    
  /home/htdocs/[software.internet2.edu/grouper/release/2.3.0](http://software.internet2.edu/grouper/release/2.3.0)
6. Is the group of the file you created grouperdist, and was it group writable?

Hints:

1. Once setup and in system email [techsupport@example.com](mailto:techsupport@example.com) for help
