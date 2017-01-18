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
package com.github.jcustenborder.netty.netflow.v9;

import com.github.jcustenborder.netty.netflow.v9.json.ObjectMapperSingleton;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;

public class NetFlowV9DecoderTest {
  private static final Logger log = LoggerFactory.getLogger(NetFlowV9DecoderTest.class);

  NetFlowV9Decoder decoder = new NetFlowV9Decoder();


  TestCase readTestCase(String name) throws IOException {
    try (InputStream inputStream = this.getClass().getResourceAsStream(name)) {
      return ObjectMapperSingleton.instance.readValue(inputStream, TestCase.class);
    }
  }

  @TestFactory
  public Stream<DynamicTest> decode() throws Exception {
    final List<String> tests = Arrays.asList("testcase001.json");
    return tests.stream().map(testfile -> dynamicTest(testfile, () -> decode(testfile)));
  }

  void assertMessage(final NetFlowV9Decoder.NetFlowMessage expected, final NetFlowV9Decoder.NetFlowMessage actual) {
    assertEquals(expected.count(), actual.count(), "count does not match.");
    assertEquals(expected.version(), actual.version(), "version does not match.");
    assertEquals(expected.flowSequence(), actual.flowSequence(), "flowSequence does not match.");
    assertEquals(expected.recipient(), actual.recipient(), "recipient does not match.");
    assertEquals(expected.sender(), actual.sender(), "sender does not match.");
    assertEquals(expected.sourceID(), actual.sourceID(), "sourceID does not match.");
    assertEquals(expected.timestamp(), actual.timestamp(), "timestamp does not match.");
    assertEquals(expected.uptime(), actual.uptime(), "uptime does not match.");
    assertEquals(expected.flowsets().size(), actual.flowsets().size(), "flowsets.size() does not match.");

    for (int i = 0; i < expected.flowsets().size(); i++) {
      NetFlowV9Decoder.FlowSet expectedFlowset = expected.flowsets().get(i);
      NetFlowV9Decoder.FlowSet actualFlowset = actual.flowsets().get(i);
      assertFlowset(expectedFlowset, actualFlowset);
    }
  }

  void assertDataFlowSet(NetFlowV9Decoder.DataFlowSet expected, NetFlowV9Decoder.DataFlowSet actual) {
    assertArrayEquals(expected.data(), actual.data(), "data does not match.");
  }

  void assertTemplateField(NetFlowV9Decoder.TemplateField expected, NetFlowV9Decoder.TemplateField actual) {
    assertEquals(expected.type(), actual.type(), "type does not match");
    assertEquals(expected.length(), actual.length(), "length does not match");
  }

  void assertTemplateFlowSet(NetFlowV9Decoder.TemplateFlowSet expected, NetFlowV9Decoder.TemplateFlowSet actual) {
    assertEquals(expected.templateID(), actual.templateID(), "templateID does not match.");
    assertEquals(expected.fields().size(), actual.fields().size(), "fields().size() does not match");

    for (int i = 0; i < expected.fields().size(); i++) {
      NetFlowV9Decoder.TemplateField expectedTemplateField = expected.fields().get(i);
      NetFlowV9Decoder.TemplateField actualTemplateField = actual.fields().get(i);
      assertTemplateField(expectedTemplateField, actualTemplateField);
    }
  }

  void assertFlowset(NetFlowV9Decoder.FlowSet expected, NetFlowV9Decoder.FlowSet actual) {
    assertEquals(expected.flowsetID(), actual.flowsetID(), "flowsetID does not match.");

    if (expected instanceof NetFlowV9Decoder.DataFlowSet) {
      assertDataFlowSet((NetFlowV9Decoder.DataFlowSet) expected, (NetFlowV9Decoder.DataFlowSet) actual);
    } else if (expected instanceof NetFlowV9Decoder.TemplateFlowSet) {
      assertTemplateFlowSet((NetFlowV9Decoder.TemplateFlowSet) expected, (NetFlowV9Decoder.TemplateFlowSet) actual);
    }
  }

  void decode(String testCaseFile) throws Exception {
    TestCase testCase = readTestCase(testCaseFile);
    ByteBuf byteBuf = testCase.byteBuf();
    log.trace("length {}", byteBuf.readableBytes());

    InetSocketAddress sender = new InetSocketAddress("8.8.8.8", 64321);
    InetSocketAddress recipient = new InetSocketAddress("8.8.4.4", 2055);
    DatagramPacket datagramPacket = new DatagramPacket(byteBuf, recipient, sender);

    ChannelHandlerContext channelHandlerContext = mock(ChannelHandlerContext.class);
    List<Object> list = new ArrayList<>();
    this.decoder.decode(
        channelHandlerContext,
        datagramPacket,
        list
    );
    assertEquals(0, datagramPacket.content().readableBytes(), "readableBytes should be 0.");
    assertEquals(1, list.size(), "list size does not match.");
    assertTrue(list.get(0) instanceof NetFlowV9Decoder.NetFlowMessage, "Object must be instanceof NetFlowMessage.");
    NetFlowV9Decoder.NetFlowMessage actual = (NetFlowV9Decoder.NetFlowMessage) list.get(0);
//    log.trace(ObjectMapperSingleton.instance.writeValueAsString(actual));
    assertMessage(testCase.expected, actual);
  }


  public static class TestCase {
    public byte[] input;
    public NetFlowV9Decoder.NetFlowMessage expected;

    public ByteBuf byteBuf() {
      return Unpooled.wrappedBuffer(this.input);
    }
  }
}
