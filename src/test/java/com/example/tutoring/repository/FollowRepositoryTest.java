package com.example.tutoring.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.example.tutoring.dto.FollowDto;
import com.example.tutoring.dto.MemberDto;
import com.example.tutoring.entity.Follow;
import com.example.tutoring.entity.Member;

@DataJpaTest
public class FollowRepositoryTest {

	@Autowired
	private FollowRepository followRepository;
	
	@Autowired
	private MemberRepository memberRepository;
	
	@Test
	public void testInsertFollow() {
		
		MemberDto me = MemberDto.builder()
                .memberId("me")
                .email("test@example.com")
                .password("1q2w3e4r5t")
                .loginType(0)
                .nickname("테스트계정")
                .profileImg("/data/img.jpg")
                .build();		
		
		 MemberDto other = MemberDto.builder()
	                .memberId("other")
	                .email("test2@example.com")
	                .password("1q2w3e4r5t")
	                .loginType(0)
	                .nickname("테스트계정")
	                .profileImg("/data/img.jpg")
	                .build();
		
		 memberRepository.save(Member.toEntity(me));
		 memberRepository.save(Member.toEntity(other));
				
		 FollowDto followDto = FollowDto.builder()
								.followerMemberId("me")
								.followerMemberNum(1)
								.followingMemberId("other")
								.followingMemberNum(2)
								.build();
		 
		 Follow savedFollow = followRepository.save(Follow.toEntity(followDto));
		 
		 assertThat(savedFollow).isNotNull();
		 assertThat(savedFollow.getFollowNum()).isNotNull();
		 assertThat(savedFollow.getFollowerMemberId()).isEqualTo(me.getMemberId());
		 assertThat(savedFollow.getFollowerMemberNum()).isEqualTo(1);
		 assertThat(savedFollow.getFollowingMemberId()).isEqualTo(other.getMemberId());
		 assertThat(savedFollow.getFollowingMemberNum()).isEqualTo(2);
								
	}
}
