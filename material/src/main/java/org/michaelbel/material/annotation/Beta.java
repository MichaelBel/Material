package org.michaelbel.material.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Date: Sut, Mar 3 2018
 * Time: 23:19 MSK
 *
 * @author Michael Bel
 */

@Retention(RetentionPolicy.CLASS)
@Target({
        ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR,
        ElementType.FIELD,
        ElementType.METHOD,
        ElementType.TYPE })
@Documented
@SuppressWarnings("all")
public @interface Beta {}