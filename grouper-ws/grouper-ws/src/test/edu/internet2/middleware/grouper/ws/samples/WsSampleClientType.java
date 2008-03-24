/*
 * @author mchyzer
 * $Id: WsSampleClientType.java,v 1.1 2008-03-24 20:19:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.samples;

import java.io.File;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;


/**
 * which type of client are we capturing
 */
public enum WsSampleClientType {

   /** generated capture type */
   GENERATED_SOAP_XML_HTTP {
     /**
      * formats for this client type
      * @return the formats (generally enums)
      */
     @Override
     public Object[] formats() {
       return WsSampleGeneratedType.values();
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
       String projectDir = GrouperWsConfig.getPropertyString("ws.testing.generated.client.dir");
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
   MANUAL_LITE_REST {
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
       File file = new File("src/test/" + clientClass.getName().replace('.', '/') + ".java");
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
   MANUAL_XML_HTTP {
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
       WsSampleManualXmlHttp wsSampleManualXmlHttp = (WsSampleManualXmlHttp)GrouperUtil.newInstance(clientClass);
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
       String projectDir = GrouperWsConfig.getPropertyString("ws.testing.manual.client.dir");
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
