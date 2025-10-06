<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.stemId}" />

            <%@ include file="../attributeDef/attributeDefHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>

                <c:set var="grouperCurrentTab" value="none" />
                <%@ include file="../attributeDef/attributeDefTabs.jsp" %>
                
                <div id="attributeDefAttributeAssignments">
                  <%@ include file="attributeDefAttributeAssignmentSection.jsp"%>
                </div>

              </div>
            </div>