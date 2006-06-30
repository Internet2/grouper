<%@ page import="java.util.Set" %>
<%@ page import="java.util.SortedSet" %>
<%@ page import="java.util.TreeSet" %>
<%@ page import="java.util.Iterator" %>

<%@ page import="java.net.URLEncoder" %>

<%@ page import="edu.internet2.middleware.signet.Signet" %>
<%@ page import="edu.internet2.middleware.signet.PrivilegedSubject" %>
<%@ page import="edu.internet2.middleware.signet.choice.Choice" %>

<%@ page import="edu.internet2.middleware.signet.ui.Common" %>
<%@ page import="edu.internet2.middleware.signet.ui.SubjectNameComparator" %>
<%@ page import="edu.internet2.middleware.signet.resource.ResLoaderUI" %>

      <LINK href="styles/signet.css" rel="stylesheet" type="text/css" />
    <DIV>

      
<% 
  Signet signet
     = (Signet)
         (request.getSession().getAttribute("signet"));

  String searchString = request.getParameter("searchString");
  Set result = signet.getSubjectSources().findPrivilegedSubjects(searchString);
  Set sortSet = new TreeSet(new SubjectNameComparator());
  sortSet.addAll(result);

  if (sortSet.size() <= 0)
  {
%>
      <SPAN class="error">
        <%=ResLoaderUI.getString("personQuickSearch.error.txt") %>
      </SPAN>
<%
  }
  else
  {   
%>
      <SPAN class="close">[<A href="javascript:;" onClick="document.getElementById('PersonSearchResults').style.display='none';"><%=ResLoaderUI.getString("personQuickSearch.close.href") %></A>]</SPAN>	
	  <%=ResLoaderUI.getString("personQuickSearch.found.txt") %>
      <DIV class="scroll">
<% 
  	Iterator sortSetIterator = sortSet.iterator();
    while (sortSetIterator.hasNext())
    {
      PrivilegedSubject listSubject
        = (PrivilegedSubject)(sortSetIterator.next());
%>
        <A href="javascript:location.replace(unescape('<%=URLEncoder.encode("PersonView.do?granteeSubjectTypeId=" + listSubject.getSubjectTypeId() + "&granteeSubjectId=" + listSubject.getSubjectId(), "UTF-8")%>'))">
          <%=listSubject.getName()%>
		</A>
        <DIV class="ident">
<%
	if (listSubject.getDescription() != null) {
		out.print(listSubject.getDescription());
	}
%>
		</DIV>
<%
    }
%>
      </DIV> <!-- scroll -->
<%
  }
%>
    </DIV>