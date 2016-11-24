/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.core.listeners;

import com.comphenix.packetwrapper.WrapperPlayClientBlockDig;
import com.comphenix.packetwrapper.WrapperPlayClientBlockPlace;
import com.comphenix.packetwrapper.WrapperPlayServerMapChunk;
import com.comphenix.packetwrapper.WrapperPlayServerMapChunkBulk;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.spleefleague.core.SpleefLeague;
import com.spleefleague.core.events.FakeBlockBreakEvent;
import com.spleefleague.core.utils.ChunkPacketUtil;
import com.spleefleague.core.utils.FakeArea;
import com.spleefleague.core.utils.FakeBlock;
import com.spleefleague.core.utils.MultiBlockChangeUtil;
import com.spleefleague.core.utils.fakeblock.FakeBlockCache;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunk.ChunkMap;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftSound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author Jonas
 */
public class FakeBlockHandler implements Listener {

    private PacketAdapter chunk, chunkBulk, breakController, placeController;

    private FakeBlockHandler() {
        initPacketListeners();
        Bukkit.getOnlinePlayers().stream().filter((player) -> (!fakeAreas.containsKey(player.getUniqueId()))).forEach((player) -> {
            fakeAreas.put(player.getUniqueId(), new HashSet<>());
            fakeBlockCache.put(player.getUniqueId(), new FakeBlockCache());
        });
    }

    public static void stop() {
        if (instance != null) {
            manager.removePacketListener(instance.chunk);
            manager.removePacketListener(instance.chunkBulk);
            manager.removePacketListener(instance.breakController);
            manager.removePacketListener(instance.placeController);
            HandlerList.unregisterAll(instance);
            instance = null;
        }
    }

    public static void init() {
        if (instance == null) {
            instance = new FakeBlockHandler();
            Bukkit.getPluginManager().registerEvents(instance, SpleefLeague.getInstance());
        }
    }

