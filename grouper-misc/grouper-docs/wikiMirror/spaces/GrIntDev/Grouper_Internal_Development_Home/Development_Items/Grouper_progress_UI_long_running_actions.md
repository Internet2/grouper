---
title: "Grouper progress UI long running actions"
space: GrIntDev
pageId: 48792704
version: 6
lastUpdated: 2026-07-12T07:01:13.658Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792704/Grouper+progress+UI+long+running+actions
---

This page is for Grouper developers who want to add a progress page for long running tasks

## Examples

1. Import members
2. Provisioning diagnostics
3. Run GSH template

## Keep a cache of state

```
  /**
   * keep an expirable cache of import progress for 5 hours (longest an import is expected).  This has multikey of session id and some random uuid
   * uniquely identifies this import as opposed to other imports in other tabs
   */
  private static ExpirableCache<MultiKey, GroupImportContainer> importThreadProgress = new ExpirableCache<MultiKey, GroupImportContainer>(300);

```

For new tasks, make ids and set in the cache

```
      String sessionId = request.getSession().getId();
      
      // uniquely identifies this task as opposed to other tasks in other tabs
      String uniqueImportId = GrouperUuid.getUuid();
  
      MultiKey reportMultiKey = new MultiKey(sessionId, uniqueImportId);
      
      importThreadProgress.put(reportMultiKey, groupImportContainer);

```

## Keep progress bean in the state container somewhere

```
public class GshTemplateExec {
  
  /**
   * have a progress bean to be able to communicate progress to the UI
   */
  private ProgressBean progressBean = new ProgressBean();
  
  /**
   * have a progress bean to be able to communicate progress to the UI
   * @return the progressBean
   */
  public ProgressBean getProgressBean() {
    return this.progressBean;
  }

```

## Update progress as task runs

```
      // total records
      progressBean.setProgressTotalRecords(GrouperUtil.length(groups) * GrouperUtil.length(subjectSet));

      // progress
      progressBean.addProgressCompleteRecords(GrouperUtil.length(subjectSet) - GrouperUtil.length(subjectList));

      // done
      progressBean.setProgressCompleteRecords(progressBean.getProgressTotalRecords());

```

## Execute in thread

```
      GrouperCallable<Void> grouperCallable = new GrouperCallable<Void>("groupImportMembers") {

        @Override
        public Void callLogic() {
          try {

            groupImportContainer.getProgressBean().setStartedMillis(System.currentTimeMillis());

            UiV2GroupImport.this.groupImportSubmitHelper(loggedInSubject, groupImportContainer, groups, subjectSet, 
                listInvalidSubjectIdsAndRow, removeMembers, importReplaceMembers, bulkAddOption, fileName[0], (List<CSVRecord>)csvEntriesObject[0]);
          } catch (RuntimeException re) {
            groupImportContainer.getProgressBean().setHasException(true);
            // log this since the thread will just end and will never get logged
            LOG.error("error", re);
          } finally {
            // we done
            groupImportContainer.getProgressBean().setComplete(true);
          }
          return null;
        }
      };      
      
      // see if running in thread
      boolean useThreads = GrouperUiConfig.retrieveConfig().propertyValueBooleanRequired("grouperUi.import.useThread");
      debugMap.put("useThreads", useThreads);

      if (useThreads) {
        
        GrouperFuture<Void> grouperFuture = GrouperUtil.executorServiceSubmit(GrouperUtil.retrieveExecutorService(), grouperCallable);
        
        Integer waitForCompleteForSeconds = GrouperUiConfig.retrieveConfig().propertyValueInt("grouperUi.import.progressStartsInSeconds");
        debugMap.put("waitForCompleteForSeconds", waitForCompleteForSeconds);

        GrouperFuture.waitForJob(grouperFuture, waitForCompleteForSeconds);
        
        debugMap.put("threadAlive", !grouperFuture.isDone());

      } else {
        grouperCallable.callLogic();
      }
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/groupImport/groupImportReportWrapper.jsp"));
      
      groupImportReportStatusHelper(sessionId, uniqueImportId);

```

## Progress JSP is specific to the ID used

```
<%@ include file="../assetsJsp/commonTaglib.jsp"%>
<div id="id_${grouperRequestContainer.groupImportContainer.uniqueImportId}">
</div>
```
