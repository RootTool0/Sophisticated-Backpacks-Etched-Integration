package com.absolutebuddies.sophisticatedbackpacksetchedintegration.mixin;

import com.absolutebuddies.sophisticatedbackpacksetchedintegration.SophisticatedBackpacksEtchedIntegrationDataBase;
import com.absolutebuddies.sophisticatedbackpacksetchedintegration.EtchedStreamInfo;
import gg.moonflower.etched.common.item.EtchedMusicDiscItem;
import gg.moonflower.etched.common.network.EtchedMessages;
import gg.moonflower.etched.common.network.play.ClientboundPlayEntityMusicPacket;
import gg.moonflower.etched.common.network.play.ClientboundPlayMusicPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedcore.network.PacketHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox.PlayDiscMessage;
import net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox.ServerStorageSoundHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox.StopDiscPlaybackMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.UUID;

import static net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox.ServerStorageSoundHandler.putSoundInfo;

@Mixin(value = ServerStorageSoundHandler.class, remap = false)
public class ServerStorageSoundHandlerMixin
{
    @Invoker("sendStopMessage")
    public static void invokeSendStopMessage(ServerLevel serverWorld, Vec3 position, UUID storageUuid) {
        throw new AssertionError();
    }

    @Inject(method = "startPlayingDisc(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Ljava/util/UUID;Lnet/minecraft/world/item/Item;Ljava/lang/Runnable;)V", at = @At("HEAD"), cancellable = true)
    private static void OnStartPlayingDiscBlock(ServerLevel serverLevel, BlockPos position, UUID storageUuid, Item item, Runnable onFinishedHandler, CallbackInfo ci)
    {
        if(SyncActiveStreams(serverLevel, Vec3.atCenterOf(position), item, storageUuid)) return;

        System.out.println("[SBEI] onStartPlayingDiscBlock!");

        Vec3 pos = Vec3.atCenterOf(position);
        PacketHandler.INSTANCE.sendToAllNear(serverLevel.dimension(), pos, 128, new PlayDiscMessage(storageUuid, Item.getId(item), position));

        putSoundInfo(serverLevel, storageUuid, onFinishedHandler, pos, serverLevel.getGameTime() + (long) SophisticatedBackpacksEtchedIntegrationDataBase.DISC_DURATION);
        System.out.println("[SBEI] Registered: " + SophisticatedBackpacksEtchedIntegrationDataBase.DISC_DURATION);

        ci.cancel();
    }

    @Inject(method = "startPlayingDisc(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;Ljava/util/UUID;ILnet/minecraft/world/item/Item;Ljava/lang/Runnable;)V", at = @At("HEAD"), cancellable = true)
    private static void OnStartPlayingDiscEntity(ServerLevel serverLevel, Vec3 position, UUID storageUuid, int entityId, Item item, Runnable onFinishedHandler, CallbackInfo ci)
    {
        if(SyncActiveStreams(serverLevel, position, item, storageUuid)) return;

        System.out.println("[SBEI] onStartPlayingDiscEntity!");

        PacketHandler.INSTANCE.sendToAllNear(serverLevel.dimension(), position, 128, new PlayDiscMessage(storageUuid, Item.getId(item), entityId));

        putSoundInfo(serverLevel, storageUuid, onFinishedHandler, position, serverLevel.getGameTime() + (long) SophisticatedBackpacksEtchedIntegrationDataBase.DISC_DURATION);
        System.out.println("[SBEI] Registered: " + SophisticatedBackpacksEtchedIntegrationDataBase.DISC_DURATION);

        ci.cancel();
    }

    @Inject(method = "sendStopMessage", at = @At("HEAD"), remap = false)
    private static void OnSendStopMessage(ServerLevel serverWorld, Vec3 position, UUID storageUuid, CallbackInfo ci)
    {
        SophisticatedBackpacksEtchedIntegrationDataBase.ACTIVE_STREAMS_CACHE.remove(storageUuid);

        EtchedStreamInfo info = SophisticatedBackpacksEtchedIntegrationDataBase.ETCHED_STREAMS_CACHE.remove(storageUuid);
        if (info == null) return;

        System.out.println("[SBEI] OnSendStopMessage!");
        StopEtchedStream(serverWorld, info);
        ci.cancel();
    }

    @Unique
    private static void StopEtchedStream(ServerLevel serverLevel, EtchedStreamInfo info)
    {
        if(info.isEntity())
        {
            Entity entity = serverLevel.getEntity(info.entityId);
            if(entity != null)
                EtchedMessages.PLAY.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), new ClientboundPlayEntityMusicPacket(entity));
        }
        else
            EtchedMessages.PLAY.send(
                    PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(info.blockPos.getX(), info.blockPos.getY(), info.blockPos.getZ(), 64.0, serverLevel.dimension())),
                    new ClientboundPlayMusicPacket(ItemStack.EMPTY, info.blockPos)
            );
    }


    @Unique
    private static boolean SyncActiveStreams(ServerLevel serverLevel, Vec3 position, Item item, UUID storageUuid)
    {
        SophisticatedBackpacksEtchedIntegrationDataBase.EStreamType type = SophisticatedBackpacksEtchedIntegrationDataBase.ACTIVE_STREAMS_CACHE.get(storageUuid);
        if(!(item instanceof EtchedMusicDiscItem))
        {
            // Vanilla
            if(type == SophisticatedBackpacksEtchedIntegrationDataBase.EStreamType.Etched)
            {
                EtchedStreamInfo info = SophisticatedBackpacksEtchedIntegrationDataBase.ETCHED_STREAMS_CACHE.get(storageUuid);
                if(info != null) StopEtchedStream(serverLevel, info);
            }

            SophisticatedBackpacksEtchedIntegrationDataBase.ACTIVE_STREAMS_CACHE.put(storageUuid, SophisticatedBackpacksEtchedIntegrationDataBase.EStreamType.Vanilla);
            return true;
        }

        // Etched
        if(type == SophisticatedBackpacksEtchedIntegrationDataBase.EStreamType.Vanilla)
            PacketHandler.INSTANCE.sendToAllNear(serverLevel.dimension(), position, 128, new StopDiscPlaybackMessage(storageUuid));
        SophisticatedBackpacksEtchedIntegrationDataBase.ACTIVE_STREAMS_CACHE.put(storageUuid, SophisticatedBackpacksEtchedIntegrationDataBase.EStreamType.Etched);

        return false;
    }
}
