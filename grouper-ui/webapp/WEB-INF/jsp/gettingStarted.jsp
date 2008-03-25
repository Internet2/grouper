<%-- @annotation@
     Tile which gives a basic introduction to Grouper - used on the splash page
     and on the help page
--%><%--
  @author Gary Brown.
  @version $Id: gettingStarted.jsp,v 1.7 2008-03-25 14:59:51 mchyzer Exp $
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
    <strong>Creation privileges</strong> </dd>
  <dd> 
    <dl>
      <dt>Create Group</dt>
      <dd>Entity may create groups in this folder</dd>
      <dt>Create Folder</dt>
      <dd>Entity may create subfolders in this folder</dd>
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
    </dl>
  </dd>
</dl>
</grouper:recordTile> 