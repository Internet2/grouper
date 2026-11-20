---
title: "Grouper Training - Use cases - Lesson 13: Entity data fields"
space: Grouper
pageId: 28544358
version: 18
lastUpdated: 2026-11-18T19:39:24.849Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544358/Grouper+Training+-+Use+cases+-+Lesson+13+Entity+data+fields
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

**Assumes 501.1 is done**

In this lesson, anything you type is in a box with a copy button on the right. Buttons, menus, and labels to click or select are in double quotes.

## Run the SIS query in the sis database

Run this SQL in the sis database to see the data the data provider will load:

```
select distinct se.person_id, se.role, sc.dept_id from sis_enrollment se, sis_courses sc where se.course_id = sc.course_id and term = '2024SP'
```

## Make a data field for SIS role

1. In the left menu, click "Miscellaneous", then click "Entity data fields".
2. Click "Data fields".
3. Click "Data field actions" → "Add data field".
4. "Config id":
  
  
  ```
  sis_role
  ```
5. "Field aliases":
  
  
  ```
  sis_role
  ```
6. "Field privacy realm": select "sis_programs".
7. "Description html":
  
  
  ```
  Role of person in current term (in a department), e.g. student or instructor
  ```
8. "Data owner html":
  
  
  ```
  Registrar
  ```
9. "How to get access html":
  
  
  ```
  For active faculty and staff, <a href="https://some.form/somewhere">fill out the student data request form</a> and take FERPA training
  ```
10. "Examples html":
  
  
  ```
  entity.hasRow('sis_departments', " sis_role == 'student' and sis_department == 'ANTH' ")
  ```
11. "Field data structure": select "rowColumn".
12. "Field data assignable to": select "individuals".
13. Click "Submit".

## Make a data field for SIS department

1. In the left menu, click "Miscellaneous", then click "Entity data fields".
2. Click "Data fields".
3. Click "Data field actions" → "Add data field".
4. "Config id":
  
  
  ```
  sis_department
  ```
5. "Field aliases":
  
  
  ```
  sis_department
  ```
6. "Field privacy realm": select "sis_programs".
7. "Description html":
  
  
  ```
  Department of person (with role) in current term, e.g. ANTH
  ```
8. "Data owner html":
  
  
  ```
  Registrar
  ```
9. "How to get access html":
  
  
  ```
  For active faculty and staff, <a href="https://some.form/somewhere">fill out the student data request form</a> and take FERPA training
  ```
10. "Examples html":
  
  
  ```
  entity.hasRow('sis_departments', " sis_role == 'student' and sis_department == 'ANTH' ")
  ```
11. "Field data structure": select "rowColumn".
12. "Field data assignable to": select "individuals".
13. Click "Submit".

## Make a data row for SIS departments

1. In the left menu, click "Miscellaneous", then click "Entity data fields".
2. Click "Data rows".
3. Click "Data row actions" → "Add data row".
4. "Config id":
  
  
  ```
  sis_departments
  ```
5. "Description html":
  
  
  ```
  Department of person (with role) in current term, e.g. ANTH
  ```
6. "Data owner html":
  
  
  ```
  Registrar
  ```
7. "How to get access html":
  
  
  ```
  For active faculty and staff, <a href="https://some.form/somewhere">fill out the student data request form</a> and take FERPA training
  ```
8. "Examples html":
  
  
  ```
  entity.hasRow('sis_departments', " sis_role == 'student' and sis_department == 'ANTH' ")
  ```
9. "Row aliases":
  
  
  ```
  sis_departments
  ```
10. "Row privacy realm": select "sis_programs".
11. "Store history of row": select "false".
12. "Row number of data fields": select "2".
13. "1 - Col data field config id": select "sis_role". "1 - Row key field": select "true".
14. "2 - Col data field config id": select "sis_department". "2 - Row key field": select "true".
15. Click "Submit".

## SIS department query

1. In the left menu, click "Miscellaneous", then click "Entity data fields".
2. Click "Data provider queries".
3. Click "Data provider query actions" → "Add data provider query".
4. "Config id":
  
  
  ```
  sis_departments
  ```
5. "Provider config id": select "sis_data".
6. "Provider query type": select "sql".
7. "Provider query sql config id": select "sis".
8. "Provider query sql query":
  
  
  ```
  select distinct se.person_id, se.role, sc.dept_id from sis_enrollment se, sis_courses sc where se.course_id = sc.course_id and term = '2024SP'
  ```
9. "Provider query data structure": select "row".
10. "Provider query row config id": select "sis_departments".
11. "Provider query subject id attribute":
  
  
  ```
  person_id
  ```
12. "Provider query subject id type": select "subjectId".
13. "Provider query subject source id": select "eduLDAP".
14. "Provider query number of data fields": select "2".
15. "1 - Provider data field config id": select "sis_department". "1 - Provider mapping type": select "attribute".
16. "1 - Provider data field attribute":
  
  
  ```
  dept_id
  ```
17. "2 - Provider data field config id": select "sis_role". "2 - Provider mapping type": select "attribute".
18. "2 - Provider data field attribute":
  
  
  ```
  role
  ```
19. Click "Submit".

## Daemon for SIS data provider

1. In the left menu, click "Miscellaneous", then click "Daemon jobs".
2. Click "Daemon actions" → "Add daemon".
3. "Config id":
  
  
  ```
  dataProviderSIS
  ```
4. "Daemon type": select "Data provider full sync".
5. "Data provider config id": select "sis_data".
6. "Quartz cron":
  
  
  ```
  39 41 7 * * ?
  ```
7. Click "Submit".

## SIS data provider run

1. In the left menu, click "Miscellaneous", then click "Daemon jobs".
2. Type this in the filter box and click "Apply filter":
  
  
  ```
  OTHER_JOB_dataProviderSIS
  ```
3. On the row for OTHER_JOB_dataProviderSIS, click "Job actions" → "Run job now".
4. Click "Apply filter" every few seconds until the job shows as complete.

## Edit policy group

1. Go to the group "app:OneDrive:service:policy:OneDriveUser".
2. Click "Group actions" → "Loader".
3. Click "Loader actions" → "Edit loader configuration".
4. "Entity JEXL script": replace the script with this policy: (faculty or staff in arts and sciences) or students, and either not required to take training or has been trained.
  
  
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
5. In "Enter an entity name or ID", paste this, pick "Jason Berger" (chemistry student) from the list, and click "Analyze":
  
  
  ```
  Jason Berger
  ```
6. Do the same for Lisa Bernard (a student not studying chemistry):
  
  
  ```
  Lisa Bernard
  ```
7. Now replace the "Entity JEXL script" with this policy: (faculty or staff in arts and sciences) or chemistry students, and either not required to take training or has been trained.
  
  
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
8. Analyze Jason Berger and Lisa Bernard again and compare the results.
9. Click "Save".
10. Click the "Members" tab and look at the members of the group.
