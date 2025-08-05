package com.samsamhajo.deepground.admin.service;

import com.samsamhajo.deepground.admin.dto.AdminDashboardStatsResponse;
import com.samsamhajo.deepground.communityPlace.repository.CommunityPlaceReviewRepository;
import com.samsamhajo.deepground.feed.feed.entity.Feed;
import com.samsamhajo.deepground.member.entity.Member;
import com.samsamhajo.deepground.member.repository.MemberRepository;
import com.samsamhajo.deepground.studyGroup.entity.StudyGroup;
import com.samsamhajo.deepground.studyGroup.repository.StudyGroupRepository;
import com.samsamhajo.deepground.feed.feed.repository.FeedRepository;
import com.samsamhajo.deepground.communityPlace.repository.SpecificAddressRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AdminServiceTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    private CommunityPlaceReviewRepository communityPlaceReviewRepository;

    @Autowired
    private SpecificAddressRepository specificAddressRepository;

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    private Member member;

    @BeforeEach
    void setup() {
        // Member
        member = Member.createLocalMember(
                "admin@example.com",
                "password123",
                "adminUser"
        );
        memberRepository.save(member);

        // Feed(Post)
        feedRepository.save(Feed.of("관리자 테스트 피드", member));

        // StudyGroup
        StudyGroup group = StudyGroup.of(
                null, "스터디 A", "설명",
                LocalDate.now(), LocalDate.now().plusDays(30),
                LocalDate.now(), LocalDate.now().plusDays(7),
                5, member, true
        );
        studyGroupRepository.save(group);
    }

    @Test
    void 대시보드_통계_정상_조회() {
        // when
        AdminDashboardStatsResponse stats = adminService.getDashboardStats();

        // then
        assertNotNull(stats);
        assertEquals(1L, stats.getTotalMembers());
        assertEquals(1L, stats.getNewMembersToday());
        assertEquals(1L, stats.getTotalPosts());
        assertEquals(0L, stats.getTotalReviews());
        assertEquals(0L, stats.getReviewsToday());
        assertEquals(1L, stats.getTotalStudyGroups());
    }
}
