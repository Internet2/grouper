---
title: "Migrate a jar-deployed hook to a compiled GSH hook"
space: Grouper
pageId: 28555829
version: 2
lastUpdated: 2026-07-01T05:37:22.708Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555829/Migrate+a+jar-deployed+hook+to+a+compiled+GSH+hook
---

Note: a hook is the one case where the source is *already* Java (in a deployed jar), not Groovy. "Migrating" here means moving that source into a DB-stored, hot-reloadable compiled GSH template so it no longer requires a jar build and container redeploy. Jar-deployed hooks keep working through v7/v8; v9/v10 removes the jar path, so move them to compiled GSH hooks before a v9 upgrade.

## Today vs compiled

Today a custom hook is a class in a jar, registered by fully-qualified class name per hook domain:

hooks.group.class = edu.institution.grouper.hooks.MyGroupHook To convert: put the same class source into a compiled GSH template (`templateType=hook`, `templateMode=compiled`) extending the same hook base class, then register it by config id (the property is plural and comma-separated) instead of by jar FQN:

hooks.group.gshTemplateConfigIds = myGroupHookTemplate The class is otherwise unchanged — same base class, same event method signatures. Compiled hooks run at full JIT speed and coexist with any remaining jar-deployed hooks on the same domain during v7/v8. (Resolved compiled hook instances are cached briefly per JVM, so edits take effect within about 30 seconds, or immediately on a cache clear.)

## The hook class (same in jar or compiled template)

package edu.institution.grouper.hooks; import edu.internet2.middleware.grouper.hooks.GroupHooks; import edu.internet2.middleware.grouper.hooks.beans.HooksContext; import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean; import edu.internet2.middleware.grouper.hooks.logic.HookVeto; public class MyGroupHook extends GroupHooks { @Override public void groupPreInsert(HooksContext hooksContext, HooksGroupBean preInsertBean) { if (preInsertBean.getGroup().getExtension().contains(" ")) { throw new HookVeto("group.extension.noSpaces", "Group extension cannot contain spaces"); } } }
