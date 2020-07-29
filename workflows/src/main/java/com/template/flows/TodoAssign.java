package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.TodoContract;
import com.template.contracts.TodoContract.Commands;
import com.template.states.TodoState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class TodoAssign extends FlowLogic<SignedTransaction> {
    private final String linearIdString;
    private final String assignedTo;

    private final ProgressTracker progressTracker = new ProgressTracker();

    public TodoAssign(String linearId, String assignedTo) {
        this.linearIdString = linearId;
        this.assignedTo = assignedTo;
    }

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        Party me = getOurIdentity();
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        Set<Party> identities = getServiceHub().getIdentityService().partiesFromName(assignedTo, false);
        Party assignedToIdentity = (Party)identities.toArray()[0];

//        UniqueIdentifier linearId = new UniqueIdentifier(linearIdString);
        UUID linearId = UUID.fromString(linearIdString);

        QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(null, Arrays.asList(linearId));

        // 2. Get a reference to the inputState data that we are going to settle.
        Vault.Page results = getServiceHub().getVaultService().queryBy(TodoState.class, queryCriteria);
        StateAndRef inputStateAndRefToTransfer = (StateAndRef) results.getStates().get(0);
        TodoState existingState = (TodoState) inputStateAndRefToTransfer.getState().getData();

        TodoState todoState = existingState.withNewAssignedTo(assignedToIdentity);

        final Command<Commands.Assign> assignCommand = new Command<Commands.Assign>(
                new Commands.Assign(), todoState.getParticipants()
                .stream().map(AbstractParty::getOwningKey)
                .collect(Collectors.toList()));

        final TransactionBuilder builder = new TransactionBuilder(notary);

        builder.addInputState(inputStateAndRefToTransfer);
        builder.addOutputState(todoState, TodoContract.ID);
        builder.addCommand(assignCommand);

        builder.verify(getServiceHub());
        final SignedTransaction ptx = getServiceHub().signInitialTransaction(builder);

        FlowSession flowSession = initiateFlow(assignedToIdentity);
        SignedTransaction stx = subFlow(new CollectSignaturesFlow(ptx, Arrays.asList(flowSession)));


        return subFlow(new FinalityFlow(stx, Arrays.asList(flowSession)));
    }
}
