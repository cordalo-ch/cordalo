/*******************************************************************************
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package ch.cordalo.corda.common.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.FlowException;
import net.corda.core.transactions.TransactionBuilder;

/**
 * this flow implement CRUD operations on any <code>ContractState</code>
 */
public class SimpleFlow {

    public interface Create<T extends ContractState> {
        @Suspendable
        T create() throws FlowException;
    }

    public interface Update<T extends ContractState> {
        @Suspendable
        T update(T state) throws FlowException;
    }

    public interface UpdateBuilder<T extends ContractState> extends Update<T> {
        @Suspendable
        CommandData getCommand(StateAndRef<T> stateRef, T state, T newState) throws FlowException;

        void updateBuilder(TransactionBuilder transactionBuilder, StateAndRef<T> stateRef, T state, T newState) throws FlowException;

    }

    public interface Delete<T extends ContractState> {
        @Suspendable
        void validateToDelete(T state) throws FlowException;
    }
}
