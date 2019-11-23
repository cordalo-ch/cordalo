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

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.serialization.CordaSerializable;
import net.corda.core.transactions.LedgerTransaction;

import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireThat;

public class ReferenceContract<T extends ContractState> implements Contract {
    public static final String ID = "ch.cordalo.corda.common.contracts.ReferenceContract";

    public interface Commands extends CommandData {

        @CordaSerializable
        public class Reference<T extends ContractState> implements Commands {
            final private T myState;

            public Reference(T myState) {
                this.myState = myState;
            }

            public void verify(LedgerTransaction tx) throws IllegalArgumentException {
                requireThat(req -> {
                    List<? extends ContractState> referencedInputs = tx.inputsOfType(myState.getClass());
                    List<? extends ContractState> referencedOutputs = tx.inputsOfType(myState.getClass());
                    req.using("There must be a matching output state for each input state",
                            referencedInputs.equals(referencedOutputs));
                    return null;
                });
            }
            public T getMyState() {
                return this.myState;
            }
        }
    }

    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        StateVerifier verifier = StateVerifier.fromTransaction(tx, ReferenceContract.Commands.class);
        CommandData commandData = verifier.command();
        if (commandData instanceof ReferenceContract.Commands.Reference) {
            ReferenceContract.Commands.Reference cmd = (ReferenceContract.Commands.Reference)commandData;
            cmd.verify(tx);
        }
    }
}