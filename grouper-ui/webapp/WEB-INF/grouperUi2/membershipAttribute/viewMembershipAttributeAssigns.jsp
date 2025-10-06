<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- show the add member button for privileges --%>
            <c:set target="${grouperRequestContainer.groupContainer}" property="showAddMember" value="false" />

            <%@ include file="../subject/subjectHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>
                
                <c:set var="grouperCurrentTab" value="none" />
                <%@ include file="../subject/subjectTabs.jsp" %>
                
                <div id="membershipAttributeAssignments">
                  <%@ include file="membershipAttributeAssignmentSection.jsp"%>
                </div>

              </div>
            </div>