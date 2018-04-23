<%-- @annotation@ Username, password and possibly locale --%>
<%@include file="/WEB-INF/jsp/include.jsp"%>

<h3>You will get a very different view of available groups depending on who you log in as. SuperUser sees everything.<p/>
</h3>

<html:form action="/login" method="post"><grouper:message key="auth.username"/> <!--html:text property="username" size="50"/-->
<html:select property="username">
<option>81122</option>
<option>83891</option>
<option>89431</option>
<c:forEach var="person" items="${peopleList}" begin="0" end="5">
<option><c:out value="${person}"/></option>
</c:forEach>
</html:select>
<br/>
<!--<grouper:message key="auth.password"/> <html:text property="password" size="50"/><br/>-->
<!--<input type="radio" CHECKED value="en" name="lang"/> English<br/>
<input type="radio" value="es" name="lang"/> Espanol<br/>
-->

<html:submit styleClass="blueButton" property="submit.login" value="${navMap['login']}"/></html:form>
<p/>
<h3>Two views are available in this prototype</h3>
<a href="populateIndex.do">Default Internet2 Middleware Initiative interface</a><p/>
<a href="uob/populateIndex.do">University of Bristol interface</a><p/>

