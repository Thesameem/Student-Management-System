package com.example.demo.controller;

import com.example.demo.model.Course;
import com.example.demo.model.Department;
import com.example.demo.model.Student;
import com.example.demo.dto.CourseDTO;
import com.example.demo.dto.DepartmentDTO;
import com.example.demo.dto.StudentDTO;
import com.example.demo.Response.ApiResponse;
import com.example.demo.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    @Autowired
    private CourseService courseService;

    @GetMapping
    public List<CourseDTO> getAllCourses() {
        return courseService.getAllCourses().stream()
            .map(this::mapCourseToDTO)
            .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
        Optional<Course> course = courseService.getCourseById(id);
        return course.map(c -> ResponseEntity.ok(mapCourseToDTO(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public CourseDTO createCourse(@RequestBody Map<String, Object> payload) {
        Course course = new Course();
        course.setName((String) payload.get("name"));
        course.setDescription((String) payload.get("description"));
        if (payload.get("departmentId") != null) {
            Long deptId = Long.valueOf(payload.get("departmentId").toString());
            Department dept = courseService.getDepartmentById(deptId);
            course.setDepartment(dept);
        }
        Course saved = courseService.saveCourse(course);
        return mapCourseToDTO(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> updateCourse(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        Optional<Course> existingCourseOpt = courseService.getCourseById(id);
        if (existingCourseOpt.isPresent()) {
            Course course = existingCourseOpt.get();
            updates.forEach((key, value) -> {
                switch (key) {
                    case "name" -> course.setName((String) value);
                    case "description" -> course.setDescription((String) value);
                    case "departmentId" -> {
                        Long deptId = Long.valueOf(value.toString());
                        Department dept = courseService.getDepartmentById(deptId);
                        course.setDepartment(dept);
                    }
                }
            });
            Course saved = courseService.saveCourse(course);
            return ResponseEntity.ok(mapCourseToDTO(saved));
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CourseDTO> patchCourse(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return updateCourse(id, updates);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteCourse(@PathVariable Long id) {
        if(courseService.getCourseById(id).isPresent()) {
            courseService.deleteCourse(id);
            return ResponseEntity.ok(new ApiResponse("Course deleted successfully", true));
        }
        return ResponseEntity.notFound().build();
    }

    // --- Mapping methods ---
    private CourseDTO mapCourseToDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setDescription(course.getDescription());
        if (course.getDepartment() != null) {
            DepartmentDTO deptDTO = new DepartmentDTO();
            deptDTO.setId(course.getDepartment().getId());
            deptDTO.setName(course.getDepartment().getName());
            deptDTO.setDescription(course.getDepartment().getDescription());
            dto.setDepartment(deptDTO);
        }
        if (course.getStudents() != null) {
            List<StudentDTO> studentDTOs = course.getStudents().stream().map(student -> {
                StudentDTO sDto = new StudentDTO();
                sDto.setId(student.getId());
                sDto.setName(student.getName());
                sDto.setLevel(student.getLevel());
                sDto.setRoll(student.getRoll());
                sDto.setAddress(student.getAddress());
                sDto.setPhone(student.getPhone());
                // Optionally set department/course in student DTO
                return sDto;
            }).toList();
            dto.setStudents(studentDTOs);
        }
        return dto;
    }
} 