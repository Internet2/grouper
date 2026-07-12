---
title: "Grouper Training - Use cases - Lesson 13: Entity data fields"
space: Grouper
pageId: 28544358
version: 17
lastUpdated: 2026-04-22T02:47:32.746Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544358/Grouper+Training+-+Use+cases+-+Lesson+13+Entity+data+fields
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

**Assumes 501.1 is done**

## Run the SIS query in sis database

SQL

```
select distinct se.person_id, se.role, sc.dept_id from sis_enrollment se, sis_courses sc where se.course_id = sc.course_id and term = '2024SP'
```

## Make a data field for SIS role

Miscellaneous → Entity data fields → Data fields → Add data field

- Config id: sis_role
- Field aliases: sis_role
- Privacy realm: sis_programs
- Description: Role of person in current term (in a department), e.g. student or instructor
- Data owner: Registrar
- How to get access:
  
  
  ```
  For active faculty and staff, <a href="https://some.form/somewhere">fill out the student data request form</a> and take FERPA training
  ```
- Examples:
  
  
  ```
  entity.hasRow('sis_departments', " sis_role == 'student' and sis_department == 'ANTH' ")
  ```
- Field data structure: rowColumn
- Field data assignable to: individuals

## Make a data field for SIS department

Miscellaneous → Entity data fields → Data fields → Add data field

- Config id: sis_department
- Field aliases: sis_department
- Privacy realm: sis_programs
- Description: Department of person (with role) in current term, e.g. ANTH
- Data owner: Registrar
- How to get access:
  
  
  ```
  For active faculty and staff, <a href="https://some.form/somewhere">fill out the student data request form</a> and take FERPA training
  ```
- Examples:
  
  
  ```
  entity.hasRow('sis_departments', " sis_role == 'student' and sis_department == 'ANTH' ")
  ```
- Field data structure: rowColumn
- Field data assignable to: individuals

## Make a data row for SIS departments

Miscellaneous → Entity data fields → Data rows → Add data row

- Config id: sis_departments
- Description: Department of person (with role) in current term, e.g. ANTH
- Data owner: Registrar
- How to get access:
  
  
  ```
  For active faculty and staff, <a href="https://some.form/somewhere">fill out the student data request form</a> and take FERPA training
  ```
- Examples:
  
  
  ```
  entity.hasRow('sis_departments', " sis_role == 'student' and sis_department == 'ANTH' ")
  ```
- Row aliases: sis_departments
- Privacy realm: sis_programs
- Store history of row: false
- Number of data fields: 2
- Data field 1:
  
  - Name: sis_role
  - Key field: true
- Data field 2:
  
  - Name: sis_department
  - Key field: true

## SIS department query

Miscellaneous → Entity data fields → Data provider queries → Add data provider query

- Config id: sis_departments
- Provider config id: sis_data
- Query type: SQL
- SQL config id: sis
- Query
  
  
  ```
  select distinct se.person_id, se.role, sc.dept_id from sis_enrollment se, sis_courses sc where se.course_id = sc.course_id and term = '2024SP'
  ```
- Data structure: row
- Row config id: sis_departments
- Subject ID attribute: person_id
- Subject ID type: subjectId
- Subject source ID: eduLDAP
- Number of data fields: 2
- Data field 1:
  
  - Config ID: sis_department
  - Mapping: attribute
  - Attribute: dept_id
- Data field 2:
  
  - Config ID: sis_role
  - Mapping: attribute
  - Attribute: role

## Daemon for SIS data provider

Miscellaneous → Daemon Jobs → Daemon actions → Add daemon

- Config ID: dataProviderSIS
- Daemon type: Data provider full sync
- Data provider config id: sis_data
- Schedule: 39 41 7 * * ?

## SIS data provider run

Filter for job name: OTHER_JOB_dataProviderSIS

## Edit policy group

In **app:OneDrive:service:policy:OneDriveUser** → Group actions → Loader → Loader actions → Edit loader configuration

- Change to these policies

- - (Faculty or staff in arts and sciences) or students, and either not required to take training or has been trained
    
    
    ```
    ( 
      /* faculty or staff in arts and sciences */
      entity.hasRow('hr_positions', " (role == 'faculty' or role == 'staff') and org_code == 'AS' ")
    
      /* or students */
      or entity.hasRow('sis_departments', " sis_role == 'student'  ")
    )
    and (
      /* either people who are not required to take privacy training */
      !entity.memberOf('ref:training:trainingRequired:privacy_cert_required')
    
      /* or people who are trained */
      or entity.memberOf('ref:training:trainingCompleted:privacy_certified')
    )
    /* do not allow people in global deny group */
    and !entity.memberOf('ref:iam:global_deny')
    ```
  - (Faculty or staff in arts and sciences) or chemistry student, and either not required to take training or has been trained
    
    
    ```
    ( 
      /* faculty or staff in arts and sciences */
      entity.hasRow('hr_positions', " (role == 'faculty' or role == 'staff') and org_code == 'AS' ")
    
      /* or chemistry students */
      or entity.hasRow('sis_departments', " sis_role == 'student' and sis_department == 'CHEM' ")
    )
    and (
      /* either people who are not required to take privacy training */
      !entity.memberOf('ref:training:trainingRequired:privacy_cert_required')
    
      /* or people who are trained */
      or entity.memberOf('ref:training:trainingCompleted:privacy_certified')
    )
    /* do not allow people in global deny group */
    and !entity.memberOf('ref:iam:global_deny')
    
    
    ```
  - Analyze for users:
    
    - Jason Berger (chem student)
    - Lisa Bernard (student not studying chem)
  - Save the loader
  - See members in group
