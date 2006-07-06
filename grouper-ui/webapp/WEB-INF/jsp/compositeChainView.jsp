<%-- @annotation@
		  Dynamic tile used to render the detail of a composite group
--%><%--
  @author Gary Brown.
  @version $Id: compositeChainView.jsp,v 1.1 2006-07-06 15:09:40 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<div class="chainComposite">

<fmt:message bundle="${nav}" key="groups.composite-member.indicator"/>
 
<div class="compositeLeft">
<tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="viewObject" beanProperty="leftGroup"/>
	  <tiles:put name="view" value="compositeGroupChainMember"/>
</tiles:insert>

</div> 
<div class="compositeType">
<c:out value="${viewObject.compositeType}"/>
</div>
<div class="compositeRight"> 
<tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="viewObject" beanProperty="rightGroup"/>
	  <tiles:put name="view" value="compositeGroupChainMember"/>
</tiles:insert>
 </div>
</div>
