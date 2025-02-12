package com.example.tutoring.dto;

import com.example.tutoring.entity.Follow;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FollowDto {

	private Integer followNum;  
    private Integer followingMemberNum;  
    private Integer followerMemberNum;  
    private String followingMemberId; 
    private String followerMemberId; 

    public static FollowDto toDto(Follow entity)
    {
    	return FollowDto.builder()
    			.followNum(entity.getFollowNum())
    			.followingMemberNum(entity.getFollowingMemberNum())
    			.followerMemberNum(entity.getFollowerMemberNum())
    			.followingMemberId(entity.getFollowingMemberId())
    			.followerMemberId(entity.getFollowerMemberId())
    			.build();
    }
}
