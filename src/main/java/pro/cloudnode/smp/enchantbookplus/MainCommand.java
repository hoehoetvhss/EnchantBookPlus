package pro.cloudnode.smp.enchantbookplus;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public final class MainCommand implements CommandExecutor, TabCompleter {
    private final EnchantBookPlus plugin;

    public MainCommand(final EnchantBookPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            final CommandSender sender,
            final Command command,
            final String label,
            final String[] args
    ) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            return reload(sender);
        }

        return overview(sender);
    }

    @Override
    public List<String> onTabComplete(
            final CommandSender sender,
            final Command command,
            final String label,
            final String[] args
    ) {
        if (sender.hasPermission(Permissions.RELOAD)) {
            return List.of("reload");
        }

        return List.of();
    }

    /**
     * Plugin overview
     */
    @SuppressWarnings("SameReturnValue")
    public boolean overview(final CommandSender sender) {
        PluginDescriptionFile description = plugin.getDescription();
        sender.sendMessage("§a" + description.getName() + " §fv" + description.getVersion() + " by §7" + String.join(
                ", ",
                description.getAuthors()
        ));

        return true;
    }

    /**
     * Reload plugin configuration
     */
    @SuppressWarnings("SameReturnValue")
    public boolean reload(final CommandSender sender) {
        if (!sender.hasPermission(Permissions.RELOAD)) {
            return overview(sender);
        }

        plugin.reload();

        sender.sendMessage("§a(!) Plugin configuration reloaded.");

        return true;
    }
}
