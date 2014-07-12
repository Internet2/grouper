<%-- @annotation@ Moved visible body parts here expect sites to copy and 
change definition for this --%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
	<tiles:importAttribute ignore="true"/>
	<body>
	<%-- note, the tooltips require the javascript to be put there, right below body tag --%>
  <script type="text/javascript" src="grouperExternal/public/assets/js/wz_tooltip.js"></script> 
   		<!--ContentSpace-->
        <div id="ContentSpace">
            <div id="TitleBox">
        <noscript>
          <h3 style="color: #990000">${textContainer.text['indexNoJavascript'] }</h3>    
        </noscript>
               <tiles:insert attribute="title" />
            </div>
			
            <c:if test="${!empty message || !empty messages}">
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
				<div id="Sidebar" class="printerFriendlyNot">
					<tiles:insert attribute="menu" />
				</div><!--/SideBar-->
			</c:if>
			<tiles:insert attribute="internet2spons"/>
			<!--Header-->
			 <div id="Header" class="printerFriendlyNot">
				<tiles:insert attribute="header" />
			 </div><!--/Header-->
    		<!--Footer-->
			<div id="Footer">
				<tiles:insert attribute="footer" />
			</div><!--Footer-->
    </div><!--/ContentSpace--> 
</grouper:recordTile>