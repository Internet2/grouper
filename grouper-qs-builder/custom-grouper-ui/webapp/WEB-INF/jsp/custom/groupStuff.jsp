<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<div class="groupStuff">
<b>At some future point, in a fully integrated system, you may be able to:</b>
<ul>
	<li>Email group members</li>
	<li>Check mail archive</li>
	<li>View a group calendar / schedule a meeting</li>
	<li>View a group task list</li>

	<li>Manage portal channels for the group</li>
	<li>Create resources for the group e.g. 
		<ul>
			<li>a Blackboard course for a group, or create a group within an existing Blackboard course</li>
			<li>a content area in a Content Management System</li>
			<li>a bulletin board or chat room</li>
			<li>a shared set of bookmarks / newsfeeds</li>
		</ul>
	</li>
</ul>
</div>
</grouper:recordTile>