---
title: "Grouper Training - Use cases - Lesson 03: Access policy groups"
space: Grouper
pageId: 28544404
version: 14
lastUpdated: 2026-11-17T20:10:07.398Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544404/Grouper+Training+-+Use+cases+-+Lesson+03+Access+policy+groups
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

In this lesson, anything you type is in a box with a copy button on the right. Buttons, menus, and labels to click or select are in double quotes.

## Learning objectives

- Understand the difference between policy groups and reference groups
- Translate natural language policy into digital policy using access policy groups

## Introduction to access policy groups

- Can be described by natural language policy, converted to digital policy
- Subject membership in the allow and deny groups should be indirect (through reference groups)
- Exceptions to policy in most cases should be handled by application scoped reference groups (e.g. ad hoc groups)
- Using reference groups in policy ensures that as subject attributes change, the effective membership is up to date and access control decisions are correct

## Hands on

### Create a new application template

1. In the left menu, click "Browse folders", click "Root", then click the folder "app".
2. Click "Folder actions" → "Run template".
3. "Template to run": select "Application".
4. Fill in the form (below), then click "Next".

"Key":

```
gitlab
```

"Friendly Name": check "Edit friendly name", then type:

```
GitLab
```

"Description":

```
Access policies for the ITS GitLab version control system
```

Review the folders and groups the template will create, then click "Submit".

### Create a new policy template

1. Go to the folder "app:gitlab:service:policy" (Browse folders → Root → app → gitlab → service → policy).
2. Click "Folder actions" → "Run template".
3. "Template to run": select "Policy group".
4. Fill in the form (below), then click "Next".

"Key":

```
gitlab_access
```

"Friendly Name": check "Edit friendly name", then type:

```
GitLab Access
```

"Description":

```
Overall access policy for the ITS GitLab version control system
```

Review the groups the template will create, then click "Submit".

### Create digital policy

The natural language policy is “all faculty and staff have access to GitLab, unless denied by the CISO, or the account is in a closure state.” Reference groups are already available.

1. Go to the group "app:gitlab:service:policy:gitlab_access_allow".
2. Click "Add members".
3. In "Member name or ID", paste this, pick "All Faculty/Staff" from the list, and click "Add":
  
  
  ```
  ref:role:all_facstaff
  ```
  
  Go to the group "app:gitlab:service:policy:gitlab_access_deny" and review it. It should already have "ref:iam:global_deny" as a member.
  
  
  
  ### Review the gitlab_access policy definition
  
  
  
  1. On the group "app:gitlab:service:policy:gitlab_access", click "Group actions" → "Visualization".
  2. Click the gear icon to show the settings.
  3. "Show number of sibling objects": uncheck "All" and type the number below.
    
    
    ```
    12
    ```
4. Check "Show Grouper object types (e.g., ref, basis)".
5. Click the gear icon again to close the settings.
6. Click "Generate", then click "fullscreen".

### Update policy to include Research Computing contractors

1. Go to the group "app:gitlab:service:policy:gitlab_access_allow".
2. Click "Add members".
3. In "Member name or ID", paste this, pick "Research Computing affiliate" from the list, and click "Add":
  
  
  ```
  basis:hr:employee:dept:10901:affiliate
  ```
  
  
  
  #### Trace the membership of Johnny Gardner
  
  
  
  1. Go to the group "app:gitlab:service:policy:gitlab_access".
  2. On the "Members" tab, type this in the "Member name" filter box and click "Apply filter":
    
    
    ```
    Johnny Gardner
    ```
4. On the row for Johnny Gardner, click "Actions" → "Trace membership".

#### View the audit log

1. Go to the group "app:gitlab:service:policy:gitlab_access_allow".
2. Click "Group actions" → "Audit log".

#### Visualize the policy again

1. On the group "app:gitlab:service:policy:gitlab_access", click "Group actions" → "Visualization".
2. Click the gear icon to show the settings.
3. "Show number of sibling objects": uncheck "All" and type the number below.
  
  
  ```
  12
  ```
4. Check "Show Grouper object types (e.g., ref, basis)".
5. Click the gear icon again to close the settings.
6. Click "Generate", then click "fullscreen".

### Manage security

Administrative access to the application template folders and groups is controlled by security groups in app:gitlab:security. Security groups are essentially policy groups for Grouper access.

Review the default privileges on gitlab_access_allow:

1. Go to the group "app:gitlab:service:policy:gitlab_access_allow".
2. Click the "Privileges" tab.

The GitLab application is owned by the ITS Infrastructure group. They should have access to update the policy once it is in production.

1. Go to the group "app:gitlab:security:gitlabUpdaters".
2. Click "Add members".
3. In "Member name or ID", paste this, pick "Infrastructure staff" from the list, and click "Add":
  
  
  ```
  basis:hr:employee:dept:10903:staff
  ```
4. Go to the group "app:gitlab:service:policy:gitlab_access_allow".
5. Click the "Privileges" tab and review.
