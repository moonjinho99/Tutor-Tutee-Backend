package com.example.tutoring.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;
import com.example.tutoring.dto.ChattingDto;
import com.example.tutoring.jwt.JwtTokenProvider;
import com.example.tutoring.repository.MemberRepository;
import com.example.tutoring.service.ChattingService;
import com.example.tutoring.type.ChattingType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
@RequiredArgsConstructor
public class StompDisconnectListener {

	private final SimpMessagingTemplate simpMessageingTemplate;	
	private final JwtTokenProvider jwtTokenProvider;
	
	@Autowired
	ChattingService chattingService;
	
	@Autowired
	MemberRepository memberRepository;
	
	@EventListener
	public void handleUnsubscribeEvent(SessionUnsubscribeEvent event) {
	    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

	    String destination = accessor.getDestination(); 
	    log.info("사용자가 채널 구독 해제: " + destination);

	    String accessToken = accessor.getFirstNativeHeader("Authorization").substring(7);
	    log.info("받은 Access Token: " + accessToken);

	    if (destination != null && destination.startsWith("/sub/")) {
	        Integer roomId = Integer.parseInt(destination.replace("/sub/", ""));
			String nickname = memberRepository.findNicknameByMemberNum(Integer.parseInt(jwtTokenProvider.getMemberNum(accessToken))).get().getNickname();

	        simpMessageingTemplate.convertAndSend(destination, getOutMessage(roomId, nickname));
	    }
	}

	
	@EventListener
	public void handleDisconnectEvent(SessionDisconnectEvent event) {
	    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
	    log.info("사용자가 WebSocket 연결 종료");

	    String destination = accessor.getDestination(); // 사용자가 구독 해제한 채널
	    String accessToken = accessor.getFirstNativeHeader("Authorization").substring(7);
	    log.info("받은 Access Token: " + accessToken);

	    if (destination != null && destination.startsWith("/sub/")) {
	        Integer roomId = Integer.parseInt(destination.replace("/sub/", ""));
			String nickname = memberRepository.findNicknameByMemberNum(Integer.parseInt(jwtTokenProvider.getMemberNum(accessToken))).get().getNickname();

	        // 채널에 메시지 전송
	        simpMessageingTemplate.convertAndSend(destination, getOutMessage(roomId, nickname));
	    }
	    
	}
	
	
	private ChattingDto getOutMessage(Integer roomId, String nickname)
	{
		ChattingDto outMessage = new ChattingDto();
        outMessage.setRoomId(roomId);
        outMessage.setNickname(nickname);
        outMessage.setContent(nickname + "님이 방에서 나갔습니다.");
        outMessage.setType(ChattingType.TYPE_OUT);
        
        return outMessage;
	}

}
