package fr.protogen.importData;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;



public class ExcelImportManager implements DataFormatDriver {

	private DAL dal = DAL.getInstance();
	private DataStructure data=new DataStructure();
	private static ExcelImportManager instance;
	
	
	private ExcelImportManager() {
		super();
		// TODO Auto-generated constructor stub
	}
	public static ExcelImportManager getInstance(){
		if(instance==null){
			instance=new ExcelImportManager();
		}
		return instance;
	}
	public List<String> types(String filename,int i){
			POIFSFileSystem fs;
			List<String> types=null;
			try {
				System.out.println("try");
				fs = new POIFSFileSystem(new FileInputStream(
						filename));
				HSSFWorkbook wb = new HSSFWorkbook(fs);
				Row row = null;
				HSSFSheet sheet = wb.getSheetAt(i);
				int nbrCol=0;
					if(sheet.getRow(0)!=null){
						for (Iterator<Cell> cellIt =  sheet.getRow(0).cellIterator(); cellIt.hasNext();) {
							cellIt.next();
							nbrCol=nbrCol+1;
							System.out.println(nbrCol);
						}
					}
						if(sheet.getRow(1)==null){
							return null;
						}
						else{
							 row=sheet.getRow(1);
						}
				types=new ArrayList<String>(nbrCol);
				int j=1,j1=1;
							for (int i1=0;i1<nbrCol;i1++) {
								Cell cell = null;
								if(sheet.getRow(j).getCell(i1)!=null){
								cell=sheet.getRow(j).getCell(i1);	}else{
									while(cell==null){
										j1=j1+1;
									cell=sheet.getRow(j1).getCell(i1);
									}
								}	
								if(cell.getCellType() == HSSFCell.CELL_TYPE_ERROR) {
									return null;}
								else if (cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
									return null;
								} else if (cell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN) {
									System.out.println(cell.getBooleanCellValue());
									types.add(i1, "boolean");
								} else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
									System.out.println(cell.getNumericCellValue());
									types.add(i1,"real");
								} else if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
									types.add(i1,"VARCHAR");
									System.out.println(cell.getStringCellValue());

								}
								else {
									System.out.println(cell);									
								}
						}
							return types;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		
		
