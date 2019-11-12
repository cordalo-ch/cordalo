package ch.cordalo.corda.common.test;

import net.corda.core.flows.FlowLogic;
import net.corda.core.identity.Party;
import net.corda.core.node.NodeInfo;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.TestCordapp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CordaTestNetwork {

    private final Class<? extends FlowLogic>[] responderClasses;
    private MockNetwork network;
    private List<TestCordapp> testCordapps;
    private List<String> testPackageNames;
    private final boolean withNodes;
    private final Map<String, CordaNodeEnvironment> nodes = new HashMap();

    public CordaTestNetwork(boolean withNodes, List<String> testPackageNames, Class<? extends FlowLogic>[] responderClasses) {
        this.testPackageNames = testPackageNames;
        this.testCordapps = testPackageNames.stream().map(x -> TestCordapp.findCordapp(x)).collect(Collectors.toList());
        this.withNodes = withNodes;
        this.responderClasses = responderClasses;
        this.createNetwork();
    }

    private void createNetwork() {
        if (this.withNodes) {
            network = new MockNetwork(new MockNetworkParameters(testCordapps));
        }
    }
    private CordaNodeEnvironment startNode(CordaNodeEnvironment env) {
        env.startWith(this);
        nodes.put(env.name, env);
        return env;
    }

    public List<String> getCordappPackageNames() {
        return this.testPackageNames;
    }
    public List<TestCordapp> getCordapps() {
        return this.testCordapps;
    }
    public MockNetwork getNetwork() {
        return this.network;
    }
    public List<Party>peers() {
        return this.nodes.values().stream().map(x -> x.party).collect(Collectors.toList());
    }
    public List<NodeInfo> networkMapSnapshot() {
        return this.nodes.values().stream().map(x -> x.getNodeInfo()).collect(Collectors.toList());
    }

    public Class<? extends FlowLogic>[] getResponderClasses() {
        return this.responderClasses;
    }

    public CordaNodeEnvironment startEnv(String name, String x500) {
        return this.startNode(
                new CordaNodeEnvironment(this,name, x500));
    }
    public CordaNodeEnvironment startNotaryEnv(String name, String x500) {
        return this.startNode(
                new CordaNotaryNodeEnvironment(this,name, x500));
    }

    public void startNodes() {
        if (this.network != null) this.network.startNodes();
    }

    public void runNetwork() {
        if (this.network != null) this.network.runNetwork();
    }

    public void stopNodes() {
        if (this.network != null) this.network.stopNodes();
    }
}
