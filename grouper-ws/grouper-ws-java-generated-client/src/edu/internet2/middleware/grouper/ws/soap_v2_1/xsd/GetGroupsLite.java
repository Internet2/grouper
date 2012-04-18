/*******************************************************************************
 * Copyright 2012 Internet2
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
 * GetGroupsLite.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1  Built on : Aug 31, 2011 (12:23:23 CEST)
 */

            
                package edu.internet2.middleware.grouper.ws.soap_v2_1.xsd;
            

            /**
            *  GetGroupsLite bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class GetGroupsLite
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                "getGroupsLite",
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
                        * field for SubjectId
                        */

                        
                                    protected java.lang.String localSubjectId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSubjectIdTracker = false ;

                           public boolean isSubjectIdSpecified(){
                               return localSubjectIdTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getSubjectId(){
                               return localSubjectId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SubjectId
                               */
                               public void setSubjectId(java.lang.String param){
                            localSubjectIdTracker = true;
                                   
                                            this.localSubjectId=param;
                                    

                               }
                            

                        /**
                        * field for SubjectSourceId
                        */

                        
                                    protected java.lang.String localSubjectSourceId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSubjectSourceIdTracker = false ;

                           public boolean isSubjectSourceIdSpecified(){
                               return localSubjectSourceIdTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getSubjectSourceId(){
                               return localSubjectSourceId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SubjectSourceId
                               */
                               public void setSubjectSourceId(java.lang.String param){
                            localSubjectSourceIdTracker = true;
                                   
                                            this.localSubjectSourceId=param;
                                    

                               }
                            

                        /**
                        * field for SubjectIdentifier
                        */

                        
                                    protected java.lang.String localSubjectIdentifier ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSubjectIdentifierTracker = false ;

                           public boolean isSubjectIdentifierSpecified(){
                               return localSubjectIdentifierTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getSubjectIdentifier(){
                               return localSubjectIdentifier;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SubjectIdentifier
                               */
                               public void setSubjectIdentifier(java.lang.String param){
                            localSubjectIdentifierTracker = true;
                                   
                                            this.localSubjectIdentifier=param;
                                    

                               }
                            

                        /**
                        * field for MemberFilter
                        */

                        
                                    protected java.lang.String localMemberFilter ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localMemberFilterTracker = false ;

                           public boolean isMemberFilterSpecified(){
                               return localMemberFilterTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getMemberFilter(){
                               return localMemberFilter;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param MemberFilter
                               */
                               public void setMemberFilter(java.lang.String param){
                            localMemberFilterTracker = true;
                                   
                                            this.localMemberFilter=param;
                                    

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
                        */

                        
                                    protected java.lang.String localSubjectAttributeNames ;
                                
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
                           * @return java.lang.String
                           */
                           public  java.lang.String getSubjectAttributeNames(){
                               return localSubjectAttributeNames;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SubjectAttributeNames
                               */
                               public void setSubjectAttributeNames(java.lang.String param){
                            localSubjectAttributeNamesTracker = true;
                                   
                                            this.localSubjectAttributeNames=param;
                                    

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
                        * field for FieldName
                        */

                        
                                    protected java.lang.String localFieldName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localFieldNameTracker = false ;

                           public boolean isFieldNameSpecified(){
                               return localFieldNameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getFieldName(){
                               return localFieldName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param FieldName
                               */
                               public void setFieldName(java.lang.String param){
                            localFieldNameTracker = true;
                                   
                                            this.localFieldName=param;
                                    

                               }
                            

                        /**
                        * field for Scope
                        */

                        
                                    protected java.lang.String localScope ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localScopeTracker = false ;

                           public boolean isScopeSpecified(){
                               return localScopeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getScope(){
                               return localScope;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Scope
                               */
                               public void setScope(java.lang.String param){
                            localScopeTracker = true;
                                   
                                            this.localScope=param;
                                    

                               }
                            

                        /**
                        * field for StemName
                        */

                        
                                    protected java.lang.String localStemName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localStemNameTracker = false ;

                           public boolean isStemNameSpecified(){
                               return localStemNameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getStemName(){
                               return localStemName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param StemName
                               */
                               public void setStemName(java.lang.String param){
                            localStemNameTracker = true;
                                   
                                            this.localStemName=param;
                                    

                               }
                            

                        /**
                        * field for StemUuid
                        */

                        
                                    protected java.lang.String localStemUuid ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localStemUuidTracker = false ;

                           public boolean isStemUuidSpecified(){
                               return localStemUuidTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getStemUuid(){
                               return localStemUuid;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param StemUuid
                               */
                               public void setStemUuid(java.lang.String param){
                            localStemUuidTracker = true;
                                   
                                            this.localStemUuid=param;
                                    

                               }
                            

                        /**
                        * field for StemScope
                        */

                        
                                    protected java.lang.String localStemScope ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localStemScopeTracker = false ;

                           public boolean isStemScopeSpecified(){
                               return localStemScopeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getStemScope(){
                               return localStemScope;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param StemScope
                               */
                               public void setStemScope(java.lang.String param){
                            localStemScopeTracker = true;
                                   
                                            this.localStemScope=param;
                                    

                               }
                            

                        /**
                        * field for Enabled
                        */

                        
                                    protected java.lang.String localEnabled ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localEnabledTracker = false ;

                           public boolean isEnabledSpecified(){
                               return localEnabledTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getEnabled(){
                               return localEnabled;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Enabled
                               */
                               public void setEnabled(java.lang.String param){
                            localEnabledTracker = true;
                                   
                                            this.localEnabled=param;
                                    

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
                        * field for PageNumber
                        */

                        
                                    protected java.lang.String localPageNumber ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPageNumberTracker = false ;

                           public boolean isPageNumberSpecified(){
                               return localPageNumberTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPageNumber(){
                               return localPageNumber;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PageNumber
                               */
                               public void setPageNumber(java.lang.String param){
                            localPageNumberTracker = true;
                                   
                                            this.localPageNumber=param;
                                    

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
                        * field for PointInTimeFrom
                        */

                        
                                    protected java.lang.String localPointInTimeFrom ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPointInTimeFromTracker = false ;

                           public boolean isPointInTimeFromSpecified(){
                               return localPointInTimeFromTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPointInTimeFrom(){
                               return localPointInTimeFrom;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PointInTimeFrom
                               */
                               public void setPointInTimeFrom(java.lang.String param){
                            localPointInTimeFromTracker = true;
                                   
                                            this.localPointInTimeFrom=param;
                                    

                               }
                            

                        /**
                        * field for PointInTimeTo
                        */

                        
                                    protected java.lang.String localPointInTimeTo ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPointInTimeToTracker = false ;

                           public boolean isPointInTimeToSpecified(){
                               return localPointInTimeToTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPointInTimeTo(){
                               return localPointInTimeTo;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PointInTimeTo
                               */
                               public void setPointInTimeTo(java.lang.String param){
                            localPointInTimeToTracker = true;
                                   
                                            this.localPointInTimeTo=param;
                                    

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
               

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":getGroupsLite",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "getGroupsLite",
                           xmlWriter);
                   }

               
                   }
                if (localClientVersionTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "clientVersion", xmlWriter);
                             

                                          if (localClientVersion==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localClientVersion);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSubjectIdTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "subjectId", xmlWriter);
                             

                                          if (localSubjectId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localSubjectId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSubjectSourceIdTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "subjectSourceId", xmlWriter);
                             

                                          if (localSubjectSourceId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localSubjectSourceId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSubjectIdentifierTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "subjectIdentifier", xmlWriter);
                             

                                          if (localSubjectIdentifier==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localSubjectIdentifier);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localMemberFilterTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "memberFilter", xmlWriter);
                             

                                          if (localMemberFilter==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localMemberFilter);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localActAsSubjectIdTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "actAsSubjectId", xmlWriter);
                             

                                          if (localActAsSubjectId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localActAsSubjectId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localActAsSubjectSourceIdTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "actAsSubjectSourceId", xmlWriter);
                             

                                          if (localActAsSubjectSourceId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localActAsSubjectSourceId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localActAsSubjectIdentifierTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "actAsSubjectIdentifier", xmlWriter);
                             

                                          if (localActAsSubjectIdentifier==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localActAsSubjectIdentifier);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localIncludeGroupDetailTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "includeGroupDetail", xmlWriter);
                             

                                          if (localIncludeGroupDetail==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localIncludeGroupDetail);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localIncludeSubjectDetailTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "includeSubjectDetail", xmlWriter);
                             

                                          if (localIncludeSubjectDetail==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localIncludeSubjectDetail);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSubjectAttributeNamesTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "subjectAttributeNames", xmlWriter);
                             

                                          if (localSubjectAttributeNames==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localSubjectAttributeNames);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localParamName0Tracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "paramName0", xmlWriter);
                             

                                          if (localParamName0==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localParamName0);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localParamValue0Tracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "paramValue0", xmlWriter);
                             

                                          if (localParamValue0==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localParamValue0);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localParamName1Tracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "paramName1", xmlWriter);
                             

                                          if (localParamName1==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localParamName1);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localParamValue1Tracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "paramValue1", xmlWriter);
                             

                                          if (localParamValue1==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localParamValue1);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localFieldNameTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "fieldName", xmlWriter);
                             

                                          if (localFieldName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localFieldName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localScopeTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "scope", xmlWriter);
                             

                                          if (localScope==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localScope);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localStemNameTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "stemName", xmlWriter);
                             

                                          if (localStemName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localStemName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localStemUuidTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "stemUuid", xmlWriter);
                             

                                          if (localStemUuid==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localStemUuid);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localStemScopeTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "stemScope", xmlWriter);
                             

                                          if (localStemScope==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localStemScope);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localEnabledTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "enabled", xmlWriter);
                             

                                          if (localEnabled==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localEnabled);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPageSizeTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "pageSize", xmlWriter);
                             

                                          if (localPageSize==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPageSize);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPageNumberTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "pageNumber", xmlWriter);
                             

                                          if (localPageNumber==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPageNumber);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSortStringTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "sortString", xmlWriter);
                             

                                          if (localSortString==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localSortString);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAscendingTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "ascending", xmlWriter);
                             

                                          if (localAscending==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAscending);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPointInTimeFromTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "pointInTimeFrom", xmlWriter);
                             

                                          if (localPointInTimeFrom==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPointInTimeFrom);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPointInTimeToTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "pointInTimeTo", xmlWriter);
                             

                                          if (localPointInTimeTo==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPointInTimeTo);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                    xmlWriter.writeEndElement();
               

        }

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd")){
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
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "clientVersion"));
                                 
                                         elementList.add(localClientVersion==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localClientVersion));
                                    } if (localSubjectIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "subjectId"));
                                 
                                         elementList.add(localSubjectId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSubjectId));
                                    } if (localSubjectSourceIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "subjectSourceId"));
                                 
                                         elementList.add(localSubjectSourceId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSubjectSourceId));
                                    } if (localSubjectIdentifierTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "subjectIdentifier"));
                                 
                                         elementList.add(localSubjectIdentifier==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSubjectIdentifier));
                                    } if (localMemberFilterTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "memberFilter"));
                                 
                                         elementList.add(localMemberFilter==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localMemberFilter));
                                    } if (localActAsSubjectIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "actAsSubjectId"));
                                 
                                         elementList.add(localActAsSubjectId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localActAsSubjectId));
                                    } if (localActAsSubjectSourceIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "actAsSubjectSourceId"));
                                 
                                         elementList.add(localActAsSubjectSourceId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localActAsSubjectSourceId));
                                    } if (localActAsSubjectIdentifierTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "actAsSubjectIdentifier"));
                                 
                                         elementList.add(localActAsSubjectIdentifier==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localActAsSubjectIdentifier));
                                    } if (localIncludeGroupDetailTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "includeGroupDetail"));
                                 
                                         elementList.add(localIncludeGroupDetail==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIncludeGroupDetail));
                                    } if (localIncludeSubjectDetailTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "includeSubjectDetail"));
                                 
                                         elementList.add(localIncludeSubjectDetail==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIncludeSubjectDetail));
                                    } if (localSubjectAttributeNamesTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "subjectAttributeNames"));
                                 
                                         elementList.add(localSubjectAttributeNames==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSubjectAttributeNames));
                                    } if (localParamName0Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "paramName0"));
                                 
                                         elementList.add(localParamName0==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localParamName0));
                                    } if (localParamValue0Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "paramValue0"));
                                 
                                         elementList.add(localParamValue0==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localParamValue0));
                                    } if (localParamName1Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "paramName1"));
                                 
                                         elementList.add(localParamName1==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localParamName1));
                                    } if (localParamValue1Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "paramValue1"));
                                 
                                         elementList.add(localParamValue1==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localParamValue1));
                                    } if (localFieldNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "fieldName"));
                                 
                                         elementList.add(localFieldName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localFieldName));
                                    } if (localScopeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "scope"));
                                 
                                         elementList.add(localScope==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localScope));
                                    } if (localStemNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "stemName"));
                                 
                                         elementList.add(localStemName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localStemName));
                                    } if (localStemUuidTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "stemUuid"));
                                 
                                         elementList.add(localStemUuid==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localStemUuid));
                                    } if (localStemScopeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "stemScope"));
                                 
                                         elementList.add(localStemScope==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localStemScope));
                                    } if (localEnabledTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "enabled"));
                                 
                                         elementList.add(localEnabled==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localEnabled));
                                    } if (localPageSizeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "pageSize"));
                                 
                                         elementList.add(localPageSize==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPageSize));
                                    } if (localPageNumberTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "pageNumber"));
                                 
                                         elementList.add(localPageNumber==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPageNumber));
                                    } if (localSortStringTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "sortString"));
                                 
                                         elementList.add(localSortString==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSortString));
                                    } if (localAscendingTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "ascending"));
                                 
                                         elementList.add(localAscending==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAscending));
                                    } if (localPointInTimeFromTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "pointInTimeFrom"));
                                 
                                         elementList.add(localPointInTimeFrom==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPointInTimeFrom));
                                    } if (localPointInTimeToTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "pointInTimeTo"));
                                 
                                         elementList.add(localPointInTimeTo==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPointInTimeTo));
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
        public static GetGroupsLite parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            GetGroupsLite object =
                new GetGroupsLite();

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
                    
                            if (!"getGroupsLite".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (GetGroupsLite)edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","clientVersion").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","subjectId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setSubjectId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","subjectSourceId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setSubjectSourceId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","subjectIdentifier").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setSubjectIdentifier(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","memberFilter").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setMemberFilter(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","actAsSubjectId").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","actAsSubjectSourceId").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","actAsSubjectIdentifier").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","includeGroupDetail").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","includeSubjectDetail").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","subjectAttributeNames").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setSubjectAttributeNames(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","paramName0").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","paramValue0").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","paramName1").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","paramValue1").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","fieldName").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setFieldName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","scope").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setScope(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","stemName").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setStemName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","stemUuid").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setStemUuid(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","stemScope").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setStemScope(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","enabled").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setEnabled(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","pageSize").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","pageNumber").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPageNumber(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","sortString").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","ascending").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","pointInTimeFrom").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPointInTimeFrom(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","pointInTimeTo").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPointInTimeTo(
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
           
    
