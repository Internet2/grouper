<%-- @annotation@
     Tile which gives a basic introduction to Grouper - used on the splash page
     and on the help page
--%><%--
  @author Gary Brown.
  @version $Id: gettingStarted.jsp,v 1.10 2008-10-22 03:24:00 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}"> 
<h2>Grouper - intro</h2>
<p>Grouper is a system for creating and maintaining institutional groups in a 
  central repository. Such groups may be used for many different purposes 
  e.g. for mailing lists, or for determining which set of people are allowed to 
  access specific web applications, or for sharing resources. The goal is to create 
  a group once, but use it as often as necessary in as wide a range of systems 
  as possible. </p>
<p> In order to use Grouper effectively you must first understand some key concepts:</p>
<dl>
  <dt>Group</dt>
  <dd>A group represents a collection of 'items' or entities which are themselves considered 
    to be members of the group. </dd>
</dl>
<dl>
  <dt>Entity</dt>
  <dd>An entity is an abstraction for any 'item' which may be a member of a group. 
    An entity has a 'type' e.g. person or group. To specify that group B is a 
    member of group A is to specify that all members of group B are also members 
    of group A. In the future, other entity types may be available to define 
    computers or applications.</dd>
</dl>
<dl>
  <dt>Membership</dt>
  <dd>A specific relationship between an entity and a group.</dd>
</dl>
<dl>
  <dt>Folder</dt>
  <dd>A folder is a name space or container in which groups exist. Folders are hierarchical 
    and may contain subfolders or groups. Folders can be used to 
    collect together related groups and provide a means of controlling access 
    to groups. Some examples of folders are: 
    <ul>
      <li>uob>faculties>artf:fren = University of Bristol> Faculties> Arts Faculty> 
        Department of French</li>
      <li>uob>personal>[username] = University of Bristol> Personal groups> [name]</li>
    </ul>
    In this web application, groups and folders are distinguished by the addition 
    of square brackets [] around group names.</dd>
</dl>
<dl>
  <dt>Privileges</dt>
  <dd>Grouper provides fine control over who can create folders and groups, who 
    can change the membership of a group, and who can grant privileges for specific 
    folders or groups to others. In fact, privileges are granted to entities. By 
    granting a privilege to an entity which is a group, all members of that group 
    are granted the privilege (for as long as they are a member of the group).<br>
    <br>
    EveryEntity is a <em>special internal entity</em>. Any privilege granted to 
    EveryEntity is, in effect, granted to all entities.<br>
    <br>
    GrouperSysAdmin is also a special internal entity which has implicit admin 
    privileges for folders and groups.<br>
    <br>
    A <em>SysAdmin</em> group, if defined, conveys implicit GrouperSysAdmin privileges 
    to its members. Members of this group, by default, act as themselves with 
    privileges limited to those assigned to them. This UI allows SysAdmin group members 
    to opt to <em>Act as admin</em>.<br/>
    <br/>
    <strong>Folder privileges</strong> </dd>
  <dd> 
    <dl>
      <dt>Create</dt>
      <dd>Entity may create groups, attributes, and subfolders in this folder</dd>
      <dt>Admin</dt>
      <dd>Entity may create groups, attributes, and subfolders in this folder, delete this folder, or assign any privilege to any entity</dd>
      <dt>Attribute read</dt>
      <dd>Entity may see the attributes for this folder</dd>
      <dt>Attribute update</dt>
      <dd>Entity may modify the attributes of this folder</dd>
    </dl>
    <br/>
    <strong>Group privileges</strong> 
    <dl>
      <dt>Member</dt>
      <dd>Entity is a member of this group</dd>
      <dt>Optin</dt>
      <dd>Entity may elect to join this group</dd>
      <dt>Optout</dt>
      <dd>Entity may elect to leave this group</dd>
      <dt>View</dt>
      <dd>Entity may see that this group exists</dd>
      <dt>Read</dt>
      <dd>Entity may see the membership list for this group</dd>
      <dt>Update</dt>
      <dd>Entity may modify the membership of this group</dd>
      <dt>Admin</dt>
      <dd>Entity may modify the membership of this group, delete the group or 
        assign privileges for the group</dd>
      <dt>Attribute read</dt>
      <dd>Entity may see the attributes for this group</dd>
      <dt>Attribute update</dt>
      <dd>Entity may modify the attributes of this group</dd>
    </dl>
  </dd>
