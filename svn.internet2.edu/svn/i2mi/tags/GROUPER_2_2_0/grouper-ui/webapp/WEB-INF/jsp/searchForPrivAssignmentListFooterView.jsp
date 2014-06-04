<%-- @annotation@
		  Dynamic tile used by genericList mechanism to render
		  content below list items - i.e. is an alternative footer
		  when searching for subjects in 'Find' mode
--%><%--
  @author Gary Brown.
  @version $Id: searchForPrivAssignmentListFooterView.jsp,v 1.7 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute />
<c:if test="${pager_removeFromSubjectSearch != 'true'}">
<c:if test="${pager.count>0}"><div class="linkButton">
	<c:if test="${pager.prev}">
		<html:link page="${pager.target}.do" name="pager" property="prevParams"><grouper:message key="find.previous-page"/></html:link>&#160;&#160;		
	</c:if>
	<c:if test="${pager.next}">
		<html:link page="${pager.target}.do" name="pager" property="nextParams"><grouper:message key="find.next-page"/></html:link>&#160;&#160;		
	</c:if>
	</div>
</c:if>
</c:if>
<div class="linkButton">
<c:if test="${pager.count>0}">
		<p><br/><input type="submit" class="blueButton" name="submit.group.member" value="<c:out value="${navMap['priv.assign']}"/>"/></p>
	</c:if>
<tiles:insert definition="callerPageButtonDef"/>
<c:if test="${!forStems}">
<html:link page="/cancelFindNewMembers.do" paramId="groupId" paramName="findForNode"  >
	<grouper:message key="find.for-groups.cancel"/>
</html:link></c:if>
<c:if test="${forStems}">
<html:link page="/cancelFindNewMembers.do" paramId="currentNode" paramName="findForNode"  >
	<grouper:message key="find.for-stems.cancel"/>
</html:link></c:if>
</div>


	</form>
	</div>
