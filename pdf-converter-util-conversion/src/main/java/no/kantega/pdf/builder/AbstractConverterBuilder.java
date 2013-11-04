package no.kantega.pdf.builder;

import com.google.common.io.Files;
import no.kantega.pdf.api.IConverter;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;

public abstract class AbstractConverterBuilder<T extends AbstractConverterBuilder<T>> {

    /**
     * The default core pool size of a converter's worker pool.
     */
    public static final int DEFAULT_CORE_POOL_SIZE = 15;

    /**
     * The default maximum pool size of a converter's worker pool.
     */
    public static final int DEFAULT_MAXIMUM_POOL_SIZE = 30;

    /**
     * The default keep alive time of a converter's worker pool.
     */
    public static final long DEFAULT_KEEP_ALIVE_TIME = TimeUnit.MINUTES.toMillis(10L);

    protected File baseFolder;
    protected int corePoolSize = DEFAULT_CORE_POOL_SIZE;
    protected int maximumPoolSize = DEFAULT_MAXIMUM_POOL_SIZE;
    protected long keepAliveTime = DEFAULT_KEEP_ALIVE_TIME;

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
     * Configures a worker pool for the converter.
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
        assertNumericArgument(maximumPoolSize, true);
        assertNumericArgument(corePoolSize + maximumPoolSize, false);
        assertNumericArgument(keepAliveTime, true);
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = unit.toMillis(keepAliveTime);
        return (T) this;
    }

    protected File normalizedBaseFolder() {
        return baseFolder == null ? Files.createTempDir() : this.baseFolder;
    }

    protected static void assertNumericArgument(long number, boolean zeroAllowed) {
        assertNumericArgument(number, zeroAllowed, Long.MAX_VALUE);
    }

    protected static void assertNumericArgument(long number, boolean zeroAllowed, long maximum) {
        checkArgument((zeroAllowed && number == 0L) || number > 0L);
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
