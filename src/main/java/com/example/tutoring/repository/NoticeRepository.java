package com.example.tutoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.tutoring.entity.Notice;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Integer> {
	
}
