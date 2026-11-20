---
title: "Grouper Training - Use cases - Lesson 06: Configuration"
space: Grouper
pageId: 28544324
version: 13
lastUpdated: 2026-11-18T19:38:51.804Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544324/Grouper+Training+-+Use+cases+-+Lesson+06+Configuration
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

In this lesson, anything you type is in a box with a copy button on the right. Buttons, menus, and labels to click or select are in double quotes.

## Learning objectives

- Find the Grouper configuration files in the container
- Understand the base and override properties file hierarchy

## Hands on

### View config files

1. In a terminal on your VM, open a shell in the Grouper container:
  
  
  ```
  ./gte-shell
  ```
2. Go to the classes folder:
  
  
  ```
  cd classes
  ```
3. Show where you are:
  
  
  ```
  pwd
  ```
4. List the files:
  
  
  ```
  ls -al
  ```
5. View the base (default) Hibernate config. Press "q" to quit:
  
  
  ```
  less grouper.hibernate.base.properties
  ```
6. View the override config, which takes precedence over the base file (the config hierarchy). Press "q" to quit:
  
  
  ```
  less grouper.hibernate.properties
  ```
7. Exit the container shell:
  
  
  ```
  exit
  ```

The prompt changes back to the student user, e.g.:

```
[student@ip-172-31-4-56 ~]$
```
