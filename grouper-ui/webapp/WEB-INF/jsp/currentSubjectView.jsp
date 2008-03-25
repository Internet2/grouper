<%-- @annotation@
		  Dynamic tile used by genericList mechanism to 
		  render memberships from Subject perspective
--%><%--
  @author Gary Brown.
  @version $Id: currentSubjectView.jsp,v 1.2 2008-03-25 14:59:51 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
 <span class="currentSubject">
<grouper:message bundle="${nav}" key="subject.privileges.current"/>
<tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="viewObject"/>
	  <tiles:put name="view" value="subjectSummaryLink"/>
	  <tiles:put name="params" beanName="params"/>
	  <tiles:put name="linkTitle" beanName="linkTitle"/>
  </tiles:insert>
 </span>