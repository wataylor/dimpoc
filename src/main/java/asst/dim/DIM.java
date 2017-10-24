package asst.dim;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Record information about the meaning of a datum.  This allows the creation
 * of application-specific data types within a Java primitive such as
 * email address, latitude, etc.
 * @author Material Gain
 * @since 2104 03
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface DIM {
  public  String value() default "";
  boolean required() default false;
  boolean notUserVisible() default false;
}
