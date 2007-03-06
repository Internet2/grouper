<%-- @annotation@ 
			Input form for uploading test data
--%><%--
  @author Gary Brown.
  @version $Id: UploadTestForm.jsp,v 1.2 2007-03-06 11:05:49 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
 <html:form action="/processUploadTest.do" enctype="multipart/form-data" method="post">

         Select File: <html:file property="testData"/> <br />

         <html:submit value="Upload File"/>

   </html:form>


