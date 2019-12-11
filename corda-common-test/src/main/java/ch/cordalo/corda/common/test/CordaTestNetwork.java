/*******************************************************************************
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package ch.cordalo.corda.common.test;

import net.corda.core.flows.FlowLogic;
import net.corda.core.identity.Party;
import net.corda.core.node.NetworkParameters;
import net.corda.core.node.NodeInfo;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.TestCordapp;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.*;

public class CordaTestNetwork {

    private final List<Class<? extends FlowLogic>> responderClasses;
    private final MockNetwork network;
    private final List<TestCordapp> testCorDApps;
    private final List<String> testPackageNames;
    private final boolean withNodes;
    private final Map<String, CordaNodeEnvironment> nodes = new HashMap();

    public CordaTestNetwork(boolean withNodes, List<String> testPackageNames, List<Class<? extends FlowLogic>> responderClasses) {
        this.testPackageNames = testPackageNames;
        this.testCorDApps = testPackageNames.stream().map(x -> TestCordapp.findCordapp(x)).collect(Collectors.toList());
        this.withNodes = withNodes;
        this.responderClasses = responderClasses;
        if (this.withNodes) {
            network = new MockNetwork(
                    new MockNetworkParameters(testCorDApps)
                            .withNetworkParameters(getTestNetworkParameters()));
        } else {
            network = null;
        }
    }

    public CordaTestNetwork(boolean withNodes, List<String> testPackageNames, Class responderMainClass) {
        this(withNodes, testPackageNames, Arrays.asList(responderMainClass));
    }

    public CordaTestNetwork(boolean withNodes, List<String> testPackageNames) {
        this(withNodes, testPackageNames, EMPTY_LIST);
    }

    private CordaNodeEnvironment startNode(CordaNodeEnvironment env) {
        env.startWith(this);
        nodes.put(env.name, env);
        return env;
    }

    public List<String> getCorDAppPackageNames() {
        return this.testPackageNames;
    }

    public List<TestCordapp> getCorDApps() {
        return this.testCorDApps;
    }

    public MockNetwork getNetwork() {
        return this.network;
    }

    public List<Party> peers() {
        return this.nodes.values().stream().map(x -> x.party).collect(Collectors.toList());
    }

    public boolean needsStart() {
        return this.network != null;
    }

    public List<NodeInfo> networkMapSnapshot() {
        return this.nodes.values().stream().map(x -> x.getNodeInfo()).collect(Collectors.toList());
    }

    public List<Party> getNotaryIdentities() {
        return this.nodes.values().stream().filter(x -> x.isNotary()).map(x -> x.party).collect(Collectors.toList());
    }

    public MockNetworkMapCache getNetworkMapCache() {
        return new MockNetworkMapCache(this);
    }

    public List<Class<? extends FlowLogic>> getResponderClasses() {
        return this.responderClasses;
    }

    public CordaNodeEnvironment startEnv(String name, String x500) {
        return this.startNode(
                new CordaNodeEnvironment(this, name, x500));
    }

    public CordaNodeEnvironment startNotaryEnv(String name, String x500) {
        return this.startNode(
                new CordaNotaryNodeEnvironment(this, name, x500));
    }

    public TestIdentity[] getIdentitiesWithoutMe(CordaNodeEnvironment me) {
        List<TestIdentity> testIdentities = this.nodes.values()
                .stream()
                .filter(x -> !x.equals(me))
                .map(x -> new TestIdentity(x.x500))
                .collect(Collectors.toList());
        TestIdentity[] testIdentityArray = new TestIdentity[testIdentities.size()];
        testIdentities.toArray(testIdentityArray);
        return testIdentityArray;
    }

    public CordaNodeEnvironment getEnv(String name) {
        return this.nodes.get(name);
    }

    public void startNodes() {
        this.startLedgers();
        if (this.needsStart()) this.network.startNodes();
    }

    private void startLedgers() {
        for (CordaNodeEnvironment node : this.nodes.values()) {
            node.startLedger(this);
        }
    }

    public void runNetwork() {
        if (this.needsStart()) this.network.runNetwork();
    }

    public void stopNodes() {
        if (this.needsStart()) {
            this.network.stopNodes();
            this.network.waitQuiescent();
        }
    }


    @NotNull
    public static NetworkParameters getTestNetworkParameters() {
        // check out net.corda.testing.common.internal.ParametersUtilitiesKt for parameters
        return new NetworkParameters(
                4,
                emptyList(),
                10485760,
                10485760 * 50,
                Instant.MIN,
                1,
                emptyMap());
    }
}
