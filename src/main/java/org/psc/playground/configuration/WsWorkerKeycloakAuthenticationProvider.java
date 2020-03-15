package org.psc.playground.configuration;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
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
public class WsWorkerKeycloakAuthenticationProvider extends KeycloakAuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) authentication;

        String userId = null;

        // get the username from a custom claim with key userId within the access token
        // NOTE: the principal name can not be from a custom claim, it has to be one of the following claims:
        // sub, preferred_username, email, name, nickname, given_name, family_name
        // see: https://www.keycloak.org/docs/latest/securing_apps/#_java_adapter_config -> principal-attribute
        if (token.getPrincipal() instanceof KeycloakPrincipal) {
            //noinspection unchecked
            KeycloakPrincipal<KeycloakSecurityContext> keycloakPrincipal =
                    (KeycloakPrincipal<KeycloakSecurityContext>) token.getPrincipal();
            userId = (String) keycloakPrincipal.getKeycloakSecurityContext().getToken().getOtherClaims().get("userId");
        }

        if (StringUtils.isBlank(userId)) {
            userId = ((Principal) token.getPrincipal()).getName();
        }

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