</dl>
<h2>Grouper end-to-end scenarios</h2>
<dl>
  <a name="findFolderNavigationAnchor"></a>
  <dt><li> <a href="#" 
  onclick="return grouperHideShow(event, 'findFolderNavigation');">Find 
  a folder or a group by navigation</a></dt>
  <dd id="findFolderNavigation0" style="display:none;visibility:hidden;">
  (requires VIEW privilege or greater)
  <ol><li> Click "Explore" in the "My Tools" segment of the left menu</li>
  <li> Click any folder name in the "Browse or list groups" panel to show
the contents of that folder.  Continue clicking through folder names to move through the folder hierarchy.
  <ul><li> If your current location is not "Root", you can click the Root
folder label to go to the top folder</li>
      <li> If you cannot see a folder or group, you may lack permission to
view any of its contents, or the folder could be in a different
location.   Try searching for a specific group contained in that
folder, or contact your administrator.</li>
      <li> Note that clicking a folder name may advance you</li>
  </ul></li>
  <li> Click any group name in the "Browse or list groups" panel to see
the Group Summary screen for that group</li>
  </ol>
  </dd>
  <a name="findEntitySearchAnchor"></a>
  <dt><li> <a href="#" 
  onclick="return grouperHideShow(event, 'findEntitySearch');">Find an entity or a group by searching</a></dt>
  <dd id="findEntitySearch0" style="display:none;visibility:hidden;">(requires VIEW privilege or greater)
    <ol><li> Click "Search" in the "My Tools" segment of the left menu</li>
     <li> In the "Search people or groups" panel, type a search term</li>
     <li> Click the "Search" button, or press the Return key, to submit.  A
search results screen will appear.</li>
     <li> In the results list, click an entity name or a group path to view
the Entity Details screen for your selection.
       <ul><li>   If you have selected a group, you can click "View Group Summary"
near the bottom of the summary panel to go to the Group Summary screen, which includes additional 
options for working with information related to that group.  (The options shown will be based upon 
your privilege level for the group.)</li>
        <li> If you cannot see an entity or group, you may lack permission to
view it.   Try modifying your search, or contact your administrator.</li></ul>
     </li></ol></dd>
     <a name="readGroupMembershipListAnchor"></a>
<dt><li>       <a href="#" 
  onclick="return grouperHideShow(event, 'readGroupMembershipList');">Read a group's membership list</a></dt>
<dd id="readGroupMembershipList0" style="display:none;visibility:hidden;">(requires READ privilege or greater)
<ol><li>      Find the Group Summary screen for the group by <a href="#" 
  onclick="grouperHideShow(event, 'findFolderNavigation', true);return goToAnchor('findFolderNavigationAnchor')">navigating</a> or 
  <a href="#" 
  onclick="grouperHideShow(event, 'findEntitySearch', true);return goToAnchor('findEntitySearchAnchor');">searching</a>.</li>
<li>      Click "Manage members" near the bottom of the summary panel.  The
Members screen will appear.
<ul><li>       You may choose to view indirect, direct, or all members of the list
by selecting the appropriate radio button at the top of the "Membership list" panel and then clicking the "Change display" button
</li></ul></li></ol>
</dd>
<a name="addMemberToGroupAnchor"></a>
<dt><li>       <a href="#" 
  onclick="return grouperHideShow(event, 'addMemberToGroup');">Add a member (entity or group) to a group</a></dt>
<dd id="addMemberToGroup0" style="display:none;visibility:hidden;">(requires UPDATE privilege or greater)
<ol><li>      <a href="#" 
  onclick="grouperHideShow(event, 'readGroupMembershipList', true);return goToAnchor('readGroupMembershipListAnchor');">Go to the group's membership list page</a></li>
<li>      Click "Add member" at the bottom of the "Membership list" panel.
The "Assign privileges / Add members" screen will appear.</li>
<li>      In the field in the "Search people or groups" panel, type the
search criteria for the member you want to add, then click the
"Search" button.   Your search results will appear.
        <ul><li>       Note that in the privileges portions of the results panel, the
MEMBER privilege is selected by default.  You may use the neighboring checkboxes to assign additional privileges to the entities you select.</li>
</ul></li>
<li>      Select each member you want to add by clicking the checkbox next to
the member listing, then click the "Assign privileges" button at the bottom of the panel.</li></ol>
</dd>
<dt><li>      <a href="#" 
  onclick="return grouperHideShow(event, 'removeMemberFromGroup');">Remove a member from a group</a></dt>
<dd id="removeMemberFromGroup0" style="display:none;visibility:hidden;">(requires UPDATE privilege or greater)
<ol><li>      <a href="#" 
  onclick="grouperHideShow(event, 'readGroupMembershipList', true);return goToAnchor('readGroupMembershipListAnchor');">Go to the group's membership list page</a></li>
