<%-- @annotation@ 
		 Standard tile used in baseDef which renders the menu. Use css to change position 
--%><%--
  @author Gary Brown.
  @version $Id: menu.jsp,v 1.6 2008-04-12 03:51:00 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>

<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<jsp:useBean id="tabStyle" class="java.util.HashMap"/>

<c:forEach var="menuItem" items="${menuItems}">
	<c:set  target="${tabStyle}" property="${menuItem.functionalArea}" value="tab"/>
</c:forEach>
<c:set  target="${tabStyle}" property="${functionalArea}" value="selectedTab"/>
<c:if test="${menuMetaBean.hasEnrollment}">
  <div class="menuSubheader"><grouper:message key="menu.subtitle.enrollment" ignoreTooltipStyle="true"/></div>
  <c:forEach var="menuItem" items="${menuMetaBean.enrollmentMenuPart}">
  <div class="actionbox <c:out value="${tabStyle[menuItem.functionalArea]}" />">
    <html:link page="${menuItem.action}">
      <grouper:message key="${menuItem.linkKey}" tooltipRef="${menuItem.titleKey}" 
      ignoreTooltipStyle="true"/>
    </html:link>
  </div>
 </c:forEach>
</c:if>
<c:if test="${menuMetaBean.hasResponsibilities}">
  <div class="menuSubheader"><grouper:message key="menu.subtitle.responsibilities" ignoreTooltipStyle="true"/></div>
  <c:forEach var="menuItem" items="${menuMetaBean.responsibilitiesMenuPart}">
  <div class="actionbox <c:out value="${tabStyle[menuItem.functionalArea]}" />">
    <html:link page="${menuItem.action}">
      <grouper:message key="${menuItem.linkKey}" tooltipRef="${menuItem.titleKey}" 
      ignoreTooltipStyle="true"/>
    </html:link>
    
  </div>
 </c:forEach>
</c:if>

<%-- everybody has tools --%>
<div class="menuSubheader"><grouper:message key="menu.subtitle.tools" ignoreTooltipStyle="true"/></div>
<c:forEach var="menuItem" items="${menuMetaBean.toolsMenuPart}">
  <div class="actionbox <c:out value="${tabStyle[menuItem.functionalArea]}" />">
    <html:link page="${menuItem.action}">
      <grouper:message key="${menuItem.linkKey}" tooltipRef="${menuItem.titleKey}" 
      ignoreTooltipStyle="true"/>
    </html:link>
  </div>
 </c:forEach>       
</grouper:recordTile>