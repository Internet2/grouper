---
title: "Grouper Training - Use cases - Lesson: Policy groups and dynamic application permissions"
space: Grouper
pageId: 28544809
version: 5
lastUpdated: 2026-07-12T15:26:29.590Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544809/Grouper+Training+-+Use+cases+-+Lesson+Policy+groups+and+dynamic+application+permissions
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

## Learning Objectives

- Understand how to use policy groups with dynamic application specific roles
- Implement delegated access control
- Configure attestation

## Hands On

### Create a cognos application and policy

Use the Application template to create the cognos application folder and group set in the app folder.

- Navigate to the *app* folder
- create a new application (More actions -> New template -> Application)
  
  - Key: `cognos`
  - Description: `Manage policy roles for Cognos application`

Use the Policy template to create two new policy groups in app:cognos:service:policy

- Navigate to the *service:policy* subfolder
- Create new policy template:
  
  - Key: `cg_fin_report_reader`
  - Description: `Report Reader Access Policy`
- Create new policy template:
  
  - Key: `cg_fin_report_writer`
  - Description: `Report Writer Access Policy`

### Implement report reader access policy

All Budget and Finance (dept. 10810) employees have read access to finance reports. Implement the reader policy.

- Add *basis:hr:employee:dept:10810:staff* (name *Budget & Finance staff*) to *cg_fin_report_reader_allow*

### Implement report writer access policy

Only employees authorized by the Finance Manager have access to write reports. This policy will require an application specific reference group. It will be used as an access control list managed by the Finance Manager.

- Navigate to subfolder *service:ref*
- Create group finance_report_writer
- Assign object type:
  
  - Type name: *ref*
  - Type: *Yes*
  - Data owner: `Finance Manager`
  - Member description: `Employees authorized by the Finance Manager have access to write reports`
- Add finance_report_writer to cg_fin_report_write_allow

### Delegate access control to Finance Manager

The Finance Manager will directly manage the finance_report_writer access control list.

- Create new group under ref:role
  
  - Name: `Finance Manager`
  - Id: `financeManager`
- Assign object type: *ref*
- Add Daniel Riddle (`driddle`) to *ref:role:financeManager*

The Finance Manager will directly manage the finance_report_writer access control list.

- Navigate to *app:cognos:service:ref:finance_report_writer*
- Go to the privilege tag
- Add member "Finance Manager", and grant UPDATE and READ
- Review privileges on finance_report_writer
- Trace privileges for Daniel Riddle (Choose action -> Actions -> Trace privileges)

### Test privileges

- Open a private browser window
- Log in with username `driddle` and password `password`
- Note *Groups I manage* on the main page
- Add Carrie Campbell (`ccampbe2`) to *finance_report_writer*
- Go back to banderson browser
- Review audit log for finance_report_writer (finance_report_writer -> More actions -> View audit log)

### Add attestation to finance_report_writer

Add attestation requirement for finance_report_writer.

- Navigate to finance_report_writer
- Add attestation (More actions -> Attestation -> Attestation actions -> Edit attestation settings)
  
  - Attestation: *Yes*
  - Default remaining options

### Test attestation

(As driddle)

- Log back in as `driddle` (if the window was closed)
- Navigate to *finance_report_writer*
- Click button *Mark group as reviewed*

(As banderson)

- Go back to the banderson window
- Review attestation audit log. (finance_report_writer -> More actions -> Attestation -> View audit log)
