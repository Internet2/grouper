---
title: "Grouper Training - Use cases - Lesson 02: Basis and reference groups part 2"
space: Grouper
pageId: 28544245
version: 26
lastUpdated: 2026-11-17T20:10:06.859Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544245/Grouper+Training+-+Use+cases+-+Lesson+02+Basis+and+reference+groups+part+2
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

In this lesson, anything you type is in a box with a copy button on the right. Buttons, menus, and labels to click or select are in double quotes.

## Learning objectives (continued)

- Understand the difference between reference groups and basis groups
- Create and manage reference and basis groups
- Implement subject attribute lifecycle requirements

## Exercise: all students reference group (continued)

### Include students with no class year

Not all students have class years assigned. This includes part-time students, employees taking courses, and non-matriculated students.

Fortunately, data about these students is available in the SIS, and basis groups have already been created for us.

1. Go to the group "ref:student:students".
2. Click "Add members".
3. In "Member name or ID", paste this group, pick "Active No Year" from the list, and click "Add":
  
  
  ```
  basis:sis:prog_status:year:ac:no_year
  ```
  
  How many students are there now? (See the member count near the bottom of the "Members" tab.)
  
  
  
  ### Include exchange students
  
  Exchange students from your sister school can take classes, but never have official records in the SIS. However, they do have a local NetID and a basis group is maintained for them.
  
  
  
  1. Go to the group "ref:student:students".
  2. Click "Add members".
  3. In "Member name or ID", paste this group, pick "Exchange Student" from the list, and click "Add":
    
    
    ```
    basis:sis:prog_status:all:es
    ```
  
  How many students are there now? (See the member count near the bottom of the "Members" tab.)
  
  
  
  ### Include ad-hoc transfer students
  
  Students who transfer to your campus often need access well ahead of SIS data being fully updated.
  
  
  
  #### Create group ref:student:transfer_student
  
  
  
  1. Go to the folder "ref:student" (click "student" in the breadcrumb at the top).
  2. Click "Folder actions" → "Create new group".
  3. Fill in the "New group" form (below).
  
  "Create in this folder": already filled in with "ref:student". If not, type:
  
  
  ```
  ref:student
  ```
  
  "Group name":
  
  
  ```
  Transfer Student
  ```
  
  "Group ID": it fills in automatically. Check "Edit the ID", and replace it with:
  
  
  ```
  transfer_student
  ```
  
  "Description":
  
  
  ```
  Students recently transferred but not yet in SIS
  ```
  
  Click "Save". You are now on the group "ref:student:transfer_student".
  
  
  
  #### Add the manual type to the group
  
  
  
  1. On the group "ref:student:transfer_student", click "Group actions" → "Types".
  2. Click "Type actions" → "Edit type settings".
  3. Fill in the form (below), then click "Save".
  
  "Type": select "Yes, has direct type configuration"
  
  "Type name": select "manual"
  
  "Data owner":
  
  
  ```
  Registrar
  ```
  
  "Member description":
  
  
  ```
  Ad-hoc recent transfer students not yet in SIS
  ```
  
  
  
  #### Add the transfer students
  
  
  
  1. Go to the group "ref:student:transfer_student".
  2. Click "Add members" → "Import".
  3. Scroll down to "Add these members" and change the radio button to "Copy/paste a list of member IDs".
  4. Paste the whole list below into the text area (one per line), then click "Submit".
    
    
    ```
    whawkins
    hyoung
    jmejia
    ```
  
  
  
  #### Add transfer_student to students
  
  
  
  1. Go to the group "ref:student:students".
  2. Click "Add members".
  3. In "Member name or ID", paste this group, pick "Transfer Student" from the list, and click "Add":
    
    
    ```
    ref:student:transfer_student
    ```
  
  How many students are there now? (See the member count near the bottom of the "Members" tab.)
  
  The number of students did not go up by 3 as you might have expected. Why? One of the transfer students was already a member of students.
  
  
  
  #### Trace the membership of jmejia
  
  
  
  1. On the group "ref:student:students", "Members" tab, type this in the "Member name" filter box and click "Apply filter":
    
    
    ```
    jmejia
    ```
4. On the row for Jennifer Mejia, click "Actions" → "Trace membership". This shows the multiple paths that make her a member.
5. Click "Show timeline" to see the membership events.

### Include leave of absence students

Students take a leave of absence for a variety of reasons. These students may or may not return, but retain student access for an extended period of time. Basis groups for leave of absence students already exist.

1. Go to the group "ref:student:students".
2. Click "Add members".
3. In "Member name or ID", paste this group, pick "Leave of Absence" from the list, and click "Add":
  
  
  ```
  basis:sis:prog_status:all:la
  ```

How many students are there now? (See the member count near the bottom of the "Members" tab.)

### Visualization: what do you mean by “student”?

1. On the group "ref:student:students", click "Group actions" → "Visualization".
2. Click "Generate". Review the students reference group.
3. In "Member name or ID", paste this, pick Jennifer Mejia from the list, and click "Generate" again to trace her membership visually:
  
  
  ```
  jmejia
  ```

The students reference group is used in access policy for student services. Being a “student” means access to a broad array of student services. This institutionally meaningful cohort is well defined, easily understood, and capable of being extended in a rational way.
