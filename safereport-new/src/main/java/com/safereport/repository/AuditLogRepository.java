package com.safereport.repository;

import com.safereport.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // ✅ Fetch all audit logs with user (fixes LAZY loading issue)
    @Query(
        value = "SELECT a FROM AuditLog a LEFT JOIN FETCH a.user ORDER BY a.createdAt DESC",
        countQuery = "SELECT COUNT(a) FROM AuditLog a"
    )
    Page<AuditLog> findAllWithUser(Pageable pageable);

    // ✅ Fetch audit logs by userId with user details
    @Query(
        value = "SELECT a FROM AuditLog a LEFT JOIN FETCH a.user WHERE a.user.id = :userId ORDER BY a.createdAt DESC",
        countQuery = "SELECT COUNT(a) FROM AuditLog a WHERE a.user.id = :userId"
    )
    Page<AuditLog> findByUserIdWithUser(Long userId, Pageable pageable);
}