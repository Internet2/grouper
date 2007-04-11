<%-- @annotation@
		  ynamic tile used  to provide footer, including form buttons, for removing all, or selected members
--%><%--
  @author Gary Brown.
  @version $Id: removableMemberLinksFooterView.jsp,v 1.2 2007-04-11 08:11:05 isgwb Exp $
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
		<br/><input type="submit" name="submit.remove.selected" value="<c:out value="${navMap['members.remove.selected']}"/>"  onclick="return confirm('<c:out value="${navMap['groups.remove.warn']}"/>')"/>
		<input type="submit" name="submit.remove.all" value="<c:out value="${navMap['members.remove.all']}"/>" onclick="return confirm('<c:out value="${navMap['groups.remove.all.warn']}"/>')"/> 
	</c:if>
	</fieldset>
	</form>
	</div>