<li>      Select the member(s) you want to remove by clicking the checkbox
next to the member listing(s).</li>
<li>      Click the "Remove selected members" button</li>
</ol>
</dd>
<dt><li>      <a href="#" 
  onclick="return grouperHideShow(event, 'assignManagerToGroup');">Assign someone to be able to manage a group</a></dt>
<dd id="assignManagerToGroup0" style="display:none;visibility:hidden;">(requires ADMIN privilege or greater)
<ol><li>      <a href="#" 
  onclick="grouperHideShow(event, 'readGroupMembershipList', true);return goToAnchor('readGroupMembershipListAnchor');">Go to the group's membership list page</a></li>
<li>      Click "Add member" at the bottom of the "Membership list" panel.</li>
The "Assign privileges / Add members" screen will appear.
<li>      In the field in the "Search people or groups" panel, type the
search criteria for the member you want to add, then click the
"Search" button.   Your search results will appear.</li>
<li>      In the privileges portions of the results panel, select the
checkbox for "update" [can modify group membership] or "admin" [can modify group membership, change group name, or delete the group].
        <ul><li>       Choose the simplest permission that will suffice, keeping in mind
that anyone with ADMIN privilege can rename or delete the group, whether intentionally or accidentally.</li>
        <li>       If the user can manage the group, but is not a member of the group,
unselect the checkbox for "member".</li></ul></li>
<li>      Select each entity you want to receive the designated privilege by
clicking the checkbox next to the entity listing, then click the "Assign privileges" button at the bottom of the panel.</li></ol>
</dd>
     <a name="createNewGroupAnchor"></a>

<dt><li>        <a href="#" 
  onclick="return grouperHideShow(event, 'createNewGroup');">Create a new group</a></dt>
<dd id="createNewGroup0" style="display:none;visibility:hidden;">(requires CREATE privilege or greater)
<ol><li>      <a href="" 
  onclick="grouperHideShow(event, 'findFolderNavigation', true);return goToAnchor('findFolderNavigationAnchor');">Find a parent folder</a>
   for the group.  This
should place you on the Browse Groups Hierarchy screen
        <ul><li>       If you have permissions to create a group in this folder, you will
see the "Manage folders" panel at the bottom of the page.  If you do not see this panel, contact your administrator.
</li></ul></li>
<li>      Click "Create Group" at the bottom of the "Manage folders" panel.
The Create Group screen will appear.
        <ul><li>        You can mouse over the field labels to learn more about what to
enter in each field</li></ul></li>
<li>      Fill the fields in the panel, then click "Save" to create the group.
        <ul><li>        If you click the "Add members" button, your new group will be
saved and you can add members to the group.</li></ul></li>
</ol>
</dd>
<dt><li> <a href="#" 
  onclick="return grouperHideShow(event, 'createCompositeGroup');">Create a composite group</a></dt>
