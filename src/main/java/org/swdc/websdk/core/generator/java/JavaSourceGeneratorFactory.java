package org.swdc.websdk.core.generator.java;

import jakarta.inject.Singleton;
import org.swdc.dependency.EventEmitter;
import org.swdc.dependency.annotations.MultipleImplement;
import org.swdc.dependency.event.AbstractEvent;
import org.swdc.dependency.event.Events;
import org.swdc.websdk.core.generator.GeneratorFactory;
import org.swdc.websdk.core.generator.SDKGenerator;

@Singleton
@MultipleImplement(GeneratorFactory.class)
public class JavaSourceGeneratorFactory implements GeneratorFactory, EventEmitter {

    private Events events;

    @Override
    public SDKGenerator create() {
        return new JavaSDKGenerator(this, new JavaSDKTemplate(), null);
    }

    @Override
    public String getName() {
        return "Java Source";
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public <T extends AbstractEvent> void emit(T t) {
        this.events.dispatch(t);
    }

    @Override
    public void setEvents(Events events) {
        this.events = events;
    }
}
