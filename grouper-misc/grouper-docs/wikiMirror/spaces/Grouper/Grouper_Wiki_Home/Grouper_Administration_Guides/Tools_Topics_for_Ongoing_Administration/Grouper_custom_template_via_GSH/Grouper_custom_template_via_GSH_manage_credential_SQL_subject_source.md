---
title: "Grouper custom template via GSH manage credential SQL subject source"
space: Grouper
pageId: 28547808
version: 3
lastUpdated: 2026-07-01T05:46:07.046Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547808/Grouper+custom+template+via+GSH+manage+credential+SQL+subject+source
---

At Penn we have a subject source for kerberos principals for LDAP/WS authentication. To grant access to a group, the privilege on the group can be set to allow READ to the service principal (or other privs for WS).

More actions → Kerberos principals

Fill in form

## GSH config

Script

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
