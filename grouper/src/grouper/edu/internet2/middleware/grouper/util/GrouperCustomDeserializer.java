package edu.internet2.middleware.grouper.util;

import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

public class GrouperCustomDeserializer extends KeyDeserializer {

  @Override
  public Object deserializeKey(String key, DeserializationContext ctxt)
      throws IOException {
    System.out.println("key is "+key);
    return key;
  }

}

