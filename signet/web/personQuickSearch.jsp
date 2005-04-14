<%@ page import="java.util.Set" %>
<%@ page import="java.util.SortedSet" %>
<%@ page import="java.util.TreeSet" %>
<%@ page import="java.util.Iterator" %>

<%@ page import="java.net.URLEncoder" %>

<%@ page import="edu.internet2.middleware.signet.Signet" %>
<%@ page import="edu.internet2.middleware.signet.PrivilegedSubject" %>
<%@ page import="edu.internet2.middleware.signet.choice.Choice" %>

<%@ page import="edu.internet2.middleware.signet.ui.Common" %>


    <div>
      <link href="styles/signet.css" rel="stylesheet" type="text/css" />
      <script language="JavaScript" type="text/javascript" src="scripts/signet.js">
      </script>
<% 
  Signet signet
     = (Signet)
         (request.getSession().getAttribute("signet"));

  Set privilegedSubjects
    = signet.getPrivilegedSubjects();
    
  // This is a shameful little hack to temporarily simulate person-quicksearch
  // until it's implemented in the upcoming new version of the Subject interface:
  String searchString = request.getParameter("searchString");
  SortedSet sortSet = Common.filterSearchResults(privilegedSubjects, searchString);
  
  if (sortSet.size() <= 0)
  {
%>
      <span class="error">
        Your search found no results.
      </span>
<%
  }
  else
  {   
%>
      Your search found:
      <div class="scroll">
<% 
  	Iterator sortSetIterator = sortSet.iterator();
    while (sortSetIterator.hasNext())
    {
      PrivilegedSubject listSubject
        = (PrivilegedSubject)(sortSetIterator.next());
%>
        <a href="javascript:location.replace(unescape('<%=URLEncoder.encode("PersonView.do?granteeSubjectTypeId=" + listSubject.getSubjectTypeId() + "&granteeSubjectId=" + listSubject.getSubjectId())%>'))">
          <%=listSubject.getName()%>
		</a><br /><!-- it's important for the br to be on the same line as the a -->
        <span class="dropback"><%=listSubject.getDescription()%></span>
        <br />
<%
    }
%>
      </div> <!-- scroll -->
<%
  }
%>
    </div>