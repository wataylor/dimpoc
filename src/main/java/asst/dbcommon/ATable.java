package asst.dbcommon;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify the table name and primary key column name for a table row object.
 * It is assumed that the annotated class has fields which match the column
 * names which are returned by select *.
 * @author Material Gain
 * @since 2014 02
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ATable {
  /** Names the table */
  public String tableName() default "";
  /** Return the primary key column name.  The column name must match
   * the column name in an AColumn notation on a field in the class.  */
  public String primaryKeyColumn() default "";
}
