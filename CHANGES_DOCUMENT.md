# Codebase Changes & System Hardening Document

This document provides a comprehensive log of all architectural fixes, bug resolutions, security hardening, and CRUD completions implemented across the Hospital backend system.

---

## 1. Architectural & Entity Refactoring

### 1.1 Doctor Entity Redesign (`Doctor.java`)
* **Previous State**: The `Doctor` entity was annotated with `@Table(name = "users")`, causing a critical JPA/Hibernate entity collision with `User.java`. Its identifier was an ungenerated primitive `int doctor_id`, and it had broken relationships.
* **Changes Made**:
  * Moved `Doctor` to its own isolated database table: `@Table(name = "doctors")`.
  * Configured primary key: `@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;`.
  * Included explicitly requested domain fields:
    * `id` (`Long`)
    * `name` (`String`, not null)
    * `specialization` (`String`, not null)
    * `qualification` (`String`)
    * `experience` (`Integer` in years)
    * imageUrl (String, max 500 characters, mapped to column image_url)
  * Retained optional lazy associations to `Department` and `User` for authentication and hospital directory linkage.
  * Added database indexes on `name` and `specialization` for high-performance querying.

### 1.2 Visit Note Entity Fix (`VisitNote.java`)
* **Previous State**: 
  * Annotated with `@Lob` on `String notes`. In PostgreSQL, `@Lob` maps strings to `oid` (Large Object) instead of `TEXT`. Accessing `oid` outside an active LOB transaction throws `PSQLException: Large Objects may not be used in auto-commit mode`.
  * Had a typo in index definition: `@Index(name = "idx_visit_note_doctvor")`.
* **Changes Made**:
  * Replaced `@Lob` with `@Column(columnDefinition = "TEXT", nullable = false)`.
  * Corrected index name to `idx_visit_note_doctor`.

### 1.3 Department Entity Hardening (`Department.java`)
* **Changes Made**:
  * Added database-level unique constraint `uk_department_name` on the `name` column to prevent duplicate departments under concurrent creation requests.

---

## 2. Complete CRUD Implementation Across All Modules

### 2.1 Doctor Module (`/api/doctors`)
* **Create (C)**: Added `POST /api/doctors` using `CreateDoctorRequest`.
* **Read (R)**:
  * **Resolved Ambiguous Mapping**: Fixed Spring MVC route collision where `/{id}` and `/{spe}` were conflicting.
  * Added `GET /api/doctors/{id}` (Find by ID).
  * Added `GET /api/doctors/name/{name}` (Case-insensitive substring search by Name).
  * Added `GET /api/doctors/specialization/{specialization}` (Search by Specialization returning `List<DoctorResponse>`).
  * Added `GET /api/doctors?name=...&specialization=...` (Combined multi-field filtering).
* **Update (U)**: Added `PUT /api/doctors/{id}` using `UpdateDoctorRequest`.
* **Delete (D)**: Added `DELETE /api/doctors/{id}` (Admin only).
* **Image URL Integration**: Supported imageUrl and image_url across CreateDoctorRequest, UpdateDoctorRequest, and DoctorResponse. Added dedicated PATCH /api/doctors/{id}/image endpoint and service method DoctorService.updateDoctorImageUrl with protocol and length validation.

### 2.2 Appointment Module (`/api/appointments`)
* **Doctor View**: Added `GET /api/appointments/doctor/my` allowing doctors to view their schedule.
* **Staff/Admin View**: Added `GET /api/appointments?status={status}` for hospital reception and administrators.
* **Update / Reschedule**: Implemented `PUT /api/appointments/{id}` with `UpdateAppointmentRequest` with conflict checking.
* **Status Transitions**: Implemented `PATCH /api/appointments/{id}/status` with `UpdateAppointmentStatusRequest` allowing doctors and staff to mark appointments as `COMPLETED`.
* **Delete**: Added `DELETE /api/appointments/{id}` (Admin only).
* **Debug Artifact Removal**: Removed leftover `System.out.println` from `AppointmentController.java`.

### 2.3 User & Admin Module (`/api/admin/users`, `/api/users`)
* **Update User**: Added `PUT /api/admin/users/{id}` with `UpdateUserRequest` allowing admins to edit name, email, phone, role, and department.
* **Delete User**: Added `DELETE /api/admin/users/{id}`.
* **Profile Hardening**: Hardened `completeTheProfile` in `UserProfileService` to verify email and phone uniqueness before saving and ensure the user account is active.

