package org.example.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AuthorizationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if(request.getServletPath().equals("/user/login")) {
            filterChain.doFilter(request, response);
        }else{
            String header = request.getHeader("Authorization");
            if(header != null && header.startsWith("Bearer ")){
                try{
                    Algorithm algorithm = Algorithm.HMAC256("secret".getBytes()); //!!
                    String token = header.substring(7);
                    JWTVerifier verifier = JWT.require(algorithm).build();
                    DecodedJWT decodedJWT = verifier.verify(token);
                    String username = decodedJWT.getSubject();

                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(username, null, null);
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    filterChain.doFilter(request, response);
                }catch (Exception e){
                    Map<String, String> map = new HashMap<>();
                    map.put("error", e.getMessage());
                    response.setStatus(401);
                    response.setContentType("application/json");
                    new ObjectMapper().writeValue(response.getOutputStream(), map);
                }
            }else filterChain.doFilter(request, response);
        }

    }
}
