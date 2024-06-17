package me.spacety.totemzombie;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.spacety.totemzombie.util.PlayerMessage;
import net.kyori.adventure.text.TextComponent;

public class ZombieTotemExecutor implements CommandExecutor, TabCompleter{

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command arg1, @NotNull String arg2,
            @NotNull String[] args) {

        if (args.length == 0) {
            commandSender.sendMessage(PlayerMessage.formatColors(Main.getString("messages.error.no-args")));
            return false;
        }

        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            if (args[0].equalsIgnoreCase("spawn") && player.hasPermission("totemzombie.spawn")) {
                handleTZSpawn(player);
                return true;
            } else if (args[0].equalsIgnoreCase("despawn") && player.hasPermission("totemzombie.despawn")) {
                handleTZDespawn(player);
                return true;
            } else if (args[0].equalsIgnoreCase("reload") && player.hasPermission("totemzombie.reload")) {
                Main.reloadCfg();
                PlayerMessage.send(player, Main.getString("messages.reload"));
                return true;
            } else {
                if (!player.hasPermission("totemzombie.spawn") || !player.hasPermission("totemzombie.despawn") || !player.hasPermission("totemzombie.reload")) {
                    PlayerMessage.send(player, Main.getString("messages.error.no-permission"));
                    return true;
                }
                PlayerMessage.send(player, "&cUsage: /tz <spawn/despawn>");
                return true;
            }
        } else {
            if (args[0].equalsIgnoreCase("reload")) {
                Main.reloadCfg();
                commandSender.sendMessage(PlayerMessage.formatColors(Main.getString("messages.reload")));
                return true;
            } else {
                commandSender.sendMessage(PlayerMessage.formatColors(Main.getString("messages.error.playerOnly")));
            }
        }

    return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command arg1,
            @NotNull String arg2, @NotNull String[] args) {

        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>();
            subCommands.add("spawn");
            subCommands.add("despawn");
            subCommands.add("reload");
            return subCommands.stream()
                .filter(subCommand -> subCommand.toLowerCase(Locale.ROOT).contains(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        return null;
    }

    private void handleTZSpawn(Player player) {
        Entity entity = player.getLocation().getWorld().spawnEntity(player.getLocation(), EntityType.ZOMBIE);

        TextComponent displayName = PlayerMessage.formatColors(Main.getString("zombie-totem.display-name"));
        entity.customName(displayName);
        entity.setCustomNameVisible(true);
        entity.setGlowing(true);
        Zombie zombie = (Zombie) entity;
        zombie.setTarget(player);

        ItemStack helmet = createArmorItem(Material.DIAMOND_HELMET, Enchantment.PROTECTION_ENVIRONMENTAL, 4);
        ItemStack chestplate = createArmorItem(Material.DIAMOND_CHESTPLATE, Enchantment.PROTECTION_ENVIRONMENTAL, 4);
        ItemStack leggings = createArmorItem(Material.DIAMOND_LEGGINGS, Enchantment.PROTECTION_EXPLOSIONS, 4);
        ItemStack boots = createArmorItem(Material.DIAMOND_BOOTS, Enchantment.PROTECTION_ENVIRONMENTAL, 4);

        ItemStack totems = new ItemStack(Material.TOTEM_OF_UNDYING, 27);

        zombie.getEquipment().setHelmet(helmet);
        zombie.getEquipment().setChestplate(chestplate);
        zombie.getEquipment().setLeggings(leggings);
        zombie.getEquipment().setBoots(boots);

        zombie.getEquipment().setItemInOffHand(totems);

        zombie.getEquipment().setHelmetDropChance(0.0F);
        zombie.getEquipment().setChestplateDropChance(0.0F);
        zombie.getEquipment().setLeggingsDropChance(0.0F);
        zombie.getEquipment().setBootsDropChance(0.0F);

        zombie.getEquipment().setItemInOffHandDropChance(0.0F);

        NamespacedKey key = new NamespacedKey(Main.getPlugin(Main.class), "totem_zombie_owner");
        zombie.getPersistentDataContainer().set(key, PersistentDataType.STRING, player.getUniqueId().toString());

        PlayerMessage.send(player, Main.getString("messages.totem-zombie-spawn.success").replace("%player%", player.getName()));
    }

    private void handleTZDespawn(Player player) {
        for (Entity entity : player.getLocation().getWorld().getEntities()) {
            if (entity.getType() == EntityType.ZOMBIE) {
                Zombie zombie = (Zombie) entity;
                NamespacedKey key = new NamespacedKey(Main.getPlugin(Main.class), "totem_zombie_owner");
                if (zombie.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                    String owner = zombie.getPersistentDataContainer().get(key, PersistentDataType.STRING);
                    if (owner.equals(player.getUniqueId().toString())) {
                        zombie.remove();
                        PlayerMessage.send(player, Main.getString("messages.totem-zombie-despawn.success").replace("%player%", player.getName()));
                        return;
                    }
                }
            }
        }
        PlayerMessage.send(player, Main.getString("messages.totem-zombie-despawn.fail").replace("%player%", player.getName()));
    }
    
    private ItemStack createArmorItem(Material material, Enchantment enchantment, int level) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addEnchant(enchantment, level, true);
            meta.setUnbreakable(true);
            item.setItemMeta(meta);
        }
        return item;
    }
}
