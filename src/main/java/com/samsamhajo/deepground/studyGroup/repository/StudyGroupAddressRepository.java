package com.samsamhajo.deepground.studyGroup.repository;

import com.samsamhajo.deepground.studyGroup.dto.CalculatedStudyGroupsInLocalResultDto;
import com.samsamhajo.deepground.studyGroup.entity.StudyGroupAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudyGroupAddressRepository extends JpaRepository<StudyGroupAddress, Long> {

    @Query("""
        SELECT s.studyGroup.id
        FROM StudyGroupAddress s
        WHERE s.address.city = :city
          AND s.address.gu = :gu
          AND s.address.dong = :dong
    """)
    List<Long> findStudyGroupIdByAddress(String city, String gu, String dong);

    @Query(value = """
        SELECT new com.samsamhajo.deepground.studyGroup.dto.CalculatedStudyGroupsInLocalResultDto(
            COUNT(sga.address.id),
            sga.address.id
        )
        FROM StudyGroupAddress sga
        WHERE sga.address.id IN :addressIds
        GROUP BY sga.address.id
    """)
    List<CalculatedStudyGroupsInLocalResultDto> countStudyGroupByAddressIdsGroupByAddressId(@Param("addressIds") List<Long> addressIds);
}
