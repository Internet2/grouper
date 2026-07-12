---
title: "Grouper Training - Use cases - Lesson 03: Access policy groups"
space: Grouper
pageId: 28544404
version: 12
lastUpdated: 2026-07-12T15:26:18.506Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544404/Grouper+Training+-+Use+cases+-+Lesson+03+Access+policy+groups
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

## Learning Objectives

- Understand the difference between policy groups and reference groups
- Translate natural language policy into digital policy using access policy groups

## Introduction to Access Policy Groups

Can be described by natural language policy, converted to digital policy

Subject membership in the allow and deny groups should be indirect (through reference groups)

Exceptions to policy in most case should be handled by application scoped reference groups (e.g. ad hoc groups)

Using reference groups in policy ensures that as subject attributes change, the effective membership is up to date and access control decisions are correct

## Hands on

### Create a new application template

- Navigate to the *app* folder
- create a new application (Folder actions -> Run template -> Template to run -> Application)
  
  - Key: ``
    ```
    gitlab
    ```
  - Friendly name: ``
    ```
    GitLab
    ```
  - Description: ``
    ```
    Access policies for the ITS GitLab version control system
    ```

### Create a new policy template

- Navigate to the *app:gitlab:service:policy* folder
- Create a new policy group (Folder actions -> Run template -> Template to run-> Policy group)
  
  - Key: ``
    ```
    gitlab_access
    ```
  - Friendly name: ``
    ```
    GitLab Access
    ```
  - Description: ``
    ```
    Overall access policy for the ITS GitLab version control system
    ```

### Create digital policy

The natural language policy is “all faculty and staff have access to GitLab, unless denied by the CISO, or the account is in a closure state. Reference groups are already available.

- Add group:  
  
  ```
  ref:role:all_facstaff
  ```
  
    
   (All Faculty/Staff) to *gitlab_access_allow*:
- Review *gitlab_access_deny*, it should already have *ref:iam:global_deny* as a member

### Review the gitlab_access policy definition

- Navigate to *gitlab_access*
- Edit visualization settings (Click the gear icon to show properties))
  
  - Show number of sibling objects: 12
  - Show Grouper object types (e.g., ref, basis): Checked
- Close the configuation
- Click Generate
- Click fullscreen

### Update policy to include Research Computing contractors

- Add basis group for "Research Computing affiliate"  
  
  ```
  basis:hr:employee:dept:10901:affiliate
  ```
  
    
   to *gitlab_access_allow*
- Trace membership for   
  
  ```
  Johnny Gardner
  ```
  
    
  from gitlab_access (Filter for Johnny Gardner -> Choose action -> Actions -> Trace membership)
- View the audit log on gitlab_access_allow (gitlab_access -> Group actions -> View audit log)
- Visualize the *gitlab_access* policy definition

### Manage security

Administrative access to the application template folders and groups is controlled by security groups in app:gitlab:security. Security groups are essentially policy groups for Grouper access.

Review the default privileges on gitlab_access_allow.

- Navigate to gitlab_access_allow
- Click on the Privileges tab

The GitLab application is owned by the ITS Infrastructure group. They should have access to update the policy once it is in production.

- Navigate to the *GitLab Updaters* group.
- Add group **
  ```
  basis:hr:employee:dept:10903:staff
  ```
  
  ** (Infrastructure staff) to this group
- Navigate to gitlab_access_allow
- Click on the Privileges tab and review
