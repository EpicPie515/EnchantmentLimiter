package net.kjnine.enchantmentlimiter;

import java.util.HashSet;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;

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
