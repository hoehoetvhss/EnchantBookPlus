package pro.cloudnode.smp.enchantbookplus;

import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class Permissions {
    public final static String RELOAD = "enchantbookplus.reload";

    public static String enchant(final Enchantment enchantment) {
        return "enchantbookplus.enchant." + enchantment.getKey().getKey();
    }

    public static void init(final EnchantBookPlus plugin) {
        final PluginManager pm = plugin.getServer().getPluginManager();

        pm.addPermission(new Permission(
                RELOAD,
                "Reload plugin config using ‘/enchantbookplus reload’",
                PermissionDefault.OP
        ));

        for (Enchantment enchantment : Registry.ENCHANTMENT) {
            pm.addPermission(new Permission(
                    enchant(enchantment),
                    "Allow enchanting " + enchantment.getKey() + "above the vanilla level, as configured in the plugin",
                    PermissionDefault.TRUE
            ));
        }
    }
}
