package com.workerai.client;

import com.workerai.utils.DebugAuth;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticationException;
import net.minecraft.client.main.Main;

import java.util.Arrays;

public class LaunchWrapper {
    public static void main(String[] args) {
        DebugAuth auth = new DebugAuth("compte1@nxgroupe.com", "^B=*_&8nB8ssp-q+");

        String assets = System.getenv().containsKey("assetDirectory") ? System.getenv("assetDirectory") : "assets";

        try {
            auth.connect();
        } catch (MicrosoftAuthenticationException e) {
            Main.main(concat(new String[]{"--version", "com/workerai", "--accessToken", "0", "--assetsDir", assets, "--assetIndex", "1.18", "--userProperties", "{}"}, args));
        }

        Main.main(concat(new String[]{"--version", "com/workerai", "--accessToken", auth.getAccessToken(), "--username", auth.getUsername(), "--uuid", auth.getUuid(), "--assetsDir", assets, "--assetIndex", "1.18", "--userProperties", "{}", "--token", "0"}, args));
    }

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}