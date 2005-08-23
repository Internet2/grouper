<%-- @annotation@ Form(s) for modifying attributes. This is a key page. Should 
some values be limited to:
1) A lookup list (how is this defined)
2) Subject to validation rules (how and where defined?)
3) Be dependent on earlier choices i.e. entered in several 
steps with page reload in between

Can save and 
1) return to GroupSummary
2) continue and view current members
3) continue to add members --%>
<%@include file="/WEB-INF/jsp/include.jsp"%>

<h3>This page does not do anything in this demo. It is a page which will need to be customised by 
institutions to cope with the group types they choose to implement</h3>
<html:form styleId="EditGroupAttributesForm" action="/saveGroupAttributes">
<html:hidden property="groupId"/>
<table border="0">
<tr>
    <td valign="top">Unit code</td>
    <td valign="top"><html:text property="groupAttrUnitCode" size="50" maxlength="50"/>
    </td>
</tr>
<tr>
    <td valign="top">Faculty code</td>
    <td valign="top"><html:text property="groupAttrFacultyCode" size="50" maxlength="50"/>
    </td>
</tr>

</table>
 <html:submit property="submit.save" value="${navMap['groups.action.attr-save']}"/>
 <html:submit property="submit.save_add" value="${navMap['groups.action.attr-save-add']}"/>
</html:form>

