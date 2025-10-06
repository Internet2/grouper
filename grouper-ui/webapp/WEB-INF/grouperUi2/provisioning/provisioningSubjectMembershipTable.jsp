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
			      <div class="lead span9">${textContainer.text['provisioningMembershipProvisioningTitle'] }</div>
			      <div class="span3" id="grouperProvisioningSubjectMembershipMoreActionsButtonContentsDivId">
			        <%@ include file="provisioningSubjectMembershipMoreActionsButtonContents.jsp"%>
			      </div>
			    </div>
			    
			    <div class="row-fluid">
			    	<div class="span9">
			    		${textContainer.text['provisioningMembershipProvisioningGroupFor']}
			    		${grouperRequestContainer.groupContainer.guiGroup.shortLink}
			    	</div>
			    </div>
			    
                <%@ include file="provisioningSubjectMembershipTableHelper.jsp" %>

              </div>
            </div>
        