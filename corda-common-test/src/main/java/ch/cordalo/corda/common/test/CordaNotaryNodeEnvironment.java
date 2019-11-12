package ch.cordalo.corda.common.test;

public class CordaNotaryNodeEnvironment extends CordaNodeEnvironment {
    public CordaNotaryNodeEnvironment(CordaTestNetwork network, String name, String x500Name) {
        super(network, name, x500Name);
    }
    @Override
    public boolean isNotary() {
        return true;
    }
}
