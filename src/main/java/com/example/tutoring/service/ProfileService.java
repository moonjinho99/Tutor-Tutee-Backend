package com.example.tutoring.service;

import com.example.tutoring.dto.*;
import com.example.tutoring.entity.LikeNotice;
import com.example.tutoring.entity.Notice;
import com.example.tutoring.repository.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.example.tutoring.entity.Follow;
import com.example.tutoring.entity.Member;
import com.example.tutoring.jwt.JwtTokenProvider;

import javax.transaction.Transactional;

@Slf4j
@Service
public class ProfileService {

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	UploadService uploadService;

	@Autowired
	FollowRepository followRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	NoticeRepository noticeRepository;

	@Autowired
	LikeNoticeRepository likeNoticeRepository;

	@Autowired
	DisLikeNoticeRepository disLikeNoticeRepository;

	public ResponseEntity<Map<String,Object>> profileImgUpdate(MultipartFile file , String accessToken)
	{
		Map<String,Object> responseMap = new HashMap<String, Object>();

		try {
			int memberNum = Integer.parseInt(jwtTokenProvider.getMemberNum(accessToken));
			Optional<Member> member = memberRepository.findById(memberNum);
			MemberDto dto = MemberDto.toDto(member.get());

			Map<String,Object> result = uploadService.uploadProfileImg(file);

			if((int)result.get("status") == 200)
			{
				dto.setProfileImg(result.get("url").toString());
				memberRepository.save(Member.toEntity(dto));
				responseMap.put("profileImg", dto.getProfileImg());
				return ResponseEntity.status(HttpStatus.OK).body(responseMap);
			}
			else {
				log.info("이미지 업로드 실패");
				responseMap.put("message", "이미지 업로드 실패");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
			}
		} catch(Exception e)
		{
			log.info(e.getMessage());
			responseMap.put("message", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
		}

	}

	public ResponseEntity<Map<String,Object>> followClick(String followerNickName, String accessToken)
	{
		Map<String,Object> responseMap = new HashMap<String, Object>();
		try {
			int memberNum = Integer.parseInt(jwtTokenProvider.getMemberNum(accessToken));
			Optional<Member> me = memberRepository.findById(memberNum);
			Optional<Member> follower = memberRepository.findByNickname(followerNickName);

			if(followRepository.followCheck(me.get().getMemberNum(), follower.get().getMemberNum()) > 0)
			{
				responseMap.put("message", "이미 팔로우한 회원입니다.");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
			}else {
				FollowDto followDto = FollowDto.builder()
						.followerMemberId(me.get().getMemberId())
						.followerMemberNum(memberNum)
						.followingMemberId(follower.get().getMemberId())
						.followingMemberNum(follower.get().getMemberNum())
						.build();
				followRepository.save(Follow.toEntity(followDto));
				log.info(me.get().getNickname()+" 가 "+followerNickName+"를 팔로우");

				return ResponseEntity.status(HttpStatus.OK).body(responseMap);
			}

		}catch(Exception e)
		{
			log.info(e.getMessage());
			responseMap.put("message", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
		}

	}

	public ResponseEntity<?> getFollowerList(int memberNum, Integer observer ,String accessToken)
	{
		try {
			Map<String,Object> response = new HashMap<String, Object>();
			int pageSize = 10;
			int offset = observer * pageSize;
			
			int myMemberNum = Integer.parseInt(jwtTokenProvider.getMemberNum(accessToken));
			
			List<Object[]> result = followRepository.findFollowerMemberList(memberNum, pageSize, offset);
			List<FollowResponseDto> followerList = new ArrayList<>();
			for (Object[] obj : result) {
				Integer followMemberNum = (Integer) obj[0];
				String nickname = (String) obj[1];
				String profileImg = (String) obj[2];
				String introduction = (String) obj[3];
							
				//맞팔로우 상태
				boolean status = true;
				
				//팔로우 확인
				boolean followStatus = true;
				
				if(followMemberNum == myMemberNum)
				{
					if(followRepository.followCheck(memberNum, myMemberNum) != 1)
						status= false;
				}else {
					//맞팔이 아닐때
					if(followRepository.eachFollowCheck(followMemberNum, myMemberNum) != 2)
					{
						status = false;
						
						//로그인한 회원이 팔로우 하지 않았을때
						if(followRepository.followCheck(myMemberNum,followMemberNum) != 1)
							followStatus = false;
					}																
				}												
				
				
								
				followerList.add(new FollowResponseDto(followMemberNum, nickname, profileImg, introduction, status, followStatus));
			}

			response.put("followList",followerList);
			
			if(followerList.size() < pageSize)
				response.put("flag", true);
			else
				response.put("flag", false);
			
			return ResponseEntity.status(HttpStatus.OK).body(response);
		}catch(Exception e)
		{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}

	}

	public ResponseEntity<?> getFollowingList(int memberNum, Integer observer ,String accessToken)
	{
		try {

			Map<String,Object> response = new HashMap<String, Object>();
			int pageSize = 10;
			int offset = observer * pageSize;
			
			int myMemberNum = Integer.parseInt(jwtTokenProvider.getMemberNum(accessToken));

			List<Object[]> result = followRepository.findFollowingMemberList(memberNum, pageSize, offset);

			List<FollowResponseDto> followingList = new ArrayList<>();
			for (Object[] obj : result) {
				Integer followMemberNum = (Integer) obj[0];
				String nickname = (String) obj[1];
				String profileImg = (String) obj[2];
				String introduction = (String) obj[3];
				
				boolean status = true;
				
				boolean followStatus = true;
				
				if(followMemberNum == myMemberNum)
				{
					if(followRepository.followCheck(myMemberNum, memberNum) != 1)
					{
						status= false;
						followStatus = false;
					}
						
				}else {
					if(followRepository.eachFollowCheck(followMemberNum, myMemberNum) != 2)
					{
						status = false;
						
						//로그인한 회원이 팔로우 하지 않았을때
						if(followRepository.followCheck(myMemberNum,followMemberNum) != 1)
							followStatus = false;
					}
				}				

				followingList.add(new FollowResponseDto(followMemberNum, nickname, profileImg, introduction,status, followStatus));
			}
			
			response.put("followList",followingList);
			
			if(followingList.size() < pageSize)
				response.put("flag", true);
			else
				response.put("flag", false);

			return ResponseEntity.status(HttpStatus.OK).body(response);
		}catch(Exception e)
		{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}

	}

	public ResponseEntity<Map<String,Object>> unFollow(int followMemberNum, String accessToken)
	{
		Map<String,Object> responseMap = new HashMap<String, Object>();
		try {
			int myMemberNum = Integer.parseInt(jwtTokenProvider.getMemberNum(accessToken));
			followRepository.unFollowMember(myMemberNum, followMemberNum);
			responseMap.put("message", "언팔로우 성공");
			return ResponseEntity.status(HttpStatus.OK).body(responseMap);
		}catch(Exception e)
		{
			log.info(e.getMessage());
			responseMap.put("message", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
		}
	}

	public ResponseEntity<?> searchFollower(String searchName, int memberNum, Integer observer,String accessToken)
	{
		
		Map<String,Object> response = new HashMap<String, Object>();
		
		try {
			if(!searchName.equals(""))
				searchName+="%";
			int pageSize = 10;
			int offset = observer * pageSize;

			int myMemberNum = Integer.parseInt(jwtTokenProvider.getMemberNum(accessToken));
			
			List<Object[]> result = followRepository.findSearchFollowerMemberList(memberNum, searchName, pageSize, offset);
			List<FollowResponseDto> followerList = new ArrayList<>();
			for (Object[] obj : result) {
				Integer followMemberNum = (Integer) obj[0];
				String nickname = (String) obj[1];
				String profileImg = (String) obj[2];
				String introduction = (String) obj[3];
				
				boolean status = true;
				
				boolean followStatus = true;
				
				if(followMemberNum == myMemberNum)
				{
					if(followRepository.followCheck(memberNum, myMemberNum) != 1)
						status= false;
				}else {
					if(followRepository.eachFollowCheck(followMemberNum, myMemberNum) != 2)
						status = false;
					
					//로그인한 회원이 팔로우 하지 않았을때
					if(followRepository.followCheck(myMemberNum,followMemberNum) != 1)
						followStatus = false;
					
				}	
				
				followerList.add(new FollowResponseDto(followMemberNum, nickname, profileImg, introduction, status, followStatus));
			}
			
			
			response.put("searchFollowList",followerList);
			
			if(followerList.size() < pageSize)
				response.put("flag", true);
			else
				response.put("flag", false);

			return ResponseEntity.status(HttpStatus.OK).body(response);
		}catch(Exception e)
		{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	public ResponseEntity<?> searchFollowing(String searchName, int memberNum, Integer observer,String accessToken)
	{
		
		Map<String,Object> response = new HashMap<String, Object>();
		
		try {
			if(!searchName.equals(""))
				searchName+="%";
			int pageSize = 10;
			int offset = observer * pageSize;
			
			int myMemberNum = Integer.parseInt(jwtTokenProvider.getMemberNum(accessToken));

			List<Object[]> result = followRepository.findSearchFollowingMemberList(memberNum, searchName, pageSize, offset);
			List<FollowResponseDto> followingList = new ArrayList<>();
			for (Object[] obj : result) {
				Integer followMemberNum = (Integer) obj[0];
				String nickname = (String) obj[1];
				String profileImg = (String) obj[2];
				String introduction = (String) obj[3];
				
				boolean status = true;
				
				boolean followStatus = true;
				
				if(followMemberNum == myMemberNum)
				{
					if(followRepository.followCheck(myMemberNum, memberNum) != 1)
					{
						status= false;
						followStatus = false;
					}
				}else {
					if(followRepository.eachFollowCheck(followMemberNum, myMemberNum) != 2)
						status = false;
					
					//로그인한 회원이 팔로우 하지 않았을때
					if(followRepository.followCheck(myMemberNum,followMemberNum) != 1)
						followStatus = false;
				}	
				
				followingList.add(new FollowResponseDto(followMemberNum, nickname, profileImg, introduction,status, followStatus));
			}

			response.put("searchFollowList",followingList);
			
			if(followingList.size() < pageSize)
				response.put("flag", true);
			else
				response.put("flag", false);
						
			return ResponseEntity.status(HttpStatus.OK).body(response);
		}catch(Exception e)
		{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}


	public ResponseEntity<Map<String,Object>> deleteFollow(int followMemberNum, String accessToken)
	{
		Map<String,Object> responseMap = new HashMap<String, Object>();
		try {
			int myMemberNum = Integer.parseInt(jwtTokenProvider.getMemberNum(accessToken));
			followRepository.deleteFollowMember(followMemberNum,myMemberNum);

			responseMap.put("message", "팔로워 삭제 성공");
			return ResponseEntity.status(HttpStatus.OK).body(responseMap);
		}catch(Exception e)
		{
			log.info(e.getMessage());
			responseMap.put("message", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
		}
	}

	// 작성한 공지글
	public ResponseEntity<Map<String, Object>> myNotice(Integer observer, int memberNum) {

		Map<String, Object> response = new HashMap<>();

		try {
			Optional<Member> member = memberRepository.findByMemberNum(memberNum);

			int pageSize = 6;
			int offset = observer * pageSize;

			List<Object[]> notices = noticeRepository.findByMemberNumWithPagination(memberNum, pageSize, offset);

			List<Map<String, Object>> noticeList = new ArrayList<>();

			for(Object[] notice:notices) {
				Map<String, Object> noticeMap = new LinkedHashMap<>();

				noticeMap.put("noticeNum", notice[0]);
				noticeMap.put("noticeContent", notice[1]);
				noticeMap.put("noticeWriter", member.get().getNickname());
				noticeMap.put("noticeDate", notice[3]);
				noticeMap.put("likeCount", notice[4]);
				noticeMap.put("disLikeCount", notice[5]);

				noticeMap.put("likeStatus", likeNoticeRepository.existsByNoticeNum((Integer) notice[0]) ? "true" : "false");
				noticeMap.put("disLikeStatus", disLikeNoticeRepository.existsByNoticeNum((Integer) notice[0]) ? "true" : "false");

				noticeList.add(noticeMap);
			}

			response.put("notices", noticeList);
			response.put("flag", notices.size() < pageSize);

			return ResponseEntity.status(HttpStatus.OK).body(response);

		} catch (Exception e) {
			response.put("message", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
	}
	
	public ResponseEntity<Map<String, Object>> profileInfo(int memberNum)
	{
		Map<String,Object> response = new HashMap<String, Object>();
		
		int followerCnt = 0;
		int followCnt = 0;
		int noticeCnt = 0;
		
		try {
			Member member = memberRepository.findById(memberNum).get();
			followerCnt = followRepository.followerCount(memberNum);
			followCnt = followRepository.followingCount(memberNum);
			noticeCnt = noticeRepository.noticeCount(memberNum);
						
			response.put("memberNum", member.getMemberNum());
			response.put("nickname", member.getNickname());
			response.put("profileImg", member.getProfileImg());
			response.put("introduction",member.getIntroduction());
			response.put("followCount", followCnt);
			response.put("followerCount", followerCnt);
			response.put("noticeCount", noticeCnt);
			
			return ResponseEntity.status(HttpStatus.OK).body(response);
		}catch(Exception e)
		{
			response.put("message", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
						
	}
	
	public ResponseEntity<Map<String, Object>> lastNotice(int observer, String accessToken)
	{
		Map<String,Object> response = new HashMap<String, Object>();
		
		try {
			int pageSize = 6;
			int offset = observer * pageSize;		
			int myMemeberNum = Integer.parseInt(jwtTokenProvider.getMemberNum(accessToken));
			
			List<Object[]> result = followRepository.findLastNoticeFollowMember(myMemeberNum, pageSize, offset);
			List<Map<String,Object>> followList = new ArrayList<Map<String,Object>>();
			for(Object[] member : result)
			{
				Map<String,Object> followMember = new HashMap<String, Object>();
				
				followMember.put("memberNum", (int)member[0]);
				followMember.put("followNickName", (String)member[1]);
				followMember.put("followProfileImg",(String)member[2]);
				
				followList.add(followMember);
			}
			
			response.put("followList",followList);
			
			if(followList.size() < pageSize)
				response.put("flag", true);
			else
				response.put("flag", false);
			
			return ResponseEntity.status(HttpStatus.OK).body(response);
		}catch(Exception e)
		{
			response.put("message", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);		
		}
				
	}

	// 닉네임 변경
	public ResponseEntity<Map<String, Object>> changeNickname(String newNickname, String accessToken) {

		Map<String, Object> response = new HashMap<>();
		int memberNum = Integer.parseInt(jwtTokenProvider.getMemberNum(accessToken));

		Optional<Member> member = memberRepository.findByMemberNum(memberNum);

		if (!jwtTokenProvider.validateToken(accessToken)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		if (member.isPresent()) {
			if (memberRepository.existsByNickname(newNickname)) {
				response.put("message", "중복된 닉네임입니다.");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}

			member.get().setNickname(newNickname);
			memberRepository.save(member.get());
			return ResponseEntity.status(HttpStatus.OK).build();
		} else {
			response.put("message", "회원 정보를 찾을 수 없습니다.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
	}

	public ResponseEntity<Map<String, Object>> introduction(String introductionData, String accessToken) {

		Map<String, Object> response = new HashMap<>();
		int memberNum = Integer.parseInt(jwtTokenProvider.getMemberNum(accessToken));
		Optional<Member> member =  memberRepository.findByMemberNum(memberNum);

		try {
			member.get().setIntroduction(introductionData);
			memberRepository.save(member.get());
			return ResponseEntity.status(HttpStatus.OK).build();
		} catch (Exception e){
			response.put("message", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
	}

	// 공지글 작성
	public ResponseEntity<Map<String, Object>> writeNotice(String writeNotice, String accessToken) {

		Map<String, Object> response = new HashMap<>();

		if (!jwtTokenProvider.validateToken(accessToken)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		int memberNum = Integer.parseInt(jwtTokenProvider.getMemberNum(accessToken));

		try {
			NoticeDto noticeDto = new NoticeDto();
			noticeDto.setContent(writeNotice);
			noticeDto.setMemberNum(memberNum);
			noticeDto.setCreateTime(new Date());
			noticeDto.setLikeCnt(0);
			noticeDto.setDisLikeCnt(0);

			Notice notice = Notice.toEntity(noticeDto);

			noticeRepository.save(notice);

			return ResponseEntity.status(HttpStatus.OK).build();
		} catch (Exception e) {
			response.put("message", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
	}

	// 공지글 삭제
	public ResponseEntity<Map<String, Object>> deleteNotice(int noticeNum, String accessToken) {

		Map<String, Object> response = new HashMap<>();

		if (!jwtTokenProvider.validateToken(accessToken)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		Optional<Notice> notice = noticeRepository.findById(noticeNum);

		noticeRepository.delete(notice.get());

		return ResponseEntity.status(HttpStatus.OK).build();
	}

	// 공지글 좋아요
	@Transactional
	public ResponseEntity<Map<String, Object>> likeNotice(int noticeNum, String accessToken) {

		int memberNum = Integer.parseInt(jwtTokenProvider.getMemberNum(accessToken));
		int likeCnt = noticeRepository.findLikeCntByNoticeNum(noticeNum) + 1;

		noticeRepository.updateLikeCount(noticeNum, likeCnt);

		LikeNoticeDto likeNoticeDto = new LikeNoticeDto();
		likeNoticeDto.setMemberNum(memberNum);
		likeNoticeDto.setNoticeNum(noticeNum);
		likeNoticeDto.setLikedAt(new Date());

		LikeNotice likeNotice = LikeNotice.toEntity(likeNoticeDto);

		likeNoticeRepository.save(likeNotice);

		return ResponseEntity.status(HttpStatus.OK).build();
	}
}