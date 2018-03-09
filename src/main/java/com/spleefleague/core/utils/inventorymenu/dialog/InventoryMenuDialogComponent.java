package com.spleefleague.core.utils.inventorymenu.dialog;

import com.spleefleague.core.utils.inventorymenu.*;
import com.spleefleague.core.utils.function.Dynamic;

public abstract class InventoryMenuDialogComponent<T> extends InventoryMenuComponent {

    private T builder;
    
    public InventoryMenuDialogComponent(ItemStackWrapper displayItem, Dynamic<Boolean> visibilityController, Dynamic<Boolean> accessController, boolean overwritePageBehavior) {
        super(displayItem, visibilityController, accessController, overwritePageBehavior);
    }

    public T getBuilder() {
        return builder;
    }

    protected void setBuilder(T builder) {
        this.builder = builder;
    }
}
