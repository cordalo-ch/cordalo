/*******************************************************************************
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package ch.cordalo.corda.common.contracts;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.corda.core.contracts.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.node.ServiceHub;
import net.corda.core.transactions.LedgerTransaction;
import net.corda.core.transactions.SignedTransaction;
import org.apache.commons.lang.NotImplementedException;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

interface TransactionDelegate {
    List<CommandWithParties<CommandData>> getCommands();

    List<ContractState> inputsOfType(Class stateClass);

    List<ContractState> outputsOfType(Class stateClass);

    List<ContractState> referenceInputsOfType(Class stateClass);

    List<ContractState> getOutputStates();

    List<ContractState> getInputStates();

    List<ContractState> getReferenceStates();
}

/* class StateVerifier must be part of states and not external package due to miss loading same cordapp package
    except if external dependency via gradle
 */

public class StateVerifier {
    TransactionDelegate tx;
    StateVerifier parent;
    String description = "";
    Class<? extends CommandData> commandClazz;
    CommandWithParties<? extends CommandData> command;
    String text;

    public static StateVerifier fromTransaction(SignedTransaction tx, ServiceHub serviceHub) {
        return new SignedStateVerifier(tx, serviceHub);
    }

    public static <T extends CommandData> StateVerifier fromTransaction(LedgerTransaction tx, Class<T> clazz) {
        return new LedgerStateVerifier(tx, clazz);
    }

    public static StateVerifier fromTransaction(LedgerTransaction tx) {
        return new LedgerStateVerifier(tx);
    }


    StateVerifier() {
        this.parent = null;
    }

    void setTransaction(TransactionDelegate tx) {
        this.tx = tx;
    }

    /*
    public <T extends CommandData> StateVerifier(TransactionDelegate tx, Class<T> clazz) {
        this.tx = tx;
        this.parent = null;
        this.commandClazz = clazz;
        this.command = requireSingleCommand(tx.getCommands(), (Class<? extends CommandData>)this.commandClazz);
    }
    */
    StateVerifier(StateVerifier parent) {
        this.tx = parent.tx;
        this.parent = parent;
        this.commandClazz = parent.commandClazz;
        this.command = parent.command;
    }

    StateVerifier(StateVerifier parent, String text) {
        this.tx = parent.tx;
        this.parent = parent;
        this.commandClazz = parent.commandClazz;
        this.command = parent.command;
        this.text = text;
    }

    String s(String text) {
        String t = this.text == null ? text : this.text;
        return this.description == null ? t : t + this.description;
    }

    public CommandData command() {
        if (this.command == null) {
            throw new IllegalStateException("command not available for this transaction type");
        }
        return this.command.getValue();
    }

    protected List<PublicKey> getSigners() {
        if (this.command == null) {
            throw new IllegalStateException("command not available for this transaction type");
        }
        return this.command.getSigners();
    }

    protected StateVerifier verify() {
        return this;
    }

    protected List<ContractState> getList() {
        return new ArrayList<>();
    }

    protected List<ContractState> getParentList() {
        if (this.parent != null) {
            if (this.parent instanceof StateList) {
                return this.parent.getList();
            } else {
                return this.parent.getParentList();
            }
        }
        return new ArrayList<>();
    }


    @Suspendable
    public <T extends ContractState> List<T> list() {
        return (List<T>) this.getParentList();
    }

    @Suspendable
    public <T extends ContractState> List<T> objects() {
        return (List<T>) this.list();
    }

    @Suspendable
    public <T extends ContractState> T object() {
        return this.one().object(0);
    }

    @Suspendable
    public <T extends ContractState> T object(int index) {
        return (T) this.getParentList().get(index);
    }


    @Suspendable
    public StateVerifier input() {
        return new InputList(this).verify();
    }

    @Suspendable
    public StateVerifier input(Class<? extends ContractState> stateClass) {
        return new InputList(this, stateClass).verify();
    }

    @Suspendable
    public StateVerifier references() {
        return new ReferencesList(this).verify();
    }

    @Suspendable
    public StateVerifier references(Class<? extends ContractState> stateClass) {
        return new ReferencesList(this, stateClass).verify();
    }

    @Suspendable
    public StateVerifier output() {
        return new OutputList(this).verify();
    }

