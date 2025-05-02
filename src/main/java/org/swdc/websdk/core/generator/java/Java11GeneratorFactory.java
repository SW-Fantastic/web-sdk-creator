package org.swdc.websdk.core.generator.java;

import jakarta.inject.Inject;
import org.swdc.dependency.EventEmitter;
import org.swdc.dependency.annotations.MultipleImplement;
import org.swdc.dependency.event.AbstractEvent;
import org.swdc.dependency.event.Events;
import org.swdc.websdk.core.generator.GeneratorFactory;
import org.swdc.websdk.core.generator.SDKGenerator;

@MultipleImplement(GeneratorFactory.class)
public class Java11GeneratorFactory implements GeneratorFactory, EventEmitter {

    @Inject
    private JavaSDKTemplate template;

    private Events events;

    @Override
    public String getName() {
        return "Java 11";
    }

    @Override
    public SDKGenerator create() {
        return new JavaSDKGenerator(this,template,"11");
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public <T extends AbstractEvent> void emit(T t) {
        events.dispatch(t);
    }

    @Override
    public void setEvents(Events events) {
        this.events = events;
    }
}
