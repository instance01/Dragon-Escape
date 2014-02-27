package com.comze_instancelabs.dragonescape;

import java.awt.List;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.minecraft.server.v1_7_R1.AttributeInstance;
import net.minecraft.server.v1_7_R1.EntityInsentient;
import net.minecraft.server.v1_7_R1.EntityTypes;
import net.minecraft.server.v1_7_R1.GenericAttributes;
import net.minecraft.server.v1_7_R1.Item;
import net.minecraft.server.v1_7_R1.NBTTagString;
import net.minecraft.server.v1_7_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_7_R1.PacketPlayOutWorldEvent;

import org.apache.logging.log4j.core.config.plugins.PluginManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.Vector;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class Main extends JavaPlugin implements Listener {

	/*
	 * 
	 * This is based off the ColorMatch arena system
	 */

	/*
	 * de setmainlobby
	 * 
	 * de createarena [name] de setlobby [name] de setfinish [name] de setbounds
	 * [name] [low/high] de savearena [name]
	 */

	public static Economy econ = null;

	/**
	 * arena -> wether ingame or not
	 */
	public static HashMap<String, Boolean> ingame = new HashMap<String, Boolean>();

	/**
	 * arena -> task
	 */
	public static HashMap<String, BukkitTask> tasks = new HashMap<String, BukkitTask>();

	/**
	 * player -> arena
	 */
	public static HashMap<Player, String> arenap = new HashMap<Player, String>();
	public static HashMap<String, String> arenap_ = new HashMap<String, String>();

	/**
	 * player -> inventory
	 */
	public static HashMap<Player, ItemStack[]> pinv = new HashMap<Player, ItemStack[]>();

	/**
	 * player -> wether lost or not
	 */
	public static HashMap<Player, String> lost = new HashMap<Player, String>();

	public static HashMap<String, Test> dragons = new HashMap<String, Test>();

	int default_max_players = 4;
	int default_min_players = 3;

	boolean economy = true;
	int reward = 30;
	int itemid = 264;
	int itemamount = 1;
	boolean command_reward = false;
	String cmd = "";
	boolean start_announcement = false;
	boolean winner_announcement = false;
	String dragon_name = "Ender Dragon";
	public double dragon_speed = 1.0;
	
	int start_countdown = 5;

	public String saved_arena = "";
	public String saved_lobby = "";
	public String saved_mainlobby = "";
	public String not_in_arena = "";
	public String reloaded = "";
	public String arena_ingame = "";
	public String arena_invalid = "";
	public String arena_invalid_sign = "";
	public String you_fell = "";
	public String arena_invalid_component = "";
	public String you_won = "";
	public String starting_in = "";
	public String starting_in2 = "";
	public String arena_full = "";
	public String removed_arena = "";
	public String winner_an = "";
	public String noperm = "";
	public String saved_finish = "";
	public String saved_spawn = "";

	// anouncements
	public String starting = "";
	public String started = "";

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);

		registerEntity();

		getConfig().options().header("I recommend you to set auto_updating to true for possible future bugfixes. If use_economy is set to false, the winner will get the item reward.");
		getConfig().addDefault("config.auto_updating", true);
		getConfig().addDefault("config.start_countdown", 5);
		getConfig().addDefault("config.default_max_players", 4);
		getConfig().addDefault("config.default_min_players", 3);
		getConfig().addDefault("config.use_economy_reward", true);
		getConfig().addDefault("config.money_reward_per_game", 30);
		getConfig().addDefault("config.itemid", 264); // diamond
		getConfig().addDefault("config.itemamount", 1);
		getConfig().addDefault("config.use_command_reward", false);
		getConfig().addDefault("config.command_reward", "pex user [user] group set DragonPro");
		getConfig().addDefault("config.start_announcement", false);
		getConfig().addDefault("config.winner_announcement", false);
	    getConfig().addDefault("config.dragon_speed", 1.0D);
	    getConfig().addDefault("config.dragon_healthbar_name", "Ender Dragon");

		getConfig().addDefault("strings.saved.arena", "&aSuccessfully saved arena.");
		getConfig().addDefault("strings.saved.lobby", "&aSuccessfully saved lobby.");
		getConfig().addDefault("strings.saved.finish", "&aSuccessfully saved finishline.");
		getConfig().addDefault("strings.saved.spawn", "&aSuccessfully saved spawn.");
		getConfig().addDefault("strings.removed_arena", "&cSuccessfully removed arena.");
		getConfig().addDefault("strings.not_in_arena", "&cYou don't seem to be in an arena right now.");
		getConfig().addDefault("strings.config_reloaded", "&6Successfully reloaded config.");
		getConfig().addDefault("strings.arena_is_ingame", "&cThe arena appears to be ingame.");
		getConfig().addDefault("strings.arena_invalid", "&cThe arena appears to be invalid.");
		getConfig().addDefault("strings.arena_invalid_sign", "&cThe arena appears to be invalid, because a join sign is missing.");
		getConfig().addDefault("strings.arena_invalid_component", "&2The arena appears to be invalid (missing components or misstyped arena)!");
		getConfig().addDefault("strings.you_fell", "&3You fell! Type &6/de leave &3to leave.");
		getConfig().addDefault("strings.you_won", "&aYou won this round, awesome man! Here, enjoy your reward.");
		getConfig().addDefault("strings.starting_in", "&aStarting in &6");
		getConfig().addDefault("strings.starting_in2", "&a seconds.");
		getConfig().addDefault("strings.arena_full", "&cThis arena is full!");
		getConfig().addDefault("strings.starting_announcement", "&aStarting a new DragonEscape Game in &6");
		getConfig().addDefault("strings.started_announcement", "&aA new DragonEscape Round has started!");
		getConfig().addDefault("strings.winner_announcement", "&6<player> &awon the game on arena &6<arena>!");
		getConfig().addDefault("strings.noperm", "&cYou don't have permission.");

		getConfig().options().copyDefaults(true);
		if (getConfig().isSet("config.min_players")) {
			getConfig().set("config.min_players", null);
		}
		this.saveConfig();

		getConfigVars();

		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
		}

		// TODO ADD UPDATER
		/*
		 * if (getConfig().getBoolean("config.auto_updating")) { Updater updater
		 * = new Updater(this, 71774, this.getFile(),
		 * Updater.UpdateType.DEFAULT, false); }
		 */

		if (economy) {
			if (!setupEconomy()) {
				getLogger().severe(String.format("[%s] - No iConomy dependency found! Disabling Economy.", getDescription().getName()));
				economy = false;
			}
		}

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

	public void getConfigVars() {
		default_max_players = getConfig().getInt("config.default_max_players");
		default_min_players = getConfig().getInt("config.default_min_players");
		reward = getConfig().getInt("config.money_reward");
		itemid = getConfig().getInt("config.itemid");
		itemamount = getConfig().getInt("config.itemamount");
		economy = getConfig().getBoolean("config.use_economy_reward");
		command_reward = getConfig().getBoolean("config.use_command_reward");
		cmd = getConfig().getString("config.command_reward");
		start_countdown = getConfig().getInt("config.start_countdown");
		start_announcement = getConfig().getBoolean("config.start_announcement");
		winner_announcement = getConfig().getBoolean("config.winner_announcement");
        dragon_speed = getConfig().getDouble("config.dragon_speed");
        if(dragon_speed < 0.05 || dragon_speed > 10){
        	dragon_speed = 1.0;
        }
		dragon_name = getConfig().getString("config.dragon_healthbar_name").replaceAll("&", "§");
        
		saved_arena = getConfig().getString("strings.saved.arena").replaceAll("&", "§");
		removed_arena = getConfig().getString("strings.removed_arena").replaceAll("&", "§");
		saved_lobby = getConfig().getString("strings.saved.lobby").replaceAll("&", "§");
		saved_finish = getConfig().getString("strings.saved.finish").replaceAll("&", "§");
		saved_spawn = getConfig().getString("strings.saved.spawn").replaceAll("&", "§");
		saved_mainlobby = "" + ChatColor.GREEN + "Successfully saved main lobby";
		not_in_arena = getConfig().getString("strings.not_in_arena").replaceAll("&", "§");
		reloaded = getConfig().getString("strings.config_reloaded").replaceAll("&", "§");
		arena_ingame = getConfig().getString("strings.arena_is_ingame").replaceAll("&", "§");
		arena_invalid = getConfig().getString("strings.arena_invalid").replaceAll("&", "§");
		arena_invalid_sign = getConfig().getString("strings.arena_invalid_sign").replaceAll("&", "§");
		you_fell = getConfig().getString("strings.you_fell").replaceAll("&", "§");
		arena_invalid_component = getConfig().getString("strings.arena_invalid_component").replace("&", "§");
		you_won = getConfig().getString("strings.you_won").replaceAll("&", "§");
		starting_in = getConfig().getString("strings.starting_in").replaceAll("&", "§");
		starting_in2 = getConfig().getString("strings.starting_in2").replaceAll("&", "§");
		arena_full = getConfig().getString("strings.arena_full").replaceAll("&", "§");
		starting = getConfig().getString("strings.starting_announcement").replaceAll("&", "§");
		started = getConfig().getString("strings.started_announcement").replaceAll("&", "§");
		removed_arena = getConfig().getString("strings.removed_arena").replaceAll("&", "§");
		winner_an = getConfig().getString("strings.winner_announcement").replaceAll("&", "§");
		noperm = getConfig().getString("strings.noperm").replaceAll("&", "§");

	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("dragonescape")) {
			if (args.length > 0) {
				String action = args[0];
				if (action.equalsIgnoreCase("createarena")) {
					// create arena
					if (args.length > 1) {
						if (sender.hasPermission("dragonescape.setup")) {
							String arenaname = args[1];
							getConfig().set(arenaname + ".name", arenaname);
							this.saveConfig();
							sender.sendMessage(saved_arena);
						} else {
							sender.sendMessage(noperm);
						}
					} else {
						sender.sendMessage("" + ChatColor.RED + "No arena submitted. Usage: /de createarena [name]");
					}
				} else if (action.equalsIgnoreCase("removearena")) {
					if (args.length > 1) {
						if (sender.hasPermission("dragonescape.setup")) {
							String arenaname = args[1];
							if (getConfig().isSet(arenaname)) {
								getConfig().set(arenaname, null);
								this.saveConfig();
								File f = new File(this.getDataFolder() + "/" + arenaname);
								if (f.exists()) {
									f.delete();
								}
								sender.sendMessage(removed_arena);
							}
						} else {
							sender.sendMessage(noperm);
						}
					} else {
						sender.sendMessage("" + ChatColor.RED + "No arena submitted. Usage: /de createarena [name]");
					}
				} else if (action.equalsIgnoreCase("savearena")) {
					if (args.length > 1) {
						if (!(sender instanceof Player)) {
							return true;
						}

						Player p = (Player) sender;

						if (isValidArena(args[1])) {
							File f = new File(this.getDataFolder() + "/" + args[1]);
							f.delete();

							sender.sendMessage("" + ChatColor.GREEN + "Arena is now saving, " + ChatColor.GOLD + "this might take a while" + ChatColor.GREEN + ".");
							saveArenaToFile(p.getName(), args[1]);
						} else {
							sender.sendMessage("" + ChatColor.RED + "The arena appears to be invalid (missing components)!");
						}
					} else {
						sender.sendMessage("" + ChatColor.RED + "Usage: " + ChatColor.DARK_GREEN + "/de savearena [name]");
					}
				} else if (action.equalsIgnoreCase("setbounds")) {
					if (sender.hasPermission("dragonescape.setup")) {
						if (args.length > 2) {
							String arena = args[1];
							String count = args[2];
							if (!count.equalsIgnoreCase("low") && !count.equalsIgnoreCase("high")) {
								sender.sendMessage("" + ChatColor.RED + "Second parameter invalid. Usage: /de setbounds [arena] [low/high]");
								return true;
							}
							if (!getConfig().isSet(arena)) {
								sender.sendMessage("" + ChatColor.RED + "Could not find this arena.");
								return true;
							}

							if (!(sender instanceof Player)) {
								return true;
							}

							Player p = (Player) sender;

							getConfig().set(arena + ".boundary" + count + ".world", p.getWorld().getName());
							getConfig().set(arena + ".boundary" + count + ".loc.x", p.getLocation().getBlockX());
							if (count.equalsIgnoreCase("low")) {
								getConfig().set(arena + ".boundary" + count + ".loc.y", p.getLocation().getBlockY() - 1);
							} else {
								getConfig().set(arena + ".boundary" + count + ".loc.y", p.getLocation().getBlockY());
							}
							getConfig().set(arena + ".boundary" + count + ".loc.z", p.getLocation().getBlockZ());
							this.saveConfig();

							sender.sendMessage("" + ChatColor.YELLOW + "Successfully saved " + count + " boundary!");
						} else {
							sender.sendMessage("" + ChatColor.RED + "Usage: /de setbounds [arena] [count].");
						}
					} else {
						sender.sendMessage(noperm);
					}
				} else if (action.equalsIgnoreCase("boundstool")) {
					if (args.length > 1) {
						if (sender.hasPermission("dragonescape.setup")) {
							Player p = (Player) sender;
							String arenaname = args[1];
							if (!getConfig().isSet(arenaname)) {
								sender.sendMessage("" + ChatColor.RED + "Could not find this arena.");
								return true;
							}
							Inventory inv = p.getInventory();
							ItemStack is = new ItemStack(369, 1);
							ItemMeta im = (ItemMeta) is.getItemMeta();
							im.setDisplayName("" + ChatColor.GREEN + "Boundary tool for arena " + ChatColor.YELLOW + "" + arenaname);
							is.setItemMeta(im);
							inv.addItem(is);
							sender.sendMessage("" + ChatColor.YELLOW + "Here's the boundary tool. Left click the lower left point and right click the higher right point.");
						} else {
							sender.sendMessage(noperm);
						}
					} else {
						sender.sendMessage("" + ChatColor.RED + "Usage: /de boundstool [arena].");
					}
				} else if (action.equalsIgnoreCase("setflypoint")) {
					if (args.length > 1) {
						if (sender.hasPermission("dragonescape.setup")) {
							Player p = (Player) sender;
							String arenaname = args[1];
							
							String count = Integer.toString(this.getCurrentFlyPoint(arenaname));
							
							// overwrite functionality
							if(args.length > 2){
								if(isNumeric(args[2])){
									if(Integer.parseInt(args[2]) <= getCurrentFlyPoint(arenaname)){
										count = args[2];
									}
								}
							}
							
							getConfig().set(arenaname + ".flypoint." + count + ".world", p.getWorld().getName());
							getConfig().set(arenaname + ".flypoint." + count + ".x", p.getLocation().getBlockX());
							getConfig().set(arenaname + ".flypoint." + count + ".y", p.getLocation().getBlockY());
							getConfig().set(arenaname + ".flypoint." + count + ".z", p.getLocation().getBlockZ());
							this.saveConfig();
							sender.sendMessage(ChatColor.GREEN + "Saved Fly point " + count);
						} else {
							sender.sendMessage(noperm);
						}
					}
				} else if (action.equalsIgnoreCase("removeflypoint")) {
					if (args.length > 2) {
						if (sender.hasPermission("dragonescape.setup")) {
							Player p = (Player) sender;
							String arenaname = args[1];
							if(!isNumeric(args[2])){
								sender.sendMessage(ChatColor.RED + "Usage: /de removeflypoint [arena] [count]");
								return true;
							}
							if(!getConfig().isSet(arenaname + ".flypoint")){
								sender.sendMessage(ChatColor.RED + "Could not find any flypoints for this arena.");
								return true;
							}
							
							int rem_count = Integer.parseInt(args[2]);
							
							if(rem_count < this.getCurrentFlyPoint(arenaname)){
								getConfig().set(arenaname + ".flypoint." + Integer.toString(rem_count), null);
								this.saveConfig();
								sender.sendMessage(ChatColor.RED + "Removed Fly point " + Integer.toString(rem_count));
							}
						} else {
							sender.sendMessage(noperm);
						}
					}
				} else if (action.equalsIgnoreCase("setlobby")) {
					if (args.length > 1) {
						if (sender.hasPermission("dragonescape.setup")) {
							Player p = (Player) sender;
							String arenaname = args[1];
							getConfig().set(arenaname + ".lobby.world", p.getWorld().getName());
							getConfig().set(arenaname + ".lobby.loc.x", p.getLocation().getBlockX());
							getConfig().set(arenaname + ".lobby.loc.y", p.getLocation().getBlockY());
							getConfig().set(arenaname + ".lobby.loc.z", p.getLocation().getBlockZ());
							this.saveConfig();
							sender.sendMessage(saved_lobby);
						} else {
							sender.sendMessage(noperm);
						}
					}
				} else if (action.equalsIgnoreCase("setfinish")) {
					if (args.length > 1) {
						if (sender.hasPermission("dragonescape.setup")) {
							Player p = (Player) sender;
							String arenaname = args[1];
							getConfig().set(arenaname + ".finish.world", p.getWorld().getName());
							getConfig().set(arenaname + ".finish.loc.x", p.getLocation().getBlockX());
							getConfig().set(arenaname + ".finish.loc.y", p.getLocation().getBlockY());
							getConfig().set(arenaname + ".finish.loc.z", p.getLocation().getBlockZ());
							this.saveConfig();
							sender.sendMessage(saved_finish);
						} else {
							sender.sendMessage(noperm);
						}
					}
				} else if (action.equalsIgnoreCase("setspawn")) {
					if (args.length > 1) {
						if (sender.hasPermission("dragonescape.setup")) {
							Player p = (Player) sender;
							String arenaname = args[1];
							getConfig().set(arenaname + ".spawn.world", p.getWorld().getName());
							getConfig().set(arenaname + ".spawn.loc.x", p.getLocation().getBlockX());
							getConfig().set(arenaname + ".spawn.loc.y", p.getLocation().getBlockY());
							getConfig().set(arenaname + ".spawn.loc.z", p.getLocation().getBlockZ());
							getConfig().set(arenaname + ".spawn.loc.yaw", p.getLocation().getYaw());
							getConfig().set(arenaname + ".spawn.loc.pitch", p.getLocation().getPitch());
							this.saveConfig();
							sender.sendMessage(saved_spawn);
						} else {
							sender.sendMessage(noperm);
						}
					}
				} else if (action.equalsIgnoreCase("setdragonspawn")) {
					if (args.length > 1) {
						if (sender.hasPermission("dragonescape.setup")) {
							Player p = (Player) sender;
							String arenaname = args[1];
							getConfig().set(arenaname + ".dragonspawn.world", p.getWorld().getName());
							getConfig().set(arenaname + ".dragonspawn.loc.x", p.getLocation().getBlockX());
							getConfig().set(arenaname + ".dragonspawn.loc.y", p.getLocation().getBlockY());
							getConfig().set(arenaname + ".dragonspawn.loc.z", p.getLocation().getBlockZ());
							getConfig().set(arenaname + ".dragonspawn.loc.yaw", p.getLocation().getYaw());
							getConfig().set(arenaname + ".dragonspawn.loc.pitch", p.getLocation().getPitch());
							this.saveConfig();
							sender.sendMessage(saved_spawn);
						} else {
							sender.sendMessage(noperm);
						}
					}
				} else if (action.equalsIgnoreCase("setmainlobby")) {
					if (sender.hasPermission("dragonescape.setup")) {
						Player p = (Player) sender;
						getConfig().set("mainlobby.world", p.getWorld().getName());
						getConfig().set("mainlobby.loc.x", p.getLocation().getBlockX());
						getConfig().set("mainlobby.loc.y", p.getLocation().getBlockY());
						getConfig().set("mainlobby.loc.z", p.getLocation().getBlockZ());
						this.saveConfig();
						sender.sendMessage(saved_mainlobby);
					} else {
						sender.sendMessage(noperm);
					}
				} else if (action.equalsIgnoreCase("leave")) {
					Player p = (Player) sender;
					if (arenap.containsKey(p)) {
						leaveArena(p, true, false);
					} else {
						p.sendMessage(not_in_arena);
					}
				} else if (action.equalsIgnoreCase("endall")) {
					if (sender.hasPermission("dragonescape.end")) {
						for (String arena : tasks.keySet()) {
							try {
								tasks.get(arena).cancel();
							} catch (Exception e) {

							}
						}
						ingame.clear();
						Bukkit.getScheduler().cancelAllTasks();
					} else {
						sender.sendMessage(noperm);
					}
				} else if (action.equalsIgnoreCase("setmaxplayers")) {
					if (sender.hasPermission("dragonescape.setup")) {
						if (args.length > 2) {
							String arena = args[1];
							String playercount = args[2];
							if (!isNumeric(playercount)) {
								playercount = Integer.toString(default_max_players);
								sender.sendMessage("" + ChatColor.RED + "Playercount is invalid. Setting to default value.");
							}
							if (!getConfig().isSet(arena)) {
								sender.sendMessage("" + ChatColor.RED + "Could not find this arena.");
								return true;
							}
							this.setArenaMaxPlayers(arena, Integer.parseInt(playercount));
							sender.sendMessage("" + ChatColor.YELLOW + "Successfully set!");
						} else {
							sender.sendMessage("" + ChatColor.RED + "Usage: /de setmaxplayers [arena] [count].");
						}
					}
				} else if (action.equalsIgnoreCase("setminplayers")) {
					if (sender.hasPermission("dragonescape.setup")) {
						if (args.length > 2) {
							String arena = args[1];
							String playercount = args[2];
							if (!isNumeric(playercount)) {
								playercount = Integer.toString(default_min_players);
								sender.sendMessage("" + ChatColor.RED + "Playercount is invalid. Setting to default value.");
							}
							if (!getConfig().isSet(arena)) {
								sender.sendMessage("" + ChatColor.RED + "Could not find this arena.");
								return true;
							}
							this.setArenaMinPlayers(arena, Integer.parseInt(playercount));
							sender.sendMessage("" + ChatColor.YELLOW + "Successfully set!");
						} else {
							sender.sendMessage("" + ChatColor.RED + "Usage: /de setminplayers [arena] [count].");
						}
					} else {
						sender.sendMessage(noperm);
					}
				} else if (action.equalsIgnoreCase("setdifficulty")) {
					if (sender.hasPermission("dragonescape.setup")) {
						if (args.length > 2) {
							String arena = args[1];
							String difficulty = args[2];
							if (!isNumeric(difficulty)) {
								difficulty = "1";
								sender.sendMessage("" + ChatColor.RED + "Difficulty is invalid. Possible difficulties: 0, 1, 2.");
							}
							if (!getConfig().isSet(arena)) {
								sender.sendMessage("" + ChatColor.RED + "Could not find this arena.");
								return true;
							}
							sender.sendMessage("" + ChatColor.YELLOW + "Successfully set!");
						} else {
							sender.sendMessage("" + ChatColor.RED + "Usage: /de setdifficulty [arena] [difficulty]. Difficulty can be 0, 1 or 2.");
						}
					}
				} else if (action.equalsIgnoreCase("join")) {
					if (args.length > 1) {
						if (isValidArena(args[1])) {
							Sign s = null;
							try {
								s = this.getSignFromArena(args[1]);
							} catch (Exception e) {
								getLogger().warning("No sign found for arena " + args[1] + ". May lead to errors.");
							}
							if (s != null) {
								if (s.getLine(1).equalsIgnoreCase("" + ChatColor.DARK_GREEN + "[join]")) {
									joinLobby((Player) sender, args[1]);
								} else {
									sender.sendMessage(arena_ingame);
								}
							} else {
								sender.sendMessage(arena_invalid_sign);
							}
						} else {
							sender.sendMessage(arena_invalid);
						}
					}
				} else if (action.equalsIgnoreCase("start")) {
					if (args.length > 1) {
						if (sender.hasPermission("dragonescape.start")) {
							final String arena = args[1];
							if (!ingame.containsKey(arena)) {
								ingame.put(arena, false);
							}
							int count = 0;
							for (Player p : arenap.keySet()) {
								if (arenap.get(p).equalsIgnoreCase(arena)) {
									count++;
								}
							}
							if (count < 1) {
								sender.sendMessage("" + ChatColor.RED + "Noone is in this arena.");
								return true;
							}
							if (!ingame.get(arena)) {
								ingame.put(arena, true);
								for (Player p_ : arenap.keySet()) {
									if (arenap.get(p_).equalsIgnoreCase(arena)) {
										final Player p__ = p_;
										Bukkit.getScheduler().runTaskLater(this, new Runnable() {
											public void run() {
												p__.teleport(getSpawnForPlayer(arena));
											}
										}, 5);
									}
								}
								Bukkit.getScheduler().runTaskLater(this, new Runnable() {
									public void run() {
										start(arena);
									}
								}, 10);
							}
						} else {
							sender.sendMessage(noperm);
						}
					}
				} else if (action.equalsIgnoreCase("reload")) {
					if (sender.hasPermission("dragonescape.reload")) {
						this.reloadConfig();
						getConfigVars();
						sender.sendMessage(reloaded);
					} else {
						sender.sendMessage(noperm);
					}
				} else if (action.equalsIgnoreCase("list")) {
					if (sender.hasPermission("dragonescape.list")) {
						sender.sendMessage("" + ChatColor.GOLD + "-= Arenas =-");
						for (String arena : getConfig().getKeys(false)) {
							if (!arena.equalsIgnoreCase("mainlobby") && !arena.equalsIgnoreCase("strings") && !arena.equalsIgnoreCase("config")) {
								sender.sendMessage("" + ChatColor.DARK_GREEN + "" + arena);
							}
						}
					} else {
						sender.sendMessage(noperm);
					}
				} else {
					sender.sendMessage("" + ChatColor.GOLD + "-= DragonEscape " + ChatColor.DARK_GREEN + "help: " + ChatColor.GOLD + "=-");
					sender.sendMessage("" + ChatColor.DARK_GREEN + "To " + ChatColor.GOLD + "setup the main lobby " + ChatColor.DARK_GREEN + ", type in " + ChatColor.RED + "/de setmainlobby");
					sender.sendMessage("" + ChatColor.DARK_GREEN + "To " + ChatColor.GOLD + "setup " + ChatColor.DARK_GREEN + "a new arena, type in the following commands:");
					sender.sendMessage("" + ChatColor.DARK_GREEN + "/de createarena [name]");
					sender.sendMessage("" + ChatColor.DARK_GREEN + "/de setlobby [name] " + ChatColor.GOLD + " - for the waiting lobby");
					sender.sendMessage("" + ChatColor.DARK_GREEN + "/de setspawn [name] " + ChatColor.GOLD + " - players spawn here");
					sender.sendMessage("" + ChatColor.DARK_GREEN + "/de setfinish [name] " + ChatColor.GOLD + " - the finish line");
					sender.sendMessage("" + ChatColor.DARK_GREEN + "/de setbounds [name] " + ChatColor.GOLD + " - don't forget to set both high and low boundaries.");
					sender.sendMessage("" + ChatColor.DARK_GREEN + "/de savearena [name] " + ChatColor.GOLD + " - save the arena");
					sender.sendMessage("");
					sender.sendMessage("" + ChatColor.DARK_GREEN + "You can join with " + ChatColor.RED + "/de join [name] " + ChatColor.DARK_GREEN + "and leave with " + ChatColor.RED + "/de leave" + ChatColor.DARK_GREEN + ".");
					sender.sendMessage("" + ChatColor.DARK_GREEN + "You can force an arena to start with " + ChatColor.RED + "/de start [name]" + ChatColor.DARK_GREEN + ".");
				}
			} else {
				sender.sendMessage("" + ChatColor.GOLD + "-= DragonEscape " + ChatColor.DARK_GREEN + "help: " + ChatColor.GOLD + "=-");
				sender.sendMessage("" + ChatColor.DARK_GREEN + "To " + ChatColor.GOLD + "setup the main lobby " + ChatColor.DARK_GREEN + ", type in " + ChatColor.RED + "/de setmainlobby");
				sender.sendMessage("" + ChatColor.DARK_GREEN + "To " + ChatColor.GOLD + "setup " + ChatColor.DARK_GREEN + "a new arena, type in the following commands:");
				sender.sendMessage("" + ChatColor.DARK_GREEN + "/de createarena [name]");
				sender.sendMessage("" + ChatColor.DARK_GREEN + "/de setlobby [name] " + ChatColor.GOLD + " - for the waiting lobby");
				sender.sendMessage("" + ChatColor.DARK_GREEN + "/de setspawn [name] " + ChatColor.GOLD + " - players spawn here");
				sender.sendMessage("" + ChatColor.DARK_GREEN + "/de setfinish [name] " + ChatColor.GOLD + " - the finish line");
				sender.sendMessage("" + ChatColor.DARK_GREEN + "/de setbounds [name] " + ChatColor.GOLD + " - don't forget to set both high and low boundaries.");
				sender.sendMessage("" + ChatColor.DARK_GREEN + "/de savearena [name] " + ChatColor.GOLD + " - save the arena");
				sender.sendMessage("");
				sender.sendMessage("" + ChatColor.DARK_GREEN + "You can join with " + ChatColor.RED + "/de join [name] " + ChatColor.DARK_GREEN + "and leave with " + ChatColor.RED + "/de leave" + ChatColor.DARK_GREEN + ".");
				sender.sendMessage("" + ChatColor.DARK_GREEN + "You can force an arena to start with " + ChatColor.RED + "/de start [name]" + ChatColor.DARK_GREEN + ".");
			}
			return true;
		}
		return false;
	}

	private boolean registerEntity() {
		try {
			Class entityTypeClass = EntityTypes.class;

			Field c = entityTypeClass.getDeclaredField("c");
			c.setAccessible(true);
			HashMap c_map = (HashMap) c.get(null);
			c_map.put("Slimey", Slimey.class);

			Field d = entityTypeClass.getDeclaredField("d");
			d.setAccessible(true);
			HashMap d_map = (HashMap) d.get(null);
			d_map.put(Slimey.class, "Slimey");

			Field e = entityTypeClass.getDeclaredField("e");
			e.setAccessible(true);
			HashMap e_map = (HashMap) e.get(null);
			e_map.put(Integer.valueOf(55), Slimey.class);

			Field f = entityTypeClass.getDeclaredField("f");
			f.setAccessible(true);
			HashMap f_map = (HashMap) f.get(null);
			f_map.put(Slimey.class, Integer.valueOf(55));

			Field g = entityTypeClass.getDeclaredField("g");
			g.setAccessible(true);
			HashMap g_map = (HashMap) g.get(null);
			g_map.put("Slimey", Integer.valueOf(55));

		} catch (Exception ex) {
			
		}
		
		
		try {
			Class entityTypeClass = EntityTypes.class;

			Field c = entityTypeClass.getDeclaredField("c");
			c.setAccessible(true);
			HashMap c_map = (HashMap) c.get(null);
			c_map.put("Test", Test.class);

			Field d = entityTypeClass.getDeclaredField("d");
			d.setAccessible(true);
			HashMap d_map = (HashMap) d.get(null);
			d_map.put(Test.class, "Test");

			Field e = entityTypeClass.getDeclaredField("e");
			e.setAccessible(true);
			HashMap e_map = (HashMap) e.get(null);
			e_map.put(Integer.valueOf(63), Test.class);

			Field f = entityTypeClass.getDeclaredField("f");
			f.setAccessible(true);
			HashMap f_map = (HashMap) f.get(null);
			f_map.put(Test.class, Integer.valueOf(63));

			Field g = entityTypeClass.getDeclaredField("g");
			g.setAccessible(true);
			HashMap g_map = (HashMap) g.get(null);
			g_map.put("Test", Integer.valueOf(63));

			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public Test spawnEnderdragon(String arena, Location t) {
		Object w = ((CraftWorld) t.getWorld()).getHandle();
		if(this.getDragonWayPoints(arena) == null){
			getLogger().severe("You forgot to set any FlyPoints! You need to have min. 2 and one of them has to be at finish.");
			return null;
		}
		Test t_ = new Test(this, arena, t, (net.minecraft.server.v1_7_R1.World) ((CraftWorld) t.getWorld()).getHandle(), this.getDragonWayPoints(arena));
		((net.minecraft.server.v1_7_R1.World) w).addEntity(t_, CreatureSpawnEvent.SpawnReason.CUSTOM);
		t_.setCustomName(dragon_name);
		
		//TODO: send entity invisibility packet
		//TODO: might get possible though with HoloAPI when MC 1.8 is released
		/*Slimey b = new Slimey(this, t, (net.minecraft.server.v1_7_R1.World) ((CraftWorld) t.getWorld()).getHandle());
		((net.minecraft.server.v1_7_R1.World) w).addEntity(b, CreatureSpawnEvent.SpawnReason.CUSTOM);
		b.setCustomName(dragon_name);
		b.setInvisible(true);*/
		
		return t_;
	}

	public void removeEnderdragon(Test t) {
		if (t != null) {
			t.getBukkitEntity().remove();
		}
	}
	
	public void setDragonSpeed(EnderDragon s, double speed) {
		AttributeInstance attributes = ((EntityInsentient) ((CraftLivingEntity) s).getHandle()).getAttributeInstance(GenericAttributes.d);
		attributes.setValue(speed);
	}

	public ArrayList<String> left_players = new ArrayList<String>();

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		if (arenap.containsKey(event.getPlayer())) {
			String arena = arenap.get(event.getPlayer());
			getLogger().info(arena);
			int count = 0;
			for (Player p_ : arenap.keySet()) {
				if (arenap.get(p_).equalsIgnoreCase(arena)) {
					count++;
				}
			}

			try {
				Sign s = this.getSignFromArena(arena);
				if (s != null) {
					s.setLine(1, "" + ChatColor.DARK_GREEN + "[Join]");
					s.setLine(3, Integer.toString(count - 1) + "/" + Integer.toString(getArenaMaxPlayers(arena)));
					s.update();
				}
			} catch (Exception e) {
				getLogger().warning("You forgot to set a sign for arena " + arena + "! This might lead to errors.");
			}

			leaveArena(event.getPlayer(), true, true);
			left_players.add(event.getPlayer().getName());
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (left_players.contains(event.getPlayer().getName())) {
			final Player p = event.getPlayer();
			Bukkit.getScheduler().runTaskLater(this, new Runnable() {
				public void run() {
					p.teleport(getMainLobby());
					p.setFlying(false);
				}
			}, 5);
			left_players.remove(event.getPlayer().getName());
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if (arenap_.containsKey(event.getPlayer().getName())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onHunger(FoodLevelChangeEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			if (arenap_.containsKey(p.getName())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		// if (arenap_.containsKey(event.getPlayer().getName())) {
		if (arenap.containsKey(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		// if (arenap_.containsKey(event.getPlayer().getName())) {
		if (arenap.containsKey(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if (arenap_.containsKey(event.getPlayer().getName())) {
			if (lost.containsKey(event.getPlayer())) {
				Location l = getSpawn(lost.get(event.getPlayer()));
				final Location spectatorlobby = new Location(l.getWorld(), l.getBlockX(), l.getBlockY() + 30, l.getBlockZ());
				if (event.getPlayer().getLocation().getBlockY() < spectatorlobby.getBlockY() || event.getPlayer().getLocation().getBlockY() > spectatorlobby.getBlockY()) {
					final Player p = event.getPlayer();
					final float b = p.getLocation().getYaw();
					final float c = p.getLocation().getPitch();
					final String arena = arenap.get(event.getPlayer());
					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
						@Override
						public void run() {
							try {
								p.setAllowFlight(true);
								p.setFlying(true);
								p.teleport(new Location(p.getWorld(), p.getLocation().getBlockX(), spectatorlobby.getBlockY(), p.getLocation().getBlockZ(), b, c));
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}, 5);
					p.sendMessage(you_fell);
					return;
				}
			}

			String arena_ = arenap_.get(event.getPlayer().getName());
			String dir = m.getDirection(getSpawn(arena_).getYaw());
			if (dir.equalsIgnoreCase("south")) {
				if (event.getPlayer().getLocation().getBlockZ() > getFinish(arenap_.get(event.getPlayer().getName())).getBlockZ()) {
					if (ingame.get(arena_)) {
						stop(h.get(arena_), arena_);
					}
					return;
				}
			} else if (dir.equalsIgnoreCase("north")) {
				if (event.getPlayer().getLocation().getBlockZ() < getFinish(arenap_.get(event.getPlayer().getName())).getBlockZ()) {
					if (ingame.get(arena_)) {
						stop(h.get(arena_), arena_);
					}
					return;
				}
			} else if (dir.equalsIgnoreCase("east")) {
				if (event.getPlayer().getLocation().getBlockX() > getFinish(arenap_.get(event.getPlayer().getName())).getBlockX()) {
					if (ingame.get(arena_)) {
						stop(h.get(arena_), arena_);
					}
					return;
				}
			} else if (dir.equalsIgnoreCase("west")) {
				if (event.getPlayer().getLocation().getBlockX() < getFinish(arenap_.get(event.getPlayer().getName())).getBlockX()) {
					if (ingame.get(arena_)) {
						stop(h.get(arena_), arena_);
					}
					return;
				}
			}

			if (event.getPlayer().getLocation().getBlockY() < getSpawn(arenap_.get(event.getPlayer().getName())).getBlockY() - 5) {
				lost.put(event.getPlayer(), arenap.get(event.getPlayer()));
				final Player p__ = event.getPlayer();
				final String arena = arenap.get(event.getPlayer());
				Bukkit.getScheduler().runTaskLater(this, new Runnable() {
					public void run() {
						try {
							Location l = getSpawn(arena);
							p__.teleport(new Location(l.getWorld(), l.getBlockX(), l.getBlockY() + 30, l.getBlockZ()));
							p__.setAllowFlight(true);
							p__.setFlying(true);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, 5);

				int count = 0;

				for (Player p : arenap.keySet()) {
					if (arenap.get(p).equalsIgnoreCase(arena)) {
						if (!lost.containsKey(p)) {
							count++;
						}
					}
				}

				if (count < 2) {
					// last man standing!
					stop(h.get(arena), arena);
				}

				return;
			}
		}
	}

	@EventHandler
	public void onSignUse(PlayerInteractEvent event) {
		if (event.hasBlock()) {
			if (event.getClickedBlock().getType() == Material.SIGN_POST || event.getClickedBlock().getType() == Material.WALL_SIGN) {
				final Sign s = (Sign) event.getClickedBlock().getState();
				if (s.getLine(0).toLowerCase().contains("dragonescape")) {
					if (s.getLine(1).equalsIgnoreCase("" + ChatColor.DARK_GREEN + "[join]")) {
						if (isValidArena(s.getLine(2))) {
							joinLobby(event.getPlayer(), s.getLine(2));
						} else {
							event.getPlayer().sendMessage(arena_invalid);
						}
					}
				}
			}

			if (event.hasItem()) {
				if (event.getItem().getTypeId() == 369) {
					if (event.getItem().hasItemMeta()) {
						ItemMeta im = event.getItem().getItemMeta();
						String itemname = im.getDisplayName();
						String arenaname = itemname.split("" + ChatColor.YELLOW + "")[1];
						if (getConfig().isSet(arenaname)) {
							if (event.getPlayer().hasPermission("dragonescape.setup")) {
								try {
									Block b = event.getClickedBlock();
									Location l = b.getLocation();
									if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
										String count = "low";
										getConfig().set(arenaname + ".boundary" + count + ".world", l.getWorld().getName());
										getConfig().set(arenaname + ".boundary" + count + ".loc.x", l.getBlockX());
										getConfig().set(arenaname + ".boundary" + count + ".loc.y", l.getBlockY() - 1);
										getConfig().set(arenaname + ".boundary" + count + ".loc.z", l.getBlockZ());
										this.saveConfig();
										event.setCancelled(true);
										event.getPlayer().sendMessage("" + ChatColor.YELLOW + "Successfully saved " + count + " boundary!");
									} else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
										String count = "high";
										getConfig().set(arenaname + ".boundary" + count + ".world", l.getWorld().getName());
										getConfig().set(arenaname + ".boundary" + count + ".loc.x", l.getBlockX());
										getConfig().set(arenaname + ".boundary" + count + ".loc.y", l.getBlockY());
										getConfig().set(arenaname + ".boundary" + count + ".loc.z", l.getBlockZ());
										this.saveConfig();
										event.setCancelled(true);
										event.getPlayer().sendMessage("" + ChatColor.YELLOW + "Successfully saved " + count + " boundary!");
									}
								} catch (NullPointerException e) {
									event.getPlayer().sendMessage("" + ChatColor.RED + "You must hit a block.");
								}
							} else {
								event.getPlayer().sendMessage(noperm);
							}
						} else {
							event.getPlayer().sendMessage("" + ChatColor.RED + "Could not find this arena.");
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		Player p = event.getPlayer();
		if (event.getLine(0).toLowerCase().equalsIgnoreCase("dragonescape")) {
			if (event.getPlayer().hasPermission("dragonescape.sign") || event.getPlayer().isOp()) {
				event.setLine(0, "" + ChatColor.GOLD + "DragonEscape");
				if (!event.getLine(2).equalsIgnoreCase("")) {
					String arena = event.getLine(2);
					if (isValidArena(arena)) {
						getConfig().set(arena + ".sign.world", p.getWorld().getName());
						getConfig().set(arena + ".sign.loc.x", event.getBlock().getLocation().getBlockX());
						getConfig().set(arena + ".sign.loc.y", event.getBlock().getLocation().getBlockY());
						getConfig().set(arena + ".sign.loc.z", event.getBlock().getLocation().getBlockZ());
						this.saveConfig();
						p.sendMessage("" + ChatColor.DARK_GREEN + "Successfully created arena sign.");
					} else {
						p.sendMessage(arena_invalid_component);
						event.getBlock().breakNaturally();
					}
					event.setLine(1, "" + ChatColor.DARK_GREEN + "[Join]");
					event.setLine(2, arena);
					event.setLine(3, "0/" + Integer.toString(getArenaMaxPlayers(arena)));
				}
			} else {
				event.getPlayer().sendMessage(noperm);
			}
		}
	}

	@EventHandler
	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
		if (arenap.containsKey(event.getPlayer()) && !event.getPlayer().isOp()) {
			if (!event.getMessage().startsWith("/de") && !event.getMessage().startsWith("/dragonescape")) {
				event.getPlayer().sendMessage("" + ChatColor.RED + "Please use " + ChatColor.GOLD + "/de leave " + ChatColor.RED + "to leave this minigame.");
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler
	public void EntityChangeBlockEvent(org.bukkit.event.entity.EntityChangeBlockEvent event) {
		if (event.getEntityType() == EntityType.FALLING_BLOCK) {
			event.setCancelled(true);
		}
	}

	public Sign getSignFromArena(String arena) {
		Location b_ = new Location(getServer().getWorld(getConfig().getString(arena + ".sign.world")), getConfig().getInt(arena + ".sign.loc.x"), getConfig().getInt(arena + ".sign.loc.y"), getConfig().getInt(arena + ".sign.loc.z"));
		BlockState bs = b_.getBlock().getState();
		Sign s_ = null;
		if (bs instanceof Sign) {
			s_ = (Sign) bs;
		} else {
		}
		return s_;
	}

	public Location getLobby(String arena) {
		Location ret = null;
		if (isValidArena(arena)) {
			ret = new Location(Bukkit.getWorld(getConfig().getString(arena + ".lobby.world")), getConfig().getInt(arena + ".lobby.loc.x"), getConfig().getInt(arena + ".lobby.loc.y"), getConfig().getInt(arena + ".lobby.loc.z"));
		}
		return ret;
	}

	public Location getMainLobby() {
		Location ret;
		if (getConfig().isSet("mainlobby")) {
			ret = new Location(Bukkit.getWorld(getConfig().getString("mainlobby.world")), getConfig().getInt("mainlobby.loc.x"), getConfig().getInt("mainlobby.loc.y"), getConfig().getInt("mainlobby.loc.z"));
		} else {
			ret = null;
			getLogger().warning("A Mainlobby could not be found. This will lead to errors, please fix this with /de setmainlobby.");
		}
		return ret;
	}

	public Location getSpawn(String arena) {
		Location ret = null;
		if (isValidArena(arena)) {
			ret = new Location(Bukkit.getWorld(getConfig().getString(arena + ".spawn.world")), getConfig().getInt(arena + ".spawn.loc.x"), getConfig().getInt(arena + ".spawn.loc.y"), getConfig().getInt(arena + ".spawn.loc.z"), getConfig().getInt(arena + ".spawn.loc.yaw"), getConfig().getInt(arena + ".spawn.loc.pitch"));
		}
		return ret;
	}
	
	
	public Location getDragonSpawn(String arena) {
		Location ret = null;
		if (isValidArena(arena)) {
			if(!getConfig().isSet(arena + ".dragonspawn")){
				return null;
			}
			ret = new Location(Bukkit.getWorld(getConfig().getString(arena + ".dragonspawn.world")), getConfig().getInt(arena + ".dragonspawn.loc.x"), getConfig().getInt(arena + ".dragonspawn.loc.y"), getConfig().getInt(arena + ".dragonspawn.loc.z"), getConfig().getInt(arena + ".dragonspawn.loc.yaw"), getConfig().getInt(arena + ".dragonspawn.loc.pitch"));
		}
		return ret;
	}
	

	public Location getFinish(String arena) {
		Location ret = null;
		if (isValidArena(arena)) {
			ret = new Location(Bukkit.getWorld(getConfig().getString(arena + ".finish.world")), getConfig().getInt(arena + ".finish.loc.x"), getConfig().getInt(arena + ".spawn.loc.y"), getConfig().getInt(arena + ".finish.loc.z"));
		}
		return ret;
	}

	public Location getSpawnForPlayer(String arena) {
		Location ret = null;
		if (isValidArena(arena)) {
			ret = new Location(Bukkit.getWorld(getConfig().getString(arena + ".spawn.world")), getConfig().getInt(arena + ".spawn.loc.x"), getConfig().getInt(arena + ".spawn.loc.y") + 2, getConfig().getInt(arena + ".spawn.loc.z"), getConfig().getInt(arena + ".spawn.loc.yaw"), getConfig().getInt(arena + ".spawn.loc.pitch"));
		}
		return ret;
	}

	public Location getLowBoundary(String arena) {
		Location ret = null;
		if (isValidArena(arena)) {
			ret = new Location(Bukkit.getWorld(getConfig().getString(arena + ".boundarylow.world")), getConfig().getInt(arena + ".boundarylow.loc.x"), getConfig().getInt(arena + ".boundarylow.loc.y") + 2, getConfig().getInt(arena + ".boundarylow.loc.z"));
		}
		return ret;
	}

	public Location getHighBoundary(String arena) {
		Location ret = null;
		if (isValidArena(arena)) {
			ret = new Location(Bukkit.getWorld(getConfig().getString(arena + ".boundaryhigh.world")), getConfig().getInt(arena + ".boundaryhigh.loc.x"), getConfig().getInt(arena + ".boundaryhigh.loc.y") + 2, getConfig().getInt(arena + ".boundaryhigh.loc.z"));
		}
		return ret;
	}

	public boolean isValidArena(String arena) {
		if (getConfig().isSet(arena + ".spawn") && getConfig().isSet(arena + ".lobby") && getConfig().isSet(arena + ".boundarylow") && getConfig().isSet(arena + ".boundaryhigh")) {
			return true;
		}
		return false;
	}

	public HashMap<Player, Boolean> winner = new HashMap<Player, Boolean>();

	public void leaveArena(final Player p, boolean flag, boolean hmmthisbug) {
		try {
			Bukkit.getScheduler().runTaskLater(this, new Runnable() {
				public void run() {
					if (p.isOnline()) {
						try {
							p.teleport(getMainLobby());
						} catch (Exception e) {
							getServer().getLogger().severe("Please send this error to the developers on bukkitdev!");
							e.printStackTrace();
							Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mvtp e:" + getMainLobby().getWorld() + ":" + getMainLobby().getX() + "," + getMainLobby().getY() + "," + getMainLobby().getZ() + ":0:0");
						}
					}
				}
			}, 8);

			if (lost.containsKey(p)) {
				lost.remove(p);
			}

			final String arena = arenap.get(p);
			
			Bukkit.getScheduler().runTaskLater(this, new Runnable() {
				public void run() {
					if (p.isOnline()) {
						p.setAllowFlight(false);
						p.setFlying(false);
						removeScoreboard(arena, p);
					}
				}
			}, 16);

			if (flag) {
				if (arenap.containsKey(p)) {
					arenap.remove(p);
				}
			}
			if (arenap_.containsKey(p.getName())) {
				arenap_.remove(p.getName());
			}

			if (p.isOnline()) {
				p.getInventory().setContents(pinv.get(p));
				p.updateInventory();
			}

			if (winner.containsKey(p)) {
				if (economy) {
					EconomyResponse r = econ.depositPlayer(p.getName(), getConfig().getDouble("config.money_reward_per_game"));
					if (!r.transactionSuccess()) {
						getServer().getPlayer(p.getName()).sendMessage(String.format("An error occured: %s", r.errorMessage));
					}
				} else {
					p.getInventory().addItem(new ItemStack(Material.getMaterial(itemid), itemamount));
					p.updateInventory();
				}

				// command reward
				if (command_reward) {
					Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd.replaceAll("[user]", p.getName()));
				}
			}

			int count = 0;
			for (Player p_ : arenap.keySet()) {
				if (arenap.get(p_).equalsIgnoreCase(arena)) {
					count++;
				}
			}

			if (hmmthisbug && count > 0) {
				getLogger().info("Sorry, I could not fix the game. Stopping now.");
				stop(h.get(arena), arena);
			}

			if (count < 2) {
				if (flag) {
					stop(h.get(arena), arena);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void joinLobby(final Player p, final String arena) {
		// check first if max players are reached.
		int count_ = 0;
		for (Player p_ : arenap.keySet()) {
			if (arenap.get(p_).equalsIgnoreCase(arena)) {
				count_++;
			}
		}
		if (count_ > getArenaMaxPlayers(arena) - 1) {
			p.sendMessage(arena_full);
			return;
		}

		// continue
		arenap.put(p, arena);
		pinv.put(p, p.getInventory().getContents());
		p.setGameMode(GameMode.SURVIVAL);
		p.getInventory().clear();
		p.updateInventory();
		Bukkit.getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				p.teleport(getLobby(arena));
				p.setFoodLevel(20);
			}
		}, 4);

		int count = 0;
		for (Player p_ : arenap.keySet()) {
			if (arenap.get(p_).equalsIgnoreCase(arena)) {
				count++;
			}
		}
		if (count > getArenaMinPlayers(arena) - 1) {
			for (Player p_ : arenap.keySet()) {
				final Player p__ = p_;
				if (arenap.get(p_).equalsIgnoreCase(arena)) {
					Bukkit.getScheduler().runTaskLater(this, new Runnable() {
						public void run() {
							p__.teleport(getSpawnForPlayer(arena));
						}
					}, 7);
				}
			}
			Bukkit.getScheduler().runTaskLater(this, new Runnable() {
				public void run() {
					if (!ingame.containsKey(arena)) {
						ingame.put(arena, false);
					}
					if (!ingame.get(arena)) {
						start(arena);
					}
				}
			}, 10);
		}

		if (!ingame.containsKey(arena)) {
			ingame.put(arena, false);
		}
		if (ingame.get(arena)) {
			Bukkit.getScheduler().runTaskLater(this, new Runnable() {
				public void run() {
					p.teleport(getSpawnForPlayer(arena));
				}
			}, 7);
		}

		try {
			Sign s = this.getSignFromArena(arena);
			if (s != null) {
				s.setLine(3, Integer.toString(count) + "/" + Integer.toString(getArenaMaxPlayers(arena)));
				s.update();
			}
		} catch (Exception e) {
			getLogger().warning("You forgot to set a sign for arena " + arena + "! This may lead to errors.");
		}

	}

	final Main m = this;

	static Random r = new Random();

	final public HashMap<String, BukkitTask> h = new HashMap<String, BukkitTask>();
	final public HashMap<String, Integer> countdown_count = new HashMap<String, Integer>();
	final public HashMap<String, Integer> countdown_id = new HashMap<String, Integer>();
	final public HashMap<String, Double> dragon_move_increment = new HashMap<String, Double>();
	
	public BukkitTask start(final String arena) {
		ingame.put(arena, true);

		// start countdown timer
		if (start_announcement) {
			Bukkit.getServer().broadcastMessage(starting + " " + Integer.toString(start_countdown));
		}

		Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				// clear hostile mobs on start:
				for (Player p : arenap.keySet()) {
					p.playSound(p.getLocation(), Sound.CAT_MEOW, 1, 0);
					if (arenap.get(p).equalsIgnoreCase(arena)) {
						for (Entity t : p.getNearbyEntities(64, 64, 64)) {
							if (t.getType() == EntityType.ZOMBIE || t.getType() == EntityType.SKELETON || t.getType() == EntityType.CREEPER || t.getType() == EntityType.CAVE_SPIDER || t.getType() == EntityType.SPIDER || t.getType() == EntityType.WITCH || t.getType() == EntityType.GIANT) {
								t.remove();
							}
						}
						break;
					}
				}
			}
		}, 20L);

		int t = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(m, new Runnable() {
			public void run() {
				if (!countdown_count.containsKey(arena)) {
					countdown_count.put(arena, start_countdown);
				}
				int count = countdown_count.get(arena);
				for (Player p : arenap.keySet()) {
					if (arenap.get(p).equalsIgnoreCase(arena)) {
						p.sendMessage(starting_in + count + starting_in2);
					}
				}
				count--;
				countdown_count.put(arena, count);
				if (count < 0) {
					countdown_count.put(arena, start_countdown);

					if (start_announcement) {
						Bukkit.getServer().broadcastMessage(started);
					}

					// update sign
					Sign s = getSignFromArena(arena);
					if (s != null) {
						s.setLine(1, "" + ChatColor.DARK_RED + "[Ingame]");
						s.update();
					}

					Bukkit.getServer().getScheduler().cancelTask(countdown_id.get(arena));
				}
			}
		}, 0, 20).getTaskId();
		countdown_id.put(arena, t);

		final String dir = m.getDirection(getSpawn(arena).getYaw());
		
		// spawn enderdragon
		if (dir.equalsIgnoreCase("south")) {
			Bukkit.getScheduler().runTask(this, new Runnable() {
				public void run() {
					try{
						if(getDragonSpawn(arena) != null){
							dragons.put(arena, spawnEnderdragon(arena, getDragonSpawn(arena)));
						}else{
							dragons.put(arena, spawnEnderdragon(arena, getSpawn(arena).add(0.0D, 0.0D, -3.0D)));
						}
					}catch(Exception e){
						stop(h.get(arena), arena);
						return;
					}
				}
			});
		} else if (dir.equalsIgnoreCase("north")) {
			Bukkit.getScheduler().runTask(this, new Runnable() {
				public void run() {
					try{
						if(getDragonSpawn(arena) != null){
							dragons.put(arena, spawnEnderdragon(arena, getDragonSpawn(arena)));
						}else{
							dragons.put(arena, spawnEnderdragon(arena, getSpawn(arena).add(0.0D, 0.0D, +3.0D)));
						}
					}catch(Exception e){
						stop(h.get(arena), arena);
						return;
					}
				}
			});
		} else if (dir.equalsIgnoreCase("east")) {
			Bukkit.getScheduler().runTask(this, new Runnable() {
				public void run() {
					try{
						if(getDragonSpawn(arena) != null){
							dragons.put(arena, spawnEnderdragon(arena, getDragonSpawn(arena)));
						}else{
							dragons.put(arena, spawnEnderdragon(arena, getSpawn(arena).add(-3.0D, 0.0D, 0.0D)));	
						}
						dragons.put(arena, spawnEnderdragon(arena, getSpawn(arena).add(-3.0D, 0.0D, 0.0D)));
					}catch(Exception e){
						stop(h.get(arena), arena);
						return;
					}
				}
			});
		} else if (dir.equalsIgnoreCase("west")) {
			Bukkit.getScheduler().runTask(this, new Runnable() {
				public void run() {
					try{
						if(getDragonSpawn(arena) != null){
							dragons.put(arena, spawnEnderdragon(arena, getDragonSpawn(arena)));
						}else{
							dragons.put(arena, spawnEnderdragon(arena, getSpawn(arena).add(3.0D, 0.0D, 0.0D)));
						}
					}catch(Exception e){
						stop(h.get(arena), arena);
						return;
					}
				}
			});
		}

		final int d = 1;
		
		BukkitTask id__ = null;
		id__ = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(m, new Runnable() {
			@Override
			public void run() {
				try {
					/*if (dir.equalsIgnoreCase("south")) {
						if (dragons.get(arena).locZ > getFinish(arena).getBlockZ()) {
							stop(h.get(arena), arena);
							return;
						}
					} else if (dir.equalsIgnoreCase("north")) {
						if (dragons.get(arena).locZ < getFinish(arena).getBlockZ()) {
							stop(h.get(arena), arena);
							return;
						}
					} else if (dir.equalsIgnoreCase("east")) {
						if (dragons.get(arena).locX > getFinish(arena).getBlockX()) {
							stop(h.get(arena), arena);
							return;
						}
					} else if (dir.equalsIgnoreCase("west")) {
						if (dragons.get(arena).locX < getFinish(arena).getBlockX()) {
							stop(h.get(arena), arena);
							return;
						}
					}*/

					for (final Player p : arenap.keySet()) {
						if (p.isOnline()) {
							if (arenap.get(p).equalsIgnoreCase(arena)) {
								arenap_.put(p.getName(), arena);
							}
						}
					}

					final Location l = getSpawn(arena);
					if (dragon_move_increment.containsKey(arena)) {
						dragon_move_increment.put(arena, dragon_move_increment.get(arena) + 0.35D);
					} else {
						dragon_move_increment.put(arena, 0.25D);
					}

					Location l1 = getHighBoundary(arena);
					Location l2 = getLowBoundary(arena);
					int length1 = l1.getBlockX() - l2.getBlockX();
					int length2 = l1.getBlockY() - l2.getBlockY();
					int length3 = l1.getBlockZ() - l2.getBlockZ();
					boolean f = false;
					boolean f_ = false;
					if (l2.getBlockX() > l1.getBlockX()) {
						length1 = l2.getBlockX() - l1.getBlockX();
						f = true;
					}

					if (l2.getBlockZ() > l1.getBlockZ()) {
						length3 = l2.getBlockZ() - l1.getBlockZ();
						f_ = true;
					}

					if(!dragons.containsKey(arena)){
						return;
					}
					if(dragons.get(arena) == null){
						return;
					}
					
					String dir_ = getDirection(dragons.get(arena).getBukkitEntity().getLocation().getYaw());

					
					if (dir_.equalsIgnoreCase("south")) {
						//dragons.get(arena).setPosition(l.getX(), l.getY(), l.getZ() + dragon_move_increment.get(arena));

						Vector v = dragons.get(arena).getNextPosition();
						if(v != null && dragons.get(arena) != null){
							dragons.get(arena).setPosition(v.getX(), v.getY(), v.getZ());
						}
						
						/*for (int i = 0; i < length1; i++) {
							for (int j = 0; j < length2; j++) {
								final Block b;
								if (f) {
									b = l.getWorld().getBlockAt(new Location(l.getWorld(), l2.getBlockX() - i, l2.getBlockY() + j - 1, dragons.get(arena).locZ + 3));
								} else {
									b = l.getWorld().getBlockAt(new Location(l.getWorld(), l1.getBlockX() - i, l2.getBlockY() + j - 1, dragons.get(arena).locZ + 3));
								}

								Bukkit.getScheduler().runTask(m, new Runnable() {
									public void run() {
										if (b.getType() != Material.AIR) {
											l.getWorld().spawnFallingBlock(b.getLocation(), b.getType(), b.getData()).setMetadata("vortex", new FixedMetadataValue(m, "protected"));
											b.setType(Material.AIR);
										}
									}
								});
							}
						}*/

						if(dragons.get(arena) == null){
							return;
						}
						
						for (int i = 0; i < 10; i++) {
							for (int j = 0; j < length2; j++) {
								final Block b;
								b = l.getWorld().getBlockAt(new Location(l.getWorld(), dragons.get(arena).locX + 5 - i, l2.getBlockY() + j - 1, dragons.get(arena).locZ + 3));
								Bukkit.getScheduler().runTask(m, new Runnable() {
									public void run() {
										if (b.getType() != Material.AIR) {
											playBlockBreakParticles(b.getLocation(), b.getType());
											l.getWorld().spawnFallingBlock(b.getLocation(), b.getType(), b.getData()).setMetadata("vortex", new FixedMetadataValue(m, "protected"));
											b.setType(Material.AIR);
										}
									}
								});
							}
						}
					} else if (dir_.equalsIgnoreCase("north")) {
						//dragons.get(arena).setPosition(l.getX(), l.getY(), l.getZ() - dragon_move_increment.get(arena));

						Vector v = dragons.get(arena).getNextPosition();
						if(v != null && dragons.get(arena) != null){
							dragons.get(arena).setPosition(v.getX(), v.getY(), v.getZ());
						}
						
						/*for (int i = 0; i < length1; i++) {
							for (int j = 0; j < length2; j++) {
								final Block b;
								if (f) {
									b = l.getWorld().getBlockAt(new Location(l.getWorld(), l2.getBlockX() - i, l2.getBlockY() + j - 1, dragons.get(arena).locZ - 3));
								} else {
									b = l.getWorld().getBlockAt(new Location(l.getWorld(), l1.getBlockX() - i, l2.getBlockY() + j - 1, dragons.get(arena).locZ - 3));
								}

								Bukkit.getScheduler().runTask(m, new Runnable() {
									public void run() {
										if (b.getType() != Material.AIR) {
											l.getWorld().spawnFallingBlock(b.getLocation(), b.getType(), b.getData()).setMetadata("vortex", new FixedMetadataValue(m, "protected"));
											b.setType(Material.AIR);
										}
									}
								});
							}
						}*/
						
						if(dragons.get(arena) == null){
							return;
						}
						
						for (int i = 0; i < 10; i++) {
							for (int j = 0; j < length2; j++) {
								final Block b;
								b = l.getWorld().getBlockAt(new Location(l.getWorld(), dragons.get(arena).locX + 5 - i, l2.getBlockY() + j - 1, dragons.get(arena).locZ - 3));

								Bukkit.getScheduler().runTask(m, new Runnable() {
									public void run() {
										if (b.getType() != Material.AIR) {
											playBlockBreakParticles(b.getLocation(), b.getType());
											l.getWorld().spawnFallingBlock(b.getLocation(), b.getType(), b.getData()).setMetadata("vortex", new FixedMetadataValue(m, "protected"));
											b.setType(Material.AIR);
										}
									}
								});
							}
						}
					} else if (dir_.equalsIgnoreCase("east")) {
						//dragons.get(arena).setPosition(l.getX() + dragon_move_increment.get(arena), l.getY(), l.getZ());

						Vector v = dragons.get(arena).getNextPosition();
						if(v != null && dragons.get(arena) != null){
							dragons.get(arena).setPosition(v.getX(), v.getY(), v.getZ());
						}
						
						/*for (int i = 0; i < length3; i++) {
							for (int j = 0; j < length2; j++) {
								final Block b;
								if (f_) {
									b = l.getWorld().getBlockAt(new Location(l.getWorld(), dragons.get(arena).locX + 3, l2.getBlockY() + j - 1, l2.getBlockZ() - i));
								} else {
									b = l.getWorld().getBlockAt(new Location(l.getWorld(), dragons.get(arena).locX + 3, l2.getBlockY() + j - 1, l1.getBlockZ() - i));
								}

								Bukkit.getScheduler().runTask(m, new Runnable() {
									public void run() {
										if (b.getType() != Material.AIR) {
											l.getWorld().spawnFallingBlock(b.getLocation(), b.getType(), b.getData()).setMetadata("vortex", new FixedMetadataValue(m, "protected"));
											b.setType(Material.AIR);
										}
									}
								});
							}
						}*/
						
						if(dragons.get(arena) == null){
							return;
						}
						
						for (int i = 0; i < 10; i++) {
							for (int j = 0; j < length2; j++) {
								final Block b;
								b = l.getWorld().getBlockAt(new Location(l.getWorld(), dragons.get(arena).locX + 3, l2.getBlockY() + j - 1, dragons.get(arena).locZ + 5 - i));

								Bukkit.getScheduler().runTask(m, new Runnable() {
									public void run() {
										if (b.getType() != Material.AIR) {
											playBlockBreakParticles(b.getLocation(), b.getType());
											l.getWorld().spawnFallingBlock(b.getLocation(), b.getType(), b.getData()).setMetadata("vortex", new FixedMetadataValue(m, "protected"));
											b.setType(Material.AIR);
										}
									}
								});
							}
						}
					} else if (dir_.equalsIgnoreCase("west")) {
						//dragons.get(arena).setPosition(l.getX() - dragon_move_increment.get(arena), l.getY(), l.getZ());

						Vector v = dragons.get(arena).getNextPosition();
						if(v != null && dragons.get(arena) != null){
							dragons.get(arena).setPosition(v.getX(), v.getY(), v.getZ());
						}
						
						/*for (int i = 0; i < length3; i++) {
							for (int j = 0; j < length2; j++) {
								final Block b;
								if (f_) {
									b = l.getWorld().getBlockAt(new Location(l.getWorld(), dragons.get(arena).locX - 3, l2.getBlockY() + j - 1, l2.getBlockZ() - i));
								} else {
									b = l.getWorld().getBlockAt(new Location(l.getWorld(), dragons.get(arena).locX - 3, l2.getBlockY() + j - 1, l1.getBlockZ() - i));
								}

								Bukkit.getScheduler().runTask(m, new Runnable() {
									public void run() {
										if (b.getType() != Material.AIR) {
											l.getWorld().spawnFallingBlock(b.getLocation(), b.getType(), b.getData()).setMetadata("vortex", new FixedMetadataValue(m, "protected"));
											b.setType(Material.AIR);
										}
									}
								});
							}
						}*/
						
						if(dragons.get(arena) == null){
							return;
						}
						
						for (int i = 0; i < 10; i++) {
							for (int j = 0; j < length2; j++) {
								final Block b;
								b = l.getWorld().getBlockAt(new Location(l.getWorld(), dragons.get(arena).locX - 3, l2.getBlockY() + j - 1, dragons.get(arena).locZ + 5 - i));

								Bukkit.getScheduler().runTask(m, new Runnable() {
									public void run() {
										if (b.getType() != Material.AIR) {
											playBlockBreakParticles(b.getLocation(), b.getType());
											l.getWorld().spawnFallingBlock(b.getLocation(), b.getType(), b.getData()).setMetadata("vortex", new FixedMetadataValue(m, "protected"));
											b.setType(Material.AIR);
										}
									}
								});
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				//TODO reminder
				updateScoreboard();

			}
		}, 3 + 20 * start_countdown, 3);

		h.put(arena, id__);
		tasks.put(arena, id__);
		return id__;
	}

	public void reset(final String arena) {
		Runnable r = new Runnable() {
			public void run() {
				loadArenaFromFileSYNC(arena);
			}
		};
		new Thread(r).start();
	}

	public void stop(BukkitTask t, final String arena) {
		ingame.put(arena, false);
		try {
			t.cancel();
		} catch (Exception e) {

		}

		try {
			removeEnderdragon(dragons.get(arena));
			dragons.put(arena, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		dragon_move_increment.put(arena, 0.0D);

		Bukkit.getScheduler().runTaskLater(this, new Runnable() {

			public void run() {
				countdown_count.put(arena, start_countdown);
				try {
					Bukkit.getServer().getScheduler().cancelTask(countdown_id.get(arena));
				} catch (Exception e) {
				}

				ArrayList<Player> torem = new ArrayList<Player>();
				determineWinners(arena);
				for (Player p : arenap.keySet()) {
					if (arenap.get(p).equalsIgnoreCase(arena)) {
						leaveArena(p, false, false);
						removeScoreboard(arena, p);
						torem.add(p);
					}
				}

				for (Player p : torem) {
					arenap.remove(p);
				}
				torem.clear();

				winner.clear();

				Sign s = getSignFromArena(arena);
				if (s != null) {
					s.setLine(1, "" + ChatColor.GOLD + "[Restarting]");
					s.setLine(3, "0/" + Integer.toString(getArenaMaxPlayers(arena)));
					s.update();
				}

				h.remove(arena);

				reset(arena);

				// clean out offline players
				clean();
			}

		}, 20); // 1 second
	}

	public void clean() {
		for (Player p : arenap.keySet()) {
			if (!p.isOnline()) {
				leaveArena(p, false, false);
			}
		}
	}

	public void determineWinners(String arena) {
		for (Player p : arenap.keySet()) {
			if (arenap.get(p).equalsIgnoreCase(arena)) {
				if (!lost.containsKey(p)) {
					// this player is a winner
					p.sendMessage(you_won);

					if (winner_announcement) {
						getServer().broadcastMessage(winner_an.replaceAll("<player>", p.getName()).replaceAll("<arena>", arena));
					}

					winner.put(p, true);
				} else {
					lost.remove(p);
				}
			}
		}
	}

	public int getArenaMaxPlayers(String arena) {
		if (!getConfig().isSet(arena + ".max_players")) {
			setArenaMaxPlayers(arena, default_max_players);
		}
		return getConfig().getInt(arena + ".max_players");
	}

	public void setArenaMaxPlayers(String arena, int players) {
		getConfig().set(arena + ".max_players", players);
		this.saveConfig();
	}

	public int getArenaMinPlayers(String arena) {
		if (!getConfig().isSet(arena + ".min_players")) {
			setArenaMinPlayers(arena, default_min_players);
		}
		return getConfig().getInt(arena + ".min_players");
	}

	public void setArenaMinPlayers(String arena, int players) {
		getConfig().set(arena + ".min_players", players);
		this.saveConfig();
	}

	public boolean isNumeric(String s) {
		return s.matches("[-+]?\\d*\\.?\\d+");
	}

	public void saveArenaToFile(String player, String arena) {
		File f = new File(this.getDataFolder() + "/" + arena);
		Cuboid c = new Cuboid(getLowBoundary(arena), getHighBoundary(arena));
		Location start = c.getLowLoc();
		Location end = c.getHighLoc();

		int width = end.getBlockX() - start.getBlockX();
		int length = end.getBlockZ() - start.getBlockZ();
		int height = end.getBlockY() - start.getBlockY();

		getLogger().info("BOUNDS: " + Integer.toString(width) + " " + Integer.toString(height) + " " + Integer.toString(length));
		getLogger().info("BLOCKS TO SAVE: " + Integer.toString(width * height * length));

		FileOutputStream fos;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream(f);
			oos = new BukkitObjectOutputStream(fos);
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (int i = 0; i <= width; i++) {
			for (int j = 0; j <= height; j++) {
				for (int k = 0; k <= length; k++) {
					Block change = c.getWorld().getBlockAt(start.getBlockX() + i, start.getBlockY() + j, start.getBlockZ() + k);

					// if(change.getType() != Material.AIR){
					ArenaBlock bl = new ArenaBlock(change);

					try {
						oos.writeObject(bl);
					} catch (IOException e) {
						e.printStackTrace();
					}
					// }

				}
			}
		}

		try {
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Bukkit.getPlayerExact(player).sendMessage("" + ChatColor.GREEN + "Successfully saved arena to file.");
	}

	public void saveArenaToFile(String arena) {
		File f = new File(this.getDataFolder() + "/" + arena);
		Cuboid c = new Cuboid(getLowBoundary(arena), getHighBoundary(arena));
		Location start = c.getLowLoc();
		Location end = c.getHighLoc();

		int width = end.getBlockX() - start.getBlockX();
		int length = end.getBlockZ() - start.getBlockZ();
		int height = end.getBlockY() - start.getBlockY();

		getLogger().info("BOUNDS: " + Integer.toString(width) + " " + Integer.toString(height) + " " + Integer.toString(length));
		getLogger().info("BLOCKS TO SAVE: " + Integer.toString(width * height * length));

		FileOutputStream fos;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream(f);
			oos = new BukkitObjectOutputStream(fos);
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (int i = 0; i <= width; i++) {
			for (int j = 0; j <= height; j++) {
				for (int k = 0; k <= length; k++) {
					Block change = c.getWorld().getBlockAt(start.getBlockX() + i, start.getBlockY() + j, start.getBlockZ() + k);

					// if(change.getType() != Material.AIR){
					ArenaBlock bl = new ArenaBlock(change);

					try {
						oos.writeObject(bl);
					} catch (IOException e) {
						e.printStackTrace();
					}
					// }

				}
			}
		}

		try {
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		getLogger().info("saved");
	}

	public void loadArenaFromFileASYNC(String arena) {
		File f = new File(this.getDataFolder() + "/" + arena);
		FileInputStream fis = null;
		BukkitObjectInputStream ois = null;
		try {
			fis = new FileInputStream(f);
			ois = new BukkitObjectInputStream(fis);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			while (true) {
				Object b = null;
				try {
					b = ois.readObject();
				} catch (EOFException e) {
					getLogger().info("Finished restoring map for " + arena + ".");
				}

				if (b != null) {
					ArenaBlock ablock = (ArenaBlock) b;
					World w = ablock.getBlock().getWorld();

					if (!w.getBlockAt(ablock.getBlock().getLocation()).getType().toString().equalsIgnoreCase(ablock.getMaterial().toString())) {
						ablock.getBlock().getWorld().getBlockAt(ablock.getBlock().getLocation()).setType(ablock.getMaterial());
					}
				} else {
					break;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			ois.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void loadArenaFromFileSYNC(final String arena) {
		int failcount = 0;
		final ArrayList<ArenaBlock> failedblocks = new ArrayList<ArenaBlock>();

		File f = new File(this.getDataFolder() + "/" + arena);
		FileInputStream fis = null;
		BukkitObjectInputStream ois = null;
		try {
			fis = new FileInputStream(f);
			ois = new BukkitObjectInputStream(fis);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			while (true) {
				Object b = null;
				try {
					b = ois.readObject();
				} catch (EOFException e) {
					getLogger().info("Finished restoring map for " + arena + ".");
				}

				if (b != null) {
					ArenaBlock ablock = (ArenaBlock) b;
					try {
						if (!ablock.getBlock().getWorld().getBlockAt(ablock.getBlock().getLocation()).getType().toString().equalsIgnoreCase(ablock.getMaterial().toString())) {
							ablock.getBlock().getWorld().getBlockAt(ablock.getBlock().getLocation()).setType(ablock.getMaterial());
						}
					} catch (IllegalStateException e) {
						failcount += 1;
						failedblocks.add(ablock);
					}
				} else {
					break;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			ois.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		getLogger().warning("Failed to update " + Integer.toString(failcount) + " blocks due to spigots async exception.");
		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				// restore spigot blocks!
				getLogger().info("Trying to restore blocks affected by spigot exception..");
				for (ArenaBlock ablock : failedblocks) {
					getServer().getWorld(ablock.world).getBlockAt(new Location(getServer().getWorld(ablock.world), ablock.x, ablock.y, ablock.z)).setType(Material.WOOL);
					getServer().getWorld(ablock.world).getBlockAt(new Location(getServer().getWorld(ablock.world), ablock.x, ablock.y, ablock.z)).getTypeId();
					getServer().getWorld(ablock.world).getBlockAt(new Location(getServer().getWorld(ablock.world), ablock.x, ablock.y, ablock.z)).setType(ablock.getMaterial());
				}
				getLogger().info("Successfully finished!");

				Sign s = getSignFromArena(arena);
				if (s != null) {
					s.setLine(1, "" + ChatColor.DARK_GREEN + "[Join]");
					s.setLine(3, "0/" + Integer.toString(getArenaMaxPlayers(arena)));
					s.update();
				}
			}
		}, 40L);

		return;
	}

	public String getDirection(Float yaw) {
		yaw = yaw / 90;
		yaw = (float) Math.round(yaw);

		if (yaw == -4 || yaw == 0 || yaw == 4) {
			return "SOUTH";
		}
		if (yaw == -1 || yaw == 3) {
			return "EAST";
		}
		if (yaw == -2 || yaw == 2) {
			return "NORTH";
		}
		if (yaw == -3 || yaw == 1) {
			return "WEST";
		}
		return "";
	}

	public ArrayList<Vector> getDragonWayPoints(String arena){
		ArrayList<Vector> ret = new ArrayList<Vector>();
		if(!getConfig().isSet(arena + ".flypoint")){
			return null;
		}else{
			Set<String> f = getConfig().getConfigurationSection(arena + ".flypoint").getKeys(false);
			for(String key : f){

				ret.add(new Vector(getConfig().getDouble(arena + ".flypoint." + key + ".x"), getConfig().getDouble(arena + ".flypoint." + key + ".y"), getConfig().getDouble(arena + ".flypoint." + key + ".z")));
			}
			return ret;
		}
	}

	public int getCurrentFlyPoint(String arena){
		if(!getConfig().isSet(arena + ".flypoint")){
			return 0;
		}else{
			int count = 0;
			Set<String> f = getConfig().getConfigurationSection(arena + ".flypoint").getKeys(false);
			for(String key : f){
				count ++;
			}
			return count;
		}
	}
	
	
	public static final void playBlockBreakParticles(final Location loc, final Material m) {
		playBlockBreakParticles(loc, m, Bukkit.getOnlinePlayers());
	}

	public static final void playBlockBreakParticles(final Location loc, final Material m, final Player... players) {
		@SuppressWarnings("deprecation")
		PacketPlayOutWorldEvent packet = new PacketPlayOutWorldEvent(2001, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), m.getId(), false);
		for (final Player p : players) {
			((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
		}
	}
	

	Scoreboard board;
	Objective objective;
	public HashMap<String, Integer> currentscore = new HashMap<String, Integer>();

	public void updateScoreboard(){

		for(Player pl : arenap.keySet()){
			Player p = pl;
			if(board == null){
				board = Bukkit.getScoreboardManager().getNewScoreboard();
			}
			if(objective == null){
				objective = board.registerNewObjective("test", "dummy");
			}

			objective.setDisplaySlot(DisplaySlot.SIDEBAR);

			objective.setDisplayName("[" + arenap.get(p) + "]");

			for(Player pl_ : arenap.keySet()){
				Player p_ = pl_;
				int score = -(int) p_.getLocation().distance(getFinish(arenap.get(p)));
				if(currentscore.containsKey(pl_.getName())){
					int oldscore = currentscore.get(pl_.getName());
					if(score > oldscore){
						currentscore.put(pl_.getName(), score);
					}else{
						score = oldscore;
					}
				}else{
					currentscore.put(pl_.getName(), score);
				}
				objective.getScore(p_).setScore(score);
			}

			p.setScoreboard(board);
		}
	}
	
	
	public void removeScoreboard(String arena, Player p) {
		try {
			ScoreboardManager manager = Bukkit.getScoreboardManager();
			Scoreboard sc = manager.getNewScoreboard();
			
			sc.clearSlot(DisplaySlot.SIDEBAR);
			p.setScoreboard(sc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
