<%-- @annotation@
			Tile which displays general help about the UI
--%><%--
  @author Gary Brown.
  @version $Id: generalHelp.jsp,v 1.9 2006-07-20 10:32:45 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}"> 
<h2>Menu</h2>
<dl>
  <dt>My Memberships*</dt>
  <dd>lets you find groups of which you are a member</dd>
  <dt>Create Groups*</dt>
  <dd>lets you create groups in stems where you have <strong>Create privilege</strong>,and 
    stems within stems where you have <strong>Stem privilege</strong></dd>
  <dt>Manage Groups*</dt>
  <dd>lets you find existing groups where you have <strong>Admin privilege</strong> 
    or <strong>Update privilege</strong></dd>
  <dt>Join Groups*</dt>
  <dd>lets you find groups where you have <strong>Optin privilege</strong></dd>
  <dt>All Groups</dt>
  <dd>lets you explore all stems and lets you see groups where you have <strong>View 
    privilege</strong>. There may be many thousands of stems and groups in Grouper.</dd>
	<dt>Search Subjects</dt>
  <dd>Lets you search for any subject known to Grouper. Allows a Subject centric 
    approach i.e. you can list all groups where the Subject is a member or has 
    an Access privilege, or stems where the subject has a Naming privilege.</dd>
  <dt>Saved Groups</dt>
  <dd>As described below, Grouper provides several ways of finding groups. It 
    is possible, from the <em>Group Summary</em> page, to save, for the duration 
    of your session, a group in a list. This menu item provides quick access to 
    the groups throughout the session and provides a way of removing groups. The 
    list provides the means for selecting groups for <em>Group Math</em>, described 
    below. Saved groups are stored in the same list as saved subjects (see belolw), 
    however, this menu item filters the list to return only groups.</dd>
  <dt>Saved Subjects</dt>
  <dd>Grouper provides a <em>Subject Summary</em> page which can be accessed from 
    many points in the UI. It is possible, from this page, to save, for the duration 
    of your session, a subject in a list. This menu item provides quick access 
    to the subjects throughout the session and provides a means of removing subjects.</dd>
  <dt>*</dt>
  <dd>These menu items filter the group hierarchy so that you see groups and stems 
    relevant to the task you want to perform. GrouperSystem can manage all groups 
    and stems and is not intended to be a group member, therefore, GrouperSystem 
    does not have access to these menu items. This is also true of wheel group 
    members who have opted to <em>Act as admin</em>.</dd>
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
	
  <dt>Saved groups</dt>
  <dd>as described above, groups can be saved in a list in the session for quick 
    access by clicking on the appropriate menu item. </dd>
</dl>
<h2>Finding subjects</h2>
Subjects can be found by:<br/>
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
  <dt>Saved subjects</dt>
  <dd>as described above, subjects can be saved in a list in the session for quick 
    access by clicking on the appropriate menu item. When looking for subjects 
    to assign membership or privileges to, the list of saved subjects can be displayed 
    for quick assignment to any subject in the list.</dd>
</dl>



<h2>Direct vs indirect</h2>
<p>Privileges and membership of a subject for a group (Group A) may be granted 
  <em>directly</em> to the subject, or may be <em>indirectly</em> derived because 
  the subject is a member of a group which has been granted a privilege for Group 
  A, or is, itself, a member of Group A. </p>
<p>The Grouper UI indicates whether a membership is direct, indirect or may, in 
  fact, have more than one source e.g. if subject A is a member of Group A and 
  Group B and both Group A and Group B are members of Group C, then subject A 
  has two memberships for Group C.</p>

<tiles:insert definition="groupMathHelpDef"/>
<tiles:insert definition="customTypesHelpDef"/>
</grouper:recordTile> 
