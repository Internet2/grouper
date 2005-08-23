<%-- @annotation@
		  Dynamic tile used in 'Find' browse mode to render 
		  selectable children
--%><%--
  @author Gary Brown.
  @version $Id: browseForFindMember.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>

<tiles:importAttribute />
			   	<input type="hidden" name="subjectType:<c:out value="${viewObject.id}"/>" value="<c:out value="${viewObject.subjectType}"/>" />
			   	<input type="checkbox" name="members" value="<c:out value="${viewObject.id}"/>"/> 
				<tiles:insert definition="dynamicTileDef">
					<tiles:put name="viewObject" beanName="viewObject."/>
					<tiles:put name="view" value="browseForFindMembers"/>
				</tiles:insert>
