<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:titleFromKeyAndText('subjectProvisioningPageTitle', grouperRequestContainer.subjectContainer.guiSubject.subject.name)}

            <%-- show the add member button for privileges --%>
            <c:set target="${grouperRequestContainer.stemContainer}" property="showAddMember" value="true" />
            
            <%@ include file="../subject/subjectHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>

                <c:set var="grouperCurrentTab" value="none" />
                <%@ include file="../subject/subjectTabs.jsp" %>
                
                <div class="row-fluid">
			      <div class="lead span9">${textContainer.text['provisioningSubjectProvisioningTitle'] }</div>
			      <div class="span3" id="grouperProvisioningSubjectMoreActionsButtonContentsDivId">
			        <%@ include file="provisioningSubjectMoreActionsButtonContents.jsp"%>
			      </div>
			    </div>
			    
			    <%-- <div class="row-fluid">
			      <div class="span9"> <p>${textContainer.text['provisioningGroupProvisioningDescription'] }</p></div>
			    </div> --%>

                <%@ include file="provisioningSubjectProvisionersTableHelper.jsp" %>

              </div>
            </div>
