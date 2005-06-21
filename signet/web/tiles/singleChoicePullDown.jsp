<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<%@ page import="java.util.Iterator" %>

<%@ page import="edu.internet2.middleware.signet.Limit" %>
<%@ page import="edu.internet2.middleware.signet.choice.Choice" %>

<%@ page import="edu.internet2.middleware.signet.ui.LimitRenderer" %>
<%@ page import="edu.internet2.middleware.signet.ui.Common" %>

<tiles:useAttribute name="limit" classname="edu.internet2.middleware.signet.Limit" />
<tiles:useAttribute name="grantableChoiceSubset" classname="java.util.Set" />

<%
String limitParamName = LimitRenderer.makeLimitValueParamName(limit, false);
%>

<select class="<%=limit.getDataType()%>" name="<%=limitParamName%>">

<% 
Choice[] choices
	= Common.getChoicesInDisplayOrder(limit.getChoiceSet());
Choice defaultChoice = null;    
for (int i = 0; i < choices.length; i ++)
{
  Choice choice = choices[i];
  boolean choiceIsGrantable = grantableChoiceSubset.contains(choice);
  if ((defaultChoice == null) && choiceIsGrantable)
  {
    defaultChoice = choice;
  }
%>

 <option <%=((choice.equals(defaultChoice)) ? " selected" : "")%>
   value=<%=choice.getValue()%>
   name=foo
   <%=(grantableChoiceSubset.contains(choice) == false) ? "disabled>" : ">"%>
     <%=choice.getDisplayValue()%>
  </option>

<%
}
%>

</select>