<%-- @annotation@
		  A popup window which immediately closes. Part of
		  the debug mechanism - if dynamic tiles are shown
		  and a JSP editor defined, JSP file names are popup links
		  to an action which launches the editor. The idea is that the 
		  underlying page does not refresh when a link is clicked.
--%><%--
  @author Gary Brown.
  @version $Id: Editor.jsp,v 1.2 2006-07-19 11:07:44 isgwb Exp $
--%>
<script language="JavaScript">
window.close();
</script>