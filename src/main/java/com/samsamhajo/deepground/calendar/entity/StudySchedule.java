package com.samsamhajo.deepground.calendar.entity;

import com.samsamhajo.deepground.calendar.exception.ScheduleErrorCode;
import com.samsamhajo.deepground.calendar.exception.ScheduleException;
import com.samsamhajo.deepground.communityPlace.entity.SpecificAddress;
import com.samsamhajo.deepground.global.BaseEntity;
import com.samsamhajo.deepground.studyGroup.entity.StudyGroup;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@SQLRestriction("is_deleted = false")
@Table(name = "study_schedules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudySchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_schedule_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_group_id")
    private StudyGroup studyGroup;

    @Column(name = "schedule_title", nullable = false)
    private String title;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "location")
    private String location;

    /**
     * 스터디 일정 생성 시, 장소를 지정하고 위도, 경도 값을 가지고 있어야 리뷰 생성 시 값을 가져올 수 있어 컬럼 추가
     * 스터디 일정 생성, 수정 시 위도, 경도 값을 가지고 올 수 있도록 Service 로직도 수정
     */
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specific_address_id")
    private SpecificAddress specificAddress;



    private StudySchedule(StudyGroup studyGroup, String title,
                          LocalDateTime startTime, LocalDateTime endTime,
                          String description, String location,
                          Double latitude, Double longitude, SpecificAddress specificAddress) {
        this.studyGroup = studyGroup;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.specificAddress = specificAddress;
    }

    // location이 있는 경우
    public static StudySchedule of(StudyGroup studyGroup, String title, LocalDateTime startTime, LocalDateTime endTime, String description, String location, Double latitude, Double longitude, SpecificAddress specificAddress) {
        if (description == null || description.trim().isEmpty()) {
            throw new ScheduleException(ScheduleErrorCode.MISSING_REQUIRED_FIELDS);
        }
        return new StudySchedule(studyGroup, title, startTime, endTime, description, location, latitude, longitude, specificAddress);
    }

    // location이 없는 경우
    public static StudySchedule of(StudyGroup studyGroup, String title, LocalDateTime startTime, LocalDateTime endTime, String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new ScheduleException(ScheduleErrorCode.MISSING_REQUIRED_FIELDS);
        }
        return new StudySchedule(studyGroup, title, startTime, endTime, description, null, null, null, null);
    }

    public void update(String title, LocalDateTime startTime, LocalDateTime endTime, String description, String location, Double latitude, Double longitude, SpecificAddress specificAddress) {
        if (startTime.isAfter(endTime)) {
            throw new ScheduleException(ScheduleErrorCode.INVALID_DATE_RANGE);
        }

        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.specificAddress = specificAddress;
    }
}
