package com.spleefleague.core.utils.inventorymenu.dialog;

import com.spleefleague.core.utils.function.Dynamic;
import com.spleefleague.core.utils.inventorymenu.ItemStackWrapper;
import org.bukkit.event.inventory.ClickType;

/**
 *
 * @author balsfull
 */
public class InventoryMenuDialogItem<T> extends InventoryMenuDialogComponent<T> {

    private final InventoryMenuDialogListener<InventoryMenuDialogClickEvent<T>> onClick;
    
    public InventoryMenuDialogItem(ItemStackWrapper displayItem, InventoryMenuDialogListener<InventoryMenuDialogClickEvent<T>> onClick, Dynamic<Boolean> visibilityController, Dynamic<Boolean> accessController, boolean overwritePageBehavior, T builder) {
        super(displayItem, visibilityController, accessController, overwritePageBehavior);
        this.onClick = onClick;
    }

    @Override
    protected void selected(ClickType clickType) {
        if (onClick != null) {
            this.getParent().
            onClick.onEvent(new InventoryMenuDialogClickEvent<T>(this, this.getParent().getOwner(), clickType, builder));
        }
    }
}
