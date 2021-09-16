<%-- @annotation@
        Standard tile which displays help text for composite types
--%><%--
  @author Gary Brown.
  @version $Id: customTypesHelp.jsp,v 1.3 2008-04-26 01:47:47 mchyzer Exp $
--%>

<grouperlegacy:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<h2>Custom group types</h2>
<p>It is possible for sites to define custom attributes for groups 
(Grouper administrators can see <a target="_blank" href="https://spaces.internet2.edu/x/QIbd">wiki</a> for more info). 
A special 
  kind of attribute, a list, is a collection of entities, similar to a group's 
  membership list. If a group has list attributes, and you have READ or WRITE 
  privilege for the list, the Grouper UI will let you manage the list in a similar 
  way to a group's membership list. A custom list cannot have a direct composite 
  member, however, it can have as members, groups which have composite members.</p>

</grouperlegacy:recordTile>