package is.web.beans.appusers;

import is.ejb.bl.conversionHistory.ConversionHistoryEntry;
import is.ejb.bl.conversionHistory.ConversionHistoryHolder;
import is.ejb.bl.eventQueue.EventQueueManager;
import is.ejb.bl.reward.RewardManager;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAOConversionHistory;
import is.ejb.dl.dao.DAOEventQueueEntity;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAOSpinnerData;
import is.ejb.dl.dao.DAOUserEvent;
import is.ejb.dl.dao.DAOWalletData;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.ConversionHistoryEntity;
import is.ejb.dl.entities.EventQueueEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.SpinnerDataEntity;
import is.ejb.dl.entities.UserEventEntity;
import is.ejb.dl.entities.WalletDataEntity;
import is.web.beans.users.LoginBean;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.lucene.analysis.compound.hyphenation.TernaryTree.Iterator;
import org.primefaces.context.RequestContext;
import org.primefaces.event.data.PageEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

@ManagedBean(name = "displayAppUsersBean")
@SessionScoped
public class DisplayAppUsersBean implements Serializable {

	@Inject
	private Logger logger;

	private LoginBean loginBean;

	private RealmEntity realm = null;

	@Inject
	private DAOAppUser daoAppUser;

	@Inject
	private DAOWalletData daoWalletData;

	@Inject
	private DAOConversionHistory daoConversionHistory;

	@Inject
	private DAOEventQueueEntity daoEventQueue;

	@Inject
	private EventQueueManager eventQueueManager;

	@Inject
	private DAOUserEvent daoUserEvent;

	private LazyDataModel<AppUserEntity> lazyModel;

	private LazyDataModel<ConversionHistoryEntry> lazyModelDownloadHistory;

	private AppUserEntity currentlyViewedUser = null;

	private boolean displayInternalUserHistory = false;

	@Inject
	private RewardManager rewardManager;

	@Inject
	private DAORealm daoRealm;

	@Inject
	private DAOSpinnerData daoSpinnerData;

	public DisplayAppUsersBean() {
	}

