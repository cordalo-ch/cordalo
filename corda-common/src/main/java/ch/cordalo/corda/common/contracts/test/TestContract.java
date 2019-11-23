package ch.cordalo.corda.common.contracts.test;

import ch.cordalo.corda.common.contracts.CommandVerifier;
import ch.cordalo.corda.common.contracts.ReferenceContract;
import ch.cordalo.corda.common.contracts.StateVerifier;
import kotlin.Pair;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.serialization.CordaSerializable;
import net.corda.core.transactions.LedgerTransaction;

import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireThat;

public class TestContract implements Contract {
    public static final String ID = TestContract.class.getName();

    public TestContract() {}

    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        StateVerifier verifier = StateVerifier.fromTransaction(tx, TestContract.Commands.class);
        TestContract.Commands commandData = (TestContract.Commands)verifier.command();
        commandData.verify(tx, verifier);
    }

    public interface Commands extends CommandData {
        public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException;
        
        class CreateCommandVerifier implements TestContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                CommandVerifier.Parameters<TestState> params = new CommandVerifier.Parameters<>();
                params.notEmpty(
                        TestState::getLinearId,
                        TestState::getOwner,
                        TestState::getProvider,
                        TestState::getCloneProvider,
                        TestState::getStringValue,
                        TestState::getIntValue,
                        TestState::getAmount
                );
                new CommandVerifier(verifier).verify_create1(TestState.class, params);
            }
        }

        class UpdateCommandVerifier implements TestContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                CommandVerifier.Parameters<TestState> params = new CommandVerifier.Parameters<>();
                params.notEmpty(
                        TestState::getLinearId,
                        TestState::getOwner,
                        TestState::getProvider,
                        TestState::getCloneProvider,
                        TestState::getStringValue,
                        TestState::getIntValue,
                        TestState::getAmount
                );
                params.equal(
                        TestState::getLinearId,
                        TestState::getOwner
                );
                new CommandVerifier(verifier).verify_update1(TestState.class, params);
            }
        }



        class CreateSingleOperators implements TestContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                TestState create = new CommandVerifier(verifier).verify_create1(TestState.class);
                verifier.input().empty();
                TestState test = verifier
                        .output()
                        .notEmpty()
                        .notEmpty("should not be empty")
                        .moreThanZero()
                        .count(1)
                        .min(1)
                        .max(2)
                        .moreThan(0)
                        .one()
                        .isNotEmpty(TestState::getLinearId, TestState::getProvider)
                        .isEqual(TestState::getOwner, TestState::getOwner)
                        .isNotEqual(TestState::getOwner, TestState::getProvider)
                        
                        .one("must be one")
                        .one(TestState.class)
                        .amountNot0("amount", x -> ((TestState)x).getAmount())
                        .object();
            }
        }

        class UpdateOneInOut implements TestContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    TestState in = verifier
                            .input()
                            .one()
                            .one(TestState.class)
                            .object();
                    TestState out = verifier
                            .output()
                            .notEmpty()
                            .notEmpty("should not be empty")
                            .moreThanZero()
                            .min(1)
                            .max(100)
                            .count(1)
                            .one()
                            .one("must be one")
                            .one(TestState.class)
                            .amountNot0("amount", x -> ((TestState)x).getAmount())
                            .object();

                    CommandVerifier.Parameters<TestState> params = new CommandVerifier.Parameters<>();
                    params.equal(TestState::getLinearId, TestState::getOwner);
                    params.notEqual(TestState::getProvider);

                    Pair<TestState, TestState> pair = new CommandVerifier(verifier).verify_update1(
                            TestState.class, params);
                    req.using("new provider must be different", !in.getProvider().equals(out.getProvider()));
                    return null;
                });

            }
        }


        class CreatedInOut implements TestContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    TestState in = verifier
                            .input(TestState.class)
                            .count(1)
                            .object();
                    List<TestState> out2 = verifier
                            .output(TestState.class)
                            .count(2)
                            .objects();
                    TestState diff = verifier
                            .newOutput(TestState.class)
                            .count(1)
                            .object();
                    return null;
                });

            }
        }


        class UnionInOut implements TestContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    TestState in = verifier
                            .input(TestState.class)
                            .count(1)
                            .object();
                    List<TestState> out2 = verifier
                            .output(TestState.class)
                            .count(2)
                            .objects();
                    TestState diff = verifier
                            .intersection(TestState.class)
                            .count(1)
                            .object();
                    return null;
                });

            }
        }


        class NotThis implements TestContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    TestState in = verifier
                            .input(TestState.class)
                            .count(1)
                            .object();
                    List<TestState> out2 = verifier
                            .output(TestState.class)
                            .count(2)
                            .objects();
                    TestState notThis = verifier
                            .output(TestState.class)
                            .notThis(in)
                            .one()
                            .object();
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
                            .min(1)
                            .max(4)
                            .moreThan(1)
                            .count(2)
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
                            .count(2)
                            .lessThan(3)
                            .moreThan(1)
                            .objects();


                    return null;
                });

            }
        }


        class Delete implements TestContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    TestState test = verifier
                            .input()
                            .one()
                            .one("must contain only 1")
                            .one(TestState.class)
                            .object();
                    verifier.output().empty();
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
