package ea.sof.ms_questions.pubsub;

import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.cloud.gcp.pubsub.integration.outbound.PubSubMessageHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Component;

@Component
public class PubSubQuestionSender {
	private static final String topicName = "topicNewQuestion";
	private static final String outputChannel = "pubsubOutputChannel_" + topicName;

	@Bean
	@ServiceActivator(inputChannel = outputChannel)
	public MessageHandler messageQuestionsSender(PubSubTemplate pubsubTemplate) {

		return new PubSubMessageHandler(pubsubTemplate, topicName);
	}

	@MessagingGateway(defaultRequestChannel = outputChannel)
	public interface PubsubOutboundQuestionsGateway {

		void sendToPubsub(String text);
	}
}
