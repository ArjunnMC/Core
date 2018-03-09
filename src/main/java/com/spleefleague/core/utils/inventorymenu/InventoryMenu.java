package com.spleefleague.core.utils.inventorymenu;

import java.util.Map;

import org.bukkit.ChatColor;

import com.spleefleague.core.player.SLPlayer;
import java.util.function.Function;
import org.bukkit.event.inventory.ClickType;

public class InventoryMenu extends AbstractInventoryMenu {
    
    protected InventoryMenu(
            ItemStackWrapper displayItem, 
            String title, 
            Map<Integer, InventoryMenuComponentTemplate<? extends InventoryMenuComponent>> components, 
            Map<Integer, InventoryMenuComponentTemplate<? extends InventoryMenuComponent>> staticComponents, 
            Function<SLPlayer, Boolean> accessController, 
            Function<SLPlayer, Boolean> visibilityController, 
            SLPlayer slp, 
            int flags) {
        super(displayItem, title, components, staticComponents, accessController, visibilityController, slp, flags);
    }
    
    @Override
    public void selectItem(int index, ClickType clickType) {
        if (getCurrentComponents().get(getCurrentPage()).containsKey(index)) {
            InventoryMenuComponent component = getCurrentComponents().get(getCurrentPage()).get(index);
            if (component.hasAccess(getSLP())) {
                component.selected(clickType);
            } else {
                getSLP().closeInventory();
                getSLP().sendMessage(ChatColor.RED + "You don't have access to this");
            }
        }
    }
}
