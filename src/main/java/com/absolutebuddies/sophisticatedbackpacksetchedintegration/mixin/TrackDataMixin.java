package com.absolutebuddies.sophisticatedbackpacksetchedintegration.mixin;

import gg.moonflower.etched.api.record.TrackData;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.URL;

import ws.schild.jave.MultimediaObject;
import ws.schild.jave.info.MultimediaInfo;

@Mixin(value = TrackData.class, remap = false)
public class TrackDataMixin
{
    @Inject(method = "save(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;", at = @At("RETURN"), remap = false)
    private void OnSave(CompoundTag nbt, CallbackInfoReturnable<CompoundTag> cir)
    {
        if(!nbt.contains("Url", Tag.TAG_STRING)) return;

        cir.getReturnValue().putInt("Duration", GetDurationTicks(nbt.getString("Url")));
    }

    private int GetDurationTicks(String Url)
    {
        try
        {
            MultimediaObject multimediaObject = new MultimediaObject(new URL(Url));

            long duration = multimediaObject.getInfo().getDuration();
            int ticks = Math.round(duration / 50);

            System.out.println("[SBEI] Duration: " + ticks + " ticks");
            return ticks;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        System.out.println("[SBEI] GetDurationTicks error!");
        return 200;
    }
}
