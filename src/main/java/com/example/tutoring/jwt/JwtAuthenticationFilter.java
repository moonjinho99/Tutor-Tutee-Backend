package com.example.tutoring.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.tutoring.repository.MemberRepository;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private MemberRepository memberRepository;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        
    	String token = resolveToken(request);
        
        	if (token != null) {
            
            var tokenCheck = jwtTokenProvider.isAccessTokenExpired(token);
            
            if ((int)tokenCheck.get("check") == 0) {  
            	
                String memberNum = jwtTokenProvider.getMemberNum(token);
                try {
                    String newAccessToken = jwtTokenProvider.reissueAccessToken(Integer.parseInt(memberNum));
                    response.setHeader("newAccessToken", newAccessToken); 
                } catch (RuntimeException e) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);  
                    response.getWriter().write("리프레시 토큰이 만료되었습니다. 다시 로그인해주세요.");
                    return;
                }
            } else if (jwtTokenProvider.validateToken(token)) {  
                int memberNum = Integer.parseInt(jwtTokenProvider.getMemberNum(token));
                
                String memberId = memberRepository.findMemberIdByMemberNum(memberNum);

                UserDetails userDetails = customUserDetailsService.loadUserByUsername(memberId);  // memberNum으로 UserDetails 조회

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                
                SecurityContextHolder.getContext().setAuthentication(authentication);  // 인증 설정
            }
        }

        filterChain.doFilter(request, response);  
    }


    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  
        }
        return null;
    }
}

