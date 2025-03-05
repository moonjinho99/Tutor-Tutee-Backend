package com.example.tutoring.service;

import com.example.tutoring.entity.Member;
import com.example.tutoring.repository.FollowRepository;
import com.example.tutoring.repository.MemberRepository;
import com.example.tutoring.repository.NoticeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SearchService {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    NoticeRepository noticeRepository;

    @Autowired
    FollowRepository followRepository;

    public ResponseEntity<Map<String, Object>> search(String nickname) {
        Map<String, Object> responseMap = new HashMap<>();

        try {
            System.out.println("여기는 실행하는가");
            List<Object[]> members = memberRepository.findByNicknameStartingWith(nickname);

            List<Map<String, Object>> memberList = new ArrayList<>();

            for (Object[] member: members) {
                Map<String, Object> memberMap = new HashMap<>();

                Member memberEntity = (Member) member[0];
                int memberNum = memberEntity.getMemberNum();

                memberMap.put("memberNum", memberNum);
                memberMap.put("nickname", memberEntity.getNickname());
                memberMap.put("profileImg", memberEntity.getProfileImg());
                memberMap.put("introduction", memberEntity.getIntroduction());
                memberMap.put("noticeCount", noticeRepository.noticeCount(memberNum));
                memberMap.put("followerNum", followRepository.followerCount(memberNum));
                memberMap.put("followingNum", followRepository.followingCount(memberNum));

                memberList.add(memberMap);
            }

            responseMap.put("memberList", memberList);
            return ResponseEntity.status(HttpStatus.OK).body(responseMap);
        } catch (Exception e) {
            responseMap.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
        }
    }

}
