<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<%@ page import="java.util.Iterator" %>

<%@ page import="edu.internet2.middleware.signet.Limit" %>
<%@ page import="edu.internet2.middleware.signet.choice.Choice" %>

<%@ page import="edu.internet2.middleware.signet.ui.LimitRenderer" %>

<tiles:useAttribute name="limit" classname="edu.internet2.middleware.signet.Limit" />

<%
String limitName = LimitRenderer.makeLimitName(limit);
%>

<select class="<%=limit.getDataType()%>" name "<%=limitName%>">

<% 
Iterator choicesIterator = limit.getChoiceSet().getChoices().iterator();
boolean isFirstChoice = true;
    
while (choicesIterator.hasNext())
{
  Choice choice = (Choice)(choicesIterator.next());
%>

 <option <%=(isFirstChoice ? " selected" : "")%>>
    <%=choice.getDisplayValue()%>
  </option>

<%
  isFirstChoice = false;
}
%>

</select>