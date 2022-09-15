package cn.ranmc.util;

import cn.ranmc.Main;

import java.util.List;

public class Recall {

    public static String check(String msg) {
        for (String word : Main.INSTANCE.getRecall()) {
            if (msg.contains(word)) return word;
            if (word.contains("$&")) {
                boolean bool = true;
                String[] worlds = word.split("\\$&");
                for (String text : worlds) {
                    if (!msg.contains(text)) bool = false;
                }
                if (bool) return word;
            }

        }
        return "";
    }
}
