package no.kantega.pdf.builder;

import com.google.common.io.Files;
import no.kantega.pdf.api.IConverter;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;

public abstract class AbstractConverterBuilder<T extends AbstractConverterBuilder<T>> {

    public static final int DEFAULT_CORE_POOL_SIZE = 15;
    public static final int DEFAULT_MAXIMUM_POOL_SIZE = 30;
    public static final long DEFAULT_KEEP_ALIVE_TIME = TimeUnit.MINUTES.toMillis(10L);

    protected File baseFolder;
    protected int corePoolSize = DEFAULT_CORE_POOL_SIZE;
    protected int maximumPoolSize = DEFAULT_MAXIMUM_POOL_SIZE;
    protected long keepAliveTime = DEFAULT_KEEP_ALIVE_TIME;

    @SuppressWarnings("unchecked")
    public T baseFolder(File baseFolder) {
        this.baseFolder = baseFolder;
        return (T) this;
    }

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

    public abstract IConverter build();

    public File getBaseFolder() {
        return baseFolder;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public long getKeepAliveTime() {
        return keepAliveTime;
    }
}
