package com.example.tutoring.repository;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import com.example.tutoring.dto.MemberDto;
import com.example.tutoring.dto.NoticeDto;
import com.example.tutoring.entity.Member;
import com.example.tutoring.entity.Notice;


@DataJpaTest
public class NoticeRepositoryTest {

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    public void testInsertNotice() {

        MemberDto memberDto = MemberDto.builder()
                .memberId("test")
                .email("test@example.com")
                .password("1q2w3e4r5t")
                .loginType(0)
                .nickname("테스트계정")
                .profileImg("/data/img.jpg")
                .build();

        Member member = Member.toEntity(memberDto);
        memberRepository.save(member); 

        Optional<Member> savedMember = memberRepository.findById(1);
        assertThat(savedMember).isPresent();

        NoticeDto noticeDto = NoticeDto.builder()
                .memberNum(1) 
                .title("공지 제목")
                .content("공지 내용")
                .createTime(new Date())
                .build();


        Notice savedNotice = noticeRepository.save(Notice.toEntity(noticeDto));

        assertThat(savedNotice).isNotNull();
        assertThat(savedNotice.getNoticeNum()).isNotNull();
        assertThat(savedNotice.getTitle()).isEqualTo("공지 제목");
        assertThat(savedNotice.getContent()).isEqualTo("공지 내용");
        assertThat(savedNotice.getMemberNum()).isEqualTo(savedMember.get().getMemberNum());
    }
}
