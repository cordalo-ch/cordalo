package ch.cordalo.corda.common.flows;

import co.paralleluniverse.fibers.Suspendable;
import kotlin.Unit;
import net.corda.confidential.IdentitySyncFlow;
import net.corda.core.contracts.ContractState;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;

abstract public class ResponderBaseFlow<T extends ContractState> extends FlowLogic<Unit> {
    protected final FlowSession otherFlow;

    public ResponderBaseFlow(FlowSession otherFlow) {
        this.otherFlow = otherFlow;
    }

    @Suspendable
    protected Unit receiveIdentitiesCounterpartiesNoTxChecking() throws FlowException {
        if (otherFlow.getCounterparty() != null) {
            Unit none = subFlow(new IdentitySyncFlow.Receive(otherFlow));
        }
        if (otherFlow.getCounterpartyFlowInfo().getFlowVersion() >= 2) {
            SecureHash id = subFlow(new BaseFlow.SignTxFlowNoChecking(otherFlow, SignTransactionFlow.Companion.tracker())).getId();
            subFlow(new ReceiveFinalityFlow(otherFlow, id));
        } else {
            subFlow(new BaseFlow.SignTxFlowNoChecking(otherFlow, SignTransactionFlow.Companion.tracker()));
        }
        return Unit.INSTANCE;
    }

    @Suspendable
    protected Unit receiveCounterpartiesNoTxChecking() throws FlowException {
        if (otherFlow.getCounterpartyFlowInfo().getFlowVersion() >= 2) {
            SecureHash id = subFlow(new BaseFlow.SignTxFlowNoChecking(otherFlow, SignTransactionFlow.Companion.tracker())).getId();
            subFlow(new ReceiveFinalityFlow(otherFlow, id));
        } else {
            subFlow(new BaseFlow.SignTxFlowNoChecking(otherFlow, SignTransactionFlow.Companion.tracker()));
        }
        return Unit.INSTANCE;
    }

}
