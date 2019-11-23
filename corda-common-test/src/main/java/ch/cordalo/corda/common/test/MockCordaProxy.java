/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.cordalo.corda.common.test;

import ch.cordalo.corda.ext.CordaProxy;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;

public class MockCordaProxy extends CordaProxy {

    private CordaNodeEnvironment env;
    private MockCordaRPCOps rpc;
    private final CordaProxy prevProxy;

    public MockCordaProxy(CordaNodeEnvironment env) {
        this.env = env;
        this.rpc = new MockCordaRPCOps(env);
        this.prevProxy = CordaProxy.getInstance();
    }

    public void setEnv(CordaNodeEnvironment env) {
        this.env = env;
        this.rpc = new MockCordaRPCOps(env);
    }
    public void tearDown() {
        if (this.prevProxy != null) this.prevProxy.register();
    }

    @Override
    public Party getMe() {
        return this.env.party;
    }

    @Override
    public Party getNotary() {
        return this.env.network.getNotaryIdentities().get(0);
    }

    @Override
    public CordaRPCOps getProxy() {
        return this.rpc;
    }

    public static CordaProxy updateInstance(CordaNodeEnvironment env) {
        CordaProxy instance = CordaProxy.getInstance();
        if (instance instanceof MockCordaProxy) {
            ((MockCordaProxy)instance).setEnv(env);
        }
        return instance;
    }
}
