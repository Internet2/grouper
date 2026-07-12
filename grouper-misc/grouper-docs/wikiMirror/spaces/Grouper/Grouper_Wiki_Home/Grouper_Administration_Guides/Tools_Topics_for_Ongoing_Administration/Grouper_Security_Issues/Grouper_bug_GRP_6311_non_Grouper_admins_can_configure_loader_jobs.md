---
title: "Grouper bug - GRP-6311 - non-Grouper-admins can configure loader jobs"
space: Grouper
pageId: 28548493
version: 9
lastUpdated: 2026-07-01T05:44:22.393Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548493/Grouper+bug+-+GRP-6311+-+non-Grouper-admins+can+configure+loader+jobs
---

In Grouper versions v5.17.1 - v5.20.2 group admins (who are not Grouper wheel group members (sysadmins)) can configure a SQL or LDAP loader job on a group. This will set attributes on the group, but will not schedule the job. The next time the daemon container (re)starts or if an admin clicks Daemon actions → Schedule jobs, the loader job will be scheduled.

## Reproduce the issue (before and after remediating)

1. Give yourself ADMIN privilege on an empty unused group (do not expect admin rights from being sys admin)
2. Go to sys admin group, make sure you have ADMIN privileges on the group (so you can get back in later)
3. Remove yourself from being a member of the sys admin group
  
  1. Might need to wait a few minutes for things to settle down, caches need to clear.
4. Go to the (NON-sysadmin!) group that is empty and is not used for anything
5. Make a SQL simple loader job
  
  1. Non functioning query, e.g. select 1 from grouper_groups where 1!=1
  2. Use a never-firing schedule, e.g. 0 0 0 31 2 ? *
6. If that saves successfully, your environment is affected.
  
  1. If it does not save successfully, then you are not affected, or you have successfully upgraded or patched
  2. If successfully patched, you will see "Cannot assign loader attribute"
7. If you click Daemon actions → Schedule jobs, or bounce your daemon, then the job will be scheduled.
8. Delete the new group when done testing
9. Add yourself back to the sysadmin group

## Remediate

Upgrade to Grouper v5.20.5+  
Or patch Grouper v5.17.1 - v5.20.2 (UI only)

```
# cd slashRoot/opt/grouper/grouperWebapp/WEB-INF
# wget https://software.internet2.edu/grouper/downloads/tools/loaderHook.tgz
# tar xzvf loaderHook.tgz
# rm loaderHook.tgz

You should see two classes

# cd classes/edu
# find .
.
./internet2
./internet2/middleware
./internet2/middleware/grouper
./internet2/middleware/grouper/hooks
./internet2/middleware/grouper/hooks/examples
./internet2/middleware/grouper/hooks/examples/LoaderAttributeVetoHook.class
./internet2/middleware/grouper/hooks/examples/LoaderAttributeValueVetoHook.class
```

Add to grouper.properties (or append with comma if the hooks are already configured with another class)

```
hooks.attributeAssign.class = edu.internet2.middleware.grouper.hooks.examples.LoaderAttributeVetoHook
hooks.attributeAssignValue.class = edu.internet2.middleware.grouper.hooks.examples.LoaderAttributeValueVetoHook
```

Hooks require a bounce of the UI.

## Remove hook

After v5.20.5+ you can remove the hook

1. Remove the class files from the classes directory
2. Remove the grouper.properties hook configurations
  
  1. remove the values if those are your only hooks of that type
  2. or remove the individual values if there are multiple comma separated

## See if a non-grouper-admin has been adding attributes

Run this query and see if anyone who is not a sysadmin has assigned loader attributes. Might want to followup with them.

```
select * from grouper_audit_entry_v gaev, grouper_audit_type gat, grouper_attribute_def_name gadn
where gat.action_name = 'addAttributeAssignGroup'
and gaev.audit_type_id = gat.id
and gaev.created_on > 1742097600000
and string04 = gadn.name
and (gadn.name like '%:etc:legacy:attribute:legacyGroupType_grouperLoader'
or gadn.name like '%:etc:attribute:loaderLdap:grouperLoaderLdap');
```
