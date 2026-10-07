package ExpertConnect.security;

import ExpertConnect.entity.Role;

/**
 * The authenticated principal placed in the SecurityContext by the JWT filter.
 * Controllers read the caller's identity from here instead of a client-supplied
 * header.
 */
public record AuthUser(Long id, String email, Role role) {
}
