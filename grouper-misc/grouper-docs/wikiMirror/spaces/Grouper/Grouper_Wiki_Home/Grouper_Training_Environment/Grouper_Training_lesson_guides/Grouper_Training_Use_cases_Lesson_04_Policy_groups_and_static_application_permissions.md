---
title: "Grouper Training - Use cases - Lesson 04: Policy groups and static application permissions"
space: Grouper
pageId: 28544252
version: 20
lastUpdated: 2026-11-17T20:10:07.937Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544252/Grouper+Training+-+Use+cases+-+Lesson+04+Policy+groups+and+static+application+permissions
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

In this lesson, anything you type is in a box with a copy button on the right. Buttons, menus, and labels to click or select are in double quotes.

## Learning objectives

- Understand access policy groups and static application permissions
- Implement the Grouper security model
- Configure provisioning to LDAP for eduPersonEntitlement
- Configure Shibboleth to release eduPersonEntitlement

## Hands on

### Create the wiki application

1. In the left menu, click "Browse folders", click "Root", then click the folder "app".
2. Click "Folder actions" → "Run template".
3. "Template to run": select "Application".
4. Fill in the form (below), then click "Next".

"Key":

```
wiki
```

"Description":

```
Student wiki
```

Review the folders and groups the template will create, then click "Submit".

### Create policy for wiki application

wiki_user is an application-specific role. Subjects in this role have general access to the wiki. The natural language policy is “All students have access to the student wiki, unless they are in the global deny group.”

1. Go to the folder "app:wiki:service:policy" (Browse folders → Root → app → wiki → service → policy).
2. Click "Folder actions" → "Run template".
3. "Template to run": select "Policy group".
4. Fill in the form (below), then click "Next".

"Key":

```
wiki_user
```

"Description":

```
Access policy for student wiki
```

Review the groups the template will create, then click "Submit".

#### Add students to the allow group

1. Go to the group "app:wiki:service:policy:wiki_user_allow".
2. Click "Add members".
3. In "Member name or ID", paste this, pick "students" from the list, and click "Add":
  
  
  ```
  ref:student:students
  ```
  
  Go to the group "app:wiki:service:policy:wiki_user_deny" and review its members.
  
  
  
  #### Visualize the policy
  
  
  
  1. Go to the group "app:wiki:service:policy:wiki_user".
  2. Click "Group actions" → "Visualization".
  3. Click the gear icon to show the settings.
  4. "Show number of sibling objects": uncheck "All" and type the number below.
    
    
    ```
    15
    ```
4. Click "Generate".

### Review the external system for provisioning

Provisioning targets depend on an external system that holds the connection settings. For this lesson, the LDAP external system has already been set up with config id “demo”.

1. In the left menu, click "Miscellaneous", then click "External systems".
2. On the row for "demo", click "Actions" → "View details".
3. Go back to "External systems". On the row for "demo", click "Actions" → "Test".

### Review the provisioner

A provisioner named eduPersonEntitlement has already been set up. In this provisioner, members of a group have their LDAP user record updated in the eduPersonEntitlement attribute. The entitlement value can be a static string, or falls back to the group name.

1. In the left menu, click "Miscellaneous", then click "Provisioning".
2. On the row for "eduPersonEntitlement", click "Actions" → "Edit provisioner".

#### Create a full sync provisioning job

The provisioner needs a full sync or incremental job to do the provisioning. If the provisioner "Actions" menu has no "Daemon - full" option, do this while editing the provisioner:

1. "Show advanced": select "true".
2. "Add disabled full sync daemon": select "true".
3. "Add disabled incremental sync daemon": select "true".
4. Click "Submit".

### Configure provisioning on the group

1. Go to the group "app:wiki:service:policy:wiki_user".
2. Click "Group actions" → "Provisioning".
3. Click "Provisioning actions" → "Edit provisioning settings".
4. "Target name": select "eduPersonEntitlement".
5. "Provision": select "Yes".
6. "Entitlement String": type the value below, then click "Save".
  
  
  ```
  http://sp.example.org/wiki
  ```

### Run the provisioner job

1. In the left menu, click "Miscellaneous", then click "Provisioning".
2. On the row for "eduPersonEntitlement", click "Actions" → "Daemon - full".
3. Click "Job actions" → "Enable job".
4. Click "Job actions" → "Run job now".
5. Click "Apply filter" every few seconds until the job shows as complete.

### Verify provisioning results

1. Open the GTE jump page and launch the LDAP manager, then log in:
  
  
  ```
  https://localhost:8443/
  ```

Check that members of "app:wiki:service:policy:wiki_user" have an eduPersonEntitlement of http://sp.example.org/wiki. For example, look up this user:

```
uid=aalexan2
```

### Check that Shibboleth releases eduPersonEntitlement

1. Open a private / incognito browser window and go to:
  
  
  ```
  https://localhost:8443/app
  ```

Log in with username:

```
abrown
```

and password:

```
password
```

Look at the value for eduPersonEntitlement.
