package is.web.services.user.validators;

import java.util.HashMap;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import is.ejb.bl.business.RespCodesEnum;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.entities.AppUserEntity;
import is.web.services.APIValidator;

@ManagedBean
public class UsernameDBValidator implements APIValidator {

	@Inject
	private DAOAppUser daoAppUser;

	@Override
	public boolean validate(HashMap<String, Object> parameters) {
		try {
			String username = (String) parameters.get("username");
			AppUserEntity appUser = daoAppUser.findByUsername(username);
			if (appUser == null){
				return true;
			} else {
				return false;
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			return false;
		}
	}

	@Override
	public RespCodesEnum getInvalidValueErrorCode() {
		return RespCodesEnum.ERROR_USER_UNDER_GIVEN_USERNAME_ALREADY_REGISTERED;
	}

}
