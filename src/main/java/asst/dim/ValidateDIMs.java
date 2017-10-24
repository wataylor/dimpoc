package asst.dim;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities associated with validating data formats and content
 * @author Material Gain
 * @since 2014 03
 */
public class ValidateDIMs {

  /**
   * Maps the name of a validator to a method that validates and reformats
   * fields containing that data type.
   */
  public static Map<String, Method> validators =
    new HashMap<String, Method>();
  static {
    try {
      Class<?>[] params = {Field.class, Object.class, StringBuilder.class};
      validators.put("URL", ValidateDIMs.class.getMethod("validateURL", params));
      validators.put("email", ValidateDIMs.class.getMethod("validateEmail", params));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Accumulate a complaint into a text string which is suitable for an
   * alert.
   * @param sb accumulator
   * @param whinge text of the current complaint
   */
  public static void whingeSB(StringBuilder sb, String whinge) {
    if (sb.length() > 0) { sb.append("\n"); }
    sb.append(whinge);
  }

  /**
   * Validate an object field values based on DIM annotations.  A validation
   * may change the value of a field by reformatting it to be in canonical
   * format.
   * @param o object whose annotations specify how to validate its fields.
   * @return Error messages or null if there are no errors in the object.
   * @throws Exception when things go wrong.
   */
  public static String validateObject(Object o) throws Exception {
    Class<?> clazz = o.getClass();
    StringBuilder sb = new StringBuilder();
    DIM dim;
    String m; // The meaning of the field
    Object fieldValue;
    boolean required;
    for (Field fld : clazz.getFields()) {
      if ( (dim = fld.getAnnotation(DIM.class)) == null) {
	continue;
      }
      m = dim.value();
      required = dim.required();
      fieldValue = fld.get(o);
      if (required && ((fieldValue == null) ||
		       (fieldValue.toString().length() <= 0))) {
	whingeSB(sb, "Required field " + fld.getName() + " has no value.");
	continue;
      }
      if ((fieldValue == null) ||
	  (fieldValue.toString().length() <= 0)) {
	/* The field is not required, but it may be null or empty.  A null
	 * or empty string is always formatted correctly.*/
	continue;
      }
      /* At this point, the field is known to have a value which must be
       * validated. */
      validateFieldValue(fld, o, m, sb);
    }
    return (sb.length() <= 0 ? null : sb.toString());
  }

  /**
   * Examine one field to validate it.
   * @param fld One field from a Java object.  The field value may be
   * replaced to get it into canonical form.
   * @param o the object that owns the field
   * @param meaning string which defines its meaning, as in URL, email, etc.
   * @param sb accumulates error messages
   * @throws Exception when things go wrong
   */
  public static void validateFieldValue(Field fld, Object o, String meaning,
					StringBuilder sb) throws Exception {
    Method meth = validators.get(meaning);
    if (meth == null) {
      throw new RuntimeException("Mising validator for data meaning " +
				 meaning);
    }
    meth.invoke(null, fld, o, sb);
  }

  /**
   * Validate and / or reformat a field that is supposed to contain a URL
   * @param fld the field whose value will be changed if it does not start
   * with http or if it contains non-URL characters
   * @param o object containing the field
   * @param sb accumulator for error messages
   * @return true if the string is a good URL
   * @throws Exception when things go wrong
   */
  public static boolean validateURL(Field fld, Object o, StringBuilder sb)
    throws Exception {
    if (fld.getGenericType() != String.class) {
      throw new RuntimeException("Field " + fld.getName() +
				 " has a URL annotation, but it is not a String field.");
    }
    String value = fld.get(o).toString();
    String maybeNewValue = value.trim().replace("\"", "");
    if (!"http".equalsIgnoreCase(maybeNewValue.substring(0, 4))) {
      maybeNewValue = "http://" + maybeNewValue;
    }
    if (!value.equals(maybeNewValue)) {
      fld.set(o, maybeNewValue);
    }
    @SuppressWarnings("unused")
      URL oil;
    try {
      oil = new URL(maybeNewValue);
    } catch (Exception e) {
      whingeSB(sb, maybeNewValue + " is a malformed URL " + e.getMessage());
      return false;
    }
    return true;
  }

  /**
   * Pattern to match email addresses
   */
  public static final Pattern emailAddress =
    Pattern.compile("(?:(?:\\r\\n)?[ \\t])*(?:(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*)|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*:(?:(?:\\r\\n)?[ \\t])*(?:(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*)(?:,\\s*(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*))*)?;\\s*)");

  /**
   * Validate and / or reformat a field that is supposed to contain an email
   * address
   * @param fld the field whose value should be a valid email
   * @param o object containing the field
   * @param sb accumulator for error messages
   * @return true if the email was valid
   * @throws Exception when things go wrong
   */
  public static boolean validateEmail(Field fld, Object o, StringBuilder sb)
    throws Exception {
    return validateString(fld, o, sb, "email address", emailAddress);
  }

  /**
   * Validate and / or reformat a field that is supposed to store a string
   * that matches a regular expression
   * @param fld the field whose value should be a valid email
   * @param o object containing the field
   * @param sb accumulator for error messages
   * @param anno the name of the annotation type
   * @param regex the regular expression to match
   * @return true if the field was valid
   * @throws Exception when things go wrong
   */
  public static boolean validateString(Field fld, Object o, StringBuilder sb,
				       String anno, Pattern regex)
    throws Exception {
    if (fld.getGenericType() != String.class) {
      throw new RuntimeException("Field " + fld.getName() +
				 " is a " + anno + ", but is not a String.");
    }
    String value = fld.get(o).toString();
    if ((value == null) || (value.length() <= 0)) { return true; } // null matches everything
    Matcher m = regex.matcher(value);
    if (!m.matches()) {
      whingeSB(sb, value + " is not a valid " + anno + ".");
      return false;
    }
    return true;
  }

  @SuppressWarnings("javadoc")
  public static boolean validateInt(Field fld, Object o, StringBuilder sb,
				    String anno, int min, int max)
    throws Exception {
    Type type;
    if (( (type = fld.getGenericType()) != Integer.TYPE) &&
	type != Integer.class) {
      throw new RuntimeException("Field " + fld.getName() +
				 " is a " + anno + ", but is not an integer.");
    }
    int value = fld.getInt(o);
    if ((value < min) || (value > max)) {
      whingeSB(sb, value + " is not between " + min + " and " + max + ".");
      return false;
    }
    return true;
  }

  @SuppressWarnings("javadoc")
  public static boolean validateLong(Field fld, Object o, StringBuilder sb,
				     String anno, long min, long max)
    throws Exception {
    Type type;
    if (((type = fld.getGenericType()) != Long.TYPE) &&
	type != Long.class) {
      throw new RuntimeException("Field " + fld.getName() +
				 " is a " + anno + ", but is not a long.");
    }
    long value = fld.getLong(o);
    if ((value < min) || (value > max)) {
      whingeSB(sb, value + " is not between " + min + " and " + max + ".");
      return false;
    }
    return true;
  }

  @SuppressWarnings("javadoc")
  public static boolean validateFloat(Field fld, Object o, StringBuilder sb,
				      String anno, float min, float max)
    throws Exception {
    Type type;
    if (((type = fld.getGenericType()) != Float.TYPE) &&
	type != Float.class) {
      throw new RuntimeException("Field " + fld.getName() +
				 " is a " + anno + ", but is not a float.");
    }
    float value = fld.getFloat(o);
    if ((value < min) || (value > max)) {
      whingeSB(sb, value + " is not between " + min + " and " + max + ".");
      return false;
    }
    return true;
  }

  @SuppressWarnings("javadoc")
  public static boolean validateDouble(Field fld, Object o, StringBuilder sb,
				       String anno, double min, double max)
    throws Exception {
    Type type;
    if (((type = fld.getGenericType()) != Double.TYPE) &&
	type != Double.class) {
      throw new RuntimeException("Field " + fld.getName() +
				 " is a " + anno + ", but is not a double.");
    }
    double value = fld.getDouble(o);
    if ((value < min) || (value > max)) {
      whingeSB(sb, value + " is not between " + min + " and " + max + ".");
      return false;
    }
    return true;
  }
}
