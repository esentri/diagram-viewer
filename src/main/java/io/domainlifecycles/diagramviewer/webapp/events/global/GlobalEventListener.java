package io.domainlifecycles.diagramviewer.webapp.events.global;

import com.vaadin.flow.component.Component;
import lombok.Getter;

import java.util.function.Consumer;

@Getter
public abstract class GlobalEventListener<T extends GlobalEvent> implements Consumer<T> {

    private final Component component;
    private final Class<T> eventClass;

    public GlobalEventListener(Component component,  Class<T> eventClass) {
        this.component = component;
        this.eventClass = eventClass;
    }

    @Override
    public final void accept(T t) {
        if(!t.getSenderComponent().equals(this.component)) {
            onEvent(t);
        }
    }

    public abstract void onEvent(T event);

    @Override
    public String toString() {
        return "GlobalEventListener{" +
                "component=" + component +
                '}';
    }
}
