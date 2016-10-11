package fr.protogen.importData;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.protogen.masterdata.dbutils.DBUtils;
import fr.protogen.masterdata.dbutils.ProtogenConnection;



public class DAL {
	
private static DAL instance;
	
	
	private DAL() {
		super();
		// TODO Auto-generated constructor stub
	}
	public static synchronized DAL getInstance(){
		if(instance==null){
			instance=new DAL();
		}
		return instance;
	}
	
	public CheckStatus dataSave(DataStructure data){
		CheckStatus statu = new CheckStatus();
		boolean flag=false;
		List<DataStructureTable> datatables = data.getDataTable();
			try {
				Class.forName("org.postgresql.Driver");
				 Connection cnx = ProtogenConnection.getInstance().getConnection();
					Statement st = cnx.createStatement();
					Statement st2 = cnx.createStatement();
					
					int tabIndex=2;
					for (Iterator<DataStructureTable> iterator = datatables.iterator(); iterator
							.hasNext();) {
						DataStructureTable dataStructureTable = (DataStructureTable) iterator
								.next();

						//	Get the next value of the table sequence
						String sql="select nextval(?)";
						PreparedStatement ps = cnx.prepareStatement(sql);
						ps.setString(1, dataStructureTable.getProtogenTable()+"_seq");
						int idBase=0;
						ResultSet rs = ps.executeQuery();
						if(rs.next())
							idBase= rs.getInt(1)+1;
						
						//	Insert rows of current table
						List<String> colonnesL=dataStructureTable.getHeaders();
						String colones="";
						Map<String, String> foreignKeys = new HashMap<String, String>();
						for (int j = 0; j <colonnesL.size(); j++) {
							String str=colonnesL.get(j);
							try{
								if(str.split(" ").length>=2){
									str="'"+str+"'";
									
								}
								}catch(java.lang.ClassCastException e){
									
								}
							colones=colones+" "+str+",";
							if(str.startsWith("fk_")){
								foreignKeys.put(j+"", str.substring(3));
							}
						}
						String[] qsplit = colones.split(",");
						colones=qsplit[0];int t=qsplit.length-1;
						for (int j = 1; j <t ; j++) {
							colones=colones+","+qsplit[j];
						}
						
						colones=colones+","+qsplit[t];
						
						List<List<Object>> datas = dataStructureTable.getData();
						dataStructureTable.setKeys(new ArrayList<String>());
						if(datas!=null){
							int nbrdatas=datas.size();
							System.out.println("nbrdatas "+nbrdatas);
							for(int i1=0;i1<nbrdatas;i1++){
								
								List<Object> list = datas.get(i1);
								String query2="INSERT INTO "+dataStructureTable.getProtogenTable()+"("+colones+") VALUES (";
								int nbrlis=list.size();
								System.out.println("nbrlis "+nbrlis);
								boolean falseFlag=false;
								for (int j = 0; j <nbrlis; j++) {
									Object object=list.get(j);
									
									if(foreignKeys.containsKey(""+j)){
										int ligneIndex = Integer.parseInt(object.toString().split("\\.")[0]);
										String table = foreignKeys.get(""+j);
										falseFlag = true;
										for(DataStructureTable reft : data.getDataTable()){
											if(reft.getProtogenTable().equals(table)){
												falseFlag= false;
												object = reft.getKeys().get(ligneIndex-2);
												break;
											}
										}
										
										if(falseFlag){
											System.out.println("WARNING : la cle etrangere fk_"+table+" est introuvable");
											flag=true;
											statu.setDescription("Des lignes ont été ignorées, leurs clés étrangères n'ont pas été retrouvées");
											statu.setStatus(Status.WARNING);
										}
									}
									
									try{
										//if(((String) object).split(" ").length>=2)
											object="'"+object+"'";
										}catch(java.lang.ClassCastException e){
											
										}
										query2=query2+" "+object+",";
								}
								String[] query2split = query2.split(",");
								query2=query2split[0];int ii2=query2split.length-1;
								for (int j = 1; j <ii2 ; j++) {
									query2=query2+","+query2split[j];
								}
								
								query2=query2+","+query2split[ii2];
								query2=query2+ ")";
								
								if(!falseFlag){
									System.out.println(query2);
									st2.executeUpdate(query2);
									//	set primary key
									dataStructureTable.getKeys().add(""+idBase);
									idBase++;
								} 
								
							}
						}
						tabIndex++;
										
					}
				
					
					st2.close();
						
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				statu.setStatus(Status.FATAL);
				statu.setDescription("ECHEC DE L'IMPORTATION");
				statu.setStackTrace(e.getMessage());
				return statu;
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				statu.setStatus(Status.FATAL);
				statu.setDescription("ECHEC DE L'IMPORTATION");
				statu.setStackTrace(e.getMessage());
				return statu;
			}
			
			if(!flag){
			   	statu.setDescription("IMPORTATION EFFECTUEE AVEC SUCCES");
			   	statu.setStatus(Status.INFO);
			}
		   	return statu;
		
	}
}
