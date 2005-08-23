<%-- @annotation@
		  Dynamic tile used in the 'Find' browse mode, which 
		  renders the content displayed after the children for
		  the active node.
--%><%--
  @author Gary Brown.
  @version $Id: browseStemsFindListFooterView.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute />
<div class="linkButton">
<c:if test="${pager.count>0}">
	<c:if test="${pager.prev}">
		<html:link page="${pager.target}.do" name="pager" property="prevParams"><fmt:message bundle="${nav}" key="find.previous-page"/></html:link>&#160;&#160;		
	</c:if>
	<c:if test="${pager.next}">
		<html:link page="${pager.target}.do" name="pager" property="nextParams"><fmt:message bundle="${nav}" key="find.next-page"/></html:link>&#160;&#160;		
	</c:if>
</c:if>
</div>

<c:if test="${!empty areAssignableChildren}">
		<br/><input type="submit" name="submit.group.member" value="<c:out value="${navMap['priv.assign']}"/>"/> 
	</c:if>
	</fieldset>
	</form>
	</div>
