/*******************************************************************************
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package ch.cordalo.corda.common.contracts.test;

import ch.cordalo.corda.common.contracts.StateVerifier;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

import static net.corda.core.contracts.ContractsDSL.requireThat;

public class TestSimpleDependentContract implements Contract {
    public static final String ID = TestSimpleDependentContract.class.getName();

    public TestSimpleDependentContract() {
    }

    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        StateVerifier verifier = StateVerifier.fromTransaction(tx, TestSimpleDependentContract.Commands.class);
        TestSimpleDependentContract.Commands commandData = (TestSimpleDependentContract.Commands) verifier.command();
        commandData.verify(tx, verifier);
    }

    public interface Commands extends CommandData {
        public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException;

        class Create implements TestSimpleDependentContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    TestSimpleState simpleState = verifier.references()
                            .one("must have at least 1 reference state")
                            .one(TestSimpleState.class)
                            .isNotEmpty(
                                    TestSimpleState::getLinearId,
                                    TestSimpleState::getOwner,
                                    TestSimpleState::getKey)
                            .object();

                    TestSimpleDependentState state = verifier.output()
                            .one()
                            .one(TestSimpleDependentState.class)
                            .isNotEmpty(
                                    TestSimpleDependentState::getLinearId,
                                    TestSimpleDependentState::getSimpleId,
                                    TestSimpleDependentState::getKey,
                                    TestSimpleDependentState::getOwner)
                            .object();
                    req.using("Simple state id must be same than dependent simpleId", simpleState.getLinearId().equals(state.getSimpleId()));
                    req.using("Simple state and dependent must have same owner", simpleState.getOwner().equals(state.getOwner()));
                    req.using("Simple state and dependent must have same key", simpleState.getKey().equals(state.getKey()));
                    return null;
                });
            }
        }

    }

}
