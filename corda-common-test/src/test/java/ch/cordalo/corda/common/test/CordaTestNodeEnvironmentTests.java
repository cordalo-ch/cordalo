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

import com.google.common.collect.ImmutableList;
import net.corda.core.node.ServiceHub;
import net.corda.finance.flows.CashExitResponderFlow;
import net.corda.testing.node.MockServices;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class CordaTestNodeEnvironmentTests {

    private List<String> getCordappPackageNames() {
        return ImmutableList.of(
                "ch.cordalo.corda.ext",
                "ch.cordalo.corda.common.contracts");
    }


    @Test
    public void test_network_construct_without_nodes() {
        CordaTestNetwork cordaTestNetwork = new CordaTestNetwork(false, this.getCordappPackageNames(), CordaTestNetwork.class);
        cordaTestNetwork.stopNodes();
        Assert.assertEquals("network constructed and dapps registered", 2, cordaTestNetwork.getCorDAppPackageNames().size() );
        Assert.assertEquals("network constructed and dapps registered", 2, cordaTestNetwork.getCorDApps().size());
        Assert.assertEquals("no need for start",false, cordaTestNetwork.needsStart());
        Assert.assertTrue("no network", cordaTestNetwork.getNetwork() == null);
        Assert.assertEquals("no peers", 0, cordaTestNetwork.peers().size());
    }
    @Test
    public void test_network_construct_responders_without_nodes() {
        CordaTestNetwork cordaTestNetwork = new CordaTestNetwork(false, this.getCordappPackageNames(), CashExitResponderFlow.class);
        cordaTestNetwork.stopNodes();
        Assert.assertEquals("network constructed and dapps registered", 2, cordaTestNetwork.getCorDAppPackageNames().size() );
        Assert.assertEquals("no need for start",false, cordaTestNetwork.needsStart());
        Assert.assertEquals("only 1 responder registered",1, cordaTestNetwork.getResponderClasses().size());
    }

    @Test
    public void test_network_construct_with_nodes() {
        CordaTestNetwork cordaTestNetwork = new CordaTestNetwork(true, this.getCordappPackageNames(), CordaTestNetwork.class);
        cordaTestNetwork.stopNodes();
        Assert.assertEquals("network constructed and dapps registered", 2, cordaTestNetwork.getCorDAppPackageNames().size() );
        Assert.assertEquals("needs start",true, cordaTestNetwork.needsStart());
    }
    @Test
    public void test_network_start() throws InterruptedException {
        CordaTestNetwork cordaTestNetwork = new CordaTestNetwork(false, this.getCordappPackageNames(), CordaTestNetwork.class);
        cordaTestNetwork.startNodes();
        cordaTestNetwork.runNetwork();
        cordaTestNetwork.stopNodes();
    }
    @Test
    public void test_network_start_with_nodes() throws InterruptedException {
        CordaTestNetwork cordaTestNetwork = new CordaTestNetwork(true, this.getCordappPackageNames(), CordaTestNetwork.class);
        //cordaTestNetwork.startNodes();
        cordaTestNetwork.stopNodes();
    }
    @Test
    public void test_network_get_no_notaries_by_default() {
        CordaTestNetwork cordaTestNetwork = new CordaTestNetwork(false, this.getCordappPackageNames(), CordaTestNetwork.class);
        Assert.assertEquals("has no notary",0, cordaTestNetwork.getNotaryIdentities().size());
    }

    @Test
    public void test_instance_startEnv() throws InterruptedException {
        CordaTestNetwork cordaTestNetwork = new CordaTestNetwork(false, this.getCordappPackageNames(), CordaTestNetwork.class);
        cordaTestNetwork.startNotaryEnv("N","O=Notary,L=Bern,ST=BE,C=CH");
        Assert.assertEquals("has no notary",1, cordaTestNetwork.getNotaryIdentities().size());
        cordaTestNetwork.stopNodes();
    }

    @Test
    public void test_instance_startEnv_Notary() throws InterruptedException {
        CordaTestNetwork cordaTestNetwork = new CordaTestNetwork(false, this.getCordappPackageNames(), CordaTestNetwork.class);
        cordaTestNetwork.startEnv("A","O=Test1,L=Bern,ST=BE,C=CH");
        cordaTestNetwork.startEnv("B","O=Test2,L=Bern,ST=BE,C=CH");
        cordaTestNetwork.startNotaryEnv("N","O=Notary,L=Bern,ST=BE,C=CH");
        Assert.assertEquals("has 1 peer",3, cordaTestNetwork.peers().size());
        cordaTestNetwork.stopNodes();
    }

    @Test
    public void test_instance_networkMapSnapshot() throws InterruptedException {
        CordaTestNetwork cordaTestNetwork = new CordaTestNetwork(false, this.getCordappPackageNames(), CordaTestNetwork.class);
        cordaTestNetwork.startEnv("A","O=Test1,L=Bern,ST=BE,C=CH");
        cordaTestNetwork.startEnv("B","O=Test2,L=Bern,ST=BE,C=CH");
        cordaTestNetwork.startNotaryEnv("N", "O=Notary,L=Bern,ST=BE,C=CH");
        cordaTestNetwork.startNodes();
        Assert.assertEquals("nof network map entries", 3, cordaTestNetwork.networkMapSnapshot().size());
        cordaTestNetwork.stopNodes();
    }


    @Test
    public void test_instance_getNetworkMap() throws InterruptedException {
        CordaTestNetwork cordaTestNetwork = new CordaTestNetwork(false, this.getCordappPackageNames(), CordaTestNetwork.class);
        cordaTestNetwork.startEnv("A","O=Test1,L=Bern,ST=BE,C=CH");
        cordaTestNetwork.startEnv("B","O=Test2,L=Bern,ST=BE,C=CH");
        cordaTestNetwork.startNotaryEnv("N", "O=Notary,L=Bern,ST=BE,C=CH");
        cordaTestNetwork.startNodes();
        Assert.assertNotNull("network map cache not null", cordaTestNetwork.getNetworkMapCache());
        cordaTestNetwork.stopNodes();
    }


    @Test
    public void test_instance_getEnv() throws InterruptedException {
        CordaTestNetwork cordaTestNetwork = new CordaTestNetwork(false, this.getCordappPackageNames(), CordaTestNetwork.class);
        cordaTestNetwork.startEnv("A","O=Test1,L=Bern,ST=BE,C=CH");
        Assert.assertEquals("has A peer","Test1", cordaTestNetwork.getEnv("A").party.getName().getOrganisation());
        Assert.assertEquals("has A peer is not notary",false, cordaTestNetwork.getEnv("A").isNotary());
        cordaTestNetwork.stopNodes();
    }

    @Test
    public void test_instance_getEnv_notary() throws InterruptedException {
        CordaTestNetwork cordaTestNetwork = new CordaTestNetwork(false, this.getCordappPackageNames(), CordaTestNetwork.class);
        cordaTestNetwork.startEnv("A", "O=Test1,L=Bern,ST=BE,C=CH");
        cordaTestNetwork.startNotaryEnv("N", "O=Notary,L=Bern,ST=BE,C=CH");
        Assert.assertEquals("has N peer", "Notary", cordaTestNetwork.getEnv("N").party.getName().getOrganisation());
        Assert.assertEquals("has N peer is notary", true, cordaTestNetwork.getEnv("N").isNotary());
        cordaTestNetwork.stopNodes();
    }

    @Test
    public void withNodeEnvNotStarted_getServiceHub_expectUsingMock() {
        // arrange
        CordaTestNetwork cordaTestNetwork = new CordaTestNetwork(false, this.getCordappPackageNames(), CordaTestNetwork.class);
        cordaTestNetwork.startEnv("A", "O=Test1,L=Bern,ST=BE,C=CH");
        cordaTestNetwork.startNotaryEnv("N", "O=Notary,L=Bern,ST=BE,C=CH");
        cordaTestNetwork.startNodes();

        // act
        ServiceHub hub = cordaTestNetwork.getEnv("A").getServiceHub();

        // asssert
        assertThat(hub, is(notNullValue()));
        assertThat(hub, is(instanceOf(MockServices.class)));
    }


    @Test
    public void withNodeEnvStarted_getServiceHub_expectUsingRealServiceHub() {
        // arrange
        CordaTestNetwork cordaTestNetwork = new CordaTestNetwork(true, this.getCordappPackageNames());
        cordaTestNetwork.startEnv("A", "O=Test1,L=Bern,ST=BE,C=CH");
        cordaTestNetwork.startNotaryEnv("N", "O=Notary,L=Bern,ST=BE,C=CH");
        cordaTestNetwork.startNodes();

        // act
        ServiceHub hub = cordaTestNetwork.getEnv("A").getServiceHub();

        // asssert
        assertThat(hub, is(notNullValue()));
        assertThat(hub, is(not(instanceOf(MockServices.class))));
        cordaTestNetwork.stopNodes();
    }

}
