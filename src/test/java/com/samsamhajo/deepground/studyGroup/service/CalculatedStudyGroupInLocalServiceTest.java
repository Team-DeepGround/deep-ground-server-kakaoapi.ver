package com.samsamhajo.deepground.studyGroup.service;

import com.samsamhajo.deepground.address.dto.AddressDto;
import com.samsamhajo.deepground.address.service.AddressService;
import com.samsamhajo.deepground.studyGroup.dto.CalculatedStudyGroupInLocalResponse;
import com.samsamhajo.deepground.studyGroup.dto.CalculatedStudyGroupsInLocalResponse;
import com.samsamhajo.deepground.studyGroup.dto.CalculatedStudyGroupsInLocalResultDto;
import com.samsamhajo.deepground.studyGroup.repository.StudyGroupAddressRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CalculatedStudyGroupInLocalServiceTest {

    @Mock
    private AddressService addressService;
    @Mock
    private StudyGroupAddressRepository studyGroupAddressRepository;
    @InjectMocks
    private CalculatedStudyGroupInLocalService calculatedStudyGroupInLocalService;

    @Nested
    @DisplayName("성공 케이스")
    class Success {
        @Test
        @DisplayName("도시와 구로 스터디 그룹 목록을 정상적으로 조회한다")
        void getStudyGroupsInLocal_success() {
            // given
            String city = "서울";
            String gu = "강남구";
            AddressDto address1 = AddressDto.builder().id(1L).city(city).gu(gu).dong("역삼동").build();
            AddressDto address2 = AddressDto.builder().id(2L).city(city).gu(gu).dong("삼성동").build();
            List<AddressDto> addressDtoList = List.of(address1, address2);
            given(addressService.getDongsByCityAndGuUsingLike(city, gu)).willReturn(addressDtoList);

            CalculatedStudyGroupsInLocalResultDto result1 = new CalculatedStudyGroupsInLocalResultDto(2L, 1L);
            CalculatedStudyGroupsInLocalResultDto result2 = new CalculatedStudyGroupsInLocalResultDto(1L, 2L);
            List<CalculatedStudyGroupsInLocalResultDto> resultList = List.of(result1, result2);
            given(studyGroupAddressRepository.countStudyGroupByAddressIdsGroupByAddressId(anyList())).willReturn(resultList);

            // when
            CalculatedStudyGroupsInLocalResponse response = calculatedStudyGroupInLocalService.getStudyGroupsInLocal(city, gu);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getCalculatedStudyGroups()).hasSize(2);
            assertThat(response.getCalculatedStudyGroups().get(0).getCount()).isEqualTo(2L);
            assertThat(response.getCalculatedStudyGroups().get(0).getAddress().getDong()).isEqualTo("역삼동");
            assertThat(response.getCalculatedStudyGroups().get(1).getCount()).isEqualTo(1L);
            assertThat(response.getCalculatedStudyGroups().get(1).getAddress().getDong()).isEqualTo("삼성동");
        }
    }

    @Nested
    @DisplayName("실패 케이스")
    class Failure {
        @Test
        @DisplayName("주소에 해당하는 동이 없을 때 빈 결과를 반환한다")
        void getStudyGroupsInLocal_noAddress() {
            // given
            String city = "서울";
            String gu = "강남구";
            given(addressService.getDongsByCityAndGuUsingLike(city, gu)).willReturn(List.of());
            given(studyGroupAddressRepository.countStudyGroupByAddressIdsGroupByAddressId(anyList())).willReturn(List.of());

            // when
            CalculatedStudyGroupsInLocalResponse response = calculatedStudyGroupInLocalService.getStudyGroupsInLocal(city, gu);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getCalculatedStudyGroups()).isEmpty();
        }
    }
} 