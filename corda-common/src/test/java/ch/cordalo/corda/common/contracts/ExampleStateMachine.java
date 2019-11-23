/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.cordalo.corda.common.contracts;

public class ExampleStateMachine extends StateMachine {

    private final static ExampleStateMachine INSTANCE = new ExampleStateMachine();

    public static ExampleStateMachine get() { return INSTANCE; }
    private ExampleStateMachine() {
        super("services");
    }

    public static StateTransition StateTransition(String transition) {
        return INSTANCE.transition(transition);
    }
    public static State State(String state) {
        return INSTANCE.state(state);
    }

    @Override
    public void initStates() {
        newState("CREATED", StateMachine.StateType.INITIAL);
        newState("REGISTERED");
        newState("INFORMED");
        newState("CONFIRMED");

        newState("TIMEOUTS", StateMachine.StateType.FINAL);
        newState("WITHDRAWN", StateMachine.StateType.FINAL);

        newState("SHARED", StateMachine.StateType.SHARE_STATE);
        newState("NOT_SHARED", StateMachine.StateType.FINAL);
        newState("DUPLICATE", StateMachine.StateType.FINAL);

        newState("PAYMENT_SENT");

        newState("ACCEPTED", StateMachine.StateType.FINAL);
        newState("DECLINED", StateMachine.StateType.FINAL);
    }

    @Override
    public void initTransitions() {
        newTransition("CREATE"          ,"CREATED");

        newTransition("REGISTER"        ,"REGISTERED"   ,"CREATED");
        newTransition("INFORM"          ,"INFORMED"     ,"CREATED","REGISTERED");
        newTransition("CONFIRM"         ,"CONFIRMED"    ,"INFORMED");
        newTransition("TIMEOUT"         ,"TIMEOUTS"     ,"INFORMED");

        newTransition("UPDATE"          ,"",            "CREATED","SHARED");

        newTransition("WITHDRAW"        ,"WITHDRAWN"    ,"CONFIRMED","INFORMED","REGISTERED","CREATED");
        newTransition("NO_SHARE"        ,"NOT_SHARED"   ,"CONFIRMED","INFORMED");
        newTransition("DUPLICATE"       ,"DUPLICATE"    ,"CONFIRMED","INFORMED","REGISTERED","CREATED");
        newTransition("SHARE"           ,"SHARED"       ,"CONFIRMED","INFORMED","REGISTERED","CREATED");

        newTransition("SEND_PAYMENT"    ,"PAYMENT_SENT" ,"SHARED");

        newTransition("ACCEPT"          ,"ACCEPTED"     ,"SHARED","PAYMENT_SENT");
    }
}
