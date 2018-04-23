<%-- @annotation@
		  Dynamic tile used to render a link to SubjectSummary page
--%><%--
  @author Gary Brown.
  @version $Id: subjectSummaryLinkView.jsp,v 1.1 2006-02-02 16:40:48 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<span class="subjectSummaryLink">
	<html:link page="/populateSubjectSummary.do" name="params" title="${linkTitle}">
		<tiles:insert definition="dynamicTileDef" flush="false">
			<tiles:put name="viewObject" beanName="viewObject"/>
			<tiles:put name="view" value="privilegee"/>
		</tiles:insert>	
	</html:link>
</span>