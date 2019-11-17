package ch.cordalo.corda.common.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.FlowException;
import net.corda.core.transactions.TransactionBuilder;

public class SimpleFlow {

    public static interface Create <T extends ContractState> {
        @Suspendable
        public T create() throws FlowException;
    }
    public static interface Update <T extends ContractState> {
        @Suspendable
        public T update(T state) throws FlowException;
    }
    public static interface UpdateBuilder<T extends ContractState> extends Update<T> {
        @Suspendable
        public void updateBuilder(TransactionBuilder transactionBuilder, StateAndRef<T> stateRef, T state, T newState) throws FlowException;
    }
    public static interface Delete <T extends ContractState> {
        @Suspendable
        public void validateToDelete(T state) throws FlowException;
    }
}
