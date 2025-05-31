package com.shafferprojects.mmcnserverutils;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import com.shafferprojects.mmcnserverutils.loggers.PlayerSessionLogger;
import com.shafferprojects.mmcnserverutils.rules.RulesAgreementHandler;
import com.shafferprojects.mmcnserverutils.rules.RulesCommand;
import com.shafferprojects.mmcnserverutils.database.DB;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(MMCNServerUtils.MODID)
public class MMCNServerUtils {
    public static final String MODID = "shafferprojectsmmcnserverutils";
    private static final Logger LOGGER = LogUtils.getLogger();

    public MMCNServerUtils(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(new RulesAgreementHandler());

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("[MMCN UTILS] Common setup starting");
        DB.validateConnectionOrExit(); // ✅ Force DB check here
        LOGGER.info("[MMCN UTILS] Database connection validated");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("[MMCN UTILS] Server starting: MMCN Utils Loaded: Rule Enforcement and Session Logging");
        PlayerSessionLogger.markAllOfflineForServer(com.shafferprojects.mmcnserverutils.database.DB.SERVER_NAME);
    }

    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent event) {
        RulesCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        GameProfile profile = player.getGameProfile();
        PlayerSessionLogger.logPlayer(profile, true);
        LOGGER.info("[MMCN UTILS] Logged player login: {}", profile.getName());
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        GameProfile profile = player.getGameProfile();
        PlayerSessionLogger.logPlayer(profile, false);
        LOGGER.info("[MMCN UTILS] Logged player logout: {}", profile.getName());
    }
}
