<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<%@ page import="java.util.Iterator" %>

<%@ page import="edu.internet2.middleware.signet.Limit" %>
<%@ page import="edu.internet2.middleware.signet.choice.Choice" %>

<%@ page import="edu.internet2.middleware.signet.ui.LimitRenderer" %>

<tiles:useAttribute name="limit" classname="edu.internet2.middleware.signet.Limit" />

<%
String limitValueParamName = LimitRenderer.makeLimitValueParamName(limit);
Iterator choicesIterator = limit.getChoiceSet().getChoices().iterator();
int choiceNumber = 0;
    
while (choicesIterator.hasNext())
{
  Choice choice = (Choice)(choicesIterator.next());
%>

  <%=((choiceNumber > 0) ? "<br />" : "")%>

  <input
     name="<%=limitValueParamName%>"
     type="checkbox"
     value="<%=choice.getValue()%>" />

  <label for="<%=limitValueParamName%>">
    <%=choice.getDisplayValue()%>
  </label>

<%
  choiceNumber++;
}
%>

</select>