package rewards;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import rewards.internal.account.AccountRepository;
import rewards.CaptureSystemOutput.OutputCapture;

@RunWith(JUnitPlatform.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { SystemTestConfig.class })
public class LoggingAspectTests {

	@Autowired
	AccountRepository repository;
	
	@Test
	@CaptureSystemOutput
	public void testLogger(OutputCapture capture){
		repository.findByCreditCard("1234123412341234");
		
		// AOP VERIFICATION
		// LoggingAspect should have output an INFO message to console
		String consoleOutput = capture.toString();
		assertTrue(consoleOutput.startsWith("INFO"));
		assertTrue(consoleOutput.contains("rewards.internal.aspects.LoggingAspect"));
	}
}
