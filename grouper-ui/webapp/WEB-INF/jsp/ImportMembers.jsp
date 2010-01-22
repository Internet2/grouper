<%-- @annotation@ 
			Imports data from uploaded file and displays output
--%><%--
  @author Gary Brown.
  @version $Id: ImportMembers.jsp,v 1.7 2009-10-16 10:30:07 isgwb Exp $
--%><%@include file="/WEB-INF/jsp/include.jsp"%>
<%@page import="edu.internet2.middleware.grouper.j2ee.GrouperRequestWrapper"%>
<%@page import="org.apache.commons.fileupload.FileItem"%><c:choose>
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
  <grouper:subtitle key="groups.heading.import-members-string" />
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
		<p><grouper:message key="groups.import.textarea-title"/></p>
		<html:textarea rows="20" cols="50" property="importString" title="${navMap['groups.import.textarea-title']}"/>
		<br/>
		<input type="submit" class="blueButton" name="submit.import" value="<c:out value="${navMap['groups.import.submit']}"/>"/>
		
	</c:if>
	</fieldset>
</html:form>
		</c:if>
	</c:when>
	<c:otherwise>
		<grouper:message key="groups.import.message.insufficient-privileges"/>
	</c:otherwise>
</c:choose>

<div class="linkButton">
<html:link page="/populateGroupMembers.do"  name="listGroupParams">
				<grouper:message key="groups.action.edit-members"/>
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
			
			if(request instanceof GrouperRequestWrapper) {
				GrouperRequestWrapper grouperRequest = (GrouperRequestWrapper) request;
				FileItem fileItem = grouperRequest.getParameterFileItem("importData");
				java.io.Reader input =null;
				if(fileItem!=null) {
					 
					 input = new java.io.InputStreamReader(fileItem.getInputStream());
				}else if(request.getParameter("importString")!=null){
				
					input = new java.io.StringReader(request.getParameter("importString"));
				}else{
					res=-1;
				}
				if(input !=null) {
					edu.internet2.middleware.grouper.ui.util.MembershipImportManager importer = (edu.internet2.middleware.grouper.ui.util.MembershipImportManager)request.getSession().getAttribute("MembershipImportManager");
					res=importer.load(format,group, input, output,field);
				}
				
			}
			

			
				output.println("</pre>\n");
			output.flush();
			return;
	}
%>
