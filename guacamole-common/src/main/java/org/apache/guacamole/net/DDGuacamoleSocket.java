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

package org.apache.guacamole.net;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.GuacamoleServerException;
import org.apache.guacamole.io.DDGuacamoleReader;
import org.apache.guacamole.io.DDGuacamoleWriter;
import org.apache.guacamole.io.GuacamoleReader;
import org.apache.guacamole.io.GuacamoleWriter;
import org.apache.guacamole.io.ReaderGuacamoleReader;
import org.apache.guacamole.io.WriterGuacamoleWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides abstract socket-like access to a Guacamole connection over a data-diode
 * given the sending hostname and port and receiving port using UDP/IP. In order to
 * limit the impact on the web GUI the sending and receiving port will be the same
 * and therefore the GUI does not need to be changed. The option datadiode must be
 * added to the combo box
 * 
 * We need to create a reader and write. The writer is easier, but for the reader we
 * need to create a thread that writes to a queue (charBuffer) and the reader is reading this queue.
 * 
 */
public class DDGuacamoleSocket implements GuacamoleSocket {

    /**
     * Logger for this class.
     */
    private Logger logger = LoggerFactory.getLogger(DDGuacamoleSocket.class);

    /**
     * The GuacamoleReader this socket should read from.
     */
    private GuacamoleReader reader;

    /**
     * The GuacamoleWriter this socket should write to.
     */
    private GuacamoleWriter writer;

    /**
     * The number of milliseconds to wait for data on the TCP socket before
     * timing out.
     */
    private static final int SOCKET_TIMEOUT = 15000;
    
    /**
     * The UDP socket that is used to send data over the data-diode. It will
     * be used by the GuacamoleWriter.
     */
    private DatagramSocket sockUDPWrite;

    /**
     * The UDP socket that the is used to recieve data from the data-diode. It
     * will be used by the GuacamoleReader.
     */
    private DatagramSocket sockUDPRead;

    /**
     * Creates a new InetGuacamoleSocket which reads and writes instructions
     * to the Guacamole instruction stream of the Guacamole proxy server
     * running at the given hostname and port.
     *
     * @param hostname The hostname of the Guacamole proxy server to connect to.
     * @param portWrite The port of the Guacamole proxy server to connect to.
     * @param portRead The port to listen to.
     * @throws GuacamoleException If an error occurs while connecting to the
     *                            Guacamole proxy server.
     */
    public DDGuacamoleSocket(String hostname, int portWrite, int portRead) throws GuacamoleException {
        try {
            logger.debug("Connecting to guacd over a data-diode sending {}:{} and listening for data on port {}.", hostname, portWrite, portRead);

            // Get address
            SocketAddress address = new InetSocketAddress(
                    InetAddress.getByName(hostname),
                    portWrite
            );

            // Connect UDP writer with timeout
            sockUDPWrite = new DatagramSocket();

            // Connect UDP reader with timeout
            sockUDPRead = new DatagramSocket(portRead);
            
            // Set read timeout
            sockUDPWrite.setSoTimeout(SOCKET_TIMEOUT);
            sockUDPRead.setSoTimeout(SOCKET_TIMEOUT);

            // On successful connect, retrieve I/O streams
            reader = new ReaderGuacamoleReader(new DDGuacamoleReader(sockUDPRead, "UTF-8"));
            writer = new WriterGuacamoleWriter(new DDGuacamoleWriter(sockUDPWrite, address, "UTF-8"));

        }
        catch (IOException e) {
            throw new GuacamoleServerException(e);
        }

    }

    @Override
    public void close() throws GuacamoleException {
        logger.debug("Closing socket to guacd.");
        this.sockUDPRead.close();
        this.sockUDPWrite.close();
    }

    @Override
    public GuacamoleReader getReader() {
        return reader;
    }

    @Override
    public GuacamoleWriter getWriter() {
        return writer;
    }

    @Override
    public boolean isOpen() {
        return this.sockUDPRead.isClosed() || this.sockUDPWrite.isClosed();
    }

}
