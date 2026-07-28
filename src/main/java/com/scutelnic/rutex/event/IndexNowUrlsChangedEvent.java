package com.scutelnic.rutex.event;

import java.util.Collection;
import java.util.List;

public record IndexNowUrlsChangedEvent(List<String> paths) {

    public IndexNowUrlsChangedEvent(Collection<String> paths) {
        this(paths == null ? List.of() : List.copyOf(paths));
    }
}
