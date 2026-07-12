---
title: "Grouper progress UI with time (lightweight) for long running events"
space: GrIntDev
pageId: 48794057
version: 10
lastUpdated: 2026-07-12T06:46:29.311Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48794057/Grouper+progress+UI+with+time+lightweight+for+long+running+events
---

## Examples

1. Composite
2. Edit stem (change name)
3. Move stem
4. Copy stem
5. Audits
6. Loader diagnostics

## Add progress bean and unique id to a container (or reuse one)

```
  private ProgressBean progressBean = null;
  
  public ProgressBean getProgressBean() {
    return progressBean;
  }
  
  public void setProgressBean(ProgressBean progressBean) {
    this.progressBean = progressBean;
  }

  /**
   * for progress
   */
  private String uniqueId;

  /**
   * for progress
   * @return
   */
  public String getUniqueId() {
    return uniqueId;
  }

  /**
   * for progress
   * @param uniqueId
   */
  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }

```

## Config the progress settings

```
# if should use threads
# {valueType: "boolean"}
grouperUi.editStem.useThread = true
 
# seconds to wait until progress starts
# {valueType: "integer"}
grouperUi.editStem.progressStartsInSeconds = 10
 
# seconds to wait until refresh
# {valueType: "integer"}
grouperUi.editStem.progressRefreshSeconds = 5
 
# pause in action to test the progress, set to 60 perhaps
# {valueType: "integer"}
grouperUi.editStem.pauseInActionSeconds = 0
   
```

## Configure externalized text

```
# exception in stem edit
stemEditProgressSubheadingInProgress = The folder edit is in progress (running for ${grouperRequestContainer.stemContainer.progressBean.elapsedSeconds} seconds), please wait<br />Note: this action will run in the background if you nagivate away.
 
# subheading stem edit
stemEditProgressSubheading = ${textContainer.text[grouperRequestContainer.stemContainer.progressBean.complete ? 'stemEditSuccess' : 'stemEditProgressSubheadingInProgress']}
 
# exception in stem edit
stemEditException = Error: the folder edit could not be completed
    
```

## Keep a cache of state

Add a progress bean and

```
  /**
   * keep an expirable cache of progress for 5 hours (longest an import is expected).  This has multikey of session id and some random uuid
   * uniquely identifies this action as opposed to other actions in other tabs
   */
  private static ExpirableCache<MultiKey, StemContainer> threadProgress = new ExpirableCache<MultiKey, StemContainer>(300);
```

## Execute in thread

```
          final ProgressBean progressBean = new ProgressBean();
        progressBean.setStartedMillis(System.currentTimeMillis());
   
        StemContainer stemContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer();
        stemContainer.setProgressBean(progressBean);
   
        GuiStem guiStem = new GuiStem(stem);
        stemContainer.setGuiStem(guiStem);
         
        StemSave stemSave = new StemSave(GROUPER_SESSION);
        
        stemContainer.setStemSave(stemSave);
        final Stem STEM = stem;
            
        GrouperCallable<Void> grouperCallable = new GrouperCallable<Void>("stemEdit") {
           
          @Override
          public Void callLogic() {
            try {
   
LOGIC
   
              GrouperUtil.sleep(1000L * GrouperUiConfig.retrieveConfig().propertyValueInt("grouperUi.editStem.pauseInActionSeconds", 0));
               
            } catch (RuntimeException re) {
              progressBean.setHasException(true);
              progressBean.setException(re);
              // log this since the thread will just end and will never get logged
              LOG.error("error", re);
            } finally {
              // we done
              progressBean.setComplete(true);
            }
            return null;
          }
        };    
          
        // see if running in thread
        boolean useThreads = GrouperUiConfig.retrieveConfig().propertyValueBooleanRequired("grouperUi.composite.useThread");
      
        if (useThreads) {
            
          GrouperFuture<Void> grouperFuture = GrouperUtil.executorServiceSubmit(GrouperUtil.retrieveExecutorService(), grouperCallable);
            
          Integer waitForCompleteForSeconds = GrouperUiConfig.retrieveConfig().propertyValueInt("grouperUi.composite.progressStartsInSeconds");
      
          GrouperFuture.waitForJob(grouperFuture, waitForCompleteForSeconds);
               
        } else {
          grouperCallable.callLogic();
        }
   
        String sessionId = request.getSession().getId();
         
        // uniquely identifies this composite as opposed to other composite in other tabs
        String uniqueStemEditId = GrouperUuid.getUuid();
     
        stemContainer.setUniqueId(uniqueStemEditId);
         
        MultiKey reportMultiKey = new MultiKey(sessionId, uniqueStemEditId);
         
        threadProgress.put(reportMultiKey, stemContainer);
   
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
            "/WEB-INF/grouperUi2/stem/stemEditWrapper.jsp"));
          
        stemEditStatusHelper(sessionId, uniqueStemEditId);
        
```

