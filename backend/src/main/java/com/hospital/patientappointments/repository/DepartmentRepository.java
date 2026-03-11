package com.hospital.patientappointments.repository;

import com.hospital.patientappointments.model.Department;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findAllByOrderByIdAsc();

    boolean existsByNameAndParentId(String name, Long parentId);

    boolean existsByNameAndParentIdIsNull(String name);

    Optional<Department> findFirstByName(String name);
}