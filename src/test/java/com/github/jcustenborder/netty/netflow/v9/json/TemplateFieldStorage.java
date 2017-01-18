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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TemplateFieldStorage {
  public short type;
  public short length;

  public static class Serializer extends JsonSerializer<NetFlowV9Decoder.TemplateField> {
    @Override
    public void serialize(NetFlowV9Decoder.TemplateField field, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
      TemplateFieldStorage storage = new TemplateFieldStorage();
      storage.type = field.type();
      storage.length = field.length();
      jsonGenerator.writeObject(storage);
    }
  }

  public static class Deserializer extends JsonDeserializer<NetFlowV9Decoder.TemplateField> {

    @Override
    public NetFlowV9Decoder.TemplateField deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
      TemplateFieldStorage storage = jsonParser.readValueAs(TemplateFieldStorage.class);
      NetFlowV9Decoder.TemplateField field = mock(NetFlowV9Decoder.TemplateField.class);
      when(field.type()).thenReturn(storage.type);
      when(field.length()).thenReturn(storage.length);
      return field;
    }
  }

}
