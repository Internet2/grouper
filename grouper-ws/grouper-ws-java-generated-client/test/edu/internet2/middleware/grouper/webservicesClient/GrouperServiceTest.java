

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
        public  void testgetGrouperPrivilegesLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGrouperPrivilegesLite getGrouperPrivilegesLite288=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGrouperPrivilegesLite.class);
                    // TODO : Fill in the getGrouperPrivilegesLite288 here
                
                        assertNotNull(stub.getGrouperPrivilegesLite(
                        getGrouperPrivilegesLite288));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetGrouperPrivilegesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGrouperPrivilegesLite getGrouperPrivilegesLite288=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGrouperPrivilegesLite.class);
                    // TODO : Fill in the getGrouperPrivilegesLite288 here
                

                stub.startgetGrouperPrivilegesLite(
                         getGrouperPrivilegesLite288,
                    new tempCallbackN65548()
                );
              


        }

        private class tempCallbackN65548  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65548(){ super(null);}

            public void receiveResultgetGrouperPrivilegesLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGrouperPrivilegesLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetGrouperPrivilegesLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindStemsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStemsLite findStemsLite290=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStemsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStemsLite.class);
                    // TODO : Fill in the findStemsLite290 here
                
                        assertNotNull(stub.findStemsLite(
                        findStemsLite290));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindStemsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStemsLite findStemsLite290=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStemsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStemsLite.class);
                    // TODO : Fill in the findStemsLite290 here
                

                stub.startfindStemsLite(
                         findStemsLite290,
                    new tempCallbackN65589()
                );
              


        }

        private class tempCallbackN65589  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65589(){ super(null);}

            public void receiveResultfindStemsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStemsLiteResponse result
                            ) {
                
            }

            public void receiveErrorfindStemsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindGroupsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroupsLite findGroupsLite292=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroupsLite.class);
                    // TODO : Fill in the findGroupsLite292 here
                
                        assertNotNull(stub.findGroupsLite(
                        findGroupsLite292));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindGroupsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroupsLite findGroupsLite292=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroupsLite.class);
                    // TODO : Fill in the findGroupsLite292 here
                

                stub.startfindGroupsLite(
                         findGroupsLite292,
                    new tempCallbackN65630()
                );
              


        }

        private class tempCallbackN65630  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65630(){ super(null);}

            public void receiveResultfindGroupsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroupsLiteResponse result
                            ) {
                
            }

            public void receiveErrorfindGroupsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindAttributeDefNamesLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNamesLite findAttributeDefNamesLite294=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNamesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNamesLite.class);
                    // TODO : Fill in the findAttributeDefNamesLite294 here
                
                        assertNotNull(stub.findAttributeDefNamesLite(
                        findAttributeDefNamesLite294));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindAttributeDefNamesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNamesLite findAttributeDefNamesLite294=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNamesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNamesLite.class);
                    // TODO : Fill in the findAttributeDefNamesLite294 here
                

                stub.startfindAttributeDefNamesLite(
                         findAttributeDefNamesLite294,
                    new tempCallbackN65671()
                );
              


        }

        private class tempCallbackN65671  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65671(){ super(null);}

            public void receiveResultfindAttributeDefNamesLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNamesLiteResponse result
                            ) {
                
            }

            public void receiveErrorfindAttributeDefNamesLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetGroups() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroups getGroups296=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroups.class);
                    // TODO : Fill in the getGroups296 here
                
                        assertNotNull(stub.getGroups(
                        getGroups296));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetGroups() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroups getGroups296=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroups.class);
                    // TODO : Fill in the getGroups296 here
                

                stub.startgetGroups(
                         getGroups296,
                    new tempCallbackN65712()
                );
              


        }

        private class tempCallbackN65712  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65712(){ super(null);}

            public void receiveResultgetGroups(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroupsResponse result
                            ) {
                
            }

            public void receiveErrorgetGroups(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignPermissions() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissions assignPermissions298=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissions)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissions.class);
                    // TODO : Fill in the assignPermissions298 here
                
                        assertNotNull(stub.assignPermissions(
                        assignPermissions298));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignPermissions() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissions assignPermissions298=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissions)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissions.class);
                    // TODO : Fill in the assignPermissions298 here
                

                stub.startassignPermissions(
                         assignPermissions298,
                    new tempCallbackN65753()
                );
              


        }

        private class tempCallbackN65753  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65753(){ super(null);}

            public void receiveResultassignPermissions(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissionsResponse result
                            ) {
                
            }

            public void receiveErrorassignPermissions(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignAttributesBatch() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributesBatch assignAttributesBatch300=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributesBatch)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributesBatch.class);
                    // TODO : Fill in the assignAttributesBatch300 here
                
                        assertNotNull(stub.assignAttributesBatch(
                        assignAttributesBatch300));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignAttributesBatch() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributesBatch assignAttributesBatch300=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributesBatch)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributesBatch.class);
                    // TODO : Fill in the assignAttributesBatch300 here
                

                stub.startassignAttributesBatch(
                         assignAttributesBatch300,
                    new tempCallbackN65794()
                );
              


        }

        private class tempCallbackN65794  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65794(){ super(null);}

            public void receiveResultassignAttributesBatch(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributesBatchResponse result
                            ) {
                
            }

            public void receiveErrorassignAttributesBatch(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetAttributeAssignmentsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignmentsLite getAttributeAssignmentsLite302=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignmentsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignmentsLite.class);
                    // TODO : Fill in the getAttributeAssignmentsLite302 here
                
                        assertNotNull(stub.getAttributeAssignmentsLite(
                        getAttributeAssignmentsLite302));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetAttributeAssignmentsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignmentsLite getAttributeAssignmentsLite302=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignmentsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignmentsLite.class);
                    // TODO : Fill in the getAttributeAssignmentsLite302 here
                

                stub.startgetAttributeAssignmentsLite(
                         getAttributeAssignmentsLite302,
                    new tempCallbackN65835()
                );
              


        }

        private class tempCallbackN65835  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65835(){ super(null);}

            public void receiveResultgetAttributeAssignmentsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignmentsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetAttributeAssignmentsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMembershipsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembershipsLite getMembershipsLite304=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembershipsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembershipsLite.class);
                    // TODO : Fill in the getMembershipsLite304 here
                
                        assertNotNull(stub.getMembershipsLite(
                        getMembershipsLite304));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMembershipsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembershipsLite getMembershipsLite304=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembershipsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembershipsLite.class);
                    // TODO : Fill in the getMembershipsLite304 here
                

                stub.startgetMembershipsLite(
                         getMembershipsLite304,
                    new tempCallbackN65876()
                );
              


        }

        private class tempCallbackN65876  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65876(){ super(null);}

            public void receiveResultgetMembershipsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembershipsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetMembershipsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMembersLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembersLite getMembersLite306=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembersLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembersLite.class);
                    // TODO : Fill in the getMembersLite306 here
                
                        assertNotNull(stub.getMembersLite(
                        getMembersLite306));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMembersLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembersLite getMembersLite306=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembersLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembersLite.class);
                    // TODO : Fill in the getMembersLite306 here
                

                stub.startgetMembersLite(
                         getMembersLite306,
                    new tempCallbackN65917()
                );
              


        }

        private class tempCallbackN65917  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65917(){ super(null);}

            public void receiveResultgetMembersLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembersLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetMembersLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindGroups() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroups findGroups308=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroups.class);
                    // TODO : Fill in the findGroups308 here
                
                        assertNotNull(stub.findGroups(
                        findGroups308));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindGroups() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroups findGroups308=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroups.class);
                    // TODO : Fill in the findGroups308 here
                

                stub.startfindGroups(
                         findGroups308,
                    new tempCallbackN65958()
                );
              


        }

        private class tempCallbackN65958  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65958(){ super(null);}

            public void receiveResultfindGroups(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroupsResponse result
                            ) {
                
            }

            public void receiveErrorfindGroups(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindStems() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStems findStems310=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStems)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStems.class);
                    // TODO : Fill in the findStems310 here
                
                        assertNotNull(stub.findStems(
                        findStems310));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindStems() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStems findStems310=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStems)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStems.class);
                    // TODO : Fill in the findStems310 here
                

                stub.startfindStems(
                         findStems310,
                    new tempCallbackN65999()
                );
              


        }

        private class tempCallbackN65999  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65999(){ super(null);}

            public void receiveResultfindStems(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStemsResponse result
                            ) {
                
            }

            public void receiveErrorfindStems(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetPermissionAssignments() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignments getPermissionAssignments312=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignments)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignments.class);
                    // TODO : Fill in the getPermissionAssignments312 here
                
                        assertNotNull(stub.getPermissionAssignments(
                        getPermissionAssignments312));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetPermissionAssignments() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignments getPermissionAssignments312=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignments)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignments.class);
                    // TODO : Fill in the getPermissionAssignments312 here
                

                stub.startgetPermissionAssignments(
                         getPermissionAssignments312,
                    new tempCallbackN66040()
                );
              


        }

        private class tempCallbackN66040  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66040(){ super(null);}

            public void receiveResultgetPermissionAssignments(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignmentsResponse result
                            ) {
                
            }

            public void receiveErrorgetPermissionAssignments(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testattributeDefNameSaveLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameSaveLite attributeDefNameSaveLite314=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameSaveLite.class);
                    // TODO : Fill in the attributeDefNameSaveLite314 here
                
                        assertNotNull(stub.attributeDefNameSaveLite(
                        attributeDefNameSaveLite314));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartattributeDefNameSaveLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameSaveLite attributeDefNameSaveLite314=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameSaveLite.class);
                    // TODO : Fill in the attributeDefNameSaveLite314 here
                

                stub.startattributeDefNameSaveLite(
                         attributeDefNameSaveLite314,
                    new tempCallbackN66081()
                );
              


        }

        private class tempCallbackN66081  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66081(){ super(null);}

            public void receiveResultattributeDefNameSaveLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameSaveLiteResponse result
                            ) {
                
            }

            public void receiveErrorattributeDefNameSaveLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemSave() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSave stemSave316=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSave.class);
                    // TODO : Fill in the stemSave316 here
                
                        assertNotNull(stub.stemSave(
                        stemSave316));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemSave() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSave stemSave316=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSave.class);
                    // TODO : Fill in the stemSave316 here
                

                stub.startstemSave(
                         stemSave316,
                    new tempCallbackN66122()
                );
              


        }

        private class tempCallbackN66122  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66122(){ super(null);}

            public void receiveResultstemSave(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSaveResponse result
                            ) {
                
            }

            public void receiveErrorstemSave(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testhasMember() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMember hasMember318=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMember.class);
                    // TODO : Fill in the hasMember318 here
                
                        assertNotNull(stub.hasMember(
                        hasMember318));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStarthasMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMember hasMember318=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMember.class);
                    // TODO : Fill in the hasMember318 here
                

                stub.starthasMember(
                         hasMember318,
                    new tempCallbackN66163()
                );
              


        }

        private class tempCallbackN66163  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66163(){ super(null);}

            public void receiveResulthasMember(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMemberResponse result
                            ) {
                
            }

            public void receiveErrorhasMember(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetGroupsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroupsLite getGroupsLite320=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroupsLite.class);
                    // TODO : Fill in the getGroupsLite320 here
                
                        assertNotNull(stub.getGroupsLite(
                        getGroupsLite320));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetGroupsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroupsLite getGroupsLite320=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroupsLite.class);
                    // TODO : Fill in the getGroupsLite320 here
                

                stub.startgetGroupsLite(
                         getGroupsLite320,
                    new tempCallbackN66204()
                );
              


        }

        private class tempCallbackN66204  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66204(){ super(null);}

            public void receiveResultgetGroupsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroupsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetGroupsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMemberships() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMemberships getMemberships322=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMemberships)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMemberships.class);
                    // TODO : Fill in the getMemberships322 here
                
                        assertNotNull(stub.getMemberships(
                        getMemberships322));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMemberships() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMemberships getMemberships322=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMemberships)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMemberships.class);
                    // TODO : Fill in the getMemberships322 here
                

                stub.startgetMemberships(
                         getMemberships322,
                    new tempCallbackN66245()
                );
              


        }

        private class tempCallbackN66245  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66245(){ super(null);}

            public void receiveResultgetMemberships(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembershipsResponse result
                            ) {
                
            }

            public void receiveErrorgetMemberships(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMembers() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembers getMembers324=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembers)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembers.class);
                    // TODO : Fill in the getMembers324 here
                
                        assertNotNull(stub.getMembers(
                        getMembers324));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMembers() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembers getMembers324=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembers)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembers.class);
                    // TODO : Fill in the getMembers324 here
                

                stub.startgetMembers(
                         getMembers324,
                    new tempCallbackN66286()
                );
              


        }

        private class tempCallbackN66286  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66286(){ super(null);}

            public void receiveResultgetMembers(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembersResponse result
                            ) {
                
            }

            public void receiveErrorgetMembers(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignGrouperPrivileges() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivileges assignGrouperPrivileges326=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivileges)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivileges.class);
                    // TODO : Fill in the assignGrouperPrivileges326 here
                
                        assertNotNull(stub.assignGrouperPrivileges(
                        assignGrouperPrivileges326));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignGrouperPrivileges() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivileges assignGrouperPrivileges326=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivileges)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivileges.class);
                    // TODO : Fill in the assignGrouperPrivileges326 here
                

                stub.startassignGrouperPrivileges(
                         assignGrouperPrivileges326,
                    new tempCallbackN66327()
                );
              


        }

        private class tempCallbackN66327  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66327(){ super(null);}

            public void receiveResultassignGrouperPrivileges(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivilegesResponse result
                            ) {
                
            }

            public void receiveErrorassignGrouperPrivileges(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testattributeDefNameSave() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameSave attributeDefNameSave328=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameSave.class);
                    // TODO : Fill in the attributeDefNameSave328 here
                
                        assertNotNull(stub.attributeDefNameSave(
                        attributeDefNameSave328));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartattributeDefNameSave() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameSave attributeDefNameSave328=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameSave.class);
                    // TODO : Fill in the attributeDefNameSave328 here
                

                stub.startattributeDefNameSave(
                         attributeDefNameSave328,
                    new tempCallbackN66368()
                );
              


        }

        private class tempCallbackN66368  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66368(){ super(null);}

            public void receiveResultattributeDefNameSave(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameSaveResponse result
                            ) {
                
            }

            public void receiveErrorattributeDefNameSave(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetSubjects() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjects getSubjects330=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjects)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjects.class);
                    // TODO : Fill in the getSubjects330 here
                
                        assertNotNull(stub.getSubjects(
                        getSubjects330));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetSubjects() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjects getSubjects330=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjects)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjects.class);
                    // TODO : Fill in the getSubjects330 here
                

                stub.startgetSubjects(
                         getSubjects330,
                    new tempCallbackN66409()
                );
              


        }

        private class tempCallbackN66409  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66409(){ super(null);}

            public void receiveResultgetSubjects(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjectsResponse result
                            ) {
                
            }

            public void receiveErrorgetSubjects(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindAttributeDefNames() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNames findAttributeDefNames332=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNames)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNames.class);
                    // TODO : Fill in the findAttributeDefNames332 here
                
                        assertNotNull(stub.findAttributeDefNames(
                        findAttributeDefNames332));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindAttributeDefNames() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNames findAttributeDefNames332=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNames)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNames.class);
                    // TODO : Fill in the findAttributeDefNames332 here
                

                stub.startfindAttributeDefNames(
                         findAttributeDefNames332,
                    new tempCallbackN66450()
                );
              


        }

        private class tempCallbackN66450  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66450(){ super(null);}

            public void receiveResultfindAttributeDefNames(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNamesResponse result
                            ) {
                
            }

            public void receiveErrorfindAttributeDefNames(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetSubjectsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjectsLite getSubjectsLite334=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjectsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjectsLite.class);
                    // TODO : Fill in the getSubjectsLite334 here
                
                        assertNotNull(stub.getSubjectsLite(
                        getSubjectsLite334));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetSubjectsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjectsLite getSubjectsLite334=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjectsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjectsLite.class);
                    // TODO : Fill in the getSubjectsLite334 here
                

                stub.startgetSubjectsLite(
                         getSubjectsLite334,
                    new tempCallbackN66491()
                );
              


        }

        private class tempCallbackN66491  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66491(){ super(null);}

            public void receiveResultgetSubjectsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjectsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetSubjectsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testmemberChangeSubject() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubject memberChangeSubject336=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubject)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubject.class);
                    // TODO : Fill in the memberChangeSubject336 here
                
                        assertNotNull(stub.memberChangeSubject(
                        memberChangeSubject336));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartmemberChangeSubject() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubject memberChangeSubject336=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubject)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubject.class);
                    // TODO : Fill in the memberChangeSubject336 here
                

                stub.startmemberChangeSubject(
                         memberChangeSubject336,
                    new tempCallbackN66532()
                );
              


        }

        private class tempCallbackN66532  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66532(){ super(null);}

            public void receiveResultmemberChangeSubject(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubjectResponse result
                            ) {
                
            }

            public void receiveErrormemberChangeSubject(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignPermissionsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissionsLite assignPermissionsLite338=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissionsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissionsLite.class);
                    // TODO : Fill in the assignPermissionsLite338 here
                
                        assertNotNull(stub.assignPermissionsLite(
                        assignPermissionsLite338));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignPermissionsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissionsLite assignPermissionsLite338=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissionsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissionsLite.class);
                    // TODO : Fill in the assignPermissionsLite338 here
                

                stub.startassignPermissionsLite(
                         assignPermissionsLite338,
                    new tempCallbackN66573()
                );
              


        }

        private class tempCallbackN66573  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66573(){ super(null);}

            public void receiveResultassignPermissionsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissionsLiteResponse result
                            ) {
                
            }

            public void receiveErrorassignPermissionsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testmemberChangeSubjectLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubjectLite memberChangeSubjectLite340=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubjectLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubjectLite.class);
                    // TODO : Fill in the memberChangeSubjectLite340 here
                
                        assertNotNull(stub.memberChangeSubjectLite(
                        memberChangeSubjectLite340));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartmemberChangeSubjectLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubjectLite memberChangeSubjectLite340=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubjectLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubjectLite.class);
                    // TODO : Fill in the memberChangeSubjectLite340 here
                

                stub.startmemberChangeSubjectLite(
                         memberChangeSubjectLite340,
                    new tempCallbackN66614()
                );
              


        }

        private class tempCallbackN66614  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66614(){ super(null);}

            public void receiveResultmemberChangeSubjectLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubjectLiteResponse result
                            ) {
                
            }

            public void receiveErrormemberChangeSubjectLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemDelete() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDelete stemDelete342=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDelete.class);
                    // TODO : Fill in the stemDelete342 here
                
                        assertNotNull(stub.stemDelete(
                        stemDelete342));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemDelete() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDelete stemDelete342=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDelete.class);
                    // TODO : Fill in the stemDelete342 here
                

                stub.startstemDelete(
                         stemDelete342,
                    new tempCallbackN66655()
                );
              


        }

        private class tempCallbackN66655  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66655(){ super(null);}

            public void receiveResultstemDelete(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDeleteResponse result
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

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributeDefNameInheritance assignAttributeDefNameInheritance344=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributeDefNameInheritance)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributeDefNameInheritance.class);
                    // TODO : Fill in the assignAttributeDefNameInheritance344 here
                
                        assertNotNull(stub.assignAttributeDefNameInheritance(
                        assignAttributeDefNameInheritance344));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignAttributeDefNameInheritance() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributeDefNameInheritance assignAttributeDefNameInheritance344=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributeDefNameInheritance)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributeDefNameInheritance.class);
                    // TODO : Fill in the assignAttributeDefNameInheritance344 here
                

                stub.startassignAttributeDefNameInheritance(
                         assignAttributeDefNameInheritance344,
                    new tempCallbackN66696()
                );
              


        }

        private class tempCallbackN66696  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66696(){ super(null);}

            public void receiveResultassignAttributeDefNameInheritance(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributeDefNameInheritanceResponse result
                            ) {
                
            }

            public void receiveErrorassignAttributeDefNameInheritance(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetAttributeAssignments() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignments getAttributeAssignments346=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignments)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignments.class);
                    // TODO : Fill in the getAttributeAssignments346 here
                
                        assertNotNull(stub.getAttributeAssignments(
                        getAttributeAssignments346));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetAttributeAssignments() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignments getAttributeAssignments346=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignments)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignments.class);
                    // TODO : Fill in the getAttributeAssignments346 here
                

                stub.startgetAttributeAssignments(
                         getAttributeAssignments346,
                    new tempCallbackN66737()
                );
              


        }

        private class tempCallbackN66737  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66737(){ super(null);}

            public void receiveResultgetAttributeAssignments(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignmentsResponse result
                            ) {
                
            }

            public void receiveErrorgetAttributeAssignments(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignGrouperPrivilegesLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivilegesLite assignGrouperPrivilegesLite348=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivilegesLite.class);
                    // TODO : Fill in the assignGrouperPrivilegesLite348 here
                
                        assertNotNull(stub.assignGrouperPrivilegesLite(
                        assignGrouperPrivilegesLite348));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignGrouperPrivilegesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivilegesLite assignGrouperPrivilegesLite348=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivilegesLite.class);
                    // TODO : Fill in the assignGrouperPrivilegesLite348 here
                

                stub.startassignGrouperPrivilegesLite(
                         assignGrouperPrivilegesLite348,
                    new tempCallbackN66778()
                );
              


        }

        private class tempCallbackN66778  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66778(){ super(null);}

            public void receiveResultassignGrouperPrivilegesLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivilegesLiteResponse result
                            ) {
                
            }

            public void receiveErrorassignGrouperPrivilegesLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignAttributeDefNameInheritanceLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributeDefNameInheritanceLite assignAttributeDefNameInheritanceLite350=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributeDefNameInheritanceLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributeDefNameInheritanceLite.class);
                    // TODO : Fill in the assignAttributeDefNameInheritanceLite350 here
                
                        assertNotNull(stub.assignAttributeDefNameInheritanceLite(
                        assignAttributeDefNameInheritanceLite350));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignAttributeDefNameInheritanceLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributeDefNameInheritanceLite assignAttributeDefNameInheritanceLite350=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributeDefNameInheritanceLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributeDefNameInheritanceLite.class);
                    // TODO : Fill in the assignAttributeDefNameInheritanceLite350 here
                

                stub.startassignAttributeDefNameInheritanceLite(
                         assignAttributeDefNameInheritanceLite350,
                    new tempCallbackN66819()
                );
              


        }

        private class tempCallbackN66819  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66819(){ super(null);}

            public void receiveResultassignAttributeDefNameInheritanceLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributeDefNameInheritanceLiteResponse result
                            ) {
                
            }

            public void receiveErrorassignAttributeDefNameInheritanceLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgroupSave() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSave groupSave352=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSave.class);
                    // TODO : Fill in the groupSave352 here
                
                        assertNotNull(stub.groupSave(
                        groupSave352));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupSave() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSave groupSave352=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSave.class);
                    // TODO : Fill in the groupSave352 here
                

                stub.startgroupSave(
                         groupSave352,
                    new tempCallbackN66860()
                );
              


        }

        private class tempCallbackN66860  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66860(){ super(null);}

            public void receiveResultgroupSave(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSaveResponse result
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

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMemberLite hasMemberLite354=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMemberLite.class);
                    // TODO : Fill in the hasMemberLite354 here
                
                        assertNotNull(stub.hasMemberLite(
                        hasMemberLite354));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStarthasMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMemberLite hasMemberLite354=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMemberLite.class);
                    // TODO : Fill in the hasMemberLite354 here
                

                stub.starthasMemberLite(
                         hasMemberLite354,
                    new tempCallbackN66901()
                );
              


        }

        private class tempCallbackN66901  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66901(){ super(null);}

            public void receiveResulthasMemberLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMemberLiteResponse result
                            ) {
                
            }

            public void receiveErrorhasMemberLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgroupDelete() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDelete groupDelete356=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDelete.class);
                    // TODO : Fill in the groupDelete356 here
                
                        assertNotNull(stub.groupDelete(
                        groupDelete356));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupDelete() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDelete groupDelete356=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDelete.class);
                    // TODO : Fill in the groupDelete356 here
                

                stub.startgroupDelete(
                         groupDelete356,
                    new tempCallbackN66942()
                );
              


        }

        private class tempCallbackN66942  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66942(){ super(null);}

            public void receiveResultgroupDelete(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDeleteResponse result
                            ) {
                
            }

            public void receiveErrorgroupDelete(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignAttributes() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributes assignAttributes358=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributes)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributes.class);
                    // TODO : Fill in the assignAttributes358 here
                
                        assertNotNull(stub.assignAttributes(
                        assignAttributes358));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignAttributes() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributes assignAttributes358=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributes)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributes.class);
                    // TODO : Fill in the assignAttributes358 here
                

                stub.startassignAttributes(
                         assignAttributes358,
                    new tempCallbackN66983()
                );
              


        }

        private class tempCallbackN66983  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66983(){ super(null);}

            public void receiveResultassignAttributes(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributesResponse result
                            ) {
                
            }

            public void receiveErrorassignAttributes(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testaddMemberLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMemberLite addMemberLite360=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMemberLite.class);
                    // TODO : Fill in the addMemberLite360 here
                
                        assertNotNull(stub.addMemberLite(
                        addMemberLite360));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartaddMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMemberLite addMemberLite360=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMemberLite.class);
                    // TODO : Fill in the addMemberLite360 here
                

                stub.startaddMemberLite(
                         addMemberLite360,
                    new tempCallbackN67024()
                );
              


        }

        private class tempCallbackN67024  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67024(){ super(null);}

            public void receiveResultaddMemberLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMemberLiteResponse result
                            ) {
                
            }

            public void receiveErroraddMemberLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgroupSaveLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSaveLite groupSaveLite362=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSaveLite.class);
                    // TODO : Fill in the groupSaveLite362 here
                
                        assertNotNull(stub.groupSaveLite(
                        groupSaveLite362));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupSaveLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSaveLite groupSaveLite362=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSaveLite.class);
                    // TODO : Fill in the groupSaveLite362 here
                

                stub.startgroupSaveLite(
                         groupSaveLite362,
                    new tempCallbackN67065()
                );
              


        }

        private class tempCallbackN67065  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67065(){ super(null);}

            public void receiveResultgroupSaveLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSaveLiteResponse result
                            ) {
                
            }

            public void receiveErrorgroupSaveLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testdeleteMember() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMember deleteMember364=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMember.class);
                    // TODO : Fill in the deleteMember364 here
                
                        assertNotNull(stub.deleteMember(
                        deleteMember364));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartdeleteMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMember deleteMember364=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMember.class);
                    // TODO : Fill in the deleteMember364 here
                

                stub.startdeleteMember(
                         deleteMember364,
                    new tempCallbackN67106()
                );
              


        }

        private class tempCallbackN67106  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67106(){ super(null);}

            public void receiveResultdeleteMember(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMemberResponse result
                            ) {
                
            }

            public void receiveErrordeleteMember(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignAttributesLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributesLite assignAttributesLite366=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributesLite.class);
                    // TODO : Fill in the assignAttributesLite366 here
                
                        assertNotNull(stub.assignAttributesLite(
                        assignAttributesLite366));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignAttributesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributesLite assignAttributesLite366=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributesLite.class);
                    // TODO : Fill in the assignAttributesLite366 here
                

                stub.startassignAttributesLite(
                         assignAttributesLite366,
                    new tempCallbackN67147()
                );
              


        }

        private class tempCallbackN67147  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67147(){ super(null);}

            public void receiveResultassignAttributesLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributesLiteResponse result
                            ) {
                
            }

            public void receiveErrorassignAttributesLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testaddMember() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMember addMember368=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMember.class);
                    // TODO : Fill in the addMember368 here
                
                        assertNotNull(stub.addMember(
                        addMember368));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartaddMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMember addMember368=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMember.class);
                    // TODO : Fill in the addMember368 here
                

                stub.startaddMember(
                         addMember368,
                    new tempCallbackN67188()
                );
              


        }

        private class tempCallbackN67188  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67188(){ super(null);}

            public void receiveResultaddMember(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMemberResponse result
                            ) {
                
            }

            public void receiveErroraddMember(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetPermissionAssignmentsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignmentsLite getPermissionAssignmentsLite370=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignmentsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignmentsLite.class);
                    // TODO : Fill in the getPermissionAssignmentsLite370 here
                
                        assertNotNull(stub.getPermissionAssignmentsLite(
                        getPermissionAssignmentsLite370));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetPermissionAssignmentsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignmentsLite getPermissionAssignmentsLite370=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignmentsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignmentsLite.class);
                    // TODO : Fill in the getPermissionAssignmentsLite370 here
                

                stub.startgetPermissionAssignmentsLite(
                         getPermissionAssignmentsLite370,
                    new tempCallbackN67229()
                );
              


        }

        private class tempCallbackN67229  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67229(){ super(null);}

            public void receiveResultgetPermissionAssignmentsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignmentsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetPermissionAssignmentsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgroupDeleteLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDeleteLite groupDeleteLite372=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDeleteLite.class);
                    // TODO : Fill in the groupDeleteLite372 here
                
                        assertNotNull(stub.groupDeleteLite(
                        groupDeleteLite372));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupDeleteLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDeleteLite groupDeleteLite372=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDeleteLite.class);
                    // TODO : Fill in the groupDeleteLite372 here
                

                stub.startgroupDeleteLite(
                         groupDeleteLite372,
                    new tempCallbackN67270()
                );
              


        }

        private class tempCallbackN67270  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67270(){ super(null);}

            public void receiveResultgroupDeleteLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDeleteLiteResponse result
                            ) {
                
            }

            public void receiveErrorgroupDeleteLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testattributeDefNameDeleteLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameDeleteLite attributeDefNameDeleteLite374=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameDeleteLite.class);
                    // TODO : Fill in the attributeDefNameDeleteLite374 here
                
                        assertNotNull(stub.attributeDefNameDeleteLite(
                        attributeDefNameDeleteLite374));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartattributeDefNameDeleteLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameDeleteLite attributeDefNameDeleteLite374=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameDeleteLite.class);
                    // TODO : Fill in the attributeDefNameDeleteLite374 here
                

                stub.startattributeDefNameDeleteLite(
                         attributeDefNameDeleteLite374,
                    new tempCallbackN67311()
                );
              


        }

        private class tempCallbackN67311  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67311(){ super(null);}

            public void receiveResultattributeDefNameDeleteLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameDeleteLiteResponse result
                            ) {
                
            }

            public void receiveErrorattributeDefNameDeleteLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testattributeDefNameDelete() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameDelete attributeDefNameDelete376=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameDelete.class);
                    // TODO : Fill in the attributeDefNameDelete376 here
                
                        assertNotNull(stub.attributeDefNameDelete(
                        attributeDefNameDelete376));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartattributeDefNameDelete() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameDelete attributeDefNameDelete376=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameDelete.class);
                    // TODO : Fill in the attributeDefNameDelete376 here
                

                stub.startattributeDefNameDelete(
                         attributeDefNameDelete376,
                    new tempCallbackN67352()
                );
              


        }

        private class tempCallbackN67352  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67352(){ super(null);}

            public void receiveResultattributeDefNameDelete(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameDeleteResponse result
                            ) {
                
            }

            public void receiveErrorattributeDefNameDelete(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemDeleteLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDeleteLite stemDeleteLite378=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDeleteLite.class);
                    // TODO : Fill in the stemDeleteLite378 here
                
                        assertNotNull(stub.stemDeleteLite(
                        stemDeleteLite378));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemDeleteLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDeleteLite stemDeleteLite378=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDeleteLite.class);
                    // TODO : Fill in the stemDeleteLite378 here
                

                stub.startstemDeleteLite(
                         stemDeleteLite378,
                    new tempCallbackN67393()
                );
              


        }

        private class tempCallbackN67393  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67393(){ super(null);}

            public void receiveResultstemDeleteLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDeleteLiteResponse result
                            ) {
                
            }

            public void receiveErrorstemDeleteLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testdeleteMemberLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMemberLite deleteMemberLite380=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMemberLite.class);
                    // TODO : Fill in the deleteMemberLite380 here
                
                        assertNotNull(stub.deleteMemberLite(
                        deleteMemberLite380));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartdeleteMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMemberLite deleteMemberLite380=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMemberLite.class);
                    // TODO : Fill in the deleteMemberLite380 here
                

                stub.startdeleteMemberLite(
                         deleteMemberLite380,
                    new tempCallbackN67434()
                );
              


        }

        private class tempCallbackN67434  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67434(){ super(null);}

            public void receiveResultdeleteMemberLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMemberLiteResponse result
                            ) {
                
            }

            public void receiveErrordeleteMemberLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemSaveLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSaveLite stemSaveLite382=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSaveLite.class);
                    // TODO : Fill in the stemSaveLite382 here
                
                        assertNotNull(stub.stemSaveLite(
                        stemSaveLite382));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemSaveLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSaveLite stemSaveLite382=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSaveLite.class);
                    // TODO : Fill in the stemSaveLite382 here
                

                stub.startstemSaveLite(
                         stemSaveLite382,
                    new tempCallbackN67475()
                );
              


        }

        private class tempCallbackN67475  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67475(){ super(null);}

            public void receiveResultstemSaveLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSaveLiteResponse result
                            ) {
                
            }

            public void receiveErrorstemSaveLite(java.lang.Exception e) {
                fail();
            }

        }
      
        //Create an ADBBean and provide it as the test object
        public org.apache.axis2.databinding.ADBBean getTestObject(java.lang.Class type) throws java.lang.Exception{
           return (org.apache.axis2.databinding.ADBBean) type.newInstance();
        }

        
        

    }
    