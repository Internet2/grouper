---
title: "Migrating Grouper from one MySQL to another"
space: Grouper
pageId: 28543979
version: 2
lastUpdated: 2017-05-22T00:46:34.894Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543979/Migrating+Grouper+from+one+MySQL+to+another
---

Grouper 2.3.0 from demo server migrate to another server. Note, I used SQLyog community for all this

1. Create a schema
  
  1. utf8 charset
  2. utf8_bin collation
2. Create user, and grant all to that user on that schema
3. Exporting MYSQL structure and data did not work. So startup grouper against the new schema, and create tables with
  
  1. gsh -registry -runscript
  2. truncate the database, so there is no data
4. Export data to SQL from existing server, note, include option to disable and re-enable foreign keys
5. Import into new database
6. Turn on Grouper
