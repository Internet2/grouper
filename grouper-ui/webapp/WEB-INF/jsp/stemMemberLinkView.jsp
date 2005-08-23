<%-- @annotation@
			Form which allows user to change an individual
			Subject`s membership of / privileges for, the
			active stem
--%><%--
  @author Gary Brown.
  @version $Id: stemMemberLinkView.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute name="viewObject"/>
<html:link page="/populateStemMember.do" name="viewObject">
  <tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="viewObject"/>
	  <tiles:put name="view" value="groupMember"/>
  </tiles:insert>
</html:link>