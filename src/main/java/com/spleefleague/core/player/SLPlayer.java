package com.spleefleague.core.player;

import com.spleefleague.core.SpleefLeague;
import com.spleefleague.core.chat.ChatChannel;
import com.spleefleague.core.chat.ChatManager;
import com.spleefleague.core.chat.Theme;
import com.spleefleague.core.infraction.Infraction;
import com.spleefleague.core.infraction.InfractionType;
import com.spleefleague.core.io.typeconverters.RankConverter;
import com.spleefleague.core.utils.UtilChat;
import com.spleefleague.entitybuilder.DBLoad;
import com.spleefleague.entitybuilder.DBSave;
import com.spleefleague.entitybuilder.EntityBuilder;
import com.spleefleague.entitybuilder.TypeConverter.UUIDStringConverter;
import com.spleefleague.gameapi.GamePlugin;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.scoreboard.Team;

import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;

/**
 *
 * @author Jonas
 */
public class SLPlayer extends GeneralPlayer {

    private Rank baseRank;
    private TemporaryRank tempRank;
    private List<TemporaryRank> tempRankList;
    
    private UUID lastChatPartner;
    private int coins, premiumCredits;
    private HashSet<ChatChannel> chatChannels;
    private ChatChannel sendingChannel;
    private PlayerState state = PlayerState.IDLE;
    private PlayerOptions options;
    private boolean hasForumAccount = false;
    private ChatColor chatArrowColor = ChatColor.DARK_GRAY;
    private String tabName = null;
    private long areaMessageCooldown = 0L;
    private int premiumCreditsGotThatMonth;
    private long premiumCreditsLastReceptionTime;
    private Checkpoint checkpoint;
    private boolean requeue = true;
    
    public SLPlayer() {
        super();
        this.chatChannels = new HashSet<>();
        this.tempRankList = new ArrayList<>();
        this.sendingChannel = ChatChannel.GLOBAL;
    }

    public Rank getRank() {
        Rank rank = getActiveRank();
        if(getName() == null) {
            setPlayerListName(rank.getColor() + getName());
            setDisplayName(rank.getColor() + getName());
        }
        return rank;
    }
    
    @DBSave(fieldName = "rank", typeConverter = RankConverter.class)
    public Rank getBaseRank() {
        return baseRank;
    }

