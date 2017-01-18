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

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;

class NetFlowFactoryImpl implements NetFlowV9Decoder.NetflowFactory {

  @Override
  public NetFlowV9Decoder.NetFlowMessage netflowMessage(short version, short count, int uptime, int timestamp, int flowSequence, int sourceID, InetSocketAddress sender, InetSocketAddress recipient, List<NetFlowV9Decoder.FlowSet> flowsets) {
    return new NetFlowMessageImpl(version, count, uptime, timestamp, flowSequence, sourceID, sender, recipient, flowsets);
  }

  @Override
  public NetFlowV9Decoder.TemplateField templateField(short type, short length) {
    return new TemplateFieldImpl(type, length);
  }

  @Override
  public NetFlowV9Decoder.TemplateFlowSet templateFlowSet(short flowsetID, short templateID, List<NetFlowV9Decoder.TemplateField> fields) {
    return new TemplateFlowSetImpl(flowsetID, templateID, fields);
  }

  @Override
  public NetFlowV9Decoder.DataFlowSet dataFlowSet(short flowsetID, byte[] data) {
    return new DataFlowSetImpl(flowsetID, data);
  }

  static class NetFlowMessageImpl implements NetFlowV9Decoder.NetFlowMessage {
    final short version;
    final short count;
    final int uptime;
    final int timestamp;
    final int flowSequence;
    final int sourceID;
    final InetSocketAddress sender;
    final InetSocketAddress recipient;
    final List<NetFlowV9Decoder.FlowSet> flowsets;

    NetFlowMessageImpl(short version, short count, int uptime, int timestamp, int flowSequence, int sourceID, InetSocketAddress sender, InetSocketAddress recipient, List<NetFlowV9Decoder.FlowSet> flowsets) {
      this.version = version;
      this.count = count;
      this.uptime = uptime;
      this.timestamp = timestamp;
      this.flowSequence = flowSequence;
      this.sourceID = sourceID;
      this.sender = sender;
      this.recipient = recipient;
      this.flowsets = Collections.unmodifiableList(flowsets);
    }


    @Override
    public short version() {
      return this.version;
    }

    @Override
    public short count() {
      return this.count;
    }

    @Override
    public int uptime() {
      return this.uptime;
    }

    @Override
    public int timestamp() {
      return this.timestamp;
    }

    @Override
    public int flowSequence() {
      return this.flowSequence;
    }

    @Override
    public int sourceID() {
      return this.sourceID;
    }

    @Override
    public InetSocketAddress sender() {
      return this.sender;
    }

    @Override
    public InetSocketAddress recipient() {
      return this.recipient;
    }

    @Override
    public List<NetFlowV9Decoder.FlowSet> flowsets() {
      return this.flowsets;
    }
  }

  static class TemplateFieldImpl implements NetFlowV9Decoder.TemplateField {
    final short type;
    final short length;

    TemplateFieldImpl(short type, short length) {
      this.type = type;
      this.length = length;
    }

    @Override
    public short type() {
      return this.type;
    }

    @Override
    public short length() {
      return this.length;
    }
  }

  static class TemplateFlowSetImpl implements NetFlowV9Decoder.TemplateFlowSet {
    final short flowsetID;
    final short templateID;
    final List<NetFlowV9Decoder.TemplateField> fields;

    TemplateFlowSetImpl(short flowsetID, short templateID, List<NetFlowV9Decoder.TemplateField> fields) {
      this.flowsetID = flowsetID;
      this.templateID = templateID;
      this.fields = fields;
    }

    @Override
    public short flowsetID() {
      return this.flowsetID;
    }

    @Override
    public short templateID() {
      return this.templateID;
    }

    @Override
    public List<NetFlowV9Decoder.TemplateField> fields() {
      return this.fields;
    }
  }

  static class DataFlowSetImpl implements NetFlowV9Decoder.DataFlowSet {
    final short flowsetID;
    final byte[] data;

    DataFlowSetImpl(short flowsetID, byte[] data) {
      this.flowsetID = flowsetID;
      this.data = data;
    }

    @Override
    public short flowsetID() {
      return this.flowsetID;
    }

    @Override
    public byte[] data() {
      return this.data;
    }
  }
}