    @Suspendable
    public StateVerifier output(Class<? extends ContractState> stateClass) {
        return new OutputList(this, stateClass).verify();
    }

    @Suspendable
    public StateVerifier newOutput() {
        return new NewOutputList(this).verify();
    }

    @Suspendable
    public StateVerifier newOutput(Class<? extends ContractState> stateClass) {
        return new NewOutputList(this, stateClass).verify();
    }

    @Suspendable
    public StateVerifier intersection() {
        return new IntersectionList<>(this).verify();
    }

    @Suspendable
    public StateVerifier intersection(Class<? extends ContractState> stateClass) {
        return new IntersectionList(this, stateClass).verify();
    }

    @Suspendable
    public <T extends ContractState> StateVerifier use(T state) {
        return new UseThis<T>(this, state).verify();
    }

    @Suspendable
    public <T extends ContractState> StateVerifier use(List<T> states) {
        return new UseThese<T>(this, states).verify();
    }

    @Suspendable
    public <T extends ContractState> StateVerifier filter(Class<T> stateClass) {
        return new FilterList(this, stateClass).verify();
    }

    @Suspendable
    public <T extends ContractState> StateVerifier filterWhere(Function<T, Boolean> mapper) {
        return new FilterWhere(this, mapper).verify();
    }

    @Suspendable
    public <T extends ContractState> StateVerifier notThis(T state) {
        return new FilterWhere(this, x -> !x.equals(state)).verify();
    }


    @Suspendable
    public StateVerifier one() {
        return new One(this).verify();
    }

    @Suspendable
    public StateVerifier one(String text) {
        return new One(this, text).verify();
    }

    @Suspendable
    public <T extends ContractState> StateVerifier one(Class<T> stateClass) {
        return this.filter(stateClass).one();
    }

    @Suspendable
    public StateVerifier empty() {
        return new Empty(this).verify();
    }

    @Suspendable
    public StateVerifier empty(String text) {
        return new Empty(this, text).verify();
    }

    @Suspendable
    public <T extends ContractState> StateVerifier empty(Class<T> stateClass) {
        return this.filter(stateClass).empty();
    }

    @Suspendable
    public StateVerifier notEmpty() {
        return new NotEmpty(this).verify();
    }

    @Suspendable
    public StateVerifier notEmpty(String text) {
        return new NotEmpty(this, text).verify();
    }

    @Suspendable
    public StateVerifier moreThanZero() {
        return new MoreThanZero(this).verify();
    }

    @Suspendable
    public StateVerifier count(int size) {
        return new Count(this, size).verify();
    }

    @Suspendable
    public <T extends ContractState> StateVerifier count(int size, Class<T> stateClass) {
        return this.filter(stateClass).count(size);
    }

    @Suspendable
    public StateVerifier max(int size) {
        return new Max(this, size).verify();
    }

    @Suspendable
    public StateVerifier min(int size) {
        return new Min(this, size).verify();
    }

    @Suspendable
    public StateVerifier moreThanOne() {
        return new MoreThanN(this, 1).verify();
    }

    @Suspendable
    public StateVerifier moreThan(int size) {
        return new MoreThanN(this, size).verify();
    }

    @Suspendable
    public StateVerifier lessThan(int size) {
        return new LessThanN(this, size).verify();
    }

    @Suspendable
    public StateVerifier amountNot0(String name, Function<ContractState, Amount> mapper) {
        return new AmountNot0(this, name, mapper).verify();
    }

    @Suspendable
    public StateVerifier participantsAreSigner() {
        return new ParticipantsAreSigners(this).verify();
    }

    @Suspendable
    public StateVerifier participantsAreSigner(String text) {
        return new ParticipantsAreSigners(this, text).verify();
    }

    @Suspendable
    public StateVerifier signer(String text, Function<ContractState, ? extends AbstractParty> mapper) {
        return new Signers(this, text, mapper).verify();
    }

    @Suspendable
    public StateVerifier differentParty(String name1, Function<ContractState, ? extends AbstractParty> party1Mapper,
                                        String name2, Function<ContractState, ? extends AbstractParty> party2Mapper
    ) {
        return new DifferentParty(this, name1, party1Mapper, name2, party2Mapper).verify();
    }

