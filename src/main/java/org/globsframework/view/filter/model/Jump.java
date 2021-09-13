package org.globsframework.view.filter.model;

import org.globsframework.model.Glob;

import java.util.Optional;
import java.util.stream.Stream;

interface Jump {
    Stream<Glob> from(Glob glob);
}
