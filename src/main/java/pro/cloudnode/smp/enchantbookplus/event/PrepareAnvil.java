package pro.cloudnode.smp.enchantbookplus.event;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jspecify.annotations.NullMarked;
import pro.cloudnode.smp.enchantbookplus.ConfigEnchantmentEntry;
import pro.cloudnode.smp.enchantbookplus.EnchantBookPlus;
import pro.cloudnode.smp.enchantbookplus.Permissions;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@NullMarked
public final class PrepareAnvil implements Listener {
    final EnchantBookPlus plugin;

    public PrepareAnvil(final EnchantBookPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepareAnvil(final PrepareAnvilEvent event) {
        final Optional<ItemStack> result = Optional.ofNullable(event.getResult());
        if (result.isEmpty()) {
            return;
        }

        final AnvilInventory inventory = event.getInventory();

        final ItemStack item = inventory.getItem(0);
        if (item == null) {
            return;
        }

        final ItemStack upgrade = inventory.getItem(1);
        if (upgrade == null) {
            return;
        }

        final Map<Enchantment, Integer> itemEnchants = item.getType() == Material.ENCHANTED_BOOK
                && item.getItemMeta() instanceof final EnchantmentStorageMeta itemMeta
                ? itemMeta.getStoredEnchants()
                : item.getEnchantments();

        final Map<Enchantment, Integer> upgradeEnchants = upgrade.getType() == Material.ENCHANTED_BOOK
                && upgrade.getItemMeta() instanceof final EnchantmentStorageMeta upgradeMeta
                ? upgradeMeta.getStoredEnchants()
                : upgrade.getEnchantments();
        if (upgradeEnchants.isEmpty()) {
            return;
        }

        final Map<Enchantment, Integer> upgrades = new HashMap<>();

        int cost = 0;

        for (final Map.Entry<Enchantment, Integer> entry : upgradeEnchants.entrySet()) {
            final Enchantment enchantment = entry.getKey();

            if (!event.getView().getPlayer().hasPermission(Permissions.enchant(enchantment))) {
                continue;
            }

            if (enchantment.getMaxLevel() == 1) {
                continue;
            }

            final Optional<ConfigEnchantmentEntry> configEnchantment = plugin.getConfigEnchantment(enchantment);
            if (configEnchantment.isEmpty()) {
                continue;
            }

            final int upgradeLevel = entry.getValue();

            final int finalLevel;

            if (itemEnchants.containsKey(enchantment)) {
                final int itemLevel = itemEnchants.get(enchantment);
                if (itemLevel > upgradeLevel) {
                    finalLevel = itemLevel;
                } else if (itemLevel == upgradeLevel) {
                    finalLevel = upgradeLevel + 1;
                } else {
                    finalLevel = upgradeLevel;
                }
            } else {
                finalLevel = upgradeLevel;
            }

            if (finalLevel <= enchantment.getMaxLevel()) {
                continue;
            }

            if (configEnchantment.get().getMaxLevel().isPresent() && finalLevel > configEnchantment.get()
                    .getMaxLevel()
                    .getAsInt()) {
                // guard against vanilla downgrade
                upgrades.put(enchantment, configEnchantment.get().getMaxLevel().getAsInt());
                continue;
            }

            if (finalLevel > upgradeLevel) {
                cost += configEnchantment.get().getMultiplyCostByLevel() ? configEnchantment.get().getCost() * (
                        finalLevel - enchantment.getMaxLevel()) : configEnchantment.get().getCost();
            }

            upgrades.put(enchantment, finalLevel);
        }

        if (upgrades.isEmpty()) {
            return;
        }

        final ItemStack resultStack = result.get().clone();
        inventory.setRepairCost(inventory.getRepairCost() + cost);

        for (final Map.Entry<Enchantment, Integer> entry : upgrades.entrySet()) {
            if (resultStack.getItemMeta() instanceof final EnchantmentStorageMeta resultMeta) {
                resultMeta.addStoredEnchant(entry.getKey(), entry.getValue(), true);
                resultStack.setItemMeta(resultMeta);
            } else {
                resultStack.addUnsafeEnchantment(entry.getKey(), entry.getValue());
            }
        }

        event.setResult(resultStack);
    }
}
