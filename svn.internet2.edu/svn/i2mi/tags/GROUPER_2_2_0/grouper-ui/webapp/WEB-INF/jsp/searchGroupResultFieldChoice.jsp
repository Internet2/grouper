<%-- @annotation@
		  Tile which lets user select which field to display in search results
--%><%--
  @author Gary Brown.
  @version $Id: searchGroupResultFieldChoice.jsp,v 1.7 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<tiles:importAttribute ignore="true"/>
<%
	Map map = (Map)session.getAttribute("mediaMap");
	String fieldList = (String)map.get("search.group.result-field-choice");
	if(fieldList != null && !fieldList.startsWith("???") && !"".equals(fieldList)) {
%>
<tr class="formTableRow">
  <c:if test="${empty groupSearchResultField}">
    <c:set var="groupSearchResultField" value="${mediaMap['search.group.result-field']}" scope="request"/>
  </c:if>
  <td class="formTableLeft">
    <grouper:message key="find.groups.select-result-field"/>
  </td>
  <td class="formTableRight">
    <c:forTokens items="${mediaMap['search.group.result-field-choice']}" delims=" " var="field" varStatus="counter">
    	<c:if test="${counter.count==1}"></c:if>
    	<c:set var="checked" value=""/>
    	<c:if test="${field==groupSearchResultField}"><c:set var="checked" value="checked='checked'"/></c:if>
      <input type="radio" name="groupSearchResultField" value="<c:out value="${field}"/>" 
        <c:out value="${checked}" escapeXml="false"/>/> 
        <%-- hack this up so we can get some tooltips --%>
        <c:choose>
          <c:when test="${fieldList[field].displayName == navMap['field.displayName.displayName']}">
            <grouper:message key="field.displayName.displayName" />
          </c:when>
          <c:when test="${fieldList[field].displayName == navMap['field.displayName.displayExtension']}">
            <grouper:message key="field.displayName.displayExtension" />
          </c:when>
          <c:when test="${fieldList[field].displayName == navMap['field.displayName.name']}">
            <grouper:message key="field.displayName.name" />
          </c:when>
          <c:when test="${fieldList[field].displayName == navMap['field.displayName.extension']}">
            <grouper:message key="field.displayName.extension" />
          </c:when>
          <c:when test="${fieldList[field].displayName == navMap['field.displayName.description']}">
            <grouper:message key="field.displayName.description" />
          </c:when>
        
          <c:otherwise><c:out value="${fieldList[field].displayName}"/></c:otherwise>
        </c:choose>
        
    </c:forTokens>
  </td>
</tr>
<%
}
%>
</grouper:recordTile>