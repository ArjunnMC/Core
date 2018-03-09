package com.spleefleague.core.utils.inventorymenu;

import java.util.Map;

import org.bukkit.ChatColor;

import com.spleefleague.core.player.SLPlayer;
import java.util.function.Function;
import org.bukkit.event.inventory.ClickType;

public class InventoryMenu extends AbstractInventoryMenu {
    
    protected InventoryMenu(ItemStackWrapper displayItem, String title, Map<Integer, ? extends InventoryMenuComponentTemplate<? extends InventoryMenuComponent>> components, Map<Integer, ? extends InventoryMenuComponentTemplate<? extends InventoryMenuComponent>> staticComponents, boolean exitOnClickOutside, boolean menuControls, Dynamic<Boolean> accessController, Dynamic<Boolean> visibilityController, SLPlayer slp, boolean overwritePageBehavior) {
        super(displayItem, visibilityController, accessController, overwritePageBehavior);
        this.slp = slp;
        this.allComponents = new HashMap<>();
        this.staticComponents = new HashMap<>();
        this.allComponents.putAll(components);
        this.staticComponents.putAll(staticComponents);
        this.inventories = new TreeMap<>();
        this.exitOnClickOutside = exitOnClickOutside;
        this.menuControls = menuControls;
        this.title = title;
        this.currentComponents = new HashMap<>();
        addMenuControls();
        populateInventory();
    }

    public SLPlayer getOwner() {
        return slp;
    }

    private void setParents() {
        currentComponents
                .values()
                .stream()
                .flatMap(m -> m.values().stream())
                .forEach(component -> component.setParent(this));
    }

    protected void populateInventory() {
        int highestDefined = 0;
        int count = 0;
        for(Entry<Integer, InventoryMenuComponentTemplate<? extends InventoryMenuComponent>> e : allComponents.entrySet()) {
            if(!e.getValue().isVisible(slp)) continue;
            count++;
            highestDefined = Math.max(highestDefined, e.getKey());
        }
        boolean multiPage = Math.max(count, highestDefined) > pagesize;
        TreeMap<Integer, Map<Integer, InventoryMenuComponent>> componentPageMap = generateComponentPageMap(allComponents);
        Queue<InventoryMenuComponent> fillupQueue = new LinkedList<>(allComponents
                .keySet()
                .stream()
                .sorted((i1, i2) -> Integer.compare(i2, i1))
                .filter(key -> key < 0)
                .map(key -> allComponents.get(key).construct(slp))
                .filter(m -> m.isVisible(slp))
                .collect(Collectors.toList()));
        for(int page = 0; !fillupQueue.isEmpty(); page++) {
            Map<Integer, InventoryMenuComponent> slots = componentPageMap.getOrDefault(page, new HashMap<>());
            if(slots.size() >= pagesize) continue;
            for (int slot = 0; !fillupQueue.isEmpty() && slot < pagesize; slot++) {
                if(slots.containsKey(slot)) continue;
                slots.put(slot, fillupQueue.poll());
            }
            componentPageMap.put(page, slots);
        }
        componentPageMap.put(-1, null);
        componentPageMap = MapUtil.compress(componentPageMap);
        componentPageMap.remove(-1);
        componentPageMap.values()
                .stream()
                .forEach(m -> {
                    staticComponents
                            .forEach((i, imct) -> m.put(i, imct.construct(slp)));
                });
        if(multiPage) {
            int max = componentPageMap.lastKey();
            for (int page = 0; page <= max; page++) {
                Map<Integer, InventoryMenuComponent> slots = componentPageMap.get(page);
                if(page > 0) {
                    InventoryMenuComponent lastPage = createPreviousPageItem(page).construct(slp);
                    lastPage.setParent(this);
                    slots.put(MAX_PAGE_SIZE - 9, lastPage);
                }
                if(page < max) {
                    InventoryMenuComponent nextPage = createNextPageItem(page).construct(slp);
                    nextPage.setParent(this);
                    slots.put(MAX_PAGE_SIZE - 1, createNextPageItem(page).construct(slp));
                }
            }
        }
        inventories.clear();
        currentComponents.clear();
        currentComponents.putAll(componentPageMap);
        for(Entry<Integer, Map<Integer, InventoryMenuComponent>> e : componentPageMap.entrySet()) {
            int max = e.getValue()
                    .keySet()
                    .stream()
                    .mapToInt(i -> i)
                    .max()
                    .orElse(0);
            int rows = (max + ROWSIZE) / ROWSIZE;
            Inventory inventory = Bukkit.createInventory(this, rows * ROWSIZE, title);
            e.getValue().forEach((key, value) -> inventory.setItem(key, value.getDisplayItemWrapper().construct(slp)));
            inventories.put(e.getKey(), inventory);
        }
        setParents();
    }
    
