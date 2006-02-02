<%-- @annotation@ 
	Displays single step in chain of effective memberships by which a subject is a member of a group.
	Called from chainPath which iterates over all steps	
 --%><%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<li><tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="group"/>
	  <tiles:put name="view" value="chainPath"/>
  </tiles:insert> <c:out value="${linkSeparator}" escapeXml="false"/>
  
   	<tiles:insert definition="dynamicTileDef" flush="false">
		<tiles:put name="viewObject" beanName="currentSubject"/>
		<tiles:put name="view" value="isMemberOf"/>
		<tiles:put name="params" beanName="params"/>
	  	<tiles:put name="linkTitle" value="${navMap['groups.membership.through.title']} ${group.desc}"/>
	</tiles:insert>
</li>
</grouper:recordTile>