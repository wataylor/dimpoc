package asst.dim;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utilities associated with masking data based on user roles.  Masking
 * requires changing the value of a JSON object attribute.
 * @author Material Gain
 * @since 2014 02
 */
public class MaskDIMs {

  /**
   * Mask object field values based on DIM annotations.  A mask may
   * change the value of a field in the JSON object if the field us
   * visible.  This method assumes that the values have been validated
   * and that the user has corrected any invalid values.
   * @param o object whose annotations specify how to send its fields
   * to the use and how to mask them.
   * @param uo Information about the user roles
   * @return Masked JSON object based on DIM annotations and the user
   * roles
   * @throws Exception when things go wrong.
   */
  public static JSONObject maskObject(Object o, IRoleInfo uo) throws Exception {
    Class<?> clazz = o.getClass();
    JSONObject jobj = pojoToJson(o);
    DIM dim;
    String m; // The meaning of the field
    Object fieldValue;

    for (Field fld : clazz.getFields()) {
      if ( (dim = fld.getAnnotation(DIM.class)) == null) {
	continue;
      }
      if (dim.notUserVisible()) { continue; }
      m = dim.value();
      fieldValue = fld.get(o);
      if ((fieldValue == null) ||
	  (fieldValue.toString().length() <= 0)) {
	/* A null or empty string is always masked correctly.*/
	continue;
      }
      /* At this point, the field is known to have a value which must be
       * masked in the JSON object. */
      maskFieldValue(fld, o, m, jobj, uo);
    }
    return jobj;
  }

  /**
   * Mask one specific field if it has a masking method.  It should be
   * called only on fields whose DIM annotation does not declare that
   * they are not visible.  If a visible field does not have a masking
   * method declared, it is assumed not to require masking and the
   * data are passed to the user without change.  Only strings can be
   * masked.
   * @param fld the POJO field
   * @param o the object where the file is found
   * @param m the name of the DIM - email, phone, etc.
   * @param jobj the JSON object whose value may need to be changed
   * @param uo information about the user roles
   * @return true if a value was changed, false otherwise
   * @throws Exception when things go wrong
   */
  public static boolean maskFieldValue(Field fld, Object o, String m,
				       JSONObject jobj, IRoleInfo uo)
    throws Exception {
    Method masker = RoleToMask.findMaskerForDimAndRoles(m, uo);
    if (masker == null) { return false; } // no mask, no change

    String value = fld.get(o).toString();
    if ((value == null) || (value.length() <= 0)) { return false; }

    String maybeNewValue = (String)masker.invoke(null, value);
    if (!value.equals(maybeNewValue)) {
      /* Field value was changed by the masking routine, must replace
       * it in the JSON object. */
      String jsonAttr = getJsonAttr(fld);
      if (jsonAttr == null) {
	throw new RuntimeException("Cannot find JSON attribute for DIM " + m +
				   " user-visible field " + fld.getName());
      }
      /* Putting a null value removes it from the JSON object */
      jobj.put(jsonAttr,  maybeNewValue);
      return true;
    }
    return false;
  }

  /**
   * Convert an annotated POJO to a JSON object.
   * @param o object to be converted to a JSON object based on annotations
   * @return JSON object
   * @throws Exception when things go wrong
   */
  public static JSONObject pojoToJson(Object o) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    mapper.writeValue(baos, o);
    JSONParser parser = new JSONParser();
    return (JSONObject) parser.parse(baos.toString());
  }

  /**
   * @param fld field from a POJO
   * @return the name of the field in the JSON object or null
   */
  public static String getJsonAttr(Field fld) {
    JsonProperty jp = fld.getAnnotation(JsonProperty.class);
    if (jp == null) { return null; }
    String name = jp.value();
    if ((name != null) && (name.length() > 0)) { return name; }
    return fld.getName();
  }

  /**
   * @param datum value from the field
   * @return null because the user may not see it
   */
  public static String maskSeeNothing(String datum) {
    return null;
  }

  /**
   * @param datum value from the field
   * @return the string because the user may see it all
   */
  public static String maskSeeAll(String datum) {
    return datum;
  }

}
