<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<%@ page import="java.util.Iterator" %>

<%@ page import="edu.internet2.middleware.signet.Limit" %>
<%@ page import="edu.internet2.middleware.signet.choice.Choice" %>

<%@ page import="edu.internet2.middleware.signet.ui.LimitRenderer" %>
<%@ page import="edu.internet2.middleware.signet.ui.Common" %>

<tiles:useAttribute name="limit" classname="edu.internet2.middleware.signet.Limit" />
<tiles:useAttribute name="grantableChoiceSubset" classname="java.util.Set" />

<%
String limitValueParamName = LimitRenderer.makeLimitValueParamName(limit, true);
Iterator choicesIterator = limit.getChoiceSet().getChoices().iterator();

Choice[] choices
	= Common.getChoicesInDisplayOrder(limit.getChoiceSet());
    
for (int i = 0; i < choices.length; i ++)
{
  Choice choice = choices[i];
%>

  <%=((i > 0) ? "<br />" : "")%>

  <input
     name="<%=limitValueParamName%>"
     type="checkbox"
     value="<%=choice.getValue()%>"
     <%=(grantableChoiceSubset.contains(choice) ? "enabled=true" : "disabled=true")%>"
     onClick="selectLimitCheckbox();" />

  <label for="<%=limitValueParamName%>">
    <%=choice.getDisplayValue()%>
  </label>

<%
}
%>

</select>