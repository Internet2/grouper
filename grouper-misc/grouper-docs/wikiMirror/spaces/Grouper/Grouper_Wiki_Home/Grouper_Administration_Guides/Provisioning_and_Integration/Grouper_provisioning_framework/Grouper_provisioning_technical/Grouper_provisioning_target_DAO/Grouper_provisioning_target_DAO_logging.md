---
title: "Grouper provisioning target DAO logging"
space: Grouper
pageId: 28559936
version: 2
lastUpdated: 2021-03-25T17:46:32.869Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28559936/Grouper+provisioning+target+DAO+logging
---

The target DAO can log low level information to the diagnostics screen (or other things).

The DAO should implement these methods

```
  /**
   * start logging the source low level actions
   */
  public void loggingStart();

  /**
   * stop logging and get the output
   */
  public String loggingStop();

```

Append information in requests and responses and separate with newlines

You can use inheritable threadlocals or whatever, e.g.

```
  @Override
  public void loggingStart() {
    LdapSessionUtils.logStart();
  }

  @Override
  public String loggingStop() {
    return LdapSessionUtils.logEnd();
  }

```
