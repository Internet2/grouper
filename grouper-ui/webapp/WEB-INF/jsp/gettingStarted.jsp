<%-- @annotation@
		 Tile which gives a basic introduction to Grouper - used on the splash page
		 and on the help page
--%><%--
  @author Gary Brown.
  @version $Id: gettingStarted.jsp,v 1.5 2006-07-19 11:07:59 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}"> 
<h2>Grouper - intro</h2>
<p>Grouper is a system for creating and maintaining institutional groups in a 
  central repository. Such groups may be used for many different reasons e.g. 
  for mailing lists, or for determining which set of people are allowed to access 
  specific web applications, or for sharing resources. The goal is to create a 
  group once, but use it as often as necessary in as wide a range of systems as 
  possible. </p>
<p> In order to use Grouper effectively you must first understand some key concepts:</p>
<dl>
  <dt>Group</dt>
  <dd>A group represents a collection of 'items' which are themselves considered 
    to be members of the group. </dd>
</dl>
<dl>
  <dt>Subject</dt>
  <dd>A subject is an abstraction for any 'item' which may be a member of a group. 
    A subject has a 'type' e.g. person or group. To specify that group B is a 
    member of group A is to specify that all members of group B are also members 
    of group A. In the future, other subject types may be available to define 
    computers or applications.</dd>
</dl>
<dl>
  <dt>Stem</dt>
  <dd>A stem is a name space in which groups exist. Stems are hierarchical so 
    one way of thinking about stems is as folders. Folders may contain subfolders 
    or files while stems can contain substems or groups. Stems can be used to 
    collect together related groups and provide a means of controlling access 
    to groups. Some examples of stems are: 
    <ul>
      <li>uob:faculties:artf:fren = University of Bristol: Faculties: Arts Faculty: 
        Department of French</li>
      <li>uob:personal:[username] = University of Bristol: Personal groups: [name]</li>
    </ul>
    In this web application, groups and stems are distinguished by the addition 
    of square brackets [] around group names.</dd>
</dl>
<dl>
  <dt>Privileges</dt>
  <dd>Grouper provides fine control over who can create stems and groups, who 
    can change the membership of a group, and who can grant privileges for specific 
    stems or groups to others. In fact, privileges are granted to subjects. By 
    granting a privilege to a subject which is a group, all members of that group 
    are granted the privilege (for as long as they are a member of the group).<br>
    <br>
    GrouperAll is a <em>special internal subject</em>. Any privilege granted to 
    GrouperAll is, in effect, granted to all subjects.<br/>
    <br/>
    <strong>Stem privileges</strong> </dd>
  <dd> 
    <dl>
      <dt>Create</dt>
      <dd>subject may create groups in this stem</dd>
      <dt>Stem</dt>
      <dd>subject may create stems in this stem</dd>
    </dl>
    <br/>
    <strong>Group privileges</strong> 
    <dl>
      <dt>Member</dt>
      <dd>subject is a member of this group</dd>
      <dt>Optin</dt>
      <dd>subject may elect to become a member of this group</dd>
      <dt>Optout</dt>
      <dd>subject may elect to stop being a member of this group</dd>
      <dt>View</dt>
      <dd>subject may see that this group exists</dd>
      <dt>Read</dt>
      <dd>subject may see the membership list for this group</dd>
      <dt>Update</dt>
      <dd>subject may modify the membership of this group</dd>
      <dt>Admin</dt>
      <dd>subject may modify the membership of this group, delete the group or 
        assign privileges for the group</dd>
    </dl>
  </dd>
</dl>
</grouper:recordTile> 