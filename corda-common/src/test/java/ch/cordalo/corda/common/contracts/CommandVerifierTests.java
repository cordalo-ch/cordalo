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

import ch.cordalo.corda.common.contracts.test.TestContract;
import ch.cordalo.corda.common.contracts.test.TestState;
import net.corda.core.contracts.UniqueIdentifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static net.corda.testing.node.NodeTestUtils.transaction;

public class CommandVerifierTests extends CordaloTestEnvironment {

    @Before
    public void setup() {
        this.setup(false);
    }

    private TestState newTest() {
        return new TestState(new UniqueIdentifier(), testNode1.party, testNode2.party, testNode2.party, "string", 100);
    }

    private TestState newTest(String stringValue) {
        return new TestState(new UniqueIdentifier(), testNode1.party, testNode2.party, testNode2.party, stringValue, 100);
    }

    @Test
    public void contract_create_parties() {
        transaction(testNode1.ledgerServices, tx -> {
            TestState test1 = newTest("test1");
            tx.output(TestContract.ID, test1);
            tx.command(test1.getParticipantKeys(), new TestContract.Commands.CreateCommandVerifier());
            tx.verifies();
            return null;
        });
    }


    @After
    public void tearDown() {
        super.tearDown();
    }
}
