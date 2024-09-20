package org.globsframework.view.filter.model;

import org.globsframework.core.model.Glob;

import java.util.stream.Stream;

interface Jump {
    Stream<Glob> from(Glob glob);
}
