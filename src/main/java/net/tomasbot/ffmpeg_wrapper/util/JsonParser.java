package net.tomasbot.ffmpeg_wrapper.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;

public class JsonParser {

    private static final Gson gson = new GsonBuilder().create();

    public static <T> T fromJson(@NotNull String data, Class<T> type) {
        return gson.fromJson(data, type);
    }

    public static <T> String toJson(@NotNull T data) {
        return gson.toJson(data);
    }
}
