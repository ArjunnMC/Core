package com.spleefleague.core.utils.inventorymenu;

import java.util.Map;

import org.bukkit.ChatColor;

import com.spleefleague.core.player.SLPlayer;
import com.spleefleague.core.utils.Tuple;
import java.util.function.Function;
import java.util.function.Supplier;
import org.bukkit.event.inventory.ClickType;

public class InventoryMenu extends AbstractInventoryMenu<SelectableInventoryMenuComponent> {
    
    protected InventoryMenu(
            AbstractInventoryMenu parent,
            ItemStackWrapper displayItem, 
            String title, 
            Map<Integer, Tuple<Supplier<InventoryMenuComponentTemplate<? extends SelectableInventoryMenuComponent>>, InventoryMenuComponentAlignment>> components, 
            Map<Integer, Supplier<InventoryMenuComponentTemplate<? extends SelectableInventoryMenuComponent>>> staticComponents,
            Function<SLPlayer, Boolean> accessController, 
            Function<SLPlayer, Boolean> visibilityController, 
            SLPlayer slp,
            int componentFlags, 
            int flags) {
        super(parent, displayItem, title, components, staticComponents, Function.identity(), accessController, visibilityController, slp, componentFlags, flags);
    }
    
    @Override
    public void selectItem(int index, ClickType clickType) {
        if (getCurrentComponents().get(getCurrentPage()).containsKey(index)) {
            SelectableInventoryMenuComponent component = getCurrentComponents().get(getCurrentPage()).get(index);
            if (component.hasAccess(getSLP())) {
                component.selected(clickType);
            } else {
                if(component.isSet(InventoryMenuComponentFlag.EXIT_ON_NO_PERMISSION)) {
                    getSLP().closeInventory();
                    getSLP().sendMessage(ChatColor.RED + "You don't have access to this");
                }
            }
        }
    }
}
