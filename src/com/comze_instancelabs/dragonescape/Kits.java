package com.comze_instancelabs.dragonescape;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Kits {

	public static void giveJumperKit(Player p){
		// right click -> jump a bit
		p.getInventory().addItem(new ItemStack(Material.IRON_AXE, 4));
		p.updateInventory();
	}
	
	public static void giveBatKit(Player p){
		// when dying, you'll get a last chance and can ride for a few seconds on a bat (that might change)
	}
	
	public static void giveTNTKit(Player p){
		// right click -> throw tnt, if someone picks up tnt, gets blindness
		p.getInventory().addItem(new ItemStack(Material.IRON_AXE, 4));
		p.getInventory().addItem(new ItemStack(Material.TNT, 4));
		p.updateInventory();
	}
	
	public static void giveWarperKit(Player p){
		// right click -> warp to player
		p.getInventory().addItem(new ItemStack(Material.IRON_AXE, 2));
		p.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));
		p.updateInventory();
	}
}
