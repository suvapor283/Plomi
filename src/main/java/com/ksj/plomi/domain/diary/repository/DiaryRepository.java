package com.ksj.plomi.domain.diary.repository;

import com.ksj.plomi.domain.diary.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
}
