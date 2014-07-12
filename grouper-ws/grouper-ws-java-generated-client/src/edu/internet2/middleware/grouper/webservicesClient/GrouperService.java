/*******************************************************************************
 * Copyright 2014 Internet2
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/**
 * GrouperService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1  Built on : Aug 31, 2011 (12:22:40 CEST)
 */

    package edu.internet2.middleware.grouper.webservicesClient;

    /*
     *  GrouperService java interface
     */

    public interface GrouperService {
          

        /**
          * Auto generated method signature
          * 
                    * @param getPermissionAssignments0
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetPermissionAssignmentsResponse getPermissionAssignments(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetPermissionAssignments getPermissionAssignments0)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getPermissionAssignments0
            
          */
        public void startgetPermissionAssignments(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetPermissionAssignments getPermissionAssignments0,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param findStems2
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindStemsResponse findStems(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindStems findStems2)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param findStems2
            
          */
        public void startfindStems(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindStems findStems2,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param findStemsLite4
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindStemsLiteResponse findStemsLite(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindStemsLite findStemsLite4)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param findStemsLite4
            
          */
        public void startfindStemsLite(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindStemsLite findStemsLite4,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param addMember6
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AddMemberResponse addMember(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AddMember addMember6)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param addMember6
            
          */
        public void startaddMember(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AddMember addMember6,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getMemberships8
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMembershipsResponse getMemberships(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMemberships getMemberships8)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getMemberships8
            
          */
        public void startgetMemberships(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMemberships getMemberships8,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param stemDelete10
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemDeleteResponse stemDelete(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemDelete stemDelete10)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param stemDelete10
            
          */
        public void startstemDelete(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemDelete stemDelete10,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getPermissionAssignmentsLite12
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetPermissionAssignmentsLiteResponse getPermissionAssignmentsLite(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetPermissionAssignmentsLite getPermissionAssignmentsLite12)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getPermissionAssignmentsLite12
            
          */
        public void startgetPermissionAssignmentsLite(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetPermissionAssignmentsLite getPermissionAssignmentsLite12,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param assignPermissions14
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignPermissionsResponse assignPermissions(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignPermissions assignPermissions14)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param assignPermissions14
            
          */
        public void startassignPermissions(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignPermissions assignPermissions14,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param assignAttributeDefNameInheritanceLite16
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributeDefNameInheritanceLiteResponse assignAttributeDefNameInheritanceLite(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributeDefNameInheritanceLite assignAttributeDefNameInheritanceLite16)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param assignAttributeDefNameInheritanceLite16
            
          */
        public void startassignAttributeDefNameInheritanceLite(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributeDefNameInheritanceLite assignAttributeDefNameInheritanceLite16,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getAttributeAssignmentsLite18
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetAttributeAssignmentsLiteResponse getAttributeAssignmentsLite(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetAttributeAssignmentsLite getAttributeAssignmentsLite18)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAttributeAssignmentsLite18
            
          */
        public void startgetAttributeAssignmentsLite(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetAttributeAssignmentsLite getAttributeAssignmentsLite18,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getMembershipsLite20
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMembershipsLiteResponse getMembershipsLite(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMembershipsLite getMembershipsLite20)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getMembershipsLite20
            
          */
        public void startgetMembershipsLite(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMembershipsLite getMembershipsLite20,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param assignPermissionsLite22
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignPermissionsLiteResponse assignPermissionsLite(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignPermissionsLite assignPermissionsLite22)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param assignPermissionsLite22
            
          */
        public void startassignPermissionsLite(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignPermissionsLite assignPermissionsLite22,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param stemSaveLite24
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemSaveLiteResponse stemSaveLite(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemSaveLite stemSaveLite24)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param stemSaveLite24
            
          */
        public void startstemSaveLite(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemSaveLite stemSaveLite24,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param findGroupsLite26
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindGroupsLiteResponse findGroupsLite(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindGroupsLite findGroupsLite26)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param findGroupsLite26
            
          */
        public void startfindGroupsLite(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindGroupsLite findGroupsLite26,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param assignAttributeDefNameInheritance28
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributeDefNameInheritanceResponse assignAttributeDefNameInheritance(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributeDefNameInheritance assignAttributeDefNameInheritance28)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param assignAttributeDefNameInheritance28
            
          */
        public void startassignAttributeDefNameInheritance(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributeDefNameInheritance assignAttributeDefNameInheritance28,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param addMemberLite30
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AddMemberLiteResponse addMemberLite(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AddMemberLite addMemberLite30)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param addMemberLite30
            
          */
        public void startaddMemberLite(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AddMemberLite addMemberLite30,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param hasMember32
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.HasMemberResponse hasMember(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.HasMember hasMember32)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param hasMember32
            
          */
        public void starthasMember(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.HasMember hasMember32,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getGroups34
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetGroupsResponse getGroups(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetGroups getGroups34)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getGroups34
            
          */
        public void startgetGroups(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetGroups getGroups34,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param deleteMemberLite36
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.DeleteMemberLiteResponse deleteMemberLite(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.DeleteMemberLite deleteMemberLite36)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param deleteMemberLite36
            
          */
        public void startdeleteMemberLite(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.DeleteMemberLite deleteMemberLite36,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param memberChangeSubject38
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.MemberChangeSubjectResponse memberChangeSubject(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.MemberChangeSubject memberChangeSubject38)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param memberChangeSubject38
            
          */
        public void startmemberChangeSubject(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.MemberChangeSubject memberChangeSubject38,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param stemDeleteLite40
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemDeleteLiteResponse stemDeleteLite(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemDeleteLite stemDeleteLite40)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param stemDeleteLite40
            
          */
        public void startstemDeleteLite(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemDeleteLite stemDeleteLite40,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param assignAttributesBatch42
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributesBatchResponse assignAttributesBatch(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributesBatch assignAttributesBatch42)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param assignAttributesBatch42
            
          */
        public void startassignAttributesBatch(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributesBatch assignAttributesBatch42,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param attributeDefNameDelete44
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameDeleteResponse attributeDefNameDelete(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameDelete attributeDefNameDelete44)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param attributeDefNameDelete44
            
          */
        public void startattributeDefNameDelete(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameDelete attributeDefNameDelete44,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param assignAttributes46
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributesResponse assignAttributes(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributes assignAttributes46)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param assignAttributes46
            
          */
        public void startassignAttributes(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributes assignAttributes46,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param groupDeleteLite48
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupDeleteLiteResponse groupDeleteLite(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupDeleteLite groupDeleteLite48)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param groupDeleteLite48
            
          */
        public void startgroupDeleteLite(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupDeleteLite groupDeleteLite48,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getSubjects50
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetSubjectsResponse getSubjects(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetSubjects getSubjects50)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getSubjects50
            
          */
        public void startgetSubjects(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetSubjects getSubjects50,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param assignGrouperPrivilegesLite52
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignGrouperPrivilegesLiteResponse assignGrouperPrivilegesLite(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignGrouperPrivilegesLite assignGrouperPrivilegesLite52)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param assignGrouperPrivilegesLite52
            
          */
        public void startassignGrouperPrivilegesLite(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignGrouperPrivilegesLite assignGrouperPrivilegesLite52,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param hasMemberLite54
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.HasMemberLiteResponse hasMemberLite(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.HasMemberLite hasMemberLite54)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param hasMemberLite54
            
          */
        public void starthasMemberLite(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.HasMemberLite hasMemberLite54,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param stemSave56
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemSaveResponse stemSave(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemSave stemSave56)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param stemSave56
            
          */
        public void startstemSave(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemSave stemSave56,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param attributeDefNameSave58
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameSaveResponse attributeDefNameSave(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameSave attributeDefNameSave58)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param attributeDefNameSave58
            
          */
        public void startattributeDefNameSave(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameSave attributeDefNameSave58,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param attributeDefNameSaveLite60
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameSaveLiteResponse attributeDefNameSaveLite(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameSaveLite attributeDefNameSaveLite60)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param attributeDefNameSaveLite60
            
          */
        public void startattributeDefNameSaveLite(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameSaveLite attributeDefNameSaveLite60,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getGroupsLite62
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetGroupsLiteResponse getGroupsLite(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetGroupsLite getGroupsLite62)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getGroupsLite62
            
          */
        public void startgetGroupsLite(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetGroupsLite getGroupsLite62,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param attributeDefNameDeleteLite64
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameDeleteLiteResponse attributeDefNameDeleteLite(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameDeleteLite attributeDefNameDeleteLite64)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param attributeDefNameDeleteLite64
            
          */
        public void startattributeDefNameDeleteLite(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameDeleteLite attributeDefNameDeleteLite64,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param findAttributeDefNamesLite66
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindAttributeDefNamesLiteResponse findAttributeDefNamesLite(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindAttributeDefNamesLite findAttributeDefNamesLite66)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param findAttributeDefNamesLite66
            
          */
        public void startfindAttributeDefNamesLite(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindAttributeDefNamesLite findAttributeDefNamesLite66,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param findGroups68
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindGroupsResponse findGroups(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindGroups findGroups68)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param findGroups68
            
          */
        public void startfindGroups(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindGroups findGroups68,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getSubjectsLite70
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetSubjectsLiteResponse getSubjectsLite(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetSubjectsLite getSubjectsLite70)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getSubjectsLite70
            
          */
        public void startgetSubjectsLite(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetSubjectsLite getSubjectsLite70,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param groupDelete72
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupDeleteResponse groupDelete(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupDelete groupDelete72)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param groupDelete72
            
          */
        public void startgroupDelete(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupDelete groupDelete72,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param assignAttributesLite74
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributesLiteResponse assignAttributesLite(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributesLite assignAttributesLite74)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param assignAttributesLite74
            
          */
        public void startassignAttributesLite(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributesLite assignAttributesLite74,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param assignGrouperPrivileges76
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignGrouperPrivilegesResponse assignGrouperPrivileges(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignGrouperPrivileges assignGrouperPrivileges76)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param assignGrouperPrivileges76
            
          */
        public void startassignGrouperPrivileges(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignGrouperPrivileges assignGrouperPrivileges76,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param findAttributeDefNames78
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindAttributeDefNamesResponse findAttributeDefNames(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindAttributeDefNames findAttributeDefNames78)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param findAttributeDefNames78
            
          */
        public void startfindAttributeDefNames(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindAttributeDefNames findAttributeDefNames78,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param groupSave80
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupSaveResponse groupSave(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupSave groupSave80)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param groupSave80
            
          */
        public void startgroupSave(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupSave groupSave80,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getGrouperPrivilegesLite82
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetGrouperPrivilegesLiteResponse getGrouperPrivilegesLite(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetGrouperPrivilegesLite getGrouperPrivilegesLite82)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getGrouperPrivilegesLite82
            
          */
        public void startgetGrouperPrivilegesLite(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetGrouperPrivilegesLite getGrouperPrivilegesLite82,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param deleteMember84
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.DeleteMemberResponse deleteMember(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.DeleteMember deleteMember84)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param deleteMember84
            
          */
        public void startdeleteMember(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.DeleteMember deleteMember84,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getAttributeAssignments86
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetAttributeAssignmentsResponse getAttributeAssignments(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetAttributeAssignments getAttributeAssignments86)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAttributeAssignments86
            
          */
        public void startgetAttributeAssignments(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetAttributeAssignments getAttributeAssignments86,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param groupSaveLite88
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupSaveLiteResponse groupSaveLite(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupSaveLite groupSaveLite88)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param groupSaveLite88
            
          */
        public void startgroupSaveLite(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupSaveLite groupSaveLite88,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param memberChangeSubjectLite90
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.MemberChangeSubjectLiteResponse memberChangeSubjectLite(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.MemberChangeSubjectLite memberChangeSubjectLite90)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param memberChangeSubjectLite90
            
          */
        public void startmemberChangeSubjectLite(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.MemberChangeSubjectLite memberChangeSubjectLite90,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getMembersLite92
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMembersLiteResponse getMembersLite(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMembersLite getMembersLite92)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getMembersLite92
            
          */
        public void startgetMembersLite(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMembersLite getMembersLite92,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getMembers94
                
         */

         
                     public edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMembersResponse getMembers(

                        edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMembers getMembers94)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getMembers94
            
          */
        public void startgetMembers(

            edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMembers getMembers94,

            final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        
       //
       }
    
