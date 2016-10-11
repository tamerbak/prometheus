package fr.protogen.engine.control;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.event.ValueChangeEvent;

import fr.protogen.masterdata.DAO.CountryDAO;
import fr.protogen.masterdata.model.Country;

@ManagedBean(eager=true)
@ApplicationScoped
public class CountryManager implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3498675273255031611L;

	/**
	 * 
	 */
	
	private List<Country> countries;
	private Map<String,Integer> countriesPhoneCode;
	
	private CountryDAO countryDao;
	
	@PostConstruct
	public void initialize() {
		
	countryDao=new CountryDAO();
	countries=countryDao.getAllCountries();
	
	
	}
	
	public List<Country> getCountries() {
		return countries;
	}

	public void setCountries(List<Country> countries) {
		this.countries = countries;
	}
	
	
	

}