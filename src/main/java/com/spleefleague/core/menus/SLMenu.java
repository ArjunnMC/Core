package com.spleefleague.core.menus;

import static com.spleefleague.core.utils.inventorymenu.InventoryMenuAPI.item;
import static com.spleefleague.core.utils.inventorymenu.InventoryMenuAPI.menu;

import org.bukkit.Bukkit;
import org.bukkit.Material;

import com.spleefleague.core.SpleefLeague;
import com.spleefleague.core.chat.ChatChannel;
import com.spleefleague.core.chat.ChatManager;
import com.spleefleague.core.chat.Theme;
import com.spleefleague.core.player.Rank;
import com.spleefleague.core.player.SLPlayer;
import static com.spleefleague.core.utils.inventorymenu.InventoryMenuAPI.dialog;
import static com.spleefleague.core.utils.inventorymenu.InventoryMenuAPI.dialogButton;
import static com.spleefleague.core.utils.inventorymenu.InventoryMenuAPI.dialogMenu;
import com.spleefleague.core.utils.inventorymenu.InventoryMenuComponentFlag;
import com.spleefleague.core.utils.inventorymenu.InventoryMenuFlag;
import com.spleefleague.core.utils.inventorymenu.InventoryMenuItemTemplateBuilder;
import com.spleefleague.core.utils.inventorymenu.InventoryMenuTemplate;
import com.spleefleague.core.utils.inventorymenu.InventoryMenuTemplateBuilder;
import com.spleefleague.core.utils.inventorymenu.dialog.InventoryMenuDialogButtonTemplateBuilder;
import com.spleefleague.core.utils.inventorymenu.dialog.InventoryMenuDialogFlag;
import com.spleefleague.core.utils.inventorymenu.dialog.InventoryMenuDialogHolderTemplate;
import com.spleefleague.core.utils.inventorymenu.dialog.InventoryMenuDialogHolderTemplateBuilder;
import com.spleefleague.core.utils.inventorymenu.dialog.InventoryMenuDialogTemplateBuilder;
import com.spleefleague.gameapi.GamePlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class SLMenu {

    private static InventoryMenuTemplateBuilder slMenuBuilder;
    public static InventoryMenuTemplate slMenu;
    private static Integer currentGamemodeMenu = Integer.MIN_VALUE;

    public static void init() {
        slMenuBuilder = menu()
                .title("SpleefLeague Menu")
                .displayIcon(Material.COMPASS)
                .displayName(ChatColor.GOLD + "" + ChatColor.BOLD + "SpleefLeague Menu");
                //Gamemode submenus added by game plugins
        slMenuBuilder.component(8, createOptionsMenu(slMenuBuilder.build()))
                .component(7, createSpectateMenu())
                .component(6, createStaffMenu(slMenuBuilder.build()));
        Bukkit.getScheduler().runTask(SpleefLeague.getInstance(), () -> {
            slMenu = slMenuBuilder.build();
            InventoryMenuTemplateRepository.addMenu(slMenu);
        }); //Gets called after all plugins were loaded
    }
    
    public static InventoryMenuTemplateBuilder getMenuBuilder() {
        return slMenuBuilder;
    }
    
    private static void addBackButton(InventoryMenuTemplateBuilder from, InventoryMenuTemplate to) {
        from.component(8, item()
                .displayName(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Back")
                .displayItem(new ItemStack(Material.DIAMOND_AXE, 1, (short) 9))
                .onClick(event -> {
                    to.construct(null, SpleefLeague.getInstance().getPlayerManager().get(event.getPlayer())).open();
                }));
    }
    
    private static SLPlayer getSLP(Player p) {
        return SpleefLeague.getInstance().getPlayerManager().get(p);
    }
    
    private static Function<SLPlayer, InventoryMenuDialogHolderTemplate<MenuPlayer>> createSpectatePlayerDialog() {
        return (SLPlayer slp1) -> {
            InventoryMenuDialogHolderTemplateBuilder<MenuPlayer> dialogSpectatePlayer = dialogMenu(MenuPlayer.class)
                    .title("Select a player")
                    .unsetFlags(InventoryMenuFlag.HIDE_EMPTY_SUBMENU)
                    .unsetFlags(InventoryMenuComponentFlag.EXIT_ON_NO_PERMISSION);
            SpleefLeague.getInstance().getPlayerManager()
                    .getAll()
                    .stream()
                    .sorted((p1, p2) -> p1.getName().compareTo(p2.getName()))
                    .forEach(p -> {
                        ItemStack skull = new ItemStack(Material.SKULL_ITEM);
                        skull.setDurability((short) 3);
                        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
                        skullMeta.setOwner(p.getName());
                        skull.setItemMeta(skullMeta);
                        InventoryMenuDialogButtonTemplateBuilder playerButton = dialogButton(MenuPlayer.class)
                                .displayItem(skull)
                                .visibilityController(slp -> (slp != p && GamePlugin.isIngameGlobal(p)))
                                .displayName(SpleefLeague.playerColor + p.getName())
                                .onClick(e -> e.getBuilder().setTarget(p));
                        for (GamePlugin gp : GamePlugin.getGamePlugins()) {
                            if (gp.isIngame(p)) {
                                playerButton.displayName(gp.getPlayerName(p));
                                for(String l : gp.getPlayerDescription(p)) {
                                    playerButton.description(l);
                                }
                                break;
                            }
                        }
                        dialogSpectatePlayer.component(playerButton);
                    });
            return dialogSpectatePlayer.build();
        };
    }
    
    private static InventoryMenuDialogTemplateBuilder createSpectateMenu() {
        return (dialog(MenuPlayer.class)
                .displayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Spectate Player")
                .displayIcon(Material.EYE_OF_ENDER)
                .start(createSpectatePlayerDialog())
                .flags(InventoryMenuDialogFlag.EXIT_ON_COMPLETE_DIALOG)
                .builder(slp -> new MenuPlayer(getSLP(slp)))
                .onDone((p, m) -> {
                    p.spectate(m.target);
                }));
    }

    private static InventoryMenuTemplateBuilder createOptionsMenu(InventoryMenuTemplate menu) {
        InventoryMenuTemplateBuilder chatOptions = menu()
                .title("Chat options")
                .displayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Chat options")
                .displayIcon(Material.BOOK_AND_QUILL);
        for (ChatChannel channel : ChatManager.getVisibleChatChannels()) {
            chatOptions.component(item()
                    .displayName(ChatColor.GREEN + "" + ChatColor.BOLD + channel.getDisplayName())
                    .displayIcon(Material.BOOK)
                    .visibilityController((slp) -> slp.getRank().hasPermission(channel.getMinRank()))
                    .description((slp) -> {
                        List<String> description = new ArrayList<>();
                        description.add("");
                        description.add(ChatColor.GRAY + "Click here to " + (slp.isInChatChannel(channel) ? ChatColor.RED + "disable" : ChatColor.GREEN + "enable") + ChatColor.GRAY + " this channel!");
                        return description;
                    })
                    .onClick((event) -> {
                        SLPlayer slp = SpleefLeague.getInstance().getPlayerManager().get(event.getPlayer());
                        if (slp.isInChatChannel(channel)) {
                            slp.removeChatChannel(channel);
                            slp.getOptions().disableChatChannel(channel);
                        } else {
                            slp.addChatChannel(channel);
                            slp.getOptions().enableChatChannel(channel);
                        }
                        event.getItem().getParent().update();
                    }));
        }
        InventoryMenuItemTemplateBuilder requeue = item()
                .displayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Auto Requeue")
                .displayIcon(Material.BLAZE_POWDER)
                .description((slp) -> {
                    List<String> description = new ArrayList<>();
                    description.add("");
                    description.add(ChatColor.GRAY + "Click here to " + (slp.isRequeueing() ? ChatColor.RED + "disable" : ChatColor.GREEN + "enable") + ChatColor.GRAY + " requeueing!");
                    return description;
                })
                .onClick((event) -> {
                    SLPlayer slp = SpleefLeague.getInstance().getPlayerManager().get(event.getPlayer());
                    slp.setRequeueing(!slp.isRequeueing());
                    event.getItem().getParent().update();
                });
        InventoryMenuTemplateBuilder builder = menu()
                .title("Options")
                .displayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Options")
                .displayIcon(Material.SIGN)
                .description("")
                .description(ChatColor.GRAY + "Toggle a variety of options related")
                .description(ChatColor.GRAY + "to your SpleefLeague experience.")
                .component(chatOptions)
                .component(requeue);
        addBackButton(builder, menu);
        addBackButton(chatOptions, builder.build());
        return builder;
    }

    private static InventoryMenuTemplateBuilder createStaffMenu(InventoryMenuTemplate menu) {
        InventoryMenuTemplateBuilder builder = menu()
                .title("Moderative")
                .displayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Moderative")
                .displayIcon(Material.REDSTONE)
                .rank(Rank.DEVELOPER)
                .visibilityController((slp) -> slp.getRank().hasPermission(Rank.DEVELOPER))
                .component(menu()
                        .title(ChatColor.GREEN + "" + ChatColor.BOLD + "Reload Menu")
                        .displayName("Reload Menu")
                        .displayIcon(Material.WATCH)
                        .description("")
                        .description(SpleefLeague.fillColor + "Reloading various things")
                        .component(item()
                                .displayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Reload Server")
                                .displayIcon(Material.REDSTONE_TORCH_ON)
                                .description("")
                                .description(SpleefLeague.fillColor + "Reloads the server")
                                .onClick(event -> {
                                    event.getPlayer().closeInventory();
                                    Bukkit.reload();
                                })
                        ).component(item()
                                .displayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Reload Ranks")
                                .displayIcon(Material.BOOK_AND_QUILL)
                                .description("")
                                .description(SpleefLeague.fillColor + "Reloads all ranks")
                                .onClick(event -> {
                                    event.getPlayer().closeInventory();
                                    Rank.init();
                                    SpleefLeague.getInstance().getPlayerManager().getAll().stream().forEach((slp) -> {
                                        slp.setRank(Rank.valueOf(slp.getRank().getName()));
                                    });
                                    event.getPlayer().sendMessage(Theme.SUCCESS + "Reloaded " + Rank.values().length + " ranks!");
                                })
                        )
                )
                .component(item()
                        .displayName("Cancel all")
                        .displayIcon(Material.DIAMOND_SPADE)
                        .description("Cancels all currently")
                        .description("running matches")
                        .onClick(event -> {
                            event.getPlayer().closeInventory();
                            //GamePlugin.cancelAllMatches();
                            //event.getPlayer().sendMessage(Theme.SUCCESS + "All games have been cancelled.");
                        })
                );
        addBackButton(builder, menu);
        return builder;
    }

    public static InventoryMenuTemplateBuilder getNewGamemodeMenu() {
        InventoryMenuTemplateBuilder builder = menu();
        slMenuBuilder.component(currentGamemodeMenu++, builder);
        return builder;
    }

    public static InventoryMenuTemplate getInstance() {
        return slMenu;
    }
    
    private static class MenuPlayer {
        
        private SLPlayer target;
        private final SLPlayer source;

        public MenuPlayer(Player source) {
            this.source = SpleefLeague.getInstance().getPlayerManager().get(source);
        }

        public SLPlayer getSource() {
            return source;
        }
        
        public SLPlayer getTarget() {
            return target;
        }

        public void setTarget(Player player) {
            this.target = SpleefLeague.getInstance().getPlayerManager().get(player);
        }
    }
    
}
