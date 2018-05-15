<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simpleMembershipUpdateSubjectDetails.jsp -->
<div class="simpleMembershipUploadMemberDetails">

  <div class="section">
  
    <grouper:subtitle label="${simpleMembershipUpdateContainer.text.memberDetailsSubtitle}" />
  
    <div class="sectionBody">
      <table class="formTable formTableSpaced SubjectInfo">
      
        <%-- this is a map of key value pairs Map.Entry --%>
        <c:forEach var="attribute" items="${simpleMembershipUpdateContainer.subjectDetails}">
          <tr class="formTableRow">
            <td class="formTableLeft">
              <%-- this could be misleading, but put in some common labels from nav.properties and tooltips
              
          subject.summary.displayName=Path
          subject.summary.extension=ID
          subject.summary.createTime=Created
          subject.summary.createSubjectId=Creator ID (entity ID)
          subject.summary.createSubjectType=Creator entity type
          subject.summary.modifyTime=Last edited
          subject.summary.modifySubjectId=Last editor ID (entity ID)
          subject.summary.modifySubjectType=Last editor entity type
          subject.summary.subjectType=Entity type
              
              --%>
              <c:set var="subjectSummaryNavKey" value="subject.summary.${attribute.key}"  />
              <%-- change some common ones so they dont overlap --%>
              <c:if test="${simpleMembershipUpdateContainer.subjectForDetails.type.name == 'group'}">
                <c:set var="subjectSummaryNavKey" value="subject.summary.group.${attribute.key}"  />
              </c:if>
              <c:choose>
                <c:when test="${! empty navNullMap[subjectSummaryNavKey]}">
                  <grouper:message key="${subjectSummaryNavKey}" />
                </c:when>
                <c:otherwise>
                  <c:out value="${attribute.key}"/>
                </c:otherwise>    
              </c:choose>
            </td>
            <td class="formTableRight">
              <c:out value="${attribute.value}" />
            </td>
          </tr>
         
        </c:forEach>
      </table>
    </div>
  </div>
</div>
<!-- End: simpleMembershipUpdateSubjectDetails.jsp -->