    private void initPacketListeners() {
        chunk = new PacketAdapter(SpleefLeague.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Server.MAP_CHUNK) {
            @Override
            public void onPacketReceiving(PacketEvent event) {

            }

            @Override
            public void onPacketSending(PacketEvent event) {
                WrapperPlayServerMapChunk wpsmc = new WrapperPlayServerMapChunk(event.getPacket());
                Bukkit.getScheduler().runTask(SpleefLeague.getInstance(), () -> {
                    Chunk chunk = event.getPlayer().getWorld().getChunkAt(wpsmc.getChunkX(), wpsmc.getChunkZ());
                    if (((ChunkMap) wpsmc.getChunkMap()).b == 0 && wpsmc.getGroundUpContinuous()) {
                        MultiBlockChangeUtil.removeChunk(event.getPlayer(), chunk);
                    } else {
                        MultiBlockChangeUtil.addChunk(event.getPlayer(), chunk);
                    }
                });
                Set<FakeBlock> blocks = getFakeBlocksForChunk(event.getPlayer(), wpsmc.getChunkX(), wpsmc.getChunkZ());
                if (blocks != null) {
                    ChunkPacketUtil.setBlocksPacketMapChunk(event.getPacket(), blocks);
                }
            }
        };
        chunkBulk = new PacketAdapter(SpleefLeague.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Server.MAP_CHUNK_BULK) {
            @Override
            public void onPacketReceiving(PacketEvent event) {

            }

            @Override
            public void onPacketSending(PacketEvent event) {
                WrapperPlayServerMapChunkBulk wpsmcb = new WrapperPlayServerMapChunkBulk(event.getPacket());
                Bukkit.getScheduler().runTask(SpleefLeague.getInstance(), () -> {
                    for (int i = 0; i < wpsmcb.getChunksX().length; i++) {
                        Chunk chunk = event.getPlayer().getWorld().getChunkAt(wpsmcb.getChunksX()[i], wpsmcb.getChunksZ()[i]);
                        if (((ChunkMap) wpsmcb.getChunks()[i]).b == 0) {
                            MultiBlockChangeUtil.removeChunk(event.getPlayer(), chunk);
                        } else {
                            MultiBlockChangeUtil.addChunk(event.getPlayer(), chunk);
                        }
                    }
                });
                Set<FakeBlock> blocks = getFakeBlocksForChunks(event.getPlayer(), wpsmcb.getChunksX(), wpsmcb.getChunksZ());
                if (blocks != null) {
                    ChunkPacketUtil.setBlocksPacketMapChunk(event.getPacket(), blocks);
                }
            }
        };
        breakController = new PacketAdapter(SpleefLeague.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Client.BLOCK_DIG) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                WrapperPlayClientBlockDig wrapper = new WrapperPlayClientBlockDig(event.getPacket());
                if (wrapper.getStatus() != EnumWrappers.PlayerDigType.START_DESTROY_BLOCK &&
                    wrapper.getStatus() != EnumWrappers.PlayerDigType.ABORT_DESTROY_BLOCK &&
                    wrapper.getStatus() != EnumWrappers.PlayerDigType.STOP_DESTROY_BLOCK) {
                    return;
                }
                if (event.getPlayer().getLocation().multiply(0).add(wrapper.getLocation().toVector()).getBlock().getType() == Material.AIR) {//Avoiding async world access
                    if (wrapper.getStatus() == EnumWrappers.PlayerDigType.STOP_DESTROY_BLOCK || wrapper.getStatus() == EnumWrappers.PlayerDigType.START_DESTROY_BLOCK) {
                        Location loc = wrapper.getLocation().toVector().toLocation(event.getPlayer().getWorld());
                        FakeBlock broken = null;
                        Chunk chunk = loc.getChunk();
                        Set<FakeBlock> fakeBlocks = getFakeBlocksForChunk(event.getPlayer(), chunk.getX(), chunk.getZ());
                        if (fakeBlocks != null) {
                            for (FakeBlock block : fakeBlocks) {
                                if (blockEqual(loc, block.getLocation())) {
                                    broken = block;
                                    break;
                                }
                            }
                        }
                        if (broken != null) {
                            if (wrapper.getStatus() == EnumWrappers.PlayerDigType.STOP_DESTROY_BLOCK || (broken.getType() == Material.SNOW_BLOCK && event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getType() == Material.DIAMOND_SPADE) || event.getPlayer().getGameMode() == GameMode.CREATIVE) {
                                FakeBlockBreakEvent fbbe = new FakeBlockBreakEvent(broken, event.getPlayer());
                                Bukkit.getPluginManager().callEvent(fbbe);
                                if (fbbe.isCancelled()) {
                                    event.getPlayer().sendBlockChange(fbbe.getBlock().getLocation(), fbbe.getBlock().getType(), (byte) 0);
                                } else {
                                    broken.setType(Material.AIR);
                                    for (Player subscriber : getSubscribers(broken)) {
                                        if (subscriber != event.getPlayer()) {
                                            subscriber.sendBlockChange(fbbe.getBlock().getLocation(), Material.AIR, (byte) 0);
                                            sendBreakParticles(subscriber, broken);
                                            sendBreakSound(subscriber, broken);
                                        }
                                    }
                                }
                            } else {
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }

            @Override
            public void onPacketSending(PacketEvent event) {

            }
        };
        placeController = new PacketAdapter(SpleefLeague.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Client.BLOCK_PLACE) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                WrapperPlayClientBlockPlace wrapper = new WrapperPlayClientBlockPlace(event.getPacket());
                if (wrapper.getLocation().getY() < 0) {
                    return;
                }
                Location loc = wrapper.getLocation().toVector().toLocation(event.getPlayer().getWorld());
                int chunkX = wrapper.getLocation().getX() >> 4;
                int chunkZ = wrapper.getLocation().getZ() >> 4;
                Set<FakeBlock> fakeBlocks = getFakeBlocksForChunk(event.getPlayer(), chunkX, chunkZ);
                if (fakeBlocks != null) {
                    for (FakeBlock fakeBlock : fakeBlocks) {
                        if (blockEqual(fakeBlock.getLocation(), loc)) {
                            event.setCancelled(true);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onPacketSending(PacketEvent event) {

            }
        };
        //1.9 patch manager.addPacketListener(chunk);
        manager.addPacketListener(chunkBulk);
        manager.addPacketListener(breakController);
        manager.addPacketListener(placeController);
    }

    private void sendBreakParticles(Player p, FakeBlock block) {
        PacketPlayOutWorldEvent packet = new PacketPlayOutWorldEvent(2001, new BlockPosition(block.getX(), block.getY(), block.getZ()), 80, false);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
    }

    private void sendBreakSound(Player p, FakeBlock b) {
        p.playSound(b.getLocation(), breakSounds.get(b.getType()), 1, 0.9f);
    }

    private boolean blockEqual(Location loc1, Location loc2) {
        if ((loc1.getX() + 0.5) / 1 == (loc2.getX() + 0.5) / 1) {
            if ((loc1.getZ() + 0.5) / 1 == (loc2.getZ() + 0.5) / 1) {
                if (loc1.getY() / 1 == loc2.getY() / 1) {
                    return true;
                }
            }
        }
        return false;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!fakeAreas.containsKey(event.getPlayer().getUniqueId())) {
            fakeAreas.put(event.getPlayer().getUniqueId(), new HashSet<>());
            fakeBlockCache.put(event.getPlayer().getUniqueId(), new FakeBlockCache());
        }
    }

    private static final ProtocolManager manager = SpleefLeague.getProtocolManager();
    private static final Map<UUID, Set<FakeArea>> fakeAreas;
    private static final Map<UUID, FakeBlockCache> fakeBlockCache;
    private static FakeBlockHandler instance;
    private static final HashMap<Material, Sound> breakSounds;
    
    static {
        fakeAreas = new HashMap<>();
        fakeBlockCache = new HashMap<>();
        breakSounds = new HashMap();
        initBreakSounds();
    }

    public static void addArea(FakeArea area, Player... players) {
        addArea(area, true, players);
    }

    public static void addArea(FakeArea area, boolean update, Player... players) {
        for (Player player : players) {
            fakeAreas.get(player.getUniqueId()).add(area);
            fakeBlockCache.get(player.getUniqueId()).addArea(area);
        }
        if (update) {
            MultiBlockChangeUtil.changeBlocks(area.getBlocks().toArray(new FakeBlock[0]), players);
        }
    }

    public static void removeArea(FakeArea area, Player... players) {
        removeArea(area, true, players);
    }

    public static void removeArea(FakeArea area, boolean update, Player... players) {
        for (Player player : players) {
            fakeAreas.get(player.getUniqueId()).remove(area);
            recalculateCache(player);
        }
        if (update) {
            Collection<FakeBlock> fblocks = area.getBlocks();
            Block[] blocks = new Block[fblocks.size()];
            int i = 0;
            for (FakeBlock fblock : fblocks) {
                blocks[i++] = fblock.getLocation().getBlock();
            }
            MultiBlockChangeUtil.changeBlocks(blocks, Material.AIR, players);
        }
    }

    public static Collection<Player> getSubscribers(FakeBlock block) {
        Collection<Player> players = new HashSet<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (FakeArea area : fakeAreas.get(player.getUniqueId())) {
                if (area.getBlocks().contains(block)) {
                    players.add(player);
                    break;
                }
            }
        }
        return players;
    }

    public static void addBlock(FakeBlock block, Player... players) {
        addBlock(block, true, players);
    }

    public static void addBlock(FakeBlock block, boolean update, Player... players) {
        for (Player player : players) {
            fakeAreas.get(player.getUniqueId()).add(block);
            fakeBlockCache.get(player.getUniqueId()).addBlocks(block);
            if (update) {
                player.sendBlockChange(block.getLocation(), block.getType(), (byte) 0);
            }
        }
    }

    public static void removeBlock(FakeBlock block, Player... players) {
        removeBlock(block, true, players);
    }

    public static void removeBlock(FakeBlock block, boolean update, Player... players) {
        for (Player player : players) {
            fakeAreas.get(player.getUniqueId()).remove(block);
            recalculateCache(player);
            if (update) {
                player.sendBlockChange(block.getLocation(), Material.AIR, (byte) 0);
            }
        }
    }

    public static void update(FakeArea area) {
        Collection<Player> players = new HashSet<>();
        for (Entry<UUID, Set<FakeArea>> entry : fakeAreas.entrySet()) {
            if (entry.getValue().contains(area)) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null) {
                    players.add(player);
                }
            }
        }
        MultiBlockChangeUtil.changeBlocks(area.getBlocks().toArray(new FakeBlock[0]), players.toArray(new Player[players.size()]));
    }

