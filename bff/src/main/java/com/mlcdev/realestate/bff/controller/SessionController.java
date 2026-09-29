package com.mlcdev.realestate.bff.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SessionController {

    @GetMapping("/api/bff/session")
    public SessionInfoDTO findSessionInfo(@AuthenticationPrincipal OidcUser user){
        if(user == null) return new SessionInfoDTO(false , null, null);
        List<String> authorities = user.getIdToken().getClaimAsStringList("authorities");
        return new SessionInfoDTO(true, user.getName(), authorities);
    }


    public record SessionInfoDTO(boolean authenticated, String username, List<String> authorities){}
}
