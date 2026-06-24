package ru.danil.springtech.util.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtech.model.DeferredJob;
import ru.danil.springtech.model.enums.DeferredJobStatus;
import ru.danil.springtech.service.DeferredJobService;
import ru.danil.springtech.util.JobHandler;

import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
public class DeferredJobScheduler {
    private final DeferredJobService deferredJobService;
    private final List<JobHandler<?>> handlers;

    @Scheduled(fixedDelayString = "${scheduler-config.delay}")
    @Transactional
    public void processDeferredJobs() {
        List<DeferredJob> jobs = deferredJobService.claimAndGetPendingJobs();
        for (DeferredJob job : jobs) {
            JobHandler<?> handler = handlers.stream()
                    .filter(h -> h.getSupportedEntity() == job.getEntityName())
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Нет обработчика для " + job.getEntityName()));
            try {
                handler.handle(job);
                job.setDeferredJobStatus(DeferredJobStatus.COMPLETED);
            } catch (Exception e) {
                log.error("Ошибка выполнения задачи {}: {}", job.getJobId(), e.getMessage());
                job.setDeferredJobStatus(DeferredJobStatus.FAILED);
            }
            deferredJobService.saveJob(job);
        }
    }
}
