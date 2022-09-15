package cn.ranmc.util;

import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;

public class DataFile {

    public static String read(String name) {
        try {
            File file = new File(System.getProperty("user.dir") + "/ranmc/" + name + ".txt");
            file.getCanonicalFile();
            return FileUtils.fileRead(file, "utf8");
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void write(String name,String text) {
        try {
            File file = new File(System.getProperty("user.dir") + "/ranmc/" + name + ".txt");
            FileUtils.fileWrite(file, text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