    private TreeMap<Integer, Map<Integer, InventoryMenuComponent>> generateComponentPageMap(Map<Integer, InventoryMenuComponentTemplate<? extends InventoryMenuComponent>> allComponents) {
        TreeMap<Integer, Map<Integer, InventoryMenuComponent>> pageMap = allComponents
                .entrySet()
                .stream()
                .filter((entry) -> (entry.getKey() >= 0 && entry.getValue().isVisible(slp)))
                .collect(Collectors.groupingBy(e -> e.getKey() / (e.getValue().getOverwritePageBehavior() ? MAX_PAGE_SIZE : pagesize),
                        TreeMap::new,
                        Collectors.toMap(
                                e -> e.getKey() % (e.getValue().getOverwritePageBehavior() ? MAX_PAGE_SIZE : pagesize), 
                                e -> e.getValue().construct(slp))));
        return pageMap;
    }
    
    private InventoryMenuItemTemplate createNextPageItem(int page) {
        return InventoryMenuAPI.item()
                .displayItem(new ItemStack(Material.DIAMOND_AXE, 1, (short)8))
                .displayName(ChatColor.GREEN + "Next page")
                .description("Click to go to the next page")
                .onClick(event -> {
                    this.open(page + 1);
                })
                .build();
    }
    
    private InventoryMenuItemTemplate createPreviousPageItem(int page) {
        return InventoryMenuAPI.item()
                .displayItem(new ItemStack(Material.DIAMOND_AXE, 1, (short)9))
                .displayName(ChatColor.RED + "Last page")
                .description("Click to go to the previous page")
                .onClick(event -> {
                    this.open(page - 1);
                })
                .build();
    }

    protected void addMenuControls() {
        if (menuControls) {
            InventoryMenuComponent rootComp = getRoot();

            if (rootComp instanceof InventoryMenu) {
//                InventoryMenu rootMenu = (InventoryMenu) rootComp;

                if (getParent() != null) {
//                    InventoryMenuItem mainMenuItem = InventoryMenuAPI.item()
//                            .displayIcon(Material.MINECART)
//                            .displayName(ChatColor.GREEN + "Main Menu")
//                            .description("Click to back to the main menu")
//                            .onClick(event -> rootMenu.open())
//                            .build().construct(slp);
//
//                    allComponents.put(1, mainMenuItem);
//                    inventory.setItem(1, mainMenuItem.getDisplayItemWrapper().construct(slp));

                    InventoryMenuItemTemplate goBackItem = InventoryMenuAPI.item()
                            .displayIcon(Material.ANVIL)
                            .displayName(ChatColor.GREEN + "Go back")
                            .description("Click to go back one menu level")
                            .onClick(event -> {
                                if (getParent() != null) {
                                    getParent().open();
                                } else {
                                    event.getPlayer().closeInventory();
                                }
                            })
                            .build();
                    allComponents.put(0, goBackItem);
                    //inventory.setItem(0, goBackItem.getDisplayItemWrapper().construct(slp));
                }
            }
        }
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
