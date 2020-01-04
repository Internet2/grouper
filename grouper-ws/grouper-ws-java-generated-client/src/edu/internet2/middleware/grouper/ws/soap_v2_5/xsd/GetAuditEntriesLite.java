
/**
 * GetAuditEntriesLite.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1  Built on : Aug 31, 2011 (12:23:23 CEST)
 */

            
                package edu.internet2.middleware.grouper.ws.soap_v2_5.xsd;
            

            /**
            *  GetAuditEntriesLite bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class GetAuditEntriesLite
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                "getAuditEntriesLite",
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
                        * field for ActAsSubjectId
                        */

                        
                                    protected java.lang.String localActAsSubjectId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localActAsSubjectIdTracker = false ;

                           public boolean isActAsSubjectIdSpecified(){
                               return localActAsSubjectIdTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getActAsSubjectId(){
                               return localActAsSubjectId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ActAsSubjectId
                               */
                               public void setActAsSubjectId(java.lang.String param){
                            localActAsSubjectIdTracker = true;
                                   
                                            this.localActAsSubjectId=param;
                                    

                               }
                            

                        /**
                        * field for ActAsSubjectSourceId
                        */

                        
                                    protected java.lang.String localActAsSubjectSourceId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localActAsSubjectSourceIdTracker = false ;

                           public boolean isActAsSubjectSourceIdSpecified(){
                               return localActAsSubjectSourceIdTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getActAsSubjectSourceId(){
                               return localActAsSubjectSourceId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ActAsSubjectSourceId
                               */
                               public void setActAsSubjectSourceId(java.lang.String param){
                            localActAsSubjectSourceIdTracker = true;
                                   
                                            this.localActAsSubjectSourceId=param;
                                    

                               }
                            

                        /**
                        * field for ActAsSubjectIdentifier
                        */

                        
                                    protected java.lang.String localActAsSubjectIdentifier ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localActAsSubjectIdentifierTracker = false ;

                           public boolean isActAsSubjectIdentifierSpecified(){
                               return localActAsSubjectIdentifierTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getActAsSubjectIdentifier(){
                               return localActAsSubjectIdentifier;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ActAsSubjectIdentifier
                               */
                               public void setActAsSubjectIdentifier(java.lang.String param){
                            localActAsSubjectIdentifierTracker = true;
                                   
                                            this.localActAsSubjectIdentifier=param;
                                    

                               }
                            

                        /**
                        * field for AuditType
                        */

                        
                                    protected java.lang.String localAuditType ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAuditTypeTracker = false ;

                           public boolean isAuditTypeSpecified(){
                               return localAuditTypeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAuditType(){
                               return localAuditType;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AuditType
                               */
                               public void setAuditType(java.lang.String param){
                            localAuditTypeTracker = true;
                                   
                                            this.localAuditType=param;
                                    

                               }
                            

                        /**
                        * field for AuditActionId
                        */

                        
                                    protected java.lang.String localAuditActionId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAuditActionIdTracker = false ;

                           public boolean isAuditActionIdSpecified(){
                               return localAuditActionIdTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAuditActionId(){
                               return localAuditActionId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AuditActionId
                               */
                               public void setAuditActionId(java.lang.String param){
                            localAuditActionIdTracker = true;
                                   
                                            this.localAuditActionId=param;
                                    

                               }
                            

                        /**
                        * field for WsGroupName
                        */

                        
                                    protected java.lang.String localWsGroupName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsGroupNameTracker = false ;

                           public boolean isWsGroupNameSpecified(){
                               return localWsGroupNameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getWsGroupName(){
                               return localWsGroupName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param WsGroupName
                               */
                               public void setWsGroupName(java.lang.String param){
                            localWsGroupNameTracker = true;
                                   
                                            this.localWsGroupName=param;
                                    

                               }
                            

                        /**
                        * field for WsGroupId
                        */

                        
                                    protected java.lang.String localWsGroupId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsGroupIdTracker = false ;

                           public boolean isWsGroupIdSpecified(){
                               return localWsGroupIdTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getWsGroupId(){
                               return localWsGroupId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param WsGroupId
                               */
                               public void setWsGroupId(java.lang.String param){
                            localWsGroupIdTracker = true;
                                   
                                            this.localWsGroupId=param;
                                    

                               }
                            

                        /**
                        * field for WsStemName
                        */

                        
                                    protected java.lang.String localWsStemName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsStemNameTracker = false ;

                           public boolean isWsStemNameSpecified(){
                               return localWsStemNameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getWsStemName(){
                               return localWsStemName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param WsStemName
                               */
                               public void setWsStemName(java.lang.String param){
                            localWsStemNameTracker = true;
                                   
                                            this.localWsStemName=param;
                                    

                               }
                            

                        /**
                        * field for WsStemId
                        */

                        
                                    protected java.lang.String localWsStemId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsStemIdTracker = false ;

                           public boolean isWsStemIdSpecified(){
                               return localWsStemIdTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getWsStemId(){
                               return localWsStemId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param WsStemId
                               */
                               public void setWsStemId(java.lang.String param){
                            localWsStemIdTracker = true;
                                   
                                            this.localWsStemId=param;
                                    

                               }
                            

                        /**
                        * field for WsAttributeDefName
                        */

                        
                                    protected java.lang.String localWsAttributeDefName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsAttributeDefNameTracker = false ;

                           public boolean isWsAttributeDefNameSpecified(){
                               return localWsAttributeDefNameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getWsAttributeDefName(){
                               return localWsAttributeDefName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param WsAttributeDefName
                               */
                               public void setWsAttributeDefName(java.lang.String param){
                            localWsAttributeDefNameTracker = true;
                                   
                                            this.localWsAttributeDefName=param;
                                    

                               }
                            

                        /**
                        * field for WsAttributeDefId
                        */

                        
                                    protected java.lang.String localWsAttributeDefId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsAttributeDefIdTracker = false ;

                           public boolean isWsAttributeDefIdSpecified(){
                               return localWsAttributeDefIdTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getWsAttributeDefId(){
                               return localWsAttributeDefId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param WsAttributeDefId
                               */
                               public void setWsAttributeDefId(java.lang.String param){
                            localWsAttributeDefIdTracker = true;
                                   
                                            this.localWsAttributeDefId=param;
                                    

                               }
                            

                        /**
                        * field for WsAttributeDefNameName
                        */

                        
                                    protected java.lang.String localWsAttributeDefNameName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsAttributeDefNameNameTracker = false ;

                           public boolean isWsAttributeDefNameNameSpecified(){
                               return localWsAttributeDefNameNameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getWsAttributeDefNameName(){
                               return localWsAttributeDefNameName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param WsAttributeDefNameName
                               */
                               public void setWsAttributeDefNameName(java.lang.String param){
                            localWsAttributeDefNameNameTracker = true;
                                   
                                            this.localWsAttributeDefNameName=param;
                                    

                               }
                            

                        /**
                        * field for WsAttributeDefNameId
                        */

                        
                                    protected java.lang.String localWsAttributeDefNameId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsAttributeDefNameIdTracker = false ;

                           public boolean isWsAttributeDefNameIdSpecified(){
                               return localWsAttributeDefNameIdTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getWsAttributeDefNameId(){
                               return localWsAttributeDefNameId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param WsAttributeDefNameId
                               */
                               public void setWsAttributeDefNameId(java.lang.String param){
                            localWsAttributeDefNameIdTracker = true;
                                   
                                            this.localWsAttributeDefNameId=param;
                                    

                               }
                            

                        /**
                        * field for WsSubjectId
                        */

                        
                                    protected java.lang.String localWsSubjectId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsSubjectIdTracker = false ;

                           public boolean isWsSubjectIdSpecified(){
                               return localWsSubjectIdTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getWsSubjectId(){
                               return localWsSubjectId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param WsSubjectId
                               */
                               public void setWsSubjectId(java.lang.String param){
                            localWsSubjectIdTracker = true;
                                   
                                            this.localWsSubjectId=param;
                                    

                               }
                            

                        /**
                        * field for WsSubjectSourceId
                        */

                        
                                    protected java.lang.String localWsSubjectSourceId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsSubjectSourceIdTracker = false ;

                           public boolean isWsSubjectSourceIdSpecified(){
                               return localWsSubjectSourceIdTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getWsSubjectSourceId(){
                               return localWsSubjectSourceId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param WsSubjectSourceId
                               */
                               public void setWsSubjectSourceId(java.lang.String param){
                            localWsSubjectSourceIdTracker = true;
                                   
                                            this.localWsSubjectSourceId=param;
                                    

                               }
                            

                        /**
                        * field for WsSubjectIdentifier
                        */

                        
                                    protected java.lang.String localWsSubjectIdentifier ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsSubjectIdentifierTracker = false ;

                           public boolean isWsSubjectIdentifierSpecified(){
                               return localWsSubjectIdentifierTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getWsSubjectIdentifier(){
                               return localWsSubjectIdentifier;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param WsSubjectIdentifier
                               */
                               public void setWsSubjectIdentifier(java.lang.String param){
                            localWsSubjectIdentifierTracker = true;
                                   
                                            this.localWsSubjectIdentifier=param;
                                    

                               }
                            

                        /**
                        * field for ActionsPerformedByWsSubjectId
                        */

                        
                                    protected java.lang.String localActionsPerformedByWsSubjectId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localActionsPerformedByWsSubjectIdTracker = false ;

                           public boolean isActionsPerformedByWsSubjectIdSpecified(){
                               return localActionsPerformedByWsSubjectIdTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getActionsPerformedByWsSubjectId(){
                               return localActionsPerformedByWsSubjectId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ActionsPerformedByWsSubjectId
                               */
                               public void setActionsPerformedByWsSubjectId(java.lang.String param){
                            localActionsPerformedByWsSubjectIdTracker = true;
                                   
                                            this.localActionsPerformedByWsSubjectId=param;
                                    

                               }
                            

                        /**
                        * field for ActionsPerformedByWsSubjectSourceId
                        */

                        
                                    protected java.lang.String localActionsPerformedByWsSubjectSourceId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localActionsPerformedByWsSubjectSourceIdTracker = false ;

                           public boolean isActionsPerformedByWsSubjectSourceIdSpecified(){
                               return localActionsPerformedByWsSubjectSourceIdTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getActionsPerformedByWsSubjectSourceId(){
                               return localActionsPerformedByWsSubjectSourceId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ActionsPerformedByWsSubjectSourceId
                               */
                               public void setActionsPerformedByWsSubjectSourceId(java.lang.String param){
                            localActionsPerformedByWsSubjectSourceIdTracker = true;
                                   
                                            this.localActionsPerformedByWsSubjectSourceId=param;
                                    

                               }
                            

                        /**
                        * field for ActionsPerformedByWsSubjectIdentifier
                        */

                        
                                    protected java.lang.String localActionsPerformedByWsSubjectIdentifier ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localActionsPerformedByWsSubjectIdentifierTracker = false ;

                           public boolean isActionsPerformedByWsSubjectIdentifierSpecified(){
                               return localActionsPerformedByWsSubjectIdentifierTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getActionsPerformedByWsSubjectIdentifier(){
                               return localActionsPerformedByWsSubjectIdentifier;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ActionsPerformedByWsSubjectIdentifier
                               */
                               public void setActionsPerformedByWsSubjectIdentifier(java.lang.String param){
                            localActionsPerformedByWsSubjectIdentifierTracker = true;
                                   
                                            this.localActionsPerformedByWsSubjectIdentifier=param;
                                    

                               }
                            

                        /**
                        * field for ParamName0
                        */

                        
                                    protected java.lang.String localParamName0 ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localParamName0Tracker = false ;

                           public boolean isParamName0Specified(){
                               return localParamName0Tracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getParamName0(){
                               return localParamName0;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ParamName0
                               */
                               public void setParamName0(java.lang.String param){
                            localParamName0Tracker = true;
                                   
                                            this.localParamName0=param;
                                    

                               }
                            

                        /**
                        * field for ParamValue0
                        */

                        
                                    protected java.lang.String localParamValue0 ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localParamValue0Tracker = false ;

                           public boolean isParamValue0Specified(){
                               return localParamValue0Tracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getParamValue0(){
                               return localParamValue0;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ParamValue0
                               */
                               public void setParamValue0(java.lang.String param){
                            localParamValue0Tracker = true;
                                   
                                            this.localParamValue0=param;
                                    

                               }
                            

                        /**
                        * field for ParamName1
                        */

                        
                                    protected java.lang.String localParamName1 ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localParamName1Tracker = false ;

                           public boolean isParamName1Specified(){
                               return localParamName1Tracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getParamName1(){
                               return localParamName1;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ParamName1
                               */
                               public void setParamName1(java.lang.String param){
                            localParamName1Tracker = true;
                                   
                                            this.localParamName1=param;
                                    

                               }
                            

                        /**
                        * field for ParamValue1
                        */

                        
                                    protected java.lang.String localParamValue1 ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localParamValue1Tracker = false ;

                           public boolean isParamValue1Specified(){
                               return localParamValue1Tracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getParamValue1(){
                               return localParamValue1;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ParamValue1
                               */
                               public void setParamValue1(java.lang.String param){
                            localParamValue1Tracker = true;
                                   
                                            this.localParamValue1=param;
                                    

                               }
                            

                        /**
                        * field for PageSize
                        */

                        
                                    protected java.lang.String localPageSize ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPageSizeTracker = false ;

                           public boolean isPageSizeSpecified(){
                               return localPageSizeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPageSize(){
                               return localPageSize;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PageSize
                               */
                               public void setPageSize(java.lang.String param){
                            localPageSizeTracker = true;
                                   
                                            this.localPageSize=param;
                                    

                               }
                            

                        /**
                        * field for SortString
                        */

                        
                                    protected java.lang.String localSortString ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSortStringTracker = false ;

                           public boolean isSortStringSpecified(){
                               return localSortStringTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getSortString(){
                               return localSortString;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SortString
                               */
                               public void setSortString(java.lang.String param){
                            localSortStringTracker = true;
                                   
                                            this.localSortString=param;
                                    

                               }
                            

                        /**
                        * field for Ascending
                        */

                        
                                    protected java.lang.String localAscending ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAscendingTracker = false ;

                           public boolean isAscendingSpecified(){
                               return localAscendingTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAscending(){
                               return localAscending;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Ascending
                               */
                               public void setAscending(java.lang.String param){
                            localAscendingTracker = true;
                                   
                                            this.localAscending=param;
                                    

                               }
                            

                        /**
                        * field for PageIsCursor
                        */

                        
                                    protected java.lang.String localPageIsCursor ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPageIsCursorTracker = false ;

                           public boolean isPageIsCursorSpecified(){
                               return localPageIsCursorTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPageIsCursor(){
                               return localPageIsCursor;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PageIsCursor
                               */
                               public void setPageIsCursor(java.lang.String param){
                            localPageIsCursorTracker = true;
                                   
                                            this.localPageIsCursor=param;
                                    

                               }
                            

                        /**
                        * field for PageLastCursorField
                        */

                        
                                    protected java.lang.String localPageLastCursorField ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPageLastCursorFieldTracker = false ;

                           public boolean isPageLastCursorFieldSpecified(){
                               return localPageLastCursorFieldTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPageLastCursorField(){
                               return localPageLastCursorField;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PageLastCursorField
                               */
                               public void setPageLastCursorField(java.lang.String param){
                            localPageLastCursorFieldTracker = true;
                                   
                                            this.localPageLastCursorField=param;
                                    

                               }
                            

                        /**
                        * field for PageLastCursorFieldType
                        */

                        
                                    protected java.lang.String localPageLastCursorFieldType ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPageLastCursorFieldTypeTracker = false ;

                           public boolean isPageLastCursorFieldTypeSpecified(){
                               return localPageLastCursorFieldTypeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPageLastCursorFieldType(){
                               return localPageLastCursorFieldType;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PageLastCursorFieldType
                               */
                               public void setPageLastCursorFieldType(java.lang.String param){
                            localPageLastCursorFieldTypeTracker = true;
                                   
                                            this.localPageLastCursorFieldType=param;
                                    

                               }
                            

                        /**
                        * field for PageCursorFieldIncludesLastRetrieved
                        */

                        
                                    protected java.lang.String localPageCursorFieldIncludesLastRetrieved ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPageCursorFieldIncludesLastRetrievedTracker = false ;

                           public boolean isPageCursorFieldIncludesLastRetrievedSpecified(){
                               return localPageCursorFieldIncludesLastRetrievedTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPageCursorFieldIncludesLastRetrieved(){
                               return localPageCursorFieldIncludesLastRetrieved;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PageCursorFieldIncludesLastRetrieved
                               */
                               public void setPageCursorFieldIncludesLastRetrieved(java.lang.String param){
                            localPageCursorFieldIncludesLastRetrievedTracker = true;
                                   
                                            this.localPageCursorFieldIncludesLastRetrieved=param;
                                    

                               }
                            

                        /**
                        * field for FromDate
                        */

                        
                                    protected java.lang.String localFromDate ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localFromDateTracker = false ;

                           public boolean isFromDateSpecified(){
                               return localFromDateTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getFromDate(){
                               return localFromDate;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param FromDate
                               */
                               public void setFromDate(java.lang.String param){
                            localFromDateTracker = true;
                                   
                                            this.localFromDate=param;
                                    

                               }
                            

                        /**
                        * field for ToDate
                        */

                        
                                    protected java.lang.String localToDate ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localToDateTracker = false ;

                           public boolean isToDateSpecified(){
                               return localToDateTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getToDate(){
                               return localToDate;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ToDate
                               */
                               public void setToDate(java.lang.String param){
                            localToDateTracker = true;
                                   
                                            this.localToDate=param;
                                    

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
               

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":getAuditEntriesLite",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "getAuditEntriesLite",
                           xmlWriter);
                   }

               
                   }
                if (localClientVersionTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "clientVersion", xmlWriter);
                             

                                          if (localClientVersion==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localClientVersion);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localActAsSubjectIdTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "actAsSubjectId", xmlWriter);
                             

                                          if (localActAsSubjectId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localActAsSubjectId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localActAsSubjectSourceIdTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "actAsSubjectSourceId", xmlWriter);
                             

                                          if (localActAsSubjectSourceId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localActAsSubjectSourceId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localActAsSubjectIdentifierTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "actAsSubjectIdentifier", xmlWriter);
                             

                                          if (localActAsSubjectIdentifier==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localActAsSubjectIdentifier);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAuditTypeTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "auditType", xmlWriter);
                             

                                          if (localAuditType==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAuditType);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAuditActionIdTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "auditActionId", xmlWriter);
                             

                                          if (localAuditActionId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAuditActionId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localWsGroupNameTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "wsGroupName", xmlWriter);
                             

                                          if (localWsGroupName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localWsGroupName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localWsGroupIdTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "wsGroupId", xmlWriter);
                             

                                          if (localWsGroupId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localWsGroupId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localWsStemNameTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "wsStemName", xmlWriter);
                             

                                          if (localWsStemName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localWsStemName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localWsStemIdTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "wsStemId", xmlWriter);
                             

                                          if (localWsStemId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localWsStemId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localWsAttributeDefNameTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "wsAttributeDefName", xmlWriter);
                             

                                          if (localWsAttributeDefName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localWsAttributeDefName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localWsAttributeDefIdTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "wsAttributeDefId", xmlWriter);
                             

                                          if (localWsAttributeDefId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localWsAttributeDefId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localWsAttributeDefNameNameTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "wsAttributeDefNameName", xmlWriter);
                             

                                          if (localWsAttributeDefNameName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localWsAttributeDefNameName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localWsAttributeDefNameIdTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "wsAttributeDefNameId", xmlWriter);
                             

                                          if (localWsAttributeDefNameId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localWsAttributeDefNameId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localWsSubjectIdTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "wsSubjectId", xmlWriter);
                             

                                          if (localWsSubjectId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localWsSubjectId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localWsSubjectSourceIdTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "wsSubjectSourceId", xmlWriter);
                             

                                          if (localWsSubjectSourceId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localWsSubjectSourceId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localWsSubjectIdentifierTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "wsSubjectIdentifier", xmlWriter);
                             

                                          if (localWsSubjectIdentifier==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localWsSubjectIdentifier);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localActionsPerformedByWsSubjectIdTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "actionsPerformedByWsSubjectId", xmlWriter);
                             

                                          if (localActionsPerformedByWsSubjectId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localActionsPerformedByWsSubjectId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localActionsPerformedByWsSubjectSourceIdTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "actionsPerformedByWsSubjectSourceId", xmlWriter);
                             

                                          if (localActionsPerformedByWsSubjectSourceId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localActionsPerformedByWsSubjectSourceId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localActionsPerformedByWsSubjectIdentifierTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "actionsPerformedByWsSubjectIdentifier", xmlWriter);
                             

                                          if (localActionsPerformedByWsSubjectIdentifier==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localActionsPerformedByWsSubjectIdentifier);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localParamName0Tracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "paramName0", xmlWriter);
                             

                                          if (localParamName0==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localParamName0);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localParamValue0Tracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "paramValue0", xmlWriter);
                             

                                          if (localParamValue0==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localParamValue0);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localParamName1Tracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "paramName1", xmlWriter);
                             

                                          if (localParamName1==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localParamName1);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localParamValue1Tracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "paramValue1", xmlWriter);
                             

                                          if (localParamValue1==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localParamValue1);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPageSizeTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "pageSize", xmlWriter);
                             

                                          if (localPageSize==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPageSize);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSortStringTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "sortString", xmlWriter);
                             

                                          if (localSortString==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localSortString);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAscendingTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "ascending", xmlWriter);
                             

                                          if (localAscending==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAscending);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPageIsCursorTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "pageIsCursor", xmlWriter);
                             

                                          if (localPageIsCursor==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPageIsCursor);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPageLastCursorFieldTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "pageLastCursorField", xmlWriter);
                             

                                          if (localPageLastCursorField==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPageLastCursorField);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPageLastCursorFieldTypeTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "pageLastCursorFieldType", xmlWriter);
                             

                                          if (localPageLastCursorFieldType==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPageLastCursorFieldType);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPageCursorFieldIncludesLastRetrievedTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "pageCursorFieldIncludesLastRetrieved", xmlWriter);
                             

                                          if (localPageCursorFieldIncludesLastRetrieved==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPageCursorFieldIncludesLastRetrieved);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localFromDateTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "fromDate", xmlWriter);
                             

                                          if (localFromDate==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localFromDate);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localToDateTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "toDate", xmlWriter);
                             

                                          if (localToDate==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localToDate);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                    xmlWriter.writeEndElement();
               

        }

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd")){
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
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "clientVersion"));
                                 
                                         elementList.add(localClientVersion==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localClientVersion));
                                    } if (localActAsSubjectIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "actAsSubjectId"));
                                 
                                         elementList.add(localActAsSubjectId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localActAsSubjectId));
                                    } if (localActAsSubjectSourceIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "actAsSubjectSourceId"));
                                 
                                         elementList.add(localActAsSubjectSourceId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localActAsSubjectSourceId));
                                    } if (localActAsSubjectIdentifierTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "actAsSubjectIdentifier"));
                                 
                                         elementList.add(localActAsSubjectIdentifier==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localActAsSubjectIdentifier));
                                    } if (localAuditTypeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "auditType"));
                                 
                                         elementList.add(localAuditType==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAuditType));
                                    } if (localAuditActionIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "auditActionId"));
                                 
                                         elementList.add(localAuditActionId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAuditActionId));
                                    } if (localWsGroupNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "wsGroupName"));
                                 
                                         elementList.add(localWsGroupName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localWsGroupName));
                                    } if (localWsGroupIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "wsGroupId"));
                                 
                                         elementList.add(localWsGroupId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localWsGroupId));
                                    } if (localWsStemNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "wsStemName"));
                                 
                                         elementList.add(localWsStemName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localWsStemName));
                                    } if (localWsStemIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "wsStemId"));
                                 
                                         elementList.add(localWsStemId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localWsStemId));
                                    } if (localWsAttributeDefNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "wsAttributeDefName"));
                                 
                                         elementList.add(localWsAttributeDefName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localWsAttributeDefName));
                                    } if (localWsAttributeDefIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "wsAttributeDefId"));
                                 
                                         elementList.add(localWsAttributeDefId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localWsAttributeDefId));
                                    } if (localWsAttributeDefNameNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "wsAttributeDefNameName"));
                                 
                                         elementList.add(localWsAttributeDefNameName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localWsAttributeDefNameName));
                                    } if (localWsAttributeDefNameIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "wsAttributeDefNameId"));
                                 
                                         elementList.add(localWsAttributeDefNameId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localWsAttributeDefNameId));
                                    } if (localWsSubjectIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "wsSubjectId"));
                                 
                                         elementList.add(localWsSubjectId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localWsSubjectId));
                                    } if (localWsSubjectSourceIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "wsSubjectSourceId"));
                                 
                                         elementList.add(localWsSubjectSourceId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localWsSubjectSourceId));
                                    } if (localWsSubjectIdentifierTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "wsSubjectIdentifier"));
                                 
                                         elementList.add(localWsSubjectIdentifier==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localWsSubjectIdentifier));
                                    } if (localActionsPerformedByWsSubjectIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "actionsPerformedByWsSubjectId"));
                                 
                                         elementList.add(localActionsPerformedByWsSubjectId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localActionsPerformedByWsSubjectId));
                                    } if (localActionsPerformedByWsSubjectSourceIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "actionsPerformedByWsSubjectSourceId"));
                                 
                                         elementList.add(localActionsPerformedByWsSubjectSourceId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localActionsPerformedByWsSubjectSourceId));
                                    } if (localActionsPerformedByWsSubjectIdentifierTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "actionsPerformedByWsSubjectIdentifier"));
                                 
                                         elementList.add(localActionsPerformedByWsSubjectIdentifier==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localActionsPerformedByWsSubjectIdentifier));
                                    } if (localParamName0Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "paramName0"));
                                 
                                         elementList.add(localParamName0==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localParamName0));
                                    } if (localParamValue0Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "paramValue0"));
                                 
                                         elementList.add(localParamValue0==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localParamValue0));
                                    } if (localParamName1Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "paramName1"));
                                 
                                         elementList.add(localParamName1==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localParamName1));
                                    } if (localParamValue1Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "paramValue1"));
                                 
                                         elementList.add(localParamValue1==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localParamValue1));
                                    } if (localPageSizeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "pageSize"));
                                 
                                         elementList.add(localPageSize==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPageSize));
                                    } if (localSortStringTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "sortString"));
                                 
                                         elementList.add(localSortString==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSortString));
                                    } if (localAscendingTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "ascending"));
                                 
                                         elementList.add(localAscending==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAscending));
                                    } if (localPageIsCursorTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "pageIsCursor"));
                                 
                                         elementList.add(localPageIsCursor==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPageIsCursor));
                                    } if (localPageLastCursorFieldTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "pageLastCursorField"));
                                 
                                         elementList.add(localPageLastCursorField==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPageLastCursorField));
                                    } if (localPageLastCursorFieldTypeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "pageLastCursorFieldType"));
                                 
                                         elementList.add(localPageLastCursorFieldType==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPageLastCursorFieldType));
                                    } if (localPageCursorFieldIncludesLastRetrievedTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "pageCursorFieldIncludesLastRetrieved"));
                                 
                                         elementList.add(localPageCursorFieldIncludesLastRetrieved==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPageCursorFieldIncludesLastRetrieved));
                                    } if (localFromDateTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "fromDate"));
                                 
                                         elementList.add(localFromDate==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localFromDate));
                                    } if (localToDateTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "toDate"));
                                 
                                         elementList.add(localToDate==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localToDate));
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
        public static GetAuditEntriesLite parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            GetAuditEntriesLite object =
                new GetAuditEntriesLite();

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
                    
                            if (!"getAuditEntriesLite".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (GetAuditEntriesLite)edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","clientVersion").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","actAsSubjectId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setActAsSubjectId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","actAsSubjectSourceId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setActAsSubjectSourceId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","actAsSubjectIdentifier").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setActAsSubjectIdentifier(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","auditType").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAuditType(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","auditActionId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAuditActionId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","wsGroupName").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setWsGroupName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","wsGroupId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setWsGroupId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","wsStemName").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setWsStemName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","wsStemId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setWsStemId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","wsAttributeDefName").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setWsAttributeDefName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","wsAttributeDefId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setWsAttributeDefId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","wsAttributeDefNameName").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setWsAttributeDefNameName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","wsAttributeDefNameId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setWsAttributeDefNameId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","wsSubjectId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setWsSubjectId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","wsSubjectSourceId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setWsSubjectSourceId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","wsSubjectIdentifier").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setWsSubjectIdentifier(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","actionsPerformedByWsSubjectId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setActionsPerformedByWsSubjectId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","actionsPerformedByWsSubjectSourceId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setActionsPerformedByWsSubjectSourceId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","actionsPerformedByWsSubjectIdentifier").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setActionsPerformedByWsSubjectIdentifier(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","paramName0").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setParamName0(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","paramValue0").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setParamValue0(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","paramName1").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setParamName1(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","paramValue1").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setParamValue1(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","pageSize").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPageSize(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","sortString").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setSortString(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","ascending").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAscending(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","pageIsCursor").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPageIsCursor(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","pageLastCursorField").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPageLastCursorField(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","pageLastCursorFieldType").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPageLastCursorFieldType(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","pageCursorFieldIncludesLastRetrieved").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPageCursorFieldIncludesLastRetrieved(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","fromDate").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setFromDate(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","toDate").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setToDate(
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
           
    