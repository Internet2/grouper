<%-- @annotation@
		  Dynamic tile used to render Lists - header,
		  items and footer
--%><%--
  @author Gary Brown.
  @version $Id: genericListView.jsp,v 1.5 2009-08-10 14:03:01 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>

<c:if test="${!empty skipText}"><br/><a href="<c:out value="${pageUrl}"/>#endList" class="noCSSOnly"><c:out value="${skipText}"/><br/></a></c:if>
  <tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="view" beanName="headerView"/>
	  <tiles:put name="viewObject" beanName="viewObject"/>
	  <tiles:put name="pager" beanName="pager"/>
	 
	  <tiles:put name="noResultsMsg" beanName="noResultsMsg"/>
	  <tiles:put name="listInstruction" beanName="listInstruction"/>
  </tiles:insert>
  <%int itemPos = 0;pageContext.setAttribute("itemPos",new Integer(itemPos));
  Collection viewCollection = (Collection)pageContext.findAttribute("viewObject");
  if(viewCollection !=null && viewCollection.size()>0) {
  %>
  <c:if test="${itemViewInFieldset=='true'}">
	<fieldset>
	</c:if>
	<c:choose>
		<c:when test="${useTable=='true'}">
			<table class="<c:out value="${tableClass}"/>">
				<c:if test="${!empty headerRow}">
					<c:out value="${headerRow}" escapeXml="false"/>
				</c:if>
			<c:forEach var="listItem" items="${viewObject}">
			
				<tiles:insert definition="dynamicTileDef" flush="false">
					<tiles:put name="view" beanName="itemView"/>
					<tiles:put name="viewObject" beanName="listItem"/>
					 <tiles:put name="linkSeparator" beanName="linkSeparator"/>
					<tiles:put name="itemPos" beanName="itemPos"/>
				</tiles:insert>
				<%pageContext.setAttribute("itemPos",new Integer(++itemPos));%>
			  </c:forEach>
			  </table>
		</c:when>
		<c:otherwise>
		  <ul>
			  <c:forEach var="listItem" items="${viewObject}">
				<li><tiles:insert definition="dynamicTileDef" flush="false">
					<tiles:put name="view" beanName="itemView"/>
					<tiles:put name="viewObject" beanName="listItem"/>
					 <tiles:put name="linkSeparator" beanName="linkSeparator"/>
					<tiles:put name="itemPos" beanName="itemPos"/>
				</tiles:insert></li>
				<%pageContext.setAttribute("itemPos",new Integer(++itemPos));%>
			  </c:forEach>
		  </ul>
  		</c:otherwise>
  	</c:choose>
    <c:if test="${itemViewInFieldset=='true'}">
	</fieldset>
	</c:if>
  <%}%>
  
  <tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="view" beanName="footerView"/>
	  <tiles:put name="viewObject" beanName="viewObject"/>
	  <tiles:put name="pager" beanName="pager"/>
  </tiles:insert>
<a name="endList" id="endList"></a>