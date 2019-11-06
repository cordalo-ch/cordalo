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
