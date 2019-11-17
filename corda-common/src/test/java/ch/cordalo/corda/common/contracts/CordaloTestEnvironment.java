package ch.cordalo.corda.common.contracts;

import ch.cordalo.corda.common.test.CordaNodeEnvironment;
import ch.cordalo.corda.common.test.CordaTestNetwork;
import ch.cordalo.corda.common.test.CordaloBaseTests;
import com.google.common.collect.ImmutableList;
import net.corda.core.flows.FlowLogic;

import java.util.List;

public class CordaloTestEnvironment extends CordaloBaseTests {

    protected CordaTestNetwork network;
    protected CordaNodeEnvironment testNode1;
    protected CordaNodeEnvironment testNode2;
    protected CordaNodeEnvironment testNode3;

    public List<String> getCordappPackageNames() {
        return ImmutableList.of(
                "ch.cordalo.corda.ext",
                "ch.cordalo.corda.common.contracts");
    }
    public CordaTestNetwork setup(boolean withNodes, List<Class<? extends FlowLogic>> responderClasses) {
        this.network = new CordaTestNetwork(
                withNodes,
                this.getCordappPackageNames(),
                responderClasses
        );
        this.testNode1 = network.startEnv("Test1", "O=Test 1 Ltd.,L=Toronto,ST=ON,C=CA");
        this.testNode2 = network.startEnv("Test2", "O=Test 2 Inc.,L=Seattle,ST=WA,C=CH");
        this.testNode3 = network.startEnv("Test3", "O=Test 3 Inc.,L=Seattle,ST=WA,C=CH");
        this.network.startNodes();
        return this.network;
    }

    @Override
    public CordaTestNetwork getNetwork() {
        return this.network;
    }

    public void tearDown() {
        if (network != null) network.stopNodes();
    };

}
