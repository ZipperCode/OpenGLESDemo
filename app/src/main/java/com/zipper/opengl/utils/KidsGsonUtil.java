package com.zipper.opengl.utils;


import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

/**
 * Gson序列化
 */
public class KidsGsonUtil {

    private static Gson gson;

    private static Gson getGson() {
        if (gson == null) {
            GsonBuilder builder = new GsonBuilder();
            builder.disableHtmlEscaping();
            gson = builder.create();
        }
        return gson;
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return getGson().fromJson(json, clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public static <T> T fromJson(String json, Type typeOfT) {
        try {
            return getGson().fromJson(json, typeOfT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String toJson(Object obj) {
        if (obj == null) {
            return "";
        }
        return getGson().toJson(obj);
    }


}
