package com.example.tutoring.service;

import com.example.tutoring.dto.MemberDto;
import com.example.tutoring.entity.Member;
import com.example.tutoring.jwt.CustomUserDetails;
import com.example.tutoring.jwt.JwtTokenProvider;
import com.example.tutoring.repository.MemberRepository;
import com.example.tutoring.repository.RefreshTokenRespository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

	@Autowired
    private AuthenticationManager authenticationManager;

	@Autowired
    private MemberRepository memberRepository;

	@Autowired
	private RefreshTokenRespository refreshTokenRespository;

	@Autowired
    private JavaMailSender mailSender;

	private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public int checkNum;

    // 회원가입 처리
    public ResponseEntity<Map<String, Object>> signUp(Map<String, Object> memberData) {
    
        String newNick = createNick();
        Map<String, Object> responseMap = new HashMap<>();

        try {
            // 중복 아이디가 있을 경우
            if (checkMemberId(memberData.get("memberId").toString())) {
                responseMap.put("message", "이미 존재하는 아이디입니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
            }

            MemberDto memberDto = MemberDto.builder()
                    .loginType(0)
                    .memberId(memberData.get("memberId").toString())
                    .email(memberData.get("email").toString())
                    .password(passwordEncoder.encode(memberData.get("password").toString()))
                    .nickname(newNick)
                    .introduction("안녕하세요")  // DB 설정대로 출력되도록 수정 필요
                    .build();

            Member member = Member.toEntity(memberDto);
            memberRepository.save(member);
            return ResponseEntity.status(HttpStatus.OK).body(responseMap);
        } catch (Exception e) {
            responseMap.put("messsage", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
        }


    }

    // 아이디 중복 확인
    public boolean checkMemberId(String memberId) {

        return memberRepository.existsByMemberId(memberId);
    }

    // 닉네임 생성
    public String createNick() {

        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        Random random = new Random();
        int randNick =  1000 + random.nextInt(9000);

        return now.format(formatter) + randNick;
    }

    //로그인
    public ResponseEntity<Map<String,Object>>login(Map<String, Object> loginData)
    {
    	Map<String,Object> responseMap = new HashMap<String, Object>();

    	String memberId = loginData.get("memberId").toString();
    	String password = loginData.get("password").toString();

    	try {

    		UsernamePasswordAuthenticationToken authenticationToken =
    	            new UsernamePasswordAuthenticationToken(memberId, password);
	        Authentication authentication = authenticationManager.authenticate(authenticationToken);

	        SecurityContextHolder.getContext().setAuthentication(authentication);

	        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
	        Member member = customUserDetails.getMember();

        	if(!passwordEncoder.matches(password, member.getPassword())) {
        		responseMap.put("message", "아이디와 패스워드가 일치하지 않습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
        	}

        	String memberNumString = Integer.toString(member.getMemberNum());

        	String accessToken = jwtTokenProvider.createAccessToken(memberNumString);
            String refreshToken = jwtTokenProvider.createRefreshToken(memberNumString);


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
    		responseMap.put("message", "아이디 또는 비밀번호가 일치하지 않습니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
    	}

    }

    //토큰 체크
    public ResponseEntity<Map<String,Object>> tokenCheck(String accessToken)
    {
    	Map<String,Object> responseMap = new HashMap<String,Object>();

    	try {

    		Map<String,Object> result = jwtTokenProvider.isAccessTokenExpired(accessToken);
    		int check = (int)result.get("check");
    		//엑세스 토큰이 만료되었을 경우
        	if(check == 0)
        	{
        		int memberNum = Integer.parseInt(jwtTokenProvider.getMemberNum(accessToken));
        		responseMap.put("access", jwtTokenProvider.reissueAccessToken(memberNum));
        		return ResponseEntity.status(HttpStatus.OK).body(responseMap);
        	}
        	//엑세스 토큰이 만료되지 않았을 경우
        	else if(check == 1) {
        		responseMap.put("message", "엑세스 토큰이 유효합니다.");
        		return ResponseEntity.status(HttpStatus.OK).body(responseMap);
        	}
        	else{
        		responseMap.put("message", "유효하지 않은 토큰입니다.");
        		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
        	}

    	}catch(Exception e)
    	{
    		responseMap.put("message", e.getMessage());
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
    	}

    }

    // 인증번호 전송
    public ResponseEntity<Map<String, Object>> sendEmail(@RequestBody String email) {

        Map<String, Object> responseMap = new HashMap<>();

        boolean checkEmail = memberRepository.existsByEmail(email);

        if (checkEmail) {
            responseMap.put("message", "중복된 이메일입니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
        } else {
            // 인증번호 생성
            Random random = new Random();
            int checkNum = 100000 + random.nextInt(900000);
            log.info("{}", checkNum);

            // 인증번호 전송
            sendCheckNum(email, checkNum);
            responseMap.put("checkNum", checkNum);

            this.checkNum = checkNum;

            return ResponseEntity.status(HttpStatus.OK).body(responseMap);
        }
    }

    // 인증번호 전송
    private void sendCheckNum(String email, int checkNum) {
        String subject = "Tutor-Tuti: 이메일 인증번호";

        String text = "<html><body>" +
                "<div class='container' style='max-width: 600px; margin: 0 auto; padding: 20px; background-color: #fff; border-radius: 8px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);'>" +
                "<div class='header' style='text-align: center; color: #1a73e8; font-size: 24px; font-weight: bold; margin-bottom: 20px;'>Tutor-Tuti 이메일 인증</div>" +
                "<div class='content' style='font-size: 16px; color: #555; margin-bottom: 20px;'>" +
                "<p>안녕하세요! 아래 인증번호를 입력하여 이메일 인증을 완료해 주세요.</p>" +
                "<p><strong>인증번호: " + checkNum + "</strong></p>" +
                "<p>감사합니다!<br> - Tutor-Tuti 팀</p>" +
                "</div>" +
                "<div class='footer' style='text-align: center; font-size: 14px; color: #888;'>이 이메일은 자동으로 발송되었습니다. <br>문의사항이 있으시면 support@tutor-tuti.com 으로 연락주세요.</div>" +
                "</div>" +
                "</body></html>";


        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);  // true로 설정하면 HTML 지원

            messageHelper.setTo(email);
            messageHelper.setSubject(subject);
            messageHelper.setText(text, true);  // 두 번째 인자는 HTML 사용 여부

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 인증번호 확인
    public ResponseEntity<Map<String, Object>> checkEmail(int inputCheckNum) {

        Map<String, Object> responseMap = new HashMap<>();

        if (this.checkNum == inputCheckNum) {
            return ResponseEntity.status(HttpStatus.OK).body(responseMap);
        } else {
            responseMap.put("message", "인증번호가 다릅니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
        }
    }

    // 아이디 찾기
    public ResponseEntity<Map<String, Object>> findId(String email) {

        Optional<Member> memberOptional = memberRepository.findByEmail(email);

        Map<String, Object> responseMap = new HashMap<>();

        if (memberOptional.isPresent()) {
            if (memberOptional.get().getLoginType() == 0) {
                String memberId = memberOptional.get().getMemberId();
                String maskedId = maskedMemberId(memberId); // 가려진 아이디 생성

                responseMap.put("memberId", maskedId);
                return ResponseEntity.status(HttpStatus.OK).body(responseMap);
            } else {
                responseMap.put("message", "해당하는 유저가 없습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
            }
        } else {
            responseMap.put("message", "해당하는 유저가 없습니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
        }
    }

    // 비밀번호 찾기
    public ResponseEntity<Map<String, Object>> findPassword(String memberId) {

        Member member = memberRepository.findByMemberId(memberId);

        Map<String, Object> responseMap = new HashMap<>();

        if (member != null) {
            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            responseMap.put("message", "해당하는 유저가 없습니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
        }
    }

    // 아이디 가림 처리
    public String maskedMemberId(String memberId) {

        String prefix = memberId.substring(0, 2); // 앞 2글자
        String suffix = memberId.substring(memberId.length() - 2); // 뒤 2글자
        String masked = "*".repeat(memberId.length() - 4); // 중간 부분 *

        return prefix + masked + suffix;
    }


    //로그아웃
    public ResponseEntity<Map<String,Object>> logout(String accessToken) {
    	 Map<String, Object> responseMap = new HashMap<>();
    	 try {

    		 if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
    		        String memberNum = jwtTokenProvider.getMemberNum(accessToken);
    		        refreshTokenRespository.deleteById(Integer.parseInt(memberNum));
    		        responseMap.put("message", "로그아웃 성공");

    		        return ResponseEntity.status(HttpStatus.OK).body(responseMap);
    		    }

    		    responseMap.put("message", "엑세스 토큰이 유효하지 않습니다.");
    	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);

    	 }catch(Exception e)
    	 {
    		 responseMap.put("message", e.getMessage());
 	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
    	 }

	}


    @Transactional
    public ResponseEntity<Map<String,Object>> outMember(String accessToken)
    {
    	 Map<String, Object> responseMap = new HashMap<>();
    	 try {

    		 if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
    		        String memberNum = jwtTokenProvider.getMemberNum(accessToken);
    		        refreshTokenRespository.deleteById(Integer.parseInt(memberNum));
    		        memberRepository.deleteById(Integer.parseInt(memberNum));

    		        responseMap.put("message", "회원탈퇴 성공");

    		        return ResponseEntity.status(HttpStatus.OK).body(responseMap);
    		    }

    		    responseMap.put("message", "엑세스 토큰이 유효하지 않습니다.");
    	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);

    	 }catch(Exception e)
    	 {
    		 responseMap.put("message", e.getMessage());
 	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
    	 }
    }

}
