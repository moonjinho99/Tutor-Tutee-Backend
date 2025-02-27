package com.example.tutoring.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.tutoring.dto.AlimDto;
import com.example.tutoring.entity.Alim;
import com.example.tutoring.jwt.JwtTokenProvider;
import com.example.tutoring.repository.AlimRepository;
import com.example.tutoring.type.AlimType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlimService {
	
	@Autowired
	private AlimRepository alimRepository;
	
	@Autowired
	private EntityManager entityManager;
	
	@Autowired
	private JwtTokenProvider jwtTokenProvider;
	
	private final Map<Integer, SseEmitter> emitters = new ConcurrentHashMap<Integer, SseEmitter>();
	
	private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; 
	
	public SseEmitter subscribe(Integer memberNum) {
		SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
		emitters.put(memberNum, emitter);
		
		emitter.onCompletion(() -> emitters.remove(memberNum));
		emitter.onTimeout(() -> emitters.remove(memberNum));
		
		return emitter;
	}
	
	public void sendAlim(Integer memberNum, String alimMsg, AlimType alimType) {			
		AlimDto alimDto = AlimDto.builder()
						.memberNum(memberNum)
						.alimMsg(alimMsg)
						.alimType(alimType)
						.sendTime(new Date())
						.isRead(false)
						.build();
		
		alimRepository.save(Alim.toEntity(alimDto));
		
		if(emitters.containsKey(memberNum)) {
			SseEmitter emitter = emitters.get(memberNum);
			
			try {
				emitter.send(SseEmitter.event().data(alimDto));
			} catch(IOException e) {
				emitters.remove(memberNum);
			}
		}
	}

	@Transactional
	public ResponseEntity<Map<String,Object>> read(Map<String,Object> alimData)
	{
		Map<String,Object> response = new HashMap<String, Object>();
		
		try {
			int alimNum = (int)alimData.get("alimNum");
			alimRepository.readAlim(new Date(), true, alimNum);
			entityManager.flush();
			entityManager.clear();
	
			Optional<Alim> updateAlim = alimRepository.findById(alimNum);
			response.put("alim", AlimDto.toDto(updateAlim.get()));
			
			return ResponseEntity.status(HttpStatus.OK).body(response);
		}catch(Exception e)
		{
			response.put("message", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
								
	}
	
	@Transactional
	public ResponseEntity<Map<String,Object>> delete(int alimNum)
	{
		Map<String,Object> response = new HashMap<String, Object>();
		
		try {
			alimRepository.deleteById(alimNum);
			
			response.put("message", "success");
			return ResponseEntity.status(HttpStatus.OK).body(response);
		} catch(Exception e)
		{
			response.put("message", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
	}
	
	public ResponseEntity<Map<String,Object>> list(String accessToken)
	{		
		Map<String,Object> response = new HashMap<String, Object>();	
		
		try {
			int memberNum = Integer.parseInt(jwtTokenProvider.getMemberNum(accessToken));
			List<AlimDto> alimList = new ArrayList<>();
			
			for(Alim alim : alimRepository.findAlimList(memberNum))
			{
				alimList.add(AlimDto.toDto(alim));
			}
			
			response.put("alimList", alimList);
						
			return ResponseEntity.status(HttpStatus.OK).body(response);
		}catch(Exception e)
		{
			response.put("message", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}		
		
	}
}
