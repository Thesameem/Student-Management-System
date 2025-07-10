package com.example.demo.dto;

import lombok.Data;
import java.util.List;

@Data
public class CourseDTO {
    private Long id;
    private String name;
    private String description;
    private DepartmentDTO department;
    private List<StudentDTO> students;
} 