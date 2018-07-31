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
    RestAssured.given().when().get(RestUtil.BASE_URL + "api/rest/auth/state/");
  }
}
