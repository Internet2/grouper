<%-- @annotation@
			Tile which displays general help about the UI
--%><%--
  @author Gary Brown.
  @version $Id: generalHelp.jsp,v 1.6 2005-12-08 15:33:28 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}"> 
<h2>Menu</h2>
<dl>
  <dt>My Memberships</dt>
  <dd>lets you find groups of which you are a member</dd>
  <dt>Create Groups</dt>
  <dd>lets you create groups in stems where you have <strong>Create privilege</strong>,and 
    stems within stems where you have <strong>Stem privilege</strong></dd>
  <dt>Manage Groups</dt>
  <dd>lets you find existing groups where you have <strong>Admin privilege</strong> 
    or <strong>Update privilege</strong></dd>
  <dt>Join Groups</dt>
  <dd>lets you find groups where you have <strong>Optin privilege</strong></dd>
  <dt>All Groups</dt>
  <dd>lets you explore all stems and lets you see groups where you have <strong>View 
    privilege</strong>. There may be many thousands of stems and groups in Grouper.</dd>
	<dt>Search Subjects</dt>
  <dd>Lets you search for any subject known to Grouper. Allows a Subject centric approach i.e. you
  can list all groups where the Subject is a member or has an Access privilege.</dd>
</dl>
<h2>Finding groups</h2>
Grouper provides several ways of finding groups<br/>
<br/>
<dl>
  <dt>Browsing</dt>
  <dd>click on stems to find child stems and groups:</dd>
  <dt>Listing</dt>
  <dd>with the exception of <em>All Groups</em> it is possible to hide stems and 
    simply show the list of groups for a particular section. This may work well 
    when there are relatively few groups</dd>
  <dt>Searching</dt>
  <dd>case-insensitive substring searching of group names below a selected stem 
    is provided. There is an advanced search screen which gives the user more 
    control over which attributes are searched.</dd>
</dl>
<h2>Finding subjects</h2>
Currently subjects are limited to <em>people</em> and <em>groups</em>. They can 
both be found by:<br/>
<dl>
  <dt>Browsing</dt>
  <dd>click on stems to find child groups. You can also click on a group to expand 
    its membership list and select subjects from that list</dd>
  <dt>Searching</dt>
  <dd>the user interface allows the user to enter a query string which is used 
    to match Subjects. How the query string is interpreted depends on the specific 
    implementation(s) of the Subject API present in the Grouper installation. 
    The reference Grouper installation will return subjects where:
	
  <ol>
    <li>any 
      of the subject attributes are an exact* match for the query string</li>
    <li>the 
      subject is not a person and the the query string is a substring of a subject 
      attribute.</li>
    <li>the 
      subject is a person and:
      <ol>
        <li>the 
          query string is an exact match for a fisrt name</li>
        <li> the query string is two terms e.g. ben fiona, and the second term 
          is an exact first name match, and the first term matches the start of 
          the subject's surname. </li>
      </ol>
    </li>
  </ol>

*searches are case-insensitive</dd>
</dl>


</grouper:recordTile>
