<%-- @annotation@
      Tile which displays general help about the UI
--%><%--
  @author Gary Brown.
  @version $Id: generalHelp.jsp,v 1.11 2008-04-12 03:51:00 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}"> 
<h2>Menu</h2>
<dl>
  <dt>My Memberships*</dt>
  <dd>lets you find groups of which you are a member</dd>
  <dd>&nbsp;&nbsp;&nbsp;-- groups where you have <strong>member</strong> privilege --</dd>
  <dt>Join Groups*</dt>
  <dd>lets you find groups that you are eligible to join</dd>
  <dd>&nbsp;&nbsp;&nbsp;-- groups where you have <strong>optin</strong> privilege --</dd>
  <dt></dt>
  <dt>Manage Groups*</dt>
  <dd>lets you find groups where you may update membership lists or assign privileges to others</dd>
  <dd>&nbsp;&nbsp;&nbsp;-- groups where you have <strong>update</strong> privilege or <strong>admin</strong> privilege --</dd>
  <dt></dt>
  <dt>Create Groups*</dt>
  <dd>lets you create new groups or (sub)folders, as permitted by location</dd>
  <dd>&nbsp;&nbsp;&nbsp;-- folders where you have <strong>admin </strong> privilege or <strong>create</strong> privilege --</dd>
  <dt>Explore</dt>
  <dd>lets you explore all groups that are visible to you</dd>
  <dd>&nbsp;&nbsp;&nbsp;-- groups where you have <strong>view</strong> privilege --</dd>
  <dt>Search</dt>
  <dd>Lets you search for any entity known to Grouper. Allows an entity-centric 
    approach i.e. you can list all groups where the entity is a member or has 
    an Access privilege, or folders where the entity has one of the Folder privileges.</dd>
  <dt>Group workspace</dt>
  <dd>As described below, Grouper provides several ways of finding groups. It 
    is possible, from the <em>Group Summary</em> page, to save, for the duration 
    of your session, a group in a list. This menu item provides quick access to 
    the groups throughout the session and provides a way of removing groups. The 
    list provides the means for selecting groups for <em>Group Math</em>, described 
    below. Saved groups are stored in the same list as saved entities (see belolw), 
    however, this menu item filters the list to return only groups.</dd>
  <dt>Entity workspace</dt>
  <dd>Grouper provides a <em>Entity summary</em> page which can be accessed from 
    many points in the UI. It is possible, from this page, to save, for the duration 
    of your session, an entity in a list. This menu item provides quick access 
    to the entities throughout the session and also provides a means to remove them.</dd>
  <dt>*</dt>
  <dd>These menu items filter the group hierarchy so that you see groups and folders 
    relevant to the task you want to perform. GrouperSystemAdmin can manage all groups 
    and folders and is not intended to be a group member, therefore, GrouperSystemAdmin 
    does not have access to these menu items. This is also true of SystemAdminGroup 
    members who have opted to <em>Act as admin</em>.</dd>
</dl>
<h2>Finding groups</h2>
Grouper provides several ways of finding groups<br/>
<br/>
<dl>
  <dt>Browsing</dt>
  <dd>click on folders to find subfolders and groups:</dd>
  <dt>Listing</dt>
  <dd>with the exception of <em>All Groups</em> it is possible to hide folders and 
    simply show the list of groups for a particular section. This may work well 
    when there are relatively few groups</dd>
  <dt>Searching</dt>
  <dd>case-insensitive substring searching of group names below a selected folder 
    is provided. There is an advanced search screen which gives the user more 
    control over which attributes are searched.</dd>
  
  <dt>Saved groups</dt>
  <dd>as described above, groups can be saved in a list in the session for quick 
    access by clicking on the appropriate menu item. </dd>
</dl>
<h2>Finding entities</h2>
Entities can be found by:<br/>
<dl>
  <dt>Browsing</dt>
  <dd>click on folders to find child groups. You can also click on a group to expand 
    its membership list and select entities from that list</dd>
  <dt>Searching</dt>
  <dd>the user interface allows the user to enter a query string which is used 
    to match entities. How the query string is interpreted depends on the specific 
    implementation(s) of the Entity API present in the Grouper installation. 
    The reference Grouper installation will return entities where:
  
  <ol>
    <li>any 
      of the entity attributes are an exact* match for the query string</li>
    <li>the 
      entity is not a person and the the query string is a substring of a entity 
      attribute.</li>
    <li>the 
      entity is a person and:
      <ol>
        <li>the 
          query string is an exact match for a fisrt name</li>
        <li> the query string is two terms e.g. ben fiona, and the second term 
          is an exact first name match, and the first term matches the start of 
          the entity's surname. </li>
      </ol>
    </li>
  </ol>

*searches are case-insensitive</dd>
  <dt>Saved entities</dt>
  <dd>as described above, entities can be saved in a list in the session for quick 
    access by clicking on the appropriate menu item. When looking for entities 
    to assign membership or privileges to, the list of saved entities can be displayed 
    for quick assignment to any entity in the list.</dd>
</dl>



<h2>Direct vs indirect</h2>
<p>Privileges and membership of a entity for a group (Group A) may be granted 
  <em>directly</em> to the entity, or may be <em>indirectly</em> derived because 
  the entity is a member of a group which has been granted a privilege for Group 
  A, or is, itself, a member of Group A. </p>
<p>The Grouper UI indicates whether a membership is direct, indirect or may, in 
  fact, have more than one source e.g. if entity A is a member of Group A and 
  Group B and both Group A and Group B are members of Group C, then entity A 
  has two memberships for Group C.</p>
  
<tiles:insert definition="groupMathHelpDef"/>
<tiles:insert definition="customTypesHelpDef"/>
</grouper:recordTile> 
