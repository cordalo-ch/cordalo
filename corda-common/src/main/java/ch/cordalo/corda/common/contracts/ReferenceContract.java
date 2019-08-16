package ch.cordalo.corda.common.contracts;

import ch.cordalo.corda.common.StateVerifier;
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