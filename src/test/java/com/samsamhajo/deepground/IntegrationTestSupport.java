package com.samsamhajo.deepground;

import com.samsamhajo.deepground.global.config.MongoConfig;
import com.samsamhajo.deepground.global.config.RedisConfig;
import com.samsamhajo.deepground.global.config.S3Config;
import com.samsamhajo.deepground.global.upload.S3Uploader;
import org.junit.jupiter.api.TestInstance;
import com.samsamhajo.deepground.support.TestRedisConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(TestRedisConfig.class)
public abstract class IntegrationTestSupport {

    @MockBean
    protected S3Config s3Config;


    @MockBean
    protected S3Uploader s3Uploader;

    @MockBean
    protected MongoConfig mongoConfig;

}
