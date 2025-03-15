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
import java.io.Reader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

// https://jenkov.com/tutorials/java-networking/udp-datagram-sockets.html

public class DDGuacamoleReader extends Reader {

    private byte[] buffer;
    private DatagramSocket socket;
    private DatagramPacket packet;
    private String charset;

    public DDGuacamoleReader (DatagramSocket socket, String charset ) {
        this.buffer = new byte[20480];
        this.packet = new DatagramPacket(this.buffer, this.buffer.length);
        this.socket = socket;
        this.charset = charset;
    }

    // Closes the stream and releases any system resources associated with it. Once the 
    // stream has been closed, further read(), ready(), mark(), reset(), or skip() 
    // invocations will throw an IOException. Closing a previously closed stream has no effect.
    @Override
    public void close() throws IOException {
        this.socket.receive(this.packet); // Some kind of flush and throws an IOException
        this.socket.close();
    }

    // Reads characters into a portion of an array. This method will block until 
    // some input is available, an I/O error occurs, or the end of the stream is reached.
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        this.socket.receive(this.packet); // Read from the UDP socket

        int total = Math.max(off+len, this.packet.getLength()); // Calculate total chars to read

        byte[] b = this.packet.getData();
        for ( int i=off; i < total; i++ ) { // Copy the data
            cbuf[i] = (char) b[i];
        }

        return total-off;
    }

    
    
}
