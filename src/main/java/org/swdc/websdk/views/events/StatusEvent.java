package org.swdc.websdk.views.events;

import org.swdc.dependency.event.AbstractEvent;

public class StatusEvent extends AbstractEvent {

    private String text;

    private double progress;

    private boolean alert;

    public StatusEvent(String text) {
        this(text,0d,false);
    }

    public StatusEvent(String text, double progress) {
        this(text,progress,false);
    }

    public StatusEvent(String text, double progress, boolean exception) {
        super(null);
        this.text = text;
        this.progress = progress;
        this.alert = exception;
    }

    public boolean isAlert() {
        return alert;
    }

    public double getProgress() {
        return progress;
    }

    public String getText() {
        return text;
    }
}
