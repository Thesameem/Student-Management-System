package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class StudentDTO {
    private Long id;
    private String name;
    private Integer level;
    private Integer roll;
    private String address;
    private String phone;
    private DepartmentDTO department;
    private List<CourseDTO> courses;
} 