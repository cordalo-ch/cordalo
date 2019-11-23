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

import ch.cordalo.corda.ext.CordaProxy;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import org.springframework.stereotype.Component;

@Component
public class CordaRpcProxy extends CordaProxy {

    private final RpcConnection rpc;
    private final Party me;
    private final Party notary;

    public CordaRpcProxy(RpcConnection rpc) {
        this.rpc = rpc;
        if (rpc.isValid()) {
            this.me = this.rpc.getProxy().nodeInfo().getLegalIdentities().get(0);
            this.notary = this.rpc.getProxy().notaryIdentities().get(0);
        } else {
            this.me = null;
            this.notary = null;
        }
    }
    public Party getMe() {
        return me;
    }
    public Party getNotary () {
        return this.notary;
    }
    public CordaRPCOps getProxy() {
        return this.rpc.getProxy();
    }

}
