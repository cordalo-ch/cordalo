package ch.cordalo.corda.common.contracts;

import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;

public abstract class CordaProxy {

    private static CordaProxy instance;

    public static CordaProxy getInstance() {
        return instance;
    }
    public static void register(CordaProxy proxy) {
        instance = proxy;
    }

    public abstract Party getMe();
    public abstract Party getNotary ();
    public abstract CordaRPCOps getProxy();
    public boolean isValid() {
        return this.getMe() != null;
    }

}
