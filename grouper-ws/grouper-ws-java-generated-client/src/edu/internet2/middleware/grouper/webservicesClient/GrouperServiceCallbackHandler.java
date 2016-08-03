
/**
 * GrouperServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1  Built on : Aug 31, 2011 (12:22:40 CEST)
 */

    package edu.internet2.middleware.grouper.webservicesClient;

    /**
     *  GrouperServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class GrouperServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public GrouperServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public GrouperServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for assignAttributeDefActions method
            * override this method for handling normal response from assignAttributeDefActions operation
            */
           public void receiveResultassignAttributeDefActions(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.AssignAttributeDefActionsResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from assignAttributeDefActions operation
           */
            public void receiveErrorassignAttributeDefActions(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for findStemsLite method
            * override this method for handling normal response from findStemsLite operation
            */
           public void receiveResultfindStemsLite(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.FindStemsLiteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from findStemsLite operation
           */
            public void receiveErrorfindStemsLite(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for attributeDefSaveLite method
            * override this method for handling normal response from attributeDefSaveLite operation
            */
           public void receiveResultattributeDefSaveLite(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.AttributeDefSaveLiteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from attributeDefSaveLite operation
           */
            public void receiveErrorattributeDefSaveLite(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for attributeDefDeleteLite method
            * override this method for handling normal response from attributeDefDeleteLite operation
            */
           public void receiveResultattributeDefDeleteLite(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.AttributeDefDeleteLiteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from attributeDefDeleteLite operation
           */
            public void receiveErrorattributeDefDeleteLite(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for assignPermissionsLite method
            * override this method for handling normal response from assignPermissionsLite operation
            */
           public void receiveResultassignPermissionsLite(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.AssignPermissionsLiteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from assignPermissionsLite operation
           */
            public void receiveErrorassignPermissionsLite(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for findGroupsLite method
            * override this method for handling normal response from findGroupsLite operation
            */
           public void receiveResultfindGroupsLite(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.FindGroupsLiteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from findGroupsLite operation
           */
            public void receiveErrorfindGroupsLite(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for findAttributeDefs method
            * override this method for handling normal response from findAttributeDefs operation
            */
           public void receiveResultfindAttributeDefs(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.FindAttributeDefsResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from findAttributeDefs operation
           */
            public void receiveErrorfindAttributeDefs(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAttributeAssignActionsLite method
            * override this method for handling normal response from getAttributeAssignActionsLite operation
            */
           public void receiveResultgetAttributeAssignActionsLite(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.GetAttributeAssignActionsLiteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAttributeAssignActionsLite operation
           */
            public void receiveErrorgetAttributeAssignActionsLite(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for addMember method
            * override this method for handling normal response from addMember operation
            */
           public void receiveResultaddMember(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.AddMemberResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from addMember operation
           */
            public void receiveErroraddMember(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for assignAttributeDefNameInheritanceLite method
            * override this method for handling normal response from assignAttributeDefNameInheritanceLite operation
            */
           public void receiveResultassignAttributeDefNameInheritanceLite(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.AssignAttributeDefNameInheritanceLiteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from assignAttributeDefNameInheritanceLite operation
           */
            public void receiveErrorassignAttributeDefNameInheritanceLite(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for sendMessage method
            * override this method for handling normal response from sendMessage operation
            */
           public void receiveResultsendMessage(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.SendMessageResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from sendMessage operation
           */
            public void receiveErrorsendMessage(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for assignGrouperPrivilegesLite method
            * override this method for handling normal response from assignGrouperPrivilegesLite operation
            */
           public void receiveResultassignGrouperPrivilegesLite(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.AssignGrouperPrivilegesLiteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from assignGrouperPrivilegesLite operation
           */
            public void receiveErrorassignGrouperPrivilegesLite(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for attributeDefNameSave method
            * override this method for handling normal response from attributeDefNameSave operation
            */
           public void receiveResultattributeDefNameSave(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.AttributeDefNameSaveResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from attributeDefNameSave operation
           */
            public void receiveErrorattributeDefNameSave(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for externalSubjectDelete method
            * override this method for handling normal response from externalSubjectDelete operation
            */
           public void receiveResultexternalSubjectDelete(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.ExternalSubjectDeleteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from externalSubjectDelete operation
           */
            public void receiveErrorexternalSubjectDelete(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for groupSave method
            * override this method for handling normal response from groupSave operation
            */
           public void receiveResultgroupSave(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.GroupSaveResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from groupSave operation
           */
            public void receiveErrorgroupSave(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for assignAttributesLite method
            * override this method for handling normal response from assignAttributesLite operation
            */
           public void receiveResultassignAttributesLite(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.AssignAttributesLiteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from assignAttributesLite operation
           */
            public void receiveErrorassignAttributesLite(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for assignAttributes method
            * override this method for handling normal response from assignAttributes operation
            */
           public void receiveResultassignAttributes(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.AssignAttributesResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from assignAttributes operation
           */
            public void receiveErrorassignAttributes(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getGroupsLite method
            * override this method for handling normal response from getGroupsLite operation
            */
           public void receiveResultgetGroupsLite(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.GetGroupsLiteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getGroupsLite operation
           */
            public void receiveErrorgetGroupsLite(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for findExternalSubjects method
            * override this method for handling normal response from findExternalSubjects operation
            */
           public void receiveResultfindExternalSubjects(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.FindExternalSubjectsResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from findExternalSubjects operation
           */
            public void receiveErrorfindExternalSubjects(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getMemberships method
            * override this method for handling normal response from getMemberships operation
            */
           public void receiveResultgetMemberships(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.GetMembershipsResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getMemberships operation
           */
            public void receiveErrorgetMemberships(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getMembershipsLite method
            * override this method for handling normal response from getMembershipsLite operation
            */
           public void receiveResultgetMembershipsLite(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.GetMembershipsLiteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getMembershipsLite operation
           */
            public void receiveErrorgetMembershipsLite(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getGroups method
            * override this method for handling normal response from getGroups operation
            */
           public void receiveResultgetGroups(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.GetGroupsResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getGroups operation
           */
            public void receiveErrorgetGroups(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for assignAttributesBatch method
            * override this method for handling normal response from assignAttributesBatch operation
            */
           public void receiveResultassignAttributesBatch(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.AssignAttributesBatchResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from assignAttributesBatch operation
           */
            public void receiveErrorassignAttributesBatch(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getGrouperPrivilegesLite method
            * override this method for handling normal response from getGrouperPrivilegesLite operation
            */
           public void receiveResultgetGrouperPrivilegesLite(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.GetGrouperPrivilegesLiteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getGrouperPrivilegesLite operation
           */
            public void receiveErrorgetGrouperPrivilegesLite(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getMembersLite method
            * override this method for handling normal response from getMembersLite operation
            */
           public void receiveResultgetMembersLite(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.GetMembersLiteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getMembersLite operation
           */
            public void receiveErrorgetMembersLite(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getPermissionAssignments method
            * override this method for handling normal response from getPermissionAssignments operation
            */
           public void receiveResultgetPermissionAssignments(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.GetPermissionAssignmentsResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getPermissionAssignments operation
           */
            public void receiveErrorgetPermissionAssignments(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for externalSubjectSave method
            * override this method for handling normal response from externalSubjectSave operation
            */
           public void receiveResultexternalSubjectSave(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.ExternalSubjectSaveResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from externalSubjectSave operation
           */
            public void receiveErrorexternalSubjectSave(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getMembers method
            * override this method for handling normal response from getMembers operation
            */
           public void receiveResultgetMembers(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.GetMembersResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getMembers operation
           */
            public void receiveErrorgetMembers(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for attributeDefNameSaveLite method
            * override this method for handling normal response from attributeDefNameSaveLite operation
            */
           public void receiveResultattributeDefNameSaveLite(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.AttributeDefNameSaveLiteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from attributeDefNameSaveLite operation
           */
            public void receiveErrorattributeDefNameSaveLite(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for assignGrouperPrivileges method
            * override this method for handling normal response from assignGrouperPrivileges operation
            */
           public void receiveResultassignGrouperPrivileges(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.AssignGrouperPrivilegesResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from assignGrouperPrivileges operation
           */
            public void receiveErrorassignGrouperPrivileges(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for groupDelete method
            * override this method for handling normal response from groupDelete operation
            */
           public void receiveResultgroupDelete(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.GroupDeleteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from groupDelete operation
           */
            public void receiveErrorgroupDelete(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for memberChangeSubjectLite method
            * override this method for handling normal response from memberChangeSubjectLite operation
            */
           public void receiveResultmemberChangeSubjectLite(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.MemberChangeSubjectLiteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from memberChangeSubjectLite operation
           */
            public void receiveErrormemberChangeSubjectLite(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for attributeDefDelete method
            * override this method for handling normal response from attributeDefDelete operation
            */
           public void receiveResultattributeDefDelete(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.AttributeDefDeleteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from attributeDefDelete operation
           */
            public void receiveErrorattributeDefDelete(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for deleteMember method
            * override this method for handling normal response from deleteMember operation
            */
           public void receiveResultdeleteMember(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.DeleteMemberResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from deleteMember operation
           */
            public void receiveErrordeleteMember(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for attributeDefNameDeleteLite method
            * override this method for handling normal response from attributeDefNameDeleteLite operation
            */
           public void receiveResultattributeDefNameDeleteLite(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.AttributeDefNameDeleteLiteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from attributeDefNameDeleteLite operation
           */
            public void receiveErrorattributeDefNameDeleteLite(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for stemDeleteLite method
            * override this method for handling normal response from stemDeleteLite operation
            */
           public void receiveResultstemDeleteLite(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.StemDeleteLiteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from stemDeleteLite operation
           */
            public void receiveErrorstemDeleteLite(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getSubjects method
            * override this method for handling normal response from getSubjects operation
            */
           public void receiveResultgetSubjects(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.GetSubjectsResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getSubjects operation
           */
            public void receiveErrorgetSubjects(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getSubjectsLite method
            * override this method for handling normal response from getSubjectsLite operation
            */
           public void receiveResultgetSubjectsLite(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.GetSubjectsLiteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getSubjectsLite operation
           */
            public void receiveErrorgetSubjectsLite(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for deleteMemberLite method
            * override this method for handling normal response from deleteMemberLite operation
            */
           public void receiveResultdeleteMemberLite(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.DeleteMemberLiteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from deleteMemberLite operation
           */
            public void receiveErrordeleteMemberLite(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for assignPermissions method
            * override this method for handling normal response from assignPermissions operation
            */
           public void receiveResultassignPermissions(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.AssignPermissionsResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from assignPermissions operation
           */
            public void receiveErrorassignPermissions(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for groupDeleteLite method
            * override this method for handling normal response from groupDeleteLite operation
            */
           public void receiveResultgroupDeleteLite(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.GroupDeleteLiteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from groupDeleteLite operation
           */
            public void receiveErrorgroupDeleteLite(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAttributeAssignActions method
            * override this method for handling normal response from getAttributeAssignActions operation
            */
           public void receiveResultgetAttributeAssignActions(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.GetAttributeAssignActionsResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAttributeAssignActions operation
           */
            public void receiveErrorgetAttributeAssignActions(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getPermissionAssignmentsLite method
            * override this method for handling normal response from getPermissionAssignmentsLite operation
            */
           public void receiveResultgetPermissionAssignmentsLite(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.GetPermissionAssignmentsLiteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getPermissionAssignmentsLite operation
           */
            public void receiveErrorgetPermissionAssignmentsLite(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for addMemberLite method
            * override this method for handling normal response from addMemberLite operation
            */
           public void receiveResultaddMemberLite(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.AddMemberLiteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from addMemberLite operation
           */
            public void receiveErroraddMemberLite(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for findAttributeDefNames method
            * override this method for handling normal response from findAttributeDefNames operation
            */
           public void receiveResultfindAttributeDefNames(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.FindAttributeDefNamesResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from findAttributeDefNames operation
           */
            public void receiveErrorfindAttributeDefNames(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for attributeDefSave method
            * override this method for handling normal response from attributeDefSave operation
            */
           public void receiveResultattributeDefSave(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.AttributeDefSaveResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from attributeDefSave operation
           */
            public void receiveErrorattributeDefSave(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAttributeAssignments method
            * override this method for handling normal response from getAttributeAssignments operation
            */
           public void receiveResultgetAttributeAssignments(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.GetAttributeAssignmentsResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAttributeAssignments operation
           */
            public void receiveErrorgetAttributeAssignments(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAttributeAssignmentsLite method
            * override this method for handling normal response from getAttributeAssignmentsLite operation
            */
           public void receiveResultgetAttributeAssignmentsLite(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.GetAttributeAssignmentsLiteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAttributeAssignmentsLite operation
           */
            public void receiveErrorgetAttributeAssignmentsLite(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for memberChangeSubject method
            * override this method for handling normal response from memberChangeSubject operation
            */
           public void receiveResultmemberChangeSubject(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.MemberChangeSubjectResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from memberChangeSubject operation
           */
            public void receiveErrormemberChangeSubject(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for receiveMessage method
            * override this method for handling normal response from receiveMessage operation
            */
           public void receiveResultreceiveMessage(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.ReceiveMessageResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from receiveMessage operation
           */
            public void receiveErrorreceiveMessage(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for hasMember method
            * override this method for handling normal response from hasMember operation
            */
           public void receiveResulthasMember(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.HasMemberResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from hasMember operation
           */
            public void receiveErrorhasMember(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for assignAttributeDefNameInheritance method
            * override this method for handling normal response from assignAttributeDefNameInheritance operation
            */
           public void receiveResultassignAttributeDefNameInheritance(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.AssignAttributeDefNameInheritanceResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from assignAttributeDefNameInheritance operation
           */
            public void receiveErrorassignAttributeDefNameInheritance(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for findAttributeDefNamesLite method
            * override this method for handling normal response from findAttributeDefNamesLite operation
            */
           public void receiveResultfindAttributeDefNamesLite(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.FindAttributeDefNamesLiteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from findAttributeDefNamesLite operation
           */
            public void receiveErrorfindAttributeDefNamesLite(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for findStems method
            * override this method for handling normal response from findStems operation
            */
           public void receiveResultfindStems(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.FindStemsResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from findStems operation
           */
            public void receiveErrorfindStems(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for stemSaveLite method
            * override this method for handling normal response from stemSaveLite operation
            */
           public void receiveResultstemSaveLite(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.StemSaveLiteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from stemSaveLite operation
           */
            public void receiveErrorstemSaveLite(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for attributeDefNameDelete method
            * override this method for handling normal response from attributeDefNameDelete operation
            */
           public void receiveResultattributeDefNameDelete(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.AttributeDefNameDeleteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from attributeDefNameDelete operation
           */
            public void receiveErrorattributeDefNameDelete(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for groupSaveLite method
            * override this method for handling normal response from groupSaveLite operation
            */
           public void receiveResultgroupSaveLite(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.GroupSaveLiteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from groupSaveLite operation
           */
            public void receiveErrorgroupSaveLite(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for hasMemberLite method
            * override this method for handling normal response from hasMemberLite operation
            */
           public void receiveResulthasMemberLite(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.HasMemberLiteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from hasMemberLite operation
           */
            public void receiveErrorhasMemberLite(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for stemDelete method
            * override this method for handling normal response from stemDelete operation
            */
           public void receiveResultstemDelete(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.StemDeleteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from stemDelete operation
           */
            public void receiveErrorstemDelete(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for stemSave method
            * override this method for handling normal response from stemSave operation
            */
           public void receiveResultstemSave(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.StemSaveResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from stemSave operation
           */
            public void receiveErrorstemSave(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for findAttributeDefsLite method
            * override this method for handling normal response from findAttributeDefsLite operation
            */
           public void receiveResultfindAttributeDefsLite(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.FindAttributeDefsLiteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from findAttributeDefsLite operation
           */
            public void receiveErrorfindAttributeDefsLite(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for acknowledge method
            * override this method for handling normal response from acknowledge operation
            */
           public void receiveResultacknowledge(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.AcknowledgeResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from acknowledge operation
           */
            public void receiveErroracknowledge(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for findGroups method
            * override this method for handling normal response from findGroups operation
            */
           public void receiveResultfindGroups(
                    edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.FindGroupsResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from findGroups operation
           */
            public void receiveErrorfindGroups(java.lang.Exception e) {
            }
                


    }
    