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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import net.corda.core.serialization.CordaSerializable;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@CordaSerializable
public abstract class StateMachine {

    public static final State[] EMPTY_STATES = new State[0];
    public static final Map<String, StateMachine> machineMap = new LinkedHashMap<>();

    private final String name;
    private final Map<String, State> stateMap = new LinkedHashMap<>();
    private final Map<String, List<StateTransition>> stateTransitions = new LinkedHashMap<>();
    private final Map<String, StateTransition> transitionMap = new LinkedHashMap<>();

    public static StateMachine get(String name) {
        return machineMap.get(name);
    }

    public StateMachine(String name) {
        this.name = name;
        machineMap.put(name, this);
        this.initStates();
        this.initTransitions();
    }

    public String getName() {
        return this.name;
    }

    public abstract void initStates();

    public abstract void initTransitions();

    public StateMachine.State state(String name) {
        if (name != null && !name.isEmpty()) {
            State state = this.stateMap.get(name);
            if (state == null)
                throw new IllegalArgumentException(name + " is not a valid state in state machine " + this.name);
            return state;
        } else {
            return null;
        }
    }

    public State getInitialState() {
        List<State> initialStates = this.getInitialStates();
        if (initialStates.isEmpty()) return null;
        if (initialStates.size() == 1) return initialStates.get(0);
        throw new RuntimeException("more than one initial state. Use getInitialStates to choose from");
    }

    public List<State> getInitialStates() {
        return this.stateMap.values().stream().filter(x -> x.isInitialState()).collect(Collectors.toList());
    }

    public StateTransition getInitialTransition() {
        List<StateTransition> initialTransitions = this.getInitialTransitions();
        if (initialTransitions.isEmpty()) return null;
        if (initialTransitions.size() == 1) return initialTransitions.get(0);
        throw new RuntimeException("more than one transition for initial state. Use getInitialTransitions to choose from");
    }

    public List<StateTransition> getInitialTransitions() {
        return this.transitionMap.values().stream().filter(
                x -> x.getNextState() != null && x.getNextState().isInitialState()).collect(Collectors.toList());
    }

    public StateMachine.StateTransition transition(String name) {
        StateTransition stateTransition = this.transitionMap.get(name);
        if (stateTransition == null)
            throw new IllegalArgumentException(name + " is not a valid state transition in state machine " + this.name);
        return stateTransition;
    }

    public StateMachine.State newState(String value, StateMachine.StateType type) {
        State state = new State(this.name, value, type);
        this.stateMap.put(value, state);
        return state;
    }

    public StateMachine.State newState(String value, String type) {
        return this.newState(value, StateType.valueOf(type));
    }

    public StateMachine.State newState(String value) {
        return newState(value, StateMachine.StateType.CONDITIONAL);
    }

    public StateMachine.StateTransition newTransition(String action, State next, String... previous) {
        if (previous != null && previous.length > 0) {
            StateMachine.State[] prevStates = new State[previous.length];
            StateTransition stateTransition = new StateTransition(
                    this.getName(),
                    action, next,
                    Arrays.stream(previous).map(x -> this.state(x)).toArray(value -> prevStates));
            this.transitionMap.put(action, stateTransition);
            for (State state : prevStates) {
                this.addTransitionToState(stateTransition, state);
            }
            return stateTransition;
        } else {
            StateTransition stateTransition = new StateTransition(this.getName(), action, next);
            this.transitionMap.put(action, stateTransition);
            return stateTransition;
        }
    }

    public StateMachine.StateTransition newTransition(String... values) {
        String action = values[0];
        String next = values[1];
        if (values.length > 2) {
            String[] currentStates = Arrays.copyOfRange(values, 2, values.length);
            return newTransition(action, this.state(next), currentStates);
        } else {
            return newTransition(action, this.state(next));
        }
    }

    public StateMachine.StateTransition newTransitionSameState(String... values) {
        String action = values[0];
        if (values.length > 1) {
            String[] currentStates = Arrays.copyOfRange(values, 1, values.length - 1);
            return newTransition(action, null, currentStates);
        } else {
            return newTransition(action, (State) null);
        }
    }

    public void addTransitionToState(StateTransition stateTransition, State state) {
        List<StateTransition> stateTransitions = this.stateTransitions.get(state.getValue());
        if (stateTransitions == null) {
            this.stateTransitions.put(state.getValue(), Lists.newArrayList(stateTransition));
        } else {
            stateTransitions.add(stateTransition);
        }
    }

    public List<StateTransition> getTransitions(String state) {
        return this.stateTransitions.get(state);
    }

    public List<StateTransition> getTransitions(State state) {
        List<StateTransition> stateTransitions = this.stateTransitions.get(state.getValue());
        return stateTransitions != null ? stateTransitions : Collections.EMPTY_LIST;
    }

    @CordaSerializable
    public enum StateType {
        INITIAL,
        CONDITIONAL,
        SHARE_STATE,
        FINAL
    }

    @CordaSerializable
    public static class State {

        private final String stateMachine;
        private final String value;
        private final StateType type;

        @ConstructorForDeserialization
        public State(String stateMachine, String value, StateType type) {
            this.stateMachine = stateMachine;
            this.value = value;
            this.type = type;
        }

        public State(String stateMachine, String value) {
            this(stateMachine, value, StateType.CONDITIONAL);
        }

        public String getStateMachine() {
            return this.stateMachine;
        }

        public StateType getType() {
            return this.type;
        }

        public String getValue() {
            return this.value;
        }

