package com.example.Hospital;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TestRunner {

    public static void main(String[] args) {
        int passed = 0;
        int failed = 0;

        System.out.println("==================================================");
        System.out.println("Running Test Suites");
        System.out.println("==================================================");

        // 1. Run Unit Tests (PackageServiceTest, PackageEnquiryServiceTest & DoctorServiceTest)
        Class<?>[] unitTestClasses = {PackageServiceTest.class, PackageEnquiryServiceTest.class, DoctorServiceTest.class};
        for (Class<?> clazz : unitTestClasses) {
            System.out.println("\n--- Running Unit Test: " + clazz.getSimpleName() + " ---");
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.isAnnotationPresent(org.junit.jupiter.api.Test.class)) {
                    try {
                        Object instance = clazz.getDeclaredConstructor().newInstance();
                        AutoCloseable mocks = org.mockito.MockitoAnnotations.openMocks(instance);

                        // Run @BeforeEach if present
                        for (Method setup : clazz.getDeclaredMethods()) {
                            if (setup.isAnnotationPresent(org.junit.jupiter.api.BeforeEach.class)) {
                                setup.setAccessible(true);
                                setup.invoke(instance);
                            }
                        }
                        
                        m.setAccessible(true);
                        m.invoke(instance);

                        // Run @AfterEach if present
                        for (Method tearDown : clazz.getDeclaredMethods()) {
                            if (tearDown.isAnnotationPresent(org.junit.jupiter.api.AfterEach.class)) {
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

        // 2. Boot Application Context and Run HospitalFlowIntegrationTests
        System.out.println("\n--- Running Integration Suite: HospitalFlowIntegrationTests ---");
        ConfigurableApplicationContext context = null;
        try {
            System.setProperty("spring.profiles.active", "local");
            context = SpringApplication.run(HospitalApplication.class);
            WebApplicationContext wac = (WebApplicationContext) context;

            HospitalFlowIntegrationTests testInstance = new HospitalFlowIntegrationTests();

            // Inject WebApplicationContext
            Field wacField = HospitalFlowIntegrationTests.class.getDeclaredField("webApplicationContext");
            wacField.setAccessible(true);
            wacField.set(testInstance, wac);

            // Setup MockMvc
            Method setupMethod = HospitalFlowIntegrationTests.class.getDeclaredMethod("setup");
            setupMethod.setAccessible(true);
            setupMethod.invoke(testInstance);

            // Execute ordered tests
            String[] orderedMethods = {
                    "testMedicineCrudFlow",
                    "testDeviceRegistrationFlow",
                    "testDocumentUploadAndDownloadFlow",
                    "testOrderPlacementAndCancellationFlow",
                    "testNotificationFlow",
                    "testEnquiryFlow",
                    "testFaqFlow",
                    "testCleanupTeardown"
            };

            for (String methodName : orderedMethods) {
                try {
                    Method m = HospitalFlowIntegrationTests.class.getDeclaredMethod(methodName);
                    m.setAccessible(true);
                    setupMethod.invoke(testInstance); // re-verify mockMvc setup
                    m.invoke(testInstance);
                    System.out.println("  PASS: " + methodName);
                    passed++;
                } catch (Exception ex) {
                    System.err.println("  FAIL: " + methodName + " - " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()));
                    if (ex.getCause() != null) ex.getCause().printStackTrace(System.err);
                    failed++;
                }
            }

        } catch (Exception e) {
            System.err.println("Failed to start application context for HospitalFlowIntegrationTests: " + e.getMessage());
            e.printStackTrace(System.err);
            failed++;
        } finally {
            if (context != null) {
                context.close();
            }
        }

        System.out.println("\n==================================================");
        System.out.println("Test Execution Summary:");
        System.out.println("Total Tests Run: " + (passed + failed));
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("==================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
