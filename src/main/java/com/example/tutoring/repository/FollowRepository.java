package com.example.tutoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.tutoring.entity.Follow;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Integer> {

}
