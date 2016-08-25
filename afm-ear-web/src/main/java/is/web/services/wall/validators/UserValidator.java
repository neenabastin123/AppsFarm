package is.web.services.wall.validators;

import java.util.HashMap;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import is.ejb.bl.business.RespCodesEnum;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.entities.AppUserEntity;
import is.web.services.APIValidator;

@ManagedBean
public class UserValidator implements APIValidator {

	@Inject
	private DAOAppUser daoAppUser;

	@Override
	public boolean validate(HashMap<String, Object> parameters) {
		try {
			if (parameters.containsKey("userId")) {
				String userId = (String) parameters.get("userId");
				if (userId != null && userId.length() > 0 && getUserWithId(userId) != null) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			return false;
		}
	}

	private AppUserEntity getUserWithId(String userId) {
		try {
			return daoAppUser.findById(Integer.valueOf(userId));
		} catch (Exception exc) {
			exc.printStackTrace();
			return null;
		}
	}

	@Override
	public RespCodesEnum getInvalidValueErrorCode() {
		return RespCodesEnum.ERROR_INVALID_USER;
	}

}
