---
title: "Grouper custom templates via GSH - converting an existing GSH script - insert a row in table"
space: Grouper
pageId: 28548406
version: 6
lastUpdated: 2026-07-12T06:40:15.541Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548406/Grouper+custom+templates+via+GSH+-+converting+an+existing+GSH+script+-+insert+a+row+in+table
---

This wiki will show how an existing GSH script can be converted to a GSH template using Java and eclipse.

## Setup the development environment

First, [install Eclipse and create a maven project with a Grouper dependency](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/48792900/How+to+Setup+a+lite+Grouper+Development+Environment+for+Grouper). Note, use the most current version of Grouper.

## Existing script

Here is the script to convert. It takes two pieces of input and inserts a row into a custom subject source table in the Grouper database

```
grouperSession = GrouperSession.startRootSession();
kerb = "someName/medley.isc-seo.upenn.edu";
reason = "some school kerberos principal for so and so";
sqlRun("insert into service_principals (principal_name, id, last_updated, reason) values ('" + kerb + "', (select max(id)+1 from service_principals), CURRENT_TIMESTAMP
, '" + reason + "')");
addMember("penn:etc:ldapUsers", kerb);
addMember("penn:etc:webServiceClientUsers", kerb);
```

This is for admins, so it was not initially implemented with bind variables, but we should change it so it uses bind variables to improve the security.

This is currently a groovy script, but not Java. You can use groovy in GSH templates, but for this example, we will make it java

## Convert the script to Java with bind variables

We dont need to start a session anymore, since the GSH template does that for you

```
grouperSession = GrouperSession.startRootSession();
```

Convert the inputs to start with the standard GSH template prefix, put them in java with sample data, but they will be commented out in the actual script. They are in Java so the script compiles

```
//input of project name, alphanumeric, start with lower.
String gsh_input_kerberosPrincipal = "someName/medley.isc-seo.upenn.edu";
String gsh_input_description = "some school kerberos principal for so and so";
```

Users might want to do multiple in a row, so lets not navigate away from the screen

```
// stay on screen
gsh_builtin_gshTemplateOutput.assignRedirectToGrouperOperation("NONE");
```

Convert the SQL to use bind variables for security

```
// insert the row with bind variables
GcDbAccess gcDbAccess = new GcDbAccess().connectionName("grouper");
gcDbAccess.sql("insert into service_principals (principal_name, id, last_updated, reason) values (?, (select max(id)+1 from service_principals), CURRENT_TIMESTAMP, ?)");
gcDbAccess.addBindVar(gsh_input_kerberosPrincipal).addBindVar(gsh_input_description).executeSql();
gsh_builtin_gshTemplateOutput.addOutputLine("Success: row is inserted in subject source table");
```

Convert the GSH shortcut help methods to use the GSH method chaining API

```
// allow principal to use LDAP
new MembershipSave().assignGroupName("penn:etc:ldapUsers").assignSubjectId(gsh_input_kerberosPrincipal).save();
gsh_builtin_gshTemplateOutput.addOutputLine("Success: kerberos principal subject added to LDAP users group: penn:etc:ldapUsers");

// allow principal to use WS
new MembershipSave().assignGroupName("penn:etc:webServiceClientUsers").assignSubjectId(gsh_input_kerberosPrincipal).save();
gsh_builtin_gshTemplateOutput.addOutputLine("Success: kerberos principal subject added to WS users group: penn:etc:webServiceClientUsers");

```

Add a success message to the end

```
// done
gsh_builtin_gshTemplateOutput.addOutputLine("Success: kerberos insert is complete");

```

## Make the script compile in eclipse

This script can be put in a class in the default (none) package with some imports and Java stuff at start including main(), and a curly at end

