package fr.protogen.engine.utils;

import java.util.HashMap;
import java.util.Map;

import fr.protogen.masterdata.model.CoreUser;

public class ApplicationRepository {
	private static ApplicationRepository instance = null;
	private Map<String, ApplicationCache> registery;
	
	public static synchronized ApplicationRepository getInstance(){
		if(instance == null)
			instance = new ApplicationRepository();
		return instance;
	}
	
	private ApplicationRepository(){
		registery = new HashMap<String, ApplicationCache>();
	}
	
	public synchronized ApplicationCache buildCache(String appKey, String sessionKey, CoreUser user){
		ApplicationCache cache = new ApplicationCache(appKey,user);
		registery.put(sessionKey, cache);
		return cache;
	}
	
	public synchronized ApplicationCache getCache(String skey){
		if(registery.containsKey(skey))
			return registery.get(skey);
		return null;
	}

	public void terminate(String key) {
		// TODO Auto-generated method stub
		if(registery.containsKey(key))
			registery.remove(key);
	}
}
