package org.swdc.websdk.core.generator.java;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.swdc.dependency.EventEmitter;
import org.swdc.dependency.annotations.MultipleImplement;
import org.swdc.dependency.event.AbstractEvent;
import org.swdc.dependency.event.Events;
import org.swdc.fx.FXResources;
import org.swdc.websdk.core.generator.GeneratorFactory;
import org.swdc.websdk.core.generator.SDKGenerator;

@Singleton
@MultipleImplement(GeneratorFactory.class)
public class Java8GeneratorFactory implements GeneratorFactory,EventEmitter {

    @Inject
    private FXResources resources;

    @Inject
    private JavaSDKTemplate template;

    private Events events;

    @Override
    public SDKGenerator create() {
        return new JavaSDKGenerator(this, template, "1.8");
    }

    @Override
    public String getName() {
        return "Java8";
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
