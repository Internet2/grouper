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
Iterator choicesIterator = limit.getChoiceSet().getChoices().iterator();
boolean isFirstChoice = true;
    
while (choicesIterator.hasNext())
{
  Choice choice = (Choice)(choicesIterator.next());
%>

 <option <%=(isFirstChoice ? " selected" : "")%> label=<%=choice.getDisplayValue()%>>
    <%=choice.getValue()%>
  </option>

<%
  isFirstChoice = false;
}
%>

</select>