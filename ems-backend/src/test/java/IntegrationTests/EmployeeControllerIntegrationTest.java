//package IntegrationTests;
//
//import net.coursework.ems_backend.EmsBackendApplication;
//import net.coursework.ems_backend.controller.EmployeeController;
//import net.coursework.ems_backend.dto.EmployeeDto;
//import net.coursework.ems_backend.entity.Employee;
//import net.coursework.ems_backend.repository.EmployeeRepository;
//import net.coursework.ems_backend.service.EmployeeService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.List;
//
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest(classes = EmsBackendApplication.class)
//@ActiveProfiles("test")  // Використовуємо test профіль, щоб підключити налаштування з application-test.properties
//public class EmployeeControllerIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private EmployeeRepository employeeRepository;
//
//    @MockBean
//    private EmployeeService employeeService;
//
//    @BeforeEach
//    public void setup() {
//        // Завжди працюємо з мокованим сервісом
//    }
//
//    @Test
//    public void testCreateEmployee() throws Exception {
//        EmployeeDto employeeDto = new EmployeeDto(null, "John", "Doe", "john.doe@example.com");
//
//        // Мокування відповіді сервісу
//        when(employeeService.createEmployee(any(EmployeeDto.class))).thenReturn(employeeDto);
//
//        mockMvc.perform(post("/api/employees")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{ \"firstName\": \"John\", \"lastName\": \"Doe\", \"email\": \"john.doe@example.com\" }"))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.firstName").value("John"))
//                .andExpect(jsonPath("$.lastName").value("Doe"));
//    }
//
//    @Test
//    public void testGetEmployeeById() throws Exception {
//        Employee employee = new Employee(null, "John", "Doe", "john.doe@example.com");
//        employeeRepository.save(employee);
//
//        when(employeeService.getEmployeeById(anyLong())).thenReturn(new EmployeeDto(employee.getId(), employee.getFirstName(), employee.getLastName(), employee.getEmail()));
//
//        mockMvc.perform(get("/api/employees/" + employee.getId()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.firstName").value("John"))
//                .andExpect(jsonPath("$.lastName").value("Doe"));
//    }
//
//    @Test
//    public void testDeleteEmployee() throws Exception {
//        Employee employee = new Employee(null, "John", "Doe", "john.doe@example.com");
//        employeeRepository.save(employee);
//
//        doNothing().when(employeeService).deleteEmployee(anyLong());
//
//        mockMvc.perform(delete("/api/employees/" + employee.getId()))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Employee deleted successfully!"));
//    }
//}
//



//package IntegrationTests;
//
//import net.coursework.ems_backend.EmsBackendApplication;
//import net.coursework.ems_backend.controller.EmployeeController;
//import net.coursework.ems_backend.dto.EmployeeDto;
//import net.coursework.ems_backend.service.EmployeeService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(EmployeeController.class)
////@SpringBootTest(classes = EmsBackendApplication.class)
//public class EmployeeControllerIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private EmployeeService employeeService;
//
//    @Test
//    public void testCreateEmployee() throws Exception {
//        EmployeeDto employeeDto = new EmployeeDto(1L, "John", "Doe", "john.doe@example.com");
//
//        when(employeeService.createEmployee(any(EmployeeDto.class))).thenReturn(employeeDto);
//
//        mockMvc.perform(post("/api/employees")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{ \"firstName\": \"John\", \"lastName\": \"Doe\", \"email\": \"john.doe@example.com\" }"))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.firstName").value("John"))
//                .andExpect(jsonPath("$.lastName").value("Doe"));
//    }
//
//    @Test
//    public void testGetEmployeeById() throws Exception {
//        EmployeeDto employeeDto = new EmployeeDto(1L, "John", "Doe", "john.doe@example.com");
//
//        when(employeeService.getEmployeeById(anyLong())).thenReturn(employeeDto);
//
//        mockMvc.perform(get("/api/employees/1"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.firstName").value("John"))
//                .andExpect(jsonPath("$.lastName").value("Doe"));
//    }
//
//    @Test
//    public void testDeleteEmployee() throws Exception {
//        doNothing().when(employeeService).deleteEmployee(anyLong());
//
//        mockMvc.perform(delete("/api/employees/1"))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Employee deleted successfully!"));
//    }
//}

package IntegrationTests;

import net.coursework.ems_backend.controller.EmployeeController;
import net.coursework.ems_backend.dto.EmployeeDto;
import net.coursework.ems_backend.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerIntegrationTest {

    @MockBean
    private EmployeeService employeeService;
    private MockMvc mockMvc;

    @Test
    public void testCreateEmployee() throws Exception {
        EmployeeDto employeeDto = new EmployeeDto(1L, "John", "Doe", "john.doe@example.com");
        when(employeeService.createEmployee(any(EmployeeDto.class))).thenReturn(employeeDto);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"firstName\": \"John\", \"lastName\": \"Doe\", \"email\": \"john.doe@example.com\" }"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    public void testGetEmployeeById() throws Exception {
        EmployeeDto employeeDto = new EmployeeDto(1L, "John", "Doe", "john.doe@example.com");
        when(employeeService.getEmployeeById(anyLong())).thenReturn(employeeDto);

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    public void testDeleteEmployee() throws Exception {
        doNothing().when(employeeService).deleteEmployee(anyLong());

        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Employee deleted successfully!"));
    }
}
