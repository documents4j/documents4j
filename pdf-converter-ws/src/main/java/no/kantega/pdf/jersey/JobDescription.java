package no.kantega.pdf.jersey;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "job")
public class JobDescription {

    private String name;

    private boolean done;

    private boolean cancelled;

    private Boolean successful;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public Boolean getSuccessful() {
        return successful;
    }

    public void setSuccessful(Boolean successful) {
        this.successful = successful;
    }
}
