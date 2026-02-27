package com.absolutebuddies.sophisticatedbackpacksetchedintegration.mixin;

import com.absolutebuddies.sophisticatedbackpacksetchedintegration.EtchedStreamData;
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
import net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox.ServerStorageSoundHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(value = ServerStorageSoundHandler.class, remap = false)
public class ServerStorageSoundHandlerMixin
{
    @Inject(method = "startPlayingDisc(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Ljava/util/UUID;Lnet/minecraft/world/item/Item;Ljava/lang/Runnable;)V", at = @At("HEAD"), cancellable = true)
    private static void onStartPlayingDiscBlock(ServerLevel serverLevel, BlockPos position, UUID storageUuid, Item item, Runnable onFinishedHandler, CallbackInfo ci)
    {
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
        /*
        if (item instanceof EtchedMusicDiscItem)
        {
            EtchedStreamData.ACTIVE_STREAMS.put(storageUuid, EtchedStreamInfo.forEntity(entityId));

            EtchedMessages.PLAY.send(
                    PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(position.x, position.y, position.z, 64.0, serverLevel.dimension())),
                    new ClientboundPlayEntityMusicPacket(stack.copy(), serverLevel.getEntity(entityId), false)
            );
        }
        */
    }

    @Inject(method = "sendStopMessage", at = @At("HEAD"), remap = false)
    private static void OnSendStopMessage(ServerLevel serverWorld, Vec3 position, UUID storageUuid, CallbackInfo ci)
    {
        EtchedStreamInfo info = EtchedStreamData.ACTIVE_STREAMS.remove(storageUuid);

        if (info == null) return;

        if (info.isEntity())
        {
            Entity entity = serverWorld.getEntity(info.entityId);

            EtchedMessages.PLAY.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                    new ClientboundPlayEntityMusicPacket(entity)
            );
        }
        else
        {
            BlockPos pos = info.blockPos;

            /*
            EtchedMessages.PLAY.send(
                    PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 64.0, serverWorld.dimension())),
                    new ClientboundPlayMusicPacket(ItemStack.EMPTY, pos)
            );
            */

            // 2. Дополнительный пакет Etched для верности с увеличенным радиусом
            EtchedMessages.PLAY.send(
                    PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 128.0, serverWorld.dimension())),
                    new ClientboundPlayMusicPacket(ItemStack.EMPTY, pos)
            );

            System.out.println("DEBUG: Послали LevelEvent 1010 на " + pos);
        }
    }
}