    private static void recalculateCache(Player... players) {
        for (Player player : players) {
            FakeBlockCache cache = fakeBlockCache.get(player.getUniqueId());
            cache.clear();
            for (FakeArea area : fakeAreas.get(player.getUniqueId())) {
                if (area instanceof FakeBlock) {
                    cache.addBlocks((FakeBlock) area);
                } else {
                    cache.addArea(area);
                }
            }
        }
    }

    //Cache this with and make a getFakeBlocks(chunkx, chunkz, player)
    public static Set<FakeBlock> getFakeBlocksForChunk(Player player, int x, int z) {
        return fakeBlockCache.get(player.getUniqueId()).getBlocks(x, z);
    }

    public static Set<FakeBlock> getFakeBlocksForChunks(Player player, int[] x, int[] z) {
        return fakeBlockCache.get(player.getUniqueId()).getBlocks(x, z);
    }

    private static void initBreakSounds() {
        for (net.minecraft.server.v1_8_R3.Block block : net.minecraft.server.v1_8_R3.Block.REGISTRY) {
            String breaksound = block.stepSound.getBreakSound();
            breakSounds.put(CraftMagicNumbers.getMaterial(block), toSound(breaksound));
        }
    }

    private static Sound toSound(String mcname) {
        for (Sound s : Sound.values()) {
            if (CraftSound.getSound(s).equals(mcname)) {
                return s;
            }
        }
        return null;
    }
}
