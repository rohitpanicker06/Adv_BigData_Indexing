package com.example.demo1.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OAuthFilter extends OncePerRequestFilter {

    private static final String CLIENT_ID ="5972497234-mcpao55aq81n0568t147pnlf13rbk6ge.apps.googleusercontent.com";

    private static final GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
            .setAudience(Collections.singletonList(CLIENT_ID))
            .build();

    private void generateNoAuthResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
        return;

    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
           generateNoAuthResponse(response, "No Auth token is present");
           return;
        }

        String token = authHeader.substring(7);

        try {
            if (validateToken(token)) {
                filterChain.doFilter(request, response);
            } else {
                generateNoAuthResponse(response, "Invalid Id Token, Token might be expired");
            }
        } catch (GeneralSecurityException e) {
            generateNoAuthResponse(response, "Invalid Id Token");
        }
    }

    private boolean validateToken(String token) throws GeneralSecurityException, IOException {

        GoogleIdToken token1 = verifier.verify(token);
        return token1 != null;
    }


}
