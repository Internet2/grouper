---
title: "Grouper Training - Use cases - Lesson 02: Basis and reference groups part 2"
space: Grouper
pageId: 28544245
version: 22
lastUpdated: 2026-07-12T15:26:15.123Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544245/Grouper+Training+-+Use+cases+-+Lesson+02+Basis+and+reference+groups+part+2
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

# Learning Objectives (continued)

- Understand the difference between reference groups and basis groups
- Create and manage reference and basis groups
- Implement subject attribute lifecycle requirements

## Exercise: All Students Reference Group (continued)

### Include students with no class year

Not all students have class years assigned. This includes part-time students, employees taking course, and non-matriculated students.

Fortunately, data about these students is available in the SIS, and basis groups have already been created for us.

- Add group **
  ```
  basis:sis:prog_status:year:ac:no_year
  ```
  
   (name Active No Year) to *ref:student:students*

How many students are there now?

### Include exchange students

Exchange students from your sister school can take classes, but never have official records in the SIS. However, they do have a local NetID and a basis group is maintained or them.

- Add group **
  ```
  basis:sis:prog_status:all:es
  ```
  
   (name Exchange Student) to *ref:student:students*

How many students are there now?

### Include ad-hoc transfer students

Students who transfer to your campus often need access well ahead of SIS data being fully updated.

- In folder *ref:student*, create group:
  
  
  
  - name: ``
    ```
    Transfer Student
    ```
  - id: ``
    ```
    transfer_student
    ```
  - description: ``
    ```
    Students recently transferred but not yet in SIS
    ```
- Add the *manual* object type to this group
  
  
  
  - Navigate to group. Group actions → Types
  - Type name: `manual`
  - Type: Yes, has direct type configuration
  - Data owner: ``
    ```
    Registrar
    ```
  - Member description: ``
    ```
    Ad-hoc recent transfer students not yet in SIS
    ```
- Add the following subjects to transfer_student
  
  
  ```
  whawkins
  hyoung
  jmejia
  ```
- Add
  
  
  ```
  ref:student:transfer_student
  ```
  
  to “students” group

How many students are there now?

The number of students did not go up by 3 as you might have expected. Why? One of the transfer students was already a member of students.

- Trace the membership of jmejia (Jennifer Mejia) to show the user’s multiple memberships.
- Click on the Membership timeline to see events

### Include Leave of Absence students

Students take a leave of absence for a variety of reasons. These students may or may not return, but retain student access for an extend period of time. Basis groups for leave of absence students already exist.

- Add **
  ```
  basis:sis:prog_status:all:la
  ```
  
  ** (Leave of Absence) to students

How many students are there now?

### Visualization: What do you mean by “student”?

Review the students reference group by using group visualization

Visually trace the membership of jmejia by adding it to the Member ID and generate visualization again

The students reference group is used in access policy for student services. Being a “student” means access to a broad array of student services. This institutionally meaningful cohort is well defined, easily understood, and capable of being extended in a rational way.
