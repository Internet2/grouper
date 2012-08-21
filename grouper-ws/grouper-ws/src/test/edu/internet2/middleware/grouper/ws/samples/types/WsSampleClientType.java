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
/*
 * @author mchyzer
 * $Id: WsSampleClientType.java,v 1.2 2008-03-31 17:51:44 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.samples.types;

import java.io.File;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;


/**
 * which type of client are we capturing
 */
public enum WsSampleClientType {

   /** generated capture type */
   GENERATED_SOAP {
     /**
      * formats for this client type
      * @return the formats (generally enums)
      */
     @Override
     public Object[] formats() {
       return new Object[]{WsSampleGeneratedType.soap};
     }
     
     /**
      * execute a sample based on format
      * @param clientClass
      * @param format
      */
     @Override
     public void executeSample(Class<? extends WsSample> clientClass, Object format) {
       WsSampleGenerated wsSampleGenerated = (WsSampleGenerated)GrouperUtil.newInstance(clientClass);
       WsSampleGeneratedType wsSampleGeneratedType = (WsSampleGeneratedType)format;
       wsSampleGenerated.executeSample(wsSampleGeneratedType);
     }

     /**
      * friendly name
      * @return friendly
      */
     @Override
     public String friendlyName() {
       return "code generated classes";
     }


     /**
      * find the source file from the client class
      * @param clientClass is the client which is running
      * @return the source file
      */
     @Override
     public File sourceFile(Class<?> clientClass) {
       String projectDir = GrouperWsConfig.retrieveConfig().propertyValueString("ws.testing.generated.client.dir");
       File file = new File(projectDir + "/src/" + clientClass.getName().replace('.', '/') + ".java");
       if (!file.exists()) {
         throw new RuntimeException("Source file doesnt exist: " + file.getAbsolutePath());
       }
       return file;
     }
     /**
      * see if this client type supports this class and type
      * @param format
      * @param clientClass
      * @return
      */
     @Override
     public boolean validFormat(Class<?> clientClass, Object format) {
       //all formats supported
       return true;
     }
   },
   
   /** manual rest */
   REST_BEANS {
     /**
      * formats for this client type
      * @return the formats (generally enums)
      */
     @Override
     public Object[] formats() {
       return WsSampleRestType.values();
     }
     
     /**
      * execute a sample based on format
      * @param clientClass
      * @param format
      */
     @Override
     public void executeSample(Class<? extends WsSample> clientClass, Object format) {
       WsSampleRest wsSampleRest = (WsSampleRest)GrouperUtil.newInstance(clientClass);
       WsSampleRestType wsSampleRestType = (WsSampleRestType)format;
       wsSampleRest.executeSample(wsSampleRestType);
     }

     /**
      * friendly name
      * @return friendly
      */
     @Override
     public String friendlyName() {
       return "manually written lite/rest";
     }


     /**
      * find the source file from the client class
      * @param clientClass is the client which is running
      * @return the source file
      */
     @Override
     public File sourceFile(Class<?> clientClass) {
       //src\test\edu\internet2\middleware\grouper\ws\samples\rest
       File file = new File(GrouperWsConfig.retrieveConfig().propertyValueString("ws.testing.grouper-ws.dir") + 
           "/src/test/" + clientClass.getName().replace('.', '/') + ".java");
       if (!file.exists()) {
         throw new RuntimeException("Source file doesnt exist: " + file.getAbsolutePath());
       }
       return file;
     }
     /**
      * see if this client type supports this class and type
      * @param format
      * @param clientClass
      * @return
      */
     @Override
     public boolean validFormat(Class<?> clientClass, Object format) {
       //see if format supported
       WsSampleRest wsSampleRest = (WsSampleRest)GrouperUtil.newInstance(clientClass);
       WsSampleRestType wsSampleRestType = (WsSampleRestType)format;
       return wsSampleRest.validType(wsSampleRestType);
     }
   },
   
   /** manual capture type (http client) */
   MANUAL_HTTP {
     /**
      * formats for this client type
      * @return the formats (generally enums)
      */
     @Override
     public Object[] formats() {
       return null;
     }
     
     /**
      * execute a sample based on format
      * @param clientClass
      * @param format
      */
     @Override
     public void executeSample(Class<? extends WsSample> clientClass, Object format) {
       GrouperUtil.assertion(format == null, "no formats for http / xml");
       WsSampleManualHttp wsSampleManualXmlHttp = (WsSampleManualHttp)GrouperUtil.newInstance(clientClass);
       wsSampleManualXmlHttp.executeSample();
     }

     /**
      * friendly name
      * @return friendly
      */
     @Override
     public String friendlyName() {
       return "manually written xml/http";
     }

     /**
      * find the source file from the client class
      * @param clientClass is the client which is running
      * @return the source file
      */
     @Override
     public File sourceFile(Class<?> clientClass) {
       String projectDir = GrouperWsConfig.retrieveConfig().propertyValueString("ws.testing.manual.client.dir");
       File file = new File(projectDir + "/src/java-manual-client/" + clientClass.getName().replace('.', '/') + ".java");
       if (!file.exists()) {
         throw new RuntimeException("Source file doesnt exist: " + file.getAbsolutePath());
       }
       return file;
     }
     /**
      * see if this client type supports this class and type
      * @param format
      * @param clientClass
      * @return
      */
     @Override
     public boolean validFormat(Class<?> clientClass, Object format) {
       //all formats supported
       return true;
     }
   };

   /**
    * formats for this client type
    * @return the formats (generally enums)
    */
   public abstract Object[] formats();
   
   /**
    * execute a sample based on format
    * @param clientClass
    * @param format
    */
   public abstract void executeSample(Class<? extends WsSample> clientClass, Object format);
   
   /**
    * friendly name
    * @return friendly
    */
   public abstract String friendlyName();

   /**
    * find the source file from the client class
    * @param clientClass is the client which is running
    * @return the source file
    */
   public abstract File sourceFile(Class<?> clientClass);
   
   /**
    * see if this client type supports this class and type
    * @param format
    * @param clientClass
    * @return
    */
   public abstract boolean validFormat(Class<?> clientClass, Object format);
} 
