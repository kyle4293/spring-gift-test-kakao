package gift.acceptance.cucumber.steps;

import gift.acceptance.cucumber.CucumberSpringConfiguration;
import gift.acceptance.cucumber.support.RestAssuredContext;
import gift.acceptance.cucumber.support.TestDataContext;
import io.cucumber.java.ko.먼저;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.만약;
import io.cucumber.java.ko.그러면;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class GiftStepDefinitions {

    @Autowired
    private CucumberSpringConfiguration springConfig;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RestAssuredContext restAssuredContext;

    @Autowired
    private TestDataContext testDataContext;

    @먼저("재고가 {int}개인 옵션 {long}이 존재한다")
    public void 옵션이_존재한다(int quantity, Long optionId) {
        jdbcTemplate.execute("INSERT INTO category (id, name) VALUES (1, '전자기기')");
        jdbcTemplate.execute("INSERT INTO product (id, name, price, image_url, category_id) VALUES (1, '아이폰', 1000000, 'http://image.png', 1)");
        jdbcTemplate.execute(
            String.format("INSERT INTO option (id, name, quantity, product_id) VALUES (%d, '128GB', %d, 1)", optionId, quantity)
        );
        testDataContext.setOptionId(optionId);
    }

    @그리고("회원 {long}이 존재한다")
    public void 회원이_존재한다(Long memberId) {
        jdbcTemplate.execute(
            String.format("INSERT INTO member (id, name, email) VALUES (%d, '회원%d', 'member%d@example.com')", memberId, memberId, memberId)
        );
        testDataContext.setSenderId(memberId);
    }

    @만약("회원 {long}이 회원 {long}에게 옵션 {long}을 {int}개 {string} 메시지와 함께 선물하면")
    public void 선물하면(Long senderId, Long receiverId, Long optionId, int quantity, String message) {
        RestAssured.port = springConfig.getPort();

        String requestBody = createGiftRequest(optionId, quantity, receiverId, message);

        Response response = RestAssured
            .given()
            .log().all()
            .contentType(APPLICATION_JSON_VALUE)
            .header("Member-Id", senderId)
            .body(requestBody)
            .when()
            .post("/api/gifts")
            .then()
            .log().all()
            .extract().response();

        restAssuredContext.setLastResponse(response);
    }

    @그러면("선물 보내기가 성공한다")
    public void 선물_보내기가_성공한다() {
        Response response = restAssuredContext.getLastResponse();
        assertThat(response.statusCode()).isEqualTo(200);
    }

    @그러면("선물 보내기가 실패한다")
    public void 선물_보내기가_실패한다() {
        Response response = restAssuredContext.getLastResponse();
        assertThat(response.statusCode()).isEqualTo(500);
    }

    private String createGiftRequest(Long optionId, int quantity, Long receiverId, String message) {
        return """
            {
                "optionId": %d,
                "quantity": %d,
                "receiverId": %d,
                "message": "%s"
            }
            """.formatted(optionId, quantity, receiverId, message);
    }
}
