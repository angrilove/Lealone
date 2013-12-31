/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.codefollower.lealone.atomicdb.transport.messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.codefollower.lealone.atomicdb.cql.QueryProcessor;
import com.codefollower.lealone.atomicdb.service.QueryState;
import com.codefollower.lealone.atomicdb.transport.FrameCompressor;
import com.codefollower.lealone.atomicdb.transport.Message;

/**
 * Message to indicate that the server is ready to receive requests.
 */
public class OptionsMessage extends Message.Request
{
    public static final Message.Codec<OptionsMessage> codec = new Message.Codec<OptionsMessage>()
    {
        public OptionsMessage decode(ChannelBuffer body, int version)
        {
            return new OptionsMessage();
        }

        public void encode(OptionsMessage msg, ChannelBuffer dest, int version)
        {
        }

        public int encodedSize(OptionsMessage msg, int version)
        {
            return 0;
        }
    };

    public OptionsMessage()
    {
        super(Message.Type.OPTIONS);
    }

    public Message.Response execute(QueryState state)
    {
        List<String> cqlVersions = new ArrayList<String>();
        cqlVersions.add(QueryProcessor.CQL_VERSION.toString());

        List<String> compressions = new ArrayList<String>();
        if (FrameCompressor.SnappyCompressor.instance != null)
            compressions.add("snappy");
        // LZ4 is always available since worst case scenario it default to a pure JAVA implem.
        compressions.add("lz4");

        Map<String, List<String>> supported = new HashMap<String, List<String>>();
        supported.put(StartupMessage.CQL_VERSION, cqlVersions);
        supported.put(StartupMessage.COMPRESSION, compressions);

        return new SupportedMessage(supported);
    }

    @Override
    public String toString()
    {
        return "OPTIONS";
    }
}