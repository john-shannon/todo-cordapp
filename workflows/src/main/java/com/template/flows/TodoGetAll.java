package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.TodoContract;
import com.template.contracts.TodoContract.Commands;
import com.template.states.TodoState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class TodoGetAll extends FlowLogic<Void> {

    private final ProgressTracker progressTracker = new ProgressTracker();

    public TodoGetAll() { }

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {
        Vault.Page results = getServiceHub().getVaultService().queryBy(TodoState.class);
        List<StateAndRef> inputStatesAndRefs = results.getStates();

        List<TodoState> states = inputStatesAndRefs.stream()
                .filter(elt -> elt != null)
                .map(elt -> (TodoState)elt.getState().getData())
                .collect(Collectors.toList());

        states.forEach(state -> System.out.println(state.toString()));

        return null;
    }
}
