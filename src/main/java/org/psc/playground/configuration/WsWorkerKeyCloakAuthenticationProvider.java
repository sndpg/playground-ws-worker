package org.psc.playground.configuration;

import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Component
public class WsWorkerKeyCloakAuthenticationProvider extends KeycloakAuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) authentication;

        var userId = ((Principal) token.getPrincipal()).getName();

        // get real permissions as grantedAuthorities from somewhere
        List<GrantedAuthority> grantedAuthorities = retrieveUserPermissions(userId);

        return new KeycloakAuthenticationToken(token.getAccount(), token.isInteractive(), grantedAuthorities);
    }

    /**
     * This method should normally access some kind of user database which stores the actual permissions of a user
     * account.
     *
     * @param userId
     * @return
     */
    private List<GrantedAuthority> retrieveUserPermissions(String userId) {

        List<GrantedAuthority> grantedAuthorities;
        if ("sandro".equalsIgnoreCase(userId)) {
            grantedAuthorities = AuthorityUtils.createAuthorityList("USER", "ADMIN");
        } else {
            grantedAuthorities = new ArrayList<>();
        }
        return grantedAuthorities;

    }

}
