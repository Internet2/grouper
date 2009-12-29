

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
        public  void testgroupDeleteLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.GroupDeleteLite groupDeleteLite180=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GroupDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GroupDeleteLite.class);
                    // TODO : Fill in the groupDeleteLite180 here
                
                        assertNotNull(stub.groupDeleteLite(
                        groupDeleteLite180));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupDeleteLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GroupDeleteLite groupDeleteLite180=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GroupDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GroupDeleteLite.class);
                    // TODO : Fill in the groupDeleteLite180 here
                

                stub.startgroupDeleteLite(
                         groupDeleteLite180,
                    new tempCallbackN1000C()
                );
              


        }

        private class tempCallbackN1000C  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1000C(){ super(null);}

            public void receiveResultgroupDeleteLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.GroupDeleteLiteResponse result
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

           edu.internet2.middleware.grouper.ws.soap.xsd.GroupSave groupSave182=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GroupSave)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GroupSave.class);
                    // TODO : Fill in the groupSave182 here
                
                        assertNotNull(stub.groupSave(
                        groupSave182));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupSave() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GroupSave groupSave182=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GroupSave)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GroupSave.class);
                    // TODO : Fill in the groupSave182 here
                

                stub.startgroupSave(
                         groupSave182,
                    new tempCallbackN10032()
                );
              


        }

        private class tempCallbackN10032  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10032(){ super(null);}

            public void receiveResultgroupSave(
                         edu.internet2.middleware.grouper.ws.soap.xsd.GroupSaveResponse result
                            ) {
                
            }

            public void receiveErrorgroupSave(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testaddMemberLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.AddMemberLite addMemberLite184=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.AddMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.AddMemberLite.class);
                    // TODO : Fill in the addMemberLite184 here
                
                        assertNotNull(stub.addMemberLite(
                        addMemberLite184));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartaddMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.AddMemberLite addMemberLite184=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.AddMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.AddMemberLite.class);
                    // TODO : Fill in the addMemberLite184 here
                

                stub.startaddMemberLite(
                         addMemberLite184,
                    new tempCallbackN10058()
                );
              


        }

        private class tempCallbackN10058  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10058(){ super(null);}

            public void receiveResultaddMemberLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.AddMemberLiteResponse result
                            ) {
                
            }

            public void receiveErroraddMemberLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMembershipsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.GetMembershipsLite getMembershipsLite186=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetMembershipsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetMembershipsLite.class);
                    // TODO : Fill in the getMembershipsLite186 here
                
                        assertNotNull(stub.getMembershipsLite(
                        getMembershipsLite186));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMembershipsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GetMembershipsLite getMembershipsLite186=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetMembershipsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetMembershipsLite.class);
                    // TODO : Fill in the getMembershipsLite186 here
                

                stub.startgetMembershipsLite(
                         getMembershipsLite186,
                    new tempCallbackN1007E()
                );
              


        }

        private class tempCallbackN1007E  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1007E(){ super(null);}

            public void receiveResultgetMembershipsLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.GetMembershipsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetMembershipsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testhasMember() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.HasMember hasMember188=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.HasMember)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.HasMember.class);
                    // TODO : Fill in the hasMember188 here
                
                        assertNotNull(stub.hasMember(
                        hasMember188));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStarthasMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.HasMember hasMember188=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.HasMember)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.HasMember.class);
                    // TODO : Fill in the hasMember188 here
                

                stub.starthasMember(
                         hasMember188,
                    new tempCallbackN100A4()
                );
              


        }

        private class tempCallbackN100A4  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN100A4(){ super(null);}

            public void receiveResulthasMember(
                         edu.internet2.middleware.grouper.ws.soap.xsd.HasMemberResponse result
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

           edu.internet2.middleware.grouper.ws.soap.xsd.StemDelete stemDelete190=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.StemDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.StemDelete.class);
                    // TODO : Fill in the stemDelete190 here
                
                        assertNotNull(stub.stemDelete(
                        stemDelete190));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemDelete() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.StemDelete stemDelete190=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.StemDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.StemDelete.class);
                    // TODO : Fill in the stemDelete190 here
                

                stub.startstemDelete(
                         stemDelete190,
                    new tempCallbackN100CA()
                );
              


        }

        private class tempCallbackN100CA  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN100CA(){ super(null);}

            public void receiveResultstemDelete(
                         edu.internet2.middleware.grouper.ws.soap.xsd.StemDeleteResponse result
                            ) {
                
            }

            public void receiveErrorstemDelete(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemSaveLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.StemSaveLite stemSaveLite192=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.StemSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.StemSaveLite.class);
                    // TODO : Fill in the stemSaveLite192 here
                
                        assertNotNull(stub.stemSaveLite(
                        stemSaveLite192));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemSaveLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.StemSaveLite stemSaveLite192=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.StemSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.StemSaveLite.class);
                    // TODO : Fill in the stemSaveLite192 here
                

                stub.startstemSaveLite(
                         stemSaveLite192,
                    new tempCallbackN100F0()
                );
              


        }

        private class tempCallbackN100F0  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN100F0(){ super(null);}

            public void receiveResultstemSaveLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.StemSaveLiteResponse result
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

           edu.internet2.middleware.grouper.ws.soap.xsd.GroupSaveLite groupSaveLite194=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GroupSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GroupSaveLite.class);
                    // TODO : Fill in the groupSaveLite194 here
                
                        assertNotNull(stub.groupSaveLite(
                        groupSaveLite194));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupSaveLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GroupSaveLite groupSaveLite194=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GroupSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GroupSaveLite.class);
                    // TODO : Fill in the groupSaveLite194 here
                

                stub.startgroupSaveLite(
                         groupSaveLite194,
                    new tempCallbackN10116()
                );
              


        }

        private class tempCallbackN10116  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10116(){ super(null);}

            public void receiveResultgroupSaveLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.GroupSaveLiteResponse result
                            ) {
                
            }

            public void receiveErrorgroupSaveLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testaddMember() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.AddMember addMember196=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.AddMember)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.AddMember.class);
                    // TODO : Fill in the addMember196 here
                
                        assertNotNull(stub.addMember(
                        addMember196));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartaddMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.AddMember addMember196=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.AddMember)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.AddMember.class);
                    // TODO : Fill in the addMember196 here
                

                stub.startaddMember(
                         addMember196,
                    new tempCallbackN1013C()
                );
              


        }

        private class tempCallbackN1013C  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1013C(){ super(null);}

            public void receiveResultaddMember(
                         edu.internet2.middleware.grouper.ws.soap.xsd.AddMemberResponse result
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

           edu.internet2.middleware.grouper.ws.soap.xsd.FindGroups findGroups198=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.FindGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.FindGroups.class);
                    // TODO : Fill in the findGroups198 here
                
                        assertNotNull(stub.findGroups(
                        findGroups198));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindGroups() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.FindGroups findGroups198=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.FindGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.FindGroups.class);
                    // TODO : Fill in the findGroups198 here
                

                stub.startfindGroups(
                         findGroups198,
                    new tempCallbackN10162()
                );
              


        }

        private class tempCallbackN10162  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10162(){ super(null);}

            public void receiveResultfindGroups(
                         edu.internet2.middleware.grouper.ws.soap.xsd.FindGroupsResponse result
                            ) {
                
            }

            public void receiveErrorfindGroups(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testdeleteMember() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMember deleteMember200=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMember)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMember.class);
                    // TODO : Fill in the deleteMember200 here
                
                        assertNotNull(stub.deleteMember(
                        deleteMember200));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartdeleteMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMember deleteMember200=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMember)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMember.class);
                    // TODO : Fill in the deleteMember200 here
                

                stub.startdeleteMember(
                         deleteMember200,
                    new tempCallbackN10188()
                );
              


        }

        private class tempCallbackN10188  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10188(){ super(null);}

            public void receiveResultdeleteMember(
                         edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMemberResponse result
                            ) {
                
            }

            public void receiveErrordeleteMember(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testhasMemberLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.HasMemberLite hasMemberLite202=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.HasMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.HasMemberLite.class);
                    // TODO : Fill in the hasMemberLite202 here
                
                        assertNotNull(stub.hasMemberLite(
                        hasMemberLite202));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStarthasMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.HasMemberLite hasMemberLite202=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.HasMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.HasMemberLite.class);
                    // TODO : Fill in the hasMemberLite202 here
                

                stub.starthasMemberLite(
                         hasMemberLite202,
                    new tempCallbackN101AE()
                );
              


        }

        private class tempCallbackN101AE  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN101AE(){ super(null);}

            public void receiveResulthasMemberLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.HasMemberLiteResponse result
                            ) {
                
            }

            public void receiveErrorhasMemberLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemDeleteLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.StemDeleteLite stemDeleteLite204=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.StemDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.StemDeleteLite.class);
                    // TODO : Fill in the stemDeleteLite204 here
                
                        assertNotNull(stub.stemDeleteLite(
                        stemDeleteLite204));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemDeleteLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.StemDeleteLite stemDeleteLite204=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.StemDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.StemDeleteLite.class);
                    // TODO : Fill in the stemDeleteLite204 here
                

                stub.startstemDeleteLite(
                         stemDeleteLite204,
                    new tempCallbackN101D4()
                );
              


        }

        private class tempCallbackN101D4  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN101D4(){ super(null);}

            public void receiveResultstemDeleteLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.StemDeleteLiteResponse result
                            ) {
                
            }

            public void receiveErrorstemDeleteLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetGrouperPrivilegesLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.GetGrouperPrivilegesLite getGrouperPrivilegesLite206=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetGrouperPrivilegesLite.class);
                    // TODO : Fill in the getGrouperPrivilegesLite206 here
                
                        assertNotNull(stub.getGrouperPrivilegesLite(
                        getGrouperPrivilegesLite206));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetGrouperPrivilegesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GetGrouperPrivilegesLite getGrouperPrivilegesLite206=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetGrouperPrivilegesLite.class);
                    // TODO : Fill in the getGrouperPrivilegesLite206 here
                

                stub.startgetGrouperPrivilegesLite(
                         getGrouperPrivilegesLite206,
                    new tempCallbackN101FA()
                );
              


        }

        private class tempCallbackN101FA  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN101FA(){ super(null);}

            public void receiveResultgetGrouperPrivilegesLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.GetGrouperPrivilegesLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetGrouperPrivilegesLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMembersLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.GetMembersLite getMembersLite208=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetMembersLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetMembersLite.class);
                    // TODO : Fill in the getMembersLite208 here
                
                        assertNotNull(stub.getMembersLite(
                        getMembersLite208));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMembersLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GetMembersLite getMembersLite208=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetMembersLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetMembersLite.class);
                    // TODO : Fill in the getMembersLite208 here
                

                stub.startgetMembersLite(
                         getMembersLite208,
                    new tempCallbackN10220()
                );
              


        }

        private class tempCallbackN10220  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10220(){ super(null);}

            public void receiveResultgetMembersLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.GetMembersLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetMembersLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMemberships() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.GetMemberships getMemberships210=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetMemberships)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetMemberships.class);
                    // TODO : Fill in the getMemberships210 here
                
                        assertNotNull(stub.getMemberships(
                        getMemberships210));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMemberships() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GetMemberships getMemberships210=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetMemberships)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetMemberships.class);
                    // TODO : Fill in the getMemberships210 here
                

                stub.startgetMemberships(
                         getMemberships210,
                    new tempCallbackN10246()
                );
              


        }

        private class tempCallbackN10246  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10246(){ super(null);}

            public void receiveResultgetMemberships(
                         edu.internet2.middleware.grouper.ws.soap.xsd.GetMembershipsResponse result
                            ) {
                
            }

            public void receiveErrorgetMemberships(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetGroupsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.GetGroupsLite getGroupsLite212=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetGroupsLite.class);
                    // TODO : Fill in the getGroupsLite212 here
                
                        assertNotNull(stub.getGroupsLite(
                        getGroupsLite212));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetGroupsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GetGroupsLite getGroupsLite212=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetGroupsLite.class);
                    // TODO : Fill in the getGroupsLite212 here
                

                stub.startgetGroupsLite(
                         getGroupsLite212,
                    new tempCallbackN1026C()
                );
              


        }

        private class tempCallbackN1026C  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1026C(){ super(null);}

            public void receiveResultgetGroupsLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.GetGroupsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetGroupsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignGrouperPrivilegesLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.AssignGrouperPrivilegesLite assignGrouperPrivilegesLite214=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.AssignGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.AssignGrouperPrivilegesLite.class);
                    // TODO : Fill in the assignGrouperPrivilegesLite214 here
                
                        assertNotNull(stub.assignGrouperPrivilegesLite(
                        assignGrouperPrivilegesLite214));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignGrouperPrivilegesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.AssignGrouperPrivilegesLite assignGrouperPrivilegesLite214=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.AssignGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.AssignGrouperPrivilegesLite.class);
                    // TODO : Fill in the assignGrouperPrivilegesLite214 here
                

                stub.startassignGrouperPrivilegesLite(
                         assignGrouperPrivilegesLite214,
                    new tempCallbackN10292()
                );
              


        }

        private class tempCallbackN10292  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10292(){ super(null);}

            public void receiveResultassignGrouperPrivilegesLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.AssignGrouperPrivilegesLiteResponse result
                            ) {
                
            }

            public void receiveErrorassignGrouperPrivilegesLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetSubjectsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.GetSubjectsLite getSubjectsLite216=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetSubjectsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetSubjectsLite.class);
                    // TODO : Fill in the getSubjectsLite216 here
                
                        assertNotNull(stub.getSubjectsLite(
                        getSubjectsLite216));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetSubjectsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GetSubjectsLite getSubjectsLite216=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetSubjectsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetSubjectsLite.class);
                    // TODO : Fill in the getSubjectsLite216 here
                

                stub.startgetSubjectsLite(
                         getSubjectsLite216,
                    new tempCallbackN102B8()
                );
              


        }

        private class tempCallbackN102B8  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN102B8(){ super(null);}

            public void receiveResultgetSubjectsLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.GetSubjectsLiteResponse result
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

           edu.internet2.middleware.grouper.ws.soap.xsd.GetSubjects getSubjects218=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetSubjects)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetSubjects.class);
                    // TODO : Fill in the getSubjects218 here
                
                        assertNotNull(stub.getSubjects(
                        getSubjects218));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetSubjects() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GetSubjects getSubjects218=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetSubjects)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetSubjects.class);
                    // TODO : Fill in the getSubjects218 here
                

                stub.startgetSubjects(
                         getSubjects218,
                    new tempCallbackN102DE()
                );
              


        }

        private class tempCallbackN102DE  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN102DE(){ super(null);}

            public void receiveResultgetSubjects(
                         edu.internet2.middleware.grouper.ws.soap.xsd.GetSubjectsResponse result
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

           edu.internet2.middleware.grouper.ws.soap.xsd.FindGroupsLite findGroupsLite220=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.FindGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.FindGroupsLite.class);
                    // TODO : Fill in the findGroupsLite220 here
                
                        assertNotNull(stub.findGroupsLite(
                        findGroupsLite220));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindGroupsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.FindGroupsLite findGroupsLite220=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.FindGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.FindGroupsLite.class);
                    // TODO : Fill in the findGroupsLite220 here
                

                stub.startfindGroupsLite(
                         findGroupsLite220,
                    new tempCallbackN10304()
                );
              


        }

        private class tempCallbackN10304  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10304(){ super(null);}

            public void receiveResultfindGroupsLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.FindGroupsLiteResponse result
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

           edu.internet2.middleware.grouper.ws.soap.xsd.GroupDelete groupDelete222=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GroupDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GroupDelete.class);
                    // TODO : Fill in the groupDelete222 here
                
                        assertNotNull(stub.groupDelete(
                        groupDelete222));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupDelete() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GroupDelete groupDelete222=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GroupDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GroupDelete.class);
                    // TODO : Fill in the groupDelete222 here
                

                stub.startgroupDelete(
                         groupDelete222,
                    new tempCallbackN1032A()
                );
              


        }

        private class tempCallbackN1032A  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1032A(){ super(null);}

            public void receiveResultgroupDelete(
                         edu.internet2.middleware.grouper.ws.soap.xsd.GroupDeleteResponse result
                            ) {
                
            }

            public void receiveErrorgroupDelete(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetGroups() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.GetGroups getGroups224=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetGroups.class);
                    // TODO : Fill in the getGroups224 here
                
                        assertNotNull(stub.getGroups(
                        getGroups224));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetGroups() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GetGroups getGroups224=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetGroups.class);
                    // TODO : Fill in the getGroups224 here
                

                stub.startgetGroups(
                         getGroups224,
                    new tempCallbackN10350()
                );
              


        }

        private class tempCallbackN10350  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10350(){ super(null);}

            public void receiveResultgetGroups(
                         edu.internet2.middleware.grouper.ws.soap.xsd.GetGroupsResponse result
                            ) {
                
            }

            public void receiveErrorgetGroups(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindStems() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.FindStems findStems226=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.FindStems)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.FindStems.class);
                    // TODO : Fill in the findStems226 here
                
                        assertNotNull(stub.findStems(
                        findStems226));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindStems() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.FindStems findStems226=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.FindStems)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.FindStems.class);
                    // TODO : Fill in the findStems226 here
                

                stub.startfindStems(
                         findStems226,
                    new tempCallbackN10376()
                );
              


        }

        private class tempCallbackN10376  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10376(){ super(null);}

            public void receiveResultfindStems(
                         edu.internet2.middleware.grouper.ws.soap.xsd.FindStemsResponse result
                            ) {
                
            }

            public void receiveErrorfindStems(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testmemberChangeSubjectLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubjectLite memberChangeSubjectLite228=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubjectLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubjectLite.class);
                    // TODO : Fill in the memberChangeSubjectLite228 here
                
                        assertNotNull(stub.memberChangeSubjectLite(
                        memberChangeSubjectLite228));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartmemberChangeSubjectLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubjectLite memberChangeSubjectLite228=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubjectLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubjectLite.class);
                    // TODO : Fill in the memberChangeSubjectLite228 here
                

                stub.startmemberChangeSubjectLite(
                         memberChangeSubjectLite228,
                    new tempCallbackN1039C()
                );
              


        }

        private class tempCallbackN1039C  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1039C(){ super(null);}

            public void receiveResultmemberChangeSubjectLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubjectLiteResponse result
                            ) {
                
            }

            public void receiveErrormemberChangeSubjectLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMembers() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.GetMembers getMembers230=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetMembers)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetMembers.class);
                    // TODO : Fill in the getMembers230 here
                
                        assertNotNull(stub.getMembers(
                        getMembers230));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMembers() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GetMembers getMembers230=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetMembers)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetMembers.class);
                    // TODO : Fill in the getMembers230 here
                

                stub.startgetMembers(
                         getMembers230,
                    new tempCallbackN103C2()
                );
              


        }

        private class tempCallbackN103C2  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN103C2(){ super(null);}

            public void receiveResultgetMembers(
                         edu.internet2.middleware.grouper.ws.soap.xsd.GetMembersResponse result
                            ) {
                
            }

            public void receiveErrorgetMembers(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testdeleteMemberLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMemberLite deleteMemberLite232=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMemberLite.class);
                    // TODO : Fill in the deleteMemberLite232 here
                
                        assertNotNull(stub.deleteMemberLite(
                        deleteMemberLite232));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartdeleteMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMemberLite deleteMemberLite232=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMemberLite.class);
                    // TODO : Fill in the deleteMemberLite232 here
                

                stub.startdeleteMemberLite(
                         deleteMemberLite232,
                    new tempCallbackN103E8()
                );
              


        }

        private class tempCallbackN103E8  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN103E8(){ super(null);}

            public void receiveResultdeleteMemberLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMemberLiteResponse result
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

           edu.internet2.middleware.grouper.ws.soap.xsd.StemSave stemSave234=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.StemSave)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.StemSave.class);
                    // TODO : Fill in the stemSave234 here
                
                        assertNotNull(stub.stemSave(
                        stemSave234));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemSave() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.StemSave stemSave234=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.StemSave)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.StemSave.class);
                    // TODO : Fill in the stemSave234 here
                

                stub.startstemSave(
                         stemSave234,
                    new tempCallbackN1040E()
                );
              


        }

        private class tempCallbackN1040E  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1040E(){ super(null);}

            public void receiveResultstemSave(
                         edu.internet2.middleware.grouper.ws.soap.xsd.StemSaveResponse result
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

           edu.internet2.middleware.grouper.ws.soap.xsd.FindStemsLite findStemsLite236=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.FindStemsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.FindStemsLite.class);
                    // TODO : Fill in the findStemsLite236 here
                
                        assertNotNull(stub.findStemsLite(
                        findStemsLite236));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindStemsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.FindStemsLite findStemsLite236=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.FindStemsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.FindStemsLite.class);
                    // TODO : Fill in the findStemsLite236 here
                

                stub.startfindStemsLite(
                         findStemsLite236,
                    new tempCallbackN10434()
                );
              


        }

        private class tempCallbackN10434  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10434(){ super(null);}

            public void receiveResultfindStemsLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.FindStemsLiteResponse result
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

           edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubject memberChangeSubject238=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubject)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubject.class);
                    // TODO : Fill in the memberChangeSubject238 here
                
                        assertNotNull(stub.memberChangeSubject(
                        memberChangeSubject238));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartmemberChangeSubject() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubject memberChangeSubject238=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubject)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubject.class);
                    // TODO : Fill in the memberChangeSubject238 here
                

                stub.startmemberChangeSubject(
                         memberChangeSubject238,
                    new tempCallbackN1045A()
                );
              


        }

        private class tempCallbackN1045A  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1045A(){ super(null);}

            public void receiveResultmemberChangeSubject(
                         edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubjectResponse result
                            ) {
                
            }

            public void receiveErrormemberChangeSubject(java.lang.Exception e) {
                fail();
            }

        }
      
        //Create an ADBBean and provide it as the test object
        public org.apache.axis2.databinding.ADBBean getTestObject(java.lang.Class type) throws java.lang.Exception{
           return (org.apache.axis2.databinding.ADBBean) type.newInstance();
        }

        
        

    }
    