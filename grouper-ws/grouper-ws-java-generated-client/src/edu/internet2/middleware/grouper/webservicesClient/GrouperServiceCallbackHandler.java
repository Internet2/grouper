/**
 * GrouperServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.3  Built on : Aug 10, 2007 (04:45:47 LKT)
 */
package edu.internet2.middleware.grouper.webservicesClient;


/**
 *  GrouperServiceCallbackHandler Callback class, Users can extend this class and implement
 *  their own receiveResult and receiveError methods.
 */
public abstract class GrouperServiceCallbackHandler {
    protected Object clientData;

    /**
     * User can pass in any object that needs to be accessed once the NonBlocking
     * Web service call is finished and appropriate method of this CallBack is called.
     * @param clientData Object mechanism by which the user can pass in user data
     * that will be avilable at the time this callback is called.
     */
    public GrouperServiceCallbackHandler(Object clientData) {
        this.clientData = clientData;
    }

    /**
     * Please use this constructor if you don't want to set any clientData
     */
    public GrouperServiceCallbackHandler() {
        this.clientData = null;
    }

    /**
     * Get the client data
     */
    public Object getClientData() {
        return clientData;
    }

    /**
     * auto generated Axis2 call back method for findStems method
     * override this method for handling normal response from findStems operation
     */
    public void receiveResultfindStems(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStemsResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from findStems operation
     */
    public void receiveErrorfindStems(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for getGroupsLite method
     * override this method for handling normal response from getGroupsLite operation
     */
    public void receiveResultgetGroupsLite(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroupsLiteResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from getGroupsLite operation
     */
    public void receiveErrorgetGroupsLite(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for deleteMember method
     * override this method for handling normal response from deleteMember operation
     */
    public void receiveResultdeleteMember(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMemberResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from deleteMember operation
     */
    public void receiveErrordeleteMember(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for groupDelete method
     * override this method for handling normal response from groupDelete operation
     */
    public void receiveResultgroupDelete(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDeleteResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from groupDelete operation
     */
    public void receiveErrorgroupDelete(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for stemSave method
     * override this method for handling normal response from stemSave operation
     */
    public void receiveResultstemSave(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSaveResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from stemSave operation
     */
    public void receiveErrorstemSave(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for findGroupsLite method
     * override this method for handling normal response from findGroupsLite operation
     */
    public void receiveResultfindGroupsLite(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsLiteResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from findGroupsLite operation
     */
    public void receiveErrorfindGroupsLite(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for deleteMemberLite method
     * override this method for handling normal response from deleteMemberLite operation
     */
    public void receiveResultdeleteMemberLite(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMemberLiteResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from deleteMemberLite operation
     */
    public void receiveErrordeleteMemberLite(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for groupSaveLite method
     * override this method for handling normal response from groupSaveLite operation
     */
    public void receiveResultgroupSaveLite(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSaveLiteResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from groupSaveLite operation
     */
    public void receiveErrorgroupSaveLite(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for groupSave method
     * override this method for handling normal response from groupSave operation
     */
    public void receiveResultgroupSave(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSaveResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from groupSave operation
     */
    public void receiveErrorgroupSave(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for stemDeleteLite method
     * override this method for handling normal response from stemDeleteLite operation
     */
    public void receiveResultstemDeleteLite(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDeleteLiteResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from stemDeleteLite operation
     */
    public void receiveErrorstemDeleteLite(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for addMember method
     * override this method for handling normal response from addMember operation
     */
    public void receiveResultaddMember(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from addMember operation
     */
    public void receiveErroraddMember(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for viewOrEditAttributes method
     * override this method for handling normal response from viewOrEditAttributes operation
     */
    public void receiveResultviewOrEditAttributes(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditAttributesResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from viewOrEditAttributes operation
     */
    public void receiveErrorviewOrEditAttributes(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for getMembershipsLite method
     * override this method for handling normal response from getMembershipsLite operation
     */
    public void receiveResultgetMembershipsLite(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembershipsLiteResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from getMembershipsLite operation
     */
    public void receiveErrorgetMembershipsLite(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for stemDelete method
     * override this method for handling normal response from stemDelete operation
     */
    public void receiveResultstemDelete(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDeleteResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from stemDelete operation
     */
    public void receiveErrorstemDelete(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for getMembers method
     * override this method for handling normal response from getMembers operation
     */
    public void receiveResultgetMembers(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembersResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from getMembers operation
     */
    public void receiveErrorgetMembers(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for groupDeleteLite method
     * override this method for handling normal response from groupDeleteLite operation
     */
    public void receiveResultgroupDeleteLite(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDeleteLiteResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from groupDeleteLite operation
     */
    public void receiveErrorgroupDeleteLite(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for addMemberLite method
     * override this method for handling normal response from addMemberLite operation
     */
    public void receiveResultaddMemberLite(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberLiteResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from addMemberLite operation
     */
    public void receiveErroraddMemberLite(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for stemSaveLite method
     * override this method for handling normal response from stemSaveLite operation
     */
    public void receiveResultstemSaveLite(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSaveLiteResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from stemSaveLite operation
     */
    public void receiveErrorstemSaveLite(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for viewOrEditAttributesLite method
     * override this method for handling normal response from viewOrEditAttributesLite operation
     */
    public void receiveResultviewOrEditAttributesLite(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditAttributesLiteResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from viewOrEditAttributesLite operation
     */
    public void receiveErrorviewOrEditAttributesLite(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for findStemsLite method
     * override this method for handling normal response from findStemsLite operation
     */
    public void receiveResultfindStemsLite(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStemsLiteResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from findStemsLite operation
     */
    public void receiveErrorfindStemsLite(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for getMemberships method
     * override this method for handling normal response from getMemberships operation
     */
    public void receiveResultgetMemberships(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembershipsResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from getMemberships operation
     */
    public void receiveErrorgetMemberships(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for hasMemberLite method
     * override this method for handling normal response from hasMemberLite operation
     */
    public void receiveResulthasMemberLite(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMemberLiteResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from hasMemberLite operation
     */
    public void receiveErrorhasMemberLite(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for hasMember method
     * override this method for handling normal response from hasMember operation
     */
    public void receiveResulthasMember(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMemberResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from hasMember operation
     */
    public void receiveErrorhasMember(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for viewOrEditPrivilegesLite method
     * override this method for handling normal response from viewOrEditPrivilegesLite operation
     */
    public void receiveResultviewOrEditPrivilegesLite(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditPrivilegesLiteResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from viewOrEditPrivilegesLite operation
     */
    public void receiveErrorviewOrEditPrivilegesLite(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for viewOrEditPrivileges method
     * override this method for handling normal response from viewOrEditPrivileges operation
     */
    public void receiveResultviewOrEditPrivileges(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditPrivilegesResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from viewOrEditPrivileges operation
     */
    public void receiveErrorviewOrEditPrivileges(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for getGroups method
     * override this method for handling normal response from getGroups operation
     */
    public void receiveResultgetGroups(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroupsResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from getGroups operation
     */
    public void receiveErrorgetGroups(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for getMembersLite method
     * override this method for handling normal response from getMembersLite operation
     */
    public void receiveResultgetMembersLite(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembersLiteResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from getMembersLite operation
     */
    public void receiveErrorgetMembersLite(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for findGroups method
     * override this method for handling normal response from findGroups operation
     */
    public void receiveResultfindGroups(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from findGroups operation
     */
    public void receiveErrorfindGroups(java.lang.Exception e) {
    }
}
