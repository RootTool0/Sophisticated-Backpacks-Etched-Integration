package com.absolutebuddies.sophisticatedbackpacksetchedintegration.mixin;

import gg.moonflower.etched.api.util.DownloadProgressListener;
import gg.moonflower.etched.api.util.ProgressTrackingInputStream;
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

import org.apache.commons.io.FilenameUtils;

@Mixin(value = SoundCache.class, remap = false)
public class SoundCacheMixin {

    @Inject(
            method = "updateCache",
            at = @At("RETURN"),
            remap = false
    )
    private static void onCacheUpdate(Path path, String key, InputStream stream, SoundCache.CacheMetadata metadata, CallbackInfo ci) {

        String ext = "." + FilenameUtils.getExtension(key);

        System.out.println("[Etched Integration] !!!");
        System.out.println("  Path: " + path);
        System.out.println("  String: " + key);
        System.out.println("  ext: " + ext);

        try {
            // Проверяем что файл существует и не пустой
            if (!Files.exists(path) || Files.size(path) == 0) {
                System.out.println("[Etched Integration] 123");
                return;
            }

            AudioFile audioFile = AudioFileIO.readMagic(path.toFile());
            AudioHeader header = audioFile.getAudioHeader();

            long durationMs = header.getTrackLength() * 1000L;
            String format = header.getFormat();
            long bitrate = header.getBitRateAsNumber();
            String sampleRate = header.getSampleRate();

            System.out.println("[Etched Integration] Audio file cached:");
            System.out.println("  File: " + path.getFileName());
            System.out.println("  Format: " + format);
            System.out.println("  Duration: " + durationMs);
            System.out.println("  Bitrate: " + bitrate + " kbps");
            System.out.println("  Sample Rate: " + sampleRate + " Hz");

            // Сохраняем куда-нибудь для дальнейшего использования
            // например в статическую мапу или в metadata Etched (если есть доступ)

        } catch (Exception e) {
            System.out.println("[Etched Integration] Failed to read audio metadata: " + e.getMessage());
            // Не крашим игру если не получилось прочитать
        }
    }
}
