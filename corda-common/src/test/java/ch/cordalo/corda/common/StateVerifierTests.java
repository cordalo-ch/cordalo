package ch.cordalo.corda.common;

import ch.cordalo.corda.common.states.TestContract;
import ch.cordalo.corda.common.states.TestState;
import net.corda.core.contracts.UniqueIdentifier;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static net.corda.testing.node.NodeTestUtils.transaction;

public class StateVerifierTests extends CordaloTestEnvironment {

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
    private TestState newJson(String json) {
        String json2 = JsonHelper.convertJsonToString(JsonHelper.convertStringToJson(json));
        return new TestState(new UniqueIdentifier(), testNode1.party, testNode2.party, testNode2.party, json2, 100);
    }

    private TestState newTest(String stringValue, Integer intValue) {
        return new TestState(new UniqueIdentifier(), testNode1.party, testNode2.party, testNode2.party, stringValue, intValue);
    }


    @Test
    public void simpleTestObject() {
        TestState test = newTest();
        Assert.assertNotNull("test not null", test);
        test.withValues("string-new", 42);
    }


    @Test
    public void simpleJsonObject() {
        TestState test = newJson("{\"testS\":\"string\", \"testB\" : true, \"testO\" : {\"int\" : 100} }");
        Assert.assertNotNull("test not null", test);
    }

    @Test
    public void simpleJsonObjectAccess() {
        TestState test = newJson("{\"testS\":\"string\", \"testB\" : true, \"testO\" : {\"int\" : 100} }");
        Map<String, Object> map = JsonHelper.convertStringToJson(test.getStringValue());
        Assert.assertEquals("testS = string","string", JsonHelper.getDataValue(map, "testS"));
        Assert.assertEquals("testB = true","true", JsonHelper.getDataValue(map, "testB"));
        Assert.assertEquals("testO.int = 100","100", JsonHelper.getDataValue(map, "testO.int"));
    }

    @Test
    public void filterByGroup() {
        TestState test = newJson("{\"testS\":\"string\", \"testB\" : true, \"testO\" : {\"int\" : 100} }");
        Map<String, Object> map = JsonHelper.convertStringToJson(test.getStringValue());
        Map<String, Object> filterMap = JsonHelper.filterByGroupId(map, new String[]{"testO"});
        Assert.assertEquals("testS = ","", JsonHelper.getDataValue(filterMap, "testS"));
        Assert.assertEquals("testB = ","", JsonHelper.getDataValue(filterMap, "true"));
        Assert.assertEquals("testO.int = 100","100", JsonHelper.getDataValue(filterMap, "testO.int"));
    }

    @Test
    public void testWithValues_string() {
        TestState test = newTest("abc");
        TestState test2 = test.withValues("string-new", 42);
        Assert.assertEquals("test id identical", test.getLinearId(), test2.getLinearId());
        Assert.assertEquals("test owner identical", test.getOwner(), test2.getOwner());
        Assert.assertEquals("test provider identical", test.getProvider(), test2.getProvider());
        Assert.assertEquals("test participants", test.getParticipants(), test2.getParticipants());
        Assert.assertNotEquals("test string not identical", test.getStringValue(), test2.getStringValue());
        Assert.assertNotEquals("test int not identical", test.getIntValue(), test2.getIntValue());
    }

    @Test
    public void testWithProvider() {
        TestState test = newTest("xyz");
        TestState test2 = test.withProvider(testNode3.party);
        Assert.assertEquals("test id identical", test.getLinearId(), test2.getLinearId());

        Assert.assertEquals("test owner identical", test.getOwner(), test2.getOwner());
        Assert.assertNotEquals("test provider not identical", test.getProvider(), test2.getProvider());
        Assert.assertNotEquals("test participants not identical", test.getParticipants(), test2.getParticipants());

        Assert.assertEquals("test string identical", test.getStringValue(), test2.getStringValue());
        Assert.assertEquals("test int identical", test.getIntValue(), test2.getIntValue());
    }


    @Test
    public void contract_create_one() {
        transaction(testNode1.ledgerServices, tx -> {
            TestState test1 = newTest();
            tx.output(TestContract.ID, test1);
            tx.command(test1.getParticipantKeys(), new TestContract.Commands.CreateSingleOperators());
            tx.verifies();
            return null;
        });
    }


    @Test
    public void contract_create_multiple() {
        transaction(testNode1.ledgerServices, tx -> {
            TestState test1 = newTest("test1", 99);
            TestState test2 = newTest("test2", 100);
            tx.output(TestContract.ID, test1);
            tx.output(TestContract.ID, test2);
            tx.command(test1.getParticipantKeys(), new TestContract.Commands.CreateMultipleOperators());
            tx.verifies();
            return null;
        });
    }


    @Test
    public void contract_create_parties() {
        transaction(testNode1.ledgerServices, tx -> {
            TestState test1 = newTest("test1");
            tx.output(TestContract.ID, test1);
            tx.command(test1.getParticipantKeys(), new TestContract.Commands.PartyChecks());
            tx.verifies();
            return null;
        });
    }


    @After
    public void tearDown() {
        super.tearDown();
    }
}
