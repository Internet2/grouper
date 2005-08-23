<%-- @annotation@
			Tile which displays general help about the UI
--%><%--
  @author Gary Brown.
  @version $Id: generalHelp.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">

<h2>Menu</h2>
<dl><dt>My Memberships</dt><dd>lets you find groups of which you are a member</dd>
<dt>Create Groups</dt><dd>lets you create groups in stems where you have <strong>Create privilege</strong>,and stems within stems where you have <strong>Stem privilege</strong></dd>
<dt>Manage Groups</dt><dd>lets you find existing groups where you have <strong>Admin privilege</strong> or <strong>Update privilege</strong></dd>
<dt>Join Groups</dt><dd>lets you find groups where you have <strong>Optin privilege</strong></dd>
<dt>All Groups</dt><dd>lets you explore all stems and lets you see groups where you have <strong>View privilege</strong>. There may
be many thousands of stems and groups in Grouper.</dd></dl>

<h2>Finding groups</h2>
Grouper provides several ways of finding groups<br/><br/>
<dl><dt>Browsing</dt><dd>click on stems to find child stems and groups:</dd>
<dt>Listing</dt><dd>with the exception of <em>All Groups</em> it is possible to hide stems and simply show the list of groups
for a particular section. This may work well when there are relatively few groups</dd>
<dt>Searching</dt><dd>substring searching of group names below a selected stem is provided</dd></dl>


<h2>Finding subjects</h2>
Currently subjects are limited to <em>people</em> and <em>groups</em>. They can both be found by:<br/><br/>
<dl><dt>Browsing</dt><dd>click on stems to find child groups. You can also click on a group to expand its membership list and select subjects from that list</dd>

<dt>Searching</dt><dd>Contact Directory style searching of people and substring searching of group names below a selected stem are	 provided</dd></dl>
</grouper:recordTile>
  


