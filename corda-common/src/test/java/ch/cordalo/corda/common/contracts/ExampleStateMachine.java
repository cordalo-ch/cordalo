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
