package com.spleefleague.core.utils.inventorymenu.dialog;

import com.spleefleague.core.player.SLPlayer;
import com.spleefleague.core.utils.function.Dynamic;
import com.spleefleague.core.utils.inventorymenu.InventoryMenu;
import com.spleefleague.core.utils.inventorymenu.ItemStackWrapper;
import java.util.Map;

/**
 *
 * @author balsfull
 */
public class InventoryMenuDialog<T> extends InventoryMenu {
    
    private final InventoryMenuDialogListener<InventoryMenuDialogCompletionEvent<T>> listener;
    private final T builder;
    
    protected InventoryMenuDialog(
            ItemStackWrapper displayItem, 
            String title, 
            Map<Integer, InventoryMenuDialogComponentTemplate<? extends InventoryMenuDialogComponent>> components, 
            Map<Integer, InventoryMenuDialogComponentTemplate<? extends InventoryMenuDialogComponent>> staticComponents, 
            boolean exitOnClickOutside, 
            boolean menuControls, 
            Dynamic<Boolean> accessController, 
            Dynamic<Boolean> visibilityController, 
            SLPlayer slp, 
            boolean overwritePageBehavior,
            InventoryMenuDialogListener<InventoryMenuDialogCompletionEvent<T>> listener,
            T builder) {
        super(displayItem, title, components, staticComponents, exitOnClickOutside, menuControls, accessController, visibilityController, slp, overwritePageBehavior);
        this.listener = listener;
        this.builder = builder;
    }
    
    
}