    @Suspendable
    public StateVerifier sameParty(String name1, Function<ContractState, ? extends AbstractParty> party1Mapper,
                                   String name2, Function<ContractState, ? extends AbstractParty> party2Mapper
    ) {
        return new SameParty(this, name1, party1Mapper, name2, party2Mapper).verify();
    }

    @Suspendable
    public <T extends ContractState> StateVerifier isEmpty(Collection<Function<T, Object>> emptyMappers) {
        return new IsEmpty(this, emptyMappers).verify();
    }

    @Suspendable
    public <T extends ContractState> StateVerifier isEmpty(Function<T, Object> emptyMapper, String text) {
        return new IsEmpty(this, emptyMapper, text).verify();
    }

    @Suspendable
    public <T extends ContractState> StateVerifier isEmpty(Function<T, Object>... emptyMapper) {
        return new IsEmpty(this, Arrays.asList(emptyMapper)).verify();
    }


    @Suspendable
    public <T extends ContractState> StateVerifier isNotEmpty(Collection<Function<T, Object>> emptyMappers) {
        return new IsNotEmpty(this, emptyMappers).verify();
    }

    @Suspendable
    public <T extends ContractState> StateVerifier isNotEmpty(Function<T, Object> emptyMapper, String text) {
        return new IsNotEmpty(this, emptyMapper, text).verify();
    }

    @Suspendable
    public <T extends ContractState> StateVerifier isNotEmpty(Function<T, Object>... emptyMapper) {
        return new IsNotEmpty(this, Arrays.asList(emptyMapper)).verify();
    }


    @Suspendable
    public <T extends ContractState> StateVerifier isEqual(Function<T, Object> firstMapper, Function<T, Object> secondMapper, String text) {
        return new IsEqual(this, firstMapper, secondMapper, text).verify();
    }

    @Suspendable
    public <T extends ContractState> StateVerifier isEqual(Function<T, Object> firstMapper, Function<T, Object> secondMapper) {
        return new IsEqual(this, firstMapper, secondMapper).verify();
    }

    @Suspendable
    public <T extends ContractState> StateVerifier isNotEqual(Function<T, Object> firstMapper, Function<T, Object> secondMapper, String text) {
        return new IsNotEqual(this, firstMapper, secondMapper, text).verify();
    }

    @Suspendable
    public <T extends ContractState> StateVerifier isNotEqual(Function<T, Object> firstMapper, Function<T, Object> secondMapper) {
        return new IsNotEqual(this, firstMapper, secondMapper).verify();
    }

    class Signers extends StateVerifier {

        private Function<ContractState, ? extends AbstractParty> mapper;

        protected Signers(StateVerifier parent, @NotNull String text, Function<ContractState, ? extends AbstractParty> mapper) {
            super(parent, text);
            this.mapper = mapper;
        }

        @Override
        protected StateVerifier verify() {
            requireThat(req -> {
                for (ContractState state : this.getParentList()) {
                    AbstractParty party = this.mapper.apply(state);
                    req.using(s("party <" + party.nameOrNull() + "> is not a signer."),
                            this.getSigners().contains(party.getOwningKey()));
                }
                return null;
            });
            return this;
        }
    }


    class DifferentParty extends StateVerifier {

        private String name1;
        private Function<ContractState, ? extends AbstractParty> party1Mapper;
        private String name2;
        private Function<ContractState, ? extends AbstractParty> party2Mapper;

        protected DifferentParty(StateVerifier parent,
                                 @NotNull String name1, Function<ContractState, ? extends AbstractParty> party1Mapper,
                                 @NotNull String name2, Function<ContractState, ? extends AbstractParty> party2Mapper
        ) {
            super(parent);
            this.name1 = name1;
            this.party1Mapper = party1Mapper;
            this.name2 = name2;
            this.party2Mapper = party2Mapper;
        }

        @Override
        protected StateVerifier verify() {
            requireThat(req -> {
                for (ContractState state : this.getParentList()) {
                    AbstractParty party1 = this.party1Mapper.apply(state);
                    AbstractParty party2 = this.party2Mapper.apply(state);
                    req.using(s("party <" + this.name1 + "> should be different than party <" + this.name2 + ">."),
                            !party1.equals(party2));
                }
                return null;
            });
            return this;
        }
    }


