
/**
 * GetGroups.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1  Built on : Aug 31, 2011 (12:23:23 CEST)
 */

            
                package edu.internet2.middleware.grouper.ws.soap_v2_1.xsd;
            

            /**
            *  GetGroups bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class GetGroups
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                "getGroups",
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
                        * field for SubjectLookups
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup[] localSubjectLookups ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSubjectLookupsTracker = false ;

                           public boolean isSubjectLookupsSpecified(){
                               return localSubjectLookupsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup[] getSubjectLookups(){
                               return localSubjectLookups;
                           }

                           
                        


                               
                              /**
                               * validate the array for SubjectLookups
                               */
                              protected void validateSubjectLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param SubjectLookups
                              */
                              public void setSubjectLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup[] param){
                              
                                   validateSubjectLookups(param);

                               localSubjectLookupsTracker = true;
                                      
                                      this.localSubjectLookups=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup
                             */
                             public void addSubjectLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup param){
                                   if (localSubjectLookups == null){
                                   localSubjectLookups = new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup[]{};
                                   }

                            
                                 //update the setting tracker
                                localSubjectLookupsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localSubjectLookups);
                               list.add(param);
                               this.localSubjectLookups =
                             (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup[list.size()]);

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
                        * field for ActAsSubjectLookup
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup localActAsSubjectLookup ;
                                
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
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup getActAsSubjectLookup(){
                               return localActAsSubjectLookup;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ActAsSubjectLookup
                               */
                               public void setActAsSubjectLookup(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup param){
                            localActAsSubjectLookupTracker = true;
                                   
                                            this.localActAsSubjectLookup=param;
                                    

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
                        * field for Params
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam[] localParams ;
                                
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
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam[] getParams(){
                               return localParams;
                           }

                           
                        


                               
                              /**
                               * validate the array for Params
                               */
                              protected void validateParams(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param Params
                              */
                              public void setParams(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam[] param){
                              
                                   validateParams(param);

                               localParamsTracker = true;
                                      
                                      this.localParams=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam
                             */
                             public void addParams(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam param){
                                   if (localParams == null){
                                   localParams = new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam[]{};
                                   }

                            
                                 //update the setting tracker
                                localParamsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localParams);
                               list.add(param);
                               this.localParams =
                             (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam[list.size()]);

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
                        * field for WsStemLookup
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsStemLookup localWsStemLookup ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsStemLookupTracker = false ;

                           public boolean isWsStemLookupSpecified(){
                               return localWsStemLookupTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsStemLookup
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsStemLookup getWsStemLookup(){
                               return localWsStemLookup;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param WsStemLookup
                               */
                               public void setWsStemLookup(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsStemLookup param){
                            localWsStemLookupTracker = true;
                                   
                                            this.localWsStemLookup=param;
                                    

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
                           namespacePrefix+":getGroups",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "getGroups",
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
                             } if (localSubjectLookupsTracker){
                                       if (localSubjectLookups!=null){
                                            for (int i = 0;i < localSubjectLookups.length;i++){
                                                if (localSubjectLookups[i] != null){
                                                 localSubjectLookups[i].serialize(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","subjectLookups"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "subjectLookups", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "subjectLookups", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
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
                             } if (localActAsSubjectLookupTracker){
                                    if (localActAsSubjectLookup==null){

                                        writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "actAsSubjectLookup", xmlWriter);

                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localActAsSubjectLookup.serialize(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","actAsSubjectLookup"),
                                        xmlWriter);
                                    }
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
                             if (localSubjectAttributeNames!=null) {
                                   namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                   for (int i = 0;i < localSubjectAttributeNames.length;i++){
                                        
                                            if (localSubjectAttributeNames[i] != null){
                                        
                                                writeStartElement(null, namespace, "subjectAttributeNames", xmlWriter);

                                            
                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSubjectAttributeNames[i]));
                                                    
                                                xmlWriter.writeEndElement();
                                              
                                                } else {
                                                   
                                                           // write null attribute
                                                            namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                                            writeStartElement(null, namespace, "subjectAttributeNames", xmlWriter);
                                                            writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                            xmlWriter.writeEndElement();
                                                       
                                                }

                                   }
                             } else {
                                 
                                         // write the null attribute
                                        // write null attribute
                                           writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "subjectAttributeNames", xmlWriter);

                                           // write the nil attribute
                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                           xmlWriter.writeEndElement();
                                    
                             }

                        } if (localParamsTracker){
                                       if (localParams!=null){
                                            for (int i = 0;i < localParams.length;i++){
                                                if (localParams[i] != null){
                                                 localParams[i].serialize(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","params"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "params", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "params", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
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
                             } if (localWsStemLookupTracker){
                                    if (localWsStemLookup==null){

                                        writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "wsStemLookup", xmlWriter);

                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localWsStemLookup.serialize(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsStemLookup"),
                                        xmlWriter);
                                    }
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
                                    } if (localSubjectLookupsTracker){
                             if (localSubjectLookups!=null) {
                                 for (int i = 0;i < localSubjectLookups.length;i++){

                                    if (localSubjectLookups[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "subjectLookups"));
                                         elementList.add(localSubjectLookups[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "subjectLookups"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "subjectLookups"));
                                        elementList.add(localSubjectLookups);
                                    
                             }

                        } if (localMemberFilterTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "memberFilter"));
                                 
                                         elementList.add(localMemberFilter==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localMemberFilter));
                                    } if (localActAsSubjectLookupTracker){
                            elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "actAsSubjectLookup"));
                            
                            
                                    elementList.add(localActAsSubjectLookup==null?null:
                                    localActAsSubjectLookup);
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
                            if (localSubjectAttributeNames!=null){
                                  for (int i = 0;i < localSubjectAttributeNames.length;i++){
                                      
                                         if (localSubjectAttributeNames[i] != null){
                                          elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "subjectAttributeNames"));
                                          elementList.add(
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSubjectAttributeNames[i]));
                                          } else {
                                             
                                                    elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "subjectAttributeNames"));
                                                    elementList.add(null);
                                                
                                          }
                                      

                                  }
                            } else {
                              
                                    elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "subjectAttributeNames"));
                                    elementList.add(null);
                                
                            }

                        } if (localParamsTracker){
                             if (localParams!=null) {
                                 for (int i = 0;i < localParams.length;i++){

                                    if (localParams[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "params"));
                                         elementList.add(localParams[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "params"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "params"));
                                        elementList.add(localParams);
                                    
                             }

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
                                    } if (localWsStemLookupTracker){
                            elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "wsStemLookup"));
                            
                            
                                    elementList.add(localWsStemLookup==null?null:
                                    localWsStemLookup);
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
        public static GetGroups parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            GetGroups object =
                new GetGroups();

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
                    
                            if (!"getGroups".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (GetGroups)edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    
                    reader.next();
                
                        java.util.ArrayList list2 = new java.util.ArrayList();
                    
                        java.util.ArrayList list7 = new java.util.ArrayList();
                    
                        java.util.ArrayList list8 = new java.util.ArrayList();
                    
                                    
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","subjectLookups").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list2.add(null);
                                                              reader.next();
                                                          } else {
                                                        list2.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone2 = false;
                                                        while(!loopDone2){
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
                                                                loopDone2 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","subjectLookups").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list2.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list2.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone2 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setSubjectLookups((edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup.class,
                                                                list2));
                                                            
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","actAsSubjectLookup").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setActAsSubjectLookup(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setActAsSubjectLookup(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
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
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                              nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                              if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                  list7.add(null);
                                                       
                                                  reader.next();
                                              } else {
                                            list7.add(reader.getElementText());
                                            }
                                            //loop until we find a start element that is not part of this array
                                            boolean loopDone7 = false;
                                            while(!loopDone7){
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
                                                    loopDone7 = true;
                                                } else {
                                                    if (new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","subjectAttributeNames").equals(reader.getName())){
                                                         
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list7.add(null);
                                                                   
                                                              reader.next();
                                                          } else {
                                                        list7.add(reader.getElementText());
                                                        }
                                                    }else{
                                                        loopDone7 = true;
                                                    }
                                                }
                                            }
                                            // call the converter utility  to convert and set the array
                                            
                                                    object.setSubjectAttributeNames((java.lang.String[])
                                                        list7.toArray(new java.lang.String[list7.size()]));
                                                
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","params").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list8.add(null);
                                                              reader.next();
                                                          } else {
                                                        list8.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone8 = false;
                                                        while(!loopDone8){
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
                                                                loopDone8 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","params").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list8.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list8.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone8 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setParams((edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam.class,
                                                                list8));
                                                            
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsStemLookup").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setWsStemLookup(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setWsStemLookup(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsStemLookup.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
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
           
    