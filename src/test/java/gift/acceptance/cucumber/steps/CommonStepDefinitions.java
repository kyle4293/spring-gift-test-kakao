package gift.acceptance.cucumber.steps;

import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;

public class CommonStepDefinitions {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void cleanupDatabase() throws Exception {
        executeSqlScript("cleanup.sql");
    }

    private void executeSqlScript(String scriptPath) throws Exception {
        String script = new String(
            new ClassPathResource(scriptPath).getInputStream().readAllBytes()
        );
        Arrays.stream(script.split(";"))
            .filter(sql -> !sql.trim().isEmpty())
            .forEach(jdbcTemplate::execute);
    }
}