		return types;
	}
	
	public List<String> headers(String filename,int i){
		POIFSFileSystem fs;
		List<String> headers=null;
		try {
			fs = new POIFSFileSystem(new FileInputStream(
					filename));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			Cell cell = null;Row row = null;
			HSSFSheet sheet = wb.getSheetAt(i);
			System.out.println("name : "+sheet.getSheetName());
			for (Iterator<Row> rowIt = sheet.rowIterator(); rowIt.hasNext();) {
					if(sheet.getRow(0)==null){
						return null;
					}else{
						 row=sheet.getRow(0);
						 break;
					}
					}
			headers=new ArrayList<String>();
						for (Iterator<Cell> cellIt =  row.cellIterator(); cellIt.hasNext();) {
							cell = cellIt.next();
							if(cell==null) return null;
							if(cell.getCellType() == HSSFCell.CELL_TYPE_ERROR) {
								return null;}
							else if (cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
								return null;
							} else{
							if (cell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN) {
								//headers.add(cell.getBooleanCellValue());
								return null;
							} else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
								//headers.add(cell.getNumericCellValue());
								return null;
							} else if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
								headers.add(cell.getStringCellValue());

							}}
					}
						return headers;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	
	
	return headers;
}
	
	@Override
	public CheckStatus chechFormat(String filename) {
		// TODO Auto-generated method stub
		CheckStatus statu = new CheckStatus();
		CharsetEncoder encoder = Charset.forName("utf-8").newEncoder();
		try {
			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(
					filename));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			
			for (int i = 0; i < wb.getNumberOfSheets(); i++) {
				HSSFSheet sheet = wb.getSheetAt(i);
				if(sheet.getRow(0)==null){
					statu.setDescription("Une erreur est survenue : erreur dans une feuille , header non existant ");
					statu.setStackTrace("stackTrace : header non existant\nVeuillez inserer un header à la feuille "+sheet.getSheetName());
					
					statu.setStatus(Status.FATAL);
					
					return statu;
				}
				if(sheet.getRow(0)!=null && this.headers(filename, i)==null){
					statu.setDescription("Une erreur est survenue : erreur dans une feuille , header non conforme ");
					statu.setStackTrace("stackTrace : header non existant\nVeuillez inserer un header conforme à la feuille "+sheet.getSheetName());
					statu.setStatus(Status.FATAL);
					return statu;
				}
				int numLigne = 0, numColonne = 0;
				for (Iterator<Row> rowIt =  sheet.rowIterator(); rowIt.hasNext();) {
					Row row = null;
					row = rowIt.next();
					numLigne = numLigne + 1;
					for (Iterator<Cell> cellIt =  row
							.cellIterator(); cellIt.hasNext();) {
						Cell cell = null;
						cell = cellIt.next();
						numColonne = numColonne + 1;
						Object value = null;
						if (cell == null) {
							value = "";
						} else if (cell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN) {
							value = cell.getBooleanCellValue();
						} else if (cell.getCellType() == HSSFCell.CELL_TYPE_ERROR) {
							value = cell.getErrorCellValue();
							statu.setDescription("Une erreur est survenue : erreur dans une cellule , la table n'est pas chargée ");
							statu.setStackTrace("stackTrace : erreur dans une cellule\n"
									+ "Ligne : "
									+ numLigne
									+ " Colonne : "
									+ numColonne
									+ " Dans la feuille  :"
									+ sheet.getSheetName());
							statu.setStatus(Status.ERROR);
							return statu;

						} else if (cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
							value = cell.getCellFormula();
						} else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
							value = cell.getNumericCellValue();
						} else if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
							String value1 = cell.getStringCellValue();
						
						if (!encoder.canEncode( value1)) {
								statu.setDescription("Un encodage non UTF-8 est détecté");
								statu.setStackTrace("stackTrace : L'erreur est survenue à cause d'un encodage non UTF-8 \n"
										+ "Ligne : "
										+ numLigne
										+ " Colonne : "
										+ numColonne
										+ " Dans la feuille  :" + sheet.getSheetName());
								statu.setStatus(Status.WARNING);
								return statu;
							}
						}

					}
				}
			}
		} catch (FileNotFoundException e) {
			// e.printStackTrace();
			statu.setDescription("fichier introuvable d'où aucune table n'est chargée");
			statu.setStackTrace("stackTrace : " + e.getMessage() + " \n"
					+ "Cause : " + e.getCause());
			statu.setStatus(Status.FATAL);
			return statu;
		} catch (IOException e) {
			// e.printStackTrace();
			statu.setDescription("fichier illisible d'où aucune table n'est chargée");
			statu.setStackTrace("stackTrace : " + e.getMessage() + " \n"
					+ "Cause : " + e.getCause());
			statu.setStatus(Status.FATAL);
			return statu;
		}

		statu.setDescription("Validation avec succés");
		statu.setStackTrace(null);
		statu.setStatus(Status.INFO);
		return statu;
	}

	@Override
	public DataStructure importData(String filename) {
		// TODO Auto-generated method stub
		
		List<DataStructureTable> dataTables=new ArrayList<DataStructureTable>();
		POIFSFileSystem fs;
		// if(this.chechFormat(filename).getStackTrace()!=null)
		try {
			fs = new POIFSFileSystem(new FileInputStream(filename));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			HSSFSheet sheet;
			
			for (int i = 0; i < wb.getNumberOfSheets(); i++) {
				List<List<Object>> datas = new ArrayList<List<Object>>();
				Row row = null;
				Cell cell = null;
				List<String> typesData = new ArrayList<String>();
				sheet = wb.getSheetAt(i);
				DataStructureTable datatable = new DataStructureTable();
				System.out.println(datatable);
				datatable.setProtogenTable(sheet.getSheetName());
				datatable.setHeaders(this.headers(filename, i));
				datatable.setTypes(this.types(filename, i));
				System.out.println("type");
				int nrow=0;
				for (Iterator<Row> rowIt =  sheet
						.rowIterator(); rowIt.hasNext();) {
					List<Object> ligne = new ArrayList<Object>();							
					nrow=nrow+1;
					System.out.println("row : "+nrow);
					row = rowIt.next();
					if(nrow!=1){
					for (Iterator<Cell> cellIt =  row
							.cellIterator(); cellIt.hasNext();) {
						cell = cellIt.next();
						if (cell == null) {
							ligne.add("");
						} else if (cell.getCellType() == HSSFCell.CELL_TYPE_ERROR) {
						} else if (cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
							typesData.add("formule");
							ligne.add(cell.getCellFormula());
						} else{
							if (cell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN) {
							ligne.add((cell.getBooleanCellValue()));
							typesData.add("boolean");
						} else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
							typesData.add("real");
							String num = cell.getNumericCellValue()+"";
							if(num.split("\\.").length>1){
								num = num.split("\\.")[0];
							}
							ligne.add(num);
						} else if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
							typesData.add("String");
							ligne.add((cell.getStringCellValue()));
						}
						}
					}
					datas.add(ligne);					
					}
					datatable.setData(datas);	
				}
				dataTables.add(datatable);
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		data.setDataTable(dataTables);
		return data;
	}
	public DAL getDal() {
		return dal;
	}
	public void setDal(DAL dal) {
		this.dal = dal;
	}

}