### 2.4 Department Module (`/api/departments`)
* **Delete**: Added `DELETE /api/departments/{id}` in `DepartmentController` and `DepartmentService`.

### 2.5 Hospital Service Module (`/api/hospital-services`)
* **Delete**: Added `DELETE /api/hospital-services/{id}`.
* **Admin Visibility**: Added `activeOnly` query parameter to `GET /api/hospital-services` so administrators can list inactive services for reactivation.

### 2.6 Doctor Schedule Module (`/api/doctor-schedules`)
* **Delete**: Added `DELETE /api/doctor-schedules/{id}`.

### 2.7 Visit Note Module (`/api/visit-notes`)
* **Permission Enhancement**: Updated `getVisitNoteById` to allow the patient who owns the record to view their note, instead of restricting ID lookups strictly to doctors and admins.

---

## 3. Critical Security & Configuration Fixes

### 3.1 Invalid JWT Secret Base64 Padding (Startup Crash)
* **Problem**: `hospital.auth.jwt-secret-base64` had 43 characters (`U1w5WWcqpuUfLaAWysJsO+22LFywOwcY7KSXWcvhwP4`), causing `Base64.getDecoder().decode(secret)` to throw an `IllegalArgumentException` and crash the application on startup.
* **Fix**: Added proper Base64 4-byte padding `=` (`U1w5WWcqpuUfLaAWysJsO+22LFywOwcY7KSXWcvhwP4=`).

### 3.2 Non-Local Profile Crash (WhatsApp Configuration)
* **Problem**: `WhatsAppCloudApiSender` required `@Value("${whatsapp.api.otp-template-name}")`, which was missing from `application.properties`. Running on non-local profiles crashed immediately.
* **Fix**: Configured default `whatsapp.api.otp-template-name` and added environment variable fallbacks.

### 3.3 Missing CORS Configuration
* **Problem**: `SecurityConfig` specified `.cors(Customizer.withDefaults())` without registering a `CorsConfigurationSource` bean, causing cross-origin requests from frontends to fail.
* **Fix**: Created a `CorsConfigurationSource` bean allowing standard HTTP methods (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`) and headers.

### 3.4 OTP Verification Race Condition (Brute-Force Guard)
* **Problem**: Concurrent OTP verification requests could bypass the 5-attempt threshold because `attempts` check was not thread-safe.
* **Fix**: Added `@Lock(LockModeType.PESSIMISTIC_WRITE)` to `findTopByPhoneAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc` in `OtpChallengeRepository.java`.

### 3.5 System Bootstrapping (Initial Admin Provisioning)
* **Problem**: Public signup created only `PATIENT` roles, and `AdminService.createUser` rejected creating `ADMIN` roles. No admin could ever be created via API.
* **Fix**: Created `DataInitializer.java` (`CommandLineRunner`) to automatically initialize a default `ADMIN` user (`admin@hospital.com` / `+919999999999`) on first boot if no admin exists.

---

## 4. Edge Case Resolutions

### 4.1 Doctor Slot Availability Bug (Permanent Schedules)
* **Problem**: `AppointmentService.getAvailableSlots` executed a repository query comparing `validFrom <= :date` and `validUntil >= :date`. For permanent schedules where `validFrom` or `validUntil` is `null`, SQL evaluated this to `UNKNOWN` (false), returning zero available slots.
* **Fix**: Query active schedules by day of week and evaluate null-safe date boundaries in Java via `isScheduleValidOnDate`.

### 4.2 Centralized Exception Handling
* **Fix**: Created `GlobalExceptionHandler.java` (`@RestControllerAdvice`) to intercept:
  * `MethodArgumentNotValidException` -> 400 Bad Request with field-level errors map.
  * `ResponseStatusException` -> Standard status code and message.
  * `DataIntegrityViolationException` -> 409 Conflict with descriptive message.
  * `AccessDeniedException` -> 403 Forbidden.
  * General `Exception` -> 500 Internal Server Error without leaking internal stack traces.

### 4.3 Environment & Build Settings
* **Fix**: Adjusted `<java.version>` in `pom.xml` from `25` to the installed LTS `21`.
* **Fix**: Corrected package declaration of `CreateDoctorRequest.java` from `DTO.response` to `DTO.request`.
