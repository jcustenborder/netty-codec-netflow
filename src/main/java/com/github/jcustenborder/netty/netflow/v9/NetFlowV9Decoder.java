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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class NetFlowV9Decoder extends MessageToMessageDecoder<DatagramPacket> {
  private static final Logger log = LoggerFactory.getLogger(NetFlowV9Decoder.class);

  final NetflowFactory netflowFactory;

  public NetFlowV9Decoder(NetflowFactory netflowFactory) {
    this.netflowFactory = netflowFactory;
  }

  public NetFlowV9Decoder() {
    this(new NetFlowFactoryImpl());
  }

  Header decodeHeader(ByteBuf b, InetSocketAddress sender, InetSocketAddress recipient) {
    final ByteBuf input = b.readSlice(20);

    short version = input.readShort();
    short count = input.readShort();
    int uptime = input.readInt();
    int timestamp = input.readInt();
    int flowSequence = input.readInt();
    int sourceID = input.readInt();

    log.trace("version = {} count = {} uptime = {} timestamp = {} flowSequence = {} sourceID = {}",
        version, count, uptime, timestamp, flowSequence, sourceID
    );

    checkReadFully(input);
    return new Header(version, count, uptime, timestamp, flowSequence, sourceID, sender, recipient);
  }

  private void checkReadFully(ByteBuf input) {
    if (input.readableBytes() > 0) {
      throw new IllegalStateException(
          String.format("input has %s bytes remaining.", input.readableBytes())
      );
    }
  }

  TemplateFlowSet decodeTemplate(ByteBuf b, final short flowSetID) {
    final int length = b.readShort() - 4;
    log.trace("readSlice({})", length);
    final ByteBuf input = b.readSlice(length);

    short templateID = input.readShort();
    short fieldCount = input.readShort();
    log.trace("templateID = {} fieldCount = {}", templateID, fieldCount);
    List<TemplateField> fields = new ArrayList<>(fieldCount);
    for (short j = 1; j <= fieldCount; j++) {
      short fieldType = input.readShort();
      short fieldLength = input.readShort();
      log.trace("field({}/{}): type = {} length = {}", j, fieldCount, fieldType, fieldLength);

      TemplateField templateField = this.netflowFactory.templateField(fieldType, fieldLength);
      fields.add(templateField);
    }
    checkReadFully(input);
    return this.netflowFactory.templateFlowSet(flowSetID, templateID, fields);
  }

  DataFlowSet decodeData(ByteBuf b, final short flowSetID) {
    final int length = b.readShort() - 4;
    log.trace("readSlice({})", length);
    final ByteBuf input = b.readSlice(length);
    byte[] data = new byte[length];
    input.readBytes(data);
    return this.netflowFactory.dataFlowSet(flowSetID, data);
  }

  @Override
  protected void decode(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket, List<Object> output) throws Exception {
    ByteBuf input = datagramPacket.content();

    if (null == input || !input.isReadable()) {
      log.trace("Message from {} was not usable.", datagramPacket.sender());
      return;
    }

    final Header header = decodeHeader(input, datagramPacket.sender(), datagramPacket.recipient());

    log.trace("Read {} for header. {} remaining", input.readerIndex(), input.readableBytes());

    List<FlowSet> flowSets = new ArrayList<>();

    while (input.readableBytes() > 0) {
      final short flowsetID = input.readShort();
      log.trace("Processing flowset {}", flowsetID);

      if (0 == flowsetID) {
        TemplateFlowSet templateFlowSet = decodeTemplate(input, flowsetID);
        flowSets.add(templateFlowSet);
      } else {
        DataFlowSet dataFlowSet = decodeData(input, flowsetID);
        flowSets.add(dataFlowSet);
      }

      log.trace("Read {}. Available {}", input.readerIndex(), input.readableBytes());
    }

    NetFlowMessage message = this.netflowFactory.netflowMessage(
        header.version,
        header.count,
        header.uptime,
        header.timestamp,
        header.flowSequence,
        header.sourceID,
        header.sender,
        header.recipient,
        flowSets
    );
    output.add(message);

  }

  static class Header {
    final short version;
    final short count;
    final int uptime;
    final int timestamp;
    final int flowSequence;
    final int sourceID;
    final InetSocketAddress sender;
    final InetSocketAddress recipient;

    Header(short version, short count, int uptime, int timestamp, int flowSequence, int sourceID, InetSocketAddress sender, InetSocketAddress recipient) {
      this.version = version;
      this.count = count;
      this.uptime = uptime;
      this.timestamp = timestamp;
      this.flowSequence = flowSequence;
      this.sourceID = sourceID;
      this.sender = sender;
      this.recipient = recipient;
    }
  }

  public interface NetFlowMessage {
    short version();

    short count();

    int uptime();

    int timestamp();

    int flowSequence();

    int sourceID();

    InetSocketAddress sender();

    InetSocketAddress recipient();

    List<FlowSet> flowsets();
  }

  public interface FlowSet {
    short flowsetID();
  }

  public interface TemplateField {
    short type();

    short length();
  }

  public interface TemplateFlowSet extends FlowSet {
    short templateID();

    List<TemplateField> fields();
  }


  public interface DataFlowSet extends FlowSet {
    byte[] data();
  }

  public interface NetflowFactory {
    NetFlowMessage netflowMessage(short version, short count, int uptime, int timestamp, int flowSequence, int sourceID, InetSocketAddress sender, InetSocketAddress recipient, List<FlowSet> flowsets);

    TemplateField templateField(short type, short length);

    TemplateFlowSet templateFlowSet(short flowsetID, short templateID, List<TemplateField> fields);

    DataFlowSet dataFlowSet(short flowsetID, byte[] data);
  }
}
