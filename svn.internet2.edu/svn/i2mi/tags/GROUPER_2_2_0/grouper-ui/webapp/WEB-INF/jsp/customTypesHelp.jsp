<%-- @annotation@
        Standard tile which displays help text for composite types
--%><%--
  @author Gary Brown.
  @version $Id: customTypesHelp.jsp,v 1.3 2008-04-26 01:47:47 mchyzer Exp $
--%>

<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<h2>Custom group types</h2>
<p>It is possible for sites to define custom attributes for groups 
(Grouper administrators can see <a target="_blank" href="https://wiki.internet2.edu/confluence/display/GrouperWG/Custom+Group+Types%2C+Fields%2C+Attributes%2C+Lists">wiki</a> for more info). 
A special 
  kind of attribute, a list, is a collection of entities, similar to a group's 
  membership list. If a group has list attributes, and you have READ or WRITE 
  privilege for the list, the Grouper UI will let you manage the list in a similar 
  way to a group's membership list. A custom list cannot have a direct composite 
  member, however, it can have as members, groups which have composite members.</p>

</grouper:recordTile>