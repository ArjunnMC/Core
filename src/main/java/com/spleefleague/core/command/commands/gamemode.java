/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.core.command.commands;

import com.spleefleague.core.plugin.CorePlugin;
import com.spleefleague.core.command.BasicCommand;
import com.spleefleague.core.player.Rank;
import com.spleefleague.core.player.SLPlayer;
import com.spleefleague.core.utils.ServerType;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Manuel
 */
public class gamemode extends BasicCommand {

    public gamemode(CorePlugin plugin, String name, String usage) {
        super(plugin, name, usage, Rank.MODERATOR);
        setAdditionalRanksDependingOnServerType(ServerType.BUILD, Rank.BUILDER);
    }

    @Override
    protected void run(Player p, SLPlayer slp, Command cmd, String[] args) {
        if (slp.getRank() == Rank.MODERATOR) {
            slp.setGameMode((slp.getGameMode() == GameMode.SPECTATOR ? GameMode.SURVIVAL : GameMode.SPECTATOR));
            success(p, "Gamemode toggled!");
            success(p, "If you wish to toggle back, please use this command again.");
            return;
        }
        GameMode mode = null;
        Player player = p;
        if (args.length == 0) {
            sendUsage(p);
            return;
        }
        if (args.length >= 1) {
            try {
                mode = GameMode.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                try {
                    mode = GameMode.getByValue(Integer.parseInt(args[0]));
                } catch (NumberFormatException ex) {
                    error(p, "The gamemode \"" + args[0] + "\" doesn't exist!");
                    return;
                }
            }
        }
        if (args.length >= 2) {
            if ((player = Bukkit.getPlayerExact(args[1])) == null) {
                error(p, "The player \"" + args[1] + "\" is not online!");
                return;
            }
        }
        if (player.getGameMode() != mode) {
            player.setGameMode(mode);
            success(player, "Your gamemode has been updated!");
        }
    }

    @Override
    protected void runConsole(CommandSender cs, Command cmd, String[] args) {
        if (args.length >= 2) {
            GameMode mode;
            try {
                mode = GameMode.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                try {
                    mode = GameMode.getByValue(Integer.parseInt(args[0]));
                } catch (NumberFormatException ex) {
                    error(cs, "The gamemode \"" + args[0] + "\" doesn't exist!");
                    return;
                }
            }
            Player player;
            if ((player = Bukkit.getPlayerExact(args[1])) == null) {
                error(cs, "The player \"" + args[1] + "\" is not online!");
                return;
            }
            if (player.getGameMode() != mode) {
                player.setGameMode(mode);
                success(player, "Your gamemode has been updated!");
            }
        } else {
            sendUsage(cs);
        }
    }
    /*@Override
    protected int runBlock(CommandSender cs, Command cmd, String[] args) {
        if(args.length >= 2) {
            GameMode mode;
            try{
                mode = GameMode.valueOf(args[0].toUpperCase());
            }
            catch(IllegalArgumentException e){
                try{
                    mode = GameMode.getByValue(Integer.parseInt(args[0]));
                }
                catch(NumberFormatException ex){
                    error(cs, "The gamemode \"" + args[0] + "\" doesn't exist!");
                    return 0;
                }
            }
            Player player;
            if((player = Bukkit.getPlayerExact(args[1])) == null){
                error(cs, "The player \"" + args[1] + "\" is not online!");
                return 0;
            }
            player.setGameMode(mode);
            success(player, "Your gamemode has been updated!");
        }
        else {
            sendUsage(cs);
        }
        return 0;
    }*/
}
