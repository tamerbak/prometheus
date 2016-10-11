package fr.protogen.engine.utils;

public class ProtogenKeyGenerator {
	
	private static ProtogenKeyGenerator instance=null;
	public synchronized static ProtogenKeyGenerator getInstance(){
		if(instance==null)
			instance=new ProtogenKeyGenerator();
		return instance;
	}
	private ProtogenKeyGenerator(){}
	
	public static enum Mode {
		ALPHA, ALPHANUMERIC, NUMERIC 
	}

	public String generateRandomString(int length, Mode mode) throws Exception {

		StringBuffer buffer = new StringBuffer();
		String characters = "";

		switch(mode){
		
		case ALPHA:
			characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
			break;
		
		case ALPHANUMERIC:
			characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
			break;

		case NUMERIC:
			characters = "1234567890";
			break;
		}
		
		int charactersLength = characters.length();

		for (int i = 0; i < length; i++) {
			double index = Math.random() * charactersLength;
			buffer.append(characters.charAt((int) index));
		}
		return buffer.toString();
	}	
	
	public String generateKey(){
		
		try {
			return generateRandomString(10, Mode.ALPHANUMERIC);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}
	public String generateKey(int i) {
		// TODO Auto-generated method stub
		try {
			return generateRandomString(5, Mode.ALPHANUMERIC);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}	}
}
