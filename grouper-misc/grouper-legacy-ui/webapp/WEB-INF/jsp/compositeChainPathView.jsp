<%-- @annotation@
		  Dynamic tile used to display a composite when part of a chain
--%><%--
  @author Gary Brown.
  @version $Id: compositeChainPathView.jsp,v 1.1 2006-07-06 15:08:15 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<div class="chainPathComposite">
<tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="viewObject" beanProperty="owner"/>
	  <tiles:put name="view" value="chainPath"/>
</tiles:insert> 
 

</div>
