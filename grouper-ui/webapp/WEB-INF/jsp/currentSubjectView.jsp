<%-- @annotation@
		  Dynamic tile used by genericList mechanism to 
		  render memberships from Subject perspective
--%><%--
  @author Gary Brown.
  @version $Id: currentSubjectView.jsp,v 1.3 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
 <span class="currentSubject">
<grouper:message key="subject.privileges.current"/>
<tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="viewObject"/>
	  <tiles:put name="view" value="subjectSummaryLink"/>
	  <tiles:put name="params" beanName="params"/>
	  <tiles:put name="linkTitle" beanName="linkTitle"/>
  </tiles:insert>
 </span>