    @DBLoad(fieldName = "rank", typeConverter = RankConverter.class, priority = 2)
    public void setRank(final Rank rank) {
        this.baseRank = rank;
        try {
            setPlayerListName(rank.getColor() + getName());
            setDisplayName(rank.getColor() + getName());
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
    
    @DBLoad(fieldName = "temporaryRank")
    public void setTemporaryRank(TemporaryRank tempRank) {
        this.tempRank = tempRank;
    }
    
    @DBSave(fieldName = "temporaryRank", priority = 2)
    public TemporaryRank getTemporaryRank() {
        if(tempRank == null || tempRank.isExpired()) {
            return null;
        }
        return tempRank;
    }
    
    public void removeTemporaryRanksOfType(Rank rank) {
        tempRankList.removeIf(tr -> tr.getRank() == rank);
    }
    
    @DBLoad(fieldName = "temporaryRankList", priority = 2)
    public void setTemporaryRankList(List<TemporaryRank> tempRankList) {
        this.tempRankList = tempRankList;
    }
    
    @DBSave(fieldName = "temporaryRankList", priority = 2)
    public List<TemporaryRank> getTemporaryRankList() {
        return tempRankList;
    }
    
    public void addTemporaryRank(TemporaryRank tempRank) {
        if(tempRank.isExpired()) return;
        ListIterator<TemporaryRank> iter = tempRankList.listIterator();
        while(iter.hasNext()) {
            if(iter.next().getRank().getLadder() <= tempRank.getRank().getLadder()) {
                iter.previous();
                iter.add(tempRank);
                return;
            }
        }
        iter.add(tempRank);
    }
    
    private Rank getActiveRank() {
        if(tempRank != null && !tempRank.isExpired()) {
            return tempRank.getRank();
        }
        tempRank = null;
        tempRankList.removeIf(TemporaryRank::isExpired);
        if(!tempRankList.isEmpty()) {
            return tempRankList.get(0).getRank();
        }
        return baseRank;
    }
    
    @DBSave(fieldName = "checkpoint")
    public Checkpoint getCheckpoint() {
        if(checkpoint != null && checkpoint.isValid()) {
            return checkpoint;
        }
        return null;
    }
    
    @DBLoad(fieldName = "checkpoint")
    public void setCheckpoint(Checkpoint checkpoint) {
        if(checkpoint == null || checkpoint.isValid()) {
            this.checkpoint = checkpoint;
        }
    }
    
    public boolean isDonor() {
        return getRank().getName().equals("$") || isDonorPlus();
    }
    
    public boolean isDonorPlus() {
        return getRank().getName().equals("$$") || isDonorPlusPlus();
    }
    
    public boolean isDonorPlusPlus() {
        return getRank().getName().equals("$$$");
    }

    @DBSave(fieldName = "premiumCreditsGotThatMonth")
    public int getPremiumCreditsGotThatMonth() {
        return premiumCreditsGotThatMonth;
    }
    
    @DBLoad(fieldName = "premiumCreditsGotThatMonth")
    public void setPremiumCreditsGotThatMonth(int premiumCreditsGotThatMonth) {
        this.premiumCreditsGotThatMonth = premiumCreditsGotThatMonth;
    }

    @DBSave(fieldName = "premiumCreditsLastReceptionTime")
    public long getPremiumCreditsLastReceptionTime() {
        return premiumCreditsLastReceptionTime;
    }
    
    @DBLoad(fieldName = "premiumCreditsLastReceptionTime")
    public void setPremiumCreditsLastReceptionTime(long premiumCreditsLastReceptionTime) {
        this.premiumCreditsLastReceptionTime = premiumCreditsLastReceptionTime;
    }

    @DBLoad(fieldName = "coins")
    public void setCoins(int coins) {
        this.coins = coins;
    }

    @DBSave(fieldName = "coins")
    public int getCoins() {
        return coins;
    }
    
    public void addCoins(int coins) {
        addCoins(coins, "");
    }
    
    public void addCoins(int coins, String info) {
        this.coins += coins;
        ChatManager.sendMessagePlayer(SpleefLeague.getInstance().getPlayerManager().get(this.getPlayer()), SpleefLeague.getInstance().getChatPrefix()
                + SpleefLeague.fillColor + " You gained "
                + SpleefLeague.pointColor + coins
                + SpleefLeague.fillColor + (coins == 1 ? " coin" : " coins")
                + info
                + SpleefLeague.fillColor + ".");
    }
    
    public void changeCoins(int delta) {
        if(delta > 0)
            UtilChat.s(Theme.INFO, this, "&6+%d coin%s", delta, delta > 1 ? "s" : "");
        setCoins(Math.max(0, coins + delta));
    }
    
    @DBLoad(fieldName = "premiumCredits")
    public void setPremiumCredits(int credits) {
        this.premiumCredits = credits;
    }
    
    @DBSave(fieldName = "premiumCredits")
    public int getPremiumCredits() {
        return premiumCredits;
    }
    
    public void changePremiumCredits(int delta) {
        setPremiumCredits(Math.max(0, premiumCredits + delta));
    }

    @DBSave(fieldName = "lastChatPartner", typeConverter = UUIDStringConverter.class)
    public UUID getLastChatPartner() {
        return lastChatPartner;
    }

    @DBLoad(fieldName = "lastChatPartner", typeConverter = UUIDStringConverter.class)
    public void setLastChatPartner(UUID lastChatPartner) {
        this.lastChatPartner = lastChatPartner;
    }

    @DBSave(fieldName = "options")
    public PlayerOptions getOptions() {
        return options;
    }

    @DBLoad(fieldName = "options", priority = -1)
    private void setOptions(PlayerOptions options) {
        this.options = options;
    }
    
    protected void setReceivingChatChannels(HashSet<ChatChannel> chatChannels) {
        this.chatChannels = chatChannels;
    }

    public void setSendingChannel(ChatChannel channel) {
        this.sendingChannel = channel;
    }

    public ChatChannel getSendingChannel() {
        return sendingChannel;
    }

    public boolean isInChatChannel(ChatChannel channel) {
        return chatChannels.contains(channel);
    }

    public void addChatChannel(ChatChannel channel) {
        this.chatChannels.add(channel);
    }

    public void removeChatChannel(ChatChannel channel) {
        this.chatChannels.remove(channel);
    }

    public void setState(PlayerState state) {
        this.state = state;
    }

    public PlayerState getState() {
        return state;
    }

    @DBLoad(fieldName = "hasForumAccount")
    public void setForumAccount(boolean forumAccount) {
        this.hasForumAccount = forumAccount;
    }

    @DBSave(fieldName = "hasForumAccount")
    public boolean hasForumAccount() {
        return hasForumAccount;
    }

    public void resetVisibility() {
        if (this.getPlayer() != null && this.getPlayer().isOnline()) {
            for (SLPlayer slp : SpleefLeague.getInstance().getPlayerManager().getAll()) {
                if (slp != this && this.getState() == PlayerState.IDLE) {
                    if (slp.getState() == PlayerState.IDLE) {
                        this.showPlayer(slp.getPlayer());
                        slp.showPlayer(this.getPlayer());
                    }
                }
            }
        }
    }
    
    @Override
    public void done() {
        //Don't do this for fake players
        if(this.getPlayer() != null && this.getPlayer().isOnline()) {
            try {
                if(this.options == null) {
                    this.setOptions(PlayerOptions.getDefault());
                }
                this.options.apply(this);
            } finally {
            }
            Rank rank = getRank();
            setPlayerListName(rank.getColor() + getName());
            setDisplayName(rank.getColor() + getName());
            for (Team t : Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) {
                t.removeEntry(getName());
            }
            rank.getScoreboardTeam().addEntry(getName());
            if (rank.hasPermission(Rank.DEVELOPER)) {
                setGameMode(GameMode.CREATIVE);
            } else {
                setGameMode(GameMode.SURVIVAL);
            }
            rank.managePermissions(this);
        }
    }

    public void setChatArrowColor(ChatColor c) {
        this.chatArrowColor = c;
    }

    public ChatColor getChatArrowColor() {
        return this.chatArrowColor;
    }

    public void resetChatArrowColor() {
        this.chatArrowColor = ChatColor.DARK_GRAY;
    }

    public void setTabName(String s) {
        this.tabName = s;
        setPlayerListName(getTabName());
    }

    public String getTabName() {
        if (this.tabName == null) {
            return getRank().getColor() + getName();
        }
        return this.tabName;
    }

    public void updateAreaMessageCooldown() {
        this.areaMessageCooldown = System.currentTimeMillis();
    }

    public long getAreaMessageCooldown() {
        return areaMessageCooldown;
    }

    @Override
    public void setDefaults() {
        super.setDefaults();
        setRank(Rank.DEFAULT);
        setCoins(0);
        setPremiumCredits(0);
        this.chatChannels.clear();
        this.chatChannels.add(ChatChannel.GLOBAL);
        setSendingChannel(ChatChannel.GLOBAL);
    }
    
    // Player control
    public void performBan(UUID sender, String senderName, String message) {
        Bukkit.getScheduler().runTaskAsynchronously(SpleefLeague.getInstance(), () -> {
            Infraction ban = new Infraction(this.getUniqueId(), sender, InfractionType.BAN, System.currentTimeMillis(), -1, message);
            SpleefLeague.getInstance().getPluginDB().getCollection("ActiveInfractions").deleteMany(new Document("uuid", this.getUniqueId().toString()));
            EntityBuilder.save(ban, SpleefLeague.getInstance().getPluginDB().getCollection("Infractions"), false);
            EntityBuilder.save(ban, SpleefLeague.getInstance().getPluginDB().getCollection("ActiveInfractions"), false);
        });
        ChatManager
                .sendMessage(new ComponentBuilder(SpleefLeague.getInstance().getChatPrefix() + " ")
                    .append(this.getName() + " has been banned by " + senderName + "!")
                    .color(net.md_5.bungee.api.ChatColor.GRAY)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Reason: " + message)
                        .color(net.md_5.bungee.api.ChatColor.GRAY).create())
                    ).create(), ChatChannel.STAFF_NOTIFICATIONS
                );
    }
    
