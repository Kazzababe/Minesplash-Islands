package net.grancraft.islands.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Command extends org.bukkit.command.Command {
    private List<Argument> arguments = new ArrayList();

    public Command(String name, String... aliases) {
        super(name);
        this.setAliases(Arrays.asList(aliases));
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        // I'm pretty sure this is stupid af, but w/e
        if (strings.length > 0) {
            for (Argument argument : arguments) {
                if (argument.matches(strings)) {
                    if ((argument.player && commandSender instanceof Player) || !argument.player) {
                        return argument.runnable.executeArgument(commandSender, argument.filler ? strings[strings.length - 1] : null);
                    }
                }
            }
        }
        return this.onCommand(commandSender, strings);
    }

    public void registerArgument(ArgumentRunnable runnable, boolean filler, boolean player, String... args) {
        arguments.add(new Argument(runnable, filler, player, args));
    }

    public abstract boolean onCommand(CommandSender sender, String[] args);

    protected class Argument {
        protected String[] args;
        protected boolean filler;
        protected boolean player;
        protected ArgumentRunnable runnable;

        public Argument(ArgumentRunnable runnable, boolean filler, boolean player, String... args) {
            this.runnable = runnable;
            this.filler = filler;
            this.player = player;
            this.args = args;
        }

        protected boolean matches(String[] strings) {
            if (strings.length != args.length && !filler) {
                return false;
            }
            if (strings.length != args.length + 1 && filler) {
                return false;
            }
            for (int i = 0; i < args.length; i++) {
                if (!strings[i].equalsIgnoreCase(args[0])) {
                    return false;
                }
            }
            return true;
        }
    }

    protected interface ArgumentRunnable {
        boolean executeArgument(CommandSender sender, String arg);
    }
}