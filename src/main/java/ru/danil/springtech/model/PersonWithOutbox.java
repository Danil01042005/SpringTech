package ru.danil.springtech.model;

import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.kafka.dto.RetryableTaskDTO;

public record PersonWithOutbox(RetryableTaskDTO retryableTaskDTO, PersonDTO personDTO) {}
