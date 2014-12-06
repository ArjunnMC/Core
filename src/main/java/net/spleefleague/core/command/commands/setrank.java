/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.spleefleague.core.command.commands;

import com.mongodb.BasicDBObject;
import net.spleefleague.core.CorePlugin;
import net.spleefleague.core.SpleefLeague;
import net.spleefleague.core.command.BasicCommand;
import net.spleefleague.core.player.Rank;
import net.spleefleague.core.player.SLPlayer;
import net.spleefleague.core.utils.DatabaseConnection;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Jonas
 */
public class setrank extends BasicCommand {

    public setrank(CorePlugin plugin, String name, String usage) {
        super(plugin, name, usage, Rank.ADMIN);
    }

    @Override
    protected void run(Player p, SLPlayer slp, Command cmd, String[] args) {
        runConsole(p, cmd, args);
    }
    
    @Override
    protected void runConsole(CommandSender cs, Command cmd, String[] args) {
        if(args.length == 2) {
            Player player = Bukkit.getPlayerExact(args[0]);
            if(player != null) {
                SLPlayer slp = SpleefLeague.getInstance().getPlayerManager().get(player);
                if(slp != null) {
                    Rank rank;
                    try {
                        rank = Rank.valueOf(args[1]);
                    } catch(Exception e) {
                        rank = null;
                    }
                    if(rank != null) {
                        slp.setRank(rank);
                        success(cs, "Rank has been set.");
                    }
                    else {
                        error(cs, "The rank " + args[1] + " does not exist!");
                    }
                }
                else {
                    setRankOffline(cs, args[0], args[1]);
                }
            }
            else {
                setRankOffline(cs, args[0], args[1]);
            }
        }
        else {
            sendUsage(cs);
        }
    }
    
    private void setRankOffline(CommandSender cs, String name, String r) {
        Rank rank = Rank.valueOf(r);
        if(rank == null) {
            error(cs, "The rank " + r + " does not exist!");
        }
        else {    
            DatabaseConnection.updateFields(SpleefLeague.getInstance().getPluginDB().getCollection("Players"), new BasicDBObject("username", name), new BasicDBObject("rank", rank.toString()));
        }
    }
}