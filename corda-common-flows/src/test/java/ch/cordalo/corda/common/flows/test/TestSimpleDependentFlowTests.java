/*******************************************************************************
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package ch.cordalo.corda.common.flows.test;

import ch.cordalo.corda.common.contracts.CordaloTestEnvironment;
import ch.cordalo.corda.common.contracts.test.TestSimpleDependentState;
import ch.cordalo.corda.common.contracts.test.TestSimpleState;
import ch.cordalo.corda.common.test.CordaNodeEnvironment;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.FlowException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestSimpleDependentFlowTests extends CordaloTestEnvironment {

    @Before
    public void setup() {
        this.setupWithFlows(
                true,
                TestSimpleFlow.class,
                TestSimpleDependentFlow.class
        );
    }

    @After
    public void tearDown() {
        super.tearDown();
    }

    private TestSimpleState newSimple(CordaNodeEnvironment env, String key, String value) throws FlowException {
        return this.startFlowAndResult(
                env,
                new TestSimpleFlow.Create(key, value, new ArrayList<>()),
                TestSimpleState.class);
    }

    private TestSimpleDependentState newDependent(CordaNodeEnvironment env, TestSimpleState state) throws FlowException {
        return this.startFlowAndResult(
                env,
                new TestSimpleDependentFlow.Create(state.getLinearId()),
                TestSimpleDependentState.class);
    }

    @Test
    public void withSimpleState_create_expectOK() throws FlowException {
        // arrange
        TestSimpleState test = newSimple(this.testNode1, "Test1", "Value");

        // act
        String key = test.getKey();

        // asssert
        assertThat(test, is(notNullValue()));
        assertThat(key, is("Test1"));
    }


    @Test
    public void withSimpleDependentState_create_expectOK() throws FlowException {
        // arrange
        TestSimpleState state = newSimple(this.testNode1, "Test1", "Value");
        TestSimpleDependentState dependentState = newDependent(this.testNode1, state);
        // act
        UniqueIdentifier simpleId = dependentState.getSimpleId();

        // asssert
        assertThat(simpleId, is(state.getLinearId()));
    }


}
