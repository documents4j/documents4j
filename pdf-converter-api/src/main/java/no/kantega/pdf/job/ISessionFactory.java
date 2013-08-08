package no.kantega.pdf.job;

public interface ISessionFactory {

    long getSessionIdleTime();

    IConversionSession createSession();

    IConversionSession findSession(String sessionId);

    void shutDown();
}
