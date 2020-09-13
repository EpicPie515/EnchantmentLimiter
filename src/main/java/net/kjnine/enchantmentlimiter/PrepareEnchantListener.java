package net.kjnine.enchantmentlimiter;

import java.util.HashSet;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

public class PrepareEnchantListener implements Listener {

	private EnchantmentLimiter limit;
	
	public PrepareEnchantListener(EnchantmentLimiter pl) {
		this.limit = pl;
	}
	
	@EventHandler
	public void onAnvil(PrepareAnvilEvent e) {
		if(e.getResult() != null) {
			ItemStack res = e.getResult();
			for(Enchantment ench : new HashSet<>(res.getEnchantments().keySet())) {
				int lim = limit.getLimit(ench);
				if(lim < 0) continue;
				if(lim == 0) {
					res.removeEnchantment(ench);
				} else if(res.getEnchantmentLevel(ench) > lim) {
					res.addEnchantment(ench, lim);
				}
			}
		}
	}
	
	@EventHandler
	public void onPrepareEnchant(PrepareItemEnchantEvent e) {
		for(int i = 0; i < e.getOffers().length; i++) {
			EnchantmentOffer offer = e.getOffers()[i];
			int lim = limit.getLimit(offer.getEnchantment());
			if(lim == 0) {
				e.getOffers()[i] = null;
			} else if(offer.getEnchantmentLevel() > lim) {
				offer.setEnchantmentLevel(lim);
				e.getOffers()[i] = offer;
			}
		}
	}
}
