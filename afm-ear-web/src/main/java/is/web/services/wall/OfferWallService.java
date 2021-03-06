package is.web.services.wall;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.business.UserEventType;
import is.ejb.bl.conversionHistory.ConversionHistoryEntry;
import is.ejb.bl.conversionHistory.ConversionHistoryHolder;
import is.ejb.bl.offerWall.RealtimeFeedDataHolder;
import is.ejb.bl.offerWall.RealtimeFeedGenerator;
import is.ejb.bl.offerWall.content.IndividualOfferWall;
import is.ejb.bl.offerWall.content.Offer;
import is.ejb.bl.offerWall.content.OfferWallContent;
import is.ejb.bl.offerWall.content.SerDeOfferWallContent;
import is.ejb.bl.offerWall.external.AdGateCallbackDetails;
import is.ejb.bl.offerWall.external.AdscendMediaCallbackDetails;
import is.ejb.bl.offerWall.external.ExternalOfferWallManager;
import is.ejb.bl.offerWall.external.FyberCallbackDetails;
import is.ejb.bl.offerWall.external.PersonalyCallbackDetails;
import is.ejb.bl.offerWall.external.TrialpayCallbackDetails;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.system.security.HashValidationManager;
import is.ejb.bl.video.VideoCallbackData;
import is.ejb.bl.video.VideoManager;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAOConversionHistory;
import is.ejb.dl.dao.DAOOfferWall;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.ConversionHistoryEntity;
import is.ejb.dl.entities.OfferWallEntity;
import is.ejb.dl.entities.RealmEntity;
import is.web.services.APIHelper;
import is.web.services.APIRequestDetails;
import is.web.services.APIValidator;
import is.web.services.ResponseMultiOfferWall;
import is.web.services.ResponseOWIds;
import is.web.services.wall.validators.UserValidator;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Path("/")
public class OfferWallService {

	@Inject
	private Logger logger;

	@Inject
	private DAORealm daoRealm;

	@Inject
	private DAOOfferWall daoOfferWall;

	@Inject
	private SerDeOfferWallContent serDeOfferWallContent;

	@Inject
	private DAOConversionHistory daoConversionHistory;

	@Inject
	private DAOAppUser daoAppUser;

	@Inject
	private RealtimeFeedGenerator realtimeFeedGenerator;

	@Context
	private HttpServletRequest httpRequest;

	@Inject
	private HashValidationManager hashValidationManager;

	@Inject
	private UserValidator userValidator;

	@Inject
	private APIHelper apiHelper;

	@Inject
	private ExternalOfferWallManager externalOfferwallManager;

	@Inject
	private VideoManager videoManager;

