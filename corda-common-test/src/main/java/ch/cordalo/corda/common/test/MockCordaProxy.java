package ch.cordalo.corda.common.test;

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
        CordaProxy.register(this.prevProxy);
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
