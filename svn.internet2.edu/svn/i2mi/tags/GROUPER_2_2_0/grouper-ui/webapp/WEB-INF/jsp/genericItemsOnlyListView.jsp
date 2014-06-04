<%-- @annotation@
		  Dynamic tile used to render Lists items - no header or footer
--%><%--
  @author Gary Brown.
  @version $Id: genericItemsOnlyListView.jsp,v 1.1 2006-02-21 16:50:18 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
  <%int itemPos = 0;pageContext.setAttribute("itemPos",new Integer(itemPos));
  Collection viewCollection = (Collection)pageContext.findAttribute("viewObject");
  if(viewCollection.size()>0) {
  %>
  <c:if test="${itemViewInFieldset=='true'}">
	<fieldset>
	</c:if>
 <c:if test="${'TRUE' != listless}">
  <ul>
  </c:if>
	  <c:forEach var="listItem" items="${viewObject}">
		 <c:if test="${'TRUE' != listless}"><li></c:if><tiles:insert definition="dynamicTileDef" flush="false">
			<tiles:put name="view" beanName="itemView"/>
			<tiles:put name="viewObject" beanName="listItem"/>
			 <tiles:put name="linkSeparator" beanName="linkSeparator"/>
			<tiles:put name="itemPos" beanName="itemPos"/>
		</tiles:insert><c:if test="${'TRUE' != listless}"></li></c:if>
		<%pageContext.setAttribute("itemPos",new Integer(++itemPos));%>
	  </c:forEach>
 <c:if test="${'TRUE' != listless}">
  </ul>
  </c:if>
    <c:if test="${itemViewInFieldset=='true'}">
	</fieldset>
	</c:if>
  <%}%>