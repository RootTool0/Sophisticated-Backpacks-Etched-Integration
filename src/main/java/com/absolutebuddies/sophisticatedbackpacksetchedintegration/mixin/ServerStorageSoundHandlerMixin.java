package com.absolutebuddies.sophisticatedbackpacksetchedintegration.mixin;

import com.absolutebuddies.sophisticatedbackpacksetchedintegration.EtchedData;
import com.absolutebuddies.sophisticatedbackpacksetchedintegration.EtchedStreamInfo;
import gg.moonflower.etched.common.item.EtchedMusicDiscItem;
import gg.moonflower.etched.common.network.EtchedMessages;
import gg.moonflower.etched.common.network.play.ClientboundPlayEntityMusicPacket;
import gg.moonflower.etched.common.network.play.ClientboundPlayMusicPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedcore.network.PacketHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox.PlayDiscMessage;
import net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox.ServerStorageSoundHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

import static net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox.ServerStorageSoundHandler.putSoundInfo;

@Mixin(value = ServerStorageSoundHandler.class, remap = false)
public class ServerStorageSoundHandlerMixin
{
    @Inject(method = "startPlayingDisc(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Ljava/util/UUID;Lnet/minecraft/world/item/Item;Ljava/lang/Runnable;)V", at = @At("HEAD"), cancellable = true)
    private static void onStartPlayingDiscBlock(ServerLevel serverLevel, BlockPos position, UUID storageUuid, Item item, Runnable onFinishedHandler, CallbackInfo ci)
    {
        if (item instanceof EtchedMusicDiscItem)
        {
            System.out.println("[SBEI] onStartPlayingDiscBlock!");

            Vec3 pos = Vec3.atCenterOf(position);
            PacketHandler.INSTANCE.sendToAllNear(serverLevel.dimension(), pos, 128, new PlayDiscMessage(storageUuid, Item.getId(item), position));
            long var10004 = serverLevel.getGameTime();
            int var10005 = 1200; // TODO

            putSoundInfo(serverLevel, storageUuid, onFinishedHandler, pos, var10004 + (long)var10005);

            ci.cancel();
        }

        /*
        if (item instanceof EtchedMusicDiscItem)
        {
            EtchedStreamData.ACTIVE_STREAMS.put(storageUuid, EtchedStreamInfo.forBlock(position));

            EtchedMessages.PLAY.send(
                    PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(position.getX() + 0.5, position.getY() + 0.5, position.getZ() + 0.5, 64.0, serverLevel.dimension())),
                    new ClientboundPlayMusicPacket(item, position)
            );
        }
        */
    }

    @Inject(method = "startPlayingDisc(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;Ljava/util/UUID;ILnet/minecraft/world/item/Item;Ljava/lang/Runnable;)V", at = @At("HEAD"), cancellable = true)
    private static void onStartPlayingDiscEntity(ServerLevel serverLevel, Vec3 position, UUID storageUuid, int entityId, Item item, Runnable onFinishedHandler, CallbackInfo ci)
    {
        if (item instanceof EtchedMusicDiscItem)
        {
            System.out.println("[SBEI] onStartPlayingDiscEntity!");

            PacketHandler.INSTANCE.sendToAllNear(serverLevel.dimension(), position, 128, new PlayDiscMessage(storageUuid, Item.getId(item), entityId));
            long var10004 = serverLevel.getGameTime();
            int var10005 = 1200; // TODO

            putSoundInfo(serverLevel, storageUuid, onFinishedHandler, position, var10004 + (long)var10005);

            ci.cancel();
            // ci.cancel();
        }

        /*
        if (item instanceof EtchedMusicDiscItem)
        {
            EtchedStreamData.ACTIVE_STREAMS.put(storageUuid, EtchedStreamInfo.forEntity(entityId));

        }
        */
    }

    @Inject(method = "sendStopMessage", at = @At("HEAD"), remap = false)
    private static void OnSendStopMessage(ServerLevel serverWorld, Vec3 position, UUID storageUuid, CallbackInfo ci)
    {
        EtchedStreamInfo info = EtchedData.ACTIVE_STREAMS_CACHE.remove(storageUuid);
        if (info != null)
        {
            System.out.println("[SBEI] OnSendStopMessage!");

            if (info.isEntity())
            {
                Entity entity = serverWorld.getEntity(info.entityId);
                if (entity != null)
                {
                    EtchedMessages.PLAY.send(
                        PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                        new ClientboundPlayEntityMusicPacket(entity)
                    );
                }
            }
            else
            {
                EtchedMessages.PLAY.send(
                    PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(info.blockPos.getX(), info.blockPos.getY(), info.blockPos.getZ(), 64.0, serverWorld.dimension())),
                    new ClientboundPlayMusicPacket(ItemStack.EMPTY, info.blockPos)
                );
            }
        }
    }
}
