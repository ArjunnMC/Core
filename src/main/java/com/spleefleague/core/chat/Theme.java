/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.core.chat;

import com.spleefleague.core.SpleefLeague;
import org.bukkit.ChatColor;

/**
 *
 * @author Jonas
 */
public enum Theme {

    SUCCESS(ChatColor.GREEN),
    SEVERE(ChatColor.DARK_RED),
    ERROR(ChatColor.RED),
    INFO(ChatColor.YELLOW),
    INCOGNITO(ChatColor.GRAY),
    SUPER_SECRET(ChatColor.DARK_GRAY),
    WARNING(ChatColor.GOLD),
    BROADCAST(ChatColor.LIGHT_PURPLE);

    private final String prefix;
    private final ChatColor color;

    Theme(String prefix, ChatColor color) {
        this.prefix = prefix;
        this.color = color;
    }

    Theme(ChatColor color) {
        this(SpleefLeague.getInstance().getChatPrefix(), color);
    }

    public String buildTheme(boolean prefix) {
        return (prefix ? this.prefix + " " : "") + color;
    }

    @Override
    public String toString() {
        return buildTheme(true);
    }
}
