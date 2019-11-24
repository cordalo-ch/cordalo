/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ch.cordalo.corda.common.flows;

import net.corda.confidential.IdentitySyncFlow;
import net.corda.core.flows.CollectSignaturesFlow;
import net.corda.core.flows.FinalityFlow;
import net.corda.core.serialization.CordaSerializable;
import net.corda.core.utilities.ProgressTracker;
import net.corda.core.utilities.ProgressTracker.Step;
import org.jetbrains.annotations.NotNull;

@CordaSerializable
public class CordaloProgressTracker {

    @CordaSerializable
    public static class SyncSteo extends Step {
        public SyncSteo(@NotNull String label) {
            super(label);
        }
        @Override
        public ProgressTracker childProgressTracker() {
            return IdentitySyncFlow.Send.Companion.tracker();
        }
    }
    @CordaSerializable
    public static class CollectingStep extends Step {
        public CollectingStep(@NotNull String label) {
            super(label);
        }
        @Override
        public ProgressTracker childProgressTracker() {
            return CollectSignaturesFlow.Companion.tracker();
        }
    }
    @CordaSerializable
    public static class FinalisingStep extends Step {
        public FinalisingStep(@NotNull String label) { super(label); }
        @Override
        public ProgressTracker childProgressTracker() { return FinalityFlow.Companion.tracker(); }
    }

    public CordaloProgressTracker() {
    }

    public final Step PREPARATION = new Step("Obtaining data from vault.");
    public final Step BUILDING = new Step("Building transaction.");
    public final Step VERIFYING = new Step("Verifying transaction.");
    public final Step SIGNING = new Step("Signing transaction.");
    public final Step SYNCING = new SyncSteo("Syncing identities.");
    public final Step COLLECTING = new CollectingStep("Collecting counterparty signature.");
    public final Step FINALISING = new FinalisingStep("Finalising transaction.");

    public final ProgressTracker PROGRESSTRACKER_SYNC = new ProgressTracker(
            PREPARATION,    // none
            BUILDING,       // none
            VERIFYING,      // none
            SIGNING,        // none
            SYNCING,        // + Identity Sync Flow: Unit / Void
            COLLECTING,     // + Collect Signatures Flow: SignedTransaction
            FINALISING      // + Finality Flow: SignedTransaction
    );
}
