package ru.danil.springtech.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.danil.springtech.model.DeferredJob;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeferredJobRepository extends JpaRepository<DeferredJob, UUID> {
    @Modifying
    @Query(value = "UPDATE test.jobs SET job_status = 'PROCESSING', updated_at = NOW() WHERE job_status = 'PENDING' RETURNING *", nativeQuery = true)
    List<DeferredJob> claimAndGetPendingJobs();
}
