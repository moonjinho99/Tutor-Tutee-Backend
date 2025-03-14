package com.example.tutoring.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import com.example.tutoring.dto.ChattingDto;
import com.example.tutoring.type.ChattingType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChattingController {

	private final SimpMessagingTemplate simpMessagingTemplate;
	
	@MessageMapping("/{chattingRoomId}/messages")
	private void chat(@DestinationVariable("chattingRoomId") Integer roomId, ChattingDto chattingDto)
	{	
		
		if(chattingDto.getType().equals(ChattingType.TYPE_IN))
		{
			log.info("방 참여");
			chattingDto.setContent("님이 방에 참여했습니다.");
		}
		
		
		simpMessagingTemplate.convertAndSend("/sub/"+roomId,chattingDto);
	}
	
}
