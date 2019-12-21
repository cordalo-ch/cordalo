/*******************************************************************************
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package ch.cordalo.corda.common.contracts;

import ch.cordalo.corda.common.contracts.StateMachine.State;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.core.TestIdentity;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

public class StateMachineTests {

    @Before
    public void setup() {
    }

    @Test
    public void withStateCreated_new_expect() {
        // arrange
        State state = ExampleStateMachine.State("CREATED");
        // act
        String stateString = state.getValue();
        // asssert
        assertThat(stateString, is("CREATED"));
    }

    @Test
    public void withStateCreated_isInitialState_expectTrue() {
        // arrange
        State state = ExampleStateMachine.State("CREATED");
        // act
        boolean initialState = state.isInitialState();
        // assert
        assertThat(initialState, is(true));
    }

    @Test
    public void withStateCreated_isInitialState_expectFalse() {
        // arrange
        State state = ExampleStateMachine.State("SHARED");
        // act
        boolean initialState = state.isInitialState();
        // assert
        assertThat(initialState, is(false));
    }

    @Test
    public void withStateCreated_isFinalState_expectFalse() {
        // arrange
        State state = ExampleStateMachine.State("CREATED");
        // act
        boolean isFinalState = state.isFinalState();
        // assert
        Assert.assertEquals("created is not final", false, isFinalState);
    }

    @Test
    public void withStateCreated_getNextActions_expectLarger0() {
        // arrange
        State state = ExampleStateMachine.State("CREATED");
        // act
        List<String> nextActions = state.getNextActions();
        // asssert
        assertThat(nextActions, hasSize(not(0)));
    }

    @Test
    public void withStateCreated_getNextActionsFor_expectLarger0() {
        // arrange
        ExamplePermissions.getInstance();
        State state = ExampleStateMachine.State("CREATED");
        // act
        List<String> nextActions = state.getNextActionsFor(companyA_IT);
        // asssert
        assertThat(nextActions, hasSize(not(0)));
        assertThat(nextActions, hasItem("SHARE"));
    }

    @Test
    public void withStateCreatedAndShared_hasLaterState_expectTrue() {
        // arrange
        State createdState = ExampleStateMachine.State("CREATED");
        State sharedState = ExampleStateMachine.State("SHARED");

        // act
        boolean hasLaterState = createdState.hasLaterState(sharedState);

        // asssert
        assertThat(hasLaterState, is(true));
    }

    @Test
    public void withStateCreatedAndShared_hasLaterState_expectFalse() {
        // arrange
        State createdState = ExampleStateMachine.State("CREATED");
        State sharedState = ExampleStateMachine.State("SHARED");

        // act
        boolean hasLaterState = sharedState.hasLaterState(createdState);

        // asssert
        assertThat(hasLaterState, is(false));
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

    @Test
    public void test_initial_state() {
        State initialState = ExampleStateMachine.get().getInitialState();
        Assert.assertNotNull("initialState shall be valid", initialState);
        Assert.assertEquals("initialState is CREATED", "CREATED", initialState.getValue());
    }


    @Test
    public void test_initial_statetransitions() {
        StateMachine.StateTransition initialStateTransition = ExampleStateMachine.get().getInitialTransition();
        Assert.assertNotNull("initial transition shall be valid", initialStateTransition);
        Assert.assertEquals("initial transition is CREATE", "CREATE", initialStateTransition.getValue());
    }

    @NotNull
    private Party newParty(String commonName, String organizationUnit) {
        CordaX500Name cordaX500Name = new CordaX500Name(commonName, organizationUnit,
                "organisation", "locality", "state", "CH");
        return new TestIdentity(cordaX500Name).getParty();
    }

    private Party companyA_IT = newParty("Company-A", "IT");

}
