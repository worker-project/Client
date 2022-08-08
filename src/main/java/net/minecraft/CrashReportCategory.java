package net.minecraft;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class CrashReportCategory
{
    private final String title;
    private final List<CrashReportCategory.Entry> entries = Lists.newArrayList();
    private StackTraceElement[] stackTrace = new StackTraceElement[0];

    public CrashReportCategory(String pTitle)
    {
        this.title = pTitle;
    }

    public static String formatLocation(LevelHeightAccessor pLevelHeightAccess, double pX, double p_178940_, double pY)
    {
        return String.format(Locale.ROOT, "%.2f,%.2f,%.2f - %s", pX, p_178940_, pY, formatLocation(pLevelHeightAccess, new BlockPos(pX, p_178940_, pY)));
    }

    public static String formatLocation(LevelHeightAccessor pLevelHeightAccess, BlockPos pPos)
    {
        return formatLocation(pLevelHeightAccess, pPos.getX(), pPos.getY(), pPos.getZ());
    }

    public static String formatLocation(LevelHeightAccessor pLevelHeightAccess, int pX, int p_178945_, int pY)
    {
        StringBuilder stringbuilder = new StringBuilder();

        try
        {
            stringbuilder.append(String.format("World: (%d,%d,%d)", pX, p_178945_, pY));
        }
        catch (Throwable throwable2)
        {
            stringbuilder.append("(Error finding world loc)");
        }

        stringbuilder.append(", ");

        try
        {
            int i = SectionPos.blockToSectionCoord(pX);
            int j = SectionPos.blockToSectionCoord(p_178945_);
            int k = SectionPos.blockToSectionCoord(pY);
            int l = pX & 15;
            int i1 = p_178945_ & 15;
            int j1 = pY & 15;
            int k1 = SectionPos.sectionToBlockCoord(i);
            int l1 = pLevelHeightAccess.getMinBuildHeight();
            int i2 = SectionPos.sectionToBlockCoord(k);
            int j2 = SectionPos.sectionToBlockCoord(i + 1) - 1;
            int k2 = pLevelHeightAccess.getMaxBuildHeight() - 1;
            int l2 = SectionPos.sectionToBlockCoord(k + 1) - 1;
            stringbuilder.append(String.format("Section: (at %d,%d,%d in %d,%d,%d; chunk contains blocks %d,%d,%d to %d,%d,%d)", l, i1, j1, i, j, k, k1, l1, i2, j2, k2, l2));
        }
        catch (Throwable throwable1)
        {
            stringbuilder.append("(Error finding chunk loc)");
        }

        stringbuilder.append(", ");

        try
        {
            int i3 = pX >> 9;
            int j3 = pY >> 9;
            int k3 = i3 << 5;
            int l3 = j3 << 5;
            int i4 = (i3 + 1 << 5) - 1;
            int j4 = (j3 + 1 << 5) - 1;
            int k4 = i3 << 9;
            int l4 = pLevelHeightAccess.getMinBuildHeight();
            int i5 = j3 << 9;
            int j5 = (i3 + 1 << 9) - 1;
            int k5 = pLevelHeightAccess.getMaxBuildHeight() - 1;
            int l5 = (j3 + 1 << 9) - 1;
            stringbuilder.append(String.format("Region: (%d,%d; contains chunks %d,%d to %d,%d, blocks %d,%d,%d to %d,%d,%d)", i3, j3, k3, l3, i4, j4, k4, l4, i5, j5, k5, l5));
        }
        catch (Throwable throwable)
        {
            stringbuilder.append("(Error finding world loc)");
        }

        return stringbuilder.toString();
    }

    public CrashReportCategory setDetail(String pSectionName, CrashReportDetail<String> pValue)
    {
        try
        {
            this.setDetail(pSectionName, pValue.call());
        }
        catch (Throwable throwable)
        {
            this.setDetailError(pSectionName, throwable);
        }

        return this;
    }

    public CrashReportCategory setDetail(String pSectionName, Object pValue)
    {
        this.entries.add(new CrashReportCategory.Entry(pSectionName, pValue));
        return this;
    }

    public void setDetailError(String pSectionName, Throwable pThrowable)
    {
        this.setDetail(pSectionName, pThrowable);
    }

    public int fillInStackTrace(int pSize)
    {
        StackTraceElement[] astacktraceelement = Thread.currentThread().getStackTrace();

        if (astacktraceelement.length <= 0)
        {
            return 0;
        }
        else
        {
            this.stackTrace = new StackTraceElement[astacktraceelement.length - 3 - pSize];
            System.arraycopy(astacktraceelement, 3 + pSize, this.stackTrace, 0, this.stackTrace.length);
            return this.stackTrace.length;
        }
    }

    public boolean validateStackTrace(StackTraceElement pS1, StackTraceElement pS2)
    {
        if (this.stackTrace.length != 0 && pS1 != null)
        {
            StackTraceElement stacktraceelement = this.stackTrace[0];

            if (stacktraceelement.isNativeMethod() == pS1.isNativeMethod() && stacktraceelement.getClassName().equals(pS1.getClassName()) && stacktraceelement.getFileName().equals(pS1.getFileName()) && stacktraceelement.getMethodName().equals(pS1.getMethodName()))
            {
                if (pS2 != null != this.stackTrace.length > 1)
                {
                    return false;
                }
                else if (pS2 != null && !this.stackTrace[1].equals(pS2))
                {
                    return false;
                }
                else
                {
                    this.stackTrace[0] = pS1;
                    return true;
                }
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    public void trimStacktrace(int pAmount)
    {
        StackTraceElement[] astacktraceelement = new StackTraceElement[this.stackTrace.length - pAmount];
        System.arraycopy(this.stackTrace, 0, astacktraceelement, 0, astacktraceelement.length);
        this.stackTrace = astacktraceelement;
    }

    public void getDetails(StringBuilder pBuilder)
    {
        pBuilder.append("-- ").append(this.title).append(" --\n");
        pBuilder.append("Details:");

        for (CrashReportCategory.Entry crashreportcategory$entry : this.entries)
        {
            pBuilder.append("\n\t");
            pBuilder.append(crashreportcategory$entry.getKey());
            pBuilder.append(": ");
            pBuilder.append(crashreportcategory$entry.getValue());
        }

        if (this.stackTrace != null && this.stackTrace.length > 0)
        {
            pBuilder.append("\nStacktrace:");

            for (StackTraceElement stacktraceelement : this.stackTrace)
            {
                pBuilder.append("\n\tat ");
                pBuilder.append((Object)stacktraceelement);
            }
        }
    }

    public StackTraceElement[] getStacktrace()
    {
        return this.stackTrace;
    }

    public static void populateBlockDetails(CrashReportCategory pCategory, LevelHeightAccessor pLevelHeightAccessor, BlockPos pPos, @Nullable BlockState pState)
    {
        if (pState != null)
        {
            pCategory.setDetail("Block", pState::toString);
        }

        pCategory.setDetail("Block location", () ->
        {
            return formatLocation(pLevelHeightAccessor, pPos);
        });
    }

    static class Entry
    {
        private final String key;
        private final String value;

        public Entry(String pKey, @Nullable Object pValue)
        {
            this.key = pKey;

            if (pValue == null)
            {
                this.value = "~~NULL~~";
            }
            else if (pValue instanceof Throwable)
            {
                Throwable throwable = (Throwable)pValue;
                this.value = "~~ERROR~~ " + throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
            }
            else
            {
                this.value = pValue.toString();
            }
        }

        public String getKey()
        {
            return this.key;
        }

        public String getValue()
        {
            return this.value;
        }
    }
}
