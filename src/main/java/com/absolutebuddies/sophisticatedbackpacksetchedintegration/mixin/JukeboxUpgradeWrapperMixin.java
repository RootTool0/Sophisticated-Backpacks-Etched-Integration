package com.absolutebuddies.sophisticatedbackpacksetchedintegration.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox.JukeboxUpgradeWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox.ServerStorageSoundHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(value = JukeboxUpgradeWrapper.class, remap = false)
public class JukeboxUpgradeWrapperMixin
{
    @Shadow private boolean isPlaying;
    @Shadow @Nullable private Level levelPlaying;
    @Shadow @Nullable private BlockPos posPlaying;
    @Shadow @Nullable private Entity entityPlaying;
    @Shadow(remap = false) protected IStorageWrapper storageWrapper;

    @Inject(method = "playNext(Z)V", at = @At("HEAD"))
    private void BeforePlayNext(boolean startOverIfAtTheEnd, CallbackInfo ci) {
        SoftStopCurrentSound();
    }

    @Inject(method = "playPrevious()V", at = @At("HEAD"))
    private void BeforePlayPrevious(CallbackInfo ci)
    {
        SoftStopCurrentSound();
    }

    @Unique
    private void SoftStopCurrentSound()
    {
        if(!this.isPlaying) return;

        System.out.println("[SBEI] SoftStopCurrentSound!");
        Level level = this.entityPlaying != null ? this.entityPlaying.level() : this.levelPlaying;

        if (level instanceof ServerLevel serverLevel)
        {
            this.storageWrapper.getContentsUuid().ifPresent(storageUuid ->
            {
                Vec3 stopPos = this.entityPlaying != null ? this.entityPlaying.position() : Vec3.atCenterOf(this.posPlaying);

                System.out.println("[SBEI] SoftStop triggered for: " + storageUuid);
                ServerStorageSoundHandler.stopPlayingDisc(serverLevel, stopPos, storageUuid);

            });
        }
    }
}
