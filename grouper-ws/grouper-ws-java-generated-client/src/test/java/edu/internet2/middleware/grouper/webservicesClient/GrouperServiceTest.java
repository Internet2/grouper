

/**
 * GrouperServiceTest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1  Built on : Aug 31, 2011 (12:22:40 CEST)
 */
    package edu.internet2.middleware.grouper.webservicesClient;

    /*
     *  GrouperServiceTest Junit test case
    */

    public class GrouperServiceTest extends junit.framework.TestCase{

     
        /**
         * Auto generated test method
         */
        public  void testgetGroupsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetGroupsLite getGroupsLite390=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetGroupsLite.class);
                    // TODO : Fill in the getGroupsLite390 here
                
                        assertNotNull(stub.getGroupsLite(
                        getGroupsLite390));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetGroupsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetGroupsLite getGroupsLite390=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetGroupsLite.class);
                    // TODO : Fill in the getGroupsLite390 here
                

                stub.startgetGroupsLite(
                         getGroupsLite390,
                    new tempCallbackN65548()
                );
              


        }

        private class tempCallbackN65548  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65548(){ super(null);}

            public void receiveResultgetGroupsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetGroupsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetGroupsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgroupSaveLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GroupSaveLite groupSaveLite392=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GroupSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GroupSaveLite.class);
                    // TODO : Fill in the groupSaveLite392 here
                
                        assertNotNull(stub.groupSaveLite(
                        groupSaveLite392));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupSaveLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GroupSaveLite groupSaveLite392=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GroupSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GroupSaveLite.class);
                    // TODO : Fill in the groupSaveLite392 here
                

                stub.startgroupSaveLite(
                         groupSaveLite392,
                    new tempCallbackN65589()
                );
              


        }

        private class tempCallbackN65589  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65589(){ super(null);}

            public void receiveResultgroupSaveLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GroupSaveLiteResponse result
                            ) {
                
            }

            public void receiveErrorgroupSaveLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testreceiveMessage() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.ReceiveMessage receiveMessage394=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.ReceiveMessage)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.ReceiveMessage.class);
                    // TODO : Fill in the receiveMessage394 here
                
                        assertNotNull(stub.receiveMessage(
                        receiveMessage394));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartreceiveMessage() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.ReceiveMessage receiveMessage394=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.ReceiveMessage)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.ReceiveMessage.class);
                    // TODO : Fill in the receiveMessage394 here
                

                stub.startreceiveMessage(
                         receiveMessage394,
                    new tempCallbackN65630()
                );
              


        }

        private class tempCallbackN65630  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65630(){ super(null);}

            public void receiveResultreceiveMessage(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.ReceiveMessageResponse result
                            ) {
                
            }

            public void receiveErrorreceiveMessage(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindGroupsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindGroupsLite findGroupsLite396=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindGroupsLite.class);
                    // TODO : Fill in the findGroupsLite396 here
                
                        assertNotNull(stub.findGroupsLite(
                        findGroupsLite396));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindGroupsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindGroupsLite findGroupsLite396=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindGroupsLite.class);
                    // TODO : Fill in the findGroupsLite396 here
                

                stub.startfindGroupsLite(
                         findGroupsLite396,
                    new tempCallbackN65671()
                );
              


        }

        private class tempCallbackN65671  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65671(){ super(null);}

            public void receiveResultfindGroupsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindGroupsLiteResponse result
                            ) {
                
            }

            public void receiveErrorfindGroupsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindAttributeDefs() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindAttributeDefs findAttributeDefs398=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindAttributeDefs)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindAttributeDefs.class);
                    // TODO : Fill in the findAttributeDefs398 here
                
                        assertNotNull(stub.findAttributeDefs(
                        findAttributeDefs398));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindAttributeDefs() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindAttributeDefs findAttributeDefs398=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindAttributeDefs)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindAttributeDefs.class);
                    // TODO : Fill in the findAttributeDefs398 here
                

                stub.startfindAttributeDefs(
                         findAttributeDefs398,
                    new tempCallbackN65712()
                );
              


        }

        private class tempCallbackN65712  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65712(){ super(null);}

            public void receiveResultfindAttributeDefs(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindAttributeDefsResponse result
                            ) {
                
            }

            public void receiveErrorfindAttributeDefs(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testattributeDefNameSaveLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefNameSaveLite attributeDefNameSaveLite400=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefNameSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefNameSaveLite.class);
                    // TODO : Fill in the attributeDefNameSaveLite400 here
                
                        assertNotNull(stub.attributeDefNameSaveLite(
                        attributeDefNameSaveLite400));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartattributeDefNameSaveLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefNameSaveLite attributeDefNameSaveLite400=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefNameSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefNameSaveLite.class);
                    // TODO : Fill in the attributeDefNameSaveLite400 here
                

                stub.startattributeDefNameSaveLite(
                         attributeDefNameSaveLite400,
                    new tempCallbackN65753()
                );
              


        }

        private class tempCallbackN65753  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65753(){ super(null);}

            public void receiveResultattributeDefNameSaveLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefNameSaveLiteResponse result
                            ) {
                
            }

            public void receiveErrorattributeDefNameSaveLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignAttributeDefActions() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributeDefActions assignAttributeDefActions402=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributeDefActions)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributeDefActions.class);
                    // TODO : Fill in the assignAttributeDefActions402 here
                
                        assertNotNull(stub.assignAttributeDefActions(
                        assignAttributeDefActions402));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignAttributeDefActions() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributeDefActions assignAttributeDefActions402=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributeDefActions)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributeDefActions.class);
                    // TODO : Fill in the assignAttributeDefActions402 here
                

                stub.startassignAttributeDefActions(
                         assignAttributeDefActions402,
                    new tempCallbackN65794()
                );
              


        }

        private class tempCallbackN65794  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65794(){ super(null);}

            public void receiveResultassignAttributeDefActions(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributeDefActionsResponse result
                            ) {
                
            }

            public void receiveErrorassignAttributeDefActions(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMembershipsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetMembershipsLite getMembershipsLite404=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetMembershipsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetMembershipsLite.class);
                    // TODO : Fill in the getMembershipsLite404 here
                
                        assertNotNull(stub.getMembershipsLite(
                        getMembershipsLite404));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMembershipsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetMembershipsLite getMembershipsLite404=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetMembershipsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetMembershipsLite.class);
                    // TODO : Fill in the getMembershipsLite404 here
                

                stub.startgetMembershipsLite(
                         getMembershipsLite404,
                    new tempCallbackN65835()
                );
              


        }

        private class tempCallbackN65835  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65835(){ super(null);}

            public void receiveResultgetMembershipsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetMembershipsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetMembershipsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetPermissionAssignments() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetPermissionAssignments getPermissionAssignments406=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetPermissionAssignments)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetPermissionAssignments.class);
                    // TODO : Fill in the getPermissionAssignments406 here
                
                        assertNotNull(stub.getPermissionAssignments(
                        getPermissionAssignments406));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetPermissionAssignments() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetPermissionAssignments getPermissionAssignments406=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetPermissionAssignments)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetPermissionAssignments.class);
                    // TODO : Fill in the getPermissionAssignments406 here
                

                stub.startgetPermissionAssignments(
                         getPermissionAssignments406,
                    new tempCallbackN65876()
                );
              


        }

        private class tempCallbackN65876  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65876(){ super(null);}

            public void receiveResultgetPermissionAssignments(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetPermissionAssignmentsResponse result
                            ) {
                
            }

            public void receiveErrorgetPermissionAssignments(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testexternalSubjectDelete() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.ExternalSubjectDelete externalSubjectDelete408=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.ExternalSubjectDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.ExternalSubjectDelete.class);
                    // TODO : Fill in the externalSubjectDelete408 here
                
                        assertNotNull(stub.externalSubjectDelete(
                        externalSubjectDelete408));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartexternalSubjectDelete() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.ExternalSubjectDelete externalSubjectDelete408=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.ExternalSubjectDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.ExternalSubjectDelete.class);
                    // TODO : Fill in the externalSubjectDelete408 here
                

                stub.startexternalSubjectDelete(
                         externalSubjectDelete408,
                    new tempCallbackN65917()
                );
              


        }

        private class tempCallbackN65917  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65917(){ super(null);}

            public void receiveResultexternalSubjectDelete(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.ExternalSubjectDeleteResponse result
                            ) {
                
            }

            public void receiveErrorexternalSubjectDelete(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMembers() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetMembers getMembers410=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetMembers)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetMembers.class);
                    // TODO : Fill in the getMembers410 here
                
                        assertNotNull(stub.getMembers(
                        getMembers410));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMembers() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetMembers getMembers410=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetMembers)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetMembers.class);
                    // TODO : Fill in the getMembers410 here
                

                stub.startgetMembers(
                         getMembers410,
                    new tempCallbackN65958()
                );
              


        }

        private class tempCallbackN65958  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65958(){ super(null);}

            public void receiveResultgetMembers(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetMembersResponse result
                            ) {
                
            }

            public void receiveErrorgetMembers(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testmemberChangeSubjectLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.MemberChangeSubjectLite memberChangeSubjectLite412=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.MemberChangeSubjectLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.MemberChangeSubjectLite.class);
                    // TODO : Fill in the memberChangeSubjectLite412 here
                
                        assertNotNull(stub.memberChangeSubjectLite(
                        memberChangeSubjectLite412));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartmemberChangeSubjectLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.MemberChangeSubjectLite memberChangeSubjectLite412=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.MemberChangeSubjectLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.MemberChangeSubjectLite.class);
                    // TODO : Fill in the memberChangeSubjectLite412 here
                

                stub.startmemberChangeSubjectLite(
                         memberChangeSubjectLite412,
                    new tempCallbackN65999()
                );
              


        }

        private class tempCallbackN65999  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65999(){ super(null);}

            public void receiveResultmemberChangeSubjectLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.MemberChangeSubjectLiteResponse result
                            ) {
                
            }

            public void receiveErrormemberChangeSubjectLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testdeleteMember() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.DeleteMember deleteMember414=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.DeleteMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.DeleteMember.class);
                    // TODO : Fill in the deleteMember414 here
                
                        assertNotNull(stub.deleteMember(
                        deleteMember414));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartdeleteMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.DeleteMember deleteMember414=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.DeleteMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.DeleteMember.class);
                    // TODO : Fill in the deleteMember414 here
                

                stub.startdeleteMember(
                         deleteMember414,
                    new tempCallbackN66040()
                );
              


        }

        private class tempCallbackN66040  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66040(){ super(null);}

            public void receiveResultdeleteMember(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.DeleteMemberResponse result
                            ) {
                
            }

            public void receiveErrordeleteMember(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgroupSave() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GroupSave groupSave416=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GroupSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GroupSave.class);
                    // TODO : Fill in the groupSave416 here
                
                        assertNotNull(stub.groupSave(
                        groupSave416));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupSave() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GroupSave groupSave416=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GroupSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GroupSave.class);
                    // TODO : Fill in the groupSave416 here
                

                stub.startgroupSave(
                         groupSave416,
                    new tempCallbackN66081()
                );
              


        }

        private class tempCallbackN66081  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66081(){ super(null);}

            public void receiveResultgroupSave(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GroupSaveResponse result
                            ) {
                
            }

            public void receiveErrorgroupSave(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testhasMemberLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.HasMemberLite hasMemberLite418=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.HasMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.HasMemberLite.class);
                    // TODO : Fill in the hasMemberLite418 here
                
                        assertNotNull(stub.hasMemberLite(
                        hasMemberLite418));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStarthasMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.HasMemberLite hasMemberLite418=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.HasMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.HasMemberLite.class);
                    // TODO : Fill in the hasMemberLite418 here
                

                stub.starthasMemberLite(
                         hasMemberLite418,
                    new tempCallbackN66122()
                );
              


        }

        private class tempCallbackN66122  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66122(){ super(null);}

            public void receiveResulthasMemberLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.HasMemberLiteResponse result
                            ) {
                
            }

            public void receiveErrorhasMemberLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignGrouperPrivileges() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignGrouperPrivileges assignGrouperPrivileges420=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignGrouperPrivileges)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignGrouperPrivileges.class);
                    // TODO : Fill in the assignGrouperPrivileges420 here
                
                        assertNotNull(stub.assignGrouperPrivileges(
                        assignGrouperPrivileges420));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignGrouperPrivileges() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignGrouperPrivileges assignGrouperPrivileges420=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignGrouperPrivileges)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignGrouperPrivileges.class);
                    // TODO : Fill in the assignGrouperPrivileges420 here
                

                stub.startassignGrouperPrivileges(
                         assignGrouperPrivileges420,
                    new tempCallbackN66163()
                );
              


        }

        private class tempCallbackN66163  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66163(){ super(null);}

            public void receiveResultassignGrouperPrivileges(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignGrouperPrivilegesResponse result
                            ) {
                
            }

            public void receiveErrorassignGrouperPrivileges(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetAuditEntriesLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAuditEntriesLite getAuditEntriesLite422=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAuditEntriesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAuditEntriesLite.class);
                    // TODO : Fill in the getAuditEntriesLite422 here
                
                        assertNotNull(stub.getAuditEntriesLite(
                        getAuditEntriesLite422));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetAuditEntriesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAuditEntriesLite getAuditEntriesLite422=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAuditEntriesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAuditEntriesLite.class);
                    // TODO : Fill in the getAuditEntriesLite422 here
                

                stub.startgetAuditEntriesLite(
                         getAuditEntriesLite422,
                    new tempCallbackN66204()
                );
              


        }

        private class tempCallbackN66204  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66204(){ super(null);}

            public void receiveResultgetAuditEntriesLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAuditEntriesLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetAuditEntriesLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testattributeDefSaveLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefSaveLite attributeDefSaveLite424=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefSaveLite.class);
                    // TODO : Fill in the attributeDefSaveLite424 here
                
                        assertNotNull(stub.attributeDefSaveLite(
                        attributeDefSaveLite424));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartattributeDefSaveLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefSaveLite attributeDefSaveLite424=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefSaveLite.class);
                    // TODO : Fill in the attributeDefSaveLite424 here
                

                stub.startattributeDefSaveLite(
                         attributeDefSaveLite424,
                    new tempCallbackN66245()
                );
              


        }

        private class tempCallbackN66245  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66245(){ super(null);}

            public void receiveResultattributeDefSaveLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefSaveLiteResponse result
                            ) {
                
            }

            public void receiveErrorattributeDefSaveLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testattributeDefDeleteLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefDeleteLite attributeDefDeleteLite426=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefDeleteLite.class);
                    // TODO : Fill in the attributeDefDeleteLite426 here
                
                        assertNotNull(stub.attributeDefDeleteLite(
                        attributeDefDeleteLite426));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartattributeDefDeleteLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefDeleteLite attributeDefDeleteLite426=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefDeleteLite.class);
                    // TODO : Fill in the attributeDefDeleteLite426 here
                

                stub.startattributeDefDeleteLite(
                         attributeDefDeleteLite426,
                    new tempCallbackN66286()
                );
              


        }

        private class tempCallbackN66286  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66286(){ super(null);}

            public void receiveResultattributeDefDeleteLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefDeleteLiteResponse result
                            ) {
                
            }

            public void receiveErrorattributeDefDeleteLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetSubjectsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetSubjectsLite getSubjectsLite428=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetSubjectsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetSubjectsLite.class);
                    // TODO : Fill in the getSubjectsLite428 here
                
                        assertNotNull(stub.getSubjectsLite(
                        getSubjectsLite428));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetSubjectsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetSubjectsLite getSubjectsLite428=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetSubjectsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetSubjectsLite.class);
                    // TODO : Fill in the getSubjectsLite428 here
                

                stub.startgetSubjectsLite(
                         getSubjectsLite428,
                    new tempCallbackN66327()
                );
              


        }

        private class tempCallbackN66327  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66327(){ super(null);}

            public void receiveResultgetSubjectsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetSubjectsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetSubjectsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testdeleteMemberLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.DeleteMemberLite deleteMemberLite430=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.DeleteMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.DeleteMemberLite.class);
                    // TODO : Fill in the deleteMemberLite430 here
                
                        assertNotNull(stub.deleteMemberLite(
                        deleteMemberLite430));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartdeleteMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.DeleteMemberLite deleteMemberLite430=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.DeleteMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.DeleteMemberLite.class);
                    // TODO : Fill in the deleteMemberLite430 here
                

                stub.startdeleteMemberLite(
                         deleteMemberLite430,
                    new tempCallbackN66368()
                );
              


        }

        private class tempCallbackN66368  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66368(){ super(null);}

            public void receiveResultdeleteMemberLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.DeleteMemberLiteResponse result
                            ) {
                
            }

            public void receiveErrordeleteMemberLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemSave() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.StemSave stemSave432=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.StemSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.StemSave.class);
                    // TODO : Fill in the stemSave432 here
                
                        assertNotNull(stub.stemSave(
                        stemSave432));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemSave() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.StemSave stemSave432=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.StemSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.StemSave.class);
                    // TODO : Fill in the stemSave432 here
                

                stub.startstemSave(
                         stemSave432,
                    new tempCallbackN66409()
                );
              


        }

        private class tempCallbackN66409  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66409(){ super(null);}

            public void receiveResultstemSave(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.StemSaveResponse result
                            ) {
                
            }

            public void receiveErrorstemSave(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetGroups() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetGroups getGroups434=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetGroups.class);
                    // TODO : Fill in the getGroups434 here
                
                        assertNotNull(stub.getGroups(
                        getGroups434));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetGroups() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetGroups getGroups434=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetGroups.class);
                    // TODO : Fill in the getGroups434 here
                

                stub.startgetGroups(
                         getGroups434,
                    new tempCallbackN66450()
                );
              


        }

        private class tempCallbackN66450  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66450(){ super(null);}

            public void receiveResultgetGroups(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetGroupsResponse result
                            ) {
                
            }

            public void receiveErrorgetGroups(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetGrouperPrivilegesLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetGrouperPrivilegesLite getGrouperPrivilegesLite436=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetGrouperPrivilegesLite.class);
                    // TODO : Fill in the getGrouperPrivilegesLite436 here
                
                        assertNotNull(stub.getGrouperPrivilegesLite(
                        getGrouperPrivilegesLite436));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetGrouperPrivilegesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetGrouperPrivilegesLite getGrouperPrivilegesLite436=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetGrouperPrivilegesLite.class);
                    // TODO : Fill in the getGrouperPrivilegesLite436 here
                

                stub.startgetGrouperPrivilegesLite(
                         getGrouperPrivilegesLite436,
                    new tempCallbackN66491()
                );
              


        }

        private class tempCallbackN66491  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66491(){ super(null);}

            public void receiveResultgetGrouperPrivilegesLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetGrouperPrivilegesLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetGrouperPrivilegesLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignAttributesLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributesLite assignAttributesLite438=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributesLite.class);
                    // TODO : Fill in the assignAttributesLite438 here
                
                        assertNotNull(stub.assignAttributesLite(
                        assignAttributesLite438));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignAttributesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributesLite assignAttributesLite438=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributesLite.class);
                    // TODO : Fill in the assignAttributesLite438 here
                

                stub.startassignAttributesLite(
                         assignAttributesLite438,
                    new tempCallbackN66532()
                );
              


        }

        private class tempCallbackN66532  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66532(){ super(null);}

            public void receiveResultassignAttributesLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributesLiteResponse result
                            ) {
                
            }

            public void receiveErrorassignAttributesLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testmemberChangeSubject() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.MemberChangeSubject memberChangeSubject440=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.MemberChangeSubject)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.MemberChangeSubject.class);
                    // TODO : Fill in the memberChangeSubject440 here
                
                        assertNotNull(stub.memberChangeSubject(
                        memberChangeSubject440));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartmemberChangeSubject() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.MemberChangeSubject memberChangeSubject440=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.MemberChangeSubject)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.MemberChangeSubject.class);
                    // TODO : Fill in the memberChangeSubject440 here
                

                stub.startmemberChangeSubject(
                         memberChangeSubject440,
                    new tempCallbackN66573()
                );
              


        }

        private class tempCallbackN66573  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66573(){ super(null);}

            public void receiveResultmemberChangeSubject(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.MemberChangeSubjectResponse result
                            ) {
                
            }

            public void receiveErrormemberChangeSubject(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetAttributeAssignments() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAttributeAssignments getAttributeAssignments442=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAttributeAssignments)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAttributeAssignments.class);
                    // TODO : Fill in the getAttributeAssignments442 here
                
                        assertNotNull(stub.getAttributeAssignments(
                        getAttributeAssignments442));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetAttributeAssignments() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAttributeAssignments getAttributeAssignments442=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAttributeAssignments)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAttributeAssignments.class);
                    // TODO : Fill in the getAttributeAssignments442 here
                

                stub.startgetAttributeAssignments(
                         getAttributeAssignments442,
                    new tempCallbackN66614()
                );
              


        }

        private class tempCallbackN66614  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66614(){ super(null);}

            public void receiveResultgetAttributeAssignments(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAttributeAssignmentsResponse result
                            ) {
                
            }

            public void receiveErrorgetAttributeAssignments(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testhasMember() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.HasMember hasMember444=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.HasMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.HasMember.class);
                    // TODO : Fill in the hasMember444 here
                
                        assertNotNull(stub.hasMember(
                        hasMember444));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStarthasMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.HasMember hasMember444=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.HasMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.HasMember.class);
                    // TODO : Fill in the hasMember444 here
                

                stub.starthasMember(
                         hasMember444,
                    new tempCallbackN66655()
                );
              


        }

        private class tempCallbackN66655  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66655(){ super(null);}

            public void receiveResulthasMember(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.HasMemberResponse result
                            ) {
                
            }

            public void receiveErrorhasMember(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindAttributeDefNamesLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindAttributeDefNamesLite findAttributeDefNamesLite446=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindAttributeDefNamesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindAttributeDefNamesLite.class);
                    // TODO : Fill in the findAttributeDefNamesLite446 here
                
                        assertNotNull(stub.findAttributeDefNamesLite(
                        findAttributeDefNamesLite446));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindAttributeDefNamesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindAttributeDefNamesLite findAttributeDefNamesLite446=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindAttributeDefNamesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindAttributeDefNamesLite.class);
                    // TODO : Fill in the findAttributeDefNamesLite446 here
                

                stub.startfindAttributeDefNamesLite(
                         findAttributeDefNamesLite446,
                    new tempCallbackN66696()
                );
              


        }

        private class tempCallbackN66696  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66696(){ super(null);}

            public void receiveResultfindAttributeDefNamesLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindAttributeDefNamesLiteResponse result
                            ) {
                
            }

            public void receiveErrorfindAttributeDefNamesLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testattributeDefNameSave() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefNameSave attributeDefNameSave448=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefNameSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefNameSave.class);
                    // TODO : Fill in the attributeDefNameSave448 here
                
                        assertNotNull(stub.attributeDefNameSave(
                        attributeDefNameSave448));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartattributeDefNameSave() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefNameSave attributeDefNameSave448=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefNameSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefNameSave.class);
                    // TODO : Fill in the attributeDefNameSave448 here
                

                stub.startattributeDefNameSave(
                         attributeDefNameSave448,
                    new tempCallbackN66737()
                );
              


        }

        private class tempCallbackN66737  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66737(){ super(null);}

            public void receiveResultattributeDefNameSave(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefNameSaveResponse result
                            ) {
                
            }

            public void receiveErrorattributeDefNameSave(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testaddMember() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AddMember addMember450=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AddMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AddMember.class);
                    // TODO : Fill in the addMember450 here
                
                        assertNotNull(stub.addMember(
                        addMember450));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartaddMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AddMember addMember450=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AddMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AddMember.class);
                    // TODO : Fill in the addMember450 here
                

                stub.startaddMember(
                         addMember450,
                    new tempCallbackN66778()
                );
              


        }

        private class tempCallbackN66778  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66778(){ super(null);}

            public void receiveResultaddMember(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AddMemberResponse result
                            ) {
                
            }

            public void receiveErroraddMember(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindAttributeDefNames() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindAttributeDefNames findAttributeDefNames452=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindAttributeDefNames)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindAttributeDefNames.class);
                    // TODO : Fill in the findAttributeDefNames452 here
                
                        assertNotNull(stub.findAttributeDefNames(
                        findAttributeDefNames452));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindAttributeDefNames() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindAttributeDefNames findAttributeDefNames452=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindAttributeDefNames)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindAttributeDefNames.class);
                    // TODO : Fill in the findAttributeDefNames452 here
                

                stub.startfindAttributeDefNames(
                         findAttributeDefNames452,
                    new tempCallbackN66819()
                );
              


        }

        private class tempCallbackN66819  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66819(){ super(null);}

            public void receiveResultfindAttributeDefNames(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindAttributeDefNamesResponse result
                            ) {
                
            }

            public void receiveErrorfindAttributeDefNames(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignPermissions() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignPermissions assignPermissions454=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignPermissions)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignPermissions.class);
                    // TODO : Fill in the assignPermissions454 here
                
                        assertNotNull(stub.assignPermissions(
                        assignPermissions454));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignPermissions() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignPermissions assignPermissions454=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignPermissions)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignPermissions.class);
                    // TODO : Fill in the assignPermissions454 here
                

                stub.startassignPermissions(
                         assignPermissions454,
                    new tempCallbackN66860()
                );
              


        }

        private class tempCallbackN66860  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66860(){ super(null);}

            public void receiveResultassignPermissions(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignPermissionsResponse result
                            ) {
                
            }

            public void receiveErrorassignPermissions(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignGrouperPrivilegesLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignGrouperPrivilegesLite assignGrouperPrivilegesLite456=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignGrouperPrivilegesLite.class);
                    // TODO : Fill in the assignGrouperPrivilegesLite456 here
                
                        assertNotNull(stub.assignGrouperPrivilegesLite(
                        assignGrouperPrivilegesLite456));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignGrouperPrivilegesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignGrouperPrivilegesLite assignGrouperPrivilegesLite456=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignGrouperPrivilegesLite.class);
                    // TODO : Fill in the assignGrouperPrivilegesLite456 here
                

                stub.startassignGrouperPrivilegesLite(
                         assignGrouperPrivilegesLite456,
                    new tempCallbackN66901()
                );
              


        }

        private class tempCallbackN66901  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66901(){ super(null);}

            public void receiveResultassignGrouperPrivilegesLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignGrouperPrivilegesLiteResponse result
                            ) {
                
            }

            public void receiveErrorassignGrouperPrivilegesLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignPermissionsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignPermissionsLite assignPermissionsLite458=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignPermissionsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignPermissionsLite.class);
                    // TODO : Fill in the assignPermissionsLite458 here
                
                        assertNotNull(stub.assignPermissionsLite(
                        assignPermissionsLite458));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignPermissionsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignPermissionsLite assignPermissionsLite458=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignPermissionsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignPermissionsLite.class);
                    // TODO : Fill in the assignPermissionsLite458 here
                

                stub.startassignPermissionsLite(
                         assignPermissionsLite458,
                    new tempCallbackN66942()
                );
              


        }

        private class tempCallbackN66942  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66942(){ super(null);}

            public void receiveResultassignPermissionsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignPermissionsLiteResponse result
                            ) {
                
            }

            public void receiveErrorassignPermissionsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignAttributesBatch() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributesBatch assignAttributesBatch460=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributesBatch)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributesBatch.class);
                    // TODO : Fill in the assignAttributesBatch460 here
                
                        assertNotNull(stub.assignAttributesBatch(
                        assignAttributesBatch460));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignAttributesBatch() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributesBatch assignAttributesBatch460=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributesBatch)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributesBatch.class);
                    // TODO : Fill in the assignAttributesBatch460 here
                

                stub.startassignAttributesBatch(
                         assignAttributesBatch460,
                    new tempCallbackN66983()
                );
              


        }

        private class tempCallbackN66983  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66983(){ super(null);}

            public void receiveResultassignAttributesBatch(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributesBatchResponse result
                            ) {
                
            }

            public void receiveErrorassignAttributesBatch(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMembersLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetMembersLite getMembersLite462=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetMembersLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetMembersLite.class);
                    // TODO : Fill in the getMembersLite462 here
                
                        assertNotNull(stub.getMembersLite(
                        getMembersLite462));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMembersLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetMembersLite getMembersLite462=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetMembersLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetMembersLite.class);
                    // TODO : Fill in the getMembersLite462 here
                

                stub.startgetMembersLite(
                         getMembersLite462,
                    new tempCallbackN67024()
                );
              


        }

        private class tempCallbackN67024  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67024(){ super(null);}

            public void receiveResultgetMembersLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetMembersLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetMembersLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemSaveLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.StemSaveLite stemSaveLite464=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.StemSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.StemSaveLite.class);
                    // TODO : Fill in the stemSaveLite464 here
                
                        assertNotNull(stub.stemSaveLite(
                        stemSaveLite464));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemSaveLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.StemSaveLite stemSaveLite464=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.StemSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.StemSaveLite.class);
                    // TODO : Fill in the stemSaveLite464 here
                

                stub.startstemSaveLite(
                         stemSaveLite464,
                    new tempCallbackN67065()
                );
              


        }

        private class tempCallbackN67065  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67065(){ super(null);}

            public void receiveResultstemSaveLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.StemSaveLiteResponse result
                            ) {
                
            }

            public void receiveErrorstemSaveLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testsendMessage() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.SendMessage sendMessage466=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.SendMessage)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.SendMessage.class);
                    // TODO : Fill in the sendMessage466 here
                
                        assertNotNull(stub.sendMessage(
                        sendMessage466));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartsendMessage() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.SendMessage sendMessage466=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.SendMessage)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.SendMessage.class);
                    // TODO : Fill in the sendMessage466 here
                

                stub.startsendMessage(
                         sendMessage466,
                    new tempCallbackN67106()
                );
              


        }

        private class tempCallbackN67106  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67106(){ super(null);}

            public void receiveResultsendMessage(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.SendMessageResponse result
                            ) {
                
            }

            public void receiveErrorsendMessage(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindStems() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindStems findStems468=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindStems)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindStems.class);
                    // TODO : Fill in the findStems468 here
                
                        assertNotNull(stub.findStems(
                        findStems468));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindStems() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindStems findStems468=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindStems)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindStems.class);
                    // TODO : Fill in the findStems468 here
                

                stub.startfindStems(
                         findStems468,
                    new tempCallbackN67147()
                );
              


        }

        private class tempCallbackN67147  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67147(){ super(null);}

            public void receiveResultfindStems(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindStemsResponse result
                            ) {
                
            }

            public void receiveErrorfindStems(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetPermissionAssignmentsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetPermissionAssignmentsLite getPermissionAssignmentsLite470=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetPermissionAssignmentsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetPermissionAssignmentsLite.class);
                    // TODO : Fill in the getPermissionAssignmentsLite470 here
                
                        assertNotNull(stub.getPermissionAssignmentsLite(
                        getPermissionAssignmentsLite470));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetPermissionAssignmentsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetPermissionAssignmentsLite getPermissionAssignmentsLite470=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetPermissionAssignmentsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetPermissionAssignmentsLite.class);
                    // TODO : Fill in the getPermissionAssignmentsLite470 here
                

                stub.startgetPermissionAssignmentsLite(
                         getPermissionAssignmentsLite470,
                    new tempCallbackN67188()
                );
              


        }

        private class tempCallbackN67188  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67188(){ super(null);}

            public void receiveResultgetPermissionAssignmentsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetPermissionAssignmentsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetPermissionAssignmentsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetAttributeAssignActions() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAttributeAssignActions getAttributeAssignActions472=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAttributeAssignActions)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAttributeAssignActions.class);
                    // TODO : Fill in the getAttributeAssignActions472 here
                
                        assertNotNull(stub.getAttributeAssignActions(
                        getAttributeAssignActions472));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetAttributeAssignActions() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAttributeAssignActions getAttributeAssignActions472=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAttributeAssignActions)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAttributeAssignActions.class);
                    // TODO : Fill in the getAttributeAssignActions472 here
                

                stub.startgetAttributeAssignActions(
                         getAttributeAssignActions472,
                    new tempCallbackN67229()
                );
              


        }

        private class tempCallbackN67229  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67229(){ super(null);}

            public void receiveResultgetAttributeAssignActions(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAttributeAssignActionsResponse result
                            ) {
                
            }

            public void receiveErrorgetAttributeAssignActions(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testacknowledge() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.Acknowledge acknowledge474=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.Acknowledge)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.Acknowledge.class);
                    // TODO : Fill in the acknowledge474 here
                
                        assertNotNull(stub.acknowledge(
                        acknowledge474));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartacknowledge() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.Acknowledge acknowledge474=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.Acknowledge)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.Acknowledge.class);
                    // TODO : Fill in the acknowledge474 here
                

                stub.startacknowledge(
                         acknowledge474,
                    new tempCallbackN67270()
                );
              


        }

        private class tempCallbackN67270  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67270(){ super(null);}

            public void receiveResultacknowledge(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AcknowledgeResponse result
                            ) {
                
            }

            public void receiveErroracknowledge(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemDelete() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.StemDelete stemDelete476=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.StemDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.StemDelete.class);
                    // TODO : Fill in the stemDelete476 here
                
                        assertNotNull(stub.stemDelete(
                        stemDelete476));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemDelete() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.StemDelete stemDelete476=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.StemDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.StemDelete.class);
                    // TODO : Fill in the stemDelete476 here
                

                stub.startstemDelete(
                         stemDelete476,
                    new tempCallbackN67311()
                );
              


        }

        private class tempCallbackN67311  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67311(){ super(null);}

            public void receiveResultstemDelete(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.StemDeleteResponse result
                            ) {
                
            }

            public void receiveErrorstemDelete(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignAttributeDefNameInheritance() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributeDefNameInheritance assignAttributeDefNameInheritance478=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributeDefNameInheritance)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributeDefNameInheritance.class);
                    // TODO : Fill in the assignAttributeDefNameInheritance478 here
                
                        assertNotNull(stub.assignAttributeDefNameInheritance(
                        assignAttributeDefNameInheritance478));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignAttributeDefNameInheritance() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributeDefNameInheritance assignAttributeDefNameInheritance478=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributeDefNameInheritance)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributeDefNameInheritance.class);
                    // TODO : Fill in the assignAttributeDefNameInheritance478 here
                

                stub.startassignAttributeDefNameInheritance(
                         assignAttributeDefNameInheritance478,
                    new tempCallbackN67352()
                );
              


        }

        private class tempCallbackN67352  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67352(){ super(null);}

            public void receiveResultassignAttributeDefNameInheritance(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributeDefNameInheritanceResponse result
                            ) {
                
            }

            public void receiveErrorassignAttributeDefNameInheritance(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetAttributeAssignActionsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAttributeAssignActionsLite getAttributeAssignActionsLite480=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAttributeAssignActionsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAttributeAssignActionsLite.class);
                    // TODO : Fill in the getAttributeAssignActionsLite480 here
                
                        assertNotNull(stub.getAttributeAssignActionsLite(
                        getAttributeAssignActionsLite480));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetAttributeAssignActionsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAttributeAssignActionsLite getAttributeAssignActionsLite480=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAttributeAssignActionsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAttributeAssignActionsLite.class);
                    // TODO : Fill in the getAttributeAssignActionsLite480 here
                

                stub.startgetAttributeAssignActionsLite(
                         getAttributeAssignActionsLite480,
                    new tempCallbackN67393()
                );
              


        }

        private class tempCallbackN67393  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67393(){ super(null);}

            public void receiveResultgetAttributeAssignActionsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAttributeAssignActionsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetAttributeAssignActionsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindAttributeDefsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindAttributeDefsLite findAttributeDefsLite482=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindAttributeDefsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindAttributeDefsLite.class);
                    // TODO : Fill in the findAttributeDefsLite482 here
                
                        assertNotNull(stub.findAttributeDefsLite(
                        findAttributeDefsLite482));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindAttributeDefsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindAttributeDefsLite findAttributeDefsLite482=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindAttributeDefsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindAttributeDefsLite.class);
                    // TODO : Fill in the findAttributeDefsLite482 here
                

                stub.startfindAttributeDefsLite(
                         findAttributeDefsLite482,
                    new tempCallbackN67434()
                );
              


        }

        private class tempCallbackN67434  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67434(){ super(null);}

            public void receiveResultfindAttributeDefsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindAttributeDefsLiteResponse result
                            ) {
                
            }

            public void receiveErrorfindAttributeDefsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testattributeDefSave() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefSave attributeDefSave484=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefSave.class);
                    // TODO : Fill in the attributeDefSave484 here
                
                        assertNotNull(stub.attributeDefSave(
                        attributeDefSave484));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartattributeDefSave() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefSave attributeDefSave484=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefSave.class);
                    // TODO : Fill in the attributeDefSave484 here
                

                stub.startattributeDefSave(
                         attributeDefSave484,
                    new tempCallbackN67475()
                );
              


        }

        private class tempCallbackN67475  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67475(){ super(null);}

            public void receiveResultattributeDefSave(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefSaveResponse result
                            ) {
                
            }

            public void receiveErrorattributeDefSave(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetSubjects() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetSubjects getSubjects486=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetSubjects)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetSubjects.class);
                    // TODO : Fill in the getSubjects486 here
                
                        assertNotNull(stub.getSubjects(
                        getSubjects486));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetSubjects() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetSubjects getSubjects486=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetSubjects)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetSubjects.class);
                    // TODO : Fill in the getSubjects486 here
                

                stub.startgetSubjects(
                         getSubjects486,
                    new tempCallbackN67516()
                );
              


        }

        private class tempCallbackN67516  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67516(){ super(null);}

            public void receiveResultgetSubjects(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetSubjectsResponse result
                            ) {
                
            }

            public void receiveErrorgetSubjects(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetAuditEntries() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAuditEntries getAuditEntries488=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAuditEntries)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAuditEntries.class);
                    // TODO : Fill in the getAuditEntries488 here
                
                        assertNotNull(stub.getAuditEntries(
                        getAuditEntries488));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetAuditEntries() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAuditEntries getAuditEntries488=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAuditEntries)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAuditEntries.class);
                    // TODO : Fill in the getAuditEntries488 here
                

                stub.startgetAuditEntries(
                         getAuditEntries488,
                    new tempCallbackN67557()
                );
              


        }

        private class tempCallbackN67557  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67557(){ super(null);}

            public void receiveResultgetAuditEntries(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAuditEntriesResponse result
                            ) {
                
            }

            public void receiveErrorgetAuditEntries(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgroupDeleteLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GroupDeleteLite groupDeleteLite490=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GroupDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GroupDeleteLite.class);
                    // TODO : Fill in the groupDeleteLite490 here
                
                        assertNotNull(stub.groupDeleteLite(
                        groupDeleteLite490));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupDeleteLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GroupDeleteLite groupDeleteLite490=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GroupDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GroupDeleteLite.class);
                    // TODO : Fill in the groupDeleteLite490 here
                

                stub.startgroupDeleteLite(
                         groupDeleteLite490,
                    new tempCallbackN67598()
                );
              


        }

        private class tempCallbackN67598  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67598(){ super(null);}

            public void receiveResultgroupDeleteLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GroupDeleteLiteResponse result
                            ) {
                
            }

            public void receiveErrorgroupDeleteLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMemberships() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetMemberships getMemberships492=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetMemberships)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetMemberships.class);
                    // TODO : Fill in the getMemberships492 here
                
                        assertNotNull(stub.getMemberships(
                        getMemberships492));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMemberships() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetMemberships getMemberships492=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetMemberships)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetMemberships.class);
                    // TODO : Fill in the getMemberships492 here
                

                stub.startgetMemberships(
                         getMemberships492,
                    new tempCallbackN67639()
                );
              


        }

        private class tempCallbackN67639  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67639(){ super(null);}

            public void receiveResultgetMemberships(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetMembershipsResponse result
                            ) {
                
            }

            public void receiveErrorgetMemberships(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testattributeDefNameDelete() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefNameDelete attributeDefNameDelete494=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefNameDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefNameDelete.class);
                    // TODO : Fill in the attributeDefNameDelete494 here
                
                        assertNotNull(stub.attributeDefNameDelete(
                        attributeDefNameDelete494));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartattributeDefNameDelete() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefNameDelete attributeDefNameDelete494=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefNameDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefNameDelete.class);
                    // TODO : Fill in the attributeDefNameDelete494 here
                

                stub.startattributeDefNameDelete(
                         attributeDefNameDelete494,
                    new tempCallbackN67680()
                );
              


        }

        private class tempCallbackN67680  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67680(){ super(null);}

            public void receiveResultattributeDefNameDelete(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefNameDeleteResponse result
                            ) {
                
            }

            public void receiveErrorattributeDefNameDelete(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindGroups() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindGroups findGroups496=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindGroups.class);
                    // TODO : Fill in the findGroups496 here
                
                        assertNotNull(stub.findGroups(
                        findGroups496));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindGroups() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindGroups findGroups496=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindGroups.class);
                    // TODO : Fill in the findGroups496 here
                

                stub.startfindGroups(
                         findGroups496,
                    new tempCallbackN67721()
                );
              


        }

        private class tempCallbackN67721  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67721(){ super(null);}

            public void receiveResultfindGroups(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindGroupsResponse result
                            ) {
                
            }

            public void receiveErrorfindGroups(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testaddMemberLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AddMemberLite addMemberLite498=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AddMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AddMemberLite.class);
                    // TODO : Fill in the addMemberLite498 here
                
                        assertNotNull(stub.addMemberLite(
                        addMemberLite498));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartaddMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AddMemberLite addMemberLite498=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AddMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AddMemberLite.class);
                    // TODO : Fill in the addMemberLite498 here
                

                stub.startaddMemberLite(
                         addMemberLite498,
                    new tempCallbackN67762()
                );
              


        }

        private class tempCallbackN67762  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67762(){ super(null);}

            public void receiveResultaddMemberLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AddMemberLiteResponse result
                            ) {
                
            }

            public void receiveErroraddMemberLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignAttributes() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributes assignAttributes500=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributes)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributes.class);
                    // TODO : Fill in the assignAttributes500 here
                
                        assertNotNull(stub.assignAttributes(
                        assignAttributes500));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignAttributes() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributes assignAttributes500=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributes)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributes.class);
                    // TODO : Fill in the assignAttributes500 here
                

                stub.startassignAttributes(
                         assignAttributes500,
                    new tempCallbackN67803()
                );
              


        }

        private class tempCallbackN67803  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67803(){ super(null);}

            public void receiveResultassignAttributes(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributesResponse result
                            ) {
                
            }

            public void receiveErrorassignAttributes(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testattributeDefDelete() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefDelete attributeDefDelete502=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefDelete.class);
                    // TODO : Fill in the attributeDefDelete502 here
                
                        assertNotNull(stub.attributeDefDelete(
                        attributeDefDelete502));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartattributeDefDelete() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefDelete attributeDefDelete502=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefDelete.class);
                    // TODO : Fill in the attributeDefDelete502 here
                

                stub.startattributeDefDelete(
                         attributeDefDelete502,
                    new tempCallbackN67844()
                );
              


        }

        private class tempCallbackN67844  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67844(){ super(null);}

            public void receiveResultattributeDefDelete(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefDeleteResponse result
                            ) {
                
            }

            public void receiveErrorattributeDefDelete(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemDeleteLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.StemDeleteLite stemDeleteLite504=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.StemDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.StemDeleteLite.class);
                    // TODO : Fill in the stemDeleteLite504 here
                
                        assertNotNull(stub.stemDeleteLite(
                        stemDeleteLite504));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemDeleteLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.StemDeleteLite stemDeleteLite504=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.StemDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.StemDeleteLite.class);
                    // TODO : Fill in the stemDeleteLite504 here
                

                stub.startstemDeleteLite(
                         stemDeleteLite504,
                    new tempCallbackN67885()
                );
              


        }

        private class tempCallbackN67885  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67885(){ super(null);}

            public void receiveResultstemDeleteLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.StemDeleteLiteResponse result
                            ) {
                
            }

            public void receiveErrorstemDeleteLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testexternalSubjectSave() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.ExternalSubjectSave externalSubjectSave506=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.ExternalSubjectSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.ExternalSubjectSave.class);
                    // TODO : Fill in the externalSubjectSave506 here
                
                        assertNotNull(stub.externalSubjectSave(
                        externalSubjectSave506));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartexternalSubjectSave() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.ExternalSubjectSave externalSubjectSave506=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.ExternalSubjectSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.ExternalSubjectSave.class);
                    // TODO : Fill in the externalSubjectSave506 here
                

                stub.startexternalSubjectSave(
                         externalSubjectSave506,
                    new tempCallbackN67926()
                );
              


        }

        private class tempCallbackN67926  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67926(){ super(null);}

            public void receiveResultexternalSubjectSave(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.ExternalSubjectSaveResponse result
                            ) {
                
            }

            public void receiveErrorexternalSubjectSave(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindStemsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindStemsLite findStemsLite508=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindStemsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindStemsLite.class);
                    // TODO : Fill in the findStemsLite508 here
                
                        assertNotNull(stub.findStemsLite(
                        findStemsLite508));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindStemsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindStemsLite findStemsLite508=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindStemsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindStemsLite.class);
                    // TODO : Fill in the findStemsLite508 here
                

                stub.startfindStemsLite(
                         findStemsLite508,
                    new tempCallbackN67967()
                );
              


        }

        private class tempCallbackN67967  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67967(){ super(null);}

            public void receiveResultfindStemsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindStemsLiteResponse result
                            ) {
                
            }

            public void receiveErrorfindStemsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetAttributeAssignmentsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAttributeAssignmentsLite getAttributeAssignmentsLite510=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAttributeAssignmentsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAttributeAssignmentsLite.class);
                    // TODO : Fill in the getAttributeAssignmentsLite510 here
                
                        assertNotNull(stub.getAttributeAssignmentsLite(
                        getAttributeAssignmentsLite510));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetAttributeAssignmentsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAttributeAssignmentsLite getAttributeAssignmentsLite510=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAttributeAssignmentsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAttributeAssignmentsLite.class);
                    // TODO : Fill in the getAttributeAssignmentsLite510 here
                

                stub.startgetAttributeAssignmentsLite(
                         getAttributeAssignmentsLite510,
                    new tempCallbackN68008()
                );
              


        }

        private class tempCallbackN68008  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN68008(){ super(null);}

            public void receiveResultgetAttributeAssignmentsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAttributeAssignmentsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetAttributeAssignmentsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testattributeDefNameDeleteLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefNameDeleteLite attributeDefNameDeleteLite512=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefNameDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefNameDeleteLite.class);
                    // TODO : Fill in the attributeDefNameDeleteLite512 here
                
                        assertNotNull(stub.attributeDefNameDeleteLite(
                        attributeDefNameDeleteLite512));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartattributeDefNameDeleteLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefNameDeleteLite attributeDefNameDeleteLite512=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefNameDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefNameDeleteLite.class);
                    // TODO : Fill in the attributeDefNameDeleteLite512 here
                

                stub.startattributeDefNameDeleteLite(
                         attributeDefNameDeleteLite512,
                    new tempCallbackN68049()
                );
              


        }

        private class tempCallbackN68049  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN68049(){ super(null);}

            public void receiveResultattributeDefNameDeleteLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AttributeDefNameDeleteLiteResponse result
                            ) {
                
            }

            public void receiveErrorattributeDefNameDeleteLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgroupDelete() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GroupDelete groupDelete514=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GroupDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GroupDelete.class);
                    // TODO : Fill in the groupDelete514 here
                
                        assertNotNull(stub.groupDelete(
                        groupDelete514));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupDelete() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GroupDelete groupDelete514=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GroupDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GroupDelete.class);
                    // TODO : Fill in the groupDelete514 here
                

                stub.startgroupDelete(
                         groupDelete514,
                    new tempCallbackN68090()
                );
              


        }

        private class tempCallbackN68090  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN68090(){ super(null);}

            public void receiveResultgroupDelete(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GroupDeleteResponse result
                            ) {
                
            }

            public void receiveErrorgroupDelete(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignAttributeDefNameInheritanceLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributeDefNameInheritanceLite assignAttributeDefNameInheritanceLite516=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributeDefNameInheritanceLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributeDefNameInheritanceLite.class);
                    // TODO : Fill in the assignAttributeDefNameInheritanceLite516 here
                
                        assertNotNull(stub.assignAttributeDefNameInheritanceLite(
                        assignAttributeDefNameInheritanceLite516));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignAttributeDefNameInheritanceLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributeDefNameInheritanceLite assignAttributeDefNameInheritanceLite516=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributeDefNameInheritanceLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributeDefNameInheritanceLite.class);
                    // TODO : Fill in the assignAttributeDefNameInheritanceLite516 here
                

                stub.startassignAttributeDefNameInheritanceLite(
                         assignAttributeDefNameInheritanceLite516,
                    new tempCallbackN68131()
                );
              


        }

        private class tempCallbackN68131  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN68131(){ super(null);}

            public void receiveResultassignAttributeDefNameInheritanceLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.AssignAttributeDefNameInheritanceLiteResponse result
                            ) {
                
            }

            public void receiveErrorassignAttributeDefNameInheritanceLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindExternalSubjects() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindExternalSubjects findExternalSubjects518=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindExternalSubjects)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindExternalSubjects.class);
                    // TODO : Fill in the findExternalSubjects518 here
                
                        assertNotNull(stub.findExternalSubjects(
                        findExternalSubjects518));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindExternalSubjects() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindExternalSubjects findExternalSubjects518=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindExternalSubjects)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindExternalSubjects.class);
                    // TODO : Fill in the findExternalSubjects518 here
                

                stub.startfindExternalSubjects(
                         findExternalSubjects518,
                    new tempCallbackN68172()
                );
              


        }

        private class tempCallbackN68172  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN68172(){ super(null);}

            public void receiveResultfindExternalSubjects(
                         edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindExternalSubjectsResponse result
                            ) {
                
            }

            public void receiveErrorfindExternalSubjects(java.lang.Exception e) {
                fail();
            }

        }
      
        //Create an ADBBean and provide it as the test object
        public org.apache.axis2.databinding.ADBBean getTestObject(java.lang.Class type) throws java.lang.Exception{
           return (org.apache.axis2.databinding.ADBBean) type.newInstance();
        }

        
        

    }
    