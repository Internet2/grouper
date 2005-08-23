<%-- @annotation@
		  A popup window which immediately closes. Part of
		  the debug mechanism - if dynamic tiles are shown
		  and a JSP editor defined, JSP file names are popup links
		  to an action whch launches the editor. The idea is that the 
		  underlying page does not refresh when a link is clicked.
--%><%--
  @author Gary Brown.
  @version $Id: Editor.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<script language="JavaScript">
window.close();
</script>