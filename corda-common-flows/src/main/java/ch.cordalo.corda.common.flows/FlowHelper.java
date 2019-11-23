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

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.node.ServiceHub;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.PageSpecification;
import net.corda.core.node.services.vault.QueryCriteria;

import java.util.ArrayList;
import java.util.List;

public class FlowHelper<T extends ContractState> {

    private ServiceHub serviceHub;

    public FlowHelper(ServiceHub serviceHub) {
        super();
        this.serviceHub = serviceHub;
    }

    @Suspendable
    public StateAndRef<T> getLastStateByCriteria(Class<T> stateClass, QueryCriteria queryCriteria) {
        PageSpecification pageSpec = new PageSpecification(1, 1);
        Vault.Page<T> tPage = this.serviceHub.getVaultService().queryBy(stateClass, queryCriteria, pageSpec);
        if (tPage.getTotalStatesAvailable() == 0) {
            return null;
        } else if (tPage.getTotalStatesAvailable() == 1) {
            return tPage.getStates().get(0);
        } else {
            pageSpec = new PageSpecification((int)tPage.getTotalStatesAvailable(), 1);
            tPage = this.serviceHub.getVaultService().queryBy(stateClass, queryCriteria, pageSpec);
            return tPage.getStates().get(0);
        }
    }
    @Suspendable
    public List<StateAndRef<T>> getStatesByCriteria(Class<T> stateClass, QueryCriteria queryCriteria, PageSpecification pageSpec) {
        Vault.Page<T> tPage = this.serviceHub.getVaultService().queryBy(stateClass, queryCriteria, pageSpec);
        if (tPage.getTotalStatesAvailable() == 0) {
            return null;
        } else {
            return tPage.getStates();
        }
    }
    @Suspendable
    public List<StateAndRef<T>> getLastStatesByCriteria(Class<T> stateClass, QueryCriteria queryCriteria, int count) {
        Vault.Page<T> tPage = this.serviceHub.getVaultService().queryBy(stateClass, queryCriteria, new PageSpecification(1, count));
        if (tPage.getTotalStatesAvailable() <= count) {
            return tPage.getStates();
        } else {
            int page = (int)tPage.getTotalStatesAvailable() / count;
            int lastPageSize = (int)tPage.getTotalStatesAvailable() % count;
            if (lastPageSize == 0) {
                // last page count = count
                return this.getStatesByCriteria(stateClass, queryCriteria, new PageSpecification(page, count));
            } else {
                List<StateAndRef<T>> list = new ArrayList<>();
                list.addAll(
                        this.getStatesByCriteria(stateClass, queryCriteria, new PageSpecification(page, count)).subList(count - lastPageSize - 1, count));
                page++;
                list.addAll(
                        this.getStatesByCriteria(stateClass, queryCriteria, new PageSpecification(page, count)));
                return list;
            }
        }
    }


    @Suspendable
    public StateAndRef<T> getLastStateByLinearId(Class<T> stateClass, UniqueIdentifier linearId) {
        QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(
                null,
                ImmutableList.of(linearId),
                Vault.StateStatus.ALL,
                null);
        return this.getLastStateByCriteria(stateClass, queryCriteria);
    }

    @Suspendable
    public List<StateAndRef<T>> getAllStatesByLinearId(Class<T> stateClass, UniqueIdentifier linearId) {
        return this.getAllStatesByLinearId(stateClass, linearId, new PageSpecification(1, 50));
    }
    @Suspendable
    public List<StateAndRef<T>> getAllStatesByLinearId(Class<T> stateClass, UniqueIdentifier linearId, PageSpecification pageSpec) {
        QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(
                null,
                ImmutableList.of(linearId),
                Vault.StateStatus.ALL,
                null);
        return this.getStatesByCriteria(stateClass, queryCriteria, pageSpec);
    }

    @Suspendable
    public StateAndRef<T> getLastState(Class<T> stateClass) {
        QueryCriteria queryCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        return this.getLastStateByCriteria(stateClass, queryCriteria);
    }

    @Suspendable
    public List<StateAndRef<T>> getUnconsumed(Class<T> stateClass) {
        return this.getUnconsumed(stateClass, new PageSpecification(1, 50));
    }
    @Suspendable
    public List<StateAndRef<T>> getUnconsumed(Class<T> stateClass, PageSpecification pageSpec) {
        QueryCriteria queryCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        return this.getStatesByCriteria(stateClass, queryCriteria, pageSpec);
    }
    @Suspendable
    public List<StateAndRef<T>> getConsumed(Class<T> stateClass) {
        return this.getConsumed(stateClass, new PageSpecification(1, 50));
    }
    @Suspendable
    public List<StateAndRef<T>> getConsumed(Class<T> stateClass, PageSpecification pageSpec) {
        QueryCriteria queryCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.CONSUMED);
        return this.getStatesByCriteria(stateClass, queryCriteria, pageSpec);
    }

}
