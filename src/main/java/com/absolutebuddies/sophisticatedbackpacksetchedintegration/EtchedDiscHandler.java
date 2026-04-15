package com.absolutebuddies.sophisticatedbackpacksetchedintegration;

import gg.moonflower.etched.common.item.EtchedMusicDiscItem;
import gg.moonflower.etched.common.network.EtchedMessages;
import gg.moonflower.etched.common.network.play.ClientboundPlayEntityMusicPacket;
import gg.moonflower.etched.common.network.play.ClientboundPlayMusicPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedcore.api.IDiscHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox.ServerStorageSoundHandler;

import java.util.*;

public class EtchedDiscHandler implements IDiscHandler<EtchedMusicDiscItem>
{
    @Override
    public Optional<EtchedMusicDiscItem> getSongInfo(ItemStack var1, Level var2) { return Optional.empty(); }

    @Override
    public void playDisc(ServerLevel level, BlockPos pos, UUID storageUuid, ItemStack stack, Runnable onFinished)
    {
        System.out.println("[SBEI] playDisc block!");
        SophisticatedBackpacksEtchedIntegrationDataBase.ETCHED_STREAMS_CACHE.put(storageUuid, EtchedStreamInfo.forBlock(pos));
        SophisticatedBackpacksEtchedIntegrationDataBase.DISC_DURATION = getLengthInTicks(stack);

        EtchedMessages.PLAY.send(
            PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(pos.getX(), pos.getY(), pos.getZ(), 64.0, level.dimension())),
            new ClientboundPlayMusicPacket(stack.copy(), pos)
        );

        ServerStorageSoundHandler.startPlayingDisc(level, pos, storageUuid, stack.getItem(), onFinished);
    }

    @Override
    public void playDisc(ServerLevel level, Vec3 pos, UUID storageUuid, ItemStack stack, int entityId, Runnable onFinished)
    {
        System.out.println("[SBEI] playDisc entity!");
        SophisticatedBackpacksEtchedIntegrationDataBase.ETCHED_STREAMS_CACHE.put(storageUuid, EtchedStreamInfo.forEntity(entityId));
        SophisticatedBackpacksEtchedIntegrationDataBase.DISC_DURATION = getLengthInTicks(stack);

        Entity entity = level.getEntity(entityId);
        if(entity != null) {
            EtchedMessages.PLAY.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                    new ClientboundPlayEntityMusicPacket(stack.copy(), entity, false)
            );
        }

        ServerStorageSoundHandler.startPlayingDisc(level, pos, storageUuid, entityId, stack.getItem(), onFinished);
    }

    @Override
    public Optional<Integer> getMusicLengthInTicks(ItemStack stack, Level level) { return Optional.of(getLengthInTicks(stack)); }

    @Override
    public boolean supports(ItemStack stack) { return stack.getItem() instanceof EtchedMusicDiscItem; }

    // @Override
    // public Optional<ItemStack> getRandomDisc(RandomSource randomSource) { return Optional.of(new ItemStack(Items.MUSIC_DISC_13)); }

    @Override
    public Optional<ItemStack> getRandomDisc(RandomSource randomSource) { return Optional.empty(); }

    @Override
    public int getMusicDiscSize() { return 0; }

    public int getLengthInTicks(ItemStack stack) { return stack.getTag().getCompound("Music").getInt("Duration"); }
}
