package sonia.scm.it;

import io.restassured.RestAssured;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

/**
 * Workaround to initialize user database for v2 rest api.
 */
public class IntegrationTestListener extends RunListener {
  @Override
  public void testRunStarted(Description description) {
    RestAssured.given().when().get("http://localhost:8081/scm/api/rest/auth/state/");
  }
}
