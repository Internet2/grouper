<%-- @annotation@ 
			Displays a set of checkboxes or radio buttons for user selection based on tiles attributes passed in
--%><%--
  @author Gary Brown.
  @version $Id: multiOption.jsp,v 1.2 2008-09-25 04:54:16 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<%
Collection items = (Collection) pageContext.getAttribute("items");
Collection selected = (Collection) pageContext.getAttribute("selected");
String columnsStr = (String) pageContext.getAttribute("columns");
String property = (String) pageContext.getAttribute("property");
String name = (String) pageContext.getAttribute("name");
int columns = Integer.parseInt(columnsStr);
int count =0;
boolean newRow = true;
boolean singleValued = "TRUE".equals(pageContext.getAttribute("singleValue"));
String inputType="checkbox";
if(singleValued) inputType="radio";
boolean checked=false;
Object item;
String checkedStr=null;
%>
<div class="multiOption">
<%
	Iterator it = items.iterator();
	while(it.hasNext()) {
		count++;
		item = it.next();
		checkedStr="";
		checked=selected.contains(item);
		if(checked) checkedStr= "checked=\"checked\"";
		pageContext.setAttribute("item",item);
		if(newRow) {%>
			<div class="multiOptionRow">
		<%}%>
		 <c:choose>
			<c:when test="${empty property}">
				<c:set var="inputValue" value="${item}"/>
			</c:when>
			<c:otherwise>
				<c:set var="inputValue" value="${item[property]}"/>
			</c:otherwise>
		  </c:choose>
			<span class="multiOptionItem">
				<input id="multi-<%=name%><%=count%>"
					   type="<%=inputType%>" 
					   name="<%=name%>"
					   value="<c:out value="${inputValue}"/>"
					   <%=checkedStr%>/><label for="multi-<%=name%><%=count%>"><grouper:message 
               value="${inputValue}" valueTooltipKey="${name}.${inputValue}" /></label> 
			</span>
		<%
		newRow = (count % columns) == 0;
		if(newRow) {%>
			</div>
		<%}	
	}
%>

</div>