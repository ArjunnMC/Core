package com.spleefleague.core.utils.inventorymenu.dialog;

import com.spleefleague.core.utils.inventorymenu.InventoryMenuComponentTemplateBuilder;

public abstract class InventoryMenuDialogComponentTemplateBuilder<C, T extends InventoryMenuDialogComponentTemplate<C>, B extends InventoryMenuDialogComponentTemplateBuilder<C, T, B>> extends InventoryMenuComponentTemplateBuilder<C, T, B>{

    public InventoryMenuDialogComponentTemplateBuilder() {
        super();
    }
}
