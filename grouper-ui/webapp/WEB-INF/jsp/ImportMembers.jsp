<%-- @annotation@ 
			Imports data from uploaded file and displays output
--%><%--
  @author Gary Brown.
  @version $Id: ImportMembers.jsp,v 1.1 2007-03-21 11:09:49 isgwb Exp $
--%><%@include file="/WEB-INF/jsp/include.jsp"%><c:choose>
	<c:when test="${canWriteField}"><%importMembers(request,response);%></c:when>
	<c:otherwise>
		<fmt:message bundle="${nav}" key="groups.import.message.insufficient-privileges"/>
	</c:otherwise>
</c:choose>

<div class="linkButton">
<html:link page="/populateGroupMembers.do"  name="listGroupParams">
				<fmt:message bundle="${nav}" key="groups.action.edit-members"/>
			</html:link>
</div>

<%!
	private void importMembers(HttpServletRequest request, HttpServletResponse response) throws Exception {
			edu.internet2.middleware.grouper.Group group = (edu.internet2.middleware.grouper.Group) request.getAttribute("group");
			edu.internet2.middleware.grouper.Field field = (edu.internet2.middleware.grouper.Field) request.getAttribute("field");
			String format=(String)request.getSession().getAttribute("importFormat");
			java.io.PrintWriter output = response.getWriter();
			output.println("<pre>\n");
			org.apache.struts.action.DynaActionForm form = (org.apache.struts.action.DynaActionForm) request.getAttribute("groupForm");
			org.apache.struts.upload.FormFile inputFile = (org.apache.struts.upload.FormFile)form.get("importData");
			java.io.Reader input = new java.io.InputStreamReader(inputFile.getInputStream());
			edu.internet2.middleware.grouper.ui.util.MembershipImportManager importer = (edu.internet2.middleware.grouper.ui.util.MembershipImportManager)request.getSession().getAttribute("MembershipImportManager");
			importer.load(format,group, input, output,field);
			output.println("</pre>\n");
			output.flush();
	}
%>
