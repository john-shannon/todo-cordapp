package com.template.flows;
import co.paralleluniverse.fibers.Suspendable;
import com.sun.istack.NotNull;
import com.template.states.TodoState;
import net.corda.core.contracts.ContractState;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.ProgressTracker;

import static net.corda.core.contracts.ContractsDSL.requireThat;

@InitiatedBy(TodoAssign.class)
public class TodoAssignResponder extends FlowLogic<SignedTransaction>  {
    private final FlowSession counterpartyFlow;
    private SecureHash signedTx;

    public TodoAssignResponder(FlowSession counterpartyFlow) {
        this.counterpartyFlow = counterpartyFlow;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        class SignTxFlow extends SignTransactionFlow {
            private SignTxFlow(FlowSession otherPartyFlow, ProgressTracker progressTracker) {
                super(otherPartyFlow, progressTracker);
            }

            @Override
            @NotNull
            protected void checkTransaction(SignedTransaction stx) {
                requireThat(require -> {
                    ContractState output = stx.getTx().getOutputs().get(0).getData();
                    require.using("This must be a TODO transaction", output instanceof TodoState);
                    return null;
                });
                signedTx = stx.getId();
            }
        }

        subFlow(new SignTxFlow(counterpartyFlow, SignTransactionFlow.Companion.tracker()));

        return subFlow(new ReceiveFinalityFlow(counterpartyFlow, signedTx));
    }

}
