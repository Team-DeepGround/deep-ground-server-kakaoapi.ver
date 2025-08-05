package com.samsamhajo.deepground.studyGroup.service;

import com.samsamhajo.deepground.member.entity.Member;
import com.samsamhajo.deepground.studyGroup.dto.StudyGroupsByLocationResponse;
import com.samsamhajo.deepground.studyGroup.entity.StudyGroup;
import com.samsamhajo.deepground.studyGroup.repository.StudyGroupAddressRepository;
import com.samsamhajo.deepground.studyGroup.repository.StudyGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudyGroupLocationServiceTest {

    @Mock
    private StudyGroupRepository studyGroupRepository;
    @Mock
    private StudyGroupAddressRepository studyGroupAddressRepository;

    @InjectMocks
    private StudyGroupLocationService studyGroupLocationService;

    private String city;
    private String gu;
    private String dong;
    private StudyGroup studyGroup;

    @BeforeEach
    void setUp() {
        city = "서울특별시";
        gu = "강남구";
        dong = "삼성동";
        Member creator = Member.createLocalMember("creator@test.com", "pw", "작성자");
        studyGroup = StudyGroup.of(
                null, "스터디 제목", "스터디 설명",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(10),
                LocalDate.now(), LocalDate.now().plusDays(5),
                5, creator, false
        );
    }

    @Test
    @DisplayName("동으로 스터디 그룹 목록을 조회한다")
    void getStudyGroupsByLocation_success() {
        // given
        when(studyGroupAddressRepository.findStudyGroupIdByAddress(city, gu, dong))
                .thenReturn(List.of(1L));
        when(studyGroupRepository.findByIdIn(List.of(1L))).thenReturn(List.of(studyGroup));

        // when
        StudyGroupsByLocationResponse response = studyGroupLocationService.getStudyGroupsByLocation(city, gu, dong);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStudyGroups().size()).isEqualTo(1);
        assertThat(response.getStudyGroups().get(0).getId()).isEqualTo(studyGroup.getId());
    }
}
