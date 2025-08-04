package com.samsamhajo.deepground.email.service;

import com.samsamhajo.deepground.email.dto.EmailRequest;
import com.samsamhajo.deepground.email.dto.EmailResponse;
import com.samsamhajo.deepground.email.dto.EmailVerifyRequest;
import com.samsamhajo.deepground.email.exception.EmailErrorCode;
import com.samsamhajo.deepground.email.exception.EmailException;
import com.samsamhajo.deepground.email.repository.EmailVerificationRepository;
import com.samsamhajo.deepground.member.entity.Member;
import com.samsamhajo.deepground.member.entity.Role;
import com.samsamhajo.deepground.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private MemberRepository memberRepository;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_CODE = "123456";

    @BeforeEach
    void setup() {
        emailVerificationRepository.delete(TEST_EMAIL);

        Member member = Member.createLocalMember(TEST_EMAIL, "test-password", "tester");
        memberRepository.save(member);

        memberRepository.save(member);
    }

    @Test
    void 이메일_인증코드_전송_성공() {
        // given
        EmailRequest request = new EmailRequest(TEST_EMAIL);

        // when
        EmailResponse response = emailService.sendVerificationEmail(request);

        // then
        assertThat(response.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(response.isSuccess()).isTrue();
        assertThat(emailVerificationRepository.exists(TEST_EMAIL)).isTrue();
    }

    @Test
    void 이메일_인증코드_검증_성공() {
        // given
        emailVerificationRepository.save(TEST_EMAIL, TEST_CODE, 300L);
        EmailVerifyRequest request = new EmailVerifyRequest(TEST_EMAIL, TEST_CODE);

        // when
        assertDoesNotThrow(() -> emailService.verifyEmail(request));

        // then
        Member verifiedMember = memberRepository.findByEmail(TEST_EMAIL).orElseThrow();
        assertThat(verifiedMember.isVerified()).isTrue();
        assertThat(verifiedMember.getRole()).isEqualTo(Role.ROLE_USER);
    }

    @Test
    void 잘못된_인증코드로_검증_실패() {
        // given
        emailVerificationRepository.save(TEST_EMAIL, TEST_CODE, 300L);
        EmailVerifyRequest request = new EmailVerifyRequest(TEST_EMAIL, "wrong_code");

        // when
        EmailException exception = assertThrows(EmailException.class,
                () -> emailService.verifyEmail(request));

        // then
        assertEquals(EmailErrorCode.INVALID_VERIFICATION_CODE, exception.getErrorCode());
        assertThat(emailVerificationRepository.exists(TEST_EMAIL)).isTrue();
    }

    @Test
    void 만료된_인증코드로_검증_실패() {
        // given
        EmailVerifyRequest request = new EmailVerifyRequest(TEST_EMAIL, TEST_CODE);

        // when
        EmailException exception = assertThrows(EmailException.class,
                () -> emailService.verifyEmail(request));

        // then
        assertEquals(EmailErrorCode.VERIFICATION_CODE_EXPIRED, exception.getErrorCode());
    }

    @Test
    void 이메일_인증상태_확인() {
        // given
        String email = "unique-email@example.com";
        String code = "654321";
        Member member = Member.createLocalMember(email, "secure-password", "uniqueTester");
        memberRepository.save(member);
        emailVerificationRepository.save(email, code, 300L);

        // when & then
        assertThat(emailService.isVerified(email)).isFalse();
        EmailVerifyRequest request = new EmailVerifyRequest(email, code);
        emailService.verifyEmail(request);


        assertThat(emailService.isVerified(email)).isTrue();
    }

    @Test
    void 동일_이메일로_인증코드_재전송() {
        // given
        EmailRequest request = new EmailRequest(TEST_EMAIL);

        // when
        EmailResponse firstResponse = emailService.sendVerificationEmail(request);
        EmailResponse secondResponse = emailService.sendVerificationEmail(request);

        // then
        assertThat(firstResponse.isSuccess()).isTrue();
        assertThat(secondResponse.isSuccess()).isTrue();
        assertThat(emailVerificationRepository.exists(TEST_EMAIL)).isTrue();
    }
}
