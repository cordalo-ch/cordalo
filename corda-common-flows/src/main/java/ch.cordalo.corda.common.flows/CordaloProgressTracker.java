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
import net.corda.core.utilities.ProgressTracker;

public class CordaloProgressTracker {

    public final static ProgressTracker.Step PREPARATION = new ProgressTracker.Step("Obtaining data from vault.");
    public final static ProgressTracker.Step BUILDING = new ProgressTracker.Step("Building transaction.");
    public final static ProgressTracker.Step VERIFYING = new ProgressTracker.Step("Verifying transaction.");
    public final static ProgressTracker.Step SIGNING = new ProgressTracker.Step("Signing transaction.");
    public final static ProgressTracker.Step SYNCING = new ProgressTracker.Step("Syncing identities.") {
        @Override
        public ProgressTracker childProgressTracker() {
            return IdentitySyncFlow.Send.Companion.tracker();
        }
    };
    public final static ProgressTracker.Step COLLECTING = new ProgressTracker.Step("Collecting counterparty signature.") {
        @Override
        public ProgressTracker childProgressTracker() {
            return CollectSignaturesFlow.Companion.tracker();
        }
    };
    public final static ProgressTracker.Step FINALISING = new ProgressTracker.Step("Finalising transaction.") {
        @Override
        public ProgressTracker childProgressTracker() {
            return FinalityFlow.Companion.tracker();
        }
    };

    public final static ProgressTracker PROGRESSTRACKER_SYNC = new ProgressTracker(
            PREPARATION,    // none
            BUILDING,       // none
            VERIFYING,      // none
            SIGNING,        // none
            SYNCING,        // + Identity Sync Flow: Unit / Void
            COLLECTING,     // + Collect Signatures Flow: SignedTransaction
            FINALISING      // + Finality Flow: SignedTransaction
    );
    public final static ProgressTracker PROGRESS_TRACKER_NOSYNC = new ProgressTracker(
            PREPARATION,    // none
            BUILDING,       // none
            VERIFYING,      // none
            SIGNING,        // none
            COLLECTING,     // + Collect Signatures Flow: SignedTransaction
            FINALISING      // + Finality Flow: SignedTransaction
    );
    public final static ProgressTracker PROGRESS_TRACKER_NOSYNC_NOCOLLECT = new ProgressTracker(
            PREPARATION,    // none
            BUILDING,       // none
            VERIFYING,      // none
            SIGNING,        // none
            FINALISING      // + Finality Flow: SignedTransaction
    );
}
