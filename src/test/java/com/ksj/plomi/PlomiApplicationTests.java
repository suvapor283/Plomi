package com.ksj.plomi;

import com.ksj.plomi.domain.test.TestEntity;
import com.ksj.plomi.domain.test.TestRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PlomiApplicationTests {

    @Autowired
    private TestRepository testRepository;

    @Test
    void saveAndFindTest() {
        TestEntity test = new TestEntity();
        test.setTitle("test title");
        test.setContent("test content");

        TestEntity saveTest = testRepository.save(test);
        TestEntity foundTest = testRepository.findById(saveTest.getId()).orElse(null);

        assertThat(foundTest).isNotNull();
        assertThat(foundTest.getTitle()).isEqualTo("test title");
        assertThat(foundTest.getContent()).isEqualTo("test content");
    }

}