## Logic for progress

```
  /**
   * get the status of a stem edit
   * @param request
   * @param response
   */
  public void stemEditStatus(HttpServletRequest request, HttpServletResponse response) {
    String sessionId = request.getSession().getId();
    String uniqueId = request.getParameter("uniqueId");
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    GrouperSession grouperSession = GrouperSession.start(loggedInSubject);

    try {
      stemEditStatusHelper(sessionId, uniqueId);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
   
  /**
   * get the status of a progress screen
   */
  private void stemEditStatusHelper(String sessionId, String uniqueId) {
     
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
     
    debugMap.put("method", "stemEditStatusHelper");
    debugMap.put("sessionId", GrouperUtil.abbreviate(sessionId, 8));
    debugMap.put("uniqueId", GrouperUtil.abbreviate(uniqueId, 8));
   
    long startNanos = System.nanoTime();
    try {
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
   
      MultiKey multiKey = new MultiKey(sessionId, uniqueId);
       
      StemContainer stemContainer = threadProgress.get(multiKey);

   
      if (stemContainer != null) {
         
        GrouperRequestContainer.retrieveFromRequestOrCreate().setStemContainer(stemContainer);

        //show the report screen
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#id_"+uniqueId,
            "/WEB-INF/grouperUi2/stem/stemEditProgress.jsp"));
         
        ProgressBean progressBean = stemContainer.getProgressBean();
     
        debugMap.put("elapsedSeconds", progressBean.getElapsedSeconds());
        
        Stem stem = stemContainer.getGuiStem().getStem();
        StemSave stemSave = stemContainer.getStemSave();
        
        // endless loop?
        if (progressBean.isThisLastStatus()) {
          return;
        }
         
        if (progressBean.isHasException()) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
              TextContainer.retrieveFromRequest().getText().get("stemEditException")));
          // it has an exception, leave it be
          threadProgress.put(multiKey, null);
          
          if (progressBean.getException() != null) {

            if (GrouperUiUtils.vetoHandle(guiResponseJs, progressBean.getException())) {
              return;
            }

            //dont change screens
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("stemEditError") 
                + ": " + GrouperUtil.xmlEscape(progressBean.getException().getMessage(), true)));

          }
          return;
        }
        // kick it off again?
        debugMap.put("complete", progressBean.isComplete());
        if (!progressBean.isComplete()) {
          int progressRefreshSeconds = GrouperUiConfig.retrieveConfig().propertyValueInt("grouperUi.editStem.progressRefreshSeconds");
          progressRefreshSeconds = Math.max(progressRefreshSeconds, 1);
          progressRefreshSeconds *= 1000;
          guiResponseJs.addAction(GuiScreenAction.newScript("setTimeout(function() {ajax('../app/UiV2Stem.stemEditStatus?uniqueId=" + uniqueId + "')}, " + progressRefreshSeconds + ")"));
        } else {
          
          //go to the view stem screen
          guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Stem.viewStem&stemId=" + stem.getId() + "')"));
      
          //lets show a success message on the new screen
          if (stemSave.getSaveResultType() == SaveResultType.NO_CHANGE) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
                TextContainer.retrieveFromRequest().getText().get("stemEditNoChangeNote")));
          } else {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
                TextContainer.retrieveFromRequest().getText().get("stemEditSuccess")));
          }

          // it is complete, leave it be
          threadProgress.put(multiKey, null);
           
        }
      } else {
        //go back to main screen
        guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Main.indexMain');"));
      }
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(re));
      throw re;
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("tookMillis", (System.nanoTime()-startNanos)/1000000);
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }

    }
  }
```

## Progress JSP wrapper is specific to the ID used

```
<%@ include file="../assetsJsp/commonTaglib.jsp"%>
<div id="id_${grouperRequestContainer.groupContainer.uniqueId}">
</div>
```

Progress JSP progress page

```
<%@ include file="../assetsJsp/commonTaglib.jsp"%>
 
             <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.parentUuid}" />

            <div class="bread-header-container">
              ${grouperRequestContainer.stemContainer.guiStem.breadcrumbs}

              <div class="page-header blue-gradient">
                <h1> <i class="fa fa-folder"></i> ${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.stem.displayExtension)}
                <br /><small>
                <c:choose>
                    <c:when test="${grouperRequestContainer.stemContainer.progressBean.complete}">
                      ${textContainer.text['stemEditSuccess']}
                    </c:when>
                    <c:otherwise>
                      <i class="fa fa-spinner fa-spin"></i> ${textContainer.text['stemEditProgressSubheading']}
                    </c:otherwise>
                  </c:choose></small>
 
                </h1>
              </div>
           </div>
 
```
