<%-- @annotation@
        Standard tile which displays help text for group math
--%><%--
  @author Gary Brown.
  @version $Id: groupMathHelp.jsp,v 1.3 2008-04-16 01:10:19 mchyzer Exp $
--%>

<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<h2 id="groupMathHelp">Group math</h2>
<p>Grouper allows the membership of a group to be defined as the <em>union</em> (or), 
  <em>intersection</em> (and) or <em>complement</em> (not) of two other groups. This special 
  type of member is known as a <em>Composite</em> member. A composite member has 
  two <em>Factor</em> groups. </p>
<p>Take two <em>ordinary</em> groups:</p>
<ol>
  <li>fionas = Fiona Windsor, Fiona Benson, Fiona Tarbuck</li>
  <li>bensons= Keith Benson, Fiona Benson, Ian Benson</li>
</ol>
</grouper:recordTile>
<dl>
  <dt>fionas 
    <em><strong>union</strong></em> bensons= Fiona Windsor, Fiona Benson, Fiona 
    Tarbuck, Keith Benson, Ian Benson</dt>
  <dd><em><strong>union</strong></em> 
    indicates the result of <em><strong>adding</strong></em> the members of <em>fionas</em> 
    and <em>bensons</em>.</dd>
  <dt>fionas 
    <em><strong>intersection</strong> </em>bensons= Fiona Benson</dt>
  <dd><em><strong>intersection</strong></em> 
    indicates the <em><strong>members-in-common</strong></em> of <em>fionas</em> 
    and <em>bensons</em>.</dd>
  <dt>fionas 
    <em><strong>complement</strong> </em>bensons= Fiona Windsor, Fiona Tarbuck</dt>
  <dd><em><strong>complement</strong></em> 
    indicates the members of <em>fionas</em> <em><strong>minus</strong></em> the 
    members of <em>bensons</em>. In this case the position, left or right, of 
    the groups is important. </dd>
</dl>

<p>A group can have a single composite member, or any number of entities (including 
  groups) as members, but not a combination, however, groups which have a composite 
  member can be used anywhere other groups can be used.</p>
