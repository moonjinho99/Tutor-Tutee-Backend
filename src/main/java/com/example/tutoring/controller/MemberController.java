package com.example.tutoring.controller;

import com.example.tutoring.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;


@Slf4j
@Controller
@RequestMapping("/member")
public class MemberController {
	
	@Autowired
	private MemberService memberService;

	@RequestMapping("/loginPage")
	public void InitLoginPage()
	{
		log.info("로그인 페이지 진입");
	}	

	// 회원가입 기능
	@PostMapping("/signup")
	public ResponseEntity<Map<String, Object>> signUp (@RequestBody Map<String, Object> memberData) {

		log.info("----/member/signup API 진입-----");
		log.info("회원가입 요청 데이터: {}", memberData);

		// 회원가입 처리 서비스 호출
		return memberService.signUp(memberData);
	}
	//로그인
	@PostMapping("/login")
	public ResponseEntity<Map<String,Object>> login(@RequestBody Map<String, Object> loginData){
		
		log.info("로그인 데이터 : "+loginData.toString());
		
		return memberService.login(loginData);
		
	}	
	
	@GetMapping("/naverLogin")
	public void naverLogin(Map<String,Object> data){
		log.info("네이버 로그인");
		log.info("값 : "+data.toString());
	}
	
	//토큰 검사
	@PostMapping("/tokenCheck")
	public ResponseEntity<Map<String, Object>> tokenCheck(HttpServletRequest request){
		log.info("----/member/tokenCheck API 진입----");
		String accessToken = request.getHeader("Authorization").substring(7);
		log.info("엑세스 토큰 : "+accessToken);
		
		return memberService.tokenCheck(accessToken);
	}
		
    // 이메일 인증번호 요청 기능
   	@PostMapping("/sendEmail")
   	@ResponseBody
	public ResponseEntity<Map<String, Object>> sendEmail(@RequestBody Map<String, Object> emailData) {

		String email = emailData.get("email").toString();

		log.info("이메일 요청 api 진입");

		return memberService.sendEmail(email);

	}

	// 이메일 인증번호 확인
	@GetMapping("/checkEmail")
	public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam int checkNum) {

		log.info("인증번호 확인 api 진입");

		return memberService.checkEmail(checkNum);
	}

	// 아이디 찾기
	@PostMapping("/findId")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> findId(@RequestBody Map<String, Object> emailData) {
		
		log.info("아이디 찾기 api 진입");
		log.info(emailData.toString());

		String email = emailData.get("email").toString();

		return memberService.findId(email);
	}

	// 비밀번호 찾기
	@PostMapping("/findPassword")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> findPassword(@RequestBody Map<String, Object> memberIdData) {

		log.info("비밀번호 찾기 api 진입");
		log.info(memberIdData.toString());

		String memberId	= memberIdData.get("memberId").toString();

		return memberService.findPassword(memberId);
	}

	//로그아웃
	@PostMapping("/logout")
	public ResponseEntity<Map<String,Object>> logout(HttpServletRequest request) {
		log.info("----/member/logout API 진입----");
		String accessToken = request.getHeader("Authorization").substring(7);
		log.info("엑세스 토큰 : "+accessToken);
		
	    return memberService.logout(accessToken);
	}
	
	//회원탈퇴
	@DeleteMapping("/outMember")
	public ResponseEntity<Map<String,Object>> outMember(HttpServletRequest request) {
		log.info("----/member/outMember API 진입----");
		String accessToken = request.getHeader("Authorization").substring(7);
		log.info("엑세스 토큰 : "+accessToken);
		
		return memberService.outMember(accessToken);
	}

}
