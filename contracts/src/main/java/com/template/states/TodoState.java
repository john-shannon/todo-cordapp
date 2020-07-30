package com.template.states;

import com.template.contracts.TodoContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(TodoContract.class)
public class TodoState implements LinearState {

    private Party assignedBy;
    private Party assignedTo;
    private String taskDescription;
    private String dateOfCreation;
    private String deadlineDate;
    private UniqueIdentifier linearId;


    public TodoState(Party assignedBy, Party assignedTo, String taskDescription, String dateOfCreation, String deadlineDate, UniqueIdentifier linearId) {
        this.assignedBy = assignedBy;
        this.assignedTo = assignedTo;
        this.taskDescription = taskDescription;
        this.dateOfCreation = dateOfCreation;
        this.deadlineDate = deadlineDate;
        this.linearId = linearId;
    }

    public Party getAssignedBy() { return assignedBy; }
    public Party getAssignedTo() { return assignedTo; }
    public String getTaskDescription() { return taskDescription; }
    public String getDateOfCreation() { return dateOfCreation; }
    public String getDeadlineDate() { return deadlineDate; }
    @Override public UniqueIdentifier getLinearId() { return linearId; }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(assignedBy, assignedTo);
    }

    public TodoState withNewAssignedToAndDeadline(Party toAssign, String deadline) {
        return new TodoState(assignedBy, toAssign, taskDescription, dateOfCreation, deadline, linearId);
    }

    public String toString() {
        return String.format("%s -- %s - %s - %s - %s - %s", linearId, assignedBy, assignedTo, taskDescription, dateOfCreation, deadlineDate);
    }

}