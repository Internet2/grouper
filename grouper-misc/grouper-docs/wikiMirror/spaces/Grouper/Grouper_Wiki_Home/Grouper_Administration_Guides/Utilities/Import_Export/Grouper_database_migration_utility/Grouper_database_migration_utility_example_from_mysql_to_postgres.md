---
title: "Grouper database migration utility example from mysql to postgres"
space: Grouper
pageId: 28555155
version: 2
lastUpdated: 2026-07-01T05:38:50.028Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555155/Grouper+database+migration+utility+example+from+mysql+to+postgres
---

1. Start with an empty postgres database (destination)
  
  
  
  1. Point grouper and clear out postgres: grouper.hibernate.properties
    
    
    ```
    
    
    hibernate.connection.url = jdbc:postgresql://localhost:5432/postgres?currentSchema=grouper_v2_5
    
    hibernate.connection.username         = grouper_v2_5
    # If you are using an empty password, depending upon your version of
    # Java and Ant you may need to specify a password of "".
    # Note: you can keep passwords external and encrypted: https://bugs.internet2.edu/jira/browse/GRP-122
    hibernate.connection.password         = *********
    ```
  2. Run gsh command to clear out database
    
    
    ```
    ./gsh.sh -registry -droponly -runscript
    2021-08-30 09:55:35,203: [main] WARN GrouperAntProject.log(96) - - SQL: DROP VIEW IF EXISTS grouper_ext_subj_v cascade
    2021-08-30 09:55:35,224: [main] WARN GrouperAntProject.log(96) - - 0 rows affected
    2021-08-30 09:55:35,317: [main] WARN GrouperAntProject.log(96) - - SQL: ALTER TABLE grouper_group_set DROP constraint fk_group_set_member_field_id
    2021-08-30 09:55:35,317: [main] WARN GrouperAntProject.log(96) - - 0 rows affected
    ...
    ```
  3. See the empty database
2. Point grouper at mysql, and see folders and groups
  
  1. grouper.hibernate.properties
    
    
    ```
    hibernate.connection.url = jdbc:mysql://localhost:3306/grouper_v2_5?useSSL=false
    
    hibernate.connection.username         = grouper_v2_5
    # If you are using an empty password, depending upon your version of
    # Java and Ant you may need to specify a password of "".
    # Note: you can keep passwords external and encrypted: https://bugs.internet2.edu/jira/browse/GRP-122
    hibernate.connection.password         = *******
    
    
    hibernate.jdbc.batch_size = 1000
    ```
  2. See some groups
3. Add a database connection for postgres in external system
4. Test connection
5. Make a file migrateToPostgres.gsh
  
  
  ```
  import edu.internet2.middleware.grouper.ddl.GrouperDdlDataMigration;
  new GrouperDdlDataMigration().assignDatabaseFrom("grouper").assignDatabaseTo("migrateToPostgres").migrateDatabase();
  ```
6. Invoke that script
  
  
  ```
  ./gsh.sh C:/git/grouper_prod/grouper/temp/migrateToPostgres.gsh
  ...
  Success: table: grouper_sync_log migrated 0 rows
  Success: table: grouper_sync_member migrated 0 rows
  Success: table: grouper_sync_membership migrated 0 rows
  Success: table: grouper_table_index migrated 4 rows
  Success: table: subject migrated 0 rows
  Success: table: subjectattribute migrated 0 rows
  STEP4: complete
  STEP5: creating indexes, foreign keys, and views in destination...
  STEP5: complete
  Took: 00:00:27.916
  
  groovy:000> :exit
  ```
