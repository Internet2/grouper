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


    <DIV>
      <LINK href="styles/signet.css" rel="stylesheet" type="text/css" />
      <SCRIPT language="JavaScript" type="text/javascript" src="scripts/signet.js">
      </SCRIPT>
      
<% 
  Signet signet
     = (Signet)
         (request.getSession().getAttribute("signet"));

  String searchString = request.getParameter("searchString");
  Set result = signet.findPrivilegedSubjects(searchString);
  Set sortSet = new TreeSet(new SubjectNameComparator());
  sortSet.addAll(result);

  if (sortSet.size() <= 0)
  {
%>
      <SPAN class="error">
        Your search found no results.
      </SPAN>
<%
  }
  else
  {   
%>
      <SPAN class="close">[<A href="javascript:;" onClick="document.getElementById('PersonSearchResults').style.display='none';">close</A>]</SPAN>	
	  Your search found:
      <DIV class="scroll">
<% 
  	Iterator sortSetIterator = sortSet.iterator();
    while (sortSetIterator.hasNext())
    {
      PrivilegedSubject listSubject
        = (PrivilegedSubject)(sortSetIterator.next());
%>
        <A href="javascript:location.replace(unescape('<%=URLEncoder.encode("PersonView.do?granteeSubjectTypeId=" + listSubject.getSubjectTypeId() + "&granteeSubjectId=" + listSubject.getSubjectId())%>'))">
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