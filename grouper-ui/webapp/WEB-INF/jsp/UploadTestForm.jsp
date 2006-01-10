<%-- @annotation@ 
			Input form for uploading test data
--%><%--
  @author Gary Brown.
  @version $Id: UploadTestForm.jsp,v 1.1 2006-01-10 12:31:43 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
 <html:form action="/processUploadTest.do" enctype="multipart/form-data">

         Select File: <html:file property="testData"/> <br />

         <html:submit value="Upload File"/>

   </html:form>