    class SameParty extends StateVerifier {

        private String name1;
        private Function<ContractState, ? extends AbstractParty> party1Mapper;
        private String name2;
        private Function<ContractState, ? extends AbstractParty> party2Mapper;

        protected SameParty(StateVerifier parent,
                            @NotNull String name1, Function<ContractState, ? extends AbstractParty> party1Mapper,
                            @NotNull String name2, Function<ContractState, ? extends AbstractParty> party2Mapper
        ) {
            super(parent);
            this.name1 = name1;
            this.party1Mapper = party1Mapper;
            this.name2 = name2;
            this.party2Mapper = party2Mapper;
        }

        @Override
        protected StateVerifier verify() {
            requireThat(req -> {
                for (ContractState state : this.getParentList()) {
                    AbstractParty party1 = this.party1Mapper.apply(state);
                    AbstractParty party2 = this.party2Mapper.apply(state);
                    req.using(s("party <" + this.name1 + "> should be the same than party <" + this.name2 + ">."),
                            party1.equals(party2));
                }
                return null;
            });
            return this;
        }
    }


    class ParticipantsAreSigners extends StateVerifier {

        protected ParticipantsAreSigners(StateVerifier parent) {
            super(parent);
        }

        protected ParticipantsAreSigners(StateVerifier parent, String text) {
            super(parent, text);
        }

        @Override
        protected StateVerifier verify() {
            requireThat(req -> {
                for (ContractState state : this.getParentList()) {
                    req.using(s("Not all participants are transaction signers."),
                            this.getSigners().containsAll(
                                    state.getParticipants().stream().map(AbstractParty::getOwningKey).collect(Collectors.toList())));
                }
                return null;
            });
            return this;
        }
    }


    class AmountNot0 extends StateVerifier {

        private String name;
        private Function<ContractState, Amount> amountMapper;

        protected AmountNot0(StateVerifier parent, String name, Function<ContractState, Amount> amountMapper) {
            super(parent);
            this.name = name;
            this.amountMapper = amountMapper;
        }

        @Override
        protected StateVerifier verify() {
            requireThat(req -> {
                for (ContractState state : this.getParentList()) {
                    req.using(s("Amount <" + this.name + "> should be not 0."),
                            this.amountMapper.apply(state).getQuantity() > 0);
                }
                return null;
            });
            return this;
        }
    }


    class Size extends StateVerifier {

        private int size = -1;

        protected Size(StateVerifier parent) {
            super(parent);
        }

        protected Size(StateVerifier parent, String text) {
            super(parent, text);
        }

        protected Size(StateVerifier parent, int size) {
            super(parent);
            this.size = size;
        }

        protected int size() {
            return this.size;
        }

        @Override
        protected StateVerifier verify() {
            requireThat(req -> {
                if (this.size != -1) {
                    req.using(s(String.format("List entries must have size %d", this.size)), this.getParentList().size() == this.size);
                }
                return null;
            });
            return this;
        }

    }

    class One extends Size {
        protected One(StateVerifier parent) {
            super(parent);
        }

        protected One(StateVerifier parent, String text) {
            super(parent, text);
        }

        @Override
        protected StateVerifier verify() {
            requireThat(req -> {
                req.using(s("List must contain only 1 entry."), this.getParentList().size() == 1);
                return null;
            });
            return this;
        }
    }

    abstract class Expression extends One {
        protected Expression(StateVerifier parent) {
            super(parent);
        }

        protected Expression(StateVerifier parent, String text) {
            super(parent, text);
        }

        @Override
        protected StateVerifier verify() {
            this.evaluate(object());
            return this;
        }

        public <T extends ContractState> T object() {
            return super.object();
        }

        public abstract <T extends ContractState> void evaluate(T state);
    }

    class IsEmpty<T extends ContractState> extends Expression {
        private final Collection<Function<T, Object>> emptyMappers;

        public IsEmpty(StateVerifier stateVerifier, Collection<Function<T, Object>> emptyMappers) {
            super(stateVerifier);
            this.emptyMappers = emptyMappers;
        }

