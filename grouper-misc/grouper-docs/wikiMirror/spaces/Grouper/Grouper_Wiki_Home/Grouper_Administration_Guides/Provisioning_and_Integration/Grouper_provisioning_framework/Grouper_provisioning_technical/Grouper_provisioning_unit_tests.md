---
title: "Grouper provisioning unit tests"
space: Grouper
pageId: 28554526
version: 3
lastUpdated: 2026-07-01T05:40:15.653Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554526/Grouper+provisioning+unit+tests
---

This should be run before each release.

1. Start up Docker
2. Make sure this is set in grouper.properties
  
  
  ```
  # if the sql provisioning tests should be included when running all tests (default false)
  # {valueType: "boolean", required: true}
  junit.test.sqlProvisioning = true
  
  # make sure docker is there
  # {valueType: "boolean", required: true}
  junit.test.ldap.dinkel = true
  
  # http port to look for to see if tomcat has started, e.g. 8500
  # {valueType: "integer", defaultValue: "8080"}
  junit.test.tomcat.port = 8400
  
  
  ```
3. Make sure this is set in grouper.hibernate.properties
  
  
  ```
  grouper.is.mockServices = false
  grouper.is.ui = true
  
  
  ```
4. Remove local or database configs that might conflict (LDAP external system or subject source)
5. Start a UI
6. Run this test:
  
  
  ```
  AllProvisioningTestsManual
  ```
