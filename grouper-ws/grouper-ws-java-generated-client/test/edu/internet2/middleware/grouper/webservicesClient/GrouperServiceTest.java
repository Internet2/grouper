

/**
 * GrouperServiceTest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4.1  Built on : Aug 13, 2008 (05:03:35 LKT)
 */
    package edu.internet2.middleware.grouper.webservicesClient;

    /*
     *  GrouperServiceTest Junit test case
    */

    public class GrouperServiceTest extends junit.framework.TestCase{

     
        /**
         * Auto generated test method
         */
        public  void testhasMember() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.HasMember hasMember234=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.HasMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.HasMember.class);
                    // TODO : Fill in the hasMember234 here
                
                        assertNotNull(stub.hasMember(
                        hasMember234));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStarthasMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.HasMember hasMember234=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.HasMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.HasMember.class);
                    // TODO : Fill in the hasMember234 here
                

                stub.starthasMember(
                         hasMember234,
                    new tempCallbackN1000C()
                );
              


        }

        private class tempCallbackN1000C  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1000C(){ super(null);}

            public void receiveResulthasMember(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.HasMemberResponse result
                            ) {
                
            }

            public void receiveErrorhasMember(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemDelete() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.StemDelete stemDelete236=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.StemDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.StemDelete.class);
                    // TODO : Fill in the stemDelete236 here
                
                        assertNotNull(stub.stemDelete(
                        stemDelete236));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemDelete() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.StemDelete stemDelete236=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.StemDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.StemDelete.class);
                    // TODO : Fill in the stemDelete236 here
                

                stub.startstemDelete(
                         stemDelete236,
                    new tempCallbackN10032()
                );
              


        }

        private class tempCallbackN10032  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10032(){ super(null);}

            public void receiveResultstemDelete(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.StemDeleteResponse result
                            ) {
                
            }

            public void receiveErrorstemDelete(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testaddMember() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AddMember addMember238=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AddMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AddMember.class);
                    // TODO : Fill in the addMember238 here
                
                        assertNotNull(stub.addMember(
                        addMember238));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartaddMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AddMember addMember238=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AddMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AddMember.class);
                    // TODO : Fill in the addMember238 here
                

                stub.startaddMember(
                         addMember238,
                    new tempCallbackN10058()
                );
              


        }

        private class tempCallbackN10058  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10058(){ super(null);}

            public void receiveResultaddMember(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AddMemberResponse result
                            ) {
                
            }

            public void receiveErroraddMember(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindGroups() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.FindGroups findGroups240=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.FindGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.FindGroups.class);
                    // TODO : Fill in the findGroups240 here
                
                        assertNotNull(stub.findGroups(
                        findGroups240));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindGroups() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.FindGroups findGroups240=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.FindGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.FindGroups.class);
                    // TODO : Fill in the findGroups240 here
                

                stub.startfindGroups(
                         findGroups240,
                    new tempCallbackN1007E()
                );
              


        }

        private class tempCallbackN1007E  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1007E(){ super(null);}

            public void receiveResultfindGroups(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.FindGroupsResponse result
                            ) {
                
            }

            public void receiveErrorfindGroups(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemSaveLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.StemSaveLite stemSaveLite242=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.StemSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.StemSaveLite.class);
                    // TODO : Fill in the stemSaveLite242 here
                
                        assertNotNull(stub.stemSaveLite(
                        stemSaveLite242));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemSaveLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.StemSaveLite stemSaveLite242=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.StemSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.StemSaveLite.class);
                    // TODO : Fill in the stemSaveLite242 here
                

                stub.startstemSaveLite(
                         stemSaveLite242,
                    new tempCallbackN100A4()
                );
              


        }

        private class tempCallbackN100A4  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN100A4(){ super(null);}

            public void receiveResultstemSaveLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.StemSaveLiteResponse result
                            ) {
                
            }

            public void receiveErrorstemSaveLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgroupSaveLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GroupSaveLite groupSaveLite244=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GroupSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GroupSaveLite.class);
                    // TODO : Fill in the groupSaveLite244 here
                
                        assertNotNull(stub.groupSaveLite(
                        groupSaveLite244));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupSaveLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GroupSaveLite groupSaveLite244=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GroupSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GroupSaveLite.class);
                    // TODO : Fill in the groupSaveLite244 here
                

                stub.startgroupSaveLite(
                         groupSaveLite244,
                    new tempCallbackN100CA()
                );
              


        }

        private class tempCallbackN100CA  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN100CA(){ super(null);}

            public void receiveResultgroupSaveLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GroupSaveLiteResponse result
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

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.DeleteMember deleteMember246=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.DeleteMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.DeleteMember.class);
                    // TODO : Fill in the deleteMember246 here
                
                        assertNotNull(stub.deleteMember(
                        deleteMember246));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartdeleteMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.DeleteMember deleteMember246=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.DeleteMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.DeleteMember.class);
                    // TODO : Fill in the deleteMember246 here
                

                stub.startdeleteMember(
                         deleteMember246,
                    new tempCallbackN100F0()
                );
              


        }

        private class tempCallbackN100F0  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN100F0(){ super(null);}

            public void receiveResultdeleteMember(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.DeleteMemberResponse result
                            ) {
                
            }

            public void receiveErrordeleteMember(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignAttributes() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignAttributes assignAttributes248=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignAttributes)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignAttributes.class);
                    // TODO : Fill in the assignAttributes248 here
                
                        assertNotNull(stub.assignAttributes(
                        assignAttributes248));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignAttributes() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignAttributes assignAttributes248=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignAttributes)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignAttributes.class);
                    // TODO : Fill in the assignAttributes248 here
                

                stub.startassignAttributes(
                         assignAttributes248,
                    new tempCallbackN10116()
                );
              


        }

        private class tempCallbackN10116  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10116(){ super(null);}

            public void receiveResultassignAttributes(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignAttributesResponse result
                            ) {
                
            }

            public void receiveErrorassignAttributes(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemDeleteLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.StemDeleteLite stemDeleteLite250=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.StemDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.StemDeleteLite.class);
                    // TODO : Fill in the stemDeleteLite250 here
                
                        assertNotNull(stub.stemDeleteLite(
                        stemDeleteLite250));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemDeleteLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.StemDeleteLite stemDeleteLite250=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.StemDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.StemDeleteLite.class);
                    // TODO : Fill in the stemDeleteLite250 here
                

                stub.startstemDeleteLite(
                         stemDeleteLite250,
                    new tempCallbackN1013C()
                );
              


        }

        private class tempCallbackN1013C  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1013C(){ super(null);}

            public void receiveResultstemDeleteLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.StemDeleteLiteResponse result
                            ) {
                
            }

            public void receiveErrorstemDeleteLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignPermissions() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignPermissions assignPermissions252=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignPermissions)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignPermissions.class);
                    // TODO : Fill in the assignPermissions252 here
                
                        assertNotNull(stub.assignPermissions(
                        assignPermissions252));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignPermissions() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignPermissions assignPermissions252=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignPermissions)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignPermissions.class);
                    // TODO : Fill in the assignPermissions252 here
                

                stub.startassignPermissions(
                         assignPermissions252,
                    new tempCallbackN10162()
                );
              


        }

        private class tempCallbackN10162  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10162(){ super(null);}

            public void receiveResultassignPermissions(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignPermissionsResponse result
                            ) {
                
            }

            public void receiveErrorassignPermissions(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testhasMemberLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.HasMemberLite hasMemberLite254=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.HasMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.HasMemberLite.class);
                    // TODO : Fill in the hasMemberLite254 here
                
                        assertNotNull(stub.hasMemberLite(
                        hasMemberLite254));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStarthasMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.HasMemberLite hasMemberLite254=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.HasMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.HasMemberLite.class);
                    // TODO : Fill in the hasMemberLite254 here
                

                stub.starthasMemberLite(
                         hasMemberLite254,
                    new tempCallbackN10188()
                );
              


        }

        private class tempCallbackN10188  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10188(){ super(null);}

            public void receiveResulthasMemberLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.HasMemberLiteResponse result
                            ) {
                
            }

            public void receiveErrorhasMemberLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetGrouperPrivilegesLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetGrouperPrivilegesLite getGrouperPrivilegesLite256=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetGrouperPrivilegesLite.class);
                    // TODO : Fill in the getGrouperPrivilegesLite256 here
                
                        assertNotNull(stub.getGrouperPrivilegesLite(
                        getGrouperPrivilegesLite256));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetGrouperPrivilegesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetGrouperPrivilegesLite getGrouperPrivilegesLite256=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetGrouperPrivilegesLite.class);
                    // TODO : Fill in the getGrouperPrivilegesLite256 here
                

                stub.startgetGrouperPrivilegesLite(
                         getGrouperPrivilegesLite256,
                    new tempCallbackN101AE()
                );
              


        }

        private class tempCallbackN101AE  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN101AE(){ super(null);}

            public void receiveResultgetGrouperPrivilegesLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetGrouperPrivilegesLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetGrouperPrivilegesLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgroupDeleteLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GroupDeleteLite groupDeleteLite258=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GroupDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GroupDeleteLite.class);
                    // TODO : Fill in the groupDeleteLite258 here
                
                        assertNotNull(stub.groupDeleteLite(
                        groupDeleteLite258));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupDeleteLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GroupDeleteLite groupDeleteLite258=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GroupDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GroupDeleteLite.class);
                    // TODO : Fill in the groupDeleteLite258 here
                

                stub.startgroupDeleteLite(
                         groupDeleteLite258,
                    new tempCallbackN101D4()
                );
              


        }

        private class tempCallbackN101D4  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN101D4(){ super(null);}

            public void receiveResultgroupDeleteLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GroupDeleteLiteResponse result
                            ) {
                
            }

            public void receiveErrorgroupDeleteLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgroupSave() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GroupSave groupSave260=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GroupSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GroupSave.class);
                    // TODO : Fill in the groupSave260 here
                
                        assertNotNull(stub.groupSave(
                        groupSave260));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupSave() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GroupSave groupSave260=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GroupSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GroupSave.class);
                    // TODO : Fill in the groupSave260 here
                

                stub.startgroupSave(
                         groupSave260,
                    new tempCallbackN101FA()
                );
              


        }

        private class tempCallbackN101FA  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN101FA(){ super(null);}

            public void receiveResultgroupSave(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GroupSaveResponse result
                            ) {
                
            }

            public void receiveErrorgroupSave(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetAttributeAssignments() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetAttributeAssignments getAttributeAssignments262=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetAttributeAssignments)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetAttributeAssignments.class);
                    // TODO : Fill in the getAttributeAssignments262 here
                
                        assertNotNull(stub.getAttributeAssignments(
                        getAttributeAssignments262));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetAttributeAssignments() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetAttributeAssignments getAttributeAssignments262=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetAttributeAssignments)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetAttributeAssignments.class);
                    // TODO : Fill in the getAttributeAssignments262 here
                

                stub.startgetAttributeAssignments(
                         getAttributeAssignments262,
                    new tempCallbackN10220()
                );
              


        }

        private class tempCallbackN10220  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10220(){ super(null);}

            public void receiveResultgetAttributeAssignments(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetAttributeAssignmentsResponse result
                            ) {
                
            }

            public void receiveErrorgetAttributeAssignments(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testaddMemberLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AddMemberLite addMemberLite264=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AddMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AddMemberLite.class);
                    // TODO : Fill in the addMemberLite264 here
                
                        assertNotNull(stub.addMemberLite(
                        addMemberLite264));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartaddMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AddMemberLite addMemberLite264=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AddMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AddMemberLite.class);
                    // TODO : Fill in the addMemberLite264 here
                

                stub.startaddMemberLite(
                         addMemberLite264,
                    new tempCallbackN10246()
                );
              


        }

        private class tempCallbackN10246  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10246(){ super(null);}

            public void receiveResultaddMemberLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AddMemberLiteResponse result
                            ) {
                
            }

            public void receiveErroraddMemberLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetPermissionAssignmentsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetPermissionAssignmentsLite getPermissionAssignmentsLite266=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetPermissionAssignmentsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetPermissionAssignmentsLite.class);
                    // TODO : Fill in the getPermissionAssignmentsLite266 here
                
                        assertNotNull(stub.getPermissionAssignmentsLite(
                        getPermissionAssignmentsLite266));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetPermissionAssignmentsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetPermissionAssignmentsLite getPermissionAssignmentsLite266=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetPermissionAssignmentsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetPermissionAssignmentsLite.class);
                    // TODO : Fill in the getPermissionAssignmentsLite266 here
                

                stub.startgetPermissionAssignmentsLite(
                         getPermissionAssignmentsLite266,
                    new tempCallbackN1026C()
                );
              


        }

        private class tempCallbackN1026C  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1026C(){ super(null);}

            public void receiveResultgetPermissionAssignmentsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetPermissionAssignmentsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetPermissionAssignmentsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMembershipsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetMembershipsLite getMembershipsLite268=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetMembershipsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetMembershipsLite.class);
                    // TODO : Fill in the getMembershipsLite268 here
                
                        assertNotNull(stub.getMembershipsLite(
                        getMembershipsLite268));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMembershipsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetMembershipsLite getMembershipsLite268=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetMembershipsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetMembershipsLite.class);
                    // TODO : Fill in the getMembershipsLite268 here
                

                stub.startgetMembershipsLite(
                         getMembershipsLite268,
                    new tempCallbackN10292()
                );
              


        }

        private class tempCallbackN10292  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10292(){ super(null);}

            public void receiveResultgetMembershipsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetMembershipsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetMembershipsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignPermissionsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignPermissionsLite assignPermissionsLite270=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignPermissionsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignPermissionsLite.class);
                    // TODO : Fill in the assignPermissionsLite270 here
                
                        assertNotNull(stub.assignPermissionsLite(
                        assignPermissionsLite270));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignPermissionsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignPermissionsLite assignPermissionsLite270=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignPermissionsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignPermissionsLite.class);
                    // TODO : Fill in the assignPermissionsLite270 here
                

                stub.startassignPermissionsLite(
                         assignPermissionsLite270,
                    new tempCallbackN102B8()
                );
              


        }

        private class tempCallbackN102B8  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN102B8(){ super(null);}

            public void receiveResultassignPermissionsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignPermissionsLiteResponse result
                            ) {
                
            }

            public void receiveErrorassignPermissionsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMembers() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetMembers getMembers272=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetMembers)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetMembers.class);
                    // TODO : Fill in the getMembers272 here
                
                        assertNotNull(stub.getMembers(
                        getMembers272));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMembers() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetMembers getMembers272=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetMembers)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetMembers.class);
                    // TODO : Fill in the getMembers272 here
                

                stub.startgetMembers(
                         getMembers272,
                    new tempCallbackN102DE()
                );
              


        }

        private class tempCallbackN102DE  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN102DE(){ super(null);}

            public void receiveResultgetMembers(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetMembersResponse result
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

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.MemberChangeSubjectLite memberChangeSubjectLite274=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.MemberChangeSubjectLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.MemberChangeSubjectLite.class);
                    // TODO : Fill in the memberChangeSubjectLite274 here
                
                        assertNotNull(stub.memberChangeSubjectLite(
                        memberChangeSubjectLite274));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartmemberChangeSubjectLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.MemberChangeSubjectLite memberChangeSubjectLite274=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.MemberChangeSubjectLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.MemberChangeSubjectLite.class);
                    // TODO : Fill in the memberChangeSubjectLite274 here
                

                stub.startmemberChangeSubjectLite(
                         memberChangeSubjectLite274,
                    new tempCallbackN10304()
                );
              


        }

        private class tempCallbackN10304  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10304(){ super(null);}

            public void receiveResultmemberChangeSubjectLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.MemberChangeSubjectLiteResponse result
                            ) {
                
            }

            public void receiveErrormemberChangeSubjectLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testdeleteMemberLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.DeleteMemberLite deleteMemberLite276=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.DeleteMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.DeleteMemberLite.class);
                    // TODO : Fill in the deleteMemberLite276 here
                
                        assertNotNull(stub.deleteMemberLite(
                        deleteMemberLite276));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartdeleteMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.DeleteMemberLite deleteMemberLite276=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.DeleteMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.DeleteMemberLite.class);
                    // TODO : Fill in the deleteMemberLite276 here
                

                stub.startdeleteMemberLite(
                         deleteMemberLite276,
                    new tempCallbackN1032A()
                );
              


        }

        private class tempCallbackN1032A  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1032A(){ super(null);}

            public void receiveResultdeleteMemberLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.DeleteMemberLiteResponse result
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

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.StemSave stemSave278=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.StemSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.StemSave.class);
                    // TODO : Fill in the stemSave278 here
                
                        assertNotNull(stub.stemSave(
                        stemSave278));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemSave() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.StemSave stemSave278=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.StemSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.StemSave.class);
                    // TODO : Fill in the stemSave278 here
                

                stub.startstemSave(
                         stemSave278,
                    new tempCallbackN10350()
                );
              


        }

        private class tempCallbackN10350  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10350(){ super(null);}

            public void receiveResultstemSave(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.StemSaveResponse result
                            ) {
                
            }

            public void receiveErrorstemSave(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindStemsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.FindStemsLite findStemsLite280=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.FindStemsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.FindStemsLite.class);
                    // TODO : Fill in the findStemsLite280 here
                
                        assertNotNull(stub.findStemsLite(
                        findStemsLite280));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindStemsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.FindStemsLite findStemsLite280=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.FindStemsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.FindStemsLite.class);
                    // TODO : Fill in the findStemsLite280 here
                

                stub.startfindStemsLite(
                         findStemsLite280,
                    new tempCallbackN10376()
                );
              


        }

        private class tempCallbackN10376  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10376(){ super(null);}

            public void receiveResultfindStemsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.FindStemsLiteResponse result
                            ) {
                
            }

            public void receiveErrorfindStemsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testmemberChangeSubject() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.MemberChangeSubject memberChangeSubject282=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.MemberChangeSubject)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.MemberChangeSubject.class);
                    // TODO : Fill in the memberChangeSubject282 here
                
                        assertNotNull(stub.memberChangeSubject(
                        memberChangeSubject282));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartmemberChangeSubject() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.MemberChangeSubject memberChangeSubject282=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.MemberChangeSubject)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.MemberChangeSubject.class);
                    // TODO : Fill in the memberChangeSubject282 here
                

                stub.startmemberChangeSubject(
                         memberChangeSubject282,
                    new tempCallbackN1039C()
                );
              


        }

        private class tempCallbackN1039C  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1039C(){ super(null);}

            public void receiveResultmemberChangeSubject(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.MemberChangeSubjectResponse result
                            ) {
                
            }

            public void receiveErrormemberChangeSubject(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMemberships() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetMemberships getMemberships284=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetMemberships)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetMemberships.class);
                    // TODO : Fill in the getMemberships284 here
                
                        assertNotNull(stub.getMemberships(
                        getMemberships284));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMemberships() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetMemberships getMemberships284=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetMemberships)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetMemberships.class);
                    // TODO : Fill in the getMemberships284 here
                

                stub.startgetMemberships(
                         getMemberships284,
                    new tempCallbackN103C2()
                );
              


        }

        private class tempCallbackN103C2  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN103C2(){ super(null);}

            public void receiveResultgetMemberships(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetMembershipsResponse result
                            ) {
                
            }

            public void receiveErrorgetMemberships(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMembersLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetMembersLite getMembersLite286=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetMembersLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetMembersLite.class);
                    // TODO : Fill in the getMembersLite286 here
                
                        assertNotNull(stub.getMembersLite(
                        getMembersLite286));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMembersLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetMembersLite getMembersLite286=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetMembersLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetMembersLite.class);
                    // TODO : Fill in the getMembersLite286 here
                

                stub.startgetMembersLite(
                         getMembersLite286,
                    new tempCallbackN103E8()
                );
              


        }

        private class tempCallbackN103E8  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN103E8(){ super(null);}

            public void receiveResultgetMembersLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetMembersLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetMembersLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetPermissionAssignments() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetPermissionAssignments getPermissionAssignments288=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetPermissionAssignments)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetPermissionAssignments.class);
                    // TODO : Fill in the getPermissionAssignments288 here
                
                        assertNotNull(stub.getPermissionAssignments(
                        getPermissionAssignments288));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetPermissionAssignments() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetPermissionAssignments getPermissionAssignments288=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetPermissionAssignments)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetPermissionAssignments.class);
                    // TODO : Fill in the getPermissionAssignments288 here
                

                stub.startgetPermissionAssignments(
                         getPermissionAssignments288,
                    new tempCallbackN1040E()
                );
              


        }

        private class tempCallbackN1040E  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1040E(){ super(null);}

            public void receiveResultgetPermissionAssignments(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetPermissionAssignmentsResponse result
                            ) {
                
            }

            public void receiveErrorgetPermissionAssignments(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignGrouperPrivilegesLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignGrouperPrivilegesLite assignGrouperPrivilegesLite290=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignGrouperPrivilegesLite.class);
                    // TODO : Fill in the assignGrouperPrivilegesLite290 here
                
                        assertNotNull(stub.assignGrouperPrivilegesLite(
                        assignGrouperPrivilegesLite290));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignGrouperPrivilegesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignGrouperPrivilegesLite assignGrouperPrivilegesLite290=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignGrouperPrivilegesLite.class);
                    // TODO : Fill in the assignGrouperPrivilegesLite290 here
                

                stub.startassignGrouperPrivilegesLite(
                         assignGrouperPrivilegesLite290,
                    new tempCallbackN10434()
                );
              


        }

        private class tempCallbackN10434  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10434(){ super(null);}

            public void receiveResultassignGrouperPrivilegesLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignGrouperPrivilegesLiteResponse result
                            ) {
                
            }

            public void receiveErrorassignGrouperPrivilegesLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignAttributesLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignAttributesLite assignAttributesLite292=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignAttributesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignAttributesLite.class);
                    // TODO : Fill in the assignAttributesLite292 here
                
                        assertNotNull(stub.assignAttributesLite(
                        assignAttributesLite292));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignAttributesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignAttributesLite assignAttributesLite292=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignAttributesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignAttributesLite.class);
                    // TODO : Fill in the assignAttributesLite292 here
                

                stub.startassignAttributesLite(
                         assignAttributesLite292,
                    new tempCallbackN1045A()
                );
              


        }

        private class tempCallbackN1045A  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1045A(){ super(null);}

            public void receiveResultassignAttributesLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignAttributesLiteResponse result
                            ) {
                
            }

            public void receiveErrorassignAttributesLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignGrouperPrivileges() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignGrouperPrivileges assignGrouperPrivileges294=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignGrouperPrivileges)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignGrouperPrivileges.class);
                    // TODO : Fill in the assignGrouperPrivileges294 here
                
                        assertNotNull(stub.assignGrouperPrivileges(
                        assignGrouperPrivileges294));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignGrouperPrivileges() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignGrouperPrivileges assignGrouperPrivileges294=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignGrouperPrivileges)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignGrouperPrivileges.class);
                    // TODO : Fill in the assignGrouperPrivileges294 here
                

                stub.startassignGrouperPrivileges(
                         assignGrouperPrivileges294,
                    new tempCallbackN10480()
                );
              


        }

        private class tempCallbackN10480  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10480(){ super(null);}

            public void receiveResultassignGrouperPrivileges(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignGrouperPrivilegesResponse result
                            ) {
                
            }

            public void receiveErrorassignGrouperPrivileges(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetGroupsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetGroupsLite getGroupsLite296=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetGroupsLite.class);
                    // TODO : Fill in the getGroupsLite296 here
                
                        assertNotNull(stub.getGroupsLite(
                        getGroupsLite296));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetGroupsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetGroupsLite getGroupsLite296=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetGroupsLite.class);
                    // TODO : Fill in the getGroupsLite296 here
                

                stub.startgetGroupsLite(
                         getGroupsLite296,
                    new tempCallbackN104A6()
                );
              


        }

        private class tempCallbackN104A6  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN104A6(){ super(null);}

            public void receiveResultgetGroupsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetGroupsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetGroupsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetSubjectsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetSubjectsLite getSubjectsLite298=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetSubjectsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetSubjectsLite.class);
                    // TODO : Fill in the getSubjectsLite298 here
                
                        assertNotNull(stub.getSubjectsLite(
                        getSubjectsLite298));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetSubjectsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetSubjectsLite getSubjectsLite298=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetSubjectsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetSubjectsLite.class);
                    // TODO : Fill in the getSubjectsLite298 here
                

                stub.startgetSubjectsLite(
                         getSubjectsLite298,
                    new tempCallbackN104CC()
                );
              


        }

        private class tempCallbackN104CC  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN104CC(){ super(null);}

            public void receiveResultgetSubjectsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetSubjectsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetSubjectsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetSubjects() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetSubjects getSubjects300=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetSubjects)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetSubjects.class);
                    // TODO : Fill in the getSubjects300 here
                
                        assertNotNull(stub.getSubjects(
                        getSubjects300));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetSubjects() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetSubjects getSubjects300=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetSubjects)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetSubjects.class);
                    // TODO : Fill in the getSubjects300 here
                

                stub.startgetSubjects(
                         getSubjects300,
                    new tempCallbackN104F2()
                );
              


        }

        private class tempCallbackN104F2  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN104F2(){ super(null);}

            public void receiveResultgetSubjects(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetSubjectsResponse result
                            ) {
                
            }

            public void receiveErrorgetSubjects(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindGroupsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.FindGroupsLite findGroupsLite302=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.FindGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.FindGroupsLite.class);
                    // TODO : Fill in the findGroupsLite302 here
                
                        assertNotNull(stub.findGroupsLite(
                        findGroupsLite302));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindGroupsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.FindGroupsLite findGroupsLite302=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.FindGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.FindGroupsLite.class);
                    // TODO : Fill in the findGroupsLite302 here
                

                stub.startfindGroupsLite(
                         findGroupsLite302,
                    new tempCallbackN10518()
                );
              


        }

        private class tempCallbackN10518  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10518(){ super(null);}

            public void receiveResultfindGroupsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.FindGroupsLiteResponse result
                            ) {
                
            }

            public void receiveErrorfindGroupsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgroupDelete() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GroupDelete groupDelete304=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GroupDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GroupDelete.class);
                    // TODO : Fill in the groupDelete304 here
                
                        assertNotNull(stub.groupDelete(
                        groupDelete304));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupDelete() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GroupDelete groupDelete304=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GroupDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GroupDelete.class);
                    // TODO : Fill in the groupDelete304 here
                

                stub.startgroupDelete(
                         groupDelete304,
                    new tempCallbackN1053E()
                );
              


        }

        private class tempCallbackN1053E  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1053E(){ super(null);}

            public void receiveResultgroupDelete(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GroupDeleteResponse result
                            ) {
                
            }

            public void receiveErrorgroupDelete(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetAttributeAssignmentsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetAttributeAssignmentsLite getAttributeAssignmentsLite306=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetAttributeAssignmentsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetAttributeAssignmentsLite.class);
                    // TODO : Fill in the getAttributeAssignmentsLite306 here
                
                        assertNotNull(stub.getAttributeAssignmentsLite(
                        getAttributeAssignmentsLite306));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetAttributeAssignmentsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetAttributeAssignmentsLite getAttributeAssignmentsLite306=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetAttributeAssignmentsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetAttributeAssignmentsLite.class);
                    // TODO : Fill in the getAttributeAssignmentsLite306 here
                

                stub.startgetAttributeAssignmentsLite(
                         getAttributeAssignmentsLite306,
                    new tempCallbackN10564()
                );
              


        }

        private class tempCallbackN10564  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10564(){ super(null);}

            public void receiveResultgetAttributeAssignmentsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetAttributeAssignmentsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetAttributeAssignmentsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindStems() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.FindStems findStems308=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.FindStems)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.FindStems.class);
                    // TODO : Fill in the findStems308 here
                
                        assertNotNull(stub.findStems(
                        findStems308));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindStems() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.FindStems findStems308=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.FindStems)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.FindStems.class);
                    // TODO : Fill in the findStems308 here
                

                stub.startfindStems(
                         findStems308,
                    new tempCallbackN1058A()
                );
              


        }

        private class tempCallbackN1058A  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1058A(){ super(null);}

            public void receiveResultfindStems(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.FindStemsResponse result
                            ) {
                
            }

            public void receiveErrorfindStems(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetGroups() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetGroups getGroups310=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetGroups.class);
                    // TODO : Fill in the getGroups310 here
                
                        assertNotNull(stub.getGroups(
                        getGroups310));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetGroups() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetGroups getGroups310=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetGroups.class);
                    // TODO : Fill in the getGroups310 here
                

                stub.startgetGroups(
                         getGroups310,
                    new tempCallbackN105B0()
                );
              


        }

        private class tempCallbackN105B0  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN105B0(){ super(null);}

            public void receiveResultgetGroups(
                         edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetGroupsResponse result
                            ) {
                
            }

            public void receiveErrorgetGroups(java.lang.Exception e) {
                fail();
            }

        }
      
        //Create an ADBBean and provide it as the test object
        public org.apache.axis2.databinding.ADBBean getTestObject(java.lang.Class type) throws java.lang.Exception{
           return (org.apache.axis2.databinding.ADBBean) type.newInstance();
        }

        
        

    }
    