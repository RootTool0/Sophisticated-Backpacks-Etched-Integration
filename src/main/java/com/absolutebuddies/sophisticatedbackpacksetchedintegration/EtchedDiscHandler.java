package com.absolutebuddies.sophisticatedbackpacksetchedintegration;

import gg.moonflower.etched.api.record.PlayableRecord;
import gg.moonflower.etched.common.item.EtchedMusicDiscItem;
import gg.moonflower.etched.common.network.EtchedMessages;
import gg.moonflower.etched.common.network.play.ClientboundPlayEntityMusicPacket;
import gg.moonflower.etched.common.network.play.ClientboundPlayMusicPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedcore.api.IDiscHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox.ServerStorageSoundHandler;
import java.util.Optional;
import java.util.UUID;

public class EtchedDiscHandler implements IDiscHandler<EtchedMusicDiscItem>
{
    @Override
    public Optional<EtchedMusicDiscItem> getSongInfo(ItemStack var1, Level var2) { return Optional.empty(); }

    @Override
    public void playDisc(ServerLevel level, BlockPos pos, UUID storageUuid, ItemStack stack, Runnable onFinished)
    {
        ServerStorageSoundHandler.startPlayingDisc(level, pos, storageUuid, stack.getItem(), onFinished);

        EtchedStreamData.ACTIVE_STREAMS.put(storageUuid, EtchedStreamInfo.forBlock(pos));

        EtchedMessages.PLAY.send(
                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 64.0, level.dimension())),
                new ClientboundPlayMusicPacket(stack.copy(), pos)
        );

        System.out.println("SBEI: Послали LevelEvent 1010 на " + pos);
    }

    @Override
    public void playDisc(ServerLevel level, Vec3 pos, UUID storageUuid, ItemStack stack, int entityId, Runnable onFinished)
    {
        ServerStorageSoundHandler.startPlayingDisc(level, pos, storageUuid, entityId, stack.getItem(), onFinished);

        EtchedStreamData.ACTIVE_STREAMS.put(storageUuid, EtchedStreamInfo.forEntity(entityId));

        EtchedMessages.PLAY.send(
                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(pos.x, pos.y, pos.z, 64.0, level.dimension())),
                new ClientboundPlayEntityMusicPacket(stack.copy(), level.getEntity(entityId), false)
        );
    }

    @Override
    public Optional<Integer> getMusicLengthInTicks(ItemStack stack, Level level) { return Optional.of(1200); }

    @Override
    public boolean supports(ItemStack stack) { return stack.getItem() instanceof EtchedMusicDiscItem; }

    @Override
    public Optional<ItemStack> getRandomDisc(RandomSource var1) {  return Optional.empty(); }

    @Override
    public int getMusicDiscSize() { return 1; }
}
