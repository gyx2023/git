package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/student")
@RestController
public class StudentController {



    @GetMapping
    public String get(@RequestParam("gender") String gender,@RequestParam(value = "age",required = false,defaultValue = "39") Integer age){
        if (age == null){
            return "null age";
        }

        if (gender.equals("male") && age == 18){
            return "GYX";
        } else if (gender.equals("female") && age == 29) {
            return "LM";
        } else {
            return "not a human";
        }
    }
}
