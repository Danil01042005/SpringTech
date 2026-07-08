package ru.danil.springtech.client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.danil.springtech.dto.PolicyDTO;

import java.util.Optional;
import java.util.UUID;

@org.springframework.cloud.openfeign.FeignClient(name = "medicine-client", url = "${api.medicine-url}")
public interface MedicineClient {

    @GetMapping("/{id}")
    PolicyDTO getPolicyByIdDTO(@PathVariable("id") UUID id);

    @PostMapping("/created")
    PolicyDTO createPolicyDTO(@RequestBody PolicyDTO policyDTO);
}
