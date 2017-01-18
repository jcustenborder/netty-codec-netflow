/**
 * Copyright (C) 2017 Jeremy Custenborder (jcustenborder@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jcustenborder.netty.netflow.v9.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.net.InetSocketAddress;

public class InetSocketAddressStorage {
  public String hostname;
  public int port;


  public static class Serializer extends JsonSerializer<java.net.InetSocketAddress> {
    @Override
    public void serialize(java.net.InetSocketAddress inetSocketAddress, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
      InetSocketAddressStorage storage = new InetSocketAddressStorage();
      storage.hostname = inetSocketAddress.getHostName();
      storage.port = inetSocketAddress.getPort();
      jsonGenerator.writeObject(storage);
    }
  }

  public static class Deserializer extends JsonDeserializer<java.net.InetSocketAddress> {

    @Override
    public java.net.InetSocketAddress deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
      InetSocketAddressStorage storage = jsonParser.readValueAs(InetSocketAddressStorage.class);
      return new InetSocketAddress(storage.hostname, storage.port);
    }
  }

}
