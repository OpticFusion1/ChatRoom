package optic_fusion1.server.network.events;

import net.lenni0451.asmevents.event.IEvent;
import net.lenni0451.asmevents.event.types.ICancellableEvent;
import optic_fusion1.commands.command.CommandSender;

public class CommandEvent implements IEvent, ICancellableEvent {

    private boolean cancelled = false;
    private final String command;
    private final CommandSender sender;

    public CommandEvent(CommandSender sender, String command) {
        this.sender = sender;
        this.command = command;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public CommandSender getSender() {
        return sender;
    }

    public String getCommand() {
        return command;
    }

}