        public IsEmpty(StateVerifier stateVerifier, Function<T, Object>... emptyMappers) {
            super(stateVerifier);
            this.emptyMappers = Arrays.asList(emptyMappers);
        }

        public IsEmpty(StateVerifier stateVerifier, Function<T, Object> emptyMapper, String text) {
            super(stateVerifier, text);
            this.emptyMappers = Collections.singletonList(emptyMapper);
        }

        public IsEmpty(StateVerifier stateVerifier, Function<T, Object> emptyMapper) {
            super(stateVerifier);
            this.emptyMappers = Collections.singletonList(emptyMapper);
        }

        public <T extends ContractState> void evaluate(T state) {
            requireThat(req -> {
                for (Function<? extends ContractState, Object> mapper : this.emptyMappers) {
                    Object value = mapper.apply(this.object());
                    if (value instanceof String) {
                        req.using(s("field " + mapper.toString() + " must be null or empty"),
                                value == null || ((String) value).isEmpty());
                    } else {
                        req.using(s("field " + mapper.toString() + " must be null"),
                                value == null);
                    }
                }
                return null;
            });
        }
    }

    class IsNotEmpty<T extends ContractState> extends Expression {
        private final Collection<Function<T, Object>> notEmptyMappers;

        public IsNotEmpty(StateVerifier stateVerifier, Collection<Function<T, Object>> notEmptyMappers) {
            super(stateVerifier);
            this.notEmptyMappers = notEmptyMappers;
        }

        public IsNotEmpty(StateVerifier stateVerifier, Function<T, Object>... notEmptyMappers) {
            super(stateVerifier);
            this.notEmptyMappers = Arrays.asList(notEmptyMappers);
        }

        public IsNotEmpty(StateVerifier stateVerifier, Function<T, Object> notEmptyMapper, String text) {
            super(stateVerifier, text);
            this.notEmptyMappers = Collections.singletonList(notEmptyMapper);
        }

        public IsNotEmpty(StateVerifier stateVerifier, Function<T, Object> notEmptyMapper) {
            super(stateVerifier);
            this.notEmptyMappers = Collections.singletonList(notEmptyMapper);
        }

        public <T extends ContractState> void evaluate(T state) {
            requireThat(req -> {
                for (Function<? extends ContractState, Object> mapper : this.notEmptyMappers) {
                    Object value = mapper.apply(this.object());
                    req.using(s("field " + mapper.toString() + " cannot be null"),
                            value != null);
                    if (value instanceof String) {
                        req.using(s("field " + mapper.toString() + " cannot be empty"),
                                !((String) value).isEmpty());
                    }
                }
                return null;
            });
        }
    }


    class IsEqual<T extends ContractState> extends Expression {
        private final Function<T, Object> firstMapping;
        private final Function<T, Object> secondMapping;

        public IsEqual(StateVerifier stateVerifier, Function<T, Object> firstMapping, Function<T, Object> secondMapping) {
            super(stateVerifier);
            this.firstMapping = firstMapping;
            this.secondMapping = secondMapping;
        }

        public IsEqual(StateVerifier stateVerifier, Function<T, Object> firstMapping, Function<T, Object> secondMapping, String text) {
            super(stateVerifier, text);
            this.firstMapping = firstMapping;
            this.secondMapping = secondMapping;
        }

        public <T extends ContractState> void evaluate(T state) {
            requireThat(req -> {
                Object inputVal = firstMapping.apply(this.object());
                Object outputVal = secondMapping.apply(this.object());
                if (inputVal != null) {
                    req.using(this.s("fields must be the same"),
                            inputVal.equals(outputVal));
                } else {
                    req.using(this.s("fields must be empty for both"),
                            outputVal == null);
                }
                return null;
            });
        }
    }


    class IsNotEqual<T extends ContractState> extends Expression {
        private final Function<T, Object> firstMapping;
        private final Function<T, Object> secondMapping;

        public IsNotEqual(StateVerifier stateVerifier, Function<T, Object> firstMapping, Function<T, Object> secondMapping) {
            super(stateVerifier);
            this.firstMapping = firstMapping;
            this.secondMapping = secondMapping;
        }

