package com.example.tutoring.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import com.example.tutoring.dto.NoticeDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "notice")
public class Notice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	@Column(name="noticeNum", nullable=false)
	private Integer noticeNum;
	
	@Column(name="memberNum", nullable=false)
	private Integer memberNum;
	
	@Column(name="title")
	private String title;
	
	@Column(name="content")
	private String content;
	
	@Column(name="createTime")
	private Date createTime;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberNum", referencedColumnName = "memberNum", insertable = false, updatable = false)
    private Member member;
	
	public static Notice toEntity(NoticeDto dto)
	{
		return Notice.builder()
				.noticeNum(dto.getNoticeNum())
				.memberNum(dto.getMemberNum())
				.title(dto.getTitle())
				.content(dto.getContent())
				.createTime(dto.getCreateTime())
				.build();
	}
}
