package com.workerai.client.modules.fairy.utils;

import com.workerai.client.WorkerClient;
import com.workerai.client.modules.fairy.config.FairyConfig;
import net.minecraft.core.BlockPos;

import java.util.Arrays;
import java.util.List;

public abstract class FairyPositions {
    public static final List<BlockPos> HUB = Arrays.asList(
            new BlockPos(138, 66, 129),
            new BlockPos(169, 60, 129),
            new BlockPos(147, 53, 88),
            new BlockPos(162, 46, 69),
            new BlockPos(176, 64, 42),
            new BlockPos(155, 62, 28),
            new BlockPos(110, 67, 58),
            new BlockPos(138, 66, 129),
            new BlockPos(87, 77, 43),
            new BlockPos(96, 106, 121),
            new BlockPos(113, 102, 106),
            new BlockPos(148, 112, 88),
            new BlockPos(149, 116, 115),
            new BlockPos(111, 120, 127),
            new BlockPos(132, 144, 114),
            new BlockPos(57, 90, 79),
            new BlockPos(48, 78, 81),
            new BlockPos(43, 120, 70),
            new BlockPos(49, 121, 69),
            new BlockPos(40, 108, 78),
            new BlockPos(82, 61, 196),
            new BlockPos(-133, 74, 133),
            new BlockPos(-152, 67, 123),
            new BlockPos(-166, 79, 93),
            new BlockPos(-183, 80, 29),
            new BlockPos(-233, 86, 84),
            new BlockPos(-229, 123, 84),
            new BlockPos(-259, 114, 85),
            new BlockPos(-262, 102, 67),
            new BlockPos(-260, 96, 48),
            new BlockPos(-252, 132, 51),
            new BlockPos(-207, 100, 66),
            new BlockPos(-214, 103, 89),
            new BlockPos(-191, 102, 109),
            new BlockPos(-261, 56, 115),
            new BlockPos(-248, 74, 125),
            new BlockPos(-245, 75, 149),
            new BlockPos(-195, 55, 153),
            new BlockPos(-142, 77, -31),
            new BlockPos(-225, 72, -21),
            new BlockPos(-208, 70, -80),
            new BlockPos(-187, 92, -104),
            new BlockPos(-94, 72, -129),
            new BlockPos(-81, 70, -88),
            new BlockPos(-49, 90, -72),
            new BlockPos(-24, 88, -63),
            new BlockPos(-20, 90, -12),
            new BlockPos(-50, 132, 32),
            new BlockPos(-56, 115, 28),
            new BlockPos(-60, 108, 3),
            new BlockPos(43, 152, 73),
            new BlockPos(22, 132, 25),
            new BlockPos(-52, 161, 43),
            new BlockPos(-39, 191, 34),
            new BlockPos(-3, 193, 32),
            new BlockPos(2, 181, 31),
            new BlockPos(10, 179, 22),
            new BlockPos(9, 75, 13),
            new BlockPos(-32, 71, 21),
            new BlockPos(-48, 76, 49),
            new BlockPos(26, 80, -65),
            new BlockPos(40, 68, -65),
            new BlockPos(44, 68, -34),
            new BlockPos(168, 60, -36),
            new BlockPos(154, 98, -71),
            new BlockPos(180, 63, -15),
            new BlockPos(-53, 70, -100),
            new BlockPos(-16, 66, -110),
            new BlockPos(-34, 67, -150),
            new BlockPos(-21, 79, -166),
            new BlockPos(-6, 66, -179),
            new BlockPos(-92, 59, -138),
            new BlockPos(-33, 76, -213),
            new BlockPos(34, 72, -162),
            new BlockPos(72, 71, -190),
            new BlockPos(70, 90, -149),
            new BlockPos(104, 77, -133),
            new BlockPos(72, 70, -99),
            new BlockPos(23, 79, -134)
    );

    public static final List<BlockPos> CRIMSON = Arrays.asList(
            new BlockPos(-644, 125, -689),
            new BlockPos(-79, 139, -779),
            new BlockPos(-352, 191, -553),
            new BlockPos(-717, 164, -981),
            new BlockPos(-726, 144, -891),
            new BlockPos(-690, 122, -752),
            new BlockPos(-31, 178, -907),
            new BlockPos(-297, 81, -835),
            new BlockPos(-462, 78, -698),
            new BlockPos(-383, 71, -883),
            new BlockPos(-721, 125, -811),
            new BlockPos(-480, 104, -593),
            new BlockPos(-606, 154, -800),
            new BlockPos(-106, 89, -883),
            new BlockPos(-343, 235, -780),
            new BlockPos(-500, 127, -795),
            new BlockPos(-346, 75, -552),
            new BlockPos(-445, 110, -1026),
            new BlockPos(-361, 133, -469),
            new BlockPos(-247, 44, -512),
            new BlockPos(-380, 141, -1020),
            new BlockPos(-310, 156, -1008),
            new BlockPos(-412, 58, -935),
            new BlockPos(-396, 108, -764),
            new BlockPos(-342, 101, -484),
            new BlockPos(-35, 116, -1055),
            new BlockPos(-361, 69, -425),
            new BlockPos(14, 108, -769),
            new BlockPos(-479, 114, -972)
    );

    public static final List<BlockPos> COMBAT_1 = Arrays.asList(
            new BlockPos(-279, 127, -177),
            new BlockPos(-185, 135, -290),
            new BlockPos(-147, 78, -299),
            new BlockPos(-169, 62, -289),
            new BlockPos(-297, 90, -169),
            new BlockPos(-309, 63, -185),
            new BlockPos(-309, 66, -245),
            new BlockPos(-203, 169, -320),
            new BlockPos(-222, 74, -361),
            new BlockPos(-140, 85, -335),
            new BlockPos(-198, 160, -331),
            new BlockPos(-160, 62, -275),
            new BlockPos(-301, 92, -171),
            new BlockPos(-294, 36, -274),
            new BlockPos(-204, 94, -241),
            new BlockPos(-336, 82, -153),
            new BlockPos(-422, 106, -206),
            new BlockPos(-322, 95, -281),
            new BlockPos(-336, 111, -253)
    );

