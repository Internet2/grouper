<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<%-- for the new group or new stem button --%>
<input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

<%@ include file="../group/groupHeader.jsp" %>

<div class="row-fluid">
  <div class="span12">
    <div id="messages"></div>
        
    <div class="tab-interface">
      <c:set var="grouperCurrentTab" value="none" />
      <%@ include file="../group/groupTabs.jsp" %>
    </div>
    
    <div class="row-fluid">
      <div class="lead span9">${textContainer.text['provisioningMembershipProvisioningTitle'] }</div>
      <div class="span3" id="grouperProvisioningGroupMembershipMoreActionsButtonContentsDivId">
        <%@ include file="provisioningGroupMembershipMoreActionsButtonContents.jsp"%>
      </div>
    </div>
    
    <div class="row-fluid">
     <%--  <div class="span9"> <p>${textContainer.text['provisioningMembershipProvisioningDescription'] }</p></div> --%>
    </div>
    
    <div class="row-fluid">
    	<div class="span9">
    		${textContainer.text['provisioningMembershipProvisioningSubjectFor']}
    		${grouperRequestContainer.subjectContainer.guiSubject.shortLink}
    	</div>
    </div>
    
    <%@ include file="provisioningGroupMembershipTableHelper.jsp"%>
    
  </div>
</div>
        