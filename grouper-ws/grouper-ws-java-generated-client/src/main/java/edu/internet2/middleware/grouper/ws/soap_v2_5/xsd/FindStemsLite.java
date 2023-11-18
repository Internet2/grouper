
/**
 * FindStemsLite.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1  Built on : Aug 31, 2011 (12:23:23 CEST)
 */

            
                package edu.internet2.middleware.grouper.ws.soap_v2_5.xsd;
            

            /**
            *  FindStemsLite bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class FindStemsLite
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                "findStemsLite",
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
                        * field for StemQueryFilterType
                        */

                        
                                    protected java.lang.String localStemQueryFilterType ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localStemQueryFilterTypeTracker = false ;

                           public boolean isStemQueryFilterTypeSpecified(){
                               return localStemQueryFilterTypeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getStemQueryFilterType(){
                               return localStemQueryFilterType;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param StemQueryFilterType
                               */
                               public void setStemQueryFilterType(java.lang.String param){
                            localStemQueryFilterTypeTracker = true;
                                   
                                            this.localStemQueryFilterType=param;
                                    

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
                        * field for ParentStemName
                        */

                        
                                    protected java.lang.String localParentStemName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localParentStemNameTracker = false ;

                           public boolean isParentStemNameSpecified(){
                               return localParentStemNameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getParentStemName(){
                               return localParentStemName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ParentStemName
                               */
                               public void setParentStemName(java.lang.String param){
                            localParentStemNameTracker = true;
                                   
                                            this.localParentStemName=param;
                                    

                               }
                            

                        /**
                        * field for ParentStemNameScope
                        */

                        
                                    protected java.lang.String localParentStemNameScope ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localParentStemNameScopeTracker = false ;

                           public boolean isParentStemNameScopeSpecified(){
                               return localParentStemNameScopeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getParentStemNameScope(){
                               return localParentStemNameScope;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ParentStemNameScope
                               */
                               public void setParentStemNameScope(java.lang.String param){
                            localParentStemNameScopeTracker = true;
                                   
                                            this.localParentStemNameScope=param;
                                    

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
                        * field for StemAttributeName
                        */

                        
                                    protected java.lang.String localStemAttributeName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localStemAttributeNameTracker = false ;

                           public boolean isStemAttributeNameSpecified(){
                               return localStemAttributeNameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getStemAttributeName(){
                               return localStemAttributeName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param StemAttributeName
                               */
                               public void setStemAttributeName(java.lang.String param){
                            localStemAttributeNameTracker = true;
                                   
                                            this.localStemAttributeName=param;
                                    

                               }
                            

                        /**
                        * field for StemAttributeValue
                        */

                        
                                    protected java.lang.String localStemAttributeValue ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localStemAttributeValueTracker = false ;

                           public boolean isStemAttributeValueSpecified(){
                               return localStemAttributeValueTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getStemAttributeValue(){
                               return localStemAttributeValue;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param StemAttributeValue
                               */
                               public void setStemAttributeValue(java.lang.String param){
                            localStemAttributeValueTracker = true;
                                   
                                            this.localStemAttributeValue=param;
                                    

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
                           namespacePrefix+":findStemsLite",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "findStemsLite",
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
                             } if (localStemQueryFilterTypeTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "stemQueryFilterType", xmlWriter);
                             

                                          if (localStemQueryFilterType==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localStemQueryFilterType);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localStemNameTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "stemName", xmlWriter);
                             

                                          if (localStemName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localStemName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localParentStemNameTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "parentStemName", xmlWriter);
                             

                                          if (localParentStemName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localParentStemName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localParentStemNameScopeTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "parentStemNameScope", xmlWriter);
                             

                                          if (localParentStemNameScope==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localParentStemNameScope);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localStemUuidTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "stemUuid", xmlWriter);
                             

                                          if (localStemUuid==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localStemUuid);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localStemAttributeNameTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "stemAttributeName", xmlWriter);
                             

                                          if (localStemAttributeName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localStemAttributeName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localStemAttributeValueTracker){
                                    namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "stemAttributeValue", xmlWriter);
                             

                                          if (localStemAttributeValue==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localStemAttributeValue);
                                            
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
                                    } if (localStemQueryFilterTypeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "stemQueryFilterType"));
                                 
                                         elementList.add(localStemQueryFilterType==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localStemQueryFilterType));
                                    } if (localStemNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "stemName"));
                                 
                                         elementList.add(localStemName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localStemName));
                                    } if (localParentStemNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "parentStemName"));
                                 
                                         elementList.add(localParentStemName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localParentStemName));
                                    } if (localParentStemNameScopeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "parentStemNameScope"));
                                 
                                         elementList.add(localParentStemNameScope==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localParentStemNameScope));
                                    } if (localStemUuidTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "stemUuid"));
                                 
                                         elementList.add(localStemUuid==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localStemUuid));
                                    } if (localStemAttributeNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "stemAttributeName"));
                                 
                                         elementList.add(localStemAttributeName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localStemAttributeName));
                                    } if (localStemAttributeValueTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "stemAttributeValue"));
                                 
                                         elementList.add(localStemAttributeValue==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localStemAttributeValue));
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
        public static FindStemsLite parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            FindStemsLite object =
                new FindStemsLite();

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
                    
                            if (!"findStemsLite".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (FindStemsLite)edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.ExtensionMapper.getTypeObject(
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","stemQueryFilterType").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setStemQueryFilterType(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","stemName").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","parentStemName").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setParentStemName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","parentStemNameScope").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setParentStemNameScope(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","stemUuid").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","stemAttributeName").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setStemAttributeName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","stemAttributeValue").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setStemAttributeValue(
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
           
    