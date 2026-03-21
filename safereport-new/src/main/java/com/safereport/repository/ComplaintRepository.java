package com.safereport.repository;

import com.safereport.entity.Complaint;
import com.safereport.entity.User;
import com.safereport.enums.ComplaintStatus;
import com.safereport.enums.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    Optional<Complaint> findByTrackingId(String trackingId);

    Page<Complaint> findByComplainant(User complainant, Pageable pageable);

    Page<Complaint> findByAssignedAuthority(User authority, Pageable pageable);

    long countByStatus(ComplaintStatus status);

    @Query("SELECT c.category.name, COUNT(c) FROM Complaint c WHERE c.category IS NOT NULL GROUP BY c.category.name")
    List<Object[]> countByCategory();

    @Query("""
        SELECT c FROM Complaint c
        WHERE (:status IS NULL OR c.status = :status)
          AND (:priority IS NULL OR c.priority = :priority)
          AND (:categoryId IS NULL OR c.category.id = :categoryId)
          AND (:keyword IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(c.trackingId) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY c.createdAt DESC
        """)
    Page<Complaint> findWithFilters(
            @Param("status") ComplaintStatus status,
            @Param("priority") Priority priority,
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("""
        SELECT c FROM Complaint c
        WHERE c.assignedAuthority = :authority
          AND (:status IS NULL OR c.status = :status)
        ORDER BY c.createdAt DESC
        """)
    Page<Complaint> findByAuthorityWithStatus(
            @Param("authority") User authority,
            @Param("status") ComplaintStatus status,
            Pageable pageable);
}
