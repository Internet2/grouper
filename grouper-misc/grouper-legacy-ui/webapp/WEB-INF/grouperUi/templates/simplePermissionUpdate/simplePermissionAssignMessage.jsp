<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simplePermissionUpdate/simplePermissionAssignMessage.jsp -->

<c:if test="${permissionUpdateRequestContainer.assignmentStatusMessage != null}">
  <div class="noteMessage permissionAssignMessage">${permissionUpdateRequestContainer.assignmentStatusMessage}</div>
  <script>
    //hide this after it shows for a while
    function hidePermissionAssignMessage() {
      $(".permissionAssignMessage").hide('slow');
    }
    setTimeout("hidePermissionAssignMessage()", 5000);
  </script>
</c:if>

<!--  End simplePermissionUpdate/simplePermissionAssignMessage.jsp -->