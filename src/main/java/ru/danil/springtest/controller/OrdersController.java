package ru.danil.springtest.controller;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class OrdersController {

    private final ModelMapper modelMapper;

}