        public IsNotEqual(StateVerifier stateVerifier, Function<T, Object> firstMapping, Function<T, Object> secondMapping, String text) {
            super(stateVerifier, text);
            this.firstMapping = firstMapping;
            this.secondMapping = secondMapping;
        }

        public <T extends ContractState> void evaluate(T state) {
            requireThat(req -> {
                Object inputVal = firstMapping.apply(this.object());
                Object outputVal = secondMapping.apply(this.object());
                if (inputVal != null) {
                    req.using(this.s("fields must be different"),
                            !inputVal.equals(outputVal));
                } else {
                    req.using(this.s("fields must be different"),
                            outputVal != null);
                }
                return null;
            });
        }
    }

    class OneClass<T> extends StateList<T> {
        protected OneClass(StateVerifier parent, Class<T> stateClass) {
            super(parent, stateClass);
        }

        @Override
        public List<ContractState> getList() {
            List<ContractState> list = this.parent.getParentList();
            return list.stream()
                    .filter(contractState -> contractState.getClass().equals(this.stateClass))
                    .collect(Collectors.toList());
        }

        @Override
        protected StateVerifier verify() {
            requireThat(req -> {
                req.using(s("List must contain only 1 entry."), this.getList().size() == 1);
                return null;
            });
            return this;
        }
    }

    class Empty extends Size {
        protected Empty(StateVerifier parent) {
            super(parent);
        }

        protected Empty(StateVerifier parent, String text) {
            super(parent, text);
        }

        @Override
        protected StateVerifier verify() {
            requireThat(req -> {
                req.using(s("List must be empty."), this.getParentList().isEmpty());
                return null;
            });
            return this;
        }
    }

    class NotEmpty extends Size {
        protected NotEmpty(StateVerifier parent) {
            super(parent);
        }

        protected NotEmpty(StateVerifier parent, String text) {
            super(parent, text);
        }

        @Override
        protected StateVerifier verify() {
            requireThat(req -> {
                req.using(s("List should not be empty."), !this.getParentList().isEmpty());
                return null;
            });
            return this;
        }
    }

    class MoreThanZero extends Size {
        protected MoreThanZero(StateVerifier parent) {
            super(parent);
        }

        @Override
        protected StateVerifier verify() {
            requireThat(req -> {
                req.using(s("List must contain at least 1 entry."), this.getParentList().size() > 0);
                return null;
            });
            return this;
        }
    }

    class MoreThanN extends Size {
        protected MoreThanN(StateVerifier parent) {
            this(parent, 1);
        }

        protected MoreThanN(StateVerifier parent, int size) {
            super(parent, size);
        }

        @Override
        protected StateVerifier verify() {
            requireThat(req -> {
                req.using(s("List must have more than " + this.size() + " entrie(s)."), this.getParentList().size() > this.size());
                return null;
            });
            return this;
        }
    }

    class LessThanN extends Size {
        protected LessThanN(StateVerifier parent) {
            this(parent, 1);
        }

        protected LessThanN(StateVerifier parent, int size) {
            super(parent, size);
        }

        @Override
        protected StateVerifier verify() {
            requireThat(req -> {
                req.using(s("List must have less than " + this.size() + " entrie(s)."), this.getParentList().size() < this.size());
                return null;
            });
            return this;
        }
    }

    class Count extends Size {
        protected Count(StateVerifier parent, int size) {
            super(parent, size);
        }

        @Override
        protected StateVerifier verify() {
            super.verify();
            return this;
        }
    }

    class Max extends Size {
        protected Max(StateVerifier parent, int size) {
            super(parent, size);
        }

        @Override
        protected StateVerifier verify() {
            requireThat(req -> {
                req.using(s("List must contain max " + this.size() + "."), this.getParentList().size() <= this.size());
                return null;
            });
            return this;
        }
    }

    class Min extends Size {
        protected Min(StateVerifier parent, int size) {
            super(parent, size);
        }

        @Override
        protected StateVerifier verify() {
            requireThat(req -> {
                req.using(s("List must contain max " + this.size() + "."), this.getParentList().size() >= this.size());
                return null;
            });
            return this;
        }
    }


    abstract class StateList<T> extends StateVerifier {
        protected Class<T> stateClass;

        protected StateList(StateVerifier parent) {
            super(parent);
        }

