Add scripts in this dir as follows to override the ddlutils ddl (and case matters!):

File name must be objectName.V#.dbname.sql
e.g. Grouper.5.oracle10.sql

That will be the script to use when upgrading from version 4 to version 5 for 
the Grouper objectName and oracle database type

The objectName is the first part of the name of enum 
in the edu.internet2.middleware.grouper.ddl package, it is also a column
in the grouper_ddl table.

The version number is a valid version in the enum in 
edu.internet2.middleware.grouper.ddl package
or -1 means there is no entry in the table.

The dbname must be a valid ddlutils dbname:
axion, cloudscape, db2, db2v8, derby, firebird, hsqldb, interbase, maxdb, mckoi, 
mssql, mysql, mysql5, oracle, oracle10, oracle9, postgresql, sapdb, sybase, sybasease15

Also the following catchalls are acceptable: oracleall, mysqlall, db2all, sybaseall
