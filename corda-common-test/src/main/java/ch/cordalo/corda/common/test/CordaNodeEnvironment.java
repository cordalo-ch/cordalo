package ch.cordalo.corda.common.test;

import net.corda.core.flows.FlowLogic;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import net.corda.testing.node.StartedMockNode;

public class CordaNodeEnvironment {
    public final String name;
    public final CordaX500Name x500;
    public final TestIdentity identity;
    public StartedMockNode node;
    public Party party;
    public MockServices ledgerServices;

    public CordaNodeEnvironment(String name, String x500Name) {
        this.name = name;
        this.x500 = CordaX500Name.parse(x500Name);
        this.identity = new TestIdentity(x500);
    }

    public void startWith(CordaTestNetwork testNetwork) {
        if (testNetwork.getNetwork() != null) {
            this.node = testNetwork.getNetwork().createPartyNode(this.identity.getName());
            this.registerResponders(testNetwork.getResponderClasses());
            this.party = this.node.getInfo().getLegalIdentities().get(0);
        } else {
            this.startNoNetwork();
        }
        ledgerServices = new MockServices(
                testNetwork.getCordappPackageNames(),
                this.party.getName()
        );

    }

    private void registerResponders(Class<? extends FlowLogic>[] responderClasses) {
        if (responderClasses != null) {
            for (Class<? extends FlowLogic> responderClass: responderClasses) {
                this.node.registerInitiatedFlow(responderClass);
            }
        }
    }

    private CordaNodeEnvironment startLedger(CordaTestNetwork testNetwork) {
        this.ledgerServices = new MockServices(
                testNetwork.getCordappPackageNames(),
                this.party.getName()
        );
        return this;
    }

    public void startNoNetwork() {
        this.party = this.identity.getParty();

    }
}
