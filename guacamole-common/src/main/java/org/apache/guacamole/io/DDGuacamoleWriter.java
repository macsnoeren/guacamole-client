/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

 package org.apache.guacamole.io;

import java.io.IOException;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

public class DDGuacamoleWriter extends Writer {
    private byte[] buffer;
    private final DatagramSocket socket;
    private final SocketAddress address;
    private final String charset;

    public DDGuacamoleWriter (DatagramSocket socket, SocketAddress address, String charset ) {
        this.buffer = new byte[20480];
        this.socket = socket;
        this.address = address;
        this.charset = charset;
    }

    @Override
    public void close() throws IOException {
        this.socket.close();
    }

    // Flushes the stream. If the stream has saved any characters from the various write() methods 
    // in a buffer, write them immediately to their intended destination. Then, if that destination 
    // is another character or byte stream, flush it. Thus one flush() invocation will flush all the 
    // buffers in a chain of Writers and OutputStreams.
    //
    // If the intended destination of this stream is an abstraction provided by the underlying operating 
    // system, for example a file, then flushing the stream guarantees only that bytes previously written 
    // to the stream are passed to the operating system for writing; it does not guarantee that they are 
    // actually written to a physical device such as a disk drive.
    @Override
    public void flush() throws IOException {
        // At this moment no saved characters!
    }

    // Writes a portion of an array of characters.
    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        for ( int i=0; i < off+len; i++ ) {
            this.buffer[i] = (byte) cbuf[i+off];
        }

        DatagramPacket p = new DatagramPacket(this.buffer, len-off, this.address);

        this.socket.send(p);        
    }
    
}
