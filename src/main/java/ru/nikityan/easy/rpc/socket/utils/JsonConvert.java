package ru.nikityan.easy.rpc.socket.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

public final class JsonConvert {

    private final static Gson gson = new GsonBuilder().serializeNulls().create();

    /**
     * Class no have instance
     */
    private JsonConvert() {

    }

    /**
     * This method deserializes the Json read from the specified parse tree into an object of the
     * specified type.
     *
     * @param <T>      the type of the desired object
     * @param message  the root of the parse tree of {@link JsonElement}s from which the object is to
     *                 be deserialized
     * @param classOfT The class of T
     * @return an object of type T from the json. Returns {@code null} if {@code json} is {@code null}.
     * @throws JsonSyntaxException if json is not a valid representation for an object of type typeOfT
     * @since 1.3
     */
    public static <T> T fromJson(JsonElement message, Class<T> classOfT) {
        return gson.fromJson(message, classOfT);
    }

    /**
     * This method deserializes the Json read from the specified parse tree into an object of the
     * specified type.
     *
     * @param <T>      the type of the desired object
     * @param payload  the string from which the object is to be deserialized
     * @param classOfT The class of T
     * @return an object of type T from the json. Returns {@code null} if {@code json} is {@code null}.
     * @throws JsonSyntaxException if json is not a valid representation for an object of type typeOfT
     * @since 1.3
     */
    public static <T> T fromJson(String payload, Class<T> classOfT) {
        return gson.fromJson(payload, classOfT);
    }

    /**
     * This method serializes the specified object into its equivalent Json representation.
     * This method should be used when the specified object is not a generic type.
     *
     * @param object the object for which Json representation is to be created setting for Gson
     * @return Json representation of {@code src}.
     */
    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    /**
     * This method serializes the specified object into its equivalent representation as a tree of
     * {@link JsonElement}s. This method should be used when the specified object is not a generic
     * type. This method uses {@link Class#getClass()} to get the type for the specified object, but
     * the {@code getClass()} loses the generic type information because of the Type Erasure feature
     * of Java.
     *
     * @param object the object for which Json representation is to be created setting for Gson
     * @return Json representation of {@code src}.
     */
    public static JsonElement toJsonElement(Object object) {
        return gson.toJsonTree(object);
    }
}