        protected StateList(StateVerifier parent, Class<T> stateClass) {
            super(parent);
            this.stateClass = stateClass;
            this.description = "For class " + stateClass.getSimpleName();
        }

        abstract public List<ContractState> getList();

        @Override
        protected StateVerifier verify() {
            return this;
        }

        protected List<ContractState> getParentList() {
            return this.getList();
        }
    }


    class OutputList<T> extends StateList {
        OutputList(StateVerifier parent) {
            super(parent);
        }

        OutputList(StateVerifier parent, Class<T> stateClass) {
            super(parent, stateClass);
        }

        @Override
        public List<ContractState> getList() {
            if (this.stateClass != null) {
                return tx.outputsOfType(this.stateClass);
            } else {
                return tx.getOutputStates();
            }
        }
    }

    // all output that are not in input based on EQUAL
    class NewOutputList<T> extends StateList {
        NewOutputList(StateVerifier parent) {
            super(parent);
        }

        NewOutputList(StateVerifier parent, Class<T> stateClass) {
            super(parent, stateClass);
        }

        @Override
        public List<ContractState> getList() {
            List<ContractState> allOutput = null;
            List<ContractState> allInputs = null;
            if (this.stateClass != null) {
                allOutput = tx.outputsOfType(stateClass);
                allInputs = tx.inputsOfType(stateClass);
            } else {
                allOutput = tx.getOutputStates();
                allInputs = tx.getInputStates();
            }
            if (allOutput != null && allInputs != null) {
                allOutput.removeAll(allInputs);
            }
            return allOutput;
        }
    }

    // and input that are also in output
    class IntersectionList<T> extends StateList {
        IntersectionList(StateVerifier parent) {
            super(parent);
        }

        IntersectionList(StateVerifier parent, Class<T> stateClass) {
            super(parent, stateClass);
        }

        @Override
        public List<ContractState> getList() {
            List<ContractState> allOutput = null;
            List<ContractState> allInputs = null;
            if (this.stateClass != null) {
                allOutput = tx.outputsOfType(stateClass);
                allInputs = tx.inputsOfType(stateClass);
            } else {
                allOutput = tx.getOutputStates();
                allInputs = tx.getInputStates();
            }
            if (allOutput != null && allInputs != null) {
                Set<ContractState> diff = Sets.intersection(Sets.newHashSet(allOutput), Sets.newHashSet(allInputs));
                return Lists.newArrayList(diff);
            } else {
                return Collections.EMPTY_LIST;
            }
        }
    }


    class NoList<T> extends StateList {
        NoList(StateVerifier parent) {
            super(parent);
        }

        NoList(StateVerifier parent, Class<T> stateClass) {
            super(parent, stateClass);
        }

        @Override
        public List<ContractState> getList() {
            throw new IllegalArgumentException("Failed requirement: no list loaded");
        }
    }

    class InputList<T> extends StateList {
        InputList(StateVerifier parent) {
            super(parent);
        }

        InputList(StateVerifier parent, Class<T> stateClass) {
            super(parent, stateClass);
        }

        @Override
        public List<ContractState> getList() {
            if (this.stateClass != null) {
                return tx.inputsOfType(this.stateClass);
            } else {
                return tx.getInputStates();
            }
        }
    }


    class ReferencesList<T> extends StateList {
        ReferencesList(StateVerifier parent) {
            super(parent);
        }

        ReferencesList(StateVerifier parent, Class<T> stateClass) {
            super(parent, stateClass);
        }

        @Override
        public List<ContractState> getList() {
            if (this.stateClass != null) {
                return tx.referenceInputsOfType(this.stateClass);
            } else {
                return tx.getReferenceStates();
            }
        }
    }

    class FilterList<T> extends StateList {
        FilterList(StateVerifier parent, Class<T> stateClass) {
            super(parent, stateClass);
        }

        @Override
        public List<ContractState> getList() {
            List<ContractState> list = this.parent.getParentList();
            return list.stream()
                    .filter(contractState -> contractState.getClass().equals(this.stateClass))
                    .collect(Collectors.toList());
        }
    }


    class UseThis<T extends ContractState> extends StateList {

        private final T state;

        UseThis(StateVerifier parent, T state) {
            super(parent);
            this.state = state;
        }

