package ch.cordalo.corda.ext;

import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;

public abstract class CordaProxy {

    private static CordaProxy instance;

    public static CordaProxy getInstance() {
        return instance;
    }
    private static void register(CordaProxy proxy) {
        instance = proxy;
    }

    public abstract Party getMe();
    public abstract Party getNotary ();
    public abstract CordaRPCOps getProxy();

    public CordaProxy register() {
        CordaProxy.register(this);
        return this;
    }
    public boolean isValid() {
        return this.getMe() != null;
    }

}