	//
	@PostConstruct
	public void init() {
		FacesContext fc = FacesContext.getCurrentInstance();
		loginBean = (LoginBean) fc.getApplication().evaluateExpressionGet(fc, "#{loginBean}", LoginBean.class);

		// init lazy model for app users list
		lazyModel = new LazyDataModel<AppUserEntity>() {
			@Override
			public List<AppUserEntity> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {

				Collection<AppUserEntity> listEvents = new ArrayList<AppUserEntity>();
				int totalCount = daoAppUser.countTotal(loginBean.getUser().getRealm().getId());
				lazyModel.setRowCount(totalCount);

				logger.info("sort field: " + sortField + " filters: " + filters);
				logger.info(
						"lazy loading devices list from between: " + first + " and " + (first + pageSize) + " total devices count: " + totalCount);

				try {
					if (sortField == null) { // by default sort by click date
						sortField = "registrationTime";
						sortOrder = SortOrder.DESCENDING;
					}

					String sortingOrder = "descending";
					if (sortOrder == SortOrder.ASCENDING) {
						sortingOrder = "ascending";
					} else if (sortOrder == SortOrder.DESCENDING) {
						sortingOrder = "descending";
					}

					listEvents = daoAppUser.findFiltered(first, pageSize, sortField, sortingOrder, filters, loginBean.getUser().getRealm().getId());

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.severe(e.toString());
				}
				logger.info("lazy loading completed, current results returned: " + listEvents.size());

				return (List) listEvents;
			}
		};

		// init lazy model for conversion history entries list (list of
		// downloads generated by selected user)
		try {
			lazyModelDownloadHistory = new LazyDataModel<ConversionHistoryEntry>() {
				@Override
				public List<ConversionHistoryEntry> load(int first, int pageSize, String sortField, SortOrder sortOrder,
						Map<String, String> filters) {

					Collection<ConversionHistoryEntry> listDownloads = new ArrayList<ConversionHistoryEntry>();
					ConversionHistoryEntity conversionHistory = null;
					try {
						System.out.println("displaying internal user history: " + displayInternalUserHistory);
						if (displayInternalUserHistory) {
							// TODO to change once switching to iOS
							conversionHistory = daoConversionHistory.findByUserId(currentlyViewedUser.getId());
						} else {
							// used when displaying altabel user history
							conversionHistory = daoConversionHistory.findByUserId(currentlyViewedUser.getAltabelUserId());
						}
						if (conversionHistory == null) {
							// fake if none exists
							conversionHistory = new ConversionHistoryEntity();
						}
					} catch (Exception e1) {
						// fake if none exists
						conversionHistory = new ConversionHistoryEntity();
						// e1.printStackTrace();
					}
					ConversionHistoryHolder conversionHistoryHolder = conversionHistory.getConversionHistoryHolder();
					if (conversionHistoryHolder == null) {
						conversionHistoryHolder = new ConversionHistoryHolder();
					}
					int totalCount = conversionHistoryHolder.getListConversionHistoryEntries().size();
					lazyModelDownloadHistory.setRowCount(totalCount);

					logger.info("sort field: " + sortField + " filters: " + filters);
					logger.info("lazy loading devices list from between: " + first + " and " + (first + pageSize) + " total devices count: "
							+ totalCount);

					try {
						if (sortField == null) { // by default sort by click
													// date
							// sortField = "clickDate";
							// sortOrder = SortOrder.DESCENDING;
						}

						String sortingOrder = "descending";
						if (sortOrder == SortOrder.ASCENDING) {
							sortingOrder = "ascending";
						} else if (sortOrder == SortOrder.DESCENDING) {
							sortingOrder = "descending";
						}

						// at the moment we provide no filtering
						// listDownloads =
						// conversionHistoryHolder.getListConversionHistoryEntries();
						listDownloads = getFilteredConversionHistoryEntries(first, pageSize, sortField, sortingOrder, filters,
								loginBean.getUser().getRealm().getId(), conversionHistoryHolder.getListConversionHistoryEntries());

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						logger.severe(e.toString());
					}
					logger.info("lazy loading completed, current results returned: " + listDownloads.size());

					return (List) listDownloads;
				}
			};
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	private ArrayList<ConversionHistoryEntry> getFilteredConversionHistoryEntries(int first, int pageSize, String sortField, String sortOrder,
			Map filters, int realmId, ArrayList<ConversionHistoryEntry> fullConversionHistoryEntriesList) {
		ArrayList<ConversionHistoryEntry> filteredList = new ArrayList<ConversionHistoryEntry>();
		int startIndex = first;
		int endIndex = startIndex + pageSize;
		logger.info(" si: " + startIndex + " ei: " + endIndex + " page size: " + pageSize + " page number: " + first);
		for (int i = 0; i < fullConversionHistoryEntriesList.size(); i++) {
			if (i >= startIndex && i <= endIndex) {
				filteredList.add(fullConversionHistoryEntriesList.get(i));
			}
			if (i > endIndex) {
				break;
			}
		}

		return filteredList;
	}

	public void refreshList() {
		refresh();
	}

	public void pageUpdate(PageEvent event) {
		logger.info("page update event triggered...");
	}

	public void refresh() {
		try {
			logger.info("refreshing bean...");
			// refresh tab GUI after model update
			RequestContext.getCurrentInstance().update("tabView:idAppUsersTable");
		} catch (Exception e) {
			e.printStackTrace();
			logger.severe("Error: " + e.toString());
		}
	}

	public void refreshDownloadHistory() {
		try {
			// refresh tab GUI after model update
			RequestContext.getCurrentInstance().update("tabView:idDownloadHistoryTable");
		} catch (Exception e) {
			e.printStackTrace();
			logger.severe("Error: " + e.toString());
		}
	}

	public void displayDownloadHistory(AppUserEntity appUser) {
		currentlyViewedUser = appUser;
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Displaying download history for user: " + appUser.getFullName()));
		refreshList();
		RequestContext.getCurrentInstance().update("tabView:idDownloadHistoryTable");
		RequestContext.getCurrentInstance().update("tabView:idDisplayAppUsersGrowl");
	}

	public void deleteUser(AppUserEntity appUser) {
		try {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "User: " + appUser.getFullName() + " successfully removed"));
			ConversionHistoryEntity conversionHistoryToDelete = daoConversionHistory.findByUserId(appUser.getId());
			if (conversionHistoryToDelete != null) {
				daoConversionHistory.delete(conversionHistoryToDelete);
			}
			AppUserEntity appUserToDelete = daoAppUser.findById(appUser.getId());
			daoAppUser.delete(appUser);

			RequestContext.getCurrentInstance().update("tabView:idAppUsersTable");
			RequestContext.getCurrentInstance().update("tabView:idDisplayAppUsersGrowl");
		} catch (Exception exc) {
			exc.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
					"Error removing user: " + appUser.getFullName() + " Error: " + exc.toString()));
		}
	}

	public LazyDataModel<AppUserEntity> getLazyModel() {
		return lazyModel;
	}

	public void setLazyModel(LazyDataModel<AppUserEntity> lazyModel) {
		this.lazyModel = lazyModel;
	}

	public LazyDataModel<ConversionHistoryEntry> getLazyModelDownloadHistory() {
		return lazyModelDownloadHistory;
	}

	public void setLazyModelDownloadHistory(LazyDataModel<ConversionHistoryEntry> lazyModelDownloadHistory) {
		this.lazyModelDownloadHistory = lazyModelDownloadHistory;
	}

	public AppUserEntity getCurrentlyViewedUser() {
		return currentlyViewedUser;
	}

	public void setCurrentlyViewedUser(AppUserEntity currentlyViewedUser) {
		this.currentlyViewedUser = currentlyViewedUser;
	}

	public boolean isDisplayInternalUserHistory() {
		return displayInternalUserHistory;
	}

	public void setDisplayInternalUserHistory(boolean displayInternalUserHistory) {
		this.displayInternalUserHistory = displayInternalUserHistory;
	}

	public double loadUserBalance() {
		double balanceResult = 0;
		try {
			WalletDataEntity walletDataEntity = daoWalletData.findByUserId(this.currentlyViewedUser.getId());
			balanceResult = walletDataEntity.getBalance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return balanceResult;
	}

	public int loadUserAvailableSpins() {
		int userSpins = 0;
		try {
			SpinnerDataEntity spinnerDataEntity = daoSpinnerData.findByUserId(this.currentlyViewedUser.getId());
			if (spinnerDataEntity != null) {
				userSpins = spinnerDataEntity.getAvailableUses();
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		return userSpins;
	}
	
	public String loadUserLastDailyBonusTime(){
		String lastBonusTime = "";
		try{
			SpinnerDataEntity spinnerDataEntity = daoSpinnerData.findByUserId(this.currentlyViewedUser.getId());
			if (spinnerDataEntity != null) {
				lastBonusTime = spinnerDataEntity.getLastDailyBonus().toString();
			}
		 } catch (Exception exc){
			 exc.printStackTrace();
		 }
		return lastBonusTime;
	}
	
	

	public EventQueueTableDataModelBean loadEventQueueTable() {
		try {
			logger.info("Loading event queue table...");
			if (this.currentlyViewedUser != null) {
				List<EventQueueEntity> queueEventList = daoEventQueue.findAllByUserId(currentlyViewedUser.getId());
				if (queueEventList != null) {
					logger.info("Returining event queue table with" + queueEventList.size());
					return new EventQueueTableDataModelBean(queueEventList);
				}
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return null;
	}

	private void showAlert(String title, String message) {
		RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_INFO, title, message));
	}

	public void sendEvent(EventQueueEntity event) {
		try {
			boolean status = false;
			logger.info("Pushing queued event: " + event.getId() + " eventId:" + event.getEventId() + " for user:" + event.getPhoneNumber()
					+ " userId:" + event.getUserId());
			UserEventEntity userEvent = daoUserEvent.findById(event.getEventId());
			if (event != null) {
				status = eventQueueManager.validateEventPushToMode(userEvent);
			}
			if (status) {
				RealmEntity realm = daoRealm.findById(loginBean.getUser().getRealm().getId());
				rewardManager.issueReward(realm, userEvent, null, false);
				showAlert("Success", "Event has been send successfully.");
			} else
				showAlert("Failed", "Event has been not send successfully.");

		} catch (Exception exc) {
			showAlert("Error", "Error occured:" + exc.getMessage());
			exc.printStackTrace();
		}
		RequestContext.getCurrentInstance().update("tabView:tabAppUsersData");

	}

}