	@GET
	@Path("/adscendmedia/reward/")
	public String saveAdscendMediaRewardCallback(@QueryParam("oid") String oid,@QueryParam("odyn") String odyn,
			@QueryParam("onm") String onm, @QueryParam("pay") String pay, @QueryParam("cur") String cur,
			@QueryParam("sb1") String sb1, @QueryParam("sid") String sid, @QueryParam("sb2") String sb2,
			@QueryParam("sb3") String sb3, @QueryParam("sb4") String sb4, @QueryParam("src") String src,
			@QueryParam("dvc") String dvc, @QueryParam("sts") String sts, @QueryParam("ip") String ip,
			@QueryParam("tid") String tid, @QueryParam("rnd") String rnd, @QueryParam("uts") String uts,
			@QueryParam("prd") String prd, @QueryParam("prf") String prf, @QueryParam("asid") String asid,
			@QueryParam("apla") String apla, @QueryParam("hsh") String hsh)
			{
		String dataContent = "oid: " + oid + " odyn: " + odyn  + " onm: " + onm + " pay: " + pay + " cur: " + cur
				+ " sb1: " + sb1 + " sid: " + sid + " sb2: " + sb2 + " sb3: " + sb3 + " sb4: " + sb4 + " src: " + src 
				+ " dvc: " + dvc + " sts: " + sts + " ip: " + ip + " tid: " + tid + " rnd: " + rnd + " uts: " + uts 
				+ " prd: " + prd + " prf: " + prf + " asid: " + asid + " apla: " + apla + " hsh: " + hsh;
		try {
			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}
			dataContent = dataContent + " ipAddress: " + ipAddress;
			
				Application.getElasticSearchLogger().indexLog(Application.ADSCEND_MEDIA_CALLBACK, -1, LogStatus.OK,
						Application.ADSCEND_MEDIA_CALLBACK + " received callback for event: " + dataContent);
				AdscendMediaCallbackDetails details = new AdscendMediaCallbackDetails();
				details.setOid(oid);
				details.setOdyn(odyn);
				details.setOnm(onm);
				details.setPay(pay);
				details.setCur(cur);
				details.setSb1(sb1);
				details.setSid(sid);
				details.setSb2(sb2);
				details.setSb3(sb3);
				details.setSb4(sb4);
				details.setSrc(src);
				details.setDvc(dvc);
				details.setSts(sts);
				details.setIp(ip);
				details.setTid(tid);
				details.setRnd(rnd);
				details.setUts(uts);
				details.setPrd(prd);
				details.setPrf(prf);
				details.setAsid(asid);
				details.setApla(apla);
				details.setHsh(hsh);
				externalOfferwallManager.saveConversion(details);
			

		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.PERSONALY_CALLBACK, -1, LogStatus.ERROR,
					Application.PERSONALY_CALLBACK + " received callback: " + dataContent + " but error occured: "
							+ ExceptionUtils.getFullStackTrace(exc));
		}

		return "1";
	}
	
	@GET
	@Path("/personaly/reward/")
	public String savePersonalyRewardCallback(@QueryParam("user_id") String userId,@QueryParam("amount") String amount,
			@QueryParam("offer_id") String offerId, @QueryParam("app_id") String appId, @QueryParam("signature") String signature,
			@QueryParam("offer_name") String offerName, @QueryParam("package_id") String packageId) {
		String dataContent = "userId: " + userId + " amount: " + amount  + " offerId: " + offerId + " appId: " + appId + " signature: " + signature
				+ " offerName: " + offerName + " packageId: " + packageId;
		try {
			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}
			dataContent = dataContent + " ipAddress: " + ipAddress;
			
				Application.getElasticSearchLogger().indexLog(Application.PERSONALY_CALLBACK, -1, LogStatus.OK,
						Application.PERSONALY_CALLBACK + " received callback for event: " + dataContent);
				PersonalyCallbackDetails details = new PersonalyCallbackDetails();
				details.setAmount(amount);
				details.setAppId(appId);
				details.setOfferId(offerId);
				details.setOfferName(offerName);
				details.setPackageId(packageId);
				details.setSignature(signature);
				details.setUserId(userId);
				externalOfferwallManager.saveConversion(details);
			

		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.PERSONALY_CALLBACK, -1, LogStatus.ERROR,
					Application.PERSONALY_CALLBACK + " received callback: " + dataContent + " but error occured: "
							+ ExceptionUtils.getFullStackTrace(exc));
		}

		return "1";
	}
	
	
	
	@GET
	@Path("/v2/adgate/reward/")
	public Response saveAdGateRewardCallback(@QueryParam("offer_id") String offerId,
			@QueryParam("offer_name") String offerName, @QueryParam("affiliate_id") String affiliateId,
			@QueryParam("source") String source, @QueryParam("s1") String s1,
			@QueryParam("transaction_id") String transactionId, @QueryParam("session_ip") String sessionIp,
			@QueryParam("date") String date, @QueryParam("time") String time, @QueryParam("datetime") String datetime,
			@QueryParam("ran") String ran, @QueryParam("payout") String payout, @QueryParam("status") String status,
			@QueryParam("points") String points, @QueryParam("vc_title") String vcTitle) {
		String dataContent = "offerId: " + offerId + " offerName: " + offerName + " affiliateId: " + affiliateId
				+ " source: " + source + " s1: " + s1 + " transactionId: " + transactionId + " sessionIp: " + sessionIp
				+ " date: " + date + " time: " + time + " datetime: " + datetime + " ran: " + ran + " payout: " + payout
				+ " status: " + status + " points: " + points + " vcTitle: " + vcTitle;
		try {
			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}
			dataContent = dataContent + " ipAddress: " + ipAddress;
			Application.getElasticSearchLogger().indexLog(Application.ADGATE_CALLBACK, -1, LogStatus.OK,
					Application.ADGATE_CALLBACK + " received callback for event: " + dataContent);
			logger.info("ADGATE CALLBACK: " + dataContent);
			AdGateCallbackDetails details = new AdGateCallbackDetails();
			details.setAffiliateId(affiliateId);
			details.setDate(date);
			details.setDateTime(datetime);
			details.setOfferId(offerId);
			details.setOfferName(offerName);
			details.setPayout(payout);
			details.setPoints(points);
			details.setRan(ran);
			details.setS1(s1);
			details.setSessionIp(sessionIp);
			details.setSource(source);
			details.setStatus(status);
			details.setTime(time);
			details.setTransactionId(transactionId);
			details.setVcTitle(vcTitle);
			
			externalOfferwallManager.saveConversion(details);

		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.ADGATE_CALLBACK, -1, LogStatus.ERROR,
					Application.ADGATE_CALLBACK + " received callback: " + dataContent + " but error occured: "
							+ ExceptionUtils.getFullStackTrace(exc));
		}
		return Response.ok().build();

	}

	@GET
	@Path("/v2/trialpay/reward/")
	public String saveTrialpayRewardCallback(@QueryParam("oid") String oid, @QueryParam("sid") String sid,
			@QueryParam("reward_amount") String rewardAmount, @QueryParam("email") String email,
			@QueryParam("country") String country, @QueryParam("revenue") String revenue,
			@QueryParam("transaction_type") String transactionType, @QueryParam("offer_category") String offerCategory,
			@QueryParam("order_date") String orderDate, @QueryParam("tid") String tid) {
		String dataContent = "oid: " + oid + " sid: " + sid + " rewardAmount: " + rewardAmount + " email: " + email
				+ " country: " + country + "transactionType: " + transactionType + " offerCategory: " + offerCategory
				+ " orderDate: " + orderDate + " tid: " + tid;
		try {
			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}
			dataContent = dataContent + " ipAddress: " + ipAddress;
			Application.getElasticSearchLogger().indexLog(Application.TRIALPAY_CALLBACK, -1, LogStatus.OK,
					Application.TRIALPAY_CALLBACK + " received callback for event: " + dataContent);

			TrialpayCallbackDetails details = new TrialpayCallbackDetails();
			details.setCountry(country);
			details.setEmail(email);
			details.setOfferCategory(offerCategory);
			details.setOid(oid);
			details.setOrderDate(orderDate);
			details.setRevenue(revenue);
			details.setRewardAmount(rewardAmount);
			details.setSid(sid);
			details.setTid(tid);
			details.setTransactionType(transactionType);
			externalOfferwallManager.saveConversion(details);

		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.TRIALPAY_CALLBACK, -1, LogStatus.ERROR,
					Application.TRIALPAY_CALLBACK + " received callback: " + dataContent + " but error occured: "
							+ ExceptionUtils.getFullStackTrace(exc));
		}

		return "1";
	}

	@GET
	@Path("/fyber/reward/")
	public Response saveFyberRewardCallback(@QueryParam("uid") String uid, @QueryParam("sid") String sid,
			@QueryParam("amount") String amount, @QueryParam("currency_name") String currencyName,
			@QueryParam("currency_id") String currencyId, @QueryParam("_trans_id_") String transId,
			@QueryParam("pub0") String pub0, @QueryParam("pub1") String pub1, @QueryParam("pub2") String pub2,
			@QueryParam("pub3") String pub3) {
		String dataContent = "uid: " + uid + " sid: " + sid + " amount: " + amount + " currencyName: " + currencyName
				+ " currencyId: " + currencyId + " transId: " + transId + " pub0: " + pub0 + " pub1: " + pub1
				+ " pub2: " + pub2 + " pub3: " + pub3;
		try {
			String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpRequest.getRemoteAddr();
			}
			dataContent = dataContent + " ipAddress: " + ipAddress;
			if (pub0 != null && pub0.equals("video")) {
				Application.getElasticSearchLogger().indexLog(Application.FYBER_VIDEO_CALLBACK, -1, LogStatus.OK,
						Application.FYBER_VIDEO_CALLBACK + " received callback for event: " + dataContent);
				VideoCallbackData data = new VideoCallbackData();
				data.setAmount(Integer.valueOf(amount));
				data.setCurrencyId(currencyId);
				data.setCurrencyName(currencyName);
				data.setUid(sid);
				data.setUserId(pub1);
				data.setUsername(pub2);
				data.setTransactionId(pub3);

				Application.getElasticSearchLogger().indexLog(Application.VIDEO_REWARD_ACTIVITY, -1, LogStatus.OK,
						Application.VIDEO_REWARD_ACTIVITY + "Received video callback request from ipAddress: "
								+ apiHelper.getIpAddressFromHttpRequest(httpRequest) + " " + data.toString());
				logger.info("Received video callback request from ipAddress: "
						+ apiHelper.getIpAddressFromHttpRequest(httpRequest) + " " + data.toString());
				videoManager.addData(data);

			} else {
				Application.getElasticSearchLogger().indexLog(Application.FYBER_CALLBACK, -1, LogStatus.OK,
						Application.FYBER_CALLBACK + " received callback for event: " + dataContent);
				FyberCallbackDetails details = new FyberCallbackDetails();
				details.setAmount(amount);
				details.setCurrencyId(currencyId);
				details.setCurrencyName(currencyName);
				details.setSid(sid);
				details.setTransId(transId);
				details.setUid(uid);
				externalOfferwallManager.saveConversion(details);
			}

		} catch (Exception exc) {
			exc.printStackTrace();
			Application.getElasticSearchLogger().indexLog(Application.FYBER_CALLBACK, -1, LogStatus.ERROR,
					Application.FYBER_CALLBACK + " received callback: " + dataContent + " but error occured: "
							+ ExceptionUtils.getFullStackTrace(exc));
		}

		return Response.ok().build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/offerwalls/")
	public String getOfferWallIds(final APIRequestDetails details) {
		OfferWallIdsResponse response = new OfferWallIdsResponse();
		try {
			String ipAddress = apiHelper.getIpAddressFromHttpRequest(httpRequest);
			Application.getElasticSearchLogger().indexLog(Application.USER_REGISTRATION_ACTIVITY, -1, LogStatus.OK,
					Application.COW_SELECTION_ACTIVITY + " " + Application.COW_IDS_SELECTION + " "
							+ " identifying available offer walls for request: " + details + "ipAddress: " + ipAddress);
			logger.info("identifying available offer walls for request: " + details + "ipAddress: " + ipAddress);
			if (!userValidator.validate(details.getParameters())) {
				apiHelper.setupFailedResponseForError(response, userValidator.getInvalidValueErrorCode());
			} else {
				executeWallIdsSelection(details, response);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			apiHelper.setupFailedResponseForError(response, RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR);
		}

		return apiHelper.getGson().toJson(response);

	}

	private void executeWallIdsSelection(final APIRequestDetails details, OfferWallIdsResponse response)
			throws Exception {
		Integer userId = (Integer) details.getParameters().get("userId");
		AppUserEntity appUser = daoAppUser.findById(userId);

		Application.getElasticSearchLogger().indexLog(Application.USER_REGISTRATION_ACTIVITY, -1, LogStatus.ERROR,
				Application.COW_SELECTION_ACTIVITY + " " + Application.COW_IDS_SELECTION + " "
						+ "aborting as no user found under following request: " + userId);

		logger.info("realm:" + appUser.getRealmId() + " countryCode: " + appUser.getCountryCode() + " deviceType: "
				+ appUser.getDeviceType() + " rewardTypeName: " + appUser.getRewardTypeName());
		List<OfferWallEntity> listOffers = daoOfferWall.findAllByRealmIdAndActiveAndCountryAndDevice(
				appUser.getRealmId(), true, appUser.getCountryCode(), appUser.getDeviceType(),
				appUser.getRewardTypeName());

		if (listOffers == null) {
			listOffers = new ArrayList<OfferWallEntity>();
		}
		Application.getElasticSearchLogger().indexLog(Application.USER_REGISTRATION_ACTIVITY, -1, LogStatus.OK,
				Application.COW_SELECTION_ACTIVITY + " " + Application.COW_IDS_SELECTION + " " + "selected offer walls:"
						+ listOffers.size());
		logger.info("selected offer walls:" + listOffers.size());
		apiHelper.setupSuccessResponse(response);
		response.setIds(getWallIds(listOffers));
	}

	private List<Integer> getWallIds(List<OfferWallEntity> offerWallList) {
		List<Integer> idList = new ArrayList<Integer>();
		for (OfferWallEntity offerWall : offerWallList) {
			idList.add(offerWall.getId());
		}
		return idList;
	}

	/**
	 * This method returns offer walls that are personalized for individual user
	 * based on his previous download history
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/offerwall/{id}/")
	public String getOfferWallById(@PathParam("id") String offerWallId, final APIRequestDetails details) {

		OfferWallResponse response = new OfferWallResponse();
		String ipAddress = apiHelper.getIpAddressFromHttpRequest(httpRequest);
		Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, -1, LogStatus.OK,
				Application.COW_SELECTION_ACTIVITY + " " + Application.COW_SELECTION_BY_ID
						+ " Retrieving offer wall with id: " + offerWallId + " ip: " + ipAddress + details.toString());
		try {
			if (!userValidator.validate(details.getParameters())) {
				apiHelper.setupFailedResponseForError(response, userValidator.getInvalidValueErrorCode());
			} else {
				executeOfferWallSelection(offerWallId, details, response, ipAddress);
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			apiHelper.setupFailedResponseForError(response, RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR);
			Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, -1, LogStatus.ERROR,
					Application.COW_SELECTION_ACTIVITY + " " + Application.COW_SELECTION_BY_ID
							+ " Error selecting offer: " + exc.toString() + " status: "
							+ RespStatusEnum.FAILED.toString() + " error code: "
							+ RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR.toString());
		}
		return apiHelper.getGson().toJson(response);

	}

	private void executeOfferWallSelection(String offerWallId, final APIRequestDetails details,
			OfferWallResponse response, String ipAddress) throws Exception {
		Integer userId = (Integer) details.getParameters().get("userId");
		AppUserEntity appUser = daoAppUser.findById(userId);
		RealmEntity realm = daoRealm.findById(appUser.getRealmId());
		if (realm == null) {
			apiHelper.setupFailedResponseForError(response, RespCodesEnum.ERROR_INVALID_USER_DATA);
			Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, -1, LogStatus.ERROR,
					Application.COW_SELECTION_ACTIVITY + " " + Application.COW_SELECTION_BY_ID
							+ " realm is null for request: " + details.toString());
		}
		OfferWallEntity offerWall = daoOfferWall.findById(Integer.valueOf(offerWallId));
		if (offerWall == null) {
			apiHelper.setupFailedResponseForError(response, RespCodesEnum.ERROR_OFFER_WALL_NOT_FOUND);
			Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, -1, LogStatus.ERROR,
					Application.COW_SELECTION_ACTIVITY + " " + Application.COW_SELECTION_BY_ID
							+ " Unable to identify offer wall with provided id: " + offerWallId
							+ " please make sure that offer id is correct" + " status: "
							+ RespStatusEnum.FAILED.toString() + " error code: "
							+ RespCodesEnum.ERROR_OFFER_NOT_FOUND.toString());
		} else {
			Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, realm.getId(),
					LogStatus.OK,
					Application.COW_SELECTION_ACTIVITY + " " + Application.COW_SELECTION_BY_ID + " "
							+ Application.COW_SELECTION_BY_ID_IDENTIFIED + " " + " selecting offer wall with id: "
							+ offerWallId + " userId: " + userId + " deviceType: " + appUser.getDeviceType()
							+ " for realm: " + realm.getName() + " network id: " + realm.getId());
			logger.info(
					"COW_SELECTION selecting offer wall with id: " + offerWallId + " for realm: " + realm.getName());

			Application.getElasticSearchLogger().indexWallSelection(realm.getName(), Integer.valueOf(userId),
					appUser.getEmail(), appUser.getPhoneNumber(), appUser.getPhoneNumberExtension(),
					Integer.valueOf(offerWallId), offerWall.getRewardTypeName(), offerWall.getTargetCountriesFilter(),
					offerWall.getTargetDevicesFilter(), appUser.getLocale(), appUser.getIdfa(), ipAddress,
					details.getSystemInfo(), details.getApplicationInfo());
			logger.info("Indexed wall selection");

			OfferWallContent offerWallContent = filterWalls(appUser, ipAddress, offerWall, details);
			apiHelper.setupSuccessResponse(response);
			response.setMultiOfferWall(offerWallContent);

		}
	}

	private OfferWallContent filterWalls(AppUserEntity appUser, String ipAddress, OfferWallEntity offerWall,
			APIRequestDetails details) throws Exception {

		RealtimeFeedDataHolder realtimeFeedDataHolder = new RealtimeFeedDataHolder();

		realtimeFeedDataHolder.setGaid(URLEncoder.encode(appUser.getAdvertisingId(), "UTF-8"));
		realtimeFeedDataHolder.setIdfa(URLEncoder.encode("", "UTF-8"));
		realtimeFeedDataHolder.setIp(URLEncoder.encode(ipAddress, "UTF-8"));
		realtimeFeedDataHolder.setUa(URLEncoder.encode("", "UTF-8"));
		realtimeFeedDataHolder.setUserId(URLEncoder.encode(appUser.getId() + "", "UTF-8"));
		realtimeFeedDataHolder.setDeviceType(URLEncoder.encode(appUser.getDeviceType(), "UTF-8"));

		HashMap<String, Object> parameters = details.getParameters();
		logger.info("Parameters: " + parameters);
		if (parameters != null) {
			String locale = "";
			if (parameters.containsKey("locale")) {
				Object value = parameters.get("locale");
				if (value != null) {
					locale = (String) parameters.get("locale");
				}
			}

			String osVersion = "";
			if (parameters.containsKey("osVersion")) {
				Object value = parameters.get("osVersion");
				if (value != null) {
					osVersion = (String) parameters.get("osVersion");
				}
			}

			boolean limitedTrackingEnabled = false;
			if (parameters.containsKey("limitedTrackingEnabled")) {
				Object value = parameters.get("limitedTrackingEnabled");
				if (value != null) {
					limitedTrackingEnabled = (Boolean) parameters.get("limitedTrackingEnabled");
				}
			}
			String ua = "";
			if (parameters.containsKey("ua")) {
				Object value = parameters.get("ua");
				if (value != null) {
					ua = (String) parameters.get("ua");
				}
			}

			realtimeFeedDataHolder.setLocale(locale);
			realtimeFeedDataHolder.setOsVersion(osVersion);
			realtimeFeedDataHolder.setLimitedTrackingEnabled(limitedTrackingEnabled);
			realtimeFeedDataHolder.setUa(ua);

		}
		logger.info(" ** REAL TIME FEED DATA HOLDER: " + realtimeFeedDataHolder);

		// TODO to be completed for Fyber
		// realtimeFeedDataHolder.setLocale(locale);
		// realtimeFeedDataHolder.setOsVersion(osVersion);
		// realtimeFeedDataHolder.setLimitedTrackingEnabled(limitedTrackingEnabled);
		// realtimeFeedDataHolder.setUa(ua);

		OfferWallContent offerWallContent = realtimeFeedGenerator.composeOfferWall(offerWall, realtimeFeedDataHolder,
				true);

		// filter offer wall and remove offers that are already
		// converted by user
		offerWallContent = filterOutAlreadyConvertedByUserApps(appUser, offerWall);

		// count how many offers are returned
		int returnedOffers = 0;
		for (int i = 0; i < offerWallContent.getOfferWalls().size(); i++) {
			IndividualOfferWall wall = offerWallContent.getOfferWalls().get(i);
			if (wall != null) {
				returnedOffers = returnedOffers + wall.getOffers().size();
			}
		}

		Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, -1, LogStatus.OK,
				Application.COW_SELECTION_ACTIVITY + " " + Application.COW_SELECTION_BY_ID + " returning wall: "
						+ offerWallContent.getCompositeOfferWallName() + " individual walls number: "
						+ offerWallContent.getOfferWalls().size() + " returned_offers: " + returnedOffers
						+ " for user with id: " + appUser.getId() + " ip: " + ipAddress);
		return offerWallContent;

	}

	private OfferWallContent filterOutAlreadyConvertedByUserApps(AppUserEntity appUser, OfferWallEntity offerWall) {
		OfferWallContent offerWallContent = null;
		try {
			// filter through offers
			offerWallContent = serDeOfferWallContent.deserialize(offerWall.getContent());
			// retrieve download history for this user
			ConversionHistoryEntity conversionHistory = daoConversionHistory.findByUserId(appUser.getId());
			if (conversionHistory == null) {
				return offerWallContent; // exit as user has not generated
											// conversion history yet
			}
			ConversionHistoryHolder conversionHistoryHolder = conversionHistory.getConversionHistoryHolder();

			ArrayList<ConversionHistoryEntry> listUserConversionHistoryEntries = conversionHistoryHolder
					.getListConversionHistoryEntries();
			ArrayList<IndividualOfferWall> listIndividualOfferWalls = offerWallContent.getOfferWalls();
			for (int i = 0; i < listIndividualOfferWalls.size(); i++) {
				IndividualOfferWall individualOfferWall = listIndividualOfferWalls.get(i);
				ArrayList<Offer> listOffers = individualOfferWall.getOffers();
				for (int k = listOffers.size() - 1; k >= 0; k--) {
					Offer offer = listOffers.get(k);
					// logger.info("checking offer: "+offer.getTitle());
					if (isOfferConvertedByUser(offerWall.getRealm().getId(), offer, listUserConversionHistoryEntries)) {
						listOffers.remove(k);
					}
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.severe(e.toString());
			Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, -1, LogStatus.ERROR,
					Application.COW_SELECTION_ACTIVITY + " " + Application.COW_SELECTION_BY_ID
							+ " Error selecting offer: " + e.toString() + " status: " + RespStatusEnum.FAILED.toString()
							+ " error code: " + RespCodesEnum.ERROR_INTERNAL_SERVER_ERROR.toString());
		}
		return offerWallContent;
	}

	private boolean isOfferConvertedByUser(int realmId, Offer offer,
			ArrayList<ConversionHistoryEntry> listUserConversionHistoryEntries) {

		for (int i = 0; i < listUserConversionHistoryEntries.size(); i++) {
			ConversionHistoryEntry conversionEntry = listUserConversionHistoryEntries.get(i);
			// logger.info("comparing: "+offer.getTitle()+"
			// "+offer.getSourceId()+" "+offer.getAdProviderCodeName()+" --- "+
			// conversionEntry.getOfferTitle()+"
			// "+conversionEntry.getSourceOfferId()+"
			// "+conversionEntry.getAdProviderCodeName());
			// logger.info("converison entry: "+conversionEntry);
			try {
				if (conversionEntry != null && conversionEntry.getConversionTimestamp() != null
						&& conversionEntry.getSourceOfferId() != null
						&& conversionEntry.getSourceOfferId().equals(offer.getSourceId())
						&& conversionEntry.getAdProviderCodeName() != null
						&& conversionEntry.getAdProviderCodeName().equals(offer.getAdProviderCodeName())) {

					Application.getElasticSearchLogger().indexLog(Application.COW_SELECTION_ACTIVITY, realmId,
							LogStatus.OK,
							Application.COW_SELECTION_ACTIVITY + " " + Application.COW_SELECTION_BY_ID + " "
									+ Application.OFFER_REJECTED_AS_ALREADY_CONVERTED + " " + " rejected offer: "
									+ offer.getTitle() + " add provder: " + offer.getAdProviderCodeName()
									+ " as it was already converted by used at time: "
									+ conversionEntry.getConversionTimestamp().toString());

					return true;
				}
			} catch (Exception exc) {
				logger.severe("Error in conversion entry when identifying already converted offers when comparing: "
						+ offer.getTitle() + " " + offer.getSourceId() + " " + offer.getAdProviderCodeName() + " --- "
						+ conversionEntry.getOfferTitle() + " " + conversionEntry.getSourceOfferId() + " "
						+ conversionEntry.getAdProviderCodeName() + " " + exc.toString());
				// exc.printStackTrace();

				return true;
			}
		}

		return false;
	}

}