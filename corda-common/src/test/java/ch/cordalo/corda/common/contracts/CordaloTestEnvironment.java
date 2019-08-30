package ch.cordalo.corda.common.contracts;

import ch.cordalo.corda.common.test.CordaNodeEnvironment;
import ch.cordalo.corda.common.test.CordaTestNetwork;
import com.google.common.collect.ImmutableList;
import net.corda.core.flows.FlowLogic;
import net.corda.testing.node.TestCordapp;

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
                "ch.cordalo.corda.common.contracts",
                "ch.cordalo.corda.common.flows"
        );
    }
    public void setup(boolean withNodes) {
        this.setup(withNodes, Collections.emptyList());
    }
    public void setup(boolean withNodes, Class<? extends FlowLogic> responderClass1) {
        this.setup(withNodes, Arrays.asList(responderClass1));
    }
    public void setup(boolean withNodes, Class<? extends FlowLogic> responderClass1, Class<? extends FlowLogic> responderClass2) {
        this.setup(withNodes, Arrays.asList(responderClass1, responderClass2));
    }
    public void setup(boolean withNodes, Class<? extends FlowLogic> responderClass1, Class<? extends FlowLogic> responderClass2, Class<? extends FlowLogic> responderClass3) {
        this.setup(withNodes, Arrays.asList(responderClass1, responderClass2, responderClass3));
    }
    public void setup(boolean withNodes, Class<? extends FlowLogic> responderClass1, Class<? extends FlowLogic> responderClass2, Class<? extends FlowLogic> responderClass3, Class<? extends FlowLogic> responderClass4) {
        this.setup(withNodes, Arrays.asList(responderClass1, responderClass2, responderClass3, responderClass4));
    }
    public void setup(boolean withNodes, List<Class<? extends FlowLogic>> responderClasses) {
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
