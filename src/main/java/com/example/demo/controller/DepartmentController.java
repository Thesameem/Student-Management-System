package com.example.demo.controller;

import com.example.demo.model.Department;
import com.example.demo.service.DepartmentService;
import com.example.demo.Response.ApiResponse;
import com.example.demo.dto.DepartmentDTO;
import com.example.demo.dto.StudentDTO;
import com.example.demo.model.Student;
import com.example.demo.dto.CourseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {
    @Autowired
    private DepartmentService departmentService;

    @GetMapping
    public List<DepartmentDTO> getAllDepartments() {
        return departmentService.getAllDepartments().stream()
            .map(this::mapDepartmentToDTO)
            .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDTO> getDepartmentById(@PathVariable Long id) {
        Optional<Department> deptOpt = departmentService.getDepartmentById(id);
        if (deptOpt.isPresent()) {
            return ResponseEntity.ok(mapDepartmentToDTO(deptOpt.get()));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public DepartmentDTO createDepartment(@RequestBody Department department) {
        Department saved = departmentService.saveDepartment(department);
        return mapDepartmentToDTO(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentDTO> updateDepartment(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        Optional<Department> existingDepartmentOpt = departmentService.getDepartmentById(id);
        if (existingDepartmentOpt.isPresent()) {
            Department department = existingDepartmentOpt.get();
            updates.forEach((key, value) -> {
                switch (key) {
                    case "name" -> department.setName((String) value);
                    case "description" -> department.setDescription((String) value);
                }
            });
            Department saved = departmentService.saveDepartment(department);
            return ResponseEntity.ok(mapDepartmentToDTO(saved));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteDepartment(@PathVariable Long id) {
        Optional<Department> deptOpt = departmentService.getDepartmentById(id);
        if (deptOpt.isPresent()) {
            Department dept = deptOpt.get();
            if (dept.getStudents() != null && !dept.getStudents().isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiResponse("Cannot delete department with students.", false));
            }
            departmentService.deleteDepartment(id);
            return ResponseEntity.ok(new ApiResponse("Department deleted successfully", true));
        }
        return ResponseEntity.notFound().build();
    }

    // --- Mapping methods ---
    private DepartmentDTO mapDepartmentToDTO(Department dept) {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(dept.getId());
        dto.setName(dept.getName());
        dto.setDescription(dept.getDescription());
        // Force fetch students if lazy
        if (dept.getStudents() != null) {
            dept.getStudents().size(); // Force initialization
            List<StudentDTO> studentDTOs = dept.getStudents().stream()
                .map(this::mapStudentToDTO)
                .toList();
            dto.setStudents(studentDTOs);
        }
        if (dept.getCourses() != null) {
            dept.getCourses().size();
            List<CourseDTO> courseDTOs = dept.getCourses().stream().map(course -> {
                CourseDTO cDto = new CourseDTO();
                cDto.setId(course.getId());
                cDto.setName(course.getName());
                cDto.setDescription(course.getDescription());
                // Optionally set department in course DTO
                return cDto;
            }).toList();
            dto.setCourses(courseDTOs);
        }
        return dto;
    }

    private StudentDTO mapStudentToDTO(Student student) {
        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setName(student.getName());
        dto.setLevel(student.getLevel());
        dto.setRoll(student.getRoll());
        dto.setAddress(student.getAddress());
        dto.setPhone(student.getPhone());
        // Do NOT set department in student DTO here to avoid recursion
        return dto;
    }
} 