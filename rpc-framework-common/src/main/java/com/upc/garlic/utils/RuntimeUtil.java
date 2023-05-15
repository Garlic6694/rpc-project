package com.upc.garlic.utils;

public class RuntimeUtil {

    public static int cpus() {
        return Runtime.getRuntime().availableProcessors();
    }
}
