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
import com.github.jcustenborder.netty.netflow.v9.NetFlowV9Decoder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NetflowMessageStorage {
  public short version;
  public short count;
  public int uptime;
  public int timestamp;
  public int flowSequence;
  public int sourceID;
  public InetSocketAddress sender;
  public InetSocketAddress recipient;
  public List<NetFlowV9Decoder.FlowSet> flowsets;

  public static class Serializer extends JsonSerializer<NetFlowV9Decoder.NetFlowMessage> {
    @Override
    public void serialize(NetFlowV9Decoder.NetFlowMessage header, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
      NetflowMessageStorage storage = new NetflowMessageStorage();
      storage.count = header.count();
      storage.flowSequence = header.flowSequence();
      storage.recipient = header.recipient();
      storage.sender = header.sender();
      storage.sourceID = header.sourceID();
      storage.timestamp = header.timestamp();
      storage.uptime = header.uptime();
      storage.version = header.version();
      storage.flowsets = header.flowsets();
      jsonGenerator.writeObject(storage);
    }
  }

  public static class Deserializer extends JsonDeserializer<NetFlowV9Decoder.NetFlowMessage> {

    @Override
    public NetFlowV9Decoder.NetFlowMessage deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
      NetflowMessageStorage storage = jsonParser.readValueAs(NetflowMessageStorage.class);
      NetFlowV9Decoder.NetFlowMessage header = mock(NetFlowV9Decoder.NetFlowMessage.class);
      when(header.count()).thenReturn(storage.count);
      when(header.flowSequence()).thenReturn(storage.flowSequence);
      when(header.recipient()).thenReturn(storage.recipient);
      when(header.sender()).thenReturn(storage.sender);
      when(header.sourceID()).thenReturn(storage.sourceID);
      when(header.timestamp()).thenReturn(storage.timestamp);
      when(header.uptime()).thenReturn(storage.uptime);
      when(header.version()).thenReturn(storage.version);
      when(header.flowsets()).thenReturn(storage.flowsets);
      return header;
    }
  }

}
