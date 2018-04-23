<%-- @annotation@ 
			Input form for uploading test data
--%><%--
  @author Gary Brown.
  @version $Id: UploadTestForm.jsp,v 1.3 2008-04-10 19:50:25 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
 <html:form action="/processUploadTest.do" enctype="multipart/form-data" method="post">

         Select File: <html:file property="testData"/> <br />

         <html:submit styleClass="blueButton" value="Upload File"/>

   </html:form>