<dd id="createCompositeGroup0" style="display:none;visibility:hidden;">(requires CREATE privilege or greater) 
Grouper allows you to use two existing groups (called "factors") to define a third (composite) group.  
You may combine two groups in the following ways:
<ul><li> UNION includes all members of the two original (factor) groups -- "adding"</li>
  <li> INTERSECTION includes entities that belong to both of two original (factor) groups -- "members-in-common"</li>
  <li> COMPLEMENT includes entities that belong to the primary ("left) factor group who are not also members of 
  the secondary ("right") factor group -- "left minus right"</li></ul>
To create a composite group:
<ol><li>      Place each of your two factor groups in the Group Workspace
        <ol><li type="a">     Find or create each factor group
                <ul><li>       Find a group by <a href="#" 
  onclick="grouperHideShow(event, 'findFolderNavigation', true);return goToAnchor('findFolderNavigationAnchor');">navigating</a> or 
  <a href="#" 
  onclick="grouperHideShow(event, 'findEntitySearch', true);return goToAnchor('findFolderNavigationAnchor');">searching</a>, and
proceed to the Group Summary page.</li>
                <li>       Create a factor group using the steps above to <a href="#" 
  onclick="grouperHideShow(event, 'createNewGroup', true);return goToAnchor('createNewGroupAnchor');">create a
group</a> and <a href="#" 
  onclick="grouperHideShow(event, 'addMemberToGroup', true);return goToAnchor('addMemberToGroupAnchor');">assign members</a> to it.  After you assign new members, click "Group Summary" at the bottom of the "Assign Privileges/Add Members"
page to proceed.</li></ul>
        <li type="a">     Click "Add to Group Workspace" at the bottom of the summary panel.</li>
        <li type="a">     Confirm that both factor groups are in the Group Workspace by
clicking "Group Workspace" in the "My Tools" segment of the left menu.</li></ol></li>
<li>      Combine the factor groups to make a new, third group (composite)
        <ol><li type="a">     <a href="#" 
  onclick="grouperHideShow(event, 'findFolderNavigation', true);return goToAnchor('findFolderNavigationAnchor');">Find a parent folder</a> for the group.  This
should place you on the Browse Groups Hierarchy screen
                <ul><li>       If you have permissions to create a group in this folder, you will
see the "Manage folders" panel at the bottom of the page.  If you do not see this panel, contact your administrator.</li></ul></li>
        <li type="a">     Click "Create Group" at the bottom of the "Manage folders"
panel.  The Create Group screen will appear.
                <ul><li>        You can mouse over the field labels to learn more about what to
enter in each field</li></ul></li>
        <li type="a">     Fill the fields in the panel, then click "Make composite" to
begin creating a composite group.  The "Create composite group" panel will appear.</li>
        <li type="a">     Use the pulldown lists to select the two factor groups and how
you wish to combine them.
                <ul><li>       The groups appearing in the pulldown lists are those in your Group
Workspace</li>
                <li>       Assigning "Left group" and "Right group" will only matter if you
are using COMPLEMENT ("left minus right") to combine the groups.</li></ul></li>
        <li type="a">     Click the "Create composite group" button at the bottom of the
panel to create the new composite group.</li></ol></li></ol>
</dd>
<dt><li>      <a href="#" 
  onclick="return grouperHideShow(event, 'createNewFolder');">Create a new folder</a></dt>
<dd id="createNewFolder0" style="display:none;visibility:hidden;">(requires CREATE privilege or greater)
<ol><li>      <a href="#" 
  onclick="grouperHideShow(event, 'findFolderNavigation', true);return goToAnchor('findFolderNavigationAnchor');">Find a parent folder</a>
   for the folder you will be creating.  This should place you on the Browse Groups Hierarchy screen
        <ul><li>       If you have permissions to create a folder in this folder, you will
see the "Manage folders" panel at the bottom of the page.  If you do not see this panel, contact your administrator.</li></ul></li>
<li>      Click "Create Folder" at the bottom of the "Manage folders" panel.
The Create Folder screen will appear.
        <ul><li>        You can mouse over the field labels to learn more about what to
enter in each field</li></ul></li>
<li>      Fill the fields in the panel, then click "Save" to create the group.
        <ul><li>        If you click the "Add members" button, your new group will be
saved and can add search for and members to the group</li></ul></li></ol>
</dd>
<dt><li>     <a href="#" 
  onclick="return grouperHideShow(event, 'assignFolderManager');">Assign someone to be 
  able to create new folders or groups within a
parent folder</a></dt>
<dd id="assignFolderManager0" style="display:none;visibility:hidden;">(requires ADMIN privilege)
<ol><li>      <a href="#" 
  onclick="grouperHideShow(event, 'findFolderNavigation', true);return goToAnchor('findFolderNavigationAnchor');">Find a parent folder</a> 
  for the folder you will be creating.  
This should place you on the Browse Groups Hierarchy screen
        <ul><li>       If you have permissions to assign privileges in this folder, you
will see the "Manage folders" panel at the bottom of the page.  If you do not see this panel, contact your administrator.
</li></ul></li>
<li>      Click the "Show Entities with" at the bottom of the "Manage
folders" panel.  The "Current entities with [Create] privilege"
screen will appear.</li>
<li>      Click "Assign this privilege" at the bottom of the "Entity list
filtered by privilege" panel.  The "Assign creation privileges for [group name]" screen will appear.</li>
<li>      In the field in the "Search people or groups" panel, type the
search criteria for the member you want to add, then click the
"Search" button.   Your search results will appear.
        <ul><li>       Note that in the privileges portions of the results panel, the
CREATE privilege is selected by default.  You may also use the ADMIN, ATTRIBUTE READ, or ATTRIBUTE UPDATE checkboxes to assign privileges to the entities you select.</li>
        <li>       The folder privileges you grant apply to the parent folder only,
and not to any subfolders contained within it (i.e. there is no hierarchical inheritance of folder privileges by default)</li>
</ul></li></ol>
  </dd>
</dl>

</grouper:recordTile> 
