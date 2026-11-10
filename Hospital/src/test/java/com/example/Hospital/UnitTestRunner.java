package com.example.Hospital;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;

public class UnitTestRunner {

    public static void main(String[] args) {
        int passed = 0;
        int failed = 0;

        System.out.println("==================================================");
        System.out.println("Running Unit Test Suites");
        System.out.println("==================================================");

        Class<?>[] unitTestClasses = {
                PackageServiceTest.class,
                PackageEnquiryServiceTest.class,
                DoctorServiceTest.class
        };

        for (Class<?> clazz : unitTestClasses) {
            System.out.println("\n--- Running Unit Test: " + clazz.getSimpleName() + " ---");
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.isAnnotationPresent(Test.class)) {
                    try {
                        Object instance = clazz.getDeclaredConstructor().newInstance();
                        AutoCloseable mocks = MockitoAnnotations.openMocks(instance);

                        // Run @BeforeEach if present
                        for (Method setup : clazz.getDeclaredMethods()) {
                            if (setup.isAnnotationPresent(BeforeEach.class)) {
                                setup.setAccessible(true);
                                setup.invoke(instance);
                            }
                        }

                        m.setAccessible(true);
                        m.invoke(instance);

                        // Run @AfterEach if present
                        for (Method tearDown : clazz.getDeclaredMethods()) {
                            if (tearDown.isAnnotationPresent(AfterEach.class)) {
                                tearDown.setAccessible(true);
                                tearDown.invoke(instance);
                            }
                        }
                        mocks.close();
                        System.out.println("  PASS: " + m.getName());
                        passed++;
                    } catch (Exception ex) {
                        System.err.println("  FAIL: " + m.getName() + " - " + ex.getCause());
                        if (ex.getCause() != null) ex.getCause().printStackTrace(System.err);
                        failed++;
                    }
                }
            }
        }

        System.out.println("\n==================================================");
        System.out.println("Unit Test Execution Summary:");
        System.out.println("Total Tests Run: " + (passed + failed));
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("==================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
