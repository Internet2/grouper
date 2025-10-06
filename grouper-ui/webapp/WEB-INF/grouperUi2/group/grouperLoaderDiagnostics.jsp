<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <%-- show the add member button for privileges --%>
            <c:set target="${grouperRequestContainer.groupContainer}" property="showAddMember" value="false" />
            <%@ include file="groupHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <c:set var="grouperCurrentTab" value="none" />
                <%@ include file="../group/groupTabs.jsp" %>
                <div class="row-fluid">
                  <div class="lead span10">${textContainer.text['grouperLoaderDiagnosticsHeader'] }</div>
                  <div class="span2" id="grouperLoaderMoreActionsButtonContentsDivId">
                    <%@ include file="grouperLoaderMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
                
                <a class="btn" role="button" 
                  onclick="ajax('../app/UiV2GrouperLoader.loaderDiagnosticsRun?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                  >${textContainer.text['grouperLoaderDiagnosticsRunButton'] }</a> 
                <br /><br />
                <div id="grouperLoaderDiagnosticsResults"></div>

              </div>
            </div>
