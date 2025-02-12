package com.example.tutoring.entity;

import javax.persistence.*;

import com.example.tutoring.dto.FollowDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "follow")
public class Follow {
	
	//팔로우를 눌렀을때 -> 팔로잉한(follower)에 자신의 계정정보 입력, 팔로우한(following)에 자신이 팔로잉을 누를 계정정보 입력
	//ex)나 : me  , 다른 회원 : other 
	//내가 어떤 회원의 계정을 보고 팔로우를 누름.
	//1. follwer~ 에는 나의 num(PK)과 Id(me)를 insert
	//2. follwing~ 에는 다른 회원의 num(PK)과 Id(other)를 insert
	//insert into follow(followingMemberNum, followerMemberNum, followingMemberId, followerMemberId) values(1,2,'other','me');
		
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "followNum")
    private Integer followNum;  //팔로우 번호

    @Column(name = "followingMemberNum", nullable = false)
    private Integer followingMemberNum;  //팔로우를 받은 회원

    @Column(name = "followerMemberNum", nullable = false)
    private Integer followerMemberNum;  //팔로잉을 클릭한 회원

    @Column(name = "followingMemberId", nullable = false)
    private String followingMemberId; //팔로우를 받은 회원 아이디

    @Column(name = "followerMemberId", nullable = false)
    private String followerMemberId; //팔로잉을 클릭한 회원 아이디

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followingMemberNum", referencedColumnName = "memberNum", insertable = false, updatable = false)
    private Member followingMember; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followerMemberNum", referencedColumnName = "memberNum", insertable = false, updatable = false)
    private Member followerMember; 
    
    public static Follow toEntity(FollowDto dto)
    {
    	return Follow.builder()
    			.followNum(dto.getFollowNum())
    			.followingMemberNum(dto.getFollowingMemberNum())
    			.followerMemberNum(dto.getFollowerMemberNum())
    			.followingMemberId(dto.getFollowingMemberId())
    			.followerMemberId(dto.getFollowerMemberId())
    			.build();
    }
    
}
