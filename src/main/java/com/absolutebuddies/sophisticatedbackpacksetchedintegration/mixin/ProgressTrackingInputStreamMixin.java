package com.absolutebuddies.sophisticatedbackpacksetchedintegration.mixin;

import gg.moonflower.etched.api.util.DownloadProgressListener;
import gg.moonflower.etched.api.util.ProgressTrackingInputStream;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.InputStream;
import java.net.HttpURLConnection;

@Mixin(value = ProgressTrackingInputStream.class, remap = false)
public class ProgressTrackingInputStreamMixin {

    @Inject(method = "close", at = @At("RETURN"), remap = false)
    private void onClose(CallbackInfo ci) {
        // Поток закрыт, если это был скачивание - файл готов
        System.out.println("[Etched] Stream closed after reading");
    }
}
