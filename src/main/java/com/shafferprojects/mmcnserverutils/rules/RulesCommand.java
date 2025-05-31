package com.shafferprojects.mmcnserverutils.rules;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.commands.arguments.EntityArgument;

public class RulesCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("agree")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    RulesAgreementHandler.agree(player);
                    return 1;
                }));

        dispatcher.register(Commands.literal("decline")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    player.connection.disconnect(Component.literal("§c⛔ You must agree to the server rules to play.\n§7Visit §bhttps://moddedmc.net/rules §7and click §a[✔ I Agree] §7to continue."));
                    return 1;
                }));

        dispatcher.register(Commands.literal("showrules")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(ctx -> {
                            ServerPlayer admin = ctx.getSource().getPlayerOrException();
                            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");

                            RulesAgreementHandler.forceRulesRecheck(admin, target);
                            ctx.getSource().sendSystemMessage(Component.literal("§eForced rules prompt for §6" + target.getName().getString()));
                            return 1;
                        })));
    }
}