    public static final List<BlockPos> COMBAT_3 = Arrays.asList(
            new BlockPos(-517, 100, -295),
            new BlockPos(-583, 208, -272),
            new BlockPos(-696, 116, -256),
            new BlockPos(-587, 122, -276),
            new BlockPos(-587, 48, -293),
            new BlockPos(-492, 21, -175),
            new BlockPos(-492, 81, -275),
            new BlockPos(-545, 92, -257),
            new BlockPos(-748, 106, -284),
            new BlockPos(-723, 75, -222),
            new BlockPos(-609, 84, -230),
            new BlockPos(-657, 36, -201)
    );

    public static final List<BlockPos> FORAGING = Arrays.asList(
            new BlockPos(-294, 85, 31),
            new BlockPos(-315, 89, -72),
            new BlockPos(-390, 61, -6),
            new BlockPos(-357, 99, 79),
            new BlockPos(-386, 108, -69),
            new BlockPos(-404, 136, 6),
            new BlockPos(-454, 120, -58),
            new BlockPos(-408, 122, -92),
            new BlockPos(-450, 113, -87),
            new BlockPos(-370, 112, -118),
            new BlockPos(-471, 132, -125)
    );

    public static final List<BlockPos> FARMING = Arrays.asList(
            new BlockPos(138, 72, -587),
            new BlockPos(150, 60, -448),
            new BlockPos(155, 23, -204),
            new BlockPos(273, 141, -467),
            new BlockPos(111, 63, -447),
            new BlockPos(126, 91, -304),
            new BlockPos(193, 66, -468),
            new BlockPos(279, 112, -541),
            new BlockPos(387, 78, -365),
            new BlockPos(254, 70, -493),
            new BlockPos(271, 56, -361),
            new BlockPos(145, 77, -374),
            new BlockPos(182, 99, -235),
            new BlockPos(152, 67, -361),
            new BlockPos(263, 177, -565),
            new BlockPos(183, 99, -305),
            new BlockPos(96, 96, -287),
            new BlockPos(99, 112, -275),
            new BlockPos(261, 133, -348),
            new BlockPos(143, 90, -243)
    );

    public static final List<BlockPos> GOLD_MINE = Arrays.asList(
            new BlockPos(-47, 75, -298),
            new BlockPos(-62, 104, -289),
            new BlockPos(-37, 78, -308),
            new BlockPos(17, 86, -312),
            new BlockPos(21, 36, -320),
            new BlockPos(-44, 100, -344),
            new BlockPos(-26, 94, -340),
            new BlockPos(-1, 80, -337),
            new BlockPos(19, 57, -341),
            new BlockPos(-19, 142, -364),
            new BlockPos(-23, 113, -353),
            new BlockPos(-11, 76, -395)
    );

    public static final List<BlockPos> DEEP_CAVERNS = Arrays.asList(
            new BlockPos(3, 152, 85),
            new BlockPos(18, 74, 74),
            new BlockPos(-21, 25, 72),
            new BlockPos(3, 182, 50),
            new BlockPos(0, 65, 57),
            new BlockPos(3, 14, 51),
            new BlockPos(9, 170, 44),
            new BlockPos(-60, 37, 52),
            new BlockPos(-35, 127, 28),
            new BlockPos(-18, 163, 26),
            new BlockPos(44, 98, 23),
            new BlockPos(57, 161, 19),
            new BlockPos(29, 149, 14),
            new BlockPos(-2, 255, -1),
            new BlockPos(-40, 72, 0),
            new BlockPos(-11, 102, -16),
            new BlockPos(-71, 13, 5),
            new BlockPos(-76, 13, 24),
            new BlockPos(-8, 74, -44),
            new BlockPos(71, 167, -12),
            new BlockPos(22, 156, -42)
    );

    public static final List<BlockPos> MINING_3 = Arrays.asList(
            new BlockPos(-9, 230, -135),
            new BlockPos(-21, 208, -59),
            new BlockPos(-139, 220, -89),
            new BlockPos(155, 189, 123),
            new BlockPos(133, 104, 104),
            new BlockPos(-53, 205, 50),
            new BlockPos(34, 102, 86),
            new BlockPos(-204, 131, 199),
            new BlockPos(22, 127, 184),
            new BlockPos(-110, 142, 143),
            new BlockPos(-116, 142, 154)
    );

    public static final List<BlockPos> WINTER = Arrays.asList(
            new BlockPos(-95, 77, 20),
            new BlockPos(-44, 87, 76),
            new BlockPos(-7, 108, 107),
            new BlockPos(56, 108, 64),
            new BlockPos(74, 109, -18)
    );

    public static final List<BlockPos> DUNGEON = Arrays.asList(
            new BlockPos(17, 124, -58),
            new BlockPos(1, 134, 75),
            new BlockPos(10, 164, -10),
            new BlockPos(-139, 46, -1),
            new BlockPos(-55, 82, -10),
            new BlockPos(-4, 21, -17),
            new BlockPos(14, 60, 99)
    );

    public static boolean doesFairyExistAtPosition(BlockPos fairyPos) {
        FairyConfig fairyConfig = (FairyConfig) WorkerClient.getInstance().getWorkerHandler().getModuleHandler().getModule("Fairy").getModuleConfig();
        return !fairyConfig.getCollectedFairies().contains(fairyPos) && fairyConfig.getAllFairies().contains(fairyPos);
    }
}
