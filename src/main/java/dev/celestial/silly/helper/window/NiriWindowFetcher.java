package dev.celestial.silly.helper.window;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonWriter;
import dev.celestial.silly.SillyPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketImpl;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class NiriWindowFetcher extends JsonWindowFetcher {
    private static final Gson _gson = new Gson();
    @Override
    public @Nullable String getJsonString() {
        // {"Ok":{"FocusedWindow":{"id":336,"title":"erik@WinterSolstice:~","app_id":"Alacritty","pid":2077035,"workspace_id":48,"is_focused":true,"is_floating":false,"is_urgent":false,"layout":{"pos_in_scrolling_layout":[3,1],"tile_size":[936.0,1004.0],"window_size":[936,1004],"tile_pos_in_workspace_view":null,"window_offset_in_tile":[0.0,0.0]},"focus_timestamp":{"secs":427731,"nanos":985818022}}}}
        var sock_path = System.getenv("NIRI_SOCKET");
        if (sock_path != null && !sock_path.isBlank()) {
            var path = UnixDomainSocketAddress.of(sock_path);
            try (var sock = SocketChannel.open(path)) {
                ByteBuffer buf = ByteBuffer.wrap("\"FocusedWindow\"\n".getBytes());
                sock.write(buf);
                var res_buf = ByteBuffer.allocate(4096);
                sock.read(res_buf);
                res_buf.flip();
                var res = StandardCharsets.UTF_8.decode(res_buf).toString();
                var reader = JsonParser.parseString(res);
                if (reader.isJsonObject()) {
                    var j = reader.getAsJsonObject();
                    if (j.has("Ok")) {
                        var Ok = j.getAsJsonObject("Ok");
                        if (Ok.has("FocusedWindow")) {
                            return _gson.toJson(Ok.getAsJsonObject("FocusedWindow"));
                        }
                    }
                }
            } catch (IOException e) {
                return null;
            }
        }
        return null;

//        ProcessBuilder builder = new ProcessBuilder("/usr/bin/niri", "msg", "-j", "focused-window");
//        try {
//            var proc = builder.start();
//            String output;
//            try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
//                output = reader.lines().collect(Collectors.joining("\n"));
//            }
//
//            var exit = proc.waitFor();
//            if (exit != 0) return null;
//
//            return output;
//        } catch (IOException | InterruptedException e) {
//            return null;
//        }
    }
}