        public boolean isFinalState() {
            return this.type == StateType.FINAL;
        }

        @JsonIgnore
        public boolean isSharingState() {
            return this.type == StateType.SHARE_STATE;
        }

        public boolean isInitialState() {
            return this.type == StateType.INITIAL;
        }

        @JsonIgnore
        public List<StateTransition> getTransitions() {
            return StateMachine.get(this.getStateMachine()).getTransitions(this);
        }

        public List<String> getNextActions() {
            if (this.isFinalState()) return Collections.EMPTY_LIST;
            return this.getTransitions().stream().map(x -> x.getValue()).collect(Collectors.toList());
        }

        private String getPermissionNameFromAction(String action) {
            return this.getStateMachine() + ":state:" + action;
        }

        public List<String> getNextActionsFor(Party party) {
            List<String> stateMachinePermissions = this.getNextActions().stream().map(x -> this.getPermissionNameFromAction(x)).collect(Collectors.toList());
            if (!stateMachinePermissions.isEmpty()) {
                return Collections.EMPTY_LIST;
            }
            Permissions permissions = Permissions.get(this.getStateMachine());
            if (permissions == null) {
                return Collections.EMPTY_LIST;
            } else {
                return permissions.isPermitted(party, stateMachinePermissions);
            }
        }

        public boolean isValidAction(String action) {
            if (this.isFinalState()) return false;
            this.isValidActionInStateMachine(action);
            return this.getTransitions().stream().anyMatch(x -> x.getValue().equals(action));
        }

        private void isValidActionInStateMachine(String action) {
            if (!StateMachine.get(this.getStateMachine()).isValidTransition(action)) {
                throw new IllegalArgumentException(MessageFormat.format("action {0}does not exist in state machine {1}", action, this.getStateMachine()));
            }
        }

        public boolean isValidActionFor(Party party, String action) {
            if (!this.isValidAction(action)) return false;
            Permissions permissions = Permissions.get(this.getStateMachine());
            if (permissions == null) {
                return false;
            } else {
                return permissions.isPermitted(party, this.getPermissionNameFromAction(action));
            }
        }

        public boolean isLaterState(State state) {
            if (this.equals(state)) return false;
            return state.hasLaterState(this);
        }

        public boolean isEarlierState(State state) {
            if (this.equals(state)) return false;
            return state.hasEarlierState(this);
        }

        public boolean hasLaterState(State state) {
            if (this.equals(state)) return false;
            return hasLaterState(state, new HashSet<>());
        }

        private boolean hasLaterState(State state, Set<State> visited) {
            if (visited.contains(this)) return false;
            visited.add(this);
            if (this.equals(state)) return true;
            for (StateMachine.StateTransition transition : this.getTransitions()) {
                if (transition.nextState != null && transition.nextState.hasLaterState(state, visited)) {
                    return true;
                }
            }
            return false;
        }

        public boolean hasEarlierState(State state) {
            if (this.equals(state)) return false;
            return state.hasLaterState(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o instanceof String) {
                return this.getValue().equals(o);
            }
            if (!(o instanceof State)) return false;
            State state = (State) o;
            return getValue().equals(state.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getValue());
        }
    }

    public boolean isValidTransition(String action) {
        return this.transitionMap.containsKey(action);
    }

    @CordaSerializable
    public static class StateTransition {

        private final String stateMachine;
        private final String value;
        @JsonIgnore
        private final State nextState;
        @JsonIgnore
        private final State[] currentStates;

        @ConstructorForDeserialization
        StateTransition(String stateMachine, String value, State nextState, State[] currentStates) {
            this.stateMachine = stateMachine;
            this.value = value;
            this.currentStates = currentStates;
            this.nextState = nextState;
        }

        StateTransition(String stateMachine, String value, State nextState) {
            this.stateMachine = stateMachine;
            this.value = value;
            this.currentStates = EMPTY_STATES;
            this.nextState = nextState;
        }

        public String getStateMachine() {
            return this.stateMachine;
        }

        public String getValue() {
            return this.value;
        }

        @JsonIgnore
        public boolean willBeInFinalState() {
            return this.nextState.isFinalState();
        }

        @JsonIgnore
        public State getNextState() {
            return this.nextState;
        }

        @JsonIgnore
        public boolean willBeSharingState() {
            return this.nextState.isSharingState();
        }

        public State getNextStateFrom(State from) throws IllegalStateException {
            if (from.isFinalState()) {
                throw new IllegalStateException("state <" + from.getValue() + "> is final state and cannot be transitioned");
            }
            for (State s : this.currentStates) {
                if (s.equals(from)) {
                    return this.nextState != null ? this.nextState : from;
                }
            }
            throw new IllegalStateException("state <" + from.getValue() + "> is not allowed in this current transition");
        }

        @JsonIgnore
        public State getInitialState() throws IllegalStateException {
            if (this.currentStates.length == 0) {
                return this.nextState;
            }
            throw new IllegalStateException("transition has preconditions and is not an initial state");
        }

        private String getPermissionNameFromAction(String action) {
            return this.getStateMachine() + ":state:" + action;
        }

        public boolean isValidAction(String action) {
            return this.getValue().equals(action);
        }

        public boolean isValidActionFor(Party party, String action) {
            if (!this.isValidAction(action)) return false;
            Permissions permissions = Permissions.get(this.getStateMachine());
            if (permissions == null) {
                return false;
            } else {
                return permissions.isPermitted(party, this.getPermissionNameFromAction(action));
            }
        }
    }

}
