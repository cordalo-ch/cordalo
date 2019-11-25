/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.cordalo.corda.common.client.webserver;

import net.corda.client.jackson.JacksonSupport;
import net.corda.client.rpc.CordaRPCClient;
import net.corda.client.rpc.CordaRPCConnection;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.utilities.NetworkHostAndPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Wraps an RPC connection to a Corda node.
 * <p>
 * The RPC connection is configured using command line arguments.
 */
@Component
public class RpcConnection implements AutoCloseable {

    private final static Logger logger = LoggerFactory.getLogger(RpcConnection.class);

    // The host of the node we are connecting to.
    @Value("${config.rpc.host}")
    private String host;
    // The RPC port of the node we are connecting to.
    @Value("${config.rpc.username}")
    private String username;
    // The username for logging into the RPC client.
    @Value("${config.rpc.password}")
    private String password;
    // The password for logging into the RPC client.
    @Value("${config.rpc.port}")
    private int rpcPort;

    private CordaRPCConnection rpcConnection;
    private CordaRPCOps proxy;

    public RpcConnection() {
    }

    @PostConstruct
    public void initialiseNodeRPCConnection() {
        JacksonSupport.createNonRpcMapper();
        // validate if hostname is not empty, to have a working test environment
        // CordaRPCClient cannot be initialized twice
        if (this.host != null && !this.host.isEmpty()) {
            NetworkHostAndPort rpcAddress = new NetworkHostAndPort(host, rpcPort);
            CordaRPCClient rpcClient = new CordaRPCClient(rpcAddress);
            try {
                rpcConnection = rpcClient.start(username, password);
                proxy = rpcConnection.getProxy();
            } catch (Exception e) {
                logger.error("NodeRPC connection + proxy is not initialized (null)");
                rpcConnection = null;
                proxy = null;
            }
        } else {
            logger.error("NodeRPC connection + proxy is not initialized (for testing)");
            rpcConnection = null;
            proxy = null;
        }
    }

    public boolean isValid() {
        return this.proxy != null;
    }

    public CordaRPCOps getProxy() {
        return this.proxy;
    }

    @PreDestroy
    public void close() {
        if (rpcConnection != null) {
            rpcConnection.notifyServerAndClose();
        }
    }
}