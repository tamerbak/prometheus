package fr.protogen.importData;



public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DAL dt=DAL.getInstance();
		ExcelImportManager dtim=ExcelImportManager.getInstance();
		CheckStatus check = dtim.chechFormat("C:\\Users\\samsug\\Desktop\\testpays.xls");
		if(check.stackTrace==null){
			System.out.println("stack null");
		DataStructure data=dtim.importData("C:\\Users\\samsug\\Desktop\\testpays.xls");
		dt.dataSave(data);
		}else{
			System.out.println(check.getStackTrace());
			/*System.out.println(check.getDescription());
			System.out.println(check.getStackTrace());
			System.out.println(check.getStatus().getERROR()+check.getStatus().getFATAL()+check.getStatus().getWARNING());*/
		}
			// dt.chechFormat("C:\\Users\\HANANE\\Desktop\\AccountsFROpenbravoV2.xls");
			
	}

}
