<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- show the add member button for privileges --%>
            <c:set target="${grouperRequestContainer.stemContainer}" property="showAddMember" value="true" />
            
            <%@ include file="../subject/subjectHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>

                <c:set var="grouperCurrentTab" value="none" />
                <%@ include file="../subject/subjectTabs.jsp" %>
                
                <div class="row-fluid">
			      <div class="lead span12">${textContainer.text['provisioningMembershipProvisioningTitle'] }</div>
			    </div>
			    
			    <div class="row-fluid">   
			     <div class="span9"> <p>${textContainer.text['provisioningSubjectGroupMembershipProvisioningDescription'] }</p></div>
			    </div>

                <%@ include file="provisioningGroupOrSubjectMembershipDetailsHelper.jsp" %>

              </div>
            </div>
        