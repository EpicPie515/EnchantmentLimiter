package net.kjnine.enchantmentlimiter;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class EnchantmentLimiter extends JavaPlugin {
	
	private Map<Enchantment, Integer> limits;
	
	private boolean useNamespacedNames = true;
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		limits = new HashMap<>();
		try {
			Class.forName("org.bukkit.NamespacedKey");
		} catch(ClassNotFoundException e) {
			useNamespacedNames = false;
		}
		reloadLimits();
		getServer().getPluginManager().registerEvents(new EnchantListener(this), this);
		getLogger().info(getDescription().getName() + " v" + getDescription().getVersion() + " enabled.");
	}
	
	public String enchantName(Enchantment en) {
		if(useNamespacedNames) {
			return en.getKey().getKey();
		} else {
			return en.getName().toLowerCase();
		}
	}
	
	public Enchantment getEnchant(String name) {
		if(useNamespacedNames) {
			return Enchantment.getByKey(NamespacedKey.minecraft(name));
		} else {
			return Enchantment.getByName(name.toUpperCase());
		}
	}
	
	public void reloadLimits() {
		ConfigurationSection ench = getConfig().getConfigurationSection("enchant-limits");
		Map<Enchantment, Integer> limitsTemp = new HashMap<>();
		for(String k : ench.getKeys(false)) {
			Enchantment en = getEnchant(k);
			if(en == null) {
				getLogger().severe("Unknown enchantment: '" + k + "' in EnchantmentLimiter configuration.");
				return;
			}
			limitsTemp.put(en, ench.getInt(k));
		}
		limits = limitsTemp;
		getLogger().info("Enchant Limits reloaded from config.");
	}
	
	public void setLimit(Enchantment ench, int limit) {
		limits.put(ench, limit);
		getConfig().set("enchant-limits." + enchantName(ench), limit);
		saveConfig();
	}
	
	/**
	 * @return the level limit for the given enchantment, or null if the enchant is not limited.
	 */
	public int getLimit(Enchantment ench) {
		return limits.getOrDefault(ench, -1);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equals("enchantlimiter")) {
			if(sender instanceof Player) {
				Player p = (Player) sender;
				if(p.hasPermission("enchantlimiter.view")) {
					if(args.length >= 1 && args[0].equalsIgnoreCase("reload") && p.hasPermission("enchantlimiter.manage")) {
						reloadConfig();
						reloadLimits();
						sendMsg(p, "&9&m========================");
						sendMsg(p, "&a - - Config Reloaded - - ");
					}
					sendMsg(p, "&9&m========================");
					limits.forEach((en, lv) -> {
						sendMsg(p, getConfig().getString("messages.enchantlimit").replace("{0}", enchantName(en)).replace("{1}", lv + (lv == 0 ? "&7 (&cDisabled&7)" : "")));
					});
					if(p.hasPermission("enchantlimiter.version")) sendMsg(p, "&7" + getDescription().getName() + " v" + getDescription().getVersion() + " by KJNine");
					sendMsg(p, "&9&m========================");
				} else {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.no-permission")));
					return true;
				}
			} else {
				if(args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
					reloadConfig();
					reloadLimits();
					sendMsg(sender, "&9&m========================");
					sendMsg(sender, "&a - - Config Reloaded - - ");
				}
				sendMsg(sender, "&9&m========================");
				limits.forEach((en, lv) -> {
					sendMsg(sender, getConfig().getString("messages.enchantlimit").replace("{0}", enchantName(en)).replace("{1}", lv + (lv == 0 ? "&7 (&cDisabled&7)" : "")));
				});
				sendMsg(sender, "&7" + getDescription().getName() + " v" + getDescription().getVersion() + " by KJNine");
				sendMsg(sender, "&9&m========================");
			}
		}
		return true;
	}
	
	public void sendMsg(CommandSender p, String s) {
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
	}
	
}
