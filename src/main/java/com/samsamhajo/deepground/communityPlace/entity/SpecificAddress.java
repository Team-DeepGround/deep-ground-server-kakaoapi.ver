package com.samsamhajo.deepground.communityPlace.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.samsamhajo.deepground.calendar.entity.StudySchedule;
import com.samsamhajo.deepground.global.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "specific_address")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpecificAddress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="specific_address_id")
    private Long id;

    @Column(name="specific_address_location")
    private String location;

    @Column(name="specific_address_place_name")
    private String name;

    @Column(name="specific_address_place_phone_number")
    private Long number;

    /**
     * POINT 클래스 명시를 해주고, SQLTypes.GEOMETRY라고 명시를 해준 후에
     * MYSQL에서 해당 컬럼은 POINT라고 지정 해줘야 POINT 클래스 인식 후 사용 가능
     */
    @Column(name = "specific_address_location_point", columnDefinition = "POINT")
    @JdbcTypeCode(SqlTypes.GEOMETRY)
    private Point locationPoint;

    @Column(name = "phone")
    private String phone;

    @Column(name = "place_url")
    private String placeUrl;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @OneToMany(mappedBy = "specificAddress", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudySchedule> studySchedules = new ArrayList<>();

    @OneToMany(mappedBy = "specificAddress", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<CommunityPlaceReview> communityPlaceReviews = new ArrayList<>();


    private SpecificAddress(String location,Point locationPoint,
                            String name, String phone, String placeUrl,
                            Double latitude, Double longitude){
        this.location = location;
        this.locationPoint = locationPoint;
        this.name = name;
        this.phone = phone;
        this.placeUrl = placeUrl;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static SpecificAddress of(String location, Point locationPoint,
                                     String name, String phone, String placeUrl) {
        return new SpecificAddress(
                location,
                locationPoint,
                name,
                phone,
                placeUrl,
                locationPoint.getY(), // 위도 = Y
                locationPoint.getX()  // 경도 = X
        );
    }

    public static SpecificAddress of(String location, Point locationPoint) {
        return new SpecificAddress(
                location,
                locationPoint,
                null,  // name
                null,  // phone
                null,  // placeUrl
                locationPoint.getY(),
                locationPoint.getX()
        );
    }
}
