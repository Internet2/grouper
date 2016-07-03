/*******************************************************************************
 * Copyright 2016 Internet2
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
 *******************************************************************************/

/**
 * AssignPermissions.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1  Built on : Aug 31, 2011 (12:23:23 CEST)
 */

            
                package edu.internet2.middleware.grouper.ws.soap_v2_3.xsd;
            

            /**
            *  AssignPermissions bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class AssignPermissions
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                "assignPermissions",
                "ns1");

            

                        /**
                        * field for ClientVersion
                        */

                        
                                    protected java.lang.String localClientVersion ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localClientVersionTracker = false ;

                           public boolean isClientVersionSpecified(){
                               return localClientVersionTracker;
                           }

                           

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
                            localClientVersionTracker = true;
                                   
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

                           public boolean isPermissionTypeSpecified(){
                               return localPermissionTypeTracker;
                           }

                           

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
                            localPermissionTypeTracker = true;
                                   
                                            this.localPermissionType=param;
                                    

                               }
                            

                        /**
                        * field for PermissionDefNameLookups
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefNameLookup[] localPermissionDefNameLookups ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPermissionDefNameLookupsTracker = false ;

                           public boolean isPermissionDefNameLookupsSpecified(){
                               return localPermissionDefNameLookupsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefNameLookup[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefNameLookup[] getPermissionDefNameLookups(){
                               return localPermissionDefNameLookups;
                           }

                           
                        


                               
                              /**
                               * validate the array for PermissionDefNameLookups
                               */
                              protected void validatePermissionDefNameLookups(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefNameLookup[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param PermissionDefNameLookups
                              */
                              public void setPermissionDefNameLookups(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefNameLookup[] param){
                              
                                   validatePermissionDefNameLookups(param);

                               localPermissionDefNameLookupsTracker = true;
                                      
                                      this.localPermissionDefNameLookups=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefNameLookup
                             */
                             public void addPermissionDefNameLookups(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefNameLookup param){
                                   if (localPermissionDefNameLookups == null){
                                   localPermissionDefNameLookups = new edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefNameLookup[]{};
                                   }

                            
                                 //update the setting tracker
                                localPermissionDefNameLookupsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localPermissionDefNameLookups);
                               list.add(param);
                               this.localPermissionDefNameLookups =
                             (edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefNameLookup[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefNameLookup[list.size()]);

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

                           public boolean isPermissionAssignOperationSpecified(){
                               return localPermissionAssignOperationTracker;
                           }

                           

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
                            localPermissionAssignOperationTracker = true;
                                   
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

                           public boolean isAssignmentNotesSpecified(){
                               return localAssignmentNotesTracker;
                           }

                           

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
                            localAssignmentNotesTracker = true;
                                   
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

                           public boolean isAssignmentEnabledTimeSpecified(){
                               return localAssignmentEnabledTimeTracker;
                           }

                           

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
                            localAssignmentEnabledTimeTracker = true;
                                   
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

                           public boolean isAssignmentDisabledTimeSpecified(){
                               return localAssignmentDisabledTimeTracker;
                           }

                           

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
                            localAssignmentDisabledTimeTracker = true;
                                   
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

                           public boolean isDelegatableSpecified(){
                               return localDelegatableTracker;
                           }

                           

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
                            localDelegatableTracker = true;
                                   
                                            this.localDelegatable=param;
                                    

                               }
                            

                        /**
                        * field for WsAttributeAssignLookups
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeAssignLookup[] localWsAttributeAssignLookups ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsAttributeAssignLookupsTracker = false ;

                           public boolean isWsAttributeAssignLookupsSpecified(){
                               return localWsAttributeAssignLookupsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeAssignLookup[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeAssignLookup[] getWsAttributeAssignLookups(){
                               return localWsAttributeAssignLookups;
                           }

                           
                        


                               
                              /**
                               * validate the array for WsAttributeAssignLookups
                               */
                              protected void validateWsAttributeAssignLookups(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeAssignLookup[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param WsAttributeAssignLookups
                              */
                              public void setWsAttributeAssignLookups(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeAssignLookup[] param){
                              
                                   validateWsAttributeAssignLookups(param);

                               localWsAttributeAssignLookupsTracker = true;
                                      
                                      this.localWsAttributeAssignLookups=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeAssignLookup
                             */
                             public void addWsAttributeAssignLookups(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeAssignLookup param){
                                   if (localWsAttributeAssignLookups == null){
                                   localWsAttributeAssignLookups = new edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeAssignLookup[]{};
                                   }

                            
                                 //update the setting tracker
                                localWsAttributeAssignLookupsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localWsAttributeAssignLookups);
                               list.add(param);
                               this.localWsAttributeAssignLookups =
                             (edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeAssignLookup[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeAssignLookup[list.size()]);

                             }
                             

                        /**
                        * field for RoleLookups
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsGroupLookup[] localRoleLookups ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localRoleLookupsTracker = false ;

                           public boolean isRoleLookupsSpecified(){
                               return localRoleLookupsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsGroupLookup[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsGroupLookup[] getRoleLookups(){
                               return localRoleLookups;
                           }

                           
                        


                               
                              /**
                               * validate the array for RoleLookups
                               */
                              protected void validateRoleLookups(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsGroupLookup[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param RoleLookups
                              */
                              public void setRoleLookups(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsGroupLookup[] param){
                              
                                   validateRoleLookups(param);

                               localRoleLookupsTracker = true;
                                      
                                      this.localRoleLookups=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsGroupLookup
                             */
                             public void addRoleLookups(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsGroupLookup param){
                                   if (localRoleLookups == null){
                                   localRoleLookups = new edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsGroupLookup[]{};
                                   }

                            
                                 //update the setting tracker
                                localRoleLookupsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localRoleLookups);
                               list.add(param);
                               this.localRoleLookups =
                             (edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsGroupLookup[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsGroupLookup[list.size()]);

                             }
                             

                        /**
                        * field for SubjectRoleLookups
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsMembershipAnyLookup[] localSubjectRoleLookups ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSubjectRoleLookupsTracker = false ;

                           public boolean isSubjectRoleLookupsSpecified(){
                               return localSubjectRoleLookupsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsMembershipAnyLookup[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsMembershipAnyLookup[] getSubjectRoleLookups(){
                               return localSubjectRoleLookups;
                           }

                           
                        


                               
                              /**
                               * validate the array for SubjectRoleLookups
                               */
                              protected void validateSubjectRoleLookups(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsMembershipAnyLookup[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param SubjectRoleLookups
                              */
                              public void setSubjectRoleLookups(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsMembershipAnyLookup[] param){
                              
                                   validateSubjectRoleLookups(param);

                               localSubjectRoleLookupsTracker = true;
                                      
                                      this.localSubjectRoleLookups=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsMembershipAnyLookup
                             */
                             public void addSubjectRoleLookups(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsMembershipAnyLookup param){
                                   if (localSubjectRoleLookups == null){
                                   localSubjectRoleLookups = new edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsMembershipAnyLookup[]{};
                                   }

                            
                                 //update the setting tracker
                                localSubjectRoleLookupsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localSubjectRoleLookups);
                               list.add(param);
                               this.localSubjectRoleLookups =
                             (edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsMembershipAnyLookup[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsMembershipAnyLookup[list.size()]);

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

                           public boolean isActionsSpecified(){
                               return localActionsTracker;
                           }

                           

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

                               localActionsTracker = true;
                                      
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

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsSubjectLookup localActAsSubjectLookup ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localActAsSubjectLookupTracker = false ;

                           public boolean isActAsSubjectLookupSpecified(){
                               return localActAsSubjectLookupTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsSubjectLookup
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsSubjectLookup getActAsSubjectLookup(){
                               return localActAsSubjectLookup;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ActAsSubjectLookup
                               */
                               public void setActAsSubjectLookup(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsSubjectLookup param){
                            localActAsSubjectLookupTracker = true;
                                   
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

                           public boolean isIncludeSubjectDetailSpecified(){
                               return localIncludeSubjectDetailTracker;
                           }

                           

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
                            localIncludeSubjectDetailTracker = true;
                                   
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

                           public boolean isSubjectAttributeNamesSpecified(){
                               return localSubjectAttributeNamesTracker;
                           }

                           

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

                               localSubjectAttributeNamesTracker = true;
                                      
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

                           public boolean isIncludeGroupDetailSpecified(){
                               return localIncludeGroupDetailTracker;
                           }

                           

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
                            localIncludeGroupDetailTracker = true;
                                   
                                            this.localIncludeGroupDetail=param;
                                    

                               }
                            

                        /**
                        * field for Params
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsParam[] localParams ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localParamsTracker = false ;

                           public boolean isParamsSpecified(){
                               return localParamsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsParam[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsParam[] getParams(){
                               return localParams;
                           }

                           
                        


                               
                              /**
                               * validate the array for Params
                               */
                              protected void validateParams(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsParam[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param Params
                              */
                              public void setParams(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsParam[] param){
                              
                                   validateParams(param);

                               localParamsTracker = true;
                                      
                                      this.localParams=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsParam
                             */
                             public void addParams(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsParam param){
                                   if (localParams == null){
                                   localParams = new edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsParam[]{};
                                   }

                            
                                 //update the setting tracker
                                localParamsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localParams);
                               list.add(param);
                               this.localParams =
                             (edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsParam[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsParam[list.size()]);

                             }
                             

                        /**
                        * field for AttributeDefsToReplace
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefLookup[] localAttributeDefsToReplace ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAttributeDefsToReplaceTracker = false ;

                           public boolean isAttributeDefsToReplaceSpecified(){
                               return localAttributeDefsToReplaceTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefLookup[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefLookup[] getAttributeDefsToReplace(){
                               return localAttributeDefsToReplace;
                           }

                           
                        


                               
                              /**
                               * validate the array for AttributeDefsToReplace
                               */
                              protected void validateAttributeDefsToReplace(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefLookup[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param AttributeDefsToReplace
                              */
                              public void setAttributeDefsToReplace(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefLookup[] param){
                              
                                   validateAttributeDefsToReplace(param);

                               localAttributeDefsToReplaceTracker = true;
                                      
                                      this.localAttributeDefsToReplace=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefLookup
                             */
                             public void addAttributeDefsToReplace(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefLookup param){
                                   if (localAttributeDefsToReplace == null){
                                   localAttributeDefsToReplace = new edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefLookup[]{};
                                   }

                            
                                 //update the setting tracker
                                localAttributeDefsToReplaceTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localAttributeDefsToReplace);
                               list.add(param);
                               this.localAttributeDefsToReplace =
                             (edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefLookup[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefLookup[list.size()]);

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

                           public boolean isActionsToReplaceSpecified(){
                               return localActionsToReplaceTracker;
                           }

                           

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

                               localActionsToReplaceTracker = true;
                                      
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

                           public boolean isDisallowedSpecified(){
                               return localDisallowedTracker;
                           }

                           

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
                            localDisallowedTracker = true;
                                   
                                            this.localDisallowed=param;
                                    

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
                       new org.apache.axis2.databinding.ADBDataSource(this,MY_QNAME);
               return factory.createOMElement(dataSource,MY_QNAME);
            
        }

         public void serialize(final javax.xml.namespace.QName parentQName,
                                       javax.xml.stream.XMLStreamWriter xmlWriter)
                                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                           serialize(parentQName,xmlWriter,false);
         }

         public void serialize(final javax.xml.namespace.QName parentQName,
                               javax.xml.stream.XMLStreamWriter xmlWriter,
                               boolean serializeType)
            throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
            
                


                java.lang.String prefix = null;
                java.lang.String namespace = null;
                

                    prefix = parentQName.getPrefix();
                    namespace = parentQName.getNamespaceURI();
                    writeStartElement(prefix, namespace, parentQName.getLocalPart(), xmlWriter);
                
                  if (serializeType){
               

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd");
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
                                    namespace = "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "clientVersion", xmlWriter);
                             

                                          if (localClientVersion==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localClientVersion);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPermissionTypeTracker){
                                    namespace = "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "permissionType", xmlWriter);
                             

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
                                                 localPermissionDefNameLookups[i].serialize(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","permissionDefNameLookups"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd", "permissionDefNameLookups", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd", "permissionDefNameLookups", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localPermissionAssignOperationTracker){
                                    namespace = "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "permissionAssignOperation", xmlWriter);
                             

                                          if (localPermissionAssignOperation==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPermissionAssignOperation);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAssignmentNotesTracker){
                                    namespace = "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "assignmentNotes", xmlWriter);
                             

                                          if (localAssignmentNotes==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAssignmentNotes);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAssignmentEnabledTimeTracker){
                                    namespace = "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "assignmentEnabledTime", xmlWriter);
                             

                                          if (localAssignmentEnabledTime==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAssignmentEnabledTime);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAssignmentDisabledTimeTracker){
                                    namespace = "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "assignmentDisabledTime", xmlWriter);
                             

                                          if (localAssignmentDisabledTime==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAssignmentDisabledTime);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localDelegatableTracker){
                                    namespace = "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "delegatable", xmlWriter);
                             

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
                                                 localWsAttributeAssignLookups[i].serialize(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","wsAttributeAssignLookups"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd", "wsAttributeAssignLookups", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd", "wsAttributeAssignLookups", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localRoleLookupsTracker){
                                       if (localRoleLookups!=null){
                                            for (int i = 0;i < localRoleLookups.length;i++){
                                                if (localRoleLookups[i] != null){
                                                 localRoleLookups[i].serialize(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","roleLookups"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd", "roleLookups", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd", "roleLookups", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localSubjectRoleLookupsTracker){
                                       if (localSubjectRoleLookups!=null){
                                            for (int i = 0;i < localSubjectRoleLookups.length;i++){
                                                if (localSubjectRoleLookups[i] != null){
                                                 localSubjectRoleLookups[i].serialize(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","subjectRoleLookups"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd", "subjectRoleLookups", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd", "subjectRoleLookups", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localActionsTracker){
                             if (localActions!=null) {
                                   namespace = "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd";
                                   for (int i = 0;i < localActions.length;i++){
                                        
                                            if (localActions[i] != null){
                                        
                                                writeStartElement(null, namespace, "actions", xmlWriter);

                                            
                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localActions[i]));
                                                    
                                                xmlWriter.writeEndElement();
                                              
                                                } else {
                                                   
                                                           // write null attribute
                                                            namespace = "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd";
                                                            writeStartElement(null, namespace, "actions", xmlWriter);
                                                            writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                            xmlWriter.writeEndElement();
                                                       
                                                }

                                   }
                             } else {
                                 
                                         // write the null attribute
                                        // write null attribute
                                           writeStartElement(null, "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd", "actions", xmlWriter);

                                           // write the nil attribute
                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                           xmlWriter.writeEndElement();
                                    
                             }

                        } if (localActAsSubjectLookupTracker){
                                    if (localActAsSubjectLookup==null){

                                        writeStartElement(null, "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd", "actAsSubjectLookup", xmlWriter);

                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localActAsSubjectLookup.serialize(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","actAsSubjectLookup"),
                                        xmlWriter);
                                    }
                                } if (localIncludeSubjectDetailTracker){
                                    namespace = "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "includeSubjectDetail", xmlWriter);
                             

                                          if (localIncludeSubjectDetail==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localIncludeSubjectDetail);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSubjectAttributeNamesTracker){
                             if (localSubjectAttributeNames!=null) {
                                   namespace = "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd";
                                   for (int i = 0;i < localSubjectAttributeNames.length;i++){
                                        
                                            if (localSubjectAttributeNames[i] != null){
                                        
                                                writeStartElement(null, namespace, "subjectAttributeNames", xmlWriter);

                                            
                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSubjectAttributeNames[i]));
                                                    
                                                xmlWriter.writeEndElement();
                                              
                                                } else {
                                                   
                                                           // write null attribute
                                                            namespace = "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd";
                                                            writeStartElement(null, namespace, "subjectAttributeNames", xmlWriter);
                                                            writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                            xmlWriter.writeEndElement();
                                                       
                                                }

                                   }
                             } else {
                                 
                                         // write the null attribute
                                        // write null attribute
                                           writeStartElement(null, "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd", "subjectAttributeNames", xmlWriter);

                                           // write the nil attribute
                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                           xmlWriter.writeEndElement();
                                    
                             }

                        } if (localIncludeGroupDetailTracker){
                                    namespace = "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "includeGroupDetail", xmlWriter);
                             

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
                                                 localParams[i].serialize(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","params"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd", "params", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd", "params", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localAttributeDefsToReplaceTracker){
                                       if (localAttributeDefsToReplace!=null){
                                            for (int i = 0;i < localAttributeDefsToReplace.length;i++){
                                                if (localAttributeDefsToReplace[i] != null){
                                                 localAttributeDefsToReplace[i].serialize(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","attributeDefsToReplace"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd", "attributeDefsToReplace", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd", "attributeDefsToReplace", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localActionsToReplaceTracker){
                             if (localActionsToReplace!=null) {
                                   namespace = "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd";
                                   for (int i = 0;i < localActionsToReplace.length;i++){
                                        
                                            if (localActionsToReplace[i] != null){
                                        
                                                writeStartElement(null, namespace, "actionsToReplace", xmlWriter);

                                            
                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localActionsToReplace[i]));
                                                    
                                                xmlWriter.writeEndElement();
                                              
                                                } else {
                                                   
                                                           // write null attribute
                                                            namespace = "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd";
                                                            writeStartElement(null, namespace, "actionsToReplace", xmlWriter);
                                                            writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                            xmlWriter.writeEndElement();
                                                       
                                                }

                                   }
                             } else {
                                 
                                         // write the null attribute
                                        // write null attribute
                                           writeStartElement(null, "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd", "actionsToReplace", xmlWriter);

                                           // write the nil attribute
                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                           xmlWriter.writeEndElement();
                                    
                             }

                        } if (localDisallowedTracker){
                                    namespace = "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "disallowed", xmlWriter);
                             

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

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd")){
                return "ns1";
            }
            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        /**
         * Utility method to write an element start tag.
         */
        private void writeStartElement(java.lang.String prefix, java.lang.String namespace, java.lang.String localPart,
                                       javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
            if (writerPrefix != null) {
                xmlWriter.writeStartElement(namespace, localPart);
            } else {
                if (namespace.length() == 0) {
                    prefix = "";
                } else if (prefix == null) {
                    prefix = generatePrefix(namespace);
                }

                xmlWriter.writeStartElement(prefix, localPart, namespace);
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
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
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName,attValue);
            } else {
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
                javax.xml.namespace.NamespaceContext nsContext = xmlWriter.getNamespaceContext();
                while (true) {
                    java.lang.String uri = nsContext.getNamespaceURI(prefix);
                    if (uri == null || uri.length() == 0) {
                        break;
                    }
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
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "clientVersion"));
                                 
                                         elementList.add(localClientVersion==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localClientVersion));
                                    } if (localPermissionTypeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "permissionType"));
                                 
                                         elementList.add(localPermissionType==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPermissionType));
                                    } if (localPermissionDefNameLookupsTracker){
                             if (localPermissionDefNameLookups!=null) {
                                 for (int i = 0;i < localPermissionDefNameLookups.length;i++){

                                    if (localPermissionDefNameLookups[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "permissionDefNameLookups"));
                                         elementList.add(localPermissionDefNameLookups[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "permissionDefNameLookups"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "permissionDefNameLookups"));
                                        elementList.add(localPermissionDefNameLookups);
                                    
                             }

                        } if (localPermissionAssignOperationTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "permissionAssignOperation"));
                                 
                                         elementList.add(localPermissionAssignOperation==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPermissionAssignOperation));
                                    } if (localAssignmentNotesTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "assignmentNotes"));
                                 
                                         elementList.add(localAssignmentNotes==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAssignmentNotes));
                                    } if (localAssignmentEnabledTimeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "assignmentEnabledTime"));
                                 
                                         elementList.add(localAssignmentEnabledTime==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAssignmentEnabledTime));
                                    } if (localAssignmentDisabledTimeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "assignmentDisabledTime"));
                                 
                                         elementList.add(localAssignmentDisabledTime==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAssignmentDisabledTime));
                                    } if (localDelegatableTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "delegatable"));
                                 
                                         elementList.add(localDelegatable==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDelegatable));
                                    } if (localWsAttributeAssignLookupsTracker){
                             if (localWsAttributeAssignLookups!=null) {
                                 for (int i = 0;i < localWsAttributeAssignLookups.length;i++){

                                    if (localWsAttributeAssignLookups[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAttributeAssignLookups"));
                                         elementList.add(localWsAttributeAssignLookups[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAttributeAssignLookups"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAttributeAssignLookups"));
                                        elementList.add(localWsAttributeAssignLookups);
                                    
                             }

                        } if (localRoleLookupsTracker){
                             if (localRoleLookups!=null) {
                                 for (int i = 0;i < localRoleLookups.length;i++){

                                    if (localRoleLookups[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "roleLookups"));
                                         elementList.add(localRoleLookups[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "roleLookups"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "roleLookups"));
                                        elementList.add(localRoleLookups);
                                    
                             }

                        } if (localSubjectRoleLookupsTracker){
                             if (localSubjectRoleLookups!=null) {
                                 for (int i = 0;i < localSubjectRoleLookups.length;i++){

                                    if (localSubjectRoleLookups[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "subjectRoleLookups"));
                                         elementList.add(localSubjectRoleLookups[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "subjectRoleLookups"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "subjectRoleLookups"));
                                        elementList.add(localSubjectRoleLookups);
                                    
                             }

                        } if (localActionsTracker){
                            if (localActions!=null){
                                  for (int i = 0;i < localActions.length;i++){
                                      
                                         if (localActions[i] != null){
                                          elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "actions"));
                                          elementList.add(
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localActions[i]));
                                          } else {
                                             
                                                    elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "actions"));
                                                    elementList.add(null);
                                                
                                          }
                                      

                                  }
                            } else {
                              
                                    elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "actions"));
                                    elementList.add(null);
                                
                            }

                        } if (localActAsSubjectLookupTracker){
                            elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "actAsSubjectLookup"));
                            
                            
                                    elementList.add(localActAsSubjectLookup==null?null:
                                    localActAsSubjectLookup);
                                } if (localIncludeSubjectDetailTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "includeSubjectDetail"));
                                 
                                         elementList.add(localIncludeSubjectDetail==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIncludeSubjectDetail));
                                    } if (localSubjectAttributeNamesTracker){
                            if (localSubjectAttributeNames!=null){
                                  for (int i = 0;i < localSubjectAttributeNames.length;i++){
                                      
                                         if (localSubjectAttributeNames[i] != null){
                                          elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "subjectAttributeNames"));
                                          elementList.add(
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSubjectAttributeNames[i]));
                                          } else {
                                             
                                                    elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "subjectAttributeNames"));
                                                    elementList.add(null);
                                                
                                          }
                                      

                                  }
                            } else {
                              
                                    elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "subjectAttributeNames"));
                                    elementList.add(null);
                                
                            }

                        } if (localIncludeGroupDetailTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "includeGroupDetail"));
                                 
                                         elementList.add(localIncludeGroupDetail==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIncludeGroupDetail));
                                    } if (localParamsTracker){
                             if (localParams!=null) {
                                 for (int i = 0;i < localParams.length;i++){

                                    if (localParams[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "params"));
                                         elementList.add(localParams[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "params"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "params"));
                                        elementList.add(localParams);
                                    
                             }

                        } if (localAttributeDefsToReplaceTracker){
                             if (localAttributeDefsToReplace!=null) {
                                 for (int i = 0;i < localAttributeDefsToReplace.length;i++){

                                    if (localAttributeDefsToReplace[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "attributeDefsToReplace"));
                                         elementList.add(localAttributeDefsToReplace[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "attributeDefsToReplace"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "attributeDefsToReplace"));
                                        elementList.add(localAttributeDefsToReplace);
                                    
                             }

                        } if (localActionsToReplaceTracker){
                            if (localActionsToReplace!=null){
                                  for (int i = 0;i < localActionsToReplace.length;i++){
                                      
                                         if (localActionsToReplace[i] != null){
                                          elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "actionsToReplace"));
                                          elementList.add(
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localActionsToReplace[i]));
                                          } else {
                                             
                                                    elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "actionsToReplace"));
                                                    elementList.add(null);
                                                
                                          }
                                      

                                  }
                            } else {
                              
                                    elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "actionsToReplace"));
                                    elementList.add(null);
                                
                            }

                        } if (localDisallowedTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
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
                                return (AssignPermissions)edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.ExtensionMapper.getTypeObject(
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","clientVersion").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","permissionType").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","permissionDefNameLookups").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list3.add(null);
                                                              reader.next();
                                                          } else {
                                                        list3.add(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefNameLookup.Factory.parse(reader));
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
                                                                if (new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","permissionDefNameLookups").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list3.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list3.add(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefNameLookup.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone3 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setPermissionDefNameLookups((edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefNameLookup[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefNameLookup.class,
                                                                list3));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","permissionAssignOperation").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","assignmentNotes").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","assignmentEnabledTime").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","assignmentDisabledTime").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","delegatable").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","wsAttributeAssignLookups").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list9.add(null);
                                                              reader.next();
                                                          } else {
                                                        list9.add(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeAssignLookup.Factory.parse(reader));
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
                                                                if (new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","wsAttributeAssignLookups").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list9.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list9.add(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeAssignLookup.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone9 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setWsAttributeAssignLookups((edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeAssignLookup[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeAssignLookup.class,
                                                                list9));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","roleLookups").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list10.add(null);
                                                              reader.next();
                                                          } else {
                                                        list10.add(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsGroupLookup.Factory.parse(reader));
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
                                                                if (new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","roleLookups").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list10.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list10.add(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsGroupLookup.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone10 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setRoleLookups((edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsGroupLookup[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsGroupLookup.class,
                                                                list10));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","subjectRoleLookups").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list11.add(null);
                                                              reader.next();
                                                          } else {
                                                        list11.add(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsMembershipAnyLookup.Factory.parse(reader));
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
                                                                if (new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","subjectRoleLookups").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list11.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list11.add(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsMembershipAnyLookup.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone11 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setSubjectRoleLookups((edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsMembershipAnyLookup[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsMembershipAnyLookup.class,
                                                                list11));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","actions").equals(reader.getName())){
                                
                                    
                                    
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
                                                    if (new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","actions").equals(reader.getName())){
                                                         
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","actAsSubjectLookup").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setActAsSubjectLookup(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setActAsSubjectLookup(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsSubjectLookup.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","includeSubjectDetail").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","subjectAttributeNames").equals(reader.getName())){
                                
                                    
                                    
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
                                                    if (new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","subjectAttributeNames").equals(reader.getName())){
                                                         
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","includeGroupDetail").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","params").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list17.add(null);
                                                              reader.next();
                                                          } else {
                                                        list17.add(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsParam.Factory.parse(reader));
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
                                                                if (new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","params").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list17.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list17.add(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsParam.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone17 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setParams((edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsParam[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsParam.class,
                                                                list17));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","attributeDefsToReplace").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list18.add(null);
                                                              reader.next();
                                                          } else {
                                                        list18.add(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefLookup.Factory.parse(reader));
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
                                                                if (new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","attributeDefsToReplace").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list18.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list18.add(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefLookup.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone18 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setAttributeDefsToReplace((edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefLookup[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefLookup.class,
                                                                list18));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","actionsToReplace").equals(reader.getName())){
                                
                                    
                                    
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
                                                    if (new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","actionsToReplace").equals(reader.getName())){
                                                         
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","disallowed").equals(reader.getName())){
                                
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
                                throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                            



            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
    