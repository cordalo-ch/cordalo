package ch.cordalo.corda.common.contracts;

import ch.cordalo.corda.common.contracts.StateMachine.State;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StateMachineTests {

    @Before
    public void setup() {
    }

    @Test
    public void testInitial() {
        State state = ExampleStateMachine.State("CREATED");
        Assert.assertEquals("created is a valid state", ExampleStateMachine.State("CREATED"), state);
        Assert.assertEquals("created is initial", true, state.isInitialState());
        Assert.assertEquals("created is not final", false, state.isFinalState());
    }

    @Test
    public void testActions() {
        State state = ExampleStateMachine.State("CREATED");
        Assert.assertTrue("no valid next actions", state.getNextActions().size() > 0);
    }

    @Test
    public void testSharedAfterCreate() {
        State createdState = ExampleStateMachine.State("CREATED");
        State sharedState = ExampleStateMachine.State("SHARED");
        Assert.assertEquals("shared is follower of CREATED", true, createdState.hasLaterState(sharedState));
        Assert.assertEquals("created is NOT follower of SHARED", false, sharedState.hasLaterState(createdState));
    }


    @Test
    public void testSharedAfterBeforeShared() {
        State sharedState = ExampleStateMachine.State("SHARED");
        Assert.assertEquals("shared is never later than shared", false, sharedState.hasLaterState(sharedState));
        Assert.assertEquals("shared is never earlier than shared", false, sharedState.hasLaterState(sharedState));
    }

    @Test
    public void testCreateBeforeShared() {
        State createdState = ExampleStateMachine.State("CREATED");
        StateMachine.State sharedState = ExampleStateMachine.State("SHARED");
        Assert.assertEquals("shared is NOT before CREATED", false, createdState.hasEarlierState(sharedState));
        Assert.assertEquals("created is before of SHARED", true, sharedState.hasEarlierState(createdState));
    }


    @Test
    public void testAcceptAfterShared() {
        State sharedState = ExampleStateMachine.State("SHARED");
        State acceptedState = ExampleStateMachine.State("ACCEPTED");
        Assert.assertEquals("accept has earlier shared", true, acceptedState.hasEarlierState(sharedState));
        Assert.assertEquals("shared is later accept", true, sharedState.hasLaterState(acceptedState));
    }


    @Test
    public void testNoShare_is_not_shared() {
        State notShared = ExampleStateMachine.State("NOT_SHARED");
        State shared = ExampleStateMachine.State("SHARED");
        Assert.assertEquals("shared is not later than not-shared", false, shared.hasLaterState(notShared));
        Assert.assertEquals("shared is not earlier than not-shared", false, shared.hasEarlierState(notShared));
    }


}
