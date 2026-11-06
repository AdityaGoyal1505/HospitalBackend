package com.example.Hospital.Repository;

import com.example.Hospital.model.PackageEnquiry;
import com.example.Hospital.model.enums.PackageEnquiryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageEnquiryRepository extends JpaRepository<PackageEnquiry, Long> {

    List<PackageEnquiry> findAllByOrderByCreatedAtDesc();

    List<PackageEnquiry> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<PackageEnquiry> findByStatusOrderByCreatedAtDesc(PackageEnquiryStatus status);

    List<PackageEnquiry> findByPhoneOrderByCreatedAtDesc(String phone);

    List<PackageEnquiry> findByPackageNameIgnoreCaseOrderByCreatedAtDesc(String packageName);
}
