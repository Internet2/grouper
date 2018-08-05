
/**
 * FindAttributeDefs.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1  Built on : Aug 31, 2011 (12:23:23 CEST)
 */

            
                package edu.internet2.middleware.grouper.ws.soap_v2_4.xsd;
            

            /**
            *  FindAttributeDefs bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class FindAttributeDefs
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                "findAttributeDefs",
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
                        * field for SplitScope
                        */

                        
                                    protected java.lang.String localSplitScope ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSplitScopeTracker = false ;

                           public boolean isSplitScopeSpecified(){
                               return localSplitScopeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getSplitScope(){
                               return localSplitScope;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SplitScope
                               */
                               public void setSplitScope(java.lang.String param){
                            localSplitScopeTracker = true;
                                   
                                            this.localSplitScope=param;
                                    

                               }
                            

                        /**
                        * field for WsAttributeDefLookups
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsAttributeDefLookup[] localWsAttributeDefLookups ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsAttributeDefLookupsTracker = false ;

                           public boolean isWsAttributeDefLookupsSpecified(){
                               return localWsAttributeDefLookupsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsAttributeDefLookup[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsAttributeDefLookup[] getWsAttributeDefLookups(){
                               return localWsAttributeDefLookups;
                           }

                           
                        


                               
                              /**
                               * validate the array for WsAttributeDefLookups
                               */
                              protected void validateWsAttributeDefLookups(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsAttributeDefLookup[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param WsAttributeDefLookups
                              */
                              public void setWsAttributeDefLookups(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsAttributeDefLookup[] param){
                              
                                   validateWsAttributeDefLookups(param);

                               localWsAttributeDefLookupsTracker = true;
                                      
                                      this.localWsAttributeDefLookups=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsAttributeDefLookup
                             */
                             public void addWsAttributeDefLookups(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsAttributeDefLookup param){
                                   if (localWsAttributeDefLookups == null){
                                   localWsAttributeDefLookups = new edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsAttributeDefLookup[]{};
                                   }

                            
                                 //update the setting tracker
                                localWsAttributeDefLookupsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localWsAttributeDefLookups);
                               list.add(param);
                               this.localWsAttributeDefLookups =
                             (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsAttributeDefLookup[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsAttributeDefLookup[list.size()]);

                             }
                             

                        /**
                        * field for PrivilegeName
                        */

                        
                                    protected java.lang.String localPrivilegeName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPrivilegeNameTracker = false ;

                           public boolean isPrivilegeNameSpecified(){
                               return localPrivilegeNameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPrivilegeName(){
                               return localPrivilegeName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PrivilegeName
                               */
                               public void setPrivilegeName(java.lang.String param){
                            localPrivilegeNameTracker = true;
                                   
                                            this.localPrivilegeName=param;
                                    

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
                        * field for ParentStemId
                        */

                        
                                    protected java.lang.String localParentStemId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localParentStemIdTracker = false ;

                           public boolean isParentStemIdSpecified(){
                               return localParentStemIdTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getParentStemId(){
                               return localParentStemId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ParentStemId
                               */
                               public void setParentStemId(java.lang.String param){
                            localParentStemIdTracker = true;
                                   
                                            this.localParentStemId=param;
                                    

                               }
                            

                        /**
                        * field for FindByUuidOrName
                        */

                        
                                    protected java.lang.String localFindByUuidOrName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localFindByUuidOrNameTracker = false ;

                           public boolean isFindByUuidOrNameSpecified(){
                               return localFindByUuidOrNameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getFindByUuidOrName(){
                               return localFindByUuidOrName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param FindByUuidOrName
                               */
                               public void setFindByUuidOrName(java.lang.String param){
                            localFindByUuidOrNameTracker = true;
                                   
                                            this.localFindByUuidOrName=param;
                                    

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
                        * field for ActAsSubjectLookup
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsSubjectLookup localActAsSubjectLookup ;
                                
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
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsSubjectLookup
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsSubjectLookup getActAsSubjectLookup(){
                               return localActAsSubjectLookup;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ActAsSubjectLookup
                               */
                               public void setActAsSubjectLookup(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsSubjectLookup param){
                            localActAsSubjectLookupTracker = true;
                                   
                                            this.localActAsSubjectLookup=param;
                                    

                               }
                            

                        /**
                        * field for Params
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsParam[] localParams ;
                                
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
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsParam[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsParam[] getParams(){
                               return localParams;
                           }

                           
                        


                               
                              /**
                               * validate the array for Params
                               */
                              protected void validateParams(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsParam[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param Params
                              */
                              public void setParams(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsParam[] param){
                              
                                   validateParams(param);

                               localParamsTracker = true;
                                      
                                      this.localParams=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsParam
                             */
                             public void addParams(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsParam param){
                                   if (localParams == null){
                                   localParams = new edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsParam[]{};
                                   }

                            
                                 //update the setting tracker
                                localParamsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localParams);
                               list.add(param);
                               this.localParams =
                             (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsParam[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsParam[list.size()]);

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
               

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":findAttributeDefs",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "findAttributeDefs",
                           xmlWriter);
                   }

               
                   }
                if (localClientVersionTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "clientVersion", xmlWriter);
                             

                                          if (localClientVersion==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localClientVersion);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localScopeTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "scope", xmlWriter);
                             

                                          if (localScope==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localScope);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSplitScopeTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "splitScope", xmlWriter);
                             

                                          if (localSplitScope==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localSplitScope);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localWsAttributeDefLookupsTracker){
                                       if (localWsAttributeDefLookups!=null){
                                            for (int i = 0;i < localWsAttributeDefLookups.length;i++){
                                                if (localWsAttributeDefLookups[i] != null){
                                                 localWsAttributeDefLookups[i].serialize(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","wsAttributeDefLookups"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd", "wsAttributeDefLookups", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd", "wsAttributeDefLookups", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localPrivilegeNameTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "privilegeName", xmlWriter);
                             

                                          if (localPrivilegeName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPrivilegeName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localStemScopeTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "stemScope", xmlWriter);
                             

                                          if (localStemScope==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localStemScope);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localParentStemIdTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "parentStemId", xmlWriter);
                             

                                          if (localParentStemId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localParentStemId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localFindByUuidOrNameTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "findByUuidOrName", xmlWriter);
                             

                                          if (localFindByUuidOrName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localFindByUuidOrName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPageSizeTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "pageSize", xmlWriter);
                             

                                          if (localPageSize==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPageSize);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPageNumberTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "pageNumber", xmlWriter);
                             

                                          if (localPageNumber==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPageNumber);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSortStringTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "sortString", xmlWriter);
                             

                                          if (localSortString==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localSortString);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAscendingTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "ascending", xmlWriter);
                             

                                          if (localAscending==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAscending);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localActAsSubjectLookupTracker){
                                    if (localActAsSubjectLookup==null){

                                        writeStartElement(null, "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd", "actAsSubjectLookup", xmlWriter);

                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localActAsSubjectLookup.serialize(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","actAsSubjectLookup"),
                                        xmlWriter);
                                    }
                                } if (localParamsTracker){
                                       if (localParams!=null){
                                            for (int i = 0;i < localParams.length;i++){
                                                if (localParams[i] != null){
                                                 localParams[i].serialize(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","params"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd", "params", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd", "params", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 }
                    xmlWriter.writeEndElement();
               

        }

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd")){
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
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "clientVersion"));
                                 
                                         elementList.add(localClientVersion==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localClientVersion));
                                    } if (localScopeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "scope"));
                                 
                                         elementList.add(localScope==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localScope));
                                    } if (localSplitScopeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "splitScope"));
                                 
                                         elementList.add(localSplitScope==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSplitScope));
                                    } if (localWsAttributeDefLookupsTracker){
                             if (localWsAttributeDefLookups!=null) {
                                 for (int i = 0;i < localWsAttributeDefLookups.length;i++){

                                    if (localWsAttributeDefLookups[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAttributeDefLookups"));
                                         elementList.add(localWsAttributeDefLookups[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAttributeDefLookups"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAttributeDefLookups"));
                                        elementList.add(localWsAttributeDefLookups);
                                    
                             }

                        } if (localPrivilegeNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "privilegeName"));
                                 
                                         elementList.add(localPrivilegeName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPrivilegeName));
                                    } if (localStemScopeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "stemScope"));
                                 
                                         elementList.add(localStemScope==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localStemScope));
                                    } if (localParentStemIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "parentStemId"));
                                 
                                         elementList.add(localParentStemId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localParentStemId));
                                    } if (localFindByUuidOrNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "findByUuidOrName"));
                                 
                                         elementList.add(localFindByUuidOrName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localFindByUuidOrName));
                                    } if (localPageSizeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "pageSize"));
                                 
                                         elementList.add(localPageSize==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPageSize));
                                    } if (localPageNumberTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "pageNumber"));
                                 
                                         elementList.add(localPageNumber==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPageNumber));
                                    } if (localSortStringTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "sortString"));
                                 
                                         elementList.add(localSortString==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSortString));
                                    } if (localAscendingTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "ascending"));
                                 
                                         elementList.add(localAscending==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAscending));
                                    } if (localActAsSubjectLookupTracker){
                            elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "actAsSubjectLookup"));
                            
                            
                                    elementList.add(localActAsSubjectLookup==null?null:
                                    localActAsSubjectLookup);
                                } if (localParamsTracker){
                             if (localParams!=null) {
                                 for (int i = 0;i < localParams.length;i++){

                                    if (localParams[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "params"));
                                         elementList.add(localParams[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "params"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "params"));
                                        elementList.add(localParams);
                                    
                             }

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
        public static FindAttributeDefs parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            FindAttributeDefs object =
                new FindAttributeDefs();

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
                    
                            if (!"findAttributeDefs".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (FindAttributeDefs)edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    
                    reader.next();
                
                        java.util.ArrayList list4 = new java.util.ArrayList();
                    
                        java.util.ArrayList list14 = new java.util.ArrayList();
                    
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","clientVersion").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","scope").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","splitScope").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setSplitScope(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","wsAttributeDefLookups").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list4.add(null);
                                                              reader.next();
                                                          } else {
                                                        list4.add(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsAttributeDefLookup.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone4 = false;
                                                        while(!loopDone4){
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
                                                                loopDone4 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","wsAttributeDefLookups").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list4.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list4.add(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsAttributeDefLookup.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone4 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setWsAttributeDefLookups((edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsAttributeDefLookup[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsAttributeDefLookup.class,
                                                                list4));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","privilegeName").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPrivilegeName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","stemScope").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","parentStemId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setParentStemId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","findByUuidOrName").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setFindByUuidOrName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","pageSize").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","pageNumber").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","sortString").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","ascending").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","actAsSubjectLookup").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setActAsSubjectLookup(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setActAsSubjectLookup(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsSubjectLookup.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","params").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list14.add(null);
                                                              reader.next();
                                                          } else {
                                                        list14.add(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsParam.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone14 = false;
                                                        while(!loopDone14){
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
                                                                loopDone14 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","params").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list14.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list14.add(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsParam.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone14 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setParams((edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsParam[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsParam.class,
                                                                list14));
                                                            
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
           
    