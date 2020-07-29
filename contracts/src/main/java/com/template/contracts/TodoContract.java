package com.template.contracts;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

// ************
// * Contract *
// ************
public class TodoContract implements Contract {
    public static final String ID = "com.template.contracts.TodoContract";

    public interface Commands extends CommandData {
        class Create implements Commands {}
        class Assign implements Commands {}
        class MarkComplete implements Commands {}
    }

    @Override
    public void verify(LedgerTransaction tx) {
        final CommandWithParties<Commands> command = requireSingleCommand(tx.getCommands(), Commands.class);
        final Commands commandData = command.getValue();

        if (commandData.equals(new Commands.Create())) {
            requireThat(require -> {
                require.using("No inputs should be consumed when issuing a TODO.", tx.getInputStates().size() == 0);
                require.using( "Only one output state should be created when issuing a TODO.", tx.getOutputStates().size() == 1);

                return null;
            });
        }
    }
}