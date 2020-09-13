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

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;

import net.md_5.bungee.api.ChatColor;

public class EnchantmentLimiter extends JavaPlugin {
	
	private Map<Enchantment, Integer> limits;
	
	public boolean autoLapis = false;
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		limits = new HashMap<>();
		reloadLimits();
		getServer().getPluginManager().registerEvents(new EnchantListener(this), this);
		try {
			// Force throw an exception if the version is before EnchantmentOffer was added.
			Class.forName("org.bukkit.enchantments.EnchantmentOffer");
			// No Exception thrown, Register the listener.
			getServer().getPluginManager().registerEvents(new PrepareEnchantListener(this), this);
		} catch(ClassNotFoundException e) { }
		getLogger().info(getDescription().getName() + " v" + getDescription().getVersion() + " enabled.");
	}
	
	public String enchantName(Enchantment en) {
		if(XMaterial.isNewVersion()) {
			if(!en.getKey().getNamespace().equals(NamespacedKey.MINECRAFT)) return en.getKey().getKey().toLowerCase();
		}
		return XEnchantment.matchXEnchantment(en).getVanillaName().toLowerCase();
	}
	
	@SuppressWarnings("deprecation")
	public Enchantment getEnchant(String name) {
		if(name.contains(":") && XMaterial.isNewVersion() && !name.split(":")[0].equalsIgnoreCase("minecraft")) {
			String[] spl = name.split(":");
			return Enchantment.getByKey(new NamespacedKey(spl[0], spl[1]));
		}
		XEnchantment xen = XEnchantment.matchXEnchantment(name).orElseGet(() -> null);
		if(xen == null) return null;
		return xen.parseEnchantment();
	}
	
	public void reloadLimits() {
		autoLapis = getConfig().getBoolean("auto-lapis");
		ConfigurationSection ench = getConfig().getConfigurationSection("enchant-limits");
		Map<Enchantment, Integer> limitsTemp = new HashMap<>();
		for(String k : ench.getKeys(false)) {
			Enchantment en = getEnchant(k);
			if(en == null) {
				getLogger().severe("Config Reload - Unknown enchantment: '" + k + "'");
				continue;
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
