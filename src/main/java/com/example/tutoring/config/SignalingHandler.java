package com.example.tutoring.config;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import org.json.JSONObject;

@Slf4j
@Component
public class SignalingHandler extends TextWebSocketHandler{

    private static final ConcurrentHashMap<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, WebSocketSession> hostSessions = new ConcurrentHashMap<>();	
	
    @Override
    public void afterConnectionEstablished(WebSocketSession session){
       log.info("새로운 WebRTC 연결 : {}",session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String senderId = session.getId();
        String payload = message.getPayload();
        
        log.info("메시지 수신 : "+payload);
        
        
        JSONObject jsonMessage = new JSONObject(payload);
        String type = jsonMessage.getString("type");
        String roomId = jsonMessage.getString("roomId");
        
        switch(type) {
            case "create_room":
                handleCreateRoom(roomId, session);
                break;
            case "join_room" :
                handleJoinRoom(roomId,session);
                break;
            case "offer":
                handleOffer(roomId,session,jsonMessage);
                break;
            case "answer":
                handleAnswer(roomId, session, jsonMessage);
                break;
            case "candidate":
                handleCandidate(roomId, session, jsonMessage);
                break;
            case "leave":
                handleLeave(roomId, session);
                break;
        }
        
    }
       
    private void handleCreateRoom(String roomId, WebSocketSession session) {
        rooms.put(roomId, new CopyOnWriteArraySet<>());
        hostSessions.put(roomId, session);
        log.info("방 생성됨 : {}", roomId);
    }
    
    private void handleJoinRoom(String roomId, WebSocketSession session) throws IOException{
        rooms.computeIfAbsent(roomId,k -> new CopyOnWriteArraySet<>()).add(session);
        log.info("참여자 입장: {} - 방 ID: {}", session.getId(), roomId);
        
        WebSocketSession hostSession = hostSessions.get(roomId);
        if(hostSession != null && hostSession.isOpen()) {
            
            log.info("new_member 응답");
            
            JSONObject newParticipantMessage = new JSONObject();
            newParticipantMessage.put("type", "new_member");
            newParticipantMessage.put("id", session.getId());
            
            hostSession.sendMessage(new TextMessage(newParticipantMessage.toString()));
        }
        
    }
    
    private void handleOffer(String roomId, WebSocketSession session, JSONObject jsonMessage) throws IOException
    {
        log.info("Offer 전송: {} -> {}",session.getId(), roomId);
        
        for(WebSocketSession participantSession : rooms.get(roomId)) {
            if(participantSession.isOpen() && !participantSession.equals(session)) {
                participantSession.sendMessage(new TextMessage(jsonMessage.toString()));
            }
        }
    }
    
    private void handleAnswer(String roomId, WebSocketSession session, JSONObject jsonMessage) throws IOException{
        log.info("Answer 전송 : {} -> {}",session.getId(), roomId);
        
        WebSocketSession hostSession = hostSessions.get(roomId);
        if(hostSession != null && hostSession.isOpen()) {
            hostSession.sendMessage(new TextMessage(jsonMessage.toString()));
        }
    }
    
    private void handleCandidate(String roomId, WebSocketSession session, JSONObject jsonMessage) throws IOException{
        log.info("Candidiate 전송 : {} -> {}", session.getId(), roomId);
        
        for(WebSocketSession participant : rooms.get(roomId))
        {
            if(participant.isOpen() && !participant.equals(session)) {
                participant.sendMessage(new TextMessage(jsonMessage.toString()));
            }
        }
    }

    private void handleLeave(String roomId, WebSocketSession session) throws IOException {
        rooms.getOrDefault(roomId, new HashSet<>()).remove(session);
        log.info("사용자 퇴장 : {} - 방 ID: {}",session.getId(), roomId);
        
        WebSocketSession hostSession = hostSessions.get(roomId);
        if (hostSession != null && hostSession.isOpen()) {
            JSONObject leaveMessage = new JSONObject();
            leaveMessage.put("type", "participant_leave");
            leaveMessage.put("id", session.getId());
            
            hostSession.sendMessage(new TextMessage(leaveMessage.toString()));
        }
        
        if(rooms.getOrDefault(roomId, new HashSet<>()).isEmpty()) {
            rooms.remove(roomId);
            hostSessions.remove(roomId);
        }
        
    }
    
	
}
