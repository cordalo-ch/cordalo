package ch.cordalo.corda.common.contracts;

import ch.cordalo.corda.common.test.CordaNodeEnvironment;
import ch.cordalo.corda.common.test.CordaTestNetwork;
import com.google.common.collect.ImmutableList;
import net.corda.core.flows.FlowLogic;
import net.corda.testing.node.TestCordapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CordaloTestEnvironment {

    protected CordaTestNetwork network;
    protected CordaNodeEnvironment testNode1;
    protected CordaNodeEnvironment testNode2;
    protected CordaNodeEnvironment testNode3;

    public List<String> getCordappPackageNames() {
        return ImmutableList.of(
                "ch.cordalo.corda.common.contracts");
    }
    public void setup(boolean withNodes, Class<? extends FlowLogic> ...responderClasses) {
        this.network = new CordaTestNetwork(
                withNodes,
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
