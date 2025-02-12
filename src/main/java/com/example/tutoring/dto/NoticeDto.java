package com.example.tutoring.dto;


import java.util.Date;

import com.example.tutoring.entity.Notice;

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
public class NoticeDto {

	private Integer noticeNum;	
	private Integer memberNum;	
	private String title;	
	private String content;	
	private Date createTime;
	
	public static NoticeDto toDto(Notice entity)
	{
		return NoticeDto.builder()
				.noticeNum(entity.getNoticeNum())
				.memberNum(entity.getMemberNum())
				.title(entity.getTitle())
				.content(entity.getContent())
				.createTime(entity.getCreateTime())
				.build();
	}
}
