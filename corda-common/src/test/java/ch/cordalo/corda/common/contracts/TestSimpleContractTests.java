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

            tx.input(TestSimpleContract.ID,test1);
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

            tx.input(TestSimpleContract.ID,test1);
            tx.output(TestSimpleContract.ID, testUpdate);
            tx.command(test1.getParticipantKeys(), new TestSimpleContract.Commands.Share());
            tx.verifies();
            return null;
        });
    }

}
