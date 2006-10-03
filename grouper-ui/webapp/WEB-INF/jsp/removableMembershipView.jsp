<%-- @annotation@
		  Dynamic tile used by genericList mechanism to 
		  render individual group members which can be removed
--%><%--
  @author Gary Brown.
  @version $Id: removableMembershipView.jsp,v 1.1 2006-10-03 11:33:37 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<input type="checkbox" name="subjectIds" value="<c:out value="${viewObject.subject.id}"/>"/>
			<tiles:insert definition="dynamicTileDef" flush="false">
	  			<tiles:put name="viewObject" beanName="viewObject" />
	  			<tiles:put name="view" value="membershipInfo"/>
  			</tiles:insert>