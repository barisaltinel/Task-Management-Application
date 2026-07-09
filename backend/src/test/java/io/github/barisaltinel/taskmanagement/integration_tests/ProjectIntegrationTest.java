package io.github.barisaltinel.taskmanagement.integration_tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.barisaltinel.taskmanagement.model.Project;
import io.github.barisaltinel.taskmanagement.model.ProjectStatus;
import io.github.barisaltinel.taskmanagement.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProjectIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectRepository projectRepository;

    private Project testProject;

    @BeforeEach
    void setUp() {
        testProject = new Project();
        testProject.setTitle("Test Project");
        testProject.setDescription("Project for testing");
        testProject.setStatus(ProjectStatus.IN_PROGRESS);
        testProject.setDepartmentName("IT");
        testProject = requireProject(projectRepository.save(testProject), "Saved project is required");
    }

    @Test
    @WithMockUser(username = "project_manager", roles = "PROJECT_MANAGER")
    void shouldReturnAllProjects() throws Exception {
        mockMvc.perform(get("/api/projects").contentType(requireMediaType(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "project_manager", roles = "PROJECT_MANAGER")
    void shouldCreateProjectSuccessfully() throws Exception {
        Project newProject = new Project();
        newProject.setTitle("New Test Project");
        newProject.setDescription("Integration Test");
        newProject.setStatus(ProjectStatus.IN_PROGRESS);
        newProject.setDepartmentName("HR");

        mockMvc.perform(post("/api/projects")
                        .contentType(requireMediaType(MediaType.APPLICATION_JSON))
                        .content(requireContent(objectMapper.writeValueAsString(newProject))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Test Project"));
    }

    @Test
    @WithMockUser(username = "project_manager", roles = "PROJECT_MANAGER")
    void shouldFailToCreateProjectWithoutTitle() throws Exception {
        Project newProject = new Project();
        newProject.setDescription("Integration Test");
        newProject.setStatus(ProjectStatus.IN_PROGRESS);
        newProject.setDepartmentName("HR");

        mockMvc.perform(post("/api/projects")
                        .contentType(requireMediaType(MediaType.APPLICATION_JSON))
                        .content(requireContent(objectMapper.writeValueAsString(newProject))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "project_manager", roles = "PROJECT_MANAGER")
    void shouldUpdateProjectSuccessfully() throws Exception {
        testProject.setTitle("Updated Project Title");

        mockMvc.perform(put("/api/projects/" + testProject.getId())
                        .contentType(requireMediaType(MediaType.APPLICATION_JSON))
                        .content(requireContent(objectMapper.writeValueAsString(testProject))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Project Title"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldSoftDeleteProject() throws Exception {
        mockMvc.perform(delete("/api/projects/" + testProject.getId())).andExpect(status().isNoContent());

        assertThat(projectRepository.findById(requireId(testProject.getId(), "Project id is required")))
                .isPresent();
    }

    private @NonNull Project requireProject(@Nullable Project project, String message) {
        return Objects.requireNonNull(project, message);
    }

    private @NonNull MediaType requireMediaType(@Nullable MediaType mediaType) {
        return Objects.requireNonNull(mediaType, "MediaType is required");
    }

    private @NonNull String requireContent(@Nullable String content) {
        return Objects.requireNonNull(content, "Serialized content is required");
    }

    private @NonNull Long requireId(@Nullable Long id, String message) {
        return Objects.requireNonNull(id, message);
    }
}
