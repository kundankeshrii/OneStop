package com.project5.OneStop.auth.oauth2;

import com.project5.OneStop.auth.entity.AuthProvider;
import com.project5.OneStop.auth.entity.User;
import com.project5.OneStop.auth.repository.UserRepository;
import com.project5.OneStop.auth.security.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oauth2User =
                (OAuth2User) authentication.getPrincipal();

        String email =
                oauth2User.getAttribute("email");

        String name =
                oauth2User.getAttribute("name");

        String picture =
                oauth2User.getAttribute("picture");

        String providerId =
                oauth2User.getAttribute("sub");

        User user =
                userRepository.findByEmail(email)
                        .orElseGet(User::new);

        user.setName(name);
        user.setEmail(email);
        user.setProvider(AuthProvider.GOOGLE);
        user.setProviderId(providerId);
        user.setProfileImage(picture);

        /*
         * Google users don't need a local password.
         */
        user.setPassword(null);

        user = userRepository.save(user);

        String token =
                jwtService.generateToken(user);

        /*
         * For now we expose the token as a query parameter
         * so we can test the complete OAuth2 flow.
         *
         * We will replace this with a safer frontend handoff
         * when we connect the final OneStop frontend.
         */
        response.sendRedirect(
                "http://127.0.0.1:5500/oauth2-success.html?token="
                        + token
        );
    }
}