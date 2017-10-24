package asst.dim;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Map a list of roles to the most permissive mask in the set of roles
 * @author Material Gain
 * @since 2014 03
 *
 */
public class RoleToMask {

  /** Map DIM names to arrays of objects which map roles to masking methods.
   * The arrays are arranged in order from most permissive to lease
   * permissive.  The ordering depends on the mask. */
  public static Map<String, RoleToMask[]> ROLE_MASKS =
    new HashMap<String, RoleToMask[]>();
  /**
   * Method which is used to forbid the data from being seen when no role
   * matches.
   */
  public static Method maskForbid;

  static {
    try {
//      RoleToMask[] rRay;
      Class<?>[] params = {String.class};
      maskForbid = RoleToMask.class.getMethod("maskSeeNothing", params);
/*      rRay    = new RoleToMask[4];
	rRay[0] = new RoleToMask("role3",
	MaskDIMs.class.getMethod("maskSeeAll", params));
	rRay[1] = new RoleToMask("role2",
	MaskDIMs.class.getMethod("maskSeeLots", params));
	rRay[2] = new RoleToMask("role1",
	MaskDIMs.class.getMethod("maskSeeLess", params));
	rRay[3] = new RoleToMask("role0",
	MaskDIMs.class.getMethod("maskSeeNothing", params));
	ROLE_MASKS.put("DataMeaning", rRay);
*/    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * @param cc input string to be masked
   * @return null so that the caller knows that the user is not
   * permitted to see it at all
   */
  public static String maskSeeNothing(String cc) {
    return null;
  }

  /**
   * @param dim name of the meaning of the datum to be masked
   * @param roleInfo information about the various roles
   * @return method to take a string and mask a string or null if
   * there is no masking for the DIM.  The caller then skips masking.
   */
  public static Method findMaskerForDimAndRoles(String dim,
      	IRoleInfo roleInfo) {
    RoleToMask[] roleMasks = ROLE_MASKS.get(dim);
    if (roleMasks == null) { return null; } //  No masking for this dim

    if ("*".equals(roleMasks[0].role)) {
      return roleMasks[0].masker; // This masker applies to all user roles
    }

    Set<String> userRoles;
    if ((roleInfo == null) ||
	( (userRoles = roleInfo.getUserRoles()) == null)) {
      return null;		// no masking
    }
    /* Roles are sorted in order of descending privilege */
    for (RoleToMask rm : roleMasks) {
      if (userRoles.contains(rm.role)) { return rm.masker; }
    }
    return maskForbid;		// Forbid the field if no role matches
  }

  /**
   * The name of a role to be associated with a data masker
   */
  public String role;
  /**
   * The masking method associated with the role
   */
  public Method masker;
  /**
   * @param role name of the role
   * @param masker method to mask the string
   */
  public RoleToMask(String role, Method masker) {
    this.role   = role;
    this.masker = masker;
  }
}
