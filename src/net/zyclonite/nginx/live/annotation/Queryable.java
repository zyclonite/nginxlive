/*
 * nginxlive
 *
 * Copyright (c) 2015   zyclonite networx
 * https://zyclonite.net
 * Lukas Prettenthaler
 */

package net.zyclonite.nginx.live.annotation;

import java.lang.annotation.*;

/**
 * Created by zyclonite on 13/11/15.
 */
@Documented
@Target(ElementType.FIELD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Queryable {
}