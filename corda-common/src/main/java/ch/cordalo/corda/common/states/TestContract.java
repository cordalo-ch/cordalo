package ch.cordalo.corda.common.states;

import ch.cordalo.corda.common.StateVerifier;
import ch.cordalo.corda.common.contracts.ReferenceContract;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.serialization.CordaSerializable;
import net.corda.core.transactions.LedgerTransaction;
import org.junit.Assert;

import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireThat;

public class TestContract implements Contract {
    public static final String ID = "ch.cordalo.corda.common.states.TestContract";

    public TestContract() {}

    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        StateVerifier verifier = StateVerifier.fromTransaction(tx, TestContract.Commands.class);
        TestContract.Commands commandData = (TestContract.Commands)verifier.command();
        commandData.verify(tx, verifier);
    }

    public interface Commands extends CommandData {
        public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException;


        class CreateSingleOperators implements TestContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    verifier.input().empty();
                    TestState test = verifier
                            .output()
                            .notEmpty()
                            .notEmpty("should not be empty")
                            .moreThanZero()
                            .moreThanZero(1)
                            .one()
                            .one("must be one")
                            .one(TestState.class)
                            .amountNot0("amount", x -> ((TestState)x).getAmount())
                            .object();
                    Assert.assertNotNull("test is not null", test);

                    return null;
                });

            }
        }



        class CreateMultipleOperators implements TestContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    verifier.input().empty();
                    List<TestState> tests = verifier
                            .output()
                            .notEmpty()
                            .moreThanOne()
                            .moreThanOne(2)
                            .objects();

                    TestState testSmallerThan100 = verifier
                            .output()
                            .notEmpty()
                            .filter(TestState.class)
                            .filterWhere(
                                    x -> ((TestState)x).getAmount().getQuantity() < 100*100)
                            .one()
                            .object();

                    TestState testAnotherOne = verifier
                            .use(tests.get(1))
                            .one()
                            .object();
                    List<TestState> sameThanList = verifier
                            .use(tests)
                            .moreThanZero(2)
                            .objects();


                    return null;
                });

            }
        }


        class PartyChecks implements TestContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    verifier.input().empty();
                    TestState test = verifier
                            .output()
                            .one()
                            .one("must contain only 1")
                            .one(TestState.class)
                            .participantsAreSigner()
                            .participantsAreSigner("all are signers")
                            .signer("owner", x -> ((TestState)(x)).getOwner())
                            .differentParty(
                                    "owner", x -> ((TestState)(x)).getOwner(),
                                    "provider", x -> ((TestState)(x)).getProvider())
                            .sameParty(
                                    "provider", x -> ((TestState)(x)).getProvider(),
                                    "clone", x -> ((TestState)(x)).getCloneProvider())
                            .object();
                    return null;
                });

            }
        }



        @CordaSerializable
        public class Reference extends ReferenceContract.Commands.Reference<TestState> implements TestContract.Commands {
            public Reference(TestState myState) {
                super(myState);
            }

            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                this.verify(tx);
            }
        }
    }

}
