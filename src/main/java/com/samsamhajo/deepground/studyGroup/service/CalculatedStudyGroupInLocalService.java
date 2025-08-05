package com.samsamhajo.deepground.studyGroup.service;


import com.samsamhajo.deepground.address.dto.AddressDto;
import com.samsamhajo.deepground.address.service.AddressService;
import com.samsamhajo.deepground.studyGroup.dto.CalculatedStudyGroupInLocalResponse;
import com.samsamhajo.deepground.studyGroup.dto.CalculatedStudyGroupsInLocalResponse;
import com.samsamhajo.deepground.studyGroup.dto.CalculatedStudyGroupsInLocalResultDto;
import com.samsamhajo.deepground.studyGroup.repository.StudyGroupAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalculatedStudyGroupInLocalService {

    private final AddressService addressService;
    private final StudyGroupAddressRepository studyGroupAddressRepository;

    public CalculatedStudyGroupsInLocalResponse getStudyGroupsInLocal(String city, String gu) {
        // 주소의 아이디로 주소 목록을 분류하기
        Map<Long, AddressDto> addressDtoMap =
                addressService.getDongsByCityAndGuUsingLike(city, gu)
                        .stream().collect(Collectors.toMap(AddressDto::getId, dto -> dto));

        // 주소 아이디 목록
        List<Long> addressIds = new ArrayList<>(addressDtoMap.keySet());

        // 주소 아이디 별 스터디 그룹 카운트 집계 및 스터디 그룹 아이디 목록
        List<CalculatedStudyGroupsInLocalResultDto> result =
                getStudyGroupAddressesByAddressIds(addressIds);

        List<CalculatedStudyGroupInLocalResponse> calculatedStudyGroupInLocalResponses =
                result.stream().map(r -> (
                        CalculatedStudyGroupInLocalResponse.of(
                                r.getCount(),
                                addressDtoMap.get(r.getAddressId())
                        )
                )).toList();

        return CalculatedStudyGroupsInLocalResponse.of(calculatedStudyGroupInLocalResponses);
    }

    private List<CalculatedStudyGroupsInLocalResultDto> getStudyGroupAddressesByAddressIds(List<Long> addressIds) {
        return studyGroupAddressRepository.countStudyGroupByAddressIdsGroupByAddressId(addressIds);
    }
}
