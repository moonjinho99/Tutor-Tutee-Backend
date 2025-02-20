package com.example.tutoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.tutoring.entity.Notice;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Integer> {

    // memberNum에 해당하는 공지글 목록 조회
    List<Notice> findByMemberNum(Integer memberNum);
    
    // 게시글 카운트
	@Query("SELECT COUNT(n) FROM Notice n WHERE n.memberNum = :memberNum")
  	int noticeCount(@Param("memberNum") int memberNum);
}
