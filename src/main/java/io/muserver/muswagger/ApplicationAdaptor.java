package io.muserver.muswagger;

import jakarta.ws.rs.core.Application;

import java.util.Set;

class ApplicationAdaptor extends Application {
    private final Set<Object> singletons;

    public ApplicationAdaptor(Set<Object> singletons) {
        this.singletons = singletons;
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

}
