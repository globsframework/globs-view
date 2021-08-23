package org.globsframework.view.model;

import org.globsframework.metamodel.GlobType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface _StringAsDouble {
    GlobType TYPE = StringAsDouble.TYPE;

}
