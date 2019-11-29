/*******************************************************************************
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package ch.cordalo.corda.common.flows;

import co.paralleluniverse.fibers.Suspendable;
import kotlin.Unit;
import net.corda.confidential.IdentitySyncFlow;
import net.corda.core.contracts.ContractState;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.utilities.ProgressTracker;

abstract public class ResponderBaseFlow<T extends ContractState> extends FlowLogic<Unit> {
    protected final FlowSession otherFlow;
    protected final CordaloProgressTracker progress = new CordaloProgressTracker();

    public ResponderBaseFlow(FlowSession otherFlow) {
        this.otherFlow = otherFlow;
    }

    @Suspendable
    protected Unit receiveIdentitiesCounterpartiesNoTxChecking() throws FlowException {
        if (otherFlow.getCounterparty() != null) {
            // TODO handle value none
            Unit none = subFlow(new IdentitySyncFlow.Receive(otherFlow));
        }
        return receiveCounterpartiesNoTxChecking();
    }

    @Override
    @Suspendable
    public ProgressTracker getProgressTracker() {
        return progress.PROGRESSTRACKER_SYNC;
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
