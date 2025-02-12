package com.example.tutoring.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.tutoring.dto.MemberDto;
import com.example.tutoring.entity.Member;
import com.example.tutoring.jwt.JwtTokenProvider;
import com.example.tutoring.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class OAuthService {
	
	@Autowired
	private RestTemplate restTemplate;
    
	@Autowired
	private final MemberRepository memberRepository;
    
	@Autowired
	private final JwtTokenProvider jwtTokenProvider;
	
	@Value("${spring.security.oauth2.client.registration.naver.client-id}")
	private String clientId;
	
	@Value("${spring.security.oauth2.client.registration.naver.client-secret}")
	private String clientSecret;
	
	
	public ResponseEntity<Map<String,Object>> naverLogin(Map<String,Object> loginData)
	{
		Map<String,Object> responseMap = new HashMap<String, Object>();
		
		try {
			String code = loginData.get("code").toString();
	        String state = loginData.get("state").toString();
	        
	        
	        String tokenUrl = "https://nid.naver.com/oauth2.0/token" +
	                "?grant_type=authorization_code" +
	                "&client_id="+clientId +
	                "&client_secret="+clientSecret+
	                "&code=" + code +
	                "&state=" + state;
	        
	        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, null, Map.class);
	        String naverAccessToken = (String) tokenResponse.getBody().get("access_token");
			
	        HttpHeaders headers = new HttpHeaders();
	        headers.set("Authorization", "Bearer " + naverAccessToken);
	        HttpEntity<String> entity = new HttpEntity<>(headers);

	        ResponseEntity<Map> userResponse = restTemplate.exchange(
	                "https://openapi.naver.com/v1/nid/me",
	                HttpMethod.GET,
	                entity,
	                Map.class
	        );
	        
	        Map<String, Object> naverUser = (Map<String, Object>) userResponse.getBody().get("response");
	        String email = (String) naverUser.get("email");
	        
	        Member member = memberRepository.findByMemberId(email);
	        
	        if (member == null) {   	
	        	MemberDto memberDto = MemberDto.builder()
	                    .loginType(2)
	                    .memberId(email)
	                    .email(email)
	                    .password("naverPw")
	                    .nickname(createNick())
	                    .introduction("안녕하세요") 
	                    .build();
	        	       	
	        	member = memberRepository.saveAndFlush(Member.toEntity(memberDto));
	        }
			
	        String accessToken = jwtTokenProvider.createAccessToken(Integer.toString(member.getMemberNum()));
	        jwtTokenProvider.createRefreshToken(Integer.toString(member.getMemberNum()));
	                        
	        responseMap.put("memberNum", member.getMemberNum());
	        responseMap.put("loginType", member.getLoginType());
	        responseMap.put("nickname", member.getNickname());
	        responseMap.put("profileImg", member.getProfileImg());
	        responseMap.put("introduction", member.getIntroduction());
	        responseMap.put("access", accessToken);
	        responseMap.put("hasNotice", false);
	                                      
			return ResponseEntity.status(HttpStatus.OK).body(responseMap);
			
		}catch(Exception e)
		{			
			responseMap.put("message", e.getMessage());			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
		}
				
	}
	
	
	
	
	  private String createNick() {

	        LocalDate now = LocalDate.now();
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

	        Random random = new Random();
	        int randNick =  1000 + random.nextInt(9000);

	        return now.format(formatter) + randNick;
	    }  
}
