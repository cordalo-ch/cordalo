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
import net.corda.core.flows.FlowLogic;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CordaloBaseTestsTests extends CordaloBaseTests {

    protected CordaTestNetwork network;
    protected CordaNodeEnvironment unitTestOnlyNode1;
    protected CordaNodeEnvironment unitTestOnlyNode2;
    protected CordaNodeEnvironment unitTestOnlyNode3;

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
        this.unitTestOnlyNode1 = network.startEnv("Test1", "O=Test 1 Ltd.,L=Toronto,ST=ON,C=CA");
        this.unitTestOnlyNode2 = network.startEnv("Test2", "O=Test 2 Inc.,L=Seattle,ST=WA,C=CH");
        this.unitTestOnlyNode3 = network.startEnv("Test3", "O=Test 3 Inc.,L=Seattle,ST=WA,C=CH");
        this.network.startNodes();
        return this.network;
    }

    @Override
    public CordaTestNetwork getNetwork() {
        return this.network;
    }

    public void tearDown() {
        if (network != null) network.stopNodes();
    }

    @Test
    public void testSetup_network() {
        CordaTestNetwork testEnv = new CordaloBaseTestsTests().setup(false);
        CordaNodeEnvironment test1 = testEnv.getEnv("Test1");
        Assert.assertEquals("","Test 1 Ltd.", test1.party.getName().getOrganisation());
    }

    @Test
    public void test_tearDown() {
        this.tearDown();
    }

}
