---
title: "Export / import Grouper MySQL database"
space: Grouper
pageId: 28548099
version: 5
lastUpdated: 2026-07-01T05:45:20.141Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548099/Export+import+Grouper+MySQL+database
---

## The problem

There are issues (especially with older versions of Mysql) in exporting and importing a database.

When importing, look for this error (or something like it):

```
ERROR 1356 (HY000) at line 4855: View 'XXXXXXXXXX' (e.g. grouper_attr_asn_stem_v) references invalid table(s) or column(s) or function(s) or definer/invoker of view lack rights to use them
```

When starting Grouper you get this error:

```
Caused by: java.sql.BatchUpdateException: Duplicate entry '871ed869f7084f308cbdd25cc9c35b66-0e3f440fbf38473c87d266d6ba81188' for key 'membership_uniq_idx'
```

If you look in the database you might see views that are tables:

The reason for this is the mysqldump will create temporary tables that look like views, but even so the views which are based on other views and created in alphabetical order do not work right. Mysql cannot create views if the underlying objects are not there, which makes things complicated and delicate.

## The solution

1. Create a database and a user and password **(note the character set and collation)**Note: you should lock down the user to only be able to connect from wherever they should be able to connect from
  
  
  ```
  [mchyzer@i2midev6 tomcats]$ mysql -u root -p
  MariaDB [(none)]> CREATE DATABASE grouper_v2_3temp CHARACTER SET utf8 COLLATE utf8_bin;
  MariaDB [(none)]> CREATE USER grouper_v2_3temp IDENTIFIED BY 'grouper_v2_3temp1';
  MariaDB [(none)]> GRANT ALL PRIVILEGES ON grouper_v2_3temp.* TO 'grouper_v2_3temp';
  ```
2. Generate the Grouper SQL to create the database, but dont run it
  
  
  ```
  [mchyzer@i2midev6 bin]$ ./gsh -registry -check
  edit that file and delete all the stuff at the beginning, and keep the "create view" statements at the end
  ```
3. Mysqldump from the database to copy from. Do not include views. If you include views, it difficult to edit the file since peppered throughout the file is creating temporary tables for views. So you need to create without views. sqlyog can do this, maybe other gui tools too
4. Edit the sql dump file with the new database at top
  
  
  ```
  USE grouper_v2_3temp;
  ```
5. Import the database into the new database
  
  
  ```
   mysql -u grouper_v2_3temp -p themysqldump.sql
  ```
6. Import the views sql from the gsh script
  
  
  ```
  mysql -u grouper_v2_3temp -p < /tmp/grouper_v2_3_ui_temp/WEB-INF/ddlScripts/grouperDdl_20200130_01_30_43_153.sql
  ```
7. Start Grouper!
