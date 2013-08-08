package no.kantega.pdf.jersey;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "session")
public class SessionDescription {

    private String id;
    private long timeout;

    private boolean complete;

    private boolean valid;

    private int numberOfFiles;
    private int numberOfFilesCompleted;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public int getNumberOfFiles() {
        return numberOfFiles;
    }

    public void setNumberOfFiles(int numberOfFiles) {
        this.numberOfFiles = numberOfFiles;
    }

    public int getNumberOfFilesCompleted() {
        return numberOfFilesCompleted;
    }

    public void setNumberOfFilesCompleted(int numberOfFilesCompleted) {
        this.numberOfFilesCompleted = numberOfFilesCompleted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SessionDescription that = (SessionDescription) o;

        if (complete != that.complete) return false;
        if (numberOfFiles != that.numberOfFiles) return false;
        if (numberOfFilesCompleted != that.numberOfFilesCompleted) return false;
        if (timeout != that.timeout) return false;
        if (valid != that.valid) return false;
        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (int) (timeout ^ (timeout >>> 32));
        result = 31 * result + (complete ? 1 : 0);
        result = 31 * result + (valid ? 1 : 0);
        result = 31 * result + numberOfFiles;
        result = 31 * result + numberOfFilesCompleted;
        return result;
    }

    @Override
    public String toString() {
        return "SessionDescription{" +
                "id='" + id + '\'' +
                ", timeout=" + timeout +
                ", complete=" + complete +
                ", valid=" + valid +
                ", numberOfFiles=" + numberOfFiles +
                ", numberOfFilesCompleted=" + numberOfFilesCompleted +
                '}';
    }
}
