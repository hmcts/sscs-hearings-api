package uk.gov.hmcts.reform.sscs.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MappingField {

    String columnName() default "";

    String isPrimary() default "";

    Class<?> clazz() default Object.class;

    int objectCount() default 0;

    int position() default -1;
}
