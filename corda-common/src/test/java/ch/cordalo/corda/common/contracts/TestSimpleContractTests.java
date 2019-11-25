/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.cordalo.corda.common.contracts;

import ch.cordalo.corda.common.contracts.test.TestSimpleContract;
import ch.cordalo.corda.common.contracts.test.TestSimpleState;
import ch.cordalo.corda.common.test.CordaNodeEnvironment;
import net.corda.core.contracts.UniqueIdentifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static net.corda.testing.node.NodeTestUtils.transaction;

public class TestSimpleContractTests extends CordaloTestEnvironment {

    @Before
    public void setup() {
        this.setup(false);
    }

    @After
    public void after() {
        this.tearDown();
    }

    private TestSimpleState newSimpleTest(CordaNodeEnvironment env, String key, String value) {
        return new TestSimpleState(new UniqueIdentifier(), env.party, key, value, new ArrayList<>());
    }

    @Test
    public void test_create() {
        transaction(testNode1.ledgerServices, tx -> {
            TestSimpleState test1 = newSimpleTest(this.testNode1, "key", "value");

            tx.output(TestSimpleContract.ID, test1);
            tx.command(test1.getParticipantKeys(), new TestSimpleContract.Commands.Create());
            tx.verifies();
            return null;
        });
    }


    @Test
    public void test_update() {
        transaction(testNode1.ledgerServices, tx -> {
            TestSimpleState test1 = newSimpleTest(this.testNode1, "key", "value");
            TestSimpleState testUpdate = test1.update("key-1", "value-1");

            tx.input(TestSimpleContract.ID, test1);
            tx.output(TestSimpleContract.ID, testUpdate);
            tx.command(test1.getParticipantKeys(), new TestSimpleContract.Commands.Update());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void test_share() {
        transaction(testNode1.ledgerServices, tx -> {
            TestSimpleState test1 = newSimpleTest(this.testNode1, "key", "value");
            TestSimpleState testUpdate = test1.share(this.testNode2.party);

            tx.input(TestSimpleContract.ID, test1);
            tx.output(TestSimpleContract.ID, testUpdate);
            tx.command(test1.getParticipantKeys(), new TestSimpleContract.Commands.Share());
            tx.verifies();
            return null;
        });
    }

}
