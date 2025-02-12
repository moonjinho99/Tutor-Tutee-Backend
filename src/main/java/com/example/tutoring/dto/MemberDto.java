package com.example.tutoring.dto;

import com.example.tutoring.entity.Member;

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
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {

	private Integer memberNum;
	
	private String memberId;

	private String email;

	private String password;

	private Integer loginType;

	private String nickname;

	private String  profileImg;
	
	private String introduction;
	
	public static MemberDto toDto(Member entity)
	{
		return MemberDto.builder()
				.memberNum(entity.getMemberNum())
				.memberId(entity.getMemberId())
				.email(entity.getEmail())
				.password(entity.getPassword())
				.loginType(entity.getLoginType())
				.nickname(entity.getNickname())
				.profileImg(entity.getProfileImg())
				.introduction(entity.getIntroduction())
				.build();
	}
	
}
