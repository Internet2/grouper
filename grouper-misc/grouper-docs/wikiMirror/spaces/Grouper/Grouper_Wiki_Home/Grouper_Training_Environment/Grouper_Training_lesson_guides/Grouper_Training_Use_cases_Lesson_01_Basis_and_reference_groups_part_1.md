---
title: "Grouper Training - Use cases - Lesson 01: Basis and reference groups part 1"
space: Grouper
pageId: 28544224
version: 48
lastUpdated: 2026-11-17T20:10:06.271Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544224/Grouper+Training+-+Use+cases+-+Lesson+01+Basis+and+reference+groups+part+1
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

In this lesson, anything you type is in a box with a copy button on the right. Buttons, menus, and labels to click or select are in double quotes.

## Learning objectives

- Understand the difference between reference groups and basis groups
- Create and manage reference and basis groups
- Implement subject attribute lifecycle requirements

## Exercise: all students reference group

Create an all students reference group to be used in access policy and the “all students” mailing list.

### Create folder ref:student

1. In the left menu, click "Browse folders", click "Root", then click the folder "ref".
2. Click "Folder actions" → "Create new folder".
3. Fill in the "New folder" form (below).

"Create in this folder": already filled in with "ref". If not, type:

```
ref
```

"Folder name":

```
student
```

Click "Save". You are now on the folder "ref:student".

### Create group ref:student:students

1. On the folder "ref:student", click "Folder actions" → "Create new group".
2. Fill in the "New group" form (below).

"Create in this folder": already filled in with "ref:student". If not, type:

```
ref:student
```

"Group name":

```
students
```

"Description":

```
This group contains all students for the purpose of access control. Members automatically get access to a broad selection of student services. You can view where this group is in use by selecting "This group's memberships in other groups" under the "More" tab
```

Click "Save". You are now on the group "ref:student:students".

### Add ref type to group

1. On the group "ref:student:students", click "Group actions" → "Types".
2. Click "Type actions" → "Edit type settings".
3. Fill in the form (below), then click "Save".

"Type": select "Yes, has direct type configuration"

"Type name": select "ref"

"Data owner":

```
Registrar
```

"Member description":

```
All student subjects for the purpose of access control
```

### Add class years to ref:student:students

1. Go to the group "ref:student:students" (click it in the breadcrumb at the top).
2. Click "Add members" → "Import".
3. Scroll down to "Add these members" and change the radio button to "Copy/paste a list of member IDs".
4. Paste the whole list below into the text area (one per line), then click "Submit".
  
  
  ```
  basis:sis:prog_status:year:ac:2027
  basis:sis:prog_status:year:ac:2028
  basis:sis:prog_status:year:ac:2029
  basis:sis:prog_status:year:ac:2030
  ```

### Filter for direct membership

On the "Members" tab, set "Filter for" to "Has direct membership", then click "Apply filter".

### Filter for indirect membership

Set "Filter for" to "Has indirect membership", then click "Apply filter".

How many students are in the group (look near the bottom)?

### Recently graduated students

You suddenly remember that recently graduated students have a 7 month grace period where they retain full access to student services.

1. On the group "ref:student:students", click "Add members".
2. In "Member name or ID", paste this group and pick it from the list:
  
  
  ```
  basis:sis:prog_status:year:cm:2026
  ```

Before clicking "Add", edit the membership details and set the "End date" to:

```
12/31/2026 05:00:00 PM
```

Click "Add".
