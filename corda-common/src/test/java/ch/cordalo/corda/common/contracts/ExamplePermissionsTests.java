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

import kotlin.Pair;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.core.TestIdentity;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class ExamplePermissionsTests {


    @Before
    public void setup() {
        ExampleStateMachine.get();
        ExamplePermissions.getInstance();
    }

    @NotNull
    private Party newParty(String commonName, String organizationUnit) {
        CordaX500Name cordaX500Name = new CordaX500Name(commonName, organizationUnit,
                "organisation", "locality", "state", "CH");
        return new TestIdentity(cordaX500Name).getParty();
    }

    private Party newParty() {
        return newParty("commonName", "organisationUnit");
    }


    private Party companyA_IT = newParty("Company-A", "IT");
    private Party companyA_Marketing = newParty("Company-A", "Marketing");
    private Party companyB_IT = newParty("Company-B", "IT");
    private Party companyB_Business = newParty("Company-B", "Business");
    private Party companyC_IT = newParty("Company-C", "IT");

    private Pair<Boolean, Boolean> isValidAction(String state, String action, Party party) {
        if (state != null && !state.isEmpty()) {
            StateMachine.State stateObject = ExampleStateMachine.get().state(state);
            return new Pair<>(stateObject.isValidAction(action), stateObject.isValidActionFor(party, action));
        } else {
            return isValidInitialAction(action, party);
        }
    }

    private Pair<Boolean, Boolean> isValidInitialAction(String action, Party party) {
        StateMachine.StateTransition stateTransitionObject = ExampleStateMachine.get().getInitialTransition();
        return new Pair<>(stateTransitionObject.isValidAction(action), stateTransitionObject.isValidActionFor(party, action));
    }

    private void assertValidAction(String state, String action, Party party, boolean validAction, boolean validActionForParty) {
        Pair<Boolean, Boolean> validActions = isValidAction(state, action, party);
        Assert.assertEquals("is valid action", validAction, validActions.component1());
        Assert.assertEquals("is valid action for party", validActionForParty, validActions.component2());
    }

    private void assertValidInitialAction(String action, Party party, boolean validAction, boolean validActionForParty) {
        Pair<Boolean, Boolean> validActions = isValidInitialAction(action, party);
        Assert.assertEquals("is valid action", validAction, validActions.component1());
        Assert.assertEquals("is valid action for party", validActionForParty, validActions.component2());
    }


    @Test()
    public void test_action_check_role_admin() {
        assertValidAction("CREATED", "REGISTER", companyA_IT, true, true);
        assertValidAction("", "CREATE", companyA_IT, true, true);
    }

    @Test
    public void withExistingAction_isValidAction_expectTrue() {
        //Arrange
        //Act
        //Assert
        assertValidAction("PAYMENT_SENT", "ACCEPT", companyB_Business, true, true);
    }

    @Test
    public void withExistingStateAction_isStateActionPermitted_expectTrue() {
        //Arrange
        //Act
        boolean accept = ExamplePermissions.getInstance().isStateActionPermitted(companyB_Business, "ACCEPT");
        //Assert
        assertThat(accept, is(true));
    }

    @Test
    public void withExistingAction_isActionPermitted_expectTrue() {
        //Arrange
        //Act
        boolean accept = ExamplePermissions.getInstance().isActionPermitted(companyA_Marketing, "search");
        //Assert
        assertThat(accept, is(true));
    }

    @Test
    public void withExistingAction_isValidAction_expectFalse() {
        assertValidAction("PAYMENT_SENT", "ACCEPT", companyB_IT, true, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void withNotExistingAction_isValidAction_expectException() {
        assertValidAction("PAYMENT_SENT", "NEIN!!!!", companyB_IT, false, false);
    }

    @Test
    public void withExistingAttribute_getAttribute_expectString() {
        // arrange
        ExamplePermissions.getInstance();
        // act
        String logo = ExamplePermissions.getInstance().getAttribute(companyA_IT, "logo");
        // asssert
        assertThat(logo, is("companyA.png"));
    }

    @Test
    public void withNonExistingAttribute_getAttribute_expectNull() {
        // arrange
        ExamplePermissions.getInstance();
        // act
        String logo = ExamplePermissions.getInstance().getAttribute(companyA_IT, "logo-X");
        // asssert
        assertThat(logo, is(nullValue()));
    }

    @Test
    public void withNonExistingAttributeForParty_getAttribute_expectNull() {
        // arrange
        ExamplePermissions.getInstance();
        // act
        String logo = ExamplePermissions.getInstance().getAttribute(companyA_Marketing, "logo");
        // asssert
        assertThat(logo, is(nullValue()));
    }

    @Test
    public void withExamplePermissions_getAllPermissions_expectMap() {
        // arrange
        ExamplePermissions.getInstance();

        // act
        Map<String, Object> allPermissions = Permissions.getAllPermissions(companyA_IT);

        // asssert
        assertThat(allPermissions, is(notNullValue()));
    }


    @Test
    public void withExamplePermissions_hasRole_expectTrue() {
        // arrange

        // act
        boolean isDecider = ExamplePermissions.getInstance().hasRole(companyA_IT, "decider");

        // asssert
        assertThat(isDecider, is(true));
    }
    @Test
    public void withExamplePermissions_hasRole_expectFalse() {
        // arrange

        // act
        boolean isDecider = ExamplePermissions.getInstance().hasRole(companyA_Marketing, "decider");

        // asssert
        assertThat(isDecider, is(false));
    }
    @Test
    public void withExamplePermissionsMissingRole_hasRole_expectFalse() {
        // arrange

        // act
        boolean isDecider = ExamplePermissions.getInstance().hasRole(companyA_Marketing, "missing role");

        // asssert
        assertThat(isDecider, is(false));
    }
}
