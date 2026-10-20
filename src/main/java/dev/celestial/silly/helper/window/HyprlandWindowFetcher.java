package dev.celestial.silly.helper.window;

import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

// TODO: use IPC directly instead
public class HyprlandWindowFetcher extends JsonWindowFetcher {
    @Override
    public @Nullable String getJsonString() {
        ProcessBuilder builder = new ProcessBuilder("/usr/bin/hyprctl", "activewindow", "-j");
        try {
            var proc = builder.start();
            String output;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                output = reader.lines().collect(Collectors.joining("\n"));
            }

            var exit = proc.waitFor();
            if (exit != 0) return null;

            return output;
        } catch (java.io.IOException | InterruptedException | NullPointerException e) {
            return null;
        }
    }
}
