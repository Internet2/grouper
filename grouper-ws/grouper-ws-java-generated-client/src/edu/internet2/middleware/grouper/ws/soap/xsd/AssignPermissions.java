
/**
 * AssignPermissions.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4.1  Built on : Aug 13, 2008 (05:03:41 LKT)
 */
            
                package edu.internet2.middleware.grouper.ws.soap.xsd;
            

            /**
            *  AssignPermissions bean class
            */
        
        public  class AssignPermissions
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://soap.ws.grouper.middleware.internet2.edu/xsd",
                "assignPermissions",
                "ns1");

            

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://soap.ws.grouper.middleware.internet2.edu/xsd")){
                return "ns1";
            }
            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        

                        /**
                        * field for ClientVersion
                        */

                        
                                    protected java.lang.String localClientVersion ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localClientVersionTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getClientVersion(){
                               return localClientVersion;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ClientVersion
                               */
                               public void setClientVersion(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localClientVersionTracker = true;
                                       } else {
                                          localClientVersionTracker = true;
                                              
                                       }
                                   
                                            this.localClientVersion=param;
                                    

                               }
                            

                        /**
                        * field for PermissionType
                        */

                        
                                    protected java.lang.String localPermissionType ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPermissionTypeTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPermissionType(){
                               return localPermissionType;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PermissionType
                               */
                               public void setPermissionType(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localPermissionTypeTracker = true;
                                       } else {
                                          localPermissionTypeTracker = true;
                                              
                                       }
                                   
                                            this.localPermissionType=param;
                                    

                               }
                            

                        /**
                        * field for PermissionDefNameLookups
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeDefNameLookup[] localPermissionDefNameLookups ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPermissionDefNameLookupsTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeDefNameLookup[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeDefNameLookup[] getPermissionDefNameLookups(){
                               return localPermissionDefNameLookups;
                           }

                           
                        


                               
                              /**
                               * validate the array for PermissionDefNameLookups
                               */
                              protected void validatePermissionDefNameLookups(edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeDefNameLookup[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param PermissionDefNameLookups
                              */
                              public void setPermissionDefNameLookups(edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeDefNameLookup[] param){
                              
                                   validatePermissionDefNameLookups(param);

                               
                                          if (param != null){
                                             //update the setting tracker
                                             localPermissionDefNameLookupsTracker = true;
                                          } else {
                                             localPermissionDefNameLookupsTracker = true;
                                                 
                                          }
                                      
                                      this.localPermissionDefNameLookups=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeDefNameLookup
                             */
                             public void addPermissionDefNameLookups(edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeDefNameLookup param){
                                   if (localPermissionDefNameLookups == null){
                                   localPermissionDefNameLookups = new edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeDefNameLookup[]{};
                                   }

                            
                                 //update the setting tracker
                                localPermissionDefNameLookupsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localPermissionDefNameLookups);
                               list.add(param);
                               this.localPermissionDefNameLookups =
                             (edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeDefNameLookup[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeDefNameLookup[list.size()]);

                             }
                             

                        /**
                        * field for PermissionAssignOperation
                        */

                        
                                    protected java.lang.String localPermissionAssignOperation ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPermissionAssignOperationTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPermissionAssignOperation(){
                               return localPermissionAssignOperation;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PermissionAssignOperation
                               */
                               public void setPermissionAssignOperation(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localPermissionAssignOperationTracker = true;
                                       } else {
                                          localPermissionAssignOperationTracker = true;
                                              
                                       }
                                   
                                            this.localPermissionAssignOperation=param;
                                    

                               }
                            

                        /**
                        * field for AssignmentNotes
                        */

                        
                                    protected java.lang.String localAssignmentNotes ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAssignmentNotesTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAssignmentNotes(){
                               return localAssignmentNotes;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AssignmentNotes
                               */
                               public void setAssignmentNotes(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localAssignmentNotesTracker = true;
                                       } else {
                                          localAssignmentNotesTracker = true;
                                              
                                       }
                                   
                                            this.localAssignmentNotes=param;
                                    

                               }
                            

                        /**
                        * field for AssignmentEnabledTime
                        */

                        
                                    protected java.lang.String localAssignmentEnabledTime ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAssignmentEnabledTimeTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAssignmentEnabledTime(){
                               return localAssignmentEnabledTime;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AssignmentEnabledTime
                               */
                               public void setAssignmentEnabledTime(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localAssignmentEnabledTimeTracker = true;
                                       } else {
                                          localAssignmentEnabledTimeTracker = true;
                                              
                                       }
                                   
                                            this.localAssignmentEnabledTime=param;
                                    

                               }
                            

                        /**
                        * field for AssignmentDisabledTime
                        */

                        
                                    protected java.lang.String localAssignmentDisabledTime ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAssignmentDisabledTimeTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAssignmentDisabledTime(){
                               return localAssignmentDisabledTime;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AssignmentDisabledTime
                               */
                               public void setAssignmentDisabledTime(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localAssignmentDisabledTimeTracker = true;
                                       } else {
                                          localAssignmentDisabledTimeTracker = true;
                                              
                                       }
                                   
                                            this.localAssignmentDisabledTime=param;
                                    

                               }
                            

                        /**
                        * field for Delegatable
                        */

                        
                                    protected java.lang.String localDelegatable ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDelegatableTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getDelegatable(){
                               return localDelegatable;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Delegatable
                               */
                               public void setDelegatable(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localDelegatableTracker = true;
                                       } else {
                                          localDelegatableTracker = true;
                                              
                                       }
                                   
                                            this.localDelegatable=param;
                                    

                               }
                            

                        /**
                        * field for WsAttributeAssignLookups
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeAssignLookup[] localWsAttributeAssignLookups ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsAttributeAssignLookupsTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeAssignLookup[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeAssignLookup[] getWsAttributeAssignLookups(){
                               return localWsAttributeAssignLookups;
                           }

                           
                        


                               
                              /**
                               * validate the array for WsAttributeAssignLookups
                               */
                              protected void validateWsAttributeAssignLookups(edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeAssignLookup[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param WsAttributeAssignLookups
                              */
                              public void setWsAttributeAssignLookups(edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeAssignLookup[] param){
                              
                                   validateWsAttributeAssignLookups(param);

                               
                                          if (param != null){
                                             //update the setting tracker
                                             localWsAttributeAssignLookupsTracker = true;
                                          } else {
                                             localWsAttributeAssignLookupsTracker = true;
                                                 
                                          }
                                      
                                      this.localWsAttributeAssignLookups=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeAssignLookup
                             */
                             public void addWsAttributeAssignLookups(edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeAssignLookup param){
                                   if (localWsAttributeAssignLookups == null){
                                   localWsAttributeAssignLookups = new edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeAssignLookup[]{};
                                   }

                            
                                 //update the setting tracker
                                localWsAttributeAssignLookupsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localWsAttributeAssignLookups);
                               list.add(param);
                               this.localWsAttributeAssignLookups =
                             (edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeAssignLookup[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeAssignLookup[list.size()]);

                             }
                             

                        /**
                        * field for RoleLookups
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap.xsd.WsGroupLookup[] localRoleLookups ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localRoleLookupsTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap.xsd.WsGroupLookup[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap.xsd.WsGroupLookup[] getRoleLookups(){
                               return localRoleLookups;
                           }

                           
                        


                               
                              /**
                               * validate the array for RoleLookups
                               */
                              protected void validateRoleLookups(edu.internet2.middleware.grouper.ws.soap.xsd.WsGroupLookup[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param RoleLookups
                              */
                              public void setRoleLookups(edu.internet2.middleware.grouper.ws.soap.xsd.WsGroupLookup[] param){
                              
                                   validateRoleLookups(param);

                               
                                          if (param != null){
                                             //update the setting tracker
                                             localRoleLookupsTracker = true;
                                          } else {
                                             localRoleLookupsTracker = true;
                                                 
                                          }
                                      
                                      this.localRoleLookups=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap.xsd.WsGroupLookup
                             */
                             public void addRoleLookups(edu.internet2.middleware.grouper.ws.soap.xsd.WsGroupLookup param){
                                   if (localRoleLookups == null){
                                   localRoleLookups = new edu.internet2.middleware.grouper.ws.soap.xsd.WsGroupLookup[]{};
                                   }

                            
                                 //update the setting tracker
                                localRoleLookupsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localRoleLookups);
                               list.add(param);
                               this.localRoleLookups =
                             (edu.internet2.middleware.grouper.ws.soap.xsd.WsGroupLookup[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap.xsd.WsGroupLookup[list.size()]);

                             }
                             

                        /**
                        * field for SubjectRoleLookups
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap.xsd.WsMembershipAnyLookup[] localSubjectRoleLookups ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSubjectRoleLookupsTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap.xsd.WsMembershipAnyLookup[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap.xsd.WsMembershipAnyLookup[] getSubjectRoleLookups(){
                               return localSubjectRoleLookups;
                           }

                           
                        


                               
                              /**
                               * validate the array for SubjectRoleLookups
                               */
                              protected void validateSubjectRoleLookups(edu.internet2.middleware.grouper.ws.soap.xsd.WsMembershipAnyLookup[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param SubjectRoleLookups
                              */
                              public void setSubjectRoleLookups(edu.internet2.middleware.grouper.ws.soap.xsd.WsMembershipAnyLookup[] param){
                              
                                   validateSubjectRoleLookups(param);

                               
                                          if (param != null){
                                             //update the setting tracker
                                             localSubjectRoleLookupsTracker = true;
                                          } else {
                                             localSubjectRoleLookupsTracker = true;
                                                 
                                          }
                                      
                                      this.localSubjectRoleLookups=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap.xsd.WsMembershipAnyLookup
                             */
                             public void addSubjectRoleLookups(edu.internet2.middleware.grouper.ws.soap.xsd.WsMembershipAnyLookup param){
                                   if (localSubjectRoleLookups == null){
                                   localSubjectRoleLookups = new edu.internet2.middleware.grouper.ws.soap.xsd.WsMembershipAnyLookup[]{};
                                   }

                            
                                 //update the setting tracker
                                localSubjectRoleLookupsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localSubjectRoleLookups);
                               list.add(param);
                               this.localSubjectRoleLookups =
                             (edu.internet2.middleware.grouper.ws.soap.xsd.WsMembershipAnyLookup[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap.xsd.WsMembershipAnyLookup[list.size()]);

                             }
                             

                        /**
                        * field for Actions
                        * This was an Array!
                        */

                        
                                    protected java.lang.String[] localActions ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localActionsTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String[]
                           */
                           public  java.lang.String[] getActions(){
                               return localActions;
                           }

                           
                        


                               
                              /**
                               * validate the array for Actions
                               */
                              protected void validateActions(java.lang.String[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param Actions
                              */
                              public void setActions(java.lang.String[] param){
                              
                                   validateActions(param);

                               
                                          if (param != null){
                                             //update the setting tracker
                                             localActionsTracker = true;
                                          } else {
                                             localActionsTracker = true;
                                                 
                                          }
                                      
                                      this.localActions=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param java.lang.String
                             */
                             public void addActions(java.lang.String param){
                                   if (localActions == null){
                                   localActions = new java.lang.String[]{};
                                   }

                            
                                 //update the setting tracker
                                localActionsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localActions);
                               list.add(param);
                               this.localActions =
                             (java.lang.String[])list.toArray(
                            new java.lang.String[list.size()]);

                             }
                             

                        /**
                        * field for ActAsSubjectLookup
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap.xsd.WsSubjectLookup localActAsSubjectLookup ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localActAsSubjectLookupTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap.xsd.WsSubjectLookup
                           */
                           public  edu.internet2.middleware.grouper.ws.soap.xsd.WsSubjectLookup getActAsSubjectLookup(){
                               return localActAsSubjectLookup;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ActAsSubjectLookup
                               */
                               public void setActAsSubjectLookup(edu.internet2.middleware.grouper.ws.soap.xsd.WsSubjectLookup param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localActAsSubjectLookupTracker = true;
                                       } else {
                                          localActAsSubjectLookupTracker = true;
                                              
                                       }
                                   
                                            this.localActAsSubjectLookup=param;
                                    

                               }
                            

                        /**
                        * field for IncludeSubjectDetail
                        */

                        
                                    protected java.lang.String localIncludeSubjectDetail ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIncludeSubjectDetailTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getIncludeSubjectDetail(){
                               return localIncludeSubjectDetail;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IncludeSubjectDetail
                               */
                               public void setIncludeSubjectDetail(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localIncludeSubjectDetailTracker = true;
                                       } else {
                                          localIncludeSubjectDetailTracker = true;
                                              
                                       }
                                   
                                            this.localIncludeSubjectDetail=param;
                                    

                               }
                            

                        /**
                        * field for SubjectAttributeNames
                        * This was an Array!
                        */

                        
                                    protected java.lang.String[] localSubjectAttributeNames ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSubjectAttributeNamesTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String[]
                           */
                           public  java.lang.String[] getSubjectAttributeNames(){
                               return localSubjectAttributeNames;
                           }

                           
                        


                               
                              /**
                               * validate the array for SubjectAttributeNames
                               */
                              protected void validateSubjectAttributeNames(java.lang.String[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param SubjectAttributeNames
                              */
                              public void setSubjectAttributeNames(java.lang.String[] param){
                              
                                   validateSubjectAttributeNames(param);

                               
                                          if (param != null){
                                             //update the setting tracker
                                             localSubjectAttributeNamesTracker = true;
                                          } else {
                                             localSubjectAttributeNamesTracker = true;
                                                 
                                          }
                                      
                                      this.localSubjectAttributeNames=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param java.lang.String
                             */
                             public void addSubjectAttributeNames(java.lang.String param){
                                   if (localSubjectAttributeNames == null){
                                   localSubjectAttributeNames = new java.lang.String[]{};
                                   }

                            
                                 //update the setting tracker
                                localSubjectAttributeNamesTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localSubjectAttributeNames);
                               list.add(param);
                               this.localSubjectAttributeNames =
                             (java.lang.String[])list.toArray(
                            new java.lang.String[list.size()]);

                             }
                             

                        /**
                        * field for IncludeGroupDetail
                        */

                        
                                    protected java.lang.String localIncludeGroupDetail ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIncludeGroupDetailTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getIncludeGroupDetail(){
                               return localIncludeGroupDetail;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IncludeGroupDetail
                               */
                               public void setIncludeGroupDetail(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localIncludeGroupDetailTracker = true;
                                       } else {
                                          localIncludeGroupDetailTracker = true;
                                              
                                       }
                                   
                                            this.localIncludeGroupDetail=param;
                                    

                               }
                            

                        /**
                        * field for Params
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap.xsd.WsParam[] localParams ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localParamsTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap.xsd.WsParam[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap.xsd.WsParam[] getParams(){
                               return localParams;
                           }

                           
                        


                               
                              /**
                               * validate the array for Params
                               */
                              protected void validateParams(edu.internet2.middleware.grouper.ws.soap.xsd.WsParam[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param Params
                              */
                              public void setParams(edu.internet2.middleware.grouper.ws.soap.xsd.WsParam[] param){
                              
                                   validateParams(param);

                               
                                          if (param != null){
                                             //update the setting tracker
                                             localParamsTracker = true;
                                          } else {
                                             localParamsTracker = true;
                                                 
                                          }
                                      
                                      this.localParams=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap.xsd.WsParam
                             */
                             public void addParams(edu.internet2.middleware.grouper.ws.soap.xsd.WsParam param){
                                   if (localParams == null){
                                   localParams = new edu.internet2.middleware.grouper.ws.soap.xsd.WsParam[]{};
                                   }

                            
                                 //update the setting tracker
                                localParamsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localParams);
                               list.add(param);
                               this.localParams =
                             (edu.internet2.middleware.grouper.ws.soap.xsd.WsParam[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap.xsd.WsParam[list.size()]);

                             }
                             

                        /**
                        * field for AttributeDefsToReplace
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeDefLookup[] localAttributeDefsToReplace ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAttributeDefsToReplaceTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeDefLookup[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeDefLookup[] getAttributeDefsToReplace(){
                               return localAttributeDefsToReplace;
                           }

                           
                        


                               
                              /**
                               * validate the array for AttributeDefsToReplace
                               */
                              protected void validateAttributeDefsToReplace(edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeDefLookup[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param AttributeDefsToReplace
                              */
                              public void setAttributeDefsToReplace(edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeDefLookup[] param){
                              
                                   validateAttributeDefsToReplace(param);

                               
                                          if (param != null){
                                             //update the setting tracker
                                             localAttributeDefsToReplaceTracker = true;
                                          } else {
                                             localAttributeDefsToReplaceTracker = true;
                                                 
                                          }
                                      
                                      this.localAttributeDefsToReplace=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeDefLookup
                             */
                             public void addAttributeDefsToReplace(edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeDefLookup param){
                                   if (localAttributeDefsToReplace == null){
                                   localAttributeDefsToReplace = new edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeDefLookup[]{};
                                   }

                            
                                 //update the setting tracker
                                localAttributeDefsToReplaceTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localAttributeDefsToReplace);
                               list.add(param);
                               this.localAttributeDefsToReplace =
                             (edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeDefLookup[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeDefLookup[list.size()]);

                             }
                             

                        /**
                        * field for ActionsToReplace
                        * This was an Array!
                        */

                        
                                    protected java.lang.String[] localActionsToReplace ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localActionsToReplaceTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String[]
                           */
                           public  java.lang.String[] getActionsToReplace(){
                               return localActionsToReplace;
                           }

                           
                        


                               
                              /**
                               * validate the array for ActionsToReplace
                               */
                              protected void validateActionsToReplace(java.lang.String[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param ActionsToReplace
                              */
                              public void setActionsToReplace(java.lang.String[] param){
                              
                                   validateActionsToReplace(param);

                               
                                          if (param != null){
                                             //update the setting tracker
                                             localActionsToReplaceTracker = true;
                                          } else {
                                             localActionsToReplaceTracker = true;
                                                 
                                          }
                                      
                                      this.localActionsToReplace=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param java.lang.String
                             */
                             public void addActionsToReplace(java.lang.String param){
                                   if (localActionsToReplace == null){
                                   localActionsToReplace = new java.lang.String[]{};
                                   }

                            
                                 //update the setting tracker
                                localActionsToReplaceTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localActionsToReplace);
                               list.add(param);
                               this.localActionsToReplace =
                             (java.lang.String[])list.toArray(
                            new java.lang.String[list.size()]);

                             }
                             

                        /**
                        * field for Disallowed
                        */

                        
                                    protected java.lang.String localDisallowed ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDisallowedTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getDisallowed(){
                               return localDisallowed;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Disallowed
                               */
                               public void setDisallowed(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localDisallowedTracker = true;
                                       } else {
                                          localDisallowedTracker = true;
                                              
                                       }
                                   
                                            this.localDisallowed=param;
                                    

                               }
                            

     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;
        
        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }
     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement (
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{


        
                org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,MY_QNAME){

                 public void serialize(org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
                       AssignPermissions.this.serialize(MY_QNAME,factory,xmlWriter);
                 }
               };
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               MY_QNAME,factory,dataSource);
            
       }

         public void serialize(final javax.xml.namespace.QName parentQName,
                                       final org.apache.axiom.om.OMFactory factory,
                                       org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                           serialize(parentQName,factory,xmlWriter,false);
         }

         public void serialize(final javax.xml.namespace.QName parentQName,
                               final org.apache.axiom.om.OMFactory factory,
                               org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter,
                               boolean serializeType)
            throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
            
                


                java.lang.String prefix = null;
                java.lang.String namespace = null;
                

                    prefix = parentQName.getPrefix();
                    namespace = parentQName.getNamespaceURI();

                    if ((namespace != null) && (namespace.trim().length() > 0)) {
                        java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                        if (writerPrefix != null) {
                            xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                        } else {
                            if (prefix == null) {
                                prefix = generatePrefix(namespace);
                            }

                            xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                            xmlWriter.writeNamespace(prefix, namespace);
                            xmlWriter.setPrefix(prefix, namespace);
                        }
                    } else {
                        xmlWriter.writeStartElement(parentQName.getLocalPart());
                    }
                
                  if (serializeType){
               

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://soap.ws.grouper.middleware.internet2.edu/xsd");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":assignPermissions",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "assignPermissions",
                           xmlWriter);
                   }

               
                   }
                if (localClientVersionTracker){
                                    namespace = "http://soap.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"clientVersion", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"clientVersion");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("clientVersion");
                                    }
                                

                                          if (localClientVersion==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localClientVersion);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPermissionTypeTracker){
                                    namespace = "http://soap.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"permissionType", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"permissionType");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("permissionType");
                                    }
                                

                                          if (localPermissionType==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPermissionType);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPermissionDefNameLookupsTracker){
                                       if (localPermissionDefNameLookups!=null){
                                            for (int i = 0;i < localPermissionDefNameLookups.length;i++){
                                                if (localPermissionDefNameLookups[i] != null){
                                                 localPermissionDefNameLookups[i].serialize(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","permissionDefNameLookups"),
                                                           factory,xmlWriter);
                                                } else {
                                                   
                                                            // write null attribute
                                                            java.lang.String namespace2 = "http://soap.ws.grouper.middleware.internet2.edu/xsd";
                                                            if (! namespace2.equals("")) {
                                                                java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                                if (prefix2 == null) {
                                                                    prefix2 = generatePrefix(namespace2);

                                                                    xmlWriter.writeStartElement(prefix2,"permissionDefNameLookups", namespace2);
                                                                    xmlWriter.writeNamespace(prefix2, namespace2);
                                                                    xmlWriter.setPrefix(prefix2, namespace2);

                                                                } else {
                                                                    xmlWriter.writeStartElement(namespace2,"permissionDefNameLookups");
                                                                }

                                                            } else {
                                                                xmlWriter.writeStartElement("permissionDefNameLookups");
                                                            }

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                // write null attribute
                                                java.lang.String namespace2 = "http://soap.ws.grouper.middleware.internet2.edu/xsd";
                                                if (! namespace2.equals("")) {
                                                    java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                    if (prefix2 == null) {
                                                        prefix2 = generatePrefix(namespace2);

                                                        xmlWriter.writeStartElement(prefix2,"permissionDefNameLookups", namespace2);
                                                        xmlWriter.writeNamespace(prefix2, namespace2);
                                                        xmlWriter.setPrefix(prefix2, namespace2);

                                                    } else {
                                                        xmlWriter.writeStartElement(namespace2,"permissionDefNameLookups");
                                                    }

                                                } else {
                                                    xmlWriter.writeStartElement("permissionDefNameLookups");
                                                }

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localPermissionAssignOperationTracker){
                                    namespace = "http://soap.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"permissionAssignOperation", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"permissionAssignOperation");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("permissionAssignOperation");
                                    }
                                

                                          if (localPermissionAssignOperation==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPermissionAssignOperation);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAssignmentNotesTracker){
                                    namespace = "http://soap.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"assignmentNotes", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"assignmentNotes");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("assignmentNotes");
                                    }
                                

                                          if (localAssignmentNotes==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAssignmentNotes);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAssignmentEnabledTimeTracker){
                                    namespace = "http://soap.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"assignmentEnabledTime", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"assignmentEnabledTime");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("assignmentEnabledTime");
                                    }
                                

                                          if (localAssignmentEnabledTime==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAssignmentEnabledTime);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAssignmentDisabledTimeTracker){
                                    namespace = "http://soap.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"assignmentDisabledTime", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"assignmentDisabledTime");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("assignmentDisabledTime");
                                    }
                                

                                          if (localAssignmentDisabledTime==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAssignmentDisabledTime);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localDelegatableTracker){
                                    namespace = "http://soap.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"delegatable", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"delegatable");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("delegatable");
                                    }
                                

                                          if (localDelegatable==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localDelegatable);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localWsAttributeAssignLookupsTracker){
                                       if (localWsAttributeAssignLookups!=null){
                                            for (int i = 0;i < localWsAttributeAssignLookups.length;i++){
                                                if (localWsAttributeAssignLookups[i] != null){
                                                 localWsAttributeAssignLookups[i].serialize(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","wsAttributeAssignLookups"),
                                                           factory,xmlWriter);
                                                } else {
                                                   
                                                            // write null attribute
                                                            java.lang.String namespace2 = "http://soap.ws.grouper.middleware.internet2.edu/xsd";
                                                            if (! namespace2.equals("")) {
                                                                java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                                if (prefix2 == null) {
                                                                    prefix2 = generatePrefix(namespace2);

                                                                    xmlWriter.writeStartElement(prefix2,"wsAttributeAssignLookups", namespace2);
                                                                    xmlWriter.writeNamespace(prefix2, namespace2);
                                                                    xmlWriter.setPrefix(prefix2, namespace2);

                                                                } else {
                                                                    xmlWriter.writeStartElement(namespace2,"wsAttributeAssignLookups");
                                                                }

                                                            } else {
                                                                xmlWriter.writeStartElement("wsAttributeAssignLookups");
                                                            }

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                // write null attribute
                                                java.lang.String namespace2 = "http://soap.ws.grouper.middleware.internet2.edu/xsd";
                                                if (! namespace2.equals("")) {
                                                    java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                    if (prefix2 == null) {
                                                        prefix2 = generatePrefix(namespace2);

                                                        xmlWriter.writeStartElement(prefix2,"wsAttributeAssignLookups", namespace2);
                                                        xmlWriter.writeNamespace(prefix2, namespace2);
                                                        xmlWriter.setPrefix(prefix2, namespace2);

                                                    } else {
                                                        xmlWriter.writeStartElement(namespace2,"wsAttributeAssignLookups");
                                                    }

                                                } else {
                                                    xmlWriter.writeStartElement("wsAttributeAssignLookups");
                                                }

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localRoleLookupsTracker){
                                       if (localRoleLookups!=null){
                                            for (int i = 0;i < localRoleLookups.length;i++){
                                                if (localRoleLookups[i] != null){
                                                 localRoleLookups[i].serialize(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","roleLookups"),
                                                           factory,xmlWriter);
                                                } else {
                                                   
                                                            // write null attribute
                                                            java.lang.String namespace2 = "http://soap.ws.grouper.middleware.internet2.edu/xsd";
                                                            if (! namespace2.equals("")) {
                                                                java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                                if (prefix2 == null) {
                                                                    prefix2 = generatePrefix(namespace2);

                                                                    xmlWriter.writeStartElement(prefix2,"roleLookups", namespace2);
                                                                    xmlWriter.writeNamespace(prefix2, namespace2);
                                                                    xmlWriter.setPrefix(prefix2, namespace2);

                                                                } else {
                                                                    xmlWriter.writeStartElement(namespace2,"roleLookups");
                                                                }

                                                            } else {
                                                                xmlWriter.writeStartElement("roleLookups");
                                                            }

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                // write null attribute
                                                java.lang.String namespace2 = "http://soap.ws.grouper.middleware.internet2.edu/xsd";
                                                if (! namespace2.equals("")) {
                                                    java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                    if (prefix2 == null) {
                                                        prefix2 = generatePrefix(namespace2);

                                                        xmlWriter.writeStartElement(prefix2,"roleLookups", namespace2);
                                                        xmlWriter.writeNamespace(prefix2, namespace2);
                                                        xmlWriter.setPrefix(prefix2, namespace2);

                                                    } else {
                                                        xmlWriter.writeStartElement(namespace2,"roleLookups");
                                                    }

                                                } else {
                                                    xmlWriter.writeStartElement("roleLookups");
                                                }

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localSubjectRoleLookupsTracker){
                                       if (localSubjectRoleLookups!=null){
                                            for (int i = 0;i < localSubjectRoleLookups.length;i++){
                                                if (localSubjectRoleLookups[i] != null){
                                                 localSubjectRoleLookups[i].serialize(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","subjectRoleLookups"),
                                                           factory,xmlWriter);
                                                } else {
                                                   
                                                            // write null attribute
                                                            java.lang.String namespace2 = "http://soap.ws.grouper.middleware.internet2.edu/xsd";
                                                            if (! namespace2.equals("")) {
                                                                java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                                if (prefix2 == null) {
                                                                    prefix2 = generatePrefix(namespace2);

                                                                    xmlWriter.writeStartElement(prefix2,"subjectRoleLookups", namespace2);
                                                                    xmlWriter.writeNamespace(prefix2, namespace2);
                                                                    xmlWriter.setPrefix(prefix2, namespace2);

                                                                } else {
                                                                    xmlWriter.writeStartElement(namespace2,"subjectRoleLookups");
                                                                }

                                                            } else {
                                                                xmlWriter.writeStartElement("subjectRoleLookups");
                                                            }

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                // write null attribute
                                                java.lang.String namespace2 = "http://soap.ws.grouper.middleware.internet2.edu/xsd";
                                                if (! namespace2.equals("")) {
                                                    java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                    if (prefix2 == null) {
                                                        prefix2 = generatePrefix(namespace2);

                                                        xmlWriter.writeStartElement(prefix2,"subjectRoleLookups", namespace2);
                                                        xmlWriter.writeNamespace(prefix2, namespace2);
                                                        xmlWriter.setPrefix(prefix2, namespace2);

                                                    } else {
                                                        xmlWriter.writeStartElement(namespace2,"subjectRoleLookups");
                                                    }

                                                } else {
                                                    xmlWriter.writeStartElement("subjectRoleLookups");
                                                }

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localActionsTracker){
                             if (localActions!=null) {
                                   namespace = "http://soap.ws.grouper.middleware.internet2.edu/xsd";
                                   boolean emptyNamespace = namespace == null || namespace.length() == 0;
                                   prefix =  emptyNamespace ? null : xmlWriter.getPrefix(namespace);
                                   for (int i = 0;i < localActions.length;i++){
                                        
                                            if (localActions[i] != null){
                                        
                                                if (!emptyNamespace) {
                                                    if (prefix == null) {
                                                        java.lang.String prefix2 = generatePrefix(namespace);

                                                        xmlWriter.writeStartElement(prefix2,"actions", namespace);
                                                        xmlWriter.writeNamespace(prefix2, namespace);
                                                        xmlWriter.setPrefix(prefix2, namespace);

                                                    } else {
                                                        xmlWriter.writeStartElement(namespace,"actions");
                                                    }

                                                } else {
                                                    xmlWriter.writeStartElement("actions");
                                                }

                                            
                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localActions[i]));
                                                    
                                                xmlWriter.writeEndElement();
                                              
                                                } else {
                                                   
                                                           // write null attribute
                                                            namespace = "http://soap.ws.grouper.middleware.internet2.edu/xsd";
                                                            if (! namespace.equals("")) {
                                                                prefix = xmlWriter.getPrefix(namespace);

                                                                if (prefix == null) {
                                                                    prefix = generatePrefix(namespace);

                                                                    xmlWriter.writeStartElement(prefix,"actions", namespace);
                                                                    xmlWriter.writeNamespace(prefix, namespace);
                                                                    xmlWriter.setPrefix(prefix, namespace);

                                                                } else {
                                                                    xmlWriter.writeStartElement(namespace,"actions");
                                                                }

                                                            } else {
                                                                xmlWriter.writeStartElement("actions");
                                                            }
                                                            writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                            xmlWriter.writeEndElement();
                                                       
                                                }

                                   }
                             } else {
                                 
                                         // write the null attribute
                                        // write null attribute
                                            java.lang.String namespace2 = "http://soap.ws.grouper.middleware.internet2.edu/xsd";
                                            if (! namespace2.equals("")) {
                                                java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                if (prefix2 == null) {
                                                    prefix2 = generatePrefix(namespace2);

                                                    xmlWriter.writeStartElement(prefix2,"actions", namespace2);
                                                    xmlWriter.writeNamespace(prefix2, namespace2);
                                                    xmlWriter.setPrefix(prefix2, namespace2);

                                                } else {
                                                    xmlWriter.writeStartElement(namespace2,"actions");
                                                }

                                            } else {
                                                xmlWriter.writeStartElement("actions");
                                            }

                                           // write the nil attribute
                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                           xmlWriter.writeEndElement();
                                    
                             }

                        } if (localActAsSubjectLookupTracker){
                                    if (localActAsSubjectLookup==null){

                                            java.lang.String namespace2 = "http://soap.ws.grouper.middleware.internet2.edu/xsd";

                                        if (! namespace2.equals("")) {
                                            java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                            if (prefix2 == null) {
                                                prefix2 = generatePrefix(namespace2);

                                                xmlWriter.writeStartElement(prefix2,"actAsSubjectLookup", namespace2);
                                                xmlWriter.writeNamespace(prefix2, namespace2);
                                                xmlWriter.setPrefix(prefix2, namespace2);

                                            } else {
                                                xmlWriter.writeStartElement(namespace2,"actAsSubjectLookup");
                                            }

                                        } else {
                                            xmlWriter.writeStartElement("actAsSubjectLookup");
                                        }


                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localActAsSubjectLookup.serialize(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","actAsSubjectLookup"),
                                        factory,xmlWriter);
                                    }
                                } if (localIncludeSubjectDetailTracker){
                                    namespace = "http://soap.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"includeSubjectDetail", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"includeSubjectDetail");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("includeSubjectDetail");
                                    }
                                

                                          if (localIncludeSubjectDetail==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localIncludeSubjectDetail);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSubjectAttributeNamesTracker){
                             if (localSubjectAttributeNames!=null) {
                                   namespace = "http://soap.ws.grouper.middleware.internet2.edu/xsd";
                                   boolean emptyNamespace = namespace == null || namespace.length() == 0;
                                   prefix =  emptyNamespace ? null : xmlWriter.getPrefix(namespace);
                                   for (int i = 0;i < localSubjectAttributeNames.length;i++){
                                        
                                            if (localSubjectAttributeNames[i] != null){
                                        
                                                if (!emptyNamespace) {
                                                    if (prefix == null) {
                                                        java.lang.String prefix2 = generatePrefix(namespace);

                                                        xmlWriter.writeStartElement(prefix2,"subjectAttributeNames", namespace);
                                                        xmlWriter.writeNamespace(prefix2, namespace);
                                                        xmlWriter.setPrefix(prefix2, namespace);

                                                    } else {
                                                        xmlWriter.writeStartElement(namespace,"subjectAttributeNames");
                                                    }

                                                } else {
                                                    xmlWriter.writeStartElement("subjectAttributeNames");
                                                }

                                            
                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSubjectAttributeNames[i]));
                                                    
                                                xmlWriter.writeEndElement();
                                              
                                                } else {
                                                   
                                                           // write null attribute
                                                            namespace = "http://soap.ws.grouper.middleware.internet2.edu/xsd";
                                                            if (! namespace.equals("")) {
                                                                prefix = xmlWriter.getPrefix(namespace);

                                                                if (prefix == null) {
                                                                    prefix = generatePrefix(namespace);

                                                                    xmlWriter.writeStartElement(prefix,"subjectAttributeNames", namespace);
                                                                    xmlWriter.writeNamespace(prefix, namespace);
                                                                    xmlWriter.setPrefix(prefix, namespace);

                                                                } else {
                                                                    xmlWriter.writeStartElement(namespace,"subjectAttributeNames");
                                                                }

                                                            } else {
                                                                xmlWriter.writeStartElement("subjectAttributeNames");
                                                            }
                                                            writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                            xmlWriter.writeEndElement();
                                                       
                                                }

                                   }
                             } else {
                                 
                                         // write the null attribute
                                        // write null attribute
                                            java.lang.String namespace2 = "http://soap.ws.grouper.middleware.internet2.edu/xsd";
                                            if (! namespace2.equals("")) {
                                                java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                if (prefix2 == null) {
                                                    prefix2 = generatePrefix(namespace2);

                                                    xmlWriter.writeStartElement(prefix2,"subjectAttributeNames", namespace2);
                                                    xmlWriter.writeNamespace(prefix2, namespace2);
                                                    xmlWriter.setPrefix(prefix2, namespace2);

                                                } else {
                                                    xmlWriter.writeStartElement(namespace2,"subjectAttributeNames");
                                                }

                                            } else {
                                                xmlWriter.writeStartElement("subjectAttributeNames");
                                            }

                                           // write the nil attribute
                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                           xmlWriter.writeEndElement();
                                    
                             }

                        } if (localIncludeGroupDetailTracker){
                                    namespace = "http://soap.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"includeGroupDetail", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"includeGroupDetail");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("includeGroupDetail");
                                    }
                                

                                          if (localIncludeGroupDetail==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localIncludeGroupDetail);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localParamsTracker){
                                       if (localParams!=null){
                                            for (int i = 0;i < localParams.length;i++){
                                                if (localParams[i] != null){
                                                 localParams[i].serialize(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","params"),
                                                           factory,xmlWriter);
                                                } else {
                                                   
                                                            // write null attribute
                                                            java.lang.String namespace2 = "http://soap.ws.grouper.middleware.internet2.edu/xsd";
                                                            if (! namespace2.equals("")) {
                                                                java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                                if (prefix2 == null) {
                                                                    prefix2 = generatePrefix(namespace2);

                                                                    xmlWriter.writeStartElement(prefix2,"params", namespace2);
                                                                    xmlWriter.writeNamespace(prefix2, namespace2);
                                                                    xmlWriter.setPrefix(prefix2, namespace2);

                                                                } else {
                                                                    xmlWriter.writeStartElement(namespace2,"params");
                                                                }

                                                            } else {
                                                                xmlWriter.writeStartElement("params");
                                                            }

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                // write null attribute
                                                java.lang.String namespace2 = "http://soap.ws.grouper.middleware.internet2.edu/xsd";
                                                if (! namespace2.equals("")) {
                                                    java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                    if (prefix2 == null) {
                                                        prefix2 = generatePrefix(namespace2);

                                                        xmlWriter.writeStartElement(prefix2,"params", namespace2);
                                                        xmlWriter.writeNamespace(prefix2, namespace2);
                                                        xmlWriter.setPrefix(prefix2, namespace2);

                                                    } else {
                                                        xmlWriter.writeStartElement(namespace2,"params");
                                                    }

                                                } else {
                                                    xmlWriter.writeStartElement("params");
                                                }

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localAttributeDefsToReplaceTracker){
                                       if (localAttributeDefsToReplace!=null){
                                            for (int i = 0;i < localAttributeDefsToReplace.length;i++){
                                                if (localAttributeDefsToReplace[i] != null){
                                                 localAttributeDefsToReplace[i].serialize(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","attributeDefsToReplace"),
                                                           factory,xmlWriter);
                                                } else {
                                                   
                                                            // write null attribute
                                                            java.lang.String namespace2 = "http://soap.ws.grouper.middleware.internet2.edu/xsd";
                                                            if (! namespace2.equals("")) {
                                                                java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                                if (prefix2 == null) {
                                                                    prefix2 = generatePrefix(namespace2);

                                                                    xmlWriter.writeStartElement(prefix2,"attributeDefsToReplace", namespace2);
                                                                    xmlWriter.writeNamespace(prefix2, namespace2);
                                                                    xmlWriter.setPrefix(prefix2, namespace2);

                                                                } else {
                                                                    xmlWriter.writeStartElement(namespace2,"attributeDefsToReplace");
                                                                }

                                                            } else {
                                                                xmlWriter.writeStartElement("attributeDefsToReplace");
                                                            }

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                // write null attribute
                                                java.lang.String namespace2 = "http://soap.ws.grouper.middleware.internet2.edu/xsd";
                                                if (! namespace2.equals("")) {
                                                    java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                    if (prefix2 == null) {
                                                        prefix2 = generatePrefix(namespace2);

                                                        xmlWriter.writeStartElement(prefix2,"attributeDefsToReplace", namespace2);
                                                        xmlWriter.writeNamespace(prefix2, namespace2);
                                                        xmlWriter.setPrefix(prefix2, namespace2);

                                                    } else {
                                                        xmlWriter.writeStartElement(namespace2,"attributeDefsToReplace");
                                                    }

                                                } else {
                                                    xmlWriter.writeStartElement("attributeDefsToReplace");
                                                }

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localActionsToReplaceTracker){
                             if (localActionsToReplace!=null) {
                                   namespace = "http://soap.ws.grouper.middleware.internet2.edu/xsd";
                                   boolean emptyNamespace = namespace == null || namespace.length() == 0;
                                   prefix =  emptyNamespace ? null : xmlWriter.getPrefix(namespace);
                                   for (int i = 0;i < localActionsToReplace.length;i++){
                                        
                                            if (localActionsToReplace[i] != null){
                                        
                                                if (!emptyNamespace) {
                                                    if (prefix == null) {
                                                        java.lang.String prefix2 = generatePrefix(namespace);

                                                        xmlWriter.writeStartElement(prefix2,"actionsToReplace", namespace);
                                                        xmlWriter.writeNamespace(prefix2, namespace);
                                                        xmlWriter.setPrefix(prefix2, namespace);

                                                    } else {
                                                        xmlWriter.writeStartElement(namespace,"actionsToReplace");
                                                    }

                                                } else {
                                                    xmlWriter.writeStartElement("actionsToReplace");
                                                }

                                            
                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localActionsToReplace[i]));
                                                    
                                                xmlWriter.writeEndElement();
                                              
                                                } else {
                                                   
                                                           // write null attribute
                                                            namespace = "http://soap.ws.grouper.middleware.internet2.edu/xsd";
                                                            if (! namespace.equals("")) {
                                                                prefix = xmlWriter.getPrefix(namespace);

                                                                if (prefix == null) {
                                                                    prefix = generatePrefix(namespace);

                                                                    xmlWriter.writeStartElement(prefix,"actionsToReplace", namespace);
                                                                    xmlWriter.writeNamespace(prefix, namespace);
                                                                    xmlWriter.setPrefix(prefix, namespace);

                                                                } else {
                                                                    xmlWriter.writeStartElement(namespace,"actionsToReplace");
                                                                }

                                                            } else {
                                                                xmlWriter.writeStartElement("actionsToReplace");
                                                            }
                                                            writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                            xmlWriter.writeEndElement();
                                                       
                                                }

                                   }
                             } else {
                                 
                                         // write the null attribute
                                        // write null attribute
                                            java.lang.String namespace2 = "http://soap.ws.grouper.middleware.internet2.edu/xsd";
                                            if (! namespace2.equals("")) {
                                                java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                if (prefix2 == null) {
                                                    prefix2 = generatePrefix(namespace2);

                                                    xmlWriter.writeStartElement(prefix2,"actionsToReplace", namespace2);
                                                    xmlWriter.writeNamespace(prefix2, namespace2);
                                                    xmlWriter.setPrefix(prefix2, namespace2);

                                                } else {
                                                    xmlWriter.writeStartElement(namespace2,"actionsToReplace");
                                                }

                                            } else {
                                                xmlWriter.writeStartElement("actionsToReplace");
                                            }

                                           // write the nil attribute
                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                           xmlWriter.writeEndElement();
                                    
                             }

                        } if (localDisallowedTracker){
                                    namespace = "http://soap.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"disallowed", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"disallowed");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("disallowed");
                                    }
                                

                                          if (localDisallowed==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localDisallowed);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                    xmlWriter.writeEndElement();
               

        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

        /**
          * Util method to write an attribute without the ns prefix
          */
          private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
                if (namespace.equals(""))
              {
                  xmlWriter.writeAttribute(attName,attValue);
              }
              else
              {
                  registerPrefix(xmlWriter, namespace);
                  xmlWriter.writeAttribute(namespace,attName,attValue);
              }
          }


           /**
             * Util method to write an attribute without the ns prefix
             */
            private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                             javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

                java.lang.String attributeNamespace = qname.getNamespaceURI();
                java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
                if (attributePrefix == null) {
                    attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
                }
                java.lang.String attributeValue;
                if (attributePrefix.trim().length() > 0) {
                    attributeValue = attributePrefix + ":" + qname.getLocalPart();
                } else {
                    attributeValue = qname.getLocalPart();
                }

                if (namespace.equals("")) {
                    xmlWriter.writeAttribute(attName, attributeValue);
                } else {
                    registerPrefix(xmlWriter, namespace);
                    xmlWriter.writeAttribute(namespace, attName, attributeValue);
                }
            }
        /**
         *  method to handle Qnames
         */

        private void writeQName(javax.xml.namespace.QName qname,
                                javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();
            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix,namespaceURI);
                }

                if (prefix.trim().length() > 0){
                    xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                }

            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

            if (qnames != null) {
                // we have to store this data until last moment since it is not possible to write any
                // namespace data after writing the charactor data
                java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                java.lang.String namespaceURI = null;
                java.lang.String prefix = null;

                for (int i = 0; i < qnames.length; i++) {
                    if (i > 0) {
                        stringToWrite.append(" ");
                    }
                    namespaceURI = qnames[i].getNamespaceURI();
                    if (namespaceURI != null) {
                        prefix = xmlWriter.getPrefix(namespaceURI);
                        if ((prefix == null) || (prefix.length() == 0)) {
                            prefix = generatePrefix(namespaceURI);
                            xmlWriter.writeNamespace(prefix, namespaceURI);
                            xmlWriter.setPrefix(prefix,namespaceURI);
                        }

                        if (prefix.trim().length() > 0){
                            stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                    }
                }
                xmlWriter.writeCharacters(stringToWrite.toString());
            }

        }


         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = generatePrefix(namespace);

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }


  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                    throws org.apache.axis2.databinding.ADBException{


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                 if (localClientVersionTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "clientVersion"));
                                 
                                         elementList.add(localClientVersion==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localClientVersion));
                                    } if (localPermissionTypeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "permissionType"));
                                 
                                         elementList.add(localPermissionType==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPermissionType));
                                    } if (localPermissionDefNameLookupsTracker){
                             if (localPermissionDefNameLookups!=null) {
                                 for (int i = 0;i < localPermissionDefNameLookups.length;i++){

                                    if (localPermissionDefNameLookups[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "permissionDefNameLookups"));
                                         elementList.add(localPermissionDefNameLookups[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "permissionDefNameLookups"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "permissionDefNameLookups"));
                                        elementList.add(localPermissionDefNameLookups);
                                    
                             }

                        } if (localPermissionAssignOperationTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "permissionAssignOperation"));
                                 
                                         elementList.add(localPermissionAssignOperation==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPermissionAssignOperation));
                                    } if (localAssignmentNotesTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "assignmentNotes"));
                                 
                                         elementList.add(localAssignmentNotes==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAssignmentNotes));
                                    } if (localAssignmentEnabledTimeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "assignmentEnabledTime"));
                                 
                                         elementList.add(localAssignmentEnabledTime==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAssignmentEnabledTime));
                                    } if (localAssignmentDisabledTimeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "assignmentDisabledTime"));
                                 
                                         elementList.add(localAssignmentDisabledTime==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAssignmentDisabledTime));
                                    } if (localDelegatableTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "delegatable"));
                                 
                                         elementList.add(localDelegatable==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDelegatable));
                                    } if (localWsAttributeAssignLookupsTracker){
                             if (localWsAttributeAssignLookups!=null) {
                                 for (int i = 0;i < localWsAttributeAssignLookups.length;i++){

                                    if (localWsAttributeAssignLookups[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAttributeAssignLookups"));
                                         elementList.add(localWsAttributeAssignLookups[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAttributeAssignLookups"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAttributeAssignLookups"));
                                        elementList.add(localWsAttributeAssignLookups);
                                    
                             }

                        } if (localRoleLookupsTracker){
                             if (localRoleLookups!=null) {
                                 for (int i = 0;i < localRoleLookups.length;i++){

                                    if (localRoleLookups[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "roleLookups"));
                                         elementList.add(localRoleLookups[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "roleLookups"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "roleLookups"));
                                        elementList.add(localRoleLookups);
                                    
                             }

                        } if (localSubjectRoleLookupsTracker){
                             if (localSubjectRoleLookups!=null) {
                                 for (int i = 0;i < localSubjectRoleLookups.length;i++){

                                    if (localSubjectRoleLookups[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "subjectRoleLookups"));
                                         elementList.add(localSubjectRoleLookups[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "subjectRoleLookups"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "subjectRoleLookups"));
                                        elementList.add(localSubjectRoleLookups);
                                    
                             }

                        } if (localActionsTracker){
                            if (localActions!=null){
                                  for (int i = 0;i < localActions.length;i++){
                                      
                                         if (localActions[i] != null){
                                          elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "actions"));
                                          elementList.add(
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localActions[i]));
                                          } else {
                                             
                                                    elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "actions"));
                                                    elementList.add(null);
                                                
                                          }
                                      

                                  }
                            } else {
                              
                                    elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "actions"));
                                    elementList.add(null);
                                
                            }

                        } if (localActAsSubjectLookupTracker){
                            elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "actAsSubjectLookup"));
                            
                            
                                    elementList.add(localActAsSubjectLookup==null?null:
                                    localActAsSubjectLookup);
                                } if (localIncludeSubjectDetailTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "includeSubjectDetail"));
                                 
                                         elementList.add(localIncludeSubjectDetail==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIncludeSubjectDetail));
                                    } if (localSubjectAttributeNamesTracker){
                            if (localSubjectAttributeNames!=null){
                                  for (int i = 0;i < localSubjectAttributeNames.length;i++){
                                      
                                         if (localSubjectAttributeNames[i] != null){
                                          elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "subjectAttributeNames"));
                                          elementList.add(
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSubjectAttributeNames[i]));
                                          } else {
                                             
                                                    elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "subjectAttributeNames"));
                                                    elementList.add(null);
                                                
                                          }
                                      

                                  }
                            } else {
                              
                                    elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "subjectAttributeNames"));
                                    elementList.add(null);
                                
                            }

                        } if (localIncludeGroupDetailTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "includeGroupDetail"));
                                 
                                         elementList.add(localIncludeGroupDetail==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIncludeGroupDetail));
                                    } if (localParamsTracker){
                             if (localParams!=null) {
                                 for (int i = 0;i < localParams.length;i++){

                                    if (localParams[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "params"));
                                         elementList.add(localParams[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "params"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "params"));
                                        elementList.add(localParams);
                                    
                             }

                        } if (localAttributeDefsToReplaceTracker){
                             if (localAttributeDefsToReplace!=null) {
                                 for (int i = 0;i < localAttributeDefsToReplace.length;i++){

                                    if (localAttributeDefsToReplace[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "attributeDefsToReplace"));
                                         elementList.add(localAttributeDefsToReplace[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "attributeDefsToReplace"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "attributeDefsToReplace"));
                                        elementList.add(localAttributeDefsToReplace);
                                    
                             }

                        } if (localActionsToReplaceTracker){
                            if (localActionsToReplace!=null){
                                  for (int i = 0;i < localActionsToReplace.length;i++){
                                      
                                         if (localActionsToReplace[i] != null){
                                          elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "actionsToReplace"));
                                          elementList.add(
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localActionsToReplace[i]));
                                          } else {
                                             
                                                    elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "actionsToReplace"));
                                                    elementList.add(null);
                                                
                                          }
                                      

                                  }
                            } else {
                              
                                    elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "actionsToReplace"));
                                    elementList.add(null);
                                
                            }

                        } if (localDisallowedTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "disallowed"));
                                 
                                         elementList.add(localDisallowed==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDisallowed));
                                    }

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{

        
        

        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static AssignPermissions parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            AssignPermissions object =
                new AssignPermissions();

            int event;
            java.lang.String nillableValue = null;
            java.lang.String prefix ="";
            java.lang.String namespaceuri ="";
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = null;
                    if (fullTypeName.indexOf(":") > -1){
                        nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    }
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    
                            if (!"assignPermissions".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (AssignPermissions)edu.internet2.middleware.grouper.ws.soap.xsd.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                 
                    
                    reader.next();
                
                        java.util.ArrayList list3 = new java.util.ArrayList();
                    
                        java.util.ArrayList list9 = new java.util.ArrayList();
                    
                        java.util.ArrayList list10 = new java.util.ArrayList();
                    
                        java.util.ArrayList list11 = new java.util.ArrayList();
                    
                        java.util.ArrayList list12 = new java.util.ArrayList();
                    
                        java.util.ArrayList list15 = new java.util.ArrayList();
                    
                        java.util.ArrayList list17 = new java.util.ArrayList();
                    
                        java.util.ArrayList list18 = new java.util.ArrayList();
                    
                        java.util.ArrayList list19 = new java.util.ArrayList();
                    
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","clientVersion").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setClientVersion(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","permissionType").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPermissionType(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","permissionDefNameLookups").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list3.add(null);
                                                              reader.next();
                                                          } else {
                                                        list3.add(edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeDefNameLookup.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone3 = false;
                                                        while(!loopDone3){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone3 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","permissionDefNameLookups").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list3.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list3.add(edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeDefNameLookup.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone3 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setPermissionDefNameLookups((edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeDefNameLookup[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeDefNameLookup.class,
                                                                list3));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","permissionAssignOperation").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPermissionAssignOperation(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","assignmentNotes").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAssignmentNotes(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","assignmentEnabledTime").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAssignmentEnabledTime(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","assignmentDisabledTime").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAssignmentDisabledTime(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","delegatable").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDelegatable(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","wsAttributeAssignLookups").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list9.add(null);
                                                              reader.next();
                                                          } else {
                                                        list9.add(edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeAssignLookup.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone9 = false;
                                                        while(!loopDone9){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone9 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","wsAttributeAssignLookups").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list9.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list9.add(edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeAssignLookup.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone9 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setWsAttributeAssignLookups((edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeAssignLookup[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeAssignLookup.class,
                                                                list9));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","roleLookups").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list10.add(null);
                                                              reader.next();
                                                          } else {
                                                        list10.add(edu.internet2.middleware.grouper.ws.soap.xsd.WsGroupLookup.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone10 = false;
                                                        while(!loopDone10){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone10 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","roleLookups").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list10.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list10.add(edu.internet2.middleware.grouper.ws.soap.xsd.WsGroupLookup.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone10 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setRoleLookups((edu.internet2.middleware.grouper.ws.soap.xsd.WsGroupLookup[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap.xsd.WsGroupLookup.class,
                                                                list10));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","subjectRoleLookups").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list11.add(null);
                                                              reader.next();
                                                          } else {
                                                        list11.add(edu.internet2.middleware.grouper.ws.soap.xsd.WsMembershipAnyLookup.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone11 = false;
                                                        while(!loopDone11){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone11 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","subjectRoleLookups").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list11.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list11.add(edu.internet2.middleware.grouper.ws.soap.xsd.WsMembershipAnyLookup.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone11 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setSubjectRoleLookups((edu.internet2.middleware.grouper.ws.soap.xsd.WsMembershipAnyLookup[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap.xsd.WsMembershipAnyLookup.class,
                                                                list11));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","actions").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                              nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                              if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                  list12.add(null);
                                                       
                                                  reader.next();
                                              } else {
                                            list12.add(reader.getElementText());
                                            }
                                            //loop until we find a start element that is not part of this array
                                            boolean loopDone12 = false;
                                            while(!loopDone12){
                                                // Ensure we are at the EndElement
                                                while (!reader.isEndElement()){
                                                    reader.next();
                                                }
                                                // Step out of this element
                                                reader.next();
                                                // Step to next element event.
                                                while (!reader.isStartElement() && !reader.isEndElement())
                                                    reader.next();
                                                if (reader.isEndElement()){
                                                    //two continuous end elements means we are exiting the xml structure
                                                    loopDone12 = true;
                                                } else {
                                                    if (new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","actions").equals(reader.getName())){
                                                         
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list12.add(null);
                                                                   
                                                              reader.next();
                                                          } else {
                                                        list12.add(reader.getElementText());
                                                        }
                                                    }else{
                                                        loopDone12 = true;
                                                    }
                                                }
                                            }
                                            // call the converter utility  to convert and set the array
                                            
                                                    object.setActions((java.lang.String[])
                                                        list12.toArray(new java.lang.String[list12.size()]));
                                                
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","actAsSubjectLookup").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setActAsSubjectLookup(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setActAsSubjectLookup(edu.internet2.middleware.grouper.ws.soap.xsd.WsSubjectLookup.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","includeSubjectDetail").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIncludeSubjectDetail(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","subjectAttributeNames").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                              nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                              if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                  list15.add(null);
                                                       
                                                  reader.next();
                                              } else {
                                            list15.add(reader.getElementText());
                                            }
                                            //loop until we find a start element that is not part of this array
                                            boolean loopDone15 = false;
                                            while(!loopDone15){
                                                // Ensure we are at the EndElement
                                                while (!reader.isEndElement()){
                                                    reader.next();
                                                }
                                                // Step out of this element
                                                reader.next();
                                                // Step to next element event.
                                                while (!reader.isStartElement() && !reader.isEndElement())
                                                    reader.next();
                                                if (reader.isEndElement()){
                                                    //two continuous end elements means we are exiting the xml structure
                                                    loopDone15 = true;
                                                } else {
                                                    if (new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","subjectAttributeNames").equals(reader.getName())){
                                                         
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list15.add(null);
                                                                   
                                                              reader.next();
                                                          } else {
                                                        list15.add(reader.getElementText());
                                                        }
                                                    }else{
                                                        loopDone15 = true;
                                                    }
                                                }
                                            }
                                            // call the converter utility  to convert and set the array
                                            
                                                    object.setSubjectAttributeNames((java.lang.String[])
                                                        list15.toArray(new java.lang.String[list15.size()]));
                                                
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","includeGroupDetail").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIncludeGroupDetail(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","params").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list17.add(null);
                                                              reader.next();
                                                          } else {
                                                        list17.add(edu.internet2.middleware.grouper.ws.soap.xsd.WsParam.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone17 = false;
                                                        while(!loopDone17){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone17 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","params").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list17.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list17.add(edu.internet2.middleware.grouper.ws.soap.xsd.WsParam.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone17 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setParams((edu.internet2.middleware.grouper.ws.soap.xsd.WsParam[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap.xsd.WsParam.class,
                                                                list17));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","attributeDefsToReplace").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list18.add(null);
                                                              reader.next();
                                                          } else {
                                                        list18.add(edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeDefLookup.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone18 = false;
                                                        while(!loopDone18){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone18 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","attributeDefsToReplace").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list18.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list18.add(edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeDefLookup.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone18 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setAttributeDefsToReplace((edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeDefLookup[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap.xsd.WsAttributeDefLookup.class,
                                                                list18));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","actionsToReplace").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                              nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                              if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                  list19.add(null);
                                                       
                                                  reader.next();
                                              } else {
                                            list19.add(reader.getElementText());
                                            }
                                            //loop until we find a start element that is not part of this array
                                            boolean loopDone19 = false;
                                            while(!loopDone19){
                                                // Ensure we are at the EndElement
                                                while (!reader.isEndElement()){
                                                    reader.next();
                                                }
                                                // Step out of this element
                                                reader.next();
                                                // Step to next element event.
                                                while (!reader.isStartElement() && !reader.isEndElement())
                                                    reader.next();
                                                if (reader.isEndElement()){
                                                    //two continuous end elements means we are exiting the xml structure
                                                    loopDone19 = true;
                                                } else {
                                                    if (new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","actionsToReplace").equals(reader.getName())){
                                                         
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list19.add(null);
                                                                   
                                                              reader.next();
                                                          } else {
                                                        list19.add(reader.getElementText());
                                                        }
                                                    }else{
                                                        loopDone19 = true;
                                                    }
                                                }
                                            }
                                            // call the converter utility  to convert and set the array
                                            
                                                    object.setActionsToReplace((java.lang.String[])
                                                        list19.toArray(new java.lang.String[list19.size()]));
                                                
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap.ws.grouper.middleware.internet2.edu/xsd","disallowed").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDisallowed(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                  
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            
                                if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
                            



            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
          