<%-- @annotation@
		  ynamic tile used  to provide footer, including form buttons, for removing all, or selected members
--%><%--
  @author Gary Brown.
  @version $Id: removableMemberLinksFooterView.jsp,v 1.1 2006-10-03 11:33:37 isgwb Exp $
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
<div class="linkButton">
<tiles:insert definition="callerPageButtonDef"/>
</div>

<c:if test="${removableMembers}">
		<br/><input type="submit" name="submit.remove.selected" value="<c:out value="${navMap['members.remove.selected']}"/>"/>
		<input type="submit" name="submit.remove.all" value="<c:out value="${navMap['members.remove.all']}"/>"/> 
	</c:if>
	</fieldset>
	</form>
	</div>
