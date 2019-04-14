package parser;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("testing XCSPparser")
public class XCSPparserTest {

	@Test
	@DisplayName("testing getAgents()")
	void testGetAgents() {
		XCSPparser parser = new XCSPparser("xcsp/RandomDCOP10.xml");
		assertTrue(parser.getAgents().size() == 10);
	}

	// TODO add remaining tests

}
