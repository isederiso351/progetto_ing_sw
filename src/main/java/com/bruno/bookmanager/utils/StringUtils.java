package com.bruno.bookmanager.utils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class StringUtils {
    public static String formatEnumName(String raw) {
        return Arrays.stream(raw.split("_"))
                .map(word -> word.charAt(0) + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
}
