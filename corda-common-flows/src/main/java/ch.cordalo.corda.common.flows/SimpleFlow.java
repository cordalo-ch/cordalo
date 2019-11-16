package ch.cordalo.corda.common.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.UniqueIdentifier;

public class SimpleFlow {

    public static interface Create <T extends ContractState> {
        @Suspendable
        public T create();
    }
    public static interface Update <T extends ContractState> {
        @Suspendable
        public T update(T state);
    }
    public static interface Delete <T extends ContractState> {
        @Suspendable
        public void delete(UniqueIdentifier id);
    }
}
