package io.domainlifecycles.diagramviewer.webapp.events.global;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SessionScope
@Component
public class GlobalUIEventBus {
    private final Map<Class<? extends GlobalEvent>, List<GlobalEventListener<? extends GlobalEvent>>> listeners = new ConcurrentHashMap<>();

    public synchronized void register(GlobalEventListener<? extends GlobalEvent> listener) {
        var list = listeners
                .computeIfAbsent(listener.getEventClass(), k -> new ArrayList<>());
        list.add(listener);
    }

    public synchronized void unregister(GlobalEventListener<? extends GlobalEvent> listener) {
        var list = listeners.get(listener.getEventClass());
        if (list != null) {
            var removed = list.remove(listener);
            if(!removed){
                throw new IllegalStateException(String.format("Listener was not registered! %s", listener.toString()));
            }
            if (list.isEmpty()) {
                listeners.remove(listener.getEventClass());
            }
        }
    }

    public void fireEvent(GlobalEvent event) {
        var classType = event.getClass();
        var list = listeners.get(classType);
        for (GlobalEventListener listener : list) {
            listener.accept(event);
        }
    }

}
