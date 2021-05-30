package me.polishkrowa.itemseller;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ItemSeller extends JavaPlugin implements CommandExecutor, TabCompleter {

    private static final Logger log = Logger.getLogger("Minecraft");
    private static Economy econ = null;

    @Override
    public void onEnable() {
        // Plugin startup logic
        if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.getCommand("sell").setExecutor(this);
        this.getCommand("sell").setTabCompleter(this);
        this.saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("help")) {
                sender.sendMessage(ChatColor.YELLOW + "" + String.format(this.getConfig().getString("message_help"), this.getConfig().getString("item_display_name"), this.getConfig().getDouble("item_price"), this.getConfig().getString("currency_symbol")));
                return true;
            }
            if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("itemseller.reload")) {
                    sender.sendMessage(ChatColor.RED + this.getConfig().getString("message_permission"));
                    return true;
                }
                this.saveDefaultConfig();
                this.reloadConfig();
                sender.sendMessage(ChatColor.GREEN + this.getConfig().getString("message_reload"));
                return true;
            }
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + this.getConfig().getString("message_player"));
            return true;
        }


        Player player = (Player) sender;
        ItemStack stack = player.getInventory().getItemInMainHand();

        if (stack == null) {
            sendInvalidItemMsg(sender);
            return true;
        } else if (!stack.getType().equals(Material.valueOf(this.getConfig().getString("item_name")))) {
            sendInvalidItemMsg(sender);
            return true;
        }

        int amount = stack.getAmount();

        econ.depositPlayer(player, amount * 0.5);

        stack.setAmount(0);

        player.sendMessage(ChatColor.GREEN + String.format(this.getConfig().getString("message_success"), amount * 0.5, this.getConfig().getString("currency_symbol")));

        return true;
    }

    private void sendInvalidItemMsg(CommandSender player) {
        player.sendMessage(ChatColor.RED + String.format(this.getConfig().getString("message_invalid"), this.getConfig().getString("item_display_name"), this.getConfig().getDouble("item_price"), this.getConfig().getString("currency_symbol")));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> output = new ArrayList<>();
        if (args.length == 1) {
            output.add("help");
            if (sender.hasPermission("itemseller.reload"))
                output.add("reload");
        }

        return output;
    }
}
