package ejb.bl.offerProviders.hasoffersExt.getPayoutDetails;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.*;

public class GetPayoutDetails {
	@JsonProperty("response") private Response response;
	@JsonProperty("request") private Request request;
}
