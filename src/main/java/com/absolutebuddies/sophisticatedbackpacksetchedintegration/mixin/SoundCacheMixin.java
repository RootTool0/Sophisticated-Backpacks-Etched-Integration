package com.absolutebuddies.sophisticatedbackpacksetchedintegration.mixin;

import com.absolutebuddies.sophisticatedbackpacksetchedintegration.EtchedData;
import gg.moonflower.etched.client.sound.SoundCache;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Mixin(value = SoundCache.class, remap = false)
public class SoundCacheMixin
{
    @Inject(method = "updateCache", at = @At("RETURN"), remap = false)
    private static void onCacheUpdate(Path path, String key, InputStream stream, SoundCache.CacheMetadata metadata, CallbackInfo ci)
    {

        System.out.println("[SBEI] !!!");
        System.out.println("  Path: " + path);
        System.out.println("  String: " + key);

        try
        {
            if(!Files.exists(path) || Files.size(path) == 0)
            {
                System.out.println("[SBEI] File not found!");
                return;
            }

            AudioFile audioFile = AudioFileIO.readMagic(path.toFile());
            AudioHeader header = audioFile.getAudioHeader();

            double duration = header.getPreciseTrackLength();
            Integer ticks = (int) Math.round(duration * 20);

            System.out.println("[SBEI] Audio file cached:");
            System.out.println("  File: " + path.getFileName());
            System.out.println("  Format: " + header.getFormat());
            System.out.println("  Duration: " + duration);
            System.out.println("  Ticks: " + ticks);
            System.out.println("  Bitrate: " + header.getBitRateAsNumber() + " kbps");
            System.out.println("  Sample Rate: " + header.getSampleRate() + " Hz");

            EtchedData.AUDIO_DURATION_CACHE.put(key, ticks);
        }
        catch (Exception e)
        {
            System.out.println("[SBEI] Failed to read audio metadata: " + e.getMessage());
        }
    }
}
