<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<%@ page import="java.util.Iterator" %>

<%@ page import="edu.internet2.middleware.signet.Limit" %>
<%@ page import="edu.internet2.middleware.signet.choice.Choice" %>

<%@ page import="edu.internet2.middleware.signet.ui.LimitRenderer" %>

<tiles:useAttribute name="limit" classname="edu.internet2.middleware.signet.Limit" />

<%
String limitParamName = LimitRenderer.makeLimitValueParamName(limit);
%>

<select class="<%=limit.getDataType()%>" name="<%=limitParamName%>">

<% 
Choice[] choices = limit.getChoiceSet().getChoicesInDisplayOrder();
    
for (int i = 0; i < choices.length; i ++)
{
  Choice choice = choices[i];
%>

 <option <%=((i == 0) ? " selected" : "")%> value=<%=choice.getValue()%>>
    <%=choice.getDisplayValue()%>
  </option>

<%
}
%>

</select>