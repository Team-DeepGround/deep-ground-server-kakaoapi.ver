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
import com.samsamhajo.deepground.notification.entity.data.ScheduleNotificationData;
import com.samsamhajo.deepground.notification.event.NotificationEvent;
import com.samsamhajo.deepground.studyGroup.entity.StudyGroup;
import com.samsamhajo.deepground.studyGroup.entity.StudyGroupMember;
import com.samsamhajo.deepground.studyGroup.repository.StudyGroupMemberRepository;
import com.samsamhajo.deepground.studyGroup.repository.StudyGroupRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyScheduleService {

    private final StudyScheduleRepository studyScheduleRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final MemberStudyScheduleRepository memberStudyScheduleRepository;
    private final StudyGroupMemberRepository studyGroupMemberRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SpecificAddressRepository specificAddressRepository;
    private final GeometryFactory geometryFactory;

    @Transactional
    public StudyScheduleResponseDto createStudySchedule(Long studyGroupId,
                                                        Long userId,
                                                        StudyScheduleRequestDto requestDto) {

        StudyGroup studyGroup = validateStudyGroup(studyGroupId);
        validateSchedule(studyGroupId, requestDto);
        validateStudyLeader(userId, studyGroup);

        PlaceRequestDto placeDto = requestDto.getPlace();
        SpecificAddress specificAddress = null;
        if (placeDto != null) {
            specificAddress = specificAddressRepository.findByNameAndLocation(placeDto.getName(), placeDto.getAddress())
                    .orElseGet(() -> {
                        Point point = geometryFactory.createPoint(
                                new Coordinate(placeDto.getLongitude(), placeDto.getLatitude()));
                        SpecificAddress newAddress = SpecificAddress.of(
                                placeDto.getAddress(), point,
                                placeDto.getName(), placeDto.getPhone(), placeDto.getPlaceId());
                        return specificAddressRepository.save(newAddress);
                    });


        }

        StudySchedule studySchedule = StudySchedule.of(
                studyGroup,
                requestDto.getTitle(),
                requestDto.getStartTime(),
                requestDto.getEndTime(),
                requestDto.getDescription(),
                requestDto.getLocation(),
                requestDto.getLatitude(),
                requestDto.getLongitude(),
                specificAddress
        );

        StudySchedule savedSchedule = studyScheduleRepository.save(studySchedule);

        List<Member> members = new ArrayList<>(studyGroupMemberRepository
                .findAllByStudyGroupIdAndIsAllowedTrue(studyGroupId)
                .stream()
                .map(StudyGroupMember::getMember)
                .toList());

        Member creator = studyGroup.getCreator();
        if (members.stream().noneMatch(m -> m.getId().equals(creator.getId()))) {
            members.add(creator);
        }

        List<MemberStudySchedule> memberStudySchedules = members.stream()
                .map(member -> MemberStudySchedule.of(member, studySchedule, null, false, null))
                .toList();

        memberStudyScheduleRepository.saveAll(memberStudySchedules);

        // 스케줄 생성 알림
        eventPublisher.publishEvent(NotificationEvent.of(
                members.stream().map(Member::getId).toList(),
                ScheduleNotificationData.create(studySchedule)
        ));

        return StudyScheduleResponseDto.from(savedSchedule);
    }

    @Transactional(readOnly = true)
    public List<StudyScheduleResponseDto> findSchedulesByStudyGroupId(Long studyGroupId) {

        validateStudyGroup(studyGroupId);

        List<StudySchedule> schedules = studyScheduleRepository.findAllByStudyGroupId(studyGroupId);

        return schedules.stream()
                .map(StudyScheduleResponseDto::from)
                .toList();
    }

    @Transactional
    public StudyScheduleResponseDto updateStudySchedule(Long studyGroupId,
                                                        Long userId,
                                                        Long scheduleId,
                                                        StudyScheduleRequestDto requestDto) {

        StudyGroup studyGroup = validateStudyGroup(studyGroupId);
        validateStudyLeader(userId, studyGroup);

        StudySchedule studySchedule = studyScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        boolean isDuplicated = studyScheduleRepository.existsByStudyGroupIdAndEndTimeGreaterThanAndStartTimeLessThan(
                studyGroupId,
                requestDto.getStartTime(),
                requestDto.getEndTime()
        );

        // 자기 자신은 제외
        if (isDuplicated && !studySchedule.getId().equals(scheduleId)) {
            throw new ScheduleException(ScheduleErrorCode.DUPLICATE_SCHEDULE);
        }

        PlaceRequestDto placeDto = requestDto.getPlace();
        SpecificAddress specificAddress = null;
        if (placeDto != null) {
            specificAddress = specificAddressRepository.findByNameAndLocation(placeDto.getName(), placeDto.getAddress())
                    .orElseGet(() -> {
                        Point point = geometryFactory.createPoint(
                                new Coordinate(placeDto.getLongitude(), placeDto.getLatitude()));
                        return specificAddressRepository.save(
                                SpecificAddress.of(placeDto.getAddress(), point,
                                        placeDto.getName(), placeDto.getPhone(), placeDto.getPlaceId()));
                    });
        }

        studySchedule.update(
                requestDto.getTitle(),
                requestDto.getStartTime(),
                requestDto.getEndTime(),
                requestDto.getDescription(),
                requestDto.getLocation(),
                requestDto.getLatitude(),
                requestDto.getLongitude(),
                specificAddress
        );

        List<MemberStudySchedule> memberSchedules = memberStudyScheduleRepository.findByStudyScheduleId(scheduleId);
        for (MemberStudySchedule memberSchedule : memberSchedules) {
            memberSchedule.updateAvailable(null);
        }

        return StudyScheduleResponseDto.from(studySchedule);
    }

    @Transactional
    public void deleteStudySchedule(Long studyGroupId,
                                    Long userId,
                                    Long scheduleId) {
        StudySchedule schedule = studyScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        StudyGroup studyGroup = schedule.getStudyGroup();
        if (!studyGroup.getId().equals(studyGroupId)) {
            throw new ScheduleException(ScheduleErrorCode.MISMATCHED_GROUP);
        }

        validateStudyLeader(userId, studyGroup);

        memberStudyScheduleRepository.deleteAllByStudyScheduleId(schedule.getId());

        studyScheduleRepository.delete(schedule);
    }

    private StudyGroup validateStudyGroup(Long studyGroupId) {
        return studyGroupRepository.findById(studyGroupId)
                .orElseThrow(() -> new ScheduleException(ScheduleErrorCode.STUDY_GROUP_NOT_FOUND));
    }

    private void validateSchedule(Long studyGroupId, StudyScheduleRequestDto requestDto) {
        if (requestDto.getEndTime().isBefore(requestDto.getStartTime())) {
            throw new ScheduleException(ScheduleErrorCode.INVALID_DATE_RANGE);
        }
        boolean isDuplicated = studyScheduleRepository.existsByStudyGroupIdAndEndTimeGreaterThanAndStartTimeLessThan(
                studyGroupId,
                requestDto.getStartTime(),
                requestDto.getEndTime()
        );

        if (isDuplicated) {
            throw new ScheduleException(ScheduleErrorCode.DUPLICATE_SCHEDULE);

        }
    }
    private void validateStudyLeader(Long userId, StudyGroup studyGroup) {
        if (!studyGroup.getCreator().getId().equals(userId)) {
            throw new ScheduleException(ScheduleErrorCode.UNAUTHORIZED_USER);
        }
    }
}
