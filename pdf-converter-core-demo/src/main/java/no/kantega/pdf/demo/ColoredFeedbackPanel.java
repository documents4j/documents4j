package no.kantega.pdf.demo;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;

public class ColoredFeedbackPanel extends FeedbackPanel {

    public ColoredFeedbackPanel(String id) {
        super(id);
        add(new AttributeAppender("class", new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject() {
                return findClass();
            }
        }));
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        setVisible(anyMessage());
    }

    private int findMaxLevel() {
        int level = 0;
        for (FeedbackMessage feedbackMessage : getCurrentMessages()) {
            level = Math.max(level, feedbackMessage.getLevel());
        }
        return level;
    }

    private String findClass() {
        int maxLevel = findMaxLevel();
        if (maxLevel > FeedbackMessage.WARNING) {
            return "alert alert-error";
        } else if (maxLevel > FeedbackMessage.SUCCESS) {
            return "alert-info";
        } else {
            return "alert alert-success";
        }
    }
}
