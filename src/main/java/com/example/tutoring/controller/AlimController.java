package com.example.tutoring.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.example.tutoring.service.AlimService;
import com.example.tutoring.type.AlimType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/alim")
public class AlimController {

	@Autowired
	AlimService alimService;
	
	@GetMapping(value="/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter subscribe(@RequestParam("memberNum") Integer memberNum) {		
		log.info(memberNum+"번 회원 연결 요청");
		
		return alimService.subscribe(memberNum);
	}
			
	@PatchMapping("/read")
	public ResponseEntity<Map<String,Object>> read(@RequestBody Map<String, Object> alimData)
	{
		return alimService.read(alimData);
	}
	
	@DeleteMapping("/delete")
	public ResponseEntity<Map<String,Object>> delete(@RequestParam("alimNum") int alimNum)
	{
		return alimService.delete(alimNum);
	}
	
	@GetMapping("/list")
	public  ResponseEntity<Map<String,Object>> list(@RequestParam(value="observer", required = false)Integer observer, HttpServletRequest request)
	{
		String accessToken = request.getHeader("Authorization").substring(7);
		log.info("엑세스 토큰 : "+accessToken);
		
		return alimService.list(observer, accessToken);
	}
	
	@GetMapping("/send")
	public ResponseEntity<Map<String,Object>> send(@RequestParam(value="memberNum") Integer memberNum)
	{
		Map<String,Object> response = new HashMap<String, Object>();
		try {
			
			alimService.sendAlim(memberNum, "알림 전송 테스트", AlimType.TYPE_FOLLOW);
			response.put("message", "success");
			
			return ResponseEntity.status(HttpStatus.OK).body(response);
		}catch(Exception e)
		{
			response.put("message", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
			
	}

}