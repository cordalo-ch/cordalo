package ch.cordalo.corda.common.contracts;

import ch.cordalo.corda.common.test.CordaNodeEnvironment;
import ch.cordalo.corda.common.test.CordaTestNetwork;
import com.google.common.collect.ImmutableList;
import net.corda.core.flows.FlowLogic;
import net.corda.testing.node.TestCordapp;

import java.util.List;

public class CordaloTestEnvironment {

    protected CordaTestNetwork network;
    protected CordaNodeEnvironment testNode1;
    protected CordaNodeEnvironment testNode2;
    protected CordaNodeEnvironment testNode3;

    public List<TestCordapp> getTestCordapps() {
        return ImmutableList.of(
                TestCordapp.findCordapp("ch.cordalo.corda.common.contracts"),
                TestCordapp.findCordapp("ch.cordalo.corda.common.flows")
        );
    }
    public List<String> getCordappPackageNames() {
        return ImmutableList.of(
                "ch.cordalo.corda.common.contracts",
                "ch.cordalo.corda.common.flows"
        );
    }

    public void setup(boolean withNodes, Class<? extends FlowLogic> ...responderClasses) {
        this.network = new CordaTestNetwork(
                withNodes,
                this.getTestCordapps(),
                this.getCordappPackageNames(),
                responderClasses
        );
        this.testNode1 = network.startEnv("Test1", "O=Test 1 Ltd.,L=Toronto,ST=ON,C=CA");
        this.testNode2 = network.startEnv("Test2", "O=Test 2 Inc.,L=Seattle,ST=WA,C=CH");
        this.testNode3 = network.startEnv("Test3", "O=Test 3 Inc.,L=Seattle,ST=WA,C=CH");
        this.network.startNodes();
    }

    public void tearDown() {
        if (network != null) network.stopNodes();
    };

}
