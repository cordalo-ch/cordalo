package ch.cordalo.corda.common.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.node.ServiceHub;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;

import java.util.List;

public class FlowHelper<T extends ContractState> {

    private ServiceHub serviceHub;

    public FlowHelper(ServiceHub serviceHub) {
        super();
        this.serviceHub = serviceHub;
    }

    @Suspendable
    public StateAndRef<T> getLastStateByLinearId(Class<T> stateClass, UniqueIdentifier linearId) {
        QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(
                null,
                ImmutableList.of(linearId),
                Vault.StateStatus.ALL,
                null);
        List<StateAndRef<T>> data = this.serviceHub.getVaultService().queryBy(stateClass, queryCriteria).getStates();
        return data != null && data.size() > 0 ? data.get(data.size()-1) : null;
    }

    @Suspendable
    public List<StateAndRef<T>> getAllStatesByLinearId(Class<T> stateClass, UniqueIdentifier linearId) {
        QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(
                null,
                ImmutableList.of(linearId),
                Vault.StateStatus.ALL,
                null);
        return this.serviceHub.getVaultService().queryBy(stateClass, queryCriteria).getStates();
    }

    @Suspendable
    public StateAndRef<T> getLastState(Class<T> stateClass) {
        QueryCriteria queryCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        List<StateAndRef<T>> data = this.serviceHub.getVaultService().queryBy(stateClass, queryCriteria).getStates();
        return data != null && data.size() > 0 ? data.get(data.size()-1) : null;
    }

    @Suspendable
    public List<StateAndRef<T>> getUnconsumed(Class<T> stateClass) {
        QueryCriteria queryCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        return this.serviceHub.getVaultService().queryBy(stateClass, queryCriteria).getStates();
    }
    @Suspendable
    public List<StateAndRef<T>> getConsumed(Class<T> stateClass) {
        QueryCriteria queryCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.CONSUMED);
        return this.serviceHub.getVaultService().queryBy(stateClass, queryCriteria).getStates();
    }

}
