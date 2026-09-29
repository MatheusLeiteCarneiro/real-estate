package com.mlcdev.realestate.auth.util;

import com.mlcdev.realestate.auth.TestcontainersConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@SpringBootTest
@ActiveProfiles({"dev", "test"})
@Import(TestcontainersConfiguration.class)
@ContextConfiguration(initializers = TestRsaKeysInitializer.class)
public @interface PostgresIntegrationTest {
}
