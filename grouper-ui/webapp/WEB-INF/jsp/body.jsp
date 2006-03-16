<%-- @annotation@ Moved visible body parts here expect sites to copy and 
change definition for this --%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
	<tiles:importAttribute ignore="true"/>
	<body>
   		<!--ContentSpace-->
        <div id="ContentSpace">
            <div id="TitleBox">
               <tiles:insert attribute="title" />
            </div>
			
            <c:if test="${!empty message}">
                    <tiles:insert attribute="messageArea" />   
            </c:if>
            <!--content-->
            <div id="Content">
                <tiles:insert attribute='contentwrapper'>
					<tiles:put name="tile"><tiles:getAsString name="content"/></tiles:put>
				</tiles:insert>
            </div>
            <!--/content-->
        	<!--Right-->
			<div id="Right">
				<tiles:insert attribute="right" />
			</div><!--/Right-->
			<!--NavBar-->
			<div id="Navbar">
				<tiles:insert attribute='subheader'/>
			</div><!--/NavBar-->
			<!--Left-->
			<div id="left">
				<tiles:insert attribute="left" />
			</div><!--/Left-->
			<c:if test="${!empty authUser}">
				<!--SideBar-->
				<div id="Sidebar">
					<tiles:insert attribute="menu" />
				</div><!--/SideBar-->
			</c:if>
			<!--Header-->
			 <div id="Header">
				<tiles:insert attribute="header" />
			 </div><!--/Header-->
    
    </div><!--/ContentSpace--> 
			<!--Footer-->
			<div id="Footer">
				<tiles:insert attribute="footer" />
			</div><!--Footer-->
</grouper:recordTile>