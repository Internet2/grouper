

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

           edu.internet2.middleware.grouper.ws.soap.xsd.GroupDeleteLite groupDeleteLite156=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GroupDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GroupDeleteLite.class);
                    // TODO : Fill in the groupDeleteLite156 here
                
                        assertNotNull(stub.groupDeleteLite(
                        groupDeleteLite156));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupDeleteLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GroupDeleteLite groupDeleteLite156=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GroupDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GroupDeleteLite.class);
                    // TODO : Fill in the groupDeleteLite156 here
                

                stub.startgroupDeleteLite(
                         groupDeleteLite156,
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

           edu.internet2.middleware.grouper.ws.soap.xsd.GroupSave groupSave158=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GroupSave)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GroupSave.class);
                    // TODO : Fill in the groupSave158 here
                
                        assertNotNull(stub.groupSave(
                        groupSave158));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupSave() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GroupSave groupSave158=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GroupSave)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GroupSave.class);
                    // TODO : Fill in the groupSave158 here
                

                stub.startgroupSave(
                         groupSave158,
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

           edu.internet2.middleware.grouper.ws.soap.xsd.AddMemberLite addMemberLite160=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.AddMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.AddMemberLite.class);
                    // TODO : Fill in the addMemberLite160 here
                
                        assertNotNull(stub.addMemberLite(
                        addMemberLite160));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartaddMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.AddMemberLite addMemberLite160=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.AddMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.AddMemberLite.class);
                    // TODO : Fill in the addMemberLite160 here
                

                stub.startaddMemberLite(
                         addMemberLite160,
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
        public  void testhasMember() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.HasMember hasMember162=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.HasMember)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.HasMember.class);
                    // TODO : Fill in the hasMember162 here
                
                        assertNotNull(stub.hasMember(
                        hasMember162));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStarthasMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.HasMember hasMember162=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.HasMember)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.HasMember.class);
                    // TODO : Fill in the hasMember162 here
                

                stub.starthasMember(
                         hasMember162,
                    new tempCallbackN1007E()
                );
              


        }

        private class tempCallbackN1007E  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1007E(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap.xsd.StemDelete stemDelete164=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.StemDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.StemDelete.class);
                    // TODO : Fill in the stemDelete164 here
                
                        assertNotNull(stub.stemDelete(
                        stemDelete164));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemDelete() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.StemDelete stemDelete164=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.StemDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.StemDelete.class);
                    // TODO : Fill in the stemDelete164 here
                

                stub.startstemDelete(
                         stemDelete164,
                    new tempCallbackN100A4()
                );
              


        }

        private class tempCallbackN100A4  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN100A4(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap.xsd.StemSaveLite stemSaveLite166=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.StemSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.StemSaveLite.class);
                    // TODO : Fill in the stemSaveLite166 here
                
                        assertNotNull(stub.stemSaveLite(
                        stemSaveLite166));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemSaveLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.StemSaveLite stemSaveLite166=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.StemSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.StemSaveLite.class);
                    // TODO : Fill in the stemSaveLite166 here
                

                stub.startstemSaveLite(
                         stemSaveLite166,
                    new tempCallbackN100CA()
                );
              


        }

        private class tempCallbackN100CA  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN100CA(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap.xsd.GroupSaveLite groupSaveLite168=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GroupSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GroupSaveLite.class);
                    // TODO : Fill in the groupSaveLite168 here
                
                        assertNotNull(stub.groupSaveLite(
                        groupSaveLite168));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupSaveLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GroupSaveLite groupSaveLite168=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GroupSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GroupSaveLite.class);
                    // TODO : Fill in the groupSaveLite168 here
                

                stub.startgroupSaveLite(
                         groupSaveLite168,
                    new tempCallbackN100F0()
                );
              


        }

        private class tempCallbackN100F0  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN100F0(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap.xsd.AddMember addMember170=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.AddMember)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.AddMember.class);
                    // TODO : Fill in the addMember170 here
                
                        assertNotNull(stub.addMember(
                        addMember170));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartaddMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.AddMember addMember170=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.AddMember)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.AddMember.class);
                    // TODO : Fill in the addMember170 here
                

                stub.startaddMember(
                         addMember170,
                    new tempCallbackN10116()
                );
              


        }

        private class tempCallbackN10116  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10116(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap.xsd.FindGroups findGroups172=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.FindGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.FindGroups.class);
                    // TODO : Fill in the findGroups172 here
                
                        assertNotNull(stub.findGroups(
                        findGroups172));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindGroups() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.FindGroups findGroups172=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.FindGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.FindGroups.class);
                    // TODO : Fill in the findGroups172 here
                

                stub.startfindGroups(
                         findGroups172,
                    new tempCallbackN1013C()
                );
              


        }

        private class tempCallbackN1013C  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1013C(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMember deleteMember174=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMember)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMember.class);
                    // TODO : Fill in the deleteMember174 here
                
                        assertNotNull(stub.deleteMember(
                        deleteMember174));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartdeleteMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMember deleteMember174=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMember)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMember.class);
                    // TODO : Fill in the deleteMember174 here
                

                stub.startdeleteMember(
                         deleteMember174,
                    new tempCallbackN10162()
                );
              


        }

        private class tempCallbackN10162  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10162(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap.xsd.HasMemberLite hasMemberLite176=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.HasMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.HasMemberLite.class);
                    // TODO : Fill in the hasMemberLite176 here
                
                        assertNotNull(stub.hasMemberLite(
                        hasMemberLite176));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStarthasMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.HasMemberLite hasMemberLite176=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.HasMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.HasMemberLite.class);
                    // TODO : Fill in the hasMemberLite176 here
                

                stub.starthasMemberLite(
                         hasMemberLite176,
                    new tempCallbackN10188()
                );
              


        }

        private class tempCallbackN10188  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10188(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap.xsd.StemDeleteLite stemDeleteLite178=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.StemDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.StemDeleteLite.class);
                    // TODO : Fill in the stemDeleteLite178 here
                
                        assertNotNull(stub.stemDeleteLite(
                        stemDeleteLite178));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemDeleteLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.StemDeleteLite stemDeleteLite178=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.StemDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.StemDeleteLite.class);
                    // TODO : Fill in the stemDeleteLite178 here
                

                stub.startstemDeleteLite(
                         stemDeleteLite178,
                    new tempCallbackN101AE()
                );
              


        }

        private class tempCallbackN101AE  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN101AE(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap.xsd.GetGrouperPrivilegesLite getGrouperPrivilegesLite180=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetGrouperPrivilegesLite.class);
                    // TODO : Fill in the getGrouperPrivilegesLite180 here
                
                        assertNotNull(stub.getGrouperPrivilegesLite(
                        getGrouperPrivilegesLite180));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetGrouperPrivilegesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GetGrouperPrivilegesLite getGrouperPrivilegesLite180=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetGrouperPrivilegesLite.class);
                    // TODO : Fill in the getGrouperPrivilegesLite180 here
                

                stub.startgetGrouperPrivilegesLite(
                         getGrouperPrivilegesLite180,
                    new tempCallbackN101D4()
                );
              


        }

        private class tempCallbackN101D4  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN101D4(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap.xsd.GetMembersLite getMembersLite182=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetMembersLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetMembersLite.class);
                    // TODO : Fill in the getMembersLite182 here
                
                        assertNotNull(stub.getMembersLite(
                        getMembersLite182));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMembersLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GetMembersLite getMembersLite182=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetMembersLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetMembersLite.class);
                    // TODO : Fill in the getMembersLite182 here
                

                stub.startgetMembersLite(
                         getMembersLite182,
                    new tempCallbackN101FA()
                );
              


        }

        private class tempCallbackN101FA  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN101FA(){ super(null);}

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
        public  void testgetGroupsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.GetGroupsLite getGroupsLite184=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetGroupsLite.class);
                    // TODO : Fill in the getGroupsLite184 here
                
                        assertNotNull(stub.getGroupsLite(
                        getGroupsLite184));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetGroupsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GetGroupsLite getGroupsLite184=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetGroupsLite.class);
                    // TODO : Fill in the getGroupsLite184 here
                

                stub.startgetGroupsLite(
                         getGroupsLite184,
                    new tempCallbackN10220()
                );
              


        }

        private class tempCallbackN10220  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10220(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap.xsd.AssignGrouperPrivilegesLite assignGrouperPrivilegesLite186=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.AssignGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.AssignGrouperPrivilegesLite.class);
                    // TODO : Fill in the assignGrouperPrivilegesLite186 here
                
                        assertNotNull(stub.assignGrouperPrivilegesLite(
                        assignGrouperPrivilegesLite186));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignGrouperPrivilegesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.AssignGrouperPrivilegesLite assignGrouperPrivilegesLite186=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.AssignGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.AssignGrouperPrivilegesLite.class);
                    // TODO : Fill in the assignGrouperPrivilegesLite186 here
                

                stub.startassignGrouperPrivilegesLite(
                         assignGrouperPrivilegesLite186,
                    new tempCallbackN10246()
                );
              


        }

        private class tempCallbackN10246  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10246(){ super(null);}

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
        public  void testfindGroupsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.FindGroupsLite findGroupsLite188=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.FindGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.FindGroupsLite.class);
                    // TODO : Fill in the findGroupsLite188 here
                
                        assertNotNull(stub.findGroupsLite(
                        findGroupsLite188));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindGroupsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.FindGroupsLite findGroupsLite188=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.FindGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.FindGroupsLite.class);
                    // TODO : Fill in the findGroupsLite188 here
                

                stub.startfindGroupsLite(
                         findGroupsLite188,
                    new tempCallbackN1026C()
                );
              


        }

        private class tempCallbackN1026C  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1026C(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap.xsd.GroupDelete groupDelete190=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GroupDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GroupDelete.class);
                    // TODO : Fill in the groupDelete190 here
                
                        assertNotNull(stub.groupDelete(
                        groupDelete190));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupDelete() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GroupDelete groupDelete190=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GroupDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GroupDelete.class);
                    // TODO : Fill in the groupDelete190 here
                

                stub.startgroupDelete(
                         groupDelete190,
                    new tempCallbackN10292()
                );
              


        }

        private class tempCallbackN10292  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10292(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap.xsd.GetGroups getGroups192=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetGroups.class);
                    // TODO : Fill in the getGroups192 here
                
                        assertNotNull(stub.getGroups(
                        getGroups192));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetGroups() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GetGroups getGroups192=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetGroups.class);
                    // TODO : Fill in the getGroups192 here
                

                stub.startgetGroups(
                         getGroups192,
                    new tempCallbackN102B8()
                );
              


        }

        private class tempCallbackN102B8  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN102B8(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap.xsd.FindStems findStems194=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.FindStems)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.FindStems.class);
                    // TODO : Fill in the findStems194 here
                
                        assertNotNull(stub.findStems(
                        findStems194));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindStems() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.FindStems findStems194=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.FindStems)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.FindStems.class);
                    // TODO : Fill in the findStems194 here
                

                stub.startfindStems(
                         findStems194,
                    new tempCallbackN102DE()
                );
              


        }

        private class tempCallbackN102DE  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN102DE(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubjectLite memberChangeSubjectLite196=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubjectLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubjectLite.class);
                    // TODO : Fill in the memberChangeSubjectLite196 here
                
                        assertNotNull(stub.memberChangeSubjectLite(
                        memberChangeSubjectLite196));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartmemberChangeSubjectLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubjectLite memberChangeSubjectLite196=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubjectLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubjectLite.class);
                    // TODO : Fill in the memberChangeSubjectLite196 here
                

                stub.startmemberChangeSubjectLite(
                         memberChangeSubjectLite196,
                    new tempCallbackN10304()
                );
              


        }

        private class tempCallbackN10304  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10304(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap.xsd.GetMembers getMembers198=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetMembers)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetMembers.class);
                    // TODO : Fill in the getMembers198 here
                
                        assertNotNull(stub.getMembers(
                        getMembers198));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMembers() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GetMembers getMembers198=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetMembers)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetMembers.class);
                    // TODO : Fill in the getMembers198 here
                

                stub.startgetMembers(
                         getMembers198,
                    new tempCallbackN1032A()
                );
              


        }

        private class tempCallbackN1032A  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1032A(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMemberLite deleteMemberLite200=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMemberLite.class);
                    // TODO : Fill in the deleteMemberLite200 here
                
                        assertNotNull(stub.deleteMemberLite(
                        deleteMemberLite200));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartdeleteMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMemberLite deleteMemberLite200=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMemberLite.class);
                    // TODO : Fill in the deleteMemberLite200 here
                

                stub.startdeleteMemberLite(
                         deleteMemberLite200,
                    new tempCallbackN10350()
                );
              


        }

        private class tempCallbackN10350  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10350(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap.xsd.StemSave stemSave202=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.StemSave)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.StemSave.class);
                    // TODO : Fill in the stemSave202 here
                
                        assertNotNull(stub.stemSave(
                        stemSave202));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemSave() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.StemSave stemSave202=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.StemSave)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.StemSave.class);
                    // TODO : Fill in the stemSave202 here
                

                stub.startstemSave(
                         stemSave202,
                    new tempCallbackN10376()
                );
              


        }

        private class tempCallbackN10376  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10376(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap.xsd.FindStemsLite findStemsLite204=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.FindStemsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.FindStemsLite.class);
                    // TODO : Fill in the findStemsLite204 here
                
                        assertNotNull(stub.findStemsLite(
                        findStemsLite204));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindStemsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.FindStemsLite findStemsLite204=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.FindStemsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.FindStemsLite.class);
                    // TODO : Fill in the findStemsLite204 here
                

                stub.startfindStemsLite(
                         findStemsLite204,
                    new tempCallbackN1039C()
                );
              


        }

        private class tempCallbackN1039C  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1039C(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubject memberChangeSubject206=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubject)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubject.class);
                    // TODO : Fill in the memberChangeSubject206 here
                
                        assertNotNull(stub.memberChangeSubject(
                        memberChangeSubject206));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartmemberChangeSubject() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubject memberChangeSubject206=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubject)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubject.class);
                    // TODO : Fill in the memberChangeSubject206 here
                

                stub.startmemberChangeSubject(
                         memberChangeSubject206,
                    new tempCallbackN103C2()
                );
              


        }

        private class tempCallbackN103C2  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN103C2(){ super(null);}

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
    