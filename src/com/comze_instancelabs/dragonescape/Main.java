package com.comze_instancelabs.dragonescape;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener{

	/***
	 * Player Name -> Arena
	 */
	public static HashMap<String, String> arenap = new HashMap<String, String>(); // player -> arena

	
	public void onEnable(){
		getServer().getPluginManager().registerEvents(this, this);
	}
	
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){

    	if(cmd.getName().equalsIgnoreCase("de") || cmd.getName().equalsIgnoreCase("dragonescape")){
    		if(args.length > 0){
    			String action = args[0];
    			if(action.equalsIgnoreCase("createrace")){
    				
    			}else if(action.equalsIgnoreCase("setmainlobby")){
    				
    			}else if(action.equalsIgnoreCase("setlobby")){
    				
    			}else if(action.equalsIgnoreCase("setbounds")){
    				
    			}else if(action.equalsIgnoreCase("leave")){
    				
    			}else if(action.equalsIgnoreCase("stats")){
    				
    			}else if(action.equalsIgnoreCase("reload")){
    				
    			}else if(action.equalsIgnoreCase("leave")){
    				
    			}else if(action.equalsIgnoreCase("join")){
    				
    			}
    		}else{
    			//TODO show help
    		}
    		return true;
    	}
    	
    	return false;
    }
    
    
    public Sign getSignFromArena(String arena) {
		Location b_ = new Location(getServer().getWorld(getConfig().getString(arena + ".sign.world")), getConfig().getInt(arena + ".sign.loc.x"), getConfig().getInt(arena + ".sign.loc.y"), getConfig().getInt(arena + ".sign.loc.z"));
		BlockState bs = b_.getBlock().getState();
		Sign s_ = null;
		if (bs instanceof Sign) {
			s_ = (Sign) bs;
		}
		return s_;
	}

    /***
     * Returns the location of the waiting lobby
     * @param arena
     * @return
     */
	public Location getLobby(String arena) {
		Location ret = null;
		if (isValidArena(arena)) {
			ret = new Location(Bukkit.getWorld(getConfig().getString(arena + ".lobby.world")), getConfig().getInt(arena + ".lobby.loc.x"), getConfig().getInt(arena + ".lobby.loc.y"), getConfig().getInt(arena + ".lobby.loc.z"));
		}
		return ret;
	}

	/***
	 * Returns the location of the main lobby, if set
	 * @return
	 */
	public Location getMainLobby() {
		Location ret;
		if(getConfig().isSet("mainlobby")){
			ret = new Location(Bukkit.getWorld(getConfig().getString("mainlobby.world")), getConfig().getInt("mainlobby.loc.x"), getConfig().getInt("mainlobby.loc.y"), getConfig().getInt("mainlobby.loc.z"));
		}else{
			ret = null;
			getLogger().warning("A Mainlobby could not be found. This will lead to errors, please fix this with /cm setmainlobby.");
		}
		return ret;
	}

	/***
	 * Returns the location of the spawn
	 * @param arena
	 * @return
	 */
	public Location getSpawn(String arena) {
		Location ret = null;
		if (isValidArena(arena)) {
			ret = new Location(Bukkit.getWorld(getConfig().getString(arena + ".spawn.world")), getConfig().getInt(arena + ".spawn.loc.x"), getConfig().getInt(arena + ".spawn.loc.y"), getConfig().getInt(arena + ".spawn.loc.z"));
		}
		return ret;
	}
	
	public boolean isValidArena(String arena) {
		if (getConfig().isSet(arena + ".spawn") && getConfig().isSet(arena + ".lobby")) {
			return true;
		}
		return false;
	}
	
	
	
	@EventHandler
   	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
       	if(arenap.containsKey(event.getPlayer().getName())){
       		if(!event.getMessage().startsWith("/de") && !event.getMessage().startsWith("/dragonescape")){
       			event.getPlayer().sendMessage("§cPlease use §6/de leave §cto leave this minigame.");
        		event.setCancelled(true);
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
					if (s.getLine(1).equalsIgnoreCase("§2[join]")) {
						// TODO JOIN GAME
					}
				}
			}
		}
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		Player p = event.getPlayer();
		if (event.getLine(0).toLowerCase().equalsIgnoreCase("colormatch")) {
			if (event.getPlayer().hasPermission("cm.sign") || event.getPlayer().hasPermission("colormatch.sign") || event.getPlayer().isOp()) {
				event.setLine(0, "§6§lColorMatch");
				if (!event.getLine(2).equalsIgnoreCase("")) {
					String arena = event.getLine(2);
					if (isValidArena(arena)) {
						getConfig().set(arena + ".sign.world", p.getWorld().getName());
						getConfig().set(arena + ".sign.loc.x", event.getBlock().getLocation().getBlockX());
						getConfig().set(arena + ".sign.loc.y", event.getBlock().getLocation().getBlockY());
						getConfig().set(arena + ".sign.loc.z", event.getBlock().getLocation().getBlockZ());
						this.saveConfig();
						p.sendMessage("§2Successfully created arena sign.");
					} else {
						p.sendMessage("");
						event.getBlock().breakNaturally();
					}
					event.setLine(1, "§2[Join]");
					event.setLine(2, arena);
					//TODO MAX/MIN PLAYERS
					//event.setLine(3, "0/" + Integer.toString(getArenaMaxPlayers(arena)));
				}
			}
		}
	}

	
}
