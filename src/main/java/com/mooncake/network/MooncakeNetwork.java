package com.mooncake.network;

import com.mooncake.MooncakeMod;
import com.mooncake.blockentity.MooncakeNamingStandBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = MooncakeMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class MooncakeNetwork {
    private MooncakeNetwork() {
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(SetNamingStandTextPayload.TYPE, SetNamingStandTextPayload.STREAM_CODEC, MooncakeNetwork::handleSetText);
        registrar.playToClient(OpenNamingStandScreenPayload.TYPE, OpenNamingStandScreenPayload.STREAM_CODEC, MooncakeNetwork::handleOpenScreen);
    }

    public static void openNamingStandScreen(ServerPlayer player, BlockPos pos, String currentText) {
        PacketDistributor.sendToPlayer(player, new OpenNamingStandScreenPayload(pos, currentText));
    }

    private static void handleSetText(SetNamingStandTextPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            Level level = player.level();
            BlockPos pos = payload.pos();
            if (!level.isLoaded(pos) || player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D) {
                return;
            }
            if (level.getBlockEntity(pos) instanceof MooncakeNamingStandBlockEntity stand) {
                stand.setPendingName(payload.text());
            }
        });
    }

    private static void handleOpenScreen(OpenNamingStandScreenPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> com.mooncake.client.NamingStandClient.open(payload.pos(), payload.text()));
    }

    public record SetNamingStandTextPayload(BlockPos pos, String text) implements CustomPacketPayload {
        public static final Type<SetNamingStandTextPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(MooncakeMod.MOD_ID, "set_naming_stand_text"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SetNamingStandTextPayload> STREAM_CODEC =
                StreamCodec.composite(
                        BlockPos.STREAM_CODEC,
                        SetNamingStandTextPayload::pos,
                        ByteBufCodecs.STRING_UTF8,
                        SetNamingStandTextPayload::text,
                        SetNamingStandTextPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record OpenNamingStandScreenPayload(BlockPos pos, String text) implements CustomPacketPayload {
        public static final Type<OpenNamingStandScreenPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(MooncakeMod.MOD_ID, "open_naming_stand_screen"));
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenNamingStandScreenPayload> STREAM_CODEC =
                StreamCodec.composite(
                        BlockPos.STREAM_CODEC,
                        OpenNamingStandScreenPayload::pos,
                        ByteBufCodecs.STRING_UTF8,
                        OpenNamingStandScreenPayload::text,
                        OpenNamingStandScreenPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
