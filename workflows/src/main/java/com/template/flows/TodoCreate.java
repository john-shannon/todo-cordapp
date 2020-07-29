package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.TodoContract;
import com.template.states.TodoState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import com.template.contracts.TodoContract.Commands;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class TodoCreate extends FlowLogic<SignedTransaction> {
    private final String assignedTo;
    private final String taskDescription;

    private final ProgressTracker progressTracker = new ProgressTracker();

    public TodoCreate(String assignedTo, String taskDescription) {
        this.assignedTo = assignedTo;
        this.taskDescription = taskDescription;
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

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);
        String dateOfCreation = df.format(new Date());

        UniqueIdentifier linearId = new UniqueIdentifier();

        TodoState todoState = new TodoState(me, assignedToIdentity, taskDescription, dateOfCreation, linearId);

        final Command<Commands.Create> createCommand = new Command<Commands.Create>(
                new Commands.Create(), todoState.getParticipants()
                .stream().map(AbstractParty::getOwningKey)
                .collect(Collectors.toList()));

        final TransactionBuilder builder = new TransactionBuilder(notary);

        builder.addOutputState(todoState, TodoContract.ID);
        builder.addCommand(createCommand);

        builder.verify(getServiceHub());
        final SignedTransaction ptx = getServiceHub().signInitialTransaction(builder);


//        if (assignedToIdentity != me) {
////            FlowSession flowSession = initiateFlow(assignedToIdentity);
////            SignedTransaction stx = subFlow(new CollectSignaturesFlow(ptx, session));
////        }


        return subFlow(new FinalityFlow(ptx, Arrays.asList()));
    }
}
