package com.ksj.plomi;

import com.ksj.plomi.domain.diary.entity.Diary;
import com.ksj.plomi.domain.diary.repository.DiaryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

@SpringBootTest
class PlomiApplicationTests {

    @Autowired
    private DiaryRepository diaryRepository;

    @Test
    void saveAndFindDiary() {
        Diary diary = new Diary();
        diary.setTitle("test title");
        diary.setContent("test content");
        diary.setDate(LocalDate.now());

        Diary savedDiary = diaryRepository.save(diary);
        Diary foundDiary = diaryRepository.findById(savedDiary.getId()).orElse(null);

        assertThat(foundDiary).isNotNull();
        assertThat(foundDiary.getTitle()).isEqualTo("test title");
        assertThat(foundDiary.getContent()).isEqualTo("test content");
    }

}
