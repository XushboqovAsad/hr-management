package uz.hrms;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uz.hrms.other.*;

@WebMvcTest(controllers = LmsController.class)
@AutoConfigureMockMvc(addFilters = false)
class LmsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LmsService lmsService;

    @MockBean
    private AccessPolicy accessPolicy;

    @MockBean
    private LocalFileStorageService localFileStorageService;

    @Test
    void listCoursesReturnsCatalog() throws Exception {
        UUID courseId = UUID.randomUUID();
        when(accessPolicy.hasPermission(any(), eq("LMS"), eq("READ"))).thenReturn(true);
        when(lmsService.listCourses(null, null, null)).thenReturn(List.of(
            new LmsCourseListItemResponse(courseId, "INTRO-1", "Вводный курс", "Safety", LmsCourseLevel.INTRODUCTORY, LmsCourseStatus.PUBLISHED, true, true, 60, false, false, true)
        ));

        mockMvc.perform(get("/api/v1/lms/courses"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(courseId.toString()))
            .andExpect(jsonPath("$[0].code").value("INTRO-1"));
    }

    @Test
    void createCourseReturnsCreatedEntity() throws Exception {
        UUID courseId = UUID.randomUUID();
        when(accessPolicy.hasPermission(any(), eq("LMS"), eq("WRITE"))).thenReturn(true);
        when(lmsService.createCourse(any())).thenReturn(
            new LmsCourseResponse(courseId, "INTRO-1", "Вводный курс", "desc", "Safety", LmsCourseLevel.INTRODUCTORY, LmsCourseStatus.PUBLISHED, true, true, 60, true, null, List.of(), List.of())
        );

        mockMvc.perform(post("/api/v1/lms/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      \"code\": \"INTRO-1\",
                      \"title\": \"Вводный курс\",
                      \"courseLevel\": \"INTRODUCTORY\",
                      \"status\": \"PUBLISHED\",
                      \"mandatoryForAll\": true,
                      \"introductoryCourse\": true,
                      \"estimatedMinutes\": 60,
                      \"certificateEnabled\": true,
                      \"modules\": [
                        {
                          \"title\": \"Модуль 1\",
                          \"moduleOrder\": 1,
                          \"required\": true,
                          \"lessons\": [
                            {
                              \"title\": \"Урок 1\",
                              \"lessonOrder\": 1,
                              \"contentType\": \"TEXT\",
                              \"contentText\": \"hello\",
                              \"durationMinutes\": 10,
                              \"required\": true
                            }
                          ]
                        }
                      ]
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(courseId.toString()))
            .andExpect(jsonPath("$.code").value("INTRO-1"));
    }
}
