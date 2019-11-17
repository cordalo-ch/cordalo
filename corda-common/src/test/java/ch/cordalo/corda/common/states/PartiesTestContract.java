package ch.cordalo.corda.common.states;

import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

public class PartiesTestContract implements Contract {
    public static final String ID = PartiesTestContract.class.getName();

    public PartiesTestContract() {}

    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
    }

}
