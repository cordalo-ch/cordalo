/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.cordalo.corda.common.test;

import net.corda.core.concurrent.CordaFuture;
import net.corda.core.flows.FlowLogic;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.node.NodeInfo;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import net.corda.testing.node.StartedMockNode;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class CordaNodeEnvironment {
    public CordaTestNetwork network;
    public final String name;
    public final CordaX500Name x500;
    public final TestIdentity identity;
    public StartedMockNode node;
    public Party party;
    public MockServices ledgerServices;

    public CordaNodeEnvironment(CordaTestNetwork network, String name, String x500Name) {
        this.network = network;
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
    public boolean isNotary() {
        return false;
    }

    private void registerResponders(List<Class<? extends FlowLogic>> responderClasses) {
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

    public SignedTransaction startFlow(FlowLogic<SignedTransaction> flow) throws ExecutionException, InterruptedException {
        CordaFuture<SignedTransaction> future = this.node.startFlow(flow);
        this.network.runNetwork();
        return future.get();
    }

    public NodeInfo getNodeInfo() {
        return this.ledgerServices.getMyInfo();
    }
}
