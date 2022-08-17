package com.workerai.utils;

import net.minecraft.core.BlockPos;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class ClientInfos {
    public static final String NAME = "WorkerClient";
    public static final String VERSION = "1.0.0";
    public static final String COPYRIGHT = "Copyright WorkerClient. Do not distribute!";
    public static final String URL = "NoIdeaIndustry.com";

    public static final String SERVER_NAME = "Hypixel";
    public static final String SERVER_IP = "mc.hypixel.net";

    public static final Pattern HYPIXEL_PATTERN = Pattern.compile("^(?:(?:(?:.+\\.)?hypixel\\.net)|(?:209\\.222\\.115\\.\\d{1,3})|(?:99\\.198\\.123\\.[123]?\\d?))\\.?(?::\\d{1,5}\\.?)?$", Pattern.CASE_INSENSITIVE);
}
