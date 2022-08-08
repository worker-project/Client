package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;

public class GameTestRegistry
{
    private static final Collection<TestFunction> TEST_FUNCTIONS = Lists.newArrayList();
    private static final Set<String> TEST_CLASS_NAMES = Sets.newHashSet();
    private static final Map<String, Consumer<ServerLevel>> BEFORE_BATCH_FUNCTIONS = Maps.newHashMap();
    private static final Map<String, Consumer<ServerLevel>> AFTER_BATCH_FUNCTIONS = Maps.newHashMap();
    private static final Collection<TestFunction> LAST_FAILED_TESTS = Sets.newHashSet();

    public static void register(Class<?> pTestClass)
    {
        Arrays.stream(pTestClass.getDeclaredMethods()).forEach(GameTestRegistry::register);
    }

    public static void register(Method pTestClass)
    {
        String s = pTestClass.getDeclaringClass().getSimpleName();
        GameTest gametest = pTestClass.getAnnotation(GameTest.class);

        if (gametest != null)
        {
            TEST_FUNCTIONS.add(turnMethodIntoTestFunction(pTestClass));
            TEST_CLASS_NAMES.add(s);
        }

        GameTestGenerator gametestgenerator = pTestClass.getAnnotation(GameTestGenerator.class);

        if (gametestgenerator != null)
        {
            TEST_FUNCTIONS.addAll(useTestGeneratorMethod(pTestClass));
            TEST_CLASS_NAMES.add(s);
        }

        registerBatchFunction(pTestClass, BeforeBatch.class, BeforeBatch::batch, BEFORE_BATCH_FUNCTIONS);
        registerBatchFunction(pTestClass, AfterBatch.class, AfterBatch::batch, AFTER_BATCH_FUNCTIONS);
    }

    private static <T extends Annotation> void registerBatchFunction(Method p_177506_, Class<T> p_177507_, Function<T, String> p_177508_, Map<String, Consumer<ServerLevel>> p_177509_)
    {
        T t = p_177506_.getAnnotation(p_177507_);

        if (t != null)
        {
            String s = p_177508_.apply(t);
            Consumer<ServerLevel> consumer = p_177509_.putIfAbsent(s, (Consumer<ServerLevel>)turnMethodIntoConsumer(p_177506_));

            if (consumer != null)
            {
                throw new RuntimeException("Hey, there should only be one " + p_177507_ + " method per batch. Batch '" + s + "' has more than one!");
            }
        }
    }

    public static Collection<TestFunction> getTestFunctionsForClassName(String pClassName)
    {
        return TEST_FUNCTIONS.stream().filter((p_127674_) ->
        {
            return isTestFunctionPartOfClass(p_127674_, pClassName);
        }).collect(Collectors.toList());
    }

    public static Collection<TestFunction> getAllTestFunctions()
    {
        return TEST_FUNCTIONS;
    }

    public static Collection<String> getAllTestClassNames()
    {
        return TEST_CLASS_NAMES;
    }

    public static boolean isTestClass(String pClassName)
    {
        return TEST_CLASS_NAMES.contains(pClassName);
    }

    @Nullable
    public static Consumer<ServerLevel> getBeforeBatchFunction(String pFunctionName)
    {
        return BEFORE_BATCH_FUNCTIONS.get(pFunctionName);
    }

    @Nullable
    public static Consumer<ServerLevel> getAfterBatchFunction(String pFunctionName)
    {
        return AFTER_BATCH_FUNCTIONS.get(pFunctionName);
    }

    public static Optional<TestFunction> findTestFunction(String pTestName)
    {
        return getAllTestFunctions().stream().filter((p_127663_) ->
        {
            return p_127663_.getTestName().equalsIgnoreCase(pTestName);
        }).findFirst();
    }

    public static TestFunction getTestFunction(String pTestName)
    {
        Optional<TestFunction> optional = findTestFunction(pTestName);

        if (!optional.isPresent())
        {
            throw new IllegalArgumentException("Can't find the test function for " + pTestName);
        }
        else
        {
            return optional.get();
        }
    }

    private static Collection<TestFunction> useTestGeneratorMethod(Method pTestMethod)
    {
        try
        {
            Object object = pTestMethod.getDeclaringClass().newInstance();
            return (Collection)pTestMethod.invoke(object);
        }
        catch (ReflectiveOperationException reflectiveoperationexception)
        {
            throw new RuntimeException(reflectiveoperationexception);
        }
    }

    private static TestFunction turnMethodIntoTestFunction(Method pTestMethod)
    {
        GameTest gametest = pTestMethod.getAnnotation(GameTest.class);
        String s = pTestMethod.getDeclaringClass().getSimpleName();
        String s1 = s.toLowerCase();
        String s2 = s1 + "." + pTestMethod.getName().toLowerCase();
        String s3 = gametest.template().isEmpty() ? s2 : s1 + "." + gametest.template();
        String s4 = gametest.batch();
        Rotation rotation = StructureUtils.getRotationForRotationSteps(gametest.rotationSteps());
        return new TestFunction(s4, s2, s3, rotation, gametest.timeoutTicks(), gametest.setupTicks(), gametest.required(), gametest.requiredSuccesses(), gametest.attempts(), (Consumer<net.minecraft.gametest.framework.GameTestHelper>)turnMethodIntoConsumer(pTestMethod));
    }

    private static Consumer<?> turnMethodIntoConsumer(Method pTestMethod)
    {
        return (p_177512_) ->
        {
            try {
                Object object = pTestMethod.getDeclaringClass().newInstance();
                pTestMethod.invoke(object, p_177512_);
            }
            catch (InvocationTargetException invocationtargetexception)
            {
                if (invocationtargetexception.getCause() instanceof RuntimeException)
                {
                    throw(RuntimeException)invocationtargetexception.getCause();
                }
                else
                {
                    throw new RuntimeException(invocationtargetexception.getCause());
                }
            }
            catch (ReflectiveOperationException reflectiveoperationexception)
            {
                throw new RuntimeException(reflectiveoperationexception);
            }
        };
    }

    private static boolean isTestFunctionPartOfClass(TestFunction pTestFunction, String pClassName)
    {
        return pTestFunction.getTestName().toLowerCase().startsWith(pClassName.toLowerCase() + ".");
    }

    public static Collection<TestFunction> getLastFailedTests()
    {
        return LAST_FAILED_TESTS;
    }

    public static void rememberFailedTest(TestFunction pTestFunction)
    {
        LAST_FAILED_TESTS.add(pTestFunction);
    }

    public static void forgetFailedTests()
    {
        LAST_FAILED_TESTS.clear();
    }
}
