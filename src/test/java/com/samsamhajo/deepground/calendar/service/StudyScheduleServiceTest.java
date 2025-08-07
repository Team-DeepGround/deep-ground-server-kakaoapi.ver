package com.samsamhajo.deepground.calendar.service;

import com.samsamhajo.deepground.calendar.dto.PlaceRequestDto;
import com.samsamhajo.deepground.calendar.dto.StudyScheduleRequestDto;
import com.samsamhajo.deepground.calendar.dto.StudyScheduleResponseDto;
import com.samsamhajo.deepground.calendar.entity.MemberStudySchedule;
import com.samsamhajo.deepground.calendar.entity.StudySchedule;
import com.samsamhajo.deepground.calendar.exception.ScheduleErrorCode;
import com.samsamhajo.deepground.calendar.exception.ScheduleException;
import com.samsamhajo.deepground.calendar.repository.MemberStudyScheduleRepository;
import com.samsamhajo.deepground.calendar.repository.StudyScheduleRepository;
import com.samsamhajo.deepground.communityPlace.entity.SpecificAddress;
import com.samsamhajo.deepground.communityPlace.repository.SpecificAddressRepository;
import com.samsamhajo.deepground.member.entity.Member;
import com.samsamhajo.deepground.studyGroup.entity.StudyGroup;
import com.samsamhajo.deepground.studyGroup.repository.StudyGroupMemberRepository;
import com.samsamhajo.deepground.studyGroup.repository.StudyGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudyScheduleServiceTest {

    @Mock
    private StudyScheduleRepository studyScheduleRepository;

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @Mock
    private StudyGroupMemberRepository studyGroupMemberRepository;

    @Mock
    private MemberStudyScheduleRepository memberStudyScheduleRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private SpecificAddressRepository specificAddressRepository;

    @Mock
    private GeometryFactory geometryFactory;

    @InjectMocks
    private StudyScheduleService studyScheduleService;

    private StudyGroup studyGroup;
    private StudyScheduleRequestDto requestDto;
    private final Long userId = 1L;

    @BeforeEach
    void setup() {
        studyGroup = mock(StudyGroup.class);
        requestDto = StudyScheduleRequestDto.builder()
                .title("스터디 제목")
                .description("스터디 설명")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(2))
                .location("강남역")
                .latitude(37.0)
                .longitude(127.0)
                .place(PlaceRequestDto.builder()
                        .name("카페")
                        .address("서울 강남구")
                        .phone("010-0000-0000")
                        .placeId("123")
                        .latitude(37.0)
                        .longitude(127.0)
                        .build())
                .build();
    }

    @Test
    @DisplayName("스터디 일정 생성 성공")
    void createStudySchedule_Success() throws Exception {
        when(studyGroupRepository.findById(anyLong())).thenReturn(Optional.of(studyGroup));
        when(studyScheduleRepository.existsByStudyGroupIdAndEndTimeGreaterThanAndStartTimeLessThan(anyLong(), any(), any())).thenReturn(false);

        Member leader = mock(Member.class);
        when(leader.getId()).thenReturn(userId);
        when(studyGroup.getCreator()).thenReturn(leader);

        when(specificAddressRepository.findByNameAndLocation(anyString(), anyString())).thenReturn(Optional.empty());

        Point point = mock(Point.class);
        when(point.getY()).thenReturn(37.0);
        when(point.getX()).thenReturn(127.0);
        when(geometryFactory.createPoint(any(Coordinate.class))).thenReturn(point);

        when(studyScheduleRepository.save(any(StudySchedule.class))).thenAnswer(invocation -> {
            StudySchedule schedule = invocation.getArgument(0);
            Field idField = StudySchedule.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(schedule, 1L);
            return schedule;
        });

        when(studyGroupMemberRepository.findAllByStudyGroupIdAndIsAllowedTrue(anyLong())).thenReturn(List.of());

        StudyScheduleResponseDto responseDto = studyScheduleService.createStudySchedule(1L, userId, requestDto);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getTitle()).isEqualTo(requestDto.getTitle());
    }


    @Test
    @DisplayName("스터디 일정 생성 실패 - 종료 시간이 시작 시간보다 빠른 경우")
    void createStudySchedule_Fail_EndTimeBeforeStartTime() {
        // given
        when(studyGroupRepository.findById(anyLong())).thenReturn(Optional.of(studyGroup));

        LocalDateTime startTime = LocalDateTime.of(2025, 5, 20, 15, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 5, 20, 13, 0);

        requestDto = requestDto.toBuilder()
                .startTime(startTime)
                .endTime(endTime)
                .build();

        // when & then
        assertThatThrownBy(() -> studyScheduleService.createStudySchedule(1L, userId, requestDto))
                .isInstanceOf(ScheduleException.class)
                .hasMessageContaining(ScheduleErrorCode.INVALID_DATE_RANGE.getMessage());

        verify(studyScheduleRepository, never()).save(any(StudySchedule.class));
    }

    @Test
    @DisplayName("스터디 일정 생성 실패 - 존재하지 않는 스터디 그룹으로 생성 요청")
    void createStudySchedule_Fail_StudyGroupNotFound() {
        // given
        when(studyGroupRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> studyScheduleService.createStudySchedule(1L, userId, requestDto))
                .isInstanceOf(ScheduleException.class)
                .hasMessageContaining(ScheduleErrorCode.STUDY_GROUP_NOT_FOUND.getMessage());

        verify(studyScheduleRepository, never()).save(any(StudySchedule.class));
    }

    @Test
    @DisplayName("스터디 일정 생성 실패 - 중복된 시간대의 일정이 존재할 경우")
    void createStudySchedule_Fail_DuplicateSchedule() {
        // given
        when(studyGroupRepository.findById(anyLong())).thenReturn(Optional.of(studyGroup));
        when(studyScheduleRepository.existsByStudyGroupIdAndEndTimeGreaterThanAndStartTimeLessThan(
                anyLong(),
                eq(requestDto.getStartTime()),
                eq(requestDto.getEndTime())
        )).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> studyScheduleService.createStudySchedule(1L, userId, requestDto))
                .isInstanceOf(ScheduleException.class)
                .hasMessageContaining(ScheduleErrorCode.DUPLICATE_SCHEDULE.getMessage());

        verify(studyScheduleRepository, never()).save(any(StudySchedule.class));
    }

    @Test
    @DisplayName("스터디 일정 생성 실패 - 스터디 장이 아닌 경우")
    void createStudySchedule_Fail_NotLeader() {
        // given
        Member notLeader = mock(Member.class);
        when(notLeader.getId()).thenReturn(9L);

        when(studyGroup.getCreator()).thenReturn(notLeader);
        when(studyGroupRepository.findById(anyLong())).thenReturn(Optional.of(studyGroup));

        // when & then
        assertThatThrownBy(() -> studyScheduleService.createStudySchedule(1L, userId, requestDto))
                .isInstanceOf(ScheduleException.class)
                .hasMessageContaining(ScheduleErrorCode.UNAUTHORIZED_USER.getMessage());

        verify(studyScheduleRepository, never()).save(any(StudySchedule.class));
    }

    @Test
    @DisplayName("스터디 그룹 ID로 일정 조회 성공")
    void findSchedulesByStudyGroupId_Success() {
        // given
        when(studyGroupRepository.findById(anyLong())).thenReturn(Optional.of(studyGroup));

        SpecificAddress address = mock(SpecificAddress.class);
        when(address.getName()).thenReturn("카페");
        when(address.getLocation()).thenReturn("서울 강남구");
        when(address.getPhone()).thenReturn("010-0000-0000");
        when(address.getPlaceUrl()).thenReturn("http://place.kakao.com");
        when(address.getLatitude()).thenReturn(37.0);
        when(address.getLongitude()).thenReturn(127.0);

        StudySchedule studySchedule1 = StudySchedule.of(
                studyGroup, "스터디 1",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                "설명 1", "온라인",
                37.0, 127.0, address
        );

        StudySchedule studySchedule2 = StudySchedule.of(
                studyGroup, "스터디 2",
                LocalDateTime.now().plusHours(2),
                LocalDateTime.now().plusHours(3),
                "설명 2", "오프라인",
                37.0, 127.0, address
        );

        when(studyScheduleRepository.findAllByStudyGroupId(anyLong()))
                .thenReturn(List.of(studySchedule1, studySchedule2));

        // when
        List<StudyScheduleResponseDto> responseDtos = studyScheduleService.findSchedulesByStudyGroupId(1L);

        // then
        assertThat(responseDtos).hasSize(2);
        assertThat(responseDtos.get(0).getTitle()).isEqualTo("스터디 1");
        assertThat(responseDtos.get(1).getTitle()).isEqualTo("스터디 2");
        assertThat(responseDtos.get(0).getPlace().getName()).isEqualTo("카페");

        verify(studyScheduleRepository, times(1)).findAllByStudyGroupId(anyLong());
    }


    @Test
    @DisplayName("스터디 그룹 ID로 일정 조회 실패 - 존재하지 않는 스터디 그룹 ID로 조회")
    void findSchedulesByStudyGroupId_StudyGroupNotFound() {
        // given
        when(studyGroupRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> studyScheduleService.findSchedulesByStudyGroupId(1L))
                .isInstanceOf(ScheduleException.class)
                .hasMessageContaining(ScheduleErrorCode.STUDY_GROUP_NOT_FOUND.getMessage());

        verify(studyScheduleRepository, never()).findAllByStudyGroupId(anyLong());

    }

    @Test
    @DisplayName("스터디 일정 수정 성공")
    void updateStudySchedule_Success() throws Exception {
        when(studyGroupRepository.findById(anyLong())).thenReturn(Optional.of(studyGroup));

        Member leader = mock(Member.class);
        when(leader.getId()).thenReturn(userId);
        when(studyGroup.getCreator()).thenReturn(leader);

        StudySchedule existingSchedule = StudySchedule.of(
                studyGroup,
                "기존 제목",
                requestDto.getStartTime(),
                requestDto.getEndTime(),
                requestDto.getDescription(),
                requestDto.getLocation(),
                requestDto.getLatitude(),
                requestDto.getLongitude(),
                null
        );

        Field idField = StudySchedule.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(existingSchedule, 1L);

        when(studyScheduleRepository.findById(anyLong())).thenReturn(Optional.of(existingSchedule));
        when(studyScheduleRepository.existsByStudyGroupIdAndEndTimeGreaterThanAndStartTimeLessThan(anyLong(), any(), any())).thenReturn(false);

        when(specificAddressRepository.findByNameAndLocation(anyString(), anyString())).thenReturn(Optional.empty());

        Point point = mock(Point.class);
        when(point.getY()).thenReturn(37.0);
        when(point.getX()).thenReturn(127.0);
        when(geometryFactory.createPoint(any(Coordinate.class))).thenReturn(point);

        MemberStudySchedule memberSchedule = mock(MemberStudySchedule.class);
        when(memberStudyScheduleRepository.findByStudyScheduleId(anyLong())).thenReturn(List.of(memberSchedule));

        StudyScheduleResponseDto responseDto = studyScheduleService.updateStudySchedule(1L, userId, 1L, requestDto);

        assertThat(responseDto.getTitle()).isEqualTo(requestDto.getTitle());
        verify(memberSchedule, times(1)).updateAvailable(null);
    }

    @Test
    @DisplayName("스터디 일정 수정 실패 - 존재하지 않는 스터디 그룹")
    void updateStudySchedule_Fail_StudyGroupNotFound() {
        // given
        when(studyGroupRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> studyScheduleService.updateStudySchedule(1L, userId, 1L, requestDto))
                .isInstanceOf(ScheduleException.class)
                .hasMessageContaining(ScheduleErrorCode.STUDY_GROUP_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("스터디 일정 수정 실패 - 존재하지 않는 스터디 일정")
    void updateStudySchedule_Fail_ScheduleNotFound() {
        // given
        when(studyGroupRepository.findById(anyLong())).thenReturn(Optional.of(studyGroup));
        when(studyScheduleRepository.findById(anyLong())).thenReturn(Optional.empty());

        Member leader = mock(Member.class);
        when(leader.getId()).thenReturn(userId);
        when(studyGroup.getCreator()).thenReturn(leader);

        // when & then
        assertThatThrownBy(() -> studyScheduleService.updateStudySchedule(1L, userId, 1L, requestDto))
                .isInstanceOf(ScheduleException.class)
                .hasMessageContaining(ScheduleErrorCode.SCHEDULE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("스터디 일정 수정 실패 - 종료 시간이 시작 시간보다 빠른 경우")
    void updateStudySchedule_Fail_EndTimeBeforeStartTime() throws IllegalAccessException, NoSuchFieldException {
        // given
        LocalDateTime startTime = LocalDateTime.of(2025, 5, 21, 13, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 5, 21, 11, 0); // 종료 시간이 더 빠름

        // 장소 정보 생략 (테스트 목적에 불필요하므로)
        StudyScheduleRequestDto request = requestDto.toBuilder()
                .startTime(startTime)
                .endTime(endTime)
                .place(null) // ✅ null 처리하여 SpecificAddress 생성 로직 건너뜀
                .build();

        Member leader = mock(Member.class);
        when(leader.getId()).thenReturn(userId);
        when(studyGroup.getCreator()).thenReturn(leader);

        StudySchedule schedule = StudySchedule.of(
                studyGroup,
                "제목",
                requestDto.getStartTime(),
                requestDto.getEndTime(),
                requestDto.getDescription(),
                requestDto.getLocation(),
                requestDto.getLatitude(),
                requestDto.getLongitude(),
                null // SpecificAddress 없음
        );

        Field idField = StudySchedule.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(schedule, 1L);

        when(studyGroupRepository.findById(anyLong())).thenReturn(Optional.of(studyGroup));
        when(studyScheduleRepository.findById(anyLong())).thenReturn(Optional.of(schedule));
        when(studyScheduleRepository.existsByStudyGroupIdAndEndTimeGreaterThanAndStartTimeLessThan(
                anyLong(), any(), any()
        )).thenReturn(false); // 중복 일정 없음

        // when & then
        assertThatThrownBy(() -> studyScheduleService.updateStudySchedule(1L, userId, 1L, request))
                .isInstanceOf(ScheduleException.class)
                .hasMessageContaining(ScheduleErrorCode.INVALID_DATE_RANGE.getMessage());
    }


    @Test
    @DisplayName("스터디 일정 수정 실패 - 중복된 시간대의 일정이 존재할 경우")
    void updateStudySchedule_Fail_DuplicateSchedule() {
        // given
        when(studyGroupRepository.findById(anyLong())).thenReturn(Optional.of(studyGroup));
        StudySchedule schedule = mock(StudySchedule.class);
        when(schedule.getId()).thenReturn(2L);

        Member leader = mock(Member.class);
        when(leader.getId()).thenReturn(userId);
        when(studyGroup.getCreator()).thenReturn(leader);

        when(studyScheduleRepository.findById(anyLong())).thenReturn(Optional.of(schedule));
        when(studyScheduleRepository.existsByStudyGroupIdAndEndTimeGreaterThanAndStartTimeLessThan(
                anyLong(),
                eq(requestDto.getStartTime()),
                eq(requestDto.getEndTime())
        )).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> studyScheduleService.updateStudySchedule(1L, userId, 1L, requestDto))
                .isInstanceOf(ScheduleException.class)
                .hasMessageContaining(ScheduleErrorCode.DUPLICATE_SCHEDULE.getMessage());
    }

    @Test
    @DisplayName("스터디 일정 수정 실패 - 스터디 장이 아닌 경우")
    void updateStudySchedule_Fail_NotLeader() {
        // given
        Member notLeader = mock(Member.class);
        when(notLeader.getId()).thenReturn(999L);
        when(studyGroup.getCreator()).thenReturn(notLeader);
        when(studyGroupRepository.findById(anyLong())).thenReturn(Optional.of(studyGroup));

        // when & then
        assertThatThrownBy(() -> studyScheduleService.updateStudySchedule(1L, userId, 1L, requestDto))
                .isInstanceOf(ScheduleException.class)
                .hasMessageContaining(ScheduleErrorCode.UNAUTHORIZED_USER.getMessage());
    }

    @Test
    @DisplayName("스터디 일정 삭제 성공")
    void deleteStudySchedule_Success() {
        // given
        Long studyGroupId = 1L;
        Long scheduleId = 1L;

        when(studyGroup.getId()).thenReturn(studyGroupId);

        Member leader = mock(Member.class);
        when(leader.getId()).thenReturn(userId);
        when(studyGroup.getCreator()).thenReturn(leader);

        StudySchedule schedule = mock(StudySchedule.class);
        when(schedule.getStudyGroup()).thenReturn(studyGroup);
        when(studyScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));

        // when
        studyScheduleService.deleteStudySchedule(studyGroupId, userId, scheduleId);

        // then
        verify(memberStudyScheduleRepository).deleteAllByStudyScheduleId(schedule.getId());
        verify(studyScheduleRepository).delete(schedule);
    }

    @Test
    @DisplayName("스터디 일정 삭제 실패 - 존재하지 않는 스터디 일정")
    void deleteStudySchedule_Fail_StudyScheduleNotFound() {
        // given
        Long studyGroupId = 1L;
        Long scheduleId = 1L;

        when(studyScheduleRepository.findById(scheduleId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> studyScheduleService.deleteStudySchedule(studyGroupId, userId, scheduleId))
                .isInstanceOf(ScheduleException.class)
                .hasMessageContaining(ScheduleErrorCode.SCHEDULE_NOT_FOUND.getMessage());

        verify(studyScheduleRepository, never()).delete(any());
    }

    @Test
    @DisplayName("스터디 일정 삭제 실패 - 스터디 장이 아닌 경우")
    void deleteStudySchedule_Fail_NotLeader() {
        // given
        Long studyGroupId = 1L;
        Long scheduleId = 1L;

        Member notLeader = mock(Member.class);
        when(notLeader.getId()).thenReturn(999L);
        when(studyGroup.getCreator()).thenReturn(notLeader);
        when(studyGroup.getId()).thenReturn(studyGroupId);

        StudySchedule schedule = mock(StudySchedule.class);
        when(schedule.getStudyGroup()).thenReturn(studyGroup);
        when(studyScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));

        // when & then
        assertThatThrownBy(() -> studyScheduleService.deleteStudySchedule(studyGroupId, userId, scheduleId))
                .isInstanceOf(ScheduleException.class)
                .hasMessageContaining(ScheduleErrorCode.UNAUTHORIZED_USER.getMessage());

        verify(studyScheduleRepository, never()).delete(any());
    }
}