        @Override
        public List<ContractState> getList() {
            return ImmutableList.of(this.state);
        }
    }


    class UseThese<T extends ContractState> extends StateList {

        private final List<T> states;

        UseThese(StateVerifier parent, List<T> state) {
            super(parent);
            this.states = state;
        }

        @Override
        public List<ContractState> getList() {
            return (List<ContractState>) this.states;
        }
    }


    class FilterWhere<T extends ContractState> extends StateList {
        private final Function<T, Boolean> mapper;

        FilterWhere(StateVerifier parent, Function<T, Boolean> mapper) {
            super(parent);
            this.mapper = mapper;
        }

        @Override
        public List<ContractState> getList() {
            List<T> list = (List<T>) this.parent.getParentList();
            return list.stream()
                    .filter(x -> this.mapper.apply(x))
                    .collect(Collectors.toList());
        }
    }


    static class SignedStateVerifier extends StateVerifier implements TransactionDelegate {

        private SignedTransaction stx;
        private ServiceHub serviceHub;

        protected SignedStateVerifier(SignedTransaction stx, ServiceHub serviceHub) {
            super();
            this.setTransaction(this);
            this.stx = stx;
            this.serviceHub = serviceHub;
            this.parent = null;
            this.commandClazz = null;
            this.command = null;
        }

        @Override
        public List<CommandWithParties<CommandData>> getCommands() {
            return new ArrayList<>();
        }

        @Override
        public List<ContractState> inputsOfType(Class stateClass) {
            return this.getInputStates().stream().filter(
                    x -> stateClass.isInstance(x)
            ).collect(Collectors.toList());
        }

        @Override
        public List<ContractState> outputsOfType(Class stateClass) {
            return this.getOutputStates().stream().filter(
                    x -> stateClass.isInstance(x)
            ).collect(Collectors.toList());
        }

        @Override
        public List<ContractState> getOutputStates() {
            return stx.getTx().getOutputStates();
        }

        @Override
        public List<ContractState> referenceInputsOfType(Class stateClass) {
            throw new NotImplementedException("reference states are not supported in SignTransaction verifiers");
        }

        @Override
        public List<ContractState> getReferenceStates() {
            throw new NotImplementedException("reference states are not supported in SignTransaction verifiers");
        }

        @Override
        public List<ContractState> getInputStates() {
            ArrayList<ContractState> inputStates = new ArrayList<>();
            for (StateRef stateRef : this.stx.getInputs()) {
                try {
                    StateAndRef stateAndRef = this.serviceHub.toStateAndRef(stateRef);
                    inputStates.add(stateAndRef.getState().getData());
                } catch (TransactionResolutionException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
            return inputStates;
        }

    }

    static class LedgerStateVerifier extends StateVerifier implements TransactionDelegate {

        private LedgerTransaction ltx;
        private ServiceHub serviceHub;

        protected <T extends CommandData> LedgerStateVerifier(LedgerTransaction ltx, Class<T> clazz) {
            super();
            this.ltx = ltx;
            this.setTransaction(this);
            this.commandClazz = clazz;
            this.command = requireSingleCommand(this.ltx.getCommands(), (Class<? extends CommandData>) this.commandClazz);
        }

        protected LedgerStateVerifier(LedgerTransaction ltx) {
            super();
            this.ltx = ltx;
            this.setTransaction(this);
            this.commandClazz = null;
            this.command = null;
        }


        @Override
        public List<CommandWithParties<CommandData>> getCommands() {
            return ltx.getCommands();
        }

        @Override
        public List<ContractState> inputsOfType(Class stateClass) {
            return ltx.inputsOfType(stateClass);
        }

        @Override
        public List<ContractState> outputsOfType(Class stateClass) {
            return ltx.outputsOfType(stateClass);
        }

        @Override
        public List<ContractState> referenceInputsOfType(Class stateClass) {
            return ltx.referenceInputsOfType(stateClass);
        }

        @Override
        public List<ContractState> getOutputStates() {
            return ltx.getOutputStates();
        }

        @Override
        public List<ContractState> getInputStates() {
            return ltx.getInputStates();
        }

        @Override
        public List<ContractState> getReferenceStates() {
            return ltx.getReferenceStates();
        }

    }
}
