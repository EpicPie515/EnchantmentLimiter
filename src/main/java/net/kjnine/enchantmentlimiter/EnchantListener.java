package net.kjnine.enchantmentlimiter;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.ItemStack;

import com.cryptomorin.xseries.XMaterial;

public class EnchantListener implements Listener {

	private EnchantmentLimiter limit;
	
	public EnchantListener(EnchantmentLimiter pl) {
		this.limit = pl;
	}
	
	@EventHandler
	public void onEnchant(EnchantItemEvent e) {
		for(Enchantment ench : new HashSet<>(e.getEnchantsToAdd().keySet())) {
			int lim = limit.getLimit(ench);
			if(lim < 0) continue;
			if(lim == 0) {
				e.getEnchantsToAdd().remove(ench);
			} else if(e.getEnchantsToAdd().get(ench) > lim) {
				e.getEnchantsToAdd().replace(ench, lim);
			}
		}
		ItemStack m = XMaterial.LAPIS_LAZULI.parseItem();
		m.setAmount(3);
		e.getInventory().setItem(1, m);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onOpen(InventoryOpenEvent e) {
		if(!limit.autoLapis) return;
		if(e.getInventory().getType().equals(InventoryType.ENCHANTING)) {
			EnchantingInventory inv = (EnchantingInventory) e.getInventory();
			ItemStack m = XMaterial.LAPIS_LAZULI.parseItem();
			m.setAmount(3);
			inv.setItem(1, m);
		}
	}
	
	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		if(!limit.autoLapis) return;
		if(e.getInventory().getType().equals(InventoryType.ENCHANTING)) {
			EnchantingInventory inv = (EnchantingInventory) e.getInventory();
			inv.setItem(1, new ItemStack(Material.AIR));
		}
	}
	
	@EventHandler
	public void onInvClick(InventoryClickEvent e) {
		if(e.getInventory().getType().equals(InventoryType.ANVIL)) {
			AnvilInventory inv = (AnvilInventory) e.getInventory();
			
			if(inv.getItem(2) != null) {
				ItemStack res = inv.getItem(2);
				for(Enchantment ench : new HashSet<>(res.getEnchantments().keySet())) {
					int lim = limit.getLimit(ench);
					if(lim < 0) continue;
					if(lim == 0) {
						res.removeEnchantment(ench);
					} else if(res.getEnchantmentLevel(ench) > lim) {
						res.addEnchantment(ench, lim);
					}
				}
				inv.setItem(2, res);
			}
		} else if(e.getInventory().getType().equals(InventoryType.ENCHANTING)) {
			if(e.getSlot() == 1) e.setCancelled(true);
		}
	}
	
}
