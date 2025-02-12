package com.example.tutoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.tutoring.entity.Member;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Integer>{

    @Query("SELECT COUNT(m) > 0 FROM Member m WHERE m.memberId = :memberId")
    boolean existsByMemberId(@Param("memberId") String memberId);
    
    Member findByMemberId(String memberId);

    // 이메일 중복 검사
    @Query("SELECT COUNT(m) > 0 FROM Member m WHERE m.email = :email")
    boolean existsByEmail(@Param("email") String email);

    // 아이디 찾기
    Optional<Member> findByEmail(String email);
    
    //네이버 회숸 조회
    @Query("SELECT m FROM Member m WHERE m.memberId = :memberId AND m.loginType = 2")
    Optional<Member> findByNaverMember(@Param("memberId") String memberId);
    
    //PK로 ID값 조회
    @Query("SELECT m.memberId FROM Member m WHERE m.memberNum = :memberNum")
    String findMemberIdByMemberNum(Integer memberNum);
}
