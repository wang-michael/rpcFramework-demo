package com.michaelwang;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jiuwang.wjw
 */
public class TestCompile {

    static Map<String, String> testMap = new HashMap<>();

    public static void main(String[] args) {
        testMap.put("a", "bb");
        String s = testMap.get("a");
        s = "cc";
        System.out.println(testMap.get("a"));
    }
}
