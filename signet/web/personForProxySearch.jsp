<%@ page import="java.util.Set" %>
<%@ page import="java.util.SortedSet" %>
<%@ page import="java.util.TreeSet" %>
<%@ page import="java.util.Iterator" %>

<%@ page import="java.net.URLEncoder" %>

<%@ page import="edu.internet2.middleware.signet.Signet" %>
<%@ page import="edu.internet2.middleware.signet.PrivilegedSubject" %>
<%@ page import="edu.internet2.middleware.signet.choice.Choice" %>

<%@ page import="edu.internet2.middleware.signet.ui.Common" %>
<%@ page import="edu.internet2.middleware.signet.ui.Constants" %>
<%@ page import="edu.internet2.middleware.signet.ui.SubjectNameComparator" %>


    <div>
      <link href="styles/signet.css" rel="stylesheet" type="text/css" />
      <script language="JavaScript" type="text/javascript" src="scripts/signet.js">
      </script>
<% 
  Signet signet
     = (Signet)
         (request.getSession().getAttribute("signet"));

  PrivilegedSubject loggedInPrivilegedSubject
    = (edu.internet2.middleware.signet.PrivilegedSubject)
        (request.getSession().getAttribute(Constants.LOGGEDINUSER_ATTRNAME));

  String searchString = request.getParameter("searchString");
  Set result = signet.findPrivilegedSubjects(searchString);
  
  // There are some Subjects which are not candidates to serve as proxies
  // for a specific Subject:
  //
  //    a) The Subject itself cannot be its own Proxy
  //    b) A Group cannot be a Proxy.
  result.remove(loggedInPrivilegedSubject.getEffectiveEditor());
  result = Common.removeGroups(result);
  
  Set sortSet = new TreeSet(new SubjectNameComparator());
  sortSet.addAll(result);

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
      <br />
      <select
        class="long"
        style="float: left; margin-bottom: 10px;"
        name="<%=Constants.SUBJECT_SELECTLIST_ID%>"
        size="10"
        id="<%=Constants.SUBJECT_SELECTLIST_ID%>"
        onchange="<%="javascript:selectSubject('" + Constants.SUBSYSTEM_HTTPPARAMNAME + "', '" + Constants.SUBSYSTEM_PROMPTVALUE+ "');"%>">
<% 
  	Iterator sortSetIterator = sortSet.iterator();
    while (sortSetIterator.hasNext())
    {
      PrivilegedSubject listSubject
        = (PrivilegedSubject)(sortSetIterator.next());
      String listSubjectCompoundId = Common.buildCompoundId(listSubject);
%>
        <option value="<%=listSubjectCompoundId%>">
          <%=listSubject.getName()%>
        </option>
        
<%
    }
%>
	  </select>
<%
    // Now, let's tuck away some detail info about each Subject,
    // which will be displayed when the Subject (as rendered above)
    // is clicked on.
%>
				
      <div id="subjectDetailsDiv">
<%
	sortSetIterator = sortSet.iterator();
	while (sortSetIterator.hasNext())
	{
      PrivilegedSubject listSubject
        = (PrivilegedSubject)(sortSetIterator.next());
      String listSubjectCompoundId = Common.buildCompoundId(listSubject);
%>
        <div style="display:none" id="SUBJECT_NAME:<%=listSubjectCompoundId%>">
          <%=listSubject.getName()%>
        </div>
        <div style="display:none" id="SUBJECT_DESCRIPTION:<%=listSubjectCompoundId%>">
          <%=listSubject.getDescription()%>
        </div>
        <div style="display:none" id="SUBJECT_WARNING:<%=listSubjectCompoundId%>">
        </div>
<%
    }
%>

      </div> <!-- subjectDetailsDiv -->

    </div>

<%
  }
%>