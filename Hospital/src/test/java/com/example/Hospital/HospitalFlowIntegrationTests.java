package com.example.Hospital;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HospitalFlowIntegrationTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    private static Long medicineId;
    private static Long documentId;
    private static Long orderId;
    private static Long enquiryId;
    private static Long faqId;
    private static final Long TEST_PATIENT_ID = 888L;
    private static final String TEST_DEVICE_TOKEN = "e2e_token_" + System.currentTimeMillis();
    private static final String TEST_MEDICINE_NAME = "E2EAntibiotic_" + System.currentTimeMillis();

    @Test
    @Order(1)
    void testMedicineCrudFlow() throws Exception {
        // 1. Create Medicine with initial stock
        String createMedJson = String.format("""
            {
              "medicineName": "%s",
              "description": "Integration test antibiotic",
              "price": 25.50,
              "requiresPrescription": true,
              "active": true,
              "quantity": 100
            }
            """, TEST_MEDICINE_NAME);

        MvcResult createResult = mockMvc.perform(post("/api/medicines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createMedJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.medicineName").value(TEST_MEDICINE_NAME))
                .andExpect(jsonPath("$.quantity").value(100))
                .andReturn();

        medicineId = ((Number) JsonPath.read(createResult.getResponse().getContentAsString(), "$.medicineId")).longValue();

        // 2. Read single active medicine
        mockMvc.perform(get("/api/medicines/" + medicineId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(25.50));

        // 3. Update price
        mockMvc.perform(patch("/api/medicines/" + medicineId + "/price").param("price", "29.99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(29.99));
    }

    @Test
    @Order(2)
    void testDeviceRegistrationFlow() throws Exception {
        String registerDeviceJson = String.format("""
            {
              "userId": %d,
              "token": "%s",
              "platform": "ANDROID"
            }
            """, TEST_PATIENT_ID, TEST_DEVICE_TOKEN);

        mockMvc.perform(post("/api/devices/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerDeviceJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.platform").value("ANDROID"));

        // Query user's registered devices
        mockMvc.perform(get("/api/devices/user/" + TEST_PATIENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].platform").value("ANDROID"));
    }

    @Test
    @Order(3)
    void testDocumentUploadAndDownloadFlow() throws Exception {
        // Valid PDF file header (%PDF-1.5)
        byte[] pdfBytes = "%PDF-1.5 Sample test content for prescription verification".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "prescription_test.pdf", "application/pdf", pdfBytes);

        MvcResult uploadResult = mockMvc.perform(multipart("/api/patients/" + TEST_PATIENT_ID + "/documents")
                        .file(file)
                        .param("documentType", "PRESCRIPTION")
                        .param("uploadedById", String.valueOf(TEST_PATIENT_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documentType").value("PRESCRIPTION"))
                .andExpect(jsonPath("$.patientId").value(TEST_PATIENT_ID))
                .andReturn();

        documentId = ((Number) JsonPath.read(uploadResult.getResponse().getContentAsString(), "$.id")).longValue();

        // Get Document Metadata
        mockMvc.perform(get("/api/documents/" + documentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(documentId));

        // Download Document
        mockMvc.perform(get("/api/documents/" + documentId + "/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    @Order(4)
    void testOrderPlacementAndCancellationFlow() throws Exception {
        String idempotencyKey = "key_" + System.currentTimeMillis();
        String orderJson = String.format("""
            {
              "patientId": %d,
              "items": [
                {
                  "medicineId": %d,
                  "quantity": 5
                }
              ],
              "prescriptionDocumentId": %d,
              "deliveryAddress": "456 Test Lane, Health City",
              "contactPhone": "+919876543210"
            }
            """, TEST_PATIENT_ID, medicineId, documentId);

        MvcResult orderResult = mockMvc.perform(post("/api/medicine-orders")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(149.95))
                .andReturn();

        orderId = ((Number) JsonPath.read(orderResult.getResponse().getContentAsString(), "$.id")).longValue();

        // Verify stock decreased from 100 to 95
        mockMvc.perform(get("/api/medicines/" + medicineId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(95));

        // Patient cancels the order
        mockMvc.perform(post("/api/medicine-orders/" + orderId + "/cancel")
                        .param("patientId", String.valueOf(TEST_PATIENT_ID))
                        .param("reason", "Decided to postpone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // Verify stock is refunded back to 100
        mockMvc.perform(get("/api/medicines/" + medicineId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(100));
    }

    @Test
    @Order(5)
    void testNotificationFlow() throws Exception {
        // Check unread count
        mockMvc.perform(get("/api/notifications/unread-count").param("userId", String.valueOf(TEST_PATIENT_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));

        // Bulk mark all notifications read
        mockMvc.perform(patch("/api/notifications/read-all").param("userId", String.valueOf(TEST_PATIENT_ID)))
                .andExpect(status().isNoContent());

        // Verify unread count is now 0
        mockMvc.perform(get("/api/notifications/unread-count").param("userId", String.valueOf(TEST_PATIENT_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(0));
    }

    @Test
    @Order(6)
    void testEnquiryFlow() throws Exception {
        String enquiryJson = String.format("""
            {
              "patientId": %d,
              "patientName": "Test Patient",
              "patientEmail": "testpatient@hospital.com",
              "patientPhone": "+919876543210",
              "enquiryType": "CONTACT",
              "message": "Do you offer MRI scans on weekends?"
            }
            """, TEST_PATIENT_ID);

        MvcResult result = mockMvc.perform(post("/api/enquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(enquiryJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("NEW"))
                .andReturn();

        enquiryId = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();

        // Staff responds
        String updateJson = """
            {
              "status": "RESPONDED",
              "staffResponse": "Yes, our radiology department is open on Saturdays and Sundays."
            }
            """;

        mockMvc.perform(patch("/api/enquiries/" + enquiryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESPONDED"))
                .andExpect(jsonPath("$.staffResponse").value("Yes, our radiology department is open on Saturdays and Sundays."))
                .andExpect(jsonPath("$.respondedAt").isNotEmpty());
    }

    @Test
    @Order(7)
    void testFaqFlow() throws Exception {
        String faqQuestion = "Emergency admission procedure " + System.currentTimeMillis();
        String faqJson = String.format("""
            {
              "question": "%s",
              "answer": "Proceed directly to the 24/7 ER entrance at Gate 2.",
              "category": "Emergency"
            }
            """, faqQuestion);

        MvcResult result = mockMvc.perform(post("/api/faqs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(faqJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.question").value(faqQuestion))
                .andReturn();

        faqId = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.faqId")).longValue();

        // Read single FAQ
        mockMvc.perform(get("/api/faqs/" + faqId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.faqId").value(faqId));
    }

    @Test
    @Order(8)
    void testCleanupTeardown() throws Exception {
        if (faqId != null) {
            mockMvc.perform(delete("/api/faqs/" + faqId)).andExpect(status().isNoContent());
        }
        if (enquiryId != null) {
            mockMvc.perform(delete("/api/enquiries/" + enquiryId)).andExpect(status().isNoContent());
        }
        if (documentId != null) {
            mockMvc.perform(delete("/api/documents/" + documentId)).andExpect(status().isNoContent());
        }
        if (TEST_DEVICE_TOKEN != null) {
            mockMvc.perform(delete("/api/devices/token/" + TEST_DEVICE_TOKEN)).andExpect(status().isNoContent());
        }
        if (medicineId != null) {
            mockMvc.perform(delete("/api/medicines/" + medicineId)).andExpect(status().isNoContent());
        }
    }
}
