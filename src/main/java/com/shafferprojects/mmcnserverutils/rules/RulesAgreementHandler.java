package com.shafferprojects.mmcnserverutils.rules;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RulesAgreementHandler {

    private static final Set<UUID> lockedPlayers = new HashSet<>();

    public RulesAgreementHandler() {
        RulesDatabase.initTable();
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        UUID uuid = player.getUUID();

        if (RulesDatabase.hasAgreed(uuid.toString())) {
            lockedPlayers.remove(uuid);
        } else {
            lockedPlayers.add(uuid);
            sendRulesPrompt(player);
        }
    }

    public static void agree(ServerPlayer player) {
        UUID uuid = player.getUUID();
        RulesDatabase.markAgreed(uuid.toString());
        lockedPlayers.remove(uuid);
        player.sendSystemMessage(Component.literal("§a✅ Thanks for agreeing to the rules. You are now free to explore and play!"));
    }

    public static void decline(ServerPlayer player) {
        player.connection.disconnect(Component.literal("§c⛔ You must agree to the server rules to play.\n§7Visit §bhttps://moddedmc.net/rules §7and click §a[✔ I Agree] §7to continue."));
    }

    private static void sendRulesPrompt(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("§6📜 Please review our rules: ")
                .append(Component.literal("§b§nhttps://moddedmc.net/rules")
                        .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://moddedmc.net/rules")))));

        player.sendSystemMessage(Component.literal("§eDo you agree to follow the server rules?"));

        Component agree = Component.literal("§a§l[✔ I Agree]")
                .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/agree")));

        Component decline = Component.literal("§c§l[✖ Decline]")
                .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/decline")));

        player.sendSystemMessage(Component.literal("")
                .append(agree)
                .append(Component.literal(" "))
                .append(decline));
    }

    public static void forceRulesRecheck(ServerPlayer admin, ServerPlayer target) {
        RulesDatabase.deleteAgreement(target.getUUID().toString(), admin.getUUID().toString());
        lockedPlayers.add(target.getUUID());
        target.sendSystemMessage(Component.literal("§c⚠ You've been observed breaking our community rules. Please re-read them and indicate whether or not you agree."));
        sendRulesPrompt(target);
    }

    public static boolean isLocked(ServerPlayer player) {
        return lockedPlayers.contains(player.getUUID());
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isLocked(player)) return;

        player.setDeltaMovement(0, 0, 0);
        player.teleportTo(player.getX(), player.getY(), player.getZ());
    }

    @SubscribeEvent
    public void onPlayerRightClick(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (isLocked(player)) {
            event.setCanceled(true);
            sendRulesPrompt(player);
        }
    }

    @SubscribeEvent
    public void onPlayerLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (isLocked(player)) {
            event.setCanceled(true);
            sendRulesPrompt(player);
        }
    }

    @SubscribeEvent
    public void onPlayerUse(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (isLocked(player)) {
            event.setCanceled(true);
            sendRulesPrompt(player);
        }
    }
}
