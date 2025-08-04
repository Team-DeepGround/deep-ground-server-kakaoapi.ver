package com.samsamhajo.deepground.studyGroup.service;

import com.samsamhajo.deepground.address.entity.Address;
import com.samsamhajo.deepground.address.repository.AddressRepository;
import com.samsamhajo.deepground.chat.entity.ChatRoom;
import com.samsamhajo.deepground.chat.entity.ChatRoomType;
import com.samsamhajo.deepground.chat.service.ChatRoomService;
import com.samsamhajo.deepground.member.entity.Member;
import com.samsamhajo.deepground.studyGroup.dto.StudyGroupCreateRequest;
import com.samsamhajo.deepground.studyGroup.dto.StudyGroupCreateResponse;
import com.samsamhajo.deepground.studyGroup.repository.StudyGroupAddressRepository;
import com.samsamhajo.deepground.studyGroup.repository.StudyGroupMemberRepository;
import com.samsamhajo.deepground.studyGroup.repository.StudyGroupRepository;
import com.samsamhajo.deepground.studyGroup.repository.StudyGroupTechTagRepository;
import com.samsamhajo.deepground.techStack.repository.TechStackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StudyGroupServiceTest {

    private StudyGroupRepository studyGroupRepository;
    private StudyGroupMemberRepository studyGroupMemberRepository;
    private ChatRoomService chatRoomService;
    private TechStackRepository techStackRepository;
    private StudyGroupTechTagRepository studyGroupTechTagRepository;
    private StudyGroupAddressRepository studyGroupAddressRepository;
    private AddressRepository addressRepository;
    private StudyGroupService studyGroupService;

    @BeforeEach
    void setUp() {
        studyGroupRepository = mock(StudyGroupRepository.class);
        studyGroupMemberRepository = mock(StudyGroupMemberRepository.class);
        chatRoomService = mock(ChatRoomService.class);
        techStackRepository = mock(TechStackRepository.class); // ✅ 추가
        studyGroupTechTagRepository = mock(StudyGroupTechTagRepository.class);
        addressRepository = mock(AddressRepository.class);

        studyGroupService = new StudyGroupService(
                studyGroupRepository,
                studyGroupMemberRepository,
                chatRoomService,
                techStackRepository, // ✅ 추가
                studyGroupTechTagRepository,
                addressRepository
        );
    }

    private StudyGroupCreateRequest validRequest() {
        LocalDate now = LocalDate.now();
        return StudyGroupCreateRequest.builder()
                .title("Java 스터디")
                .explanation("자바 스터디 모임입니다.")
                .studyStartDate(now.plusDays(10))
                .studyEndDate(now.plusDays(30))
                .recruitStartDate(now)
                .recruitEndDate(now.plusDays(5))
                .groupMemberCount(5)
                .isOffline(true)
                .addressIds(List.of(1L, 2L))
                .techStackNames(List.of("Java")) // 필수 필드
                .build();
    }

    @Test
    void createStudyGroup_success() {
        // given
        Member creator = mock(Member.class);
        StudyGroupCreateRequest request = validRequest();
        ChatRoom chatRoom = ChatRoom.of(ChatRoomType.STUDY_GROUP);

        Address address1 = mock(Address.class);
        Address address2 = mock(Address.class);

        when(chatRoomService.createStudyGroupChatRoom(any())).thenReturn(chatRoom);
        when(studyGroupRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(addressRepository.findAllById(request.getAddressIds()))
                .thenReturn(List.of(address1, address2));

        // when
        StudyGroupCreateResponse response = studyGroupService.createStudyGroup(request, creator);

        // then
        assertThat(response.getTitle()).isEqualTo(request.getTitle());
        assertThat(response.getIsOffline()).isTrue();

        verify(chatRoomService).createStudyGroupChatRoom(any());
        verify(studyGroupRepository, atLeastOnce()).save(any());
        verify(studyGroupMemberRepository).save(any());
        verify(addressRepository).findAllById(request.getAddressIds());
        verify(studyGroupAddressRepository, atLeastOnce()).save(any());
    }

    @Test
    void createStudyGroup_recruitEndDateInPast_throwsException() {
        // given
        Member creator = mock(Member.class);
        StudyGroupCreateRequest request = validRequest().toBuilder()
                .recruitEndDate(LocalDate.now().minusDays(1))
                .build();

        // when & then
        assertThatThrownBy(() -> studyGroupService.createStudyGroup(request, creator))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("모집 마감일은 현재 시점보다 미래여야 합니다.");
    }

    @Test
    void createStudyGroup_startDateAfterEndDate_throwsException() {
        // given
        Member creator = mock(Member.class);
        StudyGroupCreateRequest request = validRequest().toBuilder()
                .studyStartDate(LocalDate.now().plusDays(20))
                .studyEndDate(LocalDate.now().plusDays(10))
                .build();

        // when & then
        assertThatThrownBy(() -> studyGroupService.createStudyGroup(request, creator))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("스터디 시작일은 종료일보다 이전이어야 합니다.");
    }

    @Test
    void createStudyGroup_zeroMemberCount_throwsException() {
        // given
        Member creator = mock(Member.class);
        StudyGroupCreateRequest request = validRequest().toBuilder()
                .groupMemberCount(0)
                .build();

        // when & then
        assertThatThrownBy(() -> studyGroupService.createStudyGroup(request, creator))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("정원은 1명 이상이어야 합니다.");
    }
}
