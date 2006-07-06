<%-- @annotation@
		  Displays a Composite from the perspective of a group which is a factor of the of the composite
--%><%--
  @author Gary Brown.
  @version $Id: compositeAsFactorView.jsp,v 1.1 2006-07-06 15:06:32 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
 <div class="composite">
 <tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="viewObject" beanProperty="owner"/>
	  <tiles:put name="view" value="compositeOwner"/>
  </tiles:insert>
  <tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="viewObject" beanProperty="owner"/>
	  <tiles:put name="view" value="linkGroupMembers"/>
	  <tiles:put name="linkKey" value="groups.composite-member.composed-as"/>
  </tiles:insert>
<div class="compositeDef">
<div class="compositeLeft">
<tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="viewObject" beanProperty="leftGroup"/>
	  <tiles:put name="view" value="compositeMember"/>
  </tiles:insert>
</div> 
<div class="compositeType">
<c:out value="${viewObject.compositeType}"/>
</div>
<div class="compositeRight"> 
  <tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="viewObject" beanProperty="rightGroup"/>
	  <tiles:put name="view" value="compositeMember"/>
  </tiles:insert>
 </div>
 </div>
 </div>