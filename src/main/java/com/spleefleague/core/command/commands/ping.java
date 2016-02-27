/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.core.command.commands;


import net.minecraft.server.v1_8_R3.EntityPlayer;
import com.spleefleague.core.plugin.CorePlugin;
import com.spleefleague.core.command.BasicCommand;
import com.spleefleague.core.player.Rank;
import com.spleefleague.core.player.SLPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author JoBa
 */
public class ping extends BasicCommand{

    public ping(CorePlugin plugin, String name, String usage) {
        super(plugin, name, usage, Rank.DEFAULT);
    }

    @Override
    protected void run(Player p, SLPlayer slp, Command cmd, String[] args) {
        Player j = p;
        if (args.length > 0) {
            j = Bukkit.getPlayer(args[0]);
            if (j == null) {
                error(p, "Player not found!");
                return;
            }
        }
        showPing(p, j);
    }    
    
    private void showPing(Player to, Player whose) {
        boolean same = (to == whose);
        int ping = getPlayerPing(whose);
        ChatColor c = ChatColor.DARK_RED;
        if (ping < 10*1000) {
            c = ChatColor.RED;
        }
        if (ping < 1000) {
            c = ChatColor.GOLD;
        }
        if (ping < 200) {
            c = ChatColor.YELLOW;
        }
        if (ping < 100) {
            c = ChatColor.GREEN;
        }
        if (ping < 25) {
            c = ChatColor.DARK_GREEN;
        }
		String pingStr = Integer.toString(ping) + " ms";
        if (same) {
            success(to, ChatColor.GRAY + "Your ping is: " + c + pingStr);
        } else {
            success(to, ChatColor.GRAY + "Showing ping for " + ChatColor.RED + whose.getName() + ChatColor.GRAY + ": " + c + pingStr);
        }
    }
    
    private int getPlayerPing(Player p) {
        EntityPlayer nmsp = (EntityPlayer)(((CraftPlayer)p).getHandle());
        return nmsp.ping;
    }    
}
