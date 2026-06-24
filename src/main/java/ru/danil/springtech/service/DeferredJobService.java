package ru.danil.springtech.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtech.model.DeferredJob;
import ru.danil.springtech.model.enums.DeferredJobStatus;
import ru.danil.springtech.repository.DeferredJobRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeferredJobService {
    private final DeferredJobRepository deferredJobRepository;

    @Transactional
    public void createJob(DeferredJob newDeferredJob) {
        if (newDeferredJob.getDeferredJobStatus() == null) {
            newDeferredJob.setDeferredJobStatus(DeferredJobStatus.PENDING);
        }
        DeferredJob deferredJob = deferredJobRepository.save(newDeferredJob);
        log.debug("Создана задача: {}", deferredJob);
    }

    @Transactional
    public List<DeferredJob> claimAndGetPendingJobs() {
        return deferredJobRepository.claimAndGetPendingJobs();
    }

    @Transactional
    public void saveJob(DeferredJob deferredJob) {
        deferredJobRepository.save(deferredJob);
    }
}
