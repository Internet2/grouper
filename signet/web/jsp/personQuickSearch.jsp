<%@ page import="java.util.Set" %>
<%@ page import="java.util.SortedSet" %>
<%@ page import="java.util.TreeSet" %>
<%@ page import="java.util.Iterator" %>

<%@ page import="java.net.URLEncoder" %>

<%@ page import="edu.internet2.middleware.signet.Signet" %>
<%@ page import="edu.internet2.middleware.signet.subjsrc.SignetSubject" %>
<%@ page import="edu.internet2.middleware.signet.choice.Choice" %>

<%@ page import="edu.internet2.middleware.signet.ui.Common" %>
<%@page import="edu.internet2.middleware.signet.ui.Constants"%>
<%@ page import="edu.internet2.middleware.signet.ui.SubjectNameComparator" %>
<%@ page import="edu.internet2.middleware.signet.resource.ResLoaderUI" %>
<%@page import="java.util.Vector"%>
<%@page import="edu.internet2.middleware.signet.subjsrc.SignetSubjectAttr"%>

<LINK href="styles/signet.css" rel="stylesheet" type="text/css" />
    <DIV>

      
<% 
  Signet signet
     = (Signet)
         (request.getSession().getAttribute(Constants.SIGNET_ATTRNAME));

  String searchString = request.getParameter("searchString");
  Set result = signet.getSubjectsByIdentifier(searchString);
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
      SignetSubject listSubject = (SignetSubject)(sortSetIterator.next());
		StringBuffer title1 = new StringBuffer();
		Vector<SignetSubjectAttr> ids = listSubject.getAttributesForName("subjectAuthId");
		for (int i = 0; i < ids.size(); i++)
		{
			if (0 == i)
				title1.append("Subject AuthId(s): ");
			title1.append(ids.elementAt(i).getValue());
			if (i < (ids.size() - 1))
				title1.append(", ");
		}

		StringBuffer title2 = new StringBuffer();
//Don't do 'description' until I can figure out how to do multi-line tooltips
//		if (null != listSubject.getDescription())
//			title2.append(listSubject.getDescription());

		StringBuffer title = new StringBuffer();
		if ((0 < title1.length()) || (0 < title2.length()))
		{
			title.append("title=\"");
			if (0 < title1.length())
				title.append(title1.toString());
			if (0 < title2.length())
			{
				if (0 < title1.length())
					title.append("    ");
				title.append("Description: " + title2.toString());
			}
			title.append("\"");
		}


%>
        <A <%=title.toString() %> href="javascript:location.replace(unescape('<%=URLEncoder.encode("PersonView.do?" + Constants.SIGNET_SOURCE_ID_HTTPPARAMNAME + "=" + listSubject.getSourceId() + "&" + Constants.SIGNET_SUBJECT_ID_HTTPPARAMNAME + "=" + listSubject.getId(), "UTF-8")%>'))">
        
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