<%-- @annotation@ 
			Imports data from uploaded file and displays output
--%><%--
  @author Gary Brown.
  @version $Id: ImportMembers.jsp,v 1.2 2007-08-08 11:36:23 isgwb Exp $
--%><%@include file="/WEB-INF/jsp/include.jsp"%><c:choose>
	<c:when test="${canWriteField}"><%importMembers(request,response);%>
		<c:if test="${mediaMap['membership-import.allow-textarea']=='true'}">
			<html:form  action="/populateGroupMembers" enctype="multipart/form-data">
<html:hidden property="groupId"/>
<html:hidden property="contextSubject"/>
<html:hidden property="contextSubjectId"/>
<html:hidden property="contextSubjectType"/>
<!--html:hidden property="callerPageId"/-->
<input type="hidden" name="callerPageId" value="<c:out value="${thisPageId}"/>"/>
<fieldset>
	
<html:hidden property="groupId"/>
	<c:if test="${MembershipImportManager.active}">
	<h2 class="actionheader">
	<fmt:message bundle="${nav}" key="groups.heading.import-members-string"/>
	</h2>
		<c:choose>
			<c:when test="${MembershipImportmanager.numberOfAvailableFormats ==1}">
				<input type="hidden" name="importFormat" value="<c:out value="${MembershipImportmanager.availableFormats[0]}"/>"/>
			</c:when>
			<c:otherwise>
			<select name="importFormat" title="<c:out value=
			"${navMap['groups.import.select-format-title']}"/>">
					<c:forEach var="format" items="${MembershipImportManager.availableFormats}">
						<option<c:if test="${format==importFormat}"> selected="selected"</c:if>><c:out value="${format}"/></option>
					</c:forEach>
			</select>
			</c:otherwise>
		</c:choose><br/><br/>
		<p><fmt:message bundle="${nav}" key="groups.import.textarea-title"/></p>
		<html:textarea rows="20" cols="50" property="importString" title="${navMap['groups.import.textarea-title']}"/>
		<br/>
		<input type="submit" name="submit.import" value="<c:out value="${navMap['groups.import.submit']}"/>"/>
		
	</c:if>
	</fieldset>
</html:form>
		</c:if>
	</c:when>
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
			int res=0;
			edu.internet2.middleware.grouper.Group group = (edu.internet2.middleware.grouper.Group) request.getAttribute("group");
			edu.internet2.middleware.grouper.Field field = (edu.internet2.middleware.grouper.Field) request.getAttribute("field");
			String format=(String)request.getSession().getAttribute("importFormat");
			java.io.PrintWriter output = response.getWriter();
			output.println("<pre>\n");
			org.apache.struts.action.DynaActionForm form = (org.apache.struts.action.DynaActionForm) request.getAttribute("groupForm");
			org.apache.struts.upload.FormFile inputFile = (org.apache.struts.upload.FormFile)form.get("importData");
			java.io.Reader input =null;
			if(inputFile!=null) {
				 
				 input = new java.io.InputStreamReader(inputFile.getInputStream());
			}else if(form.get("importString")!=null){
			
				input = new java.io.StringReader((String)form.get("importString"));
			}else{
				res=-1;
			}
			if(input !=null) {
				edu.internet2.middleware.grouper.ui.util.MembershipImportManager importer = (edu.internet2.middleware.grouper.ui.util.MembershipImportManager)request.getSession().getAttribute("MembershipImportManager");
				res=importer.load(format,group, input, output,field);
			}
				output.println("</pre>\n");
			output.flush();
			return;
	}
%>
