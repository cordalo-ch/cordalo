package ch.cordalo.corda.ext;

import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

public class ParticipantsTestContract implements Contract {
    public static final String ID = ParticipantsTestContract.class.getName();

    public ParticipantsTestContract() {}

    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
    }

}