    public void spectate(Player target) {
        SLPlayer slp = SpleefLeague.getInstance().getPlayerManager().get(target);
        if (!GamePlugin.isIngameGlobal(this)) {
            for (GamePlugin gp : GamePlugin.getGamePlugins()) {
                if (gp.isIngame(target)) {
                    if (GamePlugin.isSpectatingGlobal(this)) {
                        GamePlugin.unspectateGlobal(this);
                    }
                    if (gp.spectateGracefully(target, this)) {
                        ChatManager.sendMessagePlayer(this, SpleefLeague.getInstance().getChatPrefix()
                                + SpleefLeague.fillColor + " You are now spectating "
                                + SpleefLeague.playerColor + target.getName()
                                + SpleefLeague.fillColor + ". Type '/spectate' to leave.");
                        return;
                    }
                }
            }
            ChatManager.sendMessagePlayer(this, SpleefLeague.getInstance().getChatPrefix() + " "
                    + SpleefLeague.playerColor + target.getName() 
                    + SpleefLeague.fillColor + " is not ingame.");
        } else {
            ChatManager.sendMessagePlayer(this, SpleefLeague.getInstance().getChatPrefix()
                    + SpleefLeague.fillColor + " You are currently ingame!");
        }
    }
    
    public void unspectate() {
        SLPlayer slp = SpleefLeague.getInstance().getPlayerManager().get(getPlayer());
        if (GamePlugin.isSpectatingGlobal(getPlayer())) {
            GamePlugin.unspectateGlobal(getPlayer());
            ChatManager.sendMessagePlayer(slp, SpleefLeague.getInstance().getChatPrefix()
                    + SpleefLeague.fillColor + " You are no longer spectating.");
            if (slp.getGameMode() == GameMode.ADVENTURE || slp.getGameMode() == GameMode.SURVIVAL) {
                slp.setAllowFlight(false);
                slp.setFlying(false);
            }
        } else {
            ChatManager.sendMessagePlayer(slp, SpleefLeague.getInstance().getChatPrefix()
                    + SpleefLeague.fillColor + " You are not currently spectating anyone!");
        }
    }
    
    @DBSave(fieldName="requeue")
    public boolean isRequeueing() {
        return this.requeue;
    }
    
    @DBLoad(fieldName="requeue")
    public void setRequeueing(boolean requeue) {
        this.requeue = requeue;
    }
}
