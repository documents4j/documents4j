package com.documents4j.job;

import com.documents4j.api.IConverter;
import com.google.common.io.Files;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;

abstract class AbstractConverterBuilder<T extends AbstractConverterBuilder<T>> {

    /**
     * The default core pool size of a converter's worker pool.
     */
    public static final int DEFAULT_CORE_POOL_SIZE = 15;
    protected int corePoolSize = DEFAULT_CORE_POOL_SIZE;
    /**
     * The default maximum pool size of a converter's worker pool.
     */
    public static final int DEFAULT_MAXIMUM_POOL_SIZE = 30;
    protected int maximumPoolSize = DEFAULT_MAXIMUM_POOL_SIZE;
    /**
     * The default keep alive time of a converter's worker pool.
     */
    public static final long DEFAULT_KEEP_ALIVE_TIME = TimeUnit.MINUTES.toMillis(10L);
    protected long keepAliveTime = DEFAULT_KEEP_ALIVE_TIME;
    protected File baseFolder;

    protected static void assertNumericArgument(long number, boolean zeroAllowed) {
        assertNumericArgument(number, zeroAllowed, Long.MAX_VALUE);
    }

    protected static void assertNumericArgument(long number, boolean zeroAllowed, long maximum) {
        checkArgument(((zeroAllowed && number == 0L) || number > 0L) && number < maximum);
    }

    private static void assertSmallerEquals(int first, int second) {
        checkArgument(first <= second);
    }

    /**
     * Sets a folder for the constructed converter to save files in.
     *
     * @param baseFolder The base folder to be used or {@code null} if such a folder should be
     *                   created as temporary folder by the converter.
     * @return This builder instance.
     */
    @SuppressWarnings("unchecked")
    public T baseFolder(File baseFolder) {
        this.baseFolder = baseFolder;
        return (T) this;
    }

    /**
     * Configures a worker pool for the converter. This worker pool implicitly sets a maximum
     * number of conversions that are concurrently undertaken by the resulting converter. When a
     * converter is requested to concurrently execute more conversions than {@code maximumPoolSize},
     * it will queue excess conversions until capacities are available again.
     * <p>&nbsp;</p>
     * If this number is set too low, the concurrent performance of the resulting converter will be weak
     * compared to a higher number. If this number is set too high, the converter might <i>overheat</i>
     * when accessing the underlying external resource (such as for example an external process or a
     * HTTP connection). A remote converter that shares a conversion server with another converter might
     * also starve these other remote converters.
     *
     * @param corePoolSize    The core pool size of the worker pool.
     * @param maximumPoolSize The maximum pool size of the worker pool.
     * @param keepAliveTime   The keep alive time of the worker pool.
     * @param unit            The time unit of the specified keep alive time.
     * @return This builder instance.
     */
    @SuppressWarnings("unchecked")
    public T workerPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
        assertNumericArgument(corePoolSize, true);
        assertNumericArgument(maximumPoolSize, false);
        assertSmallerEquals(corePoolSize, maximumPoolSize);
        assertNumericArgument(keepAliveTime, true);
        assertNumericArgument(keepAliveTime, true);
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = unit.toMillis(keepAliveTime);
        return (T) this;
    }

    protected File normalizedBaseFolder() {
        return baseFolder == null ? Files.createTempDir() : this.baseFolder;
    }

    /**
     * Creates the converter that is specified by this builder.
     *
     * @return The converter that is specified by this builder.
     */
    public abstract IConverter build();

    /**
     * Returns the currently configured base folder.
     *
     * @return The specified base folder or {@code null} if the folder was not specified.
     */
    public File getBaseFolder() {
        return baseFolder;
    }

    /**
     * The currently specified core pool size of the converter's worker pool.
     *
     * @return The core pool size of the converter's worker pool.
     */
    public int getCorePoolSize() {
        return corePoolSize;
    }

    /**
     * The currently specified maximum pool size of the converter's worker pool.
     *
     * @return The maximum pool size of the converter's worker pool.
     */
    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    /**
     * The currently specified keep alive time of the converter's worker pool in milliseconds.
     *
     * @return The keep alive time of the converter's worker pool in milliseconds.
     */
    public long getKeepAliveTime() {
        return keepAliveTime;
    }
}
