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