```

import edu.internet2.middleware.grouper.MembershipSave;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class Test19 {

  public static void main(String[] args) {
    
    GrouperStartup.startup();
    
    //input of project name, alphanumeric, start with lower.
    String gsh_input_kerberosPrincipal = "someName/medley.isc-seo.upenn.edu";
    String gsh_input_description = "some school kerberos principal for so and so";

    GshTemplateOutput gsh_builtin_gshTemplateOutput = new GshTemplateOutput();

    // stay on screen
    gsh_builtin_gshTemplateOutput.assignRedirectToGrouperOperation("NONE");

    // insert the row with bind variables
    GcDbAccess gcDbAccess = new GcDbAccess().connectionName("grouper");
    gcDbAccess.sql("insert into service_principals (principal_name, id, last_updated, reason) values (?, (select max(id)+1 from service_principals), CURRENT_TIMESTAMP, ?)");
    gcDbAccess.addBindVar(gsh_input_kerberosPrincipal).addBindVar(gsh_input_description).executeSql();
    gsh_builtin_gshTemplateOutput.addOutputLine("Success: row is inserted in subject source table");

    // allow principal to use LDAP
    new MembershipSave().assignGroupName("penn:etc:ldapUsers").assignSubjectId(gsh_input_kerberosPrincipal).save();
    gsh_builtin_gshTemplateOutput.addOutputLine("Success: kerberos principal subject added to LDAP users group: penn:etc:ldapUsers");

    // allow principal to use WS
    new MembershipSave().assignGroupName("penn:etc:webServiceClientUsers").assignSubjectId(gsh_input_kerberosPrincipal).save();
    gsh_builtin_gshTemplateOutput.addOutputLine("Success: kerberos principal subject added to WS users group: penn:etc:webServiceClientUsers");
    
    // done
    gsh_builtin_gshTemplateOutput.addOutputLine("Success: kerberos insert is complete");
  }

}

```

That compiles in Eclipse so we know we have no Java compile issues, lets comment out parts to be put in the GSH template config. Note, you could just copy the middle part for the eclipse template, but then if someone goes later to copy from template config to eclipse, they will have to recreate the top and bottom part, which takes some time. So upload it commented out, and its easy to put in Eclipse later to modify it

```
//
////uncomment to compile in eclipse (and last line)
//// these are standard imports, can be commented out in script but needed in eclipse
//import edu.internet2.middleware.grouper.MembershipSave;
//import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
//import edu.internet2.middleware.grouper.misc.GrouperStartup;
//import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
//
//
//public class Test19 {
//
//  public static void main(String[] args) {
//    
//    GrouperStartup.startup();
//    
//    //input of project name, alphanumeric, start with lower.
//    String gsh_input_kerberosPrincipal = "someName/medley.isc-seo.upenn.edu";
//    String gsh_input_description = "some school kerberos principal for so and so";
//
//    GshTemplateOutput gsh_builtin_gshTemplateOutput = new GshTemplateOutput();

    // stay on screen
    gsh_builtin_gshTemplateOutput.assignRedirectToGrouperOperation("NONE");

    // insert the row with bind variables
    GcDbAccess gcDbAccess = new GcDbAccess().connectionName("grouper");
    gcDbAccess.sql("insert into service_principals (principal_name, id, last_updated, reason) values (?, (select max(id)+1 from service_principals), CURRENT_TIMESTAMP, ?)");
    gcDbAccess.addBindVar(gsh_input_kerberosPrincipal).addBindVar(gsh_input_description).executeSql();
    gsh_builtin_gshTemplateOutput.addOutputLine("Success: row is inserted in subject source table");

    // allow principal to use LDAP
    new MembershipSave().assignGroupName("penn:etc:ldapUsers").assignSubjectId(gsh_input_kerberosPrincipal).save();
    gsh_builtin_gshTemplateOutput.addOutputLine("Success: kerberos principal subject added to LDAP users group: penn:etc:ldapUsers");

    // allow principal to use WS
    new MembershipSave().assignGroupName("penn:etc:webServiceClientUsers").assignSubjectId(gsh_input_kerberosPrincipal).save();
    gsh_builtin_gshTemplateOutput.addOutputLine("Success: kerberos principal subject added to WS users group: penn:etc:webServiceClientUsers");
    
    // done
    gsh_builtin_gshTemplateOutput.addOutputLine("Success: kerberos insert is complete");
//  }
//
//}

```

## Configure the template

This template doesnt naturally belong to a folder so I have an "etc:templates" folder to put things like this

Make a group of users who can do this

Configure the template

## Run the template
