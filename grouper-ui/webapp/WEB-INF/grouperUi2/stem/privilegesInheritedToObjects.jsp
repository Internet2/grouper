<%@ include file="../assetsJsp/commonTaglib.jsp"%>
${grouper:titleFromKeyAndText('stemInheritedPrivilegesPageTitle', grouperRequestContainer.stemContainer.guiStem.stem.displayName)}

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />
            
            <c:if test="${grouperRequestContainer.stemContainer.canUpdatePrivilegeInheritance}">
              <%-- show the add member button for privileges --%>
              <c:set target="${grouperRequestContainer.stemContainer}" property="showAddMember" value="true" />
              <c:set target="${grouperRequestContainer.stemContainer}" property="showAddInheritedPrivileges" value="true" />
            </c:if>
            <%@ include file="stemHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12 tab-interface">
                <c:set var="grouperCurrentTab" value="none" />
                <%@ include file="../stem/stemTabs.jsp" %>
                <p class="lead">${textContainer.text['stemPrivilegesInheritedDecription'] }</p>
                <script>
                  //set this flag so we get one confirm message on this screen
                  confirmedChanges = false;
                </script>
                <div id="privilegesInheritedResultsId">
                </div>                
              </div>
            </div>
