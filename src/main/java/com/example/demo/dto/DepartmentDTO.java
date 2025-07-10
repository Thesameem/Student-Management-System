package com.example.demo.dto;

import lombok.Data;
import java.util.List;

@Data
public class DepartmentDTO {
    private Long id;
    private String name;
    private String description;
    private List<StudentDTO> students;
    private List<CourseDTO> courses;
} 