package com.example.demo.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Response.ApiResponse;
import com.example.demo.model.Student;
import com.example.demo.service.StudentService;
import com.example.demo.dto.StudentDTO;
import com.example.demo.dto.DepartmentDTO;
import com.example.demo.dto.CourseDTO;
@RestController
@RequestMapping("/api/students")

public class StudentController {
    @Autowired
    private StudentService studentService;

    @GetMapping
    public List<StudentDTO> getAllStudents() {
        return studentService.getAllStudents().stream()
            .map(this::mapStudentToDTO)
            .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentDTO> getStudentById(@PathVariable Long id) {
        Optional<Student> student = studentService.getStudentById(id);
        return student.map(s -> ResponseEntity.ok(mapStudentToDTO(s)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public StudentDTO createStudent(@RequestBody Map<String, Object> payload) {
        Student student = new Student();
        student.setName((String) payload.get("name"));
        student.setLevel(payload.get("level") != null ? Integer.valueOf(payload.get("level").toString()) : null);
        student.setRoll(payload.get("roll") != null ? Integer.valueOf(payload.get("roll").toString()) : null);
        student.setAddress((String) payload.get("address"));
        student.setPhone((String) payload.get("phone"));
        if (payload.get("department") != null) {
            Map<String, Object> deptMap = (Map<String, Object>) payload.get("department");
            if (deptMap.get("id") != null) {
                Long deptId = Long.valueOf(deptMap.get("id").toString());
                com.example.demo.model.Department dept = studentService.getDepartmentById(deptId);
                student.setDepartment(dept);
            }
        }
        if (payload.get("courses") != null) {
            List<Map<String, Object>> courseList = (List<Map<String, Object>>) payload.get("courses");
            List<com.example.demo.model.Course> courses = new java.util.ArrayList<>();
            for (Map<String, Object> courseMap : courseList) {
                if (courseMap.get("id") != null) {
                    Long courseId = Long.valueOf(courseMap.get("id").toString());
                    com.example.demo.model.Course course = studentService.getCourseById(courseId);
                    if (course != null) courses.add(course);
                }
            }
            student.setCourses(courses);
        }
        Student saved = studentService.saveStudent(student);
        return mapStudentToDTO(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentDTO> updateStudent(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        Optional<Student> existingStudentOpt = studentService.getStudentById(id);
        if (existingStudentOpt.isPresent()) {
            Student student = existingStudentOpt.get();
            updates.forEach((key, value) -> {
                switch (key) {
                    case "name" -> student.setName((String) value);
                    case "level" -> {
                        if (value instanceof Integer) {
                            student.setLevel((Integer) value);
                        } else if (value instanceof String) {
                            try {
                                student.setLevel(Integer.parseInt((String) value));
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                    case "roll" -> {
                        if (value instanceof Integer) {
                            student.setRoll((Integer) value);
                        } else if (value instanceof String) {
                            try {
                                student.setRoll(Integer.parseInt((String) value));
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                    case "address" -> student.setAddress((String) value);
                    case "phone" -> student.setPhone((String) value);
                    case "department" -> {
                        if (value instanceof Map<?, ?> map) {
                            Object deptIdObj = map.get("id");
                            if (deptIdObj != null) {
                                Long deptId = null;
                                if (deptIdObj instanceof Integer) {
                                    deptId = ((Integer) deptIdObj).longValue();
                                } else if (deptIdObj instanceof Long) {
                                    deptId = (Long) deptIdObj;
                                } else if (deptIdObj instanceof String) {
                                    try {
                                        deptId = Long.parseLong((String) deptIdObj);
                                    } catch (NumberFormatException ignored) {}
                                }
                                if (deptId != null) {
                                    // Fetch department and set
                                    com.example.demo.model.Department dept = studentService.getDepartmentById(deptId);
                                    if (dept != null) student.setDepartment(dept);
                                }
                            }
                        }
                    }
                    case "courses" -> {
                        if (value instanceof List<?> list) {
                            List<com.example.demo.model.Course> courses = new java.util.ArrayList<>();
                            for (Object obj : list) {
                                if (obj instanceof Map<?, ?> courseMap) {
                                    Object courseIdObj = courseMap.get("id");
                                    if (courseIdObj != null) {
                                        Long courseId = null;
                                        if (courseIdObj instanceof Integer) {
                                            courseId = ((Integer) courseIdObj).longValue();
                                        } else if (courseIdObj instanceof Long) {
                                            courseId = (Long) courseIdObj;
                                        } else if (courseIdObj instanceof String) {
                                            try {
                                                courseId = Long.parseLong((String) courseIdObj);
                                            } catch (NumberFormatException ignored) {}
                                        }
                                        if (courseId != null) {
                                            com.example.demo.model.Course course = studentService.getCourseById(courseId);
                                            if (course != null) courses.add(course);
                                        }
                                    }
                                }
                            }
                            student.setCourses(courses);
                        }
                    }
                }
            });
            Student saved = studentService.saveStudent(student);
            return ResponseEntity.ok(mapStudentToDTO(saved));
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<StudentDTO> patchStudent(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return updateStudent(id, updates);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteStudent(@PathVariable Long id) {
        if(studentService.getStudentById(id).isPresent()) {
            studentService.deleteStudent(id);
            return ResponseEntity.ok(new ApiResponse("Student deleted successfully", true));
        }   
        return ResponseEntity.notFound().build();
    }

    // --- Mapping methods ---
    private StudentDTO mapStudentToDTO(Student student) {
        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setName(student.getName());
        dto.setLevel(student.getLevel());
        dto.setRoll(student.getRoll());
        dto.setAddress(student.getAddress());
        dto.setPhone(student.getPhone());
        if (student.getDepartment() != null) {
            DepartmentDTO deptDTO = new DepartmentDTO();
            deptDTO.setId(student.getDepartment().getId());
            deptDTO.setName(student.getDepartment().getName());
            deptDTO.setDescription(student.getDepartment().getDescription());
            dto.setDepartment(deptDTO);
        }
        if (student.getCourses() != null) {
            List<CourseDTO> courseDTOs = student.getCourses().stream().map(course -> {
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
}
