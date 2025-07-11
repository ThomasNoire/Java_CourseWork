package net.coursework.ems_backend.repository;

import net.coursework.ems_backend.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;


public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
