package asst.dim;

import java.util.Set;

/**
 * Provide information about user roles.  The roles are not stored in
 * any particular order.  Each DIM is required to order them from
 * most permissive to least permissive
 * @author Material Gain
 * @since 4014 04
 */
public interface IRoleInfo {

  /**
   * @return set of unordered user role names.
   */
  public Set<String> getUserRoles();
}
