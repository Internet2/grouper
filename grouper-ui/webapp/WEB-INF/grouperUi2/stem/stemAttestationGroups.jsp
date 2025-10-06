<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />

            <%@ include file="stemHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12 tab-interface">
                <c:set var="grouperCurrentTab" value="none" />
                <%@ include file="../stem/stemTabs.jsp" %>
              </div>
              <div>
                <div class="row-fluid">
                  <div class="lead span9">${textContainer.text['stemAttestationGroupsTitle'] }</div>
                  <div class="span3" id="stemAttestationMoreActionsButtonContentsDivId">
                    <%@ include file="stemAttestationMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
                <div class="row-fluid">
                  <div class="span12">
                    <p style="margin-top: 0em; margin-bottom: 1em">${textContainer.text['stemAttestationGroupsDescription']}</p>
                  </div>
                </div>
              </div>
            </div>

<%@ include file="../group/groupAttestationListOfGroupsHelper.jsp"%>
